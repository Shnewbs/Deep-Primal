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
