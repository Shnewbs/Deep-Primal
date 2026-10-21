# Deep Primal

**Built-in engine direction:** Deep Primal remains the project. Its target is an original Overworld terrain and liquid engine, informed by TFC, Terralith and Tectonic, with connected drainage and measured generation/rendering budgets. Minecraft/NeoForge remain the host platform; their standard Overworld terrain/biome decisions are not the target generator.

**Current implementation:** alpha.16 below still uses Minecraft's noise-generator/biome pipeline with authored terrain definitions and custom river/water hooks. The separate [Primal Engine alpha.1 experiment](standalone-engine/README.md) proves some independent generator/fluid/storage contracts, but is not integrated, is not an accepted visual replacement and must not be presented as the next completed Deep Primal engine. Preserve the existing terrain, performance, settings and Vulkan work while developing and validating the replacement. See [engine requirements and reference status](docs/LOD-AND-DRAINAGE-ARCHITECTURE.md).

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.16 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is the default normal world generator. Alpha 16 adds bank-supported v6 river steps and stable generated channel water, an optional finite-water solver, partial measured buckets, transactional water ports, rain/soil/runoff/evaporation, stamina/treading controls, equipment-aware pressure, persistent mesh wakes, resource-pack water palettes and optional unlimited snow with local snow fog. Finite water and the new survival penalties are disabled by default. Use a new world for v6 terrain; existing saves retain their generator and explored-water layout.

The procedural horizon now permits 512 chunks with denser smooth-lit meshes. Full-detail requests stop at Minecraft's native signed-byte tracker ceiling of 125 chunks; heap and server delivery guards remain active. This is macro terrain, not detailed voxel/tree/building LOD. OpenGL remains the default; the separate experimental Vulkan backend includes the cloud storage-range crash fix. Graphics controls, the common performance engine and platform-neutral core remain separate modules. The complete survival conversion remains in design; use disposable test worlds.

