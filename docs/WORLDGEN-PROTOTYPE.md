# Worldgen prototype 0.0.1-alpha.10

Target: **Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25**. This is a terrain development build, not the full survival conversion. Use new disposable worlds. Alpha 5 fixes extended-distance queue bounds and can reopen alpha 4 saves. Alpha 4 changed cave geometry; existing chunks retain their old carving, and new chunks can meet them at visible seams. Never install it into a valuable existing world as a world-height migration tool.

## Play

1. Install the matching NeoForge version and place the DeepPrimal JAR in `mods` on client and server.
2. Create a new world normally. **Deep Primal** is the default normal world type; no custom preset selection is needed.
3. Use Creative with commands enabled. `/gamemode spectator` is useful for inspecting geology and caves.
4. Run `/deepprimal info`, then explore. Vanilla blocks/items, biome vegetation, and feature placement are temporary test materials.

The default spans Y=-128..639 (768 blocks). In `config/deepprimal-worldgen.toml`, set `extraUndergroundDepth = true` and restart Minecraft/the server **before creating a new world** to use -384..639 (1024 blocks). Sea level remains 96 and the mountain ceiling is unchanged. Both bounded profiles stay registered for saved-world compatibility, but are absent from the world-type lists. Saved dimension types and noise settings determine an existing world's height; changing this creation-time config does not migrate it.

A required built-in data pack supplies the configured `minecraft:normal` preset on client and server. An ordinary dedicated server using its default `level-type=minecraft:normal` therefore creates Deep Primal terrain. Flat, amplified, and other explicitly selected world types retain their own generation. This is not unlimited height or cubic chunk streaming.

## Implemented scope

- Default normal world generation with two config-selected saved-world profiles, dimension types, and noise settings.
- Seeded continental terrain, amplified mountain ridges, and valley masks with vanilla multi-noise biomes.
- Vanilla underground aquifers, intersecting noise-band tunnels, continuous surface-rock cover, and deeper chambers that taper out below the alpine terrain. Native biome carvers retain ordinary lower-elevation entrances. The previous single-band sheet carving and hard shallow-depth switch are removed. These remain geological placeholders, without erosion simulation, structural support simulation, renewable groundwater, or connected river drainage.
- Visible placeholder strata: andesite, granite, diorite, deepslate; bedrock floor; simple soil/sand/snow surface rules.
- A data-driven terrain-height field stored in the native bounded 5×5 chunk grid, direct preliminary-surface calculation without a vertical search, and interpolated density evaluation through the native engine.
- Explicit overworld sky/fog/light colors, high clouds, small-scale terrain detail, slope-based exposed rock, and rocky subsoil above Y=224 to reduce dirt stripes on high cliffs.
- Operator-only diagnostic sampling and a bounded sequential chunk benchmark.
- Block and mob XP drops suppressed before orb creation; vanilla XP gain/level events and XP-orb spawning disabled; loaded/directly-set player vanilla XP is cleared. The MMO XP replacement is not built yet.

Native survival, custom catalog, Earth data, complete saved-world/voxel LOD, finite ore economy, renewable wells, navigation/hearth, vehicles, fishing, animal yields, and professions are not implemented. Vanilla ore/features remain placeholders and are not the final finite ore generation. Flora/biome transitions and terrain balance need visual playtesting.

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

## Performance configuration

`config/deepprimal-performance.toml` controls operator diagnostics:

| Setting | Default | Purpose |
| --- | --- | --- |
| `maxBenchmarkRadius` | 4 | Bounds a run to 81 requests; supported range 0–8 |
| `requestTimeoutSeconds` | 120 | Releases the diagnostic ticket on a stuck request; range 5–600 |
| `pauseAboveTickMilliseconds` | 50 | Delays the next benchmark request after a slow tick; 0 disables pacing |
| `sampleCacheEntries` | 1024 | Fixed-capacity LRU of diagnostic results; 0 disables caching |
| `logSamplerQueries` | false | Optional per-query log output |

