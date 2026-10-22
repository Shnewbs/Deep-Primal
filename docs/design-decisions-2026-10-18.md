# Deep Primal — Design Decisions and Proposals

Date: 2026-10-18; updated with the 12:47 PDT owner follow-up. Source: owner responses to the design audit.

This document refines the [design reference](design-reference.md) and [roadmap](../roadmap.md). Confirmed choices supersede conflicting earlier draft text. Proposals remain proposals; no implementation, release date, or v0.1 expansion is implied. Specific balancing numbers require playtesting.

## World height and performance priority

Confirmed owner priority: world generation performance is a non-negotiable foundation and release gate. Expanded vertical terrain is the goal; the vanilla default height must not be our assumed design ceiling. “Uncapped” is a desired experience to investigate, not a claim that literal unlimited height is implemented or supported on the unselected target.

Distinguish terrain elevation range, buildable vertical range, and distant visibility. Raising one does not automatically raise the others. Preserve mountain shapes, deep geology, aquifers, caves, and bedrock resources within an explicitly supported vertical range; do not flatten peaks against an arbitrary default ceiling. Earth horizontal 1:1 scale does not silently imply 1:1 vertical scale.

### Required feasibility and performance gates

- Select the exact Minecraft/NeoForge target, then inspect/test dimension height bounds, generator settings, coordinate encoding, section indexing, lighting, persistence, networking, and rendering. Do not adopt an older version’s numeric limits as current facts.
- Compare bounded taller-world profiles before selecting a default. Exact heights, minimum Y, and sea level are not locked. Lock the selected vertical geometry at world creation and require an explicit migration policy for changes.
- If supported taller dimensions cannot achieve the desired world, investigate independently streamed vertical sections/cubic world architecture as a separate feasibility effort. This touches much more than a heightmap and is not a presumed drop-in optimization.
- Use measured exploration performance to decide architecture and scope. A larger height range cannot pass merely because it loads or renders a screenshot. Correctness, save integrity, and deterministic terrain remain mandatory alongside speed.
- Set numeric budgets on named reference hardware before declaring success. Record CPU, RAM, storage, client settings, server player count, seed, software versions, height profile, and worldgen configuration for reproducibility. No invented performance guarantees.

### Generation approach to prototype

- Stage terrain, geology, hydrology, caves, ores, and vegetation so their separate time and memory costs are visible. Reuse regional/column samples across stages instead of recomputing climate/noise for each block.
- Use bounded worker queues for pure sampling and safe parallel stages; respect engine ownership/threading rules for world mutation. More threads must not cause unbounded memory growth or main-thread stalls.
- Prefer sparse/cached derived data and avoid unnecessary work in empty vertical space where the selected engine permits it. Profile total height scaling, not just block-placement cost.
- Bound caches and queues; define eviction, cancellation, and backpressure. Coalesce repeated requests and avoid forced generation of distant chunks just to answer map/LOD or aquifer queries.
- Preprocess Earth datasets locally; no synchronous remote downloads in chunk generation. Missing tiles must produce a deliberate, documented behavior.
- Preserve deterministic output across worker counts and generation order, consistent chunk borders, and bounded neighbor dependencies. Performance tuning must not create seams or aquifer/ore duplication.
- Keep terrain generation, lighting/meshing, networking, and client rendering separately measured; a faster sampler alone does not prove smoother travel. Far-terrain LOD does not solve world-height limits or server generation stalls.

### Benchmark and regression suite

| Scenario | Required evidence |
| --- | --- |
| New world and cold exploration | Time to playable spawn; request-to-ready chunk latency (median/p95/p99), throughput, queue depth, and server tick impact |
| Several players exploring separately | Throughput and latency under contention; CPU, memory, network, and fairness |
| Tall mountains, deep caves, dense forests, and aquifers | Per-stage cost and height-scaling behavior across representative worst cases |
| Warm cache versus fresh terrain | Cache effectiveness without using pregeneration to conceal new-generation cost |
| Fast transport and hearth arrival with a wagon | Terrain readiness, frame/tick spikes, safe arrival, and no load/transfer deadlocks |
| Save/reload and long exploration | Save size, I/O, memory stability, cache eviction, and version-consistent output |

