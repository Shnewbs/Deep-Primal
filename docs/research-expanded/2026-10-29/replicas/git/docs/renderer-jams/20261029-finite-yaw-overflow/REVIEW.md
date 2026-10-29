# Expanded replica — docs/renderer-jams/20261029-finite-yaw-overflow/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-finite-yaw-overflow/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-finite-yaw-overflow/REVIEW.md |
| Preserved source SHA-256 | `4532bac3de212e697d32e097c913d68993f97d8352b85e1b25ac3b0cd5a01096` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Pins the corrected angle helper at ce3ff1d, with 15 Java21 runs, 1,418,131 assertions, and the original helper failing the overflow witness. The review discloses excluded near-boundary randomized pairs and a 29–34% slower ordinary-angle arithmetic fixture, with no native client execution.

## Research expansion — gaps and details to deepen

- Random finite-pair coverage intentionally excludes 1,010 near-boundary pairs, so aggregate assertion counts do not alone establish every threshold decision. Renderer getter integration and the cost of two comparisons in a real extraction remain separate unrun gates.

## Research expansion — proposed contracts

- Add an explicit numerical acceptance ledger separating extreme-finite witnesses, exact/adjacent threshold tests, random exclusions, nonfinite fallbacks, and inherited pose coverage. Preserve the adverse fixture and link jam30 as its later source optimization without retroactive performance credit.

## Research expansion — acceptance checks

- Execute targeted boundary cases and full camera-adapter compilation, then profile the actual extraction path rather than a duplicated arithmetic loop. Keep fine visibility authoritative, validate reset/failure behavior, and record native CPU/GPU tails and water pixels before interpreting any frame impact.

## Research expansion — source anchors

- Renderer jam 29 — read-only self-audit (not independent engine audit)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 29 — read-only self-audit (not independent engine audit)

