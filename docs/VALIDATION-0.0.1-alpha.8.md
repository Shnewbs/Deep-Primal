# Validation — 0.0.1-alpha.8

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This update improves the existing OpenGL horizon and video controls. Full Vulkan and a complete native near-block renderer replacement remain unimplemented.

## Changes and focused checks

Indexed mesh generation shares roof vertices and uses unsigned-short GPU indices. Expanded triangles, positions, colors and winding are compared against a test-only alpha 7 reference at every detail level, including gradient colors. Fine tile plus all cached draw levels: 59,412 bytes versus 172,320, a 65.52% reduction. This covers vertex/index payload rather than driver allocations or total VRAM.

View-policy thresholds are computed once per extraction/tick, then compared by squared distance; 10,000 differential cases check the previous selection rule. Model-view/texture/color/offset state and indexed draw records are reused. Optional shared indexed submission retains a per-draw fallback. The native 26.1 OpenGL API still loops over native draws; this is not GPU MultiDrawIndirect.

Uploads stage one tile's detail levels across frames. Level count, payload bytes and elapsed CPU time bound submission between indivisible uploads. The current cached tile stays available until the replacement is complete. Cancellation releases partial resources. Unnecessary horizon work stops when its distance falls within the native full-detail range.

Exact sample-grid reduction allows finer cached/in-flight terrain samples to satisfy coarser requests. Grid reduction correctness has standalone checks; the sample container validates grid shape and rejects requests for finer data. Nearest-first requests use player position. A disabled horizon cancels work even before its first cached result. No protocol or saved-world migration is introduced.

Optional entity/block-entity/particle range and particle-admission controls default to native behavior. Bounds tests and FPS-ceiling tests cover inclusive boundaries and lower-only limits. Live checks exercise actual dispatcher/particle entry points, glowing-entity exemption, skipped counters, upload ceilings and staged profiles. A four-digit upload-time value exposed an old three-character parser restriction; corrected bounded parsing also rejects integer overflow.

All existing standalone height, selection, grid, cache and workload checks remain in the build. The reference mesh and all development controllers are excluded from the production JAR. No third-party optimizer/native library is bundled. The [cross-project audit](PERFORMANCE-AUDIT-2026-10-18.md) records Mojang, CurseForge and Hytale sources and maps implemented versus deferred techniques.

## Packaged live validation

Disposable dedicated server/client, seed `8000542833694305166`, height -128..639, full-detail distance 6, simulation 2 and horizon 256. Mesa llvmpipe on a virtual display, 20-FPS cap, client heap 3 GiB. Adaptive work and background pausing are disabled while filling the correctness footprint, then enabled for focused checks. These runs verify behavior and bounds; they do not establish hardware FPS gains.

The complete footprint reached 238/238 cached tiles with 213 native near chunks, 1,884,096 retained vertex/index bytes and all three visible detail classes. Maximum observed staged upload was two levels / 54,456 bytes, below the configured two levels / 256 KiB ceiling. Batched and fallback paths produced pixel-identical 854×480 captures at the fixed viewpoint. Horizon/full-height/vertical-clipping captures and all four settings pages were visually inspected.

Focus loss through a second X11 window lowered the effective cap to 7 FPS and paused optional horizon work. Restoring focus restored normal work. A coarse request reused 81 samples from the cached finer grid. Disable/re-enable reused the shared server cache. The 1 MiB retained-mesh limit stayed within its ceiling; large teleports discarded the obsolete footprint. Adaptive work was re-enabled for budget/movement checks. Native client/server shutdown completed cleanly.

Dashboard HTTP checks passed read-only method, Host/Origin/fetch-site rejection and incremental polling. Desktop/mobile browser checks passed all six charts, pause/resume/history and zero browser page errors. This is a local read-only dashboard, not remote telemetry.

Sodium 0.9.2 for NeoForge 26.1.2 passed a separate packaged run: renderer ownership was `sodium`, the built-in renderer pack and overlapping mixins were absent, the Deep Primal horizon stayed inactive, and the dashboard continued working. Client/server shutdown passed. Other renderer combinations and Dynamic FPS retain policy coverage rather than equivalent live coverage.

The [runtime summary](validation/0.0.1-alpha.8/runtime-summary.json) retains observations and scope. Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.8.jar`, 180,924 bytes. SHA-256: `fb542f3b65c0a3a539ee9a567502c940ad42df9e1615b53edf63aebf854fe294`. All 76 compiled production classes match the packaged JAR; development classes are excluded. All 16 terrain/data files are byte-identical to alpha 7.

## Reproduction

Run `./gradlew build compileVisualValidationJava` with Java 25. Packaged client/server invocation follows [alpha 6](VALIDATION-0.0.1-alpha.6.md). Additional opt-in controller actions: `performance-checks`, `performance-page=0` through `performance-page=3`, `batching=false`, `batching=true`, `background=7`, `reuse-samples`, `memory=1`, `memory=32` and `adaptive=true`. These are development-only commands, absent from the distributed JAR. Use a disposable world and fresh log markers/telemetry. A second focused X11 window drives the focus-loss check; root focus can leave a GLFW window active under PointerRoot semantics.

## Vulkan and remaining coverage

`python3 tools/vulkan_probe.py` finds a loader but Vulkan 1.2 instance creation returns `VK_ERROR_INCOMPATIBLE_DRIVER` (-9). It is not a rendering test. A full device/shader/presentation implementation still needs to be authored and tested; the missing local driver is an additional validation limitation.

Remaining: real AMD/Intel/NVIDIA/integrated-device comparisons, resource reload/connection/world transitions, long-session allocation profiles, multi-client fairness and travel/load, minimized-window behavior on actual desktop platforms, all scene exceptions under modded renderers, edited-world LODs, true occlusion/indirect rendering and full Vulkan startup recovery. Default visual admission remains native. Speed explicitly trades visual detail for less work without changing simulation distances.
