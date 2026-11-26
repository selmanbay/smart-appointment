# Feedback

## Scope

| | |
|---|---|
| SRS | F11 |
| Use cases | UC-7 |
| Backend | `backend/src/main/java/com/smartappointment/feedback/` |
| Web | `web/src/features/my-appointments/` |
| Android | My appointments |

## Status

In progress

## Open questions

- None

## Log

### 2026-11-26 · yusuf · backend

- **Done:** `FeedbackService.findMeanRatingByStaffId(...)`: the mean rating of each staff member's appointments, for the B7 tie-break. Staff without any rating are left out.
- **Refs:** handoff B7, UC-18

### 2026-11-22 · yusuf · backend

- **Done:** Added the `Feedback` entity and `FeedbackRepository` (with `findByAppointmentId`),
  mapped field-for-field to the V1 schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No feedback endpoint yet. Verified with `FeedbackRepositoryTest`
  (`@DataJpaTest`, Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-21 · selman · backend

- **Done:** `feedback` table in Flyway V1: one per appointment, rating 1-5.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
