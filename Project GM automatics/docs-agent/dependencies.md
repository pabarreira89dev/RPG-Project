---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Dependencies and impact map for Project GM automatics.
last_reviewed: null
---

# Project GM automatics — Dependencies

## Internal (within this workspace)
| Depends on | Direction | Why |
|------------|-----------|-----|
| `Project GM` | this repo depends on it (hard runtime requirement) | Every scenario calls its HTTP API (`http://localhost:8080/project_gm`); `AppLifecycle` also stops/relaunches its real OS process. This repo cannot run at all without "Project GM" already running locally. |

No repo in this workspace depends on `Project GM automatics` — it is a leaf, test-only project.

## External dependencies
| Dependency | Used for | Notes |
|------------|----------|-------|
| A locally running "Project GM" instance | System under test | Must be started manually first (`mvn spring-boot:run` in `Project GM/`, `local` profile, `.env.local` sourced) — this suite never starts it on its own except mid-scenario via `AppLifecycle.restart()`. |
| Cucumber / JUnit Platform | Test execution engine | `RunCucumberTest` `@Suite` |
| REST Assured | HTTP client + assertions | Needs `jackson-databind` on the test classpath to serialize `Map` request bodies |

## Impact map — "if I change X in Project GM, check Y here"
| Change in `Project GM` | Also check in this repo |
|--------|-----------|
| Request/response DTO shape on any endpoint | `GameSessionSteps.java` (body construction + JSON path assertions) |
| Auth mechanism / header name (`X-Dev-Player-Id`) | `GameSessionSteps.java` request builders |
| Flyway seed data for locations/NPCs (`V3`/`V4` migrations) | `LocationCatalog.java` / `NpcCatalog.java` — fixed UUIDs must match exactly |
| `/actuator/shutdown` or `/actuator/health` behavior, or whether they're exposed in `local` | `AppLifecycle.java` |
| Context path or port | `ApiConfig.BASE_URI` |
| New endpoint that should be part of the full journey | `recorrido_completo.feature` (and possibly its own standalone `.feature`, see `testing.md`) |

## Impact map — the other direction
Changes in this repo (new/changed scenarios or steps) never require changes in "Project GM" — this repo
only reads/exercises its behavior, it never dictates its API shape.
