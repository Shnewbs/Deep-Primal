# Drainage, materials and a chunk-derived LOD renderer

Status, 2026-10-21: **governing direction with an integrated alpha 17 implementation.** Deep Primal has not been renamed. New normal worlds now select our original terrain/biome/fluid path. The actual-chunk LOD and custom graphics screen are integrated into the main mod. Previous terrain profiles/modules remain available for compatibility; the separate alpha.1 experiment remains historical and is not shipped. This is a working foundation, not final visual/performance acceptance. See [validation](VALIDATION-0.0.1-alpha.17.md).

Alpha 18 adds an audit and recovery pass: registry context composition for KubeJS, bit-exact terrain calculation reuse, greedy voxel faces, sorted memory-pressure eviction, fair live/disk scheduling, progress under continuous fluid edits, best-effort disk persistence and bounded fluid border wake-up. See [alpha 18 evidence](VALIDATION-0.0.1-alpha.18.md). Current river basin repetition, synthetic LOD page walls and placeholder vegetation remain unresolved visual defects; the existing version-one geography is preserved.

Alpha 19 gives graphics and LOD separate ownership: shared GPU/UI infrastructure remains in `graphics-settings`, and `lod-engine` owns actual-chunk capture, cache/streaming, edit invalidation and rendering. The main mod consumes an optional interface. Video Settings opens our screen directly with complete native option coverage. Independent 16-unit liquids now include bounded connected-pool pressure; displacement, recharge and coast surf remain unfinished. See [module contracts](MODULES.md) and [validation](VALIDATION-0.0.1-alpha.19.md).

Alpha 23 adds Vulkan GUI render-target origin corrections, authored searchable graphics controls, three native water-transport substeps per host tick, and `continental_v2` river cuts for new worlds. Version one remains serialized and bit exact for existing worlds. Column sample reuse preserves generated block output. These fixes do not close regional water-cycle, complete lake/outlet, breaking-surf or advanced LOD requirements; see [alpha 23 evidence](VALIDATION-0.0.1-alpha.23.md).

**Owner clarification:** these projects are inspiration and comparison targets for better Deep Primal systems. Do not treat adding a dependency, copying a feature list or matching a screenshot as completion. Establish improvements through conservation/correctness, useful behavior, integration and matched performance measurements.

## Engine charter and quality gates

**Confirmed:** Deep Primal builds its own original terrain/worldgen engine inside the mod. The selected independent path is implemented through [TerrainEngine](../engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java), [EngineChunkGenerator](../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java) and the native fluid adapters. Minecraft/NeoForge still host saves, scheduling, registries, lighting, networking and GPU integration. This is an independent world-design subsystem, not a replacement for every host API or a claim that final quality is achieved.

| Reference group | What guides our implementation |
| --- | --- |
| TFC and GTCEu Modern | Coherent geology/river behavior and flexible deposit definitions/authoring. See [ore/config contract](CONFIGURATION-AND-ORE-AUTHORING.md). |
| Terralith and Tectonic | Landform variety, relief, coasts and cave/mountain-river examples, adapted to our natural setting. |
| Gaea and World Machine | Connected drainage before carving, discharge-aware cross-sections, generation-time resistance/wear/material context. Full live sediment transport remains optional/default-off. |
| Streams Reflowing, Underground Rivers and the audited water/snow references | Channel continuity, bank/source scale, underground passages, fluid behavior and atmosphere; inspect exact features rather than claiming bundled parity. |
| Voxel Tools, Luanti, Veloren, Vintage Story and Voxel Plugin | [New voxel research shortlist](VOXEL-ENGINE-REFERENCES.md): runtime data/jobs, regional generation and authoring workflows; none is adopted as our runtime. |
| Distant Horizons, Cesium, Nanite, meshoptimizer/clusterlod | Distance coverage, hierarchy/error/streaming and constrained mesh processing; separate architecture references from candidate code components. |
| USGS, NPS and NOAA | Geological/hydrological grounding. Scientific sources inform simplified rules; they do not make a game generator scientifically complete. |

Required quality gates:

