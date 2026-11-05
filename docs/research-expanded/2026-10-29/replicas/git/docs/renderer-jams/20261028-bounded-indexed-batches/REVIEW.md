# Expanded replica — docs/renderer-jams/20261028-bounded-indexed-batches/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-bounded-indexed-batches/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-bounded-indexed-batches/REVIEW.md |
| Preserved source SHA-256 | `ac0f694e2dca19754abdf6d4265fbb752835dc7a086ac3a020d58310fd681d76` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Bounds each synchronous drawMultipleIndexed call to 4,096 ordered Java draw records and clears references after return or failure. The direct path is preserved, and diagnostics count returned API batches rather than hardware multi-draw-indirect commands. The 2,412,262 helper assertions validate bounded delivery, not a native driver optimization.

## Research expansion — gaps and details to deepen

- Bounded reference retention is valuable but does not prove cheaper submission. The recorded CPU preparation comparison was slower, and native transparency, driver calls, and GPU performance remain unmeasured; later hard-limit work also strengthens misuse protection.

## Research expansion — proposed contracts

- Add a borrowed-list lifetime contract, stable pass/material order, completion semantics, and a link to the hard-limit successor. Any future indirect path must have its own command capacity, overflow, culling, and fallback contract rather than reuse this API-batch label.

## Research expansion — acceptance checks

- Retain ordered delivery, callback failure, nested-call, and scrub checks for 131,072 parts. Run exact-artifact Vulkan/OpenGL batched/direct comparisons at ordinary and extreme visible counts with CPU/GPU timings, memory, and transparent-water images.

## Research expansion — source anchors

- Renderer jam 18 — bounded indexed-draw batches (read-only self-audit)
- Audit findings consumed and implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 18 — bounded indexed-draw batches (read-only self-audit)

