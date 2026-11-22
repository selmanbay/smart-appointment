# Services and staff

## Scope

| | |
|---|---|
| SRS | F3 |
| Use cases | UC-11 |
| Backend | `backend/src/main/java/com/smartappointment/catalog/` |
| Web | `web/src/features/owner-settings/` |
| Android | — |

## Status

In progress

## Open questions

- None

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Contract operation listServices: `CatalogController` and `CatalogService` filter to
  active services of the organization and answer 404 when the organization does not exist. Added
  `findByOrganizationIdAndIsActiveTrue` to `ServiceRepository`, `existsById` to `StaffLookupService`
  (now also used by `schedule`), and marked the route public (GET only) in `SecurityConfig`. The
  `getAppointmentRule` read lives in `schedule` instead, next to the entity and repository B1 put
  there; see that feature's log.
- **Why:** B4 of the backend MVP handoff; keeping a feature's reads next to its own repository
  avoids a cross-feature repository dependency ArchUnit would reject.
- **State / next:** `CatalogService` is annotated with the fully-qualified
  `org.springframework.stereotype.Service` per ADR-0016 (Proposed), because importing it would
  shadow the `Service` entity the class reads. Verified with `CatalogControllerTest`,
  `CatalogServiceTest`, `make backend-verify`, and manually against the demo data (`make up`):
  `GET /api/v1/organizations/1/services` returns the two active services with their learned
  durations, no token needed; a missing organization answers 404.
- **Refs:** handoff B4, ADR-0016, SRS F3

### 2026-11-22 · yusuf · backend

- **Done:** Added `Staff` and `Service` entities with their repositories. The staff-provides-
  service relationship is mapped as a `@ManyToMany` over `staff_service`; no extra entity was
  needed since the join table has no columns of its own.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No catalog reads endpoint yet (B4). Verified with `CatalogRepositoryTest`
  (`@DataJpaTest`, Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B1 (entities), B2 (seed), B4 (service and rule reads), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-21 · selman · backend

- **Done:** `staff`, `service` and `staff_service` (M:N provides) tables in Flyway V1 with capacity ≥ 1 and positive duration checks.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
