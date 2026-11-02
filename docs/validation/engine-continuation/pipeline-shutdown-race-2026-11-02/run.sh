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
EXPECTED_NEW=3f4f86a7e7fc0b61b458fbb87835886219c6acca
EXPECTED_OLD=2e1a5cb55007d10745260dd355fbf180d5f2920b
[[ "$(git hash-object "$SOURCE")" == "$EXPECTED_NEW" ]] || { echo 'Source changed; review and update the pinned regression.' >&2; exit 2; }
javac --release 21 -Xlint:all -Werror -d "$TMP/normal" "$SOURCE" \
  "$OLD_FIXTURE/ChunkCache.java" "$OLD_FIXTURE/ChunkMaterializer.java" \
  "$CHECKS/WorldGenPipelineKeyChecks.java" "$CHECKS/WorldGenPipelineShutdownChecks.java"
java -ea -Xverify:all -cp "$TMP/normal" com.shnewbs.deepprimal.core.WorldGenPipelineKeyChecks
java -ea -Xverify:all -cp "$TMP/normal" com.shnewbs.deepprimal.core.WorldGenPipelineShutdownChecks
python3 - "$SOURCE" "$TMP/race-src/WorldGenPipeline.java" "$TMP/old-src/WorldGenPipeline.java" <<'PY'
from pathlib import Path
import sys
source = Path(sys.argv[1]).read_text()
old = source
new_guard = '''        // Keep this query discoverable by shutdown until its shared future is settled.
        // Otherwise shutdown can miss a completion between cache admission and complete().
        if(stopped)failure'''
old_guard = '''        pending.remove(key);
        if(stopped)failure'''
new_settle = '''      try {
        if(failure!=null)result.completeExceptionally(failure);else result.complete(chunk);
      } finally {
        synchronized(this){pending.remove(key,result);}
      }
    });}catch(Throwable error){
      try {result.completeExceptionally(error);}
      finally {synchronized(this){pending.remove(key,result);}}
    }'''
old_settle = '''      if(failure!=null)result.completeExceptionally(failure);else result.complete(chunk);
    });}catch(Throwable error){synchronized(this){pending.remove(key);}result.completeExceptionally(error);}'''
assert source.count(new_guard) == source.count(new_settle) == 1
old = old.replace(new_guard, old_guard).replace(new_settle, old_settle)
Path(sys.argv[3]).write_text(old)
needle = '      // User continuations may call back into EngineCore: never run them under this lock.\n'
assert source.count(needle) == old.count(needle) == 1
for body, path in [(source, sys.argv[2]), (old, sys.argv[3])]:
    Path(path).write_text(body.replace(needle, needle + '      RaceGate.pause();\n', 1))
PY
# Verify the reverse-patched original is byte-identical to the audited prior Git blob.
python3 - "$TMP/old-src/WorldGenPipeline.java" "$EXPECTED_OLD" <<'PY'
from pathlib import Path
import hashlib, sys
s=Path(sys.argv[1]).read_text().replace('      RaceGate.pause();\n','',1).encode()
hash=hashlib.sha1(b'blob '+str(len(s)).encode()+b'\0'+s).hexdigest()
if hash != sys.argv[2]: raise SystemExit('Original negative-control source identity mismatch: '+hash)
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
