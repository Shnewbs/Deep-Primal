# Expanded replica — docs/renderer-jams/20261027-runtime-retarget/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-runtime-retarget/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-runtime-retarget/REVIEW.md |
| Preserved source SHA-256 | `a8826838107fdbea5e78067447dba2c523ebd274dec6ce54bd57a016e2c26174` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Integrates the retarget helper and check registration into the current renderer while preserving unrelated source work. Its focused validation uses a Java21 distance-policy mirror; the source version and an older packaged JAR are explicitly not equivalent evidence. The historical dev32 label and old JAR are not the current project release baseline.

## Research expansion — gaps and details to deepen

- The promoted source needs actual-module compilation and a source-to-artifact receipt before native behavior can be accepted. Retarget correctness also needs continuous-movement fairness and useful coverage evidence beyond the helper’s reset decisions.

## Research expansion — proposed contracts

- Record exact helper/renderer/class identities, remaining mirror assumptions, superseding artifact, and the nonloading authoritative-data boundary. Add age and cancellation telemetry so a near-priority scheduler cannot hide perpetual far-work restart.

## Research expansion — acceptance checks

- Build and exercise the exact integrated source, then replay radius changes, travel, reopen, edits, and repeated world attachment. Verify stale jobs cannot publish and compare visible coverage age; carried FSR 1 work receives no new credit from retarget tests.

## Research expansion — source anchors

- Renderer jam — read-only scoped self-audit
- Six equally weighted scores

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam — read-only scoped self-audit

**Exact source pin:** [`98d4b261c1c1e21a7d9f292166b8eb1916a608da`](https://github.com/Shnewbs/Deep-Primal/commit/98d4b261c1c1e21a7d9f292166b8eb1916a608da). Scored after product code and component tests completed, before this docs-only review commit. This is the renderer author's self-review, **not an independent engine regrade**. No product source was changed during scoring.

## Six equally weighted scores

| Category | /10 | Prior | Pinned evidence and exclusions |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | 4 | [shouldRetarget](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L30-L45), [real tick](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L247-L280), 1056 local tests; no visual coverage, continuous travel may starve far rings |
| Mesh generation/reuse/scheduling | 6 | 6 | [LodMesher](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java); unchanged, staged byte-accounting still not shipped, no new measured gain |
| GPU culling/submission/memory | 4 | 4 | [VoxelLodRenderer](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java); no GPU MDI/arena/VRAM or draw-count evidence |
| Actual FSR/DLSS | 5 | 0 | [Completed dev32 FSR1 EASU/RCAS OpenGL/Vulkan source and prior software GPU receipts](https://github.com/Shnewbs/Deep-Primal/blob/bd28005d7c8703c7abdd822634cb4a07875d2038/docs/validation/upscaling-2026-10-27/README.md), carried from another completed jam, not freshly executed. Optional [DLSS NGX bridge](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssVulkanProvider.java) is in-game disabled awaiting motion/depth/jitter, dispatcher, runtime and actual RTX |
| Visual/backend/settings compatibility | 5 | 4 | Prior dev32 HUD/native-resolution/FSR1 functional GL/Vulkan receipts carried; current LOD retarget visual/API/reload and water ordering unrun |
| Validation and measured performance | 4 | 4 | Exact helper/test Git blobs, [registered checks](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L15-L25), 108999+1056 component checks and correctly rejected negative control; no current full Java25/module/JAR/native or measured GPU frames |
| **Mean** | **4.67** | **3.67** | **+1.00** primarily due to separately completed actual FSR1 rather than LOD hardware acceptance |

This is **provisional** because full root Java25/Gradle/JAR/client acceptance and named GPU measurements were not rerun. Neither byte-accounting nor optional DLSS SDK code earns unshipped feature credit. Independent whole-engine baseline remains **50/100 = 5.0/10** (carried), not a fresh external audit; acceptance >8.5 remains open.

**Completed regression:** `javac --release 21`, `java -ea -Xverify:all`, 108999 original coordinates, 1056 retarget assertions, expected negative-control failure `AssertionError: completed move +1:16`. Exact helper/test read-back Git blobs: `afccbfb6d92e3b97e97d6223235cde4bd16df2f9` and `29bb2aec20b612a42fdff08b78a0cdf76f53d0f6`. Java21 local DistancePolicy mirror not hash-identical and full Minecraft client not compiled here.

**Missing:** full Java25/Gradle+NeoForge and native client runtime; recursive archive CRCs; source/compiled/packaged identities; private fixture/platform resource exclusion; Vulkan/OpenGL pixel/reload/resize/save/world switch/shutdown; AMD8060S and RTX; p50/p95/p99 CPU/GPU/upload/draw/descriptor/RAM/VRAM; authoritative unseen-terrain contract. No new JAR, GitHub prerelease or CurseForge upload. Current source mod_version still dev32; old dev32 binary MUST NOT be relabeled. Next: full qualified source-matched build and tested admission-gate integration.


<!-- END PRESERVED SOURCE -->
