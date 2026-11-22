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

- [ ] Is UC-13 (logo, terminology) part of the MVP?

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Added the `Organization` entity and `OrganizationRepository`, mapped field-for-field to
  the V1 schema; `terminology` uses Hibernate's native JSON mapping over the `jsonb` column.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** No service or controller yet. Verified with `OrganizationRepositoryTest`
  (`@DataJpaTest`, Testcontainers) and `make backend-verify` (schema validates, ArchUnit passes).
- **Refs:** SRS Table 9, ADR-0001

### 2026-11-21 · selman · backend

- **Done:** `organization` table in Flyway V1; `terminology` stored as JSONB.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
