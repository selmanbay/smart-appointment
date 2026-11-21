# UI guidelines

Rules for the web app (and later Android). The web app is built only from **Mantine** components themed by one theme file, so screens added by different people and agents look the same (ADR-0013). The static prototype in `prototype/` and the screenshots in `ui-screenshots/` show layout and content; this file replaces their visual style, which had several generic AI-generated patterns. Agents building screens follow this file (see `docs/guides/AGENT_RULES.md` section 11).

## 1. What changed from the prototype

| Prototype | Now | Why |
|---|---|---|
| Indigo / purple primary (`#515db2`), earlier teal, mixed | One accent: deep green `--accent` | A single deliberate accent; purple is the default look of generated UIs |
| Sparkle icon and "RECOMMENDED · SHORTEST WAIT" in caps | A plain line under the staff name: "Recommended: shortest estimated wait" | The SRS asks the screen to explain the recommendation, not decorate it |
| "~5 min" | "5 min" with the label "estimated wait" once per card | The label already says it is an estimate |
| "Lower workload", "Available to select" filler | Removed; workload shown as "6 of 14 slots booked" | Every line must carry a fact |
| Uppercase letter-spaced labels everywhere | Sentence case; uppercase only for status chips | Easier to read, less noise |
| "Confirm Appointment →" | "Confirm appointment" | Sentence case, no decorative arrows |
| Fake phone status bar (9:41, Wi-Fi) | Removed | The web app is not a phone mock-up |
| Metric strip with sample percentages | Only numbers the staff member acts on: appointments today, next appointment | No decorative statistics |
| Four font families, text down to 8 px | One family, minimum 12 px, `tabular-nums` for times | Readability (SRS U1–U3) |
| `layout-1` … `layout-41` one-off classes | Mantine components + one theme | Consistency, no magic values |

## 2. UI library and tokens

- Use **Mantine** (`@mantine/core`, `@mantine/dates`, `@mantine/form`, `@mantine/notifications`) and **Tabler icons** (`@tabler/icons-react`). No other UI or CSS framework (no MUI, Chakra, Ant, Tailwind, shadcn, styled-components).
- All visual values live in `web/src/app/theme.ts` (Mantine theme). Components use theme props (`c="dimmed"`, `gap="md"`, `variant="light"`), never raw colours, pixel sizes or inline `style`.
- No `.css` files inside `web/src/features/`; ESLint blocks them. If Mantine cannot express something, add it to the theme or to a shared component in `web/src/components/`.

Token values (as set in `theme.ts`):

| Token | Value | Use |
|---|---|---|
| `ink` | `#1c2426` | text |
| `dimmed` | `#5b6669` | secondary text |
| `line` | `#dfe4e3` | borders, dividers |
| `surface` | `#ffffff` | cards, inputs |
| `canvas` | `#f6f7f6` | page background |
| `primaryColor: accent` | `#1f6b52` (shade 7) | primary actions, selection, links |
| `accent.0` | `#e6f1ec` | selected backgrounds |
| `yellow` (Mantine) | shade 8 | warnings, "checked in" |
| `red` (Mantine) | shade 8 | no-show, cancel, errors |
| `defaultRadius` | `sm` (6px) | all corners |
| `spacing` | Mantine `xs`–`xl` | spacing scale |
| `fontFamily` | `"IBM Plex Sans", system-ui, sans-serif` | everything |
| `fontSizes` | `xs 12, sm 13, md 15, lg 18, xl 24` | type scale; nothing below 12px |

## 3. Components

- **Button:** primary (accent fill) for the one main action of a screen, secondary (outline) for the rest, danger text for destructive actions. Labels are verbs in sentence case: "Book", "Check in", "Mark no-show".
- **Status chip:** uppercase short label, colour by meaning only: booked (neutral), checked in (warning), in service (accent), completed (muted), cancelled / no-show (danger).
- **Staff option:** name, title, next free time, estimated wait, workload as "x of y slots booked". The recommended one shows one plain line saying why.
- **Slot grid:** times in 24-hour format with tabular numbers; unavailable slots are not shown instead of struck through.
- **Empty and error states:** one sentence saying what happened and what to do next ("No free slots on this day. Try another date.").

## 4. Copy

- Write for the user's task, in plain English, sentence case.
- No "~", no exclamation marks, no emoji, no marketing words ("seamless", "smart", "effortless").
- Errors say what went wrong and how to fix it, without apologising.
- Times as `10:00`, dates as `Tue 27 Oct`, durations as `30 min`.

## 5. Layout and spacing

Every screen is built from the same shared layout components in `web/src/components/layout/`, so spacing is identical across screens. Feature code does not set margins or paddings.

| Component | What it does | Spacing |
|---|---|---|
| `AppFrame` | Mantine `AppShell`: header with product name and role, left navigation per role | header 56px, navbar 240px, main padding `lg` |
| `Page` | title, optional subtitle and actions, then content | max width 1200px; title block → content `lg`; between sections `xl` |
| `Section` | a titled group inside a page | title → content `sm` |
| `Panel` | a bordered surface for a standalone or selectable group | padding `md`, radius `sm`, border `line` |
| `Columns` | two-column layout on desktop, one column below 900px | gap `lg` |

Inside those, use Mantine `Stack` / `Group` / `SimpleGrid` with only these gaps:

| Gap | Use |
|---|---|
| `xs` (8px) | text lines inside one item (name + detail) |
| `sm` (12px) | items in a list, form fields, chips |
| `md` (16px) | cards in a grid, blocks inside a panel |
| `lg` (24px) | columns, page title to content |
| `xl` (32px) | sections of a page |

- Navigation per role: Customer (Book appointment, My appointments), Staff (Daily agenda), Owner and Admin later.
- Use dividers and spacing to group content; a `Panel` border only where an item is selectable or stands alone.
- Every interactive element has a visible focus state and works with the keyboard.
