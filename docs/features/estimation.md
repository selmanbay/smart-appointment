# Waiting time and staff recommendation

## Scope

| | |
|---|---|
| SRS | F9 |
| Use cases | UC-18, UC-19 |
| Backend | `backend/src/main/java/com/smartappointment/estimation/` |
| Web | `web/src/features/booking/` (recommendation cards) |
| Android | Book appointment |

## Status

Not started

## Open questions

- [ ] For a service with no completed appointments, does learned_duration start at expected_duration?

## Log

### 2026-11-22 · selman · web

- **Done:** Staff option card shows estimated wait, workload as "x of y slots booked", mean rating and a plain "Recommended: shortest estimated wait" line; the mock API ranks by wait, then rating.
- **State / next:** Values are sample data; the real numbers come from Eq. 3 and Eq. 4 in the backend.
- **Refs:** UC-18, UC-19

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
