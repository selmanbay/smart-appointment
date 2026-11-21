# ADR-0005: First goal is a web MVP that runs locally

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** general

## Context

The static HTML prototype (`docs/design/prototype/`) shows five screens with fake data. The goal is to turn it into an MVP connected to a real backend. Cloud deployment is not required.

## Decision

- MVP = PostgreSQL + Spring Boot backend + React web, running locally with `docker compose up`.
- The prototype is a design reference only; its code is not carried over and the screens are rebuilt in React.
- Android, FCM and analytics come after the MVP.

## Consequences

- No deployment, domain or HTTPS work.
- Demos run on a developer's machine with seed data.
