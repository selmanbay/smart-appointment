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

Not started

## Open questions

- [ ] Is the UC-10 schedule change approval part of the MVP?

## Log

### 2026-11-21 · selman · backend

- **Done:** `working_hours`, `schedule_change_request` and `appointment_rule` tables in Flyway V1. Breaks must lie inside the shift; one rule per service.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
