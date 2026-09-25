---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: How to run tests and manually verify the OAuth2 custom "password" grant flow for Project GM Auth.
last_reviewed: null
---

# Project GM Auth — Testing

## Automated tests
Only `AppUserServiceImplTest` exists so far (Mockito, standard style — service constructed inside each
`@Test`, not as a field initializer, matching the convention documented for "Project GM"'s own
`*ServiceImplTest` classes). No integration test exercises the Authorization Server endpoints themselves
yet (see `dependencies.md`'s "not a dependency of" section).

Run with `mvn clean test` (always `clean` — a plain `mvn test` can reuse a stale `target/classes` and
produce misleading unrelated failures; this bit "Project GM" repeatedly, see its own
`docs-agent/testing.md` if this repo ever hits the same thing).

## Manual end-to-end verification (no automated coverage yet)
1. Start this service locally (`local` profile, MySQL `project_gm_auth_local` must exist and have the
   `V1` migration applied — Flyway applies it automatically on startup).
2. Confirm discovery: `GET http://localhost:9000/.well-known/oauth-authorization-server` and
   `GET http://localhost:9000/oauth2/jwks` both respond.
3. Register a user via the JSON API (what the Android app actually calls):
   `POST http://localhost:9000/api/v1/register` with JSON body `{"username":"...","password":"...","email":"..."}`
   → `201`. Repeating with the same username → `409 USERNAME_ALREADY_EXISTS`.
4. Get a token via the custom password grant:
   `POST http://localhost:9000/oauth2/token` with form body
   `grant_type=password&client_id=project-gm-android&client_secret=<auth.android-client-secret>&username=...&password=...&scope=game.api`
   → `200` JSON `{access_token, refresh_token, scope, token_type, expires_in}`.
5. Decode the returned JWT (e.g. jwt.io) and confirm: `sub` equals the registered user's `app_user.id`
   (check the MySQL row), `aud` equals the configured `auth.audience`, `iss` equals `auth.issuer-uri`.
6. Exchange the returned `refresh_token` at the same endpoint
   (`grant_type=refresh_token&client_id=...&client_secret=...&refresh_token=...`) and confirm a new access
   token comes back. If this specifically fails with `principal cannot be null` server-side, see
   `architecture.md`'s note about `PasswordGrantAuthenticationProvider` needing the `Principal` attribute
   attached to the `OAuth2Authorization` it builds.
7. Wrong password → `401` with an OAuth2 error body (`invalid_grant`).

See [`Project GM/docs-agent/testing.md`](../../Project%20GM/docs-agent/testing.md) for the follow-up step
of using the JWT from step 5 against "Project GM"'s `cloud` profile.

## Android app gotcha this flow already surfaced once
`CloudAuthSessionManager`'s `OkHttpClient` must NOT follow redirects automatically
(`.followRedirects(false).followSslRedirects(false)`) — this is a plain JSON/form REST API that should
never legitimately redirect; if it ever does (misconfiguration on this side), OkHttp's default behavior of
transparently chasing redirects can hit its hard-coded follow-up limit and throw a confusing
`Too many follow-up requests: 21` instead of surfacing a clear HTTP status to the user.
