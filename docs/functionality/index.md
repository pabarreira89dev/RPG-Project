---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Catalog of RPG Project functionalities — ownership, repos, and entry points.
last_reviewed: null
---

# Functionality Catalog

> Agent entry point for functional tasks. Pick the row that matches your task, open that file.
> This workspace has no `docs/index.md`/`docs/product-overview.md` yet — only this functionality
> catalog has been seeded so far. Product-level narrative context still lives at the workspace root
> (`CONTEXTO_PROYECTO.md`, `GDD v0.1.md`, `MVP v0.1/0.2/0.3.md`, `TDD MVP v0.2.md`,
> `ARCHITECTURE_CONTRACT.md`).

## Functionalities

| functionality | description | owning repos | criticality | detail |
|---|---|---|---|---|
| Session & character management | Create, retrieve and list game sessions; each session owns one player character (attributes, health, level, world time). | Project GM | high | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Action resolution (game engine) | Resolves free-text or explicit player actions via deterministic d20-style checks gated by pluggable game rules; never decided by AI. | Project GM | high | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Combat | Turn-based combat: start an encounter, roll initiative, resolve attacks with damage tiered by result grade. | Project GM | high | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Quests | Global quest/stage/transition catalog plus a per-session state machine, advanced by explicit choice or free text. | Project GM | high | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| NPCs & relationships | Per-location NPC catalog; tracks a per-session relationship value that social actions change. | Project GM | medium | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Inventory & items | Global item catalog plus per-session item instances (owned by the player or lying in a location); picking up an item emits an `ITEM_ACQUIRED` event. | Project GM | medium | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| AI narration & free-text interpretation | Turns an already-resolved outcome into prose, and free player text into a structured intent/candidate id — never decides the outcome itself. | Project GM | medium | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Conversation memory | Short per-session history of player text + narrated outcome, summarized and fed back into the AI's narration/interpretation prompts so it reasons with recent context. | Project GM | low | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Authentication & player identity | JWT-based identity in `cloud`, fixed dev-identity bypass in `local`/`test`; `playerId` always derived from the authenticated identity, never from request input. | Project GM | high | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Observability | Correlation ids, structured JSON logs, and Micrometer metrics for actions, session conflicts, and OpenAI usage/cost. | Project GM | medium | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| Idempotency & concurrency control | Idempotent action submission (replay-safe) plus optimistic locking on session version to prevent lost updates. | Project GM | medium | [Project GM/docs-agent/functionality-map.md](../../Project%20GM/docs-agent/functionality-map.md) |
| End-to-end verification & real-restart persistence | Black-box Cucumber/REST Assured suite that drives the real API and also restarts the real "Project GM" process mid-test to verify game state survives a restart. | Project GM, Project GM automatics | medium | [e2e-verification.md](e2e-verification.md) |

## How to add a new functionality entry

The pipeline auto-creates a `<slug>.md` detail page for CROSS-REPO functionalities (owned by ≥2 repos);
single-repo functionalities just link to that repo's `functionality-map.md`. Add one MANUALLY only to
document a new functionality the pipeline missed, or to promote a single-repo functionality to its own page:

1. Copy `_functionality.md` to `<slug>.md` in this folder.
2. Fill in each section of the new file.
3. Add a row to the table above (its `detail` link → `<slug>.md`).
4. Update each owning repo's `functionality-map.md` `product-level detail` column to link back to `<slug>.md`.
