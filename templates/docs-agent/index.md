---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Entry point for <REPO_NAME> context. Routes agents to this repo's docs.
last_reviewed: null
---

# <REPO_NAME> — Product Context (repo index)

> Agent entry point for this repo. Read only the row whose "read when…" matches your task; open that single file.
> Product context: <PLACEHOLDER: link to docs/index.md (product level)>

## Core docs
| file | read when… | purpose |
|------|-----------|---------|
| overview.md | always, first | purpose, stack, responsibilities, entry points |
| architecture.md | task changes structure/flow | internal architecture & key decisions |
| dependencies.md | assessing impact / integrations | internal+external deps, events in/out |
| coding-patterns.md | writing/changing code | repo-specific non-obvious patterns |
| env-and-config.md | running/configuring/deploying | config & vars not inferable from code |
| testing.md | adding/changing functionality | test tiers, conventions, run commands, verification workflow |

## Optional docs (present ONLY if applicable to this repo)
| file | read when… | purpose |
|------|-----------|------|
| api-contracts.md | task touches endpoints/payloads | exposed API contracts |
| domain-model.md | functional/business task | core entities & business rules |
| observability.md | task adds functionality requiring alarms/metrics | how to instrument this repo, add alarms, validate via logs/metrics |
| functionality-map.md | task adds or changes a functionality in this repo | which functionalities this repo implements and its specific contribution |

## Custom / Business-specific (extend here)
<!-- Add files specific to this repo's business. Keep frontmatter: status: draft. -->
| file | read when… | purpose |
|------|-----------|---------|
<!-- | pricing-rules.md | task touches billing | non-obvious pricing logic | -->
