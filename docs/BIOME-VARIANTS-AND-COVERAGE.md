# Deep Primal — biome variants, layers and habitat coverage

Planning draft: 2026-10-21. **No implementation change.**

[Biome plan](BIOME-PLAN.md) · [118 named profiles](BIOME-CATALOG.md) · [Existing natural type catalog](NATURAL-TYPE-CATALOG.md)

These are proposed representations of regional communities, local forms and states. They do not automatically consume biome IDs and their counts must not be added to the base-profile registry count.

## Regional community variants

A variant changes the compatible species/community expression of a parent profile. It must satisfy province, climate, soil, hydrology and light constraints. It can be represented as data rather than as another registered biome. Scientific names, exact species pools and tolerances require later review.

| Variant ID | Named variant | Eligible parent profiles | Relationship / remaining decision |
| --- | --- | --- | --- |
| VAR-001 | Spruce–fir taiga | Closed Boreal Forest | Boreal spruce/fir pool; province selects actual species |
| VAR-002 | Pine taiga | Closed Boreal Forest; Dry Boreal Woodland | Cold pine community within drainage/climate limits |
| VAR-003 | Larch woodland | Open Boreal Woodland; Mixed Boreal Forest | Deciduous conifer community; regional species selection |
| VAR-004 | Birch–aspen boreal mix | Mixed Boreal Forest | Cold deciduous/conifer mosaic |
| VAR-005 | Lichen woodland | Open Boreal Woodland; Dry Boreal Woodland | Sparse canopy with lichen-dominated ground |
| VAR-006 | Oak–beech forest | Temperate Deciduous Forest | Regional mixed broadleaf community |
| VAR-007 | Maple–linden forest | Temperate Deciduous Forest; Cool Mixed Forest | Regional mixed broadleaf community |
| VAR-008 | Oak–hickory forest | Temperate Deciduous Forest; Dry Temperate Woodland | Regional warm/seasonal broadleaf community |
| VAR-009 | Beech–maple forest | Temperate Deciduous Forest; Cool Mixed Forest | Regional mixed broadleaf community |
| VAR-010 | Chestnut–oak woodland | Temperate Deciduous Forest; Dry Temperate Woodland | Regional mixed broadleaf community |
| VAR-011 | Birch–aspen temperate woodland | Cool Mixed Forest | Pioneer/region-specific community; disturbance can also be an overlay |
| VAR-012 | Douglas-fir–hemlock forest | Temperate Conifer Forest; Temperate Rainforest | Regional moist-conifer community |
| VAR-013 | Coast-redwood forest | Temperate Rainforest | Regional canopy form; not every wet coast |
| VAR-014 | Cedar–hemlock forest | Temperate Rainforest | Resolve true cedar versus Thuja naming in species pool |
| VAR-015 | Southern-beech forest | Temperate Rainforest; Montane Forest | Southern biological-province candidate; species pool needs expansion/research |
| VAR-016 | Araucaria woodland | Subtropical Conifer Forest; Montane Forest | Southern conifer community |
| VAR-017 | Podocarp forest | Subtropical Moist Forest; Tropical Montane Forest | Regional upland/moist conifer community |
| VAR-018 | Kauri forest | Subtropical Moist Forest | Regional moist-forest community |
| VAR-019 | Cork-oak woodland | Mediterranean Woodland | Cork-oak species/material community |
| VAR-020 | Olive–evergreen scrub | Mediterranean Woodland; Mediterranean Scrub | Regional dry-summer community |
| VAR-021 | Stone-pine woodland | Mediterranean Woodland | Regional conifer woodland |
| VAR-022 | Chaparral-type scrub | Mediterranean Scrub | Regional vegetation-form candidate, not a global species mix |
| VAR-023 | Heath-rich scrub | Temperate Heathland; Mediterranean Scrub | Region-specific heath/shrub community |
| VAR-024 | Sagebrush steppe | Cold Shrubland; Cold Steppe | Regional cold/dry shrub community |
| VAR-025 | Saltbush scrub | Dry Shrubland; Salt Basin | Salt-tolerant community only where actual substrate chemistry fits |
| VAR-026 | Spinifex-type grassland | Tropical Grassland; Semidesert | Resolve inland Triodia versus coastal Spinifex naming |
| VAR-027 | Feather-grass steppe | Cold Steppe; Dry Grassland | Regional grass community |
| VAR-028 | Tall flowering prairie | Tallgrass Prairie; Temperate Meadow | Community/seasonal flower expression within parent ecology |
| VAR-029 | Acacia-group savanna | Open Savanna; Wooded Savanna; Semiarid Savanna | Resolve Vachellia/Senegalia/Acacia by biological province |
| VAR-030 | Baobab savanna | Open Savanna; Wooded Savanna | Regional tree community; not every tropical savanna |
| VAR-031 | Mesquite woodland | Semiarid Savanna; Subtropical Dry Woodland | Regional dry woody community; accepted genus mapping later |
| VAR-032 | Eucalyptus woodland | Subtropical Dry Woodland; Dry Temperate Woodland; Wooded Savanna | Province and species tolerance control placement |
| VAR-033 | Teak seasonal forest | Seasonal Tropical Forest; Tropical Dry Forest | Regional seasonal-canopy community |
| VAR-034 | Dipterocarp lowland forest | Tropical Rainforest; Seasonal Tropical Forest | Regional canopy community; no global tropical scatter |
| VAR-035 | Bamboo thicket | Subtropical Moist Forest; Subtropical Seasonal Forest; Tropical Rainforest | Patch/regional vegetation community; not new rock/soil identities |
| VAR-036 | Palm grove | Subtropical Moist Forest; Tropical Swamp Forest | Species and water regime select palms; not arbitrary tidal coconut generation |
| VAR-037 | Dwarf-willow tundra | Shrub Tundra | Regional low-shrub community |
| VAR-038 | Lichen tundra | Dry Tundra; Rock Tundra | Regional cryptogam-dominated community |
| VAR-039 | Sedge tundra | Tussock Tundra; Wet Tundra | Wet/cold ground-cover community |
| VAR-040 | Sphagnum bog | Boreal Bog; Temperate Bog | Peatland vegetation/organic profile expression |
| VAR-041 | Sedge fen | Fen | Groundwater-compatible wetland community |
| VAR-042 | Willow–alder riparian woodland | Floodplain Forest; Forested Swamp | Climate/chemistry-compatible woody corridor |
| VAR-043 | Cypress swamp | Forested Swamp; Tropical Swamp Forest | Regional wet-tree community, not every swamp |
| VAR-044 | Low mangrove zone | Mangrove Forest | Inundation/salinity and province select species; measured thresholds later |
| VAR-045 | Upper mangrove zone | Mangrove Forest | Landward/inundation-gradient community |
| VAR-046 | Alpine tussock community | Alpine Meadow; Tropical Alpine Grassland | Region-specific tussock forms and ground profile |
| VAR-047 | Alpine cushion community | Alpine Meadow; Alpine Barrens | Sparse low alpine plant forms |
| VAR-048 | Seagrass regional community | Seagrass Meadow | Region-specific marine flowering plants |
| VAR-049 | Kelp regional community | Kelp Forest | Region-specific kelps selected by actual marine conditions |
| VAR-050 | Reef community mosaic | Coral Reef | Colony forms, rubble and algal patches; no random coral at every depth |
| VAR-051 | Tropical pine forest | Tropical Conifer Forest | Regional tropical-conifer community; species and fire/moisture conditions require later research |

