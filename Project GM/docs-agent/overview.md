---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Purpose, stack, responsibilities and entry points of Project GM (the game master backend).
last_reviewed: null
---

# Project GM — Overview

## Purpose
Spring Boot backend that acts as the "Game Master" engine for a text-driven RPG: it owns game state
(sessions, characters, locations, NPCs, quests, combat), enforces deterministic game rules (dice/skill
checks via `CheckResolver`), and delegates only narration/text-interpretation (never rule outcomes) to
an AI adapter (OpenAI or a deterministic stub). See `ARCHITECTURE_CONTRACT.md` at the workspace root for
the "AI narrates, engine decides" contract this repo must never violate.

## Stack
Java 21, Spring Boot 4.1.1 (Maven, `groupId=pab.rpg`, `artifactId=project_gm`), Spring Web MVC, Spring
Data JPA (Hibernate), Spring Security (OAuth2 resource server / JWT), MySQL 8 + Flyway (`flyway-mysql`),
MapStruct 1.6.3, Lombok, Micrometer + `logstash-logback-encoder` (structured JSON logs), Spring Boot
Actuator.

## Responsibilities
- Own and persist all game state (sessions, characters, locations, NPCs/relationships, quests, combat,
  items/inventory, conversation memory, domain events) — MySQL via Flyway-versioned migrations
  (`V1`…`V8`).
- Resolve player actions deterministically: attribute + difficulty → `CheckResolver` (d20-style roll) →
  `ResultGrade`, gated by pluggable `GameRule`s (e.g. actor alive, location exists, valid NPC target).
- Run combat (initiative, turn order, attack resolution) and quest state machines (stage transitions).
- Optionally interpret free-text player input into a structured `ActionType`/target/candidate via
  `MasterAdapter`, and narrate resolved outcomes — both AI-assisted but never authoritative over rules.
- Authenticate requests (JWT in `cloud`, or a fixed dev identity in `local`/`test`) and derive `playerId`
  only from the authenticated identity, never from request bodies/query params.
- Emit structured logs/metrics (correlation id, session id, action/OpenAI durations, cost estimate).

## Non-responsibilities
- Does not render any UI/client — it is a pure HTTP JSON API (context path `/project_gm`).
- Does not decide narrative outcomes with AI — `MasterAdapter` only narrates an already-resolved
  `ResultGrade` or proposes a candidate that the engine still validates.
- Does not manage its own JWT identity provider in `cloud` — expects an external issuer
  (`JWT_ISSUER_URI`/`JWT_AUDIENCE`).

## Entry points
- `pab.rpg.Application` — `@SpringBootApplication` + `@ConfigurationPropertiesScan` main class.
- HTTP API under `/project_gm/api/v1/**` (see [`api-contracts.md`](api-contracts.md)).
- `/actuator/**` (health/info/metrics always; `shutdown` only in the `local` profile, used by the E2E
  suite to test real-process restarts).

## Related docs in this repo
- [`architecture.md`](architecture.md) — layering and key internal decisions.
- [`domain-model.md`](domain-model.md) — entities and game rules.
- [`env-and-config.md`](env-and-config.md) — profiles, required env vars, secrets hygiene.
- [`testing.md`](testing.md) — how to run unit/integration tests and the separate E2E suite.
