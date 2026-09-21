---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Cross-repo test environment, shared fixtures, and quality gates for Project GM.
last_reviewed: null
---

# Testing Conventions (cross-repo)

> See also: per-repo test details in [`Project GM/docs-agent/testing.md`](../Project%20GM/docs-agent/testing.md).

## E2E environment
There is no shared/deployed E2E environment — `Project GM automatics` runs against a real local instance
of "Project GM" the developer starts manually (`mvn spring-boot:run` in `Project GM/`, `local` profile,
`.env.local` sourced into the same shell), listening on `localhost:8080` with context path
`/project_gm`, backed by local MySQL (`project_gm_local`/`project_gm_test`). To trigger a run: start
"Project GM", then `mvn test` inside `Project GM automatics/`.

## Shared fixtures / utilities
None — the two Maven projects are fully independent, no shared test library or base class. Fixed seed
data (location/NPC/quest UUIDs) is duplicated by convention as small catalog classes on the E2E side
(`LocationCatalog`, `NpcCatalog` in `Project GM automatics`) mirroring the Flyway seed data in
`Project GM`'s migrations — keep both in sync manually if seed data changes.

## Quality gates
No CI is configured for this product yet (no workflow files beyond `.github/modernize/`). There are no
enforced coverage thresholds or required test tiers before merge — the developer runs `mvn test` in each
project manually and decides when to proceed. If CI is introduced later, document the gate names here.
