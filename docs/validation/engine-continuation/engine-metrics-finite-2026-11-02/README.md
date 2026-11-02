# Deep Primal dev182 — engine metrics finite admission (2026-11-02)

## Coordination
- Scoped claim `engine-20261102T0502-metrics-finite` from base `be25f82584b8b04ce1c93eb7331fbdfab747a57d`; independent auditor/organics/native renderer/water files excluded.
- Production scope: `engine-core/.../EngineMetrics.java`; new `EngineMetricsTimingChecks.java`; registered in `CoreChecks.java`.
- No GitHub Actions, no compiled binaries committed.

## Defect and correction
`EngineMetrics.recordTerrainSample`, `recordWaterSim`, and `recordChunkMaterialize` previously counted negative elapsed samples as successful operations and accumulated them. Signed-long addition could overflow to negative totals and produce impossible negative timing averages. The three paths now reject negative samples, preserve zero samples, and use one shared nonallocating operator for saturating atomic totals. `getCacheHitRate` now converts hit/miss counts to floating point before addition so two individually valid large counts cannot overflow a signed-long denominator.

The normal finite, nonoverflowing numerical results are unchanged. This affects diagnostics only; no worldgen, water quantity, saved formats or shader changes.

## Validation
- Production Git blob: `bcb2066df3056cf8d5b9f1632a473fd91c08f7b2`.
- Registered regression Git blob: `5445d13d814a11bfcb8baf1ecba835a5387fa53c`.
- `CoreChecks` registration Git blob: `77f48ea56c613a9ae79a740d01d3d7ed83c46b95`.
- Exact production class and exact registered test compiled with OpenJDK 21 `javac --release 21 -Xlint:all -Werror`; executed with `java -ea -Xverify:all`.
- **600,028 assertions passed**, including 300,000 randomized ordinary duration samples, three saturation channels, negative samples, reset, and cache arithmetic.
- Independent 8-thread concurrency harness: **320,000 samples per channel** with exact totals and another **320,000 saturating samples** with no lost updates.
- Original arithmetic negative control compiled and failed at check 5: `negative duration admitted`. This is expected and not counted as passing.
- Reproducibility ZIP: `DeepPrimal_dev182_engine_metrics_evidence.zip`, SHA-256 `0ab2b86950c1ee7e75158d7c767f5a875531f5209ffc0255c96721c9c29b58d3`; 9 files, ZIP integrity checked.

## Unverified and blockers
The complete `CoreChecks` suite, full Java25 NeoForge Gradle build, playable JAR, native water conservation/save-reopen, Vulkan GPU and LOD benchmarks were **not** run. Local environment has only JDK21, no complete private checkout or resolved Gradle/NeoForge dependency cache, and local GitHub DNS fails. The connector permits source writes, not a local private clone. Saturating atomic accumulation may cost more than the prior `addAndGet`; production hot-path microbenchmarks are still needed. No replacement JAR/SHA was generated; prior successful JARs were not deleted. Historical whole-engine grade remains 5.0/10, pending native validation.
