# Handoff: backend MVP

- **From:** Selman · **To:** Yusuf · **Date:** 2026-11-22 (updated after B3)
- **Starting point:** `main` at the commit that contains this file
- **Your branch:** `develop/yusuf` (ADR-0011)
- **Goal:** implement the backend for the MVP scenarios so the web prototype can switch from mock data to the real API, and the SDD sequence diagrams match running code.

This document assumes you know nothing about the repository. Read it top to bottom once; after that, `docs/PROJECT_MAP.md` and `docs/STATUS.md` are the day-to-day references.

## 1. Set up (about 30 minutes)

Install once:

| Tool | Version | Check |
|---|---|---|
| Git | any recent | `git --version` |
| JDK | 21 (Temurin) | `java -version` shows 21 |
| Docker Desktop or OrbStack | recent | `docker compose version` |
| Node | 24 LTS (only to run the web app) | `node -v` |

Then:

```bash
git clone git@github.com:selmanbay/smart-appointment.git
cd smart-appointment
git switch -c develop/yusuf origin/main
make setup          # git hooks (blocks AI attribution in commits) and .env from .env.example
make help           # every command; they mirror CI
make backend-verify # Checkstyle, Spotless check, unit + ArchUnit tests, integration tests (Docker)
make up             # PostgreSQL, Mailpit, backend, web
```

Then open:

| What | Where |
|---|---|
| Swagger UI (contract next to the implemented API) | <http://localhost:8080/swagger-ui/index.html> |
| Health | <http://localhost:8080/actuator/health> |
| Outgoing e-mail | <http://localhost:8025> |
| Web prototype (mock data) | <http://localhost:5173>, sign in with a demo account |

> **Not yet verified on a machine with Docker:** `make up`, the backend and web images, and `BackendApplicationIT` have only run in GitHub Actions, not locally (Selman's machine has no Docker). If anything fails on your first `make up`, fix it first and log it in `docs/features/platform.md`.

## 2. Read before you code

1. `docs/PROJECT_MAP.md`: folder map, task routing table, test commands
2. `docs/guides/AGENT_RULES.md` (if you use an agent) and `docs/guides/CONTRIBUTING.md`: branches, commits, PRs, versions, clean-code limits, naming, SOLID
3. `docs/decisions/0012-mvp-scope.md`: what the MVP is and is not
4. `docs/decisions/0001`, `0008`: package-per-feature and hosting-ready rules you must follow
5. `api/openapi.yaml` in Swagger UI: filter for operations with `x-mvp: true`
6. SRS sections 2.2 (use cases), 2.5 (data model and integrity rules), 2.6 (Eq. 1–4)

## 3. Where things stand

| Area | State |
|---|---|
| Repository rules, CI, lint, branch model | Done. CI is green on `main`. |
| Backend skeleton | Done: Spring Boot 4.2.0, Java 21, profiles, typed config, stateless security (everything except health, auth and docs returns 401), rate limiting (off locally), RFC 7807 errors, correlation id, Swagger. |
| Database | `V1__initial_schema.sql` (eleven SRS tables); entities and repositories for all of them (B1, Yusuf); local-only demo data `db/seed/R__demo_data.sql` (B2, Selman). |
| API contract | Done: 36 operations in `api/openapi.yaml`, 16 marked `x-mvp: true` (logout added with B3). Linted in CI. |
| Sign-in | Done (B3, Selman, ADR-0015): login, logout, `/me`; JWT in an HttpOnly cookie for browsers or a bearer header; CSRF for cookie requests. |
| Backend features | **B4–B12 not started.** Next is B4. |
| Web | Prototype of the three MVP screens on mock data (`web/src/api/mockStore.ts`). Selman switches it to the API endpoint by endpoint as you deliver them. |
| SDD | Due 2026-11-26. Your implementation should match the sequence diagrams in `docs/sdd/DESIGN_FLOWS.md` (UC-3, UC-4, UC-8); update the diagram when your code differs. |

## 3a. Notes from B1–B3 (read before B4)

**Signed-in user in a controller.** The principal name is the user id; claims carry the rest.

```java
@GetMapping("/api/v1/staff/{staffId}/agenda")
public AgendaResponse agenda(@PathVariable Long staffId, JwtAuthenticationToken authentication) {
  Long userId = Long.valueOf(authentication.getName());
  Long organizationId = authentication.getToken().getClaim("organizationId"); // null for customers and admin
  Long ownStaffId = authentication.getToken().getClaim("staffId");            // null unless the user is staff
  ...
}
```

- **Roles** are authorities `ROLE_CUSTOMER`, `ROLE_STAFF`, `ROLE_ORGANIZATION_OWNER`, `ROLE_SYSTEM_ADMIN`. For role checks add `@EnableMethodSecurity` to `common.security.SecurityConfig` once (first PR that needs it) and use `@PreAuthorize("hasRole('STAFF')")`. Organization scoping (`org_id`) is still checked in the service.
- **Every route is authenticated** except health, `/api/v1/auth/**` and the docs. Public reads in the contract (`security: []`, e.g. `listServices`) must be added to `PUBLIC_PATHS` in `SecurityConfig`.
- **Testing a secured endpoint:** copy the set-up of `auth/AuthControllerTest` (`@WebMvcTest` + `@Import({SecurityConfig, CorsConfig, JwtConfig})` + `@EnableConfigurationProperties({CorsProperties, JwtProperties})`) and send a real token: `.header("Authorization", "Bearer " + TestTokens.issuer(Clock.systemUTC()).issue(account).value())`. Bearer requests need no CSRF token.
- **Do not use** spring-security-test's `csrf()` post-processor: it replaces the CSRF repository in the shared test context and breaks other tests. For cookie flows, read `XSRF-TOKEN` from a GET and send it back as `X-XSRF-TOKEN` (see `AuthFlowIT`).
- **Tests run with the `test` profile** (`application-test.yml`, set in `pom.xml`). The demo data is never loaded in tests; insert what a test needs.
- **Demo accounts** for manual testing (`make up`): see `backend/README.md`; password `demo-password-2026`, stored as `{bcrypt}…`; `PasswordEncoder` is a bean.
- **Cross-feature lookups go through a service**, like `catalog.StaffLookupService` used by `auth`. Never inject another feature's repository (ArchUnit fails).
- **Checkstyle fan-out (max 12 classes, annotations included)** rejects "do everything" classes; split by responsibility (`AuthController` + `AccountController`) instead of asking to relax it. The limit is enforced and cannot be bypassed.
- **Inject `java.time.Clock`** (bean in `common.config`) instead of calling `Instant.now()`, so lead-time and deadline rules can be tested with a fixed time.
- **Open items from the B1 review:** `Appointment`'s constructor does not take `endTime` (NOT NULL); `Appointment.setStatus` allows any transition (replace with the transition map in B10); decide whether `catalog.Service` keeps its name (clashes with Spring's `@Service`, write an ADR either way).

