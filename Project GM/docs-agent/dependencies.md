---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Internal/external dependencies and impact map for Project GM.
last_reviewed: null
---

# Project GM — Dependencies

## Internal (within this workspace)
| Depends on | Direction | Why |
|------------|-----------|-----|
| `Project GM automatics/` | consumed by (not a dependency of this repo) | Separate Maven project; drives this app's HTTP API as a black box (Cucumber + REST Assured). Restarts this app's real process (`AppLifecycle`, via `/actuator/shutdown` + relaunching `mvn spring-boot:run`) to verify persistence across restarts. Changing an endpoint's request/response shape or auth header here requires updating its step definitions (`GameSessionSteps.java`) too. |
| `Project GM Auth/` | depends on (`cloud` profile only) | This repo's `SecurityConfig.jwtDecoder()` fetches `Project GM Auth`'s OIDC/OAuth2 discovery document + JWKS to validate JWT signatures, and validates the `aud` claim it issues. No direct HTTP call between the two besides that metadata/JWKS fetch — this repo never calls `Project GM Auth`'s endpoints, and `local`/`test` don't use it at all (`DevelopmentIdentityFilter` instead). |

No other repo in this workspace depends on Project GM.

## External dependencies
| Dependency | Used for | Notes |
|------------|----------|-------|
| MySQL 8 (`mysql-connector-j`, `flyway-mysql`) | System of record, schema managed by Flyway (`V1`-`V8` under `src/main/resources/db/migration`) | Local dev DB `project_gm_local`, test DB `project_gm_test`, both need the `project_gm_user` role. No Postgres anymore (migrated 2026-09-20). |
| OpenAI Responses API (`POST {openai.base-url}/responses`) | Narration + free-text interpretation, only when `openai.enabled=true` | Isolated behind `MasterAdapter`/`OpenAiMasterAdapter`; disabled (stubbed) in `test`, opt-out in `local` via `OPENAI_ENABLED=false`. Network/parsing failures raise `AiUnavailableException` → 503 `OPENAI_UNAVAILABLE`. |
| JWT issuer (`cloud` only) | Authentication | `JWT_ISSUER_URI` + `JWT_AUDIENCE`; validated via `NimbusJwtDecoder` + `DelegatingOAuth2TokenValidator` (issuer + `aud` claim). The issuer is `Project GM Auth` (see above) — this repo does not run its own IdP. |
| Spring Boot Actuator | Health/metrics (+ `shutdown`, `local` only) | `/actuator/**` is `permitAll()` regardless of profile. |

## Events / integration points
This service has no message broker and no async events in/out — all integration is synchronous HTTP.
`GameEvent` (domain entity) is an **internal** append-only log of what happened in a session (e.g.
`RELATIONSHIP_CHANGED`), exposed read-only for diagnostics via `GET .../events`; it is not a published
event stream consumed by another repo.

## Impact map — "if I change X, check Y"
| Change | Also check |
|--------|-----------|
| Any request/response DTO shape, auth header, or new required field | `Project GM automatics/` step definitions and `.feature` files |
| A Flyway migration file already applied locally/in test | Never edit an already-applied `Vn__*.sql` in place — add a new `Vn+1__*.sql` (no shared/prod DB yet, but this is still the convention going forward) |
| `openai.*` config keys | All three `application-*.yml` profiles + `OpenAiProperties` record fields must stay in sync |
| A new `GameRule` | Nothing else — it's auto-picked up via `List<GameRule>` injection |
