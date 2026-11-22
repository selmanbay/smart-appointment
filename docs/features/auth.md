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

- [x] Token lifetime: is a refresh token needed? No for the MVP: 8-hour access token, configurable with `JWT_LIFETIME` (ADR-0015).
- [ ] Are Staff and Owner accounts created by sign-up or by invitation? Proposed: created by the owner or admin; seed data for the MVP.

## Log

### 2026-11-22 · selman · backend

- **Done:** B3 sign-in (ADR-0015, Proposed): `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/me`. Stateless HS256 JWT (Spring Security resource server, no extra library) with `sub`, `role`, `organizationId`, `staffId`; secret from `JWT_SECRET` (startup fails under 32 bytes). Browsers get an HttpOnly `SameSite=Strict` cookie on `/api`; Android uses the bearer token from the response body. CSRF stays on for cookie requests (SPA `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header); bearer requests and login are exempt. Passwords via the delegating encoder (`{bcrypt}`); unknown e-mail and wrong password give the same 401 problem and take the same time. Contract updated (logout, `cookieAuth`, CSRF header).
- **Why:** F1 / UC-2 for the MVP; tokens out of JavaScript's reach (XSS) without giving up CSRF protection.
- **State / next:** 58 unit and slice tests pass locally; `AuthFlowIT` (login → `/me` with cookie → logout with CSRF) runs only in CI. A test caught that resolving the cookie in Spring's bearer resolver silently disabled CSRF for cookie requests; the cookie is now turned into a bearer header by a filter that runs after the CSRF check. The web app still uses the in-memory demo session; switching it to `/auth/login` and `/me` is the next web task.
- **Refs:** ADR-0015, handoff B3, contract `login`, `logout`, `getCurrentAccount`

### 2026-11-22 · yusuf · backend

- **Done:** Added the `AppUser` entity, `UserRole` enum and `AppUserRepository` (with
  `findByEmail`), mapped field-for-field to the V1 schema.
- **Why:** B1 (entities and repositories) from the backend MVP handoff.
- **State / next:** Sign-in and the JWT filter (B3) are not started; the
  `ck_app_user_org_required` constraint is enforced by the database, not yet by a service.
  Verified with `AppUserRepositoryTest` (`@DataJpaTest`, Testcontainers) and `make backend-verify`.
- **Refs:** SRS Table 9, ADR-0001

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
