# Expanded replica — docs/renderer-jams/20261027-pending-gpu-ledger/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-gpu-ledger/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-pending-gpu-ledger/REVIEW.md |
| Preserved source SHA-256 | `6c550aa049548027fab60d315f1fad9a972e60fc93df18622ce13f95a0962709` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Replaces repeated pending-buffer summation with an O(1) committed byte ledger. Append validates null, negative size, and overflow before mutation; bytes advance only after successful list insertion, relying on immutable accepted GPU buffer sizes. The large randomized fixture establishes ledger semantics, not GPU-memory measurement.

## Research expansion — gaps and details to deepen

- The local ledger should connect to exactly-once ownership transfer through published resident and retired states. Correct pending totals alone do not prevent a replacement from being double-charged or a GPU-referenced resource from being declared free too early.

## Research expansion — proposed contracts

- Specify immutable allocation identity and byte size, transfer events, rollback, and completion-aware release. Maintain separate reserved, pending, resident, and retired totals with conservation checks across successful append, failed append, cache replacement, reset, and close faults.

## Research expansion — acceptance checks

- Retain the 418,317-assertion randomized/overflow coverage, then execute actual backend ownership transitions with partial failures. After completion, require each allocation to have exactly one accounting owner or a verified released state; component timing is not a frame result.

## Research expansion — source anchors

