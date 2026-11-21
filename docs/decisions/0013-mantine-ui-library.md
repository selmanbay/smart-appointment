# ADR-0013: Mantine as the only UI library

- **Status:** Accepted
- **Date:** 2026-11-22
- **Feature:** platform

## Context

Four people and their agents will add screens. Hand-written CSS from many authors drifts quickly and tends towards generic generated-UI styling (the prototype already showed this). A mature component library with one theme keeps every screen consistent.

## Decision

- The web app uses Mantine (`core`, `hooks`, `dates`, `form`, `notifications`) with Tabler icons. No other UI or CSS framework.
- One theme file, `web/src/app/theme.ts`, holds colours, font, radius and type scale (values in `docs/design/UI_GUIDELINES.md`).
- Feature code uses Mantine components and theme props only: no `.css` files under `src/features/` (ESLint rule), no raw colours, no inline styles.
- Shared compositions (status chip, staff option card) live in `src/components/`.

## Consequences

- New screens look consistent by default; reviewers check for raw values and custom CSS.
- Changing the look later means editing the theme, not every screen.
- Bundle size is larger than hand-written CSS; acceptable for this project.

## Alternatives considered

- shadcn/ui + Tailwind: flexible, but its default look is the most common generated-UI style and every screen carries utility classes that drift.
- MUI: mature, but Material styling is harder to make neutral.
- Hand-written CSS with tokens: no guard against drift.
