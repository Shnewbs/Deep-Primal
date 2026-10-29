# Expanded replica — docs/renderer-jams/20261028-gpu-upload-abort/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-gpu-upload-abort/README.md) |
| Source identity | git: docs/renderer-jams/20261028-gpu-upload-abort/README.md |
| Preserved source SHA-256 | `5e17ec36b50805b08a043475fec05ba3c74b1d1acf3250bd004dabb99ba98feb` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Makes partial LOD GPU upload unwind clear pending ownership before rollback, attempt all accepted-buffer closes, preserve the original failure, and avoid freeing completed cache buffers twice after superseded-entry close faults. Committed byte accounting is paired with accepted ownership.

## Research expansion — gaps and details to deepen

- The helper fixture proves exception and ownership order, but actual recorded/submitted GPU references must join the model. Logical rollback cannot imply immediate reuse of allocations still visible to a command buffer or device.

## Research expansion — proposed contracts

- Extend the ownership state machine with recorded, submitted, retired, and quarantined references. Define exactly-once transfer to a completed page, cleanup failure aggregation, and bounded renderer failure behavior; keep retries and device-loss recovery separately specified.

## Research expansion — acceptance checks

- Retain the 450,015 focused assertions and release-negative control. Inject failures after allocation, pending append, page commit, and old-entry cleanup in the actual backend; verify no double free, no stale publication, complete accounting, and native resource safety.

## Research expansion — source anchors

- Renderer jam 13 — fault-safe pending LOD GPU uploads

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-gpu-upload-abort/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 13 — fault-safe pending LOD GPU uploads

**Jam:** `renderer-20261028T1700-gpu-upload-failure-unwind`. Source [`61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd`](https://github.com/Shnewbs/Deep-Primal/commit/61f6405d55d05b0f1d762e50c3d6d6ef4e4486fd), parent claim `ad950094b844fe73b75d58ba07f52b6ae06cc137`. Audit consumed: [2026-10-28 16:28 UTC](../../engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md).

Changed production: `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java` and new `LodGpuUploadFailure.java`; reproducible `LodGpuUploadFailureChecks.java` lives here. A partial upload failure now clears pending ownership before rollback; accepted buffers are closed without masking the original exception; committed byte accounting is paired with accept, and completed cache buffers are never freed twice after superseded-entry close failure. No GPU retry or device-loss recovery is promised. Water geometry, shaders and other schedules unchanged.

Strict Java21 compilation/verification and **450,015** focused assertions passed; intentional no-release negative control failed as expected. Git source/test blobs matched. Whole module Java25/Gradle, Minecraft/Vulkan/OpenGL, FPS/VRAM, water pixels and JAR publishing remain unrun with the owner. See [REVIEW.md](REVIEW.md) and [RECEIPT.json](RECEIPT.json). Next: owner-native forced-failure and world/resize/reload validation plus real GPU frame-time baselines.


<!-- END PRESERVED SOURCE -->
