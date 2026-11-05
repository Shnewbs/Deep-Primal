#!/usr/bin/env bash
set -euo pipefail
repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
javac --release 21 -Xlint:all -Werror -d "$work" \
  "$repo/engine-core/src/main/java/com/shnewbs/deepprimal/core/VisualBudget.java" \
  "$repo/src/coreChecks/java/com/shnewbs/deepprimal/core/VisualBudgetOutsideChecks.java"
java -ea -Xverify:all -cp "$work" com.shnewbs.deepprimal.core.VisualBudgetOutsideChecks
