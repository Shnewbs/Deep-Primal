# Expanded replica — docs/renderer-jams/20261029-squared-error-selection/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-squared-error-selection/README.md) |
| Source identity | git: docs/renderer-jams/20261029-squared-error-selection/README.md |
| Preserved source SHA-256 | `e2e91a94717918e888fe62e9b3c706650a4eea49cf83c2bbcecbef074e386d09` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Precomputes four hysteresis thresholds once per extraction and compares squared horizontal range, using the previous hypot/division selector near a 1e-10 relative squared-boundary window or invalid/nonfinite inputs. Error settings are snapshotted once; camera, previous level, and native handoff are preserved.

## Research expansion — gaps and details to deepen

- Exact parity depends on the configured error, projection inputs, threshold fallback, and prior selected level being coherent for the extraction. The subsequent scene-pixel change alters projection policy while keeping this selector, so the two changes need distinct evidence boundaries.

## Research expansion — proposed contracts

- Specify selector-input lifetime, numeric fallback domain, hysteresis state ownership, and invalid-input behavior. Link scene-pixel projection as a later policy change; count fallback frequency and per-extraction setup separately from per-page comparison cost.

## Research expansion — acceptance checks

- Retain all four threshold sweeps, abnormal inputs, signed offsets, and inverted-comparison rejection. Exercise real FOV/extent/error changes and camera travel on native Vulkan/OpenGL, comparing exact selections, transitions, water images, and full-frame tails before generalizing the component median improvement.

## Research expansion — source anchors

- Renderer jam 24 — squared-distance LOD screen-error selection

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
