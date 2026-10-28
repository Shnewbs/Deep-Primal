# Renderer jam 14 — read-only GPU reset cleanup self-audit

**Jam ID:** `renderer-20261028T1800-gpu-reset-failure-cleanup`. **Pinned source commit:** [`96f368e8e212db211376c111a9bef148f9d1f67f`](https://github.com/Shnewbs/Deep-Primal/commit/96f368e8e212db211376c111a9bef148f9d1f67f), parent renderer ownership claim [`a2d8c36b`](https://github.com/Shnewbs/Deep-Primal/commit/a2d8c36bcd6fc9abedb3561535bc9e3d408444ec); version at the source `0.0.1-alpha.29-dev.59-ecotone-query.1`. All production changes were committed **before** this read-only renderer self-audit. This is not the separate independent engine audit. Previous renderer score: **5.48/10**. Independent [Dev59 17:26 UTC engine review](../../engine-hourly-audits/2026-10-28T1726-182cdce/REVIEW.md) retains historic **5.0/10**, not updated here.

| Equal-weight renderer category | Grade /10 | Pinned evidence and explicit limits |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Persisted LOD nearest scan](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L255-L315) unchanged; native unknown/unexplored coverage not established. |
| Mesh generation/reuse/scheduling | **6.85 carried** | [Bounded eviction selector](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L27-L76) unchanged; no fresh mesh or performance baseline. |
| GPU culling/submission/memory | **4.20 (+0.10)** | [`reset()` relinquishes pending ownership and attempts every cached entry](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L844-L874); [first-error/suppressed-cleanup helper](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java#L11-L27). This is fault-path source correctness, **not** measured GPU memory, multi-draw indirect or GPU recovery. |
| Real AMD FSR / NVIDIA DLSS | **5.00 carried** | [Upscaling contract](../../UPSCALING.md): spatial FSR1 implemented; temporal FSR3/4 and production DLSS Super Resolution still lack working native providers. No new credit. |
| Visual/backend/settings compatibility | **5.25 carried** | [Vulkan/GL-independent LOD reset](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L832-L875) only. Water geometry, shaders and UI unchanged; actual pixels, reload, resize and switch unrun. |
| Validation and measured performance | **5.25 (+0.05)** | [Exact production helper](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java#L1-L28) and [checked-in harness](https://github.com/Shnewbs/Deep-Primal/blob/96f368e8e212db211376c111a9bef148f9d1f67f/docs/renderer-jams/20261028-reset-gpu-cleanup/LodGpuResetCleanupChecks.java#L15-L86) compiled strictly under Java21; **44,444 assertions pass** and release-skipping mutation rejected. No native GPU/frame-time or Java25/Gradle data. |
| **Average** | **33.05/60 = 5.51 (+0.03)** | Target >8.5 not met; previous 5.48. Unchanged categories explicitly carried. |

## Read-only review observations

Before this change, [`reset()` at previous source](https://github.com/Shnewbs/Deep-Primal/blob/829348829ab4a5d47327e227579871f7378672ab/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L844-L860) called `pendingGpu.close()` **before** clearing `pendingGpu`, then used fail-fast `cache.values().forEach(Entry::close)`. A pending buffer close failure could abort the entire reset, leave stale owners and skip every other cached GPU buffer close. An early cached-entry failure also skipped later cache entries and state clearing.

New order: move pending ownership to a local before any fallible close; aggregate the first RuntimeException/Error while still attempting every cached Entry; clear world/cache/heap/transform state, then rethrow the first failure with subsequent failures suppressed. Existing per-entry `closeGpuMeshes` continues attempting every inner buffer. This does **not** catch failures from earlier timing/disk index reset, future cancellation or executor shutdown; does not recover a lost graphics device; and cannot prove native rendering or driver-side release correctness.

**Exactly verified commands** using isolated local copies whose Git blobs equal committed helper `9756d3b42b232a90b2c5af4c3686205dd2c48279` and checked-in checks `b6ec2096453ea7de518e002c60805849f83c8644`:

```sh
javac --release 21 -Xlint:all -Werror -d out \
  src/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanup.java \
  tests/com/shnewbs/deepprimal/lod/client/LodGpuResetCleanupChecks.java
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodGpuResetCleanupChecks
# LOD_GPU_RESET_CLEANUP_PASS assertions=44444
```

**Negative control:** in a separate copy replace `closer.run();` with an empty operation; strict compilation passes, assertions fail at **#1**, as intended. Source integration was also statically checked on the pinned full renderer: pending=null precedes close, every cached Entry release precedes cache.clear, and failure rethrow follows level=null. Test API doubles do not compile or execute the full Minecraft class.

**Measured before/after:** no executable GPU/real Minecraft/reset-failure performance baseline; no invented FPS, VRAM, upload or frame-time tail measurements. This is an exceptional-path robustness change, not an optimized hot path.

**Unrun gates:** Java25 toolchain (not present this runtime), full Gradle9.2.1/NeoForge/mod compile (owner-operated; no full checkout/dependencies), client Vulkan/OpenGL failure injection, resource reload/resize/world-switch/logout/shutdown, save/reopen, water transparency readbacks, GPU fences/VRAM and p50/p95/p99, FSR3/4, NVIDIA RTX DLSS, packaged JAR and release. No qualified JAR is claimed. **Next repair:** owner-native injected GPU close/upload failures and matched frame/GPU-memory baselines; investigate rare non-close reset exceptions separately before advertising recovery.
