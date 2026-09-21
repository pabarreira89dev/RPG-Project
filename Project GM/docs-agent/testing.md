---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Test tiers, conventions, run commands and verification workflow for Project GM.
last_reviewed: null
---

# Project GM — Testing

## Tiers
| Tier | Location | Framework | Notes |
|------|----------|-----------|-------|
| Unit/integration | `src/test/java/pab/rpg/{api,domain,service}` (mirrors main packages) | JUnit 5 + Spring Boot Test | Runs against `test` profile (`application-test.yml`), MySQL `project_gm_test`, OpenAI always stubbed |
| Context load | `src/test/java/pab/rpg/ApplicationTests.java` | `@SpringBootTest` | Full context boot smoke test — catches missing env vars/misconfigured conditional beans at load time |
| E2E (API black-box) | separate Maven project `Project GM automatics/` | Cucumber 7 + JUnit 5 + REST Assured | Drives this app's real HTTP API against a running instance; see below |

## Where to add each type
- New service/business logic → unit test next to it under `src/test/java/pab/rpg/service/...`.
- New endpoint/DTO/mapping → `src/test/java/pab/rpg/api/...` (`@WebMvcTest`/`MockMvc` or
  `@SpringBootTest` as appropriate).
- New entity/repository query → `src/test/java/pab/rpg/domain/...`.
- New cross-cutting flow spanning multiple endpoints in sequence → a new `.feature` in
  `Project GM automatics/src/test/resources/features/`, not a Java test in this repo.

## Run commands (this repo)
```powershell
mvn test           # unit/integration tests, uses application-test.yml automatically
mvn clean test      # if you see a NoClassDefFoundError for a class that clearly compiles — try this
                     # before deeper investigation (stale target/classes)
mvn spring-boot:run  # manual run, local profile — requires .env.local sourced first (see env-and-config.md)
```
User runs Maven/tests manually in normal day-to-day work — do not run `mvn`/tests proactively unless
explicitly asked.

## E2E (`Project GM automatics`)
- Independent Maven project (`project_gm_autimatics`), not a module of this repo's `pom.xml`.
- Requires "Project GM" already running locally on `:8080` with context path `/project_gm` (real MySQL
  behind it) — the E2E suite does not start it.
- Feature files (Spanish Gherkin, `# language: es`; step annotations always `@Given`/`@When`/`@Then` in
  English): `sesiones_de_juego.feature`, `acciones.feature`, `combate.feature`, `misiones.feature`,
  `npcs.feature`, `autenticacion.feature`, `recorrido_completo.feature` (full 10-step journey that also
  restarts the real "Project GM" process to verify persistence across a real restart).
- `AppLifecycle` (test support) stops the app via `/actuator/shutdown` (must have
  `Content-Type: application/json` or it 415s) and relaunches it via `ProcessBuilder` — that child
  process needs `.env.local` sourced into the **same shell** running `mvn test` there (see
  `env-and-config.md`), otherwise the relaunch fails with a misleading `BeanDefinitionStoreException`.
- Auth in E2E: `X-Dev-Player-Id` header (not query/body) — `playerId` is never a request field on this
  API (see `coding-patterns.md`).

## Known gotchas
- `NoClassDefFoundError`/`ClassNotFoundException` for a class that visibly compiles → try
  `mvn clean test` before investigating classpath theories (stale `target/classes`).
- `BeanDefinitionStoreException: Failed to read candidate component class` at startup is usually **not**
  a stale-build issue — check 2 levels into the cause chain for
  `PlaceholderResolutionException: Could not resolve placeholder '<ENV_VAR>'` first (a
  `@ConditionalOnProperty` bean needing an unset env var during component scan).
- `application-test.yml` keeps `flyway.clean-disabled: false` — tests may reset the schema; never point
  `TEST_DATABASE_URL` at a shared/prod database.
