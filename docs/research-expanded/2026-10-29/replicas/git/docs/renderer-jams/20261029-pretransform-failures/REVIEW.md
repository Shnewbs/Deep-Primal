# Expanded replica — docs/renderer-jams/20261029-pretransform-failures/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-pretransform-failures/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-pretransform-failures/REVIEW.md |
| Preserved source SHA-256 | `3a76f98c0d7f1b1a67199f6cebd26b4aff2bfbe6d559dc0e637d35ffc0c3f2bb` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends opt-in submission telemetry to target/camera, transform reservation, offset, and uniform-write failures before draw recording. These become preparation-only failed observations with zero returned draw work, while existing draw-stage errors remain on their single finally path. The 66,368 assertions improve CPU observability, not GPU lighting, pixels, or frame-rate acceptance.

## Research expansion — gaps and details to deepen

- The new coverage still excludes no-visible-LOD frames and culling, and diagnostic failure during unwinding can obscure the original error. The probe should join a complete frame population without converting unobserved work into zero duration.

## Research expansion — proposed contracts

- Add nonthrowing or guarded error-path observation, original-exception preservation, explicit skipped/empty outcomes, and frame/epoch correlation. Keep telemetry-off clock avoidance and prevent duplicate observations across preparation and draw-finally paths.

## Research expansion — acceptance checks

- Retain 30,000 mixed cases and exact-source boundary checks, then inject real preparation, recording, close, and diagnostic failures. Verify one observation per relevant pass, original error identity, and complete-frame/GPU correlation on the target artifact.

## Research expansion — source anchors

- Renderer jam 22 — read-only self-audit of exact implementation
- Scope and consumed findings

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 22 — read-only self-audit of exact implementation

