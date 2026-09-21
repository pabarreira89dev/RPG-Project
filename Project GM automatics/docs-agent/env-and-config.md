---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: What must be running, ports, and environment variables for Project GM automatics.
last_reviewed: null
---

# Project GM automatics — Environment & Configuration

## Prerequisite: "Project GM" must already be running
This suite has no config profiles of its own — its only "configuration" is
`ApiConfig.BASE_URI = "http://localhost:8080/project_gm"` (hardcoded, no override mechanism today).
Before running any test here, "Project GM" must already be up on that exact host/port/context-path,
using its `local` profile with `.env.local` sourced into the **same shell** that will later run
`mvn test` in `Project GM automatics/` — see
[`Project GM/docs-agent/env-and-config.md`](../../Project%20GM/docs-agent/env-and-config.md) for how to
load it:
```powershell
cd "../Project GM"
Get-Content ".env.local" | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
}
mvn spring-boot:run
```
Then, in another shell, run this suite's tests. For `recorrido_completo.feature` specifically (the one
that restarts "Project GM" mid-test), the shell that runs `mvn test` **here** must also have
`.env.local` sourced — the relaunched child process inherits env vars from that JVM, not from the shell
that started "Project GM" manually the first time.

## No env vars of its own
This project reads no environment variables and has no `application*.yml`/`.env` file — everything it
needs (base URI, seed UUIDs) is a Java constant in `support/`.

## Ports
Talks to `localhost:8080` only. Does not open any port of its own (it's a test client, not a server).

## Database
No direct DB connection — all state is observed indirectly through "Project GM"'s HTTP API.
