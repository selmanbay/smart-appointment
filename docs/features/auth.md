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

- [ ] Token lifetime: is a refresh token needed?
- [ ] Are Staff and Owner accounts created by sign-up or by invitation?

## Log

### 2026-11-21 · selman · backend
- **Done:** `app_user` table in Flyway V1 (unique e-mail, role check, org required for owner and staff). Security is stateless and returns 401 for everything except `/api/v1/auth/**` and health; JWT is not implemented yet.
- **State / next:** Implement sign-up, sign-in and the JWT filter in `auth/`.
- **Refs:** SRS Table 9, ADR-0008

### 2026-11-21 · selman · docs
- **Done:** Created the feature file; scope taken from the SRS.
- **Refs:** ADR-0001, ADR-0002
