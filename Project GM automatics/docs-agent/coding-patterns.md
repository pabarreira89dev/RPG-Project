---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Repo-specific non-obvious coding patterns and gotchas for Project GM automatics.
last_reviewed: null
---

# Project GM automatics — Coding Patterns

> See also: [`architecture.md`](architecture.md) for why these patterns exist.

## Gherkin
- `.feature` files declare `# language: es` and are written entirely in Spanish (`Característica`,
  `Escenario`, `Dado`/`Cuando`/`Entonces`/`Y`).
- Java step method annotations are **always** English (`@Given`/`@When`/`@Then`), regardless of the
  Spanish Gherkin text — `io.cucumber.java.en` has no Spanish equivalent annotations.
- A method can carry more than one Cucumber annotation (e.g. both `@Given` and `@When` on the same
  method) when the same action is meaningfully both a precondition in one scenario and the tested step
  in another — reuse the method instead of duplicating logic.

## Steps
- All steps live in the single `GameSessionSteps` class — do not create a second steps class unless a DI
  module (PicoContainer/Spring) is added first; otherwise shared instance-field state (`playerId`,
  `lastSessionId`, …) breaks across classes.
- Shared state is plain instance fields, reset per-scenario by Cucumber's default per-scenario
  instantiation — do not add `static` fields for scenario state, it would leak between scenarios.
- Only parse a response field when you know the call succeeded — e.g. `combatId` must only be read from
  the response when `statusCode == 200`; a prior bug assumed success unconditionally and crashed on the
  expected-rejection scenarios.

## Fixture catalogs
- `LocationCatalog`/`NpcCatalog` hardcode the same UUIDs as `Project GM`'s Flyway seed migrations
  (`V3__create_location.sql`, `V4__create_npc_relationship_and_knowledge.sql`). If seed data changes in
  "Project GM", update these catalogs in the same change — there is no shared library, so they must be
  kept in sync manually.
- Look up ids by their seed `code` (e.g. `"village_square"`, `"forest_hunter"`), never hardcode a raw
  UUID literal in a step or feature file.

## Combat scenarios without enemy AI
There is no AI-controlled enemy turn in "Project GM" — scenarios that need to finish a combat simulate
the `ENEMY` team's turn by calling the explicit-id attack endpoint themselves (attacker=NPC,
target=player) whenever `currentParticipantId` isn't on the `PLAYER` team. Use an NPC that is already in
the session's current location (`village_guard` in `village_square`) — starting combat against an NPC in
a different location fails validation in "Project GM".

## Process-control tests (`recorrido_completo.feature`)
- Running this scenario **kills and relaunches the developer's local "Project GM" process** — warn
  before running it if the user might have that instance open for manual testing.
- `AppLifecycle`'s relaunched child process inherits env vars from whatever JVM ran `mvn test` in this
  repo, not from any other interactive terminal — `Project GM/.env.local` must be sourced into that same
  shell first (see [`env-and-config.md`](env-and-config.md)).
