# Project status

The current state of the project. Read [`PROJECT_MAP.md`](PROJECT_MAP.md) first for the structure and commands. Update rules: [`guides/LOGGING.md`](guides/LOGGING.md) section 3.

Last updated: 2026-11-22

## Summary

Team rules, folder structure and the backend skeleton are ready. Entities and repositories for
all eleven SRS tables are in place (B1 of the backend MVP handoff); `make backend-verify` passes
(37 tests) and the app starts against PostgreSQL with `ddl-auto: validate`. No business logic,
endpoints or seed data yet. Goal: an MVP that runs locally (backend + React + PostgreSQL via
`docker compose up`), see ADR-0005.

## Features

| Feature | SRS | Status | Note |
|---|---|---|---|
| [Platform and infrastructure](features/platform.md) | — | In progress | Repo rules, CI pipeline, lint, backend skeleton done |
| [Authentication and roles](features/auth.md) | F1 | In progress | Entity and repository only; sign-in not started |
| [Organization management](features/organization.md) | F2 (+ UC-13 branding) | In progress | Entity and repository only |
| [Services and staff](features/catalog.md) | F3 | In progress | Entity and repository only |
| [Working hours, capacity and rules](features/schedule.md) | F4 | In progress | Entity and repository only |
| [Booking: slots, booking, cancel, reschedule, status](features/booking.md) | F5, F6, F8 | In progress | Web prototype on mock data; `Appointment` entity and repository only |
| [Waiting time and staff recommendation](features/estimation.md) | F9 | Not started | |
| [Notifications and reminders](features/notification.md) | F7 | In progress | Entity and repository only |
| [Workload and analytics](features/analytics.md) | F10 | Not started | |
| [Feedback](features/feedback.md) | F11 | In progress | Entity and repository only |

## Next

MVP scope: ADR-0012 (UC-3 book, UC-4 reschedule / UC-5 cancel, UC-8 service status, web screens for Figures 2–4).

| Who | Work | Where |
|---|---|---|
| Yusuf | Backend MVP tasks in order. B1 (entities, repositories) is merged and B2 (demo data) is done by Selman; next B3 JWT, catalog reads, estimation, slots, recommendation, booking with lock, reschedule / cancel, status, agenda, e-mail | [`handoff/2026-11-22-backend-mvp.md`](handoff/2026-11-22-backend-mvp.md) |
| Selman | Web: generate API types from the contract, switch screens to the API as endpoints land, cancel confirmation and reschedule flow | `web/` |
| Selman | SDD diagrams (class, 3 activity, 3 sequence, DFD level 0, component + deployment) | `docs/sdd/` |
| Team | Work split in CONTRIBUTING section 1 and CODEOWNERS; Barış Alkan and Zanyar Erkozan open `develop/<name>` | |

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
