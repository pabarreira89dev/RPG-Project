# Project GM automatics — Agent instructions

## Agent context (product-context docs)

This repo has agent-ready documentation under `docs-agent/`. Read it on demand —
do NOT read the whole tree.

1. ALWAYS start at [`docs-agent/index.md`](docs-agent/index.md).
2. Read ONLY the row whose "read when…" matches your task; open that single file.
3. For cross-repo / app-wide context, see the product-level [`../docs/index.md`](../docs/index.md).
4. For the product's root agent anchor, see [`../AGENTS.md`](../AGENTS.md).

## Stack (anchor only)
Java 21, Maven, Cucumber 7 + JUnit 5 + REST Assured — see `docs-agent/overview.md` for detail.
No Spring context of its own; this repo only drives `Project GM`'s HTTP API as a black box.

## Conventions
- Docs with frontmatter `status: draft` are UNREVIEWED — treat as provisional.
- Prefer the docs above over re-deriving context from raw code.
- Scope tasks narrowly (e.g. "add a scenario for X", not "improve module Y").
- `Project GM` must already be running locally before any test here can pass — see
  `docs-agent/env-and-config.md`.
- Running `recorrido_completo.feature` restarts the developer's local `Project GM` process — warn
  before running it if unsure whether that's expected.
