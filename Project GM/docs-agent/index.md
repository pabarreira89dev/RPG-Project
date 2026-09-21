---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Entry point for Project GM (Spring Boot backend) context. Routes agents to this repo's docs.
last_reviewed: null
---

# Project GM — Product Context (repo index)

> Agent entry point for this repo. Read only the row whose "read when…" matches your task; open that single file.
> This workspace has no full `docs/` product-context tree yet — only [`docs/functionality/index.md`](../../docs/functionality/index.md)
> (the functionality catalog) has been seeded so far. The rest of the product-level context lives at the
> workspace root instead: [`CONTEXTO_PROYECTO.md`](../../CONTEXTO_PROYECTO.md) (always read first — current
> implementation state + next step), [`ARCHITECTURE_CONTRACT.md`](../../ARCHITECTURE_CONTRACT.md),
> [`GDD v0.1.md`](../../GDD%20v0.1.md), [`MVP v0.1.md`](../../MVP%20v0.1.md)/[`v0.2`](../../MVP%20v0.2.md)/[`v0.3`](../../MVP%20v0.3.md),
> [`TDD MVP v0.2.md`](../../TDD%20MVP%20v0.2.md).

## Core docs
| file | read when… | purpose |
|------|-----------|---------|
| overview.md | always, first | purpose, stack, responsibilities, entry points |
| architecture.md | task changes structure/flow | internal architecture & key decisions |
| dependencies.md | assessing impact / integrations | internal+external deps, events in/out |
| coding-patterns.md | writing/changing code | repo-specific non-obvious patterns |
| env-and-config.md | running/configuring/deploying | config & vars not inferable from code |
| testing.md | adding/changing functionality | test tiers, conventions, run commands, verification workflow |

## Optional docs
| file | read when… | purpose |
|------|-----------|---------|
| api-contracts.md | task touches endpoints/payloads | exposed API contracts |
| domain-model.md | functional/business task | core entities & business rules |
| observability.md | task adds functionality requiring alarms/metrics | how to instrument this repo, add alarms, validate via logs/metrics |
| functionality-map.md | task adds or changes a functionality in this repo | which product functionalities this repo implements, and its specific contribution |

## Custom / Business-specific (extend here)
| file | read when… | purpose |
|------|-----------|---------|
<!-- Add files specific to this repo's business here, keeping frontmatter: status: draft. -->

## Related repo
`Project GM automatics/` is a separate Maven project (Cucumber + REST Assured E2E suite) that drives
this app's HTTP API as a black box. It has no `docs-agent/` of its own; see
[`testing.md`](testing.md#e2e-project-gm-automatics) for how it relates to this repo.
