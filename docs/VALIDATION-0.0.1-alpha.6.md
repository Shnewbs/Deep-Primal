# Validation — 0.0.1-alpha.6

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. The first authored horizon-renderer stage, automatic renderer handoff, and read-only localhost diagnostics. Nearby voxel meshing/draw preparation remains native. Vulkan is not implemented.

## Build and package

`build` and `compileVisualValidationJava` pass, including the existing 200,000 surface-solver differential cases, 3,000 nearest-queue cases, 34,300 horizontal coordinates, vertical boundaries, queue bounds, and cache/tick checks. Additional checks cover signed horizon tile coordinates, range bounds, ownership for each recognized renderer, and retention with unrelated optimization mods.

The packaged JAR is the authoritative main mod source for dedicated-server and graphical-client runs. Development validation classes are added separately and excluded from the distributed JAR. All 16 `data/` and `worldgen/` resources are byte-identical to alpha 5. No save migration is required. Renderer assets are in a conditional built-in pack; there is no unconditional root override of Minecraft's fog include.

## Native graphical run

A real graphical client connected to a real dedicated server on the supplied crash seed `8000542833694305166`, standard Y=-128..639, sea 96. Full-detail distance 6, simulation 2, horizon 256, full vertical height. Mesa llvmpipe/virtual display, 3-GiB client and 2-GiB server heaps. FPS was deliberately limited to 20. This is correctness validation, not a real-GPU speedup benchmark.

- The complete footprint reached 238 cached horizon tiles and 84 visible tiles. It required 1,462,272 bytes of authored horizon vertex payload. Native near-grid distance remained 6 and 213 nearby client chunks were delivered in the focused settings run.
- Real GPU triangle submission was observed, along with a visibly populated macro-landscape beyond the native full-detail range. Native and horizon-disabled captures were inspected.
- The shared server cache reached 238 tiles. Client horizon disable/re-enable reused at least 27 cached responses; the retained server snapshot recorded 42 hits. The server sampled 19,278 points to complete those original 238 tiles.
- Queues, meshes, cache counts and world counters were recorded through the actual dashboard. The full footprint snapshot and server counters are retained alongside this report.
- The staged controls reject horizon 257, accept 192, and preserve the prior value on Cancel. The screen includes a dashboard button and is available through the NeoForge Mods config entry.

The horizon is coarse generated terrain. It does not represent edited blocks, structures, trees, caves, overhangs or entities. Its colors are approximate, and transition seams/coarse facets remain. It must not be presented as a complete replacement for near-block rendering or a saved-world LOD system. Full-detail distance retains its existing 96-chunk experimental ceiling; the 256 setting is the independent horizon.

## Automatic handoff

An actual checksum-verified `sodium-neoforge-0.9.2+mc26.1.2.jar` was installed only in a disposable test client. Sodium's real graphics bootstrap and worker threads ran. Deep Primal selected owner `sodium`, did not register its native renderer resource pack, did not apply its overlapping LevelRenderer accessor, and reported horizon 0. Its handoff settings screen, server/world systems and dashboard remained operational. The client and server shut down successfully.

The same policy recognizes Embeddium, Rubidium, VulkanMod, Nvidium, Iris, Oculus and Distant Horizons; their IDs are covered by policy checks. Those other rendering implementations have not received equivalent live combination testing. No third-party renderer or performance-mod JAR is distributed in Deep Primal.

## Dashboard

Two actual game processes obtained separate loopback ports 8787 and 8788, exercising occupied-port fallback. HTTP checks verified the root page, JSON and incremental sequence polling, foreign Host/Origin/fetch-site rejection (403), and POST rejection (405). Sampling/export does not touch live world objects from HTTP threads.

Browser checks exercised live canvases, pause/resume, 2/5/10-minute selection, desktop and 390-pixel layouts, and a populated three-dimension table. No JavaScript page errors or document-level mobile overflow occurred. Graph and settings captures were visually inspected. The JSON includes real dimension/entity/tick/queue totals and installed mod/version metadata. Collection and frame timings retain the limitations described in the user guide; GPU timings/VRAM and disk inventories are not measured.

The listener is enabled by default, IPv4 loopback only, read-only, with 600 history entries and bounded daemon workers. See [renderer and diagnostics guide](RENDERER-AND-DIAGNOSTICS.md) for configuration, retained budgets, limitations, and reproduction steps.

## Reproduction

1. Run `./gradlew build compileVisualValidationJava`.
2. Start a disposable server using `runServer -PpackagedValidation -PrunDir=<directory>`, default normal worldgen, view 6, simulation 2. Accept Minecraft's EULA before starting.
3. Start `runClient -PpackagedValidation -PvisualValidation -PterrainValidation -PvalidationHeap=3G -PcaptureServer=127.0.0.1:<port> -PclientRunDir=<directory>`. Set client full-detail 6, horizon 256, vertical 0. Use a high spectator vantage point to inspect the coarse landscape.
4. The development `build/visual-validation/capture-request.txt` controller accepts `renderer-checks`, `horizon=0`, `horizon=256`, `controls`, named screenshots, and `stop`. Wait for the current process's explicit log markers; old `.done` files are not evidence of a current test.
5. Repeat with the real supported Sodium JAR in that client's `mods` folder to exercise handoff. Inspect the actual logged renderer owner and resource packs.
6. Open each process's logged dashboard address. `/api/metrics?since=<sequence>` returns new points; `/api/export` downloads retained diagnostics. No external web assets or browser extensions are needed.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.6.jar` (135,061 bytes). SHA-256: `4af4b026ac849ee0af5e3f0d0df09dd9de2af0e86cd093cb909c86f5d3a6ab95`.
