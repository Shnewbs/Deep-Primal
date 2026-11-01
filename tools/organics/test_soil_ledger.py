#!/usr/bin/env python3
"""Compile and run the portable organic nutrient invariant suite without Gradle."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
source = root / "engine-core/src/main/java/com/shnewbs/deepprimal/core/organic/SoilNutrientLedger.java"
test = root / "src/coreChecks/java/com/shnewbs/deepprimal/core/organic/SoilNutrientLedgerChecks.java"
with tempfile.TemporaryDirectory(prefix="deepprimal-organic-") as output:
    subprocess.run(["javac", "-d", output, str(source), str(test)], check=True)
    subprocess.run(["java", "-cp", output,
                    "com.shnewbs.deepprimal.core.organic.SoilNutrientLedgerChecks"], check=True)
