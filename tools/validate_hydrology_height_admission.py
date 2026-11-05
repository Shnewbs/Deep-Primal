#!/usr/bin/env python3
"""Non-native source-contract check for loaded/bounded hydrology reads.

A Java 25 stand-in checks short-circuit read admission separately.
Neither check replaces real NeoForge chunk-unload/save/reopen testing.
"""
from pathlib import Path
import re

SOURCE = Path("src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java")



def code_only(source: str) -> str:
    """Mask Java comments and literals without shifting offsets or newlines.

    A guard appearing in a comment, ordinary string, character or Java text
    block is not executable. Braces in those regions must not delimit methods,
    and getBlockState text there must not count as the first native read.
    """
    characters = list(source)
    length = len(source)
    index = 0
    while index < length:
        start = index
        if source.startswith("//", index):
            end = source.find("\n", index + 2)
            if end < 0:
                end = length
        elif source.startswith("/*", index):
            close = source.find("*/", index + 2)
            if close < 0:
                raise AssertionError("Unterminated Java block comment")
            end = close + 2
        elif source.startswith('"""', index):
            end = index + 3
            while end < length:
                if source[end] == "\\":
                    end += 2
                elif source.startswith('"""', end):
                    end += 3
                    break
                else:
                    end += 1
            else:
                raise AssertionError("Unterminated Java text block")
        elif source[index] in ('"', "'"):
            quote = source[index]
            end = index + 1
            while end < length:
                if source[end] == "\\":
                    end += 2
                elif source[end] == quote:
                    end += 1
                    break
                else:
                    end += 1
            else:
                raise AssertionError("Unterminated Java quoted literal")
        else:
            index += 1
            continue
        for offset in range(start, end):
            if characters[offset] not in ("\r", "\n"):
                characters[offset] = " "
        index = end
    return "".join(characters)

