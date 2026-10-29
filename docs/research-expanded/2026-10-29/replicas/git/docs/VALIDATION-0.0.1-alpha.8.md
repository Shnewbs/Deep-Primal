# Expanded replica — docs/VALIDATION-0.0.1-alpha.8.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.8.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.8.md |
| Preserved source SHA-256 | `6afd5b65e3b36203120052d12e423969e3a1939a4e430641ad11709f6a0ca91e` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Validation — 0.0.1-alpha.8”. Its original opening states: “Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This update improves the existing OpenGL horizon and video controls. Full Vulkan and a complete native near-block renderer replacement remain unimplemented.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `VK_ERROR_INCOMPATIBLE_DRIVER`.

## Research expansion — gaps and details to deepen

- Carry the renderer result from bounded CPU/model behavior to actual GPU lifetime, submission, presentation, and target-device evidence.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Use separate ledgers for queued, staged, resident, retired, and reusable bytes, and identify the completion signal permitting reuse.
- Name pre-draw preparation, command encoding, API submission, GPU execution, and presentation intervals separately.

## Research expansion — acceptance checks

- Inject failure before allocation, after reservation, during upload, and before publish; verify accounting and in-flight resource validity.
- Run matched native scenes with travel, stationary recovery, minimize/resize, world switch, and device recreation before claiming frame-time or VRAM gains.

## Research expansion — source anchors

- Validation — 0.0.1-alpha.8
- Reproduction

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.8/runtime-summary.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.8/runtime-summary.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
