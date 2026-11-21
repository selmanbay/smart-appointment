# Platform and infrastructure

## Scope

| | |
|---|---|
| SRS | — |
| Use cases | — |
| Backend | `backend/src/main/java/com/smartappointment/common/` |
| Web | `web/src/app/`, `web/src/api/`, `web/src/components/` |
| Android | — |

## Status

In progress

## Open questions

- [ ] When are the backend and web Dockerfiles added to docker-compose?
- [ ] Which coverage threshold do we start with once core features exist?

## Log

### 2026-11-21 · selman · infra
- **Done:** Added quality gates: web ESLint + Prettier config, backend Checkstyle config, `english-only` CI check. Defined hosting-ready patterns (profiles, typed config, pluggable rate limiting, layered tests).
- **Why:** Prevent spaghetti code from agent-written changes; keep the local MVP deployable later by configuration only.
- **State / next:** ESLint and Checkstyle configs verified on sample files in a temp project, not yet in the real `web/` and `backend/` (no code there). Wire Spotless, Checkstyle and ArchUnit into `pom.xml` and the ESLint dev dependencies into `package.json` when the skeletons are generated.
- **Refs:** ADR-0007, ADR-0008

### 2026-11-21 · selman · docs
- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
