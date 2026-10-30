#!/usr/bin/env python3
from pathlib import Path

src = Path("src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java").read_text()
required = [
    "blendPolicy = V8BlendPolicy.effectiveForSnapshot(version,",
    'Objects.requireNonNull(blendPolicy, "blendPolicy")',
    "legacy V8 snapshot cannot silently enable soft blending",
]
for text in required:
    if text not in src:
        raise SystemExit(f"missing source anchor: {text}")
if "effectiveForSnapshot(version,\n        Objects.requireNonNull(blendPolicy" not in src:
    raise SystemExit("policy admission is not adjacent to snapshot construction")
print("DEV104_SOURCE_GUARD_PASS")