Jam ID: `renderer-20261029T0152-pretransform-failures`. Review pin: [`155aeda003e6310d2174ecff74dfa7bb18f0b8b2`](https://github.com/Shnewbs/Deep-Primal/commit/155aeda003e6310d2174ecff74dfa7bb18f0b8b2). Initial main `985b2f83538993130d1c94a25f6b77f26e490415`; bounded ownership claim commit `b9be5e1de7237d00d23ff3f9061d8f8868269ffb` is the implementation parent. `gradle.properties` version **0.0.1-alpha.29-dev.67-hydrology-binding.1** unchanged. This is a **renderer jam self-audit** after implementation was complete, **not** a fresh independent whole-engine audit. No production code changes occurred during this scored review.

## Scope and consumed findings

Consumed [jam21 audit](../20261029-submit-stage-timing/REVIEW.md) and [independent Dev67 audit](../../engine-hourly-audits/2026-10-28T1831-a8e26ba/REVIEW.md), which identified failures before `writeTransforms` escaping CPU timing history. Current main, scoped claim and other writer lease were inspected before integration. Source changes are restricted to the LOD renderer/probe and diagnostic map; no water, terrain, mesh format, shaders, FSR/DLSS provider, graphics menu, Vulkan/OpenGL adapter or external scheduler state changed.

Changed files and pinned evidence:

- [`LodSubmissionProbe.java` lines 11–38](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodSubmissionProbe.java#L11-L38) blob `5c2175cc1f22d781312baf1dbcfdeec43f301bed`: failed target/transform-preparation observations are entered as transform CPU elapsed with zero draw-stage time and zero returned API work, counted separately as pre-draw failures. Invalid negative duration is rejected before mutation; reset clears counts. Existing completed/partial-failed draw API accounting stays in `finished`.
- [`VoxelLodRenderer.java` lines 781–881](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L781-L881) blob `584dbd6056b2bb803d9e7a71eb63682cbd388d49`: capture target, camera, transform acquisition, offsets and uniform-write failures; catch original RuntimeException or Error, record only when telemetry is opted in and transforms have not been written, and rethrow the same failure. Draw-pass exceptions are already counted by their old finally path; no double count. All synchronous API draw calls, 4,096-entry batching and transparent-water pipeline inputs are unchanged.
- [`LodClient.java` lines 61–73](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L61-L73) blob `819536df5143c2e75048962abb8fc36ac069bc2e`: new `lodSubmissionPreDrawFailures` diagnostic and explicit CPU-only/zero-draw semantics.
- [New committed 30,000-case regression](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/docs/renderer-jams/20261029-pretransform-failures/LodPretransformFailureChecks.java) blob `73924381d87c8d39e6e1bb22ab9922c6abb2715e`; [README](README.md).

## Executed checks and limitations

Isolated Linux system JDK **21.0.12.1**; `java -version` and `javac -version` verified. Previous owner JDK25 path and Linux archive were absent in this environment, so no JDK25 claim. Tested helper Git blob and new test Git blob match the committed implementation exactly; unchanged `RollingTiming.java` blob `0be7512957537596295ba204f8f1dfc176aa686f`, previous regression blob `241f91c0207b278e59dcc8c074071d08078d365d`.

```sh
find src -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSubmissionProbeChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPretransformFailureChecks
```

**PASS:** original 20,000-case 29,276-assertion probe regression plus new 30,000-case 37,092-assertion pre-draw/correct-order/percentile/counter/reset regression, **66,368 assertions** per paired run. Re-running the new test also passed 37,092 assertions. The deliberate mutant omitted the `failedBeforeDrawPasses` increment, compiled successfully and **FAILED as expected at assertion 4**. **PASS:** 25/25 read-only GitHub-pinned renderer/probe/diagnostic/in-scope integration checks on implementation SHA. Not a full renderer module compile: Minecraft/NeoForge API dependencies and a complete source checkout are unavailable locally.

Archive: `DeepPrimal_Renderer_PretransformFailureTelemetry_20261029_155aeda_SourceEvidence.zip`, **6,971 bytes**, SHA-256 `33e586dceef2d9eaec4b0a9c68a409ca499b25ea2628e06587bbd4dfe069b708`; 7 outer ZIP members passed CRC and SHA-256 manifest checks. Saved successfully in Library `/Deep Primal/Renderer Jams/`. This is source/test evidence, **not a JAR**.

## Equal-weight six-category renderer self-audit

| Category | /10 | Pinned evidence and unaccepted gates |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.50 carried** | Existing [visible page/render pass](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L780-L804); no useful-range/GPU coverage claim |
| Mesh generation, reuse and scheduling | **6.95 carried** | Existing [splitter audit](../20261028-split-preflight-once/REVIEW.md); no new mesh worker change |
| GPU culling, submission and memory | **4.45 carried** | [Synchronous API submission](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L813-L857); unchanged commands, no MDI or GPU memory measurement |
| Actual AMD FSR / NVIDIA DLSS implementation | **5.00 carried** | [FSR1 implemented, temporal and DLSS incomplete](../../UPSCALING.md); no provider progress in this jam |
| Visual/backend/settings compatibility | **5.25 carried** | [Opt-in CPU-only diagnostic](https://github.com/Shnewbs/Deep-Primal/blob/155aeda003e6310d2174ecff74dfa7bb18f0b8b2/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L61-L73); native transparent-water and Vulkan/OpenGL image gates not run |
| Validation and measured performance | **5.60 (+0.05)** | 66,368 strict Java21 source-model assertions, expected mutant rejection, 25/25 source checks; no measured real frame-time gain |
| **Equal-weight mean** | **5.63 (+0.01)** | `33.75/6=5.625`, versus prior displayed 5.62. Does not change independent whole-engine 5.0/10. |

## Unrun gates, defects, next action

**Not run:** full Java25 Gradle9.2.1/NeoForge compilation, Java25 unit/integration suite, GPU/Vulkan/OpenGL/Minecraft client, save-reopen/world switching/resource reload/resize/shutdown, actual GPU failure injection, transparent water pixels/ordering, motion/silhouette seams, CPU/GPU whole-frame p50/p95/p99, draw/descriptor counts, RAM/VRAM, FPS, native FSR temporal or RTX DLSS, and JAR/native/archive qualification. Per October27 owner override these builds are owner-operated; no fresh JAR, provider acceptance, usable render-distance increase or FPS gain is claimed.

Remaining defect: new stage probe still samples only **LOD-visible** passes; culling/empty-frame time and hardware GPU execution lie outside this metric. Tracking a caught exception may be lost if diagnostics themselves throw during error unwinding, a native fault-injection gate. Unexplored-terrain delivery and full-detail tracker constraints remain open.

**Next priority:** owner Vulkan/OpenGL same-scene batched-versus-direct camera/travel/edit/world-reopen, transparent-water screenshots, GPU timestamps, VRAM and CPU p50/p95/p99; then act on a measured bottleneck. Continue real FSR temporal/DLSS work without treating spatial linear scaling as vendor integration. Do not overlap engine/water workstreams.


<!-- END PRESERVED SOURCE -->
