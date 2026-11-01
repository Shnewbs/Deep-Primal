#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
javac --release 21 -Xlint:all -Werror -d "$tmp" \
  "$root/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterDynamics.java" \
  "$root/src/coreChecks/java/com/shnewbs/deepprimal/core/WaterDynamicsPresentationFiniteChecks.java" \
  "$root/src/coreChecks/java/com/shnewbs/deepprimal/core/WaterDynamicsSurfaceAdmissionChecks.java"
java -ea -Xverify:all -cp "$tmp" com.shnewbs.deepprimal.core.WaterDynamicsPresentationFiniteChecks
java -ea -Xverify:all -cp "$tmp" com.shnewbs.deepprimal.core.WaterDynamicsSurfaceAdmissionChecks
