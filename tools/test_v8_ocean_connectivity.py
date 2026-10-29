#!/usr/bin/env python3
"""Reproduce the dev69 V8 stitched-marine source checks, not native Minecraft.

Point --fixture-root at extracted isolated evidence, or --bundle at its ZIP. A
--checkout validates the three changed production/test files against the actual
Git checkout; omitting --checkout records the identity gate as unverified.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
from tempfile import TemporaryDirectory
import zipfile

IDENTITIES = {
    "src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java":
        "source/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java",
    "src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java":
        "source/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java",
    "tools/tests/V8OceanConnectivityChecks.java":
        "tests/V8OceanConnectivityChecks.java",
}
TARGET = "com.shnewbs.deepprimal.engine.V8OceanConnectivityChecks"


def require(condition: bool, explanation: str) -> None:
    if not condition:
        raise RuntimeError(explanation)


def source_sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def invoke(args: list[str], timeout: int = 180) -> subprocess.CompletedProcess[str]:
    return subprocess.run(args, capture_output=True, text=True, check=False, timeout=timeout)


def verify_fixture(fixture: Path, checkout: Path | None, receipt: dict) -> None:
    fingerprints = {}
    for actual, within in IDENTITIES.items():
        fixture_path = fixture / within
        require(fixture_path.is_file(), f"missing isolated file {within}")
        data = fixture_path.read_bytes()
        fingerprints[actual] = {"sha256": source_sha(data), "bytes": len(data)}
        if checkout is not None:
            source = checkout / actual
            require(source.is_file(), f"missing current main source {actual}")
            require(source.read_bytes() == data, f"source drift {actual}")
    receipt["source_files"] = fingerprints
    receipt["checkout_identity_checked"] = checkout is not None


def run(fixture: Path, checkout: Path | None) -> dict:
    receipt: dict = {"kind": "Java21 source-only / isolated Minecraft API doubles",
                     "native_watershed": False, "native_minecraft": False,
                     "java25_full_build": False, "real_save_reopen": False,
                     "gpu_test": False, "jar_produced": False}
    verify_fixture(fixture, checkout, receipt)
    with TemporaryDirectory(prefix="dev69-stitch-replay-") as temporary:
        classes = Path(temporary) / "classes"
        src = sorted((fixture / "source").rglob("*.java"))
        doubles = sorted((fixture / "api-doubles").rglob("*.java"))
        checks = fixture / "tests/V8OceanConnectivityChecks.java"
        require(bool(src) and bool(doubles), "missing isolated source or API doubles")
        compile_command = ["javac", "--release", "21", "-Xlint:all", "-Werror",
                           "-d", str(classes), *map(str, src + doubles + [checks])]
        compiled = invoke(compile_command)
        require(compiled.returncode == 0, "Java21 component compile failed: " + compiled.stderr[-1500:])
        command = ["java", "-ea", "-Xverify:all", "-cp", str(classes), TARGET]
        positive = [invoke(command) for _ in range(2)]
        for result in positive:
            require(result.returncode == 0 and "V8_STITCHED_MARINE_PASS" in result.stdout,
                    "V8 marine check failed: " + result.stderr[-1500:])
        require(positive[0].stdout == positive[1].stdout, "separate JVM replays disagree")
        count = re.search(r"checks=(\\d+)", positive[0].stdout)
        require(count is not None, "missing component assertion receipt")
        receipt["component_assertions_per_jvm"] = int(count.group(1))
        receipt["java21_compile_sources"] = len(src) + len(doubles) + 1
        receipt["positive_replays"] = 2
        receipt["positive_stdout"] = positive[0].stdout.strip()
    negative = fixture / "mutation_checks_dev69.py"
    require(negative.is_file(), "missing compiled negative controls")
    mutated = invoke([sys.executable, "-OO", str(negative)], 240)
    require(mutated.returncode == 0 and "DEV69_MUTATION_PASS controls=3" in mutated.stdout,
            "compiled negative control failure: " + (mutated.stdout + mutated.stderr)[-1500:])
    receipt["negative_compiled_controls_rejected"] = 3
    receipt["python_optimized_controls"] = True
    receipt["all_scoped_checks_passed"] = True
    return receipt


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    files = parser.add_mutually_exclusive_group(required=True)
    files.add_argument("--bundle", type=Path)
    files.add_argument("--fixture-root", type=Path)
    parser.add_argument("--checkout", type=Path)
    ns = parser.parse_args()
    try:
        with TemporaryDirectory(prefix="dev69-bundle-") as temp:
            if ns.bundle is not None:
                target = Path(temp)
                with zipfile.ZipFile(ns.bundle) as archive:
                    require(archive.testzip() is None, "bundle CRC failure")
                    for item in archive.infolist():
                        dest = (target / item.filename).resolve()
                        require(dest.is_relative_to(target) and not item.filename.startswith("/"),
                                "unsafe evidence archive path")
                    archive.extractall(target)
            else:
                target = ns.fixture_root.resolve()
            receipt = run(target, ns.checkout.resolve() if ns.checkout else None)
            print(json.dumps(receipt, indent=2))
        return 0
    except (OSError, RuntimeError, ValueError, subprocess.TimeoutExpired, zipfile.BadZipFile) as error:
        print(json.dumps({"all_scoped_checks_passed": False, "error": str(error)}, indent=2))
        return 1


if __name__ == "__main__":
    sys.exit(main())
