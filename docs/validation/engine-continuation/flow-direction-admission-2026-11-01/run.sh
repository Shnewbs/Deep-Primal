#!/usr/bin/env bash
set -euo pipefail
repo="$(cd "$(dirname "$0")/../../../.." && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
javac --release 21 -Xlint:all,-cast -Werror -d "$tmp" \
  "$repo/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java" \
  "$repo/src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowDirectionAdmissionChecks.java"
java -ea -Xverify:all -cp "$tmp" com.shnewbs.deepprimal.core.WaterFlowDirectionAdmissionChecks
