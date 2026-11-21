# ADR-0009: Each app is self-contained, with its own tests

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** general

## Context
The first layout kept load tests in a shared `infra/` folder and did not say where web and Android tests go. The team wants one clear rule: an app's code, tests and build files live together in that app's folder.

## Decision
Top level:

```
backend/    Spring Boot API: code, unit/slice/integration/architecture tests, load tests, Dockerfile
web/        React app: code, colocated unit tests, e2e tests, lint config, Dockerfile
android/    Kotlin app: code, local unit tests, instrumented tests
api/        openapi.yaml, the contract shared by all three apps
docs/       SRS, design, status, feature logs, decisions
scripts/    repository tooling (setup, checks)
.github/    CI and repository rules
```

Tests per app:

| App | Unit | Integration / UI | Load / e2e |
|---|---|---|---|
| backend | `backend/src/test/java/com/smartappointment/<feature>/*Test.java` | same folders, `*IT.java` (Testcontainers) | `backend/load-tests/` (k6) |
| web | next to the file: `src/features/<feature>/BookingForm.test.tsx` (Vitest) | same, React Testing Library | `web/e2e/` (Playwright) |
| android | `android/app/src/test/java/com/smartappointment/android/<feature>/` | `android/app/src/androidTest/java/com/smartappointment/android/<feature>/` | n/a |

- Test folders mirror the code's feature folders (ADR-0001).
- `api/` stays at the top level because no single app owns the contract.
- The shared `infra/` folder is removed; k6 load tests moved to `backend/load-tests/` (this replaces the `infra/k6/` path in ADR-0008).

## Consequences
- Anyone working on one app needs only that folder plus `api/`.
- CI jobs map one-to-one to app folders.