1. **Connected world decisions:** choose the biome roster first; share geological, terrain, drainage and soil inputs instead of stacking unrelated generators. Later climate gates placement.
2. **Physical plausibility:** accepted host-controlled ores/gems, supported channels/caves, local water budgets and believable materials. Enforce no sinkholes and optional/default-off live sediment transport.
3. **Determinism and saves:** immutable versioned region plans, shared boundaries, independent seeded candidates, identical results across worker/order/cache changes and explicit saved-generation policies.
4. **Bounded runtime:** reuse fields, bulk data access and compiled definitions; no per-voxel scripts/file reads, forced whole-world loading or unbounded work/memory queues. Distinguish simulation, generation, near rendering and LOD budgets.
5. **Actual visual acceptance:** inspect native fixed-seed coasts, cliffs, soil cuts, mountain/cave rivers, confluences, lake sources, forests and ore cross-sections. Floating fragments/water sheets, repeated bowls, page walls and placeholder scatter are unresolved defects until captures prove otherwise.
6. **Editable-world correctness:** preserve player edits, water quantity, lighting/material classes and revisioned cache ownership through unload/reload, multiplayer, teleport and resource changes.
7. **Controls and authoring:** [domain-organized config paths and typed deposit data](CONFIGURATION-AND-ORE-AUTHORING.md), working effective controls, documented reload/world locks and source-aware validation; external editor remains proposed.
8. **Measured performance:** fixed camera/exploration/edit suites, named hardware and p50/p95/p99 frame/tick/chunk readiness, throughput, RAM/VRAM, I/O/network and edit lag. Software-driver evidence does not certify the owner's AMD driver. No cross-engine or DH superiority claim without matched conditions.
9. **Modularity and provenance:** retain core/host/graphics/LOD/Vulkan boundaries and the max-64×64 world-texture/shared-tint direction. Pin sources/licenses before actual reuse; proprietary engine code is not a shortcut to the target.

These are the meaning of the requested exceptional quality: coherent behavior, useful authoring, clean integration and demonstrated performance/visual results.

## Current owner follow-up — 2026-10-22

The [terrain/water/snow/LOD completeness audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md) is the current detailed feature mapping against alpha 20. It checks 20 owner-linked projects, including the [Streams Reflowing, Canopy Cascade and Puddles & Floods follow-up](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md), and distinguishes native implementation from older configured hooks. TERRAIN-04, WATER-07 and WATER-08 add the detailed stream/bank, canopy-moisture and puddle/flood requirements. Alpha 20 improves bounded liquid activation/transport and mesh-build progress; it does not establish frame-interpolated water, complete watershed budgets or finished visual quality.

**P0 rendering report:** the owner observes graphical problems disappearing when "horizonal view" is removed. The exact control and responsible pass are unresolved. Reproduce full-detail distance, actual-chunk LOD, legacy horizon and water overlays separately, then fix enabled-view behavior. A historical depth fix, passing a small fixture, or leaving the feature disabled is not acceptance for this report.

**Water/source acceptance:** require supported partial-height descending steps, proper banks and source scale, distinct river speeds, source-derived color/confluence mixing, richer coasts/currents and climate-aware deep snow. A broad headwater needs a substantial contained lake or sufficient accumulated supply; a small spring should begin narrow. See P0-RENDER, TERRAIN-01/02, WATER-01/03, OCEAN-01/02 and SNOW-01 in the audit.

## Adopted water-cycle direction — 2026-10-22

The [water mechanics contract](WATER-BUDGET-REQUIREMENTS.md) is now owner-approved design: finite physical water locally, metered renewable regional reservoirs, perennial major-river allocations, seasonal glacial/desert surface waters, depletable wells, and accounted ocean/atmosphere recycling at stable average sea level. Local backwater/flooding remains meaningful; strict depletion and source safety are optional. Evaporation/infiltration/drainage drive real recession rather than hiding fluid. Implement coarse/loaded ownership with bounded work and no double accounting. Alpha 21 presentation does not implement these reservoir/cycle mechanics.

### Natural waterway head correction — dev.6

An elevated generated creek reproduced a supply failure: the regional minimum
terrain datum put groundwater head below its outlets even while finite reserves
were active. `Aquifer.naturalDatum` now references each planned natural surface
at the drought-stop reserve. `HydrologyRuntime` uses that same head reference
for infiltration and metered replenishment. This is a simplified perched-waterway
model sharing the existing finite regional account, not a new reservoir, infinite
refill or calibrated three-dimensional groundwater simulation. Unplanned
dry-land springs retain the regional terrain datum and sustained-recharge rule.
The 32-unit regional quota, fractional stage cap, chemistry, drought shutdown,
saved terrain and player edits remain in force.

Real-server checks monitor high/low creeks and a wider river for 1,200 ticks;
every-tick wet-column continuity supplements 100-tick bank/stage observations.
These are bounded loaded-reach checks, not full catchment conservation, storm/
drought acceptance, perennial-river allocation or temporal rendering acceptance.
The high creek still loses up to 1.0625 blocks of stage. See
`ACTIVE-ENGINE-AUDIT.md` for executed results and outstanding gates.

### Steady natural-source scheduling — dev.7

