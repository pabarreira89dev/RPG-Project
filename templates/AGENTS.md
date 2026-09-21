# <PRODUCT_NAME> — Agent Entry Point

> **Purpose:** This is the root anchor for AI agents working on the **<PRODUCT_NAME>** product.
> Read this file first, then navigate to the relevant context using the links below.
> Do not read docs blindly — use the routing tables in `docs/index.md` to load only what your task needs.

---

## What this product is

<PRODUCT_NAME> is a multi-repository product. All agent-relevant context lives in two levels:

| Level | Location | Answers |
|-------|----------|---------|
| **Product level** | `docs/` | What is the product? How do the repos fit together? What rules apply everywhere? |
| **Repo level** | `repos/<repo>/docs-agent/` | What is this repo? How is it built? How do I change it safely? |

Start at `docs/index.md` — it routes you to the right file for your task.

---

## Working in composing repositories

Before changing `repos/<repo>/`, read its `docs-agent/index.md`, `AGENTS.md`, and
`.github/copilot-instructions.md` when present; load every applicable
`.github/instructions/*.instructions.md` for the files being changed; inspect `.github/skills/`
and `.github/agents/`, then apply the local tools whose triggers match the task.

For multi-repo work, repeat this per affected repo and normally delegate one agent per repo. Each
delegation names its target repo and applicable local instructions/tools. Local tools remain
versioned in their owning repo; they are not promoted to the parent skill catalog.

---

## 8-stage pipeline (how this context is generated and maintained)

| Stage | Name | Tool / Agent | Output |
|-------|------|--------------|--------|
| 1 | Intake | `pc-intake` skill | `intake/latest-intake.json` |
| 2 | Clone | `pc-clone-repos` skill | `repos/<repo>/` |
| 3 | Scan | `pc-repo-scan` skill | `.pc-work/facts/<repo>.json` |
| 4 | Seed | `pc-seed-context` skill | `docs/` scaffold + `docs-agent/` per repo |
| 5 | Per-repo fill | `Repo Context Analyst` (parallel) | `docs-agent/*.md` per repo |
| 6 | Consolidation | `Cross-Repo Consolidator` | `docs/repos/index.md`, `docs/architecture.md`, `docs/product-overview.md` |
| 7 | Enrich | `pc-confluence-enrich` skill (optional) | enriched docs |
| 8 | Validate | `pc-validate-context` skill | validation report + `.pc-manifest.json` |

See `docs/index.md` for navigation.

---

## Rehydration (missing `repos/`)

`repos/` is gitignored. If a composing repo's `repos/<repo>/` is missing, run `pc-clone-repos`
first (chat: `Use pc-clone-repos to download the product repositories`). It rebuilds `repos/`
from the committed `intake/latest-intake.json`, cloning only what's absent. On failure, show the
skill's auth checklist.

---

## Clone links

<!-- PC:CLONE-LINKS:START -->
<!-- This block is auto-populated by pc-seed-context from intake/latest-intake.json.
     Run the Python seed-context script after intake to fill clone commands here. -->
<!-- PC:CLONE-LINKS:END -->
