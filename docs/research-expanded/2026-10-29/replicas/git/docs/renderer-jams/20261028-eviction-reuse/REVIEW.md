# Expanded replica — docs/renderer-jams/20261028-eviction-reuse/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-reuse/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-eviction-reuse/REVIEW.md |
| Preserved source SHA-256 | `133ed092e57e8675f3aeafc72b4881494a44c2649acf5eb3495131c50ecb7f2b` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reuses four primitive eviction arrays at a fixed 4,096-entry capacity, lazily creates them, rebuilds in place, and releases them on world reset. The source reports lower isolated selector allocation and modest CPU benefit while retaining about 86 KiB until reset. The 86,112-to-zero allocated-byte result concerns the CPU selector, not reduced VRAM.

## Research expansion — gaps and details to deepen

- The fixed scratch capacity is coupled to the renderer resident cap. Any future cap change needs coordinated validation, and reduced per-event allocation must remain separate from retained heap, actual device memory, and complete-frame benefit.

## Research expansion — proposed contracts

- Define resident-cap compatibility, single-owner reset/rebuild semantics, lazy allocation, and a bounded failure path for oversized input. Publish retained scratch bytes separately from eviction allocations and include world-generation reset in the ownership receipt.

## Research expansion — acceptance checks

- Retain the 761,183 combined reuse/parity assertions and missing-reset negative control. On the real artifact, compare pressure-event allocation, retained heap, GC, frame tails, GPU retirement, and water pixels across repeated world switches.

## Research expansion — source anchors

- Renderer jam 11 — exact-source read-only self-audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 11 — exact-source read-only self-audit

Implementation candidate [87a38079](https://github.com/Shnewbs/Deep-Primal/commit/87a38079b72a9934fc6dc2d1537760853b119c1b), helper blob `7fff32140b922b065bc3ee60f4ad2c6573399ca3`, renderer blob `9e912435f85caa3238c7733d3823e2fbf4e355da`. Post-push content was reviewed **read-only**; no code changed during scoring. The independent [Dev56 audit](https://github.com/Shnewbs/Deep-Primal/commit/9e2de9b28600613682680049b8be9f2cdbabacc6) confirms FSR3/4 still unavailable and carries whole-engine 5.0/10. This is renderer self-audit only, previous 5.33/10.

| Equal-weight category | Score /10 | Pinned evidence and unrun gate |
|---|---:|---|
| Distance / LOD usable coverage | **6.50 carried** | [Cache bounds](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L70-L85); no native far-world or unexplored delivery proof. |
| Mesh generation / reuse / scheduling | **6.75 (+0.25)** | [In-place heap rebuild](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L27-L65) and [renderer binding](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L566-L580); 761,183 parity/reuse assertions. |
| GPU culling/submission/memory | **4.00 carried** | [Ordinary indexed draw loop](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L749-L781); GPU MDI, VRAM and pixels not validated. |
| Actual FSR/DLSS | **5.00 carried** | [FSR policy](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L24-L33); FSR1 exists, temporal newer FSR/DLSS unintegrated. |
| Visual/backend/settings | **5.25 carried** | [World reset cleanup](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L821-L829); no native Vulkan/GL/water tests. |
| Validation and performance | **5.00 (+0.25)** | Java21 strict compile, **761,183** passing assertions and expected missing-reset mutant failure; two paired 4,096-page CPU runs p50 baseline/new 65.42/53.78 and 66.27/55.88 µs, allocated 86,112/0 bytes. Full Java25/native FPS/GPU unrun. |
| **Overall** | **32.50/60 = 5.42 (+0.09)** | Below target >8.5; whole-engine 5.0 carried separately. |

**Known limits:** capped at 4,096 snapshot candidates (coordinate with renderer resident cap if changed), ~86KiB array retention until session reset. The improved CPU selection/allocations do not demonstrate frame-rate, renderer GPU memory or native correctness improvements. Owner performs Gradle/Minecraft/JAR validation. No code was modified after the read-only review.


<!-- END PRESERVED SOURCE -->