## 4. Your MVP tasks, in order

Each task is one PR from `develop/yusuf` to `main` (or two if it grows past ~400 lines). For every task: implement exactly the contract operation, add the listed tests, run `make backend-verify`, add a log entry to the feature file, and tick the matching line in section 6.

### B1. Entities and repositories (all MVP tables)

- **Where:** `organization/`, `catalog/`, `schedule/`, `booking/`, `notification/`, `feedback/` (entity + repository per package), plus `auth/` for `app_user`.
- **Rules:** map exactly to `V1__initial_schema.sql` (`ddl-auto: validate` fails the app on any mismatch). Enums for `role`, `status`, `channel`, `type` stored as strings. Times as `Instant` / `OffsetDateTime`, never `LocalDateTime`.
- **Tests:** `@DataJpaTest` with Testcontainers per package that saves and reads one entity; the existing `BackendApplicationIT` must still pass.
- **Done when:** the app starts against PostgreSQL with `validate`, and ArchUnit still passes (no repository used across features).

### B2. Seed data for local and demo (`V2`)

- **Where:** a Flyway location used only by the `local` profile, e.g. `db/seed/V2__demo_data.sql`, enabled via `spring.flyway.locations` in `application-local.yml`.
- **Content:** one organization (Northbridge University · Student Services, `Europe/Istanbul`); services *Academic advising* (30 min) and *Document request* (15 min) with appointment rules; staff Maya Chen and Daniel Brooks with Mon–Fri 09:00–16:00, break 12:00–13:00; customer Alex Morgan; an owner and a system admin; **completed appointments with recorded service times over the last two weeks** so waiting estimates are meaningful. Use the same names as `web/src/api/mockStore.ts`.
- **Passwords:** BCrypt hashes of a documented demo password, listed in the seed file header and in `backend/README.md`. No real personal data (AGENT_RULES section 6).
- **Done when:** `make up` gives a database the demo can run on.

