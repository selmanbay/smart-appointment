# web

React web app for customers, staff, organization owners and the system admin. No code yet.

## Planned stack

- Node 24 LTS (`.nvmrc`), npm (commit `package-lock.json`)
- Vite + React 19 + TypeScript
- React Router, TanStack Query
- openapi-typescript: types generated from `../api/openapi.yaml`
- react-big-calendar: owner team schedule with one column per staff member (Figure 5)
- ESLint + Prettier

## Folder layout (ADR-0001)

```
src/
  app/          routing, layout, auth guard
  api/          generated API client and types
  components/   shared UI
  features/
    auth/                 sign in, sign up
    booking/              book appointment, staff recommendation (Figure 2)
    my-appointments/      list, cancel, reschedule, feedback (Figure 3)
    staff-agenda/         staff daily agenda (Figure 4)
    team-schedule/        owner team schedule (Figure 5)
    owner-settings/       services, staff, working hours, rules
    admin-organizations/  system admin organizations (Figure 6)
    analytics/            workload and rates (after the MVP)
```

## Screens (SRS 1.4.1.2)

| Screen | Role | Use cases |
|---|---|---|
| Book appointment | Customer | UC-3, 18, 19 |
| My appointments | Customer | UC-4, 5, 6, 7 |
| Daily agenda | Staff | UC-8, 9 |
| Team schedule, services, staff, rules | Owner | UC-10 to 15 |
| Organizations | System admin | UC-16 |

Visual reference: `../docs/design/prototype/index.html` and `../docs/design/ui-screenshots/`.
