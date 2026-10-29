# Expanded replica — docs/STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md) |
| Source identity | git: docs/STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md |
| Preserved source SHA-256 | `f230e8e6403d9bfef785cff27fcd494d00f96ebb1868f9be302311158897c55d` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 20 follow-up expands the reference comparison to twenty projects and supplies detailed open requirements for riparian generation, canopy interception/dew, and shallow puddles or wet traces. It separates author descriptions, targeted Puddles & Floods source observations, and existing Deep Primal primitives, and records that no new native tests were run. The document already distinguishes cosmetic moisture from physical water, requires one event owner and conserved sub-unit accumulation, and addresses block coexistence, elevated flood levels, configurable effects, dedicated servers, and bounded work. Its historical implementation statements do not cover later hydrology automatically.

## Research expansion — gaps and details to deepen

- Elaboration: the event paths and ownership rules are already extensive; a concrete event envelope, retry semantics, and volume/species conversion record would close implementation ambiguity.
- Elaboration: canopy exposure and drying are specified conceptually, but topology invalidation after leaf/roof edits and unsupported climate inputs need typed behavior.
- Clarification: cosmetic puddle resources and physical water capacity must remain distinct even when both use visually similar placement masks and shared meshes.

## Research expansion — proposed contracts

- Define MoistureEvent with world/epoch, event ID, source-account ID, event kind, accepted water quanta, constituent amounts, spatial footprint, tick, and optional presentation seed. Retries deduplicate by event ID; a visual event without an authoritative debit cannot become spendable water.
- Add a SubUnitWaterStore per admitted patch or owner, with exact remainder and composition. Promotion into a native cell transfers only whole accepted units; residual quanta remain owned. Demotion, evaporation, overlap, and patch merging preserve the same quantities.
- Version canopy support/exposure by foliage and roof geometry. A removed leaf either transfers retained authoritative interception water under the declared policy or leaves it in an explicit owner; deleting the visual emitter cannot erase it. Missing humidity or temperature inputs return unavailable and disable only the affected optional process.
- Give wetness decals/meshes a render-only support key and lifetime. They cannot write fluid occupancy, alter collision, block redstone, power equipment, or request another weather debit. Physical inundation uses the shared displacement and waterlogging transaction.
- Attach riparian style rules to stable reach, substrate, bank-band, and feature IDs. Cosmetic debris is non-harvestable; a physical log has persistent occupancy and removal state, and cannot respawn through repeated decoration.

## Research expansion — acceptance checks

- Replay each of the six documented puddle event paths twice, including reconnect/reload; source debit and receiver credit occur once.
- Accumulate many sub-unit droplets, promote a full native amount, then drain and reload; total water and species match exactly.
- Remove wet foliage and add a roof during delayed effect work; no drip passes through the roof or creates unaccounted water.
- Toggle client particles, puddles, and shader modes during rain; authoritative moisture, collision, and server accounting remain identical.

## Research expansion — source anchors

- WATER-07 — canopy interception, dew and drying
- WATER-08 — detailed puddles, wet traces and meaningful flood extent
- TERRAIN-04 — complete stream integration, banks and river atmosphere
- Ordering and completion

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java) |
| `../src/main/java/com/shnewbs/deepprimal/client/WaterEffects.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/client/WaterEffects.java) |
| `../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java) |
| `../src/main/java/com/shnewbs/deepprimal/engine/EngineFluid.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/EngineFluid.java) |
| `../src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java) |
| `../src/main/java/com/shnewbs/deepprimal/mixin/RiverCurrentMixin.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/mixin/RiverCurrentMixin.java) |
| `../src/main/java/com/shnewbs/deepprimal/mixin/RiverImpulseMixin.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/mixin/RiverImpulseMixin.java) |
| `../src/main/java/com/shnewbs/deepprimal/water/Precipitation.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/water/Precipitation.java) |
| `../src/main/java/com/shnewbs/deepprimal/water/WeatherHydrology.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/water/WeatherHydrology.java) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Streams, canopy moisture and puddles — reference follow-up

