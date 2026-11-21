# main branch ruleset

`main-protection.json` applies the rules from CONTRIBUTING.md sections 3 and 5 on GitHub:

- `main` cannot be deleted or force-pushed, and history stays linear
- Changes land only through PRs: at least one approval, resolved threads, squash merge only
- The `ci-ok` (CI), `check` (PR title), `no-ai-attribution` and `english-only` checks must pass, and the branch must be up to date with `main`

## How to apply

On GitHub: repository → **Settings → Rules → Rulesets → New ruleset → Import a ruleset** → choose this file → **Create**.

Note: rulesets on a private repository need GitHub Pro or Team. Students can get Pro for free through GitHub Education, or the repository can be public.