**Jam ID:** renderer-20261028T2205-bounded-indexed-batches. **Exact reviewed source:** [cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9](https://github.com/Shnewbs/Deep-Primal/commit/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9). Initial implementation 612833c70df6353985ec9be3d4666bc3c30fa54d; final source parent e908765f030f198df72c2b9f63daded6fe8ba55e; ownership claim 75d05887c62b69c490485d0c228fa9d988f0fa7d. Code completed before this read-only scored review; no production code changed during grading. This is the renderer author's self-audit, not the independent whole-engine audit.

## Audit findings consumed and implementation

The [Dev62 independent review](../../engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md) confirmed source-shaped optional LOD splitting but kept real Vulkan/OpenGL, water pixels, frame p99, and Java25/Gradle unaccepted. [Jam 17](../20261028-eviction-failure-recovery/REVIEW.md) protected GPU-close persistence recovery; it had no GPU-submission measurement. This jam addresses the ordinary batched LOD indexed-draw path, not terrain generation, water simulation, visual assets or FSR/DLSS.

An unbounded ArrayList previously retained all visible indexed Draw objects until the next batched frame. The new [BoundedIndexedDrawBatches.java lines 14–49](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java#L14-L49) (Git blob 2bfc247e178024350807c2f144a411a845fa8330) reserves up to 4096 draw references and clears them after each synchronous API submission, even when preparation or submission throws. [VoxelLodRenderer.java lines 800–830](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L800-L830) (blob f8c398ee0a71bd06bcd0d9a1ac28398c31004ade) issues every visible part in stable contiguous 4096-record batches; nonbatched fallback remains unchanged; [world reset line 886](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L886) releases scratch. [LodClient.java lines 81–85](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L81-L85) (blob 91b3762c90b9a78658dd2610c098b3a85f4baecd) reports successful drawMultipleIndexed API batches, explicitly not hardware multi-draw indirect. No rendering/material or water vertex shader was edited.

**Actual scoped files changed in this jam:** lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/{BoundedIndexedDrawBatches.java,VoxelLodRenderer.java,LodClient.java}; docs/renderer-jams/20261028-bounded-indexed-batches/{CLAIM.json,BoundedIndexedDrawBatchesChecks.java,README.md,REVIEW.md,RECEIPT.json}; renderer-only backlog. Other writers' engine commits on main were preserved.

## Tests and measurements executed (Linux OpenJDK 21.0.12.1)

Strict source compilation: javac --release 21 -Xlint:all -Werror -d classes @sources.txt. Runtime: java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BoundedIndexedDrawBatchesChecks. **PASS: 313 scenarios and 2,412,262 assertions.** Pinned committed production-excluded test [BoundedIndexedDrawBatchesChecks.java](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/docs/renderer-jams/20261028-bounded-indexed-batches/BoundedIndexedDrawBatchesChecks.java) blob 73b7a0f1134e45ae56566aa789a831c9dc6f1476; tested helper and test git hashes match source blobs. High-load case models 131,072 visible parts across 32 batches. A deliberately defective helper omitting scratch.clear compiled but failed assertion 5, as intended. Nineteen read-only source integration assertions passed on exact remote committed blobs. This smoke tests the real helper but only models RenderPass consumption; no full Minecraft module compilation or real GPU command-list lifetime is demonstrated.

Matched source-level Java CPU-only loops (240 paired timed samples, 500 warmups each). Old single-list versus new bounded direct loop: 512 parts p50 2,844→3,105 ns; 4096 parts 9,054→16,725 ns; 16,384 parts 36,225→66,891 ns; 65,536 parts 147,883→267,763 ns. **The new CPU submission-preparation loop was slower in these isolated measurements.** At most 4096 Java references are retained between submissions versus an unbounded visible set, and zero draw references after each batch. These tests do not measure FPS, frame time, GPU calls, VRAM, driver behavior, draw descriptors or byte allocations. Do not claim a performance speedup.

## Six equally weighted renderer categories

| Category | Score /10 | Pinned evidence and limits |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.50 carried** | [LOD handoff](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L508-L566); no fresh distant unexplored/teleport native acceptance |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Optional splitter](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java#L42-L99); no new mesher or GPU gain |
| GPU culling/submission/memory | **4.40 (+0.05)** | [Bounded API batch and finally-clearing](https://github.com/Shnewbs/Deep-Primal/blob/cd264cbf5fa2eb87eeb972f8d7284f3e1af6e2c9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L800-L823); no MDI or real GPU evidence, CPU model slower |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [Upscaling architecture](../../UPSCALING.md): spatial FSR1; temporal FSR3/4 and RTX DLSS not active |
| Visual/backend/settings compatibility | **5.25 carried** | Nonbatched path unchanged; no Vulkan/GL visual or water-opacity acceptance |
| Validation and measured performance | **5.40 (+0.05)** | 2,412,262 strict Java21 helper assertions, expected mutant rejection, 19 pinned static checks; matched isolated CPU model slower; Java25/native missing |
| **Overall arithmetic average** | **5.58 (+0.01)** | 33.50/60 versus prior 5.57; independent whole-engine grade remains separately 5.0 carried |

## Unrun and owner-operated gates

No Java25 executable or user-provided archive was present in this isolated workspace. DNS/complete checkout unavailable. **Full Java25 Gradle/NeoForge, Vulkan/OpenGL packaged Minecraft, Windows AMD/RTX GPU, actual GPU pixels, water transparency, world load/reopen, resize/reload/shutdown, hardware frame p50/p95/p99, VRAM, device failure, source/compiled/JAR archive/native packaging and qualified JAR** remain unrun under the October 27 owner-build arrangement. No GitHub Actions, release or new JAR was attempted. Temporal FSR and DLSS still need genuine end-to-end provider dispatch and hardware validation.

**Next:** Owner-run Vulkan/OpenGL same-camera batched on/off recordings at normal and >4096 visible parts, measure actual API submission overhead, CPU/GPU frame tails, draw counts, VRAM and water opacity. Address observed CPU preparation overhead without losing cleanup guarantees; do not label drawMultipleIndexed calls hardware GPU multi-draw indirect. The renderer schedule stays separate.


<!-- END PRESERVED SOURCE -->
