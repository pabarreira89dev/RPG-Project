---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Project GM — a conversational fantasy RPG where an AI narrates but a deterministic Game Engine owns all truth.
last_reviewed: null
---

# Product Overview

## What it is
Project GM is a conversational, narrative fantasy RPG for the web. The player acts through free natural
language; an AI plays the role of "Game Master" narrator, but it is never the authority over game state.
Founding principle (see `ARCHITECTURE_CONTRACT.md`): **"the AI narrates what happens; the Game Engine
determines what actually happens."** The product starts as a single-player experience, deliberately
leaving room for a future multiplayer evolution.

## Functional domain & capabilities
- Persistent game sessions, each with one player character (attributes, health, level, world time).
- Free-text or explicit player actions, resolved deterministically via skill/attribute checks
  (`CheckResolver`) gated by pluggable game rules — never resolved by the AI.
- Turn-based combat (initiative, attacks, damage by result grade).
- Branching quests, modeled as a stage/transition state machine per quest, advanced by explicit choice
  or free text.
- NPCs with per-session relationships that change based on the player's social actions.
- Basic inventory: a global item catalog plus per-session item instances that the player can find lying
  in a location and pick up (owned items don't influence combat damage yet).
- AI narration and free-text interpretation, isolated behind the `MasterAdapter` interface so the AI can
  be swapped (OpenAI Responses API in `cloud`, a deterministic stub in `local`/`test`) without touching
  domain logic.
- Short-term conversation memory: recent player/narration turns are summarized and fed back into the AI's
  prompts, so narration/interpretation reasons with recent context instead of a single isolated action.
- Authentication (JWT in `cloud`) and idempotent/concurrency-safe action submission.

## Current scope
Active development targets the `MVP v0.2` vertical slice (see `MVP v0.2.md`): 1 scenario, 3 locations,
5 NPCs, 1 faction, 3 branching quests, basic turn-based combat. `MVP v0.1.md` describes the full
long-term vision (5–10h campaign, multiple regions/factions/economy) which is explicitly **out of scope**
for now. `MVP v0.3.md` tracks TDD items closed out of order (JWT, observability, E2E suite, basic
inventory, conversation memory done; death rules, weapon damage, skill modifiers, etc. still pending).

## Key stakeholders / actors
- **Player** — the sole external actor for now; interacts only via the HTTP API (no first-party client
  UI exists in this workspace).
- **Game Engine** (`Project GM`) — the deterministic authority over world state, rules, dice, and
  consequences. Never delegates a decision about state to the AI.
- **AI / Game Master narrator** (OpenAI, or a stub) — interprets free text and narrates outcomes the
  engine already resolved; explicitly forbidden from deciding rolls, damage, HP, quest completion, or
  reputation (see `ARCHITECTURE_CONTRACT.md` §4).

## External documentation
No Confluence/external wiki — all product-design documentation is versioned in this repo's root:
`GDD v0.1.md`, `MVP v0.1.md`/`v0.2`/`v0.3`, `TDD MVP v0.2.md`, `ARCHITECTURE_CONTRACT.md`,
`CONTEXTO_PROYECTO.md`.
