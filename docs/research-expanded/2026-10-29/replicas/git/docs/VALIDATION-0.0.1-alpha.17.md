# Expanded replica — docs/VALIDATION-0.0.1-alpha.17.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.17.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.17.md |
| Preserved source SHA-256 | `6cc8752017f80275d6a3680e9526af79aacc6007f72655c61f9dcb7e0e301bf7` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal 0.0.1-alpha.17 — integrated engine, LOD and graphics”. Its original opening states: “Validated 2026-10-21 with Java 25, Minecraft 26.1.2 and NeoForge 26.1.2.122. These are functional tests in a Linux container using Mesa software rendering, not FPS measurements or certification on Windows/AMD hardware.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `ChunkGenerator`, `NoiseBasedChunkGenerator`, `Fluid`.

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

- Deep Primal 0.0.1-alpha.17 — integrated engine, LOD and graphics
- Result and ownership
- Reproduction

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.17/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.17/installer-audit.json) |
| `validation/0.0.1-alpha.17/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.17/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal 0.0.1-alpha.17 — integrated engine, LOD and graphics

Validated 2026-10-21 with Java 25, Minecraft 26.1.2 and NeoForge 26.1.2.122. These are functional tests in a Linux container using Mesa software rendering, not FPS measurements or certification on Windows/AMD hardware.

## Result and ownership

New normal Overworlds serialize `deepprimal:continental_v1`, an independently authored `ChunkGenerator`, and `deepprimal:regional_v1`, our own biome source. Terrain decisions do not use `NoiseBasedChunkGenerator` or the native multi-noise biome source. Original seeded continental provinces, warped shores, relief, cached regional drainage, channel/valley carving, material fields, cave volumes and vegetation placement live in our core/host adapter. Existing block/biome assets remain placeholders. Nether/End remain native. Existing generators/data packs and the common performance/settings/Vulkan modules are preserved.

The root registers an independent `Fluid`, 16-unit water block/bucket, conservative bounded transport and original shared-corner surface mesher. The old eight-unit finite solver yields in these engine worlds. The new renderer captures actual delivered chunks, preserves vertical intervals and edit revisions, builds three voxel mesh levels and draws them through the common GPU API. It uses no DH, Cesium, Nanite or meshoptimizer runtime. The separate rejected standalone experiment is not included.

## Executed checks

| Check | Evidence |
| --- | --- |
| Default build, optional module checks and source boundaries | `build --offline` passed; core/module boundaries and Vulkan cache/descriptor checks ran. |
| Windows alternative packaging | `build --offline -PvulkanPlatform=windows` passed. Package contains Windows x64 shaderc/VMA DLLs, no Linux/macOS natives. This is packaging verification, not a Windows runtime test. |
| Original terrain core | 5,776 fixed-seed grid samples, 1,000 cross-instance/order comparisons, acyclic/downhill graph assertions, negative coordinates and bounded 12-region cache eviction passed. Sampled heights: -44.93..288.36. The coarse grid observed 6 upland river samples, 1,621 dry-land samples and 4,149 sea-level/ocean samples; these are coverage checks, not a quality/river-density benchmark. |
| Independent transport | All 578 legal source/destination/gravity combinations conserve volume and remain in range. Native closed-basin transport retained 16 units. Native flowing water imported eight units correctly even with legacy finite-water enabled. |
| Voxel storage/reduction | Actual vertical runs round-trip, stale revisions/corrupt files rejected, negative coordinates supported, roofs and lower terrain with air gaps retained at all three levels; each upload part bounded below 256 KiB and indices valid. |
| New standard/deep native worlds | Own generator/biome source and retained modules asserted, nine FULL chunks checked for floor/bounds, native fluid transport/input conversion and actual snapshot reduction passed. |
| Standard/deep reopen | Saved seven-unit/current-direction state and actual chunk snapshot survived reopen. Deep world stayed -384..639 after changing the creation-time depth option to false. |
| Legacy reopen | A disposable copy of an alpha 16 standard world reopened with its saved `NoiseBasedChunkGenerator` and -128..639 height preserved. The original world was not modified. |
| Native OpenGL/Vulkan client | Actual edit revision, source native chunk unload, LOD GPU drawing and disk snapshot restoration after process restart/renderer reset exercised. The fixture checks the selected backend actually runs; a fallback OpenGL run is not counted as Vulkan. |
| Settings regression | Real controls exercised across Apply, Revert, tab changes, a new screen, 340×240 resize and process restart. Saved radius 208 and target 88 were restored. Lean installer can clear a stale saved Vulkan request. |
| Native terrain scenes | Fixed-seed coast, elevated river and ridge captures were rendered by the native client on actual Vulkan. Final scene run reported zero Vulkan validation errors and zero remaining buffers/images after orderly disconnect/shutdown. |
| Existing regressions | Legacy waterway/drainage/partial-river, 3,000 nearest-queue, 34,300 ring-grid, 200,000 differential surface, 500,000 legacy hydrology transfers, horizon bounds/seams, corruption/cache and exact descriptor checks passed. Legacy checks do not imply those gameplay systems are integrated with the new fluid. |

Nine-chunk fixture request times are included in the raw records. Some runs reopen already-generated terrain; these are request-to-ready smoke timings and must not be advertised as cold generation throughput. LOD triangle/upload counts are cumulative functional counters, not per-frame performance results.

The first scene fixture stopped immediately after requesting an asynchronous screenshot, before readback/save completion. It was corrected to wait for completion and use orderly saving disconnect; the final rerun passed without the resulting shutdown errors. Test fixtures are excluded from release JARs.

## Custom graphics screen

The original Deep Primal screen is reachable from Video Settings and Mods config. Its General, Quality, Performance, LOD, Water and Graphics API categories expose saved requests, native effective limits, visible/requested LOD span, typed fields/sliders, staged changes and help. Water mesh/vertical changes rebuild native mesh allocations. API changes are saved atomically and take effect after restart. A missing optional adapter cannot trap the UI in a stale Vulkan preference. External renderer ownership disables the internal LOD/water controls.

LOD configuration is separate from full-detail rendering and simulation: requested radius 16–512 chunks (default 192), detail 1–12 pixels (default 3), GPU payload 4–256 MiB (default 32), compressed disk 16–4096 MiB (default 512), capture interval 1–8 ticks and upload count 1–4/frame. It captures immutable packed section copies on the game thread; a single bounded worker builds snapshots/meshes and handles disk I/O. Staged uploads leave the previous complete mesh visible until replacements are complete. Disk keys include schema, persistent server world UUID, local-save/server identity, dimension and coordinates. The old macro horizon requires an explicit legacy config switch; LOD off never enables it automatically.

## Package audit

[Machine-readable installer audit](validation/0.0.1-alpha.17/installer-audit.json) records byte sizes, hashes and nested entries. The default installer is about 0.49 MiB, with three nested modules and no native libraries. The optional Windows installer is about 10.91 MiB, with a fourth Vulkan module and only the relevant DLLs. Recursive audits found no duplicate classes, validation fixtures, rejected standalone classes or bundled Minecraft/NeoForge platform classes. JarJar manifests match actual contents and module versions. Licensing notices for the retained optional adapter remain included.

## Reproduction

Use Java 25 and the wrapper (`gradlew.bat` on Windows). `build` includes core/cache/source-boundary checks. This workspace used a toolchain launcher and `--offline` with already-resolved dependencies; normal checkouts can resolve dependencies through Gradle.

After accepting the Minecraft EULA, the dedicated fixture runs as follows against a **disposable** server directory:

```sh
DEEPPRIMAL_ENGINE_CHECKS=fresh DEEPPRIMAL_ENGINE_RECORD=/absolute/result.txt \
  ./gradlew runServer -PwithoutVulkan -PvisualValidation -PpackagedValidation -PrunDir=run-engine-check
