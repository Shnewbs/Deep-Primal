#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = (root / "src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java").read_text()
needles = (
    "consumeSavedEcotoneVegetationReceipt",
    "prepared.consumeVegetationReceipt(chunk, planned, receipt)",
    "explicit saved V8 revision 6 vegetation required",
    "Objects.requireNonNull(planned, \"V8 planned vegetation\")",
)
for needle in needles:
    assert needle in src, needle
assert src.index("requireCompatibleVegetationReceipt") < src.index("consumeSavedEcotoneVegetationReceipt")
print("DEV101_SOURCE_GUARD_PASS")