Dev.6 diagnostics showed the remaining high-creek loss was not the regional
quota or reserve: the broad hydrology scan covered 49 loaded chunks, so one
planned source column was topped up roughly every 196 ticks, only 4 units at a
time and only into the top outlet cell. `HydrologyRuntime` now keeps a compact
per-chunk list of planned waterway columns, found by a deadline-bounded survey
of visible engine water after chunk load and by ordinary visits. Each listed
column gets at most one top-up per 20-tick aquifer interval. The top-up targets
the actual deficit cell up to two blocks below the planned outlet. That
cell is wholly below the planned stage, so it cannot raise the reach above plan.
Natural delivery is limited by aquifer head pressure, fractional stage capacity,
finite groundwater and the unchanged 32-unit regional quota. `sourceRate` still
meters dry-land springs. This changes scheduling and placement, not the finite
budget; quota saturation now limits recovery as intended.

### Headwater baseflow quota — dev.8

The remaining startup dip was bounded by the 32-unit dry-spring quota, which planned waterways
shared. Planned waterways now draw on a separate regional natural quota,
`Aquifer.baseflowUnits(rainQuantaPerColumnSecond)`. It equals the long-run mean rain recharge of
the 64x64 region: vanilla's mean weather duty is 18,000 rain ticks of 114,000, taken at half
permeability. With the default 64 quanta that is 80 units per interval, and it is never below 32.

The quota is still debited from the finite aquifer and is still limited by head pressure, drought
shutdown and the planned stage cap. The cap per interval matches the region's modeled mean
recharge at half permeability; actual recharge varies with permeability and weather. The
cap cannot fill a reach above plan.

Columns are ordered headwater-first because springs feed the head of a reach and lower columns are
fed by flow. Dry-land springs keep the 32-unit quota.

### Between-step drought protection - dev.9

Pressure alone cannot enforce the drought reserve: an outlet below planned stage
can have positive head even at STOP. `Aquifer.dischargeUnits` therefore caps each
delivery to whole native units above STOP, and `canSupply` checks the current
balance as well as the active latch. Multiple outlets within an interval cannot
spend the protected reserve. The next environmental step still latches drought
and requires sustained recharge before reactivation. Evaporation remains allowed
to draw down inactive groundwater; no water is created or discarded by this cap.

Six core scenarios exercise long drought/rain/snow/melt sequences and exact
water/salt/sediment totals across 180,000 intervals. Native block-write checks
cover six reserve boundaries and repeated delivery without an intervening step.
These do not replace native catchment-scale weather, permeability and ledger gates.

### Regional clock versus local permeability - dev.10

An impermeable surface blocks local recharge and outlets, not the drought clock
of groundwater that already exists below it. Loaded-column visits now advance
that regional account before returning for sealed surfaces. They do not seed a
new aquifer under sealed ground. Rain allowance is tracked separately against
the regional environmental step, allowing the next eligible permeable visit to
collect rain once without rerunning snowmelt/circulation or resetting discharge.
Runtime-cache reconstruction remains fail-closed until the next aquifer step.

Native checks cover sealed-first visitation, subsequent permeable rain,
duplicate visits, no new reserve and exact accounted constituents. Rain amount
still extrapolates one eligible column to the loaded region; spatial sampling
and complete catchment acceptance remain unfinished.

### Loaded-chunk representative precipitation - dev.11

The first eligible visited soil no longer sets rainfall for the entire region.
Once per environmental interval, sum fixed chunk-center samples for at most
16 already-loaded chunks. Each contributes its permeability times 256 columns
and the configured rain rate, routed by that sample's precipitation biome to
groundwater or snow. Live impermeable roofs and ocean boundaries are excluded.
The finite atmosphere pays accepted transfers; unloaded chunks are never requested.

This is a coarse representative estimate, not exact integration over all 4,096
columns. A snapshot is consumed even if all sampled roofs are sealed; roof edits
apply next interval. Since dev.16, rain and snow share scarce atmospheric water
in proportion to their requested amounts capped by each destination's free
capacity. Full stores have no claim on that interval's supply; unused demand
does not become a backlog. Integer division leaves less than one quantum of
rounding remainder to snow. Both transfers are preflighted together and preserve
exact total water, salt and sediment. Single-phase and sufficiently funded
weather retain their requested amounts.
Native grass/sand visit-order checks reproduce the previous bias and verify the
new weighted sum, finite atmospheric exhaustion and absent-region behavior.
Dev.16 additionally checks 24 mixed-biome/capacity cases with swapped jungle and
snowy-plains samples and 10,000 randomized core splits against an independent
integer oracle. Many-region storm latency, full-column integration and broader
catchment/seasonal calibration remain unaccepted.

### Exact hydraulic sampling subset - dev.17

Precipitation no longer evaluates a terrain column and the entire strata/deposit
column to obtain one permeability number. Soil textures, sand/gravel, dirt/grass
and impermeable structures use the same live block rules directly. Rock and ice
query `Geology.permeability(x,z)`, the exact two-field province/fault subset of
`Geology.column`. Ordinary environmental visits use that subset too, while their
terrain sample remains necessary for waterway planning, climate and aquifer
initialization. No terrain revision, precipitation weight, storage rule or
sampling/cadence policy changes.

