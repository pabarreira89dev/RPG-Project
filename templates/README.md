---
status: draft
summary: High-level overview of the product-context structure for team leads and managers — what gets generated, why, and the principles behind it.
last_reviewed: null
---

# Product Context — Structure Overview (for Team Leads & Managers)

> **Audience:** Engineering managers and team leads.
> **Scope:** The *structure* of the documentation we generate — two levels: **product level** (`docs/`) and **repo level** (`docs-agent/`). (Agents and skills that produce it are out of scope here.)
> **Goal:** Understand what we create, what each file is for, and the principles that make it worth maintaining.

---

## 1. Why this matters — the first step to using AI in your projects

Before a team can get real value from AI assistants, the AI needs **context about the product, not just the code**. Reading raw code tells the AI *what* the code does; it does **not** tell it *why* it exists, *which repos* a change touches, *how the team works*, or *what is not obvious from the source*.

This starter kit gives every product a thin, structured **context layer** that an AI agent reads before working. Concretely, this delivers:

- **Context on demand** — the AI knows your product's domain, architecture, conventions and ways of working from day one, instead of re-discovering them on every task.
- **More accuracy, fewer mistakes** — the agent stops guessing. It follows your real conventions and touches the right repositories.
- **Lower token cost** — instead of reading thousands of lines of code to "understand the project", the agent reads a few small, targeted files. Less context burned = cheaper and faster.
- **Less technical debt** — decisions, gotchas and cross-repo contracts that normally live only in people's heads become written, reviewable knowledge.
- **A clear impact map** — given a new feature, the agent (and any engineer) can immediately see *which repositories must be modified* and what the blast radius is.

In short: **this is the foundation that makes every later AI use case faster, cheaper and more reliable.** It is the prerequisite, not an optional extra.

---

## 2. The two levels

| Level | Lives where | Answers the question |
|-------|-------------|----------------------|
| **Application level** (`docs/`) | Once per product (central context repo/folder) | "What is this product, how do its repos fit together, what rules apply everywhere?" |
| **Repo level** (`docs-agent/`) | A `docs-agent/` folder inside **each** repository (under `repos/`) | "What is *this* repo, how is it built, how do I safely change it?" |

The product level (`docs/`) acts as the **map**; each repo level (`docs-agent/`) acts as the **detailed street view** of one location. Agents navigate top-down: start at the product index (`docs/index.md`), find the relevant repo(s), then dive into that repo's context.

---

## 3. Structure diagram — a product with 3 projects

This is what the structure looks like for a product composed of **3 repositories** (`repo-a`, `repo-b`, `repo-c`):

```
PRODUCT
│
├── docs/                               ← ONE per product (the map)
│   ├── index.md                        ← root entry point: routes the agent
│   ├── product-overview.md             ← what the product is
│   ├── architecture.md                 ← how the 3 repos interact
│   ├── conventions.md                  ← rules that apply to ALL repos
│   ├── observability.md                ← product-wide monitoring platform
│   ├── testing-conventions.md          ← cross-repo test policy (optional)
│   ├── functionality/
│   │   ├── index.md                    ← catalog of product features
│   │   ├── _functionality.md           ← template for a new feature entry
│   │   └── <feature>.md                ← one file per feature (added on demand)
│   └── repos/
│       └── index.md                    ← repo catalog + impact map (3 repos listed)
│
├── AGENTS.md                           ← root agent anchor; carries clone links
├── .github/                            ← pc-* skills, agents, copilot instructions
├── .gitignore                          ← includes: repos/
├── .git/                               ← the PRODUCT repo's own git (its own remote)
│
└── repos/                              ← gitignored; populated at runtime by clone-repos
    ├── repo-a/                         ← an INDEPENDENT git repo (its own remote)
    │   ├── .git/                       ← its own git history — arrives with the clone
    │   ├── src/                        ← the repo's REAL code (the actual project)
    │   ├── docs-agent/                 ← agent docs, versioned in THIS repo
    │   │   ├── index.md                ← repo entry point: routes the agent
    │   │   ├── overview.md             ← purpose, stack, responsibilities
    │   │   ├── architecture.md         ← internal architecture
    │   │   ├── dependencies.md         ← deps + impact map (events in/out)
    │   │   ├── coding-patterns.md      ← non-obvious repo patterns
    │   │   ├── env-and-config.md       ← config not inferable from code
    │   │   ├── testing.md              ← test tiers + run commands
    │   │   │   ── optional, only if applicable: ──
    │   │   ├── api-contracts.md        ← if the repo exposes an API
    │   │   ├── domain-model.md         ← if the domain is rich
    │   │   ├── observability.md        ← if it needs metrics/alarms
    │   │   └── functionality-map.md    ← features this repo implements
    │   └── AGENTS.md                   ← per-repo agent anchor
    ├── repo-b/
    │   ├── .git/  src/                 ← own git + real code (same shape as repo-a)
    │   ├── docs-agent/                 ← same structure as repo-a
    │   │   └── … (index.md, overview.md, …)
    │   └── AGENTS.md
    └── repo-c/
        ├── .git/  src/                 ← own git + real code (same shape as repo-a)
        ├── docs-agent/                 ← same structure as repo-a
        │   └── … (index.md, overview.md, …)
        └── AGENTS.md
```