### B3. Sign-in with JWT (F1)

- **Contract:** `login`, `getCurrentAccount`.
- **Approach (propose as an ADR):** Spring Security OAuth2 resource server with `NimbusJwtEncoder` / `NimbusJwtDecoder` and an HS256 secret from `JWT_SECRET` (already in `.env.example`), so no extra JWT library is needed. `JwtProperties` record in `common.config` (secret, issuer, lifetime). BCrypt for passwords. Claims: `sub` = user id, `role`, `organizationId`, `staffId`.
- **Rules:** rate-limit group `auth`; wrong e-mail or password both return the same 401 problem.
- **Tests:** unit test for token creation and parsing; `@WebMvcTest` for 200 / 400 / 401; a protected endpoint returns 401 without a token and 200 with one.

### B4. Catalog reads

- **Contract:** `getOrganization`, `listServices`, `getAppointmentRule`, `getWorkingHours`.
- **Rules:** only active services; every organization-scoped query filtered by `org_id` (AGENT_RULES section 8); DTOs as records in each feature's `dto/`.
- **Tests:** `@WebMvcTest` per controller, service unit tests for the active filter and 404s.

### B5. Waiting-time estimation (F9, plain Java)

- **Where:** `estimation/`. No Spring, no repositories (ArchUnit enforces this).
- **Rules:** Eq. 3 `d_new = 0.2 · d_actual + 0.8 · d_old`; Eq. 4 projected start `S_k = max(s_k, F_{k−1})`, `F_k = S_k + d_l,k`, wait `S_k − s_k`, `F_0` = now or opening time; for capacity `c > 1` use the earliest projected finish among `c` parallel positions. Learned duration starts at `expected_duration_min` when there is no history (proposed answer to the open question).
- **Tests:** unit tests using the SRS examples (Eq. 1 gives 21 slots for 09–12 and 13–17 at 20 min; Eq. 2 gives ≈ 81 %), late runs pushing later appointments, capacity 2, an empty day.

### B6. Free slots (UC-3 step 4)

- **Contract:** `listFreeSlots`.
- **Rules:** Eq. 1 from working hours minus breaks, service duration plus buffer, minus booked capacity; then the appointment rule: at least `min_lead_hours` from now, at most `max_advance_days` ahead. Generate in the organization's time zone, return ISO-8601 with offset. Each slot carries its estimated wait (B5).
- **Tests:** unit tests for break handling, buffer, lead time, advance limit, capacity 2 with one booking; slice test for the endpoint.

### B7. Staff recommendation (UC-18, UC-19)

- **Contract:** `listStaffRecommendations`.
- **Rules:** staff who provide the service, active only; rank by estimated wait of their next free slot, ties by mean rating (no rating sorts last); first entry `isRecommended: true` with `recommendationReason: "Shortest estimated wait"`. Computed on request, never stored.
- **Tests:** ranking and tie-break unit tests.

### B8. Book an appointment (UC-3) — the critical one

- **Contract:** `bookAppointment` (rate-limit group `booking`, optional `Idempotency-Key`).
- **Rules (SRS 2.5):** staff provides the service; slot inside working hours; lead time and advance limit; `max_active_per_user`; capacity never exceeded. Do the check and insert in **one transaction under a lock**. Proposed answer to the open question: `SELECT … FOR UPDATE` on the staff row, then count overlapping BOOKED / CHECKED_IN / IN_SERVICE appointments, then insert. Failed rules return 409 with the matching `code` from `BookingProblem`.
- **Tests:** unit tests per rule; **an integration test that fires parallel requests for the last free place and asserts exactly one succeeds** (AGENT_RULES section 7).

### B9. Reschedule and cancel (UC-4, UC-5)

- **Contract:** `rescheduleAppointment`, `cancelAppointment`.
- **Rules:** reschedule takes the new slot (same checks and lock as B8) before releasing the old one, in one transaction; if the new slot fails, the old appointment is unchanged. Customers only until `cancel_deadline_hours` before start (`CANCEL_DEADLINE_PASSED`); owners may cancel any booked appointment. Appointments are never deleted.
- **Tests:** reschedule success, reschedule to a taken slot keeps the original, deadline rule.

### B10. Service status and learned duration (UC-8)

