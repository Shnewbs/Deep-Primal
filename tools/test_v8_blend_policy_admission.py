from pathlib import Path

src = Path("src/main/java/com/shnewbs/deepprimal/engine/V8BlendPolicy.java").read_text()
required = [
    "effectiveForSnapshot(int snapshotVersion",
    "if (snapshotVersion == 1) return savedPolicy == null ? HARD : savedPolicy;",
    "explicit V8 blend policy required for saved revision",
    "requireForSnapshotVersion(int snapshotVersion)",
    "incompatible V8 blend policy version",
]
missing = [needle for needle in required if needle not in src]
if missing:
    raise SystemExit("missing V8 policy admission guards: " + ", ".join(missing))
print("V8_BLEND_POLICY_ADMISSION_PASS")
