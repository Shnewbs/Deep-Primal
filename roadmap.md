# Deep Primal — Roadmap

## Built-in engine — modular alpha 22, 2026-10-22

Deep Primal remains the product. Its original Overworld terrain/liquid engine is now integrated into the main mod, together with an actual-chunk LOD renderer and custom tabbed graphics settings. Minecraft/NeoForge provide host APIs, storage, lighting, scheduling, networking and GPU interfaces. Native Overworld terrain/biome decisions are not used by the selected `continental_v1` / `regional_v1` generator. Existing saves retain their own generator and height. All previous profiles, performance/settings modules and optional Vulkan source remain available; the separate alpha.1 experiment is historical and not shipped.

- [x] Integrate an independent Overworld generator and biome source with original seeded continents, warped coasts, relief, cave volumes, climate/palette fields and feature placement.
- [x] Plan whole-continent downhill drainage with rainfall-weighted catchment area, curved reaches and area/slope-derived width, depth and stored speed.
- [x] Integrate an independent 16-unit finite fluid, bounded gravity/pooling, bucket input and curved shared-corner water meshing; verify saved states and native-water conversion without the legacy solver taking ownership.
- [x] Implement owning-thread chunk snapshot capture, edit invalidation, vertical-run storage, three mesh resolutions, native-ready transitions, OpenGL/Vulkan GPU submission and bounded persistent cache lifecycle. The current viewer uses normally delivered/explored chunks; no bulk server LOD stream or unseen-terrain generator is implemented.
- [x] Implement original General/Quality/Performance/LOD/Water/API graphics tabs; verify Apply/Revert, tab changes, small-window resizing, reopen and full-process persistence of requested values.
- [x] Recover KubeJS built-in registry access before serialization contexts are created, including Create World after an unsupported saved generator; define invisible fluid models to remove missing-model spam.
- [x] Reuse coordinate warp/trigonometry without changing version-one terrain output; greedily merge matching LOD faces while retaining air gaps and fractional fluid steps; keep live updates and disk restores progressing under continuous invalidation.
- [x] Resume dormant finite water at loaded chunk borders through bounded, coalesced scans without forced chunk loads.
- [x] Activate dormant loaded interiors, drain thin films toward nearby outlets, wash through replaceable plants, and retain per-tick transport work under budget saturation; validate conservation and continued local basin filling.
- [x] Coalesce unfinished non-player native mesh builds while preserving newer dirtiness and immediate player-edit replacement.
- [x] Make default installer lean; retain an optional, separately classified Vulkan platform bundle and original module boundaries.
- [x] Separate the actual-chunk LOD runtime from graphics and root integration; provide an optional interface and a build that omits LOD without losing worldgen/settings.
- [x] Replace the native Video Settings entry directly; preserve native option coverage, correct parent navigation, staged preset previews and texture/display updates.
- [x] Add bounded connected-pool pressure to our 16-unit fluid. Native communicating-vessel and blocked-passage fixtures verify raised surfaces and conservation.
- [ ] Map the full TFC geology/climate/river, Terralith landform variety and Tectonic relief/underground-river behavior to implementation and visual acceptance. TFC has partial source review; broad Terralith/Tectonic source audits remain pending.
- [ ] Remove repeated bowl/channel bank artifacts and develop a versioned drainage/carving revision; fix LOD page-edge walls and refine placeholder vegetation. Preserve existing generator geography.
- [ ] Pass broader fixed-seed native visual comparisons for coasts, rivers/confluences/outlets, relief, forests and unsupported terrain. Current captures prove the new path renders; they do not establish the desired final terrain quality. The rejected alpha.1 appearance is not an acceptance baseline.
- [ ] Complete shared watershed boundary/outlet contracts, unified lakes/spillways, erosion/deposition, geological catalog placement and structural connectivity checks.
- [ ] Complete displacement, groundwater/recharge, generated water budgets and breaking shoreline surf. Existing legacy hydrology features must be audited individually before claiming compatibility with the new fluid.
- [x] Add independent liquid patches with GPU height interpolation and wet/dry face fading; validate terrain invalidation bypass, fallback, resource reload and GPU teardown.
- [x] Query delivered client chunks explicitly, refresh liquid halos on chunk arrival/replacement/unload, release departed GPU pages, and propagate direct lighting invalidation; verify full oceans without false unloaded-boundary walls.
- [ ] Refine waterfall/front topology, mixed transparency and extreme-view behavior with broader fixed-seed scenes.
- [ ] Implement backed source/catchment budgets, unloaded transfer accounting and an explicit ocean reservoir policy; see [long-running water requirements](docs/WATER-BUDGET-REQUIREMENTS.md).
- [ ] Add unseen-terrain LOD planning/streaming, better voxel reduction and material/lighting/transparent-water transitions; test resource reload, saturation, reconnect and broader multiplayer scenes.
- [ ] Benchmark against Distant Horizons on matched worlds/cameras/detail/radius/hardware: frame-time percentiles, memory/VRAM, generation/loading latency, cache I/O and edit lag. No superiority claim is established.

