---
status: draft
template_version: 1.0.1
summary: Functionalities this repo implements — links to product-level detail.
last_reviewed: null
---

# Functionality Map

> See also: full functionality catalog at `docs/functionality/index.md` (product level).
> Escape hatch: if this repo implements only 1–2 functionalities, add a `## Functionalities` section to overview.md instead of maintaining this file.

## This repo's contributions

| functionality | role | interfaces | files | product-level detail |
|---|---|---|---|---|
<!-- role       : this repo's part in the functionality — initiator (UI/trigger) | provider (serves it) |
                  participant (worker/consumer/job) | library (shared contract) | standalone (no cross-repo boundary).
     interfaces : boundary-crossing tokens, separated by ' | ', each as "<direction>:<kind> <token>".
                  Write "internal" if the functionality crosses no repo boundary. Tech-agnostic on purpose.
                    direction = provides | consumes | shares (a contract used on both sides)
                    kind      = rest | graphql | grpc | event | queue | dto | type | cli | lib | db | file | webhook | other
                  Examples: "provides:rest POST /releases" | "consumes:rest GET /api/releases" |
                            "provides:event release.created" | "shares:dto ReleaseDto" | "consumes:lib common-dto"
                  When a gateway/BFF/ingress rewrites the path, declare the peer spelling inline so the join
                  still works: "consumes:rest GET /api/v1/orders (alias GET /orders)". A REST alias may omit
                  the method. Add an alias only from real config evidence, never to force a join.
                  These tokens are the cross-repo JOIN KEY the Stage 6 consolidator uses to link the SAME
                  functionality across repos (consumes<->provides, shares<->shares) regardless of stack.
     files      : repo-relative source path(s) that implement this functionality, from facts.json evidence
                  (exposes.endpoints[].source for provider rows, consumes.http_clients[] for consumer rows).
                  Basename(s), '/'-normalized, comma-separated, max 3. Write [NOT DETECTED] if no source path exists.
     product-level detail : owned by the Stage 6 consolidator — a link to docs/functionality/<slug>.md
                  for CROSS-REPO functionalities, or — for single-repo. At Stage 5 leave it [PENDING — set by consolidator]. -->
| <PLACEHOLDER: name> | <PLACEHOLDER: role> | <PLACEHOLDER: direction:kind token, or internal> | <PLACEHOLDER: file basename(s), or [NOT DETECTED]> | [PENDING — set by consolidator] |
