# Water polish: terrain geometry, discovery latency and native continuity

Version: `0.0.1-alpha.29-dev.129-water-polish.1`. Parent source: `12886c9926f090a2d9c9a332f24a983902849f05`.

Publication reconciled `main` at `9b83343f2ea76ce55595c816f49c02b18be0e4f6`, preserving two concurrent, isolated root-zone files. The combined Windows package was rebuilt successfully and its separate root-zone check passed 300,009 assertions. The complete water build/native checks below precede those unrelated additions; water production code did not change afterward. The combined assembly reused the already-passed water core checks (`-x coreChecks`), rather than representing that assembly as a second complete core-suite run.

This is a tested development delivery. It improves generated waterways and nearby water presentation. It does **not** close the complete water-engine roadmap or qualify the engine as AAA-complete.

## Resulting behavior

New standard and deep worlds select saved channel revision 11. Rain-weighted drainage area and channel grade determine width, depth and a bounded presentation-speed proxy. Larger catchments grow broader and deeper; steep reaches remain confined. Bend cross-sections shift the deeper bed toward the outer bank. Shared terrain-fitted drainage knots and water stages remain fixed, while cubic controls use a common node tangent to remove independent control-point kinks. Straight terrain corridors can still produce straight reaches; this implementation does not overlay repeating sine meanders.

The existing native finite-water authority remains responsible for volume, constituents, gravity, pooling, pressure, environmental supply and persistence. The hydraulic geometry is a generation/presentation approximation, **not** a new discharge ledger, calibrated real-world velocity model or full shallow-water solver. It creates no refill source and does not change water budgets or chemistry.

The animated renderer discovers newly delivered water sections immediately through a bounded queue. It also runs separate nearby and far scans, with nearest-column and nearest-height priority. Resident mesh updates still run first. Discovery retains the existing total 1 ms soft time allowance and bounded page/GPU memory limits; it never requests chunk loading. This removes the dependency on the arbitrary native render-grid slot order at large view distances. It does not make the entire render radius resident or guarantee a fixed frame rate.

Existing worlds retain their saved revisions 0–10. No migration or terrain rewrite is performed. Default geometry changes require a **new standard/deep world**. Rendering discovery benefits existing worlds too. Explicit historical waterways/delta presets retain their own revisions.

## Validation

| Check | Observed result | Scope |
| --- | --- | --- |
| Full Gradle build and visual-validation compilation | PASS, no check exclusions | Java 25, Gradle 9.2.1; `build compileVisualValidationJava -PvulkanPlatform=windows` |
| Hydraulic geometry | 302,851 assertions; 2,397 wet witnesses; 2,158 changed beds | Four real seeds, monotonic catchment geometry, mirrored outer-bend sections, deterministic replay and shared routed knots/stages |
| Width/depth witnesses | Widths 2.50–31.53 blocks; maximum sampled water-to-bed depth 27.85 | Observed sample range, not configured world maxima or a promise every river reaches those dimensions |
| Discovery traversal | 426,958 assertions | Nearest order, refusal/retry, budget, complete coverage, signed-height extremes; radius 125 starts at the camera page |
| Legacy replay | Revisions 0–10, 704 columns, 7,040 fields identical | Compared against actual parent-source classes, not a compatibility shim |
| Planning query probe | 324,823 → 290,243 terrain-height calls, 10.65% reduction | Four seeds, same 2,470 routed reaches; **not FPS or wall-clock loading time** |
| Python repository tests | 74 tests PASS | Existing tooling/source-guard suite; qualification scripts also parse successfully |
| Native world creation | Revision 11, 40 owned biomes, six scene captures, clean save/shutdown | Coast, headwater, river, lake, outlet, ridge; real integrated client |
| Native saved codecs | 26 checks PASS | Production codecs round-trip 0–11 and reject -1/12 inside the actual FML lifecycle |
| Native moving water | PASS | Intermediate frames, updates, drying, fallback, reenabling and resource reload; 28,854 terrain invalidations avoided in the fixture |
| Live cliff/cascade continuity | 224 frames; 1,292,256 shared-corner comparisons; 0 mismatches beyond quantization tolerance | Eight pages, 252 client-fixture cells; maximum gap 1/16384 block; CPU upload mirrors and screenshots, not pixel-difference or physical-flow acceptance |
| Windows artifact | CRC, six nested module declarations, shaderc/VMA DLLs, revision-11 presets PASS | No Linux/macOS natives or private build agent bundled; see `package-check.json` |

