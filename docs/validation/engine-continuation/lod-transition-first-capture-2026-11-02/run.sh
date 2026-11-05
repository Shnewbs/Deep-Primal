#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
if [[ -f "$ROOT/src/com/shnewbs/deepprimal/core/TransitionDependencies.java" ]]; then
  # Standalone evidence archive.
  SOURCE="$ROOT/src/com/shnewbs/deepprimal/core/TransitionDependencies.java"
  TEST="$ROOT/src/com/shnewbs/deepprimal/core/TransitionDependenciesLifecycleChecks.java"
else
  # Script checked into docs/validation/engine-continuation/<case>/ in a full checkout.
  REPO="$(cd "$ROOT/../../../.." && pwd)"
  SOURCE="$REPO/engine-core/src/main/java/com/shnewbs/deepprimal/core/TransitionDependencies.java"
  TEST="$REPO/src/coreChecks/java/com/shnewbs/deepprimal/core/TransitionDependenciesLifecycleChecks.java"
fi
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac --release 21 -Xlint:all -Werror -d "$TMP" "$SOURCE" "$TEST"
java -ea -Xverify:all -cp "$TMP" com.shnewbs.deepprimal.core.TransitionDependenciesLifecycleChecks
