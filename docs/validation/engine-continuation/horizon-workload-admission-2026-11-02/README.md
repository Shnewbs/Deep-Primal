# Dev180 — horizon workload and camera telemetry admission (2026-11-02)

## Scope / coordination
- Claim parent: `19adbc7b3d1331015dcb8fa541adcc2981ac0c08`.
- Bounded scope: `engine-core/.../HorizonView.java`, registered `HorizonViewSafetyChecks.java`, and this validation folder.
- The prior shared writer lease was `completed-released`. Independent audit, organics, native water, renderer-vulkan and lod-engine writer-owned files were not modified.
- No GitHub Actions used.

## Defect and correction
`HorizonView.create` treated finite zero and negative workload fractions as valid, clamping them to 0.125 and allowing expensive fine/medium LOD requests despite a nonpositive work budget. It also retained NaN/infinite camera origins with nonzero LOD thresholds. This is malformed telemetry admission, not a claim of measured GPU flicker.

The guard now fails closed to zero detail thresholds for nonpositive/nonfinite workload, invalid FOV/viewport or invalid camera X/Z. Invalid camera origins are normalized to finite zero. Every ordinary **positive, finite** workload retains the original projection and threshold arithmetic. No water quantity, world generation, saved format or shader changes.

## Evidence
- Exact source baseline Git blob: `26eba18ff8f38b71ad886a2359fc4b41943a106e`.
- Patched source Git blob: `3388e0a02cb26cd58719652a9807a04d8a9021cc`.
- Updated registered safety-test Git blob: `0790005b805ce234e2c306da5c44b31c02691134`.
- OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: PASS, exact production HorizonView and registered test with a source-equivalent FarTerrain dependency.
- Registered `HorizonViewSafetyChecks`: **100,043 checks**, all three detail tiers covered; randomized workload parity now exercises strictly positive values.
- Independent deterministic workload/invalid-camera harness: **200,102 assertions** passed, including 200,000 positive finite view comparisons.
- Original production with updated registered safety test fails on invalid workload. Original production with independent harness fails at assertion 1. Both negative controls were compiled and run, not inferred.
- Local reproducibility archive `DeepPrimal_dev180_horizon_workload_evidence.zip`, SHA-256 `1eeaa38a0f12bad1ee04ce3cc2fdda77e9420aace21c05f1242574ebc2f1cdbb`. ZIP integrity checked; source, baseline, tests, runner and logs included; no compiled binaries in GitHub.

## Limitations
This is a **Java21 isolated** engine-core regression, not full Java25 NeoForge build, full `coreChecks`, Vulkan GPU test, terrain/LOD seam test, or playable release. Local GitHub DNS, complete private checkout, Java25 and resolved Gradle/NeoForge dependencies remain unavailable. No replacement playable JAR or JAR SHA-256 was produced; previous successful JARs were not deleted. Whole-engine historical grade 5.0/10 is unchanged pending comprehensive acceptance.
