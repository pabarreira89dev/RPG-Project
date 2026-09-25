---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Repo-specific non-obvious coding patterns for Project GM Auth.
last_reviewed: null
---

# Project GM Auth — Coding Patterns

## Entity/repository style matches "Project GM"
`AppUser` follows the exact same convention as "Project GM"'s entities: `@Entity`/`@Getter`/
`@NoArgsConstructor(access = PROTECTED)`/`@AllArgsConstructor`, no public setters, `@GeneratedValue(strategy
= GenerationType.UUID)` for the id, constructed as `new AppUser(null, ...)` and left to Hibernate to fill
the id on save (see `pab.rpg.auth.service.impl.AppUserServiceImpl.register`). Repository interfaces live
under `domain.repository`, separate from the entity package — same split as "Project GM".

## Two `@Order`ed `SecurityFilterChain` beans, not one
`AuthorizationServerConfig`'s chain (`@Order(1)`) matches only the AS endpoints via
`OAuth2AuthorizationServerConfigurer.getEndpointsMatcher()`; `DefaultSecurityConfig`'s chain (`@Order(2)`,
implicitly lower priority) matches everything else. This is the standard Spring Authorization Server
layout (see Spring's own "getting started" sample) — do not merge them into one chain, the AS endpoints
need `.oidc(...)`/the custom grant's `tokenEndpoint(...)` applied via `http.oauth2AuthorizationServer(...)`,
which only makes sense on the matcher-scoped chain.

## Custom OAuth2 grant types: Token + Converter + Provider, registered on the token endpoint
`PasswordGrantAuthenticationToken` (extends `OAuth2AuthorizationGrantAuthenticationToken`, defines the
grant type constant), `PasswordGrantAuthenticationConverter` (`AuthenticationConverter`, parses form
params, returns `null` if `grant_type` doesn't match — required so the framework's default converters
still handle other grants on the same endpoint), `PasswordGrantAuthenticationProvider`
(`AuthenticationProvider`, does the actual work) — this is the standard Spring Authorization Server
extension pattern for a grant type it doesn't ship out of the box, registered via
`tokenEndpoint.accessTokenRequestConverter(new PasswordGrantAuthenticationConverter()).authenticationProvider(new PasswordGrantAuthenticationProvider(...))`
in `AuthorizationServerConfig`. The provider must reuse the exact same `OAuth2TokenGenerator` bean the
framework's built-in grants use (constructor-injected, not re-created) so `TokenCustomizerConfig`'s
`sub`/`aud` override applies uniformly regardless of which grant issued the token.

## CSRF is per-request-matcher, not implied by `permitAll()`
`/api/v1/register` needed both `.requestMatchers("/api/v1/register").permitAll()` (authorization) AND
`.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/register"))` (a separate filter) in
`DefaultSecurityConfig` — `permitAll` alone still left the endpoint behind Spring Security's default CSRF
protection, which rejects a session-less POST from a native app with no CSRF token to send.

## The token customizer looks up `AppUser` by username, not by id
`TokenCustomizerConfig` receives `context.getPrincipal().getName()` — this is the Spring Security
authentication name, which is the **username** (from `UserDetailsService`), not the account's UUID. It
re-queries `AppUserRepository.findByUsername(...)` to get the UUID to put in `sub`. If a future change
makes the principal carry the `AppUser` object directly (e.g. a custom `UserDetails` implementation),
this lookup can be simplified — but don't assume `getName()` is ever the UUID, today it never is.