`GEOLOGY_PERMEABILITY_SUBSET_PASS` checks 40,000 coordinates/four seeds against
both the original noise formula and full columns, bit for bit, including
negative/distant coordinates and all province/fault values. The private
`StormSamplingChecks` fixture compares packaged dev.16/dev.17 on the same four
loaded regions (64 centers), including all 36 soil forms, four vanilla soil
controls, nine rock controls, four ice/snow controls and sealed glass.
It checks exact recharge and chemistry against the full-column oracle on every
batch. Fixture skylight is explicitly enabled and completed before timing; no
generation, biome editing or lighting setup is counted as sampling work.

Repeated same-host timings, 100 warm-up and 200 measured batches per scenario:

| Four-region component | dev.16 p50/p95 (ms) | dev.17 p50/p95 (ms) |
| --- | --- | --- |
| Soil | 0.3752 / 0.5832 | 0.1347 / 0.2519 |
| Rock/ice | 0.3655 / 1.5246 | 0.0746 / 0.0950 |
| Sealed glass | 0.2850 / 0.4157 | 0.0287 / 0.0609 |

Evidence: `build\storm-dev16-baseline.json`, `build\storm-dev17-candidate.json`,
`build\storm-dev16-repeat.json`, `build\storm-dev17-repeat.json` and their logs.
Host: AMD64 Family 26 Model 112 Stepping 0, AuthenticAMD; Java 25.0.4.1+1-LTS.
These are warmed, synchronous sampling-component distributions in a controlled
playerless fixture, not real storm tick/frame latency or a hard 2 ms guarantee.
Full-tick scheduling/contention, cold region work and large loaded-world storm
acceptance remain open.

### Bounded presentation settling - dev.18

Finite one-unit parcel reversals can hold the displayed height at their midpoint
without changing the authoritative 16-unit simulation. That hold must not become
permanent when flow stops. Versioned `WaterSurfaceTransition` samples now expire
the hold 400 ms after the last real reversal and retarget from the currently
displayed value to the last raw height over 100 ms. Duplicate shared-corner
publications neither restart the raw clock nor extend expiry; stale revisions
are rejected before expiry handling. Monotonic/large/slow changes retain exact
targets, and unversioned opacity retains its ordinary interpolation.

The renderer includes hold expiry in each uploaded page's cleanup deadline,
even when its visible height is currently still. Cleanup recapture services
expiry, then schedules the final interpolation cleanup; settled water returns
to exact height with no perpetual cleanup. No solver, mesh topology, quantities,
source quotas, terrain revision or saved-state format changes.

The old code fails `build\dev17-settle-repro.log`. Core checks cover 150 release
cases. `WaterFadeContinuityChecks`, also run by the no-world material preview,
uses an isolated real GPU renderer/mesh with a controlled clock on OpenGL and
Vulkan: 8/9/8 units settle at 0.53125, retain the original 1501 cleanup through
an identical recapture, return continuously to 0.5, then have zero displacement
and cleanup at 1602. Existing dry/rewet/edit guards and GPU release pass.
Evidence: `build\gates-dev18-client-{OpenGL,Vulkan}-retry.log` and
`build\stream-dev18-packaged-{opengl,vulkan}-captures\water-fade-continuity.txt`.
Follow-up `build\gates-dev18-client-{OpenGL,Vulkan}-readback.log` verifies three
fenced GPU copies per API at hold/release/idle against every accepted byte,
including twelve interior corner target/displacement witnesses per API. The
fixture adds copy-source capability only to its private GPU allocation and waits
for readback before preview reload/exit; production allocations are unchanged.
These are actual-upload/readback functional checks, not complete rendered live-stream/
shared-edge/connected-waterfall or frame-time acceptance.

### Running-world height publication failure - dev.18 follow-up

The private `water-live-continuity-check` follows visible resident top-boundary
corners for 600 client ticks in a cloned natural high-creek world. It decodes
retained upload bytes with the production shader's elapsed-time/height formula,
deduplicates each page/corner within a tick and compares neighboring pages at
the same world position and base layer. Fully opaque tops only; no water edits
or simulation freezing. Same-page repeated vertices are a consistency control.

OpenGL produces 25,256 disagreements in 122,425 comparisons, maximum 0.203125
blocks, across 37 pages. A Vulkan repeat produces 27,513 in 124,581, maximum
0.33333302, across 38 pages. Both same-page controls have zero disagreements.
Both saves/shutdowns complete; no paused ticks. See the live evidence in
ACTIVE-ENGINE-AUDIT. Different scheduling means these counts are not an API
comparison, and CPU mirror observations are not rendered GPU pixel gaps.

