# Keeping logs in the repository

The project's memory lives in Markdown files in this repository. Whoever opens it, human or agent, should be able to `git pull`, read a few files and know where the project stands, what was done and why, and what comes next. Chat histories, messaging apps and personal notes get lost; the repository stays.

## 1. Which file is for what

| File | What goes in | When to update |
|---|---|---|
| `docs/PROJECT_MAP.md` | Structure map, task routing, test and check commands | When folders, features, commands or checks change |
| `docs/STATUS.md` | One-page snapshot: status of each feature, next steps, blockers | When a feature's status changes |
| `docs/features/<feature>.md` | Scope, status, open questions and **log** of one feature | In every PR that touches that feature |
| `docs/decisions/NNNN-*.md` | A lasting technical or product decision and its reasoning (ADR) | When a hard-to-reverse decision is made |
| `AGENTS.md` | Stable working rules | Rarely, by team decision |

In short: **log** = what happened, **decision** = what we chose and why, **status** = where we are now.

## 2. Feature logs

Each feature has one file: `docs/features/booking.md`, `docs/features/estimation.md`, and so on. The list is in `docs/features/README.md`.

Every feature file has the same structure:

```markdown
# <Feature name>

## Scope
SRS functions, use cases, code folders.

## Status
One line: Not started | In progress | Testing | Done | Blocked: <reason>

## Open questions
- [ ] An unanswered question (who will answer it)

## Log
(newest first)
```

### Log entry format

```markdown
### 2026-11-24 · selman · backend
- **Done:** Slot generation follows Eq. 1; break periods are excluded.
- **Why:** UC-3 step 4 needs the list of free slots.
- **State / next:** No capacity lock yet; next is the F5 concurrency test.
- **Refs:** PR #12, ADR-0001, UC-3
```

Rules:

- **Newest entry first**, directly under the `## Log` heading. Old entries are never deleted or edited; correct a wrong entry with a new one.
- Heading line: `### YYYY-MM-DD · <person> · <area>`. The person is the human who did the work or ran the agent (never the agent's name). Area: `backend`, `web`, `android`, `api`, `infra`, `docs`.
- **Done** is required; the other lines only when useful. Two to six lines in total; long explanations belong in the PR or an ADR.
- Write the **outcome**, not the code: not "added a method to BookingService" but "Booking now checks capacity under a row lock".
- If a PR touches several features, each feature file gets its own short entry.
- The log entry is committed **inside** the PR that does the work, never in a separate "update logs" PR, so code and record reach `main` together.
- Record things that failed, are half done or were reverted too. "Tried X, it did not work because Y" saves the next person hours.

### When to write an entry

Write one when:
- Behaviour was added, changed or removed
- A bug was found or fixed
- An approach was tried and dropped
- A question was answered or a new one came up
- Something is blocked (another feature, missing information, a supervisor decision)

Skip it for:
- Formatting, typos, comment-only changes
- Dependabot version bumps (the Dependabot PR is the record)

## 3. STATUS.md

One page, the first file everyone reads. The **Status** column in its feature table must match the `## Status` line of the feature file. A PR that changes a feature's status updates both.

Keep `## Next` and `## Blockers` short (five to seven items at most). Remove finished items; their history is already in the feature log.

## 4. Decisions (ADR)

Open a new file in `docs/decisions/` when:

- A choice affects the architecture, the data model or the API contract
- A rule the SRS left open is clarified
- A library or external service is chosen
- Anyone will later ask "why did we do it this way?"

Format and numbering: `docs/decisions/README.md`. An accepted ADR is never edited; a changed decision gets a new ADR and the old one is marked `Superseded by ADR-NNNN`.

## 5. For agents

Agents both read and write these files (`AGENTS.md` section 10):

1. **Before starting:** read `docs/STATUS.md`, the relevant `docs/features/<feature>.md` and the ADRs it mentions.
2. **When finished, in the same commit or PR:** add a log entry; if the status changed, update `## Status` and `STATUS.md`; if there is a new decision, draft an ADR as `Proposed` and have a human approve it.
3. Only verified results go into the log. If something was not tested, say "not tested".

## 6. Merge conflicts

Logs are Markdown, so conflicts are easy to resolve. If two PRs added entries to the same feature at the same time, keep both and order them by date. For a `STATUS.md` conflict, take the version on `main` and reapply your own change on top.

## 7. Language

All logs, status notes and ADRs are written in English.
