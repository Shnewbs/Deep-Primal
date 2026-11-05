from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SNAPSHOT = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java"
MATERIALIZER = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java"

snapshot = SNAPSHOT.read_text()
materializer = MATERIALIZER.read_text()
required_snapshot = [
    "public long vegetationProfileFingerprint()",
    "private static long mix(long state, long value)",
    "if (version != VEGETATION_VERSION || vegetationPolicy == null) return 0L;",
    "h = mix(h, vegetationPolicy.hashCode());",
]
for token in required_snapshot:
    assert token in snapshot, token
assert "prepared.snapshot().vegetationProfileFingerprint()" in materializer
assert "V8VegetationNativeReceipt.captureBoundToProfile(" in materializer
assert "V8VegetationNativeReceipt.capture(" not in materializer
print("V8_VEGETATION_PROFILE_BINDING_SOURCE_PASS")