The current shared `heights` map is consulted only when each page uploads.
Neighbor publication patches shared material-direction packets but not the
resident height endpoints/clocks, so sharing history does not make resident
geometry coherent. The next repair must update compatible height trajectories
without replaying opacity/material clocks, accepting stale edits or losing
failed-upload recovery. Whole-page rebasing/cohort publication must be measured,
not assumed to meet the extraction budget. Disabling interpolation or snapping
all boundaries would trade one visible defect for another and is not acceptance.
This publication failure remains open; no candidate fix is promoted here.

## Accepted terrain scope — 2026-10-21 PDT

The [terrain mechanics contract](TERRAIN-MECHANICS.md) governs realistic structural geology and host-controlled ores/gems, accepted erosion resistance, soil profiles, wind-shaped terrain, cave/karst mountain rivers and springs, slope stability with underground cave-ins, and natural obstructions. No sinkholes or sinkhole-producing live collapse. Full live sediment transport is optional/default-off; recipe-based panning/sluices are the baseline, with optional useful local provenance. Generated wear/material context and coarse water-composition blending do not require that live solver. IDs TERRAIN-05–10 provide the detailed acceptance requirements.

## Built-in terrain engine requirements and inspiration

The target is our own Overworld generator and biome/region decisions: original continents, connected relief/drainage/lakes, geological/climate fields, terrain-derived material rules and deliberate feature placement. Use Minecraft/NeoForge as host infrastructure; do not delegate those world-design decisions to the standard vanilla Overworld noise/biome pipeline. Generic noise functions and serialized data are implementation tools, not evidence of vanilla or original terrain by themselves. Nether/End remain native in the current prototypes; no replacement of those dimensions is claimed.

