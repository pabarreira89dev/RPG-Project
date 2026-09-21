---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Universal rules applying to ALL repos in Project GM. Only what is truly common belongs here.
last_reviewed: null
---

# Conventions & Best Practices (universal)

> See also: repo-specific patterns live in each repo's docs-agent/coding-patterns.md.

## Mandatory
- Documentation language: **English** for all `docs/`/`docs-agent/` files. Exception: Gherkin `.feature`
  files in `Project GM automatics` are written in Spanish (`# language: es`) — but their Cucumber step
  annotations are always English (`@Given`/`@When`/`@Then`), never `@Dado`/`@Cuando`/`@Entonces`.
- Every product-context file MUST carry frontmatter `status: draft` until human-reviewed
  (`status: reviewed`).
- Every seeded file carries frontmatter `template_version: <semver>` identifying the template structure
  it was generated from. This field is tooling-managed — do not hand-edit it.
- Do NOT duplicate info inferable from raw code; link to source instead. There is no Confluence/external
  wiki for this product — all design docs live at the workspace root (see `product-overview.md`).
- File size: keep each doc small (soft cap ~150 lines).
- One-line description lives ONLY in frontmatter `summary:`. Do NOT repeat it as a body `> TL;DR:` line.

## Common engineering rules (apply to both repos)
- Java 21, Maven (no Gradle). No Docker/Docker Compose for this product.
- Compilation and test execution are done manually by the developer — do not run `mvn`/tests
  proactively unless explicitly asked.
- IDs are UUIDs; timestamps are `Instant` in UTC.
- Never hardcode secrets (API keys, DB passwords) as YAML defaults — always `${ENV_VAR}` with no default
  for anything sensitive. This has been a recurring mistake in `Project GM/application-local.yml`
  specifically; double-check that file whenever touching OpenAI/DB config.
- Optimistic locking / idempotency patterns used in `Project GM` (session `version`, `idempotencyKey`)
  are internal to that repo, not a cross-repo convention — see its own `coding-patterns.md`.

## What does NOT belong here
Anything that varies per repo (stack-specific patterns, test frameworks, etc.). Put it in the repo's own
`docs-agent/coding-patterns.md`.
