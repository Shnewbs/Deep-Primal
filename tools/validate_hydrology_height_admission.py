#!/usr/bin/env python3
"""Non-native source-contract check for loaded/bounded hydrology reads.

A Java 25 stand-in checks short-circuit read admission separately.
Neither check replaces real NeoForge chunk-unload/save/reopen testing.
"""
from pathlib import Path
import re

SOURCE = Path("src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java")


def method(source: str, signature: str) -> str:
    begin = source.index(signature)
    start = source.index("{", begin)
    depth = 0
    for index in range(start, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise AssertionError("Unbalanced Java method: " + signature)



# A boolean AND or a delayed check is unsafe even when both predicate
# names appear in a method. Reject it before the first state read.
_RESIDENT_GUARD = r"!l\.isInWorldBounds\(p\)\s*\|\|\s*!l\.hasChunkAt\(p\)"


def guarded_before_read(body: str, kind: str) -> bool:
    first = body.find("l.getBlockState(p)")
    if first < 0:
        return False
    prefix = body[:first]
    if kind == "null":
        return bool(re.search(r"if\s*\(\s*" + _RESIDENT_GUARD
                              + r"\s*\)\s*return\s+null\s*;", prefix))
    return bool(re.search(r"if\s*\(\s*" + _RESIDENT_GUARD + r"\s*\|\|", prefix))


def mutant_rejected(source: str, signature: str, before: str,
                    after: str, failure_key: str) -> bool:
    body = method(source, signature)
    if body.count(before) != 1:
        return False
    mutant = source.replace(body, body.replace(before, after, 1), 1)
    return not check(mutant)[failure_key]


def check(source: str) -> dict[str, bool]:
    place = method(source, "public static boolean place(")
    surface = method(source, "private static BlockPos exposedColumnCell(")
    bed = method(source, "private static BlockPos waterColumnBed(")
    rain = method(source, "private static void precipitation(")
    visit = method(source, "private static void visit(")
    survey = method(source, "private static void survey(")
    return {
        "place_checks_height_before_world_read": guarded_before_read(place, "false"),
        "place_checks_chunk_before_world_read": guarded_before_read(place, "false"),
        "surface_checks_height_before_world_read": guarded_before_read(surface, "null"),
        "surface_checks_chunk_before_world_read": guarded_before_read(surface, "null"),
        "bed_checks_height_before_world_read": guarded_before_read(bed, "null"),
        "bed_checks_chunk_before_world_read": guarded_before_read(bed, "null"),
        "surface_scan_stays_bounded": "i < 8" in surface,
        "bed_scan_stays_bounded": "i < 16" in bed,
        "precipitation_guards_surface_and_bed": "exposedColumnCell(l, top)" in rain and "if (p == null) continue;" in rain and "waterColumnBed(l, p)" in rain and "if (bed == null) continue;" in rain,
        "visit_guards_surface_and_bed": "exposedColumnCell(l, top)" in visit and "if (p == null) return;" in visit and "waterColumnBed(l," in visit and "if (bed == null) return;" in visit,
        "survey_guards_surface": "exposedColumnCell(l, top)" in survey and "if (p == null) continue;" in survey,
        "no_unguarded_old_descent": "i < 8 && l.getBlockState(p)" not in source,
    }


def main() -> None:
    source = SOURCE.read_text(encoding="utf-8")
    results = check(source)
    failed = [key for key, passed in results.items() if not passed]
    joint = "!l.isInWorldBounds(p) || !l.hasChunkAt(p)"
    controls = {
        "place_operator_and": mutant_rejected(source, "public static boolean place(",
            joint, "!l.isInWorldBounds(p) && !l.hasChunkAt(p)",
            "place_checks_height_before_world_read"),
        "surface_operator_and": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            joint, "!l.isInWorldBounds(p) && !l.hasChunkAt(p)",
            "surface_checks_chunk_before_world_read"),
        "bed_operator_and": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            joint, "!l.isInWorldBounds(p) && !l.hasChunkAt(p)",
            "bed_checks_height_before_world_read"),
        "place_missing_height": mutant_rejected(source, "public static boolean place(",
            joint, "!l.hasChunkAt(p)", "place_checks_height_before_world_read"),
    }
    failed.extend("mutation_" + name for name, rejected in controls.items() if not rejected)
    if failed:
        raise SystemExit("WATER_HEIGHT_ADMISSION_STATIC_FAIL: " + ",".join(failed))
    print(f"WATER_HEIGHT_ADMISSION_STATIC_PASS checks={len(results)+len(controls)} mutationsRejected={sum(controls.values())} production_source={SOURCE}")


if __name__ == "__main__":
    main()
