# Dev178 — finite horizon sample coordinate admission (2026-11-02)

## Scope and coordination
- Parent at claim: `2abf1a6081bb35bc16d5b1f2d2d85fc0941fa718`; bounded claim `horizon-geometry-finite-2026-11-02/CLAIM.json`.
- Consumed independent dev177 audit: `docs/engine-hourly-audits/2026-11-02T0230-dev177/REVIEW.md`. Dev177's `HorizonService` telemetry allocation repair was not modified.
- Changed only `engine-core/.../HorizonGeometry.java`, the new `HorizonGeometryFiniteAdmissionChecks.java`, and its registration in `HorizonChecks.java`. No renderer-vulkan, lod-engine, organics, audit, native water, world-save or build files changed.

## Defect and correction
`HorizonGeometry.sample(short[], int, double, double)` interpolated a NaN coordinate into a NaN height, even though every captured short height is finite. A nonfinite X or Z now returns the first captured lattice height, avoiding nonfinite far-terrain geometry. This is a deterministic, bounded fallback; it does **not** establish the cause of any in-game LOD seam or flicker. Finite coordinates, including out-of-tile clamping, still use the original triangle-plane expression bit-for-bit.

## Reproducible isolated evidence
- OpenJDK 21: `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`.
- **480,135 assertions passed:** three detail tiers, 135 nonfinite-coordinate combinations, 450,000 randomized finite triangle-plane comparisons, and 30,000 exact lattice-anchor checks.
- Original production geometry blob `4c6f3b230fec14bad36eb377e44b51fc1f71f429` fails on the **first** invalid-X assertion; patched blob `c63dfc6058fc2147fa6aedd016406d7ac3575881` passes.
- Exact production `FarTerrain.java` dependency blob: `fd1a247028c9a4fda99422515438027b809ea2eb`.
- Committed regression blob: `9ecde9142c8f5b63a51a6336150f71a455f3e5c0`. Regression registered in `HorizonChecks.run()`.
- Local reproducibility bundle: `DeepPrimal_dev178_horizon_geometry_evidence.zip`, SHA-256 `cd411290df11d8ad00b68d00f57d5a1ca1bb5feafe29c670906f181f0dcddede`. Includes source, baseline, test, runner and logs.

## Limitations and next acceptance
Only the isolated production geometry and test classes were compiled; no complete Java25 NeoForge build, packaged JAR, native GPU test, in-game water/terrain test, or full core-suite run was possible. The local environment lacks a complete private checkout, JDK25, and resolved Gradle/NeoForge dependencies. Local GitHub Git/DNS is unavailable, but GitHub connector writes and remote blob checks succeeded. **No playable JAR or JAR SHA-256 exists for this cycle.** No older successful JAR was deleted; GitHub Actions were not used. Historical whole-engine grade remains 5.0/10 pending broad revalidation.
