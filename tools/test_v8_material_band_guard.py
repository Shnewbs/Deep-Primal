#!/usr/bin/env python3
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
p=ROOT / "src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java"
s=p.read_text(encoding="utf-8")
need=[
"column.materialBand() == V8EcotoneColumnPlan.MaterialBand.SNOW",
"BlockState topState = switch (column.materialBand())",
"case SNOW -> snow;",
"column.materialBand() == V8EcotoneColumnPlan.MaterialBand.SNOW) continue",
"case SNOW -> null;"
]
for x in need:
    assert x in s, x
print("DEV108_SOURCE_GUARD_PASS")
