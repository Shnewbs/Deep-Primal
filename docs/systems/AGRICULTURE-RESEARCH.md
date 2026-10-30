# Agriculture research ledger

**Inspected:** 2026-10-30 UTC / 2026-10-29 America/Los_Angeles. **Method:** official repository metadata, pinned documentation and source inspection. No reference mod was executed; no source/artwork was copied into implementation. Evidence supports the proposed [Agriculture System](AGRICULTURE-SYSTEM.md), not release qualification.

## Exact AgriCraft ReReloaded identity

The requested repository is [Vivoxti/AgriCraft-ReReloaded](https://github.com/Vivoxti/AgriCraft-ReReloaded). [GitHub repository metadata](https://api.github.com/repos/Vivoxti/AgriCraft-ReReloaded) returned fork=true, direct parent **coredex-source/AgriCraft-Reloaded**, ultimate source **AgriCraft/AgriCraft**, default branch **1.21.1**, public/nonarchived repository and project homepage for **agricraft-rereloaded**. Metadata was read through the GitHub connector; an external web fetch of pinned wiki pages was unavailable, so these findings rely on authenticated GitHub source retrieval.

[Branch1.21.1](https://api.github.com/repos/Vivoxti/AgriCraft-ReReloaded/branches/1.21.1) resolved to **836f911b096a25de343cf7668430b829b2d12d63**, dated 2026-10-19, message “Release 4.0.24 with configurable MA cloning and clipping safeguards.” All A-source links below use that commit, not current upstream AgriCraft.

[gradle.properties][Ameta] declares version 4.0.24, Minecraft 1.21.1, NeoForge 21.1.226, Fabric API 0.116.11+1.21.1/loader 0.19.2 and both platforms. A version declaration is not a build/run result. [LICENSE][Alicense] contains MIT with InfinityRaider 2014 notice; actual code reuse would require retaining notices and reviewing individual asset provenance. This task adopts no code/assets.

## A-source observations and limits

| ID | Pinned source | Observation | Limit / design consequence |
| --- | --- | --- | --- |
| A-01 | [README.md][Areadme] | Croptopia farmland-crop pack and optional exact-version native Starfield integration; differing native/converted paths | No universal fruit-tree conversion or all-platform Starfield parity; native original Deep Primal remains independent |
| A-02 | [CHANGELOG.md][Achanges] | 4.0.24 adds opt-in Mystical Agriculture cloning and clipping safeguard; 4.0.20 describes major Starfield work | Changelog reports author scope; not independent acceptance. Source checked for relevant rules |
| A-03 | [AgriGenePair.java][Agenes] | A genome locus contains two alleles; expressed trait comes from dominant allele | A useful game-genetics representation, not evidence of real botanical genetic fidelity |
| A-04 | [AgriCrossBreedEngine.java][Across] | Mature eligible parents are fertility-selected; two selected parents combine alleles; one-parent cloning requires species/config and spread conditions; clone mutation is configurable | Breeding is not simply any two crops placed together; current MA exception is verified separately from older guide wording |
| A-05 | [AgriMutationHandler.java][Amutation] | Species mutation queries explicit parent recipes; stat mutation chooses bounded allele changes; mutation-complexity cache can invalidate | Species recipes and trait mutation are different operations. Native data should separate compatibility, outcome and tradeoffs |
| A-06 | [CropBlockEntity.java][Acrop] | Server-side requirement/growth/weed branches; weed growth/spread; harvest trials from Gain; different clipping products; state serialization | Does not evidence pest insects, nutrient-element conservation or food-mass accounting. No claim of full class/runtime audit |
| A-07 | [StatsConfig.java][Astats] | Six bounded traits: Growth, Gain, Strength, Resistance, Fertility, Mutativity | The complete six-stat ladder is a reference design, not a requirement to copy names/balance |
| A-08 | [SprinklerBlockEntity.java][Asprinkler] | Stored water buffer, one-column sweep progression, loaded-chunk guard, moisture setting and optional growth ticks; buffer/progress saved | Continuous finite-water soil ownership is not proved by setting a moisture property. Positive low consumption settings use a minimum integer per active tick |
| A-09 | [IrrigationConfig.java][Airrig] | Explicit tank/channel capacity, rain fill, sprinkler rate/chance/interval tunables | Config labels alone do not prove dimensional accuracy, physical mass accounting or integration with Deep Primal |
| A-10 | [Greenhouses.java][Agreen] | Bounded enclosure flood fill, glass ceiling fraction, indexed boundary/interior state and saved structures; special greenhouse air | Adopt structure validation ideas; avoid automatic season bypass or air replacement in original microclimate design |
| A-11 | [GreenhouseConfig.java][Agreenconfig] | Default limit1024, ceiling glass0.4, season bypass true, growth modifier1.05 | Configurable gameplay shortcut rather than heat/light/ventilation simulation |
| A-12 | [SeedAnalyzerBlockEntity.java][Aanalyzer] | Server processing/inventory, seed analysis/conversion and persisted machine state | Analyzer is a discovery/interface reference, not permission for client genome edits |
| A-13 | [SereneSeasonPlugin.java][AseasonN] and [FabricSeasonPlugin.java][AseasonF] | Both inspected plugins claim a season function returning ANY | No enforcement of those external mods' seasonal restrictions is established by these adapters |

The inspected repository's [Genes and Balance guide][Aguidegenes] and [Soil/Irrigation/Greenhouse guide][Aguidesoil] explicitly identify **4.0.20** as their baseline. They distinguish ordinary converted crops from native Starfield daily crop/water/quality rules. Soil suitability includes humidity/acidity/nutrient categories and growth modifiers; this is not the same as TFC's N/P/K stock model. The [Getting Started guide][Astart] identifies analysis, magnifying-glass allele inspection, trowel relocation and breeding layout. Use pinned4.0.24 source for changed details, including opt-in MA cloning; do not reprint every old wiki formula as a current guaranteed mechanic.

## Existing Deep Primal constraints inspected

- [design-reference §§4.1,4.2,4.6](../design-reference.md): climate-specific crop/seed needs, rotation, shared calendar, seasonal windows, nutrition, decay and preservation.
- [design-decisions: Time](../design-decisions-2026-10-18.md#time-sleep-and-server-behavior): no shut-down wall-clock simulation; default empty-server calendar pause; subsystem catch-up requires separate verification.
- [design-decisions: Resource renewal](../design-decisions-2026-10-18.md#resource-renewal-and-waste--accepted-direction): seed recovery/fertility/compost/rotation, useful byproducts and original progression.
- [roadmap](../../roadmap.md): farming/ecology remains broader gameplay after foundations; WATER-06 farm-water choice is unresolved. Native original systems and server authority remain governing.
- [water requirements](../WATER-BUDGET-REQUIREMENTS.md): actual accounted finite volume, shared aquifers and conservative composition; no water creation through presentation or chemistry.
- Latest owner scope supplied for this task removes vanilla mobs. A mandatory vanilla-bee/villager/bonemeal route is therefore excluded.

## Research limits

Source presence establishes an implemented path at the pin, not that every optional module loaded, every recipe balanced or every edge case passed. This study did not reproduce AgriCraft test reports, execute TFC, qualify Deep Primal agriculture, source-audit Firmalife, or certify platform compatibility for code reuse. Missing evidence for a feature means “not established in inspected sources,” not a global claim that no mod/addon implements it.

All physiology, nutrient/water/biomass contracts, tradeoffs, native greenhouse model and AG-01–14 phases in the companion specification are **Deep Primal proposals**. Preserve the reference-specific observations below as evidence rather than silently treating them as native behavior.

[Ameta]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/gradle.properties
[Alicense]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/LICENSE
[Areadme]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/README.md
[Achanges]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/CHANGELOG.md
[Agenes]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/genetic/AgriGenePair.java
[Across]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/genetic/AgriCrossBreedEngine.java
[Amutation]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/genetic/AgriMutationHandler.java
[Acrop]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/block/entity/CropBlockEntity.java
[Astats]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/config/StatsConfig.java
[Asprinkler]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/block/entity/SprinklerBlockEntity.java
[Airrig]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/config/IrrigationConfig.java
[Agreen]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/greenhouse/Greenhouses.java
[Agreenconfig]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/api/config/GreenhouseConfig.java
[Aanalyzer]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/common/src/main/java/com/agricraft/agricraft/common/block/entity/SeedAnalyzerBlockEntity.java
[AseasonN]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/neoforge/src/main/java/com/agricraft/agricraft/common/plugin/SereneSeasonPlugin.java
[AseasonF]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/fabric/src/main/java/com/agricraft/agricraft/common/plugin/FabricSeasonPlugin.java
[Aguidegenes]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/docs/wiki/Genes-and-Balance.md
[Aguidesoil]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/docs/wiki/Soil-Irrigation-and-Greenhouses.md
[Astart]: https://github.com/Vivoxti/AgriCraft-ReReloaded/blob/836f911b096a25de343cf7668430b829b2d12d63/docs/wiki/Getting-Started.md

---


## TerraFirmaCraft 1.21.1 — Agriculture Evidence

**Inspected:** 2026-10-30 UTC. **Purpose:** primary-source comparison for an original Deep Primal system. No TFC code/assets are adopted and no mod was executed.

## Exact identity and limits

- Official repository: <https://github.com/TerraFirmaCraft/TerraFirmaCraft>.
- Resolved branch `1.21.x` at **`72750e42f3e0e8b337400ff1b8468a7ff7bc786d`**, commit dated 2026-10-26. [Branch query](https://api.github.com/repos/TerraFirmaCraft/TerraFirmaCraft/branches/1.21.x).
- [Pinned build metadata](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/build.gradle.kts): Minecraft **1.21.1**, NeoForge **21.1.234**, Java **21**; mod version comes from the build environment, otherwise `0.0.0-indev`. Do not call this branch snapshot exactly the released binary.
- [Release v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/tag/v4.2.12), published 2026-10-14, is explicitly titled TerraFirmaCraft 1.21.1-4.2.12. The inspected branch is later than that release. [Official API docs](https://terrafirmacraft.github.io/Documentation/1.21.x/) identify 4.x as Minecraft 1.21.1.
- [Pinned license](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/LICENSE.txt): EUPL-1.2. This evidence records the license, not a legal compatibility opinion. Deep Primal should independently author its mechanics.
- The unnumbered current English Field Guide was inspected, not `/18/` or `/20/` documentation. It includes explicitly labeled addon sections. It is a live generated guide rather than a commit-pinned binary manual; use pinned source for exact behavior.

## Source evidence ledger

### TFC-A01 — Crop lifecycle and fertility

[CropHelpers.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/crop/CropHelpers.java) implements elapsed game/calendar-based crop updates, endpoint temperature/hydration checks, separate growth/expiry/yield, signed nutrient requirements and nutrient-assisted growth/yield. A positive nutrient budget is consumed; negative requirements feed soil production. It uses random growth variation. This is not a claim that growth is deterministic across arbitrary update partitions. The code's nutrient assistance is not a hard universal “no fertilizer, no growth” rule.

[CropBlockEntity.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blockentities/CropBlockEntity.java) persists growth, absorbed N/P/K, yield, expiry and update/growth timestamps. This provides a useful separation of phenology from nutritional history.

### TFC-A02 — Crop player-facing behavior

[Current Crops guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/crops.html) documents wild seed acquisition, mature plants going to seed, and cereals/legumes/vegetables/cover crops with different nutrient consumption/replenishment patterns. Some crops need supports or waterlogging; rice uses freshwater, while climbing plants need a stick. Harvest forms differ between single/double-height, spreading and pickable crops. Exact plant thresholds and NPK tables belong to this version's data; do not import a 1.7.10 “one primary nutrient only” description.

### TFC-A03 — Hydration is a local suitability abstraction

[FarmlandBlock.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/soil/FarmlandBlock.java) derives a clamped 0–100 hydration value. Rain contributes up to 60; a nearby hydrating fluid contributes 40; appropriate underlying blocks multiply by 2 or 0.5. The water search checks offsets up to four blocks horizontally and one below, with a distance penalty below. A hydrating fluid above returns 100. This query contains no measured source-volume debit.

[Hydration guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/hydration.html) explains current and annual-range hoe readouts, seasonal moisture variation and annual-average suitability for fruit trees/bushes. **Adapt for Deep Primal:** retain readable suitability, but calculate it from finite soil-water ownership and metered irrigation. Do not adopt nearby-water presence as unlimited consumption.

### TFC-A04 — Soil quality differs from nutrient balance

[Soil Fertility guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/farmland.html) lists eight soil varieties with fertilizer/replenishment effectiveness spanning 0.8–1.2. It distinguishes soil quality from the NPK stock; ordinary replanting does not degrade that base quality in the described TFC system.

[FarmlandBlockEntity.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blockentities/FarmlandBlockEntity.java) stores N/P/K values and additional water, clamps nutrient values to 0–1, persists timestamps and emits combined sync when applying fertilizer. [IFarmland.java](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blockentities/IFarmland.java) exposes consumption and production. These are normalized gameplay nutrients, not kilograms of chemical elements.

### TFC-A05 — Fertilizer data and feedback

[Fertilizers guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/fertilizers.html) documents item application to farmland/crops, feedback particles and differing nutrient contributions from compost, bone meal, raw shellfish, saltpeter, guano, wood ash and sylvite. This supports distinct sources and useful soil feedback, not copying recipes or claiming biological conservation. Deep Primal must source any animal byproduct through its original fauna/material systems.

### TFC-A06 — Fruit and berry phenology

[Wild Fruits guide](https://terrafirmacraft.github.io/Field-Guide/en_us/the_world/wild_fruits.html) describes annual growth/flower/fruit/dormancy, climate suitability, fruit-tree graft/splice behavior, tall/small bushes and a distinct banana harvest/replant lifecycle. It permits multi-fruit trees through branch grafts; it does not evidence Mendelian seed genetics.

[SeasonalPlantBlock.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/plant/fruit/SeasonalPlantBlock.java) selects twelve-month lifecycle data with hemispheral month handling, reads average climate at root and transitions seasonal states. [FruitTreeLeavesBlock.java](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/blocks/plant/fruit/FruitTreeLeavesBlock.java) supplies tree-specific lifecycle/harvest behavior. The guide's month examples should not become universal Earth-calendar constants in Deep Primal.

### TFC-A07 — Irrigation structures are not Deep Primal finite-water acceptance

[Aqueducts guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/aqueducts.html) describes horizontal water conveyance, connection to source/falling water and cessation after aqueduct removal. This is useful progression/structure inspiration. It is not evidence of volumetric depletion, hydraulic energy or conservative pipe ownership. Deep Primal's channels and irrigation must spend measured finite water.

### TFC-A08 — Food lifetime, nutrition and preservation

[FoodComponent.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/component/food/FoodComponent.java) stores traits, optional food data and creation date. [FoodData.java](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/component/food/FoodData.java) contains hunger, water, saturation, intoxication, five nutritional categories and decay modifier; this is not a physical harvest-mass ledger.

[FoodCapability.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/component/food/FoodCapability.java) preserves relative decay when changing preservation traits; merging otherwise compatible stacks chooses the earlier creation date. [FoodTraits.java](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/common/component/food/FoodTraits.java) registers salted, brined, pickled, vessel/vinegar preserved, cooked variants and canned traits. Deep Primal should prevent freshness-reset exploits across recipe, split/merge and container changes.

[Preservation guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/decay.html) describes vessels, cooking, salt/brine/vinegar and food expiry. It also documents rats attracted to unsuitable food storage. Rat implementation was not source-audited in this pass; this is documented storage-pest behavior, not proof of crop insect/weed mechanics.

### TFC-A09 — Calendar pause and elapsed updates

[ServerCalendar.java, pinned](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/72750e42f3e0e8b337400ff1b8468a7ff7bc786d/src/main/java/net/dries007/tfc/util/calendar/ServerCalendar.java) gates normal tick advancement on server freeze/player-presence state and includes explicit calendar skip methods. The crop code can integrate elapsed intervals. **Do not copy TFC catch-up wholesale:** Deep Primal requires its own explicit active-set/calendar policy and no offline wall-clock advancement.

### TFC-A10 — Greenhouse attribution boundary

[Greenhouse guide](https://terrafirmacraft.github.io/Field-Guide/en_us/firmalife/greenhouse.html) is inside **(Addon) Firmalife**, not base TFC. It describes an enclosed greenhouse controlled by a climate station, structure families and planter/sprinkler use. This was documentation-only inspection; Firmalife source/version compatibility was not audited. Therefore original greenhouses, cellars and greenhouse irrigation may be proposed, but should not be advertised as base TFC 1.21.1 mechanics or as an inspected Deep Primal dependency.

## Useful adoption boundaries

**Adopt as design lessons:** climate-readable farming; NPK stock distinct from soil quality; differing crop demands; explicit growth/yield/expiry; seasonal orchard states; preservation progress that survives transformations.

**Adapt:** finite measured irrigation; ecological nutrient cycling; bounded shared soil/plot records rather than per-cell ticking; original crop traits, flora, pollinators and pests; game-calendar semantics; physical harvest mass and treatment ledgers.

**Do not infer from inspected TFC:** AgriCraft-style seed genome/analysis/mutation mechanics; a crop weed/insect simulation; base-game greenhouse climate control; finite-volume irrigation consumption; a scientifically closed C/N/P/K simulation; old food-weight mechanics. Absence from these inspected files is not a claim that no addon or another branch offers them.

