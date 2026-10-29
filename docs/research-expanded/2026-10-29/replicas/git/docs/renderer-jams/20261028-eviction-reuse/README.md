# Expanded replica — docs/renderer-jams/20261028-eviction-reuse/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-reuse/README.md) |
| Source identity | git: docs/renderer-jams/20261028-eviction-reuse/README.md |
| Preserved source SHA-256 | `93660af15df280a4a1d757d7872c0a1cd81eacaa02e784c65c974ddb7f160e9e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reuses four primitive eviction arrays at a fixed 4,096-entry capacity, lazily creates them, rebuilds in place, and releases them on world reset. The source reports lower isolated selector allocation and modest CPU benefit while retaining about 86 KiB until reset.

## Research expansion — gaps and details to deepen

- The fixed scratch capacity is coupled to the renderer resident cap. Any future cap change needs coordinated validation, and reduced per-event allocation must remain separate from retained heap, actual device memory, and complete-frame benefit.

## Research expansion — proposed contracts

- Define resident-cap compatibility, single-owner reset/rebuild semantics, lazy allocation, and a bounded failure path for oversized input. Publish retained scratch bytes separately from eviction allocations and include world-generation reset in the ownership receipt.

## Research expansion — acceptance checks

- Retain the 761,183 combined reuse/parity assertions and missing-reset negative control. On the real artifact, compare pressure-event allocation, retained heap, GC, frame tails, GPU retirement, and water pixels across repeated world switches.

## Research expansion — source anchors

- Renderer jam 11 — reusable LOD GPU-cache eviction scratch

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 11 — reusable LOD GPU-cache eviction scratch

Jam `renderer-20261028-eviction-heap-reuse-11`; initial main `9e2de9b28600613682680049b8be9f2cdbabacc6`; reserved renderer-only claim `d2a12c5a30a0d95a7c8a2ea8aa926213628e6a29`. Source implementation [87a38079](https://github.com/Shnewbs/Deep-Primal/commit/87a38079b72a9934fc6dc2d1537760853b119c1b); parent claim SHA d2a12c5. Only renderer-owned files changed; no water/terrain, build config, or independent audit modifications.

The [victim heap](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L27-L65) now reserves 4,096 entries and resets in place, preserving stable distance/protected ordering and avoiding four new primitive arrays on each pressure event. The [render-thread caller](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L566-L580) lazily creates the selector and drops it on [world reset](https://github.com/Shnewbs/Deep-Primal/blob/87a38079b72a9934fc6dc2d1537760853b119c1b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L821-L829). No GPU buffer lifetime, geometry, water material or settings behavior changed.

**Tested:** Java21.0.12.1 `javac -Xlint:all -Werror`; `java -ea -Xverify:all`; 509,243 new reusable-heap assertions plus 251,940 legacy parity assertions = **761,183 PASS**. A no-reset source mutant fails at assertion 385 as expected. Pinned production helper blob `7fff32140b922b065bc3ee60f4ad2c6573399ca3` matches compiled helper bytes; integrated renderer blob `9e912435f85caa3238c7733d3823e2fbf4e355da` passed post-push static binding inspection.

**Matched CPU diagnostic:** 4,096 resident pages, one victim, 5,000 warm-ups + 640 alternating timed pairs. First run p50 65,420 ns baseline vs 53,777 ns reusable; repeat 66,265 vs 55,879. Median thread allocation 86,112 vs 0 bytes per eviction selection. Retained bounded arrays ~86 KiB until world switch. These are **CPU microbenchmarks**, not game FPS/VRAM improvement.

Full test sources, logs, negative control, measured evidence and source SHA manifest: **DeepPrimal_Renderer_EvictionReuse_20261028_87a3807_SourceEvidence.zip**, verified ZIP CRC + SHA256 `5991b05e8efa68ef74df6378dc61e224065bb6cce412c2bf3949206387d7d0d8`, 15,071 bytes; uploaded to Library `/Deep Primal/Renderer Jams/`.

**Not run:** Java25 (archive unavailable in this isolated run), full Gradle/NeoForge/native client, save/reopen/teleport/edit, Windows GPU Vulkan/GL/water pixels, p50/p95/p99 GPU frame/VRAM, FSR3/4, DLSS RTX and JAR packaging. Per owner override, no Gradle/Actions/release attempted; source-only, no new JAR.

**Next:** owner-native measured Vulkan/GL cache-pressure travel/edit/reopen, upload stalls/VRAM/water transparency, then target real GPU batching and bounded LOD refinement. Scope released at this checkpoint.


<!-- END PRESERVED SOURCE -->
