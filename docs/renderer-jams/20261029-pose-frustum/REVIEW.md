# Renderer jam 28 — read-only self-audit: resample after camera motion

**Jam ID:** `renderer-20261029T0802-pose-frustum`. **Pinned reviewed code + test commit:** [`5713d70252e658625fd0fff1b6c911efba073359`](https://github.com/Shnewbs/Deep-Primal/commit/5713d70252e658625fd0fff1b6c911efba073359). Prior main at claim acquisition: `38e846971c08a37e42dbce4143d33a8f0646cf8f`; claim `dd811eb25c7ed50c40ccc0034b9969eb36b704e2`; implementation commits `fe9b74d4db7dd5ba94f314786719d0923a0bfeef` and `743b2f8244f3ee782e7e61a5dbaa947564884489`. Source/test frozen during this **renderer self-audit**; this is **not** the independent whole-engine audit.

## Scope, prior findings and changes

Read missing `AGENTS.md` (404), current engine and water work-state files, LOD/drainage architecture, renderer backlog, `docs/UPSCALING.md`, jam27 review/claim and the independent [Dev73 audit](../../engine-hourly-audits/2026-10-29T0735-529bbe9/REVIEW.md). Engine Dev73 writer lease and jam27 renderer claim were released. New, bounded renderer-only claim excludes engine/water/terrain, graphics upscaler providers and unrelated schedules. The independent Dev73 marine shoreline-contract finding is engine-owned; **not touched**. Git CLI cannot resolve github.com in this Linux workspace; all writes used current GitHub file-blob CAS, fresh main reads, and no forced refs/Actions. One unrelated engine state commit entered main during this jam without a file collision.

Jam27's adaptive coarse page-culling gate was opt-in and could skip three whole extractions after an all-visible/low-saving sample. A camera turn during a skip window therefore delayed the next coarse sample, even though offscreen rejection could become useful. This jam addresses that **CPU admission heuristic only**:

- [`LodPageFrustumAdaptiveGate.java`](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java), blob `39531d82e55d905b503d52d439913914c7c1cfc1`: compare requested camera pose against the last **successfully completed coarse sample**. Force a full page test when travel exceeds **four blocks** or wrapped yaw/pitch change exceeds **eight degrees**; invalid/nonfinite pose also samples. Pending camera pose is committed only on a successful sampled extraction. World reset clears pose. Original no-pose gate and default-off policy remain callable.
- [`VoxelLodRenderer.java`](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L522-L535), blob `2bb1c95d9b800da7b036d0b1a89f4249afca3c87`: only change is supplying position plus camera yaw/pitch to the adaptive gate. Exact whole-file pre/post comparison confirms **one invocation substitution and no other renderer byte changes**. Unchanged fine-part frustum path remains authoritative.
- [Committed isolated pose test](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/docs/renderer-jams/20261029-pose-frustum/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumPoseChecks.java), blob `61852c3fdd13613a4f85830612560113f2976d2b`; claim, receipt, this review and focused backlog are docs-only follow-up.

No terrain meshes, water shaders/alpha, draw ordering, GPU buffers, LOD ranges, simulation settings, Vulkan/GL backend, FSR1 shaders, temporal FSR, DLSS, config serialization or owner build outputs were changed. The existing broadphase-off override still bypasses the gate. Motion thresholds are heuristic **CPU work** triggers, not visual culling thresholds or measured optimal settings.

## Executed validation

Host Linux OpenJDK **21.0.12.1**, no local JDK25 or owner archive. Git hash-object of locally compiled helper and test matches pinned GitHub blobs. Explicit production-excluded AABB/mesh doubles were taken from the previous renderer regression; actual `LodPageFrustumBounds.java` remains blob `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc`. Commands:

```sh
javac --release 21 -Xlint:all -Werror -d classes @results/sources.list
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumPoseChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumPoseChecks
# Repeat both modes for LodPageFrustumAdaptiveChecks, LodPageFrustumOptionChecks, LodPageFrustumBoundsChecks.
# Additionally run LodPageFrustumOptionChecks with -Ddeepprimal.lod.pageFrustumBroadphase=false, FALSE, true.
```

**PASS:** pose regression false/true **23,057/23,061 assertions**; previous adaptive **46,095/46,095**; page option **60,005/60,005**; page-bounds **332,530/332,530**; extra broadphase false/FALSE/true **60,003/60,003/60,005**. Total **1,103,389 passing assertions** across 11 runs, strict Java21 compilation and verification. The moving-camera test checks **180 ×128 = 23,040 page visibility/order comparisons per mode**, including signed/negative page coordinates, turn, translation, wraparound, NaN and reset behavior. **Expected negative:** a source mutant disabling camera-triggered sampling compiled but failed at **assertion 8**, rejecting the original gap. **16/16** read-only GitHub-pinned integration predicates passed on the frozen candidate.

Actual native Minecraft API compilation of `getYRot()/getXRot()` was **not** performed: production caller is source-inspected only. Frame p50/p95/p99, GPU time, draw count, VRAM, water pixels and real A/B performance remain unmeasured. The old bounds regression emits its own CPU timing, but those figures **do not measure this new change** and are not counted as a matched gain. No FPS improvement or higher usable render distance is claimed.

## Six equally weighted renderer grades (out of 10)

| Category | Score | Pinned evidence / gate |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.55 carried** | [Renderer original native handoff and fine visibility](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L544-L610), no unseen-terrain delivery or measured expanded distance |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Previous mesh splitter evidence](../20261028-split-preflight-once/REVIEW.md), unchanged in this jam |
| GPU culling/submission/memory | **4.55 carried** | [Opt-in CPU gate](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java), no GPU occlusion/indirect/VRAM verification |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [FSR1 spatial implemented](../../UPSCALING.md), temporal FSR and DLSS game dispatch absent/unaccepted |
| Visual/backend/settings compatibility | **5.30 carried** | [Original ordered fine-part fallback](https://github.com/Shnewbs/Deep-Primal/blob/5713d70252e658625fd0fff1b6c911efba073359/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L572-L611), default-off/override checks; actual Vulkan/GL and transparent-water pixel tests unrun |
| Validation and measured performance | **5.90 (+0.05)** | Git-pinned source identities, 11 strict JVM runs, 1,103,389 passing assertions, mutant rejected, **16/16** scoped checks; no native timing evidence |
| **Equal-weight average** | **5.71 (+0.01 displayed)** | `(6.55+6.95+4.55+5.00+5.30+5.90)/6 = 5.7083`; jam27 **5.70**. External whole-engine grade separately **5.0/10 carried**, not changed. |

## Artifacts, unrun gates, and next repair

Source/test evidence ZIP: `DeepPrimal_Renderer_PoseFrustum_20261029_5713d70_SourceEvidence.zip`, **12,685 bytes**, SHA256 `1ecaa9c0c209f03d71dac37e0379a334febc777ca0c8a3dd5948df1c1061e726`, **16 entries**, ZIP CRC and member SHA256 manifest **PASS**. Library upload **confirmed** at `/Deep Primal/Renderer Jams/`. This is **not a JAR**.

**Not run under October 27 owner override:** Java25 full Gradle 9.2.1/NeoForge build, packaged class/source identity and nested/native payload checks, real Windows Vulkan/OpenGL/RTX, camera travel A/B GPU/CPU p50/p95/p99, RAM/VRAM, transparent water pixel parity, world reopen/edit/resize/resource reload/shutdown, temporal FSR or active RTX DLSS. Owner performs full builds until authorizing a change. No qualified development JAR, GitHub release, CurseForge or hardware acceptance.

**Next:** owner-native matched fixed broadphase/default vs adaptive vs broadphase-off scenes, with abrupt yaw turns, moving camera, all-visible and 80%-offscreen geometry. Verify compile and FOV/pose getters on the actual client, pass/fail water pixel comparisons, draw counts and p50/p95/p99 CPU/GPU/VRAM. If adaptive tails regress, leave opt-in off. Follow actual measured bottleneck for the next renderer-only mesh/GPU/streaming increment; do not expand distance slider without host-budget and unseen-terrain delivery proofs.
