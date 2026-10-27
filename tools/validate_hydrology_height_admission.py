#!/usr/bin/env python3
"""Non-native source-contract check for loaded/bounded hydrology reads.

A Java 25 stand-in checks short-circuit read admission separately.
Neither check replaces real NeoForge chunk-unload/save/reopen testing.
"""
from pathlib import Path

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


def check(source: str) -> dict[str, bool]:
    place = method(source, "public static boolean place(")
    surface = method(source, "private static BlockPos exposedColumnCell(")
    bed = method(source, "private static BlockPos waterColumnBed(")
    rain = method(source, "private static void precipitation(")
    visit = method(source, "private static void visit(")
    survey = method(source, "private static void survey(")
    return {
        "place_checks_height_before_world_read": 0 <= place.find("isInWorldBounds(p)") < place.find("getBlockState(p)"),
        "place_checks_chunk_before_world_read": 0 <= place.find("hasChunkAt(p)") < place.find("getBlockState(p)"),
        "surface_checks_height_before_world_read": 0 <= surface.find("isInWorldBounds(p)") < surface.find("getBlockState(p)"),
        "surface_checks_chunk_before_world_read": 0 <= surface.find("hasChunkAt(p)") < surface.find("getBlockState(p)"),
        "bed_checks_height_before_world_read": 0 <= bed.find("isInWorldBounds(p)") < bed.find("getBlockState(p)"),
        "bed_checks_chunk_before_world_read": 0 <= bed.find("hasChunkAt(p)") < bed.find("getBlockState(p)"),
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
    # Mutation controls: fail closed when an important first-read guard is removed.
    mutant = source.replace("!l.isInWorldBounds(p) || !l.hasChunkAt(p)\n        || parcel.water()", "!l.hasChunkAt(p)\n        || parcel.water()", 1)
    if mutant == source or check(mutant)["place_checks_height_before_world_read"]:
        failed.append("mutation_missing_place_height_guard")
    if failed:
        raise SystemExit("WATER_HEIGHT_ADMISSION_STATIC_FAIL: " + ",".join(failed))
    print(f"WATER_HEIGHT_ADMISSION_STATIC_PASS checks={len(results)+1} production_source={SOURCE}")


if __name__ == "__main__":
    main()
