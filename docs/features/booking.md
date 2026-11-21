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

Not started

## Open questions

- [ ] Locking strategy for concurrent bookings: SELECT ... FOR UPDATE on the staff row, or an advisory lock?

## Log

### 2026-11-21 · selman · backend
- **Done:** `appointment` table in Flyway V1 with status values, start-before-end and service-time checks, indexes on (staff, start) and (customer, start). Rate limit group `booking` covers `/api/v1/appointments`.
- **State / next:** Capacity under concurrency and the status transition order are not enforceable in SQL; they belong in `BookingService` (open question on locking still stands).
- **Refs:** SRS Table 9, ADR-0008

### 2026-11-21 · selman · docs
- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
