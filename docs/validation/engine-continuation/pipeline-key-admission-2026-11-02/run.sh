#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../../.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
SRC="$ROOT/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java"
TEST="$ROOT/src/coreChecks/java/com/shnewbs/deepprimal/core/WorldGenPipelineKeyChecks.java"
FIXTURE="$ROOT/docs/validation/engine-continuation/pipeline-key-admission-2026-11-02/fixture"
javac --release 21 -Xlint:all -Werror -d "$TMP" "$SRC" "$TEST" "$FIXTURE/ChunkCache.java" "$FIXTURE/ChunkMaterializer.java"
java -ea -Xverify:all -cp "$TMP" com.shnewbs.deepprimal.core.WorldGenPipelineKeyChecks
