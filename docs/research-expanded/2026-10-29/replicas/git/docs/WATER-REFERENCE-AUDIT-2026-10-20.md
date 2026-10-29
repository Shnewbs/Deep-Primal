# Expanded replica — docs/WATER-REFERENCE-AUDIT-2026-10-20.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/WATER-REFERENCE-AUDIT-2026-10-20.md) |
| Source identity | git: docs/WATER-REFERENCE-AUDIT-2026-10-20.md |
| Preserved source SHA-256 | `13f98b6670618c8e95ce37118668945f076af452867cd4786555be8b497f7d1b` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 16 audit compares nineteen author-described references against the then-current legacy water/snow implementation. It carefully distinguishes source policies, eight-unit finite flow, partial measured buckets and machine ports, visual wave/wake approximations, and absent external integrations. Corrections describe procedural-horizon depth isolation, partial river steps, headwater lake candidates, preserved generator profiles, and bounded ocean sampling. The opening explicitly warns that these eight-unit, weather, and port claims do not automatically apply to the later sixteen-unit engine and that actual-chunk LOD supersedes an old gap. The review preserves those historical boundaries.

## Research expansion — gaps and details to deepen

- Elaboration: the eight-unit/sixteen-unit distinction is already emphasized, but a capability-and-conversion migration table would prevent accidental reuse of legacy port and weather behavior.
- Clarification: author-described feature rows are dated evidence and should not be refreshed or promoted without new primary inspection and matching native tests.
- Elaboration: retained ocean patches and shared wake buffers have concrete counts, but failure/reload lifetime and peak in-flight accounting are not fully specified here.

## Research expansion — proposed contracts

- Add explicit legacy-adapter records for fluid family, volume denominator, bucket conversion, machine-port quantum, weather owner, and tested world profiles. An adapter must reject unsupported independent-fluid requests rather than fall through to an eight-unit calculation.
- Define exact rational conversion between external container units and internal quanta; preserve remainder under repeated partial transfers and require simulation-before-commit semantics. A full-looking native cell is not evidence of compatible capacity or chemistry support.
- Attach a reference evidence ledger with author/source URL, inspected revision/date, licensed reuse status only as previously verified, local feature owner, and native test artifact. A similar appearance or a broad listing cannot close a capability row.
- Extend renderer resource evidence to include allocation generation, retained replacement patches, retired in-flight buffers, device reload, and world-switch cancellation. Counts such as 128 wake stamps or a 12,288-byte buffer are historical payload facts, not total current GPU cost.
- Keep historical limitations in place and point each current requirement to its later governing document. In particular, do not reactivate optional old finite-source/weather hooks alongside a new fluid authority.

## Research expansion — acceptance checks

- Transfer awkward partial amounts repeatedly through a versioned legacy adapter; verify exact return amounts and no accumulated rounding creation.
- Enable old and new settings together in a sixteen-unit world; exactly one fluid/weather owner remains active or the combination is explicitly rejected.
- Reload the renderer while wake and ocean replacements are pending; handles, retained bytes, and old-world requests retire coherently.
- Reproduce a historical native fixture only with its matching package and profile; current parity remains unclaimed until a current fixture passes.

## Research expansion — source anchors

- **Historical release record.**
- Alpha 15 corrections
- Alpha 16 additions and limits
- Remaining work

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water and snow reference audit — alpha 16

**Historical release record.** For current native-engine coverage, use the [2026-10-22 completeness audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md). In particular, the eight-unit solver/weather/port claims below describe the alpha 16 path, not automatic support for the later independent sixteen-unit engine. The newer actual-chunk LOD also supersedes the older edited-world-LOD gap. Do not use this table as current feature-parity certification.

Checked each author's project description on 2026-10-20 against Deep Primal source. These are independent equivalents, not bundled copies of the referenced mods. This audit supersedes the less precise alpha 14 inspiration table. A similar visual effect is not proof of the reference's gameplay system.

