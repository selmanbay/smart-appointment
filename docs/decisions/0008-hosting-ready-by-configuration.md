# ADR-0008: Hosting-ready by configuration

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** platform

## Context
The MVP runs locally (ADR-0005), but the system must be deployable later without rewriting code. Rate limits, test layers, monitoring and scaling should be things we switch on or tune, not features we bolt on. SRS P1–P3 already set production-like targets (500 concurrent users, 50 requests/s, notifications within 30 s).

## Decision
The same code and the same Docker image run everywhere; only configuration changes.

**Configuration**
- Spring profiles: `local` (default in docker compose), `test`, `prod`. Files: `application.yml` (safe defaults), `application-local.yml`, `application-prod.yml`.
- Every environment-specific value comes from an environment variable with a local default: database URL and credentials, JWT secret, SMTP host, CORS origins, rate limits, feature flags. No hard-coded URLs, hosts or secrets.
- Typed configuration: each concern has one `@ConfigurationProperties` record under `common.config` (for example `RateLimitProperties`, `CorsProperties`, `JwtProperties`). Nothing else reads environment variables.
- Web: the API base URL comes from `VITE_API_URL`; nothing else is environment-specific in the frontend.

**Stateless backend**
- JWT authentication, no server-side sessions, no files written to local disk. Any number of backend instances can run behind a load balancer.

**Rate limiting (off locally, on when hosted)**
- `common.ratelimit` holds a servlet filter that depends on a `RateLimiter` interface.
- First implementation: in-memory Bucket4j. When there is more than one instance, swap to a Redis-backed implementation without touching the filter or any feature.
- Policies are configuration, keyed by route group:

```yaml
app:
  rate-limit:
    enabled: ${RATE_LIMIT_ENABLED:false}
    policies:
      auth:     { capacity: 10,  refill-per-minute: 10 }   # login, register
      booking:  { capacity: 30,  refill-per-minute: 30 }   # create, reschedule, cancel
      default:  { capacity: 120, refill-per-minute: 120 }
```

- Limits are per user when authenticated, per client IP otherwise. Exceeded requests get `429 Too Many Requests` with `Retry-After` and an RFC 7807 problem body.

**Cross-cutting concerns live in `common`, behind interfaces**

| Package | Concern | Local | Hosted |
|---|---|---|---|
| `common.config` | Typed properties | defaults | env vars / secrets manager |
| `common.security` | JWT, CORS, security headers | permissive CORS for localhost | configured origins, HTTPS behind proxy |
| `common.ratelimit` | Rate limiting | disabled | enabled, Redis when scaled |
| `common.web` | Error handling (RFC 7807 `ProblemDetail`), correlation id, pagination | same | same |
| `common.observability` | Actuator health/readiness, Micrometer metrics, structured logs | plain logs | JSON logs, metrics endpoint |
| `notification` | `NotificationSender` interface | SMTP to Mailpit | real SMTP, FCM |
| `common.jobs` | Scheduled jobs (reminders) | single instance | add ShedLock so only one instance runs a job |

**API**
- All endpoints under `/api/v1`. A breaking change later goes to `/api/v2`.
- List endpoints are paginated from the start (`page`, `size`, max size enforced).
- Errors use RFC 7807 problem details everywhere.

**Tests: layered so more can be added without restructuring**

| Layer | Tool | Location | Runs |
|---|---|---|---|
| Unit | JUnit 5, AssertJ; Vitest on web | next to each feature (`src/test/.../<feature>`) | every build |
| Slice | `@WebMvcTest`, `@DataJpaTest` | same | every build |
| Integration | Testcontainers PostgreSQL | same, class suffix `IT` | `./mvnw verify`, CI |
| Architecture | ArchUnit | `ArchitectureTest` | every build |
| Contract | API responses validated against `api/openapi.yaml` | backend `IT` tests | CI |
| End-to-end | Playwright against `docker compose` | `web/e2e/` | added when screens exist |
| Load | k6 (SRS P1, P2) | `infra/k6/` | manually / before releases |

- Test data comes from builders per feature (`AppointmentTestData`), not shared SQL dumps.
- Coverage is reported by JaCoCo from day one; a minimum threshold is set in `pom.xml` once the core features exist and raised over time.

**Delivery**
- Multi-stage Dockerfiles, non-root user, health checks. The image built in CI is the one that would be deployed.
- Database changes only through Flyway; the app migrates on start in `local`, a separate migration step in `prod`.

## Consequences
- Going live is mostly configuration: set environment variables, enable rate limiting, point SMTP and database at real services, put a reverse proxy with HTTPS in front.
- Slightly more structure on day one (properties records, interfaces in `common`), which agents must follow (`AGENTS.md` section 14).
- Redis, ShedLock, Playwright and SonarCloud are deferred but have a defined place.

## Alternatives considered
- Build for local only and refactor before hosting: cheaper now, but rate limiting, config and statelessness touch every feature later.
- API gateway for rate limiting: good when hosted, but unavailable locally; the filter can be disabled once a gateway exists.
