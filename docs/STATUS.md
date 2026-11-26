# Project status

The current state of the project. Read [`PROJECT_MAP.md`](PROJECT_MAP.md) first for the structure and commands. Update rules: [`guides/LOGGING.md`](guides/LOGGING.md) section 3.

Last updated: 2026-11-26

## Summary

Team rules, folder structure and the backend skeleton are ready. Entities and repositories for
all eleven SRS tables are in place (B1). Demo seed data (B2) and JWT sign-in (B3) are done and
verified locally with Docker. Catalog reads (B4: organization profile, active services,
appointment rule, working hours) are done. Waiting-time estimation (B5: slots per day Eq. 1, utilisation
Eq. 2, learned duration Eq. 3, estimated wait Eq. 4) is done, plain Java with no Spring dependency.
`make backend-verify` passes (102 tests: 94 unit, slice and architecture plus 8 integration) and the app starts against
PostgreSQL with `ddl-auto: validate`. Goal: an MVP that runs locally (backend + React + PostgreSQL
via `docker compose up`), see ADR-0005.

## Features

| Feature | SRS | Status | Note |
|---|---|---|---|
| [Platform and infrastructure](features/platform.md) | — | In progress | Repo rules, CI pipeline, lint, backend skeleton done |
| [Authentication and roles](features/auth.md) | F1 | In progress | Sign-in, sign-out and `/me` done (cookie + bearer, CSRF); web still on demo session |
| [Organization management](features/organization.md) | F2 (+ UC-13 branding) | In progress | getOrganization done (B4) |
| [Services and staff](features/catalog.md) | F3 | In progress | listServices done (B4); appointment rule read lives in schedule |
| [Working hours, capacity and rules](features/schedule.md) | F4 | In progress | getAppointmentRule, getWorkingHours done (B4) |
| [Booking: slots, booking, cancel, reschedule, status](features/booking.md) | F5, F6, F8 | In progress | Web prototype on mock data; `Appointment` entity and repository only |
| [Waiting time and staff recommendation](features/estimation.md) | F9 | In progress | Eq. 1–4 calculations done (B5); staff recommendation (B7) next |
| [Notifications and reminders](features/notification.md) | F7 | In progress | Entity and repository only |
| [Workload and analytics](features/analytics.md) | F10 | Not started | |
| [Feedback](features/feedback.md) | F11 | In progress | Entity and repository only |

## Next

MVP scope: ADR-0012 (UC-3 book, UC-4 reschedule / UC-5 cancel, UC-8 service status, web screens for Figures 2–4).

| Who | Work | Where |
|---|---|---|
| Yusuf | Backend MVP tasks in order. B1 (entities, repositories), B2 (demo data), B3 (JWT sign-in), B4 (catalog reads) and B5 (estimation) are done; next free slots, recommendation, booking with lock, reschedule / cancel, status, agenda, e-mail | [`handoff/2026-11-22-backend-mvp.md`](handoff/2026-11-22-backend-mvp.md) |
| Selman | Web: generate API types from the contract, switch screens to the API as endpoints land, cancel confirmation and reschedule flow | `web/` |
| Selman | SDD: diagram sources and work history drafted in `docs/sdd/`; fill in hours and export the diagrams | `docs/sdd/` |
| Team | Work split in CONTRIBUTING section 1 and CODEOWNERS; Barış Alkan and Zanyar Erkozan open `develop/<name>` | |

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
