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
- [ ] The e-mail is sent right after commit in the request's thread, so a slow SMTP server delays the response by up to `MAIL_TIMEOUT_MS`. Make it asynchronous (`@Async`) as the SDD says? It needs an executor and an await helper for tests. (Selman)

## Log

### 2026-11-26 · yusuf · backend

- **Done:** B12 e-mail notices (UC-20). Booking, reschedule and cancel publish an `AppointmentEvent`; `AppointmentNotificationListener` handles it with `@TransactionalEventListener(AFTER_COMMIT)` in its own transaction, composes the confirmation, reschedule or cancellation e-mail, sends it through the `NotificationSender` interface (`SmtpNotificationSender`, Mailpit locally) and stores every attempt in `notification` (channel EMAIL, type, SENT with `sent_at` or FAILED, `provider_response` cut to the column's 1000 characters). An SMTP failure comes back as a FAILED result instead of an exception, and the appointment is already committed, so a failed send never undoes it. SMTP connect, read and write time-outs come from `MAIL_TIMEOUT_MS` (default 5000).
- **State / next:** 273 tests pass (`make backend-verify`). `NotificationIT` books with a failing sender (201, notification FAILED) and cancels with a working one (CONFIRMATION and CANCELLATION both SENT). Not yet checked against a running Mailpit (`make up`, then <http://localhost:8025>). Reminders (F7) and push are not part of the MVP.
- **Refs:** handoff B12, UC-20, AGENT_RULES sections 8 and 14

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
