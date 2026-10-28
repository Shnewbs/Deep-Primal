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
