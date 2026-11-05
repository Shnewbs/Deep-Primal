#!/usr/bin/env python3
"""Self-contained Java21 source checks and compiled broken controls for V8 outlet ports.

No Gradle, Minecraft, classpath doubles or native runtime is used. The runner
makes independent temporary compilation directories and does not run benchmarks.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java"
TEST = ROOT / "tools/tests/WatershedOutletTopologyChecks.java"
CLASS = "com.shnewbs.deepprimal.core.WatershedOutletTopologyChecks"


def validate_execution(name, run):
    if name == "positive":
        valid = (run.returncode == 0
                 and "WATERSHED_OUTLET_TOPOLOGY_PASS checks=" in run.stdout)
    else:
        valid = (run.returncode == 1
                 and re.search(r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                               run.stderr, flags=re.MULTILINE) is not None
                 and "\tat " + CLASS + "." in run.stderr)
    if not valid:
        raise RuntimeError(f"{name}: unexpected exit or missing fixture evidence "
                           f"{run.returncode}: {run.stdout} {run.stderr}")


def sha256(content):
    return hashlib.sha256(content).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--evidence", type=Path, help="optional JSON result and logs output")
    args = parser.parse_args()
    source, test = SOURCE.read_bytes(), TEST.read_bytes()
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin/java") if java_home else "java"
    javac = str(Path(java_home) / "bin/javac") if java_home else "javac"
    version = subprocess.run([java, "-version"], text=True, capture_output=True, check=True)
    mutations = {
        "unverified_water_as_ocean": (
            "if (node.port.verifiedOceanAnchor()) {",
            "if (node.port.floor() <= seaStage) {",
        ),
        "duplicated_routed_runoff": (
            "node.parent.discharge = Math.addExact(node.parent.discharge, node.discharge);",
            "node.parent.discharge = Math.addExact(node.parent.discharge, Math.multiplyExact(2, node.discharge));",
        ),
        "unknown_frontier_treated_as_closed": (
            "unknown |= at.port.unknownFrontier();",
            "unknown = false;",
        ),
    }
    results = {}
    with tempfile.TemporaryDirectory(prefix="deepprimal-outlet-test-") as temp:
        for name in ("positive", *mutations):
            directory = Path(temp) / name
            directory.mkdir()
            text = source.decode("utf-8")
            if name != "positive":
                old, new = mutations[name]
                if text.count(old) != 1:
                    raise RuntimeError(f"broken-control anchor missing or duplicated: {name}")
                text = text.replace(old, new)
            src = directory / SOURCE.name
            src.write_text(text, encoding="utf-8")
            command = [javac, "--release", "21", "-Xlint:all", "-Werror",
                       "-d", str(directory), str(src), str(TEST)]
            compiled = subprocess.run(command, text=True, capture_output=True)
            if compiled.returncode:
                raise RuntimeError(f"{name} Java21 compilation failed: {compiled.stderr}")
            run = subprocess.run([java, "-ea", "-Xverify:all", "-cp", str(directory),
                "com.shnewbs.deepprimal.core.WatershedOutletTopologyChecks"],
                text=True, capture_output=True)
            validate_execution(name, run)
            results[name] = {"exit": run.returncode,
                             "accepted": name == "positive",
                             "output": (run.stdout + run.stderr)[:4000],
                             "compiled_sha256": sha256((directory / "com/shnewbs/deepprimal/core/WatershedOutletTopology.class").read_bytes())}
    report = {"schema": 1, "test_kind": "isolated Java21 real production helper; no Minecraft/Gradle/GPU",
              "java_version": version.stderr.strip(),
              "source_sha256": sha256(source), "test_sha256": sha256(test),
              "source_path": str(SOURCE.relative_to(ROOT)),
              "test_path": str(TEST.relative_to(ROOT)),
              "results": results,
              "native_gates": False, "full_build": False,
              "finite_water_acceptance": False, "v8_gameplay_active": False}
    if args.evidence:
        args.evidence.parent.mkdir(parents=True, exist_ok=True)
        args.evidence.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"source_sha256": report["source_sha256"],
                      "test_sha256": report["test_sha256"],
                      "java_version": version.stderr.strip().splitlines()[0],
                      "results": {k: v["exit"] for k,v in results.items()}}, indent=2))


if __name__ == "__main__":
    main()

