---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Universal rules applying to ALL repos. Only what is truly common belongs here.
last_reviewed: null
---

# Conventions & Best Practices (universal)

> See also: repo-specific patterns live in each repo's docs-agent/coding-patterns.md.

## Mandatory
- Documentation language: **English**.
- Every product-context file MUST carry frontmatter `status: draft` until human-reviewed (`status: reviewed`).
- Every seeded file carries frontmatter `template_version: <semver>` identifying the template structure it was generated from. This field is **tooling-managed** — do NOT hand-edit it. It lets validation detect version mixing and lets `pc-migrate-template` upgrade the tree. See the kit's `docs/TEMPLATE-VERSIONING.md`.
- Do NOT duplicate info inferable from raw code; link to source/Confluence instead.
- File size: keep each doc small (soft cap ~150 lines). Crossing the cap triggers a structure review,
  not an automatic split. Use a folder `<name>/` with its own `index.md` only when at least two child
  documents have independent `read when` tasks and at least one additional independent boundary
  (rules, change cycle, ownership/sources, or standalone context). Sequential steps, headings,
  examples, depth, and size alone do not justify separate files.
- One-line description lives ONLY in frontmatter `summary:`. Do NOT repeat it as a body `> TL;DR:` line — it duplicates the summary and the index and just burns the agent's context. *When* to open a file is the index's job (`read when…` in `index.md`). Inside a file, add a `> See also:` line only to point to a related sibling/parent doc the index does not cover.

## Common engineering rules
<!-- PC:STANDARD-CONVENTIONS:START -->
<PLACEHOLDER: baseline standard conventions are seeded here from intake.standard_conventions by the Level 1 consolidator; tier-derived universal rules are appended below — include ONLY rules universal to every repo.>
<!-- PC:STANDARD-CONVENTIONS:END -->

## What does NOT belong here
Anything that varies per repo (stack-specific patterns, test frameworks, etc.). Put it in the repo's product-context.
