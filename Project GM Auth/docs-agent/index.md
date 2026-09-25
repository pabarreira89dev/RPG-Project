---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Entry point for Project GM Auth (Spring Authorization Server / IdP) context. Routes agents to this repo's docs.
last_reviewed: null
---

# Project GM Auth — Product Context (repo index)

> Agent entry point for this repo. Read only the row whose "read when…" matches your task; open that single file.
> Product-level context: [`docs/index.md`](../../docs/index.md) (repository catalog, cross-repo architecture,
> functionality catalog).

## Core docs
| file | read when… | purpose |
|------|-----------|---------|
| overview.md | always, first | purpose, stack, responsibilities, entry points |
| architecture.md | task changes structure/flow | internal architecture & key decisions (in-memory AS state, custom password grant, token customizer) |
| dependencies.md | assessing impact / integrations | MySQL, and the trust contract with "Project GM" + the Android app |
| coding-patterns.md | writing/changing code | repo-specific non-obvious patterns (entity style, custom grant type wiring, CSRF matchers) |
| env-and-config.md | running/configuring | ports, env vars, and the exact values that must match "Project GM" |
| testing.md | adding/changing a scenario | how to run tests and manually verify the custom "password" grant flow |
| api-contracts.md | integrating a client (e.g. the Android app) | endpoints this service exposes (AS endpoints + `/api/v1/register`) |

## Optional docs (present ONLY if applicable to this repo)
Not present (not applicable to this repo, at least for now): `domain-model.md` (a single trivial entity,
`AppUser`, documented inline in `api-contracts.md`/`overview.md` instead of its own file),
`observability.md` (no instrumentation beyond default Actuator health/info — see `docs/observability.md`
for the product-level picture), `functionality-map.md` (this repo's only functionality — issuing tokens
— is described in `overview.md`/`architecture.md`, no cross-repo functionality catalog entry needed yet).

## Custom / Business-specific (extend here)
| file | read when… | purpose |
|------|-----------|---------|
<!-- Add files specific to this repo's business here, keeping frontmatter: status: draft. -->
