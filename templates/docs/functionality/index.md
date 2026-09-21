---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Catalog of <PRODUCT_NAME> functionalities — ownership, repos, and entry points.
last_reviewed: null
---

# Functionality Catalog

> Agent entry point for functional tasks. Pick the row that matches your task, open that file.

## Functionalities

| functionality | description | owning repos | criticality | detail |
|---|---|---|---|---|
| <PLACEHOLDER: name> | <PLACEHOLDER: one-line> | <PLACEHOLDER: repo-a, repo-b> | <PLACEHOLDER: high/med/low> | [<name>.md](<name>.md) |

## How to add a new functionality entry

The pipeline auto-creates a `<slug>.md` detail page for CROSS-REPO functionalities (owned by ≥2 repos);
single-repo functionalities just link to that repo's `functionality-map.md`. Add one MANUALLY only to
document a new functionality the pipeline missed, or to promote a single-repo functionality to its own page:

1. Copy `_functionality.md` to `<slug>.md` in this folder.
2. Fill in each section of the new file.
3. Add a row to the table above (its `detail` link → `<slug>.md`).
4. Update each owning repo's `functionality-map.md` `product-level detail` column to link back to `<slug>.md`.
