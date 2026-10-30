#!/usr/bin/env python3
"""Focused source guard for the V8 reopen fingerprint materializer gate."""
from pathlib import Path

src = Path("src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java").read_text()
marker = "long expectedSavedProfileFingerprint"
start = src.index(marker)
window = src[start:start + 1200]
assert "prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);" in window
assert window.index("prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);") < window.index("return fillPlannedSurface(prepared.surface(), chunk, queriesPerSlab);")
assert "prepared.requireHeight(chunk.getMinY(), chunk.getHeight());" in window
print("DEV110_SOURCE_GUARD_PASS")
