# Expanded replica — docs/VOXEL-ENGINE-REFERENCES.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VOXEL-ENGINE-REFERENCES.md) |
| Source identity | git: docs/VOXEL-ENGINE-REFERENCES.md |
| Preserved source SHA-256 | `e7bde47f01fe1a4025c5d77e7eb6ce9f1bff63b47a01380f4c7f4bfe20e2fa91` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

A focused reference shortlist separates voxel/game-engine lessons from component libraries and authoring tools. It already discusses Voxel Tools, Luanti, Veloren, Vintage Story, Voxel Plugin, OpenVDB, and Voxel Farm with applicability limits, plus concrete prototypes and realism/acceptance boundaries rather than automatic dependency adoption.

## Research expansion — gaps and details to deepen

- The useful next layer is a decision matrix connecting each reference to an actual project owner, data contract, experiment, cost ceiling, and rejection criterion. The current shortlist provides directions, but a reader still needs to know which idea addresses which measured Deep Primal bottleneck.
- Blocky reduction, smooth-surface transition algorithms, hierarchical proxies, and volumetric authoring representations have different topology and edit assumptions. The expanded notes should prevent a technique name from being treated as a drop-in solution for opaque/cutout/water chunk-derived geometry.
- Offline design-software lessons need an import and reproducibility boundary. Terrain/material authoring can inform assets and validation scenes without replacing deterministic generator authority, saved-world versioning, client delivery restrictions, or the finite-water runtime.

## Research expansion — proposed contracts

- Add an evidence-to-experiment record with source date/version, inspected material depth, supported mechanism, project constraint, prototype inputs, correctness oracle, resource measurements, and adopt/defer decision. Reuse the lighting notebook for GI/PBR instead of duplicating its broad engine survey.
- Create comparable geometry fixtures for caves, cliffs, overhangs, cutouts, emissive blocks, and water boundaries. Evaluate each candidate on preserved opening topology, source revision handling, seam behavior, material/light fields, edit amplification, and replacement memory before measuring speed.
- Define an authoring import manifest: units, coordinate axes, color space, material identifiers, provenance, generator compatibility, and deterministic conversion version. Distinguish preview-only assets from runtime authority and expose reproducible invalid-input diagnostics to tool authors.

## Research expansion — acceptance checks

- For each proposed prototype, verify a specific source-supported mechanism and a project-specific failure case. Reject comparisons that use a static smooth mesh to claim editable block-terrain coverage, or a baked scene to claim dynamic cave and water behavior.
- Run candidate and reference implementations on identical source fixtures and record topology/material parity, CPU work, peak pending/resident memory, and visible replacement latency. An unimplemented or unmeasured candidate stays research; source availability and impressive external screenshots do not satisfy native acceptance.

## Research expansion — source anchors

- What we should extract through prototypes
- Realism and acceptance boundaries

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Voxel-engine research — performance and natural worlds

Research shortlist, **2026-10-21 PDT**. These projects are technical/design references for Deep Primal's own engine. No engine replacement, dependency, source import, benchmark result or completed integration is implied. Official author documentation was reviewed; broad source audits remain future work.

There is no demonstrated universal winner for our combination of editable Minecraft blocks, geological placement, flowing water, tall worlds and distant terrain. Treat rendering/storage performance and geological realism as separate questions with connected interfaces.

## Priority shortlist

| Project | What to study | Application and limits for Deep Primal |
| --- | --- | --- |
| **Voxel Tools for Godot** | Chunk paging, worker jobs, mesh/update budgets, edit access patterns and blocky meshing; its smooth-terrain LOD path is a separate case. | Highest-priority runtime reference. Study batching, job ownership and main-thread admission. Preserve Minecraft block silhouettes and edited terrain. Its documentation explicitly says blocky LOD has less support than smooth meshing; do not adopt Transvoxel merely because a smooth demo looks good. |
| **Luanti, formerly Minetest** | A voxel game platform with native map generation, isolated generation threads and bulk voxel manipulation. | Highest-priority block-world architecture reference: generation thread boundaries, bulk writes, sparse-region loading and scripting separation. Study implementation tradeoffs rather than assuming C++ automatically beats our Java path. Its whole engine is not a Minecraft library. |
| **Veloren** | Voxel-world generation organized around geological inputs, a simulated terrain stage and later filling/detail. | Strong regional-worldgen reference: share coarse fields instead of recomputing unrelated terrain decisions. Its documented erosion/world-simulation workflow does not mean we need a live sediment solver. Adapt finite/precomputed-map assumptions to bounded regions before using ideas for continuing exploration. |
| **Vintage Story** | Geology-oriented generation stages and data-driven content, exposed through documented server/worldgen APIs. | Strong grounded-world reference for connecting rock provinces, caves, layers, deposits and ecology. Evaluate behavior and publicly documented components under their own terms; do not infer that the whole game engine is reusable. Our fault/intrusion rules must exceed a simplistic fixed ordering of rock classes where realism requires it. |
| **Voxel Plugin 2 for Unreal** | Bounded terrain stamps, visual graph profiling, metadata/material assignment and voxel authoring/debug tools. | Secondary architecture/editor reference. Learn constrained evaluation and visual diagnostics for our companion editor. It is Unreal-specific and evolving; a blocky rendering option does not prove Minecraft-compatible editing, fluids, persistence or LOD. No direct integration is selected. |

Recommended study order: Voxel Tools and Luanti for runtime architecture; Veloren and Vintage Story for terrain-generation organization; Voxel Plugin for authoring/profiling workflows. This prioritization is our assessment of fit, not a measured ranking of engine speed.

