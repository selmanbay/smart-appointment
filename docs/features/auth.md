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

In progress

## Open questions

- [ ] Token lifetime: is a refresh token needed?
- [ ] Are Staff and Owner accounts created by sign-up or by invitation?

## Log

### 2026-11-22 · yusuf · backend

- **Done:** Added the `AppUser` entity, `UserRole` enum and `AppUserRepository` (with
  `findByEmail`), mapped field-for-field to the V1 schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** Sign-in and the JWT filter (B3) are not started; the
  `ck_app_user_org_required` constraint is enforced by the database, not yet by a service.
  Verified with `AppUserRepositoryTest` (`@DataJpaTest`, Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

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
