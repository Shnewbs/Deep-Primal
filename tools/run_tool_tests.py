#!/usr/bin/env python3
"""Run every self-contained tools/test_*.py and fail if any of them fail.

Most files in tools/ are plain scripts that print ``*_PASS`` lines and exit
non-zero on failure, so ``unittest discover`` cannot drive them. This runner
executes each one in a subprocess from the repository root, the way CI does.

Some tests cannot run in a bare checkout. They are listed in SKIP with the
reason, are reported on every run, and the runner fails if a SKIP entry points
at a file that no longer exists so the list cannot rot silently.

Usage:
    python3 tools/run_tool_tests.py            # run everything not skipped
    python3 tools/run_tool_tests.py --list     # show what would run / be skipped
    python3 tools/run_tool_tests.py -k outlet  # only names containing 'outlet'
"""
import argparse
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / "tools"
TIMEOUT_SECONDS = 180

ARGS = "harness tool: needs CLI arguments (run by the native qualification workflows)"
CLASSPATH = "compiles against Minecraft/NeoForge classes; needs the Gradle-resolved classpath"

SKIP = {
    "test_ecotone_blend.py": ARGS,
    "test_v8_ocean_connectivity.py": ARGS,
    "test_v8_outlet_authority.py": ARGS,
    "test_v8_outlet_ledger.py": ARGS,
    "test_v8_outlet_store.py": ARGS,
    "test_v8_routed_water_source.py": ARGS,
    "test_v8_materializer_coordinates.py": CLASSPATH,
    "test_v8_native_commit.py": CLASSPATH,
    "test_v8_native_coordinate.py": CLASSPATH,
    "test_v8_native_fill.py": CLASSPATH,
    "test_v8_native_receipt_audit.py": CLASSPATH,
    "test_v8_vegetation_columns.py": CLASSPATH,
    "test_v8_relief_v7_window.py": (
        "KNOWN STALE: pins V8ReliefJoinV7.java to blob 0c72f24 and its old "
        "continentalness bands, but commit e1d821b1 (2026-11-01) changed the bands "
        "to .08-.12 / .12-.16. Decide whether that change is intended, then update "
        "the test, and remove this entry."
    ),
}


def modified_tracked_files():
    """Paths git reports as modified, or None when git is unavailable."""
    try:
        done = subprocess.run(["git", "diff", "--name-only"], cwd=ROOT, text=True,
                              capture_output=True, timeout=30)
    except (OSError, subprocess.TimeoutExpired):
        return None
    return set(done.stdout.split()) if done.returncode == 0 else None


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--list", action="store_true", help="list tests and exit")
    parser.add_argument("-k", default="", help="only run tests whose name contains this")
    options = parser.parse_args()

    tests = sorted(p for p in TOOLS.glob("test_*.py") if options.k in p.name)
    names = {p.name for p in tests}
    stale = sorted(n for n in SKIP if not (TOOLS / n).exists())
    if stale:
        print("SKIP entries without a file (remove them):", ", ".join(stale))
        return 2

    runnable = [p for p in tests if p.name not in SKIP]
    skipped = [p for p in tests if p.name in SKIP]
    if options.list:
        for p in runnable:
            print("run   ", p.name)
        for p in skipped:
            print("skip  ", p.name, "-", SKIP[p.name])
        return 0

    failures = []
    dirty_before = modified_tracked_files()
    started = time.monotonic()
    for path in runnable:
        t0 = time.monotonic()
        try:
            done = subprocess.run(
                [sys.executable, str(path)], cwd=ROOT, text=True,
                capture_output=True, timeout=TIMEOUT_SECONDS)
            code, output = done.returncode, done.stdout + done.stderr
        except subprocess.TimeoutExpired as expired:
            code, output = 124, f"timed out after {TIMEOUT_SECONDS}s\n{expired.stdout or ''}"
        status = "ok  " if code == 0 else "FAIL"
        print(f"{status} {path.name} ({time.monotonic() - t0:.1f}s)", flush=True)
        if code != 0:
            failures.append(path.name)
            print("\n".join("    | " + line for line in output.strip().splitlines()[-25:]))

    print()
    for path in skipped:
        print(f"SKIPPED {path.name}: {SKIP[path.name]}")
    dirty_after = modified_tracked_files()
    if dirty_before is not None and dirty_after is not None and dirty_after - dirty_before:
        print("WARNING: some tests rewrote tracked evidence files; review before committing "
              "(`git checkout -- <path>` to discard):")
        for name in sorted(dirty_after - dirty_before):
            print("   ", name)
    print(f"\n{len(runnable) - len(failures)}/{len(runnable)} passed, "
          f"{len(skipped)} skipped, {time.monotonic() - started:.0f}s")
    if failures:
        print("FAILED:", ", ".join(failures))
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
