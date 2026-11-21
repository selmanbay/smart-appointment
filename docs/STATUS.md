# Project status

The first page everyone reads, human or agent. Update rules: [`LOGGING.md`](LOGGING.md) section 3.

Last updated: 2026-11-21

## Summary

The repository skeleton, team rules and folder structure are ready. There is no application code yet. Goal: an MVP that runs locally (backend + React + PostgreSQL via `docker compose up`), see ADR-0005.

## Features

| Feature | SRS | Status | Note |
|---|---|---|---|
| [Platform and infrastructure](features/platform.md) | — | In progress | Repo rules, CI and lint configs done |
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

1. Backend skeleton: Spring Boot project with profiles and `common/` packages (ADR-0008), Spotless + Checkstyle + ArchUnit wired into `pom.xml` (ADR-0007), backend service in `docker-compose.yml`, Flyway V1 from SRS Table 9
2. Web skeleton: Vite + React + TypeScript with the existing `eslint.config.js` and Prettier, web service in `docker-compose.yml`
3. Auth and organization endpoints in `api/openapi.yaml`
4. Work split (CONTRIBUTING.md section 1 and CODEOWNERS)

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
