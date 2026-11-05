# Expanded replica — docs/renderer-jams/20261029-visible-snapshot-reuse/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-visible-snapshot-reuse/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-visible-snapshot-reuse/REVIEW.md |
| Preserved source SHA-256 | `ed83932155618349b3ad5b819c37bcab0e72b4d5b7867b4a88689346e309c47f` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Audits final implementation 74d7447 after the oversized-collector repair, preserving source/test identities and distinct author versus independent grades. Two strict Java21 runs each pass 69,984 assertions; a mutable-publication mutant fails. The warmed extraction fixture reduces thread allocation by about 68.3%, while immutable snapshots still allocate.

## Research expansion — gaps and details to deepen

- The test executes the helper and inspects renderer insertion points; it does not exercise the full Minecraft extraction lifecycle. Randomized ordinary frames and the outlier regression need native retention and failure evidence before extrapolating allocation savings to heap, GC, frame latency, or GPU behavior.

## Research expansion — proposed contracts

- Add an evidence receipt that separates helper execution, renderer source inspection, exact-artifact compilation, and native exercise. Bind immutable snapshot consumers to their frame/world generation, and record collector retained capacity and outlier discard independently from published-snapshot allocations.

## Research expansion — acceptance checks

- Retain the mutable-alias negative control and ordered duplicate parity, then inject actual extraction/publication faults and repeated world switches. Compare allocation and retained heap across steady and alternating oversized visibility sets, with same-scene cull/CPU/GPU tails and water pixels. Preserve the source-only limit and original historical grades.

## Research expansion — source anchors

- Exact changes and evidence
- Exact tests, comparative allocations and limitations
- Explicitly unrun and next repair

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 23 — read-only self-audit: reused visibility extraction scratch