`optimizeChunkSelection` (default true) uses a primitive nearest-first queue. `maxChunkSendsPerTick` (default 16, range 1–64) bounds each player's send batch, including integrated-server connections, while respecting native client feedback. Turning selection off restores native batching.

`persistentMobIdleOptimization` (default true) backports the 26.3 behavior: distant persistent mobs accumulate idle time so native random-walk/swim goals can stop. Nearby mobs reset normally, persistence/despawn rules remain native, and other AI still runs. Set false to retain the old persistent-mob idle timer behavior.

Client config `deepprimal-client-performance.toml` contains `optimizeViewRelocation` (default true), also available as **Fast camera updates** in the distance screen. It skips unchanged render columns. With full-height rendering, turning it off uses native relocation. With a bounded vertical window, both settings retain the required vertical slot mapping; the toggle controls scanning optimizations.

These options do not change terrain geometry. Chunk delivery pacing changes arrival timing and idle optimization changes distant random strolling as described above. Cached samples contain values and seed/dimension/coordinate keys, with no retained world or chunk references. Cache contents clear on datapack sync and server shutdown. Vanilla XP suppression has no disable setting.

For a controlled benchmark, disable the optional slow-tick pause and record that setting. For ordinary playtesting, leave the default pacing enabled. The mod uses native generation workers and does not create an extra terrain-generation pool.

Your screenshots used render and simulation distance 32. Start tall-world tests around render distance 12–16 and simulation distance 6–8, then increase them while watching `/deepprimal perf`. These are starting points for testing, not measured guarantees. The mod does not override player/server settings.

## Distance controls

Open **Video Settings → Deep Primal distances…**. Horizontal render, simulation, and vertical render each have a slider and a numeric field. Changes apply together with **Apply**; Cancel/Escape leaves staged values unused. Invalid values disable Apply. Horizontal/simulation values persist in `options.txt`; vertical radius persists in `deepprimal-client-performance.toml`. Native Video Settings sliders also support the expanded horizontal/simulation ranges.

- Render distance: **2–96 chunks**, an experimental finite ceiling compatible with the native byte protocol and tracking storage.
- Simulation distance: **2–32 chunks**. The native simulation-level graph caps the useful range; higher values would need a different simulation architecture.
- Vertical render radius: **0–96 sections**, independently above/below the camera in 16-block steps. **0 = full world height**. A nonzero value allocates at most `min(worldSections, 2 × radius + 1)` Y slots per render column. Slots wrap when the camera climbs or descends. Native occlusion propagation, section lookup, and octree bounds use the vertical range independently of horizontal distance. Apply rebuilds the render grid immediately. Chunks, lighting data, network packets, and server simulation still use full-height columns. Terrain beyond a selected vertical limit is deliberately clipped.

The authored native fog-include implementation scales the Y component of distance fog independently, with an extended fog uniform buffer. Environmental fog retains its native spherical distance, so water, weather, blindness, and darkness can still limit visibility. The camera far plane includes the vertical range. Third-party shader packs or replacement renderers that replace the fog include/uniform layout require separate compatibility validation.
- Multiplayer: the server controls delivered terrain and simulation. A client's value cannot override it. The screen shows the server's advertised render cap.
- Single-player: tracking capacity is allocated when opening the world, at least 32 and up to the chosen render distance. Choosing a range above that session capacity requires reopening the world; the screen tells you when. Effective local rendering stays within the allocated tracking capacity meanwhile.
- Alpha 5 extends each native chunk-task queue to match its session tracking capacity, including unload priorities. Ordinary 32-chunk sessions keep the native 46 buckets. The fix also preserves the queue size through shutdown; native chunk generation/status levels remain unchanged.
- Dedicated server: set `view-distance` before startup (2–96); `simulation-distance` remains 2–32. Actual gameplay requests above the hard limits are clamped. Default distances retain the native 32-chunk tracking-graph capacity rather than always allocating a 96-chunk graph.

