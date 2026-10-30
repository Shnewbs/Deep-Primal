#!/usr/bin/env python3
from pathlib import Path
p=Path("src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java")
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
assert "mod_version=0.0.1-alpha.29-dev.108-material-band-consumer.1" in Path("gradle.properties").read_text()
print("DEV108_SOURCE_GUARD_PASS")
