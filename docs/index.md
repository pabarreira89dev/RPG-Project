---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Root entry point for Project GM (RPG Project) context. Routes agents to product-level docs and the repository catalog.
last_reviewed: null
---

# Project GM — Product Context (root index)

> Agent entry point. Read only the row whose "read when…" matches your task, then open that single file.
> This product has no separate git-cloned `repos/` folder (unlike the generic kit's multi-repo model,
> see `templates/README.md` §3) — both repositories are plain subfolders of this same workspace:
> `Project GM/` and `Project GM automatics/`.

## Product-level docs
| file | read when… | purpose |
|------|-----------|---------|
| product-overview.md | you need product scope / functional domain | what the product is, capabilities, stakeholders |
| architecture.md | task spans >1 repo or touches integration | global inter-repo architecture & data flow |
| conventions.md | before writing/changing code in any repo | universal rules & best practices |
| observability.md | task adds observable functionality or needs to validate via logs/metrics | observability platform, alarms, dashboards, log conventions |
| functionality/index.md | task involves adding, changing, or understanding a product functionality | functionality catalog with repo ownership, criticality, detail links |

## Repository navigation
| file | read when… | purpose |
|------|-----------|---------|
| repos/index.md | you must find which repo(s) to touch | repo catalog with #tags, links, impact map |

## Custom / Business-specific (extend here)
| file | read when… | purpose |
|------|-----------|---------|
| testing-conventions.md | task spans >1 repo and touches e2e or shared test infra | cross-repo test environment policy, shared fixtures |

## Design documents (this product, not part of the kit)
These predate and sit alongside this context layer — read them for narrative/product-design detail
that `product-overview.md` intentionally keeps brief:
[`CONTEXTO_PROYECTO.md`](../CONTEXTO_PROYECTO.md) (implementation state + next step, kept up to date),
[`ARCHITECTURE_CONTRACT.md`](../ARCHITECTURE_CONTRACT.md), [`GDD v0.1.md`](../GDD%20v0.1.md),
[`MVP v0.1.md`](../MVP%20v0.1.md) / [`v0.2`](../MVP%20v0.2.md) / [`v0.3`](../MVP%20v0.3.md),
[`TDD MVP v0.2.md`](../TDD%20MVP%20v0.2.md).
