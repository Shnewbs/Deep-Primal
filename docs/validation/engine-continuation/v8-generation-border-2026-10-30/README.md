# Dev116 — explicit V8 generation-border admission (source-only)

Source base: `ba9479935848f51ed75909f1a25b29ec9c260efe` (current main at publication preflight). Latest completed independent audit consumed: `docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md`, pinned `c57d2924562a7dd35eed007a164205953bb13061`. Its grade is a **carried** 50/100, not a fresh regrade. Full Java25 DensityGraph V5/V7 VM and native gates remain open.

## Actual implementation

Added `V8GenerationBorderAdmission` as a dependency-free, immutable four-side witness validator. A generated neighboring chunk must carry the same saved V8 profile fingerprint; an ungenerated neighbor must have no identity; unknown, legacy/zero-identity, edited, missing, duplicate, wrongly located, or overflowed observations fail closed. Signed chunk coordinates and order-independent N/E/S/W witnesses are checked without generating/loading neighbors.

Added opt-in fingerprint-and-border-gated overloads to `DensityChunkMaterializer.fillSavedSnapshot` and `fillSavedEcotoneVegetatedWithReceipt`. Both validate the saved profile and all four neighbor witnesses before delegating to existing native materialization. Earlier first-generation and fingerprint-only overloads are preserved. No V8 codec registration, no silent old-save migration, no old chunk rewrite, no geometry blur, no water-height/volume/constituent change and no renderer ownership changes. The owner must still provide authoritative persisted neighbor observations and native old/new-generation border acceptance; **this is a fail-closed admission step, not solved seam geometry**.

## Executed validation

Linux OpenJDK `javac 21.0.12.1`, `--release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. The exact new production helper blob `1b5ae577fc7f57fcff85c21a0ac67735fcafd349` and fixture blob `ce46a8e8ed374e5ab0b4a4a020f1d8dbf8780b15` were compiled and executed locally. Positive checks: **8,102 assertions per process**, normal and `python3 -OO` runners, 16,204 total. Both compiled negative variants (generated-neighbor identity comparator reversed) were rejected. Reproduce: `python3 tools/test_v8_generation_border_admission.py` and `python3 -OO tools/test_v8_generation_border_admission.py`.

The runner statically checks the native adapter anchors in a full checkout; isolated local execution printed `NATIVE_ADAPTER_SOURCE_NOT_PRESENT` because the full repository is not available in this runtime. GitHub post-commit source inspection is required separately; **the complete native materializer did not compile in this run**. Source-only tests do not establish real Minecraft, persistence freshness, GPU, performance, cross-version seam acceptance or full water-engine completion.

Owner retains full Gradle/Minecraft/GPU builds and JAR publication. No hosted Actions, full build, native acceptance, qualified JAR or release was performed. Next: persist/verify authoritative per-chunk generation revision and profile identity; supply neighbor witnesses at actual native new-generation boundary; test signed native chunk/base-column/heightmap parity and old/new border transitions; continue V5/V7 VM and finite-water authority work.
