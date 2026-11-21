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

### 2026-11-21 · selman · backend
- **Done:** Backend skeleton: Spring Boot 4.2.0 on Java 21, profiles (`local`, `prod`), typed config records, stateless security with CORS, pluggable rate limiting (Bucket4j, off locally), RFC 7807 errors, correlation id, Dockerfile and compose service. Spotless, Checkstyle 14.4.0, ArchUnit (8 rules) and JaCoCo wired into `./mvnw verify`. Load tests moved to `backend/load-tests/`; Android test folders added (ADR-0009).
- **Why:** Every feature builds on the same infrastructure; hosting later is configuration only (ADR-0008).
- **State / next:** `./mvnw verify` passes with 26 tests on JDK 21. `BackendApplicationIT` (Flyway against real PostgreSQL) and the Docker image were not run because Docker is not installed on this machine. Until JWT exists every API route except `/api/v1/auth/**` and health returns 401.
- **Refs:** ADR-0007, ADR-0008, ADR-0009

### 2026-11-21 · selman · infra
- **Done:** Added quality gates: web ESLint + Prettier config, backend Checkstyle config, `english-only` CI check. Defined hosting-ready patterns (profiles, typed config, pluggable rate limiting, layered tests).
- **Why:** Prevent spaghetti code from agent-written changes; keep the local MVP deployable later by configuration only.
- **State / next:** ESLint and Checkstyle configs verified on sample files in a temp project, not yet in the real `web/` and `backend/` (no code there). Wire Spotless, Checkstyle and ArchUnit into `pom.xml` and the ESLint dev dependencies into `package.json` when the skeletons are generated.
- **Refs:** ADR-0007, ADR-0008

### 2026-11-21 · selman · docs
- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
