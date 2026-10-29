# Expanded replica — docs/RIVER-MOD-COMPARISON-2026-10-19.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/RIVER-MOD-COMPARISON-2026-10-19.md) |
| Source identity | git: docs/RIVER-MOD-COMPARISON-2026-10-19.md |
| Preserved source SHA-256 | `4f035c2d4c17ff5b6429320d3b2cd59b92a56da8fe7cb2cbe80eecf8b21f1f50` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 12 design comparison records a bounded generation-time tributary graph, seeded from existing wet destinations, using spill routing, drainage accumulation, and downstream profile lowering. It names a 4096×4096-block routing region, 64-block samples, a 32-region cache, shared seeded samplers, and precomputed per-chunk columns. It explicitly lacks global cross-region routing, elevated-lake filling, live discharge/erosion, and persistent currents. Source examination and author-only descriptions are separated, and no compared runtime or source is incorporated. The review treats these as historical architecture and reported implementation scope, not current feature certification.

## Research expansion — gaps and details to deepen

- Elaboration: wet-destination seeding and dry-region behavior are explicit, but outlet identity and cross-region continuation are intentionally absent and need a later-profile design.
- Elaboration: junction heights and descending paths are described, but tie-breaking, boundary ownership, and discretized bank containment are not a complete contract here.
- Clarification: routing scale and cache limits are historical values; an expansion must not carry them forward as unmeasured current defaults or silently change saved geography.

## Research expansion — proposed contracts

- Introduce a new-profile macro drainage record with canonical basin/reach/outlet IDs, topology version, declared regional boundary conditions, catchment accumulation, and spill/sill metadata. Link adjacent regions through deterministic boundary contracts rather than independently inventing exits.
- Preserve the alpha 12 rule that no wet destination means no invented river. Distinguish a certified closed basin from unknown downstream coverage; missing data cannot be treated as sea or a valid low outlet.
- Define total-order handling for equal spill heights and shared junction ownership. Rasterize one shared reach blueprint into each intersecting chunk, with explicit water surface, bed depth, bank containment, and minimum structural support.
- Keep macro accumulation as a generation descriptor until a runtime water-budget system supplies actual discharge. Width based on contributing area does not authorize independent per-branch water creation.
- Add cache identity including seed, saved generator profile, settings fingerprints, coordinate domain, and macro revision. Derived eviction is safe only when recomputation reproduces the frozen profile and does not regenerate committed player edits.

## Research expansion — acceptance checks

- Generate two adjoining regions in opposite orders with equal-height saddles; compare outlet selection, junction geometry, and shared-edge cells.
- Use a wholly dry region and an unresolved neighboring region; neither may gain an invented wet destination.
- Sample bends, merges, and steps against actual discretized blocks, including negative-coordinate region borders.
- Reopen a historical profile after introducing the new macro graph; existing geography and saved routing behavior remain unchanged.

## Research expansion — source anchors

- Implemented scope
- Explicit differences and limits
- A region covers 4,096×4,096 blocks with 64-block routing samples
- A wholly dry region with no wet outlet receives no invented river.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# River and stream comparison — 2026-10-19

This audit informed alpha 12's generation-time mountain drainage. The implementation is original Java inside Deep Primal; none of the compared mods, their assets, Scala runtime or terrain dependencies is bundled.

| Project and primary evidence | Relevant behavior | Deep Primal decision |
| --- | --- | --- |
| [Streams / Farseek](https://github.com/delvr/Farseek-Mods/tree/26.2/main), including [TributaryBasin](https://github.com/delvr/Farseek-Mods/blob/26.2/main/src/streamsMain/scala/streams/world/gen/segments/TributaryBasin.scala) and [TributaryNode](https://github.com/delvr/Farseek-Mods/blob/26.2/main/src/streamsMain/scala/streams/world/gen/segments/TributaryNode.scala) | Its source selects low outlets and high springs, constructs tributary paths, and constrains river elevation using upstream terrain. Its current README explicitly describes small downhill tributary networks and sea-level directional main stems, with repeating large-scale patterns and a cave-floor limitation. | Represent tributaries as an explicit downhill graph with shared junction heights. Use a minimum-spill priority flood and accumulated catchment area, not a port of its Scala path code. Keep cave fluids scoped; do not add a solid sea-level floor across every cave. |
| [Streams Reflowing](https://modrinth.com/mod/streams-reflowing) | The author describes terrain-following streams, elevated lakes, configurable currents and banks. Its performance notes acknowledge terrain-query startup and exploration costs. Its listing is All Rights Reserved and exposes no source link. | Use this as a behavior/performance reference only. Bound terrain-query regions and cache retention; keep drainage independent of graphics. No source was copied or decompiled. Elevated lakes, boat changes, decorative debris and its configurable current system are not claimed as delivered. |
| [Dynamic Waters](https://modrinth.com/mod/dynamic-waters-realistic-flowing-rivers) | The author describes regional generation/caching and mountain tributaries traced uphill from parent junctions, along with branching main channels and deltas. Its speed statements are author claims, not independently measured here. | Establish a real wet destination before creating a catchment and solve connected tributary profiles relative to that destination. Reuse native generation workers instead of adding a separate thread pool. No delta generator or equivalent custom-current system is included. |
| [River Redux](https://modrinth.com/mod/river-redux), [surface-rule source](https://github.com/jaskarth/RiverRedux/blob/master/common/src/main/java/com/jaskarth/riverredux/RiverSurfaceRules.java) | The source selects sand/gravel river surfaces by biome. The author documents sandy, gravelly and tropical variants and a TerraBlender dependency. This is a different problem from mountain drainage routing. | Retain native biome placement and geological rock precedence. Use actual local water height for submerged inland beds, sand in arid biomes and gravel elsewhere, without adding a TerraBlender dependency or new biome IDs. |

## Implemented scope

Alpha 12 adds bounded upland tributary catchments to Deep Primal's lowland/coastal water field. The graph starts at existing wet locations, routes through the lowest available saddles, accumulates drainage area and lowers downstream profiles where a ridge would otherwise obstruct descent. Channel widths grow with contributing area. Water elevation varies along those channels, and submerged bed rules work above sea level.

The routing grid is jittered to reduce a rigid grid appearance. A region covers 4,096×4,096 blocks with 64-block routing samples; channels stay inside that region and join existing water. A four-way set-associative cache retains at most 32 regions per seeded sampler. Native density visitors share that sampler between terrain, climate depth, surface water and horizon height sampling. Per-chunk water levels are precomputed for its 256 columns, so the per-voxel aquifer call does not run routing or fresh terrain noise.

## Explicit differences and limits

This is not a claim of feature equivalence with these projects. Existing lowland trunks remain at sea level. The new solver produces regional tributary graphs, not one globally routed, unlimited-length watershed. A wholly dry region with no wet outlet receives no invented river. It does not stitch new upland rivers across region borders, fill elevated lakes, simulate rainfall/discharge/erosion over time, or add non-decaying custom current blocks. Native water is retained; sustained directional currents on flat water are not supplied. Channel geometry follows finite routing samples rather than a full-resolution erosion simulation. Bank and waterfall appearance still needs interactive playtesting across more seeds.

Source licenses were inspected to distinguish reference material from reusable implementations. Streams/Farseek is MIT; River Redux is LGPL-3.0. Author listings identify Streams Reflowing as ARR and Dynamic Waters as MIT. No code from those projects was incorporated, so their runtime dependencies and implementation licenses are not introduced by this update. Existing Vulkan attribution/licensing remains unchanged.


<!-- END PRESERVED SOURCE -->
