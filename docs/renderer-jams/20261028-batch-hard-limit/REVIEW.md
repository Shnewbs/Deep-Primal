# Renderer jam 19 — hard-bounded synchronous indexed draw records (read-only self-audit)

**Jam ID:** `renderer-20261028T2300-batch-hard-limit`. **Exact reviewed implementation source:** [`354ddba02f37f5968b3f6abf37665b01ab6df5f9`](https://github.com/Shnewbs/Deep-Primal/commit/354ddba02f37f5968b3f6abf37665b01ab6df5f9). **Parent:** `7a17c4d1b19cd5465ab9015ef9e3ceaffcd56810` (the renderer scoped ownership-claim checkpoint). Review was performed after source/testing completed; no production file was modified during scored inspection. This is a renderer self-audit, **not** a new independent engine audit.

## Consumed findings and changed files

Independent [Dev64 review](../../engine-hourly-audits/2026-10-28T2230-a0d68a1d/REVIEW.md) found that the previous `BoundedIndexedDrawBatches.begin()` returned a mutable raw `ArrayList` whose caller could add more than the declared batch size. The only current renderer caller was already compliant. [Jam18 review](../20261028-bounded-indexed-batches/REVIEW.md) showed bounded API submission but slight isolated CPU-preparation regression; native Vulkan/OpenGL performance and transparent-water pixels remained open.

**Scoped changes:** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java` (production), `docs/renderer-jams/20261028-batch-hard-limit/BoundedHardLimitChecks.java`, `docs/renderer-jams/20261028-batch-hard-limit/{CLAIM.json,README.md,REVIEW.md,RECEIPT.json}` and the renderer-only backlog. **Unchanged:** `VoxelLodRenderer.java`, shaders, water, terrain, upscaling providers, other roles' work-state and schedules.

[Production helper lines 23–40](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java#L23-L40) activates a declared per-call record limit and scrubs on `end()`. [Lines 53–103](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java#L53-L103) enforce that limit and the persistent maximum across indexed/single/bulk insertion, iterator/sublist delegations, inactive writes and `ensureCapacity`. Current [render caller lines 803–822](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L803-L822) still issues `take <= 4096` draws per synchronous `drawMultipleIndexed` API call and always invokes `end()` in `finally`. This is **not** GPU multi-draw indirect.

## Reproduced source-only tests and measurements

Isolated host: Linux OpenJDK **21.0.12.1**. Tested production helper SHA `0ba6b843b105bde21d9d6df8efe069450b42f8a8`; tested committed check SHA `357067d98660a1b1941a69bb1a78b279f7dd2194` (both matched `git hash-object` and pinned GitHub blobs). Commands:

```sh
find source tests baseline -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BoundedHardLimitChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BatchCostCompare
```

**PASS:** 8,068 scenarios / **3,302,181 assertions**, including 131,072 ordered records, exact expected count, 4096 cap, append/positional/bulk/listIterator/subList rejection, resize guard, failed submission cleanup, reuse, reset and randomized parity. Historical unguarded helper compiled under Java21 but the added negative regression failed as intended at **assertion #5** (`hard cap rejects extra append`). Read-only pinned production-to-caller checks: **13/13 PASS**. The full Minecraft module was not compiled.

**Two 301-pair synthetic Java CPU add/clear passes, 1,500 warmups per case**, same process (old→guarded median nanoseconds): 512 items **7,320→7,533** and **7,358→7,564**; 4096 items **17,831→19,100** and **18,549→19,690**; 16384 items **73,101→78,884** and **76,361→78,423**; 65536 items **802,569→799,448** and **819,929→795,401**. Small/medium CPU paths slowed modestly. No FPS, VRAM, driver, GPU frame-time or real indexed draw count gain can be inferred.

## Six equally weighted renderer scores

| Category | Score /10 | Pinned evidence and limitation |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Current native/LOD caller](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L774-L790); no new distant/unexplored or native coverage validation |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Prior constrained splitter](../20261028-split-preflight-once/REVIEW.md); no mesher/scheduler change |
| GPU culling/submission/memory | **4.40 carried** | [Hard list capacity and cleanup](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java#L53-L103), [sync submission](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L803-L822); current caller already complied, no GPU measurements/MDI |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [Upscaling implementation status](../../UPSCALING.md): FSR1 spatial available; temporal FSR and deployed RTX DLSS super resolution still not proven |
| Visual/backend/settings compatibility | **5.25 carried** | Synchronous submission code, water vertex/shaders and settings unchanged; no new GPU visual acceptance |
| Validation and measured performance | **5.45 (+0.05)** | [Committed exact helper check](https://github.com/Shnewbs/Deep-Primal/blob/354ddba02f37f5968b3f6abf37665b01ab6df5f9/docs/renderer-jams/20261028-batch-hard-limit/BoundedHardLimitChecks.java#L22-L144), 3,302,181 assertions, 13 pinned checks, expected negative regression and paired CPU results showing overhead; Java25/native absent |
| **Overall (arithmetic mean)** | **5.59 (+0.01)** | 33.55/60 vs previous 5.58; independent whole-engine 5.0 remains separately carried |

## Unrun gates, defects and next repair

**Unrun:** Java25 toolchain absent in this run (the previously owner-provided archive and directory were not mounted); complete checkout unavailable because GitHub DNS lookup fails; full Java25 Gradle9.2.1/NeoForge and qualified JAR intentionally owner-operated; live Vulkan/OpenGL pixels, CPU/GPU p50/p95/p99, upload/descriptor/VRAM, water order/transparency, reload/resize/world reopen/shutdown, Windows AMD and RTX DLSS, temporal FSR3/FSR4, actual frame-generation independence. The local source-only ZIP passed CRC/manifest SHA256 but is not a JAR.

**Remaining risks:** `ArrayList` remains mutable while an active batch is open; retaining a returned list across a later `begin()` is outside the synchronous API contract. Guard adds some CPU overhead. Hard bounds do not remove underlying driver draw calls, improve horizon coverage or create a real GPU-indirect pipeline. Next renderer item: owner hardware same-scene batched on/off CPU/GPU p50/p95/p99+water pixel tests and targeted CPU-overhead reduction without breaking ordered synchronous cleanup; then actual temporal FSR/DLSS input/provider work.

Scoped renderer ownership released after documentation. No other scheduler, build job, engine/water file or shared GPU reservation touched.
