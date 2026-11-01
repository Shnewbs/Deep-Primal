# Deep Primal Dev150 — independent audit (2026-11-01 11:30 UTC)

## Baseline and changed files
Previous audit head: `8e6005a358379f99cfe91b23b201d5400e97e4c1`.
Audited Builder head: `2380e61dd1a968ecd5678bf2243eab447c93120f`.
GitHub compare reports **7 commits ahead**, no divergence, 6 changed paths: `WaterSimulation.java`, new `WaterSimulationMaterialAdmissionChecks.java`, `CoreChecks.java`, and 3 files under `docs/validation/engine-continuation/water-query-material-admission-2026-11-01/`.

Production fix: `013f5b92085ea62366d09152fd9e5de1c48fda38`.
Exact production blob observed: `a1bf3e859ecfed046504ef0970c5f761776b8c65`.
Regression blob observed: `cec848f5beb6b538c67aa6c45e0e7aec2f90139b`.

## Independent source review
The new `setMaterial` guard rejects dry water material (`units[i]==0 && material==1`) before state mutation. Existing wet-material validation remains in place. Converting a dry cell to solid clears its source flag. `fromChunkData` passes imported materials through `setMaterial`, so the new guard covers the query import path as intended. The test is registered in `CoreChecks` and a reproducible Java21 runner is present.

This is a **query-water** correction, not a change to live `WaterRuntime` or saved-world water. No unrelated production edits were made during this audit.

## Independent reproduction
Local JDK: OpenJDK `21.0.12.1`. A **verbatim method-body excerpt** from the fetched `setMaterial` source was compiled with `javac --release 21 -Xlint:all -Werror` and executed with `java -ea -Xverify:all` against an independently written state-transition oracle. It passed **862 assertions** covering reachable wet/dry/solid states, source flags, valid and invalid material IDs, exception types, no mutation after rejection, and volume preservation.

Negative control: removing the new dry-water guard from the excerpt fails at `units=0, before=0, target=1` with `AssertionError`, exit code 1. **This was not a full production-class compile or the Builder's 1,301,352-assertion runner rerun**; those results remain Builder-reported, not independently executed here.

Evidence ZIP: `DeepPrimal_Dev150_IndependentAudit_20261101T1130_Evidence.zip`, 3,173 bytes, SHA-256 `63a2772e4af8357568d32bb0455b3182d29a88e32f3d682cd8cb5131a2216bc3`; ZIP integrity verified. The archive contains excerpt harness, passing log, negative-control failure, environment diagnostics, and scope manifest.

## Findings and grades
- **Query-water material admission: provisional 8.0/10** — guard placement, exception behavior and isolated excerpt oracle passed; full class/runtime not exercised.
- **Whole engine: 5.0/10 historical baseline only, not freshly graded**. No evidence to raise the engine-wide score.
- **Renderer, terrain, LOD, Vulkan/OpenGL, FPS, water persistence:** no changed production files in these subsystems in this comparison; native acceptance remains untested.
- **NeoForge compatibility:** `gradle.properties` still pins `neo_version=26.1.2.122`; `build.gradle` specifies Java 25, Gradle wrapper 9.2.1. NeoForge's 26.3.x source specifies Java 25. 26.3/26.4 compatibility not demonstrated.

## Build and retention
Full checkout, JDK25, Gradle executable/caches, and GitHub DNS via Git CLI are absent from the isolated local environment. Therefore no full Gradle/NeoForge compile, client/server startup, or **new verified playable JAR/SHA** was produced. No superseded audit JAR was deleted. No GitHub Actions were run.

**Disposition:** Builder's fix accepted for scoped source-level review. No independently confirmed new production defect or nonconflicting correction this round. Next acceptance priority is obtaining a complete local checkout/JDK25/dependency cache and running the actual `coreChecks`, packaging, native water/save-reopen and Vulkan/LOD benchmarks before claiming a release.
