# Expanded replica — docs/renderer-jams/20261027-part-upload-telemetry/TESTS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-part-upload-telemetry/TESTS.md) |
| Source identity | git: docs/renderer-jams/20261027-part-upload-telemetry/TESTS.md |
| Preserved source SHA-256 | `3c0d0eb4ffc20667f360c4203108809651f8b4b08f5729f3b4bed4a26f79a5da` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds LodGpuUploadProbe observations for accepted-part CPU time and actual payload bytes, separate failure attempts, saturating counters, and reset. It also closes a newly created buffer when later bounds/admission work fails before ownership is appended.

## Research expansion — gaps and details to deepen

- Accepted CPU upload calls are not completed GPU transfers. The ownership interval between allocation and pending append should join the shared lifecycle ledger, while the probe needs frame/part identity and a clear denominator for failed or rejected work.

## Research expansion — proposed contracts

- Define allocation, accepted ownership, returned API result, submission, and completion as distinct events. Preserve the primary exception when cleanup fails, record accepted part-size distribution, and correlate part observations with page commit and retired-memory state.

## Research expansion — acceptance checks

- Retain the focused 40,551-assertion accounting/failure coverage and inject faults after buffer creation but before ownership transfer in the actual backend. Verify no orphaned allocation, accurate attempt counts, and native frame/GPU evidence before performance claims.

## Research expansion — source anchors

- Renderer jam 20261027-part-upload-telemetry — focused test and owner-side native handoff
- Source and test pin

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-part-upload-telemetry — focused test and owner-side native handoff

## Source and test pin

- `main` implementation commit: `a6e24b0d2301a4101262c3912c02467e4bf49af2` (parent/claim `d40cf15b3dd602412229cb78c7cc77bcab53032a`).
- Production helper Git blob: `3320dbc1833aa64796caf8e8bf3e89a89bc0b969`.
- Checked source fixture Git blob: `26b8aeb2413b1cffe0164a8f35e4d4b41adbe791`.
- Existing `RollingTiming` source Git blob: `0be7512957537596295ba204f8f1dfc176aa686f`, copied exactly for independent compilation.
- Integrated `VoxelLodRenderer` Git blob: `5398cc8911224084127d0f784e745e374a3f8ff0`; `LodClient` blob: `244efcc1bef0f87b31d4328500e697931fa52a6f`.

## Exact executed commands (isolated Linux x86_64)

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  stubs/com/shnewbs/deepprimal/core/RollingTiming.java \
  source/com/shnewbs/deepprimal/lod/client/LodGpuUploadProbe.java \
  tests/com/shnewbs/deepprimal/lod/client/LodGpuUploadProbeChecks.java
java -ea -Xverify:all -cp classes \
  com.shnewbs.deepprimal.lod.client.LodGpuUploadProbeChecks
# LOD_GPU_UPLOAD_PROBE_PASS assertions=40551 attempts=77100 accepted=72498 failed=4602 window=240
```

Test coverage: negative elapsed/size/time-budget, zero-capacity and too-large window rejection; strict deadline comparison at exact limit, finite 240-entry rollover, 300 randomized pages and 77,100 attempted parts with independent p50/p95/p99/max reference sorting, failure/success accounting, saturating accepted byte totals, peak size, new/reset state. The 40,551 figure counts fixture assertions, **not GPU submissions**. `RollingTiming` is exact Git source, not a reimplementation.

**Mutation control:** separate source changed the strict time over-budget comparison from `>` to `>=`; Java21 compilation succeeded, but tests exited 1 with `strict greater than time budget expected=2 actual=3`. No mutant was committed.

**Static integration readback:** reviewed exact committed `VoxelLodRenderer` lines 664–697 for optional, TELEMETRY-gated measurements around actual `TerrainGpuMesh.upload`, accepted `p.addGpu`, failure counting and `gpu.close()` on postallocation rejection; reviewed reset at line 813. Reviewed `LodClient` diagnostics exposing bounded CPU duration and part-byte distributions. Full renderer Java25 compilation, actual allocation-failure/GPU readbacks and interrupted device/shutdown behavior were not executed.

## Owner-side executable native acceptance

In a **separate owner checkout**, enable `-Ddeepprimal.lod.telemetry=true`, use an exactly pinned game JAR with these source blobs, and record the LOD diagnostics before/after a fixed-camera warm scene, cold load, 3-way teleport, authoritative edit, water crossing, reload, resize and world switch on Vulkan then OpenGL. Capture resolution, seed/camera, requested/effective render/vertical/LOD/simulation settings, device/API/driver, RAM/VRAM, `lodGpuPartCpuTiming`, `lodGpuPartSizeTiming`, `lodGpuPartOverCpuBudget`, `lodGpuPartFailed`, `lodOversizedFirstUploads`, draw counts, cache bytes/queue plus whole-frame CPU/GPU p50/p95/p99. The probe duration covers synchronous CPU packing/buffer creation/driver submission **not GPU completion**, so separate GPU timestamp/readback/fence metrics remain mandatory.

The owner currently handles Gradle/Minecraft/GPU builds. No new tested Minecraft JAR, SDK/runtime deployment or GPU-performance acceptance is claimed by this run.

<!-- END PRESERVED SOURCE -->