The aim is to build better integrated Deep Primal systems, with improvements demonstrated by behavior and matched measurements. TFC/Terralith/Tectonic and PhysEx Reborn, Finite Water, BetterWaterMesh, Water Erosion and Distant Horizons remain references, not bundled generators/engines. Gaea/World Machine techniques guide independently authored region planning. Meshoptimizer is still a development probe, not a runtime dependency. Environmental catalogs remain planning-only until separately authorized. See [architecture/reference status](docs/LOD-AND-DRAINAGE-ARCHITECTURE.md) and [alpha 21 validation](docs/VALIDATION-0.0.1-alpha.21.md).

**Status:** integrated engine/LOD/settings alpha 0.0.1-alpha.22; full survival conversion remains in design. **Updated:** 2026-10-22. **Prototype target:** Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.

Deep Primal is a standalone total-conversion survival mod combining grounded world generation, demanding survival, primitive technology, metallurgy, and mechanical automation. Difficulty comes from decisions and consequences, with GregTech-level depth as the long-term target.

The initial review found design documents only. The repository now includes a compiled terrain prototype; see [play/build instructions](docs/WORLDGEN-PROTOTYPE.md) and [validation evidence](docs/VALIDATION-0.0.1-alpha.18.md). This is a partial foundation, not completion of the full v0.1 milestones. Milestone grouping and acceptance criteria organize the existing draft; they do not establish release dates or approve unresolved proposals.

Owner decisions and detailed follow-up: [2026-10-18 design decisions](docs/design-decisions-2026-10-18.md). Confirmed choices there supersede older draft assumptions; the latest follow-up confirms shelter/renewal direction, finite ores, character XP, cargo recovery, local-only storage, and rotation bridging. Exact tuning and explicitly tentative features remain open.

