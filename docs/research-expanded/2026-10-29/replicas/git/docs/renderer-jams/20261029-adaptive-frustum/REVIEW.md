# Expanded replica — docs/renderer-jams/20261029-adaptive-frustum/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-adaptive-frustum/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-adaptive-frustum/REVIEW.md |
| Preserved source SHA-256 | `79860cff8c6b848835b6172d10e4952f44f7c4e230414d8c23aa9cfd6b0a9b2f` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reviews the frozen adaptive gate and renderer binding at 9dd4b58. Six focused Java21 runs pass 590,823 assertions, a zero-skip mutant fails, and source inspection verifies completion-only training. The paired all-visible fixture improves medians about 1.5–1.8%, while tail results are mixed or worse.

## Research expansion — gaps and details to deepen

- The synthetic counter-based policy has no native performance qualification, and the baseline comparison lacks the actual render-thread cost distribution per coarse and fine test. Later pose resets change sampling opportunities without changing authoritative fine visibility.

## Research expansion — proposed contracts

- Preserve the exact fixture, adverse tails, source identities, and default-off status in an evidence receipt. Add per-extraction policy state and native CPU spans so a future decision can relate avoided work to real cost instead of treating check counts as interchangeable timing units.

## Research expansion — acceptance checks

- Reproduce complete-extract training, failed-extract exclusion, override, and reset in the real client. Compare fixed/adaptive/off variants with identical coverage and material order, including rapid camera turns; require native frame, memory, and transparent-water evidence before any default promotion.

## Research expansion — source anchors

- Renderer jam 27 — pinned read-only self-audit: adaptive page-frustum sample gate
- Exactly changed source and test files
- Executed tests, exact commands and honest measurements

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 27 — pinned read-only self-audit: adaptive page-frustum sample gate

