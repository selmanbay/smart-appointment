# Agent rules

Rules for every AI coding agent working in this repo (Claude Code, Codex / GPT, Cursor, Copilot, Gemini, ...).
Humans follow `docs/guides/CONTRIBUTING.md`; agents follow this file **and** `docs/guides/CONTRIBUTING.md`. Agents arrive here from the root `AGENTS.md` or `.claude/CLAUDE.md`. If they conflict, this file wins for agents.

The human who runs the agent is the author and is responsible for everything it commits.

**Start every task with [`docs/PROJECT_MAP.md`](../PROJECT_MAP.md).** It maps the folders, routes each kind of task to the files to change and read, and lists the test and check commands. Then follow section 10 below.

## 1. No AI attribution (hard rule)

Commits, PR titles, PR bodies, code comments and docs must not credit or mention an AI tool. Never add:

- `Co-Authored-By:` trailers naming an AI, a model or a bot (Claude, Anthropic, GPT, OpenAI, Codex, Copilot, Cursor, Gemini, ...)
- "Generated with ...", "Created by ...", "Written by AI", robot emoji, or links to AI tools
- Any `noreply@anthropic.com`, `noreply@openai.com` or similar address

Commits use the human's own git identity (`git config user.name` / `user.email`). Do not change git config.

This is enforced in three places: agent settings (`.claude/settings.json`), the local `commit-msg` hook (`.githooks/`), and the `no-ai-attribution` CI check. A PR that fails the check cannot be merged.

## 2. Git

- Never commit or push to `main`. Never force push. Never delete branches you did not create.
- One task = one branch: `<type>/<short-name>` (e.g. `feat/booking-capacity-check`).
- Commit message: `<type>(<area>): <imperative summary>`, English, ≤ 72 chars.
  - type: `feat | fix | chore | docs | test | refactor`
  - area: `backend | web | android | api | infra | docs`
- Push and open PRs only when the human asks. Fill in `.github/pull_request_template.md`.
- Do not rewrite history that is already pushed.

## 3. Scope

- Work only on the task you were given. Do not refactor, rename or reformat unrelated code.
- Stay in the folder of the task (`backend/`, `web/`, `android/`). Touching another owner's folder needs the human to agree first (see `.github/CODEOWNERS`).
- If the task is unclear or conflicts with the SRS, stop and ask. Do not guess business rules.

## 4. Source of truth

- Requirements: `docs/srs/SRS-v1-2026-10-27.pdf`. Use its IDs: F1–F11, UC-1–UC-20, Eq. 1–5, P1–P3.
- API: `api/openapi.yaml`. Endpoint changes go into this file first, in a separate PR. Do not hand-write API types in web or Android; generate them.
- Data model: SRS section 2.5 (Table 9 and the integrity constraints).
- Structure: one package / folder per feature (ADR-0001). The feature map is `docs/features/README.md`.
- Project memory: `docs/STATUS.md`, `docs/features/`, `docs/decisions/` (ADR-0002).

## 5. Versions and dependencies

- Do not add, remove or upgrade dependencies, Docker images, the JDK, Node or Gradle unless the task is about that. Upgrades come via Dependabot PRs.
- Use the wrappers and lockfiles: `./mvnw`, `./gradlew`, `npm ci`. Never global `mvn` / `gradle` / `npm install` that rewrites the lockfile.
- Do not edit an already merged Flyway migration. Add a new one.

## 6. Secrets

- Never read, print, create or commit `.env`, `google-services.json`, keys, tokens or passwords. Use `.env.example` with fake values.
- Never put real personal data in seeds, fixtures or tests.

## 7. Done means verified

Before saying a task is done, run what applies and report the real result (including failures):

| Area | Command |
|---|---|
| everything (as in CI) | `make verify` |
| backend | `make backend-lint backend-verify` |
| web | `make web-lint web-test` and `cd web && npm run build` |
| android | `cd android && ./gradlew ktlintCheck lint test assembleDebug` |
| whole stack | `make up` |

Run `make format` before committing. CI runs the same checks (`docs/PROJECT_MAP.md` section 6); a red CI is not done.

Business logic in F5 (booking/capacity), F6 (cancel/reschedule) and F9 (waiting time, Eq. 3–4) needs unit tests. Concurrency in F5 needs an integration test with parallel requests.

## 8. Domain rules agents often get wrong

