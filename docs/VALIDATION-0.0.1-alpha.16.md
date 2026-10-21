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

A packaged native Vulkan client passed backend selection, settings/tint/foliage checks, the ocean patch, persistent wake buffer, a 12-tile horizon and 1,639,680 bytes of identical native depth before/after the isolated horizon pass. GPU regression checks passed nonzero storage-buffer offsets beyond 64 KiB, buffer queries, depth readback, texture copying and both packed stencil formats. The complete run exited successfully. See `validation/0.0.1-alpha.16/native-vulkan.txt`.

The recovered OpenGL packaged client also completed successfully. It selected OpenGL, passed settings/tint/foliage checks, uploaded a 29,712-byte ocean surface, reused the 12,288-byte wake buffer and rendered a 12-tile horizon. See `validation/0.0.1-alpha.16/native-opengl.txt`.

The resumed extra-depth packaged server passed the same native suite at floor -384, including a trench bed at -344 and measured water depth 439. Both standard and extra-depth worlds then reopened successfully: stored current direction/flow and soil saturation survived, and the measured-bucket item codec retained three units. The preliminary-surface profile samples were identical before and after reopening in both worlds. See `native-deep.txt`, `native-standard-reopen.txt` and `native-deep-reopen.txt` in the validation directory. The bucket check is a codec round trip, not a player-inventory persistence test.

A final `build --offline` passed the randomized core, shader/cache and module-boundary checks. The distributed JAR SHA-256 is `b3b2db4b4f9db523edb9c3225f175a6aec8e4a40357fb4e1f424aa28101d4945`; the packaging audit found four expected nested modules, no duplicate project classes and no development fixtures. This remains an alpha worldgen prototype.

## Remaining scope

This update does not complete all referenced mods. Global drainage across regions, erosion and sediment, water tables/depletion/wells, lava conservation, machinery blocks, breaking surf/audio, lunar tides and ocean circulation, full weather/ship/shader/LOD integrations and edited-world voxel LOD remain unfinished. Desert freezing and TFC seasons/melting are absent. The procedural horizon remains a macro-terrain preview, not Distant Horizons voxel/building parity.

The horizon now requests coarse coverage before refining tiles. Teleports replace obsolete in-flight request sessions on both client and server, preserving other players' shared jobs and the existing one-request/budget limits. This fixes avoidable waiting for a tile outside the current view; it is not a measured hardware FPS gain.

No hardware FPS claim follows from software-renderer tests. The user's Windows/AMD hardware and broad modpack combinations still require testing.

Visual inspection still shows coarse horizon/near-terrain transitions and isolated floating foliage in the river scene. Ocean patch boundaries also remain visible. The passing support checks cover sampled river chunks; they do not certify every generated location or finish the requested realism/polish work.

## Build host

Native GPU runs use Mesa llvmpipe/lavapipe in an authenticated private X server, not the user's Windows/AMD system. Validation can still report upstream unused vertex-output performance warnings; those are distinct from Vulkan validation errors. The host omits process executable metadata, so the private NeoForm build tool uses a java.home fallback; that build-tool adaptation is not shipped in the mod.
