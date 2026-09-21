---
status: draft
template_version: 1.0.1
content_version: 1.0.0
fill: team
summary: Observability platform, tools, and cross-repo conventions for <PRODUCT_NAME>.
last_reviewed: null
---

# Observability

> See also: per-repo instrumentation in each repo's docs-agent/observability.md.

## Tools & platform

| tool | purpose | access / URL |
|------|---------|-------------|
| <PLACEHOLDER> | APM / tracing | <PLACEHOLDER> |
| <PLACEHOLDER> | metrics | <PLACEHOLDER> |
| <PLACEHOLDER> | log aggregation | <PLACEHOLDER> |
| <PLACEHOLDER> | alerting / alarms | <PLACEHOLDER> |

## Log conventions

<PLACEHOLDER: structured logging format, mandatory MDC/correlation fields, log level policy.>

## Alarms / alerts

<PLACEHOLDER: where alarm definitions live (IaC repo / config repo), naming convention,
who owns them, how to request a new alarm.>

## Dashboards

<PLACEHOLDER: product-wide dashboard URL(s); what each shows.>

## Distributed tracing

<PLACEHOLDER: trace propagation headers, sampler config, how to correlate across repos.>
