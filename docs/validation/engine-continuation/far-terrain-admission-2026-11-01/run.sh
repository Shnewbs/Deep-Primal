#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../../.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac --release 21 -Xlint:all -Werror -d "$TMP" \
  "$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/FarTerrain.java" \
  "$ROOT/src/coreChecks/java/com/shnewbs/deepprimal/core/FarTerrainAdmissionChecks.java"
java -ea -Xverify:all -cp "$TMP" com.shnewbs.deepprimal.core.FarTerrainAdmissionChecks
