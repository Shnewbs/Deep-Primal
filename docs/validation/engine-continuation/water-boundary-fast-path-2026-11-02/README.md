# Dev194 — water neighbor boundary-index fast path (2026-11-02)

**Baseline:** `344eb06bb91f272e99357e348b4fd28aa32ee6ea` (Dev193 independent audit). **Scoped lease:** `8511714cc8f7e8a1f5d8e7e6b12e1f19852f8cab`.

## Change

`WaterBoundaryIndex.of` previously scanned all nine neighbor slots for every input quad in each of two passes. Only quads on x/z page edges contribute, and corners contribute at most three entries. Replace 18 mask evaluations per quad with two constant-time edge-classification passes. Retain exact NW/N/NE/W/E/SW/S/SE neighbor bucket order, per-bucket original GPU order, 10-int header, byte counts, empty singleton and public mask semantics. No water quantity, terrain, saved-world, renderer shader, or native API change.

## Local verification (Java 21)

- `javac --release 21 -Xlint:all -Werror` on the complete production class and regression sources; `java -ea -Xverify:all`.
- New deterministic exact old-implementation differential: **1,098,120 assertions**; 2,000 randomized inputs, every 256 cell positions, 100,000-entry stress case, high packed bits and duplicate entries. Original header, neighbor ranges and every retained entry match.
- Existing absolute-coordinate oracle `WaterBoundaryIndexChecks`: **688,129 cases**, passing at world-coordinate extremes and with stable GPU order.
- Isolated warmed microbenchmark, 120 paired runs of 32,768 quads: original **149.478 ms** aggregate, optimized **27.461 ms**, **5.44x** speedup in this local Java21 fixture. This is not a native Minecraft frame-time result; hardware/JIT/noise may change the ratio.
- Source SHA-256 `45f7e34711041c04ed5167117079f15daebc4e6b30f78db3e9fe111c58f34858`; regression SHA-256 `5ed9864ee236df7211a8fe08153ffafef5b7db12c6d6e08bd3f3a0f6f424ecb8`.
- Regression registered in `CoreChecks` after existing `WaterBoundaryIndexChecks`.

## Outstanding acceptance

No full Java25 Gradle/NeoForge build, playable JAR, native Vulkan/LOD/water benchmark, or save/reopen validation: local GitHub DNS prevents complete private clone and Gradle dependencies are absent. Java25 was recovered by Dev193 in its separate runtime, not available in this engineering runtime. No GitHub Actions invoked. Retain previous qualified JAR until verified replacement. Historical whole-engine grade remains 5.0/10; this local optimization does not regrade it.
