# Deep Primal — named biome planning catalog

Planning draft: 2026-10-21. **118 proposed biome profiles in 19 families.** These are qualitative design candidates; no registry entries, worldgen code or assets are implemented by this document.

[Biome architecture and decisions](BIOME-PLAN.md) · [Community variants and environmental layers](BIOME-VARIANTS-AND-COVERAGE.md) · [Natural material/type catalog](NATURAL-TYPE-CATALOG.md)

**Planning order: finish the biome roster first, then design climate scenarios to support it.** Climate descriptions below are qualitative identity cues for that later work, not a preselected climate grid or a gate for accepting a biome. Numerical thresholds remain deferred. When placing biomes, climate eligibility is required alongside terrain, water, substrate and other habitat requirements. This placement gate does not determine which biome types we include in the roster.

The profiles deliberately cover natural Overworld land, inland water, coasts, oceans and underground habitats. “Biome profile” is a proposed game representation, not a claim that all rows are equivalent scientific taxonomic units. Cave geometry, material color, dominant tree species, seasonal snow and many small habitats are represented separately.

## Roster summary

| Code | Family | Candidate profiles | Realm |
| --- | --- | ---: | --- |
| [POL](#pol) | Polar and tundra | 8 | Surface land |
| [BOR](#bor) | Boreal forests and woodland | 6 | Surface land |
| [TEM](#tem) | Temperate forest communities | 7 | Surface land |
| [MED](#med) | Mediterranean and dry shrublands | 5 | Surface land |
| [GRA](#gra) | Temperate grasslands and steppe | 6 | Surface land |
| [SAV](#sav) | Tropical grasslands and savannas | 5 | Surface land |
| [SUB](#sub) | Subtropical forest and scrub | 5 | Surface land |
| [TRO](#tro) | Tropical forests | 7 | Surface land |
| [ARD](#ard) | Hot arid and semiarid environments | 6 | Surface land |
| [COLD](#cold) | Cold arid and alpine dry environments | 3 | Surface land |
| [MNT](#mnt) | Montane and alpine ecosystems | 8 | Surface land |
| [RIV](#riv) | Flowing freshwater systems | 7 | Freshwater and riparian |
| [LAK](#lak) | Lakes, ponds and inland saline waters | 7 | Freshwater or inland saline water |
| [WET](#wet) | Freshwater wetlands and wet woodland | 6 | Freshwater–terrestrial transition |
| [COA](#coa) | Coasts and estuaries | 9 | Marine–freshwater–terrestrial transition |
| [MAR](#mar) | Shallow marine habitats | 7 | Marine |
| [OCE](#oce) | Open ocean and deep marine | 6 | Marine |
| [CAV](#cav) | Underground habitats | 8 | Subterranean and subterranean freshwater |
| [GEO](#geo) | Geothermal and volcanic surface habitats | 2 | Surface land and geothermal freshwater |

## Reading the profile rows

- **Proposed key:** a stable candidate name for future registry mapping, not an entry already registered.
- **Setting/ground:** material themes and physical setting. Ground codes below are selectors/themes, not new material definitions or forced soil taxonomy.
- **Vegetation/life:** candidate community pools; regional species must satisfy climate, water, substrate and biological-province eligibility. Fauna roster and yields remain separate existing plans.
- **Water regime:** supply, drainage, flow, exposure and salinity are separate from drinking quality.
- **Neighbors:** plausible transition examples, not a requirement for each pair to border directly. Actual adjacency must satisfy compatible climate, relief and water continuity, using intermediate profiles where necessary.
- Detailed taxa, resources and regional communities belong to the current biome-design pass. Numerical climate thresholds, spatial scale, frequency, rarity, spawning balance and final per-profile registry disposition remain open for later scenario/integration review.

## Ground-theme dictionary

| Code | Meaning |
| --- | --- |
| ROCK | Exposed parent rock and fractures; rock identity comes from geology. |
| THIN | Shallow mineral soil/regolith with local coarse fragments; texture and chemistry remain data. |
| FOREST | Forest-floor litter over an appropriate mineral-soil profile; no universal soil order. |
| LOAM | Mixed mineral-soil theme; select an actual texture/profile rather than forcing every cell to USDA loam. |
| SAND | Sand-dominated surface/deposit with source-rock composition and grain-size variants. |
| GRAVEL | Coarse-clast surface/deposit; rounded/angular forms follow history. |
| SILT | Fine silty deposit/theme; depth and composition follow sediment history. |
| CLAY | Clay-rich soil/deposit theme; mineralogy, particle size and working grade stay distinct. |
| ORGANIC | Organic-enriched ground/litter theme; not automatically deep peat. |
| PEAT | Accumulated organic ground; peat type, decomposition and water regime remain explicit. |
| MUD | Wet fine-sediment/organic mixture; no fixed chemistry or texture class. |
| SALT | Salt-affected deposits/ground; identify salts and chemistry later. |
| FROZEN | Thermal/profile modifier with seasonal frost/permafrost distinction. |
| ICE | Persistent ice surface/substrate; linked to thermal/water inputs. |
| ASH | Volcanic fine-deposit theme; composition and age follow geology. |
| MINERAL | Localized precipitates/altered substrate; select actual mineral/deposit identity. |
| CARBONATE | Carbonate-rich biogenic/mineral substrate; rock/sediment/reef identity remains separate. |
| SHELL | Shell-built bed or shell-fragment deposit; biological source and composition remain explicit. |
| WATER | Water-column profile; use actual seabed profile below it, not a water block substituted for ground. |

<a id="pol"></a>

## Polar and tundra

**Realm:** Surface land. **Climate context to support later:** Polar to subpolar; short growth season.

Persistent climate and substrate produce these profiles. Ordinary winter snow on another biome remains a seasonal state.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:polar_desert | Polar Desert | Cold arid exposed ground | ROCK, THIN, FROZEN |
| deepprimal:rock_tundra | Rock Tundra | Rocky polar/subpolar surfaces | ROCK, THIN, FROZEN |
| deepprimal:dry_tundra | Dry Tundra | Better-drained cold plains | THIN, LOAM, FROZEN |
| deepprimal:shrub_tundra | Shrub Tundra | Relatively sheltered subpolar low cover | LOAM, ORGANIC, FROZEN |
| deepprimal:tussock_tundra | Tussock Tundra | Cold rolling wet/dry ground mosaic | ORGANIC, PEAT, FROZEN |
| deepprimal:wet_tundra | Wet Tundra | Low-gradient saturated polar ground | PEAT, MUD, FROZEN |
| deepprimal:forest_tundra | Forest Tundra | Climatic transition to sparse trees | THIN, ORGANIC, FROZEN |
| deepprimal:icefield | Icefield | Persistent land ice | ICE, ROCK |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Polar Desert | Sparse cryptogams; little vascular cover | Limited seasonal melt | Cold Desert; Rock Tundra; Icefield |
| Rock Tundra | Lichens, mosses, scattered dwarf plants | Melt/runoff; local seeps | Polar Desert; Dry Tundra; Alpine Barrens |
| Dry Tundra | Dwarf shrubs, lichens, short herbs | Seasonal near-surface wetness varies | Rock Tundra; Shrub Tundra; Tussock Tundra |
| Shrub Tundra | Dwarf willow/birch; shrub patches | Local thaw-layer water | Dry Tundra; Forest Tundra; Wet Tundra |
| Tussock Tundra | Sedges, tussock grasses, mosses | Hummock/hollow wetness mosaic | Dry Tundra; Wet Tundra; Shrub Tundra |
| Wet Tundra | Sedges, mosses; shallow pools | Persistent/seasonal surface saturation | Tussock Tundra; Fen; Tundra Pond |
| Forest Tundra | Sparse boreal trees over tundra | Patches follow drainage | Shrub Tundra; Open Boreal Woodland |
| Icefield | No ordinary rooted cover on ice | Meltwater at margins | Polar Desert; Alpine Barrens; Glacial Lake |

<a id="bor"></a>

## Boreal forests and woodland

**Realm:** Surface land. **Climate context to support later:** Boreal; strong seasonality.

Tree-species dominance varies within regional pools. Snow cover alone does not create a second forest ID.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:closed_boreal_forest | Closed Boreal Forest | Continuous cold conifer canopy | FOREST, ORGANIC |
| deepprimal:open_boreal_woodland | Open Boreal Woodland | Sparse cold trees with open ground cover | THIN, ORGANIC |
| deepprimal:mixed_boreal_forest | Mixed Boreal Forest | Conifer and deciduous cold forest | FOREST, LOAM |
| deepprimal:dry_boreal_woodland | Dry Boreal Woodland | Cold dry sandy/rocky woodland | SAND, THIN |
| deepprimal:boreal_heath | Boreal Heath | Cold low shrub cover with sparse trees | THIN, ORGANIC |
| deepprimal:boreal_bog | Boreal Bog | Cold peatland within boreal matrix | PEAT |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Closed Boreal Forest | Spruce/fir/pine representatives by province | Normal drainage; wet pockets mapped separately | Mixed Boreal Forest; Open Boreal Woodland; Boreal Bog |
| Open Boreal Woodland | Conifers, lichens, dwarf shrubs | Relatively drained | Forest Tundra; Closed Boreal Forest; Dry Boreal Woodland |
| Mixed Boreal Forest | Conifers with birch/aspen groups | Moderate drainage | Closed Boreal Forest; Cool Mixed Forest |
| Dry Boreal Woodland | Pine/juniper-like regional groups; lichen ground | Low local moisture | Open Boreal Woodland; Cold Steppe; Boreal Heath |
| Boreal Heath | Heath, dwarf shrubs, lichens | Drainage-dependent wet pockets | Dry Boreal Woodland; Shrub Tundra; Boreal Bog |
| Boreal Bog | Sphagnum, dwarf shrubs; stunted trees optional | Rain-fed peatland profile candidate | Closed Boreal Forest; Fen; Wet Tundra |

<a id="tem"></a>

## Temperate forest communities

**Realm:** Surface land. **Climate context to support later:** Cool to warm temperate; varied seasonality.

These broad climate/canopy profiles accept named oak, maple, beech and other community variants rather than a biome for every tree species.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:cool_mixed_forest | Cool Mixed Forest | Cool mixed canopy | FOREST, LOAM |
| deepprimal:temperate_deciduous_forest | Temperate Deciduous Forest | Seasonal broadleaf canopy | FOREST, LOAM |
| deepprimal:dry_temperate_woodland | Dry Temperate Woodland | Open or drought-limited temperate trees | THIN, LOAM |
| deepprimal:temperate_conifer_forest | Temperate Conifer Forest | Temperate needleleaf-dominant canopy | FOREST, THIN |
| deepprimal:temperate_rainforest | Temperate Rainforest | Persistently moist temperate canopy | FOREST, ORGANIC |
| deepprimal:warm_temperate_evergreen_forest | Warm Temperate Evergreen Forest | Broadleaf evergreen canopy | FOREST, LOAM |
| deepprimal:temperate_savanna | Temperate Savanna | Widely spaced trees over grass/herbs | LOAM, THIN |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Cool Mixed Forest | Deciduous broadleaf and conifer pool | Normal drainage | Mixed Boreal Forest; Temperate Deciduous Forest; Montane Forest |
| Temperate Deciduous Forest | Oak/beech/maple/ash groups by province | Normal drainage | Cool Mixed Forest; Temperate Meadow; Dry Temperate Woodland |
| Dry Temperate Woodland | Drought-tolerant regional broadleaf/conifer mix | Lower moisture; local seeps remain possible | Temperate Deciduous Forest; Temperate Savanna; Dry Shrubland |
| Temperate Conifer Forest | Regional pine/fir/Douglas-fir groups | Moderate drainage | Cool Mixed Forest; Temperate Rainforest; Montane Forest |
| Temperate Rainforest | Moist-forest trees, ferns, epiphytes and deadwood | High effective moisture; separate stream corridors | Temperate Conifer Forest; Montane Cloud Forest; Forested Swamp |
| Warm Temperate Evergreen Forest | Regional evergreen broadleaf pool | Moist but normally drained | Subtropical Moist Forest; Temperate Deciduous Forest; Mediterranean Woodland |
| Temperate Savanna | Oak/pine or regional savanna pool | Seasonal dryness | Dry Temperate Woodland; Mixed-grass Prairie; Temperate Meadow |

<a id="med"></a>

## Mediterranean and dry shrublands

**Realm:** Surface land. **Climate context to support later:** Temperate/warm; seasonally dry.

Climate seasonality and canopy structure distinguish profiles; region-specific names are community variants.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:mediterranean_woodland | Mediterranean Woodland | Open evergreen woodland | THIN, LOAM |
| deepprimal:mediterranean_scrub | Mediterranean Scrub | Dense seasonal-dry woody shrubs | THIN, ROCK |
| deepprimal:rocky_scrub | Rocky Scrub | Very thin soil among exposed rock | ROCK, THIN |
| deepprimal:dry_shrubland | Dry Shrubland | Broad semiarid shrub matrix | THIN, LOAM, SAND |
| deepprimal:temperate_heathland | Temperate Heathland | Low temperate heath vegetation | ORGANIC, THIN, SAND |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Mediterranean Woodland | Evergreen oak/olive/pine-like regional groups | Dry season; locally moist gullies | Warm Temperate Evergreen Forest; Mediterranean Scrub; Dry Grassland |
| Mediterranean Scrub | Sclerophyll shrub communities | Dry season; substrate controls retention | Mediterranean Woodland; Rocky Scrub; Dry Grassland |
| Rocky Scrub | Low drought-tolerant shrubs/herbs | Rapid runoff; pocket soils | Mediterranean Scrub; Dry Shrubland; Hot Desert |
| Dry Shrubland | Province-limited shrubs; sparse grasses | Low/episodic moisture | Dry Grassland; Semidesert; Cold Shrubland |
| Temperate Heathland | Heather/heath-like regional cover | Dry-to-wet patches; local wetland rules | Dry Temperate Woodland; Temperate Meadow; Fen |

<a id="gra"></a>

## Temperate grasslands and steppe

**Realm:** Surface land. **Climate context to support later:** Cool/temperate; dry to seasonally moist.

Grass species, flowering abundance, soil chemistry and relief form variants within these profiles.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:tallgrass_prairie | Tallgrass Prairie | Dense tall temperate grasses | LOAM, ORGANIC |
| deepprimal:mixed_grass_prairie | Mixed-grass Prairie | Intermediate-height grassland | LOAM |
| deepprimal:shortgrass_prairie | Shortgrass Prairie | Low dry grass cover | LOAM, THIN |
| deepprimal:temperate_meadow | Temperate Meadow | Mesic herb/grass open habitat | LOAM, ORGANIC |
| deepprimal:dry_grassland | Dry Grassland | Sparse/short drought-limited grassland | THIN, LOAM, SAND |
| deepprimal:cold_steppe | Cold Steppe | Cold continental dry open land | THIN, LOAM |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Tallgrass Prairie | Tall grasses and flowering herbs | Seasonal moisture; wet depressions separate | Mixed-grass Prairie; Temperate Savanna; Wet Meadow |
| Mixed-grass Prairie | Mixed grass/herb pool | Moderate seasonal dryness | Tallgrass Prairie; Shortgrass Prairie; Temperate Savanna |
| Shortgrass Prairie | Short grasses and scattered herbs | Limited moisture | Mixed-grass Prairie; Dry Grassland; Semidesert |
| Temperate Meadow | Grasses, flowers, low herbs | Normally drained to seasonally moist | Temperate Deciduous Forest; Wet Meadow; Montane Meadow |
| Dry Grassland | Bunch grasses and drought herbs | Seasonal/low moisture | Shortgrass Prairie; Dry Shrubland; Semidesert |
| Cold Steppe | Cold/dry grasses and shrubs | Strong seasonality; low moisture | Dry Boreal Woodland; Cold Shrubland; Cold Desert |

<a id="sav"></a>

## Tropical grasslands and savannas

**Realm:** Surface land. **Climate context to support later:** Warm to tropical; seasonal moisture.

Tree density and water regime are persistent profile traits, while recent fire/grazing and species dominance are overlays.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:tropical_grassland | Tropical Grassland | Warm open grass/herb matrix | LOAM, THIN |
| deepprimal:open_savanna | Open Savanna | Sparse warm trees over grasses | LOAM, THIN |
| deepprimal:wooded_savanna | Wooded Savanna | More continuous warm tree/grass mosaic | LOAM, FOREST |
| deepprimal:semiarid_savanna | Semiarid Savanna | Sparse trees/shrubs and dry grasses | THIN, SAND, LOAM |
| deepprimal:seasonal_flooded_grassland | Seasonal Flooded Grassland | Warm flood-influenced open vegetation | SILT, CLAY, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Tropical Grassland | Tall/short tropical grasses by water balance | Strong wet/dry season candidate | Open Savanna; Semiarid Savanna; Seasonal Flooded Grassland |
| Open Savanna | Regional savanna trees and grasses | Seasonal dryness | Tropical Grassland; Wooded Savanna; Semiarid Savanna |
| Wooded Savanna | Regional dryland trees with grassy gaps | Seasonal moisture | Open Savanna; Tropical Dry Forest; Seasonal Tropical Forest |
| Semiarid Savanna | Thorny/drought-tolerant regional pool | Low rainfall/strong dry season | Open Savanna; Thorn Scrub; Semidesert |
| Seasonal Flooded Grassland | Flood-tolerant grasses/sedges | Recurrent inundation linked to river/lake budget | Tropical Grassland; Freshwater Marsh; Floodplain Forest |

<a id="sub"></a>

## Subtropical forest and scrub

**Realm:** Surface land. **Climate context to support later:** Warm temperate/subtropical.

Regional species pools determine whether palms, pines or evergreen broadleaf forms belong; common climate is not permission to mix every warm species.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:subtropical_moist_forest | Subtropical Moist Forest | Moist warm broadleaf forest | FOREST, LOAM |
| deepprimal:subtropical_seasonal_forest | Subtropical Seasonal Forest | Warm forest with seasonal water limitation | FOREST, LOAM |
| deepprimal:subtropical_conifer_forest | Subtropical Conifer Forest | Warm conifer-dominant forest | THIN, FOREST |
| deepprimal:subtropical_dry_woodland | Subtropical Dry Woodland | Warm open drought-adapted woodland | THIN, LOAM |
| deepprimal:thorn_scrub | Thorn Scrub | Warm dry dense thorny cover | THIN, SAND |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Subtropical Moist Forest | Regional evergreen/broadleaf community | High effective moisture | Warm Temperate Evergreen Forest; Tropical Rainforest; Montane Cloud Forest |
| Subtropical Seasonal Forest | Seasonal evergreen/deciduous mix | Seasonal wet/dry cycle | Subtropical Moist Forest; Subtropical Dry Woodland; Seasonal Tropical Forest |
| Subtropical Conifer Forest | Pine and regional conifer pool | Climate/substrate-dependent moisture | Subtropical Seasonal Forest; Montane Forest; Dry Temperate Woodland |
| Subtropical Dry Woodland | Regional dry broadleaf/woody pool | Pronounced dry season | Subtropical Seasonal Forest; Thorn Scrub; Semiarid Savanna |
| Thorn Scrub | Thorny shrubs/small trees; sparse herbs | Semiarid; episodic water | Subtropical Dry Woodland; Semiarid Savanna; Semidesert |

<a id="tro"></a>

## Tropical forests

**Realm:** Surface land. **Climate context to support later:** Tropical; wet through seasonal dry.

Unusual soils can select a community variant without making each soil or rock a new biome.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:tropical_rainforest | Tropical Rainforest | Moist multi-layer lowland canopy | FOREST, LOAM, CLAY |
| deepprimal:seasonal_tropical_forest | Seasonal Tropical Forest | Tropical canopy with seasonal response | FOREST, LOAM |
| deepprimal:tropical_dry_forest | Tropical Dry Forest | Strongly seasonal dry tropical canopy | THIN, FOREST, LOAM |
| deepprimal:tropical_montane_forest | Tropical Montane Forest | Tropical mountain forest below cloud/treeline zones | FOREST, THIN |
| deepprimal:tropical_swamp_forest | Tropical Swamp Forest | Warm persistently wet woody ecosystem | MUD, PEAT, CLAY |
| deepprimal:peat_swamp_forest | Peat Swamp Forest | Warm forest over deep organic ground | PEAT |
| deepprimal:tropical_conifer_forest | Tropical Conifer Forest | Tropical conifer-dominant canopy where regional species and climate permit | FOREST, THIN, LOAM |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Tropical Rainforest | Regional broadleaf canopy, lianas, understory | High effective moisture; drainage still matters | Seasonal Tropical Forest; Tropical Swamp Forest; Montane Cloud Forest |
| Seasonal Tropical Forest | Regional seasonal canopy and understory | Wet/dry season candidate | Tropical Rainforest; Tropical Dry Forest; Wooded Savanna |
| Tropical Dry Forest | Drought-deciduous/regional dry forest pool | Long dry season candidate | Seasonal Tropical Forest; Wooded Savanna; Thorn Scrub |
| Tropical Montane Forest | Upland regional broadleaf/conifer candidates | Orographic moisture with rain-shadow alternatives | Tropical Rainforest; Montane Cloud Forest; Tropical Alpine Grassland |
| Tropical Swamp Forest | Flood-tolerant regional trees and aquatic edges | Freshwater flooding/saturation | Tropical Rainforest; Peat Swamp Forest; Floodplain Forest |
| Peat Swamp Forest | Peatland-adapted regional woody pool | Long-lived wet peat profile | Tropical Swamp Forest; Freshwater Marsh; Seasonal Tropical Forest |
| Tropical Conifer Forest | Province-specific tropical pine/conifer representatives; broadleaf understory | Wet/dry season and substrate constraints; not inferred from latitude alone | Seasonal Tropical Forest; Wooded Savanna; Tropical Montane Forest |

<a id="ard"></a>

## Hot arid and semiarid environments

**Realm:** Surface land. **Climate context to support later:** Warm/hot; low effective moisture.

Sand, gravel and rock are substrate/landform overlays. The base ecosystem profile follows water availability and vegetation, preventing rock-color biome multiplication.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:hot_desert | Hot Desert | Very dry warm inland/coastal land | SAND, ROCK, GRAVEL |
| deepprimal:hyperarid_desert | Hyperarid Desert | Extremely sparse warm desert ecosystem | ROCK, GRAVEL, SAND |
| deepprimal:semidesert | Semidesert | Desert/grassland/shrubland transition | THIN, SAND, LOAM |
| deepprimal:succulent_scrub | Succulent Scrub | Drought vegetation with succulent structural dominance | ROCK, THIN, SAND |
| deepprimal:fog_desert | Fog Desert | Arid coast with persistent fog influence | ROCK, SAND, THIN |
| deepprimal:salt_basin | Salt Basin | Endorheic saline basin and exposed margins | SALT, CLAY, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Hot Desert | Sparse province-appropriate drought cover | Episodic runoff; localized groundwater | Semidesert; Hyperarid Desert; Salt Basin |
| Hyperarid Desert | Very limited plant cover; selected crusts | Very rare/episodic water | Hot Desert; Fog Desert; Salt Basin |
| Semidesert | Sparse grasses, shrubs and succulents by province | Episodic/seasonal moisture | Hot Desert; Dry Grassland; Semiarid Savanna |
| Succulent Scrub | Cacti/agave/aloe-like representatives by province | Low moisture; local collection surfaces | Semidesert; Hot Desert; Thorn Scrub |
| Fog Desert | Fog-associated cryptogam/shrub candidates | Fog exposure; no automatic freshwater guarantee | Hyperarid Desert; Coastal Scrub; Rocky Shore |
| Salt Basin | Salt-tolerant edge cover; bare interior possible | Closed basin budget; saline/brine pools | Hot Desert; Cold Desert; Saline Lake |

<a id="cold"></a>

## Cold arid and alpine dry environments

**Realm:** Surface land. **Climate context to support later:** Cold continental/montane; low effective moisture.

Coldness and aridity are independent: avoid treating every snowy place as moist.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:cold_desert | Cold Desert | Cold dry exposed land | ROCK, GRAVEL, SAND |
| deepprimal:cold_shrubland | Cold Shrubland | Cold dry low woody cover | THIN, LOAM |
| deepprimal:alpine_dry_steppe | Alpine Dry Steppe | High cold dry plateau vegetation | THIN, LOAM, ROCK |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Cold Desert | Sparse cold/dry shrubs/herbs | Low precipitation; seasonal melt | Polar Desert; Cold Steppe; Cold Shrubland |
| Cold Shrubland | Province-limited sagebrush/other shrubs | Low/seasonal moisture | Cold Desert; Cold Steppe; Dry Boreal Woodland |
| Alpine Dry Steppe | Low grasses/cushion herbs | Dry rain-shadow setting | Cold Steppe; Alpine Meadow; Alpine Barrens |

<a id="mnt"></a>

## Montane and alpine ecosystems

**Realm:** Surface land. **Climate context to support later:** Elevation modifies regional climate; no fixed global Y threshold.

Relief does not independently decide biome. A mountain can carry forest, desert, grassland or tundra according to shared climate and water fields.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:montane_forest | Montane Forest | Temperate/cool mountain forest | FOREST, THIN, ROCK |
| deepprimal:montane_cloud_forest | Montane Cloud Forest | Persistent cloud-exposed mountain canopy | ORGANIC, FOREST, THIN |
| deepprimal:montane_meadow | Montane Meadow | Mountain open grass/herb vegetation | LOAM, THIN, ORGANIC |
| deepprimal:subalpine_woodland | Subalpine Woodland | Sparse trees near climatic treeline | THIN, ORGANIC |
| deepprimal:alpine_meadow | Alpine Meadow | Treeless high-elevation herbs/grasses | THIN, LOAM, ORGANIC |
| deepprimal:alpine_heath | Alpine Heath | Treeless mountain dwarf-shrub cover | THIN, ORGANIC |
| deepprimal:alpine_barrens | Alpine Barrens | Sparse plants among high exposed rock | ROCK, THIN, ICE |
| deepprimal:tropical_alpine_grassland | Tropical Alpine Grassland | Tropical high-elevation treeless vegetation | THIN, ORGANIC, LOAM |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Montane Forest | Regional elevation-appropriate tree pool | Orographic moisture/rain-shadow constraints | Temperate Conifer Forest; Cool Mixed Forest; Subalpine Woodland |
| Montane Cloud Forest | Moist-forest trees, epiphytes and ferns | Cloud/fog moisture and drainage | Tropical Montane Forest; Temperate Rainforest; Subalpine Woodland |
| Montane Meadow | Grasses/flowers selected for elevation | Normal drainage with separate wet hollows | Montane Forest; Alpine Meadow; Wet Meadow |
| Subalpine Woodland | Stunted/elevation-adapted regional trees | Cold season; slope moisture varies | Montane Forest; Alpine Meadow; Alpine Heath |
| Alpine Meadow | Low herbs, grasses, seasonal flowers | Melt/precipitation; drained soil patches | Montane Meadow; Alpine Heath; Alpine Barrens |
| Alpine Heath | Dwarf shrubs, lichens and mosses | Exposure and patchy moisture | Subalpine Woodland; Alpine Meadow; Alpine Barrens |
| Alpine Barrens | Cryptogams and scattered cushion plants | Meltwater localized; exposed dry surfaces | Alpine Meadow; Rock Tundra; Icefield |
| Tropical Alpine Grassland | Region-specific tussocks/rosettes/cushion forms | Cold highland moisture; strong daily exposure | Tropical Montane Forest; Alpine Barrens; Montane Cloud Forest |

<a id="riv"></a>

## Flowing freshwater systems

**Realm:** Freshwater and riparian. **Climate context to support later:** Inherit catchment climate; flow, substrate and connectivity are additional constraints.

The drainage graph chooses channels, joins, reaches and outlets before these habitat profiles. Rapids, waterfalls and bars are forms within a connected river.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:headwater_stream | Headwater Stream | Upper-catchment small channel | ROCK, GRAVEL |
| deepprimal:mountain_river | Mountain River | Confined higher-gradient river | ROCK, GRAVEL, SAND |
| deepprimal:foothill_river | Foothill River | Transition from steep reaches to valley channels | GRAVEL, SAND, SILT |
| deepprimal:lowland_river | Lowland River | Broad lower-gradient flowing freshwater | SAND, SILT, CLAY |
| deepprimal:seasonal_river | Seasonal River | Intermittent river corridor | GRAVEL, SAND, SILT |
| deepprimal:spring | Spring | Groundwater emergence and short source reach | ROCK, GRAVEL, MUD |
| deepprimal:floodplain_backwater | Floodplain Backwater | Slow side water connected to river floods | MUD, SILT, ORGANIC |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Headwater Stream | Sparse aquatic films; regional bank cover | Connected source/seasonal discharge | Mountain River; Spring; Montane Forest |
| Mountain River | Current-adapted aquatic cover; riparian pockets | Routed discharge and explicit cascades | Headwater Stream; Foothill River; Montane Forest |
| Foothill River | Riparian strips and bar vegetation | Connected confluences; variable width | Mountain River; Lowland River; Floodplain Forest |
| Lowland River | Regional banks, submerged/emergent cover | Unified river/lake/outlet budget | Foothill River; Floodplain Backwater; Estuary |
| Seasonal River | Seasonally exposed channel/bank cover | Surface flow may cease; aquifer link separate | Semidesert; Seasonal Pond; Lowland River |
| Spring | Local moisture-dependent source vegetation | Aquifer discharge/quality controls | Headwater Stream; Fen; Groundwater Cave |
| Floodplain Backwater | Emergent/floating vegetation; regional banks | Seasonal connection and exchange | Lowland River; Freshwater Marsh; Floodplain Forest |

<a id="lak"></a>

## Lakes, ponds and inland saline waters

**Realm:** Freshwater or inland saline water. **Climate context to support later:** Water thermal regime follows regional climate and depth.

Shore vegetation and lakebed are vertically related to the water body; do not assign an entire lake one land-cover texture.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:cold_lake | Cold Lake | Cold inland standing water | GRAVEL, SAND, MUD |
| deepprimal:temperate_lake | Temperate Lake | Temperate inland standing water | SAND, SILT, MUD |
| deepprimal:warm_lake | Warm Lake | Warm/subtropical/tropical inland water | SILT, CLAY, MUD |
| deepprimal:glacial_lake | Glacial Lake | Lake receiving ice-margin melt | ROCK, GRAVEL, SILT |
| deepprimal:seasonal_pond | Seasonal Pond | Temporary small inland water body | CLAY, SILT, MUD |
| deepprimal:tundra_pond | Tundra Pond | Shallow cold tundra pool mosaic | PEAT, MUD, FROZEN |
| deepprimal:saline_lake | Saline Lake | Closed/partly closed inland saline water | SALT, CLAY, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Cold Lake | Cold-region shore/aquatic representatives | Inflow/outflow/groundwater; freeze regime later | Glacial Lake; Headwater Stream; Boreal Bog |
| Temperate Lake | Regional submerged/floating plants and shore belt | Water budget; seasonal water/temperature | Lowland River; Freshwater Marsh; Temperate Meadow |
| Warm Lake | Warm aquatic and shore vegetation | Water budget; seasonal levels | Tropical Swamp Forest; Seasonal Flooded Grassland |
| Glacial Lake | Sparse cold shore cover; sediment conditions matter | Meltwater/input and connected spill level | Icefield; Cold Lake; Mountain River |
| Seasonal Pond | Seasonal emergent/short-lived wetland cover | Hydroperiod explicitly finite | Wet Meadow; Seasonal River; Semidesert |
| Tundra Pond | Sedges/mosses and selected aquatic cover | Thaw/melt and wet-ground exchange | Wet Tundra; Fen; Cold Lake |
| Saline Lake | Salinity-compatible life; sparse edge cover | Salt and water budget; no freshwater assumption | Salt Basin; Semidesert; Cold Desert |

<a id="wet"></a>

## Freshwater wetlands and wet woodland

**Realm:** Freshwater–terrestrial transition. **Climate context to support later:** Climate-specific representatives; water regime is decisive.

Marsh, swamp, bog and fen are distinct planning profiles. Organic ground, open water and vegetation remain separate layers.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:freshwater_marsh | Freshwater Marsh | Nonwoody emergent wetland | MUD, SILT, CLAY |
| deepprimal:wet_meadow | Wet Meadow | Seasonally saturated grass/herb ground | LOAM, SILT, ORGANIC |
| deepprimal:forested_swamp | Forested Swamp | Cool/temperate saturated woodland | MUD, ORGANIC, CLAY |
| deepprimal:floodplain_forest | Floodplain Forest | Forest tied to river floodplain | SILT, CLAY, FOREST |
| deepprimal:temperate_bog | Temperate Bog | Temperate rain-influenced peatland | PEAT |
| deepprimal:fen | Fen | Groundwater/mineral-water-influenced peat wetland | PEAT, ORGANIC, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Freshwater Marsh | Reeds, rushes, sedges and open-water patches | Shallow water/inundation | Temperate Lake; Lowland River; Wet Meadow |
| Wet Meadow | Wet grasses, sedges, herbs | Saturation without persistent deep open water | Temperate Meadow; Freshwater Marsh; Fen |
| Forested Swamp | Regional wet-tolerant woody species | Freshwater saturation/flooding | Floodplain Forest; Freshwater Marsh; Temperate Rainforest |
| Floodplain Forest | Flood-tolerant trees by province | Periodic river flooding/sediment input | Lowland River; Forested Swamp; Tropical Swamp Forest |
| Temperate Bog | Sphagnum, heath and sparse woody cover | Peatland budget; limited mineral-water input candidate | Boreal Bog; Fen; Temperate Heathland |
| Fen | Sedges, rushes, mosses by chemistry | Groundwater/surface-water supply | Wet Meadow; Freshwater Marsh; Temperate Bog |

<a id="coa"></a>

## Coasts and estuaries

**Realm:** Marine–freshwater–terrestrial transition. **Climate context to support later:** Ocean climate, exposure, substrate and salinity constrain placement.

Shore geometry, cliffs, sand color and rock composition are landform/material layers. Tidal behavior is a separately scoped water process.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:sandy_shore | Sandy Shore | Sediment-dominated sandy coastline | SAND |
| deepprimal:shingle_shore | Shingle Shore | Coarse-clast coastline | GRAVEL, ROCK |
| deepprimal:rocky_shore | Rocky Shore | Exposed rock coast and intertidal ledges | ROCK, THIN |
| deepprimal:coastal_dune_grassland | Coastal Dune Grassland | Vegetated dune belt | SAND, THIN |
| deepprimal:coastal_scrub | Coastal Scrub | Salt/wind-exposed low woody coast | THIN, SAND, ROCK |
| deepprimal:estuary | Estuary | Connected river/ocean mixing water | MUD, SILT, SAND |
| deepprimal:tidal_mudflat | Tidal Mudflat | Fine-sediment exposed/submerged coast | MUD, SILT, CLAY |
| deepprimal:salt_marsh | Salt Marsh | Saline herbaceous coastal wetland | ORGANIC, MUD, PEAT |
| deepprimal:mangrove_forest | Mangrove Forest | Warm saline/tidal woody wetland | MUD, ORGANIC, PEAT |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Sandy Shore | Sparse beach/dune vegetation | Marine exposure; shore water budget | Coastal Dune Grassland; Estuary; Marine Shelf |
| Shingle Shore | Sparse strand vegetation | Marine exposure and wave sorting | Rocky Shore; Sandy Shore; Marine Shelf |
| Rocky Shore | Cryptogams above water; marine attached cover below | Marine exposure; pools are microhabitats | Coastal Scrub; Kelp Forest; Marine Shelf |
| Coastal Dune Grassland | Dune grasses and region-specific herbs | Wind/salt exposure; freshwater lens separate | Sandy Shore; Coastal Scrub; Salt Marsh |
| Coastal Scrub | Coastal shrub communities by province | Spray/exposure; drainage varies | Rocky Shore; Coastal Dune Grassland; Fog Desert |
| Estuary | Salinity-zoned aquatic/marginal vegetation | Freshwater discharge plus marine exchange | Lowland River; Tidal Mudflat; Salt Marsh |
| Tidal Mudflat | Biofilms and burrowing-fauna hooks | Tidal exposure and sediment budget | Estuary; Salt Marsh; Mangrove Forest |
| Salt Marsh | Salt-tolerant grasses/rushes | Inundation/salinity gradients | Estuary; Tidal Mudflat; Coastal Dune Grassland |
| Mangrove Forest | Region-specific mangrove/root forms | Warm coast and brackish/saline inundation | Estuary; Tidal Mudflat; Seagrass Meadow |

<a id="mar"></a>

## Shallow marine habitats

**Realm:** Marine. **Climate context to support later:** Water temperature, light/depth, salinity and seabed determine profiles.

These are benthic/nearshore game-biome candidates. Water-column properties remain separate and can vary above the seabed.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:marine_shelf | Marine Shelf | General shallow coastal sea floor | SAND, SILT, GRAVEL, ROCK |
| deepprimal:seagrass_meadow | Seagrass Meadow | Light-accessible rooted marine vegetation | SAND, MUD |
| deepprimal:kelp_forest | Kelp Forest | Marine macroalgal canopy habitat | ROCK |
| deepprimal:seaweed_garden | Seaweed Garden | Non-kelp shallow algal cover | ROCK, GRAVEL |
| deepprimal:coral_reef | Coral Reef | Warm shallow framework-building habitat | CARBONATE, SAND |
| deepprimal:shellfish_bed | Shellfish Bed | Dense biogenic shell habitat | SHELL, MUD, GRAVEL |
| deepprimal:cold_marine_shelf | Cold Marine Shelf | Cold shallow sea floor | ROCK, GRAVEL, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Marine Shelf | Province/depth-appropriate marine communities | Ocean-connected saline water | Sandy Shore; Rocky Shore; Open Ocean |
| Seagrass Meadow | Regional seagrasses and associated fauna hooks | Compatible salinity, light and sheltered substrate | Marine Shelf; Mangrove Forest; Coral Reef |
| Kelp Forest | Regional kelps and attached organisms | Suitable cool/coastal nutrient/light regime | Rocky Shore; Marine Shelf; Cold Ocean |
| Seaweed Garden | Green/red/brown algae by province | Light and exposure-compatible marine water | Rocky Shore; Marine Shelf; Kelp Forest |
| Coral Reef | Selected reef-building corals/algae; fauna hooks | Light/temperature/sediment limits | Seagrass Meadow; Marine Shelf; Open Ocean |
| Shellfish Bed | Oyster/mussel-like regional communities | Food/flow/salinity-compatible water | Estuary; Marine Shelf; Tidal Mudflat |
| Cold Marine Shelf | Cold-region benthic pool | Cold ocean-connected water; ice seasonal state | Cold Ocean; Rocky Shore; Kelp Forest |

<a id="oce"></a>

## Open ocean and deep marine

**Realm:** Marine. **Climate context to support later:** Regional water mass plus depth/light, seabed and connection.

Cold/warm open ocean are water-column candidates; deep seabed/vent profiles are vertical habitats below them. Overlap is represented in 3D, not as mutually exclusive whole columns.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:cold_ocean | Cold Ocean | Cold open-water region | WATER |
| deepprimal:open_ocean | Open Ocean | General temperate open water | WATER |
| deepprimal:warm_ocean | Warm Ocean | Warm open-water region | WATER |
| deepprimal:deep_ocean_floor | Deep Ocean Floor | Low-light/deep seabed | MUD, ROCK |
| deepprimal:submarine_canyon | Submarine Canyon | Deep incised seabed corridor | ROCK, MUD, GRAVEL |
| deepprimal:hydrothermal_vent_field | Hydrothermal Vent Field | Local deep-sea geothermal habitat | ROCK, MINERAL |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Cold Ocean | Cold-water pelagic fauna/plankton hooks | Ocean-connected water mass | Cold Marine Shelf; Open Ocean; Deep Ocean Floor |
| Open Ocean | Regional pelagic fauna/plankton hooks | Ocean-connected water mass | Cold Ocean; Warm Ocean; Marine Shelf |
| Warm Ocean | Warm-water pelagic fauna/plankton hooks | Ocean-connected water mass | Open Ocean; Coral Reef; Deep Ocean Floor |
| Deep Ocean Floor | Deep benthic fauna/microbial hooks | Marine depth and sediment setting | Marine Shelf; Submarine Canyon; Hydrothermal Vent Field |
| Submarine Canyon | Depth/flow-dependent benthic communities | Shared bathymetry/drainage context | Marine Shelf; Deep Ocean Floor; Hydrothermal Vent Field |
| Hydrothermal Vent Field | Specialized microbial/faunal candidate pools | Geological vent and marine-depth eligibility | Deep Ocean Floor; Submarine Canyon |

<a id="cav"></a>

## Underground habitats

**Realm:** Subterranean and subterranean freshwater. **Climate context to support later:** Depth, connection, light, geology, moisture and heat are independent of the surface biome.

Cave shape alone is a feature. Broad cave habitat profiles may merit IDs; guano, crystals, speleothems and mossy entrances can remain nested habitats/features.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:dry_cave | Dry Cave | Air-filled dry subterranean space | ROCK, GRAVEL |
| deepprimal:wet_cave | Wet Cave | Air-filled damp/seeping subterranean space | ROCK, MUD |
| deepprimal:groundwater_cave | Groundwater Cave | Flooded or water-table-linked cave habitat | ROCK, MUD, GRAVEL |
| deepprimal:underground_river | Underground River | Connected flowing subterranean watercourse | ROCK, GRAVEL, SAND |
| deepprimal:cave_entrance | Cave Entrance | Daylight–darkness cave transition | ROCK, THIN, ORGANIC |
| deepprimal:lava_tube | Lava Tube | Tube-shaped volcanic cave habitat | ROCK, GRAVEL |
| deepprimal:ice_cave | Ice Cave | Persistent cold cave ice habitat | ICE, ROCK |
| deepprimal:geothermal_cave | Geothermal Cave | Subterranean heat/vent-influenced habitat | ROCK, MINERAL, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Dry Cave | Sparse organic input; no sunlight-dependent vegetation | Low seepage; local pockets possible | Wet Cave; Cave Entrance; Lava Tube |
| Wet Cave | Moisture-compatible fungi/microbes; limited imported organics | Seepage/pools; no universal purity | Dry Cave; Groundwater Cave; Underground River |
| Groundwater Cave | Selected subterranean aquatic communities | Aquifer level/flow and chemistry | Wet Cave; Underground River; Spring |
| Underground River | Low-light aquatic community hooks | Shared surface/groundwater network | Groundwater Cave; Wet Cave; Headwater Stream |
| Cave Entrance | Limited mosses/ferns where light permits; regional litter | Surface climate/flow connection | Dry Cave; Wet Cave; Montane Forest |
| Lava Tube | Sparse organics; colonization depends on connection | Cooling history; dry or wet subhabitats | Dry Cave; Volcanic Barrens; Geothermal Cave |
| Ice Cave | Sparse cold subterranean communities | Cold trapping, freeze/melt inputs | Wet Cave; Alpine Barrens; Icefield |
| Geothermal Cave | Specialized microbial candidate communities | Geological heat/gas/water inputs | Wet Cave; Lava Tube; Geothermal Springs |

<a id="geo"></a>

## Geothermal and volcanic surface habitats

**Realm:** Surface land and geothermal freshwater. **Climate context to support later:** Geological activity/history modifies otherwise regional climate.

Rock type and fresh lava morphology remain substrate/features. These persistent habitat extremes may justify specialized profiles; runtime eruption simulation is not implied.

| Proposed key | Biome name | Physical setting | Ground themes |
| --- | --- | --- | --- |
| deepprimal:volcanic_barrens | Volcanic Barrens | Young poorly developed volcanic ground | ROCK, ASH, GRAVEL |
| deepprimal:geothermal_springs | Geothermal Springs | Heated spring/pool/vent complex | ROCK, MINERAL, MUD |

| Biome name | Vegetation/life candidates | Water regime | Plausible neighboring profiles |
| --- | --- | --- | --- |
| Volcanic Barrens | Sparse pioneer/cryptogam cover; succession later | Regional precipitation with local drainage | Rocky Scrub; Alpine Barrens; Geothermal Springs |
| Geothermal Springs | Temperature/chemistry-zoned microbial/edge vegetation candidates | Geological heat and source-water budget | Spring; Volcanic Barrens; Geothermal Cave |

## Common obligations for every profile

Each candidate inherits the per-biome planning record, transition/placement requirements, resource/progression hooks, material/rendering direction, performance constraints, and validation gates in [BIOME-PLAN.md](BIOME-PLAN.md). The named variants, local layers and habitat coverage crosswalk are in [BIOME-VARIANTS-AND-COVERAGE.md](BIOME-VARIANTS-AND-COVERAGE.md).

Names and qualitative vegetation/water examples are our design proposals. Exact tolerances and scientific distributions require later source-backed property sheets. No numerical balance value, full flora/fauna species inventory, migration behavior, or implementation status is implied by a row.
