# Expanded replica — docs/engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md |
| Preserved source SHA-256 | `37a4799a3d529fe035b84b1a6461891aa3fe214136a632b6ea3d4a8dab1a76f5` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev71 report inspects opt-in route v4, which supplies the same admitted boundary sea seed to ocean classification and the real regional watershed constructor. It identifies propagation of the prior bounded marine heuristic into flood-root authority as an open source-path risk, not a newly reproduced native failure. Its independently executed Java25 evidence concerns the exact LOD page-bounds class, parity/metamorphic fixtures and a rejected mutant; the production watershed integration and broader Builder suites were not independently executed.

## Research expansion — gaps and details to deepen

- Sharing one seed removes disagreement between consumers but can also spread an invalid authority decision. Consistency between two subsystems does not establish correctness of their shared input.
- A production constructor call inspected in source and a fixture execution against a stand-in watershed cover different integration boundaries; the acceptance graph must name the missing real dependency path.

## Research expansion — proposed contracts

- Model every sea seed as a typed authority record with world/recipe revision, lattice coordinate, certifying source, completeness, and rejection reason. Downstream flood roots inherit this provenance.
- Add an evidence dependency edge from marine certification to regional priority-flood roots, then to lake stage, receiver/discharge planning, native placement, and runtime water ownership.
- Keep page-union culling results in their renderer claim family, including exact production helper identity and retained fine per-part tests; do not let the large assertion count obscure the hydrology integration gap.

## Research expansion — acceptance checks

- Inject a falsely admitted marine boundary and prove that the native watershed gate rejects or explicitly marks its derived lake/flood outputs unqualified.
- Exercise the optional authority and null legacy paths against the real WatershedRegion graph with signed boundaries and saved V1–V7 parity before native promotion.

## Research expansion — source anchors

- Deep Primal independent Dev71 audit — 2026-10-29T05:26Z
- Fresh implementation evidence
- Executed independent checks

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent Dev71 audit — 2026-10-29T05:26Z

**Immutable pinned main source:** `9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe`; prior Dev70 audit source `c08e0046d81c97315839c67ea5638b9c59cc3c0c`. Independent auditor; read-only production and Builder role. Scoped work-state says Dev71 Builder lease was released at 05:19 UTC; root work-state remains Dev53.

## Fresh implementation evidence

- [WatershedRegion.java L255–264, L297–314, L414–422](https://github.com/Shnewbs/Deep-Primal/blob/9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L255-L264): optional exact-lattice ocean-boundary authority; null preserves previous V1–V7 floor/seed path.
- [V8RoutedWaterSource.java L86–135, L224–265](https://github.com/Shnewbs/Deep-Primal/blob/9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe/src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java#L86-L135): opt-in saved/fingerprinted route policy v4; same admitted boundary sea seed goes to connected-sea mask and real regional watershed constructor.
- [V8OceanConnectivity.java L226–281, L327–354](https://github.com/Shnewbs/Deep-Primal/blob/9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java#L226-L281): exact 32-block component seed admission is bounded to eight regions, and `marineCoreSeen && unresolvedWetGateway` remains sufficient after the cap. **Open source-path risk:** prior Dev70 closed 11-region inland-basin witness can now contaminate priority-flood sea roots rather than only classification. NOT a newly reproduced native Dev71 failure; real production watershed native integration untested.
- [LodPageFrustumBounds.java L15–50](https://github.com/Shnewbs/Deep-Primal/blob/9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L15-L50) blob `90e0b76d68471c0636ce37265649ef8df5989177`; [VoxelLodRenderer.java L549–573](https://github.com/Shnewbs/Deep-Primal/blob/9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L549-L573) retains ordered individual tests after a conservative multi-part page union. This is CPU culling, not GPU acceptance.

## Executed independent checks

Owner-uploaded Temurin JDK **25.0.5+8** restored in isolated Linux audit resources; Java and javac verified. Pinned Github source blob/static path checks **24/24** pass. Compiled the **exact** page-bounds production class with isolated two API doubles using `javac --release 25 -Xlint:all -Werror`; independently reran renderer's committed regression twice (**332,530 assertions each**), and authored additional metamorphic checks (**499,558 assertions / 25,000 scenes**). **Total 1,164,618 Java25 assertions passed.** One independently compiled wrong-minimum-bound mutant rejected at assertion 2. Synthetic 4,096-page CPU-only median old/new durations: 133723/53191 and 132881/52319 ns; 5,534,186 synthetic frustum calls avoided per run. These are not native FPS/tail improvements. Builder's 19 Java21 API-double suites / 21,012,727 checks were **not independently rerun**.

## Equal-weight whole-engine rubric

Last accepted full [review](../../engine-review-2026-10-24/REVIEW.md) is **carried, not freshly regraded**: terrain 5; biomes 4; hydrology/caves 6; features/structures 4; performance 3; determinism 7; architecture 6; extensibility 4; testing 7; polish 4. **50/100 = 5.0/10, Tier 2/5**. A new complete review **strictly >8.5** still required. Staged R57/R60/R61 receive no acceptance credit.

## Not run / artifact status

**No current development JAR.** The latest October 27 owner instruction assigns complete Gradle/Minecraft/GPU building to the owner. An independent local Git CLI clone was additionally blocked by `Could not resolve host: github.com`; only selected pinned connector source + retained source evidence were available. No full Java25 Gradle9.2.1, NeoForge, real WatershedRegion production compilation, complete JAR/nested CRC/class identity, native save/reopen/shutdown, V1–V7 replay, finite-water volume/chemistry restart, Windows AMD8060S Vulkan/OpenGL, or GPU FPS p95/p99 testing. No release or JAR retention mutation. The separately verified 15-member CRC/SHA256 source-only audit evidence ZIP is **15,568 bytes**, SHA256 `fbb118a796377ea9de0dc1b6458fb98b77812b1ecb6a096c87eb3e089032b8f7`, saved to Library `/Deep Primal/Independent Audits/DeepPrimal_Dev71_IndependentAudit_20261029T0526_9c2fe3a5_Java25Evidence.zip`.

**Next:** eliminate bounded false marine outlets through global deterministic receiver connectivity, validate cross-region priority flood/flow/lake spill and legacy parity in actual WatershedRegion; then owner native saved-profile + water conservation + Windows Vulkan/OpenGL acceptance. Builder not interrupted.


<!-- END PRESERVED SOURCE -->
