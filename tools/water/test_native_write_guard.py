#!/usr/bin/env python3
"""Compile exact production write methods against explicit boundary doubles.

No Gradle, network, Minecraft classes or native callback acceptance is implied.
Requires Python 3.10+ and a Java compiler/runtime supporting --release 21.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile

PACKAGE = "com.shnewbs.deepprimal.engine"
JAVA_PATH = Path("src/main/java/com/shnewbs/deepprimal/engine")
EVIDENCE_PATH = Path("docs/validation/engine-continuation/water-native-write-guard-2026-10-28")
OPERATIONS = ("restoreOcean", "oceanSink", "place", "pickup", "emit", "infiltrate", "meltIce")


def method(source: str, name: str) -> str:
    """Extract one declaration with a lexical brace scanner, not a body rewrite."""
    matches = list(re.finditer(r"(?m)^  (?:public|private|protected)?\s*static\s+[^\n{;]+\b" + re.escape(name) + r"\s*\(", source))
    if len(matches) != 1:
        raise ValueError(f"Expected one declaration for {name}, got {len(matches)}")
    start = matches[0].start()
    opening = source.index("{", matches[0].end())
    depth, i, state = 1, opening + 1, "code"
    while i < len(source):
        c, nxt = source[i], source[i:i+2]
        if state == "line":
            if c == "\n": state = "code"
        elif state == "block":
            if nxt == "*/": state = "code"; i += 1
        elif state in ("string", "char"):
            if c == "\\": i += 1
            elif c == ('"' if state == "string" else "'"): state = "code"
        elif nxt == "//": state = "line"; i += 1
        elif nxt == "/*": state = "block"; i += 1
        elif c == '"': state = "string"
        elif c == "'": state = "char"
        elif c == "{": depth += 1
        elif c == "}":
            depth -= 1
            if not depth: return source[start:i+1]
        i += 1
    raise ValueError(f"Unclosed method {name}")


def integration(source: str) -> int:
    checks = 0
    for name in OPERATIONS:
        body = method(source, name)
        if len(re.findall(r"\bwriteAccounted\s*\(", body)) != 1:
            raise ValueError(f"{name} must use exactly one accounted native admission")
        if re.search(r"\b(?:write|setBlock)\s*\(", body):
            raise ValueError(f"{name} bypasses accounted admission")
        if body.index("writeAccounted(") > body.index("store("):
            raise ValueError(f"{name} stores chemistry before native admission")
        checks += 3
    return checks


def execute(source: str, scope: str, fixture: str, javac: str, java: str,
            release: int, accounted: bool) -> dict:
    bodies = [method(source, "write")]
    if accounted: bodies.append(method(source, "writeAccounted"))
    target = "writeAccounted" if accounted else "write"
    wrapper = (f"package {PACKAGE};\nimport {PACKAGE}.NativeWriteGuardProbe.*;\n"
               "public final class HydrologyRuntime {\n" + "\n".join(bodies)
               + "\n  public static boolean underTest(ServerLevel l, BlockPos p, BlockState s) {\n"
               + f"    return {target}(l, p, s);\n  }}\n}}\n")
    with tempfile.TemporaryDirectory(prefix="deepprimal-write-guard-") as temp:
        root = Path(temp); classes = root / "classes"; classes.mkdir()
        files = []
        for name, text in [("HydrologyRuntime.java", wrapper), ("WaterWriteScope.java", scope),
                           ("NativeWriteGuardProbe.java", fixture)]:
            p = root / name; p.write_text(text, encoding="utf-8"); files.append(str(p))
        compile_cmd = [javac, "--release", str(release), "-Xlint:all", "-Werror", "-d", str(classes), *files]
        compiled = subprocess.run(compile_cmd, capture_output=True, text=True, timeout=60)
        result = {"compile_exit": compiled.returncode,
                  "compile_stdout": compiled.stdout, "compile_stderr": compiled.stderr,
                  "extracted_methods_sha256": hashlib.sha256("\n".join(bodies).encode()).hexdigest()}
        if compiled.returncode == 0:
            ran = subprocess.run([java, "-Xverify:all", "-cp", str(classes), PACKAGE + ".NativeWriteGuardProbe"],
                                 capture_output=True, text=True, timeout=60)
            result.update(run_exit=ran.returncode, stdout=ran.stdout, stderr=ran.stderr)
        return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--source", type=Path)
    parser.add_argument("--scope", type=Path)
    parser.add_argument("--fixture", type=Path)
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("--javac", default="javac")
    parser.add_argument("--java", default="java")
    parser.add_argument("--release", type=int, default=21)
    parser.add_argument("--result", type=Path)
    args = parser.parse_args()
    source_path = args.source or args.root / JAVA_PATH / "HydrologyRuntime.java"
    scope_path = args.scope or args.root / JAVA_PATH / "WaterWriteScope.java"
    fixture_path = args.fixture or args.root / EVIDENCE_PATH / "NativeWriteGuardProbe.java"
    source, scope, fixture = [p.read_text(encoding="utf-8") for p in (source_path, scope_path, fixture_path)]
    results = {"scope": "exact-source extraction plus explicit boundary doubles; NOT native Minecraft",
               "java": subprocess.run([args.java, "-version"], capture_output=True, text=True, timeout=15).stderr,
               "javac": subprocess.run([args.javac, "-version"], capture_output=True, text=True, timeout=15).stdout,
               "release": args.release,
               "input_sha256": {p.name: hashlib.sha256(p.read_bytes()).hexdigest()
                                for p in (source_path, scope_path, fixture_path)}}
    run = lambda text, accounted=True: execute(text, scope, fixture, args.javac, args.java, args.release, accounted)
    results["integration_assertions"] = integration(source)
    results["candidate"] = run(source)
    ok = results["candidate"].get("run_exit") == 0
    mutants = {
        "accept_replaced_success": ("if (!observed.equals(requested))", "if (false)"),
        "omit_unload_guards": ("!l.hasChunkAt(cell)", "false"),
        "skip_compensation": ("    write(l, cell, before);", "    // mutation: skipped compensation"),
        "overwrite_foreign_edit": ("if (!observed.equals(requested))\n      throw", "if (false)\n      throw"),
        "mutable_position_alias": ("BlockPos cell = p.immutable();", "BlockPos cell = p;"),
        "swallow_callback_exception": ("try { return l.setBlock(p, s, 2 | 16); }",
                                       "try { return l.setBlock(p, s, 2 | 16); } catch (RuntimeException failure) { return false; }")
    }
    results["mutations"] = {}
    for name, (old, new) in mutants.items():
        if old not in source: raise ValueError(f"Mutation target absent: {name}")
        record = run(source.replace(old, new))
        record["rejected"] = record["compile_exit"] == 0 and record.get("run_exit", 0) != 0
        results["mutations"][name] = record
        ok &= record["rejected"]
    # A helper alone is insufficient: every audited caller must actually use it.
    bypass = source.replace("if (!writeAccounted(l, p, EngineRegistry.WATER_BLOCK.get().defaultBlockState()))",
                            "if (!write(l, p, EngineRegistry.WATER_BLOCK.get().defaultBlockState()))", 1)
    try:
        integration(bypass)
        results["integration_bypass_rejected"] = False
    except ValueError:
        results["integration_bypass_rejected"] = True
    ok &= results["integration_bypass_rejected"]
    if args.baseline:
        results["baseline"] = run(args.baseline.read_text(encoding="utf-8"), False)
        results["baseline_reproduces_failure"] = (results["baseline"]["compile_exit"] == 0
                                                 and results["baseline"].get("run_exit", 0) != 0)
        ok &= results["baseline_reproduces_failure"]
    results["passed"] = bool(ok)
    text = json.dumps(results, indent=2) + "\n"
    if args.result:
        args.result.parent.mkdir(parents=True, exist_ok=True); args.result.write_text(text, encoding="utf-8")
    print(text)
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
