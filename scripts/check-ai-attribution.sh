#!/usr/bin/env bash
# Fails if the given text credits an AI tool (docs/guides/AGENT_RULES.md section 1).
# Usage: check-ai-attribution.sh <file>   or   ... | check-ai-attribution.sh
set -euo pipefail
export LC_ALL=C.UTF-8 2>/dev/null || export LC_ALL=en_US.UTF-8

input="${1:-/dev/stdin}"
text="$(grep -v '^#' "$input" || true)"   # ignore git's comment lines

tools='claude|anthropic|openai|chatgpt|gpt-?[0-9]|codex|copilot|cursor|gemini|windsurf|devin|aider'
patterns=(
  "^[[:space:]]*co-authored-by:.*(${tools}|[[:<:]]ai[[:>:]]|\\[bot\\]|bot@)"
  "(generated|created|written|made)[[:space:]]+(with|by|using)[[:space:]]+.*(${tools}|[[:<:]]ai[[:>:]])"
  "noreply@(anthropic|openai)\\.com"
  "claude\\.(ai|com)/(code|claude-code)"
)

found=0
for p in "${patterns[@]}"; do
  # BSD grep uses [[:<:]]; GNU grep uses \b. Translate for GNU.
  if grep --version 2>/dev/null | grep -q GNU; then p="${p//\[\[:<:\]\]/\\b}"; p="${p//\[\[:>:\]\]/\\b}"; fi
  if matches="$(printf '%s\n' "$text" | grep -Ein -- "$p")"; then
    printf '%s\n' "$matches"; found=1
  fi
done
if printf '%s' "$text" | grep -qF '🤖'; then echo "robot emoji"; found=1; fi

if [[ $found -eq 1 ]]; then
  echo "ERROR: AI attribution found. Remove it (docs/guides/AGENT_RULES.md section 1)." >&2
  exit 1
fi
