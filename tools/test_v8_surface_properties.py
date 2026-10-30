#!/usr/bin/env python3
from pathlib import Path

p = Path("src/main/java/com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java")
s = p.read_text(encoding="utf-8")
need = [
    "public double snowFraction()",
    "public double vegetationDensity()",
    "public double soilFactor()",
    "public MaterialBand materialBand()",
    "enum MaterialBand { BARE, GRASS, PODZOL, SNOW }",
    "cover.snowPatch() || cover.snow() >= 0.66",
]
for token in need:
    assert token in s, token
assert "materialBand" in s
print("DEV107_SOURCE_GUARD_PASS")
