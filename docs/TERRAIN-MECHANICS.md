# Terrain mechanics — geology, ground and underground water

**Owner decisions: 2026-10-21, America/Los_Angeles.** Natural, geologically grounded terrain is the governing direction. This records the owner's accepted mechanics and exclusions after the water-cycle discussion. Source context: main **640fe28794799da3673995b1883caf20225addd6**, containing the alpha 21 runtime.

**Planning only.** These decisions do not implement the natural-material catalogs, a cave/ore generator, hazards, machinery or new climate model. Existing serialized terrain remains versioned. The biome roster comes first; climate later gates placement and supplies appropriate conditions.

## Scope decisions

| Topic | Decision |
| --- | --- |
| Structural geology | Accepted: realistic rock bodies/layers, folds/faults, intrusions, exposed rock, caves and geology-controlled minerals. |
| Elevation and deposits | Include both ores and gems where geology supports them. Do not restrict high elevations to gems or assume every mountain is ore-rich. |
| Erosion resistance | Accepted as already described: material-dependent cliffs, softer eroded reaches, ledges and gullies. Extend TERRAIN-03, rather than inventing a second erosion system. |
| Sediment transport | A full live sediment-transport/deposition system is **not required by default**. Baseline panning/sluices use consumed materials and recipes. Add an optional surrounding-geology connection only when it produces useful, observable gameplay. |
| Underground drainage | Accepted: rock/soil-dependent flooding and drainage, caves, karst, returning springs and rivers through mountains. |
| Sinkholes | **Excluded:** no generated sinkhole landforms and no live surface-collapse/sinkhole mechanic. |
| Soil depth/horizons | Accepted: coherent profiles and depth changes tied to parent material and landscape setting. |
| Wind-shaped terrain | Accepted: plausible dunes, wind exposure and deposition patterns. No mandatory globally ticking sand simulation. |
| Slope stability/cave-ins | Accepted: material/support-dependent slope failures and underground cave-ins, respecting the sinkhole exclusion. |
| Natural obstructions | Accepted: appropriate fallen wood, debris and rock can obstruct flow, pond water and redirect a channel within the shared water budget. |

The larger inventories remain reference catalogs. Names in those catalogs do not silently override these scope decisions or require every process to run during play.

## TERRAIN-05 — structural geology and mineral placement

- [ ] **Rock architecture:** generate coherent three-dimensional geological units across regions, with appropriate thickness, extent, orientation and contacts. Sedimentary bedding can be tilted/folded/faulted; igneous bodies can cut across older units; metamorphic units can show foliation/contact changes. Do not turn every rock class into identical flat alternating stripes.
- [ ] **Structural relationships:** plan beds, folds, faults, dikes, sills, larger intrusions and weathered/exposed contacts before placing dependent features. River cuts and cliffs reveal the same units present beneath adjacent terrain. Faults are geological structures; this decision does not add live earthquakes or plate simulation.
- [ ] **Deposits:** use host rock, formation setting, structural pathways/contact zones and appropriate formation/burial history to select ore/gem occurrences. Absolute current Y/elevation is not the primary eligibility switch. Present exposure, cover depth and erosion history can affect discoverability.
- [ ] **Scarcity and grade:** distinguish barren host rock, ordinary mineralization, lower-grade bodies, concentrated veins/lenses and rare rich pockets. Most rock is not ore or gem quality. Large bodies need not be made of uniformly rich blocks.
- [ ] **Gems:** choose crystal/gem species by compatible host/deposit setting. Crystals are not automatically gem-quality, and crystals do not replace ordinary metal deposits at high elevation. Keep gemstone pockets and metal-bearing systems individually configurable/data-driven without promising every catalog species in the prototype.
- [ ] **Discovery and depletion:** provide geologically consistent outcrops/indicators and finite deposits, preserving existing finite-vein/high-reserve-bedrock decisions. A layer exposed on a mountain must not create free duplicate resources in a nearby recipe table.
- [ ] **Acceptance:** inspect cross-sections through a faulted sedimentary region, intrusive contact and metamorphic region; trace units across chunk/region boundaries. Verify eligible high-elevation ore and gem occurrences, barren areas, rarity/grade distribution and no generation-order dependence. Record generation/memory costs and new-world version behavior.

