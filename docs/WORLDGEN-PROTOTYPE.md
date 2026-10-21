# Deep Primal engine alpha 17 — play and build

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Install one matching Deep Primal JAR. The default small JAR uses OpenGL; the `vulkan-windows` JAR is an alternative containing the optional Windows adapter. Do not install both or extracted duplicate modules. Select a bundled backend under Graphics API and restart; the screen reports saved and actual API separately.

## New worlds and compatibility

Create a normal world for our `deepprimal:continental_v1` generator and `deepprimal:regional_v1` biome source. They own Overworld terrain/biome decisions without the native noise generator. Existing Minecraft block/biome assets remain temporary palettes. Nether/End remain native. Native carvers/decorators and villages are not placed by the custom Overworld generator.

`config/deepprimal-worldgen.toml` defaults to `builtInTerrainEngine = true`. `extraUndergroundDepth = true`, followed by restart **before creation**, chooses -384..639 (1024 blocks) rather than -128..639 (768 blocks). Sea level is 96. Saved dimensions keep their serialized generator and height; changing these creation settings does not migrate old worlds. Legacy river switches select retained older profiles only when the built-in engine is explicitly disabled. Never delete a save to enable this update; create a separate inspection world.

Use Creative with commands for development. The engine independently plans continental relief, curved regional drainage, material fields, cave tunnels and vegetation. Terrain quality, unified lakes/outlets, erosion/sediment simulation and complete natural catalogs remain unfinished. Catalogs are planning-only.

## Custom graphics screen

Open **Video Settings** (opens Deep Primal Graphics directly), or **Mods → Deep Primal → Config**. Changes remain staged through tab switches and resize until **Apply**. **Revert** reloads saved values. **Done/Escape** does not commit unapplied edits. Invalid typed input disables Apply. Opening a new screen restores saved requests, including values currently constrained by a server, heap or session cap.

| Tab | Controls |
| --- | --- |
| General | Native render 2–125 chunks, simulation 2–32, vertical radius 0–96 sections (0 = full height), FOV, brightness, FPS cap/VSync and optional-work target |
| Quality | Clouds, particles, shadows and optional entity/block-entity/particle visual limits |
| Performance | Shared adaptive work/upload limits, nearby synchronous mesh limits, background FPS/work and local dashboard |
| LOD | Enabled, requested radius 16–512 chunks, projected cell size 1–12 pixels, GPU payload budget 4–256 MiB, disk budget 16–4096 MiB, persistence, capture interval and per-frame upload count |
| Water | Curved engine-water surface, tint/fog, retained swell/cascade/wake/snow controls; these controls do not implement breaking surf |
| Graphics API | Saved backend, currently active backend and restart status; optional adapter availability |

The requested LOD radius is a retention/view limit, not a promise of loaded terrain. The screen also reports current visible LOD span. Only normally delivered/explored chunks are captured; no extra full chunk tickets or unseen-terrain generation is requested. Detailed host chunks hand off when compiled. Distant meshes approximate materials/shapes and lighting; full texture/model/transparency parity remains unfinished. The legacy procedural horizon is off and is not a fallback when LOD is disabled.

Native requested distances remain subject to server delivery, memory guards and session tracking allocation. Vertical distance changes render allocations, not full-height chunk storage/simulation. Increasing native render request above session capacity may require reopening a local world. External renderer ownership disables our internal LOD/water rendering controls while retaining general settings. Broad replacement-renderer compatibility still needs testing.

The read-only local dashboard normally uses `http://127.0.0.1:8787/` and is linked from Performance. It reports LOD captures/revisions, uploads, visible/requested span and payload/cache/work counters. It is not a substitute for hardware profiling.

## Liquids

New engine water is a distinct 16-unit fluid with conservative gravity/pooling and stored current direction/speed. Its own full bucket removes/places 16 units; partial cells do not yield a free full bucket. Ordinary water introduced into these worlds is converted before native water spreading, including when the old optional finite-water config is enabled. Lava and waterlogged blocks are not replaced.

