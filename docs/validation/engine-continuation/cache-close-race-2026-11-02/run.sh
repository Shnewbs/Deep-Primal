#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/src" "$tmp/classes" "$tmp/negative-classes"
cp "$root/engine-core/src/main/java/com/shnewbs/deepprimal/core/ChunkCache.java" "$tmp/src/ChunkCache.java"
cp "$root/src/coreChecks/java/com/shnewbs/deepprimal/core/CacheLifecycleChecks.java" "$tmp/src/CacheLifecycleChecks.java"
cp "$root/src/coreChecks/java/com/shnewbs/deepprimal/core/CacheCloseRaceChecks.java" "$tmp/src/CacheCloseRaceChecks.java"
# EngineMetrics is not the subject of this isolated test; its counters are stubbed.
cat > "$tmp/src/EngineMetrics.java" <<'JAVA'
package com.shnewbs.deepprimal.core;
final class EngineMetrics {
  static final Counter CACHE_MISSES = new Counter();
  static final Counter CACHE_HITS = new Counter();
  static final Counter CACHE_EVICTIONS = new Counter();
  static final class Counter { void increment() {} }
}
JAVA
javac --release 21 -Xlint:all,-cast -Werror -d "$tmp/classes" "$tmp/src/"*.java
java -ea -Xverify:all -cp "$tmp/classes" com.shnewbs.deepprimal.core.CacheLifecycleChecks
java -ea -Xverify:all -cp "$tmp/classes" com.shnewbs.deepprimal.core.CacheCloseRaceChecks
# Reconstruct only the old completion-order behavior as a negative control.
python3 - "$tmp/src/ChunkCache.java" <<'PY'
import pathlib, sys
p=pathlib.Path(sys.argv[1]); s=p.read_text()
old='      // close() is terminal: never deliver a chunk computed for a retired world.\n      ensureOpen();\n      // clear() alone invalidates admission while preserving the caller\'s result.\n'
new='      // clear() invalidates pending admissions without waiting for terrain computation.\n'
assert s.count(old)==1, 'expected current production guard not found'
p.write_text(s.replace(old,new))
PY
javac --release 21 -Xlint:all,-cast -Werror -d "$tmp/negative-classes" "$tmp/src/"*.java
if java -ea -Xverify:all -cp "$tmp/negative-classes" com.shnewbs.deepprimal.core.CacheCloseRaceChecks > "$tmp/negative.log" 2>&1; then
  echo 'NEGATIVE_CONTROL_UNEXPECTED_PASS' >&2
  exit 1
fi
if ! grep -Fq 'closed world returned obsolete value' "$tmp/negative.log"; then
  cat "$tmp/negative.log" >&2
  echo 'NEGATIVE_CONTROL_WRONG_FAILURE' >&2
  exit 1
fi
echo 'NEGATIVE_CONTROL_REJECTED expected stale completion'