## Primary reading list

| Project | Official sources checked |
| --- | --- |
| Voxel Tools | [Author repository](https://github.com/Zylann/godot_voxel), [performance guidance](https://voxel-tools.readthedocs.io/en/latest/performance/), [VoxelLodTerrain limits](https://voxel-tools.readthedocs.io/en/latest/api/VoxelLodTerrain/). The performance page contains older Godot/OpenGL-specific discussion; do not transplant old driver workarounds into current Minecraft. |
| Luanti | [Engine repository](https://github.com/luanti-org/luanti), [custom mapgen threading](https://docs.luanti.org/for-creators/mapgen/custom-lua-mapgen/), [bulk voxel API](https://api.luanti.org/lua-voxel-manipulator/). |
| Veloren | [Worldgen documentation, marked WIP](https://veloren.gitlab.io/book/internals/worldgen/worldgen.html), [world-simulation source](https://gitlab.com/veloren/veloren/-/blob/master/world/src/sim/mod.rs). Source is a follow-up audit target, not a claim of a complete code review. |
| Vintage Story | [Official worldgen API](https://apidocs.vintagestory.at/api/Vintagestory.API.Server.IServerEventAPI.html), [project-hosted terrain-generation reference](https://wiki.vintagestory.at/Terrain_Generation). Confirm important implementation details against actual versioned source/native behavior rather than treating a descriptive page as a correctness proof. |
| Voxel Plugin 2 | [Official docs](https://docs.voxelplugin.com/), [generation performance](https://docs.voxelplugin.com/technical-information/profiling-and-debugging/generation-performance), [blocky rendering limitations](https://docs.voxelplugin.com/knowledgebase/materials/flat-normals-and-blocky-rendering), [licensing](https://docs.voxelplugin.com/resources/licensing). |

Supporting storage research: [OpenVDB](https://www.openvdb.org/) provides sparse volumetric data structures and tools. It is a component research target rather than another complete game engine. Test sparse fields/occupancy summaries against our existing palette/columnar data before adopting a native dependency. Sparse representation alone does not solve fluids, lighting, multiplayer, chunk saves or worldgen.

Voxel Farm's current public offering emphasizes cloud spatial/mining datasets. It may inform volumetric-geology/data-streaming research, but it is a lower-priority fit for an offline Minecraft runtime. See [current author site](https://www.voxelfarm.com/); do not assume older game SDK descriptions reflect a current drop-in product.

## What we should extract through prototypes

- [ ] **Streaming and scheduling:** compare worker/main-thread splits, bounded admission, cancellation and revision rejection. Demonstrate exploration plus continuous edits without queue starvation.
- [ ] **Voxel access/storage:** compare batch/chunk-local queries, empty/uniform-region shortcuts, compact material fields and cache residency. Avoid per-voxel script, allocation, file-I/O or Java/native crossings.
- [ ] **Meshing and distance:** compare blocky reduction, material-aware geometry, shared edges and edit latency under explicit error/visibility budgets. Keep water, cutout vegetation and lighting semantics correct.
- [ ] **Worldgen organization:** derive geology, catchments, soil and placement from shared immutable regional summaries. Use deterministic source versions and boundary contracts; do not require pregenerating a finite entire planet.
- [ ] **Visual authoring:** evaluate preview/profiling tools with the same schema/core sampler as runtime; an attractive preview must match native generation before it certifies a setting.

Use small Deep Primal prototypes for one technique at a time. A separate engine's benchmark can suggest a target, but only matched in-mod measurements establish a benefit here. Profile storage, generation, mesh building, uploads, lighting, fluid work and networking separately so one optimization does not merely move the bottleneck.

## Realism and acceptance boundaries

The accepted [terrain mechanics](TERRAIN-MECHANICS.md) and [water budget](WATER-BUDGET-REQUIREMENTS.md) govern the result: geological ores/gems, connected mountain rivers/springs, realistic soil/drainage, no sinkholes, optional/default-off live sediment transport, and stable average ocean level with accounted local water. A technically capable reference engine does not override those choices.

Keep the Minecraft block aesthetic and the max-64×64/shared-texture tint direction. Smooth-voxel surfaces and high-resolution demo assets are optional reference techniques, not a new product direction.

For each retained idea, record a source version, exact responsibility, native implementation owner, applicable license/notice obligations, correctness fixtures, fixed-camera captures and named-hardware budgets. Source availability does not imply unrestricted redistribution; no third-party source is adopted by this document.

The core comparative suite should cover cold/warm exploration, tall terrain, cave/mountain-river passages, coasts, fluid edits, large ore definitions, teleports, reload/reconnect and multiplayer contention. Record p50/p95/p99 frame and tick times, chunk readiness, throughput, RAM/VRAM, cache/network costs and edit lag. Validate on the owner's AMD platform when available; software-driver checks are not certification of that hardware.

## Relationship to existing references

Continue using TFC/GTCEu for geology/ore authoring, Terralith/Tectonic for landform examples, Gaea/World Machine for regional terrain/drainage/material techniques, and Distant Horizons/Cesium/Nanite/meshoptimizer for distance/rendering components. The new voxel shortlist adds implementation ideas; it does not replace that research or supply an all-in-one realism engine.

See [engine charter and quality gates](LOD-AND-DRAINAGE-ARCHITECTURE.md), [configuration/ore authoring](CONFIGURATION-AND-ORE-AUTHORING.md), [module boundaries](MODULES.md) and the [roadmap](../roadmap.md).


<!-- END PRESERVED SOURCE -->
