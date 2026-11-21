# Project status

The current state of the project. Read [`PROJECT_MAP.md`](PROJECT_MAP.md) first for the structure and commands. Update rules: [`guides/LOGGING.md`](guides/LOGGING.md) section 3.

Last updated: 2026-11-21

## Summary

Team rules, folder structure and the backend skeleton are ready. `./mvnw verify` passes (26 tests); feature code has not started. Goal: an MVP that runs locally (backend + React + PostgreSQL via `docker compose up`), see ADR-0005.

## Features

| Feature | SRS | Status | Note |
|---|---|---|---|
| [Platform and infrastructure](features/platform.md) | — | In progress | Repo rules, CI pipeline, lint, backend skeleton done |
| [Authentication and roles](features/auth.md) | F1 | Not started | |
| [Organization management](features/organization.md) | F2 (+ UC-13 branding) | Not started | |
| [Services and staff](features/catalog.md) | F3 | Not started | |
| [Working hours, capacity and rules](features/schedule.md) | F4 | Not started | |
| [Booking: slots, booking, cancel, reschedule, status](features/booking.md) | F5, F6, F8 | Not started | |
| [Waiting time and staff recommendation](features/estimation.md) | F9 | Not started | |
| [Notifications and reminders](features/notification.md) | F7 | Not started | |
| [Workload and analytics](features/analytics.md) | F10 | Not started | |
| [Feedback](features/feedback.md) | F11 | Not started | |

## Next

1. Run `docker compose up --build` once Docker is installed and confirm the backend starts and `BackendApplicationIT` passes
2. Web skeleton: Vite + React + TypeScript with the existing `eslint.config.js` and Prettier, web service in `docker-compose.yml`
3. Auth and organization endpoints in `api/openapi.yaml`
4. Work split (docs/guides/CONTRIBUTING.md section 1 and CODEOWNERS)

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
