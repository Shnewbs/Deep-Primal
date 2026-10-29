# Expanded replica — docs/renderer-jams/20261028-reset-control-cleanup/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-control-cleanup/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-reset-control-cleanup/REVIEW.md |
| Preserved source SHA-256 | `3ee04dcc8f787eb3fd7bd6ab53cbc5c23fb3921834ac998437e97bbf530a4b1a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Contains reset failures from future cancellation, disk-service reset, and executor shutdown so later GPU release and stale-world cleanup still run. Future/worker ownership is revoked before fallible calls; the first error is rethrown with later errors suppressed. The 1,664 model assertions do not establish actual executor termination or GPU device recovery.

## Research expansion — gaps and details to deepen

- Revoking publication authority and requesting cancellation do not prove worker termination. The renderer needs an observable retirement state so old work can release its own resources without mutating a new attachment or leaking untracked threads.

## Research expansion — proposed contracts

- Define reset phases: revoke generation, detach handles, request cancellation, retire workers, release owned resources, clear published state, then propagate aggregated failure. Keep blocked-worker and GPU completion accounting bounded without waiting on the render thread.

## Research expansion — acceptance checks

- Retain the 128 fault-mask model and missing-reset mutation, then inject real disk/worker cancellation failures during world switch. Verify later releases, exact exception identity, no old-world publication, bounded retired workers, and eventual native memory settlement.

## Research expansion — source anchors

- Renderer jam 15 — read-only reset control failure containment self-audit
- Finding and completed implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 15 — read-only reset control failure containment self-audit

**Jam ID:** `renderer-20261028T1854-reset-control-cleanup`. **Pinned production source commit:** [`969abe129b94df7734afe61bc3b5d76f5155bf13`](https://github.com/Shnewbs/Deep-Primal/commit/969abe129b94df7734afe61bc3b5d76f5155bf13), parent `e88904f4f9419a5be1d17e198c9bf46e960eb11b`, shared mod version `0.0.1-alpha.29-dev.60-relief-v2.1`. The code was finalized and checked before this **read-only renderer self-audit**. This does not regrade the independent whole-engine audit, whose prior **5.0/10** remains carried.

## Finding and completed implementation

The [independent 18:30 UTC audit](../../engine-hourly-audits/2026-10-28T1830-7b854ed/REVIEW.md) verified the previous GPU-close aggregation but identified unguarded earlier `future.cancel(true)`, `diskIndex.reset()` and `worker.shutdownNow()` calls. Previously any one of these could interrupt `reset()` before cached terrain buffers were closed and world/cache references were cleared. The [new reset order](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L846-L887) revokes future/worker ownership before invoking those operations through the existing [first-fault/suppressed-fault collector](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java#L11-L27). All cached GPU closes are still attempted and the world state is cleared before the first fault is rethrown. This preserves the original failure rather than silently hiding it.

Only `VoxelLodRenderer.java` changed in production and only this jam's focused test was added. No geometry, shader, water surface/transparency, other engine, graphics menu, native Vulkan, or unrelated schedules were modified. The scoped claim had no active conflicting renderer ownership and was released at the documentation checkpoint. The root `AGENTS.md` was absent; work-state, water state, LOD architecture, renderer backlog, upscaling docs and recent independent review were checked.

## Focused tests and measurements

Actual Linux Java `21.0.12.1` commands in an isolated directory, with repository Git-blob-identity-matched checked-in helper and test:

```sh
javac --release 21 -Xlint:all -Werror -d out \
  source/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java \
  tests/com/shnewbs/deepprimal/lod/client/LodResetControlChecks.java
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodResetControlChecks
# LOD_RESET_CONTROL_PASS assertions=1664 masks=128 throwableKinds=2
```

The [test](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/docs/renderer-jams/20261028-reset-control-cleanup/LodResetControlChecks.java#L15-L76) exhausts 128 injected fault masks for first-fault RuntimeException and Error, seven resource/control actions, all-actions-attempted, cleared simulated ownership, exact first exception identity and deterministic suppressed order. An intentionally defective rig that omits disk-reset invocation strictly compiles and fails on assertion 1 (expected exit 1). A source-level integration check found 14 ordered anchors and verified the old unguarded calls are gone. Pinned source blobs: renderer `ec24272a0486edf105a70dd7b06ace371cfc339a`, helper `9756d3b42b232a90b2c5af4c3686205dd2c48279`, checked-in test `842ac668c872d7a230867fc87d8ae3d779b5a019`.

**Limits:** This is a helper-level isolated fault-injection model plus production-source static integration, **not** compilation of the full `VoxelLodRenderer` against Minecraft or a native graphics-device failure test. Failure from other reset operations (timing probe, collection clear, unexpected `LodDiskIndexService` partial cancellation) is not comprehensively covered. `cancel(false)` and an unresponsive worker remain controlled by the existing session invalidation, not proof of synchronous completion. This change is robustness, not a demonstrated FPS or VRAM gain. No baseline and candidate native frame-time p50/p95/p99 measurements were executable.

## Six equally weighted renderer grades

| Renderer category | /10 | Pinned evidence and limitations |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Disk nearest selection](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L255-L315) unchanged. Unexplored delivery and native distance validation unrun. |
| Mesh generation/reuse/scheduling | **6.85 carried** | [Stable eviction heap](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L27-L76) unchanged; no new throughput evidence. |
| GPU culling/submission/memory | **4.30 (+0.10)** | [Ownership revocation and best-effort cancel/reset/shutdown](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L849-L880). CPU-side exceptional cleanup, not hardware VRAM or indirect submission. |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [Upscaling contract](../../UPSCALING.md): real FSR1 spatial exists; temporal FSR3/4 and production DLSS dispatch not accepted. |
| Visual/backend/settings compatibility | **5.25 carried** | Reset-only [source diff](https://github.com/Shnewbs/Deep-Primal/commit/969abe129b94df7734afe61bc3b5d76f5155bf13); no shader/water changes, but actual Vulkan/GL pixels and reload/reopen unrun. |
| Validation and measured performance | **5.25 carried** | [Java21 checked-in fault model](https://github.com/Shnewbs/Deep-Primal/blob/969abe129b94df7734afe61bc3b5d76f5155bf13/docs/renderer-jams/20261028-reset-control-cleanup/LodResetControlChecks.java) passed 1,664 assertions; mutant rejected and source anchors verified, but no native/GPU performance. |
| **Average** | **5.53/10 (+0.02)** | **33.15/60** versus **5.51** prior; target >8.5 remains unmet and not implied by unit tests. |

**Unrun:** Java25 compiler (no owner JDK archive or restored path in this runtime), complete Java25 Gradle 9.2.1/NeoForge build and artifact (owner-operated), native Windows Vulkan/OpenGL GPU/fault injection, memory/fence/draw/descriptor/camera and water-pixel comparisons, resize/reload/world switching/save/reopen/shutdown, AMD FSR3/4 and NVIDIA RTX DLSS acceptance. No JAR, stable release, CurseForge release, Actions job or vendor validation is claimed.

**Next repair:** native injected worker/disk shutdown failure in a real world-switch and corresponding GPU cleanup/VRAM/transparent-water observations; GPU frame-time p50/p95/p99 and upload/draw baseline before changing submission or claiming render-distance benefit.


<!-- END PRESERVED SOURCE -->
