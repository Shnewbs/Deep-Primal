# Independent Dev70 Java25 audit — 2026-10-28 21:32 PDT

**Pinned main at start:** [`c08e0046d81c97315839c67ea5638b9c59cc3c0c`](https://github.com/Shnewbs/Deep-Primal/commit/c08e0046d81c97315839c67ea5638b9c59cc3c0c). Version: `0.0.1-alpha.29-dev.70-basin-closure.1`. All production source and tests were inspected/read at the pinned SHA. Auditor did not modify source, work-state, builder lease, renderer lease or any other checkout.

## Verified changes and independent checks

- Dev70 opt-in V8 routing policy v3 rejects the previously reproduced bounded, enclosed deep basin crossing the positive and negative 8192-block region seams. Default v2 preserves prior serialized behavior; source [`V8OceanConnectivity.java L229–273`](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java#L229-L273), pinned blob `455cba98c2d340ad82fbf7c05a1ebb16c4e55ff0`. V3 saved fingerprint/open path is present in [`V8RoutedWaterSource.java`](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java), pinned blob `20fafc72e397f744a0a7d0f376bea4b4968db95a`.
- Latest renderer squared-distance LOD selector is integrated at [`VoxelLodRenderer.java L535–554`](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L554) and falls back to original calculation near hysteresis boundaries ([helper source](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodScreenErrorSelector.java#L35-L62)).
- **Java25 actually executed:** Temurin 25.0.5+8 from archived owner-provided JDK; strict `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`. The Dev70 production source was reconstructed from the archived exact Dev69 source and GitHub committed Dev70 diff, then Git blob checked **exactly** against pinned SHA. Pinned renderer source/helper tests matched two more exact Git blob hashes. Compilation used isolated source-matched core API doubles, not the real NeoForge/Mojang runtime.
- Independent new Dev70 fixtures: **2 × 24 assertions** passed, 2/2 deliberately defective source mutations rejected, 13/13 pinned integration/static checks passed. The renderer author's exact parity regression was **independently rerun** under Java25 twice: **2 × 1,556,976 assertions** pass; old/new CPU-only selector medians 34,392/22,273 ns and 34,491/21,893 ns, not game FPS/GPU.
- Builder's Dev70 source-only evidence reports 19/19 suites, 21,012,678 Java21 fixture checks; those broader suites were not rerun here.

## Verified remaining defect (not an ordinary-world claim)

A synthetic **11-region-long completely enclosed inland basin** with height=20 and continentalness=-0.4, with outer dry ring, still returns `isMarineAt(8192,2048)=true` using actual v3 production class because the search reaches its **8-region** bound and treats unresolved wet continuation plus a marine-looking core as ocean (see [L253–273](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java#L253-L273)). This confirms v3 is a **bounded conservative repair, not authoritative global ocean/outlet classification**. A real native occurrence was not demonstrated.

## Equal-weight ten-category baseline carried forward — NOT a fresh regrade

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome blending | 4 |
| Hydrology and caves | 6 |
| Features and structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/docs | 7 |
| Polish/tooling | 4 |
| **Total (Tier 2)** | **50/100 = 5.0/10** |

Baseline [`docs/engine-review-2026-10-24/REVIEW.md`](https://github.com/Shnewbs/Deep-Primal/blob/c08e0046d81c97315839c67ea5638b9c59cc3c0c/docs/engine-review-2026-10-24/REVIEW.md). No unaccepted staging earns gameplay credit. The required **strictly greater than 8.5/10** fresh full-engine regrade remains unproven.

## Unrun gates, handoff and delivery

**Not run:** complete Java25 Gradle9.2.1/NeoForge build; final/nested JAR CRC and platform payloads; compiled-vs-packaged class/fixture exclusion; real native WatershedRegion/Mojang codec/save/reopen/shutdown, conservation under unload/restart, 2-API waterfalls, AMD8060S Windows Vulkan/OpenGL/FSR/GPU benchmarks, multiplayer soak and cross-architecture replay. GitHub DNS prevented a complete isolated checkout; GitHub connector and exact archived sources used. The October 27 **owner-build override** assigns full Gradle/Minecraft/GPU and JAR creation to owner. **No development JAR was produced, published, relabeled or cleaned.** No builder source or timings touched. Root work-state remains historical Dev53 checkpoint and was not rewritten.

Next priority: global authoritative cross-region ocean/outlet proof with fail-closed unknown policy, then native V8 save/chunk-seam and finite-water constituent conservation, followed by Windows/AMD8060S dual-backend acceptance.

**Portable evidence ZIP:** `DeepPrimal_Dev70_IndependentAudit_20261028T2132_c08e0046_Java25Evidence.zip`; 19,952 bytes; SHA256 `4c6daf4569dcd3cb1d2a3f6f9e7f85016493730353361d732dbf6fe22dd9b652`; CRC and per-entry manifests checked, saved in Library `/Deep Primal/Independent Audits/`.
