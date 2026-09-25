---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Core entities and game rules of Project GM's domain model.
last_reviewed: null
---

# Project GM — Domain Model

> See also: [`architecture.md`](architecture.md) for how these are wired together.

## Session & character (per-session, mutable)
- `GameSession` — `id`, `playerId`, `worldId`, `currentLocationId`, `status` (`SessionStatus`),
  `worldTime`, optimistic-lock `version`, owns one `Character` (`@OneToOne`, cascade all), nullable
  `deletedAt` (soft delete — set via `softDelete()`, excluded from listing/lookup by player).
- `Character` — `name`, `level`, `experience`, embedded `AttributeSet` (6 attributes below), embedded
  `HealthState` (`maximum`/`current`/`wounds`).

## Attributes, checks & difficulty
- `Attribute` enum: `STRENGTH`, `AGILITY`, `INTELLECT`, `WILLPOWER`, `PERCEPTION`, `PRESENCE`.
- `Difficulty` enum (target numbers, TDD §8.3): `TRIVIAL`(8) < `EASY`(11) < `MODERATE`(14) < `HARD`(17) <
  `VERY_HARD`(20) < `EXTREME`(23) < `LEGENDARY`(26).
- `ActionType` enum binds a category to its attribute + base difficulty (GDD §5):
  `EXPLORATION`→Perception/Easy, `SOCIAL`→Presence/Moderate, `INVESTIGATION`→Intellect/Moderate,
  `PHYSICAL`→Strength/Hard.
- `CheckResolver` rolls and computes a margin; `ResultGrade.fromMargin` buckets it (TDD §8.4): margin
  ≥6 `GRAN_EXITO`, ≥0 `EXITO`, ≥-3 `EXITO_CON_COSTE`, ≥-7 `FRACASO`, else `FRACASO_GRAVE`.
- `GameRule` implementations (`ActorAliveRule`, `LocationExistsRule`, `NpcTargetRule`) gate every action
  before resolution; each throws `ActionNotAllowedException` (→ 422) to reject.

## World catalog (global, not per-session)
- `Location` — seeded (`village_square`, `tavern`, `forest_edge`); `GameSession.currentLocationId` FKs
  into it.
- `Npc` — 5 seeded NPCs; also carries `attributes` (`AttributeSet`) + `healthMaximum` so it can be a
  combat participant. `NpcStatus` tracks alive/dead.
- `Quest` (`code`, `title`, `description`) → `QuestStage` (1 `isInitial`, ≥1 `isTerminal` per quest) →
  `QuestStageTransition` (edges: `fromStageId`+`choiceKey` unique per quest → `toStageId`). 3 seeded
  quests: `aron_debt` (pay/confront), `forest_threat` (investigate/ignore), `village_elder_history`
  (listen/dismiss).

## Per-session, per-NPC state
- `Relationship` (session+NPC unique) — numeric value, default 0; changed by `SOCIAL` actions
  (`GRAN_EXITO` +2, `EXITO` +1, `EXITO_CON_COSTE` 0, `FRACASO` -1, `FRACASO_GRAVE` -2).
- `NpcKnowledgeFact` (session+NPC) — exists with `recordKnowledge`/`knowsFact` but **no caller yet**
  (deliberately left unwired until dialogue/quests need to record real facts).
- `QuestState` (session+quest unique) — the only per-session quest data; `getVisibleQuests` only returns
  quests that already have one.

## Items & inventory
- `ItemTemplate` — global catalog (like `Location`/`Npc`/`Quest`): `code` (unique), `name`, `description`.
  3 seeded (`rusty_dagger`, `healing_herbs`, `old_coin_pouch`).
- `Item` — per-session instance: `sessionId`, `templateId`, `quantity`, `durability` (nullable, unused so
  far), and exactly one of `ownerId` (carried by the session's `Character`) / `locationId` (lying in the
  world), enforced by a DB `CHECK` (same XOR pattern as `combat_participant.character_id`/`npc_id`).
- `ItemService.seedInitialItems` spawns the 3 seed items unowned into their fixed locations
  (`forest_edge`/`tavern`) when a session is created (`GameSessionServiceImpl.createSession`), so every
  new game starts with something to find.
- `pickUpItem` validates the item is unowned and in the player's current location, assigns it to the
  `Character`, and appends an `ITEM_ACQUIRED` event — the only way an item can end up owned (TDD §15
  invariant: "no item appears without an `ITEM_ACQUIRED` event").
- Not yet wired: using items in combat/weapon-damage influence, "use item" as an action, dropping items,
  free-text pick-up (only explicit `itemId` today) — see `MVP v0.3.md`.

## Combat
- `Combat` — `sessionId`, `status` (`CombatStatus`), `roundNumber`, `currentTurnOrder`, timestamps;
  behavior via `advanceTurn`/`startNewRound`/`complete`. Exactly one `ACTIVE` combat per session,
  enforced at the DB level (generated column + unique index, since MySQL has no partial indexes).
- `CombatParticipant` — team (`CombatTeam`: `PLAYER`/`ENEMY`), status (`CombatParticipantStatus`),
  initiative order.
- `startCombat` validates the NPC is alive and in the session's current location, rolls
  `d20 + Agility modifier` initiative (non-auditable `SecureRandom`, unlike `CheckResolver`).
  `performAttack` uses `CheckResolver` with `Attribute.STRENGTH` + `Difficulty.MODERATE` (provisional —
  no weapon-damage integration with `Item` yet, see `MVP v0.3.md`) and a fixed damage table by
  `ResultGrade` (8/5/3/0/0). Only an "attack" action exists (no move/defend/item, see `MVP v0.3.md`).

## Events & idempotency
- `GameEvent` — append-only per-session log (e.g. `RELATIONSHIP_CHANGED`), exposed read-only via
  `GET .../events` for diagnostics only.
- `ProcessedAction` — backs `IdempotencyService`: a repeated `idempotencyKey` on `POST .../actions`
  returns the original result instead of re-resolving.

## Conversation memory
- `ConversationTurn` (`domain.memory`) — per-session, append-only `playerText`+`narration` pair, ordered
  by its own `sequence` (same pattern as `GameEvent`, but a separate table: this is short natural-language
  history for the AI prompt, not the structured/auditable event log).
- `ConversationMemoryService.recordTurn` is called right after `MasterAdapter.narrate` succeeds, in
  `ActionServiceImpl` (free-text actions) and `CombatServiceImpl.performAttack` (combat narration) — the
  two call sites that produce a natural-language narration. Quest advancement doesn't narrate, so it
  doesn't record a turn.
- `summarizeRecent(sessionId)` returns "" when the session has no prior turns, otherwise the last 5 turns
  formatted chronologically as `Jugador: ...\nMaster: ...`. Passed as `recentConversation` on both
  `MasterAdapter.NarrationRequest` and `InterpretationRequest`; `OpenAiMasterAdapter` appends it to the
  prompt only when non-blank (so a brand-new session's first turn is unchanged); `StubMasterAdapter`
  ignores it (deterministic, no reasoning over history).

## Deliberately out of scope for now (see `MVP v0.3.md`)
Weapon/item influence on combat damage, skill/circumstance modifiers (parameters exist on
`CheckResolver.resolve` but callers always pass 0), combat beyond "attack", death/`DOWNED`→stabilize
rules, OpenAI retry/backoff, per-user usage limits.