def method(source: str, signature: str) -> str:
    code = code_only(source)
    begin = code.index(signature)
    start = code.index("{", begin)
    depth = 0
    for index in range(start, len(code)):
        if code[index] == "{":
            depth += 1
        elif code[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise AssertionError("Unbalanced Java method: " + signature)



# A boolean AND or a delayed check is unsafe even when both predicate
# names appear in a method. Reject it before the first state read.
_RESIDENT_GUARD = r"!l\.isInWorldBounds\(p\)\s*\|\|\s*!l\.hasChunkAt\(p\)"


def guarded_before_read(body: str, kind: str) -> bool:
    """Fail closed on dead branch wrappers for the installed hydrology methods.

    This validates the known straight-line method and bounded-loop structure,
    not an arbitrary Java control-flow graph. Changed method shapes must be
    reviewed and the validator updated together.
    """
    code = code_only(body)
    first = code.find("l.getBlockState(p)")
    if first < 0:
        return False
    if kind == "false":
        pattern = (r"\A\{\s*if\s*\(\s*" + _RESIDENT_GUARD
                   + r"\s*\|\|[^{};]*?l\.getBlockState\(p\)[^{};]*\)\s*return\s+false\s*;")
    elif kind == "null":
        pattern = (r"\A\{\s*var\s+p\s*=\s*(?:top\.below\(\)|initial)\s*;\s*"
                   r"for\s*\(\s*int\s+i\s*=\s*0\s*;\s*i\s*<\s*(?:8|16)\s*;"
                   r"\s*i\+\+\s*\)\s*\{\s*if\s*\(\s*" + _RESIDENT_GUARD
                   + r"\s*\)\s*return\s+null\s*;\s*"
                   r"(?:var\s+state\s*=\s*|if\s*\(\s*!\s*)l\.getBlockState\(p\)")
    else:
        raise ValueError("Unknown guard result: " + kind)
    match = re.match(pattern, code, re.DOTALL)
    return match is not None and match.start() <= first < match.end()


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
    rain = code_only(method(source, "private static void precipitation("))
    visit = code_only(method(source, "private static void visit("))
    survey = code_only(method(source, "private static void survey("))
    return {
        "place_checks_height_before_world_read": guarded_before_read(place, "false"),
        "place_checks_chunk_before_world_read": guarded_before_read(place, "false"),
        "surface_checks_height_before_world_read": guarded_before_read(surface, "null"),
        "surface_checks_chunk_before_world_read": guarded_before_read(surface, "null"),
        "bed_checks_height_before_world_read": guarded_before_read(bed, "null"),
        "bed_checks_chunk_before_world_read": guarded_before_read(bed, "null"),
        "surface_scan_stays_bounded": "i < 8" in code_only(surface),
        "bed_scan_stays_bounded": "i < 16" in code_only(bed),
        "precipitation_guards_surface_and_bed": "exposedColumnCell(l, top)" in rain and "if (p == null) continue;" in rain and "waterColumnBed(l, p)" in rain and "if (bed == null) continue;" in rain,
        "visit_guards_surface_and_bed": "exposedColumnCell(l, top)" in visit and "if (p == null) return;" in visit and "waterColumnBed(l," in visit and "if (bed == null) return;" in visit,
        "survey_guards_surface": "exposedColumnCell(l, top)" in survey and "if (p == null) continue;" in survey,
        "no_unguarded_old_descent": "i < 8 && l.getBlockState(p)" not in code_only(source),
    }


def main() -> None:
    source = SOURCE.read_text(encoding="utf-8")
    results = check(source)
    failed = [key for key, passed in results.items() if not passed]
    joint = "!l.isInWorldBounds(p) || !l.hasChunkAt(p)"
    null_guard = "if (" + joint + ") return null;"
    place_guard = "if (" + joint + "\n        ||"
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
        "place_guard_commented": mutant_rejected(source, "public static boolean place(",
            place_guard, "/* " + place_guard + " */ if (",
            "place_checks_chunk_before_world_read"),
        "surface_guard_commented": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            null_guard, "/* " + null_guard + " */", "surface_checks_height_before_world_read"),
        "bed_guard_commented": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            null_guard, "/* " + null_guard + " */", "bed_checks_chunk_before_world_read"),
        "surface_guard_string": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            null_guard, 'String decoy = "' + null_guard + '";', "surface_checks_chunk_before_world_read"),
        "bed_guard_string": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            null_guard, 'String decoy = "' + null_guard + '";', "bed_checks_height_before_world_read"),
    }
    # A lexically perfect guard cannot protect a read in unreachable code.
    controls.update({
        "place_dead_if": mutant_rejected(source, "public static boolean place(",
            place_guard, "if (false) " + place_guard, "place_checks_height_before_world_read"),
        "place_dead_else": mutant_rejected(source, "public static boolean place(",
            place_guard, "if (true) {} else " + place_guard, "place_checks_chunk_before_world_read"),
        "surface_dead_if": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            null_guard, "if (false) " + null_guard, "surface_checks_height_before_world_read"),
        "surface_dead_else": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            null_guard, "if (true) {} else " + null_guard, "surface_checks_chunk_before_world_read"),
        "surface_contradictory": mutant_rejected(source, "private static BlockPos exposedColumnCell(",
            null_guard, "if (l.isInWorldBounds(p) && l.hasChunkAt(p)) " + null_guard,
            "surface_checks_height_before_world_read"),
        "bed_dead_if": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            null_guard, "if (false) " + null_guard, "bed_checks_chunk_before_world_read"),
        "bed_dead_else": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            null_guard, "if (true) {} else " + null_guard, "bed_checks_height_before_world_read"),
        "bed_contradictory": mutant_rejected(source, "private static BlockPos waterColumnBed(",
            null_guard, "if (l.isInWorldBounds(p) && l.hasChunkAt(p)) " + null_guard,
            "bed_checks_chunk_before_world_read"),
    })
    failed.extend("mutation_" + name for name, rejected in controls.items() if not rejected)
    if failed:
        raise SystemExit("WATER_HEIGHT_ADMISSION_STATIC_FAIL: " + ",".join(failed))
    print(f"WATER_HEIGHT_ADMISSION_STATIC_PASS checks={len(results)+len(controls)} mutationsRejected={sum(controls.values())} production_source={SOURCE}")


if __name__ == "__main__":
    main()