| Reference | Role in Deep Primal | Evidence/status |
| --- | --- | --- |
| [TFC](https://terrafirmacraft.github.io/Documentation/1.21.x/worldgen/world-preset/) | Coherent continents, regional climate/geology and drainage/river behavior | Partial region/river source review and official generator documentation. Full feature parity is not implemented. |
| [GTCEu Modern](https://gregtechceu.github.io/GregTech-Modern/1.20.1/Modpacks/Ore-Generation/01-Customizing-Veins/) | Flexible multi-ore deposit authoring and deterministic planned-versus-placed generation | Official authoring/developer documentation checked. Our schema, KubeJS bridge and GUI authoring are not implemented; no GTCEu runtime/source is bundled. |
| [Terralith](https://www.stardustlabs.net/terralith) | Landform/biome variety, distinctive terrain features and surface composition suited to our grounded setting | Official terrain descriptions and earlier river-specific references reviewed. Broad source/feature audit remains pending; no Terralith source/assets bundled. |
| [Tectonic](https://modrinth.com/datapack/tectonic) | Continental-scale relief, mountain ranges, deep oceans and continuity where rivers pass through mountainous terrain | Official feature description reviewed in this clarification. Proper source audit and mapped implementation checks remain pending; no Tectonic integration is claimed. |

These are references for one coherent Deep Primal generator, not three generators stacked together. Do not add every fantasy biome or village/structure merely because a reference provides it. Keep the exhaustive natural-material/biology catalogs as planning until their implementation is authorized.

**Recovery and acceptance:** preserve existing Deep Primal terrain, performance, settings and Vulkan work. Evaluate independent engine work against those existing behaviors and the agreed visual goals before replacement. Straight planner-grid canals, uniform hills and repeated placeholder tree scatter do not pass. Prove drainage/outlet correctness, generation-order independence, edit/support behavior and generation/memory budgets with reproducible tests and fixed-seed native captures. Test-passing transport/storage utilities alone do not establish terrain quality or guideline compliance.

## Required plan and integrated implementation

| Requirement | Deep Primal alpha 17 status |
| --- | --- |
| Continents and connected drainage before carving | Original warped continental provinces, immutable bounded regional plans, priority-flood downhill graph, rainfall-weighted accumulation and curved channel/valley carving. Unified lakes/spillways and shared cross-watershed outlet contracts remain unfinished. |
| Width, depth and speed from drainage/slope | Area-derived width/depth and grade/discharge proxy for stored current speed. Physical discharge, rapids and continuous dynamic river supply remain unfinished. |
| Terrain-derived materials | Elevation, slope, rain, climate, geological palette and sediment-placement proxies drive original placement. A wear/erosion/deposition solver and catalog geology are unfinished. |
| Floating-fragment prevention | Solid base columns and owned underground tunnel carving avoid the rejected floating-surface mechanism. General post-feature support/connectivity checks are unfinished; never remove player structures as cleanup. |
| Independent liquids and smooth water | Separate `Fluid`, 16-unit states, conservative gravity/pooling, own bucket and shared-corner curved patches are integrated. Bounded connected-pool pressure is added in alpha 19. Displacement, recharge and breaking shoreline surf are unfinished. |
| Actual-chunk LOD | Owning-thread immutable captures, edit revisions, vertical-run snapshots, three GPU mesh levels, projected-size/hysteresis, native-ready handoff, bounded work/memory/disk and world identity are integrated. Uses explored/delivered chunks; no unseen terrain stream or DH parity is claimed. |
| Custom graphical controls | Original six-tab screen with slider/typed controls, staged Apply/Revert, contextual help and saved/requested/effective display. Reopen, resize and process restart tested. |

The engine is a built-in terrain/liquid/LOD subsystem, not a separate game or a replacement of every Minecraft API. Near-block mesh generation, lighting, entities and game storage still use host APIs. The optional graphics API backend remains a separate retained Vulkan module. Alpha 17 adds no new terrain/LOD native dependency.

## Tool and reference decisions

### Planet Smith rendering reference

The owner supplied [this devlog at 10:25](https://www.youtube.com/watch?v=LzivS_KzffY&t=625s) and its transcript on 2026-10-25. The transcript, not video footage or source code, was inspected. Its transferable design is direct vertex/index uploads into paged GPU arenas, indexed indirect draw commands, hierarchical frustum rejection and a compact, explicitly bounded vertex format. This is still mesh geometry: it removes engine mesh objects, not triangles or meshing work. No code or assets from the project are incorporated.

Our current paths already upload immutable worker mesh output directly and update water allocations in place. `AnimatedWaterRenderer` owns bounded pages and history; `WaterGpuMesh` uses a 36-byte native vertex layout with packed animation/light fields. `VoxelLodRenderer` performs individual part-bound frustum tests. The Vulkan adapter's `VkRenderPass.drawMultipleIndexed` reaches `VkCommandEncoder.executeDrawMultiple`, which loops over ordinary indexed draws; that API name is **not** evidence of GPU multi-draw indirect submission. Near terrain remains host-generated and is not replaced by this reference.

Renderer work checklist, after the outstanding water/material correctness gates:

- [ ] Establish matched camera/travel/edit baselines: CPU culling, allocation search, uploads, draw/descriptor counts, GPU time, frame p50/p95/p99 and RAM/VRAM. Record hardware, enabled LOD/water, geometry/coverage and warm/cold state.
- [ ] Evaluate a maintained spatial hierarchy for LOD/water page bounds, including an inside-frustum fast path. Prove exact visible coverage under edits, negative coordinates, teleport, unload and memory pressure before claiming a gain. Compare against the current individual tests; do not assume GPU culling is faster.
- [ ] Evaluate bounded paged arenas with reusable allocation ranges and fence-safe retirement. Exhaustion must retain covering geometry or explicitly defer admission, never silently drop chunks. No whole-world mega-buffer rebuild after a block edit.
- [ ] Evaluate indirect submission only for compatible opaque batches sharing actual pipeline, vertex/index buffers and bindings. Per-page transforms/materials need a defined instance/draw-data contract and capability fallback. Preserve translucent water sorting rather than blindly coalescing draws.
- [ ] Derive any compact format from our real block/UV/lighting/tint/animation requirements and error bounds. Keep world positions camera/chunk relative; test distant and negative seams, UV/mips, AO and saved/edit behavior. Do not transplant the video's half-float or lookup-table limits without measuring our data.
- [ ] Accept improvements only after output-equivalent OpenGL/Vulkan, reload/shutdown, native editable-world and matched performance runs. Retain quantity, simulation budgets and generation determinism.

The video's reported frame rates, chunk counts and byte reductions describe that project's workload, not Deep Primal measurements. Hierarchical culling, indirect draws and packing are candidates, not implemented features or a new engine grade. Terrain/worldgen, stream containment and the authorized soil catalog retain priority.

| Reference | Decision |
| --- | --- |
| Gaea / World Machine | Use drainage, erosion/deposition and material-mask techniques in our own deterministic region planner. No proprietary engine code or authored terrain-map imports are included. Licensed map import remains optional, not required. |
| PhysEx, Finite Water, BetterWaterMesh, Water Erosion | Use behavior/design references for independently authored liquids, surfaces and erosion. No reference-project code is bundled. Full feature parity is unfinished. |
| Distant Horizons | Functional baseline and future matched performance comparison. No DH code is embedded; the full source audit remains pending. |
| Nanite | Geometry-hierarchy/streaming architecture reference only; no Unreal integration. |
| Cesium Native | Screen-space selection, streaming and fallback-coverage references; no Cesium runtime adopted. |
| meshoptimizer / clusterlod | First reusable-code candidate. The LWJGL development probe has run; neither a shipping meshoptimizer renderer integration nor a clusterlod hierarchy is implemented. |
| Simplygon | Possible offline asset-preparation reference only; not a current toolchain dependency. |

The viewer must preserve block silhouettes, player edits, materials and distinct water handling, with visibility/error-based selection and explicit memory/work budgets. Its intended settings cover radius/detail, work/upload and RAM/VRAM/cache limits, with correctly restored requested values and separate effective limits. Alpha 17 exposes these controls in its LOD tab. The legacy procedural horizon is off by default behind an explicit diagnostic config switch; disabling chunk LOD never enables it automatically.

Prioritize drainage/outlet correctness and material placement, then erosion/deposition and connectivity checks. Develop the viewer from authoritative chunk data. Keep performance budgets explicit throughout; outperforming Distant Horizons is a benchmark target, not an established result.

The references and work orders below remain the technical basis and outstanding quality/performance gates. Legacy alpha.15 descriptions are historical; the current viewer is authoritative-chunk based.

## Reference fit

| Technology | Verified useful idea | Deep Primal application |
| --- | --- | --- |
| [Gaea Rivers / HydroFix](https://docs.gaea.app/reference/nodes/simulate/hydrofix.html) | Alter slopes to sustain continuous drainage. | Plan shared drainage routes and lake outlets before carving final terrain. The current whole-continent graph remains region-local; cross-watershed connectivity needs shared boundary contracts. |
| [World Machine Create Water](https://help.world-machine.com/topic/device-create-water/) and [River Device](https://help.world-machine.com/topic/device-river/) | Drainage area/discharge affects river shape and velocity; explicitly controlled reaches support tributaries. | Extend the existing catchment/slope model with rainfall-weighted discharge, consistent width/depth, rapids and a common outlet plan. Area currently counts routing cells, not simulated water volume. |
| [Gaea Erosion](https://docs.gaea.app/reference/nodes/simulate/erosion) | Separate erosion, deposition and flow information. | Cache deterministic wear/deposition/flow summaries once per region. Derive valley gravel/silt, scree, exposed slopes and soil placement from those summaries. A full erosion/sediment solver is not present. |
| [Nanite](https://dev.epicgames.com/documentation/unreal-engine/nanite-technical-details) | Cluster hierarchies and streamed geometric detail. | An architectural reference, not an Unreal engine component to drop into Minecraft. Do not require mesh-shader hardware for the baseline renderer. |
| [Cesium Native selection](https://cesium.com/learn/cesium-native/ref-doc/selection-algorithm-details.html) | Refine visible tiles according to projected geometric error, keeping fallback coverage while children load. | Learn the scheduler and coverage rules without bringing the geospatial stack into the mod. Distance alone is insufficient for tall cliffs, zoom and viewport changes. |
| [meshoptimizer](https://meshoptimizer.org/) / [clusterlod](https://github.com/zeux/meshoptimizer/blob/master/demo/clusterlod.h) | MIT-licensed mesh processing; the separate C++ helper constructs cluster LOD hierarchies. | First reusable-code candidate. Start with constrained simplification/reordering on workers. The C++ clusterlod helper is not automatically exposed by the Java binding. |
| [LWJGL MeshOptimizer](https://javadoc.lwjgl.org/org/lwjgl/util/meshoptimizer/MeshOptimizer.html) | Native simplification APIs exposed to Java. | The executed probe uses version 3.4.1, matching Minecraft's LWJGL version. Its binding reports meshoptimizer version 1000. Current online snapshot docs and newer upstream headers must not be assumed ABI-compatible with this pinned build. |
| [Simplygon vegetation optimization](https://www.simplygon.com/features/vegetationoptimization) | Prepared vegetation LODs/impostors. | Useful offline asset workflow. It does not replace the live chunk/edit/lighting pipeline; no dependency is added. |

## Legacy prototype foundation and gaps

Alpha 15 retains bounded region caches, downhill drainage inside a catchment, area/slope-based speed, versioned terrain, partial river surfaces, spring taper and occasional contained headwater lakes. It fixes foreground composition by isolating approximate horizon depth. That background approximation still omits distant player edits, individual buildings, detailed trees, caves and true voxel silhouettes.

The present heightfield hierarchy has three stitched mesh levels, worker meshing, bounded uploads, server sampling budgets and memory accounting. It remains historical comparison code, disabled by default. It is not the authoritative representation of explored chunks and is not automatically used for unexplored terrain in alpha 17.

## Terrain work order

1. **Shared drainage boundaries.** Canonically key border outlets in world coordinates, sample halos, and resolve coarse watershed ownership before fine channels. Adjacent regions must agree on outlet elevation, discharge, direction and lake spill level, including negative coordinates and region corners. Test generation order independence and cache eviction. Do not simply join unrelated local networks at chunk edges.
2. **Unified river/lake plan.** Represent tributary joins, lakes and spillways as one acyclic graph. Pin headwater identities across resolutions. Derive cross-sections and velocity from the same discharge/slope data, with shallow streams, wider lowland rivers and explicit steep cascades.
3. **Material masks.** Store compact slope, relative elevation, moisture, flow and deposition fields in the same regional result. Reuse them for biome surfaces and LOD material summaries. Keep these fields independent of GPU/backend classes.
4. **Connectivity checks.** Distinguish unsupported real blocks from rendering artifacts. Check native solid/water support after carvers and features. Never run a global world scan or silently delete player constructions. Author terrain imports as versioned data, with declared units, bounds and checksums; evaluate actual asset licensing before including any imports.

## Chunk-derived LOD work order

1. **Capture authoritative data safely.** Copy small immutable section summaries from already delivered client chunks on their owning thread. Include material category, coverage, solid height intervals, water surfaces and lighting summaries. Workers must not dereference mutable live chunks, load server chunks, or retain raw chunk objects. Never generate a 512-chunk full-resolution world for the viewer.
2. **Version edits and caches.** Key data by world/dimension, schema, source generation/version and coordinates. Increment revisions for block/section updates, relighting and water changes. Bound dirty queues; coalesce updates. Reject stale worker results. Persist checksummed data with bounded decompression and atomic replacement, and keep it scoped to the actual server/world.
3. **Separate surface classes.** Maintain solid/cutout/water representations and explicit seam ownership. Water needs surface height, holes, tint, fog and transparency semantics. A transparent pass cannot safely share arbitrary material simplification with opaque terrain. Keep the blocky silhouette where requested, including roofs, cliffs, trunks and shoreline boundaries.
4. **Build constrained meshes.** Greedily merge coplanar block faces first. Then evaluate meshoptimizer with explicit vertex locks and material/normal boundaries. Lock shared page edges, water boundaries and hard features. `SimplifyLockBorder` alone is insufficient for our closed, skirted meshes: their tile perimeter is not an open topological border. Compact all vertex attributes after remapping; reducing indices alone does not reduce allocated vertex buffers.
5. **Select and stream.** Store per-page geometric error and bounds. Use projected error, frustum visibility and hysteresis; retain a covering parent until all required children are ready. Bound CPU work, native scratch, pending results, compressed/disk data, resident mesh bytes, isolated depth resources and upload work separately. Permit cancellation on teleport or disconnect. Prioritize real-chunk revisions over speculative macro terrain.
6. **Integrate both GPU backends.** Build through the native common GPU interface and keep optional meshoptimizer native code outside Vulkan ownership. The same LOD data should render with OpenGL or Vulkan. Preserve renderer handoff to external replacements. Design depth/transparent-water transitions before claiming occlusion or seamless interoperability.

## Executed meshoptimizer probe

Source: [standalone Java probe](../tools/lod/MeshOptimizerProbe.java), [reproducible runner](../tools/lod/run_meshoptimizer_probe.py), [recorded results](validation/0.0.1-alpha.15/meshoptimizer-probe.json).

The test uses our actual `HorizonMesh` builder on synthetic flat, hilly, cliff and water-boundary inputs. It requests approximately half the indices with reported absolute error at most 0.25, explicit roof-edge locks and protected cliff/shoreline columns. It verifies index bounds and retention of locked roof anchors. Ten warmups and twenty measured iterations run entirely on the CPU. Matching LWJGL 3.4.1 binding/native jars live only in the development build cache and are not packed into the mod.

The first run reduced 9,218 triangles to 4,608 for flat/cliff/water cases and 5,270 for hills: about 43–50% fewer triangles. All four retained their checked roof anchors. Worker-call medians were roughly 2.3–4.0 ms in this software-rendering/container environment; they are not a gaming-device performance promise. The reproducible JSON contains the subsequent run and exact dependency hashes.

This probe does **not** yet validate detailed voxel input, all normals/UV seams, live edit propagation, parent/child transition coverage, terrain texture appearance, full vertex compaction, a cluster hierarchy or in-game FPS. Adding a dependency to the shipping classpath before those checks would add complexity without demonstrated end-to-end benefit.

## Acceptance before replacing the horizon

Require native scenes for cliffs, caves/overhangs, forests, villages, player buildings, edited chunks, water joins and negative-coordinate page corners. Compare terrain silhouettes and surface ownership against the native renderer. Exercise teleport, reconnect, resource reload, cache corruption, memory saturation, queue cancellation and backend changes. Measure frame-time percentiles, worker latency, GPU timing, upload spikes, CPU/GPU/disk bytes and update lag against the existing baseline. Hardware testing on the user's AMD 8060S remains required before making comparative performance claims.
