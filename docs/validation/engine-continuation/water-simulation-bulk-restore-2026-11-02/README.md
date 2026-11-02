# Dev190 — query-water bulk restore and tick scratch

**UTC cycle:** 2026-11-02 09:18–09:22. **Consumed independent audit:** [Dev189](../../../engine-hourly-audits/2026-11-02T0830-dev189/REVIEW.md), reviewed main `4e86276366c82c23675d70149106ca7addbac315`, independent audit commit `43be12680116c5e62e5dc8d5ee260c1e3e8d95a2`. The Dev188 worldgen shutdown fix was independently reproduced; no new worldgen defect was confirmed. This cycle instead addresses unclaimed query-water performance work.

## Production change

- `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterSimulation.java`: bulk `fromChunkData` now validates and fills 256 cells without calling the O(256) `update()` on each cell. The unpublished new instance invokes the existing `setMaterial` validation for dry/wet/solid/source behavior, then updates volume and active-cell totals once.
- `tick` reuses an instance-owned 256-int ground scratch array under the existing synchronized monitor. All 256 height callbacks still validate before the authoritative ground array is overwritten. Failed callbacks do not commit partial terrain.
- `src/coreChecks/java/com/shnewbs/deepprimal/core/WaterSimulationBulkRestoreChecks.java`: 400 randomized imports, 1,200 conservation ticks, packet/material/source parity, malformed-input rejection, late callback failure, 100 scratch identity checks. Registered through `CoreChecks`.

The change affects the dependency-free **query simulation**; it does not replace or modify the authoritative live `WaterRuntime`, finite-water chemistry, native saved-world format, Vulkan renderer, organics, or worldgen.

## Evidence

- Exact production source Git blob: `cb0de12bdef9b536a63e512ef6452b06d2218aff` (historical `a1bf3e859ecfed046504ef0970c5f761776b8c65`).
- Exact registered test Git blob: `050e0342dc12f8b329a91adf82a8bbbbc6f0f806`.
- Exact dependency blobs: `WaterTransport.java` `cfc0993fe503fbbe02356b42fbe0f1a0afc0fe23`, `TerrainNoise.java` `cd107471d6700d7491b865a7dbec0e254af46df2`.
- Java 21 `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **616,510 registered regression assertions passed**.
- Separate deterministic old/new differential harness: **2,473,744 assertions passed** over 800 imports, 3,200 ticks and 1,024 malformed scenarios; packet, source/material, water, flow and pressure match.
- Original source fails new reusable-scratch regression as expected (`NoSuchFieldException: nextGround`).
- Illustrative Java21 same-process microbenchmark: 5,000 imports, old **216.359 ms**, optimized **14.394 ms**, **15.03×** in one run. Not a production game throughput result and not a guaranteed speedup.
- Reproducible local source/test evidence archive `DeepPrimal_dev190_water_sim_evidence.zip`, SHA-256 `4e12fc0cd393b24124d634d314bd1d1cb1895ee0fef09daca3a3d120a2cb2365`.

## Blockers and acceptance

This runtime has Java 21 only, no complete private checkout or cached Gradle/NeoForge dependencies, and local `git ls-remote` fails DNS resolution for github.com. Authenticated GitHub connector writes/reads are available. The production class was compiled with exact `WaterTransport` and `TerrainNoise` sources plus a **test-only EngineMetrics stub**. No full `CoreChecks`, Java25 Gradle, native client/server, finite-water save/reopen, Vulkan, LOD, or GPU acceptance was performed.

**No qualified playable NeoForge JAR or JAR SHA-256 was produced.** No previous successful JAR was deleted; no GitHub Actions invoked. Historical independent whole-engine grade remains **5.0/10**, not regraded.

Next: restore complete Java25 checkout/dependencies and run full build plus `CoreChecks`, then measure native query-import allocation and frame/tick impact; continue live finite-water save/reopen, cliff continuity, Vulkan/LOD acceptance.
