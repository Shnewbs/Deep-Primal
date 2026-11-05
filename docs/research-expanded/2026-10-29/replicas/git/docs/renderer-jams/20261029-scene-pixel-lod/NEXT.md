# Expanded replica — docs/renderer-jams/20261029-scene-pixel-lod/NEXT.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-scene-pixel-lod/NEXT.md) |
| Source identity | git: docs/renderer-jams/20261029-scene-pixel-lod/NEXT.md |
| Preserved source SHA-256 | `357871422fdc3ab705a505955720b73e6f62b15b38f7cdc50c0f365006f36156` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Prioritizes actual native 1080p, FSR 1 Quality with a 720p scene, and half-resolution spatial comparisons after scene-pixel LOD selection. It requires effective height/target telemetry, silhouette and water checks, resource metrics, and separate temporal-provider prerequisites.

## Research expansion — gaps and details to deepen

- Changing scene resolution now also changes LOD choice, so an upscaling comparison can combine pixel-work savings with different geometry. The continuation needs distinct experiment objectives and post-reconstruction image acceptance, rather than treating all speed differences as provider cost.

## Research expansion — proposed contracts

- Add two capture classes: end-to-end effective quality modes with their actual selected LOD distribution, and controlled geometry comparisons for isolating reconstruction or culling cost. Record output/render extents, error units, active target generation, silhouettes, and near/far handoff.

## Research expansion — acceptance checks

- Execute the listed native/720p/half-scale scenes with resize, fallback, and world reset; verify diagnostics match the target used by that extraction. Compare reconstructed silhouette, cave opening, thin geometry, seams, and water images alongside complete CPU/GPU and memory results.

## Research expansion — source anchors

- Renderer jam 31 follow-up backlog

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 31 follow-up backlog

1. Owner Java25 Gradle/NeoForge and packaged native OpenGL/Vulkan identical-camera scenes: native 1080p, FSR1 Quality 720p scene, half-resolution spatial. Verify lodErrorViewportHeightPixels / lodErrorWorldTargetPresent at extraction and reset; compare LOD silhouettes, negative seams, near/full-detail handoff and transparent water pixels.
2. Capture named GPU/driver/scene/seed, camera/travel/edit, CPU/GPU frame p50/p95/p99, draw/descriptor count, upload/scheduling/culling, warm/cold RAM/VRAM and resident/pending/cache budgets; run fixed/adaptive/off page broadphase comparisons and record requested/effective full-detail, vertical, LOD and independent simulation distances.
3. If profiled, optimize mesh reuse, bounded worker/upload, actual supported GPU submission and fence-safe allocation. A synchronous drawMultipleIndexed API call is not GPU multi-draw indirect.
4. Preserve V1–V7, player edits, lighting, translucent water. Do not advertise unexplored remote terrain without an authoritative delivery contract and producer/network prerequisites.
5. Temporal FSR and real NVIDIA DLSS SR still need correct jitter, preserved world depth, moving-object motion vectors, temporal history, vendor dispatch, capabilities/runtime packaging/licensing. FSR1 is spatial; frame generation is separate.

Jam source [cf578b2e36fcdbde04320cc2564c5b1cd89004d6](https://github.com/Shnewbs/Deep-Primal/commit/cf578b2e36fcdbde04320cc2564c5b1cd89004d6), source-only renderer grade 5.74 (+0.01). Whole-engine grade unchanged. No owner Java25/GPU/JAR tests in this run.


<!-- END PRESERVED SOURCE -->
