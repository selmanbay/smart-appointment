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
