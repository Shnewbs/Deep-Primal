# Deep Primal — Design Decisions and Proposals

Date: 2026-10-18. Source: owner's response to the design audit.

This document refines the [design reference](design-reference.md) and [roadmap](../roadmap.md). Confirmed choices supersede conflicting earlier draft text. Proposals remain proposals; no implementation, release date, or v0.1 expansion is implied. Specific balancing numbers require playtesting.

## Decision register

| Topic | Status | Direction |
| --- | --- | --- |
| First tools | Confirmed | PrimalCore-inspired flint, loose rock, and bone tools, followed by developed stone tooling and later metals. |
| Water | Confirmed direction | Distinct real-world water types; varying groundwater per chunk; wells and hybrid depletion/recharge. Elevation-based quality is a proposal to refine, not a guaranteed purity rule. |
| Transport | Confirmed breadth | Personal storage, sledges/handcarts, pack animals, animal carts, rail carts, rafts, boats, and towable cargo craft. Horses, mules, and donkeys can pull appropriate carts. |
| Steam transport | Proposed extension | Coal-fired steam trains and boats; extends the previous mechanical-only ceiling if adopted. No electrical industry implied. |
| Time | Confirmed direction | Familiar TFC/Minecraft behavior, not a new real-time offline simulator. Use the TFC 1.21.x baseline and verify each subsystem. |
| Death | Confirmed | Keep inventory on by default; configurable off by the world/server owner. |
| Navigation | Confirmed direction | Biome-entry titles, compass HUD, configurable built-in maps, shared exploration and cartographic features inspired by the supplied references. |
| Health and hunting | Confirmed direction | First Aid-style body regions integrated with TFC-style survival; primitive weapons through hunting bows, visible back quiver, material-dependent tips, localized animal hits, and carcass processing. |
| Advanced bows | Proposed | Bow modifications and a later compound bow; exact ages and component requirements undecided. |
| Hunting rifle | Tentative | Optional late-game hunting rifle under consideration; no construction details or commitment to a release. |
| Fishing | Confirmed direction | Primitive stick rods plus ice, trolling, surf, fly, casting, and spinning specializations; active fishing, traps, and propped-up idle rods. |
| Shelter | Explanation requested | Shelter proposal below awaits approval. |
| Renewal and waste | Explanation requested | Resource lifecycle proposal below awaits approval. |
| Feedback | Confirmed preference | Follow the restrained PrimalCore/TFC style; explain additional systems without an intrusive notification layer. |
| Advancement | Mixed | Automation should make work substantially easier. Profession levels and age-based caps are proposed, not yet locked. |
| Storage | Confirmed direction | Searchable connected storage inspired by Tom's, Obscura's, and Simple Storage Network. Access range, transport rules, and power requirements remain open. |
| Cooperation | Proposed | Optional teams/guilds/parties, with ordinary multiplayer remaining fully usable without membership. |

## First-day progression

Flint, loose rocks, sticks, and accessible bone are alternative starting resources; do not require all of them at every spawn. Flint is itself stone: distinguish crude expedient tools from shaped/hafted stone tools rather than treating every material as a strict linear tier.

Proposed bootstrap: gather loose material → produce an edge or point → obtain fiber/bindings and sticks → assemble tools → establish fire, water, and shelter → pottery → first metal. Bone must not require defeating an animal with a weapon that itself requires bone. Validate alternatives across climates and geology.

Exact recipes, starter resource availability, spawn season, and cold-region survivability remain design tasks. Keep the no-punching-trees direction.

## Water, aquifers, and wells

The owner wants water quantities varying by chunk, with digging/wells exposing groundwater and over-pumping able to dry a source. Proposed implementation: chunk-local records connected into regional aquifers, rather than independent refill tanks at chunk boundaries.

Each record can carry water-table height, stored volume/capacity, extraction throughput, permeability, recharge, temperature, salinity, mineral content, sediment, and contamination. These are a proposed schema, not fixed stats.

- Water-bearing layers determine where a shaft starts taking on water. A water table does not mean every underground block becomes an open water block.
- Every chunk has hydrology data. Whether every chunk guarantees an accessible, usable well remains open: a dry or low-yield region is realistic; a universal well guarantee would be an explicit accessibility rule.
- Neighboring wells tapping the same aquifer share depletion. Moving a pump across a chunk border must not reset supply.
- Rain, snowmelt, surface infiltration, geology, biome, and season influence recharge and flow. Rivers and lakes need inflow/outflow budgets alongside groundwater.
- Pumping above replenishment draws down the source. A well recovers as water returns; recovery can be slow and seasonal.
- Sustainable extraction can continue indefinitely under suitable conditions, but does not grant unlimited instantaneous throughput. Natural discharge and other wells count against the available budget.
- Deeper digging does not automatically mean fresher water or a higher-yield well.
- Elevation influences headwaters, temperature, runoff, and climate; it is not a direct drinking-safety score. Separate freshwater/salinity from contamination and mineral properties.
- Proposed source families: rain/snowmelt, spring, river, lake/pond, marsh, groundwater, mineral/hot spring, brackish estuary, and seawater.
- Water transfer must preserve volume and relevant quality values. Avoid duplication through bucket placement, vanilla source formation, unloading, or well construction.
- Wells, pumps, irrigation, ore washing, fish habitats, and prospective steam systems share the same water model.

