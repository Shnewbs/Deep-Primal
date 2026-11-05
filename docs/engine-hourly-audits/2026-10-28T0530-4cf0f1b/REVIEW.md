# Deep Primal — independent audit, 2026-10-28 05:30 UTC

**Pinned committed main:** [`4cf0f1b930b182903af3086071859d727379a74d`](https://github.com/Shnewbs/Deep-Primal/commit/4cf0f1b930b182903af3086071859d727379a74d), version `0.0.1-alpha.29-dev.46-v8native.1`. This is a source/focused-Java25 audit with independent output and no production-code changes. The owner’s October 27 override delegates full Gradle/Minecraft/GPU builds to the owner; this audit does **not** claim a current JAR.

## High-priority new renderer regression: persisted LOD scan results never enqueue

* [LodDiskNearestScan.java L48–53](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L48-L53) sorts the selected disk keys then returns `List.copyOf(result)`, which is immutable (Git blob `aa91a4f7fc51e01ca2df9d5d8f88ee116075cf7f`).
* [VoxelLodRenderer.java L314–328](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L314-L328) calls `keys.sort(...)` **before** the loop that adds keys to `disk`. The `UnsupportedOperationException` is swallowed by the surrounding `catch (Exception)`, assigning status `Cache index unavailable` and setting `scan=null`; no persisted keys from that completed scan are enqueued (renderer Git blob `c8cb68c86537d90d406228a8ec58457cb3f50492`).
* Independent Linux Java **21 and Temurin 25.0.5+8** regression compiled the **exact Git-blob-matched production scanner**. Four real temporary-directory scenarios with **1, 16, 120 and 8,205** canonical `.plod` candidates all reproduce `UnsupportedOperationException` and **zero** restoration queue additions; consuming the scanner's already-sorted immutable list without sorting admits 1, 16, 64 and 8,192 entries. Each JDK passes **36 integration-reproduction assertions**. The existing registered scanner suite separately passes **9,006 assertions** on each JDK. Evidence: `immutability-repro.log`, `immutability-java21.log`, `existing-scanner.log`, and new `LodDiskImmutableIntegrationChecks.java` in auditor-only evidence ZIP.
* This is a **confirmed product-source integration contract violation** and an executable Java reproduction, **not** an executed Minecraft world-reopen. The author’s standalone scanner tests did not exercise its consumer.

**Suggested renderer repair (not made by auditor):** remove the redundant in-place sort; `scan()` already returns its selected keys in deterministic nearest order. Alternatively copy to a mutable list before re-sorting. Add a scanner-to-renderer queue regression proving real saved pages actually reach `disk`, followed by native world reopen/camera travel/edit/cancellation checks. The preexisting one-time-camera scan and bounded-cap recovery limitations remain separate.

## New engine source independently rechecked

[DensityChunkMaterializer.java L74–110](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L74-L110) adds an *explicit-call-only* V8 native section bridge, using [DensitySectionLayout.java](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensitySectionLayout.java) without activating V8 in registered worldgen. All **10** copied core/adapter source blobs were compared against current pinned Git blob identities, including `DensityChunkMaterializer.java` `20443809ed72fa10f1c5f6ea885dd551f433d4ab`. A fresh, isolated JDK25 `javac --release 25 -Xlint:all -Werror` / `java -ea -Xverify:all` rerun of the existing API-double check passes **11,047,019 assertions**, 1,376,512 materialized positions, 34 scenarios. This proves exact-source API-double correctness only: *actual NeoForge classes, heightmap/lighting semantics, native save/reopen, V8 activation and chunk timing were not tested*.

Recent builder source handoff: [Dev46 source/test receipt](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/docs/validation/engine-continuation/v8-native-section-bridge-2026-10-28/README.md). Latest renderer handoff: [nearest disk scan self-audit](https://github.com/Shnewbs/Deep-Primal/blob/4cf0f1b930b182903af3086071859d727379a74d/docs/renderer-jams/20261028-disk-nearest/REVIEW.md). Independent auditor did not take over either role or alter shared caches, build output, world saves, modules, presets or schedules. Source pin occurred before these isolated tests.

## Ten-category grading — carried, no fresh full-engine regrade

| Equally weighted category | /10 | Current evidence boundary |
| --- | ---: | --- |
| Terrain realism / variety | **5** | V8 exact section bridge remains unregistered; no native visual terrain |
| Biome blending / coherence | **4** | No accepted weighted ecotone gameplay stage |
| Hydrology / caves | **6** | Finite water/caves exist, but callback/unload/long-soak gates open |
| Structures / features | **4** | No general accepted structure ownership/spacing pipeline |
| Performance / threading | **3** | Last native warm fills ~14.45–14.99 ms vs 5 ms target; no fresh full benchmark |
| Determinism / stability | **7** | Frozen V1–V7 historical native identity; V8 native replay unrun |
| Architecture / code quality | **6** | Core modularity; source-contract bug discovered in LOD integration |
| Extensibility / data-driven design | **4** | Saved V8 graph/biome/features not registered as supported authoring API |
| Tests / benchmarks / documentation | **7** | Fresh JDK25 unit and modeled integration repro; native/owner GPU unavailable |
| Polish / tooling | **4** | LOD persisted-page restoration regression plus water/cliff and visual gaps |
| **Total (Tier 2)** | **50/100, average 5.0** | **Carried 2026-10-24 complete grade, not a fresh regrade** |

No score increase is justified by unregistered V8 or tests that use native API doubles. A fresh ten-category independent overall **strictly greater than 8.5/10** remains required.

## Build and publication status

**Fresh Java25 focused source tests: passed.** Java25 checked in this isolated audit by `java -version` and `javac -version` (Temurin 25.0.5+8 copied from a released builder toolchain). Java21 regression also passed. The builder's current work-state shows its Dev46 lease **released** and the exclusive timing reservation **released**; no competing Java/Gradle process was observed locally before tests. No source or stage-ownership changes.

**Not run:** full Gradle 9.2.1 and Minecraft/NeoForge module compilation, actual native blocks/lighting/heightmaps/world save/reopen/shutdown, selected JAR producer, outer/nested archive CRC/class identity, Vulkan/OpenGL/AMD8060S/Windows graphics, full-scene performance/VRAM/frame p95/p99, large-scale water/LOD soak. No new qualified JAR, GitHub Actions, GitHub Release, CurseForge upload or JAR-retention deletion. These are owner-operated builds under the latest instruction; no older binary is described as current.

**Auditor-only artifact:** `DeepPrimal_Audit_20261028T0530_4cf0f1b_SourceEvidence.zip`, 38,335 bytes, SHA-256 `72e72bd4d86f71a26835fd09d2e87722ef5370886f195fda587292bfc7813278`, **38 CRC-valid entries**.

**Next priorities:** (1) renderer repairs immutable scanner/consumer mismatch and proves disk queue admission on native reopen, (2) owner native Java25 V8 section/base-column/heightmap/lighting/save/reopen and legacy replay, (3) Builder finite-water callback-safe ledger, loaded/unloaded ownership and connected cliff continuity, (4) matched Vulkan/LOD GPU/performance. Independent audit remains nonblocking.