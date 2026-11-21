# Notifications and reminders

## Scope

| | |
|---|---|
| SRS | F7 |
| Use cases | UC-17, UC-20 |
| Backend | `backend/src/main/java/com/smartappointment/notification/` |
| Web | — |
| Android | Push notifications |

## Status

Not started

## Open questions

- [ ] How many hours before the appointment is the reminder sent? (the SRS gives no number)

## Log

### 2026-11-21 · selman · backend

- **Done:** `notification` table in Flyway V1 with `provider_response` for the FCM / SMTP result and a partial index on pending notifications. Mailpit is wired as the local SMTP server.
- **Refs:** SRS 1.4.1.1, Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
