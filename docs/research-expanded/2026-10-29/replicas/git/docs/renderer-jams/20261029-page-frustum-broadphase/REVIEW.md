# Expanded replica — docs/renderer-jams/20261029-page-frustum-broadphase/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-page-frustum-broadphase/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-page-frustum-broadphase/REVIEW.md |
| Preserved source SHA-256 | `40bcf56fd271f31205cf8d9e8e349f286eff317eb35d9ba087dbf367daeba8c3` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Pins 9fd9cd0’s full-union helper and admission binding. Two 332,530-assertion Java21 runs cover containment, ordered visibility, malformed fallback, and a rejected first-part-only mutant. Warm multi-part fixtures reduce CPU frustum calls, but visible pages incur an additional coarse test.

## Research expansion — gaps and details to deepen

- The helper uses test-only AABB/mesh APIs and conservative clipping planes, with source inspection rather than native host execution. Cached bounds, actual frustum conventions, dynamic geometry validity, and adverse all-visible costs remain separate integration questions.

## Research expansion — proposed contracts

- Add a native bounds/projection conformance receipt and geometry-revision validity contract. Link jam26’s A/B gate and explicit all-visible regression so this original favorable fixture is not treated as universal performance evidence; preserve complete-page promotion ownership.

## Research expansion — acceptance checks

- Retain containment and negative mutation tests, then verify real host frusta, near-plane crossings, signed/far coordinates, water-height changes, and cache replacement on both backends. Compare complete cull/frame distributions and pixels, including bound-construction cost and an all-visible adverse view.

## Research expansion — source anchors

- Renderer jam 25 — conservative multi-part page frustum selection
- Scope, consumed findings and changed files
- Actual tests and matched evidence

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumBoundsChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-page-frustum-broadphase/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumBoundsChecks.java) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 25 — conservative multi-part page frustum selection