**Non-negotiable priority: worldgen performance.** Expanded height/depth must meet measured generation, latency, memory, and correctness budgets. Default vanilla height is not the design ceiling; literal unlimited height remains an architecture feasibility question. See [height and performance gates](docs/design-decisions-2026-10-18.md#world-height-and-performance-priority).

## Navigation

- [Confirmed direction](#confirmed-direction)
- [Milestones](#milestones)
- [Decisions before implementation](#decisions-before-implementation)
- [Long-term backlog](#long-term-backlog)
- [Completion and maintenance rules](#completion-and-maintenance-rules)
- [Terrain, water, snow and LOD audit](docs/TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) — 20 linked projects, current source coverage, detailed gaps and acceptance requirements
- [Configuration and ore authoring](docs/CONFIGURATION-AND-ORE-AUTHORING.md) — flexible deposits, organized config roots, optional KubeJS and proposed external visual editor
- [Voxel-engine research](docs/VOXEL-ENGINE-REFERENCES.md) — runtime/worldgen/authoring references and measurable prototype targets
- [Approved terrain mechanics](docs/TERRAIN-MECHANICS.md) — realistic geology/ores, soil/wind forms, underground drainage, cave-ins and obstructions; no sinkholes; optional sediment simulation
- [Approved water mechanics](docs/WATER-BUDGET-REQUIREMENTS.md) — watershed reservoirs, seasonal wet/dry surface water, perennial/strict policies, ocean recycling and blocked-river behavior; implementation open
- [Biome plan and named roster](docs/BIOME-PLAN.md) — proposed profiles, regional communities, environmental layers and habitat coverage
- [Named environmental type catalogs](docs/NATURAL-TYPE-CATALOG.md) — actual named planning rosters, with kinds and aliases labeled
- [Environmental coverage inventory](docs/ENVIRONMENTAL-COVERAGE-INVENTORY.md) — planning completeness across materials, states, habitats and natural processes; implementation deferred by owner direction
- [Detailed design reference](docs/design-reference.md) — full feature descriptions, material catalogs, configuration layout, progression, and all 31 open questions

## Terrain, water, snow and LOD follow-up — 2026-10-22

The [current completeness audit](docs/TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) checks 20 owner-linked projects, including the [Streams Reflowing / Canopy Cascade / Puddles & Floods follow-up](docs/STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md), and the Gaea/World Machine, Nanite, Cesium, meshoptimizer/clusterlod, LWJGL and Simplygon references against alpha 20 source. It contains the detailed native behavior, integration gaps, configuration/performance constraints and acceptance scenes for every item below. Referenced mods are not bundled or fully implemented. Older alpha 14/16 water tables remain historical; legacy eight-unit hooks do not automatically support the independent sixteen-unit fluid.

- [ ] **P0-RENDER:** reproduce the owner's report that defects disappear with "horizonal view" disabled; identify the actual control/pass and fix enabled-view page walls, foreground/depth and water seams on OpenGL/Vulkan.
- [ ] **TERRAIN-01:** connect tributaries, cross-region outlets, lakes/spillways and sustained source budgets; broad initial rivers need substantial source water or upstream catchment, while small springs taper.
- [ ] **TERRAIN-02:** finish hydraulic width/depth/speed, pools/rapids/cascades and supported partial-height descending water steps. Generated river speed already varies; runtime edited-channel hydraulics remain incomplete.
- [ ] **TERRAIN-03:** use rock-specific erosion resistance and deterministic generation-time wear/material masks with bounded support checks; remove repeated bowl/rim artifacts and unsuitable surfaces. Full live sediment transport is optional/default-off; recipe-based panning/sluices are the baseline, with local provenance only when useful.
- [ ] **TERRAIN-04:** finish current/source continuity and boat navigation, versioned riparian bank/feature rules, biome identity preservation, foam/debris and bounded distant river effects; detailed Streams Reflowing comparison is in the three-project follow-up.
- [ ] **TERRAIN-05:** implement [realistic structural geology](docs/TERRAIN-MECHANICS.md): coherent beds/bodies, folds/faults, intrusions and exposures; place finite ores and gems by host/formation setting, including eligible mountain deposits without a blanket high-elevation ore ban.
- [ ] **TERRAIN-06:** connect rock/soil-dependent infiltration, flooding/drainage, caves, karst, through-mountain rivers and returning springs under one water budget; exclude generated sinkholes and live surface-collapse sinkhole behavior.
- [ ] **TERRAIN-07:** implement coherent soil horizons/depth, exposed profiles and shared drainage/rooting/material properties.
- [ ] **TERRAIN-08:** generate plausible wind-shaped terrain from material supply/exposure/history; live sand movement remains optional and bounded.
- [ ] **TERRAIN-09:** add material/support/moisture-dependent slope stability and underground cave-ins with meaningful supports, bounded propagation and no sinkhole-producing surface breach.
- [ ] **TERRAIN-10:** add grounded natural obstructions and accounted ponding, bypass/outlet response and release when removed.
- [ ] Define data-driven panning and sluice variants using consumed input recipes; optional local-geology/deposit context must provide useful prospecting and cannot mint unlimited ore/gems from flowing water.
- [ ] **WATER-01:** unify native sixteen-unit ownership with containers/displacement/waterlogging, fair basin work and per-frame water-height/wet-edge presentation.
- [ ] **WATER-02:** implement the [approved hybrid water mechanics](docs/WATER-BUDGET-REQUIREMENTS.md): metered reservoir-backed headwaters, renewable major-river baseflow, seasonal meltwater, depletable wells, balanced ocean/atmosphere recycling and stable average sea level; support strict depletion, local dam/overflow physics and optional source safety. Include wet-season desert flooding and physical dry-season recession into pools, damp ground and exposed beds through accounted evaporation/infiltration/drainage.
- [ ] **WATER-03:** carry bounded source/composition summaries for sediment/organic load and salinity; blend confluences/clarity across near/far rendering. This does not require a live terrain-eroding sediment solver; that extension is optional/default-off under the terrain mechanics decision.
- [ ] **WATER-04:** provide lossless transactional native fluid ports, partial containers and progression-appropriate pumps/pipes/tanks/valves/sensors, plus separately tested optional machine adapters.
- [ ] **WATER-05:** integrate rated pressure equipment, deep-water semantics, stamina, currents, mount swimming and useful warnings/navigation.
- [ ] **WATER-06:** specify farm-water convenience/irrigation, measured drinking/inspection, sponge storage and fluid-aware fishing/boats/dripstone; keep lava, gas hazards and admin infinite sources explicit later proposals.
- [ ] **WATER-07:** add native canopy interception, conditional dew/post-rain drying, exposed-leaf drips, climate/material styling and accounted ground release; keep particles independent of water quantity.
- [ ] **WATER-08:** add detailed puddles and all event paths, sub-unit wetness storage, safe block coexistence and local-stage flood/recession; separate cosmetic traces from authoritative physical water and test shader/depth handoffs.
- [ ] **OCEAN-01:** complete shared visible/physical waves, depth-aware shore breakers, foam/swash, sound, flow-aware wakes and bounded strandline materials.
- [ ] **OCEAN-02:** add realistic simplified circulation, depth/hemisphere effects, tidal/estuary/rip flow, wind/surge and optional vessel/weather integration.
- [ ] **OCEAN-03:** plan shelf-to-trench bathymetry and evaluate Mariana-scale depth against actual vertical storage/lighting/network/performance limits; the current generator does not support literal 11 km depth.
- [ ] **SNOW-01:** complete intensity/climate-aware deep accumulation, canopy/drifts/compaction, thaw/refreeze/meltwater and matching fog/geometry; global forced winter remains optional.
- [ ] **LOD-01:** extend authoritative edited-world LOD with bounded error-based hierarchy/streaming, evaluate constrained meshoptimizer use, and run matched Distant Horizons benchmarks.
- [ ] **LOD-02:** verify material/lighting/cutout/water ownership, depth/alpha/fades and composition/snow/wave continuity, with any external shader/renderer combinations separately certified.

Prioritize enabled-view correctness and supported water steps/filling, then connected drainage/source scale/material placement and native hydrology integration. "Do better" requires reproducible correctness, integration, visual and/or matched performance evidence. This follow-up is documentation only; environmental catalog implementation remains deferred, and existing versioned saves/geography remain unchanged.

## Configurable deposits and external authoring — 2026-10-21 PDT

The [configuration/ore contract](docs/CONFIGURATION-AND-ORE-AUTHORING.md) records flexible native deposits and domain-organized deepprimal config roots. The external visual editor remains a proposal, not a shipped tool.

- [ ] **CONFIG-01:** inventory/migrate current flat files into domain subdirectories beneath config/deepprimal/ and matching supported world overrides; preserve scope/sync, user values, backups and one authoritative live copy.
- [ ] **ORE-01:** implement one validated, versioned native deposit schema for geometry, geological hosts, occurrence/density, height/depth modes, mixed ores/gems/grades, indicators and finite/high-reserve behavior.
- [ ] **ORE-02:** provide datapack/config authoring and optional versioned KubeJS add/modify/remove; compile immutable definitions once, expose provenance/precedence and preserve saved generation contracts.
- [ ] **EDITOR-01 (proposal):** evaluate an optional editor outside Minecraft using shared schemas, sliders plus numbers, presets/diffs, registry-aware validation and seeded previews without bloating the game runtime.
- [ ] **ENGINE-RESEARCH:** prototype selected Voxel Tools/Luanti runtime lessons and Veloren/Vintage Story worldgen organization; use Voxel Plugin as a secondary editor/profiling reference. [Research scope](docs/VOXEL-ENGINE-REFERENCES.md) requires native correctness and matched measurements.

## Confirmed direction

| Area | Direction |
| --- | --- |
| Platform | Standalone NeoForge mod on Minecraft 26.x; no TFC dependency. |
| Build order | Rendering and custom world generation are the first foundation. Gameplay builds on that foundation. |
| Core | Shared properties, calendar, heat, stress, tunables, and worldgen sampling; platform-neutral rules with a NeoForge adapter. |
| World | Natural structural geology, host-controlled finite ores/gems, soil profiles, wind-shaped terrain, rock/soil-dependent drainage, mountain cave rivers/springs, slope/cave-in stability and natural obstructions; no sinkholes. See [terrain mechanics](docs/TERRAIN-MECHANICS.md). Existing biome/climate, flora, frozen-ground and optional 1:1-horizontal Earth goals remain. |
| Vanilla content | Phased replacement by default, with configurable coexistence or vanilla-only modes where supported. Registry entries remain loaded; access and behavior are gated. |
| Adapters | Property-based behavior for re-enabled vanilla content in v0.1. Other-mod coverage and special game logic need explicit scope decisions. |
| Survival | Clothing and thermoregulation are essential; hunger, thirst, injury, fatigue, decay, nutrition, weight, and structural hazards form the broader design. |
| Progression | Ages 0–6, starting with flint/loose-rock/bone tools, developed stone tools, then metals. Rotation-driven automation remains central; steam transport is a proposed later extension. |
| Inventory | Stack count and mass are independent; stack limits and encumbrance are configurable. Large-stack feasibility is unverified. |
| Signals and explosives | Cable-style redstone and powder-keg-style TNT by default, with vanilla behavior options. Detailed launch scope remains open. |
| Content and visuals | Data-driven natural materials and building variants; TFC-style grounding and Conquest Reforged visual inspiration. Asset reuse requires license review. |
| Configuration | All Deep Primal user settings beneath domain-organized deepprimal roots, preserving client/server scope and world overrides; presets/tunables, flexible deposit datapack/config/optional KubeJS authoring and optional modules. External visual editor proposed; shared core remains required. |
| Documentation | Native Markdown/datapack field guide and independent inspection API; optional tooltip integrations. |
| Multiplayer | Single-player and dedicated servers; gameplay state is server-authoritative. Keep personal inventory defaults on; recover freight in the world. Configurable fatigue escalation for closely spaced deaths, resetting after a death-free interval; optional teams/guilds/parties. |
| Water and logistics | Approved hybrid finite local water plus renewable watershed reservoirs, designated perennial major rivers, seasonal melt streams/desert inundation and recession, shared depletable aquifers/wells, accounted ocean recycling with stable mean sea level, and real local flood/dam behavior. Strict depletion and source safety are optional. See [water mechanics](docs/WATER-BUDGET-REQUIREMENTS.md); broad transport/local storage remain desired and native remote retrieval/deposit is excluded. |
| Navigation and activities | Configurable native maps/compass/biome titles, slot-free hearth return (including an occupied rideable cart/wagon and its cargo), body-region health, hunting, weight-based meat/material yields for all animals, and species journal; full scope is not automatically part of v0.1. |
| XP and resources | Non-spendable MMO-style XP with a maximum level; vanilla XP cannot be restored; finite veins and high-reserve bedrock ore; remeltable scrap, salvage piles, and configurable disposal. |

The owner has selected an **authored renderer inside Deep Primal**, with no external performance-mod installation required. Alpha 6 implements an experimental macro-terrain horizon and automatically yields its renderer module to recognized competing renderers. A complete near-terrain renderer and Vulkan backend remain future stages. Hytale and an independent game remain portability possibilities, not committed releases. The platform-neutral core is already part of the design.

## Milestones

These milestones consolidate the former feature checklist and suggested v0.1 checklist. M0 prepares the project; M1–M2 establish the rendering/worldgen foundation before gameplay milestones M3–M5. M6 validates the combined v0.1 candidate. Work within a milestone can be split into smaller commits without changing the scope.

### M0 — Target, project, and technical contracts

**Status:** in progress; toolchain/project foundation exists, module/config architecture incomplete. **Depends on:** decisions D01–D03 below.

- [x] Select the initial target (Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25, Gradle 9.2.1) and document local build commands.
- [ ] Create the development project with separate platform-neutral core and NeoForge adapter boundaries.
- [x] Enforce the core boundary with a build check that rejects Minecraft/platform dependencies.
- [ ] Define property schemas, material identifiers, module manifests, save/config versions, and migration responsibilities.
- [ ] Prototype the required core plus two optional modules, including dependencies, conflicts, defaults, and disabled behavior.
- [ ] Establish per-domain config files, central tunables, validation, reload classifications, and Primal / Hardcore / High TISM preset skeletons.
- [ ] Establish the mandatory worldgen benchmark baseline and numeric budgets on named hardware: cold/warm exploration, p95/p99 chunk readiness, throughput, server tick/frame spikes, peak memory, I/O, multiplayer contention, and height scaling; retain rendering/atlas/network budgets.
- [ ] Verify selected-version height/coordinate/storage limits and compare taller-world profiles; record whether a deeper vertical-streaming architecture is required.
- [ ] Record asset/code provenance and licenses before importing third-party material.

**Exit evidence:** reproducible build; client and dedicated-server startup; default world loads without manual setup; optional-module and config validation checks; core-boundary check passes. Infrastructure only—this does not establish gameplay completion.

### M1 — Material catalog and rendering foundation

**Status:** not started. **Depends on:** M0 and catalog/rendering decisions D04–D05.

- [ ] Implement data-driven stone, soil, and wood properties and the material × shape pipeline.
- [ ] Define the original prototype target of 10 rocks and 10 trees, with generated textures, models, and building variants.
- [ ] Render prototype materials through the normal platform pipeline; inspect textures, shapes, seasonal tint hooks, and missing-asset behavior.
- [ ] Stress-test the planned full-catalog scale for registry, atlas, model loading, and memory limits.
- [ ] Record where Stage 1 fails the agreed quality/performance bar before scheduling custom Stage 2 rendering or an optional Stage 3 chunk renderer.

**Exit evidence:** visible, correctly rendered prototype catalog, reproducible stress scene, and measured results against M0 budgets. Catalog natural functions are completed with the relevant gameplay systems in M3–M5, not claimed from visual assets alone.

### M2 — Custom world generation and climate foundation

**Status:** in progress; default normal worldgen, config-selected underground depth, revised cave shaping, direct surface solver, bounded diagnostic caches, async reports, and tick/GC probes exist. Alpha 12 adds bounded mountain tributary routing, gorges and local water heights on top of alpha 11 coasts/lowland rivers. Global watershed stitching, elevated lakes and water budgets remain open. Native material/climate core, Earth mode, renewable groundwater, and full performance gates remain incomplete. **Depends on:** M0–M1; Earth and height decisions D06.

- [ ] Implement a deterministic core sampler for terrain, surface height, geology, climate, and material queries, with a NeoForge worldgen adapter.
- [ ] Generate native rock layers, soils, climate-based vegetation, and host-rock-dependent multi-ore veins with surface indicators.
- [ ] Define hydrology contracts for chunk-local groundwater, shared aquifers, source quality, recharge, extraction, and surface-water budgets before dependent systems.
- [ ] Implement phase-1 vanilla replacement controls and verify newly generated chunks honor them.
- [ ] Add the shared configurable calendar and climate inputs needed by seasons and frozen ground.
- [ ] Prototype permafrost and moisture-driven ice cementation using shared material properties; expose thawing hooks for M3 heat systems.
- [ ] Implement the optional Earth-mode prototype using approved, preprocessed data; validate projection, horizontal scale, vertical scale, cache behavior, and spawn selection.
- [ ] Verify save/reload consistency, chunk seams, generation order, world-creation locks, and dedicated-server behavior.
- [ ] Pass worldgen performance budgets for tall mountains, deep caves, dense forests, aquifers, and simultaneous exploration; compare generation output across worker counts. Height expansion is gated on these results.

**Exit evidence:** reproducible seeds and terrain inspection, deterministic sampling checks, native-material generation, measured generation/cache costs, and a working optional Earth prototype. Earth mode and frozen-ground foundations remain in the original v0.1 scope; dataset and height feasibility are blockers to resolve, not silently dropped requirements.

**Native far terrain — selected direction:** the owner requires the renderer inside Deep Primal, with no external performance-mod installation needed. Alpha 6 supplies the first coarse horizon stage; fuller near/far rendering, saved-world/build representation, transition quality, and real-GPU budgets remain open. Third-party renderer handoff is compatibility behavior, not a required external rendering dependency. Far visibility and world height remain separate problems.

**Rendering performance update:** alpha 3 includes typed distances, an experimental 96-chunk render ceiling, bounded native tracking allocation, chunk-send backpressure and camera-column relocation. Full simulation remains bounded to the native 32-chunk setting. See [alpha 3 validation](docs/VALIDATION-0.0.1-alpha.3.md).

**Alpha 4 follow-up:** independent vertical render radius bounds render-section allocations and meshes; full chunk loading/networking and server simulation still use whole columns. Default world creation uses Deep Primal, with additional underground depth configured before creation instead of selected as a separate world type. Saved-world height remains locked. Cave shaping targets the reported hanging stone curtains; this is not an erosion or physics simulation.

**Alpha 5 correction:** chunk-task queues retain enough priority buckets for the session tracking capacity, including unload priorities. This fixes the reported 96-chunk queue bounds crash without changing generation levels or terrain. Native queue regression tests cover 32, 44, 45, 46, and 96 chunks; see [crash-fix validation](docs/VALIDATION-0.0.1-alpha.5.md).

**Alpha 6 renderer/performance direction:**

- [x] Ship an authored horizon draw pass and meshes in the mod, with 0–256-chunk controls independent of full-detail and simulation distances.
- [x] Bound sampling work, network requests, client/server caches, and GPU uploads; use no full chunk requests for the horizon.
- [x] Yield renderer patches/resources automatically when a recognized replacement is installed; retain worldgen/server optimizations and diagnostics.
- [x] Add a read-only localhost dashboard with bounded memory, frame/tick and GC history, world counters, and diagnostic export.
- [ ] Replace native near-block mesh compilation and submission with the authored renderer after differential/visual benchmarks.
- [ ] Refine horizon materials, biome coloring, transition meshes, multi-resolution tiles, generated structures/builds, and conservative terrain occlusion.
- [ ] Implement a functioning Vulkan backend with OpenGL fallback and validate real GPU/third-party combinations.

See [renderer and diagnostics guide](docs/RENDERER-AND-DIAGNOSTICS.md). The current horizon is an approximate height field, not a complete voxel or saved-world LOD representation. It deliberately does not request full chunk generation.

**Vulkan implementation:** alpha 9 introduced the experimental device/presentation/shader adapter with OpenGL recovery; alpha 10 separates Vulkan, graphics settings, common performance and core modules, and adds persistent caches, descriptor reuse, synchronization and resource-lifetime fixes. See [capability audit](docs/VULKAN-CAPABILITIES.md) and [module boundaries](docs/MODULES.md). Hardware performance certification, replacement near-block meshing and advanced GPU-driven rendering remain open, separately from LOD and world-height feasibility.

### M3 — Shared simulation, adapters, and primitive survival

**Status:** not started. **Depends on:** M0–M2 and decisions D08–D10.

- [ ] Specify TFC-style time behavior (including default empty-server calendar pause), with verified subsystem catch-up and sleep rules.
- [ ] Verify keep-inventory defaults include clothing, quiver, and carried containers without duplication.
- [ ] Connect all process durations to the shared calendar; verify changes to day length behave consistently.
- [ ] Implement the shared heat model and the playable Age 0–1 loop: knap → fire → clay → charcoal → kiln.
- [ ] Build property resolution and the adapter prototype for vanilla dirt, gravel, sand, and stone, including tool-based breaking, gravity, and cave-in behavior.
- [ ] Track vanilla profile coverage and unknown/foreign-content fallbacks; distinguish mappings from support for special block logic.
- [ ] Add a temperature and one-layer clothing prototype with wetness, drying, shelter, and seasonal inputs.
- [ ] Connect heat to frozen-ground thawing and verify the chosen tool/age restrictions do not create progression dead ends.
- [ ] Prototype true-mass encumbrance independently of configurable stack counts, including an above-platform-cap test if a custom inventory path is needed.
- [ ] Deliver the native field-guide loader and inspection API, with discovery/tool gating and optional Jade integration subject to version support.
- [ ] Demonstrate catalog functions through the implemented systems; track lime, fuel quality, knapping, and nitrogen-fixing behavior individually where applicable.

**Exit evidence:** a repeatable new-world Age 0–1 playthrough; adapter behavior checks; climate/clothing/heat interactions; inventory transfer and save/network round trips; useful field-guide pages. Features still absent stay unchecked even if their data definitions exist.

### M4 — Mechanical, signal, and explosive prototypes

**Status:** not started. **Depends on:** M3 and decisions D11–D13.

- [ ] Implement one working stress-unit chain: water wheel → axle → mill, including overstress and persistence.
- [ ] Define the stress-unit API and conversion contract for a future optional Create bridge; do not require Create for the base mod.
- [ ] Implement face-mounted cable models and connection rules, one gate, and one signal-controlled clutch within the agreed signal scope.
- [ ] Verify signal graph updates, load/reload behavior, update budgets, and a recovery/rebuild path.
- [ ] Implement the powder-keg prototype: fill, seal, ignite, scale blast to powder load, and apply material resistance and support damage.
- [ ] Verify vanilla signal/TNT modes and configured coexistence behavior where implemented.

**Exit evidence:** working mechanical chain; clutch controlled by a persisted signal network; loaded versus empty keg behavior; blast/cave-in interaction; bounded updates on a dedicated server. Full late-game automation is not implied by these prototypes.

### M5 — Integrated v0.1 content and configuration

**Status:** not started. **Depends on:** M3–M4.

- [ ] Join the prototype systems into documented progression with obtainable recipes, appropriate tools, and no circular unlocks.
- [ ] Complete the 10-rock/10-tree prototype's agreed natural functions and track any dependency on farming/ecology explicitly.
- [ ] Wire module/default-preset choices, per-domain tuning, phase-1 vanilla toggles, and adapter options through the integrated systems.
- [ ] Generate the config reference from tunables and expose it through the guide.
- [ ] Check enabled, disabled, missing-dependency, and invalid-data cases; verify optional modules become inert according to their lifecycle rules.
- [ ] Exercise the chosen existing-world migration policy for module/config changes and preserve placed content safely.

**Exit evidence:** zero-setup default experience, an alternative preset/module configuration, complete prototype documentation, and save/reload checks across both. Broader feature designs remain in the backlog until their implementation and validation are recorded.

### M6 — v0.1 candidate validation

**Status:** not started. **Depends on:** M0–M5 and resolution or explicit deferral of conditional work.

- [ ] Produce a versioned JAR from the documented build and record its commit and dependency versions.
- [ ] Test clean client startup, dedicated-server startup, client join, new-world generation, and save/reload.
- [ ] Repeat the Age 0–1, climate/clothing, inventory, adapter, mechanical, wiring, and explosive scenarios.
- [ ] Re-run catalog, rendering, worldgen, simulation, and networking benchmarks against M0 budgets.
- [ ] Document limitations, known bugs, incomplete features, migration requirements, and reproducible test results.
- [ ] Choose the artifact label from evidence: experimental/alpha, beta, or release. A successful compilation alone is not proof of playability or release readiness.

**Exit evidence:** reviewable artifact and validation record. Worldgen performance-budget failures block release; pregeneration alone cannot satisfy the live-generation gate. No release date or stability label is promised in advance.

## Decisions before implementation

The [complete question register](docs/design-reference.md#s8) preserves all 31 original questions. This table groups them by the milestone they affect; it does not silently settle them.

| ID | Decision | Needed by | Original questions |
| --- | --- | --- | --- |
| D01 | Initial prototype target selected: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25, Gradle 9.2.1; broader version support undecided. | M0 | 1 |
| D02 | Original versus reusable assets/code and license constraints. | M0/imports | 3 |
| D03 | deepprimal/domain config organization and flexible ore authoring are confirmed; implement schema/precedence/migration and saved-world rules. External companion editor remains proposed; module defaults/UI scheduling still need selection. Required core remains enabled. | M0 | 9, 10, 30, 31 |
| D04 | Initial catalog coverage and how later tiers expand it. | M1 | 16 |
| D05 | Rendering quality/performance thresholds and hand-authored versus parametric trees. | M1 | 17, 19 |
| D06 | Expanded vertical terrain is desired and performance is mandatory; verify target height limits and select measured profiles alongside Earth datasets/licenses, projection, and vertical scale. | M2 | 18, 27 |
| D07 | Built-in native renderer selected; near/far quality, saved-world coverage, default tuning and measured budgets remain open. | M2 renderer follow-up | 25, 26 |
| D08 | Vanilla-first versus foreign adapters, adapt/convert default, and special handling for pistons, portals, hoppers/droppers, and minecarts. | M3 | 2, 11, 12 |
| D09 | Calendar defaults, stack ceiling, permafrost tools/thawing, and ice-cementation scope. | M2–M3 | 5, 20, 21, 22 |
| D10 | Inspection gating and guide source format/schema. | M3 | 23, 24 |
| D11 | Stress-unit scale and eventual Create bridge contract. | M4 | 4 |
| D12 | Powder capacity, ignition/fuse rules, crafting, carrying, and blast tuning. | M4 | 13 |
| D13 | Initial wire logic, gates/channels, and technology-age placement. | M4 | 14, 15 |
| D14 | Groundwater recharge, finite ore veins, keep-inventory-on, cargo recovery, and escalating fatigue are confirmed. Exact fatigue tuning and fauna/threat roster remain open. | Relevant later systems | 6, 7, 8 |
| D15 | Whether to pursue Hytale or an independent game; engine/language only if pursued. | Future platform planning | 28, 29 |

Open choices can be researched while building independent foundations. Do not treat an unverified stack limit, platform API, compatibility claim, or external license in the design draft as an established implementation constraint.

## Long-term backlog

These are retained design goals beyond the narrow prototypes above. Scheduling is not yet locked; the detailed reference remains the source for their behavior.

| Workstream | Remaining depth | Design sections |
| --- | --- | --- |
| Natural world | Wider rock/tree coverage, soils, crops, regional vegetation, ecological functions, seasonal cycles. | 4.1, 4.17 |
| Metallurgy and processing | Full forging, welding, alloys, bloomery/blast-furnace progression; crushing, washing, purification, forming, and byproducts. | 4.4, 4.8 |
| Food and farming | Decay, nutrition, preservation, soil fertility, crop rotation, animal familiarity, breeding, aging, hunting, and butchery. | 4.6 |
| Survival and clothing | Hunger, thirst, safe water, injury, fatigue, full layered garments, textiles, tanning, repair, and seasonal survival. | 4.9, 4.10 |
| Hazards and fauna | Custom wildlife/threats, darkness, gas, fire spread, structural integrity, and broader collapse behavior. | 4.7, 4.10 |
| Mechanical industry | More power sources and machines, ratios, belts/chains, refined processing, and late mechanical automation. | 4.5; Ages 3–6 in 5 |
| Signals and explosives | Broader gates, sensors, actuators, bundled channels, manufacturing, and configurable blast/ignition options. | 4.15, 4.16 |
| Building and rendering | Expanded architectural shapes and material catalog; advanced rendering only where measured needs justify it. | 4.12, 4.17 |
| Adapters and integrations | Complete vanilla coverage, scoped foreign-mod support, Create bridge, and optional inspection integrations. | 4.5, 4.10.1, 4.14 |
| Conversion and server tools | Vanilla removal phases 2–4, wider module/config coverage, in-game settings UI, migration tools, and multiplayer balance. | 0.2, 4.11, 4.13, 4.18 |
| Long-term play | Seasonal planning, regional resources, settlements, logistics, and meaningful automation across Ages 0–6. | 5, 6 |

**Outside v0.1:** electricity/GregTech-style electrical machinery, space, extra dimensions, magic, a complete original-art pass, and broad compatibility with vanilla-dependent mods. The optional Create bridge remains the deliberate compatibility direction. See [non-goals](docs/design-reference.md#s9).

## Additional design follow-up

The [dated decision register](docs/design-decisions-2026-10-18.md) now also covers hearth return, persistent capped character XP, cargo recovery/fatigue, a fishing species journal, finite bedrock reserves, salvage/disposal, and an optional rotation bridge. These need release placement and balancing. It records groundwater/wells; transport and storage; TFC time behavior; shelter; keep inventory; maps; health and hunting; fishing; renewal and waste; professions; and optional cooperation. Agree the unresolved rules before expanding implementation checklists. Full steam vehicles, specialized fishing, web mapping, profession caps, and rifles have no assigned release.

## Completion and maintenance rules

- Check an item only when its implementation and stated verification exist; link the relevant commit and evidence.
- Label partial work explicitly. A schema, concept, or prototype does not mean the full feature is complete.
- Keep task status here. Keep system details and catalogs in the design reference to avoid competing checklists.
- Resolve decisions with a dated rationale and update both documents where behavior changes.
- Record scope reductions and deferrals explicitly rather than removing promised features during cleanup.
- Keep numerical balance values provisional until playtested; select technical budgets before measuring success.

### Cleanup record — 2026-10-18

Separated implementation tracking from the full design draft; converted flattened tables and lists into Markdown; retained section references, catalogs, all open questions, and prototype goals; clarified proposal status, required core services, and existing-world safety. No gameplay implementation or completed build is claimed by this documentation update.

## Drainage and chunk-derived LOD direction (2026-10-20)

See [the researched architecture and executed meshoptimizer probe](docs/LOD-AND-DRAINAGE-ARCHITECTURE.md). The next terrain priority is shared cross-region drainage/outlet contracts, then unified river/lake planning and material masks. The next renderer priority is immutable chunk summaries, edit invalidation and constrained meshes with measured screen-space error; meshoptimizer remains a development probe, not a bundled LOD engine. Alpha 15 keeps the procedural horizon as a background and makes no Distant Horizons/Nanite parity claim.


## Environmental planning completeness (2026-10-20)

The owner has requested an exhaustive environmental planning list before implementation. The [environmental coverage inventory](docs/ENVIRONMENTAL-COVERAGE-INVENTORY.md) records 22 coverage domains and 163 open planning reviews, preserving existing rock/tree catalogs and expanding soil, clay, grass, organic matter, sediments, decomposers, aquatic/coastal/underground habitats, states and natural processes. Added examples are proposals pending disposition. Its checkboxes measure planning review only; existing implementation statuses and release scope remain unchanged. Environmental implementation is deferred until requested.

### Named environmental lists (2026-10-20)

The [named type catalogs](docs/NATURAL-TYPE-CATALOG.md) expand the planning roster to 1013 named entries across geology/soils/clays, biology/organic materials and environmental media/habitats/processes. Counts include groups, subtypes, states and forms as labeled, rather than unique species or approved features. All original rock/tree seed rows are retained. Environmental implementation remains deferred; representation, scientific-name reconciliation and property-sheet decisions remain planning work.

## Biome planning roster (2026-10-21)

The owner requested a connected natural-world biome plan before environmental implementation. The [biome plan](docs/BIOME-PLAN.md) defines **118 proposed broad biome profiles across 19 families**, **54 regional/community variants** and **63 local habitat/landform/state layers**, with all **62 existing HABITAT-TYPE entries** reconciled. The [named roster](docs/BIOME-CATALOG.md) covers climate, physical setting, ground themes, vegetation/life, water regimes and neighboring environments. The [variant/coverage crosswalk](docs/BIOME-VARIANTS-AND-COVERAGE.md) separates species/material/seasonal variety from biome IDs. **Confirmed planning sequence: complete biomes first, then design the climate scenarios needed to support them.** Existing climate bands must not dictate the biome roster, and quantitative climate thresholds are deferred rather than gating biome planning. Climate eligibility will gate where biomes generate, alongside terrain, water and other habitat requirements. The later climate plan must include northern/southern polar regions, an equatorial region, coherent latitude trends, regional terrain/ocean/moisture variation and hemisphere-aware seasons in procedural worlds as well as Earth mode; coordinates and scale remain open. Ocean circulation must also reflect winds, rotation, basin/coast/seafloor geometry, depth and temperature/salinity, with surface/deep flow, vertical exchange and climate/habitat effects; see the [ocean circulation plan](docs/BIOME-PLAN.md#ocean-circulation--confirmed-direction). The owner also confirmed traits from actual named planets/exoplanets as environmental references; [island and planetary planning](docs/BIOME-PLAN.md#islands-and-planetary-references) records sourced examples and proposed integration rules. Trait selections and world-profile scope remain open. The owner approved [glacial landscape coverage](docs/GLACIAL-LANDSCAPES.md): 3 additional community variants and 8 layers for valleys/fjords, debris, outwash, meltwater, lake margins and foreland communities. Profiles remain planning proposals. Existing generation/renderer work and implementation checkboxes are unchanged. Environmental implementation stays deferred until requested; world block/item texture tiles are planned at up to 64×64 with shared families and material palettes/tints.