Native tests used Linux Mesa software OpenGL. Their timing is not a Windows GPU performance result. The controlled cliff fixture reported height-publication p95 0.356194 ms in that run; it is not an engine frame-time guarantee.

## Reproducing the added checks

Run the full existing build with Java 25:

```sh
./gradlew build compileVisualValidationJava -PvulkanPlatform=windows
```

`coreChecks` includes `HydraulicChannelChecks` and `WaterPageScanChecks`. Legacy differential replay runs `com.shnewbs.deepprimal.core.OrganicDrainageChecks` with two command-line arguments: the compiled parent-source classes directory and a core JAR containing its unchanged dependencies. The ordinary suite does not silently manufacture a baseline.

For the terrain-height query probe after building core classes:

```sh
javac -cp engine-core/build/classes/java/main -d build/water-planning tools/water/WaterPlanningProbe.java
java -Xmx512m -cp build/water-planning:engine-core/build/classes/java/main WaterPlanningProbe
```

Use `;` as the classpath separator on Windows. The probe counts actual production terrain calls and checks that the routed reach count stays unchanged.

Native creation uses the existing `tools/organic-native.init.gradle`, `-PvisualValidation`, and `-PworldCreationValidation`. Moving-water validation requires **both** `-PanimatedWaterValidation` and `-PfillingValidation`. Use a disposable revision-11 save, `-PcaptureWorld=<save-name>`, and a real graphics/display environment:

```sh
./gradlew -PvulkanPlatform=linux -PvisualValidation -PanimatedWaterValidation -PfillingValidation -PcaptureWorld=<save-name> -PcaptureDir=build/water-motion-native -PgraphicsBackend=OpenGL runClient
./gradlew -I tools/water/cliff-native.init.gradle -PvulkanPlatform=linux -PvisualValidation -PanimatedWaterValidation -PfillingValidation -PcaptureWorld=<save-name> -PcaptureDir=build/water-cliff-native -PgraphicsBackend=OpenGL runClient
```

The cliff fixture modifies client-side test geometry at altitude and then exits; use a disposable test world. Native codec assertions run from the validation source set after loader startup. They must not run as a plain Java process without FML.

## Remaining completion gates

- Natural waterfall temporal/pixel acceptance across more seeds, camera trajectories and both backends; the controlled seam check is narrower evidence.
- Named Windows GPU/Vulkan high-distance frame/tick/memory tails, server/network multiplayer stress, cold-load/reload measurements and extended in-world conservation/persistence soak.
- Broader shared watershed, seasonal groundwater/melt, lake/outlet/backwater, ocean/circulation and chemistry acceptance from `docs/WATER-BUDGET-REQUIREMENTS.md`; this patch does not implement or certify every planned mechanism.
- Finer routing where the current 32-block receiver lattice still exposes long, regular headwater alignments. Terrain-derived straight valleys are permitted; grid artifacts require further evidence and refinement rather than cosmetic oscillation.

The six screenshots were reviewed. The river bend is smoother; the headwater view still contains fairly straight steep reaches and a distant water-boundary silhouette that needs broader camera/load-boundary review. Neither that capture nor the fixture is evidence that every natural waterfall has stopped flickering. `engine_complete` remains false.

## Design references

The catchment geometry and velocity approximation were informed by [USGS hydraulic geometry](https://pubs.usgs.gov/publication/pp252), [USGS Manning roughness selection](https://pubs.usgs.gov/publication/wsp1898B), and [HEC-RAS uniform flow computations](https://www.hec.usace.army.mil/confluence/rasdocs/ras1dtechref/6.0/stable-channel-design-functions/uniform-flow-computations). Constants are bounded game-scale choices, not a physical calibration. No external implementation or assets were copied.