## Environmental layers

Layers can combine only when their physical/biological prerequisites hold. Generic “compatible” eligibility is a planning requirement to specify, not permission to apply a layer everywhere. Local habitats keep source/material identity and cannot regenerate through player builds.

| Layer ID | Named layer | Kind | Required source/rule | Context examples |
| --- | --- | --- | --- | --- |
| LAY-001 | Sand dunes | Landform | Wind transport and sediment supply | Hot Desert; Coastal Dune Grassland; Cold Desert |
| LAY-002 | Gravel pavement | Surface/substrate | Deflation/exposure and coarse residual material | Hot Desert; Cold Desert; Polar Desert |
| LAY-003 | Rock outcrop | Substrate/form | Geology, slope and erosion | Any compatible land profile |
| LAY-004 | Badland gullies | Landform | Erodible ground, drainage and sparse cover | Semidesert; Hot Desert; Dry Grassland |
| LAY-005 | Mesa or plateau | Relief | Coherent geology and terrain history | Any compatible land profile |
| LAY-006 | Folded ridge | Relief/geology | Regional structure and erosion | Any compatible land profile |
| LAY-007 | Karst surface | Geology/landform | Soluble-rock and water history | Compatible land/wetland profile |
| LAY-008 | Limestone cave geometry | Subterranean landform | Host rock and dissolution history | Dry Cave; Wet Cave; Groundwater Cave |
| LAY-009 | Basalt columns | Rock form | Appropriate cooling/host-rock history | Volcanic Barrens; Rocky Shore |
| LAY-010 | Lava surface forms | Rock form | Composition/cooling history | Volcanic Barrens |
| LAY-011 | Volcanic ash mantle | Deposit | Eruption history and deposition | Compatible land profile |
| LAY-012 | Scree or talus | Slope deposit | Rockfall/slope and host rock | Alpine Barrens; Rocky Scrub; Rock Tundra |
| LAY-013 | Alluvial fan | Depositional landform | Valley mouth, sediment and discharge history | Dry Grassland; Semidesert; Seasonal River |
| LAY-014 | River gorge | Fluvial relief | Connected river incision and geology | Mountain River; Foothill River |
| LAY-015 | Waterfall or cascade | River-reach form | Routed reach drop with supported bed/banks | Mountain River; Headwater Stream |
| LAY-016 | Riffle and pool | River-reach forms | Discharge, slope and substrate | Headwater Stream; Foothill River; Lowland River |
| LAY-017 | River bar | Sediment landform | Shared erosion/deposition summary | Foothill River; Lowland River |
| LAY-018 | Oxbow | Water-body relationship | River abandonment/cutoff history | Floodplain Backwater; Temperate Lake |
| LAY-019 | Delta distributaries | River-mouth landform | Sediment/water/outlet graph | Lowland River; Estuary; Freshwater Marsh |
| LAY-020 | Lake shore belt | Habitat boundary | Lake level, wave exposure and substrate | Cold Lake; Temperate Lake; Warm Lake; Glacial Lake |
| LAY-021 | Spring seep hollow | Local wet habitat | Actual groundwater emergence | Spring; Fen; Wet Meadow |
| LAY-022 | Oasis | Local habitat complex | Sustained accessible water in arid matrix | Spring; Warm Lake; Freshwater Marsh within arid surroundings |
| LAY-023 | Salt crust | Chemistry/surface state | Salinity and evaporation history | Salt Basin; Saline Lake |
| LAY-024 | Peat hummock and hollow | Wetland microrelief | Peatland water regime and vegetation | Boreal Bog; Temperate Bog; Fen; Peat Swamp Forest |
| LAY-025 | Mangrove root channels | Vegetation/water form | Species growth and inundation geometry | Mangrove Forest |
| LAY-026 | Tide pool | Coastal microhabitat | Rock geometry and marine exchange | Rocky Shore |
| LAY-027 | Coastal cliff | Landform | Relief, geology and wave erosion | Rocky Shore; Coastal Scrub |
| LAY-028 | Coastal barrier and lagoon | Landform/water complex | Sediment, shore exposure and ocean connection | Sandy Shore; Estuary; Marine Shelf |
| LAY-029 | Shore wrack line | Detritus deposit | Strand level, source vegetation and marine transport | Sandy Shore; Shingle Shore; Rocky Shore |
| LAY-030 | Shell or coral rubble patch | Biogenic deposit | Local community and transport | Shellfish Bed; Coral Reef; Marine Shelf |
| LAY-031 | Seagrass gap | Community mosaic | Light, substrate and disturbance | Seagrass Meadow |
| LAY-032 | Underwater rock wall | Substrate/relief | Bathymetry and host geology | Marine Shelf; Deep Ocean Floor; Submarine Canyon |
| LAY-033 | Cave speleothems | Geological feature | Geology, seepage and deposition | Wet Cave; Cave Entrance |
| LAY-034 | Cave crystals or geodes | Mineral feature | Actual host/deposit eligibility | Dry Cave; Wet Cave |
| LAY-035 | Guano chamber | Animal-input microhabitat | Real nest/roost input and decomposition | Dry Cave; Wet Cave; Cave Entrance |
| LAY-036 | Mossy cave entrance | Light/vegetation zone | Daylight, moisture and substrate | Cave Entrance |
| LAY-037 | Deep dark cave zone | Light/depth habitat | Geometry and surface connection | Dry Cave; Wet Cave |
| LAY-038 | Geothermal mineral terrace | Deposit/landform | Source chemistry, heat and deposition | Geothermal Springs |
| LAY-039 | Snow cover | Seasonal state | Weather, season, temperature and exposure | Any compatible exposed profile |
| LAY-040 | Permanent snow patch | Persistent local climate state | Long-term accumulation/melt balance | Icefield; Alpine Barrens; Rock Tundra |
| LAY-041 | Frozen lake or river surface | Seasonal phase state | Water thermal regime and flow | Cold Lake; Temperate Lake; Lowland River; Tundra Pond |
| LAY-042 | Sea-ice cover | Seasonal/persistent phase state | Marine climate and water state | Cold Ocean; Cold Marine Shelf |
| LAY-043 | Wet ground tint | Material state | Moisture in source material | Any compatible soil/organic surface |
| LAY-044 | Dry-season vegetation | Seasonal state | Calendar and plant traits | Compatible seasonal land profile |
| LAY-045 | Autumn canopy colors | Seasonal state | Species traits and calendar | Compatible deciduous/mixed forest |
| LAY-046 | Flowering flush | Seasonal/community state | Species, water and calendar | Compatible meadow/forest/grassland profile |
| LAY-047 | Burn scar | Disturbance state | Fire history and substrate | Compatible land profile |
| LAY-048 | Windthrow gap | Disturbance state | Canopy damage/history | Compatible forest/woodland profile |
| LAY-049 | Landslide scar | Disturbance landform | Slope failure/history | Compatible land profile |
| LAY-050 | Regrowing clearing | Successional state | Disturbance history and recovery conditions | Compatible forest/woodland/grassland profile |
| LAY-051 | Forest edge | Transition habitat | Neighboring canopy/open profile and light | Compatible forest–open-land boundary |
| LAY-052 | Treeline band | Climate/vegetation transition | Derived regional climate/elevation/exposure | Forest Tundra; Subalpine Woodland and adjacent open profile |
| LAY-053 | Rain-shadow modifier | Climate field | Terrain and prevailing moisture transport | Any compatible region |
| LAY-054 | Biological soil crust | Living surface community | Climate, substrate and disturbance | Compatible dryland profile |
| LAY-055 | Rock and soil palette | Material appearance | Geology, composition and organic fraction | Any compatible material-bearing profile |

