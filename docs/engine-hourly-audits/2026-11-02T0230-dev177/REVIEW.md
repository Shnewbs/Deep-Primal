# Dev177 — independent Deep Primal audit (2026-11-02 02:30 UTC)

## Review scope
Reviewed six commits from the prior Dev175 audit `870d8f2ac082683e1b911ac494ec460070f598f3` through Dev176 head `8310a0d9e4ee5d0bb2b8654d177eb5f36b8844f5`. Dev176 added defensive ownership of horizon height/color buffers and a registered 25,776-assertion core regression. Its writer lease was completed and released. No water solver, terrain, Vulkan, saved-world, or other engine module was changed by those six commits.

## Confirmed performance defect, corrected on main
`HorizonSamples.heights()` now returns a defensive clone, but three server telemetry-only sites in `src/main/java/com/shnewbs/deepprimal/render/HorizonService.java` still called `heights().length`, unnecessarily allocating copies to calculate a count. Commit [`46a55678`](https://github.com/Shnewbs/Deep-Primal/commit/46a55678a81cbdd1fe3fd3adcbb40ee1367cc9ce) replaces these with the already existing allocation-free `payloadBytes()` accessor:
- Sent-byte accounting: `sentBytes += data.payloadBytes()`.
- Both downsample reuse counters: `reusedSamples += ...payloadBytes() / 6L`.

The immutable packet still gets its necessary defensive copies; telemetry counts are mathematically unchanged. Verified patched GitHub blob: `af1dd0f2de62e8fe110edd5ef5cb0b9595ab17b4`.

## Independent local tests
Exact production GitHub blobs were independently reconstructed and verified with `git hash-object`: `HorizonSamples=8c0c22d5d67b6aaa3a805dbcebea8bda041c05c3`, `HorizonGeometry=4c6f3b230fec14bad36eb377e44b51fc1f71f429`, `FarTerrain=fd1a247028c9a4fda99422515438027b809ea2eb`, and `HorizonSamplesOwnershipChecks=f7b1e87f224dd36105effde3716e4cc6f60703b3`.

Strict `javac --release 21 -Xlint:all -Werror` passed. `java -ea -Xverify:all` passed **25,776 ownership assertions**. An independent OpenJDK21 allocation probe confirmed exact counter equivalence. For 100,000 paired telemetry reads (sent-count and reuse-count), old expressions allocated **1,694,400,000 / 440,000,000 / 120,000,000 bytes** at LOD detail 0/1/2 respectively; the new payload-only expressions allocated **0** in that isolated probe. This is **not** a measured in-game FPS or throughput improvement.

Local evidence ZIP: `DeepPrimal_Dev177_IndependentAudit_20261102T0230_Evidence.zip`, SHA256 `64aa04c3b55c68f61845c4360fa57d4d276c68695d324127d6e8dd9b7f002aeb`. Includes exact source, test, benchmark, logs, and diff.

## Acceptance and grades
- **Pass:** six-commit source review, reproduced allocation waste, three nonconflicting corrections on main, source hash verification, strict isolated Java21 tests.
- **Not run:** full Java25 NeoForge Gradle build, packaged JarJar check, game launch, native water/terrain, saved-world recovery, Vulkan/OpenGL, GPU/LOD frame pacing, 26.3/26.4 compatibility.
- Repository remains pinned to `neo_version=26.1.2.122`; Java25 and Gradle 9.2.1 configured. Minecraft 26.3 is released and 26.4 remains snapshots, but compatibility has **not** been established.
- **Scoped horizon metric correctness/performance:** provisional **8.5/10**. **Whole-engine:** historical **5.0/10**, not freshly regraded.
- **No playable replacement JAR or JAR SHA256 produced:** local environment lacks JDK25, complete private checkout and Gradle dependencies; local GitHub DNS fails. No prior JAR deleted; no GitHub Actions used.

Next: restore full Java25 checkout and dependency cache, build locally, verify JarJar and SHA256, then run water conservation, terrain-border, persistence and Vulkan/LOD acceptance. Preserve any existing successful JAR until a verified replacement exists.
