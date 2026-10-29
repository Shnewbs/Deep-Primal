# Expanded replica — docs/RIVER-FLOW-COMPARISON-2026-10-19.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/RIVER-FLOW-COMPARISON-2026-10-19.md) |
| Source identity | git: docs/RIVER-FLOW-COMPARISON-2026-10-19.md |
| Preserved source SHA-256 | `b9b55ab89b22ffd38fef8bf50a0f9df440afe8d3499111ef3ef216f1c7e72f69` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This explicitly historical alpha 13 record explains stored directional river currents and supported stepped mountain channels in original code. It distinguishes examined TFC source from description-only references, documents the ownership split between MountainDrainage, world generation, registered river blocks, and a common vanilla-fluid vector hook, and records bounded cache/sample costs. It states that native boats and items were tested, not arbitrary ship or contraption integrations. Regional rather than global drainage, legacy water, source placement, and unlocated screenshot evidence are all explicitly limited; newer audits are designated as current coverage references.

## Research expansion — gaps and details to deepen

- Elaboration: stored current directions are described, but their lifetime after topology edits and relationship to measured finite-water motion need a historical-versus-future contract.
- Elaboration: the record reports bounded caches and seventeen waterline samples; reproducible manifests should preserve cold construction cost separately from warm vector lookup.
- Clarification: screenshot defects were not reproduced at their original locations. Expanded text should preserve that evidential limit and attach newly reproducible fixtures rather than upgrading the claim.

## Research expansion — proposed contracts

- Keep this document immutable as a release comparison and add a dated compatibility crosswalk from legacy directional water to later independent-fluid behavior. Identify which hooks, cells, world profiles, and saved states each statement covers.
- For future current consumers, distinguish authored route direction, numerical transfer velocity, rendered normal motion, and entity force. Their units, revision source, and applicable cells must be named; no persistent authored direction may imply ongoing supply after a reach dries.
- Invalidate or reclassify generated current metadata when a dam, excavation, enclosed passage, or source removal changes its governing topology. Unsupported migration preserves the old profile explicitly instead of silently repainting currents.
- Record drainage construction samples, cold/warm cache statistics, retained bytes, and vector-query cost separately. Cache eviction must preserve deterministic recomputation and cannot discard authoritative edits or fluid volume.
- Turn unsupported sheets and river-floor gaps into named fixtures with seed, profile, coordinate bounds, actual block columns, water levels, bank support, and screenshots. Keep original unlocated images as observations only.

## Research expansion — acceptance checks

- Follow a generated current through a flat reach, descending step, and chunk boundary; native entity and visual consumers must use the documented vector semantics.
- Dam and dry a routed reach; verify any retained authored direction cannot continue moving nonexistent water or powering gameplay.
- Generate the same region after cache eviction and in reversed chunk order; compare geometry and direction hashes.
- Capture both cold region construction and repeated warm queries under a fixed seed without combining their timings into one speed claim.

## Research expansion — source anchors

- Ownership and cost
- Deliberate limits
- **Historical release record.**
- Region boundaries are not a globally stitched watershed

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# River flow references — alpha 13

**Historical release record.** For the current independent alpha 20 fluid/terrain path and the expanded Streams Reflowing feature comparison, use the [2026-10-22 streams/canopy/puddles follow-up](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md) and [main audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md). Legacy current hooks described below do not establish full native-engine support.

The requested behavior is a stepped mountain river with a supported bed and a persistent downstream current, including across flat source-water stretches. Alpha 13 implements this in original Deep Primal code. None of the following mods or their assets is bundled or required.

