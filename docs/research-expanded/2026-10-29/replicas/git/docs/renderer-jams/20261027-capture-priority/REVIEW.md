# Expanded replica — docs/renderer-jams/20261027-capture-priority/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-capture-priority/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-capture-priority/REVIEW.md |
| Preserved source SHA-256 | `274a7b4d67cd85a14ac72dd7b6ca9ceb70f38d915c39006207b8d3778db5832a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Changes dirty capture ordering from FIFO to Chebyshev ring and squared-distance priority, retaining insertion ties and an every-eighth successful-capture oldest-first escape. Work remains one capture at a time and authoritative availability is checked without loading chunks. Source ordering checks do not establish visible progress under a real moving-camera workload.

## Research expansion — gaps and details to deepen

- Fairness counts successful captures, so unavailable or repeatedly failing entries need an explicit eligibility policy. A bounded scan of up to 4,096 candidates also has CPU cost that must be included when assessing reduced visible wait.

## Research expansion — proposed contracts

- Define eligible age, retry/failure state, nearest-versus-oldest quota, and a maximum consecutive denial diagnostic. Link the later exact-distance tie repair, and separate discovery backlog from available authoritative capture work.

## Research expansion — acceptance checks

- Run continuous near-camera edits with an old far eligible item, intermittent unavailable entries, and camera movement. Verify bounded work and eventual eligible service; record native capture-to-visible latency and CPU cost rather than only chooser ordering.

## Research expansion — source anchors

- Deep Primal dedicated renderer jam — camera-near dirty capture scheduling
- Implementation and consumed findings

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../GRAPHICS-UPSCALING-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/GRAPHICS-UPSCALING-WORK-STATE.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dedicated renderer jam — camera-near dirty capture scheduling

**Jam:** `20261027-capture-priority`. **Read-only self-audit of exact committed code:** [`e4686b6322cbd0629c3c1759794e9b7d8699a5f7`](https://github.com/Shnewbs/Deep-Primal/commit/e4686b6322cbd0629c3c1759794e9b7d8699a5f7), **source parent** `22e99645fa3f80c5ceadc2429011e65243c14e84`, **claim** `72af48f1c7b4afc3991693a9228ce11b6903414d`. Global mod version remains `0.0.1-alpha.29-dev.39-waterpostwrite.1` because the independent engine builder owns shared version changes. This is the renderer author's self-audit, **not** an independent whole-engine audit. No source code was edited during scoring.

## Implementation and consumed findings

The preceding [LOD upload self-audit](../20261027-lod-upload-forward-progress/REVIEW.md) scored 28/60 and called for useful warm/cold camera, travel and edit coverage, bounded upload timing and native evidence. The latest separate [engine audit](../../engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md) identifies global chemistry-invalidation suppression; this belongs to the engine/water builder, **not** the renderer role. The engine builder acquired its dev40 documentation writer state at `22e99645...` while this run was active. No engine/water files were modified, no other jobs were paused, and no GPU timing reservation was claimed.

Previously [dirty queue admission](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L318-L344) removed the oldest pending key regardless of camera location. With up to 4096 queued edits, new near-camera pages could be delayed behind distant previously discovered chunks. New [`LodCapturePriority`](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L1-L50) selects by nearest Chebyshev chunk ring then squared distance with deterministic insertion-order ties; every eighth **successful capture** instead chooses the oldest entry so persistent far work advances. The [live client-tick integration](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L328-L351) preserves the bounded one-job capture, edit revisions, loaded-only native checks, persisted-disk turn-taking, upload and hard-residency limits. Selection scans the already-bounded dirty queue without sorting or mutating it. It adds O(n) CPU work per capture; no frame/FPS improvement is claimed. Negative-coordinate and extreme int-coordinate computations widen differences to long, avoiding int subtraction overflow. No V1–V7 geography, finite water, saved edits, materials, GPU shaders, water transparency, native backend, FSR/DLSS, textures or game settings were changed.

**Code changed (only):** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java`, `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java`. **Additional docs-only regression fixture:** [`docs/renderer-jams/20261027-capture-priority/LodCapturePriorityChecks.java`](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/docs/renderer-jams/20261027-capture-priority/LodCapturePriorityChecks.java#L1-L102). Source Git blobs: renderer `6e33502970490eb198ed897cfa52bc8c57b8434a`, helper `6bde987ac19417df6b5ff2a41998eb5b96c47652`, fixture `7f0f13420facedd72e5f8f9874a6be5f648dc88d`. All three were re-read from the pushed commit and match the tested helper/fixture content.

## Executed focused checks, matched synthetic cost and limitations

Host: Linux AMD EPYC 9V74; system OpenJDK/javac **21.0.12.1**; owner-provided Java25 archive and full local private Gradle checkout absent in this execution workspace; shell GitHub clone DNS unavailable but GitHub connector provided pinned source. Separate scratch/classes/output, no builder cache or GPU contention.

```sh
javac --release 21 -Xlint:all -d classes source/LodCapturePriority.java tests/LodCapturePriorityChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodCapturePriorityChecks
# PASS LOD_CAPTURE_PRIORITY_PASS assertions=5264 randomQueues=2500 saturatedSelections=256

javac --release 21 -d classes source/LodCapturePriority.java tests/LodCapturePriorityMicrobench.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodCapturePriorityMicrobench
```

Cases include empty queue rejection, camera-near and periodic FIFO fairness, stable ties, negative and extreme signed coordinates, 2500 randomized 1–513-element queues and 256 selections from a 4096-entry saturated queue. The first draft fixture wrongly assumed unique random keys and was corrected before committing; all final checks pass. **Mutation negative:** forcing oldest-first compiled, then exited 1 on the camera-near invariant, as intended; mutant source excluded from Git and archive.

The CPU-only synthetic 15k-warmup/5k-sample comparison for oldest-first vs near-first (respectively) was **512 keys p99 380 ns vs 1,712 ns**, and **4096 keys p99 400 ns vs 18,097 ns**. These are isolated Java lookup-cost measurements, not p99 game frame time, GPU time, real arrival latency or evidence of a performance improvement. Workload scene/seed/resolution/API/VRAM are not applicable to this CPU-only test. No real native camera/travel/edit before/after captures were possible.

Evidence archive `DeepPrimal_Renderer_CapturePriority_20261027_e4686b6_SourceEvidence.zip`: CRC verified; **5021 bytes**, SHA256 `9157d1cd59623ada0e77c4c8ba0eb30e136d6c348f28ebfadade89c2d37736c2`; saved Library `libfile_306fc79818f48191b4df8bf6f80229f5`. Includes tested helper/fixture, benchmark code, positive/negative/CPU logs and identity receipt.

## Six equally weighted renderer scores (read-only author self-audit)

| Category | Prior | Pinned source/test basis; missing evidence | This jam |
|---|---:|---|---:|
| Distance/LOD correctness and usable coverage | 4 | [Existing LOD selection](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L497-L548) unchanged; no native travel/negative seam/edit/unexplored-delivery acceptance | 4 |
| Mesh generation/reuse/scheduling | 6 | [New bounded capture priority](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L18-L47) and [capture integration](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L329-L350), 5264 Java checks; no native throughput or frame gain | 6 |
| GPU culling/submission/memory | 4 | [Upload/submission code](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L609-L757) unchanged; no descriptor/VRAM/GPU timestamp proof | 4 |
| Actual FSR/DLSS integration | 5 | [Upscaling state](../../GRAPHICS-UPSCALING-WORK-STATE.json) records real FSR1 and incomplete DLSS NGX temporal dispatch; unchanged and no RTX acceptance | 5 |
| Visual/backend/settings compatibility | 5 | Queue policy only; no real water transparency, Vulkan/OpenGL pixels, camera/resize/reload or UI setting replay | 5 |
| Validation and measured performance | 4 | [Standalone fixture](https://github.com/Shnewbs/Deep-Primal/blob/e4686b6322cbd0629c3c1759794e9b7d8699a5f7/docs/renderer-jams/20261027-capture-priority/LodCapturePriorityChecks.java#L1-L102), negative mutant and CPU synthetic cost; no Java25 full build, JAR, native frames or real p50/p95/p99 FPS | 4 |
| **Total / average** | **28/60 = 4.67/10** | **Change 0.00; targeted verified correctness, incomplete integration evidence** | **28/60 = 4.67/10** |

The original **independent whole-engine 50/100 (5.0/10)** is carried unchanged, not regraded. Neither quality target strictly greater than 8.5/10 is met.

**Unrun gates:** Java25/Gradle 9.2.1/NeoForge full build, module boundaries, source/packaged class identity, nested JAR CRC and native payload, Windows/Vulkan or OpenGL pixels, AMD8060S/RTX DLSS, actual frame-time/upload/culling/VRAM benchmarks, world-switch/save/reopen/resize/reload/shutdown. Under the October 27 owner-build directive there is **no new JAR, release, GPU acceptance or stable-build claim**.

**Next jam priority:** Measure named-device warm/cold camera/travel/edit p50/p95/p99 and actual dirty queue arrival/rebuild, validate this priority policy's native near/LOD handoff, water ordering and tail spikes; tune only with matched evidence. Follow with oversized-upload part splitting if GPU stalls, and depth/jitter/motion-vector prerequisites for real DLSS without confusing linear/FSR1 spatial scaling with temporal upscaling. Keep the dedicated renderer role enabled; no other schedule changed.


<!-- END PRESERVED SOURCE -->
