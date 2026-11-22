# ADR-0017: Learned duration starts at the expected duration when there is no history

- **Status:** Proposed
- **Date:** 2026-11-22
- **Feature:** [docs/features/estimation.md](../features/estimation.md)

## Context

SRS Eq. 3 updates a service's learned duration from its previous value (`d_old`) after each
completed appointment: `d_new = alpha * d_actual + (1 - alpha) * d_old`. `Service.learnedDurationMin`
(`catalog.Service`) is nullable because no appointment has completed yet for a new service, so there
is no `d_old` to read. The handoff (B5, section 5) proposed starting from `expected_duration_min` in
that case and asked for an ADR either way.

## Decision

When `catalog.Service.learnedDurationMin` is `null`, `estimation.LearnedDuration.next(...)` uses
`expected_duration_min` as `d_old` for that first update. Callers pass the service's current learned
duration (possibly `null`) and its `expected_duration_min` on every call; the fallback lives in one
place (`LearnedDuration`) instead of being repeated at each call site.

## Consequences

- The first learned-duration update after a service is created, before any appointment has
  completed, is anchored to the value the owner configured, not an arbitrary number (zero, or the
  first observed duration unweighted).
- `estimation.WaitingTimeEstimator` (Eq. 4) still requires a duration per appointment; callers resolve
  the same null-to-expected fallback before building a `ScheduledService`, since the estimation
  package depends on nothing but plain Java and cannot read `catalog.Service` itself.

## Alternatives considered

- Start `d_old` at `0`: rejected, would pull the first few learned durations down and understate
  waiting-time estimates until enough appointments complete.
- Seed `learned_duration_min` to `expected_duration_min` at service creation instead of leaving it
  `null`: rejected for now, it would hide the "no history yet" fact from any future feature that
  needs to distinguish a configured estimate from an observed one (e.g. analytics, F10).
