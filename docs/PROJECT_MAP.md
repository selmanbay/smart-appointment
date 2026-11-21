# Project map

The entry point for anyone working in this repository, human or agent. It says where everything is, which files to read for a task, and how to build, test and check your work. Keep it current (section 9).

Smart Appointment is a multi-organization appointment system: customers book, cancel and reschedule; staff record service progress; owners manage services, staff and rules; the system recommends staff by estimated waiting time. Requirements: [`srs/SRS-v1-2026-10-27.pdf`](srs/SRS-v1-2026-10-27.pdf).

## 1. Start here

Read in this order before your first change:

1. This file
2. [`STATUS.md`](STATUS.md): what is done, in progress and next
3. [`docs/guides/AGENT_RULES.md`](guides/AGENT_RULES.md) (agents) or [`docs/guides/CONTRIBUTING.md`](guides/CONTRIBUTING.md) (humans): the rules
4. The feature file for your task: [`features/`](features/README.md)
5. The decisions it mentions: [`decisions/`](decisions/README.md)

Branches: work on your personal `develop/<name>` branch, or a shared `feature/<short-name>` branch when several people build one feature; everything reaches `main` through a PR (ADR-0011).

## 2. Structure map

```text
smart-appointment/
│
├── backend/                         Spring Boot REST API, Java 21                     [built]
│   ├── pom.xml, mvnw, Dockerfile    build, pinned versions, container image
│   ├── config/checkstyle.xml        naming, size, coupling rules (ADR-0007)
│   ├── config/README.md             quality tooling and ArchUnit rules
│   ├── load-tests/                  k6 scenarios for SRS P1, P2                       [empty]
│   └── src/
│       ├── main/java/com/smartappointment/
│       │   ├── BackendApplication.java
│       │   ├── common/              cross-cutting code, shared by all features
│       │   │   ├── config/          @ConfigurationProperties records; only place reading env vars
│       │   │   ├── security/        stateless security, CORS (JWT comes with auth)
│       │   │   ├── ratelimit/       RateLimiter interface, Bucket4j impl, filter
│       │   │   ├── web/             RFC 7807 errors, domain exceptions, correlation id
│       │   │   ├── observability/   health, metrics, logging                         [empty]
│       │   │   └── jobs/            scheduled job support                            [empty]
│       │   └── <feature>/           one package per feature (section 3)               [empty]
│       ├── main/resources/
│       │   ├── application.yml              defaults + env vars
│       │   ├── application-local.yml        docker compose / local
│       │   ├── application-prod.yml         hosted
│       │   └── db/migration/V1__initial_schema.sql   SRS Table 9
│       └── test/java/com/smartappointment/
│           ├── ArchitectureTest.java, FeatureRules.java   ArchUnit rules
│           ├── BackendApplicationIT.java                  full app + PostgreSQL
│           ├── TestcontainersConfiguration.java
│           ├── common/...                                 tests for common code
│           └── <feature>/                                 mirrors main packages
│
├── web/                             React + Vite + TypeScript                         [config only]
│   ├── eslint.config.js             lint rules, feature boundaries (ADR-0007)
│   ├── .prettierrc.json             formatting
│   ├── e2e/                         Playwright flows                                  [empty]
│   └── src/
│       ├── app/                     routing, layout, auth guard
│       ├── api/                     client and types generated from api/openapi.yaml
│       ├── components/              shared UI
│       └── features/<screen-group>/ screens + colocated *.test.tsx (section 3)
│
├── android/                         Kotlin + Jetpack Compose, after the MVP            [folders only]
│   └── app/src/
│       ├── main/java/com/smartappointment/android/<feature>/
│       ├── test/java/...            local unit tests
│       └── androidTest/java/...     instrumented UI tests
│
├── api/openapi.yaml                 API contract shared by all apps; change it first (CONTRIBUTING 6)
│
├── docs/
│   ├── PROJECT_MAP.md               this file
│   ├── STATUS.md                    current state
│   ├── guides/
│   │   ├── AGENT_RULES.md           rules for AI agents (git, scope, clean code, naming, SOLID, hosting)
│   │   ├── CONTRIBUTING.md          team rules (branches, commits, PRs, versions, secrets)
│   │   └── LOGGING.md               how to write logs, status and ADRs
│   ├── features/<feature>.md        scope, status, open questions, log per feature
│   ├── decisions/NNNN-*.md          architecture decision records
│   ├── srs/                         SRS PDF (source of requirements)
│   └── design/                      UI screenshots and static HTML prototype (reference only)
│
├── scripts/
│   ├── setup.sh                     run once after clone: git hooks + .env
│   ├── check-ai-attribution.sh      used by the commit hook and CI
│   └── check-english.sh             used by CI
│
├── .github/
│   ├── workflows/                   ci.yml (lint, test, build), security.yml (CodeQL, dependency review),
│   │                                pr-title.yml, no-ai-attribution.yml
│   ├── rulesets/main-protection.json   branch rules to import on GitHub
│   ├── pull_request_template.md, CODEOWNERS, dependabot.yml
│
├── .githooks/commit-msg             local AI-attribution check
├── .claude/CLAUDE.md                Claude Code entry point (imports AGENT_RULES.md)
├── .claude/settings.json            shared Claude Code settings (attribution off, deny rules)
├── Makefile                         local commands that mirror CI (`make help`)
├── .markdownlint-cli2.jsonc         Markdown lint rules
├── .vscode/extensions.json          recommended editor extensions
├── docker-compose.yml               db (PostgreSQL 17), mailpit, backend
├── .env.example                     every environment variable with local values
├── AGENTS.md                        short entry point for agents (Codex, Cursor, ...) pointing here
└── README.md                        overview and quick start
```

