#!/usr/bin/env python3
"""Focused source guard for the V8 reopen fingerprint materializer gate."""
from pathlib import Path

src = Path("src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java").read_text()
assert "fillSavedSnapshot(" in src
assert "expectedSavedProfileFingerprint" in src
assert "prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);" in src
assert src.index("prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);") < src.index("return fillPlannedSurface(prepared.surface(), chunk, queriesPerSlab);")
assert "prepared.requireHeight(chunk.getMinY(), chunk.getHeight());" in src
print("DEV110_SOURCE_GUARD_PASS")
