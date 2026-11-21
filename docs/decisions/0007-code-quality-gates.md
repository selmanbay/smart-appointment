# ADR-0007: Code quality gates

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** platform

## Context
Most code will be written with AI agents by four people in a short time. Without automatic limits this quickly turns into long functions, vague names, duplicated helpers, cross-feature coupling and noise comments ("slop"). Written rules alone do not hold; agents follow what the tooling enforces.

## Decision
- Rules for humans and agents: `AGENTS.md` sections 11 (clean code), 12 (naming) and 13 (SOLID).
- **Backend:** Spotless (google-java-format) for formatting, Checkstyle (`backend/config/checkstyle.xml`) for naming, size, complexity, coupling and hygiene, ArchUnit for feature boundaries and layering (`backend/config/README.md`).
- **Web:** ESLint (`web/eslint.config.js`) with typescript-eslint strict type-checked rules, sonarjs, react-hooks, `boundaries` for feature isolation, naming conventions and size limits; Prettier for formatting.
- **Repository:** `english-only`, `no-ai-attribution` and PR title checks.
- All of these run in CI and are required by the `main` ruleset.
- Limits: functions 40 lines (Java) / 50 lines (TS), 4 parameters, cyclomatic complexity 10, nesting depth 3, files 300 lines.
- Suppressing a rule inline is not allowed: ESLint `noInlineConfig`, Checkstyle bans `@SuppressWarnings`. Changing a limit needs a new ADR.

## Consequences
- Agents get immediate, specific feedback and must split code instead of growing it.
- Some legitimate code will need restructuring to fit the limits; that is intended.
- Tests have relaxed length and literal-value rules.
- Both configs were verified on sample files: ESLint flagged 19 problems in a bad sample and none in a clean one; Checkstyle flagged 16 and none.

## Alternatives considered
- SonarQube / SonarCloud: stronger reports, but needs a server or account; can be added when the project is hosted.
- Only formatters, no design rules: does not prevent spaghetti code.
