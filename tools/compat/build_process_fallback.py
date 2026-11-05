#!/usr/bin/env python3
"""Build/check the optional private NeoForm 2.0.31 compatibility agent; never a mod artifact."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", type=Path, required=True)
    parser.add_argument("--asm-jar", type=Path, required=True)
    parser.add_argument("--neoform-jar", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    sources = Path(__file__).resolve().parent
    output = args.output.resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    asm = output.parent / "asm-9.9.1.jar"
    if args.asm_jar.resolve() != asm:
        if asm.exists() and asm.read_bytes() != args.asm_jar.read_bytes():
            raise ValueError("Refusing to overwrite a different private ASM dependency")
        shutil.copyfile(args.asm_jar, asm)
    with tempfile.TemporaryDirectory(prefix="deepprimal-build-compat-") as directory:
        classes = Path(directory)
        classpath = str(asm) + os.pathsep + str(args.neoform_jar.resolve())
        subprocess.run([str(args.java_home / "bin/javac"), "--release", "21", "-cp", classpath, "-d", str(classes),
                        str(sources / "ProcessCommandFallback.java"),
                        str(sources / "ProcessCommandFallbackChecks.java")], check=True)
        with zipfile.ZipFile(output, "w") as archive:
            archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n"
                            "Premain-Class: com.shnewbs.deepprimal.buildcompat.ProcessCommandFallback\n"
                            "Class-Path: asm-9.9.1.jar\n\n")
            for source in classes.rglob("ProcessCommandFallback*.class"):
                if "Checks" not in source.name:
                    archive.write(source, source.relative_to(classes).as_posix())
        run = subprocess.run([str(args.java_home / "bin/java"), "-javaagent:" + str(output),
                              "-cp", str(classes) + os.pathsep + classpath,
                              "com.shnewbs.deepprimal.buildcompat.ProcessCommandFallbackChecks",
                              str(args.neoform_jar.resolve())], text=True, capture_output=True)
    report = {"scope": "private-build-tool-compatibility", "exit_code": run.returncode,
              "stdout": run.stdout, "stderr": run.stderr,
              "agent_sha256": hashlib.sha256(output.read_bytes()).hexdigest(),
              "neoform_jar_sha256": hashlib.sha256(args.neoform_jar.read_bytes()).hexdigest(),
              "shipped_in_mod": False}
    output.with_suffix(".checks.json").write_text(json.dumps(report, indent=2) + "\n")
    print(run.stdout, end=""); print(run.stderr, end="")
    return run.returncode


if __name__ == "__main__":
    raise SystemExit(main())
