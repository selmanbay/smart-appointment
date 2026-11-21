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

Not started

## Open questions

- [x] Is UC-13 (logo, terminology) part of the MVP? No (ADR-0012).

## Log

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B1, B2, B4 (organization profile), with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-21 · selman · backend

- **Done:** `organization` table in Flyway V1; `terminology` stored as JSONB.
- **Refs:** SRS Table 9

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
