---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Root entry point for <PRODUCT_NAME> context. Routes agents to product-level docs and the repository catalog.
last_reviewed: null
---

# <PRODUCT_NAME> — Product Context (root index)

> Agent entry point. Read only the row whose "read when…" matches your task, then open that single file.

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
<!-- Add product-wide files the team needs. Keep frontmatter: status: draft. -->
| file | read when… | purpose |
|------|-----------|---------|
| testing-conventions.md | task spans >1 repo and touches e2e or shared test infra | cross-repo test environment policy, shared fixtures |
<!-- | glossary.md | unfamiliar domain terms appear | business glossary | -->
