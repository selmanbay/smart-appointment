# ADR-0010: Standard CI pipeline with local parity

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** platform

## Context

The first CI ran one build job per app and had an invalid condition that stopped the whole workflow. The team wants a standard development environment: every change linted, tested and built the same way, with clear failures and the same commands available locally.

## Decision

- **`CI` workflow** (`.github/workflows/ci.yml`), on every PR and push to `main`:
  - `repo-lint`: English-only check, actionlint (workflows), hadolint (Dockerfiles), markdownlint.
  - `backend-lint` (Spotless, Checkstyle), `backend-test` (unit, slice, ArchUnit, Testcontainers integration tests, JaCoCo; reports uploaded), `backend-image` (Docker build).
  - `web` (ESLint, Prettier, type check, Vitest, build) and `android` (ktlint, Android lint, tests, build), once those apps exist.
  - App jobs run only when their files change; `ci-ok` aggregates every job and is the one required CI check.
- **`Security` workflow**: dependency review on PRs (fails on high severity) and CodeQL for Java, TypeScript and workflows on PRs, `main` and weekly. Informational, not required.
- **Required checks in the ruleset:** `ci-ok`, `check` (PR title), `no-ai-attribution`. The separate English-only workflow was folded into `repo-lint`.
- **Local parity:** a root `Makefile` (`make setup`, `make up`, `make lint`, `make format`, `make test`, `make verify`) runs the same tools with the same configuration. Editor extensions are recommended in `.vscode/extensions.json`.
- Action versions are pinned to current major tags; Dependabot keeps them up to date.

## Consequences

- A red check names the exact problem (formatting, a lint rule, a test, the image).
- Contributors can reproduce CI before pushing; `make verify` is part of the definition of done.
- actionlint and hadolint run only in CI; they are not installed locally.
