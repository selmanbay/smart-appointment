# Work history

Source for the SDD's Appendix 5.2 (Sprint 2 overview) and 5.1 (milestones and tasks). Every row comes from the repository history (`git log`) and the feature logs in `docs/features/`; commit ids let anyone check it. Work done outside the repository (meetings, SDD writing, supervisor sessions) is not listed and should be added by the team.

- **Repository:** `selmanbay/smart-appointment`
- **Period covered:** 2026-11-21 to 2026-11-22 (implementation start of Sprint 2)
- **People:** Selman Bayburt (git author `selman`), Yusuf Aydın (git author `Duxew`), Barış Alkan and Zanyar Erkozan (no commits yet)

> **Hours:** git records when work was committed, not how long it took. The `Hours` column is left for each person to fill in from their own records before it is used in the burndown chart.

## 1. Completed work items

| # | Date | Person | Work item | What was done | Traceability | Commit | Hours |
|---|---|---|---|---|---|---|---|
| 1 | 2026-11-21 | Selman Bayburt | Repository and team rules | Monorepo layout (backend, web, android, api, docs), contribution rules, agent rules, PR template, CODEOWNERS, branch ruleset, Dependabot, no-AI-attribution and English-only checks, logging guide, per-feature logs, status page, decision records 0001–0008 | ADR-0001–0008 | `b940dec` | |
| 2 | 2026-11-21 | Selman Bayburt | Backend skeleton | Spring Boot 4.2 on Java 21; profiles; typed configuration; stateless security; rate limiting (Bucket4j); RFC 7807 errors; correlation id; Flyway V1 schema for the eleven SRS tables; Dockerfile; Spotless, Checkstyle, ArchUnit, JaCoCo; self-contained app folders | SRS 2.5 Table 9, ADR-0007–0009 | `cc33119` | |
| 3 | 2026-11-21 | Selman Bayburt | Project map | Structure map, task routing table, test commands and quality gates for people and agents | ADR-0002 | `58a63ef` | |
| 4 | 2026-11-21 | Selman Bayburt | CI pipeline | Repo lint (English, actionlint, hadolint, markdownlint), backend lint / test / image, web and Android jobs, security workflow (CodeQL, dependency review), Makefile mirroring CI; three CI fixes found on first runs | ADR-0010 | `cdb909b`, `22fe73b`, `5f13783`, `8c3f33b` | |
| 5 | 2026-11-21 | Selman Bayburt | Documentation structure | Rules and guides moved to `docs/guides/`; short entry points for agents | ADR-0002 | `c90ba6c` | |
| 6 | 2026-11-22 | Selman Bayburt | Branch model and MVP scope | Personal `develop/<name>` branches; MVP built around the SDD critical scenarios (UC-3, UC-4/UC-5, UC-8) | ADR-0011, ADR-0012 | `991778f` | |
| 7 | 2026-11-22 | Selman Bayburt | Web prototype of MVP screens | React 19 + Mantine (only UI library), one theme, shared layout with fixed spacing, screens for Book appointment, My appointments, Staff daily agenda on sample data; UI guidelines replacing the generic styling of the static prototype | SRS Figures 2–4, UC-3, UC-5, UC-6, UC-8, ADR-0013 | `2fc2277` | |
| 8 | 2026-11-22 | Selman Bayburt | API contract and Swagger | OpenAPI 3.1 contract with 35 operations covering UC-1 to UC-20, contract lint, Swagger UI showing contract and implementation | All UCs, ADR-0014 | `60e784d` | |
| 9 | 2026-11-22 | Selman Bayburt | Backend MVP handoff | Cold-start handoff with twelve ordered backend tasks (B1–B12), rules and required tests | ADR-0012 | `b8e62d0` | |
| 10 | 2026-11-22 | Yusuf Aydın | Entities and repositories (B1) | JPA entities and Spring Data repositories for all eleven SRS tables, one package per feature, repository tests against PostgreSQL | SRS Table 9, ADR-0001 | `4582bf9` (PR #7) | |
| 11 | 2026-11-22 | Selman Bayburt | Demo data (B2) | Local-only seed: organization, owner, admin, two staff, five customers, services and rules, working hours, two weeks of completed appointments with service times and ratings, upcoming bookings | ADR-0012, handoff B2 | `f6c0a98` | |
| 12 | 2026-11-22 | Selman Bayburt | Sign-in (B3) | Login, logout, current account; stateless JWT in an HttpOnly cookie for the web and a bearer header for Android; CSRF protection; uniform 401; unit, slice and end-to-end tests | F1, UC-2, ADR-0015 | `62739b3` | |

## 2. Per person

| Person | Work items | Areas |
|---|---|---|
| Selman Bayburt | 1–9, 11, 12 | Repository and CI setup, backend skeleton, web prototype, API contract, demo data, sign-in |
| Yusuf Aydın | 10 | Backend data layer (entities, repositories) |
| Barış Alkan | — | No repository work yet |
| Zanyar Erkozan | — | No repository work yet |

## 3. Sprint review input

- **Goal of the sprint's implementation part:** a runnable foundation for the MVP scenarios that the SDD describes.
- **Met:** the repository, CI and rules are in place; the database schema, entities and demo data exist; sign-in works; the three MVP screens exist on sample data; the API contract covers every use case.
- **Not yet met:** booking, rescheduling, status recording and the waiting-time estimate (B4–B12) have no backend implementation; the web app does not call the API yet.
- **Problems found and fixed during the sprint (from the feature logs):**
  - CI was rejected by GitHub because `hashFiles()` is not allowed in job conditions; actionlint and hadolint then flagged a shell pattern and a non-numeric container user.
  - The seed design was changed from `V2__` to a repeatable migration so it cannot clash with the next schema version.
  - Tests were switched to a dedicated profile after the demo data would have broken the repository tests.
  - A test caught that reading the session cookie inside Spring's bearer resolver disabled CSRF protection; the cookie is now converted after the CSRF check.
  - Lint limits rejected the first versions of several classes; they were split instead of relaxing the rules.

## 4. Sprint retrospective input

- Small, logged commits made the history usable for this report; keep writing a feature-log entry with every change.
- Only two of four members have repository work; the work split (CONTRIBUTING section 1) needs to be decided so everyone has a `develop/<name>` branch with tasks.
- PR #7 was merged without a review because the branch ruleset is not active; activating `main-protection.json` enforces review and squash merge.
- Integration tests run only in CI for members without Docker; installing Docker locally shortens the feedback loop.
