# Dev153 — independent Deep Primal hourly audit

**Date:** 2026-11-01 14:30 UTC. **Prior audit head:** `b7da8b0c029555620ba886f3dc96b538094a0511`. **Builder head reviewed:** `471a11d4e6e4b93bb876d7640da9d34727587fa7`. **New commits:** 3 (claim, water packet correction, claim release). **Scope:** GPU water-corner packet safety, regression coverage, NeoForge packaging gates, adjacent engine/render/terrain/LOD acceptance.

## Source review and confirmed correction

Builder corrected malformed 5-bit GPU corner direction IDs (17..31), unrelated high bits in the packed direction word, and fourth-corner ages outside 0..100 ms in `WaterFlowField.targets`, `sources`, and `heightClock`. Valid IDs 0..16 and ages 0..100 preserve their original float packet bit patterns. The writer lease `docs/ENGINE-MAIN-WRITER-LEASE.json` is `completed-released`, avoiding conflict with the engineering worker.

Production source Git blob: `16e41cd7c9e80d442aa75b33d2c99503c2448063`, SHA256 `8f885f682f8c183c9f9d2e8a5d965d23d8ee8b685dec45903e3a3e2d4666476a`.
Regression source Git blob: `d5b7303029f8f09db8253092dd11a990404e1401`, SHA256 `ff4a7bb346f1e9c25a2a0cbf203e7780b3e3689c1dc9248cd3c75f4ed5ab6d86`.

## Independent reproduction and verification

- Baseline production Git blob `943cee26227e1d3901a625a894bdf86a3bd7b4e6` compiled, but **failed** the new regression with `raw=557056 age=15 check=121` (exit code 1). This reproduces the confirmed defect.
- Corrected production source and committed regression were verified byte-for-byte against GitHub via their Git blob hashes.
- Strict isolated Java21 compilation: `javac --release 21 -Xlint:all -Werror`; execution: `java -ea -Xverify:all`.
- Builder regression independently rerun: **8,340,112 assertions passed**, all 1,048,576 possible 20-bit corner-direction words and 100,000 randomized pairs.
- Separately authored independent `ByteBuffer` GPU-boundary oracle: **3,200,000 assertions passed** across 200,000 randomized packets and 800,000 vertices, checking both float packet words, bounded age and finite values after byte serialization.
- Minimal stubs used only for `WaterPatchMesher` and `WaterSurfaceTransition` dependencies. **These results are source-level, not native shader or full mod tests.**
- No new confirmed production defect was found in the three commits. No unnecessary production edit was made.

## Compatibility, packaging and quality gates

| Gate | Outcome |
|---|---|
| Three new Builder commits reviewed | Complete |
| GPU packet sanitization and valid parity | **PASS**, isolated Java21; provisional **8.5/10** |
| Baseline defect reproduced | **PASS**, negative control failed as expected |
| Full NeoForge mod build / playable JAR | **BLOCKED** |
| NeoForge 26.1.2.122 configured | Confirmed in `gradle.properties`, **not** runtime-tested |
| NeoForge 26.3/26.4 port | Not verified |
| Native finite-water conservation and V8 save/reopen | Not tested |
| Native terrain seams, asynchronous worldgen | Not tested |
| Vulkan/OpenGL, LOD, upscaling, FPS | Not benchmarked |

`build.gradle` requests Java25 and `gradle/wrapper/gradle-wrapper.properties` pins Gradle 9.2.1; this meets the official NeoForge Java25 / Gradle 9.1+ prerequisite. The local runtime has only OpenJDK21, no complete private repository checkout, Gradle executable/cache, GPU test target, or Git CLI access to GitHub (DNS unavailable). GitHub connector access supports source review and audit report commits, but cannot replace a local Gradle build. **No new playable JAR or JAR SHA256 exists. No previous audit JAR was deleted. No GitHub Actions used.**

**Whole-engine historical baseline:** 5.0/10 (not freshly regraded). Next priority: restore complete local checkout and JDK25/Gradle dependency resolution; build a playable JAR, verify hash, then replace the prior qualified JAR only after successful verification. Follow with native water conservation, persistence, terrain-border and Vulkan/LOD hardware acceptance.

**Local evidence archive:** `DeepPrimal_Dev153_IndependentAudit_20261101T1430_Evidence.zip`, 9,082 bytes, SHA256 `f785b53ef316d9956afca55a086f5f5dcfe0bd9dbd91757674b373880640766d` (ZIP integrity checked).

Sources: [NeoForge official getting-started](https://github.com/neoforged/Documentation/blob/main/docs/gettingstarted/index.md); [NeoForge 26.1 migration guidance](https://neoforged.net/news/26.1release/).