- [Alpha 16 water, river support and renderer validation](docs/VALIDATION-0.0.1-alpha.16.md)
- [Alpha 15 horizon, partial river surface and headwater validation](docs/VALIDATION-0.0.1-alpha.15.md)
- [Alpha 14 water, weather and renderer validation](docs/VALIDATION-0.0.1-alpha.14.md)
- [Complete water/snow reference audit](docs/WATER-REFERENCE-AUDIT-2026-10-20.md)
- [Water/weather feature scope and requested-mod comparison](docs/WATER-AND-WEATHER-2026-10-20.md)
- [Alpha 13 contained stepped rivers and persistent currents](docs/VALIDATION-0.0.1-alpha.13.md)
- [TFC and requested river-mod flow comparison](docs/RIVER-FLOW-COMPARISON-2026-10-19.md)
- [Alpha 12 mountain streams and gorges](docs/VALIDATION-0.0.1-alpha.12.md)
- [River mod comparison](docs/RIVER-MOD-COMPARISON-2026-10-19.md)
- [Alpha 11 rivers, streams and beaches](docs/VALIDATION-0.0.1-alpha.11.md)
- [Alpha 10 Vulkan and module validation](docs/VALIDATION-0.0.1-alpha.10.md)
- [Module boundaries and builds](docs/MODULES.md)
- [Vulkan capabilities and remaining work](docs/VULKAN-CAPABILITIES.md)
- [Install, play, and build](docs/WORLDGEN-PROTOTYPE.md)
- [Alpha 9 crash, native rebuild and Vulkan validation](docs/VALIDATION-0.0.1-alpha.9.md)
- [Vulkan adapter and license](docs/VULKAN-BACKPORT.md)
- [Alpha 8 performance validation](docs/VALIDATION-0.0.1-alpha.8.md)
- [Mojang, CurseForge and Hytale audit](docs/PERFORMANCE-AUDIT-2026-10-18.md)
- [Alpha 7 adaptive rendering validation](docs/VALIDATION-0.0.1-alpha.7.md)
- [Alpha 6 renderer and diagnostics validation](docs/VALIDATION-0.0.1-alpha.6.md)
- [Renderer and dashboard guide](docs/RENDERER-AND-DIAGNOSTICS.md)
- [Alpha 5 crash-fix validation](docs/VALIDATION-0.0.1-alpha.5.md)
- [Alpha 4 validation](docs/VALIDATION-0.0.1-alpha.4.md)
- [Alpha 3 distance/performance validation](docs/VALIDATION-0.0.1-alpha.3.md)
- [Original baseline](docs/VALIDATION-0.0.1.md)
- [Roadmap](roadmap.md)
- [Owner decisions](docs/design-decisions-2026-10-18.md)
- [Detailed design reference](docs/design-reference.md)

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.16.jar`.

Create a new world normally; its default world type is **Deep Primal**. Prefer Creative with commands enabled for inspection. Set `extraUndergroundDepth = true` in `config/deepprimal-worldgen.toml` and restart before creating a new world to extend Y=-128..639 to -384..639. Saved worlds retain their height and generation profile. `riversAndCoasts = true` is the new-world default; set it false before creation to use alpha 10 terrain. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests, visual checks, and remaining limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. TFC and Terralith provide world-design inspiration without bundled source or assets. The optional Vulkan adapter contains adapted VulkanMod code under LGPL-3.0-only, with source, attribution and license notices in its separate module.

Open **Video Settings → Deep Primal distances…** for typed values and sliders. Full-detail horizontal request: 2–125 chunks; simulation: 2–32; vertical render: 0–96 sections (0 = full height, each section = 16 blocks above/below the camera). Vertical limits reduce render-section allocations and mesh work; chunk loading/networking and server simulation remain full-height columns. Servers control delivered terrain and simulation. Reopen a local world when raising horizontal render range above its allocated session capacity.

The built-in horizon defaults to 192 chunks and supports 0–512. Alpha 16 fills coarse coverage before requesting refinements and replaces stale requests after teleports, within the existing memory and work limits. It draws coarse generated landscape, with no extra full chunk tickets, trees, entities, or player builds. Open **Mods → Deep Primal → Config → Local performance dashboard**, or the same button in Deep Primal distance settings. The read-only dashboard normally uses `http://127.0.0.1:8787/`; additional game instances choose a free nearby port. The standard JAR includes an experimental Vulkan backend; switch it on the Graphics API performance page and restart. Native near-block meshing is retained.

Alpha 7 closes horizon tile edges to the saved-world floor, adds a near-terrain closure and stitched 16/32/64-block sample grids, and selects prebuilt detail levels by projected sample size and workload pressure. A single bounded worker builds immutable mesh snapshots; GPU uploads stay on the rendering thread. Client requests and server sampling back off independently under frame, tick and heap pressure. The server coalesces matching in-flight requests and rotates sampling fairly. Payload-byte and entry ceilings bound retention.

Alpha 8 introduced indexed horizon meshes, cached view calculations, reusable transforms, shared indexed submission and bounded staged uploads. Finer server samples can serve coarser requests without evaluating terrain again. Fine tiles with all cached draw levels use 59,412 GPU payload bytes instead of 172,320 (65.52% less). Open **Deep Primal distances → Performance…** for terrain/upload budgets, optional entity/block-entity/particle visual limits, cloud/shadow/particle quality, background FPS/work controls and staged Balanced/Speed/Quality profiles. Default scene limits retain native behavior; Speed deliberately reduces visual detail. The background FPS hook yields to Dynamic FPS when installed. The dashboard adds CPU mesh/upload/submission measurements and an asynchronous horizon-pass GPU timer. A replacement near-block renderer, edited-world LODs, GPU-driven occlusion and broad hardware performance certification remain unimplemented.

Alpha 9 bounds effective render and simulation distances using heap size and full world height, and bounds concurrent chunk serialization/writes, including bulk pause/shutdown saves. Requested distances stay saved. This addresses the uploaded 8 GiB/96-chunk memory failure; the distance policy is a conservative estimate, not a total-memory guarantee. Native nearby synchronous section builds have configurable count/time budgets, with excess ordinary work using native workers and player edits staying immediate. Exact horizon transition dependencies avoid rebuilding that GPU mesh when unrelated far tiles arrive.

