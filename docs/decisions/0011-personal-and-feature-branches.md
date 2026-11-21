# ADR-0011: Personal and shared feature branches

- **Status:** Accepted
- **Date:** 2026-11-22
- **Feature:** general

## Context
ADR-0004 used one short-lived branch per task (`<type>/<short-name>`). With four people and their agents working in parallel, the team prefers fewer branches and a clear owner for each, while still allowing two people to work on one feature.

## Decision
- **Personal branch** `develop/<name>` per team member (`develop/selman`, `develop/baris`, `develop/yusuf`, `develop/zanyar`). Only its owner commits there.
- **Shared feature branch** `feature/<short-name>` only when two or more people build the same feature at the same time; deleted after merge.
- All changes reach `main` through PRs with squash merge (CONTRIBUTING section 5). PRs stay small and are merged at least every few days.
- Personal branches are rebased on `main` daily and reset to `main` after their PR is merged.
- This replaces the branch naming part of ADR-0004; the rest of ADR-0004 stands.

## Consequences
- Each person's work is isolated; nobody overwrites another person's branch.
- Long-lived personal branches drift if PRs are not merged often; the daily rebase and small PRs are what keep conflicts small.
