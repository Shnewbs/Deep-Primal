# Deep Primal dev170 — malformed water-surface UV admission (2026-11-01)

Consumed independent audit: `docs/engine-hourly-audits/2026-11-01T2230-dev169/REVIEW.md`. Baseline `eac7a372b24351f6ec6270d4ee8899e69eb4d8aa`. Writer claim `44600d8054721c5bc9b01f0c8e736ca06d7b9a7d`.

## Defect and fix

`WaterTransport.surface()` clamps UVs with `Math.clamp`, which leaves `NaN` unchanged. With otherwise finite corner heights, a malformed NaN U or V propagated a NaN water vertex. Reproduced on the exact baseline production source before modification. Undefined UVs now use the cell midpoint (0.5); ordinary finite UVs and infinity clamping retain their previous behavior. This is rendering geometry only, not fluid volume, chemistry, terrain revision or saved data. It does not establish that natural waterfall flicker is fixed.

## Validation

Java 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` on the complete dependency-free production WaterTransport class, with a minimal test double containing the exact production `TerrainNoise.lerp` formula:

- New `WaterSurfaceUvAdmissionChecks`: **1,000,481 assertions passed** (NaN U/V/both midpoint parity, finite UV bit-exact parity, extreme finite corner matrix, water-volume invariants).
- Existing `WaterSurfaceConvexAdmissionChecks`: **1,933,122 assertions passed**.
- **2,933,603 total passing isolated assertions**.
- Baseline negative control failed with `AssertionError: NaN UV poisoned finite corners`.
- Original source Git blob: `49d5709cd209fd901501fb0f0afd5c098b62d829`.
- Corrected source expected Git blob: `cfc0993fe503fbbe02356b42fbe0f1a0afc0fe23`.
- New regression expected Git blob: `f650391c32b2c02627c0e9218f550e708ee9cd4b`.

## Unaccepted gates

Full Java25/Gradle 9.2.1/NeoForge build, complete private repository checkout, native water save/reopen, Vulkan/OpenGL GPU readback, natural cliff temporal pixels, LOD and hardware performance remain unverified in this run. Local environment has only Java21 and cannot resolve GitHub through Git; no complete playable JAR or JAR SHA256 produced. Existing successful JARs must be retained. No GitHub Actions used. Historical whole-engine grade remains 5.0/10.