Every worldgen change must carry relevant before/after measurements once a runnable baseline exists. Gate releases on the agreed latency, throughput, memory, and correctness budgets. A regression that breaches those budgets blocks advancement until fixed or the feature scope is explicitly reconsidered; do not quietly loosen the budgets to pass. Optional visual complexity must not consume unbounded generation resources. Pregeneration is a server option, not a substitute for acceptable live generation.

Research reference: [OpenCubicChunks/CubicChunks](https://github.com/OpenCubicChunks/CubicChunks) demonstrates an alternative architecture for extended height/depth. It is a design reference only; compatibility with our future target and any performance benefit are unverified.

## Decision register

| Topic | Status | Direction |
| --- | --- | --- |
| First tools | Confirmed | PrimalCore-inspired flint, loose rock, and bone tools, followed by developed stone tooling and later metals. |
| Water | Confirmed direction; expanded 2026-10-22 | Distinct water types; finite local physics with renewable regional reservoirs, perennial major rivers, seasonal melt and desert wet/dry states, shared depletable wells, stable mean ocean level and accounted recycling. Local floods remain physical; strict depletion/source safety are optional. See [water mechanics](WATER-BUDGET-REQUIREMENTS.md). Elevation is not a guaranteed purity rule. |
| Transport | Confirmed breadth | Personal storage, sledges/handcarts, pack animals, animal carts, rail carts, rafts, boats, and towable cargo craft. Horses, mules, and donkeys can pull appropriate carts. |
| Steam transport | Proposed extension | Coal-fired steam trains and boats; extends the previous mechanical-only ceiling if adopted. No electrical industry implied. |
| Time | Confirmed direction | Familiar TFC/Minecraft behavior, not a new real-time offline simulator. Use the TFC 1.21.x baseline and verify each subsystem. |
| Death | Confirmed | Keep personal inventory on by default; configurable off. Recover cargo in the world. Respawn fatigue escalates only for closely spaced deaths; a configurable death-free interval resets escalation. |
| Navigation | Confirmed direction | Biome-entry titles, compass HUD, configurable built-in maps, shared exploration and cartographic features inspired by the supplied references. |
| Glacial landscape coverage | Confirmed owner direction, 2026-10-21 | Add glacier-carved valleys/fjords, moraines/debris, braided meltwater/outwash, connected glacial lakes and ice-margin communities to planning. Named layers/variants extend existing profiles; climate details and implementation remain deferred. See [glacial plan](GLACIAL-LANDSCAPES.md). |
| Planetary environmental references | Confirmed owner scope, 2026-10-21 | Use traits from actual named planets, including exoplanets, as island/environment references alongside Earth. Selected traits and game translations remain proposals, with primary sources and observed/inferred/fictional status. See [island and planetary planning](BIOME-PLAN.md#islands-and-planetary-references). |
| Ocean circulation | Confirmed owner direction, 2026-10-21 | Currents reflect winds, rotation/hemisphere, basin/coast/seafloor geometry, depth and temperature/salinity-driven density. Plan surface/deep circulation and vertical exchange, with coastal climate and habitat effects. Implementation and tidal-model details remain deferred. See [biome plan](BIOME-PLAN.md#ocean-circulation--confirmed-direction). |
| Climate geography | Confirmed owner direction, 2026-10-21 | Realistic northern/southern polar regions and an equatorial region in procedural worlds and optional Earth mode; latitude provides the broad temperature pattern, modified by terrain, ocean influence and moisture. Seasons respect hemispheres. Coordinates and scale remain deferred. See [biome plan](BIOME-PLAN.md#geographic-climate-realism--confirmed-direction). |
| Biome/climate planning order | Confirmed owner direction, 2026-10-21 | Define biomes first; afterward design the climate scenarios needed to support them. Existing climate bands and numerical thresholds must not gate the biome roster. Climate eligibility must gate where each biome can generate. See [biome plan](BIOME-PLAN.md#design-work-order). |
| Health and hunting | Confirmed direction | First Aid-style body regions integrated with TFC-style survival; primitive weapons through hunting bows, visible back quiver, material-dependent tips, localized animal hits, and carcass processing. |
| Advanced bows | Proposed | Bow modifications and a later compound bow; exact ages and component requirements undecided. |
| Hunting rifle | Tentative | Optional late-game hunting rifle under consideration; no construction details or commitment to a release. |
| Fishing | Confirmed direction | Primitive stick rods plus ice, trolling, surf, fly, casting, and spinning specializations; active fishing, traps, and propped-up idle rods. |
| Shelter | Direction accepted | Roof, wind protection, insulation, heating/drying, and cellar benefits accepted in the general follow-up; exact simulation and optional hazards remain to tune. |
| Renewal and waste | Direction accepted | Renewal/recycling direction accepted; finite ore veins, high-reserve bedrock ore, remeltable scrap, salvage piles, and configurable disposal. |
| Feedback | Confirmed preference | Follow the restrained PrimalCore/TFC style; explain additional systems without an intrusive notification layer. |
| Advancement | Confirmed direction | Persistent, non-spendable MMO-style character XP with a configurable maximum level; vanilla XP is completely replaced with no re-enable or legacy-pool option; profession progression is distinct. Exact caps, curve, and benefits remain open. |
| Storage | Confirmed restriction | Searchable connected storage remains local/on-site. No built-in remote retrieval or deposit; external mods may supply those features. |
| Cooperation | Direction accepted | Optional teams/guilds/parties, with ordinary multiplayer fully usable without membership; detailed permissions remain open. |
| Hearth return | Confirmed feature, details open | HUD/keybind ability without an inventory slot, returning to a bed/bound home. Configurable 30–60 minute cooldown; default and exact binding rules undecided. |
| Animal yields | Confirmed | All animal meat/material yields depend on individual weight and species-specific recovery; no fixed universal drops. |
| Fishing journal | Confirmed | Persistent species catalog with discovery/catch weight records; fish meat yield is based on individual fish weight. |
| Power bridge | Confirmed direction | Native rotation interface and optional adapter blocks/modules for Create and other compatible automation mods. |

## Biome-first planning — owner clarification, 2026-10-21

Finish biome coverage, identity, terrain, geology/soil, water setting, vegetation/life, resources and regional variants first. Then design the climate scenarios and transitions needed to support that roster. Numerical climate ranges and climate-model selection belong to the later phase. Climate must gate biome placement according to each biome's relevant long-term requirements, together with terrain, water, substrate and other habitat constraints. This supersedes earlier climate-first wording in roster design; climate remains a required placement filter, while detailed runtime generation ordering remains a later technical decision. Environmental implementation remains deferred. See the [biome plan](BIOME-PLAN.md).

The owner also confirmed geographic climate realism: a generally warmer equatorial region and colder northern/southern polar regions, with gradual latitude trends, regional terrain/ocean/moisture influences and opposite seasonal cycles between hemispheres. This applies to ordinary procedural worlds as well as Earth mode. Coordinate mapping and geographic scale remain later climate-design decisions.

The owner also requires ocean currents based on realistic attributes. Later climate/ocean planning must connect winds, rotation, basin geometry, depth, temperature and salinity to surface/deep flow and vertical exchange, with consequences for coastal climate and marine habitats. Use a shared current field for future gameplay consumers, with bounded regional sampling. See [ocean circulation requirements](BIOME-PLAN.md#ocean-circulation--confirmed-direction). This records planning scope only.

The owner clarified that other planets means actual discovered/named planets, including exoplanets. Planetary physical traits are now in the environmental reference scope. The [island and planetary planning proposal](BIOME-PLAN.md#islands-and-planetary-references) records examples, scientific uncertainty and how traits could fit island communities, materials and landforms. Exact trait selections, full planetary world profiles and implementation remain open. Existing biome/climate placement requirements continue to apply.

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

### Renewable rivers and seasonal surface water — approved 2026-10-22

The owner approved the hybrid proposal and clarified that temporary desert flooding should recede through the following dry period. The [water mechanics contract](WATER-BUDGET-REQUIREMENTS.md) now governs metered regional headwaters, locally finite water, renewable minimum supply for designated major rivers, seasonal glacial/snowmelt streams, shared groundwater and accounted ocean/atmosphere recycling at stable average sea level.

This preserves well depletion/recovery and bounded extraction; it does not make every groundwater source or river cell infinitely renewable. Actual basin storage and outlets govern local damming/floods. Optional strict mode removes protected baseflow; optional source safety limits new delivery under backpressure/capacity constraints without deleting stored water.

Seasonal desert washes/lakes may fill, shrink into pools/damp ground and expose dry beds as weather, substrate and the water budget change. A sufficiently supplied river or spring may remain wet in a desert. Evaporation, infiltration and drainage transfer water to named accounts; visual hiding is not physical drying. Wet/dry season timing follows the later local climate plan rather than a rule that every desert has winter rain.

Numeric rates, reservation/extraction priority, climate thresholds and release placement remain open. This records accepted mechanics, not completed runtime implementation or an expansion of environmental catalog implementation.

## Transport and connected storage

Desired transport coverage: backpacks/baskets; handcarts/sledges; pack animals and carts pulled by horses, donkeys, or mules; rail carts; rafts/boats towing storage rafts/boats. Coal/steam trains and boats are a proposed later extension. Decide vehicle ages, capacity, draft, gradients, coupling limits, and unloaded-boundary behavior before implementation.

Cargo remains physical and contributes mass. Hitching, unhitching, capsizing/destruction, boarding, ownership, and save/reload must have explicit rules; avoid duplicating a cargo inventory when its carrier changes.

For storage, the owner requests a unified inventory interface inspired by Tom's Simple Storage, Obscura's Simple Storage, and Simple Storage Network. Confirmed boundary: on-site connected storage with search, filters, local deposit/withdraw, and crafting access. No built-in remote retrieval or deposit, including handheld wireless access or automatic deposit from the player at a distance. Players may add external mods such as RS or AE2 for that behavior; this is not a claim of verified compatibility. Revisiting native remote access requires a later explicit design change.

Indexing and permitted local access are distinct from long-distance transport. Define the physical footprint of a local storage network so long cables cannot silently recreate remote retrieval. Native physical freight/automation remains possible through its own explicit transfer rules. Food age, heat, quality, mass, and container contents must survive every transfer. Remote queries must not silently force unlimited chunk loading.

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

## Shelter — accepted direction

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

Cargo stays in the world and must be recovered after death; keep inventory does not return it. Hearth travel only carries a cart/wagon and its onboard cargo when the player is seated in that rideable vehicle at activation, as specified below. Unoccupied or merely nearby cargo is not recalled. Vehicle/container freight is distinct from ordinary carried items. If the carrier survives, leave its cargo in place. If destroyed, use a persistent recoverable cargo record/container at the loss site (safe nearby placement rules must handle lava, void, or inaccessible locations). Do not turn recoverable freight into short-lived loose drops. Define ownership, shared access, marker, and server cleanup policies. Explicit cargo-designated carried loads, if introduced, need visible labels; do not silently classify every backpack as freight.

Respawn applies configurable fatigue. Only closely spaced deaths increase severity and/or duration up to a cap. If the interval since the previous death meets or exceeds a configurable reset interval, the next death receives baseline fatigue, not another stack. This reset is separate from the duration of an existing fatigue effect; stale escalation cannot carry forward indefinitely. Exact interval and clock policy remain open. Exact debuffs, durations, thresholds, and online/offline decay policy are not set. Recommended guardrail: fatigue must leave recovery possible rather than repeatedly killing the player through hunger, cold, or immobility. Character XP is progression rather than a death-payment currency; proposed default is no XP/level loss. Profession-XP retention and respawn body-health restoration need explicit settings.

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

## Resource renewal and waste — accepted direction

The purpose is to keep long-term worlds viable and give processing leftovers useful destinations.

| Resource | Proposed lifecycle |
| --- | --- |
| Trees/fiber | Regrowth or replanting with climate/soil limits; avoid regrowing inside builds |
| Crops/seeds | Seed recovery, fertility, compost, and rotation |
| Wildlife/fish | Regional reproduction/recovery and habitat capacity; overharvesting reduces local availability |
| Groundwater | Recharge and shared drawdown as requested above |
| Ore | Finite primary veins, plus high-reserve bedrock-style ore nodes; no default regeneration. Scrap recycling complements mining. |
| Clay/sand/gravel | Decide finite deposits versus limited river/coastal replenishment; do not overwrite player builds |

Proposed byproduct uses: suitable ash in fertilizer/glass chains, organic leftovers in compost, compatible slag/tailings in recovery or construction recipes, and metal scrap in remelting. These are material-specific recipes, not universal conversions. Preserve losses so recycling cannot produce net new metal. Bulk disposal or stockpiling must remain practical when a byproduct has no current use.

Pollution, runoff, and water contamination from industry are optional later proposals, not newly locked requirements. Avoid making every production chain generate mandatory trash management before it offers useful output.

## Professions, feedback, and cooperation

MMO-style character XP and a maximum character level are confirmed. Profession levels are a separate progression track; exact age caps remain undecided. Recommended distinction: player skill belongs to the character; technology availability comes from tools/workstations/recipes. Decide whether age is personal, settlement, or world-wide before using it as a cap. Consider soft diminishing returns rather than hard caps to avoid forcing a dedicated fisher to pursue metallurgy just to improve fishing.

Candidate skills: fishing, cooking, hunting, farming, mining/prospecting, smithing, carpentry, tailoring, and medicine. Exact roster and XP rules are open. Automation should reduce repeated work; an unattended machine need not train every nearby player's profession. Decide XP attribution, team unlocks, late joining, death retention, and exploit prevention.

Follow PrimalCore/TFC-style feedback: readable tools, machine state, sound, temperature/status cues, and guide entries. Add concise explanations where native systems require them (dry well, obstructed network, invalid treatment); avoid a separate stream of pop-ups. New map titles remain independently toggleable.

Teams/guilds/parties are accepted as an optional direction. Suggested roles: party for temporary expeditions; guild/team for persistent shared maps, markers, and storage permissions. Solo players retain normal progression. Membership, storage access, map visibility, PvP/friendly fire, and land protection are separate policies; no economy, mandatory factions, or global MMO service is implied.

## Follow-up: hearth return, progression, resource reserves, and interoperability

### Hearth return

Provide an ability button beside the HUD/hotbar, with a keybind and visible cooldown; it takes no inventory slot. The owner suggests a 30-minute or one-hour cooldown and return to bed/home. Both must be configurable; neither is locked as the default yet.

Recommended behavior, pending detailed tuning:

- Explicitly show/bind the home destination, initially using a valid bed. Do not equate every temporary sleep with silently rebinding home.
- A short channel, with movement/damage interruption, discourages instant combat escape. Channel length and combat restrictions are proposed settings.
- Validate a safe landing at the destination. A missing/obstructed bed cancels with a clear message; fallback destinations are an explicit server option rather than an arbitrary teleport.
- The server owns destination and cooldown state; persist both through reconnects and restarts. Consume cooldown on successful travel, not a rejected attempt.
- Decide elapsed-real-time versus active-playtime cooldown independently of calendar/day length. The requested “30–60 minutes” must not be accidentally rescaled by game-day settings.
- Confirmed occupied-vehicle exception: if the player activates hearth while seated in a cart/wagon that supports riding, transfer the player, that occupied vehicle, and its onboard cargo together. If on foot, transfer only the player and personal inventory; an owned, nearby, led, or unoccupied cart does not follow. This is hearth transport, not remote storage access.
- Revalidate the seat relationship when travel completes. Check safe destination clearance for the whole occupied vehicle; insufficient clearance cancels the entire transfer without consuming cooldown or separating cargo. Transfer entity identity, inventory, and rider state atomically to prevent loss or duplication.
- Attached draft animals, trailers, other passengers, boats, and trains need explicit follow-up rules; the seated cart/wagon exception does not silently authorize teleporting an entire convoy. Ownership/access, passenger consent, dimension restrictions, and hitch handling remain to specify.
- Personal carried inventory normally travels with the player; this does permit periodic return with carried goods. If additional encumbrance/freight limits are desired, specify them visibly rather than silently taking items away.

### Character XP and professions

Replace spendable vanilla XP with accumulated character XP and a maximum level. Define a configurable XP curve and cap; the owner has not chosen a numeric cap. Native crafting, repair, and other actions must not spend this progression XP.

Keep character level, profession skill, and technology age as separate values. Specify level benefits, eligible XP sources, party attribution, and behavior at the cap. Prefer rewards for meaningful play/discovery over endless craft/un-craft or recycle loops. Purchased, traded, repeatedly dropped/picked-up, or machine-circulated items should not generate discovery XP repeatedly.

Vanilla XP is fully replaced whenever Deep Primal is installed. No setting, vanilla-content module, compatibility mode, or legacy XP pool may restore it. Native XP awards feed the new progression rules; vanilla XP orbs, spendable levels, and spending behavior are disabled/replaced. Audit all award/consumer paths, including death, commands, repair, enchanting, and optional mod interactions. Any retained action must use a non-XP cost or be unavailable; unsupported XP-dependent integrations remain unsupported rather than reviving vanilla XP. External mods must never subtract character progression. This is a firm exception to general vanilla-content re-enable options.

### Fishing journal

Include a native fishing log/book accessible through the guide/journal UI. Record species caught, catalog completion, and individual catch weight. Recommended additional entries: first catch, count, personal largest weight, water/habitat, season, and successful method; exact fields are tunable. Show undiscovered species as unknown or silhouettes if discovery gating is enabled.

Use stable species IDs from data so added species can extend the journal. Define personal versus shared catalog views separately from profession XP. Avoid discovery farming through trading or repeated pickup; distinguish genuine catches from purchased fish. Trap/idle-rod catches need owner attribution. Preserve records across death and save/reload.

### Animal weight and meat/material yields

Confirmed: all animal meat and item yields are governed by the individual animal's weight, including fish, livestock, and hunted wildlife. Replace fixed per-species drop counts with weight-based species profiles. Species determines which materials exist and their proportions; individual weight sets the available quantities.

Recommended model for each carcass product: recoverable product mass = animal/carcass mass × species product fraction × bounded recovery. Products include meat, fat, organs, bone, hide/leather area or size, feathers, scales, and other applicable animal materials. Proportions, density/area conversions, and tool/skill recovery are balance data. Larger specimens provide more recoverable material of the same species under comparable conditions; quality and contamination remain separate from quantity.

Anatomically discrete items such as horns or a whole hide retain sensible counts, with weight reflected in size, mass, and processing yield instead of arbitrarily adding extra body parts. Damage, trimming, spoilage, or contamination reduce usable recovery. Better tools/skill reduce waste within the available material budget; they do not create extra carcass mass.

Weight is stored per animal and changes with the chosen growth/body-condition model. Persist slaughter/catch weight on carcasses and fish through storage and processing. Products from one carcass share an accounting budget so different processing routes cannot each claim the full yield. Stack splitting, portion rounding, and repeated processing must conserve quantities; different-weight specimens must not merge in a way that erases their weights. Preserve any portion remainder or use variable-weight items. Journals retain the original catch record after processing.

Recurring animal products (milk, eggs, wool and similar) also use species/body-weight-dependent yield profiles, with nutrition, maturity, regrowth/production intervals, and applicable anatomy. They are recurring production, not an unlimited on-demand subtraction from a carcass. Exact production formulas remain to be designed.

### Finite veins and bedrock reserves

Ordinary ore veins are finite, TFC-style. Add bedrock-style deposits with much larger total recoverable ore per block. The referenced Bedrock Ores reduces a deposit's remaining yield on repeated extraction and turns exhausted deposits into ordinary bedrock. Its high output is a reserve over multiple extractions, not an infinite source.

Recommended native model: geology/ore-linked nodes with saved reserves, tier/tool requirements, and extraction costs. Deplete reserves atomically with output so reloads, automation, or concurrent miners cannot duplicate ore. Extraction method, ages, per-cycle yield, mining speed, and whether a native rotational extractor ships first remain open. Higher reserve does not automatically mean a huge instant block drop. Vanilla bedrock must remain intact beneath exhausted nodes.

### Scrap piles and remelting

Metal scraps are remeltable through the native heat/metallurgy system. Recipes preserve material/alloy identity, require appropriate processing, and include loss or bounded recovery to prevent material-creation loops.

Generate discoverable trash/salvage piles inspired by Scrap Tech Workshop. Piles have finite contents, can shrink as harvested, and use configurable loot pools. Choose locations and a visual/lore explanation appropriate to Deep Primal (for example abandoned camps or ruins). Do not import the reference's batteries, robots, or electrical progression merely because its scrap piles are an inspiration.

Loot must not bypass the intended primitive bootstrap or advanced alloy/tool gates. Specify whether placed piles can merge, how harvested piles persist, and whether any replenishment exists; no automatic scrap regeneration is implied. Recycled player items retain bounded material yield and do not become richer worldgen loot.

### Trash and item-drop policies

Provide configurable disposal choices: a trash bin, optional fire/lava destruction, and a WoW-like drag-to-delete interface. The no-manual-dropping-after-pickup mode is a proposal, not the locked default. Suggested selectable modes: normal dropping, confirmation before dropping, or manual dropping disabled with intentional deletion/trash access.

Use a confirmation or recoverable staging slot for deliberate deletion, especially full containers and valuable items. Deletion must be server-authoritative and explicitly handle nested contents. Fire/lava disposal must have a defined interaction and work without loose item entities if the chosen drop mode disables those entities.

Distinguish manual player drops, newly mined/harvested loot, automation outputs, death loot when keep inventory is off, and persistent cargo recovery. Do not disable all of them with one blanket item-entity switch. Full inventories must never silently void a new pickup. Keep a safe handoff/trade/container-transfer route when manual dropping is disabled.

Material-aware fire disposal versus an unconditional deletion abstraction remains a choice; not every material burns. The optional disposal rule should be explicit in UI/config. Persistent death cargo is not ordinary disposable litter and must not be removed by item cleanup.

### Rotation compatibility bridge

Expose a native rotational-power API and an optional physical adapter/coupler block. Create is the first named bridge target; additional mods need their own tested integrations. The standalone mod works without them.

Specify direction, RPM, load/capacity mapping, unit conversion, overstress/disconnection behavior, and supported operating ranges. Bridge updates must preserve power accounting and avoid double-counting capacity or creating power in conversion loops. Test networks that split, reconnect, unload, or change direction.

Other tech mods may expose electrical/energy APIs rather than rotation. Those require separately scoped conversions with explicit efficiency/cost rules; a generic rotation bridge cannot promise arbitrary interoperability. Keep integration modules optional and version-specific. This does not add native wireless storage or an electrical technology tree.

References inspected on 2026-10-18:

- [Bedrock Ores](https://www.curseforge.com/minecraft/mc-mods/bedrockores): finite repeated extraction from high-reserve bedrock nodes; no direct dependency adopted.
- [Scrap Tech Workshop](https://www.curseforge.com/minecraft/mc-mods/scrap-tech-workshop): discoverable/harvestable salvage piles and recycling inspiration; no unrelated technology adopted.

These additions are design requirements or clearly marked proposals. Release placement remains open; they are not implemented or automatically added to the v0.1 release gate.

## Scope and next design tasks

Rendering/worldgen remain the first foundation. Hydrology belongs in M2; bootstrap, health/recovery, and time behavior affect M3. Connected storage and transport need contracts before M4/M5 integration. Full fishing specializations, professions, web mapping, steam vehicles, and a rifle are not automatically v0.1 requirements.

Before implementation: settle water/well guarantees and surface-source rules; define attainable first tools and first metal; define the local storage footprint; decide character/profession caps and age ownership; specify shelter and renewal parameters; scope vehicle physics and steam. Verify all reference compatibility/licensing separately if integration or reuse is selected.

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
| [Simple Storage Network](https://www.curseforge.com/minecraft/mc-mods/simple-storage-network) | Local unified storage inspiration only; native remote access explicitly excluded |

Tom's Simple Storage is additionally retained as an owner-named reference; its specific implementation has not been verified in this pass.


## Built-in renderer and localhost diagnostics — latest follow-up

The owner requires an authored renderer and performance features inside Deep Primal, available out of the box. Requiring users to collect Sodium and multiple external optimization mods is not the selected distribution strategy. When recognized competing renderers are installed, Deep Primal should silently deactivate overlapping rendering components, preserve unrelated gameplay/worldgen/server optimizations, and restore its renderer automatically when those mods are removed on a later launch. This means yielding code/resources, not uninstalling or deleting other mods.

The requested visibility ceiling is 192 or 256 chunks, with horizontal and vertical culling. Alpha 6 implements a 0–256 macro-terrain horizon alongside bounded native full-detail rendering. Its coarse generated geometry is an initial renderer stage, not completion of the near-terrain pipeline or a saved-world LOD system. The renderer currently uses OpenGL; Vulkan remains implementation work and must not be shown as functional until it actually renders and passes validation.

A built-in localhost listener should make memory/performance graphs, troubleshooting, and world data easy to inspect. Alpha 6 provides a read-only loopback dashboard, bounded history, native metrics and diagnostic JSON export. Its GUI/config/compatibility behavior and limitations are documented in `RENDERER-AND-DIAGNOSTICS.md` and the alpha 6 validation report.
