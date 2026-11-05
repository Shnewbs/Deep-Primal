# Renderer jam 17 — best-effort cache eviction under GPU-close faults (read-only self-audit)

**Jam ID:** `renderer-20261028T2055-eviction-failure-recovery`. **Exact reviewed implementation commit:** [`5eb5c8961ad778a28036416f498387c988fb865e`](https://github.com/Shnewbs/Deep-Primal/commit/5eb5c8961ad778a28036416f498387c988fb865e), claim `2b588b11fc0e16a642e81cde905f199357981cbc`, prior main `f67411f96b75fcc1632cae4d55e2814a299fa1e0`. Shared version `0.0.1-alpha.29-dev.62-coastal-proof.1` unchanged. This is the **renderer author's read-only review after completing code and running checks**, not the independent engine auditor's grade or an RTX/driver test. No code was edited during this scored review.

## Prior audit consumed and changes

The independent [Dev62 20:34 UTC review](../../engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md) preserved the prior LOD split correctness on source-shaped Java21 tests but explicitly kept native GPU, LOD/water pixels, frame-time p99, and Java25/Gradle gates open. The previous [renderer jam 16](../20261028-split-preflight-once/REVIEW.md) showed CPU-only opt-in splitter gains with mixed p99 and no default-path GPU win. We chose a narrow **default cache-pressure correctness repair** instead of inflating any performance/FSR/LOD coverage claim.

Pinned [`VoxelLodRenderer.evictToFit` lines 585–620](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L585-L620) now aggregates `Entry.close()` failures with `LodGpuResetCleanup.release` at line 602, then continues old persisted-page admission (605–616), stale-key cleanup (617), and remaining eviction (597–618). It throws only at 619 after finishing required bookkeeping. The [existing helper](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java#L1-L29) had its Javadoc clarified for cache eviction; helper semantics and suppression order are unchanged. The committed production-excluded [fault smoke](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/docs/renderer-jams/20261028-eviction-failure-recovery/LodEvictionFailureSmoke.java#L1-L140) covers bounded simulated victim removal, persistent recoveries, resident byte accounting, first/follow-on exceptions and Errors, exact once-only closes, and same-Throwable self-suppression avoidance.

**Actually changed files:** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java`, `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java`, `docs/renderer-jams/20261028-eviction-failure-recovery/LodEvictionFailureSmoke.java`. No terrain generation, water chemistry/simulation, water shaders/order, FSR/DLSS, graphics UI, Gradle, scheduled jobs, stored profiles or release assets were edited.

## Verification: isolated Linux OpenJDK 21.0.12.1

```sh
find source tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodEvictionFailureSmoke
# LOD_EVICTION_FAILURE_SMOKE_PASS scenarios=6000 assertions=190949
```

Exact compiled local helper and smoke source hashes equal committed blobs `9a609d38c359eb39f8e3cfe18b5fa79b6c65b341` and `ca5c12c24fbeee000c85ff83666bc4a9b2010a8e`. Final `VoxelLodRenderer` blob `a135047e34c09846a2c7955056f778ac232aa38d` passed ten ordered Git-pinned source integration assertions. The *historical direct-close behavior* was reproduced by replacing `failures.release(page::close)` with `page.close()` in the isolated model, compiling with the same strict flags: exit 1 at assertion #1 (`eviction target must remain bounded`); the candidate passed 190,949 assertions. This is correctness/fault behavior, **not a matched FPS, frame-time, or VRAM benchmark**.

**Limits:** The compiled smoke exercises the **real committed cleanup helper but a pure-Java model** of `evictToFit` (not NeoForge or real GPU buffers). Ordered source checks do not constitute whole-project compilation or device-loss resilience. Additional unhandled failures in recovery/disk bookkeeping could still interrupt eviction. No JDK25 exists here: provided archive and restored path were checked and absent; system Java21 verified. No complete private checkout is mounted. Owner override explicitly assigns full build/JAR/GPU qualification to the owner, so no GitHub Actions/full build/JAR was attempted.

## Six equally weighted renderer categories

| Category | /10 | Exact pinned evidence and limits |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Existing native-handoff/selection](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L507-L564); native saved/teleport/edit/unexplored coverage not run |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Opt-in prior splitter](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java#L42-L85), no new mesh optimization or GPU measurements |
| GPU culling/submission/memory | **4.35 (+0.05)** | [Cache-pressure close/recovery continuation](https://github.com/Shnewbs/Deep-Primal/blob/5eb5c8961ad778a28036416f498387c988fb865e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L592-L620); source/Java fault model only, no actual GPU fault/draw/VRAM |
| Actual FSR / DLSS implementation | **5.00 carried** | [Existing upscaling state](../../UPSCALING.md): spatial FSR1 active, temporal FSR3/4 and RTX DLSS end-to-end unavailable |
| Visual/backend/settings compatibility | **5.25 carried** | No pipeline/material/alpha change; no Vulkan/GL water-pixel/resize validation |
| Validation and measured performance | **5.35 (+0.05)** | [Committed smoke](LodEvictionFailureSmoke.java) 6,000 model fault sessions/190,949 assertions, negative baseline fails, source order verified; no Java25, full module build, hardware frame p50/p95/p99, VRAM |
| **Arithmetic average** | **5.57 (+0.02)** | **33.40/60**, previous renderer **5.55**. Independent whole-engine grade remains **5.0/10 carried**, not changed. |

## Unrun gates and next scoped priority

Unrun: Java25 `./gradlew`/module compilation, archive CRC/packaging/native dependencies, Windows/AMD/NVIDIA GPU, actual Vulkan/OpenGL close failure injection, water transparency pixels, resource reload, resize, save/reopen/world-switch/shutdown, CPU/GPU frame p50/p95/p99, VRAM and draw/descriptor counts, actual FSR3/4 temporal or RTX DLSS dispatch. This source fix makes no playable-distance or FPS claim and produces **no new JAR**.

**Next:** owner native device/driver failure injection at LOD pressure with persistent pages, then same-seed camera travel/edit/reopen Vulkan/OpenGL p50/p95/p99, upload/VRAM and water-pixel comparisons. Use evidence to choose subsequent real GPU submission/streaming change; do not re-enable optional 32KiB split by default without accepted A/B.
