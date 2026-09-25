---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Configuration, profiles and environment variables for Project GM — not inferable from code alone.
last_reviewed: null
---

# Project GM — Environment & Configuration

## Profiles
| Profile | Used for | Identity | OpenAI | Notes |
|---------|----------|----------|--------|-------|
| `local` (default) | Manual dev runs | `DevelopmentIdentityFilter`, fixed UUID `00000000-0000-0000-0000-000000000001` unless header `X-Dev-Player-Id` overrides it | Real, `OPENAI_ENABLED` required (no default) | `/actuator/shutdown` enabled — used by the E2E suite's real-restart test |
| `test` | `mvn test` in this repo | Same dev bypass, same fixed UUID | Always disabled (`StubMasterAdapter`) | Deterministic, no network |
| `cloud` | Deployed environments | Real JWT issued by `Project GM Auth` (`JWT_ISSUER_URI`+`JWT_AUDIENCE`), `playerId` = `sub` claim (the account's own UUID, not derived from anything in this repo) | Real, no default for any secret | `/actuator/shutdown` intentionally NOT exposed |

Base `application.yml` sets safe defaults (`security.development-user-enabled: false`, Flyway-managed
schema with `ddl-auto: validate`, context path `/project_gm`).

## Required environment variables (no defaults — profile fails to start without them)
- `local`: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `OPENAI_ENABLED`, `OPENAI_API_KEY`,
  `OPENAI_BASE_URL`, `OPENAI_MODEL`, `OPENAI_MAX_OUTPUT_TOKENS`, `OPENAI_TEMPERATURE`,
  `OPENAI_CONNECT_TIMEOUT`, `OPENAI_READ_TIMEOUT`.
- `cloud`: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `OPENAI_API_KEY`, `OPENAI_MODEL`,
  `JWT_ISSUER_URI`, `JWT_AUDIENCE` (most others have safe defaults in this profile, e.g.
  `OPENAI_ENABLED:true`, `OPENAI_BASE_URL`, timeouts, pool sizes). `JWT_ISSUER_URI`/`JWT_AUDIENCE` must
  exactly match `Project GM Auth`'s own `AUTH_ISSUER_URI`/`AUTH_JWT_AUDIENCE` — see
  [`Project GM Auth/docs-agent/env-and-config.md`](../../Project%20GM%20Auth/docs-agent/env-and-config.md).
- `test`: nothing required — `application-test.yml` hardcodes DB creds/URL with an overridable
  `TEST_DATABASE_URL` default, and OpenAI is always disabled.

## Where local values live
`Project GM/.env.local` (gitignored, dotenv format) stores the actual values for all `local`-profile env
vars. **Spring Boot/Maven do NOT auto-load it** — it must be sourced into the current PowerShell session
before `mvn spring-boot:run`, e.g.:
```powershell
Get-Content ".env.local" | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
}
```
Gotcha (bit an E2E run before): a child process launched via `ProcessBuilder` (the `Project GM
automatics` E2E "restart the app" step, `AppLifecycle`) inherits env vars from the JVM that started
`mvn test`, **not** from whatever interactive terminal the user originally used — so `.env.local` must be
loaded into the *same* shell that runs `mvn test` in `Project GM automatics`, not just the shell used to
start "Project GM" manually.

## Secrets hygiene
Never hardcode a real API key/DB password as a YAML default (`${OPENAI_API_KEY:sk-...}`). If one is ever
found already committed, check `git log --all -p -- <file>` before assuming it reached `origin` — only
escalate to "rotate the key" if it was actually pushed.

## Database
MySQL 8, service `MySQL80`. Client at `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe` (not on
PATH). Databases: `project_gm_local` (dev), `project_gm_test` (test profile). Reset script:
`project_gm.sql` at the workspace root (`DROP/CREATE DATABASE project_gm`).

## Ports / process
Runs on `:8080`, context path `/project_gm`. `mvn spring-boot:run` without `fork=false` spawns a child
`java` process that actually listens on the port — when killing a stuck instance, find the real PID via
`Get-NetTCPConnection -LocalPort 8080` + `Get-CimInstance Win32_Process`, or prefer POST to
`/actuator/shutdown` (with `Content-Type: application/json`, `local` profile only).
