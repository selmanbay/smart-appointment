# backend

Spring Boot REST API. No code yet.

## Planned stack

- Java 21 (Temurin), Spring Boot 4.0.x, Maven wrapper
- Spring Web, Security (JWT), Data JPA, Validation, Mail
- Flyway (schema), springdoc-openapi, Testcontainers (tests)
- Firebase Admin SDK (FCM, after the MVP)

The project will be generated with start.spring.io; the versions current on that day are pinned in `pom.xml`.

## Package layout (ADR-0001)

```
com.smartappointment
  common/
    config/        typed @ConfigurationProperties records (the only place reading env vars)
    security/      JWT, CORS, security headers
    ratelimit/     rate limit filter + RateLimiter interface (off locally)
    web/           RFC 7807 errors, correlation id, pagination
    observability/ health, metrics, logging setup
    jobs/          scheduled job support
  auth/            F1 sign-in, JWT, roles
  organization/    F2
  catalog/         F3 services, staff
  schedule/        F4 working hours, rules, change requests
  booking/         F5 slots, F6 cancel/reschedule, F8 status
  estimation/      F9 Eq. 3 (EWMA) and Eq. 4 (waiting time)
  notification/    F7 SMTP, FCM, reminder job
  analytics/       F10
  feedback/        F11
```

Each feature package holds its own controller, service, repository, entities and `dto/`. The data model follows SRS section 2.5, Table 9. Feature logs: `docs/features/`.

## Configuration and hosting (ADR-0008)

```
src/main/resources/
  application.yml          safe defaults, every env-specific value as ${ENV_VAR:default}
  application-local.yml    docker compose: Mailpit, rate limiting off, permissive CORS
  application-prod.yml     JSON logs, rate limiting on, strict CORS
  db/migration/            Flyway migrations
```

The same image runs locally and when hosted; only environment variables change. See `.env.example`.

## Quality and tests

- Lint and architecture rules: `config/README.md` (ADR-0007).
- Test layers (unit, slice, integration `*IT`, ArchUnit, contract, load): ADR-0008.
