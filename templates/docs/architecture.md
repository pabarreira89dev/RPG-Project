---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Global architecture — how repos interact (not the internal design of any single repo).
last_reviewed: null
---

# Global Architecture (inter-repo)

> See also: for a single repo's internals, that repo's docs-agent/architecture.md.

## System map

```mermaid
flowchart TB
    subgraph APPLICATION["<PRODUCT_NAME>"]
        docs["docs/ (product context)"]
    end

    %% Replace the nodes and edges below with real repos and services.
    %% Keep filesystem-tree ASCII in code blocks, not here.
    repoA["repo-a"]
    repoB["repo-b"]
    repoC["repo-c"]
    ext["External Service"]

    docs --> repoA
    docs --> repoB
    docs --> repoC

    repoA -- "depends on" --> repoB
    repoB -- "calls" --> ext

    %% <PLACEHOLDER: fill edges from docs/repos/index.md 'depends on' column>
```

## Integration & data flow
<PLACEHOLDER: sync/async contracts, events, shared datastores between repos.>

## Cross-cutting concerns
<PLACEHOLDER: auth, observability, shared infra — only what is common to all repos.>

## External documentation
<PLACEHOLDER: links to architecture diagrams / specs.>
