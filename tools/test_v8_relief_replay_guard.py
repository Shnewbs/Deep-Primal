from pathlib import Path

snap = Path("src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java").read_text()
prop = Path("gradle.properties").read_text()

assert "sampleHeightAt(int worldX, int worldZ)" in snap
assert "requireExactHeightReplay(int worldX, int worldZ, double expected)" in snap
assert 'surface.graph().channel("height", worldX, 0, worldZ)' in snap
assert "Double.doubleToLongBits(actual)" in snap
assert "V8 relief sampling requires an explicit saved profile revision" in snap
assert "dev.113-relief-replay.1" in prop

print("DEV113_SOURCE_GUARD_PASS")
