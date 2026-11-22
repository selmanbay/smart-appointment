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

### 2026-11-22 · selman · docs

- **Done:** SDD sources in `docs/sdd/`: `WORK_HISTORY.md` (every work item with person, date, description, traceability and commit, from git and the feature logs; hours left for the team) and `DESIGN_FLOWS.md` (class diagram, three activity diagrams, sequence diagrams for UC-3, UC-4, UC-8 and UC-2, DFD level 0, component and deployment diagram, each with rationale). All ten Mermaid diagrams render with Mermaid 11.
- **State / next:** Booking-side classes are marked «planned»; update the diagrams when B4–B12 land.

### 2026-11-22 · selman · backend

- **Done:** Shared security pieces for B3: `JwtProperties`, `JwtConfig` (encoder, decoder with issuer check, role-to-authority mapping, password encoder), `AccessCookieAuthenticationFilter`, `CsrfCookieFilter`, `CorsConfig` (credentials allowed for listed origins), `ProblemResponses` (RFC 7807 for 401, 403 and 429; the rate-limit filter now uses it), an injectable `Clock`. Tests always run with `application-test.yml`.
- **Why:** Checkstyle's fan-out limit rejected the first versions of `AuthController`, `AuthService` and `SecurityConfig`; they were split (`AccountController`, `AccountService`, `CorsConfig`) instead of relaxing the rule.
- **Refs:** ADR-0007, ADR-0015

### 2026-11-22 · selman · backend

- **Done:** B2 demo data as a repeatable Flyway migration `db/seed/R__demo_data.sql`, loaded only by the `local` profile: one organization, owner, admin, two staff, five customers, two services with rules, Mon–Fri working hours, two weeks of completed appointments with recorded service times, no-shows, cancellations and ratings, and the next working week of bookings. Tests now always run with the `test` profile (surefire and failsafe) so the demo data never reaches them; `DemoDataIT` loads it on purpose. Added `make db-reset`.
- **Why:** A repeatable migration with a guard avoids clashing with the next schema version (`V2`), which the handoff's `V2__demo_data.sql` would have done.
- **State / next:** SQL and PL/pgSQL syntax checked with the PostgreSQL parser (libpg_query); `./mvnw verify` passes locally but `DemoDataIT` was skipped (no Docker). It runs in CI on the PR. Demo password is fake and documented in `backend/README.md`.
- **Refs:** handoff B2, ADR-0012

### 2026-11-22 · selman · api

- **Done:** Wrote the full contract in `api/openapi.yaml`: 35 operations covering UC-1 to UC-20 (auth, organizations, services and rules, staff, working hours and change requests, recommendations, slots, appointments with book / reschedule / cancel / status, agenda, feedback, analytics), RFC 7807 errors with booking error codes, pagination, idempotency key for booking, and `x-srs`, `x-mvp`, `x-rate-limit-group` on every operation. Added Redocly lint (`make api-lint`, CI) and Swagger UI in the backend serving the contract next to the generated description (ADR-0014). The backend image now builds from the repository root.
- **State / next:** Contract lint passes; `./mvnw verify` passes (27 tests). Swagger UI in the running backend and the new Docker build were not run locally (no Docker); `BackendApplicationIT` checks Swagger in CI. Web types are still hand-written for the prototype; generate them from the contract next.
- **Refs:** ADR-0014, CONTRIBUTING section 6

### 2026-11-22 · selman · web

- **Done:** Web skeleton: Vite 8, React 19, TypeScript 6, Mantine 9 as the only UI library with one theme (ADR-0013), shared layout components (AppFrame, Page, Section, Panel, Columns) with fixed spacing (UI_GUIDELINES section 5), React Router, TanStack Query, Vitest. ESLint boundaries rule migrated to the plugin's v7 syntax and verified to block cross-feature imports. Web Dockerfile (nginx) and compose service added.
- **Why:** MVP screens must look consistent when four people and their agents add to them; the prototype's generic AI styling was replaced (UI_GUIDELINES section 1).
- **State / next:** Lint, type check, 7 Vitest tests and the production build pass locally. Checked in the browser at 1440px and 375px. The Docker image was not built locally (no Docker); CI builds it. Screens use mock data until the API exists.
- **Refs:** ADR-0012, ADR-0013, docs/design/UI_GUIDELINES.md

### 2026-11-21 · selman · docs

- **Done:** Cleaned up the repository root: full agent rules moved to `docs/guides/AGENT_RULES.md`, team rules to `docs/guides/CONTRIBUTING.md`, the logging guide to `docs/guides/LOGGING.md`. The root keeps only `README.md` and a short `AGENTS.md` entry point; Claude Code's entry point is `.claude/CLAUDE.md`. All references and links were updated.
- **State / next:** Not verified that Claude Code picks up `.claude/CLAUDE.md` in a fresh session; Codex and Cursor read the root `AGENTS.md`.

### 2026-11-21 · selman · infra

- **Done:** Rebuilt CI as a standard pipeline: repo lint (English, actionlint, hadolint, markdownlint), separate backend lint / test / image jobs with uploaded reports, web and Android jobs ready for when those apps exist, one aggregated `ci-ok` check. Added a Security workflow (dependency review, CodeQL) and a `Makefile` that mirrors CI locally. Fixed the earlier CI failure (`hashFiles()` is not allowed in job conditions).
- **State / next:** `make lint` and `make test` pass locally (26 tests, markdownlint 0 issues). actionlint, hadolint, the Docker build and CodeQL run only in CI and have not run yet; check the first run after pushing. Import the updated ruleset (required checks changed).
- **Refs:** ADR-0010

### 2026-11-21 · selman · docs

- **Done:** Added `docs/PROJECT_MAP.md`: structure map with build markers, feature map, task routing table, test layers and commands per app, quality gates, local services. docs/guides/AGENT_RULES.md, CLAUDE.md, README and the PR template now send everyone there first.
- **State / next:** Keep the map current in the same PR as structural changes (PROJECT_MAP section 9).

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
