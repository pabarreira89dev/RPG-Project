---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Functionalities this repo implements — links to product-level detail.
last_reviewed: null
---

# Functionality Map

> See also: full functionality catalog at [`docs/functionality/index.md`](../../docs/functionality/index.md) (product level).

## This repo's contributions

| functionality | role | interfaces | files | product-level detail |
|---|---|---|---|---|
| Session & character management | provider | provides:rest POST /api/v1/sessions \| provides:rest GET /api/v1/sessions/{sessionId} \| provides:rest GET /api/v1/sessions | GameSessionController.java, GameSessionServiceImpl.java | — |
| Action resolution (game engine) | provider | provides:rest POST /api/v1/sessions/{sessionId}/actions | ActionController.java, ActionServiceImpl.java, CheckResolver.java | — |
| Combat | provider | provides:rest GET /api/v1/sessions/{sessionId}/combat \| provides:rest POST /api/v1/sessions/{sessionId}/combat/start \| provides:rest POST /api/v1/sessions/{sessionId}/combat/{combatId}/attack | CombatController.java, CombatServiceImpl.java | — |
| Quests | provider | provides:rest GET /api/v1/sessions/{sessionId}/quests \| provides:rest POST /api/v1/sessions/{sessionId}/quests/{questCode}/start \| provides:rest POST /api/v1/sessions/{sessionId}/quests/{questCode}/advance | QuestController.java, QuestServiceImpl.java | — |
| NPCs & relationships | provider | provides:rest GET /api/v1/sessions/{sessionId}/npcs | NpcController.java, NpcServiceImpl.java | — |
| Inventory & items | provider | provides:rest GET /api/v1/sessions/{sessionId}/items \| provides:rest POST /api/v1/sessions/{sessionId}/items/{itemId}/pick-up | ItemController.java, ItemServiceImpl.java | — |
| AI narration & free-text interpretation | participant | internal (invoked by the action/combat/quest endpoints above, no dedicated route) | MasterAdapter.java, OpenAiMasterAdapter.java, StubMasterAdapter.java | — |
| Authentication & player identity | library | internal (cross-cutting filter chain, not a functional endpoint) | SecurityConfig.java, DevelopmentIdentityFilter.java, CurrentPlayerArgumentResolver.java | — |
| Observability | standalone | internal | CorrelationIdFilter.java, logback-spring.xml | — |
| Idempotency & concurrency control | standalone | internal | IdempotencyServiceImpl.java, ProcessedAction.java | — |
| End-to-end verification & real-restart persistence | provider | provides:rest POST /actuator/shutdown \| provides:rest GET /actuator/health | application-local.yml | [docs/functionality/e2e-verification.md](../../docs/functionality/e2e-verification.md) |