**Jam ID:** `renderer-20261029T0653-adaptive-frustum`. **Reviewed exact immutable candidate:** [`9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1`](https://github.com/Shnewbs/Deep-Primal/commit/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1). Preclaim source main `eeb822642ad88355fa58414e7d5ff277c51cf6ec`, scoped claim commit `a7fa6978aecd4a258c61296a6eb415665ada15a2`. No engine version bump; owner performs full builds. **This is a renderer jam's read-only self-audit, NOT the independent whole-engine auditor.** Candidate code and test files were frozen throughout the 26/26 scored pinned-source review.

## Consumed audits and ownership

Read absent `AGENTS.md` (404), `docs/ENGINE-WORK-STATE.json`, `docs/WATER-ENGINE-WORK-STATE.json`, `docs/LOD-AND-DRAINAGE-ARCHITECTURE.md`, `docs/UPSCALING.md`, `docs/renderer-jams/RENDERER-BACKLOG.md`, jam26 claim/receipt/review, latest independent [Dev72 audit](../../engine-hourly-audits/2026-10-29T0634-84dda6f/REVIEW.md), latest commits and scoped leases. Other engine's Dev72 writer lease is checkpointed/released, jam26 renderer claim released; no competing active owner of these renderer files was identified. Used only GitHub current-file blob-SHA compare-and-swap operations, reading the live claim and file SHA before integration; no force push. A separate local Java test/classes directory was used. No other schedule or role's checkout/cache/output was touched.

Jam26's pinned 4,096-page × 6-part CPU fixture established that default page broadphase improves mostly-offscreen selection but regresses all-visible selection. Dev72 independently confirmed the remaining native Vulkan/GL A/B gap. This change adds an **experimental opt-in**, not an unverified default performance gain.

## Exactly changed source and test files

- [`LodPageFrustumAdaptiveGate.java` L10–35](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java#L10-L35), blob `6a518b2b0b69a88bec23b885223b8e67bd21e7f9`: new default-off `deepprimal.lod.pageFrustumAutoSkip=true` opt-in, sample 32+ eligible page boxes; when full coarse sampling avoids no more fine-part box tests than the number of coarse tests, bypass the next 3 extraction-level coarse passes and then sample again. World reset clears the policy. This is a counts proxy, **not measured frame/GPU performance**.
- [`VoxelLodRenderer.java` L525–613](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L525-L613), blob `082d34a6acd3d367dd2c34b4d62f8d9ec84fe190`: samples a whole extract, counts real avoided part tests even with telemetry off, and records policy success only after the complete extraction. Every bypass enters the original ordered fine-part culling; no saved visible part is elided. Original existing `pageFrustumBroadphase=false` override defeats the coarse path entirely. [World reset L935–953](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L935-L953) clears the policy/telemetry.
- [`LodClient.java` L90–103](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L90-L103), blob `6ee68b7030a2b0ec2ff345ecf7fbc81d988e57df`: exposes actual opt-in state, last completed coarse sample and bypassed eligible pages without calling this GPU occlusion.
- [Committed parity test](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/docs/renderer-jams/20261029-adaptive-frustum/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveChecks.java) blob `7c42de0c1252e451c0d5780a7b80aef09a24c589`, [committed paired microbench](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/docs/renderer-jams/20261029-adaptive-frustum/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveBench.java) blob `0740fd40c49d900aa33ccb1e6b1c150765245cd0`, [usage](README.md), scoped claim and this audit/receipt/backlog (documentation checkpoints after frozen source).

**Unchanged:** `LodPageFrustumBounds` blob `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc`, original page multi-part union, negative-coordinate bounds, native handoff, render pass draw order, meshes/lighting/cutouts, water color/opacity ordering, terrain generation, cache/stream admission, API bindings, full-detail/vertical/LOD/simulation caps, graphics and Vulkan/OpenGL providers, FSR/DLSS. The exact baseline...candidate diff includes only 7 files at review time, all in renderer source/tests/docs.

## Executed tests, exact commands and honest measurements

Host **Linux OpenJDK 21.0.12.1**. JDK25 and the owner-provided archive were absent from this isolated workspace. All tested local Java files were byte-exact with the named pinned GitHub blobs (`git hash-object`). Compiled the production `LodPageFrustumBounds.java` and `LodPageFrustumAdaptiveGate.java` with only production-excluded Minecraft AABB/mesh API doubles and committed tests:

```sh
javac --release 21 -Xlint:all -Werror -d classes [2 production-excluded stubs] [actual 2 production helpers] [3 isolated tests]
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumBoundsChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveBench
```

**PASS:** adaptive parity default/false/true each **46,095 assertions**; prior option default **60,005**, coarse-off **60,003**; original page-bound regression **332,530**. Combined **590,823 passing assertions** in these six focused runs. A mutated `SKIP_EXTRACTIONS=0` variant compiled but correctly failed test **assertion 3**. **26/26** independent read-only pinned caller/contract/source checks passed against the exact candidate; the frozen candidate had no source corrections during scoring. No Native/GPU result is implied by source tests.

A paired **301-sample** Java CPU fixture, 4,096 cached pages × 6 AABBs, 500 warmup iterations and alternating fixed/adaptive order, reported p50 fixed/adaptive **69,804/68,725 ns** and **69,605/68,362 ns** for two all-visible passes (~1.5–1.8% lower isolated median). At **80% offscreen** p50 was **16,987/16,996 ns** and **17,056/17,057 ns**, effectively identical. p95/p99 were **mixed and sometimes worse**, so no unconditional performance or native FPS uplift is claimed. Synthetic visibility uses no real client frame/GPU/API device, actual culling pixels, VRAM or water blend. A no-flag local run is fixed coarse, not adaptive.

## Six equally weighted renderer self-audit grades

| Category | /10 | Exact pinned evidence and missing gate |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.55 carried** | [Unchanged LOD selection and native handoff](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L542-L565); no unseen terrain or new distance/visual acceptance |
| Mesh generation, reuse and scheduling | **6.95 carried** | [Earlier part splitter audit](../20261028-split-preflight-once/REVIEW.md); unchanged mesher/worker and no new benchmark |
| GPU culling, submission and memory | **4.55 carried** | [CPU page gate and fine-path fallback](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L566-L595); no real GPU occlusion, MDI, VRAM or FPS acceptance |
| Actual FSR/DLSS integration | **5.00 carried** | [Pinned upscaling status](../../UPSCALING.md) implements real FSR1 spatial; temporal FSR/DLSS dispatch still absent; no provider source touched |
| Visual/backend/settings compatibility | **5.30 carried** | [Default-off and override policy](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java#L10-L34) with [ordered fine fallback](https://github.com/Shnewbs/Deep-Primal/blob/9dd4b581306fa230d775bfe06fb71ed9a4a2a3c1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L566-L590); no GPU water, Vulkan/GL or lifecycle pixels |
| Validation and measured performance | **5.85 (+0.05)** | Strict committed real-helper tests, mutant rejection, 26/26 source checks and paired adverse/offscreen CPU p50/p95/p99; no native frame/GPU measurement |
| **Equal-weight mean** | **5.70 (+0.01)** | (6.55+6.95+4.55+5.00+5.30+5.85)/6 = **5.70**; prior renderer jam26 **5.69**. Independent whole-engine score separately remains **5.0/10 carried**, not regraded. |

## Delivery and remaining blockers

Library artifact: `/Deep Primal/Renderer Jams/DeepPrimal_Renderer_AdaptiveFrustum_20261029_9dd4b58_SourceEvidence.zip`, **9,395 bytes**, SHA256 `58084a1e85ecfa39bb7ef61d9cb1318d16920a19f78c6aa330a426f292465fc4`, 11 entries; outer ZIP CRC and all manifest member SHA256 checks **PASS**, Library upload **confirmed successful**. This archive is source/tests/receipts only, **not a JAR**. Source identity and exact tested helpers were verified before publishing the review. No new development release or CurseForge.

**Not run under October 27 owner override:** complete Java25 Gradle9.2.1/NeoForge build, JAR producer/compiled/nested CRC/native payload, native Windows Vulkan/OpenGL/RTX, frame-time CPU/GPU p50/p95/p99, VRAM/RAM, GPU pixels/water transparency, reload/resize/world-switch/save/reopen/shutdown, active temporal FSR or DLSS. Current filesystem has no usable JDK25/owner archive; independent Dev72's separate Java25 result does not qualify this jam.

**Next priority:** owner-run 3-way *fixed coarse/default*, *adaptive enabled*, *broadphase disabled* with matched all-visible, 80% offscreen and moving-camera views on both Vulkan and OpenGL. Compare real CPU/GPU frame-time p50/p95/p99, coarse/part checks, draw counts, VRAM and water pixels, and leave opt-in disabled if tail regressions occur. Coordinate renderer-only admission with existing builder; do not change engine/water state. Work remains below >8.5 renderer target.


<!-- END PRESERVED SOURCE -->
