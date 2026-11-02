#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../../.." && pwd)"
HERE="$ROOT/docs/validation/engine-continuation/pipeline-shutdown-race-2026-11-02"
OLD_FIXTURE="$ROOT/docs/validation/engine-continuation/pipeline-key-admission-2026-11-02/fixture"
SOURCE="$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java"
CHECKS="$ROOT/src/coreChecks/java/com/shnewbs/deepprimal/core"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/normal" "$TMP/race" "$TMP/old" "$TMP/race-src" "$TMP/old-src"
# Full exact production source, not a hand-reimplemented method.
EXPECTED_NEW=a2f7080346a1e24d594187f12b0105059b92231b
EXPECTED_OLD=2e1a5cb55007d10745260dd355fbf180d5f2920b
[[ "$(git hash-object "$SOURCE")" == "$EXPECTED_NEW" ]] || { echo 'Source changed; review and update the pinned regression.' >&2; exit 2; }
javac --release 21 -Xlint:all -Werror -d "$TMP/normal" "$SOURCE" \
  "$OLD_FIXTURE/ChunkCache.java" "$OLD_FIXTURE/ChunkMaterializer.java" \
  "$CHECKS/WorldGenPipelineKeyChecks.java" "$CHECKS/WorldGenPipelineShutdownChecks.java"
java -ea -Xverify:all -cp "$TMP/normal" com.shnewbs.deepprimal.core.WorldGenPipelineKeyChecks
java -ea -Xverify:all -cp "$TMP/normal" com.shnewbs.deepprimal.core.WorldGenPipelineShutdownChecks
cp "$SOURCE" "$TMP/race-src/WorldGenPipeline.java"
git -C "$ROOT" cat-file blob "$EXPECTED_OLD" > "$TMP/old-src/WorldGenPipeline.java"
[[ "$(git hash-object "$TMP/old-src/WorldGenPipeline.java")" == "$EXPECTED_OLD" ]] || exit 2
# Inject a test-only pause after cache admission but before the completion decision.
# No RaceGate or hook is linked into the real production class/JAR.
python3 - "$TMP/race-src/WorldGenPipeline.java" "$TMP/old-src/WorldGenPipeline.java" <<'PY'
from pathlib import Path
import sys
needle = '      // User continuations may call back into EngineCore: never run them under this lock.\\n'
for path in sys.argv[1:]:
    s=Path(path).read_text()
    assert s.count(needle)==1
    Path(path).write_text(s.replace(needle,needle+'      RaceGate.pause();\\n',1))
PY
for kind in race old; do
  javac --release 21 -Xlint:all -Werror -d "$TMP/$kind" "$TMP/$kind-src/WorldGenPipeline.java" \
    "$OLD_FIXTURE/ChunkCache.java" "$OLD_FIXTURE/ChunkMaterializer.java" \
    "$HERE/fixture/RaceGate.java" "$HERE/fixture/WorldGenPipelineShutdownRaceChecks.java"
done
java -ea -Xverify:all -cp "$TMP/race" com.shnewbs.deepprimal.core.WorldGenPipelineShutdownRaceChecks
if java -ea -Xverify:all -cp "$TMP/old" com.shnewbs.deepprimal.core.WorldGenPipelineShutdownRaceChecks >"$TMP/old.log" 2>&1; then
  echo 'FAIL: original source unexpectedly passed deterministic race negative control' >&2; exit 1
fi
grep -q 'pending removed before future settled' "$TMP/old.log" || { cat "$TMP/old.log"; exit 1; }
echo 'PIPELINE_SHUTDOWN_NEGATIVE_CONTROL_PASS original_blob='"$EXPECTED_OLD"' fails_as_expected=true'