| Reference | What its author describes | Actual Deep Primal status |
| --- | --- | --- |
| [Farmland Water](https://www.curseforge.com/minecraft/mc-mods/farmland-water) | Walking platforms beside farmland, including flowing water, placement/cleanup and a gamerule. | **Partial, optional, off by default.** Player collision on source or partial/flowing water beside farmland, without platform blocks or periodic scans. Reference-specific platform placement/cleanup and gamerule are absent. |
| [Finite Water](https://www.curseforge.com/minecraft/mc-mods/finitewater) | Prevents water creating new source blocks. It is not itself a conserved-volume simulation. | **Core source policy available, off by default.** `finiteSources=true` and `renewableNaturalWaters=false` prevent source regeneration. The separate alpha 16 `hydrology.finiteVolume` solver conserves loaded-cell water volume and overrides source conversion; waterlogged cells stay sealed and lava stays native. |
| [Natural Waters](https://www.curseforge.com/minecraft/mc-mods/natural-waters) | Biome water palettes plus resource-pack control of color, fog and transparency. | **Partial, enabled.** Blended climate/elevation palettes, native biome colors and optional clarity/fog. Alpha 16 adds per-biome resource-pack color, fog_color and clarity settings. Fluid alpha/transparency customization and transported sediment are absent. |
| [Water Logistics](https://www.curseforge.com/minecraft/mc-mods/water-logistics) | Limited water pools in lows and drains from highs; surrounding water affects biome drying. Also adds commands/items. | **Partial, optional.** The loaded-cell solver conserves eight units per block, drains downhill, pools laterally and equalizes bounded connected pools. Stable v6 generated river steps use native partial surfaces. Drought-driven biome conversion, straw and the reference-specific command/item suite are absent. |
| [Coastal Waves](https://www.curseforge.com/minecraft/mc-mods/coastal-waves) | Breaking beach waves, sounds, entity interactions and deposited materials. | **Partial.** Bounded offshore mesh swells and boat motion exist. Shore breakers, dedicated surf audio, wave loot and general entity interactions are absent. |
| [Scary Water](https://www.curseforge.com/minecraft/mc-mods/scary-water) | Lower configurable treading height and swimming stamina. | **Core gameplay available, off by default.** Configurable treading offset, swimming stamina, support recovery and exhaustion sinking. Native server motion updates apply the sink; creative/spectator players are excluded. |
| [Beg: Water Pressure](https://www.curseforge.com/minecraft/mc-mods/beg-water-pressure) | Overhead-column pressure, tiered effects/damage, configurable equipment protection and alternative depth modes. | **Partial, off by default.** Contiguous overhead water, air/solid stops, tiered effects/damage and per-equipped-armor-slot reduction. The 0.25-per-slot setting permits complete four-slot damage protection. Alternative depth modes and Rising Flood integration are absent. |
| [Hydraulica](https://www.curseforge.com/minecraft/mc-mods/hydraulica) | Conserved water/lava volume, equalization/pressure, partial buckets, weather and fluid machinery. | **Partial, off by default.** Conserved native water, gravity/pooling, bounded connected-pool pressure/U-bends, measured partial buckets, soil/runoff/evaporation and transactional NeoForge water ports. Lava, machinery blocks, pumps, pipes and tanks are absent. |
| [Horses Can Swim](https://www.curseforge.com/minecraft/mc-mods/horses-can-swim) | Ridden horses swim; author notes deprecation from 1.21.11. | **Behavior available.** Native 26.1.2 behavior plus optional rider-retention/buoyancy hooks were tested. Do not install the old 1.21.1 addon into this target. |
| [Flood](https://www.curseforge.com/minecraft/mc-mods/flood) | Rain converts flowing water back into sources; explicitly does not simulate flash floods. | **Core behavior present, enabled with native infinite water.** Native rain visits refill existing exposed non-falling flows. The finite solver instead adds one measured unit per eligible rain visit, preventing an unaccounted source refill. |
| [Wakes Reforged](https://www.curseforge.com/minecraft/mc-mods/wakes-reforged) | Rendered wakes fitting vanilla's style. | **Partial.** Alpha 16 provides persistent expanding mesh strips in one reused 12,288-byte buffer, bounded to 128 stamps. Native particles remain a fallback; external Wakes/Particular yield duplicate effects. A full ripple simulation is absent. |
| [Flowing Rain](https://www.curseforge.com/minecraft/mc-mods/flowing-rain) | A Flowing Fluids addon: finite rainfall, soil absorption, runoff, basins, variable storms and evaporation. | **Independent partial analogue, not its addon.** Optional finite rainfall, persisted per-column soil absorption, runoff after saturation, local variable intensity and dry-weather evaporation. No Flowing Fluids dependency, biome drought conversion or weather-mod adapter. |
| [Fluid Compact](https://www.curseforge.com/minecraft/mc-mods/fluid-compact) | Adapts other mods' water-drawing machines to Flowing Fluids. | **Independent native port only.** Alpha 16 exposes transactional NeoForge water extraction/insertion in 125 mB units with rollback. It does not patch Flowing Fluids-specific machines or certify third-party machine compatibility. |
| [Ocean Seam Fix](https://www.curseforge.com/minecraft/mc-mods/ocean-seam-fix-physics-mod-ocean-distant-horizons) | Repairs a specific Physics Mod ocean / Distant Horizons / Iris overlap. | **External interoperability absent.** Alpha 15 repairs our own procedural horizon composition. It does not patch those three mods' shared seam. |
| [Sablewaves](https://www.curseforge.com/minecraft/mc-mods/sablewaves) | Eight-component sea/physics field, weather, tides, wind, ship forces, currents, sails, wakes and integrations. | **Partial visual analogue.** Eight normalized sinusoidal components share weather amplitude with four-point vanilla boat buoyancy sampling. Not full Gerstner physics; lunar tides, wind/gyre/rip/longshore/Ekman currents, sails and external ship/weather integration are absent. |
| [Snowy Weather](https://www.curseforge.com/minecraft/mc-mods/snowy-weather) | Global winter, freezing even hot biomes, optional unlimited stacking. | **Partial.** Optional globalWinter turns rain into snow in precipitating biomes, and optional unlimited stacking removes the layer cap. It does not freeze deserts or replace biome climate/seasons. |
| [TFC DeepSnowStack](https://www.curseforge.com/minecraft/mc-mods/tfc-deepsnowstack-accumulation) | Unlimited cold-weather accumulation, TFC weather/intensity/melt integration and snow fog. | **Partial.** Native cold accumulation, optional unlimited stacking, canopy/foliage support and local snow/weather-density fog. No TFC weather/intensity/melt integration or TFC dependency. |
| [Particular](https://modrinth.com/mod/particular) | A wider environmental particle enhancement suite. | **Partial.** Our bounded water/cascade/snow particles are not the whole suite; external Particular yields duplicate water effects. |
| [Better Snowy Biomes](https://www.curseforge.com/minecraft/mc-mods/better-snowy-biomes) | Snow under trees, snowy leaves, smooth snow and Tectonic support. | **Partial.** Native leaf color decorators and canopy snow support. Equivalent smooth snow terrain and explicit Tectonic/Terralith integration are absent. |

## Alpha 15 corrections

- Horizon geometry renders after sky into its own depth attachment, before real opaque chunks. Coarse heights cannot write the native terrain depth buffer. One depth allocation is reused and resized, and released when the horizon/session closes; there is no per-frame depth copy in product code. Its exact byte size is exposed in diagnostics.
- New `waterways_v5*` profiles use shallower channels and native partial fluid levels. Partial water keeps the same stored direction/speed as source river water. This improves visible stepped surfaces without claiming finite-volume physics.
- New headwater reaches taper into springs. Sufficient catchments may carve a 14–24-block-radius lake when sampled surrounding banks can contain it. Unsafe pool sites keep a narrow spring. Wide rivers therefore grow downstream rather than appearing full-width from a tiny source.
- `naturalRiverSteps` and `deepOceanTrenches` are independent creation-time options. Every prior profile/resource remains unchanged. Existing saved generation is not upgraded silently.
- Deep oceans remain limited by the selected world floor: default -128, extra-depth -384, with a solid bed guard. This is hundreds of blocks of potential depth, not a literal 11,000-block Mariana Trench.

## Alpha 16 additions and limits

New `waterways_v6*` resources sample cross-channel bank support and use a separately registered stable channel block. Existing generators and legacy river blocks keep their saved behavior. This avoids new vanilla spread curtains on planned channels; it does not clean already-explored water or repair every drainage discontinuity. Finite dynamics are opt-in.

The ocean patch samples at most 256 loaded columns per client tick, retains its previous GPU patch until replacement is ready, and does not write the native depth buffer. Horizon coverage is requested before refinements, and stale teleport requests are replaced rather than occupying the request slot. Vulkan remains a separate module; no Vulkan implementation dependency was added to gameplay, settings or the core math module.

## Remaining work

Cross-region drainage continuity, erosion/sediment transport, water tables and depletion, wells, lava conservation, pumps/pipes/tanks, breaking surf and surf audio, tides/ocean currents, complete environmental particle suites, external shader/LOD/ship/weather integrations and edited-world voxel LOD remain unfinished. Alpha 16 must not be advertised as containing every feature of the referenced projects. Native results for rebuilt source are recorded in the alpha 16 validation report; earlier removed-build observations are not reused as a pass.


<!-- END PRESERVED SOURCE -->
