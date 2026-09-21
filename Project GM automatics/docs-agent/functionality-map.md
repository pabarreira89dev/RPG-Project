---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Functionalities this repo implements — links to product-level detail.
last_reviewed: null
---

# Functionality Map

> See also: full functionality catalog at [`docs/functionality/index.md`](../../docs/functionality/index.md) (product level).
> Escape hatch used deliberately here: this repo participates in exactly one cross-repo functionality,
> so this file only lists that one row rather than every scenario it happens to cover.

## This repo's contributions

| functionality | role | interfaces | files | product-level detail |
|---|---|---|---|---|
| End-to-end verification & real-restart persistence | initiator | consumes:rest POST /actuator/shutdown \| consumes:rest GET /actuator/health \| consumes:rest (all `Project GM` game API endpoints, as an ordinary client) | RunCucumberTest.java, GameSessionSteps.java, AppLifecycle.java | [docs/functionality/e2e-verification.md](../../docs/functionality/e2e-verification.md) |

All other scenarios in this repo (`sesiones_de_juego.feature`, `acciones.feature`, `combate.feature`,
`misiones.feature`, `npcs.feature`, `autenticacion.feature`) verify functionalities owned solely by
`Project GM` (see its own `functionality-map.md`) — this repo is a consumer/test client for them, not an
implementer, so they are not repeated here.
