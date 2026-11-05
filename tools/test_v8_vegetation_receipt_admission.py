#!/usr/bin/env python3
"""Focused source guard for the dev98 V8 receipt reuse admission."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
receipt = (ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeReceipt.java").read_text()
materializer = (ROOT / "src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java").read_text()

for needle in (
    "matchesProfileAndChunk",
    "requireSameProfileAndChunk",
    "expectedFingerprint != 0L",
    "matchesProfile(expectedFingerprint)",
    "matchesChunk(chunk)",
):
    assert needle in receipt, needle

for needle in (
    "requireCompatibleVegetationReceipt",
    "prepared.snapshot().vegetationProfileFingerprint()",
    "receipt.requireSameProfileAndChunk",
    "VEGETATION_VERSION",
):
    assert needle in materializer, needle

assert "capture(" in receipt
assert "captureBoundToProfile(" in receipt
print("V8_RECEIPT_ADMISSION_SOURCE_PASS")