**Jam ID** `renderer-20261029T0300-visible-snapshot-reuse`. **Final exact reviewed implementation** [`74d7447d3206952f0aa5be6003212dea9e9f7ce4`](https://github.com/Shnewbs/Deep-Primal/commit/74d7447d3206952f0aa5be6003212dea9e9f7ce4); first implementation [`da8d719`](https://github.com/Shnewbs/Deep-Primal/commit/da8d71977762831c37fd2ba3121e23d7510730ff), original renderer-only ownership claim parent `fb1e2f8bd40fb0d75b5345661143e3c43bb7ecde`, baseline `e6fcfc1892d607a2a122d25ba98e9a103fd1ce0c`. Concurrent Dev69 terrain-only commit was preserved by the guarded new-parent update. The existing `gradle.properties` at implementation identified **0.0.1-alpha.29-dev.68-v8-routed-regions.1**; no version bump or JAR is claimed.

This is the **renderer jam's author self-audit**, performed read-only **after** the oversized-scratch repair was committed. It is not an independent engine audit. Consumed [jam22 findings](../20261029-pretransform-failures/REVIEW.md) and the [independent Dev68 review](../../engine-hourly-audits/2026-10-28T1928-2c672857/REVIEW.md). Read current main, root and water work-state, LOD/drainage and upscaling policies, latest renderer backlog, recent commits, other owners' claims and reservation status. Root `AGENTS.md` was not found. No terrain, water chemistry, graphics shader, FSR/DLSS provider, mod version, other automation or active independent audit scope was edited.

## Exact changes and evidence

- [`VoxelLodRenderer.java` 73–78, 525–579, 936–940](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L525-L579), Git blob `6d5bb1e0f84698ccb02f0ba128576bb622edcf0f`: existing per-extract `new ArrayList` replaced by a reusable collector. The render-state still receives a detached immutable snapshot, in the same culling, selection and material order. The `finally` block clears mesh references even on extraction/render-state errors; world reset drops the retained array.
- [`LodVisibleSnapshot.java` lines 12–47](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodVisibleSnapshot.java#L12-L47), Git blob `eefd98ddc7ab8f1227291de5d8f7261e4165f3c1`: render-thread-only collector, no nested extraction, immutable copy, try/finally cleanup, and **32,768-record oversized-frame retention ceiling**. After an exceptionally large extraction, the next frame receives a fresh collector rather than retaining an enlarged array. This was a pre-audit safety repair; no scored code was changed afterward.
- [`LodVisibleSnapshotChecks.java` lines 59–125](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/docs/renderer-jams/20261029-visible-snapshot-reuse/LodVisibleSnapshotChecks.java#L59-L125), Git blob `e9453aa0dabe066bc44ee4eefb946eab1c8cfb6a`: 10,000 randomized 0–512-item source-model frames, duplicate/order parity, immutable detached snapshots, nested-extraction rejection, null failure, finally cleanup, normal reuse, oversized discard and world reset. Readme is production-excluded.
- Implementation commits changed exactly `VoxelLodRenderer.java`, the new helper, the scoped test, and the scoped README; the follow-up changed only the helper/test. No mesh vertex/index attribute, UV/lighting, shader, water ordering or GPU API command changed.

## Exact tests, comparative allocations and limitations

System OpenJDK **21.0.12.1**; the previously owner-provided JDK25 folder and archive are **absent in this execution workspace**. The executed helper/test Git blobs matched their final source commit identities exactly. Commands (from the isolated source directory):

```sh
javac --release 21 -Xlint:all -Werror -d classes src/com/shnewbs/deepprimal/lod/client/LodVisibleSnapshot.java src/com/shnewbs/deepprimal/lod/client/LodVisibleSnapshotChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodVisibleSnapshotChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodVisibleSnapshotChecks
```

**PASS**: two executions, **69,984 assertions each**, 10,000 seeded frames representing 2,561,036 visible mesh references per run. Original simple extractor and candidate published identical ordered snapshots including duplicates. Explicit memory ownership guards pass after ordinary, failed, null and oversize extractions. **PASS negative:** compile a deliberately mutated helper that returns the original mutable scratch instead of `List.copyOf`; runtime correctly fails at **assertion 4**, exit 1. Exact final helper/test git blob checks pass; renderer insertion points, finally/reset and unchanged GPU code were inspected in the pinned commit.

**Paired same-process Linux Java21 thread-allocation fixture:** after warm-up, 10,000 frames with 256 stable parts each: old per-frame list allocation **66,480,000 bytes**, candidate reused collector **21,044,568 bytes**, a **45,435,432-byte reduction** (about 68.3%) on this *isolated synthetic extraction loop*, repeated identically twice. The immutable snapshot still allocates. No heap/GC p99, native Minecraft CPU/GPU frame p50/p95/p99, FPS, GPU VRAM or gameplay gain is inferred.

**Archive:** `DeepPrimal_Renderer_VisibleSnapshotReuse_20261029_74d7447_SourceEvidence.zip`, **5,512 bytes**, SHA256 `3415c4ae783587a647cad5b9d66f2c929d38971cce03de01b23244d691c48792`, nine ZIP members; outer CRC and each internal SHA256 manifest entry verified. Uploaded successfully to Library `/Deep Primal/Renderer Jams/`. This ZIP includes exact helper/test, tests, mutant results, source identities and manifest; it is **not a compiled mod or JAR**.

## Six equal-weight renderer grades (read-only at exact source)

| Category | Score /10 | Pinned evidence and missing proof |
|---|---:|---|
| Distance, LOD correctness and usable coverage | **6.50 carried** | [Existing hysteretic selection/near-native handoff](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L533-L564) unchanged; no new unseen-terrain or distant GPU coverage proof |
| Mesh generation, reuse and scheduling | **6.95 carried** | [Prior splitter jam evidence](../20261028-split-preflight-once/REVIEW.md); no mesh algorithm/worker source changed this jam |
| GPU culling, submission and memory | **4.50 (+0.05)** | [Transient visibility collector reuse and finally scrub](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L525-L579), [32K outlier cap](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodVisibleSnapshot.java#L31-L43); CPU allocations only, no GPU MDI or VRAM evidence |
| Actual AMD FSR / NVIDIA DLSS integration | **5.00 carried** | [Official FSR1 spatial present; temporal FSR/active RTX NGX dispatch incomplete](../../UPSCALING.md) |
| Visual, backend and settings compatibility | **5.25 carried** | [Immutable render data handoff](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L575-L579); preserved pipeline ordering, but no native Vulkan/GL water pixels or lifecycle acceptance |
| Validation and measured performance | **5.65 (+0.05)** | [Exact committed 69,984-assertion regression](https://github.com/Shnewbs/Deep-Primal/blob/74d7447d3206952f0aa5be6003212dea9e9f7ce4/docs/renderer-jams/20261029-visible-snapshot-reuse/LodVisibleSnapshotChecks.java#L59-L125), deliberately broken mutant rejection, matched allocation fixture; no real frame-time gain |
| **Equal-weight mean** | **5.64 (+0.01 displayed)** | `(6.50+6.95+4.50+5.00+5.25+5.65)/6=5.6417`; previous renderer **5.63**, independent engine **5.0** remains unchanged |

## Explicitly unrun and next repair

**Not run:** JDK25 compilation (toolchain missing), complete Java25 Gradle 9.2.1/NeoForge build, packaged client/Vulkan/OpenGL, transparent-water pixels/ordering, resource reload/resize/world-switch/save/reopen/shutdown, actual GPU fences/failures, CPU/GPU whole-frame p50/p95/p99, streaming/descriptor/draw counts, measured RAM/VRAM, hardware RTX DLSS or temporal FSR, native platform payload/JAR qualification. All full builds remain **owner-operated** under the October 27 override. No JAR, GitHub release, CurseForge upload, GPU FPS increase or owner-hardware acceptance is claimed.

**Next:** owner native same-scene Vulkan and GL extraction/LOD/transparent-water test, compare Java allocation and cull CPU p50/p95/p99 with prior baseline, then hardware frame/gpu/VRAM; on evidence optimize real GPU submission and temporal upscaler inputs without altering water simulation. Keep this schedule independent from all engine/water/terrain work.


<!-- END PRESERVED SOURCE -->
