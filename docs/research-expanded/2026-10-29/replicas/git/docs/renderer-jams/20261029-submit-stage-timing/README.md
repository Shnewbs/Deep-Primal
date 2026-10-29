# Expanded replica — docs/renderer-jams/20261029-submit-stage-timing/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-submit-stage-timing/README.md) |
| Source identity | git: docs/renderer-jams/20261029-submit-stage-timing/README.md |
| Preserved source SHA-256 | `6c071674697f6c0be77fccce58985e9936f28ce0f28fb1927a2b66620c405dfc` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds bounded 240-observation CPU transform, draw-API/close, and combined submit-stage histories, including failures after transform writing and successful returned work before a later failure. Telemetry is opt-in, saturating, and reset with world state.

## Research expansion — gaps and details to deepen

- Preparation failures were omitted at this historical pin and are covered by the later pretransform repair. Independent stage percentiles still lack frame correlation and exclude empty/culling work; API return cannot establish GPU completion or hardware draw count.

## Research expansion — proposed contracts

- Link the pretransform successor and define sample populations, phase boundaries, partial-return counts, and unavailable states. Add frame/world/device identifiers and separate asynchronous GPU timestamps; do not add unrelated percentile values to estimate a frame percentile.

## Research expansion — acceptance checks

- Retain 20,000 scenarios and 29,276 probe assertions, then capture exact-artifact batched/direct paths with failures and no-visible frames. Compare telemetry-on/off overhead, actual GPU/frame distributions, memory, and water images before choosing a submission optimization.

## Research expansion — source anchors

- Renderer jam 21 — synchronous LOD submit-stage CPU telemetry

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 21 — synchronous LOD submit-stage CPU telemetry

This is opt-in source-only renderer instrumentation. It does not measure GPU fence
completion, Vulkan hardware draw calls, actual frame time or FPS.

With `-Ddeepprimal.lod.telemetry=true` the LOD diagnostics now distinguish
transform array preparation/write CPU time, render-pass creation/record/close CPU
time and total CPU time, each with bounded 240-observation p50/p95/p99.
Unlike the older success-only submit timing, these stage observations include
exceptions occurring **after** transform writing. Earlier transform failures and
frames without any visible LOD remain outside the sample. Returned record and
API-call counts include successful calls before a failing pass; no GPU success
is inferred from API return. Native/Vulkan/GL completion remains owner-run.

Standalone validation (not full Minecraft/module compilation):

```sh
find src tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSubmissionProbeChecks
```

The class needs the unchanged `engine-core/.../RollingTiming.java`.
The Java21 isolated probe regression passed 20,000 scenarios and 29,276
assertions. This probes pure-Java percentile/accounting logic, not live
Vulkan/OpenGL or GpuLighting passes.

Implementation scope: `LodSubmissionProbe.java`, `VoxelLodRenderer.java`,
`LodClient.java`, this jam's source check and notes. No water/terrain/shader
or other automation file changes.


<!-- END PRESERVED SOURCE -->
