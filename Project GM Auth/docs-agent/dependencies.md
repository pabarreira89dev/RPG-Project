---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Dependencies and integration contracts for Project GM Auth.
last_reviewed: null
---

# Project GM Auth — Dependencies

## Runtime dependencies
| Dependency | Role | Notes |
|------------|------|-------|
| MySQL | Persists `app_user` only | Same MySQL80 local service as "Project GM", separate database (`project_gm_auth_local`) |
| Flyway | Schema migrations | Single migration so far: `V1__create_app_user.sql` |
| `spring-boot-starter-oauth2-authorization-server` | Issues JWTs, hosts AS endpoints | In-memory `RegisteredClientRepository`/`OAuth2AuthorizationService` — see `architecture.md` |

## Consumers (who depends on this repo)
| Consumer | Integration | Notes |
|----------|-------------|-------|
| "Project GM" (`cloud` profile only) | Fetches this service's OIDC/OAuth2 discovery document + JWKS at runtime to validate JWT signatures (`JwtDecoders.fromIssuerLocation`); validates `aud` claim against its own configured audience | No direct HTTP call between the two services besides that metadata/JWKS fetch — "Project GM" never calls this service's `/oauth2/token`. Zero code coupling: the contract is entirely "issuer URI + audience string must match" (env var values), see `env-and-config.md`. |
| Project GM Android (`cloud` flavor only) | Drives the custom `password` grant directly against `/oauth2/token` (native login form, no browser), and `grant_type=refresh_token` to renew | Plain OkHttp form-POST calls (`CloudAuthSessionManager`), registered as the single client in `AuthorizationServerConfig`; client secret must match on both sides, see `env-and-config.md` |

## Not a dependency of / on
- "Project GM automatics" (the E2E suite) does not exercise this service at all yet — no browser/PKCE
  simulation exists there (see `docs/repos/index.md` for the current dependency graph and this scope
  exclusion).
- This service does not call "Project GM"'s API, and has no knowledge of sessions/characters/etc.
