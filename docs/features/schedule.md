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

- [x] Is the UC-10 schedule change approval part of the MVP? No (ADR-0012).

## Log

### 2026-11-26 · yusuf · backend

- **Done:** `ScheduleLookupService.findWorkingHours(staffId, dayOfWeek)` gives the agenda (B11) that day's working hours in the contract's `HH:mm` shape; the mapping is shared with `getWorkingHours`.
- **Refs:** handoff B11

### 2026-11-26 · yusuf · backend

- **Done:** `ScheduleLookupService` gives other features the appointment rule (empty when a service has none) and a staff member's working periods for a weekday, split by the break. `ScheduleService` keeps the contract reads and uses it.
- **Why:** Free slots (B6) and booking (B8) need these without using schedule repositories (ADR-0001).
- **Refs:** handoff B6

### 2026-11-22 · yusuf · backend

- **Done:** Contract operations getAppointmentRule and getWorkingHours: one `ScheduleController`
  and `ScheduleService`. getAppointmentRule answers 404 only when the service has no rule (per the
  contract), with no service-existence check. getWorkingHours answers 404 when the staff member
  does not exist (checked through `catalog.StaffLookupService.existsById`, never its repository)
  and otherwise returns the configured days in order, empty if none are set. Added
  `findByStaffIdOrderByDayOfWeek` to `WorkingHoursRepository`. getAppointmentRule is public;
  getWorkingHours needs sign-in; both are set accordingly in `SecurityConfig`.
- **Why:** B4 of the backend MVP handoff. The appointment-rule read lives here and not in
  `catalog`, because `AppointmentRule`'s entity and repository were placed in `schedule` at B1 and
  ArchUnit forbids a feature from using another feature's repository.
- **State / next:** Verified with `ScheduleControllerTest`, `ScheduleServiceTest`,
  `make backend-verify`, and manually against the demo data: `GET /api/v1/services/1/rule` returns
  the rule without a token; `GET /api/v1/staff/1/working-hours` needs a token and returns Maya
  Chen's Mon-Fri 09:00-16:00 with a 12:00-13:00 break.
- **Refs:** handoff B4, SRS F4

### 2026-11-22 · yusuf · backend

- **Done:** Added `AppointmentRule`, `WorkingHours`, `ScheduleChangeRequest` entities, the
  `ScheduleChangeStatus` enum, and their repositories, mapped field-for-field to the V1 schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No business rules yet; B6 (free slots) and B9 (reschedule) will build on
  these. Verified with `ScheduleRepositoryTest` (`@DataJpaTest`, Testcontainers) and
  `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B1, B2 (working hours seed), B4 (working hours read), B6 (slots), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-21 · selman · backend

- **Done:** `working_hours`, `schedule_change_request` and `appointment_rule` tables in Flyway V1. Breaks must lie inside the shift; one rule per service.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
