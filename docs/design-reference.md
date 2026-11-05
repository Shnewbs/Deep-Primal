# Deep Primal — Design Reference

Design draft v0.6, reformatted on 2026-10-18. **Read the [owner decision update](design-decisions-2026-10-18.md) alongside this reference; confirmed updates there supersede earlier draft assumptions.** This is the detailed feature specification; use the [roadmap](../roadmap.md) for implementation order and completion status. Section numbers are retained for continuity. Vanilla XP is fully replaced and cannot be re-enabled, including through the general vanilla-content toggles below.

> **Status:** design only. Descriptions of behavior are requirements or proposals, not evidence of implemented features. External platform, version, license, and compatibility claims are inherited research notes and require verification against primary sources before implementation. Numerical values are provisional. “Suggested” and “proposed” items remain unapproved.

See the [environmental coverage inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md) for the owner's planning-only completeness audit across natural materials, states, habitats and processes. It preserves this reference's existing catalogs; additional candidates require scope review before implementation.

The [original draft](https://github.com/Shnewbs/Deep-Primal/blob/89f680063c7993ac53143aa364c08ed5ba90ce83/roadmap.md) remains available in Git history.

A standalone, total-conversion survival mod for Minecraft 26.x via NeoForge. It merges the depth of TerraFirmaCraft, primitive-tech chains, Better-with-Mods-style mechanical power, GregTech-style ore processing, and a realism layer into one long-term, high-complexity game. Difficulty comes from depth and consequence, not from inflated health bars.

## Decisions and proposals

The table includes locked direction alongside explicitly marked proposals. Open implementation choices are tracked in [§8](#s8).

| Topic | Decision |
| --- | --- |
| Distribution | Standalone mod, newer Minecraft (26.x) via NeoForge |
| Vanilla content | Total conversion by default (gutted), phased (terrain, soil, stone, plants first; mobs later) so geology, ore, and soil-nutrient systems stay fully consistent; every vanilla module can be re-enabled by config ([§0.2](#s0-2)); re-enabled content is managed by the universal adapter layer ([§4.14](#s4-14)) |
| Adapters | The v0.1 adapter layer applies shared soil, gravity, breaking, heat, decay, and weight properties to re-enabled vanilla content. Foreign-mod coverage remains an open scope decision ([§4.14](#s4-14), [§8](#s8)). |
| Explosives | TNT acts like a powder keg by default (filled with gunpowder, blast scaled to the load); config option restores vanilla TNT ([§4.15](#s4-15)) |
| Redstone / signals | Redstone is kept, but reworked to behave like wires (multipart wires, analog signals, gates), inspired by the Wired Redstone mod, with a cosmetic focus: wires look like actual cables; config option restores vanilla redstone ([§4.16](#s4-16)) |
| Configuration | Config by default: if it can be a config for the end user, it is. Everything the mod adds, changes, or removes is configurable; settings are split into multiple organized files by domain ([§4.13](#s4-13)) |
| Realism | Realistic variety in stone, soil, plants, trees, food, crops + configurable day length driving time-based systems |
| Difficulty target | GregTech-level depth, built for long-term play ("High TISM") |
| Tech ceiling | Rotation-based mechanical industry, with a proposed later coal/steam transport extension; Create bridge remains optional |
| TFC+ must-have | Clothing, warmth, cold, seasonal temperature requiring clothing |
| Multiplayer | Both single-player and server |
| Art | Blend of TFC-style and new assets, with Conquest Reforged inspiration |
| Natural catalog | All discoverable stone and tree types, each with its natural function, defined by data so the catalog can keep growing ([§4.17](#s4-17)) |
| Rendering & core | Built first. Custom rendering and world-gen are the strict foundational requirement for v0.1. Willing to build a custom rendering layer or engine inside the mod if needed to display everything well; this requires client-side code ([§4.17](#s4-17), [§10](#s10)) |
| World-gen & Earth scale | Custom rock-layer world, plus an optional 1:1 real-world Earth scale mode that maps global elevation, topography, and latitude into the mod's climate and geology ([§4.1](#s4-1)) |
| Cold & permafrost | Permafrost in polar and alpine zones (harder than obsidian; needs advanced tools or thermal thawing), plus ice cementation: cold weather hardens wet stone and soil ([§4.1](#s4-1)) |
| Inventory & stacks | Stack size is decoupled from weight: standard visual stacks in the UI, with true total mass driving encumbrance; global and per-item stack sizes are configurable (e.g. ingots anywhere from 6 to 512) ([§4.10](#s4-10)) |
| Tooltips & inspection | Abstract inspection layer exposed by the mod's own API, with soft-dependent integrations (e.g. Jade) so core systems stay independent and portable ([§4.10.1](#s4-10-1)) |
| Field guide | Native field guide driven by Markdown and datapack files, with no dependency on Patchouli or similar ([§4.10.1](#s4-10-1)) |
| Modularity | Gameplay features are optional, datapack-driven modules with working defaults; shared core services remain required ([§4.18](#s4-18)) |
| Portability | NeoForge first; the core stays platform-neutral so a later Hytale port or an own game remains possible ([§0.4](#s0-4)). A goal, not a commitment |
| Far terrain (proposed) | Heightmap-first far-terrain LOD so distant mountains aren't cut off by render distance ([§4.19](#s4-19)). Not yet locked |

<a id="s0"></a>

## 0. Scope & Approach

<a id="s0-1"></a>

### 0.1. Standalone, not a TFC add-on

The mod does not depend on TFC or any source mod. TFC 1.21.1, TFC+, Pyrotech, PrimalCore, Primal Tech, and Better with Mods are design references; every system is reimplemented natively. That means we own the heat model, calendar, climate, food, and power systems instead of hooking into someone else's.

<a id="s0-2"></a>

### 0.2. Total conversion policy (gutting vanilla; phased and configurable)

By default, vanilla content that the mod replaces is stripped out, in phases. General content phases may expose configuration, subject to firm owner exceptions: vanilla XP and all vanilla mobs cannot be restored through coexistence, vanilla-only or general re-enable settings. The October 29, 2026 mob replacement decision supersedes the earlier vanilla.mobs proposal; see [replacement policy](systems/VANILLA-MOB-REPLACEMENT.md).

| Phase | Vanilla content replaced by default | Config module |
| --- | --- | --- |
| 1 | Terrain, biomes, stone, soil (dirt/grass/sand/clay/gravel), trees and plants | vanilla.terrain, vanilla.stone, vanilla.soil, vanilla.plants |
| 2 | Vanilla ores and structures, crops and food | vanilla.ores, vanilla.structures, vanilla.food |
| 3 | All vanilla mobs and animals; native fauna/threats/NPCs replace them | Mandatory original-population policy; historical vanilla.mobs re-enable mode superseded |
| 4 | Remaining items, tools/armor, recipes, advancements, extra dimensions | vanilla.items, vanilla.recipes, vanilla.advancements, vanilla.dimensions |

For modules without a firm owner exception, supported modes may be:

- replaced (default): vanilla version is gone; the mod's version is used.
- coexist: vanilla content is added back alongside the mod's content and is adapted to the mod's rules by default ([§4.14](#s4-14)).
- vanilla-only (where it makes sense): the mod's version of that module is off.

**How removal works**

- World generation: custom biome source, terrain, and feature placement. In replaced mode, vanilla stone and soil blocks are fully removed from world generation so geology, ore, and soil-nutrient systems stay consistent. Vanilla biomes, ores, structures, and vegetation don't generate unless their module is re-enabled.
- Entities: all vanilla mobs are removed from active gameplay across supported dimensions and spawn/admission routes. Native fauna, predators, fantastical monsters, intelligent hostile factions, named villains and custom NPCs replace them; there is no production vanilla.mobs re-enable escape. Players/technical non-mob entities and recoverable existing-world records follow the [replacement/migration policy](systems/VANILLA-MOB-REPLACEMENT.md).
- Items/blocks: vanilla registry entries stay technically loaded (the game requires some of them) but are made unreachable: no worldgen, no spawns, no loot, no recipes, hidden from creative tabs, recipe book, and advancements. Re-enabling a module restores them.
- Recipes: vanilla recipes cleared by default; restored per module.

**Caveats for re-adding vanilla content**

- World-gen toggles only affect newly generated chunks; already-explored terrain keeps what it has.
- Re-added vanilla content is brought under the mod's rules by the adapter layer ([§4.14](#s4-14)): soils take on the mod's soil properties, loose blocks obey gravity and cave-in rules, break times follow the mod's hardness system, and foods decay. Adapters are on by default and can be turned off per module or per system.
- Toggling a module off in an existing world needs a policy for blocks already placed (see [§8](#s8)).
- Trade-off: other mods built around vanilla content won't fit unless the relevant modules are re-enabled. Deliberate bridges (e.g. Create stress units, [§4.5](#s4-5)) are the supported compatibility path.

<a id="s0-3"></a>

### 0.3. Things to confirm early

- Exact 26.x version to target (the loader is decided: NeoForge).
- Asset and code licensing: before reusing any TFC art or code, check its license. Conquest Reforged is inspiration only; don't copy its assets.

<a id="s0-4"></a>

### 0.4. Platform roadmap & portability

Direction (a goal, not a commitment): build for NeoForge on Minecraft 26.x first, and keep the door open to a Hytale port and, eventually, rolling the mod into your own game.

**Architecture: a platform-neutral core plus platform adapters**

- Core (no Minecraft or Hytale types): property registry, calendar, heat model, stress networks, signal-graph logic, the worldgen sampler (noise, geology, climate), the tunables registry, and module manifests.
- Adapters connect the core to a platform: NeoForge (registries, worldgen hooks, rendering, networking) now; Hytale (plugin plus asset pack) or your own engine later.
- Language: NeoForge mods and Hytale server plugins are both Java (Hytale's own modding post lists server plugins as Java .jar files), so a shared Java core is plausible. An own game in another language or engine (Godot or Unreal, for example) would need a port of the core or a native core, so the language choice belongs with the own-game engine decision ([§8](#s8)).
- Parity tests: shared test vectors check that every adapter's worldgen sampler and rule outputs match the core's.

**What would carry to Hytale (suggested; not verified in depth)**

- Carries: data definitions (properties, recipes, calendar rules, module manifests), server-side logic (survival, heat, stress, signal networks), and configs. Community Hytale mods already show kinetic power and cable-style networks are possible (Orbitech, HyProTech, H Industries).
- Doesn't carry: custom client rendering (Hytale is server-side first and its developers said they don't plan client mods), the vanilla adapter layer ([§4.14](#s4-14), since there is no vanilla to adapt), and Minecraft bridges such as Create. Worldgen would feed Hytale's own zone, biome, and cave system through an adapter instead of density functions.
- Risk: Hytale's modding post commits to releasing the server source after launch (the server is unobfuscated until then), but a third-party page said it was still unreleased as of May 2026, so the current status is unverified. The API is also still changing.

What would carry to your own game: the whole core and all data. Rendering stages 2 and 3 ([§4.17](#s4-17)) become native work, and the engine is chosen then (open question 29 in [§8](#s8)).

Cost: portability costs up-front work (interfaces, test vectors). Abstract only systems that carry real logic; don't wrap trivial platform calls.

<a id="s1"></a>

## 1. Source Pillars (Design References)

| Pillar | What we take | What we change |
| --- | --- | --- |
| TFC 1.21.1 | Rock layers, climate/rainfall/temperature, calendar & seasons, food decay + nutrition, knapping, pottery, heat/forge/anvil/welding, soil & crop fertility, animal familiarity | Reimplemented natively; calendar becomes a central clock service |
| TFC+ | Clothing, warmth, cold, seasonal temperature needing clothing (must-have), plus extra metal/tool depth | Clothing/thermoregulation becomes a core system ([§4.9](#s4-9)) |
| Pyrotech | Primitive fire, kilns, pit burning, drying/curing, crude tools, early bloomery-style chains | Early game; hands off to metalworking |
| PrimalCore | Tool-gated early game: no punching trees, no instant gathering | The "Age 0" rules layer |
| Primal Tech | Bone/flint/stone tools and primitive machines | Fills the gap before mechanical power |
| Better with Mods | Mechanical power, gearing, milling, sawing, crucible/cauldron processing, hardcore survival rules | Rebuilt around stress units ([§4.5](#s4-5)) |
| GregTech ore | Large multi-ore veins, surface indicators, multi-step ore processing, byproducts | Veins generated inside the mod's own rock layers |
| Realism | Realistic variety and behavior of world content ([§4.1](#s4-1)) and time ([§4.2](#s4-2)) | Datadriven so variants are easy to add |
| High TISM difficulty | GregTech-level depth and long-term play ([§6](#s6)) | The mod's identity, not an optional extra |
| Conquest Reforged (art) | Large building-block variety and detailed shapes/variants | Visual and building-set inspiration only |
| Wired Redstone (signals) | Multipart wires, analog signals, insulated and bundled cabling, logic gates, wires cut off by blocks | Reimplemented natively; tied to the mod's metal tiers and mechanical power ([§4.16](#s4-16)) |
| Powder keg (TFC-style) | Explosive container that must be filled with gunpowder and sealed, with blast scaled to the load | Replaces TNT by default; tied to the property system and cave-ins ([§4.15](#s4-15)) |

<a id="s2"></a>

## 2. Core Design Principles

- World generation performance is a non-negotiable release gate, measured from live exploration and multiplayer load. Expanded height/depth must pass explicit budgets; see the [height and performance requirements](design-decisions-2026-10-18.md#world-height-and-performance-priority).

- Everything is gated by a tool, a fuel, or a tier. No free resources.
- Failure is informative. Overheating, collapse, spoilage, and frostbite all have traceable causes.
- No dead-end progression. Hard, but every age has a documented way forward.
- One heat system, one clock, one power model. Fire, kilns, and forges share one temperature model; all time-based systems read from one calendar; all machines use one stress-unit model.
- Depth over grind. More steps and decisions per chain, not more repetitive clicking.
- In-game documentation is mandatory. A native, Markdown-driven field guide is a base feature ([§4.10.1](#s4-10-1)).
- Data-driven content. Rocks, soils, plants, foods, fabrics, and ores defined in data so variants are cheap to add.
- Config by default. If something can be a config for the end user, it is. Anything the mod adds, changes, or removes has a toggle or scale, and hardcoded gameplay constants are treated as bugs. Settings are organized into multiple domain-specific config files ([§4.13](#s4-13)).
- Properties, not ids. Systems (soil, gravity, collapse, breaking, heat, decay) read a shared property profile, never a specific block or item. Native and adapted content resolve to the same profile, so every block behaves by the same rules ([§4.14](#s4-14)).
- Optional modules, working defaults. Gameplay features use optional modules with data-driven definitions and working defaults; the shared core stays enabled ([§4.18](#s4-18)).
- Platform-neutral core. Rules, properties, calendar, heat, stress, worldgen sampling, and config live in a core with no Minecraft types; platforms are adapters on top ([§0.4](#s0-4)).

<a id="s3"></a>

## 3. Base Feature List (Summary)

- Custom world: climate, seasons, rock-layer geology (no vanilla worldgen), plus an optional 1:1 Earth-scale mode
- Permafrost zones and cold-weather ice cementation (block hardening)
- Realistic stone, soil, plant, tree, and crop sets
- Central configurable calendar (day length, month/year length) driving all time-based systems
- GregTech-style vein ores within the mod's rock layers
- Tool-gated primitive Age 0
- Fire, kiln, and drying chains
- Heat, forging, and welding for metalwork
- Mechanical power network using stress units
- Multi-step ore processing with byproducts
- Hardcore survival: hunger, thirst, injury, fatigue
- Encumbrance and dynamic stack weights: visual UI stacks decoupled from true item mass, with configurable stack sizes (inventory.toml)
- Clothing and thermoregulation (layers, insulation, wetness, seasons)
- Structural integrity and tunnel collapse
- Food decay, nutrition, seasonal farming, animal husbandry
- Custom fauna and threats replacing vanilla mobs
- Large building-block set (Conquest Reforged-style variety)
- Multiplayer-safe systems (per-player state, server-authoritative)
- Difficulty presets with High TISM as the design target
- Phased, per-module vanilla removal that players can re-enable
- Organized multi-file config system with presets ([§4.13](#s4-13))
- Universal adapter layer: re-added vanilla and foreign content takes on the mod's properties ([§4.14](#s4-14))
- Powder-keg explosives, with a config option for vanilla TNT behavior ([§4.15](#s4-15))
- Wire-style signal system replacing vanilla redstone dust, with a config option for vanilla redstone ([§4.16](#s4-16))
- Natural catalog: all discoverable stones and trees with their natural functions, plus the custom rendering layer to display them, built first ([§4.17](#s4-17))
- Native Markdown/datapack field guide with abstract inspection tools ([§4.10.1](#s4-10-1))
- Optional, datapack-driven module system with defaults that work out of the box ([§4.18](#s4-18))
- Platform-neutral core with a NeoForge adapter first ([§0.4](#s0-4))
- Heightmap-first far-terrain LOD so mountains aren't cut off by render distance ([§4.19](#s4-19))

<a id="s4"></a>

## 4. Base Features in Detail

<a id="s4-1"></a>

### 4.1. World, Geology, Permafrost & 1:1 Earth Generation

See the [connected biome plan](BIOME-PLAN.md), [118-profile named roster](BIOME-CATALOG.md), and [regional variants/habitat crosswalk](BIOME-VARIANTS-AND-COVERAGE.md) for the 2026-10-21 planning expansion. **Confirmed planning order: biomes first, supporting climate scenarios afterward.** Complete biome identity, terrain, materials, water setting and life before selecting climate-model ranges. Biome identity, biological communities, geology/soil, local forms and seasonal states remain separate layers. Added profiles are proposals; environmental implementation remains deferred. The current world block texture direction is shared families and material palettes/tints at 32×32 by default, with 64×64 only where necessary detail requires it; see the [block texture plan](art/BLOCK-TEXTURE-PLAN.md). The owner also confirmed actual named planets/exoplanets as physical environmental references alongside Earth. See [island and planetary reference planning](BIOME-PLAN.md#islands-and-planetary-references) for sourced examples, evidence status and proposed compatibility rules. The owner-approved [glacial landscape expansion](GLACIAL-LANDSCAPES.md) adds named landform/water layers and foreland community variants, with explicit source materials, routed meltwater/lake outlets and active-versus-inherited glacial history.

The [2026-10-22 terrain/water/snow/LOD audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) supplies the detailed connected-drainage, source-lake sizing, erosion/material placement, water composition/mixing, ocean and snow requirements, with current-source status and acceptance evidence. Reference inspiration does not establish implementation.

**Approved water mechanics, 2026-10-22:** [renewable watershed reservoirs with finite local physics](WATER-BUDGET-REQUIREMENTS.md). Designated major rivers receive budgeted renewable baseflow; glacial streams and temporary desert waters respond seasonally. Surface floods recede through accounted evaporation, infiltration and drainage into pools/damp ground/exposed beds, while supplied rivers/oases can persist. Shared wells remain depletable. Ocean/atmosphere recycling maintains stable average sea level; local damming/overflow stays physical, with optional strict depletion and source safety. Numerical climate/recharge tuning and implementation remain open.

The "realism" pillar means the world's raw materials are varied and behave like their real counterparts.

**Accepted terrain mechanics:** [structural geology, soils, wind, underground drainage and stability](TERRAIN-MECHANICS.md) govern the detailed scope. No sinkholes; underground cave-ins remain included. Panning/sluices are recipe-based by default, with optional useful local provenance rather than mandatory live sediment simulation. Accepted design is distinct from deferred implementation.

**Stone**

- Three rock classes (igneous, sedimentary, metamorphic) with multiple types in each.
- Each type has a property profile: hardness, workability, color/texture, ore associations, weathering products (the soil, sand, or clay it breaks down into), and building traits (e.g. which stones suit roofing, which carve cleanly, which crack in heat).
- Coherent geological units across depth/region: appropriate sedimentary beds, faults/folds, cross-cutting intrusions, metamorphic relationships and exposed contacts. Host/formation setting selects finite ores and gems; current high elevation is not a blanket ore exclusion. Material variants carry through to cobble, gravel, sand and building blocks. Full rock catalog in [§4.17](#s4-17); mechanics in [terrain plan](TERRAIN-MECHANICS.md).

**Soil & ground**

- Dirt by composition (clay-rich, sandy, silty, loamy, rocky) with different fertility and drainage.
- Multiple grass and ground-cover types tied to climate zone and soil.
- Distinct mud, clay (several kinds with different firing properties), and sand (colored by source rock), plus gravel.
- Soil nutrients and depletion tie into farming ([§4.6](#s4-6)).
- Soil depth and horizons vary coherently with parent material and terrain history. Rock/soil permeability governs flooding/drainage and linked springs/cave rivers; wind forms, local slope failures, underground cave-ins and natural obstructions use the same material properties. Sinkholes are excluded.

**Permafrost & ice cementation**

- Permafrost: polar and alpine ground has a dense, frozen subsurface layer that is harder than obsidian. Early tools can't dig it; players need advanced tooling, must thaw it with heat (fire, steam, thermal tools), or must route around it through natural caves.
- Cold-weather hardening (ice cementation): freezing ambient temperatures combined with moisture inside a block raise the hardness and blast resistance of soils, gravels, and sedimentary stone until heat or industrial steam thaws them. Both effects run through the property system ([§4.14](#s4-14)): hardness and blast resistance are functions of temperature and moisture state, so native and adapted blocks behave the same way.
- Interactions (suggested): seasonal freeze and thaw come from the calendar ([§4.2](#s4-2)); frozen ground resists blasting ([§4.15](#s4-15)); thawing ground can slump or collapse ([§4.10](#s4-10)); cold exposure feeds the temperature model ([§4.9](#s4-9)); frost shortens growing seasons ([§4.6](#s4-6)).
- Config: permafrost depth and hardness, freeze threshold, ice-cementation strength, and thaw rates (survival/temperature-clothing.toml, hazards/stability.toml, worldgen/terrain.toml).

**1:1 Earth scale option**

- An optional world-gen seed and projection setting that maps real-world elevation, biomes, latitude, and rainfall directly onto the mod's custom climate and geology, so global position decides survival conditions.
- Feasibility notes (suggested; verify for 26.x before committing):
- Horizontal size: at 1 block per meter the Earth is about 40 million blocks around and 20 million pole to pole. That is within vanilla's maximum world border (roughly 60 million blocks across) but far beyond what a player will ever explore, so spawn point, travel, and a map or atlas system need design.
- Vertical size: real elevations span roughly 20 km from ocean trench to peak, far more than the default build height of a few hundred blocks. Plan a separate, configurable vertical scale and a taller custom height range, within whatever cap the world format allows.
- Data: real elevation, land-cover, climate, and geology datasets are large and carry licenses. Decide what is bundled, what is an optional download, and how tiles are pre-processed and cached; check every license before shipping.
- Geology and climate mapping: real lithology can pick the mod's rock types by region ([§4.17](#s4-17)), and latitude, altitude, and ocean proximity drive temperature and rainfall, which is where permafrost, deserts, and rainforests land.
- Calendar link: hemisphere-correct seasons and latitude-based day length (polar day and night) follow naturally if the calendar ([§4.2](#s4-2)) reads latitude.
- Performance: chunk generation must stay fast on servers, so sample pre-built tiles rather than computing from raw datasets at runtime.
- Config (worldgen/terrain.toml): Earth mode on/off, horizontal and vertical scale, data source, sea-level offset, and spawn location. Locked at world creation.

**Trees, leaves, plants**

- Species by climate zone, each with its own wood properties (hardness, burn heat, workability, rot resistance), growth conditions, and products (bark, resin, fiber, fruit, nuts). Full tree catalog in [§4.17](#s4-17).
- Leaves with seasonal color/shed cycles; saplings with realistic growth conditions.
- Wild plants for fiber, dye, forage, and medicine; wild precursors of crops.

**Food, seeds, vegetables, crops**

- Distinct seeds and crops with climate ranges, planting windows, nutrient needs, and rotation effects.
- Foods grouped by nutrient category, with preparation methods ([§4.6](#s4-6)).

**Ore veins (GregTech-style)**

- Large veins holding multiple ore types, restricted to compatible host rocks.
- Surface indicators and prospecting tools with limited range early on.
- Depth bands push players deeper as they advance.

**Climate**

- Define the biome roster and its communities first, then design temperature, moisture and seasonal scenarios that support those biomes. Existing climate bands must not dictate the roster. Quantitative climate ranges and world-placement integration are deferred until after biome planning. Climate eligibility must gate where each biome can generate, alongside terrain, water and other habitat requirements. Climate will also inform crops, survival pressure and seasonal behavior.
- Geographic climate direction applies to ordinary procedural worlds and optional Earth mode: a generally warmer equatorial region and colder northern/southern polar regions, with gradual latitude trends. Elevation, ocean influence, continental interiors and moisture patterns create regional variation; seasons respect opposing hemispheres. Coordinate mapping, equator/pole positions and climate-region scale remain deferred. See [climate geography requirements](BIOME-PLAN.md#geographic-climate-realism--confirmed-direction).
- Ocean currents must follow realistic wind, hemisphere/rotation, coastline/basin, depth and temperature/salinity influences, including distinct surface/deep flow and vertical exchange. Water transport informs coastal climate and marine habitat placement. Plan shared bounded current queries for future transport/fishing interactions; detailed circulation and tide models remain deferred. See [ocean circulation requirements](BIOME-PLAN.md#ocean-circulation--confirmed-direction).

<a id="s4-2"></a>

### 4.2. Central Calendar & Time Scale

- A single calendar service with configurable ticks per day, days per month, and months per year.
- Everything time-based reads from it: seasons, crop growth, animal aging, food spoilage ("souring"), fermentation, curing, kiln and forge work times, rot, and fuel burn.
- Process durations are defined in calendar units (hours/days), so changing day length rescales the whole game consistently instead of breaking balance.
- Presets for server vs single-player (e.g. short days for fast-paced servers, long days for long-term single-player).

<a id="s4-3"></a>

### 4.3. Primitive Tech (PrimalCore + Pyrotech + Primal Tech)

**Age 0 — Primal rules**

- Punching trees yields nothing; logs need a sharp tool.
- Stone and gravel aren't gathered bare-handed.
- First tools use flint, loose rock, and bone in the PrimalCore-inspired starting stage; developed stone tooling and then metals follow. These are alternative material paths, not a requirement to find all three.
- Early crafting is hand-based and slow.

**Age 1 — Fire & Clay**

- Friction firestarting with failure chance; fuel and kindling matter (wood type affects heat).
- Campfire → pit fire → kiln, each reaching higher temperatures.
- Drying and curing racks for hides, food, and clay.
- Clay and pottery: crucibles, molds, vessels, with clay type affecting results.
- Charcoal pits for high-temperature work.
- Early bloomery-style smelting.

**Primal machines**

- Hand-operated devices: drying frames, hand mills, bow drills, pole lathes, spinning tools.
- Stepping stone before mechanical power.

<a id="s4-4"></a>

### 4.4. Metallurgy & Heat

- One heat model: every source and heated item uses the same temperatures.
- Forging with anvils, hammers, work steps, and welding.
- Alloying with precise ratio windows (bronze variants, brass, and later alloys).
- Metal tiers: copper → bronze → wrought iron → steel → advanced alloys.
- Bloomery → blast furnace progression requiring real infrastructure.
- Result quality depends on technique and temperature control.

<a id="s4-5"></a>

### 4.5. Mechanical Power (Create-Compatible Stress Units)

Power is designed around the same model Create uses so a future bridge is simple.

- Rotation speed (RPM) and stress units (SU) are the two core values.
- Sources (hand crank, water wheel, windmill, animal-driven mill) provide stress capacity that scales with rotation speed.
- Machines (millstone, saw, hammer, pump, drill, lathe) impose stress impact that scales with speed.
- A network works while total impact ≤ total capacity; overstress stops the network (or breaks weak components, configurable).
- Transfer through axles, gearboxes, belts/chains, and gear ratios (trade speed for torque).
- Placement matters: water wheels need flowing water, windmills need open sky.
- Create compat plan: expose a stress-unit interface and unit conversion so the mod's networks can connect to Create's kinetic networks as an optional bridge. The base mod works fully without Create.

<a id="s4-6"></a>

### 4.6. Food, Farming & Animals

Expanded October 29 research specifications: [agriculture](systems/AGRICULTURE-SYSTEM.md), [animal ecology and original predators](systems/ANIMAL-ECOLOGY-AND-PREDATORS.md), [shared contracts](systems/SHARED-SYSTEM-CONTRACTS.md), and [phased feature roadmap](systems/SYSTEMS-ROADMAP.md). These add detailed proposed behavior and gates; they do not claim implementation.


- All food spoils; preservation (salting, drying, smoking, pickling, cooling) is a core skill.
- Nutrition across multiple groups with long-term effects.
- Seasonal planting windows, soil nutrients, and fertilizer cycles.
- Animals with familiarity, breeding cycles, age, and product schedules.
- Hunting, butchery, fishing, and animal-product yields depend on individual animal weight with species-specific proportions and recovery rules; hides feed clothing and leather chains. See the owner decision update for mass accounting and recurring products.

<a id="s4-7"></a>

### 4.7. Danger & Hardcore Rules

- Darkness is dangerous; unlit areas are more hostile.
- Original fauna, monsters, hostile factions and named villains replace vanilla mobs. Authored regions and explicit world/quest progression set danger; player level alone does not silently rewrite a nearby actor or grant hidden immunity. The exact encounter scaling model remains proposed in the [MMO threat framework](systems/MMO-THREATS-AND-ENCOUNTERS.md).
- Keep inventory on by default, configurable off by the world/server owner. Cargo remains for recovery and respawn fatigue escalates only with closely spaced deaths and resets after a configurable death-free interval; see the [decision update](design-decisions-2026-10-18.md#death-and-recovery).
- Shelter, fire, and light have real radius and fuel costs.
- Real hazards: cave-ins, gas pockets, lava, drowning.

<a id="s4-8"></a>

### 4.8. Ore Processing Chain

- Mine (proper tool tier)
- Crush (hand, then mechanical)
- Wash (water-based separation, byproducts)
- Process (smelt/purify)
- Form (cast, forge, or weld)
- Each ore yields byproducts, making vein choice strategic.
- Better processing equals higher yield.

<a id="s4-9"></a>

### 4.9. Clothing & Thermoregulation (TFC+ must-have)

**Temperature model**

- Ambient temperature = biome + season + time of day + altitude + weather.
- Local modifiers: fires, shelter, wind, body activity, wetness.
- The player has a comfort range; going outside it produces graded effects (shivering and slowed recovery up to hypothermia, or heat exhaustion and dehydration in extremes).

**Clothing**

- Layered slots: head, torso, legs, feet, hands, and outer layer (cloak/coat).
- Base, mid, and outer layers stack insulation but add weight and bulk.
- Properties per garment: insulation, wind resistance, water resistance, breathability, weight, durability.
- Materials by age: hide and fur, linen, wool, leather, then finer fabrics.
- Wetness (rain, swimming, snow) reduces insulation until dried.
- Armor interacts with temperature (adds weight and either insulation or heat penalty).

**Production chain**

- Fiber/wool → spinning → weaving → tailoring.
- Hide → scraping → tanning → leather working.
- Dyeing from plants and minerals.
- Repair and patching rather than instant replacement.

**Seasons**

- Winters and cold biomes are lethal without proper clothing and shelter; summers and hot biomes demand light clothing and water.

<a id="s4-10"></a>

### 4.10. Realism, Survival Layer & Encumbrance

- Hunger and thirst; water must be sourced and made safe.
- Injury system with lasting effects that need treatment.
- Fatigue and sleep.
- Encumbrance and stacks (inventory.toml): the count shown on a stack in the UI is decoupled from real mass. Each item has a true weight from its property profile ([§4.14](#s4-14)), and the player's total carried mass drives movement speed, stamina use, and tool use. Global and per-item stack limits are fully configurable (for example, stacking ingots anywhere from 6 to 512), so servers can tune inventory pressure without touching weight balance.
- Engineering caveat (suggested; verify for 26.x): vanilla's per-item stack-size cap is low (believed to be 99 in current versions), so larger stacks need a custom inventory, slot, and serialization layer that also covers containers, item transfer between them, and networking. Treat that as part of the custom-inventory work.
- Containers (suggested): chests, barrels, kegs, and packs get their own configurable capacity limits by slots, weight, or both.
- Weight and load show in item tooltips and a load meter through the inspection layer ([§4.10.1](#s4-10-1)).
- Structural integrity with supports for tunnels and buildings.
- Fire spread to flammable materials.
- Tool wear and realistic repair.

<a id="s4-10-1"></a>

#### 4.10.1. Native Field Guide & Inspection Tools

**Native field guide**

- A custom in-game knowledge base built into the mod and loaded from Markdown and datapack files, with no dependency on Patchouli or similar book mods.
- Entries unlock dynamically through player discovery (finding a rock, crafting a tool, surviving a freeze).
- Pages come from two sources: hand-written chapters (ages, heat, power, survival) and pages generated from data (every rock, tree, ore, and fabric with its property sheet, plus the generated config reference from [§4.13](#s4-13)).
- Suggested: search, bookmarks, cross-links between entries, standard language files for localization, and datapack extensibility so add-ons can ship their own entries. Servers can switch discovery gating off.

**Abstract inspection tools**

- Specialized tools (prospecting picks, survey instruments, and similar) query block, item, and entity property profiles ([§4.14](#s4-14)) through an independent inspection API owned by the mod, and show live readouts (soil pH, hardness, stress capacity, temperature, load) in the mod's own client overlay.
- Soft integrations: the same API can feed third-party tooltip mods such as Jade through optional compatibility modules that only load when those mods are present. Core systems never depend on them, which keeps the core independent and portable to other platforms later.
- Gating (suggested): what a readout reveals depends on the tool's tier and on what the player has discovered. Third-party tooltips must go through the same gate; otherwise a tooltip mod could leak information the High TISM preset is meant to hide.
- Config (guide-inspection.toml): discovery gating, readout tiers per tool, overlay on/off, and per-integration switches.

<a id="s4-11"></a>

### 4.11. Multiplayer & Server Design

- Server-authoritative: temperature, injuries, hunger, and collapse calculated on the server.
- Per-player state (injury, clothing, temperature) with a shared world clock.
- Performance budgets for collapse checks and heat updates (chunk-based, throttled).
- Server options for hardcore death (team revive, grace periods, item-keep rules) and per-server difficulty presets.
- Single-player gets the same systems with presets tuned for long solo runs.

<a id="s4-12"></a>

### 4.12. Building & Art Direction

- Reuse TFC and TFC+ style material logic and add new content in a similar spirit.
- Conquest Reforged-inspired building set: many shape variants per material (stairs, slabs, walls, arches, columns, roof tiles, timber framing, thatch, detailed trims).
- Variants generated from data (material × shape) so adding a new stone or wood automatically yields a full building set.
- Cohesive, grounded, slightly weathered visual style.

<a id="s4-13"></a>

### 4.13. Configuration Architecture

Principle: anything the mod adds, changes, or removes can be turned off, scaled, or swapped by the player or server owner, and settings are grouped into several files by what they control instead of one giant file.

**Required domain organization — migration not yet implemented**

All Deep Primal-owned user configuration lives beneath `config/deepprimal/`, then domain subdirectories. World/server overrides mirror that organization beneath `<save>/serverconfig/deepprimal/` where the host supports them. The audited alpha 21 source still uses some flat filenames; [configuration and ore authoring](CONFIGURATION-AND-ORE-AUTHORING.md) defines scope, migration, precedence and the proposed external visual editor. File names below describe the target layout.

| File | Purpose |
| --- | --- |
| `core/settings.toml` | preset selection, global switches, config version |
| `core/vanilla-content.toml` | per-module vanilla removal/re-add modes ([§0.2](#s0-2)) |
| `worldgen/terrain.toml` | terrain scales, creation profile, optional Earth seed/projection and supported height | 
| `worldgen/geology.toml` | geological unit/layer/contact tuning | 
| `worldgen/ores.toml` | deposit enable states, broad scales and supported per-ID tuning | 
| `worldgen/biomes.toml`, `worldgen/climate.toml`, `worldgen/water.toml` | separated biome, later climate and native hydrology rules | 
| `graphics/quality.toml`, `graphics/water.toml`, `performance/client.toml`, `performance/server.toml`, `performance/diagnostics.toml` | presentation and performance/diagnostics with explicit scope and retained module ownership |
| `survival/inventory.toml` | global and per-item stack sizes, weight multipliers, encumbrance limits |
| `survival/calendar.toml` | day length, month/year length, season length |
| `survival/needs.toml` | hunger, thirst, injury, fatigue |
| `survival/temperature-clothing.toml` | temperature model, clothing, wetness, seasons, permafrost |
| `ecology/food-farming.toml` | decay rates, nutrition, crops, animals, preservation |
| `ecology/fauna-threats.toml` | mob roster, spawn rules, danger scaling |
| `hazards/stability.toml` | collapse, fire spread, cave gas, darkness, ice cementation |
| `technology/metallurgy-heat.toml` | heat tolerances, alloy windows, forging difficulty |
| `technology/ore-processing.toml` | vein sizes/rarity, processing steps, yields, depletion |
| `technology/power.toml` | stress units, overstress behavior, Create bridge |
| `technology/signals.toml` | wire-style redstone mode, signal rules, gates, bundled cables |
| `hazards/explosives.toml` | powder-keg vs vanilla TNT, blast scale, ignition, cave-in triggers |
| `materials/building.toml` | generated variants, shape sets, material list |
| `integration/adapters.toml` | adapter modes, auto-inference, foreign content rules |
| `multiplayer/rules.toml` | death rules, per-player state, performance limits |
| `ui/guide-inspection.toml` | field guide discovery gating, inspection tool tiers, soft-integration switches (e.g. Jade) |
| `core/modules.toml` | module enable/disable states, dependency overrides, default module set |
| `graphics/lod.toml` | LOD distances and tiers, tile cache, tint, server send options |
| `ui/client.toml` | HUD, units, visual and audio options (per-player) |
| `presets/primal.toml` | Primal preset overlay |
| `presets/hardcore.toml` | Hardcore preset overlay |
| `presets/high-tism.toml` | High TISM preset overlay |

**Rules for the config system**

- Grouped by domain: each file only holds settings for its own system, with clear section headers and generated comments explaining every key, its default, and its valid range.
- Scopes: per-player client settings, global common settings, and per-world server settings (synced to clients) are kept separate so servers control gameplay while players control display.
- Presets are overlays: presets/*.toml set many keys at once; any key can still be overridden individually. Selecting a preset in core/settings.toml applies it; scope-correct local overrides follow the documented precedence.
- Content toggles at item level: individual rock types, soils, trees, crops, metals, fabrics, ores, and mobs can be enabled/disabled by id lists, not just whole categories.
- Data-driven definitions: content definitions live in datapacks; configs hold supported rules/scales/toggles and per-ID tuning. Flexible multi-ore/gem deposits use one native schema for datapacks, optional KubeJS and GUI authoring, with exact precedence, lifecycle and immutable compilation defined in [ore/config contract](CONFIGURATION-AND-ORE-AUTHORING.md).
- Reload behavior is labelled: each key states whether it applies live, on restart, or only to new chunks/new worlds.
- Validation and migration: each key declares rejection/clamping policy with diagnostics; invalid structural definitions are rejected rather than silently rewritten. Versioned migration and backups preserve user edits and resolve old/new path conflicts.
- Server locking: server owners can lock keys so clients can't override gameplay-affecting values.
- In-game config screen: a searchable UI grouped the same way as the files (planned after the file-based system works).
- External companion editor (proposal): shared schemas/validation, sliders plus numeric entry, ID/tag selectors, staged changes, profiles and seeded ore/terrain previews. It can edit ordinary settings while Minecraft is closed; packaging and implementation remain open.
- Export/share: dump a "diff from preset" so players can share their setup.

**Config coverage policy: "if it can be a config, it is"**

- No hardcoded balance. Every gameplay number (rates, durations, temperatures, damage, yields, ranges, costs, spawn weights, thresholds) is a named tunable with a default, registered in one central place. A hardcoded gameplay constant is treated as a bug in code review.
- What gets exposed (non-exhaustive):
- World: terrain scale, rock-layer thickness, climate ranges, vein size/rarity/depth, plant density, biome sizes, 1:1 Earth mode (horizontal and vertical scale, data source), permafrost depth and hardness, ice-cementation strength and freeze threshold
- Time: day, month, season, and year length; per-system time multipliers (spoilage, growth, aging, fuel burn, process times)
- Survival: hunger/thirst rates, injury chance and severity, fatigue, temperature comfort range, clothing insulation scaling, wetness rates
- Inventory: global and per-item stack sizes, weight multipliers, container capacity, encumbrance thresholds and penalties
- Crafting/tech: heat tolerances, alloy ratio windows, forging difficulty, yields, tool durability, stress capacity/impact per machine, overstress behavior
- Danger: collapse sensitivity, fire spread, mob stats and spawn rules, darkness penalty, death/respawn rules
- Player-facing: HUD layout and visibility, temperature units (°C/°F), keybinds, colorblind and accessibility options, sound and particle levels
- Server: per-player vs shared rules, performance budgets, anti-grief and permission options
- Per-item overrides: properties of individual rocks, soils, woods, crops, metals, fabrics, ores, and animals can be overridden by id (hardness, burn heat, insulation, nutrition, decay rate, and so on).
- Recipes and loot: data-driven so players can edit them through datapacks, plus global multipliers in config for quick tuning.
- Exposure tiers: Basic (a few dozen high-impact knobs and presets), Advanced (per-system tuning), and Expert (raw constants, flagged "may break balance"). The default view shows Basic; the rest is one switch away, so full control never becomes clutter.
- Guardrails: safe ranges with warnings, experimental flags for risky options, and reset-to-default per key, file, or domain.
- Locked at world creation: options that can't change safely mid-world (registry-affecting or terrain-shaping settings) are labelled and locked after the world is created.
- Self-documenting: a config reference is generated from the central tunables registry (key, default, range, tier, reload behavior) and shipped with the field guide.

<a id="s4-14"></a>

### 4.14. Adapter Layer (Universal Property Mapping)

Decision: adapters are in v0.1. Any block, item, or entity that isn't native to the mod takes on the mod's rules, so re-enabled vanilla content behaves like the mod's own, across the board.

Core idea: systems read properties, not ids. Soil, gravity, collapse, breaking, heat, and decay all query one shared property registry. Native and adapted content both resolve to a property profile, so a vanilla dirt block placed in the world is treated exactly like the mod's own soil.

**Block properties (shared schema)**

| Group | Properties |
| --- | --- |
| Material | class (stone, soil, sand, gravel, clay, wood, metal, fabric, organic), subtype, density/mass |
| Breaking | hardness, required tool class and tier, break-time curve (time to destroy = f(hardness, tool, tier, modifiers)), blast resistance; hardness and blast resistance can be raised by freezing and moisture (ice cementation, permafrost: [§4.1](#s4-1)) |
| Gravity & collapse | loose vs cohesive, angle of repose (slumping), support strength, max unsupported span, load capacity, collapse behavior (fall, crumble, slump) |
| Soil | fertility, nutrients, drainage, moisture retention, erosion |
| Heat & fire | flammability, ignition temperature, burn heat and duration, thermal conductivity, melting point |
| Water | porosity, behavior when wet |
| Dynamic state (sparse, in chunk data) | moisture, nutrient levels, temperature, frozen / ice-cemented state |

Item properties: weight (true mass per item, independent of stack size; drives encumbrance, [§4.10](#s4-10)), decay rate and nutrition groups (food), temperature and heat capacity (heatable items), wear and durability, insulation / wind / water resistance (clothing and armor).

Entity properties (optional per module): calendar-driven aging and breeding, temperature tolerance, diet and nutrition, danger scaling.

**Resolution order (first match wins)**

- Explicit adapter entry for that id (datapack or config)
- Tag rule (e.g. everything tagged as dirt gets a loam soil profile)
- Auto-inference from the content's existing attributes (hardness, blast resistance, sound type, material, tags, tool requirement)
- Fallback defaults (configurable: apply default, warn, or block placement)

Shipped profiles: explicit mappings for every vanilla block and item (e.g. vanilla dirt → loam, gravel → coarse loose, sand → fine loose, stone → a mod rock class, logs → a wood species class, foods → decay plus nutrition groups).

**Adapt vs convert (per-module config)**

- adapt (default): keep the vanilla block and apply the mod's properties to it.
- convert: swap the vanilla block for the mod's equivalent when it is placed or generated (e.g. vanilla dirt becomes the mod's loam).

**What adapted content gets**

- Soil: fertility, drainage, nutrient depletion; plants growing on it follow the mod's crop rules.
- Gravity & cave-ins: loose blocks slump or fall by angle of repose; adapted stone and soil count toward structural integrity; unsupported spans collapse the same as native blocks.
- Break time: time to destroy follows the mod's hardness and tool curve, so vanilla stone isn't mined instantly with the wrong tool.
- Heat & fire: adapted blocks burn, conduct heat, and melt by the mod's heat model.
- Items & food: vanilla food spoils and has nutrition groups, everything has weight, and vanilla clothing/armor gets insulation and heat penalties.

Foreign content (other mods): the same pipeline applies, with auto-inference plus a report of anything unmapped. Config: adapters.foreign = off | auto | strict.

**Config & tooling**

- Per-system toggles (e.g. adapt soil but not gravity, or adapt blocks but not entities), set per module in adapters.toml.
- Datapack adapters/ folder for explicit mappings and tag rules.
- Auto-adapter results are exported to an editable adapter report and a list of unmapped content.

**Performance**

- Profiles resolve at load/reload into flat per-block-state tables, so there are no per-tick lookups by id.
- Dynamic state is stored sparsely in chunk data.

**Limits (honest scope)**

- Adapters apply properties; they don't rewrite special game logic. TNT and redstone are handled by dedicated systems ([§4.15](#s4-15), [§4.16](#s4-16)); remaining special-case logic (pistons, portals, and similar) goes on a special-case list and is left as-is, simplified, or disabled per module.
- Modded content with custom logic may only be partially adaptable.
- How property overrides hook into the game (events, mixins, or custom block logic) depends on the 26.x loader and gets decided with the version/loader choice ([§8](#s8)).
- Plan an adapter test world containing every vanilla block and item to verify behavior.

<a id="s4-15"></a>

### 4.15. Explosives (Powder-Keg Mode)

Decision: by default, TNT behaves like a powder keg. explosives.mode = vanilla restores vanilla TNT behavior.

Reference design: TFC's own powderkeg (confirmed by the user), as described in the TerraFirmaGreg field guide (text is in English). That guide is for a TFC-based modpack, so some recipe details may be pack-specific; check TFC's own documentation for exact numbers before locking them. Nothing here is attributed to Better with Mods.

**Powder-keg mode (default)**

- A keg is a container block with a small inventory (reference design: 12 slots, gunpowder only) that must be filled with gunpowder to be dangerous; an empty keg is inert.
- The keg must be sealed before it can be ignited, and can be unsealed again, like a barrel (reference design).
- Blast strength scales with the amount of powder loaded, up to a configurable capacity.
- Gunpowder comes from the mod's own processing chain (reference: saltpeter, sulfur, and charcoal powder, ground with a quern, with a mechanical mill later), so there are no stacks of instantly crafted TNT. Kegs are built from a barrel-style recipe with real materials in line with the age they appear in.
- Ignition sources (each toggleable): fire, a lit fuse, or a signal from the wiring system ([§4.16](#s4-16)). Kegs near fire can ignite.
- Chain reactions: a keg's blast sets off nearby kegs (as in the reference design); configurable.
- Drops: blocks broken by a blast drop their items, scattered rather than at their original spot (reference design), which suits blast mining.
- Uses the property system ([§4.14](#s4-14)): blast resistance comes from block properties, different rock types fracture differently, and damage to supports can trigger cave-ins (structural integrity, [§4.10](#s4-10)).
- Gameplay role: controlled blasting for mines and quarries and for demolition, with real hazards when storing, carrying, or lighting them.

**Vanilla mode**

- Classic TNT behavior. Adapters ([§4.14](#s4-14)) can still apply block properties so blast damage respects the mod's materials, as a separate toggle.

Config (explosives.toml, examples): mode, keg capacity, powder-to-power scale, fuse time, allowed ignition sources, chain reactions on/off, terrain damage on/off, entity damage scale, cave-in trigger on/off, and a server option to restrict explosives in protected areas.

<a id="s4-16"></a>

### 4.16. Signals & Wiring (Wire-Style Redstone)

Decision: redstone is kept as the signal system but reworked to behave like wires, inspired by the Wired Redstone mod (CurseForge page). The change is largely cosmetic: wires look like actual cables.

**What Wired Redstone does (from its page)**

- Multipart wires, so several wires can share one block space.
- Analog signals.
- Wires can be cut off by blocks or facades, so they don't connect through corners unintentionally.
- Insulated wires and red-alloy wires, plus bundled cables carrying multiple signals.
- Logic gates, and a machine for crafting circuit parts (the Redstone Assembler).
- A wire-net graph engine for connections (it uses GraphLib).
- Status: beta; Fabric/Quilt; the latest release listed is for 1.20.1 (May 2024); MIT license. So its concepts inform this design; it can't be used as a drop-in on 26.x.

**Our design**

- Look (cosmetic focus): wires are rendered as real cable models with thickness rather than flat dust: straight runs, corners, T-junctions, and end caps that adapt to their connections, and visible connectors where cables enter devices. Insulated cables show their color or channel; power level can be shown by a subtle glow or brightness change. Cover/facade parts let builders hide cables for cleaner builds. All of this is data-driven (models and materials), in keeping with the Conquest Reforged-inspired art direction ([§4.12](#s4-12)).
- Placement: wires attach to block faces; multiple wires per block space; explicit connection rules (adjacent, corner, and cut-off by blocks or facades).
- Signal: analog strength with optional loss over distance and a configurable max net size.
- Wire types: conductive wire, insulated (channel-isolated) wire, and bundled cable carrying multiple channels in one line.
- Parts: logic gates (AND/OR/NOT, latch, timer/delay), sensors (pressure, detectors), and actuators (doors, valves, and clutches/brakes that gate mechanical networks, [§4.5](#s4-5)).
- Materials & progression: conductors come from the metallurgy chain; parts are crafted at the mod's own assembly bench; suggested placement is basic wires in Age 3–4 and gates and bundled cables in Age 5.
- Mechanical bridge: signals control stress networks through clutches and brakes, so the two systems work together.
- Vanilla redstone: signals.mode = wired (default) | vanilla | both. In wired, vanilla dust, repeaters, and comparators are converted or disabled and their jobs are done by wires and gates. In both, they coexist, with bridge parts for crossing between systems (to be designed).

**Engine requirements**

- Wire nets stored as a graph with incremental updates, rather than per-block neighbor updates, for performance and analog support.
- Persistence that survives crashes. Wired Redstone's page documents wires disconnecting after server crashes, so include an admin repair command (e.g. /deepprimal signals rebuild <area>).

Licensing: Wired Redstone is MIT-licensed; check the licenses of its dependencies (LibMultiPart, GraphLib) and its origin project RSWires before reusing any code, and credit any reused code or ideas.

Config (signals.toml, examples): mode, max net size, loss per block, analog on/off, wire cut-off rules, gate availability and tier, bundled cable channel count, update budget per tick, and per-part enable lists. Cosmetic keys (client-side): cable thickness and style, power glow on/off, connector and end-cap style, and a performance option to simplify or hide cable rendering at distance.

<a id="s4-17"></a>

### 4.17. Natural Catalog: Stones, Trees & Rendering

Expanded named lists: [natural type catalog](NATURAL-TYPE-CATALOG.md). The original seed entries below remain preserved; the linked planning catalogs add named types, explicit group splits and environmental forms. New entries remain candidates, and implementation stays deferred until requested.

Requirement: every discoverable type of stone and tree is included, each with its natural function, and the mod will build whatever rendering work is needed to display it all well, including a custom engine layer inside the mod if necessary.

**Coverage policy**

- "All" is defined as every rock and tree type that has a distinct natural function or look across the world's geology and climates. There are tens of thousands of tree species, so the aim is to cover every distinct role, plus every regionally or historically important species, rather than hand-building each one.
- Proposed coverage tiers: Tier A (major, widely found types), Tier B (regional types), Tier C (rare and specialty types). Shipping Tier A first and expanding B/C remains subject to the catalog-depth decision in [§8](#s8).
- Everything is defined by data property sheets, so the catalog expands through datapacks and community packs without code changes (principles 7 and 8).

Stone property sheet: class and subtype; formation and depth band; hardness and tool tier; fracture type (conchoidal, blocky, layered, fibrous), which drives knapping, drops, and cave-ins; porosity and permeability; density; heat resistance and conductivity; weathering product (the soil, sand, or clay it breaks down into) and its effect on soil pH; workability (carving, polishing); appearance (color range, banding, grain); ore and mineral associations; uses (building, tools, abrasive, flux and lime, fuel, refractory, pigment); collapse behavior (bedding planes, joint spacing).

Tree property sheet:

- Wood: density, hardness, flexibility, rot and insect resistance, burn heat and ash, workability.
- Growth: rate, lifespan, max height, climate and soil preference, light needs, drought and flood tolerance.
- Form and cycle: deciduous, evergreen, conifer, or palm; leaf color and shedding, flowering, and fruiting seasons.
- Products: fruit, nuts, sap or syrup, resin and tar, bark (tannin, cork, fiber), bast fiber, leaves or needles, flowers and honey.
- Ecological functions: nitrogen fixing, shade and microclimate, windbreak, erosion control, water uptake in wet soils, wildlife habitat, leaf-litter effect on soil acidity, fire adaptation.
- Management: coppicing and regrowth, propagation.

**Seed catalog: stones (starting list to expand; ground materials such as sand, gravel, clay, and peat are in [§4.1](#s4-1))**

**Igneous**

| Rock | Natural function |
| --- | --- |
| Granite | Hard, durable building and foundation stone; weathers to sandy, acidic soils; slow to work |
| Diorite | Hard, speckled building stone; durable paving |
| Gabbro | Dense, dark, tough; paving and heavy construction; weathers to richer soils |
| Basalt | Dense and fine; columns and paving; weathers to fertile clay soils; heat-stable |
| Andesite | Workable volcanic building stone |
| Rhyolite | Silica-rich and fine; sometimes knappable |
| Obsidian | Volcanic glass with razor-sharp fracture; blades and points |
| Pumice | Porous, floats; abrasive, lightweight aggregate, soil conditioner |
| Tuff | Soft, easy to carve, light and insulating building stone |
| Scoria | Porous volcanic rock; drainage and lightweight aggregate |
| Peridotite | Dense deep rock; ore host (e.g. chromium, nickel) |
| Pegmatite | Coarse-crystal rock; host of mica, feldspar, and gems |
| Dolerite | Tough; tool stone and road stone |
| Syenite | Granite-like, alkaline building stone |

**Sedimentary**

| Rock | Natural function |
| --- | --- |
| Sandstone | Layered and porous; easy to carve; grindstones and whetstones; aquifer |
| Limestone | Building stone; burned to quicklime for mortar, fertilizer, and flux; dissolves in acid; forms caves |
| Chalk | Soft limestone; marking, lime, soil sweetener; holds flint nodules |
| Dolomite | Magnesium carbonate; flux and building stone |
| Shale | Layered and brittle; clay source; high cave-in risk |
| Mudstone / claystone | Soft; clay source |
| Siltstone | Fine layered stone; weak bedding |
| Conglomerate | Cemented gravel; strong, rough building stone |
| Breccia | Angular fragments; decorative building stone |
| Greywacke | Tough, dark sandstone; paving and tools |
| Chert | Hard silica; knappable; fire-striking |
| Flint | Knapped tools and arrowheads; strikes sparks on steel for fire |
| Coal (lignite, bituminous, anthracite) | Fuel of increasing heat; carbon source |
| Gypsum | Soft; plaster and soil conditioner |
| Rock salt (halite) | Preservation, flavor, trade |
| Travertine | Banded, porous; decorative building stone |
| Ironstone / banded iron | Iron ore host |
| Coquina | Shell limestone; coastal building stone |
| Laterite | Tropical weathering product; hardens into bricks; aluminum ore source |

**Metamorphic**

| Rock | Natural function |
| --- | --- |
| Marble | Carves and polishes well; sculpture and fine building; lime source |
| Slate | Splits into thin sheets; roofing and flooring; waterproof |
| Schist | Layered and glittering; weak planes (collapse risk); gem host |
| Gneiss | Banded and strong; heavy building stone |
| Quartzite | Very hard and durable; grinding and heavy-duty stone |
| Phyllite | Fine layered; roofing stone of lower quality |
| Hornfels | Hard, tough; tool stone |
| Amphibolite | Dense, dark; building and paving |
| Serpentinite | Soft, green; carving; asbestos-associated hazards |
| Soapstone (steatite) | Soft, holds heat; stoves, cookware, carving |
| Eclogite / granulite | Rare deep rocks; specialty stone |

**Seed catalog: trees (starting list to expand)**

**Temperate broadleaf and fruit**

| Tree | Natural function |
| --- | --- |
| Oak | Hard, durable timber; acorns; tannin-rich bark; wildlife habitat |
| Beech | Dense hardwood; beechnuts; deep shade |
| Ash | Tough, flexible wood; tool handles; fast growth |
| Elm | Interlocking grain, water-resistant timber |
| Maple | Sap for syrup; hard, fine wood |
| Birch | Pioneer on poor soil; bark for waterproofing, containers, tar, and fire-starting; light wood |
| Hickory | Very tough, shock-resistant tool wood; nuts; smoking wood |
| Walnut | Valuable timber; nuts; hulls for dye |
| Chestnut | Nuts; rot-resistant timber |
| Linden (lime) | Bast fiber for rope; honey flowers; soft carving wood |
| Hazel | Nuts; coppiced for wattle and rods |
| Hornbeam | Very hard, dense wood |
| Willow | Grows by water; flexible rods for baskets; erosion control; bark for pain relief |
| Poplar / aspen | Fast-growing, soft; pioneers |
| Alder | Nitrogen fixing; wet soils; rot-resistant underwater |
| Apple / pear / cherry / plum | Fruit and blossoms; wood for smoking and carving |
| Olive | Oil; very hard wood; dry-climate crop |

**Conifers**

| Tree | Natural function |
| --- | --- |
| Pine | Resin for pitch, tar, and turpentine; straight timber; acidic litter |
| Spruce | Light, resonant, straight timber; boreal staple |
| Fir | Straight, soft timber; shade tolerant |
| Larch | Deciduous conifer; rot-resistant timber |
| Cedar | Aromatic; insect and rot resistant; chests and shingles |
| Juniper | Berries; aromatic smoke; drought hardy |
| Yew | Elastic wood for bows; toxic foliage |
| Cypress | Rot-resistant timber |
| Redwood / sequoia | Huge; rot and fire-resistant bark and timber |
| Hemlock | Bark tannin for tanning |
| Bald cypress | Swamp tree; rot-resistant timber; knees in standing water |

**Tropical, subtropical, arid, and coastal**

| Tree | Natural function |
| --- | --- |
| Teak | Oil-rich, extremely durable timber |
| Mahogany | Stable, fine-grained timber |
| Ebony | Very dense, dark wood |
| Rubber tree | Latex |
| Coconut palm | Food, fiber, shells, thatch |
| Date palm | Food; desert oases |
| Bamboo (giant grass) | Fast-growing structural material |
| Mango / cacao | Fruit and seed crops |
| Kapok (ceiba) | Fiber for padding; tall canopy tree |
| Baobab | Stores water; hollow trunks for shelter |
| Acacia | Savanna shade; gum; thorns; nitrogen fixing |
| Mesquite | Hard, dense wood; edible pods; nitrogen fixing |
| Mangrove | Salt-tolerant coastal protection; tannin |
| Eucalyptus | Fast-growing; oils; fire adapted |
| Cork oak | Renewable cork bark; fire resistant |

**How natural functions plug into gameplay**

- Crafting and tools: flint, chert, and obsidian for knapping; ash and hickory for handles; yew for bows.
- Heat and fuel: wood burn heat and rot rating, coal grades, soapstone and refractory stones for kilns and stoves.
- Soil and farming: weathering products and soil pH from rock type; leaf litter acidity; nitrogen-fixing trees improving soil.
- Construction: hardness, workability, rot resistance, weight, and span limits feed building and structural integrity ([§4.10](#s4-10), [§4.14](#s4-14)).
- Mining and hazards: fracture type and bedding set cave-in risk and drops; ore associations guide prospecting.
- Chemistry and processing: limestone to lime, salt for preservation, gypsum for plaster, bark tannin for tanning, resin for tar and sealant.

**Scale strategy (variant explosion)**

- Define each material once and generate its blocks, items, and shapes (material × shape) from data.
- Procedural textures: base texture, palette, and noise or pattern layers per rock and wood, so hundreds of types don't need hundreds of hand-painted sheets. Hand-paint only where it matters.
- Plan for texture atlas, block-state, and memory budgets, and stress-test with the full catalog early.
- Seasonal and climate tinting (leaf color, frost, wetness) driven by parameters rather than separate assets.

**Rendering approach ("engine inside the mod")**

- Stage 1: data-driven models and generated textures through the platform's normal rendering pipeline.
- Stage 2: custom rendering where the normal pipeline falls short: adaptive cable meshes ([§4.16](#s4-16)), parametric trees generated from species data (trunk, branches, crown, seasonal foliage), Conquest Reforged-style detailed block shapes, level-of-detail, and shader effects (wetness, frost, heat shimmer, weather).
- Stage 3 (optional): a fully custom chunk renderer if earlier stages can't meet quality or performance targets.
- Build order (locked): rendering and world-gen come first. Stage 1 core rendering (data-driven models, generated textures, the material × shape pipeline) and custom world-gen (rock layers, climate, permafrost, the 1:1 Earth option) are the mandatory first milestone for v0.1, and gameplay systems build on top of them. Stages 2 and 3 are added as the stress tests show they are needed.

Platform: a custom rendering layer needs client-side code. A Minecraft mod can ship that. Hytale's developers have said modding is server-side first and that they don't plan to support client mods (Hytale modding strategy post), so a custom renderer wouldn't be possible there unless that changes. A fully standalone engine allows it at a much higher cost. Decision: the platform is a NeoForge mod on Minecraft 26.x, which can ship client-side rendering code; Hytale and a fully standalone engine were considered and set aside for v0.1; the platform-neutral core ([§0.4](#s0-4)) keeps both open for later.

<a id="s4-18"></a>

### 4.18. Modules & Datapack Architecture

Requirement: gameplay features are optional modules with datapack-driven definitions. The required core provides shared services. The default setup works with no configuration; runtime behavior and rendering still require native code.

**What a module is**

- A named bundle of data (registries, recipes, loot, tags, adapter mappings, property sheets) plus a manifest: id, version, dependencies, conflicts, default state, and whether it is locked at world creation.
- Examples: world.geology, world.earth, world.permafrost, survival.thermoregulation, survival.encumbrance, tech.mechanical, tech.signals, tech.explosives, core.adapters, ui.guide, ui.inspection, render.far_terrain, and the vanilla.* modules from [§0.2](#s0-2).
- A core module is always on: the property registry, calendar, tunables registry, config, and module loader.

**Out of the box**

- Every recommended module ships enabled in the default preset, so creating a world with no setup gives the full intended experience (High TISM is the default target, [§6](#s6)).
- Optional extras (for example world.earth) ship off and are one toggle away. Which modules default on is an open question ([§8](#s8)).
- Presets ([§4.13](#s4-13)) are named module sets plus tunable overlays.

**Delivery as datapacks (suggested; verify the NeoForge details)**

- Each module ships as a built-in datapack inside the jar, enabled by default, and shown on the world-creation datapack screen so players can switch it off.
- Player and pack-author datapacks override a module's data through normal datapack priority, so any definition (rock, tree, recipe, property sheet, adapter mapping) can be replaced without touching code.
- Content that must be registered at startup (blocks, items, entities) is always registered. A disabled module makes its content unreachable and inert (no worldgen, recipes, spawns, or ticking), the same policy as [§0.2](#s0-2). This avoids unregistering content; safety for existing worlds still requires the migration policy in [§8](#s8).
- Behavior-only modules (for example thermoregulation) read their enabled state and rules from data at load and reload, and should skip their update work while off. Reload-safe handler lifecycle and residual overhead must be measured.

**Dependencies and conflicts**

- Manifests declare dependencies and conflicts. For example, tech.explosives can use tech.signals for ignition but doesn't require it. Disabling a module that others depend on warns and offers to cascade.
- World-shaping modules (geology, Earth, permafrost, terrain) are locked at world creation. Behavior modules can change in an existing world under the policy for already-placed blocks ([§0.2](#s0-2)).

**Datapack versus TOML (suggested split)**

- Datapacks hold content and rules: definitions, recipes, module manifests, and module enable states.
- TOML holds tuning and per-player options: scales, rates, HUD.
- Module toggles may have datapack defaults and core/modules.toml server policy; resolve precedence through the shared configuration contract, then freeze world-locked module/generation state. Existing saved-world contracts cannot be silently changed by either route.

Portability: the manifest and data formats are the cross-platform contract ([§0.4](#s0-4)). Each platform adapter interprets them (Hytale asset packs, an own game's loader).

<a id="s4-19"></a>

### 4.19. Worldgen Architecture & Far Terrain (LOD)

**Current direction:** the built-in independent terrain/biome/fluid engine and actual-chunk LOD supersede the earlier heightmap-first proposal. Deep Primal remains the product, with optional LOD ownership separate from graphics and Vulkan. See [architecture and reference decisions](LOD-AND-DRAINAGE-ARCHITECTURE.md), [alpha 20 evidence](VALIDATION-0.0.1-alpha.20.md) and the [current detailed terrain/water/snow/LOD audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md).

- The platform-neutral core owns original deterministic terrain/drainage/material decisions; NeoForge hosts storage, scheduling and rendering integration. Selected independent generators do not delegate their Overworld terrain/biome decisions to vanilla density-function JSON. Previous profiles remain available and existing saves keep serialized geography/height.
- Region plans must connect tributaries, cross-region outlets, lakes and spillways before carving. Derive width/depth/speed, source-water scale, erosion/deposition and suitable surfaces from shared data; complete catalog geology/vegetation remains deferred.
- Native water uses sixteen volume units and bounded local transport/pressure. Integrate rainfall, groundwater, containers/machines, source composition and surface motion explicitly; older eight-unit facilities are not automatically native-engine features.
- Actual-chunk LOD captures delivered/explored authoritative data, including edits, through immutable revisioned snapshots. Unseen-terrain generation/streaming remains open and must not replace authoritative saved/build data with a heightmap approximation.
- Select detail using projected geometric error, three-dimensional bounds, visibility and hysteresis. Retain covering geometry while finer children load. Bound work, RAM/VRAM, uploads, network and persistent caches.
- Preserve block silhouettes, cutout vegetation, materials, snow, lighting and transparent water with explicit layer/depth ownership. Evaluate meshoptimizer as a constrained processing component; the executed LWJGL probe is not a shipped runtime hierarchy.
- Diagnose the owner's enabled "horizonal view" defect through independent renderer/water A/B cases; fix page walls and handoffs while enabled. The legacy procedural horizon is diagnostic and off by default, never an automatic substitute for disabled chunk LOD.
- Benchmark Distant Horizons using matched worlds, camera paths, radius/detail and named hardware. No superiority or full external shader/wave compatibility is established.
- Render distance and vertical world range are separate: more LOD distance cannot create deeper trenches or taller storage. Mariana-scale depth and Earth-mode scaling require separate host/streaming/performance feasibility checks.
- Keep settings effective, staged and persistent with separate gameplay/server and presentation ownership. Preserve the 32×32-default, necessary-detail-only 64×64 world block texture/shared-family tint direction; optional Earth tiles and licensed offline terrain imports are separate sources, not mandatory dependencies.

Open work and acceptance are tracked as P0-RENDER, TERRAIN-01–03, WATER-01–06, OCEAN-01–03, SNOW-01 and LOD-01/02 in the audit and roadmap. Climate supports the chosen biome roster and gates placement afterward, with latitude/poles/equator and realistic ocean influences; it does not dictate which biomes exist.

<a id="s5"></a>

## 5. Progression Ladder (Draft)

| Age | Theme | Unlocks |
| --- | --- | --- |
| 0 | Primal | Knapped tools, shelter, basic fire, fur/hide clothing |
| 1 | Fire & Clay | Kilns, pottery, charcoal, drying, primitive furnaces, linen/leather clothing |
| 2 | Copper & Bronze | Forge, anvil, first alloys, basic prospecting |
| 3 | Mechanical | Water/wind power, mills, saws, gearing, stress network |
| 4 | Iron & Steel | Bloomery/blast furnace, steel tools, deep mining, fine textiles |
| 5 | Refined | Full ore-processing chain, advanced alloys, precision machines |
| 6 | Late Mechanical | Complex mechanical networks, automation within stress limits |

Rotation-based mechanics remain the planned core. Coal/steam trains and boats are a proposed late extension; electricity remains out of scope ([§9](#s9)).

<a id="s6"></a>

## 6. Difficulty & Long-Term Play

Target: GregTech-level depth, built to last hundreds of hours.

| Preset | Intent |
| --- | --- |
| Primal | Accessible on-ramp: realism toggles relaxed, generous prospecting, slower penalties |
| Hardcore | Strong survival pressure with standard gating |
| High TISM (design target / default) | Full realism stack, tight temperature and alloy windows, rarer ore, long multi-step chains, harsher injuries and collapse, minimal hand-holding |

All presets are bundles of the same config options (stored in presets/, [§4.13](#s4-13)), so any preset can be adjusted key by key.

**Long-term play ideas (to refine)**

- Multi-year calendar with seasonal planning as a core loop.
- Deep, branching processing chains with meaningful byproducts.
- Regional resource pressure (veins that deplete or become impractical) so bases and expeditions evolve.
- Settlement-scale goals: large stone buildings, water/wind-powered industry, long-distance supply.
- Slow, satisfying scaling of automation within stress limits.

<a id="s7"></a>

## 7. Integration Conflicts to Resolve

| Conflict | Proposed resolution |
| --- | --- |
| Multiple smelting concepts (TFC, Pyrotech, BWM) | One heat model; kiln, bloomery, and crucible are stages in one chain |
| Ore generation approach | One vein system built on the mod's own rock layers |
| Tool tiers | Single tier table: primitive Ages 0–1, metals Age 2+ |
| BWM-style power vs stress units | Stress units are the canonical model; BWM supplies machine ideas only |
| Time-based balance vs configurable day length | Define all durations in calendar units |
| Early-game friction stacking (PrimalCore + Pyrotech + clothing + realism) | Tune the first hours together so they're hard, not tedious |
| Total conversion vs ecosystem | Accept limited compat; build deliberate bridges (Create) |
| Recipe/content sprawl | Data-driven registries and generated variants |
| Real-world scale vs world-format limits (suggested) | Keep 1:1 Earth an optional mode with its own horizontal and vertical scale settings; verify world-border and height caps for 26.x before locking |
| Large configurable stacks vs vanilla stack cap (suggested) | Build a custom inventory layer where needed; keep stack size and weight independent so balance never depends on the UI count |
| Third-party tooltip mods vs gated information (suggested) | Soft integrations read the mod's own inspection API and obey the same gating, so they can't reveal hidden data |
| Datapack-toggled modules vs content that must be registered at startup (suggested) | Always register content; modules gate reachability and behavior, so disabling is inert rather than unregistering |
| Far-terrain LOD vs generator features a heightmap can't show (caves, overhangs, structures) (suggested) | Far tiers show the surface silhouette only; near tiers use real chunks |

<a id="s8"></a>

## 8. Open Questions & Roadmap Status

Resolved by v0.5: loader (NeoForge), platform (a NeoForge mod rather than Hytale or a standalone engine), and build order (rendering and world-gen first).

1. Minecraft version: which exact 26.x version to target? (The loader is decided: NeoForge.)
2. Adapter scope for other mods: should the auto-adapter cover all foreign content in v0.1, or only vanilla first with other mods following later?
3. Assets and licenses: are you open to reusing TFC art if the license allows, or should everything be original? Either way, check each asset's license before reusing any code or art.
4. Stress-unit scale: match Create's numbers exactly, or use a convertible scale with a bridge?
5. Default day length for single-player vs server presets?
6. Updated: water uses hybrid depletion/recharge; ore veins are finite, with high-reserve bedrock-style deposits. See the owner update for renewal, recycling, and salvage rules.
7. Updated: keep personal inventory is on by default and configurable off; cargo remains for recovery. Configurable escalating respawn fatigue is required. Exact fatigue values, injury restoration, and profession-XP retention remain to specify.
8. Updated October 29: all vanilla mobs are replaced. The confirmed direction includes regional wildlife plus original MMO-style monsters, intelligent factions and named villains. The final roster/mix, names and release selection remain open; see the [threat and villain specifications](systems/MMO-THREATS-AND-ENCOUNTERS.md).
9. Config format and UI: plain TOML files only at first, or JSON/datapack formats for list-heavy settings? In-game config screen in v0.1 or later?
10. Existing-world changes: when a toggle removes content already placed in a world, should the mod keep it, convert it, or block the change?
11. Adapt vs convert default: is adapt (keep vanilla blocks, apply our properties) the right default, or should vanilla soil/stone convert into the mod's own blocks on placement and generation?
12. Remaining vanilla logic: TNT (powder keg) and redstone (wire-style) are decided; what about pistons, portals, hoppers/droppers, and minecarts: kept, simplified, or disabled?
13. Powder keg numbers: capacity per slot, power scale, fuse and ignition rules, crafting cost, and whether kegs can be carried; the reference design is TFC-style (12 slots, sealed to ignite), so match its numbers or tune our own?
14. Wire logic scope for v0.1: since the wire change is mainly cosmetic (actual cables), should v0.1 keep vanilla-like signal logic (strength 0–15) with the cable look and add analog signals, bundled cables, and gates later?
15. Signal tech age: should wiring unlock at Age 3–4 as suggested, or earlier/later? Copper itself is introduced in Age 2 in the draft progression.
16. Catalog depth: ship Tier A (major types) first and grow B and C over time, or hold the first release until the fuller set exists?
17. Rendering stage gates: rendering and world-gen come first (decided); what performance and quality bar should trigger Stage 2 (custom rendering) and Stage 3 (custom chunk renderer)?
18. 1:1 Earth data and scale: which datasets (elevation, land cover, climate, geology) and licenses, how is vertical scale handled (compressed, or a taller custom height range), and how much data can be bundled versus downloaded?
19. Tree rendering: hand-modelled trees per species, or parametric trees generated from species data?
20. Stack-size ceiling: how high should configurable stacks go (your example is 6 to 512), and are we willing to build a custom inventory layer if vanilla's per-item cap is lower?
21. Permafrost tools and thawing: what counts as an "advanced tool" that can break permafrost (tier and age), and which thaw methods (fire, steam, thermal tools) ship in v0.1?
22. Ice cementation scope: which blocks and moisture states harden, how fast does it thaw, and should it affect only soils, gravels, and sedimentary stone as drafted?
23. Inspection gating: should inspection tools and soft-integration tooltips (e.g. Jade) reveal everything, or be gated by tool tier and discovered knowledge?
24. Field guide format: plain Markdown files only, or Markdown plus datapack JSON for generated pages (property sheets, recipes)?
25. Far-terrain scope: heightmap-only far tiles at launch, or also a mid-range simplified-block tier? What default distances?
26. Distant Horizons versus our own LOD: test Distant Horizons first and keep it as an optional fallback, or commit to our own LOD from the start?
27. Mountain height: taller custom height range, vertical scale compression, or both (verify the 26.x height caps)?
28. Hytale: should a port become a committed roadmap item or remain a possibility? The platform-neutral core is already required; a port is not committed.
29. Own game: when it happens, which engine and language? A Java core shares directly with NeoForge and Hytale; any other engine means porting the core or building it natively.
30. Default module set: which modules are on in the default preset and which ship off (for example world.earth off, permafrost on)?
31. Datapack versus TOML split: are module toggles datapack-only (world creation screen), TOML-only, or both with the datapack winning for world-locked modules, as drafted?

<a id="s9"></a>

## 9. Non-Goals for v0.1

- Electricity or a GregTech-style machine tree
- Space, extra dimensions, or magic
- Full original art pass
- Compat with vanilla-dependent mods beyond the Create bridge plan

<a id="s10"></a>

## 10. Implementation Roadmap

The former v0.1 checklist is consolidated into the [milestones and acceptance criteria](../roadmap.md#milestones). Track completion there only. All prior prototype goals remain represented; far-terrain LOD remains a proposal awaiting a scope decision.


## Authored NPC and questing expansion — October 29, 2026

The owner requests WoW-style NPC quest interactions with an easy native admin workflow: create a custom NPC, choose a skin, assign quests, restrict eligibility by character level and questline, and delegate creation to other authorized admins. [NPC/quest specification](systems/NPC-AND-QUEST-SYSTEM.md) defines the wizard, states, typed conditions/objectives, co-authoring, test/publish/rollback and durable rewards. [Reference study](systems/NPC-QUEST-REFERENCE-STUDY.md) compares the official MMO and Minecraft-tool patterns with their version/license limits. All vanilla mobs are replaced; native NPCs cannot be a hidden vanilla-villager dependency. These are design requirements, not shipped UI or a new release claim.

## Original hostile factions and villains — October 29, 2026

The owner clarified that predators must include the broader creative range of WoW and other MMO threats fitting Minecraft and Deep Primal. Natural predators remain part of an ecosystem; monsters, intelligent faction forces, patrols, camps, elites and named antagonists add regional conflicts and quest arcs. [Encounter design](systems/MMO-THREATS-AND-ENCOUNTERS.md), [faction/villain concepts](systems/HOSTILE-FACTIONS-AND-VILLAINS.md) and the [reference study](systems/MMO-THREAT-REFERENCE-STUDY.md) define the proposed adaptation. They preserve editable-terrain counterplay, existing character progression/death rules, solo viability, protected builds and durable shared-world outcomes. Specific faction names, enemy rigs, abilities and balance values are original candidates awaiting review, not implemented systems or copies of another game's roster.
