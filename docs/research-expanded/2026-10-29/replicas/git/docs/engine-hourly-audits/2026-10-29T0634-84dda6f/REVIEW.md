# Expanded replica — docs/engine-hourly-audits/2026-10-29T0634-84dda6f/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-29T0634-84dda6f/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-29T0634-84dda6f/REVIEW.md |
| Preserved source SHA-256 | `56cb4ed0f94f52243d429cba826c4708a8f71565f73311cf22b7429e394e78ff` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This Dev72 audit inspects saved route v5 and the graph-authored, Y-independent ocean_domain source, while retaining the limitation that a negative mask does not establish global marine connectivity. Independent Java25 work validates exact LOD bounds/option classes with API doubles, multiple option modes, an independent AABB suite and a rejected mutant. The report also retains the all-visible broadphase regression and the absence of native GPU A/B evidence. Its whole-engine grade is carried.

## Research expansion — gaps and details to deepen

- Authored masks, connected-component admission, verified outlet topology and physical ocean ownership are distinct data products; a common ocean label can otherwise hide which guarantee a consumer received.
- The coarse-frustum result has both favorable and adverse workloads. Acceptance should use an explicit workload matrix rather than retaining only the mostly-offscreen improvement.

## Research expansion — proposed contracts

- Give ocean_domain a schema documenting sign convention, coordinate frame, Y-independence, sampler bounds, source fingerprint, and whether it is a candidate mask or a certified marine authority.
- Validate authored masks against connectivity or preserve unresolved authority explicitly. Add negative-mask enclosed basins and disconnected wet islands to the source-graph fixture set.
- Require a default/effective/experimental status and adverse-scene timing record for the broadphase gate; retain fine culling as the correctness oracle and separate CPU function cost from complete frame cost.

## Research expansion — acceptance checks

- Test intentionally wrong enclosed-basin masks and all corner combinations across signed tile edges, with cache invalidation when the saved source fingerprint changes.
- Measure all-visible, mostly-offscreen, mixed, rapidly turning and stationary scenes with identical visible output before qualifying the gate on either native backend.

## Research expansion — source anchors

- Deep Primal independent Dev72 audit — 2026-10-29T06:34Z
- Verified delta
- Independently executed

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent Dev72 audit — 2026-10-29T06:34Z

**Audited immutable main:** `84dda6fb293647482b560c3a13cb47a98707e8d0`; **implementation:** `adbefcc16b3c5ac16526a93c2024a2cff3b05d75`. Previous independent Dev71 source `9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe`. Read-only auditor; Builder not interrupted.

## Verified delta
Dev72 adds opt-in saved V8 route v5 with explicit Y-independent graph `ocean_domain`, negative-mask wet 32-block ocean seed admission, four-corner checks and source fingerprint. [Ocean connectivity](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java) blob `d1be856365be1b2368d8a87f305311389451b0b7`; [routed source](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java) blob `e21c7be748340668c274ac803472967c9530516b`. Real [WatershedRegion](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L255-L325) optional seed callback inspected but not compiled natively. Older V1–V7 path retains null ocean authority. Root work-state still Dev53; scoped Dev72 lease is released.

## Independently executed
Temurin **25.0.5+8** restored and `java`/`javac` verified. Git SHA-1 blobs of exact copied [LOD helper](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java) `3ec09a3ef7c4aa217510f0195d5e67c9caf62fbc` and [test](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/docs/renderer-jams/20261029-frustum-ab-gate/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumOptionChecks.java) `7f064130651be66ce7205cbb0db3f5e12b0dec84` matched published objects. `javac --release 25 -Xlint:all -Werror` and `java -ea -Xverify:all` with production-excluded two API doubles. Four exact test runs default/false/FALSE/true passed **60,005/60,003/60,003/60,005 assertions**; independent 25,000-scene AABB safety suite passed **412,258**. Total **652,274 passing Java25 assertions**. One independently compiled broken union omitting the last part was rejected at assertion 51. All source-only, **not native GPU**.

Builder's [Dev72 receipt](https://github.com/Shnewbs/Deep-Primal/blob/84dda6fb293647482b560c3a13cb47a98707e8d0/docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/results.json) claims **19 Java21 focused suites / 21,013,270 checks**; these were **not independently rerun**. Renderer jam26 synthetic CPU all-visible case regressed with coarse broadphase, despite mostly-offscreen improvement; actual AMD Vulkan/OpenGL A/B untested.

**Major remaining gap:** graph-authored `ocean_domain` is not a demonstrated globally connected marine/outlet source; an incorrectly negative enclosed-basin mask is not rejected by topological validation. Native cross-region spill/discharge, finite water save/reopen, V1–V7 replay, owner visuals and GPU frame tails remain unqualified.

**Equal-weight rubric (carried only, not fresh):** terrain 5, biomes 4, hydrology/caves 6, features 4, performance 3, determinism 7, architecture 6, extensibility 4, tests/docs 7, polish 4. **50/100 = 5.0/10, Tier 2**; target strictly >8.5 remains open.

**No JAR built.** Oct 27 owner override reserves full Gradle/Minecraft/GPU to owner; independent Git CLI failed DNS, no full checkout/Gradle 9.2.1/dependencies. No bundled/native/Windows testing; no development release, binary promotion or three-JAR retention mutation. No production code changed. Audit evidence: Library `/Deep Primal/Independent Audits/DeepPrimal_Dev72_IndependentAudit_20261029T0634_84dda6f_Java25Evidence.zip`, 10,964 bytes, SHA256 `f012cf338223952e26aafca4178da2e5a879e644e91ad747662f05a3938c7930`, 16 members, CRC pass.


<!-- END PRESERVED SOURCE -->
