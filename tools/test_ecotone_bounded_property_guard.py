#!/usr/bin/env python3
from pathlib import Path
p=Path("engine-core/src/main/java/com/shnewbs/deepprimal/core/biome/EcotoneBlend.java").read_text()
q=Path("src/coreChecks/java/com/shnewbs/deepprimal/core/EcotoneBlendChecks.java").read_text()
assert "blendBoundedValues" in p
assert "physical heights, liquid volume or hydrology ownership" in p
assert "blendBoundedValues(new double[]" in q
print("DEV105_SOURCE_GUARD_PASS")
