# Terrain, water, snow and LOD completeness audit

Reviewed 2026-10-22 against Deep Primal commit **15c4bcd4a049f2d8569d1df6d18316b029adb51a**, including alpha 20. This is the current reference-to-requirement audit; the alpha 14/16 water documents remain historical release records.

**Result: the requested systems are partially implemented, not complete.** The first 17 linked mods' author descriptions were checked against Deep Primal source and the existing roadmap. A [three-project follow-up](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md) adds Streams Reflowing, Canopy Cascade and Puddles & Floods, bringing coverage to **20 owner-linked projects**. The follow-up checks main 999e2fe, whose alpha 20 runtime is unchanged from this audit's baseline. The terrain and rendering technologies below were checked against their official documentation. This is not an exhaustive upstream source audit, installation test of those mods, new in-game validation run, or certification of feature parity. Author claims describe the references, not measured Deep Primal behavior.

This update adds planning and acceptance criteria only. It does not repair rendering/water, implement catalog content, change saved geography, add dependencies, or ship a new build. Existing environmental catalog implementation remains deferred. Biome identities come first; a realistic climate model gates their placement afterward rather than shrinking the roster.

## Immediate owner requirements

- **Rendering:** the owner reports that graphical defects disappear when "horizonal view" is removed. Preserve that observation verbatim; the exact UI control and responsible renderer have not been established. P0-RENDER below requires an A/B reproduction and a real enabled-view fix.
- **Stepped water:** shallow water should descend in supported partial-height steps, pool in low places and drain from high places, taking Water Logistics as inspiration. Native sixteen-unit volume and shared-corner geometry are a foundation, not proof of finished motion or bank behavior.
- **Different rivers:** some rivers must be faster than others, with locally calm reaches, rapids and cascades. Stored speed already varies with catchment and slope; complete hydraulic behavior remains open.
- **Water identity:** source/material history should affect color and visibility. A clear snow-fed stream and sediment-rich lowland river should mix gradually downstream, rather than changing at a biome boundary.
- **Headwater scale:** a river that begins already broad should have a credible substantial source lake/body of water or accumulated upstream supply. Small springs should begin narrow. Lake size and sustained discharge need separate accounting.
- **Oceans and snow:** include deep-trench feasibility, richer coasts/currents, pressure and navigation, and climate-appropriate deep snow. Treat the reference projects as inspiration for coherent native systems.

Reviewed screenshots include a hovering water sheet near a forest bank, thin water spreading around vegetation and repeated gravel bowls, and a distant vertical page wall. These are separate observable defects; screenshots alone cannot prove whether a block exists or which render pass produced it. The wide basin screenshot (image dated 2026-10-21 22:46:05) supports the source-lake discussion, but neither its exact scale nor its identity as the owner's "last photo" is established. Do not infer a measured lake radius from it.

## Status vocabulary

**Native partial** means source exists on the current independent engine path, with the listed limits. **Legacy/configured** means code exists but targets older water, a compatibility path, or an opt-in rule; it is not automatically complete for EngineFluid. **Open** means the complete requested behavior has no demonstrated implementation. All new work items below remain unchecked.

## First 17 requested mods

