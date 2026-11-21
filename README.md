# Smart Appointment

Smart Appointment and Service Scheduling System, the CNG 491 graduation project at METU Northern Cyprus Campus, Computer Engineering. Supervisor: İdil Candan.
Team: Barış Alkan, Yusuf Aydın, Selman Bayburt, Zanyar Erkozan.

Customers book, cancel and reschedule appointments on the web and on Android. Organizations manage their services, staff and working hours. The system recommends a staff member based on the estimated waiting time. Requirements: [`docs/srs/SRS-v1-2026-10-27.pdf`](docs/srs/SRS-v1-2026-10-27.pdf).

## Status

Team rules, the backend skeleton (configuration, security, rate limiting, database schema, quality gates) and the folder structure are in place. Feature code has not started. Current state: [`docs/STATUS.md`](docs/STATUS.md).

## Layout

```text
smart-appointment/
  backend/             Spring Boot REST API (Java 21)
    src/main/          code, one package per feature
    src/test/          unit, slice, integration (*IT) and architecture tests
    load-tests/        k6 load tests (SRS P1, P2)
  web/                 React + Vite + TypeScript
    src/               code, one folder per feature; unit tests next to each file
    e2e/               Playwright end-to-end tests
  android/             Kotlin + Jetpack Compose customer app (after the MVP)
    app/src/main/      code
    app/src/test/      local unit tests
    app/src/androidTest/  instrumented tests
  api/                 openapi.yaml, the contract shared by all apps
  docs/                SRS, design, status, feature logs, decisions
  scripts/             setup and repository checks
  .github/             CI, CODEOWNERS, PR template, branch ruleset
  docker-compose.yml   all local services with pinned versions
```

Each app owns its code, tests and build files (ADR-0009).

## Getting started

You need Git and Docker Desktop (or OrbStack). Android work also needs Android Studio.

```bash
git clone git@github.com:selmanbay/smart-appointment.git
cd smart-appointment
make setup        # enables git hooks, creates .env
make up           # starts PostgreSQL, Mailpit and the backend
make help         # all commands; `make verify` runs what CI runs
```

| Service | Address |
|---|---|
| PostgreSQL | `localhost:5432` |
| Mailpit (outgoing e-mail) | <http://localhost:8025> |
| Backend (once added) | <http://localhost:8080> |
| Web (once added) | <http://localhost:5173> |

## Working rules

Read these before your first change:

- [`docs/PROJECT_MAP.md`](docs/PROJECT_MAP.md): structure map, where to work for each task, test and check commands
- [`CONTRIBUTING.md`](CONTRIBUTING.md): branches, commits, PRs, versions, secrets
- [`AGENTS.md`](AGENTS.md): rules for AI coding agents
- [`docs/LOGGING.md`](docs/LOGGING.md): how project status, feature logs and decisions are kept in the repo
