# Project status

The current state of the project. Read [`PROJECT_MAP.md`](PROJECT_MAP.md) first for the structure and commands. Update rules: [`guides/LOGGING.md`](guides/LOGGING.md) section 3.

Last updated: 2026-11-22

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
| [Booking: slots, booking, cancel, reschedule, status](features/booking.md) | F5, F6, F8 | In progress | Web prototype on mock data; no backend yet |
| [Waiting time and staff recommendation](features/estimation.md) | F9 | Not started | |
| [Notifications and reminders](features/notification.md) | F7 | Not started | |
| [Workload and analytics](features/analytics.md) | F10 | Not started | |
| [Feedback](features/feedback.md) | F11 | Not started | |

## Next

MVP scope: ADR-0012 (UC-3 book, UC-4 reschedule / UC-5 cancel, UC-8 service status, web screens for Figures 2–4).

1. Backend MVP against the contract (`x-mvp: true` operations in `api/openapi.yaml`, Swagger UI at `/swagger-ui`): JWT sign-in, seed data, slots (Eq. 1), estimation (Eq. 3, Eq. 4), booking with capacity lock, reschedule, cancel, status, e-mail
2. Web: generate API types from the contract, switch screens from mock data to the API, add cancel confirmation and the reschedule flow
3. SDD diagrams in `docs/sdd/` (class, 3 activity, 3 sequence, DFD level 0, component + deployment)
4. Work split (docs/guides/CONTRIBUTING.md section 1 and CODEOWNERS); each member opens `develop/<name>`

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
