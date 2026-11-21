#!/usr/bin/env bash
# Run once after cloning: ./scripts/setup.sh
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
git config core.hooksPath .githooks
[[ -f .env ]] || cp .env.example .env
echo "Git hooks enabled (.githooks). .env ready."
