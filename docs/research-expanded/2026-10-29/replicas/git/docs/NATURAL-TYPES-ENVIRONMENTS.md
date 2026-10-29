# Expanded replica — docs/NATURAL-TYPES-ENVIRONMENTS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/NATURAL-TYPES-ENVIRONMENTS.md) |
| Source identity | git: docs/NATURAL-TYPES-ENVIRONMENTS.md |
| Preserved source SHA-256 | `475cd1c670bdc65ae863fa207dc5e7a7e611bac88c183d71838230181a058699` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

The roster names 200 entries across water classes, snow/ice forms, volcanic/geothermal features, atmospheric conditions, habitats/landforms, and processes. It explicitly treats source, environment, and quality as overlapping water axes, distinguishes physical material from appearance and process, and avoids claiming full atmospheric chemistry or simulation. Every heading and explanatory passage plus table structure and representative rows was reviewed; individual scientific boundaries and rates were not independently verified. Names are planning categories, with runtime representation, regional mapping, exact effects, and numerical behavior left open.

## Research expansion — gaps and details to deepen

- Elaboration: the roster avoids separate magical fluids, but downstream systems still need canonical mappings that prevent rainwater, river water, groundwater, and contaminated water from becoming incompatible duplicate quantities.
- Elaboration: process names can mean generated history, a regional state, a local update, or appearance. Each selected process needs one explicit representation and owner before scheduling it.
- Actual scientific and gameplay parameters remain unresolved for hazard effects, chemistry, rates, and distributions. These should remain evidence gaps, not be filled by plausible-sounding defaults.
- Elaboration: snow, ice, liquid, vapor, moisture, and mixtures need shared water-equivalent bookkeeping where physical, while purely visual fog or spray must remain outside that ledger.

## Research expansion — proposed contracts

- Create a typed environmental record with category, canonical material or state reference, representation mode, source prerequisites, consumers, and evidence status. Habitat and process entries cannot be registered as substances accidentally.
- Map all water-source and quality labels to derived traits over the same water volume and constituent owners. Moving a sample preserves quantities and composition but strips renewable-source and ocean-boundary identity.
- For selected phase changes, define water-equivalent units, energy coupling where enabled, constituent retention, destination capacity, and failure behavior. Ice formation must not erase water, salt, or sediment.
- Require a process card to declare generated-history, appearance-only, loaded local, or coarse authoritative operation, with update budget and unload policy. An absent runtime implementation remains an explicit deferment.
- Connect habitat and landform records to stable biome/layer IDs instead of duplicating inventories; require actual substrate and hydrologic evidence before applying a physical feature.

## Research expansion — acceptance checks

- Move rain-, river-, and salt-labeled water through one container and verify labels do not duplicate volume or transferable source identity.
- Run freeze/thaw and evaporation/condensation fixtures with exact water and constituent accounting, including dry residues.
- Toggle purely visual fog, spray, and weather quality; require an identical authoritative state hash.
- Validate every selected process has a representation, owner, budget, and disabled behavior without treating all 45 listed processes as approved live simulation.
- Check habitat references and no-sinkhole constraints across catalogs and terrain layers.

## Research expansion — source anchors

- Named water sources, environments and quality classes
- Named snow, ice and frozen-material forms
- Named atmospheric conditions, moisture and particulates
- Named environmental processes

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Named environmental types — water, ice, atmosphere, habitats and processes

Planning update: 2026-10-20. **Named roster only; implementation remains deferred.**

[Master named-catalog index](NATURAL-TYPE-CATALOG.md) · [Environmental coverage inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md) · [Design reference](design-reference.md)

Coverage: Water source/quality/environment classes, snow/ice forms, volcanic/geothermal forms, atmospheric conditions/particles, habitats/landforms and environmental processes.

This file contains **200 named entries** across 6 sections. Entries include types, umbrella groups, subtypes, mixtures, anatomical parts, states, forms and processes as labeled. The count is not a count of distinct species, substances, blocks or approved features. Parent/child and cross-context rows can overlap.

