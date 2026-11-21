# Agent instructions

This file is the entry point for AI coding agents (Codex, Cursor, Copilot, Gemini, ...). Claude Code reads `.claude/CLAUDE.md`, which points to the same files.

Before every task, read in this order:

1. [`docs/PROJECT_MAP.md`](docs/PROJECT_MAP.md): structure, where to work for each task, test and check commands
2. [`docs/guides/AGENT_RULES.md`](docs/guides/AGENT_RULES.md): the rules you must follow
3. [`docs/STATUS.md`](docs/STATUS.md) and the feature file in [`docs/features/`](docs/features/README.md)

Never add AI attribution to commits or PRs, write everything in English, and run `make verify` before saying a task is done.
