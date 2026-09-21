---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Internal architecture of this repo.
last_reviewed: null
---

# Internal Architecture

> See also: inter-repo view in `docs/architecture.md` (product level).

## Layers / structure

```mermaid
flowchart TB
    %% Replace the nodes/layers below with this repo's real module/layer structure.
    %% Keep filesystem-tree ASCII in code blocks, not here.
    api["API / Controller layer"]
    service["Service / Domain layer"]
    infra["Infrastructure / Persistence layer"]

    api -- "calls" --> service
    service -- "uses" --> infra

    %% <PLACEHOLDER: fill with real layers from facts.tree_top/modules — document only non-obvious structure>
```

## Key decisions & trade-offs
<PLACEHOLDER: ADR-style bullets an agent cannot infer from code.>

## External documentation
<PLACEHOLDER: links to diagrams/specs.>
