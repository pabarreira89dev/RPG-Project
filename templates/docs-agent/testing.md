---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Test tiers, conventions, and verification workflow for <REPO_NAME>.
last_reviewed: null
---

# Testing

> See also: cross-repo test conventions in `docs/testing-conventions.md` (product level, if present).

## Strategy

<PLACEHOLDER: which test tiers apply to this repo (unit / integration / e2e),
key frameworks and versions, what coverage is expected.>

## Test types — where to add them

| type | source path | framework / base class | run command |
|------|------------|------------------------|-------------|
| unit | <PLACEHOLDER> | <PLACEHOLDER> | <PLACEHOLDER> |
| integration | <PLACEHOLDER> | <PLACEHOLDER> | <PLACEHOLDER> |
| e2e | <PLACEHOLDER> | <PLACEHOLDER> | <PLACEHOLDER> |

## Adding tests for new functionality

<PLACEHOLDER: for each applicable tier, describe:
1. Unit — naming convention, file location, fixture/base class, assertion style.
2. Integration — what must be covered, how to bootstrap context (e.g. @SpringBootTest slice), test DB setup.
3. E2e — when it applies, how to trigger, environment required.>

## How to run all tests

<PLACEHOLDER: single command to run the full suite. Only non-obvious commands; else link to scripts.>

## How to run a single test

<PLACEHOLDER: command/IDE action to run one test class or method.>

## How to generate coverage

<PLACEHOLDER: command and where to find the report. Omit if the build already prints it.>

## Verification workflow

<PLACEHOLDER: ordered steps to confirm a built feature works:
1. Run unit tests
2. Run integration tests
3. Smoke-test in environment X
4. Check log/metric output (link to observability.md if present)>