**Elevation correction:** significant metal ore deposits do occur in mountains. The Climax operator describes a porphyry molybdenum ore body and mining elevations roughly 3,100–4,050 m above sea level. That counterexample rules out a universal high-elevation ore ban; it does not justify enriching every mountain. Ore formation depth, current cover and current elevation are different quantities.

Primary grounding: [Climax operator](https://www.climaxmolybdenum.com/operations/usa), [USGS porphyry/epithermal systems](https://www.usgs.gov/publications/porphyry-and-epithermal-mineral-deposits), [USGS gemstone environments](https://pubs.usgs.gov/gip/gemstones/environment.html), and [NPS cross-cutting relationships](https://www.nps.gov/articles/geologic-principles-cross-cutting-relationships.htm). These inform our simplified generation rules; they are not claims of a scientific simulation.

### Flexible deposit authoring — owner direction, 2026-10-21 PDT

Use TFC/GTCEu-inspired flexibility through one native deposit schema: geometry, host rock/contact eligibility, occurrence/selection weight versus density, vertical reference modes, multi-ore/gem composition/grades, indicators and finite/bedrock reserve rules. Support datapack definitions, config tuning and optional versioned KubeJS transforms; a visual companion editor is proposed. All authoring must compile to deterministic native planning data, not per-block scripts. See [configuration and ore authoring](CONFIGURATION-AND-ORE-AUTHORING.md); implementation remains open.

## TERRAIN-03 clarification — resistance without mandatory sediment simulation

Keep the accepted rock-specific resistance direction. Use coherent weathering/erosion histories and compact generation-time summaries to shape cliffs, ledges, channels, exposed rock and loose slope material. Share material properties with excavation and stability.

A regional historical sediment/deposit summary can support believable surfaces without moving every grain during play. Full live erosion, suspended solid transport, settling/resuspension and changing bank/deposit geometry remain optional, default-off extensions requiring a distinct useful outcome and budget. Existing water appearance/composition blending remains planned; it can use coarse source/composition summaries without a terrain-changing sediment solver.

### Baseline panning/sluices and optional local provenance

- [ ] Define baseline panning and different sluice types through data-driven input/output recipes, consumed material quantities, recovery/throughput and the selected progression. Exact machines, recipes and launch scope remain open.
- [ ] If environment-aware prospecting is retained, tie a material's potential yield to valid upstream/local host deposits, bed composition and a finite or explicitly replenished source. Let players discover useful geographical clues instead of receiving unrelated valuables from any water.
- [ ] Choose the cheapest useful representation: generated deposit/provenance tags or coarse catchment summaries before a live solid-transport solver. The feature must visibly improve locating/working local materials; otherwise keep recipe-only operation.
- [ ] Do not mint endless ore/gems because water flows, is muddy, crosses a chunk border or passes several sluices. Consumed recipe inputs and any contextual deposit reserve must not be counted twice.

## TERRAIN-06 — caves, through-mountain rivers and returning springs

- [ ] **Rock/soil permeability:** use material porosity/permeability, fractures/joints, saturation and available aquifer capacity to distinguish infiltration, temporary flooding, drainage and seep/spring release. Do not make all soil or all solid rock equally watertight.
- [ ] **Connected subsurface routes:** fit caves and water paths to geological units, fractures and hydraulic conditions. Use dissolution-cave forms only in suitable settings; other underground channels can have appropriate non-karst origins. Not every cave contains water.
- [ ] **Mountain rivers:** plan a channel entering a supported mountain portal, traversing connected passages and re-emerging downstream where terrain/head permits. Allow chambers, bends, varied cross-sections, side inflows, pools and supported cascades without repeating a uniform tunnel.
- [ ] **Hydraulic continuity:** share surface catchment supply, aquifer stores, tunnel/outlet capacity and head with the [water mechanics](WATER-BUDGET-REQUIREMENTS.md). No duplicated entry/exit source, magic uphill open-water flow or independent endless spring refill. Pressure behavior must be explicitly supported where a passage is submerged.
- [ ] **Water states:** allow dry/flooded passage changes and perched water where materials/head support them. A returning spring must debit the linked store; blocking a passage can back water up or divert it into another valid route.
- [ ] **No sinkholes:** exclude dolines/collapse pits and live surface-collapse drainage features. Infiltration and disappearing surface flow may use permeable beds, cracks and normal lateral cave portals without generating a sinkhole. Sinkhole-producing collapse propagation is not an accepted cave-in behavior.
- [ ] **Acceptance:** trace one water budget from mountain river entry to exit and a separate permeable-ground-to-spring route. Test high/low supply, rock/soil contrasts, sealed versus connected branches, a blocked outlet, loaded/unloaded boundaries and zero generated sinkholes. Verify meaningful roofs/beds, no hovering water curtains and supported entrances.

Reference: [Underground Rivers](https://www.curseforge.com/minecraft/mc-mods/underground-rivers). Its author describes rewritten underground rivers with varied entrances and uses Lithostitched for compatibility. This is an entrance/geometry reference, not proof of Deep Primal finite-water behavior or a required dependency. The linked Source points to the author's smaller-project collection; no complete upstream implementation audit or code adoption is claimed here.

Scientific distinction: [USGS karst overview](https://www.usgs.gov/mission-areas/water-resources/science/science-topics/karst-sinkholes-and-land-subsidence) includes caves/springs and also sinkholes in real karst. Deep Primal deliberately selects the former and excludes the latter by owner choice. Do not claim complete reproduction of every karst landform.

The two supplied external image-preview URLs were attempted but could not be retrieved during this review. They remain visual leads rather than verified image-specific geometry or acceptance evidence; the explicit through-mountain-river requirement is recorded above.

## TERRAIN-07 — soil depth and horizons

- [ ] Build soil profiles from parent material, topographic position, retained moisture, organic input and generated history. Distinguish surface litter/organic matter, topsoil, appropriate subsoil/weathered material and underlying bedrock; not every location has every horizon.
- [ ] Vary depth coherently: exposed/thin rocky uplands, deposited valley/basin soils and locally wet/organic profiles. Cuts, cliffs and excavation reveal actual layers rather than a surface tint over the same uniform dirt stack.
- [ ] Share permeability, saturation, fertility/chemistry, rooting, frozen-ground and digging properties with water, ecology and existing material systems. Preserve composition through wet/dry/frozen states without registering every combination as a separate block.
- [ ] Accept ridge-to-valley and exposed-cut cross-sections, a draining versus retaining profile, gradual regional boundaries and saved edits. Preserve existing natural-type rosters and biome-first planning; numerical climate/soil tuning remains open.

## TERRAIN-08 — wind-shaped terrain

- [ ] Generate dune forms, sand sheets, sheltered accumulations, windward exposure and compatible windblown fine deposits from available material, terrain and a coherent wind history. Include cold/coastal settings where eligible, not only hot desert.
- [ ] Let exposure, obstacles and planned vegetation stabilization influence placement. Use shared future wind/climate inputs after biome identity is defined; no independent contradictory wind field per feature.
- [ ] Start with deterministic generated forms and bounded seasonal snowdrift integration. Any live sand movement is optional and must conserve accounted material, protect occupancy/builds and meet update budgets.
- [ ] Accept consistent windward/leeward examples, sheltered/exposed coasts and dunes, material-supply limits and region seams. Preserve the low-resolution/shared-tint asset direction.

Primary grounding: [NPS aeolian landforms](https://www.nps.gov/subjects/geology/aeolian-landforms.htm). Our implementation can represent plausible resulting forms without simulating geological time or ticking every sand block.

## TERRAIN-09 — slope stability and underground cave-ins

- [ ] Use material strength, fracture/bedding orientation, support/span, steepness and moisture/freeze-thaw state to determine stability. Different rock/soil profiles should produce different safe spans and failure tendencies.
- [ ] Generate plausible stable faces and existing scree/talus/failure scars. During play, mining/undercutting, saturation or another defined disturbance may activate a local check; ordinary solid terrain must not continuously run a whole-world collapse scan.
- [ ] Include underground cave-ins and supports within the survival/progression design. Support placement/removal and excavation must have understandable consequences; expose warning/inspection and configurable severity as appropriate. Do not treat every stone block as freely falling gravel.
- [ ] Bound propagation, moved blocks, entities, effects and saved work. Keep failures conservative and reproducible through unload/restart. Generation cleanup must not silently erase player construction; gameplay collapse uses its own explicit rules.
- [ ] Enforce the sinkhole exclusion: no collapse mechanic that opens a surface sinkhole. Limit cave-in propagation before that breach; do not reintroduce the excluded feature under a different label. This does not prohibit ordinary player-dug shafts or accepted surface slope failures.
- [ ] Accept unsupported versus supported excavation, strong versus weak layered roofs, dry versus saturated slopes, a local rockfall and a shallow roof that cannot breach into a sinkhole. Verify bounded tick/memory/network cost and no global cascade.

## TERRAIN-10 — natural obstructions and drainage response

- [ ] Place appropriate fallen logs, root plates, woody debris and rockfall material according to local vegetation, substrate and disturbance history. Prefer ordinary block/feature representations over permanently ticking decorative entities.
- [ ] Let real channel occupancy change effective cross-section, pond water, create a spillway or redirect flow using the same local fluid and watershed authority. A cosmetic twig/foam particle is not a physical dam.
- [ ] Define obstruction persistence, saturation/decay, breakage or removal and the downstream release of retained water. Optional event changes must be bounded; do not restore a removed obstruction by regenerating the feature repeatedly.
- [ ] Accept a partial obstruction, complete local blockage, bypass/outlet formation and removal with an accounted release. Verify no source duplication, unsupported sheets, permanent global ocean rise or uncontrolled region-wide update chain.

## Integration and completion

Cache immutable versioned geology/terrain results once per region. Resolve shared boundaries before chunks consume them; workers must not mutate neighboring live chunks. Keep material definitions, placement, water ownership, recipes and rendering responsibilities distinct but connected through common IDs/properties.

Implementation remains open until each mechanic has a native owner, effective configuration/defaults, persistence/network semantics, deterministic generation and named-hardware performance/visual evidence. Add selected mechanics to the [roadmap](../roadmap.md); retain [environmental inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md) coverage and [biome layers](BIOME-VARIANTS-AND-COVERAGE.md) without interpreting names as implementation.

Existing speculative items not explicitly approved in this discussion retain their previous status. No new release scope, literal global tectonic simulation or mandatory sediment solver follows merely from the realism direction.

## Alpha 26 implementation update — 2026-10-24

New worlds now use saved `continental_v4`: bank containment, folded/faulted rock provinces, intrusions, finite host-dependent deposits, variable soil cover and latitude-adjusted temperature extend the existing independent continent/watershed/cave/vegetation pipeline. V1–V3 remain frozen. This is partial progress on the larger mechanics inventory, not acceptance of every checkbox above. Cave networks through mountains, deposit authoring, expanded catalogs, dunes, instability and natural-obstruction generation remain open. See [implementation scope](ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md) and [validation](VALIDATION-0.0.1-alpha.26.md).

