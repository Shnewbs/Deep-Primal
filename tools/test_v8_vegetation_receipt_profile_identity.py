#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeReceipt.java"
s = SRC.read_text(encoding="utf-8")

required = [
    "private final long profileFingerprint;",
    "public long profileFingerprint()",
    "public boolean matchesProfile(long expectedFingerprint)",
    "public void requireSameProfile(long expectedFingerprint)",
    "captureBoundToProfile",
    "profileFingerprint);",
    "return captureBoundToProfile(",
]
for token in required:
    assert token in s, token

assert "shortGrass, fern, 0L);" in s
assert "profileFingerprint);" in s
print("V8_RECEIPT_PROFILE_IDENTITY_PASS tokens=", len(required))
