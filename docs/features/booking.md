# Booking: slots, booking, cancel, reschedule, status

## Scope

| | |
|---|---|
| SRS | F5, F6, F8 |
| Use cases | UC-3, UC-4, UC-5, UC-6, UC-8 |
| Backend | `backend/src/main/java/com/smartappointment/booking/` |
| Web | `web/src/features/booking/`, `web/src/features/my-appointments/`, `web/src/features/staff-agenda/`, `web/src/features/team-schedule/` |
| Android | Book appointment, My appointments |

## Status

In progress

## Open questions

- [ ] Locking strategy for concurrent bookings: SELECT ... FOR UPDATE on the staff row, or an advisory lock? Proposed: FOR UPDATE on the staff row (handoff B8).

## Log

### 2026-11-22 · selman · backend

- **Done:** Demo data (B2) gives the booking flow two staff members with history, so slots, waiting estimates and the agenda have realistic input once B5–B11 exist.
- **Refs:** `backend/src/main/resources/db/seed/R__demo_data.sql`

### 2026-11-22 · yusuf · backend

- **Done:** Added the `Appointment` entity and `AppointmentStatus` enum (allowed transitions per
  AGENT_RULES section 8) with `AppointmentRepository`, mapped field-for-field to the V1 schema.
  Cross-feature references (`customerId`, `staffId`, `serviceId`) are plain `Long` columns, not
  JPA associations, so `booking` stays decoupled from `auth`/`catalog` (ADR-0001).
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No capacity lock or transition logic yet (B8–B10); the locking-strategy
  question below is still open. Verified with `AppointmentRepositoryTest` (`@DataJpaTest`,
  Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B6, B8 (booking with lock), B9 (reschedule, cancel), B10 (status), B11 (agenda, my appointments), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-22 · selman · web

- **Done:** Prototype screens for UC-3 (book: service, working day, staff ranked by estimated wait, free slots, confirm) and UC-5/UC-6 (my appointments: upcoming and past, cancel, reschedule link), plus UC-8 staff daily agenda with status actions limited to the SRS 2.5 transitions. Unit tests cover the transition rules and the appointment detail actions.
- **State / next:** Runs on `src/api/mockStore.ts`; reschedule only links to the booking screen. Next: define the endpoints in `api/openapi.yaml` and implement them in the backend.
- **Refs:** ADR-0012, SRS UC-3, UC-4, UC-5, UC-8

### 2026-11-21 · selman · backend

- **Done:** `appointment` table in Flyway V1 with status values, start-before-end and service-time checks, indexes on (staff, start) and (customer, start). Rate limit group `booking` covers `/api/v1/appointments`.
- **State / next:** Capacity under concurrency and the status transition order are not enforceable in SQL; they belong in `BookingService` (open question on locking still stands).
- **Refs:** SRS Table 9, ADR-0008

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