Reviewed **2026-10-22** against Deep Primal **999e2fe6d8477a1d8013f90ce1d68f95c9affb46** (alpha 20 runtime). This extends the [terrain/water/snow/LOD audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) from 17 to **20 owner-linked reference projects**. That runtime is unchanged from the earlier audited implementation.

**Result:** stream geometry/current primitives are partial; the richer bank/cascade behavior, canopy moisture lifecycle and detailed puddle presentation are unfinished. Existing vanilla leaf drips are not evidence of an authored canopy system. Legacy rainfall runoff is not proof of a native sixteen-unit puddle/flood system.

Scope: author descriptions for all three projects, targeted public Puddles & Floods source review, and Deep Primal source comparison. No reference was installed or benchmarked, no full upstream source audit is claimed, and no new native runtime tests were run. This change adds detailed planning only. Environmental catalogs remain deferred; source-code/assets reuse and dependencies are not added.

## Source and coverage matrix

| Reference | Author-described behavior checked | Current Deep Primal coverage and missing work |
| --- | --- | --- |
| [Streams Reflowing](https://modrinth.com/mod/streams-reflowing) | Terrain-fitting streams, elevated wet/dry lakes, ocean-directed currents, motion across source water and upstream boat passage. Configurable bank zones/material patches, waterside/native/custom features, waterfall/confluence spray, distant effect detail and floating foliage/twigs. Optional river rebioming; separate gameplay/client settings and quality controls. | **Partial foundation.** TerrainEngine plans regional downhill reaches and stored speed; EngineChunkGenerator selects simple surfaces; EngineFluid exposes a stored current. WaterEffects has local spray. Full lake/outlet integration, arbitrary source-water continuity, advanced boat handling, authored bank/feature schemas and richer river effects remain open. See TERRAIN-04 below and existing TERRAIN-01/02/03, WATER-01/05 and LOD-02. |
| [Canopy Cascade V1.2](https://www.curseforge.com/minecraft/mc-mods/canopy-cascade-v1-2) | Rain/storm-dependent leaf drips, post-rain drying, dawn dew, midday drying and evening moisture; exposed leaf origins and accelerating drops. Biome variations include jungle sap, swamp tint, snowmelt and hot-region evaporation. Particle density/culling and Particle Rain cooperation are advertised. | **Open native feature.** Precipitation and SnowFoliage address snow, not stored canopy wetness/dew. WaterEffects has no equivalent foliage-moisture lifecycle in the inspected code. Ambient particles alone must not create server water. See WATER-07. |
| [Puddles & Floods](https://modrinth.com/mod/puddles-floods), [public source](https://github.com/PigCart/puddles-floods) | Shallow rain/drip/flow/potion/cauldron/snowmelt traces, low-ground visual flooding, safe block coexistence and footstep splashes. Separate client/server event coverage, adjustable collection/drying/range, shader options and resource-pack placement masks. | **Open detailed presentation; legacy hydrology partial.** WeatherHydrology has old-solver moisture/runoff/evaporation; native WaterRuntime handles finite water, not a dedicated puddle layer/event suite. Cosmetic wetness must be distinguished from physical inundation. See WATER-08 and WATER-02. |

Streams Reflowing's broad compatibility/performance statements are author claims, not our certification. Its notes acknowledge cold terrain-query/exploration costs and quality tradeoffs. Do not require world pregeneration to conceal an unacceptable Deep Primal exploration budget. Streams Reflowing and Canopy Cascade are listed All Rights Reserved; learn behavior without assuming source or asset reuse permission. Puddles & Floods has an MIT license in the reviewed repository; no code is adopted here.

## Targeted evidence

### Deep Primal

- [TerrainEngine](../engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java): bounded regional drainage, rainfall accumulation, curved reaches and width/depth/speed; no completed unified lake/spillway or arbitrary-world river routing service.
- [EngineChunkGenerator](../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java): simple rain/elevation/sediment surface selection and placeholder tree decoration, not a configurable riparian feature/bank-style pipeline.
- [EngineFluid](../src/main/java/com/shnewbs/deepprimal/engine/EngineFluid.java), [WaterRuntime](../src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java): sixteen-unit volume/current and bounded transport. A full cell can carry stored flow; that does not establish momentum propagation into every ordinary player-placed source cell.
- [RiverCurrentMixin](../src/main/java/com/shnewbs/deepprimal/mixin/RiverCurrentMixin.java), [RiverImpulseMixin](../src/main/java/com/shnewbs/deepprimal/mixin/RiverImpulseMixin.java): legacy RiverWaterBlock/FlowingFluid hooks, not generic proof of native engine or all-river support.
- [WaterEffects](../src/main/java/com/shnewbs/deepprimal/client/WaterEffects.java): shared particle admission budget; 24 local trials within 24 blocks horizontally, loaded columns only, plus bounded entity inspection. Existing splash particles do not provide long-range river-effect LOD, persistent foam/debris or canopy moisture.
- [WeatherHydrology](../src/main/java/com/shnewbs/deepprimal/water/WeatherHydrology.java): finite-volume helper gate, persisted old-solver soil storage and simple runoff/evaporation. [Precipitation](../src/main/java/com/shnewbs/deepprimal/water/Precipitation.java) adds snow/refill hooks, not all six puddle-event paths.
- Current alpha 20 validation establishes limited native transport/mesh progress, not acceptance of the three richer reference suites. Earlier [river comparison](RIVER-FLOW-COMPARISON-2026-10-19.md) is historical alpha 13 evidence.

### Puddles & Floods source boundary

Targeted public source was retrieved at reported main revision **d3780c4542b710b67e20fe15c2ea8e9271dc3f70**:

| Inspected path | What the source establishes |
| --- | --- |
| [ClientBlockPlacementUtil](https://github.com/PigCart/puddles-floods/blob/d3780c4542b710b67e20fe15c2ea8e9271dc3f70/src/main/java/pigcart/puddleflood/ClientBlockPlacementUtil.java) | Camera-local collection/evaporation work and a texture-controlled placement threshold; rain presentation can run locally. |
| [BlockPlacementUtil](https://github.com/PigCart/puddles-floods/blob/d3780c4542b710b67e20fe15c2ea8e9271dc3f70/src/main/java/pigcart/puddleflood/BlockPlacementUtil.java) | The inspected flood eligibility is tied to sea-level height, flat support, open space and a beach exclusion. It is not a catchment discharge/storage calculation. |
| [PuddleBlock](https://github.com/PigCart/puddles-floods/blob/d3780c4542b710b67e20fe15c2ea8e9271dc3f70/src/main/java/pigcart/puddleflood/block/PuddleBlock.java) | Empty collision and connected edge state; drying removes presentation blocks. This is not evidence of conserved native fluid units. |
| [ConfigData](https://github.com/PigCart/puddles-floods/blob/d3780c4542b710b67e20fe15c2ea8e9271dc3f70/src/main/java/pigcart/puddleflood/config/ConfigData.java), [changelog](https://github.com/PigCart/puddles-floods/blob/d3780c4542b710b67e20fe15c2ea8e9271dc3f70/CHANGELOG.md) | Feature/authority controls and renderer/version regressions matter independently of the water algorithm. The latest notes include puddles obstructing water in 26.3 and earlier fluid-origin/client-server fixes. |

These are targeted observations, not a whole-project correctness verdict. Do not reproduce a sea-level-only visual flood rule as Deep Primal's hydrology: our elevated lakes and headwaters also need meaningful local water levels.

## Detailed open requirements

### TERRAIN-04 — complete stream integration, banks and river atmosphere

This extends TERRAIN-01/02/03 rather than introducing a second generator.

- [ ] **Connected source and lake behavior:** preserve local water elevations, bounded supply and shared outlets through wet/dry scenarios, channel changes and chunk seams. Apply the existing source-lake scale rule; regional climate may control filling/placement after biome identities are defined.
- [ ] **Current continuity and navigation:** define a single authoritative flow field through full/partial native water and still-water junctions. Avoid stopped flow merely because a cell is full, but allow genuine pools/backwater. Reconcile input, drag and buoyancy so upstream rowing/small drops are playable within configurable limits, while steep rapids remain obstacles. Test items and vehicles separately; a stored vector alone does not prove correct force strength.
- [ ] **Data-driven banks:** add a versioned style schema for bed, waterline and inner/outer bank bands, with widths derived from actual channel scale. Resolve materials from geology, soil, biome/habitat eligibility, inundation and flow. Use coherent patches and gradual blends; prevent uniform gravel rims and repeated bowls.
- [ ] **Context and feature placement:** expose substrate, steepness, speed, bend position, mouth proximity and long-flat reaches to rules. Allow tagged exclusions and validated datapack/config additions. Add a dedicated riparian feature stage with bounded density, occupancy/support checks and deterministic feature IDs; avoid rerunning whole biome decoration or duplicating large trees/rocks.
- [ ] **Catalog boundary:** plan reeds, bank cover, aquatic plants, logs and associated habitats against the existing rosters. Species implementation remains deferred. Existing vanilla placeholders can exercise the placement contract without claiming completion of native ecology.
- [ ] **River effects:** derive foam/spray/bubbles and any light floating debris from one sampled flow/surface state. Render cosmetics in bounded batches instead of persistent item entities. Preserve tint, shore clipping and flow direction across effect quality/range changes; vegetation-derived cosmetic debris must not become a free resource source.
- [ ] **Biome identity:** keep riparian habitat/material/spawn/ambience metadata separate from a compulsory minecraft:river rewrite. Any optional compatibility rebioming needs an explicit new-chunk policy and preserved surrounding identities; it must not erase the chosen biome roster.
- [ ] **Configuration and cost:** separate generation-time styles/quality from live gameplay and client effects. Validate malformed/old schemas, world-creation locks and missing tags; expose effective settings. Reuse cached regional geometry and cheap flow queries, bound distant effects and duplicate-provider ownership.
- [ ] **Acceptance:** fixed seeds covering dry/wet elevated basins, coast outlets, source-to-pool continuity, upstream boat input, bends/confluences and biome/geology transitions. Compare generated output across worker counts/reload/order, include partial-water traversal, native/LOD transitions and generation p95/p99 plus server/frame budgets. No universal foreign-worldgen compatibility claim without named-version tests.

### WATER-07 — canopy interception, dew and drying

This supplies a native owner for foliage moisture and joins WATER-02, WATER-03 and SNOW-01.

- [ ] **Moisture state:** define bounded canopy-cell/patch interception storage and exposure, with rainfall inputs, release to ground and evaporation. Only authoritative accounted release may supply soil/puddles/streams; visual droplet count and client graphics settings cannot change that quantity.
- [ ] **Conditions:** use local wetness, actual precipitation, temperature/humidity, wind, sunlight/shade and season/time where available. Post-rain release should decline with remaining storage; dew needs a declared condensation budget rather than an unlimited water source at every dawn. Keep numerical climate design after biome planning.
- [ ] **Appearance and origins:** sample exposed foliage faces/support and ray/segment collision cheaply, without sending particles through roofs or leaf interiors. Support configurable biome/material styling and cold melt/refreeze states. Sap/resin visuals must remain a distinct material category and must not produce water or imply an implemented harvesting system. Hot-region vapor requires a suitable wet/hot condition rather than arbitrary perpetual smoke.
- [ ] **Ground and sound:** allow grounded droplets to select leaf/soil/rock/water impact effects and optional quiet drip sound through a shared rate/distance budget. A client-only effect can request cosmetic wetness, never spend or create authoritative fluid.
- [ ] **Ownership and persistence:** store coarse server wetness only where gameplay needs it; derive bounded client presentation with explicit unload/reload and calendar catch-up behavior. Ensure our effects cooperate with Particle Rain or another selected weather provider without requiring it; no compatibility certification from a listing alone.
- [ ] **Acceptance:** rain-to-clear transition under a canopy, roof versus exposed leaves, dawn/day/evening cases, cold-to-thaw, arid wet/dry contrasts and edited/removed foliage. Verify no duplicate rainfall, no local-client water creation, no dedicated-server client-class loads and bounded CPU/particles/audio/cache size during storms.

### WATER-08 — detailed puddles, wet traces and meaningful flood extent

This adds a shallow presentation/interaction layer to WATER-01/02/03, not a second competing fluid solver.

- [ ] **Choose authority explicitly:** cosmetic wetness may render without collision/flow changes; physical shallow water stores authoritative volume and may affect gameplay. State this distinction in APIs, saves and inspection. A cosmetic wet patch cannot fill buckets, power a wheel, drown an entity or advance a fluid sensor.
- [ ] **Cover the event paths:** plan rain, canopy/block drips, receding flow, splash-potion impacts, spilled cauldrons and snowmelt. Use one event-ownership contract so the originating reservoir/container/snow debit happens once. Potion residue keeps its composition and gameplay policy; lava/sweat/unrelated particles do not become fresh water.
- [ ] **Sub-unit accumulation:** choose a conserved fixed-point or coarse reservoir representation for quantities below one native sixteenth. Convert to a sixteen-unit cell only at a defined threshold, preserving remainder and composition. Do not round every drip into a block unit or create a full source when a tiny film becomes visible.
- [ ] **Placement and recession:** follow supported micro-depressions/material permeability and slope; respect walls, roofs, open drains and elevated waterlines. Use configurable masks as artistic variation within physical eligibility. Dry by actual weather/storage rules, persist gameplay quantities and avoid a repetitive visible placement grid.
- [ ] **Block coexistence:** shallow cosmetic traces must preserve wiring, plants, inventories and support. Actual floodwater uses the explicit native displacement/waterlogging policy; do not promise decorative safety for destructive-depth fluid. Ensure the visual layer cannot act as a dam or obstruct real EngineFluid.
- [ ] **Flood extent:** compute local stage/storage/inflow/spillways before joining puddles into physical inundation, including elevated valleys/lakes and coastal backwater. Support bounded rise, recession and remaining wet ground. Avoid sea-level-only eligibility, disconnected water appearing across walls, or unaccounted global ocean rise from endless sources.
- [ ] **Rendering, audio and settings:** use thin, grounded edges and source-composition tint; provide restrained footstep/impact splashes. Keep independent storm coverage, collection/drying, radius and quality controls, resource-pack style variation and optional shader-water mapping. Correct depth and wave treatment must prevent tiny patches waving into ground; verify OpenGL/Vulkan and certify Iris/Oculus/Sodium combinations separately if supported.
- [ ] **Acceptance:** one scene per event, small-film coalescence, impermeable versus permeable surfaces, covered ground, opened drain, raised lake flood/recession, wiring coexistence, adjacent moving native water and unload/reload/multiplayer. Measure retained mass and capped work/memory as well as appearance; disabling effects must leave hydrology unchanged.

## Ordering and completion

Keep the existing P0 horizon/LOD regression and supported stepping/filling work first. TERRAIN-04 builds on the connected drainage/material contracts; WATER-07 and WATER-08 can share the native hydrology integration rather than duplicating rain, soil or snowmelt ownership.

A requirement stays open until its native owner, configuration defaults, persistence/network behavior, bounded cost and behavioral/visual evidence are recorded. No dependency, copied source, new build or new implementation-completion checkbox is introduced by this audit.

Return to the [roadmap](../roadmap.md), [main audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md), [architecture](LOD-AND-DRAINAGE-ARCHITECTURE.md) and [biome plan](BIOME-PLAN.md).


<!-- END PRESERVED SOURCE -->