Example game balance only: a source receiving 100 water units per game hour cannot sustain 150 units of withdrawal indefinitely; stored water covers the deficit until drawdown restricts extraction. These values are not proposed defaults or a full hydrology model.

Research basis: USGS documents groundwater in permeable layers, variable recharge, and neighboring wells sharing drawdown. Its water-quality material describes geological constituents and contamination pathways. The simplified chunk/aquifer system above is our design inference, not a claim of scientific simulation accuracy.

- [USGS: Aquifers and Groundwater](https://www.usgs.gov/water-science-school/science/aquifers-and-groundwater)
- [USGS: Contamination of Groundwater](https://www.usgs.gov/water-science-school/science/contamination-groundwater)

## Transport and connected storage

Desired transport coverage: backpacks/baskets; handcarts/sledges; pack animals and carts pulled by horses, donkeys, or mules; rail carts; rafts/boats towing storage rafts/boats. Coal/steam trains and boats are a proposed later extension. Decide vehicle ages, capacity, draft, gradients, coupling limits, and unloaded-boundary behavior before implementation.

Cargo remains physical and contributes mass. Hitching, unhitching, capsizing/destruction, boarding, ownership, and save/reload must have explicit rules; avoid duplicating a cargo inventory when its carrier changes.

For storage, the owner requests a unified inventory interface inspired by Tom's Simple Storage, Obscura's Simple Storage, and Simple Storage Network. Recommended boundary: a settlement-scale connected store with search, filters, deposit/withdraw, and crafting access; long-distance movement remains a transport task. Unlimited wireless/interdimensional retrieval is not implicitly adopted from reference mods.

Proposed distinction: indexing tells the player where stock exists; permitted local retrieval moves items; automated long-distance distribution requires infrastructure. Decide whether local retrieval is abstract or physical. Food age, heat, quality, mass, and container contents must survive every transfer. Remote queries must not silently force unlimited chunk loading.

## Time, sleep, and server behavior

The verified TFC 1.21.x `ServerCalendar` advances from server/world ticks and checks whether players are present. `ServerConfig` sets `enableTimeStopWhenServerEmpty` to true by default. This is a concrete reference baseline, not a claim that every Minecraft/TFC version behaves identically.

- Retain familiar world/server time; do not add elapsed wall-clock simulation while a world/server is shut down.
- Match TFC's default empty-server calendar pause, with the server setting able to change it.
- One player logging out does not stop the shared calendar when others remain online.
- Ordinary chunk ticking and calendar-based catch-up are separate. Verify food, crops, animals, heat, and machines individually against the selected baseline; do not award every unloaded machine free production.
- Sleep behavior, time commands, and any subsystem catch-up must preserve required inputs, fuel, and outputs. Exact matching remains an implementation verification task.

Sources read through GitHub:

- [TFC ServerCalendar](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/util/calendar/ServerCalendar.java)
- [TFC ServerConfig](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/config/ServerConfig.java)

## Shelter proposal — not yet approved

Shelter answers four questions: does rain reach the occupant, how exposed are they to wind, how much warmth is retained, and can an indoor fire vent safely?

| Construction | Proposed benefit | Remaining limitation |
| --- | --- | --- |
| Lean-to or open roof | Rain protection and a place to dry equipment | Wind and cold still reach the occupant |
| Enclosed hut or tent | Better wind protection and warmth retention | Openings and thin materials lose heat |
| Insulated home with hearth | Stable living/crafting space and useful drying | Fuel, ventilation, and seasonal heat remain relevant |
| Cellar | Cooler storage conditions | No automatic infinite preservation; flooding/wetness can matter |

Use shared block thermal/porosity properties, bounded enclosure checks, and invalidation on nearby building changes. Initial scope should not require airflow/fluid simulation or a block entity on every wall. Chimney/smoke hazards are optional proposals and must be readable before becoming lethal. A campfire still helps outdoors.

## Death and recovery

Keep inventory is on by default, including mod-managed clothing, quiver, and carried containers; verify nested contents survive exactly once. Server/world owners can turn this off. Multiplayer clients cannot independently override authoritative death rules.

Profession-XP retention, respawn health/injuries, durability penalties, and vehicle cargo are separate choices. Keeping personal inventory does not automatically restore a destroyed vehicle or its cargo. Avoid an unannounced death-teleport mechanic that undercuts hauling; any mitigation needs explicit design approval rather than a hidden penalty.

## Navigation and maps

The desired experience combines RDR2-inspired presentation, an Antique Atlas-like visual option, a compass HUD, biome-entry titles, minimap/world map, annotations, named regions, and shared exploration. Make native map/HUD features independently configurable so players can use external map mods.

Proposed architecture: one exploration/waypoint/region data service with multiple views. Avoid seven independent mapping systems or forcing all reference mods to be installed.

| View or service | Desired role |
| --- | --- |
| Biome/region title | Brief entry announcement with cooldown and boundary hysteresis |
| Compass bar | Direction, selected waypoint, and permitted party markers |
| Minimap and world map | Navigation, terrain, routes, configurable overlays |
| Atlas presentation | Hand-drawn/cartographic appearance and discovery feel |
| Sharing | Personal, party/guild, and public map information with explicit permissions |
| Frontiers | Named/colored regions and borders; map drawings do not automatically grant land protection |
| Optional web/3D view | BlueMap-inspired server presentation; separate later scope and permission review |

Discovery defaults, radar/cave visibility, map item gating, territory claims, and optional web publication remain open. Limit title spam at biome boundaries. Filter private map data on the server; a hidden client overlay is not access control. Disabling the map UI should not delete exploration records or require stopping unrelated game systems.

## Health, hunting, and equipment

Integrate localized body health and injury consequences with nutrition, temperature, clothing/armor, recovery, and treatment. First Aid is a design reference; its exact lethal-region rules and regeneration defaults are not automatically adopted.

Hunting equipment starts with sticks/primitive weapons and grows through stone/bone/metal-tipped implements and hunting bows. Include a visible back quiver. Arrowhead material affects game properties such as durability, penetration, damage, and recoverability; the shaft, draw, and target also matter.

Animal hit regions should follow anatomy and animation. Weapon, projectile travel, impact location, angle, and material determine the result; increasing range should not physically resize an animal's hitbox. Server-authoritative hit validation is required.

The owner's 45-yard deer chest/shoulder example is a desired capable one-shot outcome in the game. It is not a guaranteed outcome for every hit in that broad region, every bow, or every angle. Distinguish vital impacts, bone obstruction, non-vital wounds, fleeing, and tracking; tune results with gameplay tests.

Bow attachments and a later compound bow are proposed. Treat a compound bow as a later engineered weapon with limb/string/cam requirements and a defined draw curve; cosmetic attachments should not provide unlimited power. A late hunting rifle remains tentative.

Chemical coatings are a requested abstract game mechanic. Define fictional effects, persistence, and carcass contamination without real compound recipes. Gutting/trimming affects usable yield, spoilage, and damage loss. Do not make trimming automatically remove systemic poison: contaminated meat can remain unusable according to the game's coating rules. These are game abstractions, not food-safety guidance.

## Fishing

Requested coverage: stick rod; ice, trolling, surf, fly, casting, and spinning rods; traps; and propped-up rods for idle fishing. These should be method/environment specializations rather than one ladder where the last rod replaces every earlier rod.

Proposed loop: habitat and season → appropriate rod/tackle/bait → active fight or waiting gear → landing → handling/preservation → cooking or trade. Fishing ties into water type, depth/current, climate, population, profession skill, and equipment. A Stardew-like active minigame and passive tackle can coexist without imposing the minigame on every method.

Idle-rod design must specify bite indication, hook/escape timing, ownership, bait use, and loaded-area behavior. Passive fishing should not silently become unlimited offline harvest or an infinite profession-XP loop. Traps also need capacity, checking intervals, and population effects. Catch quality and food safety are separate properties.

## Resource renewal and waste proposal — not yet approved

The purpose is to keep long-term worlds viable and give processing leftovers useful destinations.

| Resource | Proposed lifecycle |
| --- | --- |
| Trees/fiber | Regrowth or replanting with climate/soil limits; avoid regrowing inside builds |
| Crops/seeds | Seed recovery, fertility, compost, and rotation |
| Wildlife/fish | Regional reproduction/recovery and habitat capacity; overharvesting reduces local availability |
| Groundwater | Recharge and shared drawdown as requested above |
| Ore | Finite primary veins by default is only a recommendation; scrap recycling and explicit server alternatives |
| Clay/sand/gravel | Decide finite deposits versus limited river/coastal replenishment; do not overwrite player builds |

Proposed byproduct uses: suitable ash in fertilizer/glass chains, organic leftovers in compost, compatible slag/tailings in recovery or construction recipes, and metal scrap in remelting. These are material-specific recipes, not universal conversions. Preserve losses so recycling cannot produce net new metal. Bulk disposal or stockpiling must remain practical when a byproduct has no current use.

Pollution, runoff, and water contamination from industry are optional later proposals, not newly locked requirements. Avoid making every production chain generate mandatory trash management before it offers useful output.

## Professions, feedback, and cooperation

Profession levels and age-dependent caps are proposed. Recommended distinction: player skill belongs to the character; technology availability comes from tools/workstations/recipes. Decide whether age is personal, settlement, or world-wide before using it as a cap. Consider soft diminishing returns rather than hard caps to avoid forcing a dedicated fisher to pursue metallurgy just to improve fishing.

Candidate skills: fishing, cooking, hunting, farming, mining/prospecting, smithing, carpentry, tailoring, and medicine. Exact roster and XP rules are open. Automation should reduce repeated work; an unattended machine need not train every nearby player's profession. Decide XP attribution, team unlocks, late joining, death retention, and exploit prevention.

Follow PrimalCore/TFC-style feedback: readable tools, machine state, sound, temperature/status cues, and guide entries. Add concise explanations where native systems require them (dry well, obstructed network, invalid treatment); avoid a separate stream of pop-ups. New map titles remain independently toggleable.

Teams/guilds/parties are proposed and optional. Suggested roles: party for temporary expeditions; guild/team for persistent shared maps, markers, and storage permissions. Solo players retain normal progression. Membership, storage access, map visibility, PvP/friendly fire, and land protection are separate policies; no economy, mandatory factions, or global MMO service is implied.

## Scope and next design tasks

Rendering/worldgen remain the first foundation. Hydrology belongs in M2; bootstrap, health/recovery, and time behavior affect M3. Connected storage and transport need contracts before M4/M5 integration. Full fishing specializations, professions, web mapping, steam vehicles, and a rifle are not automatically v0.1 requirements.

Before implementation: settle water/well guarantees and surface-source rules; define attainable first tools and first metal; choose storage range/retrieval semantics; decide profession caps/age ownership; approve or revise shelter and renewal proposals; scope vehicle physics and steam. Verify all reference compatibility/licensing separately if integration or reuse is selected.

## Reference catalog

Reference pages were inspected on 2026-10-18. These are design influences, not a dependency list or a promise of current target-version compatibility. RDR2 is the owner's presentation reference; no assets are to be copied.

| Reference | Relevant design influence |
| --- | --- |
| [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) | Configurable minimap, waypoints, and navigation controls |
| [Maps and Quills](https://www.curseforge.com/minecraft/mc-mods/maps-and-quills-maps-with-shared-faction-maps) | Faction/private/public annotations and controlled sharing |
| [JourneyMap Shared Maps](https://www.curseforge.com/minecraft/mc-mods/journeymap-shared-maps) | Sharing explored terrain, including team-oriented sharing |
| [MapFrontiers](https://www.curseforge.com/minecraft/mc-mods/mapfrontiers) | Named regions, colored boundaries, and paths |
| [BlueMap](https://www.curseforge.com/minecraft/mc-mods/bluemap) | Optional browser-based 3D world presentation |
| [Antique Atlas 4](https://modrinth.com/mod/antique-atlas-4) | Hand-drawn map presentation and discovery |
| [Surveyor Map Framework](https://www.curseforge.com/minecraft/mc-mods/surveyor-map-framework) | Separate map data services and frontends, sharing, and synchronization controls |
| [First Aid](https://www.curseforge.com/minecraft/mc-mods/first-aid) | Body-region health and targeted treatment |
| [Stardew Fishing](https://www.curseforge.com/minecraft/mc-mods/stardew-fishing) | Active fish-catching minigame |
| [Fisherman's Trap](https://www.curseforge.com/minecraft/mc-mods/fishermans-trap) | Baited passive trapping |
| [River Fishing](https://www.curseforge.com/minecraft/mc-mods/river-fishing) | Specialized tackle; owner specifically requests propped-up idle rods |
| [Obscura's Simple Storage](https://www.curseforge.com/minecraft/mc-mods/obscuras-simple-storage) | Connected inventories, searchable terminal, network limits/diagnostics |
| [Simple Storage Network](https://www.curseforge.com/minecraft/mc-mods/simple-storage-network) | Unified storage access; unlimited remote access not automatically adopted |

Tom's Simple Storage is additionally retained as an owner-named reference; its specific implementation has not been verified in this pass.