The standard build includes Vulkan bindings but keeps OpenGL active until selected. `./gradlew build -PwithoutVulkan` produces a smaller JAR with performance and graphics settings but no Vulkan backend. Vulkan validation uses a Linux software driver; no hardware FPS improvement is promised. See the alpha 9 report for the tested scope.

Alpha 10 separates the core, common performance engine, graphics settings and Vulkan backend into distinct nested modules. It adds bounded persistent shader/driver caches, exact descriptor reuse and redundant-bind skipping, portable signed-byte storage buffers, capability checks, Vulkan debug labels and native lifetime/synchronization fixes. See the capability audit for implemented paths and explicit remaining features. This does not replace the native near-block mesher or establish hardware FPS gains.

Alpha 11 shares continuous river/stream/coast shaping between native terrain, biome depth and horizon sampling. One stateless core function and a cached platform adapter own the geometry; a versioned surface-water adapter fills the carved surface band while retaining native deeper aquifers. These are static sea-level lowland channels, not simulated watersheds or upland flowing streams. See [validation and limitations](docs/VALIDATION-0.0.1-alpha.11.md).

Alpha 12 routes elevated tributaries through mountain valleys and gorges toward existing water. Their water heights descend locally instead of being fixed to sea level. `mountainStreams = true` is the new-world default when `riversAndCoasts` is enabled; disabling mountain streams before creation selects the frozen alpha 11 profile. Saved worlds keep their generation profile. Routing is bounded and cached in the core/worldgen adapter, independently of Vulkan and graphics/performance modules. See [scope, tests and remaining limits](docs/VALIDATION-0.0.1-alpha.12.md).

Alpha 13 keeps stepped mountain rivers while checking intermediate bed/bank elevations and filling water to the native interpolated bed. Generated routed channels store persistent downstream directions, so items and boats drift even on flat stretches. `containedRivers = true` is the new-world default alongside `mountainStreams` and `riversAndCoasts`; disabling it before creation selects frozen alpha 12 terrain. Saved profiles and explored chunks are unchanged. World-synced `deepprimal-rivers.toml` controls currents and force independently of terrain creation and graphics. See [validation, settings and limits](docs/VALIDATION-0.0.1-alpha.13.md).

The [drainage and chunk-derived LOD design](docs/LOD-AND-DRAINAGE-ARCHITECTURE.md) records the meshoptimizer prototype and the work still needed for a true editable-world LOD viewer.


## Alpha 16 water controls

Gameplay settings live in the world's `serverconfig/deepprimal-rivers.toml`. Set `[hydrology] finiteVolume = true` to opt into water conservation. The solver uses native saved fluid ticks, eight units per block, a shared operation budget (default 2048 per dimension/tick) and bounded connected-pool searches (default 128 cells). It does not load chunks. Waterlogged blocks stay sealed; lava uses native behavior. Rain/runoff and evaporation are explicit input/output, not conserved transfers.

Craft one vanilla bucket into a **Measured Bucket**. Use it on water to collect partial amounts, or pour onto a block/water; sneak-use pours one unit. Native NeoForge fluid ports expose ordinary water, river water and air cells in 125 mB increments when finite mode is active. There are no bundled pumps, pipes or tanks.

Optional swimming stamina/treading, pressure gear reductions/effects, global winter in precipitating biomes and unlimited snow have separate controls. Desert climate replacement and TFC seasons/melting are not implemented.

Resource packs can provide `assets/<biome namespace>/deepprimal/water_palette/<biome path>.json`, for example `assets/minecraft/deepprimal/water_palette/jungle.json` containing `{"color":7823946,"fog_color":5260850,"clarity":0.7}`. Colors are RGB integers, clarity ranges from 0.1 to 8. Fluid transparency is unchanged. Biome/elevation tint transitions remain blended.

Persistent wakes use one 12 KiB GPU vertex buffer and expire after 40 ticks; ocean patch sampling is capped at 256 columns per tick. These bounded systems do not establish a hardware FPS improvement or full parity with the reference mods. See the feature audit for unfinished systems.
