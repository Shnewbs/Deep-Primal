# Deep Primal Dev160 — water surface clock overflow admission

**Date:** 2026-11-01 UTC. **Baseline main:** `f6aa6a7b5f803e3d13c84e8b95c9a8f00ddd4869`. **Audit consumed:** [Dev159](../../../../docs/engine-hourly-audits/2026-11-01T1730-dev159/REVIEW.md).

## Defect and correction

`WaterSurfaceTransition` previously used signed `long` subtraction for animation age, remaining time, start offset, raw-sample settling and settled deadline arithmetic. With a start near `Long.MIN_VALUE` and an observation near `Long.MAX_VALUE`, subtraction wraps and an expired water surface can display its old height indefinitely. Near `Long.MAX_VALUE`, `now + 400` wraps and immediately retires a legitimate settling midpoint. Backward timestamps could also falsely trigger a settle.

A saturating, backward-safe elapsed helper now feeds `at`, `remaining`, `from`, `offset` and the versioned settle admission. Settle deadlines saturate at `Long.MAX_VALUE`; ordinary 100 ms animation and 400 ms settling are unchanged. This changes **presentation timing only**; no fluid volume, saved-world format, renderer shader or native Vulkan code was modified.

## Test evidence

- **Original defect reproduced:** the original production class fails at assertion 3, `overflow elapsed must finish`.
- **Patched source:** OpenJDK 21.0.12.1 isolated full-class compile with `javac -Xlint:all,-cast -Werror`, PASS **2,000,015** assertions, including 200,000 arbitrary-precision signed-long scenarios and 200,000 ordinary-domain parity steps.
- **Existing overflow regression re-run:** PASS **2,200,006** assertions, including 100,000 extreme finite endpoint scenarios and 1.1 million interpolation samples.
- **Negative controls:** 2/2 rejected: original signed overflow and a deliberately restored wrapping settle deadline.
- **Permanent check:** `src/coreChecks/java/com/shnewbs/deepprimal/core/WaterSurfaceClockAdmissionChecks.java`, registered in `CoreChecks`. The repository test uses the existing `BaselineWaterSurfaceTransition` fixture defined in `WaterSurfaceFiniteChecks.java`; the evidence ZIP includes an equivalent original-method fixture for isolated reproduction.
- **Source Git blob:** `fa508dd933578fd3fca630813135379f6c75c9f9`.
- **Regression Git blob:** `43ebb20b67d0b7e23ce6e39a182e34ebb1098c1e`.
- **Evidence ZIP SHA-256:** `85af93695d15dc3aadfcbfb826c618126d28cd9a5f213de96c74300bf0aeba19` (`DeepPrimal_dev160_surface_clock_evidence.zip`). Archive CRC checked.

## Scope, coordination, and remaining gates

Dev159's independent auditor changed `WaterCornerPacket` only. Dev158's water-flow clock lease was released; organics owns separate `SoilNutrientLedger` files. This scoped work avoids both. No GitHub Actions were run.

**Not a complete mod build:** this isolated container has Java 21, not the Java 25 required by `build.gradle`, and lacks the private full checkout, wrapper files and resolved NeoForge/Gradle dependencies. No playable NeoForge JAR or JAR SHA-256 was produced; previously successful JARs were not removed. Full `CoreChecks`, save/reopen conservation, real waterfall pixel stability and native Vulkan/LOD hardware acceptance remain open. The historical whole-engine grade of 5.0/10 is unchanged pending independent comprehensive review.
