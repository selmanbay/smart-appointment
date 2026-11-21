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

| Who | Work | Where |
|---|---|---|
| Yusuf | Backend MVP tasks B1–B12 in order: entities, seed, JWT, catalog reads, estimation, slots, recommendation, booking with lock, reschedule / cancel, status, agenda, e-mail | [`handoff/2026-11-22-backend-mvp.md`](handoff/2026-11-22-backend-mvp.md) |
| Selman | Web: generate API types from the contract, switch screens to the API as endpoints land, cancel confirmation and reschedule flow | `web/` |
| Selman | SDD diagrams (class, 3 activity, 3 sequence, DFD level 0, component + deployment) | `docs/sdd/` |
| Team | Work split in CONTRIBUTING section 1 and CODEOWNERS; Barış Alkan and Zanyar Erkozan open `develop/<name>` | |

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
