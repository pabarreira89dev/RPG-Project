---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Exposed HTTP API endpoints and error contract for Project GM.
last_reviewed: null
---

# Project GM — API Contracts

> Base path: `/project_gm/api/v1`. All endpoints require authentication (JWT in `cloud`, dev identity
> bypass in `local`/`test` — see `env-and-config.md`). `playerId` is never a request parameter; it comes
> from the authenticated identity.

## Sessions — `GameSessionController`
| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/sessions` | Create a session for the current player (body: `CreateSessionRequest`, no `playerId` field) |
| `GET` | `/sessions/{sessionId}` | Get a session — 404 if it belongs to another player or is soft-deleted |
| `GET` | `/sessions` | List the current player's sessions (excludes soft-deleted ones) |
| `DELETE` | `/sessions/{sessionId}` | Soft-delete a session (sets `deletedAt`) — 204, or 404 if it belongs to another player / doesn't exist / is already deleted |

## Actions — `ActionController`
| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/sessions/{sessionId}/actions` | Submit an action. `SubmitActionRequest`: `text` (required), `actionType`/`targetNpcId` (optional — omit to let `MasterAdapter.interpret` derive them from `text`), `expectedVersion`, `idempotencyKey`. |

## Combat — `CombatController`
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/sessions/{sessionId}/combat` | Get the active combat — 404 `COMBAT_NOT_FOUND` if none |
| `POST` | `/sessions/{sessionId}/combat/start` | Start combat vs `npcIds` (`StartCombatRequest`) — 422 `COMBAT_NOT_ALLOWED` if an NPC is dead/not in the current location, or a combat is already active |
| `POST` | `/sessions/{sessionId}/combat/{combatId}/attack` | `PerformAttackRequest`: either explicit `attackerParticipantId`+`targetParticipantId`, or `text` (free-text target selection via `MasterAdapter.selectCandidate`) when `targetParticipantId` is null |

## Quests — `QuestController`
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/sessions/{sessionId}/quests` | Visible quests (only ones with an existing `QuestState` — a brand-new session returns `[]`) |
| `POST` | `/sessions/{sessionId}/quests/{questCode}/start` | Idempotent quest start |
| `POST` | `/sessions/{sessionId}/quests/{questCode}/advance` | `AdvanceQuestRequest`: either explicit `choiceKey`, or `text` (free-text branch selection) when `choiceKey` is null — 422 `QUEST_TRANSITION_NOT_ALLOWED` if invalid |

## NPCs — `NpcController`
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/sessions/{sessionId}/npcs` | NPCs at the session's current location, each with the player's relationship value (0 by default) |

## Items — `ItemController`
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/sessions/{sessionId}/items` | `InventoryResponse(inventory, atLocation)` — the player's carried items and the (unowned) items lying in the current location |
| `POST` | `/sessions/{sessionId}/items/{itemId}/pick-up` | Pick up an item lying in the current location — 404 `ITEM_NOT_FOUND` if it doesn't exist in this session, 422 `ITEM_NOT_ALLOWED` if it's already owned or lying in a different location. Explicit `itemId` only — no free-text interpretation yet |

## Events — `GameEventController`
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/sessions/{sessionId}/events?after={seq}` | Diagnostic-only; internal event log, not consumed by the normal client |

## Errors
All errors return a single JSON shape from `GlobalExceptionHandler`:
```json
{ "code": "STRING_CODE", "message": "...", "timestamp": "...", "correlationId": "..." }
```
| HTTP | `code` | Trigger |
|------|--------|---------|
| 404 | `SESSION_NOT_FOUND` | Session missing or belongs to another player |
| 404 | `QUEST_NOT_FOUND` | Unknown quest code |
| 404 | `COMBAT_NOT_FOUND` | No active combat |
| 404 | `ITEM_NOT_FOUND` | Item id doesn't exist in this session |
| 409 | `STALE_SESSION_VERSION` | `expectedVersion` mismatch or concurrent write (`ObjectOptimisticLockingFailureException`) |
| 422 | `ACTION_NOT_ALLOWED` | A `GameRule` rejected the action (e.g. dead actor, invalid location/target) |
| 422 | `QUEST_TRANSITION_NOT_ALLOWED` | Invalid/missing `choiceKey` for the quest's current stage |
| 422 | `COMBAT_NOT_ALLOWED` | Invalid combat start/attack (dead/absent NPC, wrong turn, already active combat) |
| 422 | `ITEM_NOT_ALLOWED` | Item already picked up, or not in the player's current location |
| 503 | `OPENAI_UNAVAILABLE` | `MasterAdapter` network failure or empty narration |
| 400 | `INVALID_REQUEST` | `IllegalArgumentException`/`NullPointerException` fallback |

Note: 401/403 from Spring Security use its default body shape, not this `ApiError` format (no custom
`AuthenticationEntryPoint`/`AccessDeniedHandler` yet — a known gap, see `MVP v0.3.md`).
