---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Repo-specific non-obvious coding patterns and gotchas for Project GM.
last_reviewed: null
---

# Project GM — Coding Patterns

> See also: [`architecture.md`](architecture.md) for the layering these patterns live in.

## Entities
- `@Entity` + `@Getter` + `@NoArgsConstructor(access = PROTECTED)` + `@AllArgsConstructor`, **never**
  `@Setter`. Mutate state only via intention-revealing methods on the entity itself (e.g.
  `GameSession.advanceWorldTime`, `Combat.advanceTurn`/`startNewRound`/`complete`).
- Repositories live in `domain.repository`, separate from `domain.entity` — one repository per
  aggregate root, not per table.
- JSON columns: map as `Map<String, Object>` with `@JdbcTypeCode(SqlTypes.JSON)` (Hibernate 6 native).
  Never map a JSON column as a raw `String` — Hibernate double-serializes it (escaped JSON string
  inside the `json` column).
- MySQL has no native UUID type: `hibernate.type.preferred_uuid_jdbc_type: CHAR` forces `CHAR(36)`
  (matches the Flyway schema) instead of Hibernate's `BINARY(16)` default.

## Services
- Interface in `service`, implementation in `service.impl`, one impl per interface (except
  `MasterAdapter`, which has two, selected by `@ConditionalOnProperty`).
- Game rules are a `List<GameRule>` Spring auto-collects — to add a new rule, add a
  `@Component implements GameRule`; nothing else needs to change.
- `CheckResolver`/`ResultGrade`/`Difficulty`/`ActionType`/`Attribute` are pure enums/records — no side
  effects, no persistence, fully deterministic and easy to unit test in isolation from Spring.

## AI interpretation — anti-hallucination pattern
Any id/enum value proposed by `MasterAdapter` (interpret/selectCandidate) is treated as untrusted input:
- Malformed values (e.g. a non-UUID `targetNpcId`) are discarded (`null`) **inside the adapter**.
- Well-formed but non-existent values (valid UUID, wrong NPC/location) are **not** filtered by the
  adapter — they fall through to the same validation an explicit client-supplied id would hit
  (`NpcTargetRule`, `findParticipant`, the quest transition lookup). Never duplicate that validation
  inside the adapter.
- `OpenAiMasterAdapter` constrains the model with Structured Outputs (`text.format=json_schema,
  strict:true`); for `selectCandidate` the `enum` of valid ids is built **dynamically per call** from
  the actual candidate list, not a static enum — stronger than a fixed enum because the model can
  physically only choose from that call's real ids.

## API / security
- Controllers never read `playerId` from the request body/query — always `@CurrentPlayer UUID playerId`.
  If you see a DTO field named `playerId` reappear in a request record, that's a regression.
- Every mutating action-like endpoint accepts explicit ids as an **override** of AI interpretation: check
  the explicit field first (`if (x != null) use explicit path else use text/AI path`) — do not invert
  this precedence.
- Optimistic locking: any endpoint that mutates `GameSession` state should thread `expectedVersion`
  through and let `StaleSessionVersionException`/`ObjectOptimisticLockingFailureException` map to 409.

## Config records
- `@ConfigurationProperties` records use `@DefaultValue` for optional fields, never a hardcoded default
  for secrets (`apiKey`, DB credentials, JWT issuer/audience have no default in any profile).
- Do not manually set `requestFactory` on `RestClient.Builder` in `OpenAiMasterAdapter` — it breaks test
  wiring via `MockRestServiceServer.bindTo(RestClient.Builder)`. Use the global
  `spring.http.client.connect-timeout`/`read-timeout` properties instead.

## Known recurring pitfall (see `/memories/debugging.md`)
Do not hardcode a real OpenAI API key as a YAML default value in `application-local.yml` — this has
happened 3 times in this repo's history (never reached `origin`, always caught before push). Always use
`${OPENAI_API_KEY}` with no default, same as `cloud`.

## `ActionServiceImpl` ordering gotcha
When `actionType` is omitted, `sceneSummary()` (needed to call `MasterAdapter.interpret`) resolves the
current `Location` and throws `ActionNotAllowedException` itself if it's missing/invalid — this runs
**before** `gameRules.forEach(rule -> rule.check(context))`. So on the free-text path,
`LocationExistsRule` is effectively redundant (the location was already validated); it only does real
work on the explicit-`actionType` path, where `interpret()` is skipped. Don't "fix" this by moving the
check — it's required so `interpret()` never runs against an invalid location.
