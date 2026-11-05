"""Generate disposable native chunks and verify waterways plus legacy save compatibility.

Requires Java 25 and a prepared development runtime. Example:
python3 tools/validate_waterways.py ./gradlew --accept-eula
The caller must have accepted the Minecraft EULA before running this tool.
"""

import argparse
import csv
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]


def run(launcher, case, port, offline):
    legacy = case.startswith("legacy")
    deep = case == "deep"
    seed = 947291 if deep else 452343
    folder = "legacy" if legacy else case
    work = ROOT / ("run-waterways-check-" + folder)
    world = work / "world"
    if case == "legacy-reopen":
        if not (world / "level.dat").exists():
            raise RuntimeError("Run the legacy case first to create its saved world")
    elif world.exists():
        raise RuntimeError(f"Use a fresh test directory; preserved existing world: {world}")
    (work / "config").mkdir(parents=True, exist_ok=True)
    (work / "eula.txt").write_text("eula=true\n")
    (work / "server.properties").write_text(
        f"level-seed={seed}\nserver-port={port}\nserver-ip=127.0.0.1\n"
        "online-mode=false\nview-distance=4\nsimulation-distance=2\nmax-tick-time=-1\n"
    )
    (work / "config/deepprimal-worldgen.toml").write_text(
        f"extraUndergroundDepth = {str(deep).lower()}\n"
        f"riversAndCoasts = {str(case != 'legacy').lower()}\n"
    )
    log = work / (case + ".log")
    with log.open("w") as output:
        command = launcher + [
            "runServer", "--console=plain", "-PvisualValidation", "-PpackagedValidation",
            "-PrunDir=" + str(work),
        ]
        if offline:
            command.append("--offline")
        process = subprocess.Popen(
            command, cwd=ROOT, stdin=subprocess.PIPE, stdout=output,
            stderr=subprocess.STDOUT, text=True,
        )

        def wait(marker, seconds=180):
            deadline = time.monotonic() + seconds
            while True:
                content = log.read_text(errors="replace")
                if "AssertionError" in content or "Exception executing command" in content:
                    raise RuntimeError(f"Validation failed; inspect {log}")
                if marker in content:
                    return
                if process.poll() is not None or time.monotonic() > deadline:
                    raise RuntimeError(f"Missing {marker}; inspect {log}")
                time.sleep(0.5)

        def send(command):
            process.stdin.write(command + "\n")
            process.stdin.flush()

        try:
            wait('For help, type "help"', 360)
            if legacy:
                send("waterways legacy")
                wait("WATERWAY_LEGACY_PASS", 30)
            else:
                send("waterways map")
                wait("WATERWAY_MAP_PASS")
                directory = work / "build/waterways-validation" / str(seed) / str(-384 if deep else -128)
                with (directory / "targets.csv").open() as targets:
                    for point in csv.DictReader(targets):
                        if point["kind"] == "rough-coast":
                            continue
                        send(f"waterways verify {point['kind']} {point['x']} {point['z']}")
                        wait("WATERWAY_BLOCKS_PASS kind=" + point["kind"])
            send("save-all flush")
            send("stop")
            process.wait(timeout=120)
            if process.returncode != 0:
                raise RuntimeError(f"Server exited {process.returncode}; inspect {log}")
            print("WATERWAY_RUNTIME_PASS case=" + case, flush=True)
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=20)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("launcher", nargs="+")
    parser.add_argument("--accept-eula", action="store_true")
    parser.add_argument("--offline", action="store_true")
    parser.add_argument("--port", type=int, default=25587)
    parser.add_argument("--cases", nargs="+", choices=["standard", "deep", "legacy", "legacy-reopen"],
                        default=["standard", "deep", "legacy", "legacy-reopen"])
    args = parser.parse_args()
    if not args.accept_eula:
        parser.error("Read the Minecraft EULA and pass --accept-eula before starting the server")
    for case in args.cases:
        run(args.launcher, case, args.port, args.offline)
