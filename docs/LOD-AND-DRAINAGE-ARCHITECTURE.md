# Drainage, materials and a chunk-derived LOD renderer

Status: researched design plus an executed standalone meshoptimizer probe. Alpha 15 still ships the procedural horizon, not a Distant Horizons-equivalent voxel renderer. This plan does not imply full erosion, sediment transport, global watersheds or a new chunk LOD engine are already implemented.

## Reference fit

| Technology | Verified useful idea | Deep Primal application |
| --- | --- | --- |
| [Gaea Rivers / HydroFix](https://docs.gaea.app/reference/nodes/simulate/hydrofix.html) | Alter slopes to sustain continuous drainage. | Plan shared drainage routes and lake outlets before carving final terrain. The present 4096-block catchments remain region-local; cross-region connectivity needs shared boundary contracts. |
| [World Machine Create Water](https://help.world-machine.com/topic/device-create-water/) and [River Device](https://help.world-machine.com/topic/device-river/) | Drainage area/discharge affects river shape and velocity; explicitly controlled reaches support tributaries. | Extend the existing catchment/slope model with rainfall-weighted discharge, consistent width/depth, rapids and a common outlet plan. Area currently counts routing cells, not simulated water volume. |
| [Gaea Erosion](https://docs.gaea.app/reference/nodes/simulate/erosion) | Separate erosion, deposition and flow information. | Cache deterministic wear/deposition/flow summaries once per region. Derive valley gravel/silt, scree, exposed slopes and soil placement from those summaries. A full erosion/sediment solver is not present. |
| [Nanite](https://dev.epicgames.com/documentation/unreal-engine/nanite-technical-details) | Cluster hierarchies and streamed geometric detail. | An architectural reference, not an Unreal engine component to drop into Minecraft. Do not require mesh-shader hardware for the baseline renderer. |
| [Cesium Native selection](https://cesium.com/learn/cesium-native/ref-doc/selection-algorithm-details.html) | Refine visible tiles according to projected geometric error, keeping fallback coverage while children load. | Learn the scheduler and coverage rules without bringing the geospatial stack into the mod. Distance alone is insufficient for tall cliffs, zoom and viewport changes. |
| [meshoptimizer](https://meshoptimizer.org/) / [clusterlod](https://github.com/zeux/meshoptimizer/blob/master/demo/clusterlod.h) | MIT-licensed mesh processing; the separate C++ helper constructs cluster LOD hierarchies. | First reusable-code candidate. Start with constrained simplification/reordering on workers. The C++ clusterlod helper is not automatically exposed by the Java binding. |
| [LWJGL MeshOptimizer](https://javadoc.lwjgl.org/org/lwjgl/util/meshoptimizer/MeshOptimizer.html) | Native simplification APIs exposed to Java. | The executed probe uses version 3.4.1, matching Minecraft's LWJGL version. Its binding reports meshoptimizer version 1000. Current online snapshot docs and newer upstream headers must not be assumed ABI-compatible with this pinned build. |
| [Simplygon vegetation optimization](https://www.simplygon.com/features/vegetationoptimization) | Prepared vegetation LODs/impostors. | Useful offline asset workflow. It does not replace the live chunk/edit/lighting pipeline; no dependency is added. |

## Implemented foundation and gaps

Alpha 15 retains bounded region caches, downhill drainage inside a catchment, area/slope-based speed, versioned terrain, partial river surfaces, spring taper and occasional contained headwater lakes. It fixes foreground composition by isolating approximate horizon depth. That background approximation still omits distant player edits, individual buildings, detailed trees, caves and true voxel silhouettes.

The present heightfield hierarchy has three stitched mesh levels, worker meshing, bounded uploads, server sampling budgets and memory accounting. It is a useful fallback for unexplored terrain, but should not become the authoritative representation of explored chunks.

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