## Existing habitat crosswalk

Every one of the **62 existing HABITAT-TYPE entries** in [NATURAL-TYPES-ENVIRONMENTS.md](NATURAL-TYPES-ENVIRONMENTS.md#habitat-type) is accounted for here. A mapping is a planning disposition, not implementation completion.

| Existing ID | Existing named habitat | Proposed biome / variant / layer mapping | Representation reason |
| --- | --- | --- | --- |
| HABITAT-TYPE-001 | Boreal forest | Closed Boreal Forest; Open Boreal Woodland; Mixed Boreal Forest | Base profiles |
| HABITAT-TYPE-002 | Temperate deciduous forest | Temperate Deciduous Forest; Cool Mixed Forest | Base profiles plus named broadleaf variants |
| HABITAT-TYPE-003 | Temperate rainforest | Temperate Rainforest | Base profile |
| HABITAT-TYPE-004 | Tropical rainforest | Tropical Rainforest | Base profile |
| HABITAT-TYPE-005 | Tropical seasonal forest | Seasonal Tropical Forest; Tropical Dry Forest | Base profiles |
| HABITAT-TYPE-006 | Dry woodland | Dry Temperate Woodland; Subtropical Dry Woodland; Dry Boreal Woodland | Climate-specific base profiles |
| HABITAT-TYPE-007 | Mediterranean woodland | Mediterranean Woodland; Mediterranean Scrub | Base profiles plus regional variants |
| HABITAT-TYPE-008 | Savanna | Open Savanna; Wooded Savanna; Semiarid Savanna | Base profiles |
| HABITAT-TYPE-009 | Prairie | Tallgrass Prairie; Mixed-grass Prairie; Shortgrass Prairie | Base profiles |
| HABITAT-TYPE-010 | Steppe | Cold Steppe; Alpine Dry Steppe | Base profiles |
| HABITAT-TYPE-011 | Meadow | Temperate Meadow; Montane Meadow | Base profiles |
| HABITAT-TYPE-012 | Heathland | Temperate Heathland; Boreal Heath | Base profiles plus heath variants |
| HABITAT-TYPE-013 | Alpine meadow | Alpine Meadow; Tropical Alpine Grassland | Base profiles |
| HABITAT-TYPE-014 | Alpine scree | Alpine Barrens; LAY-012 | Base profile plus slope-deposit layer |
| HABITAT-TYPE-015 | Tundra | Dry Tundra; Shrub Tundra; Tussock Tundra; Wet Tundra; Rock Tundra | Base profiles |
| HABITAT-TYPE-016 | Hot desert | Hot Desert; Hyperarid Desert | Base profiles |
| HABITAT-TYPE-017 | Cold desert | Cold Desert | Base profile |
| HABITAT-TYPE-018 | Rocky desert | Hot Desert; Cold Desert; LAY-003 | Base climate profile plus rock substrate |
| HABITAT-TYPE-019 | Gravel desert | Hot Desert; Cold Desert; LAY-002 | Base climate profile plus gravel pavement |
| HABITAT-TYPE-020 | Dune field | Hot Desert; Cold Desert; LAY-001 | Climate profile plus dune landform |
| HABITAT-TYPE-021 | Salt flat | Salt Basin; LAY-023 | Base profile plus salt-crust state |
| HABITAT-TYPE-022 | Desert wash | Seasonal River; LAY-013 | Intermittent corridor plus depositional forms |
| HABITAT-TYPE-023 | Oasis | Spring; Warm Lake; LAY-022 | Local supported habitat complex within arid matrix |
| HABITAT-TYPE-024 | Headwater stream | Headwater Stream | Base profile |
| HABITAT-TYPE-025 | Mountain gorge | Mountain River; LAY-014 | Connected river plus gorge relief |
| HABITAT-TYPE-026 | Lowland river | Lowland River | Base profile |
| HABITAT-TYPE-027 | Floodplain | Floodplain Forest; Floodplain Backwater; Seasonal Flooded Grassland | Floodplain water/vegetation profiles share one river valley |
| HABITAT-TYPE-028 | Alluvial fan | LAY-013 | Depositional landform |
| HABITAT-TYPE-029 | Lake shore | LAY-020 | Water-body shoreline boundary |
| HABITAT-TYPE-030 | Seasonal pond | Seasonal Pond | Base profile |
| HABITAT-TYPE-031 | Marsh | Freshwater Marsh | Base profile |
| HABITAT-TYPE-032 | Swamp | Forested Swamp; Tropical Swamp Forest; Peat Swamp Forest | Climate/hydrology-specific profiles |
| HABITAT-TYPE-033 | Bog | Boreal Bog; Temperate Bog | Base profiles |
| HABITAT-TYPE-034 | Fen | Fen | Base profile |
| HABITAT-TYPE-035 | Wet meadow | Wet Meadow | Base profile |
| HABITAT-TYPE-036 | Rocky intertidal shore | Rocky Shore; LAY-026 | Base profile plus pools |
| HABITAT-TYPE-037 | Sandy beach | Sandy Shore | Base profile |
| HABITAT-TYPE-038 | Shingle beach | Shingle Shore | Base profile |
| HABITAT-TYPE-039 | Coastal dune | Coastal Dune Grassland; LAY-001 | Vegetation profile plus landform |
| HABITAT-TYPE-040 | Estuary | Estuary | Base profile |
| HABITAT-TYPE-041 | Delta | LAY-019 | Connected river/ocean distributary landform, not unrelated noise |
| HABITAT-TYPE-042 | Tidal mudflat | Tidal Mudflat | Base profile |
| HABITAT-TYPE-043 | Salt marsh | Salt Marsh | Base profile |
| HABITAT-TYPE-044 | Mangrove forest | Mangrove Forest; LAY-025 | Base profile plus root/channel structure |
| HABITAT-TYPE-045 | Seagrass meadow | Seagrass Meadow | Base profile |
| HABITAT-TYPE-046 | Kelp forest | Kelp Forest | Base profile |
| HABITAT-TYPE-047 | Coral reef | Coral Reef | Base profile |
| HABITAT-TYPE-048 | Rock pool | Rocky Shore; LAY-026 | Microhabitat rather than a biome per pool |
| HABITAT-TYPE-049 | Continental shelf floor | Marine Shelf; Cold Marine Shelf | Base profiles |
| HABITAT-TYPE-050 | Deep seafloor | Deep Ocean Floor; Submarine Canyon; Hydrothermal Vent Field | Vertical marine profiles |
| HABITAT-TYPE-051 | Karst cave | Dry Cave; Wet Cave; Groundwater Cave; LAY-008 | Ecology profiles plus karst geometry/host rock |
| HABITAT-TYPE-052 | Dry cave | Dry Cave | Base profile |
| HABITAT-TYPE-053 | Wet cave | Wet Cave; Groundwater Cave | Base profiles |
| HABITAT-TYPE-054 | Underground river | Underground River | Base profile |
| HABITAT-TYPE-055 | Guano chamber | LAY-035 | Local animal-input microhabitat |
| HABITAT-TYPE-056 | Burn scar | LAY-047 | Disturbance state |
| HABITAT-TYPE-057 | Windthrow gap | LAY-048 | Disturbance state |
| HABITAT-TYPE-058 | Landslide scar | LAY-049 | Disturbance landform |
| HABITAT-TYPE-059 | Regrowing clearing | LAY-050 | Successional state |
| HABITAT-TYPE-060 | Forest edge | LAY-051 | Habitat boundary |
| HABITAT-TYPE-061 | Treeline | LAY-052 | Climate/vegetation transition |
| HABITAT-TYPE-062 | Seep hollow | Spring; LAY-021 | Groundwater-supported local wet habitat |

## Combined examples

| Named environment encountered by a player | Proposed composition |
| --- | --- |
| Oak–beech limestone valley | Temperate Deciduous Forest + oak–beech community + geology-derived soil/profile; connected river hollows use wet/riparian profiles locally. |
| Red-rock desert with dunes | Hot Desert or Semidesert from climate + geology/material palette + dune/gravel/outcrop layers where sediment history permits. |
| Snow-covered pine forest | Existing eligible boreal/temperate forest + regional pine community + seasonal snow state; no winter-only identity required. |
| Cypress floodplain swamp | Forested Swamp or Tropical Swamp Forest + cypress community where province/climate permit + shared floodplain hydrology. |
| Mangrove estuary | Connected Estuary/Tidal Mudflat/Mangrove Forest profiles + salinity/inundation gradients and root channels. |
| Glacial valley lake | Icefield/Alpine Barrens context + Glacial Lake + routed spillway, lake-shore layer and sediment input. |
| Cave river beneath a forest | Surface forest above; Underground River/Groundwater Cave in the appropriate Y band; host rock and cave forms remain separate. |
| Kelp-covered rocky shelf | Cold Marine Shelf/Marine Shelf and Kelp Forest where eligible; water-column profile and actual seabed substrate remain distinct. |
| Burned woodland recovering | Original compatible woodland profile + burn/regrowth state + species traits and resource accounting; no mandatory new biome per recovery year. |
| Oasis in a dry basin | Arid matrix + an actual Spring/standing-water/wetland source complex + local supported vegetation; no unlimited water created by the oasis name. |

## Overlap and exclusion rules

1. Biome profiles, community variants and layers are separate planning records with explicit parent relationships.
2. A season, tint, rock type or individual tree species does not automatically create a new biome ID.
3. A local formation such as a waterfall, geode, dune, speleothem or guano deposit requires valid source geometry/material/biology.
4. Disturbance and recovery preserve the underlying climate/hydrology identity and follow the future natural-versus-placed-content policy.
5. Names such as chaparral, taiga, savanna or rainforest describe selected regional/functional expressions; species are chosen from coherent biological provinces, not copied wholesale between continents.
6. Extending these lists does not change the existing 1,013-row natural-type count; this is a separate biome-planning axis, with deliberate cross-references.
7. Neither the 118-profile proposal nor the mapped habitat list is a claim of exhaustive scientific ecoregion/species coverage.
