# Expanded replica — docs/WATER-AND-WEATHER-2026-10-20.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/WATER-AND-WEATHER-2026-10-20.md) |
| Source identity | git: docs/WATER-AND-WEATHER-2026-10-20.md |
| Preserved source SHA-256 | `eeaa3f454dc3616dd7fe340df166831e6ec1a8a09532d54c17214d3582b3f5de` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This historical alpha 14 record describes the legacy river/current, climate-tint, offshore-wave, precipitation, snow, survival-option, and procedural-horizon paths. It names concrete request and work limits while explicitly distinguishing requested distance from delivered full detail, water appearance from safety, and source-regeneration policy from conserved-volume physics. It records frozen resource profiles and separate gameplay/presentation controls. Later alpha 16 and independent engine releases supersede portions of its scope. The expanded replica should retain this as dated evidence and route readers to current contracts rather than reissue its configuration and implementation claims as present behavior.

## Research expansion — gaps and details to deepen

- Clarification: the historical banner is correct, but individual controls and legacy feature claims need a versioned crosswalk so copied tables are not mistaken for current engine integration.
- Elaboration: visual wave displacement, boat motion, and server pressure use different quantities; their numerical and authority boundaries could be explicit.
- Elaboration: a nominal sample/particle count bounds iterations, not necessarily time, memory lifetime, or useful progress after travel and reload.

## Research expansion — proposed contracts

- Add a release-scoped feature matrix identifying legacy versus independent fluid, generator IDs, actual renderer owner, configuration file/key revision, and replacement documentation. Preserve the original historical counts and label obsolete paths without deleting the record.
- Define authored current direction, speed band, visual wave height, physical water volume, and pressure exposure as distinct signals. A tint or wave setting cannot alter source supply, drinking quality, contiguous-column pressure classification, or saved water.
- For precipitation and snow adapters, name the one authoritative source of accepted water/ice changes and the renderer-only callbacks. A later finite-fluid adapter must debit the corresponding account rather than inheriting a legacy source-refill hook.
- Require cancellation and generation stamps for loaded-column ocean sampling and retained patches. A teleport, world switch, or backend change retires incompatible requests and buffers while preserving a coherent fallback until a valid replacement is available.
- Record actual delivered detail, visible coverage, simulation range, and retained bytes separately from requested 125/512-chunk legacy limits. Version-specific native constraints must be rechecked before any current configuration recommendation.

## Research expansion — acceptance checks

- Open the legacy and current profiles with the same saved option names; inspect effective owners and verify no duplicate precipitation or source writers.
- Toggle water tint, waves, and particle quality while replaying identical fluid inputs; saved volume and gameplay results stay unchanged.
- Teleport repeatedly while an ocean patch is pending; reject old-world coordinates and prove retired buffers remain bounded.
- Exercise the pressure column with an air pocket and its scan bound, recording unsupported cases rather than extrapolating full-depth correctness.

## Research expansion — source anchors

- **Historical release record.**
- Implemented behavior
- Controls
- Reference comparison

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water, weather and distance scope — alpha 14

**Historical release record.** Some counts and feature gaps below were superseded in alpha 16 and the independent alpha 17–20 engine. Consult the [2026-10-22 completeness audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) for current source coverage and detailed remaining requirements; this document does not certify the native engine or current renderer.

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. These are authored features inside Deep Primal, not an installer for the referenced mods. The four existing engine modules remain separate. Vulkan contains the backend; graphics-settings owns presentation controls; engine-core contains deterministic math without Minecraft dependencies; server water rules and worldgen adapters remain in root packages.

## Implemented behavior

- Terrain-following, bed-backed stepped rivers retain sixteen stored downstream directions. Four persistent speed bands respond to slope and catchment size. Native water rendering, items and boats consume the same fluid vector. Non-player averaging preserves speed without adding duplicate horizontal propulsion. Old direction-only states default to normal speed.
- Water tint uses temperature, precipitation climate and elevation, with a continuous 16-block lattice over the registered native tint cache. Alpine water becomes clearer/cyan; warm wet lowlands become muted brown-green. Underwater clarity and depth shading are optional presentation effects, not a drinkability simulation.
- New `waterways_v4` profiles add offshore relief and narrow trench masks. Normal height remains -128..639; optional extra depth remains -384..639. The ocean floor stays at least forty blocks above world bottom. Existing generator IDs and all 44 older data/preset/pack resources remain byte-identical. This is not retrogen or repair of explored chunks.
- One indexed, shared ocean surface patch animates a three-component wave field over already loaded deep water. Radius defaults to 32 blocks, capped at 64; terrain coverage is re-sampled at most every ten ticks or on movement. Native water remains underneath; shore cells are omitted. Small boat buoyancy deltas reuse the field.
- Wakes, cascade spray and cold-weather flurries share one optional particle admission budget (24/tick, 0..128), reuse native particles and inspect at most 128 render entities. Duplicate water effects yield when Particular or Wakes is detected.
- Native precipitation visits one existing loaded column. Rain replenishes exposed non-source, non-falling water; it does not turn air into water. Cold snow stacks to a configurable finite cap (24 layers by default, 1..64), includes a bounded canopy-gap check, and raises intersecting entities when collision height grows. Snow leaf tint decorates native sources, including constant spruce and birch palettes.
- Optional world rules cover source regeneration, natural-river/ocean renewal, pressure above a configurable depth, player walking over source water beside farmland, and ridden-horse buoyancy/retention. Pressure scans the actual contiguous column above the eyes, stops at air, checks at most 512 blocks once per second, and exempts creative/spectator/conduit players. Pressure and farm walking default off.

