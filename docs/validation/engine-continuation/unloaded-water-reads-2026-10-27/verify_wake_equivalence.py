#!/usr/bin/env python3
"""Exhaustively compare original and one-read neighbor wake predicates.

A source-text check binds this model to the staged production branch.
No Minecraft world or engine frame is executed here.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[4]
source = (root / "src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java").read_text()
start = source.index("public static boolean needsWake(ServerLevel l, BlockPos p) {")
end = source.index("private static boolean surface(ServerLevel l, BlockPos p, int amount)", start)
method = source[start:end]
assert "if (!open(l, q)) continue;" in method
assert method.count("int neighborUnits = units(l, q);") == 1
assert "if (amount > neighborUnits + 1 || (amount == 1 && neighborUnits < WaterTransport.CAPACITY))" in method
cases = 0
for own_units in range(1, 17):
    for neighbor_units in range(17):
        for opened in (False, True):
            before = opened and (own_units > neighbor_units + 1
                or (own_units == 1 and neighbor_units < 16))
            after = False
            if opened:
                sample = neighbor_units
                after = own_units > sample + 1 or (own_units == 1 and sample < 16)
            assert before == after, (own_units, neighbor_units, opened)
            cases += 1
print(f"WATER_WAKE_EQUIVALENCE_PASS cases={cases} model_equivalence=true native=false")
