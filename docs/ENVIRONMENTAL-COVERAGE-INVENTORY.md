# Environmental coverage inventory

Planning baseline: 2026-10-20. Audited against main commit `fc0b0feb29a031dba8aed6c1e0c36ffb8a228670`.

**Owner direction: finish the environmental planning inventory before implementing these materials and systems.** This document adds coverage questions and candidate content. It does not schedule implementation, change feature-completion checkboxes, alter gameplay defaults, or approve every candidate for a release.

The [design reference](design-reference.md), [owner decisions](design-decisions-2026-10-18.md) and [roadmap](../roadmap.md) retain their existing authority. Existing requirements are preserved. New examples below are **proposed coverage candidates** until reviewed. A row describing existing plans is not a claim that the feature exists in code.

**Accepted terrain scope (2026-10-21 PDT):** see [terrain mechanics](TERRAIN-MECHANICS.md) for structural geology and altitude-independent ore/gem eligibility, material erosion resistance, soil profiles, wind forms, underground rivers/springs, slope stability/cave-ins and natural obstructions. Sinkholes are excluded. Full live sediment transport is optional/default-off; panning/sluices use consumed recipes as the baseline. This supersedes broader candidate wording without completing implementation.

## What “exhaustive” means for this plan

Aim for complete coverage of environmental **roles and families**, supported climates/habitats, material states and transformations. Species and geological names remain expandable. A finite checklist cannot establish that every organism, rock name or regional variant has been enumerated.

Every candidate needs an explicit disposition: represent individually; represent within a documented group; model as a property/state; render as environmental detail; model as a generated feature or abstract process; defer; or exclude with a reason. A feature may have a complete plan while remaining entirely unimplemented.

The current audit contains **22 coverage domains and 163 review entries**. Entries remain open for scope/species/representation decisions. This inventory substantially expands coverage; it is not a scientific certification of universal completeness.

## Gaps found in the existing list

| Existing planning coverage | Finding |
| --- | --- |
| Rocks and trees | Substantial seed catalogs and property sheets already exist. Preserve them and audit missing roles, regions, forms, states and geological settings. |
| Soil, grass and clay | Broad requirements exist, but comparable named catalogs, profile/deposit coverage and lifecycle decisions are missing. |
| Organic material | Compost and litter effects are mentioned; forest-floor layers, deadwood, organic deposits and decomposer coverage need explicit entries. |
| Sediments and habitats | Sand/gravel and drainage architecture exist; sediment origins, grain/composition distinctions and wetland/coastal/underground habitat coverage need a full matrix. |
| Natural change | Many processes are mentioned separately. Inputs, outputs, timescales and representation boundaries need to be reconciled. |
| Catalog bookkeeping | Types, mixtures, states, forms and processes need separate identities so “more entries” does not mean duplicated materials. |

## Identity rules

- **Type/composition:** granite, a wood species, clay mineral assemblage or a sediment mixture.
- **Texture/grade:** grain size, sorting, plasticity or a pottery working grade. These may overlap composition.
- **State:** wet, dry, frozen, decaying, charred or fired; only combinations meaningful for that material apply.
- **Form:** boulder, gravel bed, bark, log, litter layer, tuft, root mat or soil horizon.
- **Context:** host rock, soil profile, habitat, climate, depth and depositional history.
- **Process:** weathering, transport, growth, decomposition, freezing or firing.

Do not make every combination a separate block/item/species. A clay-sized particle is not necessarily a clay mineral; a pottery grade is not a separate mineral classification. A grass-covered soil is not automatically a new soil composition. Bedrock is a substrate, not a mandatory soil horizon.

## Named type lists added

The [master named-catalog index](NATURAL-TYPE-CATALOG.md) now links **1013 named entries** in geology/soils/clays, biology/organic materials, and environmental media/habitats/processes. All original 44 rock/material rows and 43 tree/plant rows are preserved. Entries are labeled by kind so types, taxonomic groups, mixtures, forms, states and processes are not confused.

This fills in named candidate lists alongside the coverage reviews below. Individual scope dispositions, regional/species selections and complete property sheets remain open; no implementation milestone is completed by adding names.

## Coverage domains

| Code | Domain | Review entries |
| --- | --- | ---: |
| GEO | Rock families and geological structure | 8 |
| MIN | Minerals, ores and geological resources | 7 |
| SED | Loose sediments and depositional materials | 8 |
| SOI | Soil composition, horizons and condition | 10 |
| CLA | Clay composition, deposits and working properties | 6 |
| ORG | Organic matter and decomposition products | 8 |
| WOO | Wood, bark, roots and deadwood states | 7 |
| TRE | Tree and woody-plant roster coverage | 7 |
| GRA | Grasses, sedges, rushes and turf | 7 |
| HER | Herbs, flowers, ferns and other land plants | 7 |
| FUN | Fungi, mosses, lichens and soil communities | 7 |
| AQU | Aquatic vegetation and biologically formed surfaces | 7 |
| BIO | Animal-derived environmental materials and ecological links | 6 |
| WAT | Water environments and water-carried material | 7 |
| ICE | Snow, ice and frozen ground | 6 |
| VOL | Volcanic and geothermal environments | 5 |
| AIR | Air, atmospheric moisture and particles | 6 |
| HAB | Habitat and landform coverage matrix | 12 |
| PRO | Environmental processes and natural timescales | 12 |
| USE | Natural functions and processed-material boundaries | 6 |
| ART | Environmental appearance and sensory coverage | 6 |
| CTL | Planning completeness, ownership and future constraints | 8 |

## GEO — Rock families and geological structure

