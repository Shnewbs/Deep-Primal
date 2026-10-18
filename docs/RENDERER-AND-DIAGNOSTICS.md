# Built-in renderer and local diagnostics

Alpha 6 / Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.

## Renderer ownership

Deep Primal provides its rendering module and optimizations directly in the mod. No Sodium, LOD mod, or browser extension is required. This first renderer stage owns a coarse horizon mesh pass and the existing native vertical-grid, fog, distance, and camera optimizations. Nearby voxel meshing/draw preparation still uses Minecraft; this is not a complete replacement for that pipeline. OpenGL is the implemented backend. Vulkan remains a separate implementation milestone.

At startup, the module detects `sodium`, `embeddium`, `rubidium`, `vulkanmod`, `nvidium`, `iris`, `oculus`, and `distanthorizons`. If any is present, it silently yields the renderer module as a unit: overlapping client mixins are not applied, its required renderer resource pack is not registered, and its horizon is inactive. There is no popup, file deletion, uninstall, or changes to the other mod. Ownership is visible in settings and diagnostics. Removing the competing mod restores the built-in renderer on the next launch. Unrelated optimizers such as Lithium/FerriteCore do not disable this renderer. Worldgen, server optimizations, XP rules, and diagnostics remain active.

The known-ID policy is deliberately conservative. It is not a claim that every renderer/shader/mod combination has been tested. An unrecognized replacement can disable this renderer using `-Ddeepprimal.renderer.disabled=true` before launch. Both automatic and explicit handoff happen before overlapping mixins transform the client.

## Horizon and budgets

Open **Video Settings → Deep Primal distances…**, or **Mods → Deep Primal → Config**. Typed values and sliders apply together. The **Terrain horizon** setting accepts 0–256 chunks; default 192, 0 disables it. Full-detail rendering remains 2–96 chunks, simulation 2–32, and vertical radius 0–96 sections. The 256 value belongs to the authored coarse horizon and is not sent through the vanilla byte-sized full-detail distance setting.

The horizon samples Deep Primal's seeded terrain-height field on the server and sends compact tiles to the client. It never obtains FULL chunks, places tickets, or runs distant simulation. It does not share the world seed with the client. Optional versioned payloads allow old servers to decline the feature. Dimensions without the Deep Primal `ColumnSurface` height field disable the horizon instead of inventing a surface.

- 512×512-block tiles, 64-block sample spacing, 81 height/color samples and 128 triangles per tile.
- Server: at most 32 queued jobs; one pending job per player; requests at least four game ticks apart and restricted to the player's current dimension and 256-chunk vicinity. Sampling checks a global 1-ms budget between samples, with a hard maximum of 64 samples per tick. Individual native density evaluations cannot be preempted.
- Seeded source lookup is cached per dimension. The shared server tile cache holds at most 512 completed tiles. Sources, jobs, and caches clear on server stop and full datapack sync.
- Client: one in-flight request, 5-second retry, one mesh upload per extraction, and at most 400 cached tile meshes. A tile's vertex data is 6,144 bytes; the maximum cached vertex payload is about 2.35 MiB, excluding driver overhead, buffers used by Minecraft, and transient upload machinery.
- Tile/dimension/session checks reject stale replies. Tiles outside the current footprint are evicted and their GPU buffers closed. Disconnect clears the client cache.
- Camera-frustum and vertical bounds filter tile draws. Back-face and depth tests operate on the GPU. Transform uniforms are batched before the draw pass. This is not yet a conservative CPU terrain-occlusion solver.

The landscape is an approximate generated height field with simple height-based colors and an opaque water surface. It omits caves, overhangs, trees, structures, blocks/builds and entities; existing edited terrain is not represented. Coarse triangles and transition seams remain possible. Nearby native meshes depth-test against it, and a near exclusion region limits cave leakage. Fragments beyond the vertical limit are clipped. These limitations must be addressed before treating this as a complete saved-world LOD system.

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

Frame intervals include FPS limiting, waits and other work; they are not GPU execution time. JVM collection time is not an exact stop-the-world trace. Direct-buffer usage excludes other native allocations. GPU/VRAM usage and disk/save inventories are not measured. Chunk-column counters include native tracked holders and are not an exact count of fully generated/rendered chunks. Use the export with these limits in mind.
