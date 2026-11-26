# Project status

The current state of the project. Read [`PROJECT_MAP.md`](PROJECT_MAP.md) first for the structure and commands. Update rules: [`guides/LOGGING.md`](guides/LOGGING.md) section 3.

Last updated: 2026-11-26

## Summary

Team rules, folder structure and the backend skeleton are ready. Entities and repositories for
all eleven SRS tables are in place (B1). Demo seed data (B2) and JWT sign-in (B3) are done and
verified locally with Docker. Catalog reads (B4: organization profile, active services,
appointment rule, working hours) are done. Waiting-time estimation (B5: slots per day Eq. 1, utilisation
Eq. 2, learned duration Eq. 3, estimated wait Eq. 4) is done, plain Java with no Spring dependency.
Free slots (B6), staff recommendation (B7), booking under a capacity lock (B8), reschedule /
cancel (B9), status changes with learned duration (B10), the agenda / my appointments reads (B11)
and e-mail notices after commit (B12) are done: every backend MVP task of the handoff is
implemented. `make backend-verify` passes (273 tests: 251 unit, slice, repository and architecture
plus 22 integration) and the app starts against
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
| [Booking: slots, booking, cancel, reschedule, status](features/booking.md) | F5, F6, F8 | In progress | Free slots (B6), booking with capacity lock (B8), reschedule / cancel (B9), status changes (B10), agenda and my appointments (B11) done; web prototype on mock data |
| [Waiting time and staff recommendation](features/estimation.md) | F9 | In progress | Eq. 1–4 calculations (B5) and staff recommendation (B7) done |
| [Notifications and reminders](features/notification.md) | F7 | In progress | E-mail confirmation, reschedule and cancellation notices done (B12); reminders and push after the MVP |
| [Workload and analytics](features/analytics.md) | F10 | Not started | |
| [Feedback](features/feedback.md) | F11 | In progress | Entity and repository only |

## Next

MVP scope: ADR-0012 (UC-3 book, UC-4 reschedule / UC-5 cancel, UC-8 service status, web screens for Figures 2–4).

| Who | Work | Where |
|---|---|---|
| Yusuf | Backend MVP tasks in order. B1 (entities, repositories), B2 (demo data), B3 (JWT sign-in), B4 (catalog reads), B5 (estimation), B6 (free slots), B7 (recommendation), B8 (booking), B9 (reschedule / cancel), B10 (status), B11 (agenda, my appointments) and B12 (e-mail) are done; next: try the stack with `make up` and support the web switch from mock data | [`handoff/2026-11-22-backend-mvp.md`](handoff/2026-11-22-backend-mvp.md) |
| Selman | Web: generate API types from the contract, switch screens to the API as endpoints land, cancel confirmation and reschedule flow | `web/` |
| Selman | SDD: diagram sources and work history drafted in `docs/sdd/`; fill in hours and export the diagrams | `docs/sdd/` |
| Team | Work split in CONTRIBUTING section 1 and CODEOWNERS; Barış Alkan and Zanyar Erkozan open `develop/<name>` | |

## Blockers

- SDD report due on 2026-11-26; coding speeds up after that.

## Deadlines

| Date | Deliverable |
|---|---|
| 2026-11-26 | SDD report |
