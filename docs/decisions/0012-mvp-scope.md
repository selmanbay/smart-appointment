# ADR-0012: MVP scope follows the SDD critical scenarios

- **Status:** Accepted
- **Date:** 2026-11-22
- **Feature:** general

## Context

The SDD (IEEE 1016, 4+1 views) asks for at most 2–3 sequence diagrams and 3 activity diagrams for the critical use cases, and the presentation is assessed on traceability from the SRS to the design. Building exactly those scenarios first makes the design, the code and the demo the same thing.

## Decision

The MVP is a web app, running locally with `docker compose up` (ADR-0005), that implements these scenarios end to end:

| SDD artefact | SRS | MVP behaviour |
|---|---|---|
| Sequence 1 | UC-3 Book Appointment (+ UC-18, UC-19, UC-20) | pick service → staff ranked by estimated wait (Eq. 4), ties by mean rating → free slots (Eq. 1) → book under a capacity lock → confirmation e-mail |
| Sequence 2 | UC-4 Reschedule (+ UC-5 Cancel) | new slot is taken before the old one is released, in one transaction; cancel respects `cancel_deadline_hours`; notification e-mail |
| Sequence 3 | UC-8 Record Service Status | check-in → start → complete, or no-show; completion updates the learned duration (Eq. 3) |
| Activity 1 | F4, F5 | booking validation: appointment rule, working hours, capacity |
| Activity 2 | SRS 2.5 | appointment status lifecycle |
| Activity 3 | F9 | waiting-time estimate and staff ranking |

**In the MVP**

- F1 sign-in with JWT for the four roles. Accounts come from seed data; self sign-up is added later.
- F2–F4 data from a seed migration (one organization, services, staff, working hours, rules). No owner or admin management screens yet.
- F5, F6, F8, F9 and e-mail notifications (F7) through Mailpit.
- Web screens: sign in, book appointment (Figure 2), my appointments (Figure 3), staff daily agenda (Figure 4).

**After the MVP**
Android app, FCM push, reminders job, analytics (F10), feedback screen (F11), UC-10 schedule change approval, owner and admin screens, self sign-up.

## Consequences

- The SDD diagrams are drawn from this scope and stored in `docs/sdd/`; code follows the diagrams.
- Seed data must contain completed past appointments so the waiting-time estimate is meaningful in the demo.