**Existing plan coverage:** The existing catalog already covers igneous, sedimentary and metamorphic rocks, fracture, heat, workability, weathering products and ore associations. Source: [Design reference §4.17](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| GEO-01 | Preserve the existing rock roster | Retain every current catalog entry and its natural function; grouped entries remain visible for later species/type decisions. |
| GEO-02 | Additional igneous coverage | Review dacite, trachyte, anorthosite, dunite, phonolite and uncommon alkaline rocks for distinct appearance, host-rock or processing roles; merge aliases or redundant roles explicitly. |
| GEO-03 | Additional metamorphic coverage | Review migmatite, quartz-rich versus carbonate-rich contact rocks, skarn, mylonite and fault-related rocks; distinguish rock type from deformation texture. |
| GEO-04 | Sedimentary composition and texture | Review arkose, argillite, diatomite, phosphorite, chert varieties and evaporite mixtures; distinguish grain size, cement and composition. |
| GEO-05 | Rock bodies and contacts | Beds, intrusions, dikes, sills, lenses, faults, folds, unconformities and contact zones; structural mechanics accepted in [terrain plan](TERRAIN-MECHANICS.md), with specific unit/property definitions still open. |
| GEO-06 | Exposed rock forms | Outcrops, boulders, fractured faces, jointing, columnar forms, overhangs, scree and weathering rinds. |
| GEO-07 | Weathered parent material | Fresh rock, fractured rock, weathered rock and saprolite/regolith; define products and avoid implying one universal sequence. |
| GEO-08 | Special inclusions | Nodules, geodes, fossils, mineral veins, concretions and embedded shells; decide discoverable content versus decorative texture. |

## MIN — Minerals, ores and geological resources

**Existing plan coverage:** Host-rock-dependent multi-ore veins, surface indicators, finite veins and high-reserve bedrock resources are already planned. Source: [Design §4.1](design-reference.md#s4-1); [owner decisions](design-decisions-2026-10-18.md#finite-veins-and-bedrock-reserves).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| MIN-01 | Rock-forming minerals | Quartz, feldspar, mica, calcite, dolomite, olivine, pyroxene and amphibole groups; choose inspection data versus separate resources. |
| MIN-02 | Ore roster cross-check | Cross-reference metals and gems with host/deposit setting, cover/exposure, indicators and outputs. No blanket high-elevation ore exclusion; rare eligible mountain ore/gem deposits follow geology. Specific associations remain to define. |
| MIN-03 | Industrial minerals | Salt, gypsum, sulfur, graphite, phosphate, refractory minerals and abrasives; map existing entries before adding duplicates. |
| MIN-04 | Pigments and colorants | Ochres, iron oxides, carbon blacks, mineral pigments and plant dyes; decide which differences change use or appearance. |
| MIN-05 | Gems and crystals | Common crystals, gem-bearing deposits and decorative minerals; select distinct roles rather than requiring every mineral name. |
| MIN-06 | Fossil carbon resources | Coal grades are existing; review natural bitumen, oil shale and seep deposits as optional candidates without expanding the technology ceiling. |
| MIN-07 | Deposit exposure and depletion | Outcrop, buried, placer, residual and hydrothermal occurrences; define discovery cues, exhausted material and recovery limits. |

## SED — Loose sediments and depositional materials

**Existing plan coverage:** Sand, gravel, clay and rock-derived colors are present broadly; erosion/deposition maps are an architecture direction. Source: [Design §4.1](design-reference.md#s4-1); [drainage architecture](LOD-AND-DRAINAGE-ARCHITECTURE.md#terrain-work-order).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| SED-01 | Grain-size coverage | Boulders, cobbles, pebbles/gravel, sand, silt and clay-sized sediment; choose useful game-scale groupings. |
| SED-02 | Composition versus size | Quartz-rich, volcanic, carbonate/shell-rich and mixed sediment; color alone must not silently define chemistry. |
| SED-03 | River deposits | Channel beds, bars, floodplain silt, levees, fans and deltas remain coverage candidates. Full live sediment transport is optional/default-off; baseline panning/sluices use consumed recipes, with local deposit/provenance links only when useful. |
| SED-04 | Lake deposits | Shore gravels, fine lakebed mud, marl/calcareous mud, organic-rich sediment and seasonal exposed margins. |
| SED-05 | Wind deposits | Dune sand, windblown silt/loess, dust mantles and deflation surfaces. |
| SED-06 | Glacial deposits | Unsorted till, moraine material, outwash sand/gravel and fine glacial sediment; separate ancient deposits from live glacier simulation. |
| SED-07 | Slope deposits | Talus, scree, colluvium, rockfall debris, landslide material and mixed footslope deposits. |
| SED-08 | Coastal deposits | Beach sand/shingle, shell hash, tidal-flat mud, carbonate sand, dune material and drift lines; tie to habitat coverage. |

## SOI — Soil composition, horizons and condition

**Existing plan coverage:** Sandy, silty, loamy, clay-rich and rocky soils, nutrients, pH effects, drainage and moisture retention are already described. Source: [Design §4.1](design-reference.md#s4-1); [property mapping §4.14](design-reference.md#s4-14).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| SOI-01 | Texture classes | Review sand, loamy sand, sandy loam, loam, silt loam, silt, sandy clay loam, clay loam, silty clay loam, sandy clay, silty clay and clay; grouping must be explicit. |
| SOI-02 | Soil horizons | Organic surface material, topsoil, leached horizons where appropriate, subsoil and weathered parent material; bedrock is a separate substrate. Not every location has every layer. |
| SOI-03 | Profile depth and transitions | Thin rocky uplands, deep valley soils, buried layers, exposed subsoil and gradual boundaries; cover cliffs, river cuts and excavations. |
| SOI-04 | Organic soils | Peaty and other organic-rich soils; separate organic soil from mineral soil containing organic matter. |
| SOI-05 | Regional soil roles | Calcareous, acidic/leached, iron-rich tropical, volcanic-ash-derived, saline, sodic and poorly drained soils; these overlap texture classes. |
| SOI-06 | Moisture and aeration | Dry, moist, saturated, waterlogged and drained states; tie rooting and decomposition to the selected abstraction. |
| SOI-07 | Chemistry and fertility | pH, nutrient pools, organic content, salinity and nutrient availability; retain existing fertility and nitrogen-fixing concepts. |
| SOI-08 | Structure and disturbance | Loose, compacted, crusted, root-bound, tilled and trampled ground; decide which need separate appearance or behavior. |
| SOI-09 | Frozen-ground profiles | Seasonal frost, active layer, permafrost, ground ice and thawed ground; reconcile with existing permafrost decisions. |
| SOI-10 | Soil formation and loss | Parent material, litter inputs, leaching, mixing, erosion and deposition; separate generated history from changes during play. |

## CLA — Clay composition, deposits and working properties

**Existing plan coverage:** Several clay kinds, firing differences, drying and pottery are already in the design. Source: [Design §4.1](design-reference.md#s4-1); [primitive technology §4.3](design-reference.md#s4-3).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| CLA-01 | Clay mineral families | Kaolinite, illite, smectite, chlorite and mixed assemblages are coverage candidates; not interchangeable with clay-sized particles. |
| CLA-02 | Usable clay grades | Review earthenware, stoneware/refractory, kaolin-rich and highly plastic working clays; grades may overlap mineral composition. |
| CLA-03 | Deposit origins | Residual weathering pockets, transported river/lake deposits, altered volcanic material and hydrothermal deposits. |
| CLA-04 | Impurities and preparation | Sand/silt, organics, carbonate fragments, iron-bearing material, washing, levigation and temper; define relevant outputs. |
| CLA-05 | Working states | Raw deposit, extracted, cleaned, wet/plastic, drying, dry and fired; resolve shrinkage, cracking and rewetting rules. |
| CLA-06 | Replenishment decision | Finite deposits versus limited new sediment deposition remains open; preserve the existing no-build-overwrite rule. |

## ORG — Organic matter and decomposition products

**Existing plan coverage:** Compost, organic leftovers, litter acidity and byproduct uses are broad existing directions; peat is only mentioned in catalog cross-reference text. Source: [Design §4.17](design-reference.md#s4-17); [resource renewal decisions](design-decisions-2026-10-18.md#resource-renewal-and-waste--accepted-direction).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| ORG-01 | Fresh surface litter | Broadleaf litter, needles, grass litter, flowers, seeds, twigs and shed bark; choose mixtures versus individual items. |
| ORG-02 | Decomposing forest floor | Litter, partly decomposed duff and humus-rich material; define appearance and stages without treating humus as one pure substance. |
| ORG-03 | Wet organic accumulations | Peat, bog/fen material and organic lake/swamp mud; separate habitat and material classifications. |
| ORG-04 | Plant leftovers | Crop residues, straw, chaff, husks, spoiled produce and fiber scraps; distinguish natural deposits from player processing. |
| ORG-05 | Animal-derived organic inputs | Dung, guano, carcass remains and spoiled animal products; reference BIO rather than duplicate resource definitions. |
| ORG-06 | Managed amendments | Compost, mulch and manure preparations; nutrient contributions, losses and inappropriate inputs need future definitions. |
| ORG-07 | Burned organic residues | Charcoal/charred matter and ash; separate mineral-rich ash from organic carbon and retain material-specific uses. |
| ORG-08 | Decomposition conditions | Temperature, moisture, oxygen, substrate and decomposer activity; select abstract rates and endpoints later. |

## WOO — Wood, bark, roots and deadwood states

**Existing plan coverage:** Wood properties, bark, resin, fiber, fuel, coppicing and regrowth are already planned. Source: [Design §4.17](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| WOO-01 | Living tree parts | Trunk, branches, roots, bark, sapwood/heartwood distinctions and foliage; choose functional versus visual differences. |
| WOO-02 | Standing deadwood | Snags, dead branches, hollow trunks and partly dead trees; retain habitat and harvest roles. |
| WOO-03 | Downed wood | Fallen logs, branch piles, windthrow root plates, broken trunks and stumps. |
| WOO-04 | Decay and moisture states | Fresh, seasoned, waterlogged, decaying, rotten and charred wood; state compatibility differs by species and use. |
| WOO-05 | Coastal wood | Driftwood, stranded branches and water-worn logs; define source identity and handling. |
| WOO-06 | Tree exudates and products | Sap, latex, resins, gums, tannin-bearing bark, cork and bast; audit every existing species product. |
| WOO-07 | Harvest and persistence | Felling, branch collection, debarking, stump/root persistence, coppice and natural regrowth; no regrowth through builds. |

## TRE — Tree and woody-plant roster coverage

**Existing plan coverage:** The detailed reference already lists temperate broadleaf/fruit, conifers and tropical/subtropical/arid/coastal trees. Preserve all existing entries. Source: [Design §4.17](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| TRE-01 | Existing species coverage matrix | Map each listed tree/group to climate, soil, form, products, wood traits, seasonality and ecological roles; split grouped entries where roles differ. |
| TRE-02 | Cold and high-elevation coverage | Review dwarf willow/birch, treeline forms and additional boreal/alpine representatives. |
| TRE-03 | Dry woodland and Mediterranean coverage | Review drought-adapted shrubs/trees, evergreen oaks, pistachio/carob and other regionally important forms where distinct. |
| TRE-04 | Tropical forest coverage | Review understory trees, figs, breadfruit/jackfruit, balsa and tree ferns as candidates; tree ferns need their own botanical/material treatment. |
| TRE-05 | Wetland and riparian coverage | Audit flood tolerance, swamp forms, alder/willow/cypress roles and mangrove groups already present. |
| TRE-06 | Shrubs and woody climbers | Berry bushes, heath, scrub, brambles, vines and lianas; tree catalogs do not cover the full woody understory. |
| TRE-07 | Form and age diversity | Seedling, sapling, mature, old, damaged and dead forms; wind-shaped, multi-stem and coppiced forms where appropriate. |

## GRA — Grasses, sedges, rushes and turf

**Existing plan coverage:** Multiple climate/soil-dependent grass and ground-cover types are promised, but no comparable roster exists. Source: [Design §4.1](design-reference.md#s4-1).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| GRA-01 | Grassland forms | Short turf, bunch/tussock grass, tall meadow/prairie grass and savanna grass; regionally important representatives. |
| GRA-02 | Seasonal grass roles | Cool/warm-season growth groups, seed heads, dormancy, senescence and dry standing vegetation. |
| GRA-03 | Wetland graminoids | Sedges, rushes, reeds and cattails; do not label them all as true grasses. |
| GRA-04 | Coastal and dune plants | Salt-marsh grasses, dune-binding grasses and salt-tolerant ground cover. |
| GRA-05 | Bamboo and giant grasses | Existing bamboo entry retained; distinguish clump/running growth, shoots and harvested culms. |
| GRA-06 | Ground-cover continuity | Sparse tufts, continuous sod, root mats, bare patches and transitions to litter/rock/mud. |
| GRA-07 | Use and recovery | Forage, hay, thatch, straw, grazing, mowing, trampling, seed spread and regrowth; choose species-level detail later. |

## HER — Herbs, flowers, ferns and other land plants

**Existing plan coverage:** Wild fiber/dye/forage/medicine plants, crop precursors and crop climate requirements are existing directions. Source: [Design §4.1](design-reference.md#s4-1); [food/farming §4.6](design-reference.md#s4-6).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| HER-01 | Ferns and allies | Ground ferns, climbing/epiphytic ferns, horsetails and clubmoss forms; choose representative roles. |
| HER-02 | Flowering ground plants | Woodland herbs, meadow flowers, alpine flowers and desert annuals; include flowering/seed stages. |
| HER-03 | Dryland plants | Cacti, succulents, agave/yucca-like forms and drought-tolerant herbs; avoid merging all dry habitats. |
| HER-04 | Climbers and epiphytes | Vines, lianas, orchids/bromeliad-like forms and plants on trunks/branches; define substrate rules. |
| HER-05 | Useful plant products | Fiber, dye, oil, starch, forage, spices and medicinal-use candidates; each needs an identified source and processing destination. |
| HER-06 | Roots and storage organs | Roots, rhizomes, bulbs, corms and tubers; define regeneration and harvest effects. |
| HER-07 | Wild and cultivated relationships | Crop ancestors, weeds, seed banks, spreading, dormancy and harvest stages; no requirement for exhaustive agricultural cultivars. |

## FUN — Fungi, mosses, lichens and soil communities

**Existing plan coverage:** These have little explicit roster coverage in the current environmental catalog. Source: [Current natural catalog](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| FUN-01 | Macrofungi | Ground mushrooms, wood-decay brackets, puffballs and underground fruiting candidates; edible/toxic/useful distinctions require species research. Separate fruiting bodies from the organism. |
| FUN-02 | Fungal ecological roles | Decomposers, mycorrhizal partners and plant pathogens; representation may be abstract rather than entities. |
| FUN-03 | Mosses and liverworts | Ground, rock, trunk and wetland forms; moisture/substrate preferences and harvest roles. |
| FUN-04 | Lichens | Crustose, foliose and fruticose forms on rock, bark and soil; distinct growth form and substrate choices. |
| FUN-05 | Biological soil crusts | Dryland surface communities involving cyanobacteria, algae, fungi, lichens and mosses; candidate role distinct from ordinary grass. |
| FUN-06 | Microscopic soil life | Bacteria and other microbes as nutrient/decomposition state, plus soil invertebrate links; no per-organism simulation implied. |
| FUN-07 | Mold and spoilage | Environmental mold versus food spoilage; distinguish visible effects, material damage and optional hazards. |

## AQU — Aquatic vegetation and biologically formed surfaces

**Existing plan coverage:** Fishing habitats and several coastal tree roles are described; a comprehensive aquatic material/plant roster is absent. Source: [Fishing decisions](design-decisions-2026-10-18.md#fishing); [natural catalog](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| AQU-01 | Freshwater submerged plants | Representative submerged stems, rooted aquatic plants and freshwater algae. |
| AQU-02 | Floating vegetation | Water lilies, duckweed-like cover and floating plant mats; light, flow and coverage decisions. |
| AQU-03 | Emergent vegetation | Reeds, sedges, rushes and cattails share entries with GRA; banks, shallow water and seasonal exposure. |
| AQU-04 | Marine vegetation | Seagrass, kelp and other seaweed groups; distinguish flowering seagrasses from algae. |
| AQU-05 | Algal films and accumulations | Rock biofilms, stream growth, pond scum and stranded seaweed; blooms are a separate optional process. |
| AQU-06 | Reef and shell-built surfaces | Coral, shell beds, oyster/mussel structures and reef rubble; animal habitat hooks rather than plants. |
| AQU-07 | Organic shore deposits | Wrack lines, root mats, drift vegetation and mixed shell/plant deposits; avoid duplicate material definitions. |

## BIO — Animal-derived environmental materials and ecological links

**Existing plan coverage:** Weight-based animal products, recurring products, hides and habitat recovery are planned; this is an environmental interface, not a complete fauna roster. Source: [Animal-yield decisions](design-decisions-2026-10-18.md#animal-weight-and-meatmaterial-yields).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| BIO-01 | Hard remains | Bone, antler, horn, teeth, eggshell, mollusk shell and exoskeleton/chitin materials; distinct properties and origins. |
| BIO-02 | Soft and fibrous products | Hides, fur, wool/hair, feathers, silk, sinew, fat and other existing animal-product chains; review honey, beeswax and propolis as distinct candidates. |
| BIO-03 | Deposits and traces | Dung, guano, tracks, burrows, nests, webs, molts and shed materials; decide persistent objects versus decoration. |
| BIO-04 | Carcasses and scavenging | Fresh remains, decomposition, scavenger access and nutrients; connect to existing mass accounting. |
| BIO-05 | Ecosystem services | Pollination, seed dispersal, grazing, browsing, burrowing and decomposition; select abstract habitat effects versus individual behavior. |
| BIO-06 | Habitat engineering | Dams, burrows, mounds, reef formation and tree damage as review candidates; no new species is committed by this row. |

## WAT — Water environments and water-carried material

**Existing plan coverage:** Source families, aquifers, quality, salinity, sediment, recharge and extraction are already proposed. Source: [Water/aquifer decisions](design-decisions-2026-10-18.md#water-aquifers-and-wells).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| WAT-01 | Source-family completeness | Rain, snowmelt, springs, streams/rivers, lakes/ponds, wetlands, groundwater, hot/mineral springs, estuaries and seawater. |
| WAT-02 | Quality dimensions | Salinity, temperature, suspended sediment, dissolved mineral load, organic coloration and contamination are separate axes. |
| WAT-03 | Flow environments | Still pools, riffles, rapids, waterfalls/cascades, channels, seeps and groundwater flow; match materials and habitat. |
| WAT-04 | Water-substrate contact | Gravel/sand/silt/clay/organic beds, saturated banks, shore exposure, infiltration and perched layers. |
| WAT-05 | Deposited versus suspended material | Track the planning boundary between colored/turbid appearance and actual transport/deposition. |
| WAT-06 | Wet/dry boundaries | Seasonal ponds, drying streams, flood margins, floodplain pools, tidal exposure and stranded materials. |
| WAT-07 | Water budgets and connections | Catchments, lake outlets, aquifer links and coastal mixing; retain existing cross-region drainage requirements. |

## ICE — Snow, ice and frozen ground

**Existing plan coverage:** Snow, permafrost, seasonal freezing and moisture-driven ice cementation are already planned. Source: [Design §4.1](design-reference.md#s4-1); [water/snow audit](WATER-REFERENCE-AUDIT-2026-10-20.md).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| ICE-01 | Snow forms | Fresh snow, settled/packed snow, crusted snow, drifts, wind-scoured patches and snow over vegetation. |
| ICE-02 | Surface ice | Pond/lake ice, river ice, shore/sea ice, icicles and frozen seepage; decide necessary distinctions. |
| ICE-03 | Mixed states | Slush, wet snow, meltwater, refrozen crust and snow/ice with sediment. |
| ICE-04 | Glacial environments | Glacier ice, firn, crevasses, debris-covered ice and meltwater deposits as candidates; generated scenery is distinct from moving glaciers. |
| ICE-05 | Ground ice and thaw | Seasonal frost, ground-ice lenses, active layer and permafrost; reference SOI rather than duplicate properties. |
| ICE-06 | Winter/spring transitions | Accumulation, insulation, melt, refreezing, snowmelt runoff and exposure of buried litter; select future abstractions. |

## VOL — Volcanic and geothermal environments

**Existing plan coverage:** Volcanic rocks, geothermal source families and hazardous heat are present in broad design. Source: [Design §4.1](design-reference.md#s4-1); [water decisions](design-decisions-2026-10-18.md#water-aquifers-and-wells).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| VOL-01 | Volcanic surface deposits | Ash, lapilli, bombs, pumice/scoria fragments and weathered tephra; separate deposits from consolidated rocks. |
| VOL-02 | Lava and cooling products | Molten material, cooled crust, flow forms, lava tubes and cooling fractures; static generated forms versus dynamic behavior. |
| VOL-03 | Hydrothermal deposits | Silica/carbonate crusts, sulfur-bearing deposits and mineral staining; cross-reference minerals and spring water. |
| VOL-04 | Geothermal features | Hot springs, geysers, fumaroles and steam vents as coverage candidates; explicit water/heat boundaries needed. |
| VOL-05 | Landscape recovery | Fresh barren ground, weathered volcanic substrate and later vegetation stages; choose generated ages versus runtime succession. |

## AIR — Air, atmospheric moisture and particles

**Existing plan coverage:** Climate, weather, gas hazards, smoke/fire interactions and presentation hooks are broad design themes. Source: [Design §4.7](design-reference.md#s4-7); [clothing/temperature §4.9](design-reference.md#s4-9).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| AIR-01 | Atmospheric conditions | Temperature, humidity, wind, exposure and shelter; define shared inputs before separate effects. |
| AIR-02 | Condensed moisture | Rain, drizzle, mist, fog, dew, frost and hoarfrost; distinguish weather from surface coatings. |
| AIR-03 | Airborne mineral particles | Dust, blown sand, volcanic ash and mineral aerosols; separate visual effects from material transport. |
| AIR-04 | Airborne biological particles | Pollen, spores and wind-carried seeds; candidate presentation and dispersal roles. |
| AIR-05 | Combustion products | Smoke, soot and ash; source material and ventilation connections. |
| AIR-06 | Subsurface gases | Low oxygen, carbon dioxide, methane and sulfurous gases as hazard candidates; named gas chemistry/mechanics require later research and scope decisions. |

## HAB — Habitat and landform coverage matrix

**Existing plan coverage:** Climate-based regional vegetation and broad geology/hydrology are planned; no complete habitat cross-check exists. Source: [Design §4.1](design-reference.md#s4-1); [drainage architecture](LOD-AND-DRAINAGE-ARCHITECTURE.md).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| HAB-01 | Forests and woodland | Boreal, temperate deciduous, temperate rainforest, tropical seasonal, tropical wet and dry/open woodland; canopy gaps and forest edges. |
| HAB-02 | Grassland and shrubland | Meadow, steppe/prairie, savanna, heath, Mediterranean scrub and dry shrubland. |
| HAB-03 | Arid environments | Hot/cold desert, dunes, gravel plains, rocky desert, salt flats, washes and oases. |
| HAB-04 | Cold and high-elevation environments | Tundra, alpine meadow, treeline, scree, snowfields and glacial margins. |
| HAB-05 | Freshwater environments | Headwaters, rivers, floodplains, lakes, ponds, springs and seasonal waters. |
| HAB-06 | Wetlands | Marsh, swamp, bog, fen and wet meadow; map substrate, hydrology, vegetation and organic accumulation. |
| HAB-07 | Coastal environments | Rocky shores, sandy/shingle beaches, dunes, estuaries, deltas, mudflats, salt marsh and mangrove. |
| HAB-08 | Marine environments | Intertidal pools, seagrass/kelp, reefs, shelf sediments and deep seafloor; full marine simulation is a separate scope choice. |
| HAB-09 | Underground environments | Dry/wet caves, karst, mountain river passages and returning springs are accepted directions; sinkholes excluded. Other cave habitats/materials remain candidates, with all routing sharing the water budget. |
| HAB-10 | Disturbed and transitional environments | Burn scars, windthrow, landslides, river migration, regrowth, abandoned cultivation and habitat boundaries; separate natural from player-created disturbance. |
| HAB-11 | Landform continuity | Ridges, valleys, basins, terraces, fans, cliffs, shore profiles, watershed divides and cave entrances; materials follow the same terrain plan. |
| HAB-12 | Microhabitats | North/south exposure where meaningful, shaded hollows, seeps, logs, boulders, root pockets and rain shadows; choose scale rather than simulating every detail. |

## PRO — Environmental processes and natural timescales

**Existing plan coverage:** Weathering, seasons, renewal, erosion, soil nutrients and freeze/thaw exist at varying levels of detail. Source: [Design reference](design-reference.md); [renewal decisions](design-decisions-2026-10-18.md#resource-renewal-and-waste--accepted-direction).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| PRO-01 | Weathering and soil development | Physical breakup, chemical alteration, biological effects and parent-material products; define historical generation separately from play-time change. |
| PRO-02 | Erosion, transport and deposition | Water, wind, gravity and ice as separate agents; erosion/deposition maps can represent history without full live solvers. |
| PRO-03 | Hydrologic cycle | Precipitation, interception, infiltration, runoff, storage, evaporation, transpiration, snowmelt and discharge. |
| PRO-04 | Organic and nutrient cycles | Litter input, decomposition, mineralization, uptake, nitrogen-fixing roles, leaching and losses; no free-resource loops. |
| PRO-05 | Plant life cycles | Germination, establishment, growth, flowering, fruiting, seed dispersal, dormancy, senescence and death. |
| PRO-06 | Succession and regeneration | Pioneer cover, canopy closure, gap recovery and disturbance response; decide snapshots versus live regional updates. |
| PRO-07 | Fire and recovery | Ignition, fuel moisture, spread, charring, ash, canopy damage and regeneration; scope peat fire separately if considered. |
| PRO-08 | Freeze/thaw and mass movement | Slope stability and underground cave-ins accepted, with material/support rules and bounded propagation. No sinkhole-producing surface collapse; remaining frost/thaw/creep event details stay under review. |
| PRO-09 | Coastal and lake change | Waves, tides, seasonal levels, sediment movement, shoreline erosion and exposure; distinguish appearance, terrain and water-budget behavior. |
| PRO-10 | Biological interactions | Competition, shade, pollination, grazing, browsing, decomposers, disease and habitat capacity; choose abstractions individually. |
| PRO-11 | Climate and calendar transitions | Season, altitude, latitude/projection where applicable, weather and local exposure; use shared rules rather than competing clocks. |
| PRO-12 | Resource renewal boundaries | Finite geology versus replenishable biological/transported resources; rates, source budgets and protection of placed content stay explicit. |

## USE — Natural functions and processed-material boundaries

**Existing plan coverage:** Natural uses are required for rocks/trees; primitive crafting, pottery, fuel, metallurgy and byproduct reuse are already planned. Source: [Design §4.17](design-reference.md#s4-17); [primitive tech](design-reference.md#s4-3).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| USE-01 | Functional coverage | For each selected material, record ecological, construction, tool, fuel, food, farming, textile, chemical, pigment or habitat roles; decorative roles count when explicit. |
| USE-02 | Natural-to-processed links | Stone to aggregate/lime, earth/clay to cob/daub/ceramics, wood to charcoal/tar, bark to tannin/fiber, plants to thatch/wattle and minerals to mortar/plaster; preserve existing chains. |
| USE-03 | State-dependent usefulness | Wet versus dry fuel, fresh versus rotten timber, raw versus fired clay and frozen versus thawed ground; do not multiply identities unnecessarily. |
| USE-04 | Byproduct destinations | Ash, char, organic scraps, mineral fines and processing residues; useful recipes may be material-specific or absent. |
| USE-05 | Harvest and renewability | Source, yield basis, extraction tool, depletion, regrowth and ecological effect; relate animal outputs to existing mass rules. |
| USE-06 | Progression accessibility | Loose rock/flint/bone, fiber, tinder, fuel, water and clay must have planned acquisition routes across intended starting environments. |

## ART — Environmental appearance and sensory coverage

**Existing plan coverage:** Material-generated appearance, seasonal tint, wetness, frost, detailed shapes and parametric vegetation are existing design directions. Source: [Design §4.17](design-reference.md#s4-17).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| ART-01 | Material variation | Color, grain, banding, inclusions, surface roughness and weathering; preserve recognizability and inspection cues. |
| ART-02 | Ground transitions | Rock/soil, soil/litter, grass/bare ground, mud/water, roots/banks and snow/substrate boundaries. |
| ART-03 | Natural forms | Partial ground cover, irregular deposits, boulders, fallen wood, root exposure and bank detail; choose representation later. |
| ART-04 | Time-dependent appearance | Wet/dry, frozen/thawed, fresh/decayed, seasonal and burned/regrowing states. |
| ART-05 | Near/far consistency | Selected substrate, canopy, water and seasonal appearance must have a coherent distant representation. |
| ART-06 | Sound and movement | Wind in vegetation, rain by substrate, flowing water, surf, ice and wildlife ambience; distinguish optional presentation from material mechanics. |

## CTL — Planning completeness, ownership and future constraints

**Existing plan coverage:** The roadmap already requires evidence, bounded work, data-defined properties, migration and worldgen performance gates. Source: [Roadmap maintenance rules](../roadmap.md#completion-and-maintenance-rules).

All rows below are **open planning reviews**. Existing requirements remain requirements; additions are candidates.

| ID | Coverage review | Candidate content or decision needed |
| --- | --- | --- |
| CTL-01 | Representation decision | Every retained candidate eventually receives named material/species, grouped representative, property/state, visual detail, generated feature, abstract process, deferred or excluded status. |
| CTL-02 | Ownership and aliases | One canonical identity per resource; aliases, grade names, morphology and mixture names point to the right concept. |
| CTL-03 | Placement contract | Formation/source, host material, climate, moisture, elevation, slope, depth, disturbance and neighboring habitat. |
| CTL-04 | Lifecycle contract | Inputs, outputs, transformations, timescale, losses, renewal, persistence and interactions. |
| CTL-05 | Simulation scope | Choose generation-only history, calendar-based regional change, local activity or visual-only treatment; require no universal per-block live simulation. |
| CTL-06 | Future data/performance contract | Bound detail, caches and updates; review state/registry/atlas growth, save size, networking and deterministic generation before implementation. |
| CTL-07 | World safety and configuration | Distinguish natural from placed material, avoid regeneration through builds, define disabled-content behavior and future migration policy. |
| CTL-08 | Evidence and review status | Coverage review is separate from implementation completion; retain open questions, source provenance and explicit deferrals. |

## Minimum record for every selected entry

These are planning fields; select applicable fields and explicit “not applicable” values before choosing a storage or code schema.

| Field group | Required planning content |
| --- | --- |
| Identity | Stable proposed name/ID, family, aliases, parent group, individual versus grouped representation, and scope disposition. |
| Distinct role | Why it deserves representation: natural function, regional significance, appearance, physical behavior, habitat or gameplay use. |
| Composition and structure | Constituents/mixture, grain/texture, soil horizon or biological part, density/mass basis, porosity and permeability where applicable. |
| Occurrence | Formation/source, substrate/host, climate, moisture, salinity, elevation/depth, slope/exposure, habitat and neighboring materials. |
| Physical behavior | Tool/breaking properties, strength, fracture, support, erosion, compaction, swelling/shrinkage and movement where applicable. |
| Water and heat | Retention, drainage, saturation, wet/dry response, freeze/thaw, heat resistance, fuel/combustion and moisture effects where applicable. |
| Chemistry and ecology | pH/salinity, nutrients, toxicity/hazard candidates, decomposition, growth needs, ecological functions and habitat links. Values require later verification. |
| States and lifecycle | Allowed states, transitions, conditions, timescale, inputs/outputs, losses, depletion, renewal and interaction with processing. |
| Human uses | Harvest route, products, construction/tool/fuel/food/farming/textile/chemical/decorative roles and byproducts. A decorative or ecological role can be sufficient. |
| Presentation | Shape, color/texture range, state/season cues, ground/shore transitions, sound where relevant and distant appearance. |
| World behavior | Natural versus placed ownership, preservation around builds, disabled-content behavior, future persistence/configuration needs. |
| Evidence and decisions | Existing requirement/proposal provenance, primary research sources, uncertainties, disposition, reason and later validation requirements. |

## State and transition audit

Use the following as cross-checks, not universal recipes or promises of live simulation:

- **Rock and sediment:** parent rock → selected weathering products; transport → sorted/mixed deposits; deposition and burial may describe generated history.
- **Soil:** parent material + organic inputs + water/climate/organisms → a location-appropriate profile; erosion can expose lower layers. Not every profile contains all horizons.
- **Forest floor:** shed leaves/twigs and deadwood → staged decomposition → organic soil inputs; rates depend on the selected abstraction.
- **Clay:** raw deposit → prepared clay → wet formed clay → drying/dry ware → fired ceramic; rewetting and firing are different transitions.
- **Vegetation:** seeds/propagules → establishment → growth/reproduction → dormancy or death; harvest, grazing, fire and disturbance can alter recovery.
- **Water and sediment:** input/storage → infiltration/runoff/flow → transport/deposition or evaporation/discharge; quality and source budgets remain separate dimensions.
- **Snow and frozen ground:** accumulation/freezing → storage/insulation → melt/thaw/refreezing; connect to water availability and exposure of underlying material.
- **Resource use:** harvesting and processing produce outputs and losses; renewal, composting and recycling cannot silently create unlimited mass, water or nutrients.

For every selected transition, eventually answer: trigger; inputs; outputs; conservation/loss rules; time basis; spatial scope; unloaded-area behavior; natural/placed-content distinction; visual cue; configuration; and future verification evidence.

## Biome/habitat planning reconciliation

The 2026-10-21 [biome plan](BIOME-PLAN.md) supplies a [118-profile named roster](BIOME-CATALOG.md), plus [54 community variants, 63 local layers and a complete mapping of the 62 existing habitat entries](BIOME-VARIANTS-AND-COVERAGE.md). This reconciles named coverage without marking implementation or detailed property-sheet decisions complete. Geology, soil composition, tree dominance and seasonal states do not each require a new biome ID. World block/item texture tiles are planned at up to 64×64 with shared material-family textures and palettes/tints.

**Biome-first sequence:** complete biome coverage and descriptions before designing supporting climate scenarios. Climate fields elsewhere in this inventory remain later integration requirements, not gates for finishing the biome roster. Climate eligibility must gate actual biome placement, alongside terrain, water, substrate and other habitat requirements. See the [design work order](BIOME-PLAN.md#design-work-order).

The approved [glacial coverage pass](GLACIAL-LANDSCAPES.md) supplies named layers and community variants for SED-06, ICE-04, HAB-04 and connected WAT/PRO reviews. Those review entries remain open for detailed representation and behavior decisions.

## Habitat cross-check before calling the planning inventory complete

For each HAB row, record:

1. Substrate and geological setting.
2. Sediment, soil profile and organic surface.
3. Water regime and salinity, where relevant.
4. Canopy, understory, grass/herb and cryptogam/aquatic cover as appropriate.
5. Decomposers, animal-material inputs and selected ecological interactions.
6. Seasonal, wet/dry, frozen/thawed and disturbed/recovering forms.
7. Natural products, resource acquisition and depletion/renewal rules.
8. Boundaries with neighboring habitats, including riverbanks, shores, cliffs and treeline.

Cross-check representative combinations rather than every mathematically possible combination. Mark unrepresented roles, deliberate omissions and outside-scope habitats explicitly.

## Planning closure checklist

All unchecked items concern **planning completeness**, independently of implementation status.

- [ ] Preserve and map every original rock/tree catalog entry; document aliases, grouped entries and proposed expansions.
- [ ] Give every review entry a disposition and rationale; identify any further gaps discovered during review.
- [ ] Define named or grouped soil, clay, grass, understory, organic, sediment, fungi/cryptogam and aquatic catalogs comparable in clarity to the existing rock/tree catalogs.
- [ ] Give every selected entry a complete applicable planning record and an explicit natural/decorative/gameplay role.
- [ ] Complete the habitat matrix across the climates, regions and underground/coastal environments selected for scope.
- [ ] Complete state/transition rules; resolve chemistry, grade, grain-size, form and alias overlaps.
- [ ] Separate geological history and generated appearance from changes expected during gameplay.
- [ ] Resolve material renewability, timescales, natural-versus-placed protection and unloaded-area policy at the design level.
- [ ] Cross-reference progression, farming, forestry, water, animals, building, processing and presentation without creating competing catalogs.
- [ ] Record deferred/excluded content and accepted simplifications instead of silently deleting gaps.
- [ ] Review regional/biological/geological coverage against primary sources and owner priorities.
- [ ] Obtain a planning review before treating the inventory as the agreed content scope. Keep implementation work separate until requested.

## Research anchors and limits

These sources help distinguish overlooked categories. The game representation and coverage choices above are our design proposals, not recommendations made by the source organizations. Sources are not asset/data licenses for importing material into the project.

- [USDA NRCS — A Soil Profile](https://www.nrcs.usda.gov/resources/education-and-teaching-materials/a-soil-profile): horizons and profile variation; supports separating soil layers from bedrock and texture.
- [USDA NRCS — Soil Texture Calculator](https://www.nrcs.usda.gov/resources/education-and-teaching-materials/soil-texture-calculator): sand/silt/clay composition and texture classification.
- [British Geological Survey — Rock Classification Scheme](https://www.bgs.ac.uk/technologies/bgs-rock-classification-scheme/): hierarchical geological naming and the need to distinguish material families, qualifiers and deposit classes.
- [USGS — Environmental Characteristics of Clays and Clay Mineral Deposits](https://pubs.usgs.gov/info/clays/): clay mineral/deposit distinctions and origins. The indexed source excerpt was available during this audit; direct page retrieval returned 403, so detailed mineral properties require a later accessible primary reference.
- [USDA Forest Service — Ecosystem processes related to wood decay](https://research.fs.usda.gov/treesearch/54797): downed/standing wood, stumps, litter, duff, organic inputs and habitat functions.
- [USGS — Biological Soil Crusts](https://www.usgs.gov/centers/southwest-biological-science-center/science/science-topics/biological-soil-crusts): soil-surface communities that extend coverage beyond ordinary grass.
- [NOAA — Estuarine Habitats](https://oceanservice.noaa.gov/education/tutorial_estuaries/est06_habitats.html): coastal habitat and substrate diversity, including marshes, mangroves, mudflats and rocky shores.

Species lists, exact physical/chemical values, real-world distribution maps, numerical rates and health/hazard mechanics need their own later research. This planning audit does not claim those details have already been verified.
