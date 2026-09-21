---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: End-to-end verification & real-restart persistence — the one functionality that spans both repos.
last_reviewed: null
---

# End-to-end Verification & Real-restart Persistence

> See also: functionality/index.md for the full catalog.

## What it does

Verifies, as a black box over real HTTP, that a full player journey (create session → action → combat →
quest) behaves correctly, and — uniquely among this product's functionalities — that game state actually
persists in MySQL rather than only living in memory: the suite stops and relaunches the real "Project GM"
process mid-scenario and confirms the session/character/relationship/quest state survives the restart.

## Repos involved

| repo | role / contribution | key entry points |
|------|-------------------|-----------------|
| Project GM | provider — exposes the game API plus `/actuator/shutdown` (local profile only) so the suite can stop it, and `/actuator/health` so the suite can detect when it's back up | `GameSessionController`, `ActionController`, `CombatController`, `QuestController`, `NpcController`; `management.endpoint.shutdown` in `application-local.yml` |
| Project GM automatics | initiator — drives the scenario over HTTP and controls the "Project GM" process lifecycle | `recorrido_completo.feature`, `GameSessionSteps.java`, `com.pabarreira.tests.support.AppLifecycle` |

## Integration points

- `POST /actuator/shutdown` (must include `Content-Type: application/json` or it 415s) — how the suite
  stops "Project GM".
- `GET /actuator/health` — polled by the suite both to confirm shutdown and to confirm the relaunched
  process is back up (2 min timeout).
- `ProcessBuilder` relaunch of `mvn spring-boot:run` in `../Project GM` — not an HTTP integration, but a
  hard dependency: the relaunched process needs `Project GM/.env.local` sourced into the **same shell**
  running `mvn test`, since `ProcessBuilder` inherits env vars from that JVM, not from any other
  interactive terminal.
- All the normal game API endpoints (sessions/actions/combat/quests) are exercised as an ordinary client
  would use them — no special test-only endpoint besides the actuator ones above.

## How to add / modify

1. Add/extend the Gherkin scenario in `Project GM automatics/src/test/resources/features/recorrido_completo.feature`.
2. Add step definitions in `GameSessionSteps.java` if a new kind of assertion/request is needed (reuse
   existing steps where possible — they're already shared with the other single-endpoint feature files).
3. If the restart mechanics themselves need to change (timeouts, shutdown call, health polling), edit
   `AppLifecycle` — do not duplicate that logic in a step definition.
4. If a new endpoint/DTO shape is added to "Project GM" that this journey should cover, update both repos
   together (see `Project GM/docs-agent/dependencies.md` impact map).

## Related docs

- [`Project GM/docs-agent/testing.md`](../../Project%20GM/docs-agent/testing.md#e2e-project-gm-automatics)
- [`Project GM/docs-agent/env-and-config.md`](../../Project%20GM/docs-agent/env-and-config.md)