- Times are stored as UTC `timestamptz`; slots are generated in the organization's time zone.
- Appointment status order: `BOOKED → CHECKED_IN → IN_SERVICE → COMPLETED`; from `BOOKED` also `CANCELLED` or `NO_SHOW`. No other transitions.
- Staff capacity must never be exceeded, even under concurrent bookings (lock, count, insert in one transaction).
- Reschedule is atomic: the old slot is released only if the new one is taken.
- Waiting estimates and recommendations are computed on request, not stored.
- Every organization-scoped query is filtered by `org_id`.
- Appointments are never deleted; inactive things are flagged `is_active = false`.
- A failed notification never rolls back the appointment.

## 9. Language

Everything in this repository is in English: code, identifiers, comments, commit messages, PR text, Markdown files and UI strings. Do not write Turkish in the repo, even if the human talks to you in Turkish.

## 10. Shared context: read first, log last

Every agent works on a different machine. The only shared memory is this repository, so keep it current. Full rules: `docs/guides/LOGGING.md`.

**Before starting a task, read:**

1. `docs/PROJECT_MAP.md` (where to work, how to verify)
2. `docs/STATUS.md`
3. The feature file for the task: `docs/features/<feature>.md` (scope, status, open questions, log)
4. Every ADR that file or the task mentions (`docs/decisions/`)

**Before finishing, in the same branch / PR:**

1. Add an entry at the top of the feature's `## Log` section (format in `docs/guides/LOGGING.md` section 2). Write only verified results; if something is untested, say so.
2. If the feature status changed, update its `## Status` line and the matching row in `docs/STATUS.md`.
3. Tick or add questions under `## Open questions`.
4. If you added or moved a folder, feature, command or check, update `docs/PROJECT_MAP.md`.
5. If you made a decision that is hard to reverse (architecture, data model, API shape, library choice, an SRS rule clarification), write an ADR with status `Proposed` and tell the human. Never mark an ADR `Accepted` yourself.

Do not edit or delete earlier log entries or accepted ADRs.

## 11. Clean code, no slop

Linters enforce the limits below (ADR-0007). Do not disable a lint rule, add `eslint-disable`, `@SuppressWarnings` or `// NOSONAR`, or raise a limit to make code pass. Fix the code instead. If a rule is genuinely wrong for a case, ask the human.

**Size limits** (lint fails above these):

| | Backend (Java) | Web (TypeScript) |
|---|---|---|
| Function / method length | 40 lines | 50 lines |
| Parameters | 4 | 4 (use an object) |
| Cyclomatic complexity | 10 | 10 |
| Nesting depth | 3 | 3 |
| File length | 300 lines | 300 lines |

**Structure:**

- Respect feature boundaries (ADR-0001). Web features never import from another feature; shared code goes to `components/` or `api/`. Backend features talk to each other only through services.
- Controllers only map HTTP to service calls. Business rules live in services; calculations live in `estimation/`.
- Before adding a helper, search for an existing one. Do not create a second date utility, HTTP client, error type or formatter.
- No new abstraction (interface, base class, factory, generic helper) until there are two real uses.

**Code hygiene:**

- No dead code, unused variables, unused imports or commented-out code.
- No `TODO` without a reason and a name: `// TODO(selman): ...`. Prefer an open question in the feature log.
- No `any` in TypeScript, no non-null assertion `!`, no `console.log`, no `System.out.println`. Use the logger.
- No empty `catch`, no catching `Exception` / `Throwable` / `RuntimeException` to hide errors. Handle errors in `common/` exception handling.
- No magic numbers or strings for business rules; name them or read them from the organization's rules.
- Names say what something is in domain terms from the SRS (`appointment`, `staff`, `slot`, `learnedDuration`), not `data`, `item`, `info`, `helper`, `manager`, `util`, `tmp`.

**Comments:**

- Comment *why*, never *what*. Do not restate the code.
- No banner comments, no section dividers, no emoji, no "This function does X" on obvious code.
- Reference the SRS where it explains a rule: `// SRS Eq. 4: projected start is the later of scheduled start and previous finish`.

**Changes:**

- Smallest change that solves the task. Do not reformat, rename or "improve" code outside the task.
- Run the linter and formatter before finishing (`./mvnw spotless:apply checkstyle:check`, `npm run lint -- --fix && npm run format`).

## 12. Naming

Enforced by Checkstyle (backend) and `@typescript-eslint/naming-convention` (web).

