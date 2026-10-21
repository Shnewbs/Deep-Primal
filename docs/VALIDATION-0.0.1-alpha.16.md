# Deep Primal 0.0.1-alpha.16 validation

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Alpha prototype; disposable worlds recommended.

## Implemented scope

- New versioned v6 profiles sample banks across the channel and generate stable partial channel water. Native spreading does not replace planned steps. All old serialized profiles and legacy river blocks retain their old behavior; this is not retrogen.
- Optional finite native water: eight 125 mB units/block, gravity, lateral pooling, bounded connected-pool pressure, native scheduled persistence and a per-dimension work limit. No chunk loads, waterlogged-cell draining or lava replacement.
- Measured partial bucket, native transactional machine ports, persisted per-column soil water, measured rain/runoff, evaporation and local storm intensity. Ports are not bundled pumps/tanks/pipes or a Flowing Fluids machine patch.
- Optional stamina/treading, equipment-aware pressure/effects, global snow in precipitating biomes and unlimited accumulation; local snow/weather fog and foliage support.
- Per-biome resource-pack water color/fog/clarity, bounded persistent wake mesh, eight normalized sinusoidal swells, four-point boat buoyancy and incremental ocean patch sampling.

## Fresh recovered-build evidence

Workspace maintenance removed the earlier uncommitted build. Its observations are not reused as verification of recovered code. Main recovery checkpoint: `f83bde0c878a5950af34df491c4b4010061c20a1`.

The recovered source compiled and passed core/cache/module-boundary checks. The core hydrology check performs 500,000 randomized transfers, preserving 16,244 units and cell capacity; native-level round trips, head limits, stamina recovery and wake bounds/lifetime are checked.

A fresh packaged dedicated server, seed 20261018 and `deepprimal:waterways_v6`, passed the complete standard case. The record includes native generated chunk support, a 12-node downhill course, supported headwater lake, coast, standard trench bed -88/depth 183, seven partial current levels, slower/faster item/boat motion, source policy, ridden horse behavior, farmland collision and snow limits. Native new tests passed 50 stable channel ticks, conserved basin water, a sealed U-bend, aborted/committed 125 mB machine transfers, soil saturation/runoff and snow beyond 64 layers. See `validation/0.0.1-alpha.16/native-standard.txt`.

OpenGL/Vulkan client and extra-depth validation are in progress. The installer has not been published while those checks are pending.

## Remaining scope

This update does not complete all referenced mods. Global drainage across regions, erosion and sediment, water tables/depletion/wells, lava conservation, machinery blocks, breaking surf/audio, lunar tides and ocean circulation, full weather/ship/shader/LOD integrations and edited-world voxel LOD remain unfinished. Desert freezing and TFC seasons/melting are absent. The procedural horizon remains a macro-terrain preview, not Distant Horizons voxel/building parity.

No hardware FPS claim follows from software-renderer tests. The user's Windows/AMD hardware and broad modpack combinations still require testing.
