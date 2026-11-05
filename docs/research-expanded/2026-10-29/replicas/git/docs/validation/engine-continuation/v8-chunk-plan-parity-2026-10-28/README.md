# Expanded replica — docs/validation/engine-continuation/v8-chunk-plan-parity-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-chunk-plan-parity-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-chunk-plan-parity-2026-10-28/README.md |
| Preserved source SHA-256 | `c9457a78c8a836241819ddc13c2c2ebe2a23416881f8d44c232c2ad1a03c1bfa` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev44 explicit DensityChunk.samplePlanned using certified solid or empty boxes and exact job-owned sampling for every uncertain voxel, with matching BitSet and top-exclusive heightmaps. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the slab planner remains non-native and its classification budget is not a worldgen performance grade. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an occupancy certificate consumption contract requiring identical coordinate ownership, slab limits and exact fallback for all uncertain classifications. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare planned occupancy and heightmaps to scalar exact generation across slab boundaries, signed coordinates and exhausted classification budgets before qualifying native chunk materialization. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev44: opt-in exact V8 density chunk occupancy and heightmap parity

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev44: opt-in exact V8 density chunk occupancy and heightmap parity

Version: 0.0.1-alpha.29-dev.44-v8chunk.1. Source-only checkpoint; no qualified JAR.
Consumed independent audit: docs/engine-hourly-audits/2026-10-28T0233-2805140/REVIEW.md, pinned 28051408b079d3f378083554a6fb789ee43e5124.
Last full independent grade: 50/100 (5.0/10), historical carried value, not regraded.

## Concrete implementation

DensityChunk.samplePlanned is an EXPLICIT OPT-IN bridge between DensityVolumeBounds / DensityVolumePlan and the original exact chunk BitSet and top-exclusive heightmap. Each independent slab has at most 64 Y cells and 0..256 classification calls. Certified solid/air boxes avoid per-voxel exact samples; every uncertain voxel uses the same job-owned exact DensityGraph.Sampler as the baseline. Column-major z/x/y bits, full-solid/air cases, zero-is-air, cave roofs, and cross-slab top-exclusive heightmaps are preserved. Metrics report classified and exact cells, not time. Default DensityChunk.sample remains unchanged and native world generation does not yet invoke the optional planner.

V1-V7 frozen geography, saved profiles, edits, water/constituent accounts, approved art/Themes, graphics/LOD/Vulkan modules are not modified. Query caps are not a proven hard elapsed-time budget.

## Actual scoped execution and negative controls

On Linux x86_64 OpenJDK/javac 21.0.12.1: javac --release 21 -Xlint:all -Werror, java -ea -Xverify:all. Exact original Git blob input identities verified for actual DensityGraph (95ff21354), SeededNoise3D (130e339ae), NoiseSpec (67af7fba), DensityField (ae21097a), TerrainShapeV8 (2c63c39e), DensityVolumeBounds (a8f811a), DensityVolumePlan (8b7f764), and initial DensityChunk (be014224). Edited DensityChunk Git blob c98ee12b and new test blob b578e484.

DENSITY_CHUNK_PLAN_PASS assertions=1218333 voxels=1208064 certifiedSkipped=902118 maxClassificationQueries=362 exactProductGraph=true

Every voxel, solid count, byte payload, top-exclusive surface and iterator order was compared to unmodified DensityChunk.sample across zero and nonzero budgets, signed limits, 64-high slab seams, noise, nonlinear ABS cavities, warp, random chunks, and both actual experimental V8 recipes with and without floating features across seeds 73 and 20261020. 902118 is the number of certified cells avoiding exact sampler calls across this deliberately selected fixture corpus, NOT a speedup or native acceptance.

Five deliberately broken, compiled source variants were each rejected: omitted certified solids, invented solids in empty tiles, off-by-one surface, zero-is-solid, and X/Z column-address transposition.

## Unrun acceptance and continuation

Owner's Oct 27 override assigns full Java25/Gradle/Minecraft/GPU builds to the owner. The owner JDK25 archive and installed toolchain were not mounted; Java25 compilation, Gradle/modules, Minecraft native V8 chunk/base-column/heightmap, saved-profile/reopen, Windows AMD8060S Vulkan/OpenGL and performance/soak gates were not run. No Actions, JAR or release created. The V8 planner remains non-native and receives no worldgen performance grade.

Next: owner Java25/full-core + native chunk and saved profile/edits/reopen/terrain seam parity, then matched p50/p95/p99/allocations before any native opt-in activation. Separate finite-water callback/ledger, loaded-unloaded, temporal waterfall and GPU gates stay open. Independent auditor controls the >8.5/10 stopping decision.


<!-- END PRESERVED SOURCE -->
