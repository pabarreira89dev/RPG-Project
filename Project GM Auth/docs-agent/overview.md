---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Purpose, stack, responsibilities and entry points of Project GM Auth (the Spring Authorization Server IdP).
last_reviewed: null
---

# Project GM Auth — Overview

## Purpose
Standalone identity provider for the Project GM product. It is the only place a Project GM user account
exists: it persists accounts (own generated UUID per user, used as the JWT `sub`) and issues RS256 JWTs.
The Android app authenticates via a custom OAuth2 "password" grant (native username/password form, no
browser — see `architecture.md` for why the original Authorization Code+PKCE/Custom Tabs design was
abandoned). Those JWTs are what "Project GM"'s `cloud` profile validates as a resource server (see
[`Project GM/docs-agent/env-and-config.md`](../../Project%20GM/docs-agent/env-and-config.md)) — before
this repo existed, `cloud`'s JWT validation was implemented but had no real issuer to point at.

## Stack
Java 21, Spring Boot 4.1.1, Maven (`groupId=pab.rpg`, `artifactId=project_gm_auth`), MySQL + Flyway,
Lombok, `spring-boot-starter-oauth2-authorization-server`. Pure JSON API — no templating engine, no
browser-facing content of any kind.

## Responsibilities
- Persist user accounts (`AppUser`: id, username, password hash, email, enabled) in MySQL.
- Expose a JSON registration API (`POST /api/v1/register`) — what the Android app's native registration
  form calls. This is the only way to create an account; no HTML form exists anywhere in this service.
- Run the standard Spring Authorization Server token endpoint (`/oauth2/token`) for a single registered
  client (the Project GM Android app, `CLIENT_SECRET_POST`) with a custom `password` grant (native login)
  plus `refresh_token` — see `api-contracts.md`.
- Customize every issued access token so `sub` = the account's own UUID (not the username) and `aud` =
  the configured audience Project GM's `cloud` profile expects — see `architecture.md`.

## Non-responsibilities
- Does not implement any game logic — it knows nothing about sessions, characters, NPCs, etc.
- Does not call "Project GM"'s API and is not called by it at request time; the only runtime coupling is
  "Project GM" fetching this service's JWKS/discovery document to validate token signatures.
- No password reset, email verification, MFA, account lockout, or dynamic OAuth2 client registration
  (single hardcoded client) — see `architecture.md` for the full list of deliberate MVP simplifications.

## Entry points
- `pab.rpg.auth.Application` — Spring Boot main class.
- `pab.rpg.auth.config.AuthorizationServerConfig` — the registered client + AS endpoints + custom grant wiring.
- `pab.rpg.auth.security.PasswordGrantAuthenticationProvider` — the custom "password" grant's actual logic.
- `pab.rpg.auth.api.AuthApiController` — the only web controller in this service: the JSON `/api/v1/register` endpoint.

## Related docs in this repo
- [`architecture.md`](architecture.md) — the token customizer wiring, in-memory AS state decision, key management.
- [`dependencies.md`](dependencies.md) — MySQL, and the trust contract with "Project GM"/the Android app.
- [`env-and-config.md`](env-and-config.md) — ports, env vars, and required value matching with "Project GM".
- [`testing.md`](testing.md) — how to manually verify the whole custom "password" grant flow end-to-end.
- [`api-contracts.md`](api-contracts.md) — every endpoint this service exposes.
