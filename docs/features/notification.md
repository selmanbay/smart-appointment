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

In progress

## Open questions

- [ ] How many hours before the appointment is the reminder sent? (the SRS gives no number)

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Added the `Notification` entity, the `NotificationChannel`, `NotificationType` and
  `NotificationStatus` enums, and `NotificationRepository`, mapped field-for-field to the V1
  schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No `NotificationSender` yet (B12). Verified with
  `NotificationRepositoryTest` (`@DataJpaTest`, Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B12 (e-mail after commit, notification records), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-21 · selman · backend

- **Done:** `notification` table in Flyway V1 with `provider_response` for the FCM / SMTP result and a partial index on pending notifications. Mailpit is wired as the local SMTP server.
- **Refs:** SRS 1.4.1.1, Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
