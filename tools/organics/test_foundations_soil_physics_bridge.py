#!/usr/bin/env python3
"""Compile the real portable bridge and its differential reference checks in isolation."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
source = root / "engine-core/src/main/java/com/shnewbs/deepprimal/core/organic/FoundationsSoilPhysicsBridge.java"
test = root / "src/coreChecks/java/com/shnewbs/deepprimal/core/organic/FoundationsSoilPhysicsBridgeChecks.java"
with tempfile.TemporaryDirectory(prefix="deepprimal-foundations-soil-") as output:
    subprocess.run(["javac", "-d", output, str(source), str(test)], check=True)
    subprocess.run(["java", "-cp", output,
                    "com.shnewbs.deepprimal.core.organic.FoundationsSoilPhysicsBridgeChecks"], check=True)
