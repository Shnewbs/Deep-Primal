# Expanded replica — docs/renderer-jams/20261028-disk-cursor/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-disk-cursor/REVIEW.md |
| Preserved source SHA-256 | `574f6dc2c0527c2b6196959307141069bdb47ab669073043ae78239b05727f82` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs immutable scan-result sorting and replaces monolithic persisted-page discovery with a worker-owned resumable cursor. It bounds worker entries and owner reconciliation at 256 per turn, retains 8,192 nearest keys, retargets, and tracks cooperative cancellation/retirement. The 17,839 Java25 checks and callback timings exclude Minecraft/GPU and do not establish a frame-time deadline.

## Research expansion — gaps and details to deepen

- An entry bound is not an I/O deadline, and the immutable-result defect shows why producer/consumer container contracts matter. Native same-camera recovery, repeated world resets, and visible restoration still need integration evidence beyond scanner/service tests.

## Research expansion — proposed contracts

- Declare immutable result ownership, worker stream lifetime, cancellation generation, stale-result rejection, and owner-thread admission. Expose examined entries, queue mutations, oldest pending age, and discovery-to-visible progress; retain explored/delivered authority rather than manufacturing unknown terrain.

## Research expansion — acceptance checks

- Replay saturated directories, late nearest files, retarget, deletion, permission failures, and rapid world attachment with the actual client. Verify bounded mutation and no stale publication; collect filesystem tails, native coverage, GPU memory, and water images separately.

## Research expansion — source anchors

- Renderer jam 20261028-disk-cursor — read-only self-audit

This original shares identical bytes with: [library:Renderer Jams/20261028-disk-cursor/REVIEW.md](../../../../project/Renderer-Jams/20261028-disk-cursor/REVIEW.md). Both source identities are retained.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `LodDiskIncrementalChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/LodDiskIncrementalChecks.java) |
| `LodDiskIndexBench.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/LodDiskIndexBench.java) |
| `LodDiskIndexServiceChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/LodDiskIndexServiceChecks.java) |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-disk-cursor — read-only self-audit

**Read-only review of exact product source** [`004a436c7ba01b4662eb25299009b8c41ab9d075`](https://github.com/Shnewbs/Deep-Primal/commit/004a436c7ba01b4662eb25299009b8c41ab9d075), not the independently graded whole engine. The author reviewed the frozen post-fix code and executed focused results after the final candidate; no code change during scoring. Previous renderer self-audit: [`20261028-disk-nearest`](../20261028-disk-nearest/REVIEW.md), 4.75/10. The independent 05:30 UTC audit (pinned [`4cf0f1b`](../../engine-hourly-audits/2026-10-28T0530-4cf0f1b/REVIEW.md)) identified a confirmed immutable-result integration defect. Source and evidence links below are pinned to the exact source commit; see [jam receipt](RECEIPT.json) and [execution logs](README.md).

| Six equally weighted category | Prior /10 | Current /10 | Pinned evidence / limit |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4.5 | **5.0** | [Client integration L279–290](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L279-L290) eliminates immutable sort; [cursor and exact signed ranking](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L145-L169) operate in 256-entry turns; [retarget/admission](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L23-L69) is tested with 8192-key saturation. No actual native Minecraft coverage or unseen terrain delivery. |
| Mesh generation/reuse/scheduling | 6 | **6.0** | [Dedicated background scanner](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L93-L145) does not occupy mesh worker; mesh geometry and experimental splitter unchanged; no measured native rebuild latency. |
| GPU culling/submission/memory | 4 | **4.0** | [VoxelLodRenderer](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java) GPU draw/mesh paths unchanged; real GPU culling, fence, VRAM and multi-draw indirect acceptance absent. |
| Actual FSR/DLSS | 5 | **5.0 (carried)** | [Existing FSR1 spatial implementation and explicit inactive NGX state](../../UPSCALING.md) unchanged. No temporal FSR, DLSS dispatch or frame generation activated. Do not call linear scaling FSR. |
| Visual/backend/settings compatibility | 5 | **5.0** | No shader, material, water, menu/settings, or backend source modified. [Scoped renderer ownership/reset](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L808-L823) uses dedicated cancellation; actual Vulkan/GL transparency/reload/resize/world-switch not run. |
| Validation and measured performance | 4 | **4.5** | [Incremental fixture](LodDiskIncrementalChecks.java) and [service fixture](LodDiskIndexServiceChecks.java) plus inherited scanner suite pass **17,823 JDK21 / 17,839 JDK25** assertions; deliberate immutable-sort mutant rejected. [CPU-only callback probe](LodDiskIndexBench.java) reports Java25 p50 12.5µs, p95 41.7µs, p99 139.4µs, max 1.48ms (1978 calls), excluding Minecraft/GPU and not a matched frame benchmark. Full Java25 Gradle/native/owner-GPU unrun. |
| **Average** | **4.75** | **4.92/10** | **+0.17** (29.5/60; target strictly >8.5 still unmet) |

The separate **whole-engine external grade remains 5.0/10, carried**, not a new independent assessment. No score is assigned to a JAR (none built). Validation and visual limitations remain material, particularly cooperative cancellation during repeated world resets, real player camera travel, edit/stale replays, full-scene water ordering and measured frame tails.

**Review conclusion:** accept the scoped source-only integration repair as tested; it resolves the independently reproduced `List.copyOf().sort()` contract error and converts directory enumeration to entry-bounded worker turns. Do not infer game-world functional completion, performance superiority or DLSS support. Owner-operated native builds and GPU tests remain gating evidence.


<!-- END PRESERVED SOURCE -->
