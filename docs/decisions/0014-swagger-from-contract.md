# ADR-0014: Swagger UI serves the contract and the implementation

- **Status:** Accepted
- **Date:** 2026-11-22
- **Feature:** platform

## Context

The API is contract-first (`api/openapi.yaml`, CONTRIBUTING section 6). Frontend and backend developers need to read the contract in a browser, and later compare it with what the backend actually implements.

## Decision

- The backend includes springdoc-openapi. Swagger UI at `/swagger-ui/index.html` offers two descriptions:
  - **Contract (api/openapi.yaml):** copied into the jar as `/openapi.yaml` at build time (`maven-resources-plugin`), so the image always ships the contract it was built from.
  - **Implemented (generated from code):** `/v3/api-docs`, produced by springdoc from the controllers.
- Swagger UI and the generated description are on by default and off in the `prod` profile (`SWAGGER_ENABLED`).
- The backend Docker image is built from the repository root (`docker build -f backend/Dockerfile .`) so `api/` is available.
- The contract is linted with Redocly CLI (`make api-lint`, CI `repo-lint`); every operation needs an `operationId`, a summary, tags and a 4xx response.

## Consequences

- Anyone can read the contract at <http://localhost:8080/swagger-ui/index.html> once the stack runs.
- Differences between the two descriptions show what is not implemented yet or deviates from the contract.
- `BackendApplicationIT` checks that Swagger UI and both descriptions are served.
