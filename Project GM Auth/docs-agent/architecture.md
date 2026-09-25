---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Internal architecture and key decisions for Project GM Auth.
last_reviewed: null
---

# Project GM Auth — Architecture

## No browser involved — a custom "password" grant type
The Android app never opens a browser or Custom Tab. It drives a custom OAuth2 grant type
(`grant_type=password`, native username/password form) implemented the standard Spring Authorization
Server extension way: `PasswordGrantAuthenticationToken` (extends
`OAuth2AuthorizationGrantAuthenticationToken`, defines the grant type constant), `PasswordGrantAuthenticationConverter`
(parses `grant_type`/`username`/`password`/`scope` form params), `PasswordGrantAuthenticationProvider`
(validates the client, authenticates via the standard `AuthenticationManager`/`UserDetailsService`, then
generates tokens through the exact same `OAuth2TokenGenerator` as the built-in grants). All three live in
`pab.rpg.auth.security` and are registered in `AuthorizationServerConfig` via
`tokenEndpoint.accessTokenRequestConverter(...).authenticationProvider(...)`. This was a deliberate pivot
away from an earlier Authorization Code+PKCE/Custom Tabs implementation — testing on a real device showed
the browser's own cookie jar silently remembered the session across logins, and this Spring AS version
does not implement `prompt=login` to force re-authentication, so removing the browser was the only fix.

## Two security filter chains
`AuthorizationServerConfig` defines `@Order(1)` matching only the AS endpoints
(`OAuth2AuthorizationServerConfigurer.getEndpointsMatcher()`). OIDC discovery is enabled
(`.oidc(Customizer.withDefaults())`) — not for the Android app (which no longer does any discovery), but
because "Project GM"'s resource-server config resolves this issuer via `JwtDecoders.fromIssuerLocation`,
which looks up `/.well-known/openid-configuration` first; without OIDC enabled that path falls through to
the default filter chain's login page instead of 404, and `JwtDecoders` can't resolve the issuer at all.
`DefaultSecurityConfig` defines `@Order(2)` for everything else — the browser-facing `/login`/`/register`
pages (kept only for manual/admin testing, see below) plus the JSON `/api/v1/register` endpoint the app
actually uses, backed by a `UserDetailsService` that adapts `AppUserRepository` to Spring Security's `User`.

## The token customizer is the entire integration point with "Project GM"
`TokenCustomizerConfig`'s `OAuth2TokenCustomizer<JwtEncodingContext>` bean does two things Spring
Authorization Server does not do by default:
1. Overrides `sub` from the default (the authenticated username) to the account's own `AppUser.id`
   (UUID) — looked up by username at token-issuance time.
2. Adds an `aud` claim from `AuthProperties.audience()`.

This is the **only** place that matters for interop: "Project GM"'s `SecurityConfig.jwtDecoder()`
validates issuer + this exact `aud` claim, and its `CurrentPlayerArgumentResolver` reads `sub` as the
player's UUID — no code there needed to change, it already expected exactly this shape (see
[`Project GM/src/main/java/pab/rpg/security/SecurityConfig.java`](../../Project%20GM/src/main/java/pab/rpg/security/SecurityConfig.java)).

## Registered client: single, in-memory, client-secret authenticated
`AuthorizationServerConfig.registeredClientRepository()` returns an `InMemoryRegisteredClientRepository`
with exactly one client (the Android app): `ClientAuthenticationMethod.CLIENT_SECRET_POST` with a
`client_secret` embedded in the APK (`AuthProperties.androidClientSecret()`), grants
`PasswordGrantAuthenticationToken.PASSWORD` + `REFRESH_TOKEN`, `requireAuthorizationConsent(false)`
(first-party client). No `AUTHORIZATION_CODE` grant, no PKCE, no redirect URI — those were part of the
abandoned browser-based flow. Spring AS's only "no secret" client auth mechanism
(`PublicClientAuthenticationConverter`) is hardcoded to require PKCE and only makes sense for the
`authorization_code` grant, so a custom grant type needs a real (if not truly confidential — it ships
inside the APK) client credential instead. No dynamic client registration exists or is planned — adding a
second client means editing this bean.

## Deliberate MVP simplifications (in-memory AS state)
`OAuth2AuthorizationService`/`OAuth2AuthorizationConsentService` are Spring's in-memory defaults (never
overridden with a JDBC-backed implementation). Consequence: restarting this service invalidates every
outstanding authorization code **and refresh token** — every Android user must log in again. User
*accounts* (`AppUser` rows in MySQL) are unaffected. If this becomes a real pain point, the upgrade path
is a JDBC-backed `OAuth2AuthorizationService`/`RegisteredClientRepository` (Spring provides default SQL
schemas, need adapting to MySQL types the same way "Project GM"'s migrations were — see
[`Project GM/docs-agent/coding-patterns.md`](../../Project%20GM/docs-agent/coding-patterns.md)).

## Signing key management
`JwkConfig` builds the `JWKSource<SecurityContext>` bean Spring Authorization Server signs tokens with.
In `cloud`, an RSA keypair is loaded from PEM-format env vars (`AUTH_JWK_PRIVATE_KEY_PEM`/
`AUTH_JWK_PUBLIC_KEY_PEM`) so the signing key — and therefore every issued refresh token — survives a
restart. In `local`, no such env vars are set, so a fresh RSA keypair is generated every boot (acceptable:
the in-memory AS state above already forces re-login on every local restart regardless).

## Registration: JSON API only, no browser pages exist
`AuthApiController` serves `POST /api/v1/register` (JSON body, 201/409) — this is the **only** way to
create an account, and the only registration endpoint that exists. There is no Thymeleaf/HTML login or
registration page anywhere in this service (an earlier version had one, kept for manual testing after the
Android app stopped using it — it was removed entirely once confirmed unnecessary, per explicit product
decision that this service must never serve any browser-facing content or forms). `/api/v1/register` is
`permitAll()` **and** explicitly CSRF-exempted (`.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/register"))`
in `DefaultSecurityConfig`) — `permitAll` only bypasses authentication, not Spring Security's separate CSRF
filter, which would otherwise reject the app's session-less POST. Any unauthenticated request to a
protected endpoint anywhere in this service gets a plain `401` (`HttpStatusEntryPoint`), never an HTML
redirect — there is no `formLogin`/login page configured at all.

## `refresh_token` needs the principal attached, or it breaks
`PasswordGrantAuthenticationProvider` must build the `OAuth2Authorization` with
`.attribute(Principal.class.getName(), authentication)` — without it, `OAuth2RefreshTokenAuthenticationProvider`
throws `principal cannot be null` the first time a client tries to refresh. Easy to miss because the
initial password-grant token issuance works fine without it; only the refresh path breaks.
