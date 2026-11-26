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

- [x] Locking strategy for concurrent bookings: SELECT ... FOR UPDATE on the staff row, see ADR-0018 (Proposed).
- [ ] The contract's optional cancel `reason` has no column in the schema, so B9 does not store it. Add a column (new migration) or drop it from the contract? (Selman)
- [ ] Only the customer may reschedule; the owner may cancel but not reschedule, and the system admin may do neither. Is that right? (Selman)
- [ ] The rate limiter puts every path under `/api/v1/appointments` in the `booking` group, but the contract lists `changeAppointmentStatus` and `getAppointment` as `default`. Match by method and path in `RoutePolicyResolver`? Rate limiting is off locally, so nothing breaks today. (Selman)
- [ ] The agenda leaves cancelled appointments out and keeps no-shows and completed ones. Should the staff screen also show cancellations of the day? (Selman)
- [ ] Who may book? B8 lets any signed-in account book for itself (UC-3 names the customer). Restrict to CUSTOMER? (Selman)
- [ ] The SDD Activity 1 diagram does not show the Idempotency-Key: the code looks the key up before the rules and again right after the staff lock (ADR-0018). Add it to the diagram? (Selman)
- [ ] `max_advance_days`: calendar days in the organization's time zone (today + N, whole day), or N × 24 hours from now? B6 uses calendar days. (Selman)
- [ ] `max_active_per_user`: counted per service (the rule belongs to a service) or across all services? The demo data and B8 assume per service. (Selman)
- [ ] `estimatedWaitMinutes` is rounded down to whole minutes. Is that what the web app expects? (Selman)

## Log

### 2026-11-26 · yusuf · backend

- **Done:** Book, reschedule and cancel now go through `BookingRecorder`, which writes the change and publishes an `AppointmentEvent` for the notice e-mail (B12); the event lives in booking, so notification depends on booking and not the reverse. `AppointmentNoticeService` gives the notification feature the appointment's customer, service, staff member and local start.
- **Refs:** handoff B12, UC-20

### 2026-11-26 · yusuf · backend

- **Done:** B11 reads: `GET /api/v1/appointments/{id}` (`getAppointment`), `GET /api/v1/me/appointments?scope&page&size` (`listMyAppointments`) and `GET /api/v1/staff/{id}/agenda?date` (`getStaffAgenda`). An appointment is visible to its customer, its staff member and the organization's owner (403 otherwise). My appointments are paged (size 1–100): UPCOMING are still active and not yet ended, nearest first; PAST are all others, newest first. The agenda is the staff member's own or the owner's, with the day's working hours (null on a day off) and the day's appointments in time order, cancelled ones left out. Every BOOKED / CHECKED_IN appointment carries its current Eq. 4 wait; `canCancel` now depends on who is looking (owner: any BOOKED; customer: before the deadline; staff: never). The access rules live in one place (`AccessRules`) and B9/B10 use them too.
- **State / next:** 262 tests pass (`make backend-verify`); `AppointmentReadsIT` reads Alex Morgan's lists, Maya's agenda and the 403 cases against the demo data. Each appointment in a list loads its own context and day (about ten queries per item); fine for the demo, worth batching if lists grow. Not tried in the web app yet. Next: B12 e-mail notifications.
- **Refs:** handoff B11, UC-6, UC-8, SRS Eq. 4

### 2026-11-26 · yusuf · backend

- **Done:** B10 `POST /api/v1/appointments/{id}/status` (contract `changeAppointmentStatus`). The allowed changes live in one table (`StatusTransition`): BOOKED → CHECKED_IN → IN_SERVICE → COMPLETED and BOOKED → NO_SHOW; anything else answers 409 `INVALID_STATUS` and changes nothing. Each step stamps its own server time (`checked_in_at`, `service_start_at`, `service_end_at`). Only the appointment's staff member or the organization's owner may record a status (403 otherwise). The appointment row is locked; a status outside the contract's enum (for example CANCELLED) is a 400.
- **State / next:** 243 tests pass (`make backend-verify`); every from/to pair is tested against the table, and `StatusFlowIT` runs check-in → start → completion as Daniel against the demo data. Not tried in the web app yet. Next: B11 agenda and my appointments.
- **Refs:** handoff B10, UC-8, SRS 2.5, SDD Sequence 3, SDD Activity 2

### 2026-11-26 · yusuf · backend

