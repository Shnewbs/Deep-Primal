# Renderer jam 24 — read-only self-audit of guarded squared-distance LOD thresholds

**Jam:** `renderer-20261029T0356-squared-error-selection`.
**Exact reviewed implementation:** [`2cdfe022ffb823bbcbb8460368e2f13535023cf3`](https://github.com/Shnewbs/Deep-Primal/commit/2cdfe022ffb823bbcbb8460368e2f13535023cf3).
**Immediate parent:** `bb188d8b33dea4ffac2d266e209299c2b5b2631f` (bounded renderer ownership claim); preclaim baseline `45c777691a9d0934012eb114a57ab8501f0ec827`.
**Version:** no bump; current Dev69 `0.0.1-alpha.29-dev.69-stitched-ocean.1` is unrelated engine ownership.
This is the **renderer author's read-only self-audit**, *not* an independent whole-engine review; no source was modified during scoring.

## Scope and actual changes

Consumed renderer [jam23](../20261029-visible-snapshot-reuse/REVIEW.md) plus [Dev69 independent audit](../../engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md) (marine connectivity defect belongs to the engine writer). `AGENTS.md` was absent. Read engine/water work-state, LOD/drainage, FSR/DLSS policy, prior receipts, scheduler status, claims and main. Existing renderer claim is bounded and current; only renderer files are modified. No owner terrain/water/independent auditor files or workspaces were touched.

- [`LodScreenErrorSelector.java` lines 10–65](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodScreenErrorSelector.java#L10-L65), blob `f550847a571bbf56b243b2bb2b77ecc740b98e8d`: precomputes four hysteretic projected-size squared boundaries once per extraction; normal pages use squared horizontal range rather than `Math.hypot`; near any transition (1e-10 relative squared boundary) or invalid/nonfinite cases the prior division/hypot logic executes unchanged.
- [`VoxelLodRenderer.java` lines 531–552](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L531-L552), blob `2a4c3cfb2e169079c4b863b45f85de0dbaa6aa4e`: one per-extraction selector and configured error snapshot, same camera, native-section handoff, cache iteration, previous-level hysteresis and `entry.selected` publication.
- [`LodScreenErrorSelectorChecks.java`](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/docs/renderer-jams/20261029-squared-error-selection/LodScreenErrorSelectorChecks.java), blob `52da45b26d12239621a2f29963603aa25cb24476`: source-excluded strict-Java deterministic parity and paired CPU component fixture.
- [Scoped README](docs/renderer-jams/20261029-squared-error-selection/README.md) and the claim were the other files; no shaders, opaque/transparent mesh data, GPU API commands, graphics settings, simulation, terrain, water chemistry or backend changed.

## Executed evidence and unrun gates

Host: Linux OpenJDK `21.0.12.1`; Java25 toolchain/archive were **not present** in this execution environment. The exact Java helper/test Git blob SHAs were matched against their local tested files, and GitHub pinned source/usage checks passed **10/10**.

```sh
javac --release 21 -Xlint:all -Werror -d classes src/LodScreenErrorSelector.java src/LodScreenErrorSelectorChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodScreenErrorSelectorChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodScreenErrorSelectorChecks
```

**PASS:** 1,556,976 exact-selection parity assertions each run: 1–12-pixel error settings, six focal lengths, 0/1/2 previous LODs, signed random page offsets, 401-step neighborhoods of all four hysteresis thresholds and abnormal values. **PASS negative:** invert the normal squared-distance upshift predicate, compile and run: intentionally fails at assertion #1 with exit code 1.

Matched same-process CPU-only 4,096-page selector, 301 paired warmed samples per repetition:
- Run 1 old/new median `65,734/42,230 ns` (35.8% lower).
- Run 2 old/new median `60,404/45,656 ns` (24.4% lower).

The optimization is **selection-component CPU time only**. It does not establish a complete game-frame, GPU, VRAM, resource upload, FPS, visual or native-water improvement. 1 frame-scoped helper object is still created per extraction.

**Not run:** Java25 compiler, complete Gradle9.2.1/NeoForge Java25 build, packaged JAR/archives and payload identity, actual OpenGL/Vulkan scene/transparent-water pixels, resize/resource reload/world switching/save/reopen/shutdown, p50/p95/p99 real CPU/GPU frames and draw/descriptor/VRAM counts, full-detail tracker limits, unexplored-terrain delivery, native FSR temporal or RTX NGX DLSS. Owner operates full builds per October27 override; no JAR or new release is claimed.

**Evidence archive:** `DeepPrimal_Renderer_SquaredErrorSelection_20261029_2cdfe02_SourceEvidence.zip`, **4,902 bytes**, SHA256 `98b05734cc1a8a448d5d89500328cbfddc269966b2f6feb62ed053fe7c3a3066`; all 7 ZIP members passed outer CRC and listed SHA256 manifest checks. Library upload confirmed at `/Deep Primal/Renderer Jams/`. Source evidence, not a compiled mod.

## Six equally weighted renderer grades

| Category | /10 | Pinned basis and missing proof |
|---|---:|---|
| Distance / LOD correctness and usable coverage | **6.55 (+0.05)** | [Exact hysteretic threshold compatibility](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodScreenErrorSelector.java#L25-L62), 1.56M checks; no new native distance coverage, unseen-terrain or silhouette proof |
| Mesh generation, reuse, scheduling | **6.95 carried** | [Earlier optional splitter/source audit](../20261028-split-preflight-once/REVIEW.md); no current mesh/rebuild change |
| GPU culling, submission and memory | **4.50 carried** | [Unchanged cache/frustum/draw source](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L560); no actual GPU batching/VRAM gain |
| Actual AMD FSR / NVIDIA DLSS | **5.00 carried** | [Real FSR1 spatial; temporal FSR and RTX NGX dispatch incomplete](../../UPSCALING.md); no upscaler source change |
| Visual, backend and settings compatibility | **5.25 carried** | [Native-section check and saved error setting preserved](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L530-L552); native water pixels/backend tests unrun |
| Validation and measured performance | **5.70 (+0.05)** | [Exact test source](https://github.com/Shnewbs/Deep-Primal/blob/2cdfe022ffb823bbcbb8460368e2f13535023cf3/docs/renderer-jams/20261029-squared-error-selection/LodScreenErrorSelectorChecks.java), 2 × 1,556,976 assertions, expected mutant failure, 10/10 pinned integration checks, paired CPU-only selector medians; no GPU frame-tail or allocation evidence |
| **Equal-weight mean** | **5.66 (+0.02 displayed)** | `(6.55+6.95+4.50+5.00+5.25+5.70)/6=5.6583` versus prior renderer **5.64**. Independent whole-engine **5.0/10** carried unchanged |

**Next repair/measurement:** owner matched same-scene Vulkan and OpenGL camera travel/edit/LOD edge/native-section handoff with p50/p95/p99 CPU/GPU timings, draw/descriptor counts, transparent-water pixel comparison, resident GPU memory and lifecycle. Use actual bottlenecks to choose the next batch/stream optimization. Temporal vendor upscalers remain separate incomplete work.