**Jam:** `renderer-20261029T0855-finite-yaw-overflow`; **baseline** `c08932fba9431cbef4badfaf5b7735d7553f1bf1`; claim commit `0ba0fad77b3fcb457a27514c2e1685f14c550bba`; implementation `1642c710c65c54c6c428cff1e37cbdafe744b3c7`; **exact final reviewed source+test** [`ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad`](https://github.com/Shnewbs/Deep-Primal/commit/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad). Code was frozen before this review; only scoped documentation follows. Source, test and caller identities were pinned in GitHub; 12/12 read-only integration checks passed.

## Finding, actual edits and ownership
Read current main, missing `AGENTS.md` (no file in tree), engine and water work-state, LOD/drainage architecture, graphics/upscaling documentation, scoped writer leases and claims, renderer backlog, jam28 review and independent [Dev74 audit](../../engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md). Dev74 identified finite extreme-yaw float subtraction overflow to NaN after modulus. The existing fine-part culling stayed authoritative; only an optional coarse CPU sampling opportunity was missed. All older renderer claims inspected were released. The claim for this jam is limited to the LOD angle gate and this jam's documentation/tests; no engine/water/other module ownership.

**Production edit:** [`LodPageFrustumAdaptiveGate.java` lines 58–73](https://github.com/Shnewbs/Deep-Primal/blob/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java#L58-L73), Git blob `8fbcc9c4018dd5dd6c7093c59857132ab14361fe`. Preserve ordinary ±360° float calculation, reduce each large *finite* angle modulo 360 in double precision before subtraction; nonfinite input conservatively forces a sample. **New test:** [`LodPageFrustumFiniteYawChecks.java` lines 29–66](https://github.com/Shnewbs/Deep-Primal/blob/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad/docs/renderer-jams/20261029-finite-yaw-overflow/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumFiniteYawChecks.java#L29-L66), blob `5d747f87830d8a48da0bbc7cb3820e80c04c08b4`. [Unchanged `VoxelLodRenderer.java` call/extraction](https://github.com/Shnewbs/Deep-Primal/blob/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L597), blob `2bb1c95d9b800da7b036d0b1a89f4249afca3c87`; unchanged page bounds `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc`. All Git writes used scoped blob/tree/commit and fresh guarded non-force expected-parent ref updates; no branch/Actions/other writer files modified.

## Exact executed checks and limits
Linux OpenJDK **21.0.12.1**. The owner JDK25 archive/install was not available in this workspace; no Java25 claim. Compiled exact committed production gate, page bounds, previous committed renderer regression classes and explicitly test-only Minecraft/GPU mesh doubles:

```sh
find src tests stubs -name '*.java' > results/sources.list
javac --release 21 -Xlint:all -Werror -d classes @results/sources.list
# For each value true and false:
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumFiniteYawChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumPoseChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumToggleChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumBoundsChecks
# Three additional page-option checks with pageFrustumBroadphase=false, FALSE, true.
```

**PASS:** 15 Java21 runs, **1,418,131 passing assertions**, exact source/test Git-object matches and 12/12 pinned integration predicates. Finite-yaw checks true/false **77,365 each** including 38,671 random finite pairs per mode (1,010 near-boundary pairs excluded); pose **23,061/23,057**, adaptive **46,095/46,095**, page option **60,005/60,005**, toggle **80,006/80,006**, bounds **332,530/332,530**, override **60,003/60,003/60,005**. Negative check compiled the original unmodified production gate and failed at **assertion 3**, correctly detecting the bug. An initial read-only static predicate used the wrong caller field name; corrected to actual `pageFrustumGate` and passed 12/12 without code modification.

**Measured *adverse* synthetic CPU angle arithmetic:** two paired 31-sample fixtures, each 8×10,000 ordinary angles: old median **289,797 / 276,957 ns**, final **373,813 / 372,000 ns** (roughly 29–34% slower inside this narrow arithmetic fixture). A first fully double-based candidate was worse at ~467–484k ns and superseded before the scored read-only review. Only two angle comparisons are made per optional extraction; no frame-time/FPS benefit or native regression magnitude inferred from this microbenchmark. Prior bounds timings reproduced but don't measure the edited gate's real-world effect.

**Unrun per owner build override:** Java25 Gradle 9.2.1/NeoForge full build, qualified JAR/native payload, real GPU pixels, Vulkan/OpenGL/Windows/RTX, water alpha/order, camera A/B frame-time p50/p95/p99, VRAM/RAM, reload/resize/world-switch/save/reopen/shutdown, actual temporal FSR or DLSS evaluate dispatch.

## Six equal-weight renderer categories (out of 10)

| Category | /10 | Evidence / limitation |
| --- | ---: | --- |
| Distance/LOD correctness/usable coverage | **6.55 carried** | Unchanged native handoff and [fine visibility](https://github.com/Shnewbs/Deep-Primal/blob/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L610); distant unseen-terrain delivery unproven |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Prior splitter audit](../20261028-split-preflight-once/REVIEW.md); no mesh changes |
| GPU culling/submission/memory | **4.55 carried** | CPU gate fix only; no GPU indirect/occlusion/VRAM gain |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [FSR1 spatial implemented; DLSS runtime unavailable](../../UPSCALING.md); temporal FSR/RTX unaccepted |
| Visual/backend/settings | **5.30 carried** | Unchanged [ordered fine-part fallback](https://github.com/Shnewbs/Deep-Primal/blob/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L610); native water pixels unrun |
| Validation & measured performance | **5.95 (+0.05)** | 15 strict JVM runs, 1,418,131 assertions, negative witness rejected, 12/12 pinned checks, **adverse CPU fixture disclosed**; no frame/GPU metric |
| **Equal-weight average** | **5.72 (+0.01 displayed)** | (6.55+6.95+4.55+5.00+5.30+5.95)/6 = **5.7167** vs jam28 **5.71**. Independent whole-engine grade **5.0/10 carried**, not changed. |

## Delivery / next
Source-only evidence ZIP `DeepPrimal_Renderer_FiniteYaw_20261029_ce3ff1d_SourceEvidence.zip`, **25,098 bytes**, SHA256 `a75703c45913108f35f936b7db9fc6167f0abd81b61762e2580f837637e0c3c9`; **19 entries, CRC and per-member SHA256 manifest PASS**, successful Library upload `/Deep Primal/Renderer Jams/`. **Not a development JAR** and no Github release/CurseForge.

Next renderer-only gate: owner Java25 full build, named Vulkan/OpenGL three-way fixed/auto/off broadphase A/B, offscreen/all-visible and camera-turn travel, p50/p95/p99 CPU/GPU and VRAM, draw calls, transparent water pixels, reload/reopen/shutdown. Continue measurable mesh/GPU submission work or actual vendor temporal upscaling only after dependency readiness; keep separate schedules and no speculative FPS claim.


<!-- END PRESERVED SOURCE -->