### On-demand concept routing

After the standard context exists, `pc-build-concepts` may create a small, lazy concept tree. It
does not seed a concept template or alter normal pipeline output:

```text
docs/concepts/
    index.md                 # routing table
    codec-profile.md          # Read when / Agent-relevant rules / Sources, plus whatever else it needs
```

Shared or cross-repo concepts live in `docs/concepts/`; a concept internal to one repository
lives only in that repository's `docs-agent/concepts/`. The parent index receives a routing row
only when the first concept exists. Functional flows, architecture decisions, and repo-wide rules
remain in their existing appropriate documents rather than becoming concepts.

> **Note on `.git/` and `src/`:** these are not part of the starter kit — they appear at
> runtime. Each `repos/<repo>/` is a normal `git clone`, so it arrives with its own `.git/`
> (independent history + remote) and its real `src/` (the actual project code). The kit only
> seeds `docs-agent/` into each clone. That is why `repos/` is gitignored ENTIRELY in the
> product repo: every composing repo is versioned in its own git, not here.

**Key idea:** there is exactly **1 product-level** context (`docs/`) and **N per-repo** contexts (`docs-agent/`, one per repo, so **3** in this example). The `docs/repos/index.md` is the single place that links all three repos together and shows their dependencies.

### 3.1 Box view — application on top, repos connected below

The same structure seen as connected boxes. The **application level** sits on top; its `docs/repos/index.md` (the repository catalog) is the hub that links **down** to each repo's own context.

```
┌────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LEVEL                         │
│                               docs/                                │
│                                                                    │
│   index.md                                                         │
│   - product-overview.md                                            │
│   - architecture.md                                                │
│   - conventions.md                                                 │
│   - observability.md                                               │
│   - functionality/....                                             │
│                                                                    │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │  repos/index.md  →  repository catalog + impact map          │ │
│   │  ┌──────────┐   ┌──────────┐   ┌──────────┐                  │ │
│   │  │  repo-a  │   │  repo-b  │   │  repo-c  │  (+ "depends on")│ │
│   │  └────┬─────┘   └────┬─────┘   └────┬─────┘                  │ │
│   └───────┼──────────────┼──────────────┼────────────────────────┘ │
└───────────┼──────────────┼──────────────┼──────────────────────────┘
            │              │              │
            │ links to     │ links to     │ links to
            ▼              ▼              ▼
   ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
   │     REPO-A     │ │     REPO-B     │ │     REPO-C     │
   │   docs-agent/  │ │   docs-agent/  │ │   docs-agent/  │
   │ ─────────────  │ │ ─────────────  │ │ ─────────────  │
   │ index.md       │ │ index.md       │ │ index.md       │
   │ overview.md    │ │ overview.md    │ │ overview.md    │
   │ architecture   │ │ architecture   │ │ architecture   │
   │ dependencies   │ │ dependencies   │ │ dependencies   │
   │ …              │ │ …              │ │ …              │
   └────────────────┘ └────────────────┘ └────────────────┘
```

