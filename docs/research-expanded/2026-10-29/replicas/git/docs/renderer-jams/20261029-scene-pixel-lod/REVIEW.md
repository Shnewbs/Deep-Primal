# Expanded replica — docs/renderer-jams/20261029-scene-pixel-lod/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-scene-pixel-lod/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-scene-pixel-lod/REVIEW.md |
| Preserved source SHA-256 | `3a189d420787113fa1fee26f544baa032b7f2c79491ef3b43fcd9c9c71feb762` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Pins cf578b2’s projection helper, extraction binding, and diagnostics, with two 501,280-check runs and 19 source checks. The existing world-target assignment/restoration is inspected unchanged. Of 200,000 simulated selector cases, 28,529 scaled cases become coarser; these are behavior counts, not measured performance.

## Research expansion — gaps and details to deepen

- The test proves a policy change and native-path parity, while complete host lifecycle and output-quality consequences remain unmeasured. The more-coarse distribution means later frame comparisons must disclose geometry changes, and stale target/extent transitions require actual adapter validation.

## Research expansion — proposed contracts

- Add an extraction receipt containing frame/world/target generation, render and output heights, focal policy, error threshold, and selected-level distribution. Separate correctness, image quality, and performance outcomes; retain the exact helper/selector identity and explicit native runtime gate.

## Research expansion — acceptance checks

- Reject an old native-only implementation with the existing negative control, then exercise target activation, restoration, provider failure, resize, and repeated world switches in the real client. Capture output silhouettes, cave/near handoff, transparent water, CPU/GPU tails, and memory with the changed LOD distribution visible.

## Research expansion — source anchors

- Jam 31 renderer self-audit (code review read-only)
- Actual changed source and pinned evidence
- Tests and measurement

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Jam 31 renderer self-audit (code review read-only)

