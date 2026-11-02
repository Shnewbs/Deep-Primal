#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac --release 21 -Xlint:all -Werror -d "$TMP" \
  "$ROOT/src/com/shnewbs/deepprimal/core/TransitionDependencies.java" \
  "$ROOT/src/com/shnewbs/deepprimal/core/TransitionDependenciesLifecycleChecks.java"
java -ea -Xverify:all -cp "$TMP" com.shnewbs.deepprimal.core.TransitionDependenciesLifecycleChecks
