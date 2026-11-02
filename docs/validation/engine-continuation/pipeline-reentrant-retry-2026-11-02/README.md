# Dev198 — worldgen completion retry and production-shaped CoreChecks repair (2026-11-02)

**Base:** `d9741199024840d983f6ba0ac31ce7a17ef0d5eb`. **Coordination claim:** `c4cd25cfc5487ef3cfbe7746b1a8ec494f41a991`. **Audit consumed:** [Dev197 independent review](../../../../engine-hourly-audits/2026-11-02T1230-dev197/REVIEW.md).

## Correctness fix

`WorldGenPipeline.settle` previously invoked completion listeners while the completed request remained in `pending`. A synchronous failure handler retrying the same key could coalesce with the already-failed future rather than dispatch a new attempt. The request is now retired **after the successful OPEN→SETTLING CAS but before** user notification, outside the completion callback's monitor. Shutdown still wins if it sets CANCELLED before the CAS; it does not cancel a request already settled.

The existing key and shutdown `CoreChecks` were written against earlier isolated stub constructors (`new ChunkCache()`, `new ChunkMaterializer(Function)`, `new MaterializedChunk(key, bytes)`) rather than actual production types. The tests now use a package-private injected async dispatch seam, bounded real `ChunkKey` coordinates, `ChunkCache(1,64)`, and `PipelineTestFixtures` with actual production record shapes. The new reentrant regression is registered in `CoreChecks`. No saved-world data, water volume, terrain formulas, Vulkan/LOD source, renderer or organics files changed.

## Local evidence

- Java 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **110,000** key admission, **60,000** shutdown, **90,000** reentrant retry, and **6,000** deterministic shutdown-race assertions passed (**266,000 total**).
- The historical post-notification pending-removal order fails the new synchronous-retry test as expected.
- The compiled production `WorldGenPipeline.java` is the exact candidate source. Local isolated harnesses use clearly labeled API-shaped stand-ins for `ChunkMaterializer`, `ChunkCache` and density data types. These are **not** full engine or NeoForge dependencies.
- Evidence archive: `DeepPrimal_dev198_pipeline_retry_evidence.zip`; SHA-256 `aa9f223ceb13adbfe2d448d21dc6dd51c1a94bb9791566d5afc03439727b35f9`. Includes rerunnable positive/negative and shutdown-race harnesses, source, logs, and SHA-256 manifest.

## Remaining gates

Full Java 25 Gradle `coreChecks`, `check`, `jar`, native save/reopen, water conservation, Vulkan/OpenGL GPU acceptance and a playable JAR were **not run**. This environment has Java 21, no complete private checkout and no resolved Gradle/NeoForge dependency cache. The test suite's other production API bindings require full compilation. No GitHub Actions or JAR publication; retain the last successful JAR until a verified replacement exists. The whole-engine grade remains the historical **5.0/10**, not regraded by these isolated assertions.