| Reference and author-described scope | Deep Primal evidence and actual coverage | Missing behavior / requirement |
| --- | --- | --- |
| [Farmland Water](https://www.curseforge.com/minecraft/mc-mods/farmland-water): walkable water beside farmland through managed platforms, with automatic placement/cleanup and controls. | **Legacy/configured partial.** FarmWaterMixin offers optional collision beside farmland, off by default. It does not establish equivalent platform lifecycle or native partial-water behavior. | Native irrigation/walking rules, safe collision height, cleanup after farm edits and multiplayer controls: WATER-06. |
| [Finite Water](https://www.curseforge.com/minecraft/mc-mods/finitewater): prevents creation of new source blocks. | Legacy source-regeneration policy exists, off by default. The current native engine separately conserves sixteen volume units; these are different capabilities. | One explicit source/recharge policy across new and old worlds; no implicit infinite replenishment: WATER-01/02. |
| [Natural Waters](https://www.curseforge.com/minecraft/mc-mods/natural-waters): biome water appearance and resource-pack palette/fog/transparency control. | **Native presentation partial.** WaterColors blends climate/elevation/biome colors; WaterPalettes supports color, fog and clarity overrides. This is spatial tint interpolation, not transported water composition or complete alpha control. | Source-derived composition, confluence plumes, consistent underwater optics and transparent near/far handling: WATER-03, LOD-02. |
| [Water Logistics](https://www.curseforge.com/minecraft/mc-mods/water-logistics): limited water drains/pools; surroundings can dry into desert; utility commands/items. | **Native partial.** WaterRuntime provides conservative local gravity, pooling, thin-film routing and bounded connected pressure. EngineFluid has sixteen-unit levels. A legacy eight-unit solver also remains. | Supported descending steps, basin-scale budgets/outlets and dynamic wetness/ecology. No drought-driven biome conversion, straw or reference command/item suite is implemented: TERRAIN-01/02, WATER-01/02/06. |
| [Coastal Waves](https://www.curseforge.com/minecraft/mc-mods/coastal-waves): beach breakers, sound, entity interactions and deposited beach materials, with filters. | **Presentation partial.** WaterSurfaceRenderer adds bounded offshore swells; OceanBoatMixin adds small vertical motion. Offshore waves do not constitute breaking surf. | Depth-aware shoaling/breaking/swash, foam, surf audio, entity forces and material-aware strandline deposits: OCEAN-01. |
| [Scary Water](https://www.curseforge.com/minecraft/mc-mods/scary-water): lower treading position and swimming stamina. | **Configured partial.** SwimmingEffort and SwimmingMixin provide optional stamina/sinking/treading hooks. Presence is not certification for every native current, mount or multiplayer case. | Integrated fatigue, current/wave effort, recovery and accessible settings: WATER-05. Its author's boat-speed/darker-fog wishlist is not counted as released reference behavior. |
| [Beg: Water Pressure](https://www.curseforge.com/minecraft/mc-mods/beg-water-pressure): depth modes, tiered effects/damage, configurable protective equipment and Rising Flood integration. | **Configured partial.** WaterEnvironment scans up to 512 overhead water cells, stops at air/solid, and applies optional effects/damage. Any occupied armor slot currently counts toward reduction. | Connected-fluid pressure semantics, rated gear rather than arbitrary armor, deeper-world support and explicit mode/compatibility policy: WATER-05. |
| [Hydraulica](https://www.curseforge.com/minecraft/mc-mods/hydraulica): finite water/lava, pressure, partial containers, weather, fluid machinery and fluid-aware interactions. | **Native partial plus legacy facilities.** Native water conserves sixteen units with bounded pressure. Legacy finite water/WaterPort use eight units and transactional 125 mB steps. No native lava, complete machinery suite or shared sixteen-unit machine port is established. | Native containers/ports, displacement/waterlogging, machinery, lava and interaction coverage: WATER-01/04/06 and the explicit secondary-feature dispositions below. |
| [Horses Can Swim](https://www.curseforge.com/minecraft/mc-mods/horses-can-swim): ridden horses swim; author marks the addon deprecated from 1.21.11. | Native target behavior and optional HorseBuoyancyMixin provide a starting point. Earlier alpha 16 records a ridden-horse check; that is not a fresh alpha 20 native-water test. | Verify swimming, rider retention, partial water/current handling and shore exit for the selected target: WATER-05. Do not require an obsolete addon. |
| [Flood](https://www.curseforge.com/minecraft/mc-mods/flood): rain refills existing flowing water to sources; explicitly not flash flooding. | **Legacy partial.** Precipitation has a refill branch for LiquidBlock flows; WeatherHydrology adds measured units when the old finite solver is active. EngineFluid is a separate Fluid implementation. | Native measured rainfall/recharge without mass-creating source conversion; independently planned inundation is additional scope, not Flood parity: WATER-02. |
| [Wakes Reforged](https://www.curseforge.com/minecraft/mc-mods/wakes-reforged): rendered wakes styled for Minecraft. | **Presentation partial.** WakeRenderer uses bounded persistent mesh stamps; WaterEffects has a particle fallback and duplicate-effect yielding. | Flow-aware advection, shores/obstacles, boats/swimmers, foam lifetime and matching distance behavior: OCEAN-01/02, LOD-02. No upstream performance claim is adopted. |
| [Flowing Rain](https://www.curseforge.com/minecraft/mc-mods/flowing-rain): Flowing Fluids addon for finite rain, infiltration/runoff, contained pooling, variable storms and evaporation. | **Legacy partial.** WeatherHydrology is gated by FiniteWater.enabled(); it has persisted column moisture and local intensity. These hooks do not establish rainfall accounting in WaterRuntime. | Native catchment water budgets, material infiltration, local storm ownership, bounded runoff/evaporation, shelters and foreign-weather cooperation: WATER-02. |
| [Fluid Compact](https://www.curseforge.com/minecraft/mc-mods/fluid-compact): compatibility between Flowing Fluids and water-drawing machines from other mods. | **Integration open.** WaterPort is a native NeoForge API for the old solver, not this compatibility bridge, and does not register EngineRegistry.WATER_BLOCK. | Tested optional machine adapters for the current solver and exact target versions: WATER-04. Immersive Engineering/Ender IO entries on the reference page are plans, not established supported integrations. |
| [Ocean Seam Fix](https://www.curseforge.com/minecraft/mc-mods/ocean-seam-fix-physics-mod-ocean-distant-horizons): addresses a specific Physics Mod/Distant Horizons/Iris ocean overlap. | **Own-path work only.** Historical procedural-horizon depth isolation and the newer native-chunk LOD are different render paths. Neither proves interoperability with that three-mod combination. | Reproduce the owner's disappearing-defect report; establish near/far water ownership, correct depth/alpha and optional external renderer matrix: P0-RENDER, LOD-02. |
| [Sablewaves](https://www.curseforge.com/minecraft/mc-mods/sablewaves): shared Gerstner wave/physics field, tides/surge, wind, several current types, ship forces/sails, wakes/audio and soft weather integrations. | **Presentation partial.** Deep Primal sums sinusoidal waves and samples four boat points for vertical changes. This is not full Gerstner motion, vessel force/torque, tides, global ocean circulation or ship integration. | Shared geometry/physics, winds/currents/tides, craft forces, sails and optional integrations: OCEAN-01/02. The reference shifts effective flotation for tides rather than physically moving water blocks; beach swash is currently optional and has documented visual limits. |
| [Snowy Weather](https://www.curseforge.com/minecraft/mc-mods/snowy-weather): global freezing/snow, including hot biomes, with optional unlimited stacking. | **Configured partial.** Precipitation stacks cold snow with a 24-layer default cap and optional unlimited mode. GlobalWinter changes rain in precipitating biomes; it does not force deserts to freeze. | Rich climate-aware accumulation/melt with optional explicit winter-world override: SNOW-01. Global freezing is not the default biome design. |
| [TFC DeepSnowStack](https://www.curseforge.com/minecraft/mc-mods/tfc-deepsnowstack-accumulation): deep cold accumulation tied to TFC weather/intensity/melting, plus fog. | **Native partial analogue.** Stacking, a bounded canopy check, foliage tint and local weather/snow fog exist. No TFC dependency or equivalent native intensity/melt water cycle is established. | Native accumulation rate, redistribution/compaction, thaw/runoff, snow geometry and weather-linked visibility: SNOW-01. |

## Three additional references — streams, canopy and puddles

The [detailed follow-up](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md) records source scope, current coverage and acceptance for these additions. It includes targeted Puddles & Floods source review; no source inspection is claimed for the other two projects.

| Reference | Native coverage and additional open requirement |
| --- | --- |
| [Streams Reflowing](https://modrinth.com/mod/streams-reflowing) | Partial regional stream/current foundation; **TERRAIN-04** adds detailed riparian style/feature contracts, flow/navigation cases and richer bounded river atmosphere alongside TERRAIN-01/02/03. |
| [Canopy Cascade V1.2](https://www.curseforge.com/minecraft/mc-mods/canopy-cascade-v1-2) | Native canopy moisture lifecycle is open; **WATER-07** adds interception, conditional dew, drying, collision-aware presentation and accounted release. |
| [Puddles & Floods](https://modrinth.com/mod/puddles-floods) | Detailed puddle/event presentation is open; **WATER-08** separates safe cosmetic traces from physical native inundation, sub-unit storage and local-stage flood/recession behavior. |

These three detailed cards extend the existing requirements; they do not claim implemented parity or change the biome roster. The previously documented Flood source-refill behavior is also distinct from this new puddle/visual-flood reference.

### Implementation evidence

Paths below refer to the audited commit. An entry is evidence of the named mechanism, not a pass for every requirement using it.

| Source | Relevant evidence / boundary |
| --- | --- |
| [TerrainEngine](../engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java) | Regional downhill graph, rainfall-weighted accumulation, width/depth/speed and a sediment-placement proxy. Region-local planning is not a stitched global watershed; the sediment scalar is not suspended mass transport. |
| [EngineChunkGenerator](../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java) | Elevation/slope/rain/sediment surface selection and sixteen-unit water with stored direction/speed. Default min_y=-128, height=768; this codec accepts aligned heights from 256 through 2048. These are current implementation constraints, not universal Minecraft height limits. |
| [EngineFluid](../src/main/java/com/shnewbs/deepprimal/engine/EngineFluid.java), [WaterRuntime](../src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java) | Own Fluid, sixteen volume units, eight stored speed values, local conservative transport, bounded drain and pressure work. Hooks targeting vanilla FlowingFluid or old finite-water helpers need individual integration checks. |
| [WaterPort](../src/main/java/com/shnewbs/deepprimal/water/WaterPort.java), [WeatherHydrology](../src/main/java/com/shnewbs/deepprimal/water/WeatherHydrology.java), [Precipitation](../src/main/java/com/shnewbs/deepprimal/water/Precipitation.java) | Old-solver ports and weather gating, source-refill branch, cold stacking and optional winter override. Sixteen-unit engine ownership is not demonstrated by their registration alone. |
| [RiverConfig](../src/main/java/com/shnewbs/deepprimal/water/RiverConfig.java), [WaterEnvironment](../src/main/java/com/shnewbs/deepprimal/water/WaterEnvironment.java), [SwimmingEffort](../src/main/java/com/shnewbs/deepprimal/water/SwimmingEffort.java) | Defaults and optional pressure/stamina rules. A configured legacy control must not silently appear to control an independent solver when it does not. |
| [WaterColors](../src/main/java/com/shnewbs/deepprimal/client/WaterColors.java), [WaterPalettes](../src/main/java/com/shnewbs/deepprimal/client/WaterPalettes.java), [WaterColorMixin](../src/main/java/com/shnewbs/deepprimal/mixin/client/WaterColorMixin.java) | Spatial palette blending and resource-pack overrides. SmoothWaterRenderer obtains the blended biome color through the native resolver hook; it does not establish compositional mixing. |
| [SmoothWaterRenderer](../src/main/java/com/shnewbs/deepprimal/client/render/SmoothWaterRenderer.java), [WaterSurfaceRenderer](../src/main/java/com/shnewbs/deepprimal/client/render/WaterSurfaceRenderer.java), [WakeRenderer](../src/main/java/com/shnewbs/deepprimal/client/render/WakeRenderer.java), [OceanBoatMixin](../src/main/java/com/shnewbs/deepprimal/mixin/OceanBoatMixin.java) | Shared-corner surface geometry, separate offshore overlay, bounded wakes and small boat vertical deltas. Spatial smoothing differs from interpolation between authoritative simulation states. |
| [SnowAtmosphere](../src/main/java/com/shnewbs/deepprimal/client/SnowAtmosphere.java), [SnowFoliage](../src/main/java/com/shnewbs/deepprimal/client/SnowFoliage.java) | Bounded local weather/snow fog and foliage decoration; not a full snow mass/thermal model. |
| [VoxelLodRenderer](../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java), [LodMesher](../engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java) | Real chunk capture/revisions, levels, greedy geometry, cache and native-ready transitions. Delivered/explored data only; not unseen-world streaming or a certified seamless water renderer. |

[Alpha 19 validation](VALIDATION-0.0.1-alpha.19.md) records the native pressure fixture. [Alpha 20 validation](VALIDATION-0.0.1-alpha.20.md) records per-tick conservation, continued local filling despite queued work, a thin-film outlet, vegetation wash-through, interior activation and mesh-build progress on OpenGL/Vulkan. It explicitly leaves per-frame interpolation, basin accounting, displacement, erosion, groundwater, lava and breaking surf unfinished. Recorded tests are useful evidence within those scenes; no new runtime tests were run for this documentation audit.

## Terrain and rendering technology references

| Technology / primary reference | Requirement and present status |
| --- | --- |
| [Gaea HydroFix](https://docs.gaea.app/reference/nodes/simulate/hydrofix.html), [Erosion](https://docs.gaea.app/reference/nodes/simulate/erosion), [terrain-derived surface maps](https://quadspinner.com/Gaea) | Continuous drainage-friendly slopes, erosion/deposition/flow information and material masks inform TERRAIN-01/03. Current carving/masks are partial; no Gaea engine is embedded. |
| [World Machine Create Water](https://help.world-machine.com/topic/device-create-water/) / [River](https://help.world-machine.com/topic/device-river/) | Discharge, river cross-section, gradient and floodplain design inform TERRAIN-02. Current area/slope proxies are not a complete hydraulic model. |
| [Nanite technical details](https://dev.epicgames.com/documentation/unreal-engine/nanite-technical-details) | Hierarchical geometry/detail streaming reference for LOD-01. No Unreal/Nanite runtime is part of Deep Primal. |
| [Cesium Native selection](https://cesium.com/learn/cesium-native/ref-doc/selection-algorithm-details.html) | Projected geometric error, coverage-preserving refinement and bounded streaming inform LOD-01; no Cesium runtime has been adopted. |
| [meshoptimizer](https://github.com/zeux/meshoptimizer) / [clusterlod helper](https://github.com/zeux/meshoptimizer/blob/master/demo/clusterlod.h) | MIT-licensed processing is a candidate for constrained meshes and hierarchies. Only a development probe exists; neither is a shipping LOD dependency. |
| [LWJGL MeshOptimizer](https://javadoc.lwjgl.org/org/lwjgl/util/meshoptimizer/MeshOptimizer.html) | Java bindings reduce integration work. The existing probe pins 3.4.1; snapshot documentation and the separate C++ clusterlod helper are not proof of matching Java/native APIs. |
| [Simplygon vegetation optimization](https://www.simplygon.com/features/vegetationoptimization) | Optional offline vegetation/model preparation reference. It does not solve constantly edited terrain, water or runtime cache ownership. |

Use independently authored algorithms. Any actual source/native-library/assets adoption requires a pinned version, applicable license/notice review, provenance and distribution plan. Gaea/World Machine authored terrain imports remain an optional data workflow under the applicable asset/tool terms, with scale, extent, version and checksums; their proprietary engine code is not a mod implementation source. No import is added here.

## Detailed open work and acceptance

### P0-RENDER — reproduce and fix the enabled horizon/LOD defect

- [ ] Identify the exact control/version meant by "horizonal view"; retain the owner's observation and screenshots as regression cases. At a fixed seed, position, camera and time compare full-detail horizontal distance, actual-chunk LOD, the explicitly enabled legacy procedural horizon, and ocean/wake overlays independently.
- [ ] Inspect native block truth, depth attachments, alpha ordering, frustum/vertical bounds, page-edge caps and water ownership. Separate real floating terrain/water from a distant mesh defect.
- [ ] Fix enabled-view behavior: no synthetic page walls, missing banks, foreground overwrite, dark water ring, flicker or duplicated surfaces while moving, resizing, teleporting or changing view radius.
- [ ] Retain graphics/LOD modularity and both OpenGL/Vulkan paths. Disabling LOD must not silently enable the old horizon.
- [ ] Record fixed-camera on/off captures and frame-time/memory costs on named hardware, plus edits/reload/multiplayer cases. Disabling a feature to hide the symptom is a diagnostic result, not completion.

### TERRAIN-01 — one connected drainage, lake and outlet plan

- [ ] Plan tributaries, confluences, lakes, wetlands and spillways before carving valleys. Use canonical region boundary outlets with agreed elevation, direction and discharge; cache immutable seeded results with halos and versioned identities.
- [ ] Distinguish spring, seep, snow/glacier-fed reach, upstream river and lake outlet. A broad initial reach needs an appropriately substantial contained source basin or upstream catchment; a tiny spring tapers until tributaries contribute.
- [ ] Model lake area/depth/storage, inflow, evaporation/seepage and outlet control separately. A large lake alone does not guarantee sustained high discharge. Avoid repeated circular bowls, one-size gravel rims, unsupported shelves and evenly spaced basin beads.
- [ ] Preserve intentional closed basins and natural waterfalls; require valid beds/banks and explicit endorheic sinks rather than forcing every stream to the sea.
- [ ] Prove connectivity, shared borders/corners, identical output across generation orders/worker counts and cache eviction, with headwater/confluence/lake/outlet captures. No forced distant chunk generation and no silent changes to serialized version-one worlds.

### TERRAIN-02 — believable width, depth, speed and descending steps

- [ ] Extend rainfall/catchment/slope proxies into consistent discharge and cross-sections. Represent shallow creeks, broad lowland rivers, deeper channels, slow pools, constrictions, rapids, cascades and floodplains. Width alone must not imply greater velocity.
- [ ] Keep generated flow vectors, sixteen-unit levels, runtime transport, entity forces, foam/spray and sound consistent. Recompute affected flow after meaningful channel edits instead of assuming the original stored speed still fits.
- [ ] Make partial-height water follow a supported bed downhill, spill over actual lips and settle to level in still pools. Use explicit waterfalls at sharp drops; do not replace normal sloping rivers with stair walls or hovering curtains.
- [ ] Verify distinct measured travel/current speeds for gentle/steep reaches, conservative confluences and no double propulsion of items/boats/swimmers. Compare the requested Water Logistics-like appearance in native captures; upstream visual resemblance is not a conservation test.

### TERRAIN-03 — erosion, deposition, material masks and support

- [ ] Produce compact, reusable wear, flow and deposition summaries in the regional planner. Geological hardness, gradient and drainage should form gullies, weathered slopes, alluvial floors, gravel bars, silt/clay deposits and scree where appropriate.
- [ ] Keep generation-time shaping separate from optional, bounded live erosion. Define sediment accounting and edited-block protection before runtime banks can erode.
- [ ] Place stone, dirt, gravel, sand, clay, mud, grass and snow using slope, elevation relative to water, substrate, moisture, inundation and deposition history. Avoid grass on freshly scoured/unsuitable surfaces and identical beaches around all lakes.
- [ ] Check generated support/connectivity after carvers/features, including water-bed containment. Protect intentional caves, overhangs and player builds; never use a global destructive cleanup scan.
- [ ] Accept using geological/soil boundary and wet/dry transects, sediment continuity, no unsupported generated fragments, deterministic region seams and measured generation/cache budgets. Full natural-material catalog implementation remains separately deferred.

### WATER-01 — one native fluid authority and smooth presentation

- [ ] Make sixteen-unit native volume the explicit authority for the selected engine. Specify legacy-world conversion and solver/config ownership so old source rules, ports or compatibility hooks cannot duplicate or consume it inconsistently.
- [ ] Finish conservative block-placement displacement, partial containers, waterlogging/hollow storage and neighboring spill behavior. If no destination has capacity, define a visible, accounted result rather than deleting water.
- [ ] Preserve queued work fairly under large active areas; gravity, films and connected-pressure work must progress without loading absent chunks. Region-scale basin accounting must complement bounded local searches.
- [ ] Interpolate visible free-surface height and wet/dry edges between server updates with bounded render data. Keep collision, buckets and saved volume authoritative; tolerate delayed network states without endless smoothing lag or stale meshes.
- [ ] Accept per-tick conservation including containers/machines/displacement, save/reload/unload boundaries, saturated queues, enclosed basins and continuous edit captures. Alpha 20 mesh progress does not yet prove perceived smoothness.

### WATER-02 — rainfall, groundwater, drought and snowmelt budgets

**Later owner adoption, 2026-10-22:** the [hybrid water mechanics contract](WATER-BUDGET-REQUIREMENTS.md) selects renewable regional reservoirs plus finite local water, designated perennial rivers, seasonal melt/desert wet-dry regimes, depletable wells and accounted ocean recycling at stable mean sea level. Local floods remain physical; strict depletion and source safety are optional. This updates design direction, not the alpha 20 source evidence above; alpha 21 presentation progress is documented separately.

- [ ] Feed measured precipitation into native soil storage, infiltration, aquifers, surface runoff and contained pools. Account for shelter, material permeability, slope, saturation, cold precipitation and locally moving/intensifying storms.
- [ ] Route runoff to real lower outlets before raising a basin. Include evaporation, seepage, springs, extraction and recharge; use declared bounded reservoirs/boundary fluxes for large natural waters rather than untracked infinite blocks.
- [ ] Link snowmelt/thaw to the same ledger. Persist soil/aquifer summaries, cap catch-up, and define unloaded-region/calendar behavior without scanning the whole world.
- [ ] Express drying through moisture, wetland extent, vegetation and soil states. Do not permanently rewrite the chosen biome identity to desert merely because one block lost water; ecological succession is a separate versioned design decision.
- [ ] Define optional weather-provider ownership to prevent double rain/evaporation. Offer inspection of inflow/outflow/storage and configuration presets. Accept enclosed versus draining basins, sheltered soil, a dry spell, a local storm, extraction/recharge and snowmelt with bounded work.

### WATER-03 — source composition, mixing and water appearance

- [ ] Track bounded summaries of water source, temperature, suspended mineral sediment and dissolved organic/tannin load; add salinity where fresh/marine systems meet. Keep optical appearance separate from drinking safety/contaminants.
- [ ] Support clear snow-fed mountain streams, sediment-rich runoff, organic-stained forest/wetland waters and clearer settled lakes. Do not make every tropical river brown or every frozen-source stream clear: [glacial meltwater can carry rock flour](https://uol.de/en/news/article/unlocking-the-secrets-of-glacier-flour).
- [ ] Advect/mix concentrations using actual water exchange or an explicit conservative coarse model. Let sediment settle/resuspend with speed and substrate. At confluences retain plausible joining plumes that blend downstream, rather than instantly averaging a whole basin or drawing a biome seam.
- [ ] Derive tint, scattering/fog, clarity and foam response consistently for surface, underwater and LOD rendering. Retain resource-pack style overrides and the shared low-resolution texture/tint direction; no high-resolution texture requirement is introduced.
- [ ] Accept clear/muddy confluence, unequal-flow mixing, lake settling, storm turbidity and estuary transitions, with no color jump at chunks, biome boundaries or near/far handoff. Budget composition storage/update/network costs.

### WATER-04 — containers, mechanical plumbing and optional compatibility

- [ ] Provide transactional sixteen-unit native extraction/insertion, multi-machine contention and rollback. Resolve exact conversion between 1/16 block and integer mB: 62.5 mB cannot simply be rounded each transfer. Specify a lossless internal unit/remainder or supported transfer granularity.
- [ ] Add partial buckets/containers and observable stored quantities; implement native pumps, connected pipes, valves, tanks, grates and fluid sensors in the agreed mechanical progression. Head, capacity, rate and energy must constrain throughput.
- [ ] Test optional adapters for exact supported versions of Create, Modern Industrialization, Mekanism/Generators, PneumaticCraft and Alex's Caves fluid-drawing systems when applicable to the selected target. These are candidates, not certified support. Skip missing mods and respect an integration's existing ownership.
- [ ] Test simulate/commit/abort, shared-cell journals, partial capacity, unloaded boundaries and failed writes without duplication or loss. Define bridge responsibilities before adopting any Flowing Fluids-specific compatibility layer.

### WATER-05 — pressure, swimming, mounts and readable danger

- [ ] Unify swimming effort with currents, waves, cold, carried load and recovery, with configurable/accessibility-friendly limits. Verify entry, shallow standing, ladder/shore escape, exhaustion and server/client agreement.
- [ ] Replace arbitrary armor-slot pressure protection with explicit rated equipment/tags and progression. Distinguish breathable air, water-column/connected-fluid pressure and sealed-hull protection. The present overhead-solid scan is a gameplay approximation; do not label it complete physical hydrostatic pressure.
- [ ] Support the actual chosen world depth without a misleading 512-cell scan ceiling. Define pressure head/density, effect tiers, warnings, intervals and caps; any sea-level shortcut or external rising-water mode must be explicit.
- [ ] Verify ridden horses and other supported mounts retain riders, float/swim in partial/current water and exit banks without duplicate buoyancy. Verify boats/ships do not receive two competing force systems.
- [ ] Accept clear pressure/stamina feedback, ordinary armor versus rated gear, overhead roof/connected pool/air-pocket cases, optional rules disabled, and multiplayer. Deep-trench difficulty requires navigation and equipment support, not only instant damage.

### WATER-06 — farm interaction and remaining reference features

- [ ] Decide native farm-water walking as an optional convenience rule or a physical supported platform. Match collision to partial surfaces, avoid trapping entities, and respond correctly to farmland removal, water motion and multiplayer edits.
- [ ] Integrate irrigation/moisture extraction with the water budget. Reference-like straws/measurement tools should map to native drinking/inspection mechanics; infinite buckets and artificial endless sources belong to an explicit creative/admin or declared renewable-reservoir policy.
- [ ] Retain the following Hydraulica-inspired items as explicit proposals, not implied completed features: fluid-absorbing sponges with stored volume retained through pickup and a release action; natural sponge habitat/deposits; partial-water boats, fishing and bubble columns; water-consuming dripstone/cauldron transfer; configurable spring/reservoir behavior.
- [ ] Treat lava conservation, lava reservoirs and heat interactions as a separate budgeted fluid extension after water. Underground methane/gas pockets, buoyancy and ignition are a later optional geology/hazard proposal, requiring progression and performance design. They are not silently included in water parity or copied wholesale.
- [ ] For every retained interaction, specify save state, renewable versus finite mass, disabled behavior and an acceptance scene. Direct copies of the reference's item names, recipes, commands or diamond-block spring trigger are unnecessary; preserve the useful behavior within Deep Primal's progression.

### OCEAN-01 — shore waves, wakes, sound and physical agreement

- [ ] Use one time/seed/weather field for visible wave height, normals and entity buoyancy/drag; add multi-point vessel force/torque where supported. Keep an inexpensive calm baseline and deterministic, bounded sampling.
- [ ] Use bathymetry and wave approach to shoal/break near beaches, cliffs, reefs and shallows. Add shore foam, swash/backwash, whitecaps, spray and distance-appropriate surf/hull sounds, including calm bays and exposed storm coasts.
- [ ] Couple waves/currents to swimmers, items and supported vessels. Advect wakes/ripples with flow, interact with banks/obstacles, and keep foam lifetime and duplicate-effect handoff controlled.
- [ ] Place strandline materials by native habitat, substrate, storm history and supply, with tags and bounded rates. Avoid farming unlimited valuable loot from a visual wave.
- [ ] Prevent an overlay from hovering above dry land, revealing flat-water troughs, z-fighting or leaking through terrain. Accept fixed moving-camera beach/cliff/estuary/storm scenes with native/LOD changes and per-frame cost measurements.

### OCEAN-02 — circulation, tides, wind and vessel integration

- [ ] Plan hemisphere-correct winds/rotation, coast/basin steering, surface gyres, depth-dependent return flow, upwelling/downwelling and temperature/salinity contributions. Climate controls placement and physical conditions after the biome roster is defined.
- [ ] Add local longshore/rip flow and tidal inlet/estuary currents. Model lunar phase tides and storm surge with local amplitude and shoreline constraints; explicitly distinguish visual/effective flotation from actual inundation and finite-water transfer.
- [ ] Let winds gust and alter sea state; support native sailing/craft forces as progression permits. Optional ship and weather APIs must be versioned soft adapters with an internal fallback.
- [ ] Provide readable current/wind navigation or inspection without mandatory debugging overlays. Bound acceleration, CPU samples, network state and affected areas; avoid global per-block ocean updates.
- [ ] Accept consistent direction/depth/hemisphere cases, tidal reversals, sheltered/exposed coasts, craft behavior and deterministic server/client presentation. Full Earth fluid simulation is not required to demonstrate these simplified rules.

### OCEAN-03 — trenches and the world-height feasibility gate

- [ ] Plan shelves, continental slopes, submarine canyons, abyssal plains, ridges and rare trench systems tied to geological regions, including island-arc relationships where appropriate.
- [ ] Preserve Mariana-like relative scale and depth progression in a configurable vertical scale. [NOAA places Challenger Deep at about 10,935 m](https://oceanservice.noaa.gov/facts/oceandepth.html); literal metre-per-block depth would need roughly 11,000 blocks below sea level plus a seabed.
- [ ] Do not advertise that depth under the current generator: its default height is 768 and its own codec rejects heights over 2048. Changing a number or increasing view radius does not solve host storage, lighting, loading, coordinates, fluids, entities or networking.
- [ ] Evaluate taller-world/vertical-streaming architecture separately with named-hardware generation, memory/VRAM, I/O and multiplayer budgets. Preserve existing save heights; report native support limits separately from our chosen constraints.
- [ ] Accept bathymetric cross-sections and depth/pressure/light zones first. Literal extreme depth remains a feasibility item until the complete stack is verified.

### SNOW-01 — deep snow with an actual weather and melt cycle

- [ ] Accumulate by local snowfall intensity, temperature, season, elevation and exposure, with climate-appropriate polar/alpine/seasonal scenarios. Global forced winter remains an explicit override rather than redefining every biome.
- [ ] Extend under-canopy deposition, snow on suitable foliage, wind redistribution/drifts, settling/compaction and smooth layer-to-block transitions. Preserve plants/collisions and avoid suffocating or abruptly launching entities as depth increases.
- [ ] Link thaw/refreeze, sunlight/shelter, thermal inputs, slush and meltwater runoff to shared heat/hydrology. Cap or optionally uncap depth with clear bounds on loaded updates and saved data.
- [ ] Match flurries/blizzards/fog/audio and near/far snow appearance to actual conditions. Weather intensity should affect deposition, not just a fog overlay.
- [ ] Accept sustained storm, canopy/open-ground transect, warm thaw, refreeze, meltwater conservation, entity movement and reload. Use native interfaces; neither TFC nor forced desert freezing is required.

### LOD-01 — bounded geometry hierarchy, streaming and honest performance

- [ ] Extend actual delivered-chunk LOD with an explicit optional unseen-terrain source/stream contract, provenance and authority. Preserve edited/explored data over speculative terrain; workers consume immutable snapshots rather than mutable chunks.
- [ ] Store geometric error, three-dimensional bounds and revisions. Select by projected error, frustum, viewport/zoom and hysteresis, including tall cliffs and vertical travel; retain covering parents until required children are ready.
- [ ] Evaluate greedy voxel reduction first, then constrained meshoptimizer simplification/compression/reordering. Preserve hard silhouettes, material/normal edges, cutout vegetation, water boundaries and shared page anchors. A C++ clusterlod helper or a binding on the classpath is not an implemented hierarchy.
- [ ] Keep CPU work, native scratch, dirty queues, upload bytes, RAM/VRAM, cache I/O/decompression and network bandwidth bounded. Reject stale work and cancel on disconnect/teleport; test saturated revisions and reload.
- [ ] Benchmark against Distant Horizons with matched seeds/worlds/camera paths, detail/error, radius and named hardware. Record p50/p95/p99 frame times, spikes, memory/VRAM, generation/load latency, draw calls, upload/cache costs and edit lag. No superiority claim until measured.

### LOD-02 — materials, lighting and transparent water handoff

- [ ] Give opaque, cutout and transparent/water geometry explicit ownership and ordering. Preserve actual water level/holes/tint, daylight/lighting summaries, snow and material identity across reductions.
- [ ] Synchronize near/far displacement, fade ranges, depth writes and alpha coverage; no gap/double draw/dark ring as native chunks become ready. Include underwater, shore, storm, low-angle and vertical camera cases.
- [ ] Maintain the max-64×64 world texture/shared-family tint plan. Atlas packing can organize assets; it does not remove per-material data, filtering/mipmap constraints or automatically improve rendering.
- [ ] If external Physics Mod/DH/Iris/shader support is selected, verify exact versions and combinations separately. An own-renderer seam fix cannot certify those combinations.
- [ ] Record fixed-camera regressions for the owner's enabled-view problem, water composition joins, breaking surf and player edits on both graphics backends.

## Work order and completion rules

1. P0-RENDER and supported stepped-water/filling defects; preserve ongoing alpha 20 work and current saves.
2. Connected drainage/lakes/source scale and surface materials, followed by native water authority/weather/ports integration.
3. Source composition/mixing, fuller river hydraulics and snow accumulation/melt.
4. Shore surf/wakes, ocean currents/tides and pressure/navigation/vehicle integration.
5. Broader LOD streaming/mesh processing and matched performance certification; keep basic LOD correctness work alongside priority 1.
6. Deeper-world architecture, lava, secondary hazards and additional optional adapters behind their own feasibility/progression gates.

A feature is complete only with a named native owner, effective configuration/defaults, persistence/network semantics, measured bounded cost and reproducible behavioral/visual evidence. A catalog entry, copied inspiration table, configuration key, unit test or attractive screenshot alone is insufficient. Pin upstream versions/source snapshots for any future detailed upstream code audit or reuse. "Do better" means demonstrated correctness, integration, visual quality or matched performance improvements, not an unsupported claim about another project.

Return to the [roadmap](../roadmap.md), [LOD/drainage architecture](LOD-AND-DRAINAGE-ARCHITECTURE.md), [biome plan](BIOME-PLAN.md) and [environmental coverage inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md).
