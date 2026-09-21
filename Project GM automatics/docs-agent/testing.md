---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Feature files, run commands and verification workflow for Project GM automatics.
last_reviewed: null
---

# Project GM automatics — Testing

> This repo IS a test suite (E2E/API black-box for "Project GM") — there are no "unit tests of the
> tests". This file describes how to run and extend it, not tiers within it.

## Feature files (`src/test/resources/features/`)
| File | Covers |
|------|--------|
| `sesiones_de_juego.feature` | Create/get/list sessions: happy path, wrong-owner → 404, unknown id → 404, listing with several sessions |
| `acciones.feature` | `POST .../actions`: free-text exploration action, idempotent replay (same `idempotencyKey`), stale `expectedVersion` → 409, social action → `RELATIONSHIP_CHANGED` event |
| `combate.feature` | Start combat (valid + NPC out of location → 422 `COMBAT_NOT_ALLOWED`), attack with no active combat → 404 `COMBAT_NOT_FOUND`, a full standalone combat without an app restart |
| `misiones.feature` | Visible quests on a new session = empty list, start, valid advance completes, advance with an unknown `choiceKey` → 422 `QUEST_TRANSITION_NOT_ALLOWED` |
| `npcs.feature` | List NPCs at the session's location, initial relationship = 0, a social action changes it |
| `autenticacion.feature` | Default dev identity is stable across requests when `X-Dev-Player-Id` is omitted; a player cannot read another player's session |
| `recorrido_completo.feature` | The full 10-step journey (create → action → event → **real app restart** → recovery → combat → quest), with persistence verified across the restart |

## Run commands
```powershell
# 1) In "Project GM": source .env.local into THIS shell, then start it
cd "Project GM"
Get-Content ".env.local" | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
}
mvn spring-boot:run

# 2) In a second shell, also with .env.local sourced (needed for recorrido_completo.feature's restart):
cd "Project GM automatics"
mvn test
```
Cucumber prints nothing to the console without the `pretty` plugin — it's already configured
(`RunCucumberTest`/`cucumber.properties`), so real scenario/step output should always be visible. Surefire's
own "Tests run: 0" summary is normal and not reliable with `cucumber-junit-platform-engine`; read the
"pretty" output for the real pass/fail result.

## Where to add a new scenario
- A new isolated endpoint/rejection path → a new or existing single-purpose `.feature` file (see table
  above), reusing `GameSessionSteps` methods where possible.
- A new step spanning multiple existing calls → add a method to `GameSessionSteps`, do not create a new
  steps class (see `coding-patterns.md`).
- A new full-journey / cross-feature flow that should also survive a real restart → extend
  `recorrido_completo.feature`, and be aware it kills/relaunches the developer's local "Project GM".

## Known gotchas
- `junit-platform-suite` must resolve to the exact version `cucumber-junit-platform-engine` pulls in
  transitively (check with `mvn dependency:tree | Select-String junit`) — a mismatch causes
  `NoSuchMethodError` from misaligned JUnit Platform versions.
- REST Assured needs `jackson-databind` on the test classpath to serialize a `Map` request body
  (`IllegalStateException: Cannot serialize object` otherwise).
- If `recorrido_completo.feature` fails during the restart step with a misleading
  `BeanDefinitionStoreException`, it's almost always a missing env var in the shell that ran `mvn test`
  here — see `env-and-config.md`, not a real code regression.
