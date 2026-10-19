# Built-in renderer and local diagnostics

Alpha 8 / Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.

## Renderer ownership

Deep Primal provides its rendering module and optimizations directly in the mod. No Sodium, LOD mod, or browser extension is required. It owns a coarse horizon mesh pass and the native vertical-grid, fog, distance, and camera optimizations. Nearby voxel meshing/draw preparation still uses Minecraft. OpenGL is the default backend; alpha 9 includes an optional experimental Vulkan adapter selected on the Graphics API performance page after a restart.

At startup, the module detects `sodium`, `embeddium`, `rubidium`, `vulkanmod`, `nvidium`, `iris`, `oculus`, and `distanthorizons`. If any is present, it silently yields the renderer module as a unit: overlapping client mixins are not applied, its required renderer resource pack is not registered, and its horizon is inactive. There is no popup, file deletion, uninstall, or changes to the other mod. Ownership is visible in settings and diagnostics. Removing the competing mod restores the built-in renderer on the next launch. Unrelated optimizers such as Lithium/FerriteCore do not disable this renderer. Worldgen, server optimizations, XP rules, and diagnostics remain active.

The known-ID policy is deliberately conservative. It is not a claim that every renderer/shader/mod combination has been tested. An unrecognized replacement can disable this renderer using `-Ddeepprimal.renderer.disabled=true` before launch. Both automatic and explicit handoff happen before overlapping mixins transform the client.

## Horizon and budgets

Open **Video Settings → Deep Primal distances…**, or **Mods → Deep Primal → Config**. Typed values and sliders apply together. The **Terrain horizon** setting accepts 0–256 chunks; default 192, 0 disables it. Full-detail rendering remains 2–96 chunks, simulation 2–32, and vertical radius 0–96 sections. The 256 value belongs to the authored coarse horizon and is not sent through the vanilla byte-sized full-detail distance setting.

The horizon samples Deep Primal's seeded terrain-height field on the server and sends compact tiles to the client. It never obtains FULL chunks, places tickets, or runs distant simulation. It does not share the world seed with the client. Optional versioned payloads allow old servers to decline the feature. Dimensions without the Deep Primal `ColumnSurface` height field disable the horizon instead of inventing a surface.

- 512×512-block tiles, with 16/32/64-block sampling. Projection size (distance, window height and FOV) and optional frame-pressure feedback choose detail. A finer cached tile also owns prebuilt coarser draw buffers, allowing draw detail to fall without resampling. Tile edges share the 64-block grid. Heights/colors remain approximate.
- Tile walls and bottoms close the generated shell to the world's saved minimum Y. A world-aligned, 16-block-snapped closure ring covers the inner transition. This reduces exposed sheets; it does not reproduce caves, overhangs, edited surfaces or exact native voxel boundaries.
- Server: 32 jobs maximum, one pending waiter per player and at most 32 waiters per shared job. Matching in-flight requests coalesce. Round-robin sample scheduling applies a global adaptive 0.125–1 ms budget and at most 64 samples per tick. A single density evaluation cannot be preempted. Jobs expire after 30 seconds and prune disconnected, dimension-changed and out-of-range players. Requests remain four ticks apart or slower.
- Finer cached grids and in-flight jobs can satisfy coarser requests by exact subsampling; no extra density evaluation is needed. Reuse is counted in telemetry.
- Shared server samples have an 8-MiB payload-byte limit and a 512-entry ceiling. Source fields remain on the server thread; no full chunks/tickets or unsafe worker reads are introduced. Caches clear on stop/full datapack sync.
- Client: one pending request, one CPU mesh job, a single low-priority worker with a one-entry queue, and one ready mesh snapshot. The worker sees primitive snapshots only. Uploads occur on the rendering thread. Session/dimension/detail/footprint checks discard irrelevant replies; obsolete work is cancelled on disconnect or horizon disable. Teleports evict old-footprint meshes. Finer cached meshes stay stable until eviction.
- Client retained vertex/index payload defaults to 32 MiB, configurable from 1–256 MiB, with a 400-tile ceiling. Shrinking the budget evicts the farthest tiles first. Requests that cannot fit wait rather than repeatedly replacing the same data. This ceiling covers cached vertex/index payload, not driver/whole-game VRAM. A separately bounded transition ring, one in-flight upload, CPU sample arrays and mesh snapshots have additional fixed geometric limits. A fine tile with all three draw levels occupies 59,412 bytes; a coarse tile occupies 4,956 bytes. This is 65.52% less fine-tile mesh payload than alpha 7, with identical expanded triangle order and colors.
- Adaptive work is enabled by default. Target FPS defaults to 60; the lower of this target and the currently effective native FPS cap (including background limits) is used. Sustained slow frames or >85% heap pressure reduce request frequency/detail; recovery is gradual with hysteresis. Server sampling separately backs off after slow ticks or heap pressure. Simulation distance never changes automatically. Background limits preserve headroom but cannot guarantee a frame/tick deadline.
- Existing camera-frustum, vertical, GPU back-face/depth tests and batched transform uploads remain. Counts and per-pass timings expose their work. Optional shared indexed submission uses the 26.1 API to share draw setup; the native OpenGL backend still issues individual draws. This is not GPU MultiDrawIndirect, a GPU-driven terrain-occlusion solver, or a complete near-block renderer.
- The optional horizon payload is now version 2. Update both peers for horizon rendering. A server without the matching channel simply provides ordinary nearby chunks.

## Performance menu

Open **Deep Primal distances → Performance…**. Four pages stage edits until Apply; Cancel/Escape discards them. Values persist in the client performance config; cloud/shadow/particle quality uses Minecraft options. All budgets concern Deep Primal horizon work; they do not cap native chunk compilation.

