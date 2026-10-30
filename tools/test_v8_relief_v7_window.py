#!/usr/bin/env python3
"""Dependency-free, source-identity-pinned V6/V7 join-window regression.

This checks the production Java source thresholds and their mathematical
window, not the complete DensityGraph VM or native Minecraft worldgen.
"""
from pathlib import Path
import hashlib
import re

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/density"
EXPECTED = {
    "V8ReliefJoinV6.java": "7d9144fc9d9c342a1c2e933a99143a7321b3a595",
    "V8ReliefJoinV7.java": "0c72f24a62d4c94544f64045ae492cdcd3428f1a",
}
checks = 0

def require(ok, message):
    global checks
    checks += 1
    if not ok:
        raise AssertionError(message)

def source(name):
    data = (SRC / name).read_bytes()
    identity = hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()
    require(identity == EXPECTED[name], f"unexpected source blob {name}: {identity}")
    return data.decode("utf-8")

def band(text, node):
    pattern = (r'Node\.bounded\("' + re.escape(node) +
               r'",\s*Op\.SMOOTHSTEP,\s*"continentalness",\s*([.\d-]+),\s*([.\d-]+)\)')
    found = re.search(pattern, text)
    require(found is not None, f"missing source band {node}")
    lo, hi = map(float, found.groups())
    require(hi > lo, f"reversed source band {node}")
    return lo, hi

def smoothstep(x, band):
    lo, hi = band
    t = min(1.0, max(0.0, (x - lo) / (hi - lo)))
    return t * t * (3 - 2 * t)

def main():
    v6, v7 = source("V8ReliefJoinV6.java"), source("V8ReliefJoinV7.java")
    a6 = band(v6, "relief6_plateau_entry")
    b6 = band(v6, "relief6_coast_release")
    a7 = band(v7, "relief7_foothill_entry")
    b7 = band(v7, "relief7_highland_entry")
    require(v7.count("Node.") == 10, "unexpected V7 graph node count")
    for i in range(-10000, 10001):
        x = i / 10000
        w6 = smoothstep(x, a6) * (1 - smoothstep(x, b6))
        w7 = smoothstep(x, a7) * (1 - smoothstep(x, b7))
        require(0 <= w6 <= 1 and 0 <= w7 <= 1, "out-of-range window")
        if .20 <= x <= .65:
            require(w6 == 0 and w7 > 0, "V7 join not broader than frozen V6")
        if x <= .08 or x >= .80:
            require(w7 == 0, "protected coast/highland join not zero")
    require(smoothstep(.40, a7) * (1 - smoothstep(.40, b7)) == 1,
            "missing full-strength V7 join")
    print(f"V8_RELIEF_V7_WINDOW_PASS assertions={checks} "
          "sourceIdentity=verified native=false vm=false")

if __name__ == "__main__":
    main()
