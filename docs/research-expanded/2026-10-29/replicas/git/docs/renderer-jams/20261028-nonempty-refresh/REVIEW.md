# Expanded replica — docs/renderer-jams/20261028-nonempty-refresh/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-nonempty-refresh/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-nonempty-refresh/REVIEW.md |
| Preserved source SHA-256 | `c248c734ab041160b0c4064863468fa2386adf6fca4de6e72eaeb63ac3a93c51` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds successful-nonempty directory refresh after 400 client ticks, retaining separate empty polling, missing/error backoff, 64-chunk retarget, and bounded scan/admission. It closes a reproduced stationary-camera case where a newly saved page remained undiscovered. The callback p99 increased in the reported diagnostic; the repaired discovery behavior is not a frame-speed claim.

## Research expansion — gaps and details to deepen

- Polling cadence is measured in client ticks and full scans still enumerate every filename. The next improvement needs trustworthy producer publication and measured huge-save I/O cost, while preserving periodic recovery for missed notifications and external changes.

## Research expansion — proposed contracts

- Define a saved-page publication event with world identity, revision, canonical key, and durable-write completion. Treat it as a prompt discovery hint, not authority to bypass loaded revision guards; retain bounded reconciliation and report arrival-to-visible age.

## Research expansion — acceptance checks

- Retain stationary-nonempty negative controls and saturation tests. Exercise stopped ticks, long scans, repeated file replacement, deletion, notification loss, and world reset in the native client; measure extra polling CPU/I/O and visible latency rather than promising a wall-clock deadline.

## Research expansion — source anchors

- Renderer jam 20261028-nonempty-refresh — read-only six-category self-audit

This original shares identical bytes with: [library:Renderer Jams/20261028-nonempty-refresh/REVIEW.md](../../../../project/Renderer-Jams/20261028-nonempty-refresh/REVIEW.md). Both source identities are retained.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-nonempty-refresh — read-only six-category self-audit

**Exact audited source:** [`3478cf5d8f44fcc0cdcced2576f12704732f44f0`](https://github.com/Shnewbs/Deep-Primal/commit/3478cf5d8f44fcc0cdcced2576f12704732f44f0), version `0.0.1-alpha.29-dev.49-v8snapshot.1`. This scored review was done after production Java source was frozen and tested; it is **renderer self-review, not independent whole-engine audit**. The previous pinned renderer [5.08/10](../20261028-recovery-epochs/REVIEW.md) is the comparison. The independent [08:30 UTC auditor](../../engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md) reproduced the stationary-nonempty issue. No post-score production edits were made.

| Equal-weight renderer category | /10 | Pinned evidence and missing gates |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.5** | [400-tick nonempty schedule](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L19-L24), [success handling](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L114-L144), [real-folder/cap regression](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/docs/renderer-jams/20261028-nonempty-refresh/LodDiskNonemptyRefreshChecks.java#L53-L110), two baseline-negative tests. No full native/visual coverage or authoritative unexplored delivery. |
| Mesh generation/reuse/scheduling | **6.0 (carried)** | Unchanged [256 entry/turn worker budget and 8192-key capacity](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L20-L26), [owner application budget](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L168-L205); no new greedy mesh/reuse/worker throughput or native performance evidence. |
| GPU culling/submission/memory | **4.0 (carried)** | [Commit changes](https://github.com/Shnewbs/Deep-Primal/commit/3478cf5d8f44fcc0cdcced2576f12704732f44f0) touch metadata only; no GPU submission, VRAM/buffer pooling or indirect draw work, no measured GPU output. |
| Actual FSR/DLSS implementation | **5.0 (carried)** | [Existing FSR1 spatial kernel/NGX inactive contract](../../UPSCALING.md) unchanged. No temporal inputs/provider dispatch, licensed packaged DLSS runtime or RTX acceptance. No frame generation. |
| Visual/backend/settings compatibility | **5.0 (carried)** | Only LOD index and source-only regression files changed. No shaders, render settings, water surfaces/order, Vulkan/GL code, resource IDs or generation revisions edited; native reload, save/reopen and water pixels unrun. |
| Validation and measured performance | **4.5 (carried)** | [Exact-source tests and negative controls](README.md): Java25 **26,318**, Java21 **26,359** focused assertions, two expected old-source failures. 900-tick CPU callback diagnostic reveals added polling overhead (candidate p99 **73,501 ns**, prior **43,886 ns**), not FPS/whole-frame evidence; full Gradle and hardware unrun. |
| **Overall** | **31.0/60 = 5.1667 ≈ 5.17/10** | Previous 30.5/60 = 5.0833 ≈ 5.08. Exact improvement **+0.0833/10**, not native quality acceptance. |

**Read-only conclusion:** the new policy closes the independently reproduced stationary-nonempty saved-page rediscovery case under tested source/real-filesystem scenarios. It does not prove GPU-visible terrain correctness, file-system performance on huge saves, prompt discovery within a wall-clock deadline, native resource safety or an improved frame rate. Scores remain far below the >8.5 goal; no absent vendor integration receives fresh implementation credit. The whole-engine ten-category 5.0/10 is carried without regrade.

Next: owner-native LOD save/reopen/teleport/edit and transparency tests; collect matching Vulkan/OpenGL p50/p95/p99 frame, VRAM, upload and cache traces; evaluate a write/publication-driven immediate rediscovery signal before increasing polling frequency. Then continue temporal FSR/DLSS and effective-distance controls after required GPU/inputs/contracts are present. No new JAR or release is claimed.


<!-- END PRESERVED SOURCE -->