The old eight-unit finite-water solver, measured bucket, rain/soil/machine-port and survival features remain available in source for legacy worlds. Do not assume each is integrated with the new fluid: pressure/displacement, recharge, river/ocean replenishment, erosion and shoreline surf require explicit integration and validation.

## Diagnostics

| Command | Behavior |
| --- | --- |
| `/deepprimal info` | Dimension height, sea level, and seed |
| `/deepprimal perf` | Recent tick median/p95/max, heap, loaded chunks, pending tasks, sampler cache and benchmark state |
| `/deepprimal sample <blockX> <blockZ>` | Surface-height/climate sampler query without loading the target chunk |
| `/deepprimal bench <chunkX> <chunkZ> <radius>` | Request FULL chunks sequentially in a square; default radius 0–4, at most 81 requests; configurable maximum up to 8 |
| `/deepprimal cancel` | Stop scheduling further requests; a submitted engine request may still finish |

Example: `/deepprimal bench 200 200 1` requests nine chunks in a distant area. Coordinates here are **chunk coordinates**, not block coordinates. Use fresh coordinates for cold-generation runs. Repeat the same region to compare cached/loading behavior, noting that already-generated data is not fresh generation.

Results are written under the game/server directory in `deepprimal-benchmarks/` as CSV plus a text summary. Median/p95/p99/max report request-to-ready wall time, including tick polling, disk, queueing, and generation of dependent neighbors. They are **not** isolated CPU timings. Reports include end heap, heap peak observed at tick boundaries, Pre-to-Post tick timings, and GC deltas. Observed heap is not an exact allocation peak. Tick statistics cover the last 4,096 benchmark ticks; `/perf` covers the last 256 server ticks. Concurrency=1; this is an initial probe, not the full multiplayer/per-stage performance suite. No numerical performance gate has passed merely because the command exists.

Each in-flight request holds its own temporary, non-persistent loading ticket. The ticket is released on completion, cancellation, timeout, failure, or shutdown. A timed-out or cancelled engine request may drain before another run is accepted. Reports are written on a bounded worker queue, away from the server thread. The request uses the engine's off-thread dispatch path to avoid its main-thread managed wait. It does not keep the entire benchmark region loaded or force-load unrelated dimensions.

## Build and validation

```sh
./gradlew build
./gradlew build -PvulkanPlatform=windows
./gradlew build -PvulkanPlatform=linux
```

Default output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.17.jar`. Platform bundles use distinct `-vulkan-<platform>` filenames. No terrain/LOD runtime library was added. Core checks, source module boundaries and optional Vulkan cache checks run in the default build. `-PwithoutVulkan` excludes the optional project entirely. `-PbundleVulkan` explicitly requests the larger universal package.

`./gradlew runClient` / `runServer` launch development instances. Accept the Minecraft EULA before running a server. Development-only `visualValidation` fixtures are excluded from installers. Alpha 17 fixtures exercise actual engine worlds, fluids, snapshot persistence, edits after native unload, disk reload, UI persistence/resize and both common GPU backends. See [recorded commands, results and limits](VALIDATION-0.0.1-alpha.17.md).

`tools/generate_worldgen.py` reproduces **legacy** authored noise resources, not the new original Java terrain engine. Those resources remain frozen for saved-world compatibility. Change the new generator only with versioned profiles/migration policy after release; changing explored saved terrain in place is not supported.

## References and provenance

TFC, Terralith and Tectonic guide original terrain work; full parity/source audits and visual acceptance are not complete. Gaea/World Machine methods are references without proprietary engine imports. Distant Horizons, Nanite and Cesium guide LOD architecture without bundled runtimes; meshoptimizer remains a development probe. No comparative FPS claim is made. [Architecture/reference decisions](LOD-AND-DRAINAGE-ARCHITECTURE.md) remain governing requirements.

Original project code is All Rights Reserved pending the owner's final licensing choice. The optional Vulkan adapter is separately licensed LGPL-3.0-only with adapted VulkanMod source/notices retained. Minecraft/NeoForge are dependencies, not bundled game distributions.
