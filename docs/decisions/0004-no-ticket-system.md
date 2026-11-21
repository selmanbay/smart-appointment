# ADR-0004: No mandatory ticket system

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** general

## Context

Time is short and the team is small. Opening a Jira card for every change adds nothing to the actual workflow.

## Decision

- Branch name `<type>/<short-name>`; commit and PR title `<type>(<area>): <summary>`; no ticket numbers.
- Tracking: the Tuesday meeting, `docs/STATUS.md` and the feature logs. PR descriptions name the SRS function or use case.

## Consequences

- The traceability reports need (use case → code) comes from PR descriptions and feature logs.
- Charts such as a sprint burndown are not generated automatically; if needed, they can be derived from log dates.
