#!/usr/bin/env bash
# Fails if tracked text files contain Turkish characters (ADR-0006).
# Team member names are allowed.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

allowed_names='İdil Candan|Barış Alkan|Yusuf Aydın|Selman Bayburt|Zanyar Erkozan'
# Alternation instead of a bracket expression, so multi-byte symbols such as check marks do not match.
turkish='ç|ğ|ı|ö|ş|ü|Ç|Ğ|İ|Ö|Ş|Ü'

found=0
while IFS= read -r file; do
  [[ -f "$file" ]] || continue
  if matches="$(sed -E "s/${allowed_names}//g" "$file" | grep -nE "$turkish" || true)"; [[ -n "$matches" ]]; then
    printf '%s\n' "$matches" | sed "s|^|${file}:|"
    found=1
  fi
done < <(git ls-files | grep -vE '\.(pdf|png|jpe?g|gif|ico|svg|jar|docx|woff2?)$|/vendor/|^scripts/check-english\.sh$')

if [[ $found -eq 1 ]]; then
  echo "ERROR: Turkish text found. The repository language is English (ADR-0006)." >&2
  exit 1
fi
echo "OK: no Turkish text found."
