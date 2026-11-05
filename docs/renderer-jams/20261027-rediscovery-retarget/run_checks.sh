#!/usr/bin/env bash
# Candidate-only JVM checks. No native/backend/build/artifact qualification implied.
set -euo pipefail
jam="$(cd "$(dirname "$0")" && pwd)"
root="$(cd "$jam/../../.." && pwd)"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/classes" "$work/negative/com/shnewbs/deepprimal/core/lod" "$work/negative-classes"
base="$root/engine-core/src/main/java/com/shnewbs/deepprimal/core/DistancePolicy.java"
scan="$jam/candidate/DeliveredChunkScan.java"
checks="$jam/checks/DeliveredRetargetChecks.java"
old="$root/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScanChecks.java"
javac --release 21 -d "$work/classes" "$base" "$scan" "$checks" "$old"
java -ea -Xverify:all -cp "$work/classes" com.shnewbs.deepprimal.core.lod.DeliveredChunkScanChecks
java -ea -Xverify:all -cp "$work/classes" com.shnewbs.deepprimal.core.lod.DeliveredRetargetChecks
sed '/    if (done()) return true;/d' "$scan" > "$work/negative/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java"
javac --release 21 -d "$work/negative-classes" "$base" "$work/negative/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java" "$checks"
if java -ea -Xverify:all -cp "$work/negative-classes" com.shnewbs.deepprimal.core.lod.DeliveredRetargetChecks > "$work/negative.log" 2>&1; then
  echo "NEGATIVE_CONTROL_WRONGLY_PASSED" >&2
  exit 1
fi
grep -F 'completed move +1' "$work/negative.log" >/dev/null
printf '%s\n' 'NEGATIVE_CONTROL_EXPECTED_REJECTION'
git -C "$root" apply --check "$jam/candidate/VoxelLodRenderer.patch"
printf '%s\n' 'RENDERER_RETARGET_PATCH_APPLIES'
