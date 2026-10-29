# Expanded replica — docs/renderer-jams/20261028-gpu-upload-abort/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-gpu-upload-abort/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-gpu-upload-abort/REVIEW.md |
| Preserved source SHA-256 | `a55ae72e32b12764404293b98274af5aff87884768afeab3c00b5309f6863884` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Makes partial LOD GPU upload unwind clear pending ownership before rollback, attempt all accepted-buffer closes, preserve the original failure, and avoid freeing completed cache buffers twice after superseded-entry close faults. Committed byte accounting is paired with accepted ownership. Source-only cleanup evidence does not establish GPU memory recovery, frame-time improvement, or device-loss handling.

## Research expansion — gaps and details to deepen

- The helper fixture proves exception and ownership order, but actual recorded/submitted GPU references must join the model. Logical rollback cannot imply immediate reuse of allocations still visible to a command buffer or device.

## Research expansion — proposed contracts

- Extend the ownership state machine with recorded, submitted, retired, and quarantined references. Define exactly-once transfer to a completed page, cleanup failure aggregation, and bounded renderer failure behavior; keep retries and device-loss recovery separately specified.

## Research expansion — acceptance checks

- Retain the 450,015 focused assertions and release-negative control. Inject failures after allocation, pending append, page commit, and old-entry cleanup in the actual backend; verify no double free, no stale publication, complete accounting, and native resource safety.

## Research expansion — source anchors

- Renderer jam 13 — read-only self-audit
- Read-only source findings

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 13 — read-only self-audit

**Source candidate:** [`61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd`](https://github.com/Shnewbs/Deep-Primal/commit/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd); renderer blob `5e50adfe812413babfbe8463c301ead5bcaf5275`, helper blob `ca79e0b1e8bb69b861077f63872e6fcbd6244841`, test blob `5c424c299647472ffcb1409f67fc49b821235bcc`. Implementation was completed **before** this read-only scoring. This is a **renderer self-audit**, not the independent engine audit. Previous jam: **5.45/10**. The independent [16:28 UTC audit](../../engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md) remains whole-engine **5.0/10**, carried.

| Equal-weight renderer category | Score /10 | Pinned evidence and limits |
| --- | ---: | --- |
| Distance/LOD correctness and coverage | **6.50 carried** | [Nearest-saved-page selection](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L255-L315) unchanged. Unexplored native visible coverage unrun. |
| Mesh generation/reuse/scheduling | **6.85 carried** | [Eviction heap](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L27-L76) unchanged, no new throughput measurements. |
| GPU culling/submission/memory | **4.10 (+0.10)** | [Failure cleanup and cache ownership](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L619-L751), [helper](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuUploadFailure.java#L14-L29). No actual GPU fault injection, VRAM, fence or batching acceptance. |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [Provider contract](../../UPSCALING.md): real FSR1 spatial, temporal FSR3/4 and production DLSS still unavailable. |
| Visual/backend/settings compatibility | **5.25 carried** | No shaders, water-alpha/order, backend or graphics settings changed; Vulkan/OpenGL real pixels unrun. |
| Validation/measured performance | **5.20 (+0.10)** | [Git-pinned check](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/docs/renderer-jams/20261028-gpu-upload-abort/LodGpuUploadFailureChecks.java#L1-L61) + [production helper](https://github.com/Shnewbs/Deep-Primal/blob/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuUploadFailure.java#L1-L30) compiled strict Java21, **450,015 assertions PASS**, mutated no-release variant failed assertion 2 as expected. Source-integration static checks passed; full renderer/Java25/GPU and FPS unrun. |
| **Average** | **32.90/60 = 5.48 (+0.03)** | Target >8.5 not met. Carried categories are historical. |

## Read-only source findings

The previous upload path allowed a failed GPU-part creation/submission to leave accepted buffers in `pendingGpu` across frame callbacks. The new path releases accepted buffers, fixes resident-byte accounting and rethrows the **original** failure rather than claiming to recover the GPU device. Both cache Entry and PendingGpu now attempt all buffer closes even if one throws. Cache insertion transfers ownership before an old-page close can fail; pending discard paths clear the owning reference before close. The resident byte ledger updates before diagnostics, closing a failure window.

The helper and test Git blobs exactly matched isolated files compiled locally. The final static integration check required ordering *within the cache admission block*, rather than a naive global search that also matched an earlier unrelated `old.close()`; the corrected bound passed without any code change. The new helper does not yet have a production module compile or GPU-backed fault-injection test. A close failure can still propagate to the game, but will not prevent attempted cleanup of later buffers. Existing unrelated renderer and water defects were not changed.

**Exact verified commands** (Java 21.0.12.1, isolated checkout of two Git-blob-identical sources):

```sh
javac --release 21 -Xlint:all -Werror -d out \
 src/com/shnewbs/deepprimal/lod/client/LodGpuUploadFailure.java \
 test/com/shnewbs/deepprimal/lod/client/LodGpuUploadFailureChecks.java
java -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodGpuUploadFailureChecks
# LOD_GPU_ABORT_PASS assertions=450015
```

Negative control: replaced `release.run();` with a no-op in a separate copy; strict Java compile succeeded, then the same checks failed at assertion **2** as intended.

**Unrun:** JDK25 (not present), full Gradle/NeoForge/renderer compilation (complete checkout and dependencies unavailable; clone DNS failed), real Vulkan/OpenGL GPU upload/driver failure, pixels and transparent water, save/reopen, resize/resource reload/world switch/shutdown, GPU memory and frame-time p50/p95/p99, AMD/NVIDIA hardware, external independent regrade, JAR and release. **No measured FPS, VRAM improvement or native acceptance is claimed.** Owner retains full-build responsibility. Next repair: native forced upload/close failure injection on both backends; then matched owner GPU metrics and temporal FSR/DLSS prerequisites.


<!-- END PRESERVED SOURCE -->