Larger render settings multiply chunk and vertical-section costs. The upper range is not a hardware performance promise. There is no literal unlimited mode. A smaller vertical range does not establish that the reported alpha 2 heap exhaustion at 32 chunks / 4 GiB is resolved; full chunk-column demand remains.

The screen reports the **actual graphics API**. Alpha 9 includes an optional experimental Vulkan backend; select it on the Graphics API performance page, Apply, and restart. See [the implementation and compatibility notes](VULKAN-BACKPORT.md).

## Built-in renderer and local diagnostics

Alpha 6 introduced the native horizon and automatic renderer ownership described in [this guide](RENDERER-AND-DIAGNOSTICS.md). The horizon defaults to 192 chunks and accepts 0–256. It does not raise the full-detail or simulation limits. The local dashboard is enabled by default and has a button in Deep Primal’s distance/config screen.

Alpha 8 extends **Performance…** with terrain upload budgets, visual range/admission limits, background FPS/work controls and staged profiles. See [the renderer guide](RENDERER-AND-DIAGNOSTICS.md#performance-menu) and [validation](VALIDATION-0.0.1-alpha.8.md). Terrain resources are unchanged.

## Build and reproduce

Use Java 25. Run `./gradlew build` (Windows: `gradlew.bat build`). Output is under `build/libs/`. `./gradlew runClient` starts a development client; `./gradlew runServer` starts a development server (the Minecraft EULA must be accepted before use).

`./gradlew coreChecks` runs the standalone differential height, cache-bound, and tick-ring checks. `./gradlew build` includes these checks.

`python3 tools/generate_worldgen.py` reproduces the authored JSON resources without external dependencies. Edit terrain parameters/profile heights in that script, regenerate, rebuild, and create a fresh world. Do not change the geometry of an existing world without a migration policy.

Server setup for testing: leave `level-type=minecraft:normal` and choose extra depth in `config/deepprimal-worldgen.toml` before starting with a new world. The historical `deepprimal:prototype` and `deepprimal:prototype_deep` IDs remain accessible to explicit diagnostic tooling and old saves. Keep a fixed seed and record client/server settings, hardware, software, depth config, coordinates, and cache state for comparisons.

For client visual checks, the opt-in `visualValidation` source set adds an automatic screenshot 45 seconds after joining a server and then exits. Run `./gradlew runClient -PvisualValidation -PcaptureServer=127.0.0.1:25565` against a local test server. Screenshots go to `build/visual-validation/screenshots/terrain.png`. The capture hook is excluded from the distributed JAR.

## Licensing and provenance

The original project code is All Rights Reserved pending the owner's final licensing choice. The separately replaceable Vulkan adapter is LGPL-3.0-only and contains adapted VulkanMod source; its origin and license notices are included. Bundled LWJGL bindings/natives retain their notices. Gradle wrapper files retain Gradle's notices. NeoForge and Minecraft are dependencies, not bundled game distributions. The authored density/surface definitions reference vanilla registries. No TFC or Terralith source/assets are included.

## Alpha 9 memory and graphics changes

Keep the memory distance guard enabled for large requested distances. Its effective limit accounts for full-height columns even when vertical rendering is restricted. The horizon distance is separate. `config/deepprimal-performance.toml` exposes `memoryDistanceGuard`, `boundChunkSaves` and `maxConcurrentChunkWrites` (default 16 per dimension). Bulk saves can take longer while write backpressure keeps snapshots bounded.

The standard package now includes the experimental Vulkan adapter. Select it under **Performance → Graphics API**, Apply, and restart. OpenGL remains the default. Use one graphics replacement at a time; Deep Primal yields when an external renderer is installed. See [Vulkan details](VULKAN-BACKPORT.md) and [alpha 9 validation](VALIDATION-0.0.1-alpha.9.md).

Alpha 10 keeps Vulkan, graphics settings and common performance in separate nested modules. See [module builds](MODULES.md), [Vulkan capabilities](VULKAN-CAPABILITIES.md), and [current validation](VALIDATION-0.0.1-alpha.10.md). The default remains OpenGL.
