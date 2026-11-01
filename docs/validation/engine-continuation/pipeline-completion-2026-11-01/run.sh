#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac --release 21 -Xlint:all -Werror -d "$OUT" \
  "$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java" \
  "$HERE/fixture/ChunkMaterializer.java" \
  "$HERE/fixture/ChunkCache.java" \
  "$HERE/fixture/PipelineCompletionChecks.java"
java -ea -Xverify:all -cp "$OUT" com.shnewbs.deepprimal.core.PipelineCompletionChecks
