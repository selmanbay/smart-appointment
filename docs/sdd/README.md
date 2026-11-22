# SDD notes

Short notes for the diagram and flow parts of the SDD. Diagram sources are in [`DESIGN_FLOWS.md`](DESIGN_FLOWS.md) (paste a block into <https://mermaid.live> to export); who did what is in [`WORK_HISTORY.md`](WORK_HISTORY.md). Items marked «planned» are designed but not built yet.

## Class diagram (3.1.1)

- Use [section 1](DESIGN_FLOWS.md#1-logical-view-class-diagram). Too big for one slide: show the eleven entities, and the services on a second slide if needed.
- Entities = SRS Table 9; relationship names from the SRS ERD.
- Features reference each other by id, not by object (keeps packages independent).
- `WaitTimeEstimator` is plain Java (Eq. 3, Eq. 4); `NotificationSender` is an interface (SMTP now, FCM later).

## Activity diagrams (3.2.1, max 3)

| # | Flow | Point to make |
|---|---|---|
| 1 | Booking validation | Cheap rule checks first, lock only for the capacity count; rule codes go back to the user |
| 2 | Status lifecycle | Only SRS 2.5 transitions; server stamps the times that feed Eq. 3 |
| 3 | Waiting time and recommendation | Eq. 4 per staff, sort by wait, ties by rating, computed on request |

## Sequence diagrams (3.2.2, max 3)

| # | Use case | Actors | One-line interaction | State |
|---|---|---|---|---|
| 1 | UC-3 Book | Customer, web, backend, DB, SMTP | Ranked staff → free slots → book under a lock → e-mail after commit | planned |
| 2 | UC-4 Reschedule | Customer, web, backend, DB, SMTP | New slot taken before the old one is released, one transaction | planned |
| 3 | UC-8 Record status | Staff, web, backend, DB | Check-in / start / complete / no-show; completion updates the learned duration | planned |
| extra | UC-2 Sign in | Any user, web, backend, DB | Login → HttpOnly cookie → `/me`; CSRF header on later POSTs | **implemented** |

- UC-18, UC-19, UC-20 are inclusions of UC-3 / UC-4, so they have no diagram of their own (template rule).

## Data flow diagram (3.2.3, level 0)

- External entities: four roles + SMTP + FCM (same as SRS Figure 1).
- Appointments store is central: booking writes, status completes, estimate and analytics read.

## Component and deployment (3.3)

- Components = feature packages + `common`; no cycles (ArchUnit).
- Containers: `web` (nginx), `backend` (stateless Spring Boot), `postgres`, `mailpit` → real SMTP when hosted.
- Browser: cookie + CSRF via same origin; Android: bearer token.

## Gantt and Sprint 2 (appendix)

- Work items, dates, people and commits: [`WORK_HISTORY.md`](WORK_HISTORY.md). Hours are empty; each person fills in their own.
- Remaining MVP tasks for the Gantt: B4–B12 in [`../handoff/2026-11-22-backend-mvp.md`](../handoff/2026-11-22-backend-mvp.md), plus switching the web screens from sample data to the API.
