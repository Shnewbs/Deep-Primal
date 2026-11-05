#!/usr/bin/env python3
"""Compile and run the actual portable species catalog sources without game dependencies."""
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SOURCES = [
    ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/organic/SpeciesPhysiology.java",
    ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/organic/SpeciesCatalog.java",
    ROOT / "src/coreChecks/java/com/shnewbs/deepprimal/core/organic/SpeciesCatalogChecks.java",
]
MAIN = "com.shnewbs.deepprimal.core.organic.SpeciesCatalogChecks"


def main():
    javac = shutil.which("javac")
    java = shutil.which("java")
    if javac is None or java is None:
        raise RuntimeError("Java compiler and runtime are required")
    with tempfile.TemporaryDirectory(prefix="deep-primal-species-catalog-") as directory:
        output = Path(directory)
        subprocess.run(
            [javac, "--release", "21", "-Xlint:all", "-Werror", "-d", str(output),
             *(str(source) for source in SOURCES)],
            check=True,
        )
        result = subprocess.run(
            [java, "-ea", "-Xverify:all", "-cp", str(output), MAIN],
            check=True, text=True, capture_output=True,
        )
        print(result.stdout, end="")


if __name__ == "__main__":
    main()