| Control | Default | Range / behavior |
| --- | --- | --- |
| Adaptive work / target FPS | on / 60 | 15–240 FPS; bounded feedback, respects lower effective cap |
| Mesh memory | 32 MiB | 1–256 MiB retained vertex/index payload |
| Detail uploads / frame | 2 | 1–8; a detail level is indivisible |
| Upload payload / frame | 256 KiB | 128–4096 KiB; checked before each level |
| Upload CPU budget | 1000 us | 100–4000 us; checked between levels, one level may exceed it |
| Batch draws | on | Shared indexed submission; fallback available |
| Entity / block-entity / particle range | 0 / 0 / 0 | 0 keeps native admission; positive values bound visual range in blocks |
| Particle admissions / tick | 0 | 0 keeps native behavior; 1–8192 caps new ordinary particles |
| Unfocused / minimized FPS | 20 / 5 | 0 preserves native cap; positive values only lower it |
| Pause background terrain work | on | Pauses optional horizon requests/build scheduling/uploads while unfocused |

Scene range checks use the nearest point on render bounds for entities/block entities; player, glowing/leashed entity and global block-renderer exceptions preserve important visibility. Special no-render, pickup and elder-guardian particles bypass ordinary admission caps. These controls do not stop simulation or entity AI. Counters report skipped visual work. Changing the cap does not retroactively remove already admitted particles.

Balanced uses default terrain-work budgets and moderate particle limits, Speed reduces effects/ranges and disables clouds/shadows, and Quality raises resource allowances. Each profile stages edits and leaves render/simulation distances unchanged. Scene/work controls yield with the renderer module when a recognized renderer replacement is installed. Background FPS independently yields to `dynamic_fps`; the settings show ownership.

The landscape is an approximate generated height field with simple height-based colors and an opaque water surface. It omits caves, overhangs, trees, structures, blocks/builds and entities; existing edited terrain is not represented. Detail changes, approximate colors and mismatch with edited/native surfaces can still produce seams. Nearby native meshes depth-test against it, and a near exclusion region limits cave leakage. Fragments beyond the vertical limit are clipped. These limitations must be addressed before treating this as a complete saved-world LOD system.

The camera far plane and ordinary atmospheric/render fog accommodate the horizon. Short-range water, blindness/darkness and special fog remain in effect; weather attenuation is retained. Renderer resources are isolated in a conditional built-in pack so handoff does not leave a global fog include behind.

## Local performance dashboard

Use **Local performance dashboard** in the Deep Primal config/distance screen. The normal address is `http://127.0.0.1:8787/`. Each process uses the preferred port or the next available port within a 20-port range and logs its actual address. Client and dedicated-server processes have separate dashboards. An integrated server shares its client's dashboard. A remote multiplayer server's world counters are not inferred or automatically exposed through a client dashboard.

`config/deepprimal-diagnostics.toml` is a startup config:

| Setting | Default | Meaning |
| --- | --- | --- |
| `localhostDashboard` | true | Enable the read-only listener; restart to change |
| `dashboardPort` | 8787 | Preferred loopback port, 1024–65515 |

The dashboard contains heap used/committed/limit, non-heap and direct-buffer totals in the export, process CPU, JVM thread count, JVM collection-time/count deltas, frame interval median/p95/max, FPS, server TPS and tick-work timings, dimension height/sea/time, tracked chunk columns, pending tasks, scheduled ticks, players/entities, renderer ownership/API/device, distances, and horizon work counters. The diagnostic JSON also includes Java/OS/version and installed mod IDs/versions.

History is one sample per second, retained in a fixed 600-entry ring. Browser polling requests new sequences, and graphs offer 2/5/10-minute windows, pause/resume, and JSON export. World data is copied on the server thread; client data is copied on the client thread. HTTP handlers only read immutable snapshots. They do not enumerate chunks, run commands, or touch live worlds. Paused/stopped server data is marked stale rather than shown as current throughput.

The service binds only IPv4 loopback. It accepts GET requests, validates Host and browser Origin/fetch-site headers, sends no CORS permission, and has bounded daemon workers/request queues. It provides no command/config/filesystem write API and uses no external web assets or telemetry services. Dedicated-server shutdown stops its listener; the client listener remains useful in menus and ends with the process.

Frame intervals include FPS limiting, waits and other work; they are not GPU execution time. JVM collection time is not an exact stop-the-world trace. Direct-buffer usage excludes other native allocations. Whole-frame GPU/VRAM usage and disk/save inventories are not measured. The supported OpenGL horizon pass has its own timestamp pair sampled every 30 draws, polled without waits. CPU mesh build, upload and submission are reported as work per telemetry snapshot, while GPU time is the latest sampled pass; these are different units/scopes. Chunk-column counters include native tracked holders and are not an exact count of fully generated/rendered chunks. Use the export with these limits in mind.

## Alpha 9

The standard package contains the optional [Vulkan adapter](VULKAN-BACKPORT.md). Its Graphics API page stages a restart preference; the displayed active backend comes from the real device. OpenGL is the default. The Chunk rebuilds page bounds ordinary nearby synchronous work between indivisible builds; player edits bypass the budget.

The memory guard bounds effective native render and server simulation distance without overwriting requested options. It estimates full-height column residency from Java heap and loaded dimension height. The dashboard reports requested/effective render distance, native synchronous/deferred/player rebuild counts, horizon transition builds, save backpressure and peak pending chunk writes. The OpenGL horizon-pass GPU timer reports unavailable on Vulkan; native Vulkan device timestamps are implemented separately. Vulkan/Windows GPU performance has not been certified.
