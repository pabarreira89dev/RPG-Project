# Project GM Auth — Agent instructions

## Agent context (product-context docs)

This repo has agent-ready documentation under `docs-agent/`. Read it on demand —
do NOT read the whole tree.

1. ALWAYS start at [`docs-agent/index.md`](docs-agent/index.md).
2. Read ONLY the row whose "read when…" matches your task; open that single file.
3. For cross-repo / app-wide context, see the product-level [`../docs/index.md`](../docs/index.md).
4. For the product's root agent anchor, see [`../AGENTS.md`](../AGENTS.md).

## Stack (anchor only)
Java 21, Spring Boot 4.1.1, Maven, MySQL + Flyway, Lombok,
`spring-boot-starter-oauth2-authorization-server` — see `docs-agent/overview.md` for detail.

## Conventions
- Docs with frontmatter `status: draft` are UNREVIEWED — treat as provisional.
- Prefer the docs above over re-deriving context from raw code.
- Scope tasks narrowly (e.g. "add validation to field X", not "improve module Y").
- Compilation and test execution are done manually by the developer — do not run `mvn`/tests
  proactively unless explicitly asked.
- This repo's `issuer-uri`/`audience` config MUST stay in lockstep with "Project GM"'s
  `JWT_ISSUER_URI`/`JWT_AUDIENCE` — see `docs-agent/env-and-config.md` before touching either.
