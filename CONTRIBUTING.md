# Contributing

These rules let four people, each working with AI agents, share one repository without conflicts and with the same tool versions. To change a rule, open a PR; the team decides in the Tuesday meeting.

## 1. Folder ownership

| Folder | Owner | Contents |
|---|---|---|
| `backend/` |  | Spring Boot REST API and its tests |
| `web/` |  | React + Vite + TypeScript and its tests |
| `android/` |  | Kotlin + Jetpack Compose and its tests |
| `api/` |  | `openapi.yaml`, the API contract |
| `.github/`, `scripts/`, `docker-compose.yml` |  | Docker, CI, repository rules |
| `docs/` | Everyone | SRS, design, status, logs, decisions |

The work split has not been decided yet. Fill in this table and `.github/CODEOWNERS` once it is.

## 2. Tracking work

- No ticket system (see ADR-0004). Who does what is agreed in the Tuesday meeting and the team chat.
- The current state lives in `docs/STATUS.md`; the history of each feature lives in `docs/features/<feature>.md`. Rules: `docs/LOGGING.md`.
- Each PR description names the SRS function or use case it touches (for example `F5, UC-3`).

## 3. Branches

- `main` is protected: no direct pushes, no force pushes, no deletion.
- Every piece of work gets a new branch from `main`:

```
<type>/<short-name>
```

| Type | Use for |
|---|---|
| `feat/` | new behaviour |
| `fix/` | bug fixes |
| `chore/` | tooling, dependencies, configuration |
| `docs/` | documentation only |
| `test/` | tests only |

Example: `feat/booking-capacity-check`. Lowercase, words separated by hyphens.

- Keep branches short-lived (a few days at most). Rebase a longer branch on `main` daily: `git pull --rebase origin main`.

## 4. Commit messages

[Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<area>): <imperative summary>
```

- `type`: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`
- `area`: `backend`, `web`, `android`, `api`, `infra`, `docs`
- Example: `feat(backend): lock staff row before counting bookings`
- Imperative mood, at most about 72 characters.

## 5. Pull requests

- The PR title uses the commit format: `feat(web): add booking screen`. A CI check enforces it.
- Fill in the template (`.github/pull_request_template.md`).
- **Merge requirements:** at least one approval, green CI, all review threads resolved, branch up to date with `main`.
- Merge method: **squash and merge**, so `main` gets one commit per PR.
- Keep PRs small, ideally under 400 changed lines.
- Review within 24 hours. Stuck PRs are discussed on Tuesday.
- Delete the branch after merging.

## 6. API first

- A new or changed endpoint goes into `api/openapi.yaml` **first**, in its own PR.
- Backend and frontend then work in parallel against that contract; the frontend uses mocks instead of waiting.
- Web and Android types are generated from this file, never written by hand.
- Breaking changes (removing a field, changing a type) need approval from the whole team.

## 7. Versions and dependencies

- No version comes from anyone's own machine. Versions are pinned only in repository files:
  - Backend: `backend/pom.xml` + Maven wrapper (`mvnw`)
  - Web: `web/package.json` + `web/package-lock.json` + `.nvmrc`
  - Android: `android/gradle/libs.versions.toml` + Gradle wrapper (`gradlew`)
  - Services: image tags in `docker-compose.yml`
- Never use a globally installed `mvn` or `gradle`; always `./mvnw` and `./gradlew`.
- On the web side use `npm ci`, not `npm install`. Always commit `package-lock.json`.
- Dependencies change only through Dependabot PRs or a dedicated `chore` PR. Feature PRs never bump versions.

## 8. Secrets

- Never commit `.env`, `google-services.json`, Firebase service accounts, passwords or tokens.
- Required variables are listed with fake values in `.env.example`. Copy it to create your own `.env`.
- If a secret is committed by mistake, tell the team immediately and revoke it. Reverting the commit is not enough.

## 9. Code rules (once code exists)

- Formatting is automatic and not up for debate: Spotless (Google Java Format) for the backend, Prettier + ESLint for the web, ktlint for Android.
- All code, identifiers, comments, commit messages and documentation are in **English**.
- Times are stored as UTC `timestamptz` and shown in the organization's time zone. The API uses ISO-8601.
- Appointment status changes only in the order defined in SRS 2.5: `BOOKED → CHECKED_IN → IN_SERVICE → COMPLETED`, or `BOOKED → CANCELLED / NO_SHOW`.
- The database schema changes only through Flyway migrations. A merged migration is never edited; add a new one.
- A failed notification never rolls back an appointment.
- Every query on organization data is scoped by `org_id` (SRS 2.5 integrity constraints).

## 10. Definition of done

A piece of work is done only when all of these are true:

- [ ] Merged into `main` with green CI
- [ ] The related use case step works
- [ ] New business logic has tests (especially F5, F6, F9)
- [ ] `api/openapi.yaml` is up to date if the API changed
- [ ] The feature log and, if the status changed, `docs/STATUS.md` are updated
- [ ] Runs on a clean machine with `docker compose up`

## 11. Meetings

- **Tuesday:** progress, stuck PRs, open decisions.
- **Every two weeks:** demo and retrospective. Notes go into the relevant feature logs or a decision record.
- **Supervisor meeting:** weekly. Decisions are recorded in `docs/decisions/`.

## 12. Working with AI agents

Everyone may work with Claude Code, Codex/GPT or similar agents. The shared rules are in `AGENTS.md`; Claude Code reads it through `CLAUDE.md`, Codex and Cursor read it directly.

- The person running the agent owns every commit it makes. Commits use that person's own git identity.
- **No AI attribution:** no `Co-Authored-By: Claude/GPT/...`, "Generated with ..." or robot emoji in commits, PR titles, PR descriptions or code (ADR-0003). This is enforced in three places:
  1. `.claude/settings.json` turns off Claude Code attribution (shipped with the repo).
  2. `.githooks/commit-msg` rejects such commits locally. Run `./scripts/setup.sh` once after cloning.
  3. The `No AI attribution` CI check scans every commit and the PR text; a failing PR cannot be merged.
- If you use another agent (Codex, Cursor, Copilot), turn off its co-author or signature option in that tool's own settings as well.
- Agents never push to `main`, never force push, never change versions and never read `.env` (`AGENTS.md` sections 2, 5, 6).
