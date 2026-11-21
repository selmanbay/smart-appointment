# ADR-0002: Project memory lives in the repo as Markdown

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** general

## Context

Everyone works with different agents (Claude, GPT, ...) on different machines. If what one agent learns, or what the team decides, does not reach the other agents, the same questions are asked again and conflicting code is written. The SQLite decision graph from the old working folder is a binary file that git cannot merge.

## Decision

- The repository is the single source. No separate database, shared chat or personal notes.
- Three kinds of files: `docs/STATUS.md` (current state), `docs/features/<feature>.md` (scope and log), `docs/decisions/` (ADRs). Rules in `docs/LOGGING.md`.
- Log and status updates are committed in the same PR as the work.
- `AGENTS.md` makes agents read these files before starting and update them before finishing.

## Consequences

- Everyone who runs `git pull`, and their agent, sees the same information with no extra setup.
- Forgetting to log is possible; the PR template checkbox and code review catch it.
- The decisions in the old `knowledge/` database were not migrated; most were settled in the SRS. Any still needed are written as ADRs.

## Alternatives considered

- SQLite decision graph: cannot be merged, needs custom tooling.
- Notion / Google Drive: agents cannot read it and it drifts from the code.
- One large `CONTEXT.md`: grows quickly and everyone conflicts in the same file.
