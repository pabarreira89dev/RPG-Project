---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Observability platform and cross-repo conventions for Project GM.
last_reviewed: null
---

# Observability

> See also: per-repo instrumentation in [`Project GM/docs-agent/observability.md`](../Project%20GM/docs-agent/observability.md).

## Tools & platform
There is no external APM/log-aggregation/alerting platform configured for this product yet (no cloud
provider has been chosen — see `CONTEXTO_PROYECTO.md`). Observability today is local-only:

| tool | purpose | access / URL |
|------|---------|-------------|
| Logback + `logstash-logback-encoder` | structured JSON application logs | local stdout of the running "Project GM" process |
| Micrometer + Spring Boot Actuator | metrics | `GET /project_gm/actuator/metrics` on the running instance |
| — | APM / distributed tracing | not set up |
| — | dashboards | not set up |
| — | alerting / alarms | not set up |

Only `Project GM` emits logs/metrics; `Project GM automatics` is a test harness with no instrumentation
of its own.

## Log conventions
Every log line is structured JSON and carries `correlationId` (from request header `X-Correlation-Id`,
or generated if absent) and, when the URL contains one, `sessionId` — both injected into MDC by
`CorrelationIdFilter` before any other filter runs. `logging.level.pab.rpg` is `DEBUG` in `local` only.
Never log full OpenAI prompts/responses at any level — only durations/error/cost summaries.

## Alarms / alerts
None defined yet. If this product is deployed, this section should be filled with where alarm
definitions live and who owns them.

## Dashboards
None yet.

## Distributed tracing
No trace propagation beyond the single `X-Correlation-Id`/MDC pattern above (no OpenTelemetry/Zipkin
integration). Since there is only one service, there is no cross-repo trace to correlate.
