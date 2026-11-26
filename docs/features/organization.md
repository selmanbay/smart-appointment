# Organization management

## Scope

| | |
|---|---|
| SRS | F2 (+ UC-13 branding) |
| Use cases | UC-13, UC-16 |
| Backend | `backend/src/main/java/com/smartappointment/organization/` |
| Web | `web/src/features/admin-organizations/` |
| Android | — |

## Status

In progress

## Open questions

- [x] Is UC-13 (logo, terminology) part of the MVP? No (ADR-0012).
- [ ] Does `getOrganization` need `ownerName` populated? Returns `null` for now; computing it needs
  owner data from `auth` and the admin-facing operations that would use it are out of MVP scope
  (ADR-0012).

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Contract operation getOrganization: `OrganizationController` and `OrganizationService`,
  mapped to `OrganizationResponse`. Added `existsById` so other features can check organization
  existence without touching the repository (used by `catalog`). Marked the route public (GET only)
  in `SecurityConfig`.
- **Why:** B4 of the backend MVP handoff.
- **State / next:** `ownerName` is always `null` for now (see open question). Verified with
  `OrganizationControllerTest`, `OrganizationServiceTest`, `make backend-verify`, and manually
  against the demo data: `GET /api/v1/organizations/1` returns Northbridge University's profile
  without a token; `GET /api/v1/organizations/999` answers 404.
- **Refs:** handoff B4, SRS F2

### 2026-11-22 · yusuf · backend

- **Done:** Added the `Organization` entity and `OrganizationRepository`, mapped field-for-field to
  the V1 schema; `terminology` uses Hibernate's native JSON mapping over the `jsonb` column.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No service or controller yet. Verified with `OrganizationRepositoryTest`
  (`@DataJpaTest`, Testcontainers) and `make backend-verify` (schema validates, ArchUnit passes).
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B1, B2, B4 (organization profile), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-21 · selman · backend

- **Done:** `organization` table in Flyway V1; `terminology` stored as JSONB.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
