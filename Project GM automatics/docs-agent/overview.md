---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Purpose, stack, responsibilities and entry points of Project GM automatics (the E2E/API test suite).
last_reviewed: null
---

# Project GM automatics — Overview

## Purpose
Independent Maven test project that exercises "Project GM" as a black box over its real HTTP API, using
Cucumber (Gherkin, Spanish) + REST Assured. It is the only E2E/API-level test suite for this product; it
also owns the one test that proves game state actually persists across a real application restart, not
just in memory.

## Stack
Java 21, Maven (`groupId=com.pabarreira`, `artifactId=project_gm_autimatics`), Cucumber 7.30.0
(`cucumber-java` + `cucumber-junit-platform-engine`), JUnit 5 (`junit-bom` 5.14.0,
`junit-platform-suite`), REST Assured 5.5.6, Jackson Databind (test-scope, required by REST Assured to
serialize `Map` request bodies). No Spring, no dependency-injection module for Cucumber (no
PicoContainer/Spring glue) — state is shared between steps via plain instance fields on the single steps
class.

## Responsibilities
- Drive "Project GM"'s public HTTP API (`http://localhost:8080/project_gm`) exactly like a real client:
  create/list/get sessions, submit actions, start/attack in combat, start/advance quests, list NPCs,
  check the dev-identity auth header behavior.
- Assert on HTTP status codes and response bodies (including the `ApiError` shape for rejection paths).
- Own the one full-journey scenario (`recorrido_completo.feature`) that chains session → action →
  combat → quest and **restarts the real "Project GM" process** mid-scenario (`AppLifecycle`) to prove
  persistence survives a restart, not just an in-memory happy path.
- Maintain small fixed-UUID catalogs (`LocationCatalog`, `NpcCatalog`) mirroring "Project GM"'s Flyway
  seed data, so scenarios can reference seeded entities without querying for them first.

## Non-responsibilities
- Does not implement any game logic, persistence, or domain model — it only asserts on the behavior
  "Project GM" exposes over HTTP.
- Does not run in CI and is not started automatically — the developer must have "Project GM" already
  running locally before invoking this suite.
- Does not talk to MySQL or OpenAI directly — all state is observed indirectly through the API.

## Entry points
- `com.pabarreira.tests.RunCucumberTest` — JUnit 5 `@Suite` that runs all `.feature` files under
  `src/test/resources/features` with glue package `com.pabarreira.tests`.
- `.feature` files in `src/test/resources/features/` (see [`testing.md`](testing.md) for the full list).

## Related docs in this repo
- [`architecture.md`](architecture.md) — how the suite is wired (runner, steps, support classes).
- [`dependencies.md`](dependencies.md) — its one real dependency: a running "Project GM".
- [`testing.md`](testing.md) — how to run it and what each feature file covers.
