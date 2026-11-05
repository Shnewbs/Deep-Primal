# Deep Primal renderer jam — indivisible LOD upload forward progress (read-only self-audit)

**Jam:** `20261027-lod-upload-forward-progress`. **Pinned production code:** [`daaeabb1a60c970e23d396af34a359eac60cf87e`](https://github.com/Shnewbs/Deep-Primal/commit/daaeabb1a60c970e23d396af34a359eac60cf87e); **code parent/scoped claim:** `698bc2c69b889a88e42d10011bf8372f3dd536ab`; claim source parent `579516aba72bdb96006cdfabfd50837887e86c22`. **Common version:** `0.0.1-alpha.29-dev.38-guarddominance.1` (shared Gradle version unchanged to avoid the separately active engine builder's ownership). This is the **renderer author's read-only self-audit** after code commit, not the external engine audit. No production edits during this scored review.

## Consumed previous findings and actual repair

Previous renderer [transform-telemetry self-audit](../20261027-transform-telemetry/REVIEW.md) scored 28/60 and identified missing real camera/travel/edit baselines, allocation observations and upload bounds. The staged [pending-upload-bytes review](../20261027-pending-upload-bytes/REVIEW.md) had already measured indexed byte-accounting candidates; current production **already contains** indexed traversal, so it was not reimplemented. The latest separate [dev38 external audit](../../engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md) concerns post-receiver water callback conservation and remains assigned to the engine builder.

The current [old upload admission at prior commit](https://github.com/Shnewbs/Deep-Primal/blob/579516aba72bdb96006cdfabfd50837887e86c22/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L634-L654) rejected `used + mesh.gpuBytes() > perFrameLimit` even if **zero** parts had uploaded this frame. An indivisible first part above the default 256 KiB soft upload cap could be deferred on every frame, preventing the pending page from completing. This is a source-level forward-progress defect; occurrence in a native scene is not independently reproduced.

- New [`UploadWorkBudget` lines 3–29](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/UploadWorkBudget.java#L3-L29) enforces at most one first-part admission regardless of the **soft per-frame** byte/time caps, then respects count/time/byte limits for every subsequent part, using overflow-safe subtraction and rejecting negative inputs.
- [`VoxelLodRenderer` lines 618–676](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L618-L676) integrates this after the unchanged pending-page resident-memory bound, revision/identity rejection and pause-when-unfocused. Actual successful oversized uploads—not failed attempts—increment counters; resident GPU memory accounting remains.
- [`LodClient` lines 64–68](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L64-L68) exports the real oversized-upload count/bytes and explains soft versus hard budgets. [World reset L781–804](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L781-L804) resets counters.
- The standalone committed [`UploadWorkBudgetChecks.java` lines 1–88](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/docs/renderer-jams/20261027-lod-upload-forward-progress/UploadWorkBudgetChecks.java#L1-L88) supplies a source-matched model, deterministic randomized sequences and invalid-argument controls. It is docs-only and not packaged production code.

**Unchanged:** V1–V7 geography, native terrain capture, mesh geometry/material/UV/lighting/water ordering, water quantities and chemistry, saves/player edits, FSR1/NGX, OpenGL/Vulkan backends, graphics settings, textures and all other feature schedules. One first-part upload may exceed the configured soft upload time/byte cap, but only the previously admitted page under its hard resident-memory ceiling is eligible; this is not a new unbounded queue.

## Fresh executed focused evidence

Host: isolated Linux OpenJDK/Javac **21.0.12.1**; JDK25 extracted path and owner archive were **not present** in this session. Shell DNS for GitHub clone fails; private GitHub connector supplied the exact committed source. The new test helper and fixture were compiled from local bytes whose Git blob IDs equal committed **`6482ba88bb71741fc53b5ea387b37cec3661adb5`** and **`d1f4246f94db4c6b8956ebbed573186d9d383b8d`**, respectively. GitHub connector subsequently re-read all four changed production/test files at the pinned commit and confirmed blobs/integration and unchanged hard resident checks.

```sh
javac --release 21 -Xlint:all -d classes source/UploadWorkBudget.java tests/UploadWorkBudgetChecks.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.UploadWorkBudgetChecks
# PASS LOD_UPLOAD_FORWARD_PROGRESS_PASS checks=398690 randomizedAdmissions=150000 randomizedPendingQueues=10000

javac --release 21 -g -d prior/classes prior/source/ReusableTransformArrays.java prior/tests/*.java
java -Xverify:all -cp prior/classes com.shnewbs.deepprimal.lod.client.TransformTelemetryChecks
# PASS 724271
java -Xverify:all -cp prior/classes com.shnewbs.deepprimal.lod.client.TransformRetentionBudgetChecks
# PASS 625529
```

**Negative control:** replace the first-part admission with `return false` only in a private test copy; compilation succeeds, but the fixture exits 1 with `AssertionError: pending upload never advances` (correct expected rejection; mutant not committed or packaged). Initial pre-fix fixture mistakenly expected four instead of five frames; it failed and was corrected **before** final focused test and committed test blob; no credit assigned to rejected fixture.

Evidence ZIP `DeepPrimal_Renderer_LodUploadForwardProgress_20261027_daaeabb_SourceEvidence.zip`: SHA-256 `26f2bd6cd7e520f4fca72fda1ee7ab50c30b8b832c0873f5bfc0d7bce439abe4`, 5,455 bytes, CRC checked, saved Library `libfile_61980b5dd360819186879aaa448549fe`. Includes tested exact helper/fixture, positive/negative logs and old transform regression logs; private mutant **source is not in the ZIP**.

**Matched before/after measurements:** not performed on a GPU scene or game frame. The focused unit oracle demonstrates finite progress and overflow-safe policy for synthetic queues; no measured p50/p95/p99 FPS, driver upload time, real mesh size distribution, GPU/VRAM, draw/descriptor change, temporal water pixels or usable-distance gain. The previous Java transform tests are regression evidence, not a new optimization result.

## Six equally weighted renderer scores (read-only source self-audit)

| Renderer category | Prior /10 | Jam /10 | Evidence and missing acceptance |
|---|---:|---:|---|
| Distance/LOD usable coverage | 4 | 4 | Existing renderer [selection L509–549](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L509-L549) unchanged; no native travel/edit/unexplored-terrain acceptance |
| Mesh generation/reuse/scheduling | 6 | 6 | New admission [L639–676](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L639-L676) avoids perpetual oversize stall in model; no GPU upload, mesher or scheduler throughput measurement |
| GPU culling/submission/memory | 4 | 4 | [Resident bound L618–635](https://github.com/Shnewbs/Deep-Primal/blob/daaeabb1a60c970e23d396af34a359eac60cf87e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L618-L635) preserved, new oversized metrics, but no frame-synchronous GPU/descriptor/VRAM evidence |
| Actual FSR/DLSS implementation | 5 | 5 | Prior [upscaling work state](../../GRAPHICS-UPSCALING-WORK-STATE.json) reports FSR1 and incomplete DLSS NGX; no new temporal inputs/RTX dispatch or validation |
| Visual/backend/settings compatibility | 5 | 5 | Upload policy changes only; no OpenGL/Vulkan pixels, water transparency, UI/reload/device lifecycle checks |
| Validation/measured performance | 4 | 4 | 398,690 new Java21 policy assertions, negative mutation, 1,349,800 prior Java21 assertions; **no** Java25 full compile/Gradle, packaged JAR or matched GPU frame measurements |
| **Total / average** | **28/60 = 4.67** | **28/60 = 4.67** | **Delta 0.00**; author self-audit only |

The independent whole-engine grade is **50/100 = 5.0/10, carried**, not regraded. Renderer >8.5 target and independent engine/water gates remain open.

## Unrun gates and next priority

Under the owner’s October 27 build handoff, no full Java25/Gradle/Minecraft native build, Windows development JAR, outer/nested CRC or packaged source-class identity, GPU OpenGL/Vulkan pixels, AMD8060S/RTX hardware testing, save/reopen/resource-reload/shutdown or native latency test was run. **No qualified development JAR, release, GPU acceptance or fresh whole-engine grade is claimed.** Live first-part timing can exceed the configured soft CPU budget; verify real GPU stalls under worst mesh sizes before tuning the exception.

Next: on named AMD Windows Vulkan/OpenGL hardware, collect actual oversized part counts and warm/cold camera/travel/edit frame p50/p95/p99, resident/pending/upload/draw/descriptor/heap/VRAM distributions; verify positive progress without frame spikes, page revisions, loss/reload, native-near transitions and water transparency. If spikes occur, split mesh parts earlier in the generator/render pipeline without changing materials or fluid ordering, instead of unlimited budget bypass. Continue temporal depth/jitter/motion-vector prerequisites for actual NVIDIA DLSS in its appropriate renderer scope, leaving engine water simulation to the builder.
