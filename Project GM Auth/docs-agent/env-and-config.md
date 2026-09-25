---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: Profiles, ports, and environment variables for Project GM Auth — including the exact values that must match "Project GM".
last_reviewed: null
---

# Project GM Auth — Environment & Configuration

## Profiles
| Profile | Purpose | Signing key | AS state |
|---------|---------|-------------|----------|
| `local` (default) | Manual dev/testing, incl. dry-running the `cloud` flow locally | Ephemeral RSA keypair generated every boot | In-memory (always, both profiles) |
| `cloud` | Real deployment target for the Android app's `cloud` flavor + "Project GM"'s `cloud` profile | Loaded from `AUTH_JWK_PRIVATE_KEY_PEM`/`AUTH_JWK_PUBLIC_KEY_PEM` (PEM strings, no default) | In-memory |

## Ports
`server.port: 9000` (fixed, both profiles) — separate from "Project GM"'s `8080`. No `context-path`
(AS/OIDC well-known discovery paths are conventionally served from the root).

## Env vars (`local`)
`DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD` (no default, same pattern as "Project GM") — point
these at a new local database, e.g. `project_gm_auth_local`, on the same local `MySQL80` service already
set up for "Project GM". Everything else in `local` has a fixed value in `application-local.yml` (no env
vars needed) — see that file for the exact `issuer-uri`/`audience`/`android-client-secret` used for local
manual testing.

## Env vars (`cloud`)
| Var | Must match |
|-----|------------|
| `DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD` | (this service's own DB, no cross-repo constraint) |
| `AUTH_ISSUER_URI` | **Exactly** "Project GM"'s `JWT_ISSUER_URI` cloud env var |
| `AUTH_JWT_AUDIENCE` | **Exactly** "Project GM"'s `JWT_AUDIENCE` cloud env var |
| `AUTH_ANDROID_CLIENT_SECRET` | **Exactly** the Android app's `cloud` flavor `AUTH_CLIENT_SECRET` buildConfigField (`app/build.gradle.kts`) — not a real secret (ships in the APK), but must still match on both sides or the custom password grant fails with `invalid_client` |
| `AUTH_JWK_PRIVATE_KEY_PEM` / `AUTH_JWK_PUBLIC_KEY_PEM` | An RSA keypair generated once (e.g. `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048` then `openssl rsa -pubout`), stored as secrets — never regenerated on each deploy or every issued refresh token becomes invalid |

There is no automated check that any of these match their Android/Project GM counterparts — this is a
manual deploy-time (or even manual build-time, for the client secret) contract. Getting `AUTH_ISSUER_URI`/
`AUTH_JWT_AUDIENCE` wrong fails every cloud request at "Project GM"'s `SecurityConfig.jwtDecoder()` with a
JWT validation error (not a helpful one from this service). Getting `AUTH_ANDROID_CLIENT_SECRET` wrong
fails every login/refresh from the app with `invalid_client` (confirmed to happen in practice once — the
Android build had the `local` profile's secret hardcoded into the `cloud` flavor by mistake).

## Database
Owns exactly one table, `app_user` (see `V1__create_app_user.sql`). Does not share a database or schema
with "Project GM" — separate MySQL database entirely.