- Deep Primal dedicated renderer jam — constant-time partial GPU upload accounting
- Previous findings and ownership

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../WATER-ENGINE-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/WATER-ENGINE-WORK-STATE.json) |
| `results.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-gpu-ledger/results.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dedicated renderer jam — constant-time partial GPU upload accounting

**Jam ID:** `20261027-pending-gpu-ledger`. **Read-only renderer self-audit** of production code committed at **[598c860d57687b37b2c24f7f329569354dd4b113](https://github.com/Shnewbs/Deep-Primal/commit/598c860d57687b37b2c24f7f329569354dd4b113)** (parent `363140f925c27bfc0c23eb601595ac5a4e20528c`). Evidence/test commit `9e17551029f6ce1c856fe471cde62fba7d7d84f6`; common `gradle.properties` version remains **0.0.1-alpha.29-dev.41-waterwriteverify.1**, owned by the independent engine/water workstream. No production changes were made during this scoring review, and this is **not** an independent whole-engine audit.

## Previous findings and ownership

Consumed [20261027 capture-priority](../20261027-capture-priority/REVIEW.md), where near-camera capture selection was implemented but warm/cold camera/travel/edit baselines and real upload/culling/pending-memory tails remained unmeasured. Also inspected the [independent engine 2026-10-28 00:29 audit](../../engine-hourly-audits/2026-10-28T0029-bcce835/REVIEW.md): its seven post-native water-write concerns belong to the separate water role. Current [WATER-ENGINE-WORK-STATE.json](../../WATER-ENGINE-WORK-STATE.json) claimed `HydrologyRuntime` until 01:15 UTC. This jam edited **only** its two LOD files and dedicated renderer-jam docs. No water/terrain code, other schedules, graphics shaders/settings, shared Java build output, source profiles or approved art was modified. A bounded claim was recorded at `363140f...`; this report releases the scoped claim.

The previous [pending-accounting integration](../20261027-pending-accounting-integration/REVIEW.md) had replaced allocating stream traversal with indexed traversal. That **still summed all prior uploaded parts** on every partial-upload frame. The present patch is a new, nonduplicated incremental improvement.

## Exact production increment

The prior [renderer `PendingGpu.bytes()`](https://github.com/Shnewbs/Deep-Primal/blob/363140f925c27bfc0c23eb601595ac5a4e20528c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L103-L115) traversed three already-accepted mesh lists whenever the renderer checked pending memory or evicted/cancelled an unfinished page. If a page has `n` parts and uploads incrementally, repeated whole-list summation scales as **O(n²)** across progress checks. This statement is a code-complexity observation; no native scene with thousands of parts was captured.

- New [`PendingGpuBytes` L1–31](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/PendingGpuBytes.java#L1-L31) stores the committed bytes. `append(target, resource, gpu.byteSize)` checks nonnull inputs, negative size and signed-long overflow **before** the list changes; the counter increments only **after the actual list append succeeds**. It uses the actual GPU allocation byte count, not pre-mesh estimated payload. All calls occur on the renderer thread.
- [`PendingGpu` L102–126](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L102-L126) now answers `bytes()` in constant time. [Actual upload L645–675](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L645-L675) calls `p.addGpu(p.detail,gpu)` only after `TerrainGpuMesh.upload` succeeds, preserving the existing resident byte addition, timing/count caps and first-indivisible-part forward progress. The obsolete/budget abort paths subtract the same committed byte count; completed entries receive the same accepted total.
- [`TerrainGpuMesh` constructor](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L36-L58) initializes `byteSize` from fixed vertex/index buffer sizes. Although its field is public and mutable in type, the owning accepted-list path does not change it after construction. An external post-append mutation would violate this snapshot contract and was not tested in native execution.

No cache format, geometry/material/UV/transparent-water order, vertex/index bytes, shader, Vulkan/GL backend, culling result, rendering distance, DLSS/FSR selection, water solver, chemistry, persisted world or player edit semantics change. The helper adds one small Java accounting object per pending page, **not per frame**. Exact committed diff: **two production files**; no other feature files.

## Executed validation and scoped measurements

[Reproducible source/checks and commands](TESTS.md) and [structured results](results.json), with exact Git blob IDs. Separate Linux **AMD EPYC 9V74** host, OpenJDK/javac **21.0.12.1**. Source helper blob `bd3a1f251133b23eb616f6f13d7865baafbf99c2` matches byte-for-byte the locally compiled helper; committed fixture blob `5a312cd78eb1fb8b9248dd4b86743a63e8efdded` and microbench blob `27715806fc6d60b27ce9c5a08350c0a9d6a6e0e7` match their tested sources. Final renderer source blob `79970c0491579b20efe4b05934d7b4535c5d9b7d` was fetched after the commit; both active append and budget paths were inspected.

```sh
javac --release 21 -Xlint:all -d classes PendingGpuBytes.java PendingGpuBytesChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.PendingGpuBytesChecks
# PASS PENDING_GPU_BYTES_PASS checks=418317 acceptedUploads=393216 randomizedPages=512 perPageParts=768
javac --release 21 -Xlint:all -d classes PendingGpuBytes.java PendingGpuBytesMicrobench.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.PendingGpuBytesMicrobench
```

**Focus:** 418,317 passing assertions: 393,216 randomized accepted parts across 512 pages/three LOD levels, exact parity against fresh full-list oracles, partial pages, new-page replacement, zero/negative bytes, overflow boundary, and atomic counter/list behavior on failed append. **Negative control:** separate mutant returning `0` instead of committed bytes compiles but exits1 at `42 != 0`, as required. No mutant shipped.

**Diagnostic CPU-only paired lookup microprobe** (30,000 warmups, 8,000 interleaved observations per mode, isolated host; no native GPU):

| Accepted parts | Old indexed-scan p50/p95/p99 | New running-total p50/p95/p99 |
|---|---|---|
| 512 | 260 / 271 / 380 ns | 60 / 61 / 71 ns |
| 4,096 | 2,915 / 2,945 / 3,515 ns | 30 / 30 / 31 ns |

Each mode had identical accepted-byte checksum. These nanoscale samples are JIT/host sensitive, exclude uploads, allocations, thread scheduling and rendering; **they are not real frame-time, FPS, GPU, latency or broad speedup evidence**. There was no camera/seed/resolution/API/GPU scene applicable to these component probes.

## Six equally weighted renderer categories — conservative read-only self-audit

| Renderer category | Previous /10 | Current /10 | Pin and missing gate |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | **4** | [Selection/near-native handoff](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L505-L560) unchanged; no native camera/edit/silhouette/explored-only visibility test |
| Mesh generation/reuse/scheduling | 6 | **6** | Existing greedy [`LodMesher`](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L1-L65) and [capture priority](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L15-L49) unchanged; no actual mesh throughput proof |
| GPU culling/submission/memory | 4 | **4** | [PendingGpu accounting](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L102-L126) and [helper](https://github.com/Shnewbs/Deep-Primal/blob/598c860d57687b37b2c24f7f329569354dd4b113/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/PendingGpuBytes.java#L16-L31) remove repeated list summation; real device/VRAM/indirect submission not tested |
| Actual FSR/DLSS implementation | 5 | **5** | Existing [FSR1 EASU/RCAS and incomplete DLSS NGX](../../UPSCALING.md); no temporal inputs, device dispatch or RTX validation this jam |
| Visual/backend/settings compatibility | 5 | **5** | No shader/content/option change; water ordering untouched, **no** real OpenGL/Vulkan pixels, reload/resize or owner GPU execution |
| Validation and measured performance | 4 | **4** | [418,317 Java21 model/helper checks](TESTS.md), negative mutation, paired CPU lookup measurement; **no** Java25 full build, packaged Minecraft, frame p50/p95/p99, GPU/VRAM/culling benchmarks |
| **Equal-weight total/average** | **28/60 = 4.67** | **28/60 = 4.67** | **Δ 0.00**; no unsupported score uplift |

The separate **whole-engine external grade remains carried 50/100 = 5.0/10**, not freshly regraded or modified. Renderer target **>8.5/10** is not met.

## Self-audit limitations, artifact policy and next jam

Read-only review verified the exact source parent, two-file production diff, new byte-accounting/append integration, committed helper/test blob identities and scoped claim. Remaining risk: no native test of a partial GPU upload that throws mid-page, no actual deadline/resident-memory pressure, no proof of end-to-end frame improvement; a mutable `byteSize` beyond the owning fixed-buffer path would need an explicit invariant. Java25 path and owner archive are absent in this execution workspace; the cloned private Gradle checkout, dependencies, Minecraft native tests, Vulkan/OpenGL water/LOD visuals, RTX DLSS, owner AMD Windows, JAR source/compiled identity/CRC and release were **not run** under the October 27 owner-build override. **No JAR or release produced or claimed.**

The evidence source ZIP `DeepPrimal_Renderer_PendingGpuLedger_20261027_598c860_SourceEvidence.zip` is **6,669 bytes**, SHA-256 `e0338ebd654d924e8885efa95a30596b09a833c8990527d5812ed78e4a6023b5`, ZIP CRC verified; it contains the exact tested helper/test/benchmark and positive/negative output logs. It is not a game binary.

**Next renderer-only priority:** owner-side Vulkan then OpenGL real camera/travel/edit/resize/reload/water-transition measurements of CPU/GPU p50/p95/p99, mesh part sizes, upload failures and memory peak. Under observed spikes, implement material-preserving bounded mesh part splitting or fence-safe arena reuse; validate near-native/transparent-water handoff. Then pursue temporal depth/jitter/motion-vector prerequisites for real DLSS provider activation. Keep the separate water/engine/auditor tasks unchanged and leave the dedicated renderer schedule enabled.


<!-- END PRESERVED SOURCE -->
