# Expanded replica — docs/renderer-jams/20261028-eviction-heap/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-heap/README.md) |
| Source identity | git: docs/renderer-jams/20261028-eviction-heap/README.md |
| Preserved source SHA-256 | `f99721c3cbc06bfae73db97192cb03ed82a9e7956b6d474b5e8e52562551e74f` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Replaces full eviction sorting with a primitive max-priority heap, preserving protected-page-last, descending-distance, and stable access-order ties. The caller admits at most 4,096 resident pages; isolated victim-selection medians improve while temporary arrays may allocate more.

## Research expansion — gaps and details to deepen

- Comparator compatibility and selection CPU time do not establish better frame latency or memory pressure. The source should distinguish its preserved approximate-distance tie semantics from later exact ranking elsewhere, and link reusable scratch as a separate successor.

## Research expansion — proposed contracts

- Record the eviction ordering tuple, resident-cap dependency, selected-victim count, scratch allocation/retention, and protected-page behavior. Join selection to complete close/retirement and persistence costs before comparing it with the old sort.

## Research expansion — acceptance checks

- Retain 251,940 parity checks, stable ties, and rejected comparator mutation. Measure actual pressure events, heap/GC, full eviction CPU spans, GPU retirement, and visible coverage on the target artifact; do not promote the roughly 83–84% one-victim selector result to FPS.

## Research expansion — source anchors

- Deep Primal renderer jam 20261028T1301 — bounded GPU-cache eviction ordering
- Exact executed checks (Linux OpenJDK 21.0.12.1)

This original shares identical bytes with: [library:Renderer Jams/20261028-eviction-heap/README.md](../../../../project/Renderer-Jams/20261028-eviction-heap/README.md). Both source identities are retained.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal renderer jam 20261028T1301 — bounded GPU-cache eviction ordering

- **Owner:** dedicated renderer workstream only; other engine/water/auditor automations unchanged.
- **Claim:** `901953417f7c6899bcf2160275fe9eea3561ea36`, scoped to `LodEvictionHeap.java`, `VoxelLodRenderer.java` and this jam directory; claim acquired from main `ace45f78f4f181e1f591f7fd5f179e70c7d1d5a5`.
- **Production source commit:** `fcd437f45dfbac9393b4a06fa7b53363bf9944e7` (parent `901953417f7c6899bcf2160275fe9eea3561ea36`). **Final tested source plus tests:** `ca675c2e205a3fd71561e9f18409f4196da3013d`; inherited `mod_version=0.0.1-alpha.29-dev.55-blendpolicy.1`. Documentation and claim release follow as a separate non-source checkpoint.
- **Changes:** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java` introduces a primitive-array max-priority heap for resident LOD cache victims; `VoxelLodRenderer.java` uses it only in the existing memory/entry-pressure eviction path. Source-excluded, repeatable parity and CPU benchmark programs are under this jam directory. No change to mesh geometry, water alpha/depth, capture, source revision, LOD radius, native tracking, GPU upload or resource close semantics.
- **Consumed finding:** previous renderer audit 5.21/10 lacked actual render-frame measurements and required bounded memory/performance work. The previous hot pressure path sorted all cache keys on every call even when one victim sufficed. The independent 11:28 UTC engine audit remains an unrelated read-only audit with a carried 5.0/10 engine score.
- **Algorithm:** previous comparator precedence is preserved, including protected `keep` page last, descending world-space squared distance and stable access-order-map ties. Bottom-up heap construction is O(n); each selected victim O(log n); four primitive arrays of size n avoid per-entry wrapper allocations. Only 4096 resident pages are admitted by the existing caller. Heap selection itself may allocate more temporary array bytes than the previous sort; there is **no allocation reduction claim**. Cleanup, persistence, edit guards and resident-byte decrements still happen in the original code.

## Exact executed checks (Linux OpenJDK 21.0.12.1)

```bash
javac --release 21 -Xlint:all -Werror -d out \
  src/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java \
  tests/com/shnewbs/deepprimal/lod/client/LodEvictionHeapChecks.java \
  tests/com/shnewbs/deepprimal/lod/client/LodEvictionHeapBench.java
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodEvictionHeapChecks
java -cp out com.shnewbs.deepprimal.lod.client.LodEvictionHeapBench
```

**Result:** 251,940 assertions pass against an independent recreation of the original stable comparator, including 1,200 randomized configurations, 4096-key saturation, access-order changes, `keep`, equal-distance symmetry, negative/int-extreme coords, empty heap and nonmutating input. A deliberately defective equal-distance-tie reversal failed at the expected oracle assertion (`mutant-unstable-tie.log`, exit 1). Source Git blob `49f8f785a2b271558b72a13c68b858528e9d60c3` and test blobs `34ab67fa17fb74e6f0df5f157a422227c1481ad2`, `796b7bc9f71819b9491e42e6f2eba61e0a85867b` exactly match locally compiled bytes. Patched renderer Git blob `0665fdbb4f8460100f1cdda6d85eeebff7b3190e` verified at pinned source commit. Java25 JDK/archive were **not mounted**, and system Java remains 21.

**Paired microbench:** same Linux process, cache keys/camera/protected page and two repetitions, with alternating old/new calls, 1000 warmups and 360 measured cases per configuration. At 4096 entries, removing one old/new CPU median was **612,460/95,730 ns** (first) and **557,900/93,832 ns** (repeat); removing 16 was **584,311/96,135 ns**, **615,597/97,026 ns**. All are isolated victim-selection timings, including helper construction, not GPU fence time, Minecraft frames or FPS. Removing the entire 4096 entries was **679,154/539,964 ns**, **704,982/547,454 ns** respectively. Benchmark is not hardware-normalized and contains JVM/GC scheduling noise.

## Owner-operated/unrun gates and next

No full Java25 Gradle/NeoForge build, Windows Minecraft game client, GPU driver, Vulkan/OpenGL native scene, GPU draw/VRAM/frame-time p50/p95/p99, water transparency pixels, scene resize/reload/world switch/save/reopen/shutdown, source/class/JAR CRC/natives, packaged client or release. No qualified JAR. Owner handles those gates. There is no external hardware acceptance and no proof of gameplay FPS gains. Re-run the native fixed-seed travel/edit/reopen profile with cache pressure, compare observed render-thread eviction p95/p99 and heap allocation/GC against old sort, verify water depth/transparency on Vulkan and OpenGL; then pursue actual DLSS temporal prerequisites separately.


<!-- END PRESERVED SOURCE -->
