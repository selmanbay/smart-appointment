# Authentication and roles

## Scope

| | |
|---|---|
| SRS | F1 |
| Use cases | UC-1, UC-2 |
| Backend | `backend/src/main/java/com/smartappointment/auth/` |
| Web | `web/src/features/auth/` |
| Android | Sign in / sign up |

## Status

Not started

## Open questions

- [ ] Token lifetime: is a refresh token needed? Proposed: no for the MVP, 8-hour access token (handoff B3).
- [ ] Are Staff and Owner accounts created by sign-up or by invitation? Proposed: created by the owner or admin; seed data for the MVP.

## Log

### 2026-11-22 · selman · docs

- **Done:** Backend MVP work for this feature handed off to Yusuf as B3 (JWT sign-in) and the auth part of B1/B2, with rules and required tests.
- **Refs:** `docs/handoff/2026-11-22-backend-mvp.md`, ADR-0012

### 2026-11-22 · selman · web

- **Done:** Prototype sign-in with two demo accounts (customer, staff) held in memory, and a role guard on routes.
- **State / next:** Replace with e-mail and password against the JWT endpoint once it exists.

### 2026-11-21 · selman · backend

- **Done:** `app_user` table in Flyway V1 (unique e-mail, role check, org required for owner and staff). Security is stateless and returns 401 for everything except `/api/v1/auth/**` and health; JWT is not implemented yet.
- **State / next:** Implement sign-up, sign-in and the JWT filter in `auth/`.
- **Refs:** SRS Table 9, ADR-0008

### 2026-11-21 · selman · docs

- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