- **Done:** B9 `POST /api/v1/appointments/{id}/reschedule` and `/cancel` (contract `rescheduleAppointment`, `cancelAppointment`, rate-limit group `booking`). Both lock the appointment row first. Reschedule follows SDD Sequence 2: only the customer (403 otherwise), only BOOKED (`INVALID_STATUS`), deadline before any staff lock (`CANCEL_DEADLINE_PASSED`), then the new slot is checked like a booking (no active limit, a move adds no appointment), the new staff row is locked, capacity is counted without the moved appointment, and the same row is updated in one transaction. A failed rule changes nothing. Cancel: the customer until `cancel_deadline_hours` before the start, the organization's owner for any BOOKED appointment (UC-14), anyone else 403; the row is marked CANCELLED with `cancelled_at`, never deleted.
- **State / next:** 192 tests pass (`make backend-verify`). `RescheduleAndCancelIT`: a reschedule to a taken slot answers 409 `SLOT_TAKEN` and keeps the original; a reschedule to a free slot frees the old one; cancel frees the slot, a second cancel answers `INVALID_STATUS`, a stranger gets 403. Not tried in the web app yet. No notice e-mails yet (B12). Next: B10 status changes.
- **Refs:** handoff B9, UC-4, UC-5, UC-14, SDD Sequence 2, ADR-0018

### 2026-11-26 · yusuf · backend

- **Done:** B8 `POST /api/v1/appointments` (contract `bookAppointment`, rate-limit group `booking`). Checks run in SDD Activity 1 order inside one transaction: staff provides the service (409 `STAFF_DOES_NOT_PROVIDE_SERVICE`), lead time, advance limit, `max_active_per_user` per service, start must be one of the day's slots (`OUTSIDE_WORKING_HOURS`), then `SELECT ... FOR UPDATE` on the staff row, capacity count (`SLOT_TAKEN`), insert as BOOKED. 409 bodies carry the contract's `code` and a title. 201 returns the contract's Appointment with `Location`, estimated wait and `canCancel`. Optional `Idempotency-Key` is stored in a new table (`V2__booking_idempotency_key.sql`) and returns the first appointment on a repeat.
- **Why:** UC-3 step 6; the booking screen can now book for real.
- **State / next:** 169 tests pass (`make backend-verify`). `BookingConcurrencyIT`: 8 parallel requests for the last place give exactly one 201 and seven `SLOT_TAKEN`; the same key sent twice at once books once. With the lock removed both tests fail, so they catch the race. Not tried in the web app yet. No confirmation e-mail yet (B12). Next: B9 reschedule and cancel.
- **Refs:** handoff B8, ADR-0018 (Proposed), UC-3, SRS 2.5, SDD Activity 1

### 2026-11-26 · yusuf · backend

- **Done:** B6 `GET /api/v1/staff/{staffId}/slots?serviceId&date` (contract `listFreeSlots`). The slot grid follows Eq. 1 (working hours split by the break, expected duration plus buffer, a slot counts only if it and its buffer fit). Slots outside the rule's `min_lead_hours` / `max_advance_days`, slots already started, and slots with no capacity left are dropped. Each slot carries its remaining capacity and estimated wait (Eq. 4 over the day's BOOKED / CHECKED_IN / IN_SERVICE appointments, `F_0` = now today, opening time on later days). Times are generated in the organization's time zone and returned with its offset. 404 for a missing or inactive service, or a staff member who is inactive or does not provide it.
- **Why:** UC-3 step 4; the booking screen needs real slots. `BookingWindow` (lead time, advance limit) and `BookingContextLoader` are written so B8 can reuse them for its 409 checks.
- **State / next:** 131 tests pass (`make backend-verify`); `FreeSlotsIT` runs the endpoint against the demo data. Not yet tried in the running web app. Next: B7 staff recommendation.
- **Refs:** handoff B6, UC-3, SRS Eq. 1, Eq. 4, SRS 2.5

### 2026-11-26 · yusuf · backend

- **Done:** Fixed the demo data (B2): every customer, Alex Morgan included, had three or four upcoming advising bookings although the advising rule allows `max_active_per_user` = 2, so the demo customer could not have booked once B8 enforces the rule. Upcoming bookings now give Alex one and every other customer two (9 in total, spread over the next working week). `DemoDataIT` checks that no customer exceeds the rule and that Alex can still book.
- **State / next:** An existing local database keeps the old rows (the seed is a no-op once the demo organization exists); run `make db-reset` before the next `make up`.
- **Refs:** handoff B2, `db/seed/R__demo_data.sql`, SRS 2.5

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