## 3. Feature map

One feature = one backend package, one or more web folders, one log file (ADR-0001).

| Feature | SRS | Backend package | Web folders | Android | Log |
|---|---|---|---|---|---|
| Platform | — | `common/` | `app/`, `api/`, `components/` | — | [platform](features/platform.md) |
| Auth | F1, UC-1, UC-2 | `auth/` | `features/auth/` | sign in | [auth](features/auth.md) |
| Organization | F2, UC-13, UC-16 | `organization/` | `features/admin-organizations/` | — | [organization](features/organization.md) |
| Catalog | F3, UC-11 | `catalog/` | `features/owner-settings/` | — | [catalog](features/catalog.md) |
| Schedule | F4, UC-9, UC-10, UC-12 | `schedule/` | `features/owner-settings/`, `features/staff-agenda/` | — | [schedule](features/schedule.md) |
| Booking | F5, F6, F8, UC-3–6, UC-8 | `booking/` | `features/booking/`, `features/my-appointments/`, `features/staff-agenda/`, `features/team-schedule/` | book, my appointments | [booking](features/booking.md) |
| Estimation | F9, UC-18, UC-19 | `estimation/` (plain Java) | `features/booking/` | book | [estimation](features/estimation.md) |
| Notification | F7, UC-17, UC-20 | `notification/` | — | push | [notification](features/notification.md) |
| Analytics | F10, UC-15 | `analytics/` | `features/team-schedule/`, `features/analytics/` | — | [analytics](features/analytics.md) |
| Feedback | F11, UC-7 | `feedback/` | `features/my-appointments/` | my appointments | [feedback](features/feedback.md) |

Inside a backend feature package: `<Name>Controller` (HTTP only), `<Name>Service` (rules, transactions), `<Name>Repository`, entities, `dto/` (records). Features call each other's services, never repositories.

## 4. Task routing

Find your task, then work only in the listed places.

| If the task is... | Work in | Read first | Verify with | Log in |
|---|---|---|---|---|
| A new or changed endpoint | `api/openapi.yaml` first (own PR), then `backend/.../<feature>/` | feature file, CONTRIBUTING 6, AGENT_RULES 14 | `./mvnw verify` | feature file |
| Business rule (capacity, deadlines, status) | `backend/.../<feature>/<Name>Service` | SRS 2.2 and 2.5, feature file | unit test + `./mvnw verify` | feature file |
| Waiting time / recommendation (Eq. 3, Eq. 4) | `backend/.../estimation/` | SRS 2.6, estimation file | unit tests, `ArchitectureTest` | estimation |
| Database change | new `backend/src/main/resources/db/migration/V<n>__*.sql` | SRS 2.5, `V1__initial_schema.sql` | `BackendApplicationIT` (needs Docker) | feature file |
| Cross-cutting (errors, security, rate limit, config) | `backend/.../common/<area>/` | ADR-0008 | `./mvnw verify` | platform |
| New environment variable | `common/config/*Properties`, `application*.yml`, `.env.example` | AGENT_RULES 14 | app starts with defaults | platform |
| A web screen | `web/src/features/<screen-group>/` | feature file, `docs/design/` | `npm run lint && npm run build`, Vitest | feature file |
| Shared UI component | `web/src/components/` | ADR-0001 | lint, Vitest | platform |
| Android screen | `android/app/src/main/.../<feature>/` | android/README.md | `./gradlew assembleDebug` | feature file |
| Load test | `backend/load-tests/` | SRS 2.4 | k6 run | platform |
| CI, Docker, repo rules | `.github/`, `docker-compose.yml`, `scripts/` | CONTRIBUTING | CI on the PR | platform |
| A decision that is hard to reverse | `docs/decisions/NNNN-*.md` (status `Proposed`) | decisions/README.md | human review | feature file |

## 5. Tests

Each app owns its tests (ADR-0009). Test folders mirror code folders.

### Backend

| Layer | What | Where / naming | Tooling |
|---|---|---|---|
| Unit | services, calculations, filters | `src/test/.../<feature>/<Class>Test.java` | JUnit 5, AssertJ |
| Slice | one layer with Spring (web or JPA) | `<Class>Test.java` with `@WebMvcTest` / `@DataJpaTest` | Spring Boot test slices |
| Integration | full app + real PostgreSQL | `<Name>IT.java`, `@Import(TestcontainersConfiguration.class)` | Testcontainers (skipped without Docker) |
| Architecture | boundaries, layering, no field injection | `ArchitectureTest.java` | ArchUnit |
| Load | SRS P1, P2 | `backend/load-tests/` | k6 |

