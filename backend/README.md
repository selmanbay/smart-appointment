# backend

Spring Boot REST API for Smart Appointment. The skeleton is in place: configuration, security, rate limiting, error handling, the database schema (Flyway V1) and the quality gates. Feature code (controllers, services, entities) is added per feature.

## Stack

- Java 21 (Temurin), Spring Boot 4.2.0, Maven wrapper (`./mvnw`)
- Spring Web MVC, Security, Data JPA, Validation, Mail, Actuator
- springdoc-openapi 3.2.1 (Swagger UI for the contract and the implementation)
- PostgreSQL 17, Flyway
- Bucket4j (rate limiting)
- Tests: JUnit 5, AssertJ, Spring Boot test slices, Testcontainers, ArchUnit
- Quality: Spotless (google-java-format), Checkstyle 14.4.0, JaCoCo (`config/README.md`)

All versions are pinned in `pom.xml`.

## Run

With Docker (recommended, from the repository root):

```bash
docker compose up --build
```

The API listens on <http://localhost:8080>, health on <http://localhost:8080/actuator/health>, Swagger UI on <http://localhost:8080/swagger-ui/index.html> (ADR-0014). Outgoing e-mail appears in Mailpit at <http://localhost:8025>.

Without Docker for the app itself (PostgreSQL still needed, e.g. `docker compose up db mailpit`):

```bash
cd backend
./mvnw spring-boot:run
```

## Build and test

```bash
./mvnw spotless:apply     # format
./mvnw verify             # checkstyle, unit + slice + architecture tests, integration tests, coverage
```

Integration tests (`*IT`) start PostgreSQL with Testcontainers and are skipped when Docker is not available.

## Layout (ADR-0001, ADR-0009)

```text
backend/
  pom.xml, mvnw, Dockerfile
  config/checkstyle.xml           lint rules (ADR-0007)
  load-tests/                     k6 scenarios for SRS P1, P2
  src/main/java/com/smartappointment/
    BackendApplication.java
    common/
      config/          typed @ConfigurationProperties records (the only place reading env vars)
      security/        stateless security, CORS
      ratelimit/       RateLimiter interface, in-memory Bucket4j implementation, filter (off locally)
      web/             RFC 7807 error handling, correlation id
      observability/   health, metrics, logging setup
      jobs/            scheduled job support
    auth/ organization/ catalog/ schedule/ booking/
    estimation/ notification/ analytics/ feedback/      one package per feature (empty for now)
  src/main/resources/
    application.yml            safe defaults, env vars with local fallbacks
    application-local.yml      docker compose / local runs
    application-prod.yml       hosted: JSON logs, rate limiting on
    db/migration/V1__initial_schema.sql   SRS Table 9
  src/test/java/com/smartappointment/
    ArchitectureTest.java, FeatureRules.java     ArchUnit rules
    BackendApplicationIT.java                    full app + PostgreSQL
    common/...                                   unit and slice tests
    <feature>/                                   tests mirror the main packages
```

## Database

`V1__initial_schema.sql` creates the eleven tables from SRS section 2.5 with its integrity constraints (unique e-mail, capacity ≥ 1, rating 1–5, status values, start before end, working-hour breaks inside the shift). Notes:

- `user` is reserved in PostgreSQL, so the table is `app_user`.
- IDs are `BIGINT` identity columns; times are `timestamptz` in UTC.
- `notification.provider_response` stores the FCM / SMTP result (SRS 1.4.1.1).
- Rules that need data from several rows (capacity under concurrency, appointments inside working hours, one active booking limit) are enforced in the services, not in SQL.

Never edit a merged migration; add `V2__...`.
