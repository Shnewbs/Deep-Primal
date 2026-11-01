# Deep Primal Dev161 — independent hourly audit

**Date:** 2026-11-01 18:30 UTC (10:30 PST). **Previous audit:** `f6aa6a7b5f803e3d13c84e8b95c9a8f00ddd4869`. **Pre-audit main:** `26cf736a577ef56439eb8252e3e491d13973845a`.

## Commit and scope review

The GitHub compare endpoint reports **7 commits ahead, 0 behind**, covering the Dev160 water surface clock fix, regression registration, fixture reuse, validation notes and completed writer lease. Changed production path: `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterSurfaceTransition.java`; test paths: `src/coreChecks/java/com/shnewbs/deepprimal/core/WaterSurfaceClockAdmissionChecks.java` and `CoreChecks.java`. The Dev160 claim is `completed-released`; no overlapping engine writer was identified at the inspected head. Organics owns separate files.

Reviewed the exact production source at Git blob `fa508dd933578fd3fca630813135379f6c75c9f9`, verified locally by `git hash-object`. The signed-long elapsed helper, saturating settle deadline, animation offset, versioned admission and regression registration are internally consistent in the tested domain. No additional confirmed production defect emerged; **no production source was modified**.

## Independent reproduction and verification

- OpenJDK 21.0.12.1: strict `javac -Xlint:all -Werror` compilation passed for the exact production class and independent Java test.
- **PASS 1,900,000 assertions:** 300,000 independent arbitrary-precision signed-long clock scenarios (frame, remaining, offset and source), 100,000 midpoint-settling deadline scenarios and 100,000 revision/invalid-input scenarios.
- Independent negative control replaced the overflow-saturating subtraction with naive signed subtraction: compilation passed and the test **failed as expected at assertion 9**.
- The existing repository's full `CoreChecks` and Dev160 2m suite were inspected but **not rerun** here. This is isolated source-level verification, not live rendering acceptance.
- Local evidence: `DeepPrimal_Dev161_IndependentAudit_20261101T1830_Evidence.zip`, SHA256 `2ff5661d7c33f20b6342432f2e30346a4a884362aca162741484f01fc854d095`; archive integrity verified. Includes exact production source, independent test, passing log, mutation and expected failure, environment diagnostics.

## Build and compatibility gates

The repository pins NeoForge `26.1.2.122`, Gradle wrapper `9.2.1` and Java toolchain `25`. Official NeoForge 26.1 guidance requires Java25 and Gradle >=9.1. These configuration requirements are consistent, but **no full dependency resolution or runtime compatibility was tested**. NeoForge 26.3/26.4 compatibility remains unverified.

This isolated environment has **Java21 only, no JDK25, no Gradle installation/cache, no full local checkout, and no Git CLI DNS resolution to github.com**. Therefore a complete local NeoForge build cannot run. **No fresh playable JAR or JAR SHA256 was produced.** Existing audit JARs were not removed (none were present in the inspected local /mnt/data). No GitHub Actions were invoked.

Native finite-water conservation, save/reopen, terrain seams, renderer/Vulkan/OpenGL, LOD performance and FPS benchmarks remain untested. Do not interpret isolated correctness as a passing whole-engine build.

## Grade and next acceptance

**Scoped surface clock arithmetic: 8.5/10 provisional**, based on exact-source independent tests and negative control. **Whole-engine: 5.0/10 historical baseline, not freshly regraded**. Highest priority: obtain a complete checkout, JDK25 and resolved Gradle dependencies, build locally, verify the playable JAR and SHA256, then only replace the older audit JAR after successful validation. Next run live water persistence/conservation and Vulkan/LOD hardware acceptance.
