#!/usr/bin/env python3
"""Static/source-level guard for world-bound V8 vegetation receipts.

This does not claim native Minecraft execution. It verifies that the committed
receipt source records the absolute chunk origin, bounds counts, and exposes a
same-chunk guard before callers reuse a read-back mask.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeReceipt.java"
src = SOURCE.read_text()

required = [
    "private final int originX;",
    "private final int originZ;",
    "public int originX()",
    "public int originZ()",
    "public boolean matchesChunk(ChunkAccess chunk)",
    "public void requireSameChunk(ChunkAccess chunk)",
    "new V8VegetationNativeReceipt(words, candidateCount, observed, originX, originZ,\n        profileFingerprint)",
]
for anchor in required:
    if src.count(anchor) != 1:
        raise AssertionError(f"missing or duplicated receipt identity anchor: {anchor}")

if "realizedCount < 0 || realizedCount > plannedCount" not in src:
    raise AssertionError("receipt count bounds are not fail-closed")
if "chunk.getPos().getMinBlockX() == originX" not in src:
    raise AssertionError("receipt X-origin guard is not absolute-coordinate bound")
if "chunk.getPos().getMinBlockZ() == originZ" not in src:
    raise AssertionError("receipt Z-origin guard is not absolute-coordinate bound")

print("V8_RECEIPT_IDENTITY_STATIC_PASS anchors=7 native=false")
