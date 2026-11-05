# Expanded replica — docs/VALIDATION-0.0.1-alpha.9.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.9.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.9.md |
| Preserved source SHA-256 | `07dcda38a98a7590e0b9830625de4cef64df5cdf7f760a22c6d3af064c7d9373` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Validation — 0.0.1-alpha.9”. Its original opening states: “Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. The standard package includes a separately licensed, optional Vulkan adapter. OpenGL remains the default. This report describes bounded work and functional checks; software rendering cannot establish an FPS advantage on the user's GPU.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Validation — 0.0.1-alpha.9
- Memory failure and admission
- Vulkan implementation and functional scope

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../renderer-vulkan/UPSTREAM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/renderer-vulkan/UPSTREAM.json) |
| `validation/0.0.1-alpha.9/checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.9/checks.txt) |
| `validation/0.0.1-alpha.9/runtime-summary.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.9/runtime-summary.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Validation — 0.0.1-alpha.9

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. The standard package includes a separately licensed, optional Vulkan adapter. OpenGL remains the default. This report describes bounded work and functional checks; software rendering cannot establish an FPS advantage on the user's GPU.

## Memory failure and admission

The supplied alpha 8 failure exhausted an 8 GiB Java heap after the full-detail radius rose from 10 to 96 with simulation distance 16. The first recorded heap failure occurred while encoding chunk tick data during saving. Later native/JNA and watcher failures followed the exhausted heap. A radius of 96 admits far more full-height columns than the earlier setting; replacing the graphics API does not remove that server-side demand. Raw logs, account information, paths and world identifiers are not published.

The enabled memory guard estimates full-height column cost from heap capacity and dimension section count, reserves a quarter of heap for other work, and bounds both effective full-detail render and simulation radii. Requested values remain saved. Example policy results: an 8 GiB heap with 48 sections gives radius 15; 64 sections gives 13. The estimate is a conservative admission policy, not a guarantee against every modpack's memory use. Distant horizon meshes have their separate existing memory/work budgets.

Chunk snapshots and asynchronous writes share a configurable per-dimension concurrency ceiling (default 16). Bulk flush/pause/shutdown paths wait before allocating another snapshot. Native completion, serialization, storage, dirty state and failure reporting remain in charge. A failure before native future ownership releases its reserved slot. No saves are skipped to meet the limit. The dashboard reports maximum pending writes and backpressure.

A packaged integrated-world run requested render 96 and simulation 16 on a 3 GiB heap. Both effective server radii were nine, the requested options remained 96/16, repeated distance-change logging was absent, and saving/exit completed.

## Native rebuild and horizon work

Nearby synchronous section builds have a count and elapsed-time allowance checked between builds. Exhaustion uses Minecraft's native asynchronous section path. Player-caused dirty sections bypass this allowance. The default is two builds / 1,500 microseconds; individual builds cannot be interrupted at that boundary. Native default chunk prioritization already favors asynchronous work, so this is not an assertion that every default frame is faster. The focused live stress check selects native NEARBY prioritization explicitly.

Transition geometry tracks exact selected boundary-mesh identities in a bounded, reused array. Unrelated tile arrivals, camera FOV and workload changes no longer invalidate it. Visible-work storage is reused. Core checks cover identity changes, dropped dependencies, rebuild allowances, height-sensitive memory limits and existing mesh/grid/work policies.

The packaged dedicated/client stress run filled 25,921 blocks with NEARBY prioritization and a one-build / 250-microsecond allowance. It observed one ordinary synchronous build and eight native-worker deferrals. Nine player-dirty sections subsequently remained synchronous. At the forced bulk-save checkpoint, the configured ceiling was two, peak pending writes were two, and the lifetime admission-wait count was 1,083. These are observations from a disposable fixture, not general throughput comparisons.

The 256-chunk horizon reached all 238 target tiles, retaining 1,884,096 mesh bytes. Total uploaded bytes were 1,888,704 with three transition rebuilds across initial movement and footprint arrival. Maximum observed staged tile upload was two levels / 54,456 bytes. These counters do not include driver allocations or prove a total-VRAM reduction.

