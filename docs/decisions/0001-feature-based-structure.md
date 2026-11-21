# ADR-0001: Feature-based folder structure

- **Status:** Accepted
- **Date:** 2026-11-21
- **Feature:** general

## Context
Four people, each working with AI agents, will work in the same repository on a short schedule. In a layer-based layout (`controllers/`, `services/`, `repositories/`) everyone touches the same folders for every feature, conflicts multiply and an agent cannot see a feature as a whole. The SRS already splits the system into functions F1–F11.

## Decision
- **Backend:** package = feature. Under `com.smartappointment.<feature>` live that feature's controller, service, repository, entities and `dto/`. Shared concerns (error handling, security configuration, base types) live in `common/`.
- Features: `auth`, `organization`, `catalog`, `schedule`, `booking`, `estimation`, `notification`, `analytics`, `feedback` (mapping in `docs/features/README.md`).
- A feature never accesses another feature's repository directly; it calls that feature's service.
- `estimation` holds plain calculation classes (Eq. 3, Eq. 4) with no dependency on Spring or the database, verified by unit tests.
- **Web:** `web/src/features/<screen-group>/` holds the components, hooks and API calls of those screens. Shared UI goes in `components/`, the generated API client in `api/`, routing and layout in `app/`.

## Consequences
- Two people's agents usually work in different folders, so conflicts drop.
- The architecture section of the SDD maps one-to-one to these packages.
- Calls between features go through services, so the dependency direction must be watched (no cycles).

## Alternatives considered
- Layer-based layout: simple, but conflict-prone and feature boundaries are invisible.
- Hexagonal / clean architecture: too much abstraction for this schedule and team size.