**Jam ID:** renderer-20261029T1100-scene-pixel-lod. **Initial main:** c48b7bc6148be4c2f0013ae685d4bed7ad3ffdee. **Claim commit:** [653cd781272eda17b66925f9d332b1b3581984e4](https://github.com/Shnewbs/Deep-Primal/commit/653cd781272eda17b66925f9d332b1b3581984e4). A separate writer advanced main without touching renderer files; **fresh source parent:** 783e446609c817de2ee4c38e483ebbf20889dbfa. **Final reviewed production/test source commit:** [cf578b2e36fcdbde04320cc2564c5b1cd89004d6](https://github.com/Shnewbs/Deep-Primal/commit/cf578b2e36fcdbde04320cc2564c5b1cd89004d6), 4 files; no code changes during scored review. Previous renderer grade **5.73** ([jam30](../20261029-normal-angle-fastpath/REVIEW.md)). This is **our renderer self-audit**, not an independent whole-engine regrade.

## Actual changed source and pinned evidence

- [VoxelLodRenderer.java L548–560](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L548-L560), Git blob 71ed726cd3723beba3fa1fe88d8fef00fc3fe01b: world-target or native fallback screen-error focal. [L622–630](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L622-L630): only successfully published opt-in sample. [L999–1006](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L999-L1006): reset.
- [LodClient.java L47–54](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L47-L54), Git blob 8483c0f1a5e90dda37e8f9bc39d953f780af443c: last LOD pixel height/target diagnostics and CPU scope.
- [LodErrorProjection.java L1–20](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodErrorProjection.java#L1-L20), Git blob 895c4beef16e9d251cf8fe1f0b200f35a280e782: native fallback and same focal expression, no allocation.
- [LodScenePixelChecks.java L14–97](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/docs/renderer-jams/20261029-scene-pixel-lod/tests/com/shnewbs/deepprimal/lod/client/LodScenePixelChecks.java#L14-L97), Git blob 24fa8ad1d3beda28b33195844aa33cf75a0100f5: fixed-seed native-pixel parity, scaled-page selector and boundary regression; excluded from production modules.
- [Unchanged UpscalingRuntime.java L137–188](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/UpscalingRuntime.java#L137-L188), blob 98368d7cf720d817b56440532d108b604e5b63c4: world frameTarget assigned before world rendering and cleared at restore. [UpscalingRenderMixin.java](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/mixin/client/UpscalingRenderMixin.java) unchanged blob 57f5bf10706d8f6b9589496cdeeb578fe4dcc0db. [LodScreenErrorSelector.java](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodScreenErrorSelector.java) unchanged blob f550847a571bbf56b243b2bb2b77ecc740b98e8d. No changes to meshes, GPU draws, water materials, renderer-vulkan, existing saved geography or other roles' files.

Consumed [jam30 audit](../20261029-normal-angle-fastpath/REVIEW.md) (synthetic CPU arithmetic cannot establish GPU gain) and [independent Dev76 audit](../../engine-hourly-audits/2026-10-29T1032-00921e12/REVIEW.md) (no native renderer acceptance). The claim is strictly scoped and was checked before integration; no heavy native/GPU measurement slot reserved.

## Tests and measurement

- Linux/OpenJDK **21.0.12.1**. Commands: `find src tests -name '*.java' | sort > sources.list`; `javac --release 21 -Xlint:all -Werror -d classes @sources.list`; `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodScenePixelChecks` **twice**. Both PASS **501,280** checks, **1,002,560 total**. Exact helper/test/selector local Git blob IDs match pinned source.
- Negative native-only world-height mutant compiled with the same strict command then failed **assertion 3** as expected, proving the new regression detects the old behavior.
- Native-only selector parity and frame-pixel identity checked; randomized 200,000 simulated selector cases show original detailed/mid/coarse counts 32,608/51,781/115,611 vs current 23,332/41,804/134,864, 28,529 stricter coarser results in downscaled cases, never finer. These counts are **not a performance measurement**.
- **19/19** read-only GitHub source checks passed: production/test pinned blob SHAs, world-target activation/clearing, unchanged selector, water blend, draw submission, resets/diagnostics, scoped claim, exactly 4 owned changed paths.
- **Missing/unrun:** Java25 runtime, complete Gradle 9.2.1/NeoForge, native Minecraft and Windows AMD/NVIDIA GPUs, actual CPU/GPU frame time p50/p95/p99/FPS/VRAM, Vulkan/OpenGL transparent-water pixels and LOD transitions, resize/reload/world-switch/reopen/shutdown, temporal FSR3/FSR4/DLSS dispatch, qualified JAR/CRC/native platform package. The user explicitly owns full builds; no Actions or release performed. No matched real performance gain claimed.

## Six equal-weight renderer grades (out of 10)

| Category | Grade | Pinned basis / limitation |
| --- | ---: | --- |
| Distance/LOD correctness and useful coverage | **6.60 (+0.05)** | [Correct scene-pixel focal](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L548-L560) and [selector checks](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/docs/renderer-jams/20261029-scene-pixel-lod/tests/com/shnewbs/deepprimal/lod/client/LodScenePixelChecks.java#L38-L74); no native handoff/unknown-terrain acceptance. |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Prior constrained splitter evidence](../20261028-split-preflight-once/REVIEW.md); no mesh work or hardware proof this jam. |
| GPU culling/submission/memory | **4.55 carried** | [Synchronous indexed API draws](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L875-L920) unchanged, not hardware indirect; VRAM/GPU timing unrun. |
| Actual AMD FSR/NVIDIA DLSS integration | **5.00 carried** | [FSR1 actual spatial](../../UPSCALING.md) maintained. Temporal FSR and RTX DLSS dispatch incomplete; no provider credit. |
| Visual/backend/settings compatibility | **5.30 carried** | [Unmodified current world-target lifecycle](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/UpscalingRuntime.java#L137-L188); native Vulkan/GL and transparent water pixel acceptance missing. |
| Validation and measured performance | **6.05 (+0.05)** | 1,002,560 strict assertions, negative mutant, 19 source checks [test](https://github.com/Shnewbs/Deep-Primal/blob/cf578b2e36fcdbde04320cc2564c5b1cd89004d6/docs/renderer-jams/20261029-scene-pixel-lod/tests/com/shnewbs/deepprimal/lod/client/LodScenePixelChecks.java#L14-L97), no real FPS/performance. |
| **Arithmetic mean** | **5.74 (+0.01)** | (6.60+6.95+4.55+5.00+5.30+6.05)/6 = 5.74167; jam30 5.73. |

**Independent whole-engine grade 5.0/10 carried, unchanged.** This is not a fresh full audit or hardware acceptance. Full pre/post logs, Java sources, SHA256 manifest and 12-entry CRC-verified ZIP are retained in Library /Deep Primal/Renderer Jams/; archive SHA256 9578e6c648b4f84611761c3fa27852a6a953c4138ccd421d94cd10df9af02258.

**Next repair:** owner matched Vulkan/OpenGL native/native-vs-FSR1 720p scene tests at fixed camera, camera travel/edit and transparency, with reported LOD height, CPU/GPU tails, RAM/VRAM and transitions; then measured throughput optimization, preserved native handoff, and real temporal provider inputs.


<!-- END PRESERVED SOURCE -->