## Controls

`config/deepprimal-worldgen.toml` selects creation-time terrain. `deepOceanTrenches=true` requires rivers, mountain streams and contained rivers. Disable it before creation to select the frozen v3 profile. Existing saves retain serialized generation settings.

World-synced gameplay options are in the save's `serverconfig/deepprimal-rivers.toml`. Presentation options are in `config/deepprimal-water-visuals.toml` and **Distances & performance → Water & snow**. Edits are staged until Apply; Cancel preserves settings. Performance controls and the local diagnostics dashboard remain independently available.

The maximum full-detail *request* is **125 chunks**, because Mojang's distance tracker stores `distance + 2` in a signed byte and the client-information packet also uses a signed byte. The independent procedural horizon permits **512 chunks**. Heap protection and server delivery limits remain active. This does not promise 125 loaded full-detail chunks or a detailed voxel/building/tree LOD at 512 chunks.

The horizon now uses 8/16/32-block grid spacing, shared edge anchors, smooth packed normals, native lightmap sampling and a smooth water overlap. Fine detail fits the minimum 256 KiB upload budget. Sampling, mesh memory and uploads remain bounded; distant filling is progressive.

## Reference comparison

The links below identify requested reference projects and clarify scope. Their code, assets and complete feature suites are not bundled.

| Reference | Deep Primal overlap and boundary |
| --- | --- |
| [Farmland Water](https://www.curseforge.com/minecraft/mc-mods/farmland-water) | Optional player collision beside farmland; native hydration stays intact. |
| [FiniteWater](https://www.curseforge.com/minecraft/mc-mods/finitewater) | Optional source regeneration policy. This is not a finite-volume solver. |
| [Natural Waters](https://www.curseforge.com/minecraft/mc-mods/natural-waters) | Blended climate/elevation tint and underwater clarity. |
| [Water Logistics](https://www.curseforge.com/minecraft/mc-mods/water-logistics) | Source policy only; pooling, draining and water-table depletion remain future work. |
| [Coastal Waves](https://www.curseforge.com/minecraft/mc-mods/coastal-waves) | Bounded GPU swells and boat motion; no wave entities or beach-loot system. |
| [Scary Water](https://www.curseforge.com/minecraft/mc-mods/scary-water) | Depth atmosphere/optional pressure; custom treading-height behavior is not included. |
| [Beg Water Pressure](https://www.curseforge.com/minecraft/mc-mods/beg-water-pressure) | Optional contiguous-column pressure, including air-pocket stops. |
| [Hydraulica](https://www.curseforge.com/minecraft/mc-mods/hydraulica) | Full finite-volume fluids, pumps and pipes are not included. |
| [Horses Can Swim](https://www.curseforge.com/minecraft/mc-mods/horses-can-swim) | Ridden-horse buoyancy and underwater rider retention. |
| [Flood](https://www.curseforge.com/minecraft/mc-mods/flood) | Exposed existing-flow rain refill; no expanding flood simulation. |
| [Wakes Reforged](https://www.curseforge.com/minecraft/mc-mods/wakes-reforged) | Budgeted native wake particles, not a mesh wake simulation. |
| [Flowing Rain](https://www.curseforge.com/minecraft/mc-mods/flowing-rain) | Native precipitation hooks; runoff, infiltration and evaporation are not included. |
| [Fluid Compact](https://www.curseforge.com/minecraft/mc-mods/fluid-compact) | Industrial fluid adapters are not included. |
| [Ocean Seam Fix](https://www.curseforge.com/minecraft/mc-mods/ocean-seam-fix-physics-mod-ocean-distant-horizons) | Deep Primal's own horizon/water overlap is addressed; external Physics Mod/Distant Horizons/Iris integration is not claimed. |
| [Sablewaves](https://www.curseforge.com/minecraft/mc-mods/sablewaves) | Three-component swells; no gyres, tides, ships or eight-component Gerstner suite. |
| [Snowy Weather](https://www.curseforge.com/minecraft/mc-mods/snowy-weather) | Cold-biome capped accumulation and flurries; warm biomes retain their climate. |
| [Particular](https://modrinth.com/mod/particular) | Native budgeted particles and duplicate-effect detection; the full mod is not bundled. |
| [Better Snowy Biomes](https://www.curseforge.com/minecraft/mc-mods/better-snowy-biomes) | Snow foliage/canopy inspiration; native biome generation remains. |
| [TFC DeepSnowStack Accumulation](https://www.curseforge.com/minecraft/mc-mods/tfc-deepsnowstack-accumulation) | Finite cold-weather stacking; no TFC dependency or addon code. |

The [river-flow comparison](RIVER-FLOW-COMPARISON-2026-10-19.md) covers TFC, Streams Reflowing, Underground Rivers, jungle-river projects and Sable Rivers. This release preserves the v3 containment geometry while adding speed and the new v4 ocean profile. Full finite-volume hydrology, water-table depletion, industrial adapters, mesh wakes and external shader/LOD interoperability remain separate development work.


<!-- END PRESERVED SOURCE -->