**Jam:** `renderer-20261029T0502-page-frustum-broadphase`.
**Exact reviewed source/test commit:** [`9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9`](https://github.com/Shnewbs/Deep-Primal/commit/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9); implementation [`fb847d35a9d2cf33e61e0d13694a00fd1295f008`](https://github.com/Shnewbs/Deep-Primal/commit/fb847d35a9d2cf33e61e0d13694a00fd1295f008), parent scoped claim `c42d874515e8e5986277c845809815349519b0d0` and preclaim main `561e332ec651e3254911b55f40853a29e01e0209`.
**Version:** no bump; latest separate Dev70 engine version from independent auditor is `0.0.1-alpha.29-dev.70-basin-closure.1`. This is the **renderer author's read-only self-audit** of the exact candidate, not an independent whole-engine regrade; no code was changed during the scored review.

## Scope, consumed findings and changed files

Read the current main/ownership claim, absent `AGENTS.md`, engine/water work-states, LOD/drainage and UPSCALING documents, renderer backlog and [jam24 review](../20261029-squared-error-selection/REVIEW.md), plus [Dev70 independent audit](../../engine-hourly-audits/2026-10-28T2132-c08e0046/REVIEW.md). Jam24 called for native LOD/GPU baselines; no owner GPU reservation or Java25 runtime is available here. The Dev70 enclosed marine-basin issue remains **engine-owned** and was not modified. This source jam proceeds with a conservative renderer-only visibility optimization and honest CPU fixture evidence.

- [`LodPageFrustumBounds.java` L15–50](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L15-L50), blob `90e0b76d68471c0636ce37265649ef8df5989177`: union cached world-space AABBs across every mesh part in a resident LOD detail. Empty/singleton or malformed/nonfinite parts disable coarse rejection rather than causing false negatives. Bounds are built only at complete GPU-page promotion ([`VoxelLodRenderer.java` L759–771](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L759-L771)), blob `d4dd077ba46b859d944c7deb769129b541f4c4b8`.
- [`VoxelLodRenderer.java` L545–585](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L545-L585): one conservative page frustum test on multi-part levels, followed by original ordered per-part tests if not rejected. Native-section handoff, LOD selection/hysteresis, opaque/transparent geometry, GPU pipeline, page residency, worker and streaming bounds remain unchanged.
- [`LodClient.java` L90–97](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L90-L97), blob `994ac5eb10ae72335bb2f0c7c2754651dd76e1ba`: report `lodCoarseRejectedPages` and `lodCoarseSkippedPartTests` separately. Existing tested/rejected counters retain **fine tests only** and reset per world ([L925–930](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L925-L930)).
- Source-excluded [deterministic test](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/docs/renderer-jams/20261029-page-frustum-broadphase/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumBoundsChecks.java), blob `fc3388572a359644cd3921a1d22e14375174c559`, and two test-only API stand-ins (blobs `de1a109654659a3984034fe09d2b7b904a9f74ad`, `b9b0bee3aab36cb3b64ecd2485ecf7d377f9feb9`) remain under `docs/renderer-jams`, not the production module. [Scoped README](README.md) also changed.

These seven files comprise the complete candidate diff after the scoped claim. No engine/water state, terrain generation, textures, shaders, FSR/DLSS provider, owner-build outputs or other automation was modified.

## Actual tests and matched evidence

Host Linux OpenJDK **21.0.12.1**; JDK25 and owner-provided archive were absent from this workspace. Direct `git ls-remote` failed on DNS; verified GitHub connector tree/blob identities and pushed with guarded expected-parent refs. Exact committed helper/test/stub Git blob IDs match compiled files. Executed:

```sh
javac --release 21 -Xlint:all -Werror -d classes stubs/net/minecraft/world/phys/AABB.java stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java src/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumBoundsChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumBoundsChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumBoundsChecks
```

**PASS twice:** **332,530 deterministic assertions each**: 12,000 seeded multi-level/negative-coordinate scenes, all part-union containment, unchanged ordered visibility under seven conservative clipping planes, empty/singleton fallback, explicit first-vs-second-part counterexample, invalid/nonfinite input fallback and test-list immutability. **PASS mutation negative:** replacing the full union with the first part's box compiled, then failed on containment assertion #10 as expected. The read-only pinned production integration/source review passed **19/19** checks with no code edits.

Matched, warmed, same-process **CPU-only** 4,096-page × 6-part, 301 paired samples per run (bounds precomputed at cache admission):
- Run 1 old/new median **214,390 / 73,080 ns**.
- Run 2 old/new median **234,232 / 73,151 ns**.
- Total synthetic frustum calls per repetition: **7,397,376 → 1,863,190**, or **5,534,186 fewer** (~74.8%). Visible pages still incur an extra coarse check, so performance depends on camera coverage/part count. These are not full-frame p50/p95/p99, GPU timing, VRAM, actual FPS, memory stability or water-pixel measurements.

**Evidence archive**: `DeepPrimal_Renderer_PageFrustumBroadphase_20261029_9fd9cd0_SourceEvidence.zip`, **7,924 bytes**, SHA256 `e98e5f2a43eed47df9624bb71b478c1bbaab19c55b001994bae69ff930aeddab`, 10 members: ZIP CRC and every listed SHA256 manifest passed. Successfully saved to Library `/Deep Primal/Renderer Jams/`. Source+test evidence, not a compiled mod.

## Six equally weighted renderer scores

| Category | /10 | Pinned basis and missing acceptance |
|---|---:|---|
| Distance / LOD correctness and usable coverage | **6.55 carried** | [Native handoff and prior selector](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L538-L558); no new distance/unexplored coverage or native silhouette proof |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Prior splitter](../20261028-split-preflight-once/REVIEW.md); no mesh-worker/build change |
| GPU culling/submission/memory | **4.55 (+0.05)** | [Cached CPU frustum broadphase](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L550-L572) and 5.53M fewer synthetic CPU tests; **not GPU occlusion, indirect or measured hardware benefit** |
| Actual FSR/DLSS | **5.00 carried** | [Real FSR 1 spatial, temporal providers/NGX game dispatch incomplete](../../UPSCALING.md); no shader/provider changes |
| Visual/backend/settings compatibility | **5.25 carried** | [Unchanged part loop and cached world-space box validation](https://github.com/Shnewbs/Deep-Primal/blob/9fd9cd02ede557eb7ded4ef3440e2af08cc2f7d9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L15-L49); water pixels, Vulkan/OpenGL and game lifecycle unrun |
| Validation and measured performance | **5.75 (+0.05)** | [Pinned source-only regression](tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumBoundsChecks.java), 2 × 332,530 assertions, negative mutant failure, 19/19 pinned checks and paired CPU fixture; no GPU frame-time or tail measurement |
| **Equal-weight mean** | **5.68 (+0.02 displayed)** | `(6.55+6.95+4.55+5.00+5.25+5.75)/6 = 5.675` vs prior renderer jam24 **5.66**. Independent whole-engine baseline remains separately **5.0**; no regrade |

## Unrun gates / next repair

**Not run:** complete Java25 Gradle9.2.1/NeoForge, archived/compiled/packaged class identity and JAR, real Minecraft native frame/resource/resize/reload/world-switch/save/reopen/shutdown, transparent-water pixels and native-section handoff, Vulkan/OpenGL driver frustum/occlusion behavior, CPU/GPU frame p50/p95/p99, draw/descriptor/VRAM/RAM, memory pressure, AMD Windows performance, temporal AMD FSR or NVIDIA RTX NGX DLSS. Owner performs builds and GPU acceptance under October 27 override; no fresh JAR, release, Windows certification or GPU FPS gain is claimed.

**Next priority:** owner named-hardware batched/nonbatched and coarse-page A/B on/off (including water), actual screen composition and LOD cache churn; measure p50/p95/p99 CPU/GPU/draw/VRAM. Avoid forcing coarse tests on singleton and measure adverse all-visible views. Then advance actual temporal upscaler inputs/provider dispatch within renderer scope and with vendor licensing.


<!-- END PRESERVED SOURCE -->
