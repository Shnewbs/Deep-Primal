# Expanded replica — docs/renderer-jams/20261028-recovery-epochs/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-recovery-epochs/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-recovery-epochs/REVIEW.md |
| Preserved source SHA-256 | `078a8fa272697a22f27aefb37366703804d748a16d397c34d9f1be876829a144` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Protects post-snapshot recovery ownership with bounded renewal epochs and prevents stale scan completion from discarding a re-enqueued key. Reconciliation counts inspected candidates and mutations toward its turn budget, and successful-empty directories gain stationary polling. The Java21/Java25 filesystem fixture closes the scoped ownership case, not native terrain or GPU acceptance.

## Research expansion — gaps and details to deepen

- The epoch rule should join the wider world/device/source identity contract. Local renewal tokens protect queue ownership but do not alone prove old-world jobs, stale disk bytes, or GPU publications are rejected throughout the pipeline.

## Research expansion — proposed contracts

- Propagate immutable world-attachment and source-revision identity through scan, restore, mesh, upload, and publication. Preserve renewed keys under saturation, define overflow restart semantics, and link the later nonempty-refresh repair rather than treating this historical polling policy as complete.

## Research expansion — acceptance checks

- Retain renewal and empty-folder negative controls, then stress repeated remove/re-enqueue, overflow, retarget, world switch, and delayed completion in the actual client. Verify bounded worker retirement, authoritative edit precedence, and useful visible coverage.

## Research expansion — source anchors

- Renderer jam 20261028-recovery-epochs — read-only six-category self-audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-recovery-epochs — read-only six-category self-audit

**Pinned implementation:** [2931f6c7a839013c216f21386a62c72122042d83](https://github.com/Shnewbs/Deep-Primal/commit/2931f6c7a839013c216f21386a62c72122042d83); previous [renderer review](../20261028-disk-retry/REVIEW.md) **5.00/10**. This review was performed only after the implementation and exact-source JVM checks were complete; no production source was edited during scoring. It is **not** the independent whole-engine audit. Consumed [07:31 UTC external finding](../../engine-hourly-audits/2026-10-28T0731-5a23f28/REVIEW.md). No complete renderer hardware benchmark or native score is claimed.

| Equally weighted category | /10 | Evidence and limits |
| --- | ---: | --- |
| Distance/LOD correctness & usable coverage | **6.0** | [Per-snapshot renewal tokens](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L33-L49), [safe bounded discard/cap priority](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L159-L206), [stable-empty poll](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L121-L138) and [real-directory regression](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/docs/renderer-jams/20261028-recovery-epochs/LodDiskRecoveryEpochChecks.java#L24-L108). Fixed the independent source-level reproduction, not native Minecraft/visual completeness or unseen terrain delivery. |
| Mesh generation/reuse/scheduling | **6.0 carried** | Existing [256-per-turn bounded work](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L20-L26) retained, post-snapshot cleanup now bounded by the same budget. No new mesh geometry/reuse or game-worker throughput benchmark. |
| GPU culling/submission/memory | **4.0 carried** | [Only coordinator and caller LOD code changed](https://github.com/Shnewbs/Deep-Primal/commit/2931f6c7a839013c216f21386a62c72122042d83); no GPU backend, VRAM residency, indirect dispatch or hardware samples. |
| Actual FSR/DLSS | **5.0 carried** | [FSR1 EASU/RCAS and explicit DLSS nonactivation](../../UPSCALING.md) unchanged. No temporal FSR or verified NGX/DLSS Super Resolution; no frame generation. |
| Visual/backend/settings compatibility | **5.0 carried** | [Four caller recovery hooks](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java) checked against exact uploaded source. No shaders, transparency, UI, Vulkan/GL API state or configuration changed; native reload/save/pixel comparison unrun. |
| Validation/measured performance | **4.5 carried** | [Committed JDK regression](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/docs/renderer-jams/20261028-recovery-epochs/LodDiskRecoveryEpochChecks.java); Java25 22845 and Java21 22827 final focused assertions, extra unprivileged Java25 8, two expected negative-mutant failures. No matched GPU/whole-frame p50/p95/p99 or Java25 Gradle run; source-only checks cannot establish FPS. |
| **Overall** | **5.08/10** | **30.5/60**, **+0.08/10** from previous 5.00. Target strictly above 8.5 remains unproven. |

**Read-only conclusion:** a renewed persisted recovery cannot be evicted by a stale scanner completion in the tested cases, and the same fixed camera can rediscover the first file added after a successful-empty scan. Exact-source/Java tests pass, but not a full gaming, hardware or visual acceptance. No qualification, GPU score, FPS, JAR or release invented. The separate ten-category whole-engine **5.0/10 is carried forward** without regrading.

**Next blocking gates:** owner native save/reopen/teleport/edits, saturated background scan and cancellation, actual hardware-visible LOD/water pixel coverage; then measured upload/culling/frame p50/p95/p99 and temporal vendor upscaling.


<!-- END PRESERVED SOURCE -->
