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

In progress

## Open questions

- [x] For a service with no completed appointments, does learned_duration start at expected_duration? Yes, see ADR-0017.

## Log

### 2026-11-26 · yusuf · backend

- **Done:** Completed B5 as the handoff specifies: `estimation.SlotsPerDay` (Eq. 1) and `estimation.Utilisation` (Eq. 2), plain Java, with the SRS examples as unit tests (21 slots for 09-12 and 13-17 at 20 min; 17 × 20 min in 420 min ≈ 81 %), plus buffer, capacity 2 and empty-day cases. Two more Eq. 4 tests: `F_0` = now on the current day, and an on-time run with no wait.
- **State / next:** This corrects the 2026-11-22 entry, which left Eq. 1 and Eq. 2 for later. B6 (free slots) uses `SlotsPerDay`; B7 and analytics use `Utilisation`. Not wired into any bean or endpoint yet.
- **Refs:** B5, SRS Eq. 1–4

### 2026-11-22 · yusuf · backend

- **Done:** `estimation.LearnedDuration.next(...)` (Eq. 3) and `estimation.WaitingTimeEstimator.estimate(...)`
  (Eq. 4, including the capacity > 1 parallel-position case) implemented as plain Java, no Spring, no
  repositories; `ArchitectureTest.estimationIsPlainJava` and the rest of `make backend-verify` pass
  (88 tests: 81 unit/slice/architecture incl. 5 new estimation unit tests, plus 7 integration).
- **Why:** B5, next step after catalog reads (B4) towards free slots (B6) and staff recommendation (B7).
- **State / next:** Not wired into any Spring bean or controller yet; B6/B7/B10/B11 call these classes
  directly once they exist. No Eq. 1 (slots per day) or Eq. 2 (utilisation) code added here; those stay
  with the free-slots (B6) and recommendation/analytics work that actually needs them.
- **Refs:** B5, ADR-0017, SRS Eq. 3, Eq. 4

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B5 (Eq. 3, Eq. 4), B7 (staff recommendation), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-22 · selman · web

- **Done:** Staff option card shows estimated wait, workload as "x of y slots booked", mean rating and a plain "Recommended: shortest estimated wait" line; the mock API ranks by wait, then rating.
- **State / next:** Values are sample data; the real numbers come from Eq. 3 and Eq. 4 in the backend.
- **Refs:** UC-18, UC-19

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