**How to read it:** an agent enters at the top, opens `docs/repos/index.md`, picks the repo(s) its task touches (using the #tags and the "depends on" impact map), and follows the link **down** into that repo's `docs-agent/index.md` (at `repos/<repo>/docs-agent/index.md`). The arrows are one-way navigation: the catalog points to each repo, and each repo's `index.md` points back up to the product-level context (`docs/`).

---

## 4. File descriptor tables

### 4.1 Application-level files (`docs/`)

| File | Purpose / utility |
|------|-------------------|
| `index.md` | **Root entry point.** Routing table that tells an agent *which file to open for which task* ("read when…"). Nothing is read blindly. |
| `product-overview.md` | What the product is: purpose, functional domain, capabilities, stakeholders. Business context, not code. |
| `architecture.md` | **Inter-repo** architecture: how the repos/services connect, integration & data flow, cross-cutting concerns. Not the internals of any single repo. |
| `conventions.md` | Universal rules that apply to **every** repo (language, frontmatter policy, file-size rule). Only what is truly common. |
| `observability.md` | Product-wide observability: platform/tools, log conventions, alarms, dashboards, distributed tracing. |
| `testing-conventions.md` | *(Optional)* Cross-repo test environment, shared fixtures and quality gates. Filled only if there is shared test infra. |
| `functionality/index.md` | **Feature catalog.** Each functionality with its owning repos, criticality and a link to detail. The functional entry point. |
| `functionality/_functionality.md` | **Template** to create a new feature file. Copied to `<feature>.md` when a feature is added. |
| `functionality/<feature>.md` | One file per feature: what it does, which repos are involved, integration points, how to add/modify it. Created on demand. |
| `repos/index.md` | **Repository catalog + impact map.** Lists every repo with #tags, purpose, "depends on" and a link to that repo's context. The map that connects everything. |

### 4.2 Repo-level files (`repos/<repo>/docs-agent/`)

| File | Required? | Purpose / utility |
|------|-----------|-------------------|
| `index.md` | ✅ Core | **Repo entry point.** Routing table for this repo + link back to the product-level context (`docs/`). |
| `overview.md` | ✅ Core | Purpose, stack, responsibilities (and non-responsibilities), entry points. Read first, always. |
| `architecture.md` | ✅ Core | Internal architecture: layers/structure and key decisions/trade-offs an agent cannot infer from code. |
| `dependencies.md` | ✅ Core | Internal (other repos) + external dependencies, events consumed/published. This repo's **impact map**. |
| `coding-patterns.md` | ✅ Core | Repo-specific **non-obvious** patterns and anti-patterns/gotchas. |
| `env-and-config.md` | ✅ Core | Configuration and environment knowledge **not inferable from code** (no secrets). |
| `testing.md` | ✅ Core | Test tiers, where to add each type, run commands, coverage, verification workflow. |
| `api-contracts.md` | ⚪ Optional | Exposed API endpoints and contracts. Only if the repo exposes an API. |
| `domain-model.md` | ⚪ Optional | Core business entities and rules. Only if the domain is rich. |
| `observability.md` | ⚪ Optional | How to instrument *this* repo: custom metrics, adding alarms, validating via logs/metrics. |
| `functionality-map.md` | ⚪ Optional | Which product features this repo implements and its specific contribution. |

> **Note on flexibility:** Optional files appear only when they add value. Every `index.md` has a *"Custom / Business-specific (extend here)"* section so teams can add their own `.md` files without breaking the structure.

---

## 5. The principles behind the structure

These are the design principles of the kit. Each one is grouped below with **how it is materially enforced** in the generated structure.

### A. Built for machines, not humans (Agent-Oriented + Token Efficiency)
*Principles 1 & 4.*
The docs are written to be consumed by AI agents: concise, direct, no filler — but containing everything an agent needs to tackle a task.
- **How:** Files are deliberately small (soft cap ~150 lines). Crossing the cap triggers a review,
  not an automatic split: use a folder with its own `index.md` only when at least two children have
  independent `read when` tasks and rules/change boundaries. No monolithic mega-docs. The one-line
  description lives **only** in the frontmatter `summary:`, never repeated in the body — so the
  agent doesn't burn context reading the same sentence twice.

### B. Read only what you need (Modularity + Functional & Technical scope)
*Principles 2 & 5.*
An agent should load the *minimum* set of files for its specific task — whether the task is a deep technical change or a functional feature definition.
- **How:** Every `index.md` is a **routing table** with a *"read when…"* column. The agent matches its task to a row and opens that single file. Files cross-link with `> See also:` instead of duplicating content. The application level serves the functional view (`functionality/`) and the repo level serves the technical view — both reachable from the indexes.

### C. Clear dependency & impact map
*Principle 3.*
Given a new feature, an agent must instantly know *which repos to touch* and the blast radius of a change.
- **How:** `docs/repos/index.md` lists every repo with a **"depends on"** column — the impact map. Each repo's `docs-agent/dependencies.md` documents internal/external deps and events in/out. `docs/functionality/<feature>.md` lists the repos involved per feature. Together these answer "what do I need to change?" before any code is read.

### D. Only non-inferable knowledge
*Principles 8 & 9.*
We never document what the AI can already read straight from the code. Generating/reading the docs must cost **less** than the value they provide.
- **How:** Every template explicitly asks for *"non-obvious info only"* and says *"link, do not copy"* (e.g. link to OpenAPI, manifests, Confluence instead of duplicating them). `conventions.md` makes this a mandatory rule. The result is a small, high-signal layer — cheap to read, cheap to keep.

### E. Human review gate (State Metadata)
*Principle 6.*
Generated content must be verified by a human before it is trusted.
- **How:** **Every** file carries YAML frontmatter with `status: draft`. This forces the team to review each file and flip it to `status: reviewed`. Nothing is silently trusted as ground truth.

### F. A base, not a cage (Flexibility + Maintainability)
*Principles 7 & 10.*
The structure is a starting point teams can extend, and it must be easy to keep up to date.
- **How:** Each `index.md` has a *"Custom / Business-specific (extend here)"* section for new files. Optional files exist only when relevant. Small, single-purpose files mean an update touches one short file — not a giant document — so maintenance stays cheap and changes are easy to spot in review.

---

## 6. One-paragraph summary for a steering meeting

> *We generate a thin, structured context layer for each product: one **product-level** map (`docs/` — product overview, inter-repo architecture, conventions, feature catalog and a repo impact map) plus one **per-repo** context inside every repository (`docs-agent/` — overview, architecture, dependencies, patterns, tasks, config, testing). It is written for AI agents — small, modular, link-based files that contain only what can't be read from the code, each gated behind a human `draft → reviewed` review. The payoff: AI assistants work with real product context from day one — more accurate, cheaper in tokens, and aware of exactly which repos a change impacts. This is the foundation step before any team scales AI in their projects.*

---

## 7. Where we are now and what comes next
 
The rollout is deliberately staged: validate the structure in reality, automate its creation, then operate it as a living product asset.

| Phase | Status | Focus |
|-------|--------|-------|
| Phase 1 | ✅ DONE | Structure created and validated with the core team, based on pilot inputs. |
| Phase 2 | 🔄 IN PROGRESS | Validate the structure with pilots and adjust it based on real use. |
| Phase 3 | ⏭ NEXT | Create agents/skills to build context: clone repos, scaffold from templates, and combine template + repos into a complete structure. |
| Phase 4 | ⏭ NEXT | Maintain product context as a living asset. |
| Phase 5 | ⏭ NEXT | Provide foundational product agents and skills. |

> 📎 Full context and background: [Product Context — Confluence](https://verisure.atlassian.net/wiki/pages/resumedraft.action?draftId=2666659927)
