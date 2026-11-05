# Expanded replica — docs/renderer-jams/20261028-triangle-counter/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-triangle-counter/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-triangle-counter/REVIEW.md |
| Preserved source SHA-256 | `38d892f4a4c13b6e5dafcf6c3d74bd20cdc5c46cb372aff7e9bcb5257808d89c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Uses a pass-local triangle accumulator and one final volatile publication, crediting a batch or individual draw only after its API returns. Reset clears per-world metrics and partial success is retained when a later call fails. The 25,130 assertions and CPU model are not a native draw-call or GPU-performance measurement.

## Research expansion — gaps and details to deepen

- Returned API work is not completed GPU execution. The model test and seven source anchors need an integrated correlation with real draw/submit/fence events, especially when an API raises after internally recording partial work.

## Research expansion — proposed contracts

- Label counters as returned-call triangles and retain separate attempted, returned, submitted, and GPU-completed populations where observable. Attach pass/frame/world identity and document conservative partial-failure counting without inventing inaccessible driver results.

## Research expansion — acceptance checks

- Retain the 4,796-scenario failure/reset model, then inject real native draw and pass-close failures. Verify diagnostic consistency, no stale-world counts, unchanged batching/order, and identical water pixels; benchmark complete frames before claiming reduced volatile writes improve FPS.

## Research expansion — source anchors

- Renderer jam 20 — read-only self-audit of pinned source
- Consumed findings and changed files

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20 — read-only self-audit of pinned source

**Jam ID:** `renderer-20261028T2358-triangle-counter`  
**Source implementation commit:** [`61dc92eb52b030fa19bfd3572ac12661a3bc4579`](https://github.com/Shnewbs/Deep-Primal/commit/61dc92eb52b030fa19bfd3572ac12661a3bc4579)  
**Implementation parent:** `d629e74036c4a494cb0faea7520fb86057bc9df0` (renderer claim). **Original current-main snapshot:** `a564df3c1ae74c7332770e65c01d2beede571a59` (Dev65 independent audit).  
**Version:** existing `0.0.1-alpha.29-dev.65-authoring-guard.1`; renderer-only source checkpoint, no build/version increment.

## Consumed findings and changed files

Consumed [independent Dev65 audit](../../engine-hourly-audits/2026-10-28T2331-d99837b/REVIEW.md), confirming corrected jam19 hard batch limits and continuing owner-GPU qualification gap; [renderer jam19 self-audit](../20261028-batch-hard-limit/REVIEW.md) measured CPU overhead and no GPU performance proof. The original `V1–V7` geography, mesh topologies, GPU resources, FSR providers, shaders and water transparency are unchanged. No other automation or GPU reservation was touched.

Production changed **only** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java`, verified Git blob `7e92858d7fead77d7efc0c295581f0caf0a9b4d9`. [Lines 790–839](https://github.com/Shnewbs/Deep-Primal/blob/61dc92eb52b030fa19bfd3572ac12661a3bc4579/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L790-L839) use a primitive pass-local accumulator, adding each batch only after `drawMultipleIndexed` returns and each individual part only after `draw` returns, then publishing in an outer `finally` even if a later operation throws. [Reset lines 859–877](https://github.com/Shnewbs/Deep-Primal/blob/61dc92eb52b030fa19bfd3572ac12661a3bc4579/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L859-L877) now clear per-world triangle metrics. One volatile write per render invocation replaces per-part volatile writes. These counts reflect API return, **not GPU execution or submitted GPU multi-draw indirect**. Existing 4,096-record guarded batches and their mandatory cleanup remain.

## Focused execution (actual, isolated Java21)

Linux OpenJDK 21.0.12.1 (not Java25). Reproducible tests and logs in Library: `/Deep Primal/Renderer Jams/DeepPrimal_Renderer_TriangleCounter_20261028_61dc92e_SourceEvidence.zip`.

```sh
javac --release 21 -Xlint:all -Werror -d classes tests/DrawTriangleAccountingChecks.java
java -ea -Xverify:all -cp classes DrawTriangleAccountingChecks
javac --release 21 -Xlint:all -Werror -d classes tests/DrawTriangleCounterBench.java
java -ea -Xverify:all -cp classes DrawTriangleCounterBench
java -ea -Xverify:all -cp classes DrawTriangleCounterBench
```

**PASS:** 4,796 isolated fault/size/batching/reset scenarios and **25,130 assertions**. This is a model of production submission ordering, not a full-module test or mock GPU dispatch. A deliberately defective model crediting the batch before forced submission error compiled but correctly **failed at assertion 133**. Seven pinned production-source static checks PASS, including order, one volatile publish, reset and unchanged 4,096 cap. After-code review inspected immutable commit; no code was changed during scoring.

Two 501-pair, 3,000-warmup Java CPU *counter-only* tests with alternating baseline/candidate call order (old/new median nanoseconds):
- 4,096 count operations: `7,461/320` and `7,150/320`.
- 16,384 count operations: `34,812/1,112` and `34,743/991`.
- 65,536 operations: `138,629/3,656` and `138,669/3,686`.

The synthetic loop excludes indexing, render passes, GPU submission, draw preparation, shader work and frame pacing; it only measures the impact of per-part volatile counter increments. **No FPS/frame-time or Vulkan/OpenGL improvement is demonstrated.** No matched GPU/VRAM baseline exists.

## Renderer-only six-category rubric (equal weight)

| Category | /10 | Pinned evidence and qualification |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Existing LOD selection](https://github.com/Shnewbs/Deep-Primal/blob/61dc92eb52b030fa19bfd3572ac12661a3bc4579/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L776-L790); no new effective-distance acceptance |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Earlier optional constrained splitter](../20261028-split-preflight-once/REVIEW.md); no mesh change |
| GPU culling/submission/memory | **4.45 (+0.05)** | [Exact source render call ordering](https://github.com/Shnewbs/Deep-Primal/blob/61dc92eb52b030fa19bfd3572ac12661a3bc4579/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L803-L839) removes per-part volatile writes but lacks real GPU timings or multi-draw indirect |
| Real FSR/DLSS integration | **5.00 carried** | [FSR1 and inactive DLSS contract](../../UPSCALING.md); temporal FSR and RTX DLSS dispatch unproven |
| Visual/backend/settings compatibility | **5.25 carried** | Source diff restricted to counters/reset; no water shader/camera geometry change, no new Vulkan/OpenGL pixels |
| Validation and measured performance | **5.50 (+0.05)** | Isolated Java21 25,130 model assertions, expected negative fault test, 7 pinned static assertions, counter-only paired CPU trials; no native/GPU/frame evidence |
| **Mean** | **5.61 (+0.02)** | `33.65/6 = 5.6083`; previous 5.59. **Renderer self-audit**, not independent whole-engine grade (historical 5.0 remains untouched). |

## Missing gates / defects / next repair

- No Java25 toolchain or owner-provided archive was mounted in this run; `java`/`javac` verified as Java 21. No complete checkout because host `github.com` DNS resolution failed; exact source operated through the GitHub connector. Full Java25 Gradle9.2.1, NeoForge/Minecraft, native JAR, packaging/CRC/class-source identity, and hosted Actions intentionally **not run** under the October 27 owner-build override.
- No Windows/RTX DLSS, Vulkan/OpenGL device admission, actual GPU pixel readbacks, water transparency/order, resource reload/resize/world switch/reopen/shutdown, upload fault injection, CPU/GPU frame-time p50/p95/p99, draw/descriptor counts, VRAM/RAM or 125/192/512 distance tests. The CPU-only benchmark cannot establish those.
- Recorded metrics now count API-returned draws; driver/GPU-fence execution remains unknown. CPU model does not prove a complete Minecraft module. Test the native error and reset paths to exclude frame-lifecycle issues.
- **Next:** owner same-scene Vulkan/OpenGL batched A/B and counter instrumentation, full CPU/GPU p50/p95/p99, VRAM, water pixels and resource lifecycle; then real GPU submission optimization/temporal FSR/DLSS prerequisites.

The source-only ZIP is **4,976 bytes**, CRC and eight-entry SHA256 manifest verified, SHA256 `bed78d08b98c8a9052ec205be0c0fa1efec90a25be09771fecd1f274ec86de58`, uploaded successfully to Library. Not a JAR or a release. Scope released at completion.


<!-- END PRESERVED SOURCE -->
