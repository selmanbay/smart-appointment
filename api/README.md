# api

`openapi.yaml` is the single contract between the backend, the web app and Android. Rules: `docs/guides/CONTRIBUTING.md` section 6.

Endpoint changes land in this file first, in their own PR, and are implemented afterwards.

- **Read it:** Swagger UI at <http://localhost:8080/swagger-ui/index.html> when the backend runs (ADR-0014), or paste the file into any OpenAPI viewer.
- **Lint it:** `make api-lint` (Redocly, rules in `redocly.yaml`). CI runs the same check.
- **Conventions:** see the `info.description` block at the top of `openapi.yaml` (paths under `/api/v1`, RFC 7807 errors, pagination, `x-rate-limit-group`, `x-srs`, `x-mvp`).
- **MVP operations** are marked `x-mvp: true` (ADR-0012).
