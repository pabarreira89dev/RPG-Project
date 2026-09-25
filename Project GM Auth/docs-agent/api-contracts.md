---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Every HTTP endpoint Project GM Auth exposes — Authorization Server endpoints plus the JSON registration API the Android app uses.
last_reviewed: null
---

# Project GM Auth — API Contracts

## Standard Authorization Server endpoints (auto-configured, not hand-written)
| Endpoint | Purpose |
|----------|---------|
| `GET /.well-known/oauth-authorization-server` | OAuth2 Authorization Server metadata (RFC 8414) |
| `GET /.well-known/openid-configuration` | OIDC discovery document — enabled so "Project GM"'s `JwtDecoders.fromIssuerLocation` can resolve this issuer (see `architecture.md`); the Android app does not use it |
| `POST /oauth2/token` | Token endpoint — `grant_type=password` (custom, native login) or `grant_type=refresh_token` |
| `GET /oauth2/jwks` | JWK Set — public key(s) used to verify issued JWT signatures |
| `POST /oauth2/revoke` | Token revocation |

Only one client may use these: `client_id=project-gm-android` (`CLIENT_SECRET_POST`, see
`architecture.md`). `/oauth2/authorize` exists (standard AS wiring) but is unused — there is no
`authorization_code` grant registered for this client. Requested scope in practice: `game.api` (custom,
meaningless to Spring itself — just what "Project GM" could check if it ever adds scope enforcement,
which it does not today).

## Custom "password" grant (native login — no browser)
`POST /oauth2/token` with form body:
```
grant_type=password&client_id=project-gm-android&client_secret=...&username=...&password=...&scope=game.api
```
→ `200` JSON `{access_token, refresh_token, scope, token_type, expires_in}` (same shape as any other
Spring AS token response). Invalid credentials → `401` with an OAuth2 error body (`invalid_grant`).
Refreshing: same endpoint, `grant_type=refresh_token&client_id=...&client_secret=...&refresh_token=...`.

## Custom endpoints
| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `POST /api/v1/register` | POST | Public (permitAll + CSRF-exempted) | JSON account creation — what the Android app's native registration form calls. Body `{username, password, email}` → `201` empty body, or `409 {"code":"USERNAME_ALREADY_EXISTS","message":"..."}` |

This service exposes no browser-facing pages of any kind — no `/login`, no `/register` form, nothing that
renders HTML. Every endpoint (Authorization Server ones and this custom one) returns JSON, and any
unauthenticated request to a protected endpoint gets a plain `401` instead of a redirect.

## Claims contract of the issued JWT (the part "Project GM" actually relies on)
| Claim | Value | Consumed by |
|-------|-------|-------------|
| `sub` | The registering user's own generated UUID (`app_user.id`) — **not** the username | "Project GM"'s `CurrentPlayerArgumentResolver` → this becomes `playerId` everywhere |
| `aud` | The configured `auth.audience` value | "Project GM"'s `SecurityConfig.jwtDecoder()` audience validator |
| `iss` | The configured `auth.issuer-uri` value | Implicitly validated by `JwtDecoders.fromIssuerLocation`'s default issuer validator |
| `preferred_username` | The account's username | Not consumed anywhere yet — informational only |

No other claims are guaranteed or relied upon. There is no `roles`/`scope`-based authorization anywhere
in "Project GM" today — authentication (who is this UUID) is the entire contract, not authorization.

