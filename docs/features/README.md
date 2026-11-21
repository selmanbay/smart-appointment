# Feature logs

Each feature keeps its scope, status, open questions and log in its own file. Writing rules: [`../guides/LOGGING.md`](../guides/LOGGING.md).

| File | SRS | Backend package |
|---|---|---|
| [platform.md](platform.md) | — | `common/` |
| [auth.md](auth.md) | F1 | `auth/` |
| [organization.md](organization.md) | F2 (+ UC-13 branding) | `organization/` |
| [catalog.md](catalog.md) | F3 | `catalog/` |
| [schedule.md](schedule.md) | F4 | `schedule/` |
| [booking.md](booking.md) | F5, F6, F8 | `booking/` |
| [estimation.md](estimation.md) | F9 | `estimation/` |
| [notification.md](notification.md) | F7 | `notification/` |
| [analytics.md](analytics.md) | F10 | `analytics/` |
| [feedback.md](feedback.md) | F11 | `feedback/` |

To add a feature: create a file with the same structure, then add a row here and in `docs/STATUS.md`.
