from pathlib import Path

snap = Path("src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java").read_text()
prepared = snap.index("public static final class Prepared")
sample = snap.index("sampleHeightAt(int worldX, int worldZ)")
replay = snap.index("requireExactHeightReplay(int worldX, int worldZ, double expected)")
assert sample > prepared and replay > sample
assert "sampleHeightAt(int worldX, int worldZ)" not in snap[:prepared]
assert 'surface.graph().channel("height", worldX, 0, worldZ)' in snap
assert "Double.doubleToLongBits(actual)" in snap
assert "V8 relief sampling requires an explicit saved profile revision" in snap

print("DEV113_SOURCE_GUARD_PASS")
