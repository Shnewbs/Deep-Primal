# Renderer jam 24 — squared-distance LOD screen-error selection

Jam ID: `renderer-20261029T0356-squared-error-selection`. Renderer scope only; source-only owner-build handoff.

**Implementation:** `LodScreenErrorSelector` precomputes the two upshift and two
downshift hysteresis thresholds once per camera extraction. Ordinary cached
LOD pages compare squared horizontal distances instead of evaluating
`Math.hypot` per page; within a conservative 1e-10 relative squared-boundary
window (or for invalid/non-finite inputs), the exact previous hypot/division
selection code runs. Camera position, previous selected level, near/native
compiled-section handoff, material order, water transparency and draw pipeline
are unchanged. `LodConfig.ERROR` is captured once per extraction.

**Focused pure-Java checks:** strict OpenJDK 21 compilation and two seeded
runs at exact candidate helper/test blobs, each **1,556,976 parity assertions**
covering 1–12 error pixels, six focal heights, three previous levels,
random signed travel coordinates, 401-sided threshold sweeps and abnormal
values. A deliberately inverted squared-range comparison compiled and failed
at assertion #1 as expected. This is not a native Minecraft or GPU test.

```sh
javac --release 21 -Xlint:all -Werror -d classes src/LodScreenErrorSelector.java src/LodScreenErrorSelectorChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodScreenErrorSelectorChecks
```

Paired Linux Java21 CPU-only 4,096-page, 301-sample medians:
baseline `65,734 ns` versus candidate `42,230 ns` in run 1,
baseline `60,404 ns` versus candidate `45,656 ns` in run 2.
These measure the selector alone, not total frame cost or real GPU FPS.
P99/GPU/VRAM/actual renderer allocation unmeasured.

**Owner gates still open:** full Java25/Gradle/NeoForge/JAR, native Vulkan and
OpenGL game frame p50/p95/p99, water transparency and native handoff pixels,
frozen profile playback/save/reopen/reload/world switch/shutdown, FSR temporal
and RTX NGX DLSS dispatch. No change to other feature schedules or outputs.