```bash
cd backend
./mvnw test                 # unit + slice + architecture (fast)
./mvnw verify               # + Checkstyle, Spotless check, integration tests, coverage
./mvnw spotless:apply       # fix formatting
./mvnw checkstyle:check     # lint only
open target/site/jacoco/index.html   # coverage report
```

Required tests: F5 capacity (including a concurrent booking test), F6 cancel/reschedule rules, F9 Eq. 3 and Eq. 4 with the SRS examples, every status transition (AGENT_RULES 7).

### Web (once the Vite project exists)

| Layer | Where / naming | Tooling |
|---|---|---|
| Unit / component | next to the file: `StaffCard.test.tsx` | Vitest, React Testing Library |
| End-to-end | `web/e2e/<flow>.spec.ts` | Playwright against `docker compose` |

```bash
cd web
npm ci
npm run lint        # ESLint incl. feature boundaries and naming
npm run format      # Prettier
npm test            # Vitest
npm run build
```

### Android (after the MVP)

```bash
cd android
./gradlew test               # app/src/test
./gradlew connectedCheck     # app/src/androidTest, needs an emulator
./gradlew assembleDebug
```

## 6. Quality gates

The `Makefile` runs the same checks locally as CI. Run `make help` for the list; before opening a PR run `make verify`.

| Check | Local command | CI workflow / job | Required |
|---|---|---|---|
| English only, Markdown lint | `make repo-lint` | `CI / repo-lint` | via `ci-ok` |
| Workflow lint (actionlint), Dockerfile lint (hadolint) | — (CI only) | `CI / repo-lint` | via `ci-ok` |
| Backend formatting (Spotless), Checkstyle | `make backend-lint` | `CI / backend-lint` | via `ci-ok` |
| Backend unit, slice, ArchUnit, integration tests, coverage | `make backend-verify` | `CI / backend-test` (reports as artifact) | via `ci-ok` |
| Backend Docker image builds | `docker build backend` | `CI / backend-image` | via `ci-ok` |
| Web ESLint, Prettier, type check, Vitest, build | `make web-lint web-test` | `CI / web` | via `ci-ok` |
| Android ktlint, lint, tests, build | `./gradlew ktlintCheck lint test assembleDebug` | `CI / android` | via `ci-ok` |
| PR title format | — | `PR title / check` | yes |
| No AI attribution | commit hook (`make setup`) | `No AI attribution` | yes |
| Dependency vulnerabilities | — | `Security / dependency-review` (PRs) | no |
| Static security analysis (CodeQL: Java, TypeScript, workflows) | — | `Security / codeql` (PRs, main, weekly) | no |

App jobs run only when that app's files change and its build file exists. `ci-ok` fails if any job fails.

The web job expects these npm scripts in `web/package.json`: `lint`, `format`, `format:check`, `typecheck`, `test`, `build`. The Android job expects the ktlint Gradle plugin.

Limits (functions, parameters, complexity, file length) and naming rules: AGENT_RULES 11–13.

## 7. Running locally

```bash
make setup                   # once: git hooks, .env from .env.example
make up                      # db, mailpit, backend (docker compose up --build)
make verify                  # all lint and tests, as in CI
```

| Service | Address | Notes |
|---|---|---|
| Backend API | <http://localhost:8080/api/v1> | 401 for everything until auth (F1) exists |
| Health | <http://localhost:8080/actuator/health> | |
| PostgreSQL | `localhost:5432` | credentials in `.env` |
| Mailpit | <http://localhost:8025> | every e-mail the backend sends |
| Web | <http://localhost:5173> | once the web skeleton exists |

Profiles: `local` (default, compose), `prod` (hosted: JSON logs, rate limiting on). All settings and their environment variables: `backend/src/main/resources/application.yml` and `.env.example`.

## 8. Where knowledge lives

| Question | File |
|---|---|
| What is the state of the project? | `docs/STATUS.md` |
| What happened in feature X and what is open? | `docs/features/<feature>.md` |
| Why is it built this way? | `docs/decisions/` |
| How do I write a log entry or ADR? | `docs/guides/LOGGING.md` |
| What does the system have to do? | `docs/srs/` |
| What should a screen look like? | `docs/design/` |
| What does the API look like? | `api/openapi.yaml` |
| What are the rules? | `docs/guides/AGENT_RULES.md`, `docs/guides/CONTRIBUTING.md` |

## 9. Keeping this map current

Update this file in the same PR when you:

- add, move or remove a top-level folder, a `common/` area or a feature
- add a command, test layer, CI check or environment-wide setting
- change a `[built]` / `[empty]` marker in section 2

Mark folders `[built]` when they have working code, `[config only]` when only configuration exists, `[empty]` or `[folders only]` otherwise.