- **Contract:** `changeAppointmentStatus`.
- **Rules:** only `BOOKED → CHECKED_IN → IN_SERVICE → COMPLETED` and `BOOKED → NO_SHOW` (use a transition map, not if-chains, AGENT_RULES section 13); the server stamps `checked_in_at`, `service_start_at`, `service_end_at`; on COMPLETED update `service.learned_duration_min` with Eq. 3 in the same transaction. Only the appointment's staff member (or the owner) may change it.
- **Tests:** every allowed and forbidden transition; completion updates the learned duration.

### B11. Agenda and my appointments (UC-6, UC-8)

- **Contract:** `getStaffAgenda`, `listMyAppointments`, `getAppointment`.
- **Rules:** agenda in time order with that day's working hours; my appointments paginated, `UPCOMING` nearest first, `PAST` newest first; `estimatedWaitMinutes` for BOOKED and CHECKED_IN; `canCancel` computed from the rule and deadline.

### B12. E-mail notifications (UC-20)

- **Where:** `notification/` with a `NotificationSender` interface and an SMTP implementation (Mailpit locally).
- **Rules:** send confirmation, reschedule and cancellation e-mails **after commit** (`@TransactionalEventListener(phase = AFTER_COMMIT)`); store each attempt in `notification` with `status` and `provider_response`. A failed send never rolls back the appointment (AGENT_RULES section 8).
- **Tests:** a booking with a failing sender still succeeds and records `FAILED`.

## 5. Decisions to settle while you work

Write each as an ADR with status `Proposed` and tell Selman; do not wait for an answer to keep going with the proposed option.

| Question (feature file) | Proposed answer |
|---|---|
| Locking strategy for concurrent bookings (`booking`) | `SELECT … FOR UPDATE` on the staff row inside the booking transaction |
| Refresh token needed? (`auth`) | No for the MVP; access token lifetime 8 hours, configurable |
| Staff and owner accounts: sign-up or invitation? (`auth`) | Created by the owner / admin (seed data for the MVP) |
| Learned duration with no history (`estimation`) | Start at `expected_duration_min` |
| JWT library | Spring Security resource server with Nimbus (no extra dependency) |

## 6. Checklist

- [x] B1 Entities and repositories (merged in PR #7)
- [x] B2 Seed data (Selman, `db/seed/R__demo_data.sql`; `DemoDataIT` runs in CI; verified locally with Docker by Yusuf on 2026-11-22, see `docs/features/platform.md`)
- [x] B3 JWT sign-in (Selman; ADR-0015, `AuthFlowIT` runs in CI; verified locally with Docker by Yusuf on 2026-11-22, see `docs/features/platform.md`)
- [x] B4 Catalog reads (Yusuf; ADR-0016, `CatalogControllerTest`, `OrganizationControllerTest`,
  `ScheduleControllerTest` run in CI)
- [x] B5 Estimation (Yusuf; ADR-0017, `LearnedDurationTest`, `WaitingTimeEstimatorTest`,
  `SlotsPerDayTest`, `UtilisationTest`)
- [ ] B6 Free slots (Eq. 1 + rules)
- [ ] B7 Staff recommendation
- [ ] B8 Booking with capacity lock + concurrency test
- [ ] B9 Reschedule and cancel
- [ ] B10 Status changes + learned duration
- [ ] B11 Agenda and my appointments
- [ ] B12 E-mail notifications

## 7. Working agreement

- Work on `develop/yusuf`; rebase on `main` daily (`git pull --rebase origin main`); open small PRs to `main`; squash merge.
- Contract first: if an endpoint needs a change, change `api/openapi.yaml` in its own PR and tell Selman, because the web app uses it.
- Every PR updates the feature log (`docs/features/<feature>.md`) and, when a status changes, `docs/STATUS.md` (`docs/guides/LOGGING.md`).
- `make verify` before every PR; CI must be green. Do not disable lint rules or raise limits (AGENT_RULES section 11).
- Lint limits for Java: methods ≤ 40 lines, ≤ 4 parameters, complexity ≤ 10, files ≤ 300 lines; no `@SuppressWarnings`; names from the SRS vocabulary.
- No AI attribution in commits or PRs; the hook and CI block it.

## 8. Coordination

- **Selman:** web app, SDD diagrams, contract changes. Tell him when an endpoint is merged; he switches the matching screen from mock data.
- **Order matters for the demo:** B3 + B4 + B6 + B7 + B8 make the booking screen real; B10 + B11 make the staff agenda real; B9 completes my appointments.
- Questions and decisions go into the feature file's `## Open questions` or an ADR, not only into chat, so the next person (or agent) sees them.