| Kind | Java | TypeScript | Example |
|---|---|---|---|
| Class / type / interface / enum | `PascalCase` noun | `PascalCase` noun | `Appointment`, `SlotGenerator`, `BookingRequest` |
| Method / function | `camelCase` verb | `camelCase` verb | `bookAppointment`, `estimateWaitingTime` |
| Variable / parameter / field | `camelCase` noun | `camelCase` noun | `scheduledStart`, `learnedDuration` |
| Boolean | `is` / `has` / `can` / `should` prefix | same | `isActive`, `hasCapacity`, `canReschedule` |
| Constant | `UPPER_SNAKE_CASE` | `UPPER_SNAKE_CASE` | `MAX_ACTIVE_PER_USER` |
| React component / hook | n/a | `PascalCase` / `useCamelCase` | `StaffCard`, `useAvailableSlots` |
| Package / folder | lowercase, singular feature name | kebab-case | `booking`, `my-appointments` |
| Database table / column | n/a | n/a | `appointment`, `service_start_at` (SRS Table 9) |

- Use the SRS vocabulary: `organization`, `staff`, `service`, `appointment`, `slot`, `workingHours`, `appointmentRule`, `scheduleChangeRequest`, `feedback`, `notification`. Do not invent synonyms (`booking` for the entity, `employee`, `worker`, `client`, `order`).
- Names are full words. No abbreviations except well-known ones (`id`, `url`, `dto`, `api`, `jwt`); no single letters except loop indices `i`, `j`.
- Acronyms are written as words (`JwtService`, not `JWTService`). Do not shorten domain words (`organizationId`, not `orgId`), even where the database column is shorter (`org_id`).
- Banned vague names: `data`, `info`, `item`, `obj`, `temp`, `tmp`, `result2`, `foo`, `stuff`, `thing`, and class suffixes `Helper`, `Manager`, `Util(s)`, `Processor`, `Handler` unless it really handles an event.
- Units go in the name when not obvious: `durationMinutes`, `leadHours`, `timeoutMs`.
- Collections are plural: `appointments`, `staffMembers`. Maps say both sides: `waitByStaffId`.

## 13. SOLID and design rules

These apply to every class and module. Linters catch some of it (size, coupling, boundaries); reviewers catch the rest.

- **Single responsibility:** a class or component has one reason to change. A service does business rules for one feature; a controller maps HTTP; a repository does persistence; a React component renders one thing. If you need "and" to describe it, split it. Limits in section 11 and Checkstyle `ClassFanOutComplexity` / `ClassDataAbstractionCoupling` enforce this.
- **Open/closed:** add behaviour by adding code, not by growing `if/else` or `switch` chains on a type. When a rule varies per case (notification channel, appointment status transition), use a small strategy or a map of handlers.
- **Liskov substitution:** an implementation must honour its interface's contract: no throwing `UnsupportedOperationException`, no ignoring arguments, no stronger preconditions.
- **Interface segregation:** small interfaces with methods every client uses. No "god" service interfaces; a client should not depend on methods it does not call.
- **Dependency inversion:** depend on abstractions at module edges. Inject dependencies through constructors (no field injection, no `new` for services, no static service access). Business logic never depends on web, mail or database classes directly; `estimation/` depends on nothing but plain Java.

Additional rules:

- Constructor injection only, with `final` fields (Java) or explicit props / hooks (React).
- Entities are not returned from controllers; map them to DTOs in the feature's `dto/` package.
- Prefer immutable values: Java `record` for DTOs and value objects, `readonly` and `const` in TypeScript.
- Fail fast: validate input at the boundary (Bean Validation in controllers, schema validation in forms) and throw domain exceptions from services.
- No hidden side effects: a method called `get...`, `find...`, `calculate...` or `is...` never writes.
- Composition over inheritance. No base classes for sharing a few lines of code.
- No circular dependencies between features or packages (backend: ArchUnit test, web: `boundaries` lint rule).

## 14. Hosting-ready code

The app runs locally now but must be deployable later by configuration only (ADR-0008).

- Never hard-code URLs, hosts, ports, secrets, limits or time-outs. Add a field to the matching `@ConfigurationProperties` record in `common.config`, give it a local default, and add the variable to `.env.example`.
- Only `common.config` reads environment variables.
- Keep the backend stateless: no HTTP sessions, no in-memory caches that must be shared, no files on local disk.
- Cross-cutting behaviour (rate limiting, CORS, error format, correlation id, logging, metrics) lives in `common` behind an interface. Features do not implement their own versions.
- Every new endpoint: under `/api/v1`, belongs to a rate-limit policy group (`auth`, `booking` or `default`), returns RFC 7807 errors, and paginates if it returns a list.
- External services (mail, push) are called through an interface (`NotificationSender`) so they can be replaced or mocked.
- Scheduled jobs must be idempotent: running twice must not send two reminders.
- Put each test in the right layer (unit, slice, integration `*IT`, architecture) as listed in ADR-0008. Do not hit real external services in tests.
