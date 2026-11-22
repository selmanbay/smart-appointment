# Working hours, capacity and rules

## Scope

| | |
|---|---|
| SRS | F4 |
| Use cases | UC-9, UC-10, UC-12 |
| Backend | `backend/src/main/java/com/smartappointment/schedule/` |
| Web | `web/src/features/owner-settings/`, `web/src/features/staff-agenda/` |
| Android | — |

## Status

In progress

## Open questions

- [ ] Is the UC-10 schedule change approval part of the MVP?

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Added `AppointmentRule`, `WorkingHours`, `ScheduleChangeRequest` entities, the
  `ScheduleChangeStatus` enum, and their repositories, mapped field-for-field to the V1 schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No business rules yet; B6 (free slots) and B9 (reschedule) will build on
  these. Verified with `ScheduleRepositoryTest` (`@DataJpaTest`, Testcontainers) and
  `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-21 · selman · backend

- **Done:** `working_hours`, `schedule_change_request` and `appointment_rule` tables in Flyway V1. Breaks must lie inside the shift; one rule per service.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
