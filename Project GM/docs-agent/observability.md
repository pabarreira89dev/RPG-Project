---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: How to instrument Project GM — structured logging, metrics, and how to validate via logs/metrics.
last_reviewed: null
---

# Project GM — Observability

## Logging
- `logback-spring.xml` uses `LogstashEncoder` (`net.logstash.logback:logstash-logback-encoder`) — all
  logs are structured JSON.
- `CorrelationIdFilter` (`@Order(HIGHEST_PRECEDENCE)`) puts `correlationId` (from request header
  `X-Correlation-Id`, or generated) and `sessionId` (parsed from the URL path when present) into MDC —
  every log line for a request carries both automatically.
- `logging.level.pab.rpg: DEBUG` in `local` only.
- OpenAI cost is **not** tagged per-session in Micrometer (unbounded cardinality); instead it's logged as
  a dedicated `openai_cost_estimate` log line, which already carries `sessionId` via MDC. WARN-level logs
  on OpenAI failure/empty narration never dump the full prompt/response.

## Metrics (Micrometer, exposed at `/actuator/metrics`)
| Metric | Meaning |
|--------|---------|
| `pab.rpg.action.duration` | Time to resolve a submitted action |
| `pab.rpg.actions.resolved` | Count of resolved actions |
| `pab.rpg.session.version.conflicts` | Incremented on `STALE_SESSION_VERSION` (both explicit mismatch and `ObjectOptimisticLockingFailureException`) |
| `pab.rpg.validation.errors` | Incremented on `INVALID_REQUEST` (400) |
| `pab.rpg.sessions.created` / `pab.rpg.sessions.retrieved` | Session lifecycle counters |
| `pab.rpg.openai.duration` / `pab.rpg.openai.errors` / `pab.rpg.openai.tokens` | OpenAI call instrumentation |
| `pab.rpg.openai.estimated.cost.usd` | Cost estimate, driven by `OpenAiProperties.costPerInputTokenUsd`/`costPerOutputTokenUsd` (0 unless configured — prices vary by model/provider, never hardcoded) |

`management.endpoints.web.exposure.include` is `health,info,metrics` by default, plus `shutdown` in
`local` only (used by the E2E suite, see `testing.md`).

## How to validate a change via logs/metrics
1. Hit the endpoint with `X-Correlation-Id: <known-value>` (or capture the generated one from the
   response) and grep the JSON logs for that value + `sessionId` to see the full request trace.
2. For a new failure path, confirm it increments the right counter (`validation.errors` for 400s,
   `session.version.conflicts` for 409s) rather than silently passing through.
3. For anything touching `MasterAdapter`, check `pab.rpg.openai.duration`/`errors` don't spike, and that
   WARN logs (if any) don't leak prompt/response content.

## Adding a new metric/alarm
Inject `MeterRegistry` into the service/handler (constructor injection, same pattern as
`GlobalExceptionHandler`/`ActionServiceImpl`) and call `.counter(...)`/`.timer(...)` at the point of
interest — no central metrics registry file to edit.
