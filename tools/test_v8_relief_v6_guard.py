from pathlib import Path
terrain = Path("engine-core/src/main/java/com/shnewbs/deepprimal/core/density/TerrainShapeV8.java").read_text()
join = Path("engine-core/src/main/java/com/shnewbs/deepprimal/core/density/V8ReliefJoinV6.java").read_text()
snap = Path("src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java").read_text()
assert "definitionReliefV6" in terrain
assert "V8ReliefJoinV6.nodes()" in terrain
assert "relief6_before_canyon" in join
assert "newReliefV6" in snap
print("DEV112_SOURCE_GUARD_PASS")
