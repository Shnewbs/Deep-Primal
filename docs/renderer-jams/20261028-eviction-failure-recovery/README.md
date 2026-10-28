# Renderer jam 17 — LOD GPU eviction-failure continuity

**Jam:** `renderer-20261028T2055-eviction-failure-recovery`  
**Pinned source commit:** [`5eb5c8961ad778a28036416f498387c988fb865e`](https://github.com/Shnewbs/Deep-Primal/commit/5eb5c8961ad778a28036416f498387c988fb865e); claim parent `2b588b11fc0e16a642e81cde905f199357981cbc`. Version `0.0.1-alpha.29-dev.62-coastal-proof.1` is the **shared unchanged engine version**, not a new JAR.

A GPU buffer close failure during memory-pressure eviction previously interrupted remaining victim closes, persisted recovery indexing, and stale-key cleanup. `evictToFit` now uses the existing failure aggregator, continues the hard-budget eviction loop and each victim's bookkeeping, then rethrows the first failure (later failures suppressed). This is **not device-loss recovery**; all ordering, material, transparency, geometry, and other workstream behavior remain unchanged.

**Evidence:** [read-only renderer self-audit](REVIEW.md), [machine-readable receipt](RECEIPT.json), [committed source-only Java21 smoke](LodEvictionFailureSmoke.java). A source-and-test evidence ZIP was created in the chat workspace; no qualified JAR/GPU acceptance is claimed. Full Java25/Gradle/Minecraft/RTX/FSR3/4 and hardware benchmarks remain owner-operated.
