# Deep Primal

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.11 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is the default normal world generator. Alpha 11 adds lowland river valleys, narrower tributary streams, wet gravel beds and layered coastal beaches for new worlds. Existing saves retain their saved terrain profile; this update does not regenerate old chunks. Extra underground depth is a startup config setting, rather than a second selectable world type. Alpha 4 replaced sheet-shaped mountain carving with buried tunnels and deeper chambers, and added an independent vertical render radius. Alpha 5 fixed native chunk-task queue bounds at extended render distances. Alpha 6 adds an authored macro-terrain horizon up to 256 chunks, automatic renderer handoff, and a localhost performance dashboard. The prototype retains vanilla placeholder materials, geological strata, native aquifers, XP suppression, bounded diagnostics, expanded horizontal rendering, and the alpha 3 client/server optimizations. OpenGL is the default; alpha 9 includes an optional experimental Vulkan backend. The full survival conversion remains in design. Use disposable test worlds. Alpha 6 can reopen alpha 4/5 worlds without migration; generated voxel terrain is unchanged.

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

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.11.jar`.

Create a new world normally; its default world type is **Deep Primal**. Prefer Creative with commands enabled for inspection. Set `extraUndergroundDepth = true` in `config/deepprimal-worldgen.toml` and restart before creating a new world to extend Y=-128..639 to -384..639. Saved worlds retain their height and generation profile. `riversAndCoasts = true` is the new-world default; set it false before creation to use alpha 10 terrain. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests, visual checks, and remaining limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. TFC and Terralith provide world-design inspiration without bundled source or assets. The optional Vulkan adapter contains adapted VulkanMod code under LGPL-3.0-only, with source, attribution and license notices in its separate module.

Open **Video Settings → Deep Primal distances…** for typed values and sliders. Horizontal render: 2–96 chunks; simulation: 2–32; vertical render: 0–96 sections (0 = full height, each section = 16 blocks above/below the camera). Vertical limits reduce render-section allocations and mesh work; chunk loading/networking and server simulation remain full-height columns. Servers control delivered terrain and simulation. Reopen a local world when raising horizontal render range above its allocated session capacity.

The built-in horizon defaults to 192 chunks and supports 0–256. It draws coarse generated landscape, with no extra full chunk tickets, trees, entities, or player builds. Open **Mods → Deep Primal → Config → Local performance dashboard**, or the same button in Deep Primal distance settings. The read-only dashboard normally uses `http://127.0.0.1:8787/`; additional game instances choose a free nearby port. The standard JAR includes an experimental Vulkan backend; switch it on the Graphics API performance page and restart. Native near-block meshing is retained.

Alpha 7 closes horizon tile edges to the saved-world floor, adds a near-terrain closure and stitched 16/32/64-block sample grids, and selects prebuilt detail levels by projected sample size and workload pressure. A single bounded worker builds immutable mesh snapshots; GPU uploads stay on the rendering thread. Client requests and server sampling back off independently under frame, tick and heap pressure. The server coalesces matching in-flight requests and rotates sampling fairly. Payload-byte and entry ceilings bound retention.

Alpha 8 introduced indexed horizon meshes, cached view calculations, reusable transforms, shared indexed submission and bounded staged uploads. Finer server samples can serve coarser requests without evaluating terrain again. Fine tiles with all cached draw levels use 59,412 GPU payload bytes instead of 172,320 (65.52% less). Open **Deep Primal distances → Performance…** for terrain/upload budgets, optional entity/block-entity/particle visual limits, cloud/shadow/particle quality, background FPS/work controls and staged Balanced/Speed/Quality profiles. Default scene limits retain native behavior; Speed deliberately reduces visual detail. The background FPS hook yields to Dynamic FPS when installed. The dashboard adds CPU mesh/upload/submission measurements and an asynchronous horizon-pass GPU timer. A replacement near-block renderer, edited-world LODs, GPU-driven occlusion and broad hardware performance certification remain unimplemented.

Alpha 9 bounds effective render and simulation distances using heap size and full world height, and bounds concurrent chunk serialization/writes, including bulk pause/shutdown saves. Requested distances stay saved. This addresses the uploaded 8 GiB/96-chunk memory failure; the distance policy is a conservative estimate, not a total-memory guarantee. Native nearby synchronous section builds have configurable count/time budgets, with excess ordinary work using native workers and player edits staying immediate. Exact horizon transition dependencies avoid rebuilding that GPU mesh when unrelated far tiles arrive.

The standard build includes Vulkan bindings but keeps OpenGL active until selected. `./gradlew build -PwithoutVulkan` produces a smaller JAR with performance and graphics settings but no Vulkan backend. Vulkan validation uses a Linux software driver; no hardware FPS improvement is promised. See the alpha 9 report for the tested scope.

Alpha 10 separates the core, common performance engine, graphics settings and Vulkan backend into distinct nested modules. It adds bounded persistent shader/driver caches, exact descriptor reuse and redundant-bind skipping, portable signed-byte storage buffers, capability checks, Vulkan debug labels and native lifetime/synchronization fixes. See the capability audit for implemented paths and explicit remaining features. This does not replace the native near-block mesher or establish hardware FPS gains.

Alpha 11 shares continuous river/stream/coast shaping between native terrain, biome depth and horizon sampling. One stateless core function and a cached platform adapter own the geometry; a versioned surface-water adapter fills the carved surface band while retaining native deeper aquifers. These are static sea-level lowland channels, not simulated watersheds or upland flowing streams. See [validation and limitations](docs/VALIDATION-0.0.1-alpha.11.md).
