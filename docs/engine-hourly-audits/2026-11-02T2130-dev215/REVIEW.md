# Dev215 independent audit — 2026-11-02 21:30 UTC

## Commit review
Compared Dev213 head `82161f5e2014a917dec489f1ddd3e7a1555869bf` with current `main` `6d3dfca42b2f2996aba81513b485a290ebb08685`: **8 commits ahead, 0 behind, 5 files changed**. Dev214 introduced one production change: a three-line full-parcel fast path in `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterAccount.java`. The other changes were test registration, evidence, writer claim/release and an audit link correction. No terrain, LOD, Vulkan or NeoForge build changes in this delta.

Pinned exact production blob `0b38c08342182152af0397f3102ecd8f84e9b951`, baseline blob `3e18f72b42f32569fd7fa2c7c3751af12508dee2` and registered regression blob `b2e5c1a20fa05eacb5d1e625c5459f81e387a40d`. The locally reconstructed sources match those Git blob hashes.

## Independent Java 21 evidence
- Strict `javac -Xlint:all -Werror` and `java -Xverify:all` succeeded.
- Registered `WaterAccountFullParcelChecks`: **700,000 assertions passed**, digest `79e36dba79042795`.
- Registered `WaterAccountDebitChecks`: **150,000 assertions passed**.
- New independent `Dev215WaterAudit`: **1,230,003 assertions passed**, digest `d155ed8b21dc672f`. Differential BigInteger fraction oracle, signed request extremes, take/move/distribute, evaporation, water/salt/sediment conservation and rejected admission atomicity.
- **2,080,003 current-source assertions passed** across those three suites. Baseline full-parcel and independent oracles passed with identical digests.
- Injected `mass - 1` full-volume mutant failed registered test at assertion 1.
- Isolated thread-allocation probe (100k warmup + 5 x 100k samples): **median 704 bytes per full preview baseline, 40 bytes candidate** (~94.3% reduction). This is NOT a native simulation or FPS benchmark.
- No additional production defect confirmed; no speculative production changes.

## Build and compatibility limitations
- `gradle.properties` pins NeoForge `26.1.2.122`; root Gradle build requests Java 25.
- This local runtime has OpenJDK 21.0.12.1 only; full private checkout, `gradlew`, Gradle executable, resolved NeoForge dependencies and native game runtime unavailable. Direct `git ls-remote` failed DNS.
- **No full mod compile, playable JAR, JAR SHA256, client/server launch or native Vulkan/LOD/terrain/water save-reopen test.** Previous audit JARs preserved; no GitHub Actions.
- Minecraft 26.4 Snapshot 1 (2026-09-22) changed `Default` graphics preference to Vulkan and removed automatic API switching after startup crashes. This requires actual 26.4 compatibility validation, not inference. Official notes: https://feedback.minecraft.net/hc/en-us/articles/49076034811533-Minecraft-Java-Edition-26-4-Snapshot-1

## Grades and next gates
- **8.5/10 scoped WaterAccount exactness/conservation**: registered + independent tests and mutation detection; native integration not verified.
- **8.0/10 scoped allocation optimization**: isolated allocation measurement only.
- **2/10 local release-validation readiness**: cannot produce/test the full mod JAR in this runtime.
- **Whole-engine not freshly graded**; historical 5.0/10 is not a current measurement.

Next: restore full Java 25 checkout and NeoForge dependency cache; run local `./gradlew clean check jar`, verify JarJar and JAR SHA256; then client/server startup, water persistence/conservation, waterfall seams, terrain continuity, Vulkan/OpenGL fallback, LOD FPS/tick benchmarking. Delete superseded audit JAR only after replacement is fully verified.

Reproducible local evidence ZIP: `DeepPrimal_Dev215_IndependentAudit_20261102T2130_Evidence.zip`, SHA256 `3f2fa2b015c2ea7416e531ceb45fba8db0b86d7611cef25cd3d69ca37655638d`.
