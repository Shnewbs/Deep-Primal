# Dev166 — finite deposit elevation admission

**UTC:** 2026-11-01 21:06. **Baseline:** `97ac42a77a7984978f4ee64987fa386f7209a020` (Dev165 audit). **Scope:** bounded worldgen deposit compilation; excludes organics, renderer, LOD, native water, and independent audit files. **Actions:** not used.

## Reproduced defect and correction

A DEPTH-referenced deposit whose surface callback returns `NaN` previously compiled a body with `y=NaN`. The column bounding-height conversion narrowed NaN to integer zero, and the subsequent NaN distance/grade comparisons failed open, producing phantom ore at y=0. Infinite body elevations also occupied candidate slots unnecessarily.

`DepositPlan.chunk()` now skips a candidate if its **final** body elevation is not finite. This intentionally does not reject malformed but *unused* surface callback values for ABSOLUTE/GEOLOGICAL references. All finite worldgen body elevations retain their original path; no versioned geography or saved format is changed.

Production commit: [`bf869f3d`](https://github.com/Shnewbs/Deep-Primal/commit/bf869f3d735f22ffa4736ba5b63cb27c9c1f2ae1). Regression: [`c1cbe7b1`](https://github.com/Shnewbs/Deep-Primal/commit/c1cbe7b1902c095af2fb03d697d1116a2e0fd9e3). Core registration: [`6423a484`](https://github.com/Shnewbs/Deep-Primal/commit/6423a484ada31cb6180691630314bd5af473030f).

## Exact-source local validation

- Full `DepositPlan.java` blob: `5c62e5e16cd8805bdcf2fa98ddae28a661f77f3d`; prior blob: `9fc5da42607d17f55e17a7d8533dc8a611cda8c6`.
- Full regression source blob: `abffb7e14ae01eb3fa1f5bb2eef503f334ed9c49`; `CoreChecks.java` blob: `88800b3e8e79709ee9cb6b5cc669ee5e3d05418e`. Fetched from GitHub after push.
- `TerrainNoise.java` and `Geology.java` exact production blobs `cd107471d6700d7491b865a7dbec0e254af46df2` and `1315be8126bd0c0ce162124b5a1b1837542c5140`; only `TerrainEngine.Sample` was doubled for dependency-free compilation.
- Java21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` **passed 751,471 assertions**, including **740,880 finite output samples** (original/fixed digest `869f403583530ee1`) and **9,408 invalid-depth material checks**. Four deposit shapes, three vertical references, signed chunk coordinates and nonfinite height variants covered.
- **3/3 negative controls rejected**: original source generated NaN ore, NaN-only filtering retained infinite bodies, and an overbroad ground-height guard incorrectly suppressed ABSOLUTE/GEOLOGICAL deposits.
- The existing full `DepositPlanChecks` oracle was compiled from exact source, but did not finish within a 200-second local timeout; **not a passing check**. No full coreChecks or NeoForge build was run.

Reproducible local ZIP `DeepPrimal_dev166_deposit_finite_evidence.zip`, SHA-256 `5fb6484973926091ab821e69061ff0e64447a91d56d98d427513351c3bedeec5`, ZIP CRC verified. Contains exact sources, minimal type double, test, replay script, negative variants, compile/run logs, and environment diagnostics; kept out of GitHub binaries.

## Build blockers and next gates

The execution environment has only Java 21, no complete local private checkout, no Gradle executable/cached NeoForge dependencies, and cannot resolve github.com through local Git. No full Java25 NeoForge build, playable JAR, JAR SHA-256, native save/reopen, terrain visual test, Vulkan/LOD hardware benchmark or replacement artifact. No prior successful JAR was deleted.

Latest independent whole-engine baseline remains **5.0/10**, not regraded by this scoped change. Restore full local build infrastructure first; then run native worldgen/save/reopen, finite-water conservation, Vulkan/LOD acceptance and compare with the independent auditor. Do not mark the engine complete from isolated tests.