```

Use a fixed `level-seed=20261020` and `builtInTerrainEngine=true`. `deep` additionally requires `extraUndergroundDepth=true` before first creation. Then change the depth config to false and run `reopen_deep`; `reopen` checks the standard world. `legacy` checks a disposable copy of an existing alpha 16 standard world. Never use `fresh` on a valuable save: it places a high-altitude transport test basin.

The client fixture uses a copied engine world named `engine-integration` and an actual display/GPU context:

```sh
./gradlew runClient -PwithoutVulkan -PvisualValidation -PpackagedValidation \
  -PengineValidation -PengineReopen -PcaptureWorld=engine-integration \
  -PclientRunDir=/absolute/disposable-client -PcaptureDir=/absolute/captures -PacceptWorldWarning
```

Omit `engineReopen` for an initial settings run. For Linux Vulkan use `-PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanRetry -PvulkanValidation` instead of `withoutVulkan`. `-PengineScenes` captures coast/river/ridge after delivered chunk/native mesh readiness. The reproduction commands explicitly permit experimental-world warnings. Capture classes never ship. [Compact final records](validation/0.0.1-alpha.17/native-evidence.txt) contain the functional markers without full startup logs.

## Limits and acceptance still open

- Terrain is a new integrated alpha. Native captures show working coasts, upland channels and relief, but vegetation, banks, coastal variety and broader seed composition still need refinement. They do not establish TFC/Terralith/Tectonic visual parity or final acceptance. Their full reference/implementation audits remain pending.
- Drainage plans are bounded whole-continent regional graphs, not shared global watershed/lake/outlet contracts. Rivers use a discharge proxy, not a physical catchment water balance. Unified lakes/spillways, erosion/deposition, connectivity validation and catalog geology are unfinished.
- The independent fluid has no complete pressure/displacement, groundwater/recharge, replenishment, lava or waterlogged-shape integration. Legacy rain/soil/ports/survival paths are retained but require individual integration checks. Breaking surf traveling into the coastline remains unfinished. Smooth volume surfaces do not constitute a surf engine.
- LOD currently uses explored/delivered chunks only, no unseen world generation or generated-terrain network stream. Reduction approximates block models, cutouts, materials, lighting and water; texture-atlas parity, stronger seam/transparent-water handling, resource-pack cache invalidation and broad cache-saturation/reconnect/multiplayer checks remain open. A resource pack change may require clearing that world's LOD cache.
- Native nearby chunk meshing remains the host implementation. GPU-driven occlusion, meshoptimizer/clusterlod integration, DH benchmark parity and hardware frame-time/VRAM certification are not established. Windows packaging has not been runtime-tested on the user's AMD hardware.
- Existing saved generators/heights are preserved; no migration is performed. Use a new world to inspect the new engine. Environmental catalogs remain planning-only, and the full survival conversion is not complete.


<!-- END PRESERVED SOURCE -->
