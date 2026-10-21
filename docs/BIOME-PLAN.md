# Deep Primal — biome plan

Planning draft: 2026-10-21. Source baseline: main commit `9ee0f1841ddb0e4aa9b0ece1bdf5f4997dde7dd2`.

**Confirmed owner direction, 2026-10-21: biomes first; climate scenarios afterward.** Define the biome roster and its requirements, then design climate scenarios to support those biomes. An existing climate grid must not dictate or prune the roster. **Climate must gate placement:** each biome can generate only where its relevant long-term climate requirements are met, alongside its terrain, water, substrate and other habitat requirements. Environmental implementation remains deferred. This update supplies named profiles, qualitative placement/material/community requirements and coverage checks. It does not create runtime biome entries, change generation, add textures, or complete an implementation milestone.

The existing [roadmap](../roadmap.md), [design reference](design-reference.md), [owner decisions](design-decisions-2026-10-18.md), [natural type catalogs](NATURAL-TYPE-CATALOG.md) and [environmental coverage inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md) remain linked sources of scope and requirements. Deep Primal remains the product and its integrated built-in engine is the target.

## The concrete planning roster

| Planning layer | Size | Named list |
| --- | ---: | --- |
| Proposed broad biome profiles | 118 across 19 families | [BIOME-CATALOG.md](BIOME-CATALOG.md) |
| Named regional/community variants | 51 | [Variants](BIOME-VARIANTS-AND-COVERAGE.md#regional-community-variants) |
| Named local forms, habitats, states and modifiers | 55 | [Environmental layers](BIOME-VARIANTS-AND-COVERAGE.md#environmental-layers) |
| Existing habitat entries explicitly reconciled | 62 of 62 | [Coverage crosswalk](BIOME-VARIANTS-AND-COVERAGE.md#existing-habitat-crosswalk) |

These counts are different axes and must not be added together as registered biome IDs or claimed species counts. The 118 profile keys are **proposed registration candidates**. Some may later merge or become variants after visual, performance, gameplay and API review; record any such decision explicitly. Registry capacity alone is not the reason to split a biome.

## Scope and identity

Plan natural Overworld surface, inland water, coastal, marine and subterranean environments. Species-by-species global ecoregions, human-made towns/plantations, new dimensions and fantasy ecosystems are not silently added by this natural-world roster. Existing content/dimension policies retain their own scope.

A biome profile should identify a persistent, meaningful ecological setting: canopy/ground-cover structure, terrain, materials, hydrologic role, community behavior and distinct environmental presentation. Climatic terms in names describe the intended biome; matching scenarios and numerical ranges are designed afterward. A material, temporary weather state or small landform does not automatically need another biome ID.

Use separate, connected planning layers. This table describes responsibilities, not runtime generation order:

| Layer | Owns | Example |
| --- | --- | --- |
| Biome profile | Broad ecological setting, defined first | Temperate Deciduous Forest |
| Relief and geology | Continents, mountains, valleys, rock bodies and sediment history | Folded range, granite body, carbonate bedrock |
| Hydrology | Catchments, channels, lakes/outlets, groundwater and salinity | Connected river from headwaters to estuary |
| Biological province/community | Compatible regional species pools and dominant communities | Oak–beech versus maple–linden expression |
| Ground profile/material | Soil horizons, composition, texture, organic cover and deposits | Gravelly topsoil over weathered parent rock |
| Local habitat/landform | Fine forms and environmental pockets | Spring seep, scree patch, forest gap |
| Time/state | Seasons, wetness, freezing, damage and recovery | Autumn canopy, wet soil, snow, burn scar |
| Supporting climate scenarios — later | Long-term temperature, effective moisture, seasonality and exposure that support the selected biomes | Cool moist coast versus cold dry continental basin |

Example: **Temperate Deciduous Forest + oak–beech community + locally derived loamy soil + limestone geology + a wet river hollow**. This does not require a separate ID for every multiplication of soil color, rock type, tree species and season.

## Deferred climate and placement record

Complete biome identity, coverage and community design first. After that roster is reviewed, derive the climate scenarios needed to sustain it, including seasonal patterns and coherent neighboring regions. Support more than one scenario where a biome calls for it; resolve any conflicts through an explicit design review rather than silently removing biome variety.

The fields below are a later integration record. Existing qualitative terrain, substrate, water and community descriptions inform biome design now; numerical climate thresholds and climate-model choices are deferred and are not prerequisites for completing the roster.

| Input/field | Required decision |
| --- | --- |
| Long-term temperature | Suitable annual/seasonal conditions and extremes, plus how elevation and regional climate are sampled. |
| Effective moisture/aridity | Water balance or a bounded proxy that accounts for climate, exposure and available moisture; precipitation alone is not soil saturation. |
| Seasonality | Temperature and wet/dry variation; permanent biome classification reads long-term conditions, while current season changes appearance and biology. |
| Relief | Slope, relative elevation, topographic position, exposure, valley/ridge relationship and climatic treeline; no universal hardcoded mountain Y. |
| Hydrologic eligibility | Inland/coastal/ocean/cave setting, channel connectivity, discharge, flood frequency/hydroperiod, lake spill level, groundwater dependence and water depth. |
| Salinity/chemistry | Fresh/brackish/saline eligibility and substrate/source chemistry; clarity/elevation do not imply drinking safety. |
| Substrate | Host geology, sediment history, soil/profile depth, drainage, organic accumulation and actual material catalog references. |
| Light/vertical setting | Surface, cave entrance, dark cave, water column and seabed; one surface biome must not overwrite every vertical habitat. |
| Biological province | Coherent connected regions with compatible species pools; avoid scattering every similar-climate tree from the whole world into one forest. |
| Patch scale/rarity | Regional area, minimum patch scale, transition width and rare-site rules; tune and measure later. |
| Stable identity | Proposed name/key, parent/variant relationships, tags, existing-world mapping and generation-data version. |

### Geographic climate realism — confirmed direction

The later climate plan must have a northern polar region, an equatorial region and a southern polar region. This applies to ordinary procedural worlds as well as the optional Earth mode.

- Use latitude as the broad thermal baseline: generally warmer near the equator and colder toward either pole, with gradual tropical, subtropical, temperate, subpolar and polar trends.
- Let elevation, ocean influence, distance inland, prevailing moisture transport and rain shadows produce coherent regional variation. Mountain climates can be cold even at low latitudes.
- Model moisture and seasonality alongside temperature so latitude allows different suitable communities and water regimes within a region. Climate zones should support varied biome mosaics and gradual transitions.
- Use hemisphere-aware seasons: northern and southern seasonal cycles are opposite, with regional differences in seasonal strength.
- Choose world-coordinate mapping, equator/pole positions, climate-region distances and behavior beyond polar regions during later climate planning. Procedural worlds do not inherit Earth's 1:1 scale automatically.

These are requirements for future climate scenarios and placement eligibility. Biome roster design remains first; a biome's inclusion does not authorize placement outside its relevant climate and habitat requirements.

### Design work order

1. Review and expand the named biome roster, resolving missing environments and profile/variant/layer distinctions.
2. Define each biome's identity, terrain, geology/soil, water setting, vegetation, animal/habitat roles and useful resources.
3. Develop regional/community variants, local layers and intended neighboring-biome relationships. Describe the desired transitions qualitatively.
4. After biome planning, design the climate scenarios that support the selected biomes and their communities. Work out temperature, moisture, seasonality, exposure and regional transitions at that stage.
5. Then settle numerical ranges, world-placement rules, size/frequency tuning and runtime sampling architecture against the agreed biome and climate plans. Climate eligibility gates biome placement; it does not decide which biome types belong in the catalog.

This is the content-design sequence. Runtime dependency ordering will be resolved during later integration; this planning correction does not change the current generator. Shared watershed connectivity, deterministic regional sampling and agreement between detailed terrain, maps and LOD remain requirements.

Desired transitions should preserve recognizable biome identity without random checkerboards. Later climate scenarios should support the intended forest–woodland–grassland–semidesert relationships; coastlines, cliffs, water boundaries and geological contacts can have sharper boundaries.

## Water, shoreline and vertical priorities

- Headwater, mountain, foothill and lowland reaches share one routed river system. River habitat selection must not invent disconnected channel noise.
- Rapids, cascades, waterfalls, bars, gorges and deltas come from the same bed/bank/discharge/deposition plan, with actual solid support checks reserved for implementation validation.
- Wetland identity follows water regime and vegetation structure. Do not treat every wet location as the same swamp or every organic surface as deep peat.
- Springs and oases need an identified source; moving to another chunk cannot reset aquifer supply.
- Lake beds, shore belts, marshes and spillways agree on water level and connectivity. Seasonal ponds, temporary rivers and permanent waters need different source/storage policies.
- Estuaries connect rivers to the sea with separate salinity and sediment considerations. Mangrove, salt-marsh and tidal-flat placement must respect climate and inundation eligibility.
- Ocean water-column and seabed profiles can occupy different Y bands. Reefs/seagrass/kelp cannot be assigned merely because the surface says “ocean.”
- Cave habitats use light, moisture, connection, geology and heat. No sunlight-dependent forest appears in a sealed dark cavern. Crystals, speleothems, guano and lava-tube shapes are not substitutes for habitat logic.
- Tide, erosion, glacier movement, live succession and complete environmental chemistry remain separately scoped systems. A planned profile or generated history does not imply those live simulations exist.

## Materials, vegetation and natural functions

Use the [geology/soil/clay list](NATURAL-TYPES-GEOLOGY.md) and [biology/organic list](NATURAL-TYPES-BIOLOGY.md) as the material/name sources. Ground-theme codes in the roster select compatible profiles; they do not duplicate material catalogs or force a single soil texture throughout a biome.

Every biome card must eventually specify:

- representative trees/canopy, shrubs, grasses/herbs, fungi/cryptogams, aquatic cover and decomposer roles where relevant;
- source geology, actual soil horizons, texture/chemistry, deposits and organic surface;
- seasonal vegetation stages, seeds/propagules, litter, deadwood, disturbance and recovery interfaces;
- fauna/habitat hooks and fish-water compatibility without assuming all species occur in all suitable climates;
- useful natural resources, harvest/depletion/renewal routes and environmental constraints;
- naturally sparse or absent components explicitly, rather than placing all material families everywhere.

Deposits such as clay, salt, sand and ore follow geology/transport history. Biome influences exposure, weathering, vegetation and accessibility; it is not the sole resource lottery.

## Survival and spawn planning

The first-day tool paths (flint/loose rock/bone), suitable fiber, tinder, fuel, water access and shelter remain connected to the existing primitive-survival design. Not every biome needs every resource.

Proposed spawn policy for later review: default starts prefer recoverable temperate/open woodland or meadow-edge settings with reachable starter resources. Polar deserts, deep water, sealed caves, active geothermal extremes and other severe starts require an explicit challenge-start policy or tested recovery route. This is a proposal, not a new locked default.

For each profile, fill the following later planning fields without adding balance numbers now:

| Gameplay field | Required content |
| --- | --- |
| Early resources | Which source materials can be found locally and which require movement or trade. |
| Seasonal pressure | Cold/heat/wetness, fuel needs, growing window and food availability. |
| Travel | Visibility, canopy density, ground wetness, slopes, crossing opportunities and cargo-transport constraints. |
| Water/food | Accessible source families, required treatment/handling, forage and fishing/hunting habitat hooks. |
| Building | Suitable terrain, drainage, supporting ground, timber/stone access and optional environmental hazards. |
| Progression | Why later tools, storage, clothing, transport or local industry improve living there. |
| Fairness | Readable environmental cues, viable escape/recovery and prevention of circular resource dead ends. |

## Visual and asset direction from this conversation

- **World block/item texture tiles: up to 64×64.** This records the current planning ceiling; dedicated UI art has a separate existing design context.
- Use shared neutral/grayscale texture families with per-material palettes/tints. Soil patterns can distinguish crumbly loam, granular sand, compact clay-rich surfaces and fibrous organic ground.
- Rock grain/fracture, wood bark/grain, canopy forms, soil layers, grass structure and litter should distinguish material families as well as colors.
- Grass, stones, litter, roots, wetness and snow can use appropriate separate masks/overlays/state appearance. The exact renderer representation is still a design choice.
- Do not create a separate image set for every biome × soil × rock × species × season combination.
- Preserve coherent material, water and canopy appearance across nearby chunks, biome boundaries, depth bands and the built-in LOD viewer.

## Reference roles

| Reference | What this plan takes from it |
| --- | --- |
| TFC | Geology/climate grounding, regional resources, species eligibility, seasons and rivers as explicit design references. |
| Terralith | Natural landform/biome variety and deliberate local environmental composition. |
| Tectonic | Continental relief, mountain/valley scale and connected terrain context. |
| Gaea / World Machine | Connected drainage/outlets, discharge-linked channels, erosion/deposition summaries and terrain-derived material masks. |
| IUCN Global Ecosystem Typology | Completeness check across environmental realms and functional ecosystem roles; this game roster is not a reproduction of its classification. |
| NOAA habitat material | Check coastal/aquatic coverage beyond “ocean” and “beach,” including wetlands and marine vegetation/biogenic habitats. |

This document adds no dependency, imports no third-party code/assets/maps, and makes no claim that the referenced methods are already implemented. Source/licensing and exact-version code audits retain their existing requirements.

## Future performance and saved-world constraints

- All profile/community/material sampling must remain deterministic in world coordinates, including negative coordinates, region edges/corners, worker order and cache eviction.
- Use shared bounded regional summaries and cheap point queries; no global scans or full distant-chunk generation just to answer a biome query.
- Choose biological communities from eligible pools with reusable templates, not unbounded registration or per-block object graphs.
- Keep registry definitions/profile selection stable within a saved generator version. Seasons and wetness change states without reassigning registry IDs each tick.
- Numeric runtime registry IDs are not persistent design identifiers; use names/keys and explicit version/migration mapping.
- Distinguish existing-world retrogen, new chunks and palette/property updates. No automatic replacement of explored/placed terrain is authorized here.
- Match full-detail and LOD environmental summaries, including edits and changed cover through the existing cache/invalidation contract.
- Measure registry/resource loading, biome point-query cost, chunk readiness, memory/network size, dense vegetation and mixed vertical habitats. No hardware performance gain follows from this plan.
- Keep all biological/material state and generation choices server-authoritative where they affect gameplay; client presentation consumes synchronized data.

## Planning and later implementation gates

Current biome planning review:

- [ ] Review the 118 proposed base-profile names, investigate missing environments and resolve merge/split/disposition decisions.
- [ ] Review all 51 named community variants and 55 local layers.
- [ ] Complete biome identity, terrain, geology/soil, water setting, actual material references, vegetation/life and natural resource functions.
- [ ] Define coherent regional biological pools and review named species/functions against primary sources; record climate questions for the later scenario phase.
- [ ] Review spawn/progression needs and explicit severe-biome exceptions qualitatively.
- [ ] Describe intended neighboring biomes, transitions and rare-site roles without requiring numerical climate or placement tuning.
- [ ] Preserve the 62-entry habitat crosswalk and record any further discovered gaps.

Deferred climate and world-placement planning, after the biome roster:

- [ ] Derive the climate scenarios required by the selected biome profiles and regional communities, with equatorial and northern/southern polar regions, coherent latitude trends and hemisphere-aware seasons.
- [ ] Define temperature, effective moisture, seasonal patterns and exposure, with quantitative ranges and scientific tolerance review; use each biome's relevant requirements as placement gates.
- [ ] Reconcile climate scenarios with connected water systems, relief, geology and intended biome transitions.
- [ ] Choose numerical water/scale fields, transition widths, default frequency and future configuration scope.
- [ ] Settle runtime sampling/placement dependencies and document any agreed changes to the biome plan.

Future implementation acceptance, only after authorized:

- [ ] Register selected profiles and connect the common sampler without altering frozen saved-world profiles.
- [ ] Validate clean startup, world creation, dedicated-server join, save/reload and optional-content configurations.
- [ ] Use fixed-seed surveys across all selected climate/realm families and mixed vertical settings.
- [ ] Check transitions and water connectivity across region edges/corners and generation orders.
- [ ] Inspect terrain support, lake spillways, river confluences, coast profiles and cave entrances.
- [ ] Verify no artificial biome walls, indiscriminate tree scatter or climate-incompatible biological mixtures.
- [ ] Compare actual materials/vegetation/water and edited-world LOD at matching locations.
- [ ] Pass named-hardware worldgen, memory, loading and network budgets; record limitations honestly.

## Primary research anchors

The ecological examples and game architecture above are our planning inferences. These references anchor classification/coverage distinctions; they do not endorse this game taxonomy or validate every candidate species/property.

- [IUCN Global Ecosystem Typology](https://iucn.org/resources/conservation-tool/iucn-global-ecosystem-typology): hierarchical functional/compositional classification across environments.
- [IUCN Global Ecosystem Typology 2.0](https://iucn.org/resources/publication/iucn-global-ecosystem-typology-20): ecosystem profile/classification reference.
- [NOAA Estuarine Habitats](https://oceanservice.noaa.gov/education/tutorial_estuaries/est06_habitats.html): coastal habitat/substrate and flooding/salinity distinctions.
- [NOAA Estuary Habitat](https://www.fisheries.noaa.gov/national/habitat-conservation/estuary-habitat): estuarine habitat diversity.
- [NOAA marine ecosystem overview](https://sanctuaries.noaa.gov/visit/ecosystems/): marine vegetation, reef, substrate and deep-water coverage.

Biome/material/species/landform lists remain expandable. This is a comprehensive first connected game-biome plan and a reconciliation of the existing habitat roster, not a claim that every scientific ecosystem or regional ecoregion has been enumerated.
