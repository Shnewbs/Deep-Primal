#!/usr/bin/env python3
from pathlib import Path

p = Path("src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java")
s = p.read_text()
required = [
    "baseColumnVegetatedRealized(",
    "result.realized()",
    "if (!receipt.realized(localX, localZ)) return base;",
    "receipt realized an unplanned site",
    "V8VegetationColumnPlan.Plant.FERN",
]
missing = [x for x in required if x not in s]
if missing:
    raise SystemExit("missing realized projection guards: " + ", ".join(missing))
print("DEV102_SOURCE_GUARD_PASS")
