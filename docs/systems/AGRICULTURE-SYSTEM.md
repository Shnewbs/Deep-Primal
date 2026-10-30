# Deep Primal Agriculture System

**Edition:** 2026-10-29, America/Los_Angeles. **Status:** research-backed proposed specification; no agriculture implementation or release qualification is claimed. **Purpose:** connect native farming, selective breeding, soil, finite water, seasons, food and storage into one original system.

## 1. Confirmed foundation and proposed scope

Existing [design-reference §§4.1, 4.2, 4.6](../design-reference.md#s4-6), [owner decisions: Time and Resource renewal](../design-decisions-2026-10-18.md#time-sleep-and-server-behavior), and [roadmap M2–M5](../../roadmap.md) already establish seasonal planting, nutrient/fertilizer cycles, crop rotation/seed recovery, multi-group nutrition, food decay/preservation, shared calendar, native rules and server authority. Farming remains broader gameplay work after rendering/worldgen foundations; this document does not silently add the whole catalog to v0.1.

The latest owner instruction removes vanilla mobs. Farming must work without vanilla villagers, bees, skeleton bonemeal, wandering traders or their drops. Wind/self-pollination and manual assisted pollination provide complete routes; future original wildlife/pollinators are optional ecological benefits. Wild seeds, forage, crop residues and mineral inputs provide a viable first farm before original livestock manure exists.

Everything below marked **proposed** is a recommended implementation contract, not an already approved tuning choice. Species lists, physiological values, recipe yields, genetics balance and release placement need data review. The intended loop is observe season/soil → prepare bed → plant suitable seed → manage water/nutrients → optionally select a breeding population → harvest produce or viable seed → preserve food → recycle accountable residues and rotate.

## 2. Exact references and adoption decisions

AgriCraft here means **[Vivoxti/AgriCraft-ReReloaded](https://github.com/Vivoxti/AgriCraft-ReReloaded)**, not an unspecified older AgriCraft. GitHub metadata identifies its direct parent as coredex-source/AgriCraft-Reloaded and ultimate source as AgriCraft/AgriCraft. Reviewed branch `1.21.1` is pinned to [836f911b096a25de343cf7668430b829b2d12d63](https://github.com/Vivoxti/AgriCraft-ReReloaded/commit/836f911b096a25de343cf7668430b829b2d12d63), declaring **4.0.24**, Minecraft **1.21.1**, NeoForge **21.1.226** and Fabric support in [gradle.properties][Ameta]. The pin's [MIT license][Alicense] requires its notice if substantial code is reused. This design proposes original implementation, not code or artwork copying.

TerraFirmaCraft is reviewed specifically against the **1.21.x branch targeting Minecraft 1.21.1**, pinned [72750e42f3e0e8b337400ff1b8468a7ff7bc786d][Tpin]. Its build declares NeoForge 21.1.234/Java 21 and its license is EUPL1.2; it is a design reference, not a Deep Primal dependency. Full path/version evidence and limitations are in [AGRICULTURE-RESEARCH.md](AGRICULTURE-RESEARCH.md).

| Concern | Evidence from the selected references | Deep Primal decision |
| --- | --- | --- |
| Climate and crop growth | TFC CropHelpers evaluates climate/hydration and elapsed calendar growth; nutrient availability affects growth/yield [Tcrop] | **Adapt:** integrated calendar/environment progression with visible limiting factors |
| Soil fertility | TFC FarmlandBlockEntity tracks N/P/K; AgriCraft ordinary soil definitions expose humidity, acidity and a nutrient category [Tsoil][Asoil] | **Adapt TFC:** finite N/P/K pools; keep pH, structure and moisture distinct |
| Irrigation | TFC hydration uses rainfall and nearby hydrating fluid; AgriCraft has stored-water channel/sprinkler machinery [Thyd][Asprinkler] | **Adapt architecture:** actual accepted finite-water transfers into soil; reject radius-based free hydration and bonus random ticks |
| Genetics | Exact AgriCraft fork has paired alleles, six traits, selected mature parents, explicit species mutations and clone rules [Agenes][Across] | **Adapt:** inherited varieties and analysis; reject six universally increasing stats as the sole farming objective |
| Weeds | Current CropBlockEntity grows/spreads weeds and can regress a crop; pests are not established by this inspected path [Acrop] | **Adapt optionally:** local competition and seedbank; pests/disease are separate proposals |
| Greenhouses | Exact fork recognizes enclosures and can bypass seasons with a small growth modifier [Agreen]; TFC guide's greenhouse refers to Firmalife, not base TFC | **Adapt shelter mechanics:** light, thermal and ventilation balance; reject blanket season immunity |
| Food/orchards | TFC provides food decay/preservation and seasonal fruit/berry systems [Tfood][Tfruit] | **Adapt:** food lots, perennial phenology and storage; native definitions/art |
| Automation | AgriCraft's native Starfield integration has separate calendar/water/quality rules, distinct from converted ordinary crops [Areadme] | **Adopt distinction:** one native agriculture authority; optional adapters cannot run a second lifecycle |

The fork's wiki guides at this pin describe a **4.0.20** baseline. Current **4.0.24** source/changelog takes precedence for changed behavior: the optional Mystical Agriculture one-parent cloning switch defaults off, and unsupported clipping does not reset growth [Achanges]. Croptopia integration covers farmland crops rather than fruit trees; Starfield Pastoral support is an optional, exact-version NeoForge integration. Those are adapter scopes, not universal native features. The two inspected external season plugins return `ANY`, so naming an adapter is not evidence of enforced seasons [AseasonN][AseasonF].

## 3. Proposed calendar, climate and crop lifecycle

**AG-01 — Time contract.** Use the shared server calendar only. No elapsed real-time simulation while the server/world is shut down; default empty-server calendar pause follows the existing decision, with a server override. One logout does not pause time if other players remain. Save calendar epoch, processed interval and policy revision. Day-length changes rescale calendar-unit durations, not duplicate completed work.

Keep growth, water transport, decay and machine processing separate consumers. Loaded plant growth advances on admitted calendar intervals. For initial release, unloaded crop physiology pauses with no deferred harvest production. Food aging may use bounded calendar elapsed-time evaluation because it consumes no machine inputs; this must be explicit and cannot extend to free unloaded irrigation/heating/harvesting. Sleep/time commands use a defined bounded calendar-advance path: no crop receives water or heat from an interval for which inputs were never provided.

**AG-02 — Species physiology.** Model germination, vegetative, flowering, seed/fruit filling, mature, senescent/dead, plus dormancy/regrowth for perennials. Species declare base/optimal/lethal temperature bands, thermal-time requirement by stage, moisture/oxygen response, salinity tolerance, photoperiod where meaningful, planting/root depth, frost exposure and harvest mode. Planting windows are guide forecasts from regional seasonal conditions, not an unexplained month-number prohibition.

Proposed growth increment:

```text
potential = thermalOpportunity × lightFactor × speciesStageRate × genotypeResponse
accepted = min(potential, waterSupported, nutrientSupported, biomassCapacity)
```

These factors are game-model quantities with declared units, not a claim of agronomic accuracy. Integrate over bounded intervals and retain fixed-point remainders. Nutrient deficiency may slow growth/reduce yield before death; drought/flood/frost severity uses accumulated exposure rather than arbitrary immediate destruction. An unsupported/pending climate query pauses the affected update and reports pending; it is not ideal weather.

A growth-speed trait shortens physiological opportunity requirements only within balanced limits. It cannot advance a crop at zero light, spend unavailable nutrients or ignore lethal temperature. Waterlogging limits root aeration; flooded rice-like species use a distinct profile. Species models need not all share the same annual lifecycle.

**AG-03 — Native content pilot.** Proposed first data set: a cool-season cereal, warm-season cereal, pulse, root vegetable, leafy vegetable, fiber/oil crop, berry perennial and fruit tree. Assign actual named plants only after climate/community/catalog reconciliation. Test at least two latitudes, highland and dry basin, including bootstrap forage alternatives. No location should require imported late-game greenhouse equipment merely to avoid a permanent food dead end.

## 4. Proposed soil fertility, rotations and finite irrigation

**AG-04 — Soil state.** Farmland inherits its actual horizon, parent material, texture, depth and drainage; tilling changes structure and surface cover, not underlying geology. Store plant-available N/P/K, organic matter, pore-water owner ID, salinity, pH class, compaction/cover, microbial/fixation policy and previous crop-family history. N/P/K use documented integer game nutrient units; they are not mislabeled kilograms or real laboratory concentrations.

Growth debits nutrients into plant biomass. Harvest exports its allocated fraction; residues retain the remainder. Compost/mineral fertilizer/manure input moves declared nutrient amounts; decomposition mineralizes existing organic pools. Crop residues cannot produce more compost nutrients than they contained. Weathering is a bounded, explicitly declared geological input; nitrogen fixation, if enabled for compatible pulses, is an explicit atmospheric-N input conditioned on growth/soil, with costs and no creation of P/K. Do not award all nutrients merely for alternating crop IDs.

Rotation is useful through complementary demand, cover/residue management, rooting and optional family-specific pressure. Fallow replenishes only through modeled inputs/mineralization and reduces competition; repeatedly hoeing/replacing a farmland block does not reset fertility. Soil transport preserves stocks or uses a declared material-conversion policy. Excess fertilizer remains stored, leaches into accounted drainage or produces a declared loss; it is never silently clamped.

**AG-05 — Water contract.** Irrigation requests physical water from canal/container/pipe/aquifer authorities through a qualified adapter to the existing water authority and the proposed shared transaction contract. Credit the soil pore-water owner only for accepted delivery, carrying salt and contaminants. Transpiration/evaporation returns measured water to the atmosphere; infiltration/drainage moves it into named lower stores. Rain collection uses atmosphere-paid precipitation and catchment area once, not one free source per sprinkler.

Root access is a bounded footprint with explicit overlap: several plants cannot each claim the same soil quantum. Demand is allocated fairly under scarcity. Channels have real storage and hydraulic/capacity constraints; pumps need progression-appropriate power. Sprinklers distribute measured water over a bounded pattern, blocked by cover. Particles are visual and never pay for hydration. Persist partial sweep progress/reservoir balances; disable growth-speed bonuses that grant extra biology merely for an irrigation pass.

Proposed convenience options: hand-watering and furrow routing first; optional assisted distribution still debits the same quantity and exposes its efficiency. No universal freshwater radius, rain refill while time is paused, infinite two-source water or dry farmland hydration from an empty decorative canal.

The inspected AgriCraft sprinkler uses integer per-active-tick consumption with a minimum quantum for positive settings; its configured “per-second” label does not exactly describe all low values [Asprinkler]. Deep Primal instead stores fractional rate remainder and tests requested-versus-accepted delivery over time. Do not transplant its numbers into Deep Primal's water ledger. Existing native cells use sixteen saved volume units per block; any proposed finer internal precision must define an exact, versioned conversion and retained remainder without silently changing that saved representation.

## 5. Proposed selective breeding, seeds and competition

**AG-06 — Varieties and genome.** Keep species identity, genotype, expressed phenotype, seed viability and analysis knowledge separate. A seed is not a consumable stat-upgrade token. Use a small diploid allele set initially: phenology, edible allocation, water-use response, nutrient-use response, stress tolerance and reproductive behavior. Trait effects have costs/tradeoffs: drought tolerance can reduce optimal yield; faster maturity may lower biomass; greater fruit allocation can reduce seed or residue. All bounds are species-specific.

Define dominance/additivity explicitly per locus. Offspring inherit one allele from each valid parent. Mutations use bounded, versioned distributions and a deterministic address from world/plant IDs, reproductive event, locus and offspring ordinal. Never reroll by reopening the analyzer, saving, reconnecting or moving the same seed. Species crossings need an explicit compatibility rule and viable offspring definition; adjacent unrelated crops do not invent a new species or ore-producing plant.

Controlled breeding beds and manual pollen transfer make parent choice legible. Optional marker supports serve layout/inspection; exact AgriCraft single/double-stick geometry is not a required copy. Parent maturity/flowering, pollen availability and compatible timing matter. Self-pollinated, wind-pollinated and assisted crops can all complete reproduction with vanilla bees absent. Any future original insect service is optional and sampled regionally rather than simulating every pollen particle.

Clonal propagation preserves genotype, pays for a cutting/tuber/scion and reduces available parent biomass/reserves. It grants no free genetic improvement and resets neither age nor disease by default. Sexual seed production spends an allocated reproductive budget; harvesting food early can reduce seed return. Starting survival still needs a reliable seed-saving path.

**AG-07 — Analysis and journal.** Initial inspection shows species, visible health and environmental blockers. A progression-appropriate native analyzer reveals allele pairs and confidence/measurement state without changing them; an early seed trial can reveal limited traits. Analysis consumes time/tool use only as configured, never consumes the seed without a declared recipe. Knowledge is player/team discovery state, not a client-editable “analyzed” permission. Seed packets expose lot ID, parentage, viability, harvest date and known traits; journal comparisons retain provenance.

**AG-08 — Weeds and optional crop pressure.** Weeds have an original local seedbank, germination conditions and bounded growth; they compete for admitted light/water/nutrients. Mulch, rotation, dense canopy and removal matter. Weeds do not spontaneously destroy every perfect crop through a constant world-tick roll. Removed biomass enters an accounted residue/waste output, with seed-bearing compost treatment explicit.

Pest/disease systems are **proposed later scope**, not features established by the inspected TFC/AgriCraft evidence. If adopted, use species/family-specific regional pressure, host/season/moisture, visible symptoms, propagation caps and treatment costs; no unbounded flood-fill or mandatory pesticide loop. Vanilla-mob removal leaves no dependency hole. Default early prototype can omit pests while testing weeds/rotation independently.

## 6. Proposed orchards, greenhouse and farm microclimate

**AG-09 — Perennials.** Berries/fruit trees retain age, structural biomass, dormancy/chill/flowering/fruit phases and reserved water/nutrients. Fruit has a bounded crop load and individual/aggregate mass; picking, pruning and breaking share one harvest state. Rootstock/scion and grafting are optional extensions, not automatically inherited from TFC evidence. Use original plant assets/species rules, climate-compatible establishment and real sunlight/root space.

Harvest removes fruit without copying the whole tree yield at each branch. A moved plant retains genotype/age but can suffer a transplant pause; repeatedly uprooting cannot refresh fruit or viability. Structures/player planting have provenance so wild regeneration does not overwrite a farm.

**AG-10 — Sheltered agriculture.** Reuse the shared shelter/heat/light service. An enclosure reduces wind/rain exposure and changes heat/humidity/ventilation within bounded physical approximations. Transparent roof transmission, shading, leakage/doors, thermal mass, water availability and optional fueled heat determine conditions. An enclosure is not an automatic year-round-season bypass or a five-percent magic multiplier.

Validate on edits, then cache a versioned bounded region; missing neighboring geometry returns pending. Proposed pilot maximum 4,096 interior cells, incremental validation at 256 cells/tick, at most one outstanding validation per structure. Larger agricultural halls require an explicitly different coarse model. Do not replace all air blocks or rescan every crop each tick. Freezing/summer overheating, condensation/rot risk and inadequate winter light provide meaningful limits; tune severity for playability.

## 7. Proposed harvest, food mass and storage

**AG-11 — Crop and food lots.** Plants accumulate dry biomass from a declared photosynthetic production input and water from accounted uptake. Food/seed/stem/residue fractions partition that available biomass; this avoids pretending that edible mass is created solely from seed mass. Yield traits alter allocation/efficiency within the budget. Better harvesting skill reduces waste, not creates matter.

Persist dry matter, water content, edible fraction, nutrient group contributions, safety/contamination, maturity quality, freshness/decay and genotype provenance independently. Quality is not weight, and high Gain is not a license for unlimited nutrients. Harvesting a root plant, repeat fruit crop and seed crop have distinct transitions. Manual and mechanical harvesting call one atomic API, reserving output capacity before spending the plant state; two players/machines cannot claim the same crop.

**AG-12 — Preservation and seed storage.** Food spoilage follows calendar time and storage temperature/moisture/exposure; cooling, drying, smoking, salting and pickling apply explicit native recipes. Processing partitions input mass/water/nutrients into products, evaporated water and waste. Sealed containers have capacity and contents, not universal immortality; cellars are useful under suitable conditions and may flood.

Food lots split/merge conservatively. Merging fresh and old food cannot reset the old fraction's age; retain sublots or conservatively use the more decayed state under a declared approximation. Moving inventory, changing item form, death/reconnect and container nesting cannot refresh freshness. Seed viability has its own moisture/temperature/calendar curve; a dry seed vault slows deterioration without making every seed immortal. A failed lot retains a defined compost/waste destination.

## 8. Proposed data, authority and persistence

```json
{
  "schema": 1,
  "id": "deepprimal:pilot_cereal",
  "definition_revision": 1,
  "family": "deepprimal:pilot_grass",
  "lifecycle": "annual_seed",
  "climate_profile": "deepprimal:cool_season_trial",
  "stage_requirements": "deepprimal:cereal_trial_v1",
  "nutrient_profile": "deepprimal:cereal_npk_v1",
  "root_profile": "deepprimal:shallow_fibrous_v1",
  "genome_schema": "deepprimal:diploid_pilot_v1",
  "harvest_recipe": "deepprimal:cereal_grain_straw_seed_v1",
  "pollination": ["self", "manual"],
  "art_family": "deepprimal:pilot_cereal"
}
```

This is a proposed schema example, not a registered crop or finalized balance. Referenced records declare units/ranges, validate cross-references and compile immutable native data once. Reject missing stages, impossible capacity, invalid allele ranges, negative outputs, recursive recipes and unknown units; report source pack/path/field. Scripts may author validated definitions or submit actions, not run unbounded per-plant callbacks.

Persist `PlantState` with world/dimension/plant UUID, definition/schema revision, accepted state revision, stage/progress, biomass, genome/parent IDs, reproductive-event counter, water/nutrient allocations and last processed calendar interval. `SoilPatchState` retains stocks/owner links/history; `SeedLot` and `FoodLot` retain quantities/provenance/aging; `FarmStructureState` retains dependency hash/coverage and effective microclimate.

**AG-13 — Server authority.** Clients request plant/water/analyze/harvest actions; server validates reach, ownership, inventory, crop revision and effective rules. Atomic transactions span plant/soil/water/output owners, with idempotent event IDs and durable exactly-once outcomes. Delayed callbacks from unloaded chunks or switched worlds are fenced. Unknown future schemas preserve bytes and become unavailable rather than regenerating ideal soil or resetting seeds.

Save dictionary-backed IDs and compact sparse states; no object or ticking entity per leaf/fruit/pollen. Datapack reload creates new definitions; existing plants retain their revision until a documented migration. Unsupported module disabling freezes/preserves owned state or converts through an explicit migration; it never deletes quantities or harvests everything for free. Optional machine/mod adapters are separately qualified, and no foreign callback may commit half an operation unnoticed.

## 9. Proposed performance, progression and acceptance

**AG-14 — Resource envelope.** Starting qualification workload: 65,536 crop roots, 16,384 active soil patches, 64 irrigation networks and 64 greenhouse records on a named dedicated-server host. Proposed admission budgets: at most 1,024 due roots and 256 soil patches/tick; 2 ms total agriculture p95 target under that workload, 5 ms p99 target; these are targets to measure, not promises. Cap dirty/network records and process oldest eligible work fairly. Publish oldest simulation lag, rejected/deferred actions, bytes by state type, greenhouse validation age and irrigation debit/credit totals.

Schedule by calendar opportunity and events, not every plant every tick. Reuse climate/soil samples by patch and interval; bound root/pollen neighborhoods. No force-loaded farms, wall-clock worker loops or huge login catch-up. When saturated, defer the interval coherently and expose lag, preserving inputs and avoiding partial biological progress. Visual growth, animations and particles can degrade independently.

| Phase / stable IDs | Deliverable | Required exit evidence |
| --- | --- | --- |
| **AG-01–05: foundation** | Calendar contract, native lifecycle pilot, soil and finite irrigation | Paused/offline invariance; defined units; closed water/nutrient ledger; realistic limiting factors |
| **AG-06–08: propagation** | Genetics, seed saving, analyzer/journal, bounded weeds | Deterministic inheritance/distributions; no reroll/clone exploit; original-mob-independent completion |
| **AG-09–10: perennial/shelter** | Berry/tree lifecycle and bounded greenhouse microclimate | Seasonal cycle, edited enclosure invalidation, no free heat/light or duplicate branch yield |
| **AG-11–13: harvest/storage** | Mass-aware lots, preservation and authoritative actions | Atomic multiplayer/automation harvest; food/seed split/merge and save/reload accounting |
| **AG-14: scale** | Integrated server, rendering/network and progression qualification | Named workload p50/p95/p99, lag/bytes, fairness and bootstrap playtest |

Acceptance is exact where discrete quantities are conserved and statistical where genetics is stochastic. Proposed gates:

1. **Calendar:** shutdown/restart for simulated months yields zero wall-clock growth; empty-server pause and other-player-online cases match policy; changing day length preserves calendar-unit maturity.
2. **Water:** 10,000 randomized irrigation/rain/drain/evaporation/harvest actions preserve every owner total; shared roots and overlapping sprinklers never double-credit. Saline irrigation retains salts after evaporation.
3. **Fertility:** three rotation cycles include recorded harvest export, residues, compost and explicit inputs; no hoe/block-replacement nutrient reset; fixation creates only declared N.
4. **Lifecycle:** cold, drought, flood, shade and appropriate seasons produce declared responses; no missing-climate fallback to ideal growth. All native-mob dependencies disabled still permit seed saving.
5. **Genetics:** fixed event IDs replay identical offspring across worker/chunk order and restart; sampled distributions fit declared confidence bounds; cloning preserves alleles and spends biomass.
6. **Harvest:** simultaneous manual/machine actions, full outputs, callback refusal and crash transitions yield one harvest or none, with no lost state. Seed and food mass never each claim the full plant.
7. **Lots:** repeated split/merge/cook/preserve/container transfers retain mass/nutrients and cannot refresh decay/viability; worst-case food age cannot improve from regrouping.
8. **Perennials/shelter:** full seasonal cycle, graft/transplant if enabled, broken roof/doors, missing chunks and reload retain valid phase/coverage and no infinite produce.
9. **Scale:** saturation, remote farms, storms, bulk harvesting, reconnect and datapack failure remain bounded; quiet farms eventually progress. Publish measured machine/config/results before claiming budget compliance.

No arbitrary grade or “complete agriculture” label follows from this study. Completion means these original contracts have native callers, saved compatibility, effective configuration, readable player feedback and the specified evidence.
 
[Ameta]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/gradle.properties
[Alicense]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/LICENSE
[Areadme]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/README.md
[Achanges]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/CHANGELOG.md
[Agenes]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/genetic/AgriGenePair.java
[Across]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/genetic/AgriCrossBreedEngine.java
[Acrop]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/block/entity/CropBlockEntity.java
[Asoil]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/docs/wiki/Soil-Irrigation-and-Greenhouses.md
[Asprinkler]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/block/entity/SprinklerBlockEntity.java
[Agreen]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/greenhouse/Greenhouses.java
[AseasonN]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/neoforge/src/main/java/com/agricraft/agricraft/common/plugin/SereneSeasonPlugin.java
[AseasonF]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/fabric/src/main/java/com/agricraft/agricraft/common/plugin/FabricSeasonPlugin.java
[Tpin]: https://github.com/TerraFirmaCraft/TerraFirmaCraft/tree/72750e42f3e0e8b337400ff1b8468a7ff7bc786d
[Tcrop]: https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/crop/CropHelpers.java
[Tsoil]: https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blockentities/FarmlandBlockEntity.java
[Thyd]: https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/soil/FarmlandBlock.java
[Tfood]: https://terrafirmacraft.github.io/Field-Guide/en_us/food/decay.html
[Tfruit]: https://terrafirmacraft.github.io/Field-Guide/en_us/the_world/wild_fruits.html