**Existing named entry** preserves a row from the original design. **Planning expansion** adds a named candidate, explicit split or classification entry. Original scope decisions retain authority. New entries still need representation/disposition, regional selection and applicable property sheets before implementation; a name alone does not complete its behavior plan.

| Section | Named entries |
| --- | ---: |
| [Named water sources, environments and quality classes](#water-type) | 23 |
| [Named snow, ice and frozen-material forms](#snow-ice) | 24 |
| [Named volcanic/geothermal materials and features](#volcanic-form) | 20 |
| [Named atmospheric conditions, moisture and particulates](#air-form) | 26 |
| [Named habitat and landform checklist](#habitat-type) | 62 |
| [Named environmental processes](#process-type) | 45 |

<a id="water-type"></a>

## Named water sources, environments and quality classes

Source, environment and quality are separate axes; water belongs to more than one row. These are not separate magical fluids or purity guarantees.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| WATER-TYPE-001 | Rainwater | Source class | Precipitation input. | Planning expansion |
| WATER-TYPE-002 | Snowmelt | Source class | Seasonal melt input. | Planning expansion |
| WATER-TYPE-003 | Glacial meltwater | Source class | Ice-derived input. | Planning expansion |
| WATER-TYPE-004 | Spring water | Source class | Groundwater discharge source. | Planning expansion |
| WATER-TYPE-005 | Seep water | Source class | Diffuse groundwater discharge. | Planning expansion |
| WATER-TYPE-006 | Stream water | Environment class | Small flowing channel. | Planning expansion |
| WATER-TYPE-007 | River water | Environment class | Larger flowing channel. | Planning expansion |
| WATER-TYPE-008 | Lake water | Environment class | Inland standing water. | Planning expansion |
| WATER-TYPE-009 | Pond water | Environment class | Small standing water. | Planning expansion |
| WATER-TYPE-010 | Wetland water | Environment class | Marsh/swamp/bog/fen contexts. | Planning expansion |
| WATER-TYPE-011 | Groundwater | Storage/source class | Aquifer/pore/fracture water. | Planning expansion |
| WATER-TYPE-012 | Perched groundwater | Storage setting | Water above a local low-permeability layer. | Planning expansion |
| WATER-TYPE-013 | Artesian groundwater | Hydraulic setting | Confined-pressure setting. | Planning expansion |
| WATER-TYPE-014 | Hot-spring water | Thermal/source class | Geothermal input. | Planning expansion |
| WATER-TYPE-015 | Mineral-spring water | Composition/source class | Dissolved-mineral setting. | Planning expansion |
| WATER-TYPE-016 | Brackish water | Salinity class | Intermediate-salinity water. | Planning expansion |
| WATER-TYPE-017 | Seawater | Salinity/environment class | Ocean-source water. | Planning expansion |
| WATER-TYPE-018 | Hypersaline brine | Salinity class | Concentrated saltwater. | Planning expansion |
| WATER-TYPE-019 | Freshwater | Salinity class | Low-salinity class; not synonymous with safe drinking water. | Planning expansion |
| WATER-TYPE-020 | Turbid water | Suspended-material state | Sediment/particle-rich condition. | Planning expansion |
| WATER-TYPE-021 | Tannin-colored water | Dissolved-organic state | Organic coloration. | Planning expansion |
| WATER-TYPE-022 | Clear water | Optical state | Appearance separate from safety/chemistry. | Planning expansion |
| WATER-TYPE-023 | Contaminated water | Quality state | Contaminant identity requires later definition. | Planning expansion |

<a id="snow-ice"></a>

## Named snow, ice and frozen-material forms

Types and states are explicit. Separate visual representation, physical material and runtime processes.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| SNOW-ICE-001 | Fresh powder snow | Snow state | Newly fallen loose snow. | Planning expansion |
| SNOW-ICE-002 | Settled snow | Snow state | Compacted by settling. | Planning expansion |
| SNOW-ICE-003 | Packed snow | Snow state | Densified snow condition. | Planning expansion |
| SNOW-ICE-004 | Wind-packed snow | Snow state | Wind-affected snow. | Planning expansion |
| SNOW-ICE-005 | Snow crust | Snow surface state | Hardened surface layer. | Planning expansion |
| SNOW-ICE-006 | Melt-freeze crust | Snow state | Melt/refreeze surface. | Planning expansion |
| SNOW-ICE-007 | Wind drift | Snow deposit form | Wind-deposited accumulation. | Planning expansion |
| SNOW-ICE-008 | Cornice | Snow deposit form | Overhanging accumulation. | Planning expansion |
| SNOW-ICE-009 | Slush | Snow/water mixture | Wet mixed state. | Planning expansion |
| SNOW-ICE-010 | Firn | Transitional snow/ice material | Long-lived compacted snow. | Planning expansion |
| SNOW-ICE-011 | Glacier ice | Ice environment/material | Glacial mass. | Planning expansion |
| SNOW-ICE-012 | Lake ice | Ice environment/form | Frozen lake surface. | Planning expansion |
| SNOW-ICE-013 | River ice | Ice environment/form | Frozen channel surface. | Planning expansion |
| SNOW-ICE-014 | Frazil ice | Ice crystal form | Waterborne fine ice crystals. | Planning expansion |
| SNOW-ICE-015 | Anchor ice | Ice attachment form | Submerged attached ice. | Planning expansion |
| SNOW-ICE-016 | Sea ice | Ice environment/material | Frozen seawater; salt behavior pending. | Planning expansion |
| SNOW-ICE-017 | Shore ice | Ice environment/form | Shore-bound ice. | Planning expansion |
| SNOW-ICE-018 | Icicle | Ice growth form | Hanging frozen water. | Planning expansion |
| SNOW-ICE-019 | Glaze ice | Ice coating | Smooth deposited ice. | Planning expansion |
| SNOW-ICE-020 | Rime ice | Ice deposit form | Droplet-freezing deposit. | Planning expansion |
| SNOW-ICE-021 | Hoarfrost | Ice crystal deposit | Vapor-derived surface frost. | Planning expansion |
| SNOW-ICE-022 | Ground-ice lens | Subsurface ice form | Ground ice distinct from soil composition. | Planning expansion |
| SNOW-ICE-023 | Ice wedge | Subsurface ice form | Frozen-ground feature candidate. | Planning expansion |
| SNOW-ICE-024 | Dirty ice | Ice/sediment mixture | Embedded particulate material. | Planning expansion |

<a id="volcanic-form"></a>

## Named volcanic/geothermal materials and features

Some materials already appear in geology. Shared names are cross-context references, not extra unique substances.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| VOLCANIC-FORM-001 | Volcanic ash fall | Deposit form | Ash deposition. | Planning expansion |
| VOLCANIC-FORM-002 | Lapilli fall | Deposit form | Coarser ejecta deposition. | Planning expansion |
| VOLCANIC-FORM-003 | Volcanic bomb | Ejecta form | Coarse erupted fragment form. | Planning expansion |
| VOLCANIC-FORM-004 | Volcanic block | Ejecta form | Coarse angular fragment. | Planning expansion |
| VOLCANIC-FORM-005 | Pumice fall | Deposit form | Pumice-rich ejecta. | Planning expansion |
| VOLCANIC-FORM-006 | Scoria cone material | Deposit/form class | Scoria-dominated deposit. | Planning expansion |
| VOLCANIC-FORM-007 | Volcanic breccia | Rock/deposit class | Fragmental volcanic material. | Planning expansion |
| VOLCANIC-FORM-008 | Welded tuff | Rock subtype | Links tuff/ignimbrite entries. | Planning expansion |
| VOLCANIC-FORM-009 | Lava flow | Molten/cooled landscape form | Composition and cooling separate. | Planning expansion |
| VOLCANIC-FORM-010 | Pahoehoe surface | Lava surface form | Named surface morphology. | Planning expansion |
| VOLCANIC-FORM-011 | Aa surface | Lava surface form | Named rough surface morphology. | Planning expansion |
| VOLCANIC-FORM-012 | Pillow lava | Lava form | Subaqueous emplacement morphology. | Planning expansion |
| VOLCANIC-FORM-013 | Lava tube | Geological feature | Tube/cave feature. | Planning expansion |
| VOLCANIC-FORM-014 | Fumarole | Geothermal feature | Gas/steam vent. | Planning expansion |
| VOLCANIC-FORM-015 | Geyser | Geothermal feature | Intermittent heated-water feature. | Planning expansion |
| VOLCANIC-FORM-016 | Hot-spring pool | Geothermal water feature | Thermal aquatic setting. | Planning expansion |
| VOLCANIC-FORM-017 | Mudpot | Geothermal feature | Heated muddy-water feature. | Planning expansion |
| VOLCANIC-FORM-018 | Siliceous sinter | Mineral deposit | Silica-rich precipitate. | Planning expansion |
| VOLCANIC-FORM-019 | Travertine terrace | Deposit landform | Links retained travertine material. | Planning expansion |
| VOLCANIC-FORM-020 | Sulfur crust | Mineral deposit | Sulfur-rich surface deposit. | Planning expansion |

<a id="air-form"></a>

## Named atmospheric conditions, moisture and particulates

Conditions, particles and gases are labeled; no full atmospheric chemistry or live particulate simulation is promised.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| AIR-FORM-001 | Dry air | Moisture condition | Humidity state. | Planning expansion |
| AIR-FORM-002 | Humid air | Moisture condition | Humidity state. | Planning expansion |
| AIR-FORM-003 | Wind | Air-motion process | Exposure/transport input. | Planning expansion |
| AIR-FORM-004 | Gust | Air-motion event | Short wind variation. | Planning expansion |
| AIR-FORM-005 | Rain | Precipitation form | Liquid precipitation. | Planning expansion |
| AIR-FORM-006 | Drizzle | Precipitation form | Fine liquid precipitation. | Planning expansion |
| AIR-FORM-007 | Snowfall | Precipitation form | Snow precipitation. | Planning expansion |
| AIR-FORM-008 | Sleet / ice pellets | Precipitation/common term | Resolve regional naming before mechanics. | Planning expansion |
| AIR-FORM-009 | Freezing rain | Precipitation condition | Supercooled liquid depositing ice. | Planning expansion |
| AIR-FORM-010 | Hail | Precipitation form | Ice-pellet/hailstone form. | Planning expansion |
| AIR-FORM-011 | Fog | Suspended-droplet condition | Near-surface cloud. | Planning expansion |
| AIR-FORM-012 | Mist | Suspended-droplet condition | Visibility/coverage representation. | Planning expansion |
| AIR-FORM-013 | Dew | Surface-water deposit | Condensed moisture. | Planning expansion |
| AIR-FORM-014 | Dust | Airborne material mixture | Mineral/organic constituents separate. | Planning expansion |
| AIR-FORM-015 | Blown sand | Transported sediment state | Shared sediment identity. | Planning expansion |
| AIR-FORM-016 | Volcanic ash cloud | Airborne material state | Shared ash identity. | Planning expansion |
| AIR-FORM-017 | Pollen | Biological particulate | Species source/season links. | Planning expansion |
| AIR-FORM-018 | Spores | Biological propagules | Source identity and dispersal links. | Planning expansion |
| AIR-FORM-019 | Smoke | Combustion mixture | Source/fuel and ventilation links. | Planning expansion |
| AIR-FORM-020 | Soot deposition | Surface coating state | Shared soot material. | Planning expansion |
| AIR-FORM-021 | Sea spray | Aerosol/water mixture | Salt/water exposure. | Planning expansion |
| AIR-FORM-022 | Steam plume | Thermal/condensation feature | Distinguish water vapor from visible droplets. | Planning expansion |
| AIR-FORM-023 | Oxygen-depleted air | Gas-mixture condition | Optional enclosed-area hazard. | Planning expansion |
| AIR-FORM-024 | Carbon dioxide-rich pocket | Gas-mixture condition | Optional cave/vent candidate. | Planning expansion |
| AIR-FORM-025 | Methane-bearing pocket | Gas-mixture condition | Optional subsurface hazard. | Planning expansion |
| AIR-FORM-026 | Sulfurous vent gas | Gas-mixture candidate | Actual constituents/effects require later definition. | Planning expansion |

<a id="habitat-type"></a>

## Named habitat and landform checklist

These are ecological/landform contexts rather than material IDs. Local representatives and boundaries remain planning work.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| HABITAT-TYPE-001 | Boreal forest | Terrestrial habitat | Cold forest. | Planning expansion |
| HABITAT-TYPE-002 | Temperate deciduous forest | Terrestrial habitat | Seasonal broadleaf forest. | Planning expansion |
| HABITAT-TYPE-003 | Temperate rainforest | Terrestrial habitat | Moist temperate forest. | Planning expansion |
| HABITAT-TYPE-004 | Tropical rainforest | Terrestrial habitat | Moist tropical forest. | Planning expansion |
| HABITAT-TYPE-005 | Tropical seasonal forest | Terrestrial habitat | Seasonal tropical forest. | Planning expansion |
| HABITAT-TYPE-006 | Dry woodland | Terrestrial habitat | Open/dry woody cover. | Planning expansion |
| HABITAT-TYPE-007 | Mediterranean woodland | Terrestrial habitat | Seasonal dry-summer coverage. | Planning expansion |
| HABITAT-TYPE-008 | Savanna | Terrestrial habitat | Grass/woody mosaic. | Planning expansion |
| HABITAT-TYPE-009 | Prairie | Grassland habitat | Regional grassland coverage. | Planning expansion |
| HABITAT-TYPE-010 | Steppe | Grassland habitat | Dry grassland coverage. | Planning expansion |
| HABITAT-TYPE-011 | Meadow | Grassland habitat | Herb/grass coverage. | Planning expansion |
| HABITAT-TYPE-012 | Heathland | Shrubland habitat | Heath/scrub coverage. | Planning expansion |
| HABITAT-TYPE-013 | Alpine meadow | High-elevation habitat | High-elevation herb/grass cover. | Planning expansion |
| HABITAT-TYPE-014 | Alpine scree | High-elevation habitat | Rock-fragment slope. | Planning expansion |
| HABITAT-TYPE-015 | Tundra | Cold habitat | Low vegetation/permafrost contexts. | Planning expansion |
| HABITAT-TYPE-016 | Hot desert | Arid habitat | Warm arid coverage. | Planning expansion |
| HABITAT-TYPE-017 | Cold desert | Arid habitat | Cold arid coverage. | Planning expansion |
| HABITAT-TYPE-018 | Rocky desert | Arid substrate habitat | Exposed stone/regolith. | Planning expansion |
| HABITAT-TYPE-019 | Gravel desert | Arid substrate habitat | Coarse sediment surface. | Planning expansion |
| HABITAT-TYPE-020 | Dune field | Aeolian landform | Wind-shaped sand. | Planning expansion |
| HABITAT-TYPE-021 | Salt flat | Evaporitic landform | Saline exposed basin. | Planning expansion |
| HABITAT-TYPE-022 | Desert wash | Intermittent fluvial landform | Episodic-flow channel. | Planning expansion |
| HABITAT-TYPE-023 | Oasis | Water-supported habitat | Local arid-region water setting. | Planning expansion |
| HABITAT-TYPE-024 | Headwater stream | Freshwater habitat | Upland small channel. | Planning expansion |
| HABITAT-TYPE-025 | Mountain gorge | Fluvial landform | Steep confined valley. | Planning expansion |
| HABITAT-TYPE-026 | Lowland river | Freshwater habitat | Broad low-gradient channel. | Planning expansion |
| HABITAT-TYPE-027 | Floodplain | Fluvial habitat/landform | Flood-associated valley floor. | Planning expansion |
| HABITAT-TYPE-028 | Alluvial fan | Depositional landform | Fan-shaped sediment setting. | Planning expansion |
| HABITAT-TYPE-029 | Lake shore | Freshwater boundary | Shore exposure/deposit setting. | Planning expansion |
| HABITAT-TYPE-030 | Seasonal pond | Freshwater habitat | Variable-duration water. | Planning expansion |
| HABITAT-TYPE-031 | Marsh | Wetland habitat | Emergent-vegetation coverage. | Planning expansion |
| HABITAT-TYPE-032 | Swamp | Wetland habitat | Woody wetland coverage. | Planning expansion |
| HABITAT-TYPE-033 | Bog | Peatland habitat | Peatland coverage. | Planning expansion |
| HABITAT-TYPE-034 | Fen | Peatland habitat | Peatland coverage. | Planning expansion |
| HABITAT-TYPE-035 | Wet meadow | Wetland boundary | Seasonally wet herb/grass ground. | Planning expansion |
| HABITAT-TYPE-036 | Rocky intertidal shore | Coastal habitat | Rock/tide boundary. | Planning expansion |
| HABITAT-TYPE-037 | Sandy beach | Coastal habitat | Sand shore. | Planning expansion |
| HABITAT-TYPE-038 | Shingle beach | Coastal habitat | Coarse-clast shore. | Planning expansion |
| HABITAT-TYPE-039 | Coastal dune | Coastal landform/habitat | Windblown shore deposits. | Planning expansion |
| HABITAT-TYPE-040 | Estuary | Coastal water/habitat | River/sea mixing. | Planning expansion |
| HABITAT-TYPE-041 | Delta | Coastal/fluvial landform | Mouth deposition. | Planning expansion |
| HABITAT-TYPE-042 | Tidal mudflat | Coastal habitat | Fine exposed/submerged substrate. | Planning expansion |
| HABITAT-TYPE-043 | Salt marsh | Coastal wetland | Saline emergent-vegetation coverage. | Planning expansion |
| HABITAT-TYPE-044 | Mangrove forest | Coastal wetland | Woody saline/tidal coverage. | Planning expansion |
| HABITAT-TYPE-045 | Seagrass meadow | Marine habitat | Flowering-plant cover. | Planning expansion |
| HABITAT-TYPE-046 | Kelp forest | Marine habitat | Algal canopy. | Planning expansion |
| HABITAT-TYPE-047 | Coral reef | Marine habitat | Biogenic framework. | Planning expansion |
| HABITAT-TYPE-048 | Rock pool | Intertidal microhabitat | Isolated tidal water. | Planning expansion |
| HABITAT-TYPE-049 | Continental shelf floor | Marine substrate context | Shallow offshore sediments. | Planning expansion |
| HABITAT-TYPE-050 | Deep seafloor | Marine substrate context | Deep offshore substrate. | Planning expansion |
| HABITAT-TYPE-051 | Karst cave | Underground habitat/landform | Dissolution-cave setting. | Planning expansion |
| HABITAT-TYPE-052 | Dry cave | Underground environment | Dry substrate coverage. | Planning expansion |
| HABITAT-TYPE-053 | Wet cave | Underground environment | Seep/pool/stream coverage. | Planning expansion |
| HABITAT-TYPE-054 | Underground river | Underground water feature | Connected flowing water. | Planning expansion |
| HABITAT-TYPE-055 | Guano chamber | Underground microhabitat | Animal-derived deposits. | Planning expansion |
| HABITAT-TYPE-056 | Burn scar | Disturbed habitat | Fire-affected landscape. | Planning expansion |
| HABITAT-TYPE-057 | Windthrow gap | Disturbed microhabitat | Fallen-tree canopy opening. | Planning expansion |
| HABITAT-TYPE-058 | Landslide scar | Disturbed landform | Mass-movement exposure. | Planning expansion |
| HABITAT-TYPE-059 | Regrowing clearing | Successional habitat | Recovery-stage cover. | Planning expansion |
| HABITAT-TYPE-060 | Forest edge | Habitat boundary | Forest/open-ground transition. | Planning expansion |
| HABITAT-TYPE-061 | Treeline | Habitat boundary | Woody/alpine transition. | Planning expansion |
| HABITAT-TYPE-062 | Seep hollow | Microhabitat | Locally wet ground. | Planning expansion |

<a id="process-type"></a>

## Named environmental processes

Named process coverage completes the inventory axis; each may be represented by generated history, regional state, local updates or appearance.

| Catalog ID | Name | Kind | Role or relationship to specify | Provenance |
| --- | --- | --- | --- | --- |
| PROCESS-TYPE-001 | Mechanical weathering | Geological process | Physical breakdown. | Planning expansion |
| PROCESS-TYPE-002 | Chemical weathering | Geological process | Chemical alteration. | Planning expansion |
| PROCESS-TYPE-003 | Biological weathering | Geological process | Organism-mediated effects. | Planning expansion |
| PROCESS-TYPE-004 | Soil-profile development | Pedogenic process | Profile formation/history. | Planning expansion |
| PROCESS-TYPE-005 | Leaching | Material-transfer process | Dissolved/particle transfer in profiles. | Planning expansion |
| PROCESS-TYPE-006 | Compaction | Physical process | Ground-density/structure change. | Planning expansion |
| PROCESS-TYPE-007 | Desiccation | Drying process | Moisture loss/cracking links. | Planning expansion |
| PROCESS-TYPE-008 | Fluvial erosion | Sediment process | River/runoff removal. | Planning expansion |
| PROCESS-TYPE-009 | Bank erosion | Sediment process | Channel-margin removal. | Planning expansion |
| PROCESS-TYPE-010 | Wind erosion | Sediment process | Aeolian removal. | Planning expansion |
| PROCESS-TYPE-011 | Slope creep | Mass-movement process | Slow downslope movement. | Planning expansion |
| PROCESS-TYPE-012 | Rockfall | Mass-movement event | Detached rock movement. | Planning expansion |
| PROCESS-TYPE-013 | Landslide | Mass-movement event | Slope failure. | Planning expansion |
| PROCESS-TYPE-014 | Sediment transport | Material-transfer process | Movement of loose particles; full live solver optional/default-off. Recipe-first panning/sluices; useful local provenance may be added. | Owner scope clarified; see [terrain mechanics](TERRAIN-MECHANICS.md) |
| PROCESS-TYPE-015 | Sediment deposition | Material-transfer process | Accumulation. | Planning expansion |
| PROCESS-TYPE-016 | Hydrologic recharge | Water process | Water-storage replenishment. | Planning expansion |
| PROCESS-TYPE-017 | Infiltration | Water process | Entry into ground. | Planning expansion |
| PROCESS-TYPE-018 | Percolation | Water process | Subsurface movement. | Planning expansion |
| PROCESS-TYPE-019 | Runoff | Water process | Surface flow. | Planning expansion |
| PROCESS-TYPE-020 | Evaporation | Phase/water process | Liquid-water loss. | Planning expansion |
| PROCESS-TYPE-021 | Transpiration | Biological water process | Plant water transfer. | Planning expansion |
| PROCESS-TYPE-022 | Freezing | Phase process | Water/ground thermal transition. | Planning expansion |
| PROCESS-TYPE-023 | Thawing | Phase process | Frozen-ground transition. | Planning expansion |
| PROCESS-TYPE-024 | Snowmelt | Phase/water process | Snow-water transition. | Planning expansion |
| PROCESS-TYPE-025 | Germination | Plant process | Propagule establishment. | Planning expansion |
| PROCESS-TYPE-026 | Vegetative propagation | Plant process | Non-seed reproduction. | Planning expansion |
| PROCESS-TYPE-027 | Growth | Biological process | Biomass development. | Planning expansion |
| PROCESS-TYPE-028 | Flowering | Plant process | Reproductive stage. | Planning expansion |
| PROCESS-TYPE-029 | Pollination | Ecological process | Pollen transfer. | Planning expansion |
| PROCESS-TYPE-030 | Fruiting | Plant process | Reproductive-product stage. | Planning expansion |
| PROCESS-TYPE-031 | Seed dispersal | Ecological process | Propagule movement. | Planning expansion |
| PROCESS-TYPE-032 | Dormancy | Biological state/process | Growth-cycle pause. | Planning expansion |
| PROCESS-TYPE-033 | Senescence | Biological process | Aging/death transition. | Planning expansion |
| PROCESS-TYPE-034 | Decomposition | Biological/chemical process | Organic matter breakdown. | Planning expansion |
| PROCESS-TYPE-035 | Nutrient mineralization | Nutrient process | Organic-to-inorganic nutrient transformation. | Planning expansion |
| PROCESS-TYPE-036 | Nitrogen fixation | Biological nutrient process | Existing ecological-function connection. | Planning expansion |
| PROCESS-TYPE-037 | Grazing | Ecological interaction | Grass/herb consumption. | Planning expansion |
| PROCESS-TYPE-038 | Browsing | Ecological interaction | Woody/leaf consumption. | Planning expansion |
| PROCESS-TYPE-039 | Scavenging | Ecological interaction | Carcass/resource consumption. | Planning expansion |
| PROCESS-TYPE-040 | Burrowing | Ecological/physical process | Soil/habitat disturbance. | Planning expansion |
| PROCESS-TYPE-041 | Succession | Ecological process | Community change. | Planning expansion |
| PROCESS-TYPE-042 | Coppice regrowth | Plant-management process | Existing forestry direction. | Planning expansion |
| PROCESS-TYPE-043 | Wildfire | Disturbance process | Fuel/heat/landscape change. | Planning expansion |
| PROCESS-TYPE-044 | Tidal exposure | Coastal process | Wet/dry boundary changes. | Planning expansion |
| PROCESS-TYPE-045 | Wave action | Coastal process | Motion/erosion/deposition linkage. | Planning expansion |

## Naming and research status

The finite lists explicitly identified as complete are complete at their stated classification level. Open-ended rock, mineral, plant, fungus, habitat and regional-variant lists remain extensible. Taxonomic ranks differ and common names can be ambiguous. No exhaustive global species inventory, scientific-property database, or verified geographic distribution set is claimed.

Candidate roles/relationships guide later specification. They are not automatically real-world property guarantees or approved survival/processing behavior. All inherited design traits remain subject to the original reference's research caveat.

## Sources and validation anchors

- [Owner water/aquifer decisions](design-decisions-2026-10-18.md#water-aquifers-and-wells): existing source/quality model and scope.
- [Environmental inventory research anchors](ENVIRONMENTAL-COVERAGE-INVENTORY.md#research-anchors-and-limits): geology, soils, decay, biological-crust and habitat sources.
- [NOAA estuarine habitats](https://oceanservice.noaa.gov/education/tutorial_estuaries/est06_habitats.html): coastal habitat diversity.
- [NOAA ecosystem overview](https://sanctuaries.noaa.gov/visit/ecosystems/): habitat cross-check resource.

These names are proposed game-planning categories. Exact scientific boundaries, rates, chemistry, hazard behavior and geographic mappings are not verified or locked by this roster.


<!-- END PRESERVED SOURCE -->