| Reference | Evidence examined | Application and scope |
| --- | --- | --- |
| [TerraFirmaCraft 1.21.x](https://github.com/TerraFirmaCraft/TerraFirmaCraft/tree/1.21.x) | Actual [RiverWaterFluid](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/common/fluids/RiverWaterFluid.java), [RiverWaterBlock](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/common/blocks/RiverWaterBlock.java), and [Flow](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/world/river/Flow.java) source. These use stored directional river water and a direction-derived fluid vector. The inspected files carry EUPL-1.2 notices. | Adopt the behavior of persistent directional flow. Deep Primal uses its own numeric block-state direction and the existing vanilla water fluid, with one common fluid-vector hook. No TFC fluid, world generator, vector-interpolation implementation, particles or source is copied. Native entity push and native fluid rendering consume the same vector. |
| [Streams Reflowing](https://www.curseforge.com/minecraft/mc-mods/streams-reflowing) | Author description: downhill streams, elevated lakes, ocean-directed currents, momentum across source blocks, boat handling, biome-specific banks and decorative effects. Its performance notes describe terrain-query costs. Listing: All Rights Reserved; no source inspection. | Reference for continuity and containment. Alpha 13 checks intermediate bed/bank terrain, retains descending terraces, and fills to the native interpolated bed. Persistent currents do not require continuous terrain queries. Global ocean routing, elevated lakes, generalized vanilla-fluid momentum propagation, decorations and feature equivalence are not claimed. |
| [Underground Rivers](https://www.curseforge.com/minecraft/mc-mods/underground-rivers) | Author describes a rewritten underground-river generator with cave entrances and Lithostitched compatibility. Description only. | Distinguish underground channel geometry from persistent surface currents. This update protects a thin generated river floor and leaves deeper native aquifers intact. It does not add underground-river networks or a Lithostitched dependency. |
| [Terralith: River In Jungle](https://www.curseforge.com/minecraft/mc-mods/terralith-river-in-jungle) | Author describes broad winding lowland jungle rivers, sloping banks, material variety, chunk continuity and a hard Terralith dependency. Description only. | Reference for banks and terrain integration. Deep Primal retains its own biome source, native bed/surface rules and versioned terrain profiles. It does not install Terralith or claim an Amazon-style jungle biome overhaul. |
| [TFC Sable Rivers](https://www.curseforge.com/minecraft/mc-mods/tfc-sable-rivers) | Author describes a Minecraft 1.21.1 compatibility bridge making Sable floating contraptions react to TFC currents. MIT listing; description only. | Confirms that floating-structure compatibility is a separate integration concern. Native boats and dropped items are tested here. Sable/Create contraption integration is not included or certified. |
| [RiverInJungle](https://www.curseforge.com/minecraft/mc-mods/riverinjungle) | Author describes sea-level jungle river valleys, broad winding channels, graded banks and merges into existing water. Description only. | Reference for terrain-backed channels, rather than water placed above finished terrain. Deep Primal additionally retains elevated stepped tributaries. This does not establish identical geometry or biome behavior. |

## Ownership and cost

`engine-core/MountainDrainage` owns deterministic drainage geometry. Root `worldgen` integrates the native density grid and surface aquifer; root `water` owns the registered river block and world-synced current settings. The common `RiverCurrentMixin` returns its stored direction through vanilla fluid flow. There is no per-water-block entity, scheduled hydrology solver, terrain-search loop during entity movement, custom packet stream or additional generation executor.

The existing 32-region cache stays bounded. The contained profile adds seventeen waterline samples per selected routing segment, computed from its center and both banks during region construction. This increases cold terrain work and bounded cache memory; it is not a free generation optimization. Warm fluid-vector lookup uses one block-state lookup and a cached vector at default strength. The per-voxel aquifer reads precomputed column arrays. No graphics, performance-engine or Vulkan implementation changes are needed for water physics.

## Deliberate limits

Currents belong to the new profile's generated routed channels, including its selected sea-level reaches. Oceans, pools and unselected legacy lowland channels retain vanilla water behavior. Directions follow the routing graph; lowland height remains sea level. Deep water and wet region exits seed drainage, with a real wet minimum as fallback in an enclosed regional basin. Region boundaries are not a globally stitched watershed, and an ocean outlet is not guaranteed. Player-placed vanilla water is not assigned a current. Previously generated chunks are not repaired or converted.

The screenshots supplied for this update were inspected successfully. They show unsupported water sheets and gaps beneath water. Their seed and coordinates were not supplied, so validation covers reproducible independent seeds and whole chunk columns rather than claiming to reproduce those exact locations. See [native checks and release scope](VALIDATION-0.0.1-alpha.13.md).


<!-- END PRESERVED SOURCE -->