Additional packaged checks passed visual admission controls, all six settings pages, background focus throttling to seven FPS, finer-grid sample reuse, shared server-cache reuse, a one-MiB mesh ceiling, teleport cancellation, dashboard HTTP rejection/polling behavior and desktop/mobile dashboard interactions without browser page errors. Client and dedicated-server save/exit completed.

## Vulkan implementation and functional scope

The adapter is based on VulkanMod 0.6.8+26.1.2 under LGPL-3.0-only, with origin and artifact hash pinned in [UPSTREAM.json](../renderer-vulkan/UPSTREAM.json). [The backport notes](VULKAN-BACKPORT.md) describe selection, integration, licensing and the core-only build option. It retains Minecraft's native chunk mesher and gameplay simulation. Fabric modules, game classes and the upstream custom terrain mesher are not bundled.

Linux x64 SwiftShader Vulkan 1.3 on a virtual display rendered a disposable integrated world with near terrain, textured vegetation, clouds, distant horizon, text and the performance menus. Captures were visually inspected. The native GPU atlas projection required an origin correction; device readback verifies nonempty base and coarse atlas mip levels. Buffer and RGBA texture copies are compared byte for byte, timestamps must return a positive result, and lightmap readback must contain lit pixels. Both NeoForge depth/stencil formats are exercised with matching and failing stencil references. Window resize and resource reload complete with correctly textured terrain and a clean world save/exit.

The graphics page distinguishes the saved next-launch preference from the actual active API. Device initialization failure attempts OpenGL immediately; an incomplete startup marker selects OpenGL on the next launch. Known competing graphics replacements disable this adapter. Applying a preference clears the recovery marker. These controls do not swap devices inside a running world.

The packaged recovery matrix passed a missing Vulkan driver, a preexisting incomplete-startup marker, and Sodium 0.9.2+mc26.1.2 installed with Vulkan requested. Each loaded an integrated world on OpenGL and saved/exited cleanly. The failed-window retry retains GLFW until the next backend can create its window; normal shutdown still terminates it. Sodium ownership was verified with the native renderer pack/overlapping accessor absent and the Deep Primal horizon inactive.

## Limits

No universal FPS, total-memory reduction, or full OptiFine/Sodium/Lithium equivalence is claimed. Vulkan can reduce API overhead when that is the bottleneck, but simulation, generation, chunk retention and driver behavior still matter. AMD/Windows, Intel/NVIDIA hardware, macOS, long sessions, multi-client travel and arbitrary modpacks remain unverified. Bundled platform natives are not evidence of platform certification. Direct OpenGL mods and shader packs require separate compatibility work. OpenAL is unavailable in this test environment; graphics/save checks do not validate audio.

World generation, dimension data and saved-world formats are unchanged from alpha 8. Development capture/check controllers are excluded from the shipped JAR.

## Reproduction

Run `./gradlew build compileVisualValidationJava` with Java 25. The ordinary build includes the optional backend; `-PwithoutVulkan` produces the core-only package. Packaged launch validation uses `-PpackagedValidation -PvisualValidation -PterrainValidation` with `-PcaptureWorld=world` or `-PcaptureServer=127.0.0.1:PORT`, a disposable run directory and a 3 GiB client heap. Vulkan selection during development uses `-PgraphicsBackend=Vulkan`; installed clients use the Graphics API page and restart.

Development controller actions include `memory-guard-checks`, `integrated-memory-checks`, `rebuild-stress`, `player-rebuild`, `vulkan-checks`, `backend-check=OpenGL`, `resize-check`, `reload-check` and `performance-page=0` through `performance-page=5`, in addition to prior horizon checks. The stencil fixture tests actual GPU output, including a copied combined depth/stencil image. Never run destructive stress fills against a user's real world.

## Release artifact

`DeepPrimal-26.1.2-0.0.1-alpha.9.jar`, 22489175 bytes. SHA-256: `89cf65f976c9c0fbeb2552f9e3951e7041c8736df69e2f63f92165eb4ea5ae2d`. All 85 compiled production classes and 177 adapter classes match the package. All 12 terrain/data files match alpha 8. The [sanitized runtime summary](validation/0.0.1-alpha.9/runtime-summary.json) and [check markers](validation/0.0.1-alpha.9/checks.txt) retain the tested scope. The final standard build and standalone core checks passed.


<!-- END PRESERVED SOURCE -->
