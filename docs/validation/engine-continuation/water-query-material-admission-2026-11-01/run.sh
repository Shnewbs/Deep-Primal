#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/stubs/com/shnewbs/deepprimal/core" "$TMP/classes"
cat > "$TMP/stubs/com/shnewbs/deepprimal/core/EngineMetrics.java" <<'STUB'
package com.shnewbs.deepprimal.core;
public final class EngineMetrics { public static void recordWaterSim(long nanos) {} }
STUB
cat > "$TMP/stubs/com/shnewbs/deepprimal/core/TerrainNoise.java" <<'STUB'
package com.shnewbs.deepprimal.core;
public final class TerrainNoise {
  public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
}
STUB
javac --release 21 -Xlint:all -Werror -d "$TMP/classes" \
  "$TMP/stubs/com/shnewbs/deepprimal/core/EngineMetrics.java" \
  "$TMP/stubs/com/shnewbs/deepprimal/core/TerrainNoise.java" \
  "$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java" \
  "$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterSimulation.java" \
  "$ROOT/src/coreChecks/java/com/shnewbs/deepprimal/core/WaterSimulationMaterialAdmissionChecks.java"
java -ea -Xverify:all -cp "$TMP/classes" \
  com.shnewbs.deepprimal.core.WaterSimulationMaterialAdmissionChecks
