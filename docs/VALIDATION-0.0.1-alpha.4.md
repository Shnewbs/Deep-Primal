# Validation — 0.0.1-alpha.4

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Experimental alpha, OpenGL renderer. The full survival conversion and Vulkan backend remain unimplemented.

## Changes

- Normal new-world creation defaults to Deep Primal on client and dedicated server. A required built-in data pack supplies `minecraft:normal`; the English world-type label is Deep Primal. Historical profile IDs remain registered for old saves and diagnostic tooling, but neither is added to the selectable world-type lists.
- `config/deepprimal-worldgen.toml` is a startup config. `extraUndergroundDepth=false` selects Y=-128..639 (768); true selects -384..639 (1024) for new normal worlds. Restart before creation. Both saved dimension types remain fixed. Sea level 96 and the height field are unchanged.
- Caves intersect independent bands to form tubes instead of cutting a single broad noise sheet. Continuous density protects 48 blocks of surface cover; larger chambers taper out below the alpine terrain. Native biome carvers retain lower-elevation entrances. There is no erosion or structural-support simulation, and no guarantee that all natural overhangs or small disconnected feature blocks are eliminated.
- The vertical render setting spans 0–96 16-block sections above/below the camera; 0 is full height. Apply rebuilds the native render area. Limited views allocate at most `min(worldSections, 2 * radius + 1)` Y slots per horizontal column. Absolute-Y ring mapping, section lookup, occlusion propagation and octree bounds support negative Y and vertical ranges larger than horizontal range.
- Distance fog scales Y independently through an authored compatible shader include and an extended native fog buffer. Environmental spherical fog remains active. The camera far plane includes the selected vertical range. Replacement shader/rendering systems require compatibility testing.

The cave change applies to newly generated chunks. Existing chunks retain old carving, so old/new chunks can have seams. The height config never migrates an existing save. Chunk storage, lighting and networking still operate on full-height columns; vertical rendering does not resolve the reported alpha 2 32-chunk/4-GiB heap exhaustion by itself.

## Runtime checks

Disposable local worlds, seed `-8843447232845721624`:

| Check | Result |
| --- | --- |
| Dedicated server without an explicit level-type | Deep Primal, -128..639, sea 96 |
| Enable extra depth and reopen that save | Height remains -128..639 |
| Fresh normal world with extra depth | -384..639 |
| Disable extra depth and reopen that save | Height remains -384..639 |
| Explicit flat world with extra-depth config enabled | Flat generation, -64..319 |
| Actual new-world UI | Default label Deep Primal; configured 768/1024 height; legacy comparison entries absent |
| Standard world, horizontal 6, vertical 2 | Render-section allocations 8,112 → 845; 8,450 live nodes checked over movement/boundaries, with optimized scanning on/off |
| Deep world, horizontal 10, vertical 2 | Render-section allocations 28,224 → 2,205; 22,050 live nodes checked; Apply/Cancel, invalid input, full-height restore |
| Horizontal 2, vertical 8 | 425 allocated sections; occlusion propagates vertically beyond horizontal radius; octree encloses the complete window |
| Actual GPU image, horizontal 2 / vertical 20 | Temporary gold marker about 260 blocks below the camera remains visible; 100 yellow pixels in the center inspection region. Marker removed afterward. |

The standalone build checks include 200,000 surface-height differential cases, 3,000 nearest-queue comparisons, 34,300 horizontal ring coordinates, vertical-window coverage/negative-Y/boundary cases, input and fog-scale checks, and cache/tick-ring checks. Validation-only commands and capture code are excluded from the JAR.

The normal new-world screen is exercised with both client startup configurations. The shader include and extended uniform buffer compile and render in the real graphical client; the visual marker verifies vertical visibility beyond the horizontal distance-fog limit. The final packaged JAR also receives its own dedicated-server startup and default-world check, using the JAR as the authoritative mod source.

## Cave generation cost

One observational before/after run, same seed `20261018`, same three fresh regions and 75 cold FULL requests per version, each followed by a 75-request aggregate repeat. The baseline uses the alpha 3 noise-settings JSON in a disposable data pack with the same alpha 4 runtime. Radius 2, concurrency 1, slow-tick benchmark pacing disabled, 2-GiB server heaps. Native workers and vanilla biome/feature generation remain active.

| Metric | Old carving | Revised carving |
| --- | ---: | ---: |
| Cold median request-to-ready | 199.742 ms | 199.934 ms |
| Cold p95 | 398.118 ms | 400.327 ms |
| Cold batch elapsed total | 16.46 s | 17.08 s |
| Repeat median | 149.938 ms | 149.921 ms |

Cold aggregate elapsed was approximately 3.8% higher in this run. This is not a generation speedup claim or statistical performance gate. Request timings include tick polling, neighbor generation, I/O and scheduling; they are not isolated density-function CPU timings. Raw reports and comparison JSON are retained alongside this document.

Graphical checks use Mesa llvmpipe in a virtual display on a shared Linux container, 8 available processors, with 2-GiB client/server heaps. These are correctness and focused allocation checks, not real-GPU FPS/VRAM benchmarks or a full 96-chunk stress test.

## Reproduction

`./gradlew build` runs core checks. `python3 tools/generate_worldgen.py` regenerates the geometry and configured preset packs. `tools/server_smoke.py` exercises the historical profiles for controlled benchmarks; use `--comparison --radius 2` for the three cold/repeat pairs.

The optional `visualValidation` source set supports `-PterrainValidation -PcaptureServer=127.0.0.1:25565` and a `build/visual-validation/capture-request.txt` control file. `checks` exercises the real vertical widgets/grid/occlusion; `volume=2,20` sets render axes for a visual test; other scene names capture a PNG; `world-creation` verifies the actual creation model and selects its World tab; `stop` exits. This controller and `/rockscan` exist only in the development source set. The conservative rock-component scan treats exposed region boundaries as supported and checks stone/snow only; it is not a complete geological support solver.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.4.jar`. SHA-256: `f1100ea2d0e358a14b055f0b1562cb1c5c894e5b274247d03633795563d96b64`.
