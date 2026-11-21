# ADR-0003: No AI attribution in commits and PRs

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** platform

## Context

The team works with AI agents. Some tools add a `Co-Authored-By` trailer to commits or a "Generated with ..." note to PRs. The team does not want these in the repository; the person who makes a commit owns it.

## Decision

- No attribution to AI tools in commits, PRs or code (rule: `AGENTS.md` section 1).
- Three layers of enforcement: `.claude/settings.json` (Claude Code attribution off), `.githooks/commit-msg` (local), and the `No AI attribution` CI check (required by the branch ruleset).

## Consequences

- The local hook must be enabled once per clone with `./scripts/setup.sh`; if it is not, CI still catches violations.
- Other tools (Codex, Cursor, Copilot) need their signature option turned off in each person's own settings.

## Alternatives considered

- A written rule only: agents fall back to their defaults, so it would not hold.
