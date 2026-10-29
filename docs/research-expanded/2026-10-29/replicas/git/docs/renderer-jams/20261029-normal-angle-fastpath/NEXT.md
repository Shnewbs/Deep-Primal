# Expanded replica — docs/renderer-jams/20261029-normal-angle-fastpath/NEXT.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-normal-angle-fastpath/NEXT.md) |
| Source identity | git: docs/renderer-jams/20261029-normal-angle-fastpath/NEXT.md |
| Preserved source SHA-256 | `aadf5a417c38c0e896085301d2c324c14706d355580b140bd95ebb1c119110d6` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Continues after jam30’s bounded normal-angle optimization, prioritizing owner-native fixed/adaptive/off frustum comparisons, measured mesh/GPU throughput, useful coverage, and temporal-input prerequisites. It keeps the source-only checkpoint and separate engine grade explicit.

## Research expansion — gaps and details to deepen

- The later scene-pixel LOD change makes reconstruction comparisons alter selected geometry as well as render resolution. The follow-up capture plan should identify this coupling and carry numeric correctness from jam29 instead of treating the arithmetic optimization as the next performance baseline by itself.

## Research expansion — proposed contracts

- Add a fixed-source experiment manifest with effective scene height, selected LOD counts, startup culling policy, and ordinary/extreme-angle coverage. Separate end-to-end quality-mode comparisons from experiments intended to isolate culling or upscaler cost, using equivalent geometry where required.

## Research expansion — acceptance checks

- Qualify the actual artifact’s camera adapter and angle fallback before profiling. Run matched three-way views, scene-resolution variants, lifecycle, and water images; retain native coverage, GPU timing, and temporal-provider dispatch as distinct gates after the source arithmetic result.

## Research expansion — source anchors

- Scoped renderer backlog after jam 30

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Scoped renderer backlog after jam 30

1. **Highest priority native evidence:** owner-run Java25 Gradle/Minecraft and matched Vulkan/OpenGL fixed/adaptive/disabled coarse page-frustum A/B, moving/turning cameras, all-visible and 80%-offscreen views; frame CPU/GPU p50/p95/p99, draw calls, VRAM/RAM and transparent-water pixel comparison. No native acceptance from source tests.
2. **Mesh/GPU throughput:** profile actual submission, descriptor/upload/scratch allocation, worker queue, fence-safe buffers and constrained meshoptimizer semantics before selecting a real bottleneck. The present broadphase is CPU culling, **not** GPU indirect.
3. **Useful coverage and streaming:** native edit invalidation, nearby/full-detail handoff, negative seams, cache bounds and authoritative unexplored distant tiles; do not merely lift sliders or host tracking limits.
4. **Temporal upscaling:** preserve real FSR1 spatial. Acquire valid jitter, motion vectors, preserved world depth, frame history and vendor runtime/extension/licensing requirements before activating temporal FSR or NVIDIA DLSS SR. Frame generation remains separate.
5. **Acceptance and owner builds:** packaged Java25 NeoForge/client lifecycle, water alpha/surface order, resource reload/resize/world-switch/save/reopen/shutdown, GPU captures; release only if owner authorizes a qualified artifact.

**Latest source checkpoint:** [jam30 `ab15e44cdad0a40966b6682f06b486031bb3aa5e`](https://github.com/Shnewbs/Deep-Primal/commit/ab15e44cdad0a40966b6682f06b486031bb3aa5e); source-only self-audit **5.73/10** (jam29 5.72); engine grade remains separate.


<!-- END PRESERVED SOURCE -->
