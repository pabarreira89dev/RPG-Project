---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Entry point for Project GM automatics (E2E/API test suite) context. Routes agents to this repo's docs.
last_reviewed: null
---

# Project GM automatics — Product Context (repo index)

> Agent entry point for this repo. Read only the row whose "read when…" matches your task; open that single file.
> Product-level context: [`docs/index.md`](../../docs/index.md) (repository catalog, cross-repo architecture,
> functionality catalog).

## Core docs
| file | read when… | purpose |
|------|-----------|---------|
| overview.md | always, first | purpose, stack, responsibilities, entry points |
| architecture.md | task changes structure/flow | internal architecture & key decisions |
| dependencies.md | assessing impact / integrations | dependency on "Project GM", what breaks if its API changes |
| coding-patterns.md | writing/changing code | repo-specific non-obvious patterns (Gherkin, step sharing, catalogs) |
| env-and-config.md | running/configuring | what must be running first, ports, env vars |
| testing.md | adding/changing a scenario | feature files, run commands, verification workflow |

## Optional docs (present ONLY if applicable to this repo)
| file | read when… | purpose |
|------|-----------|---------|
| functionality-map.md | task adds or changes the E2E-verification functionality | this repo's contribution to the one cross-repo functionality it participates in |

Not present (not applicable to this repo): `api-contracts.md` (this repo consumes an API, it does not
expose one), `domain-model.md` (no domain of its own — it exercises "Project GM"'s domain via HTTP),
`observability.md` (no instrumentation of its own, see `docs/observability.md`).

## Custom / Business-specific (extend here)
| file | read when… | purpose |
|------|-----------|---------|
<!-- Add files specific to this repo's business here, keeping frontmatter: status: draft. -->
