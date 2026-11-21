# Architecture decision records (ADR)

Every decision that is hard to reverse, or that someone will later ask "why did we do it this way?" about, gets one file here. When to write one: [`../guides/LOGGING.md`](../guides/LOGGING.md) section 4.

## Rules

- File name: `NNNN-short-title.md` (four digits, next free number).
- Status: `Proposed` → `Accepted` → if replaced, `Superseded by ADR-NNNN`.
- Agents write ADRs only as `Proposed`; a human marks them `Accepted`.
- An accepted ADR is never edited. If the decision changes, write a new ADR and update the old one's status line. Updating a file path or link after a move is allowed.
- Keep it under one page.

## Template

```markdown
# ADR-NNNN: <Title>

- **Status:** Proposed | Accepted | Superseded by ADR-NNNN
- **Date:** YYYY-MM-DD
- **Feature:** <docs/features/... or "general">

## Context
The problem, the constraints, what the SRS says.

## Decision
What we will do. One paragraph or a short list.

## Consequences
What we gain and lose, and what to watch out for from now on.

## Alternatives considered
- Alternative: why it was not chosen
```

## Index

| No | Title | Status |
|---|---|---|
| [0001](0001-feature-based-structure.md) | Feature-based folder structure | Accepted |
| [0002](0002-repo-as-shared-context.md) | Project memory lives in the repo as Markdown | Accepted |
| [0003](0003-no-ai-attribution.md) | No AI attribution in commits and PRs | Accepted |
| [0004](0004-no-ticket-system.md) | No mandatory ticket system (branch naming superseded by 0011) | Accepted |
| [0005](0005-local-web-mvp.md) | First goal: a web MVP that runs locally | Accepted |
| [0006](0006-english-only.md) | English is the project language | Accepted |
| [0007](0007-code-quality-gates.md) | Code quality gates | Accepted |
| [0008](0008-hosting-ready-by-configuration.md) | Hosting-ready by configuration | Accepted |
| [0009](0009-self-contained-apps.md) | Each app is self-contained, with its own tests | Accepted |
| [0010](0010-ci-pipeline.md) | Standard CI pipeline with local parity | Accepted |
| [0011](0011-personal-and-feature-branches.md) | Personal and shared feature branches | Accepted |
| [0012](0012-mvp-scope.md) | MVP scope follows the SDD critical scenarios | Accepted |
| [0013](0013-mantine-ui-library.md) | Mantine as the only UI library | Accepted |
| [0014](0014-swagger-from-contract.md) | Swagger UI serves the contract and the implementation | Accepted |
