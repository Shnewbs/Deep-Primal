# Expanded replica — docs/validation/engine-continuation/v8-ecotone-native-columns-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-ecotone-native-columns-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-ecotone-native-columns-2026-10-28/README.md |
| Preserved source SHA-256 | `e33fcd2bc3648e742f20d9f65ded9a10090034e157c81241533808cec31db53c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Revision3 opt-in ecotone material preflight for immutable 16x16 column plans, resolving climate and approved states before any setter and excluding hydrological cover. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the path changes surface materials rather than density or water volume, remains unregistered and does not place vegetation from its retained density flags. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a materialization contract preserving bedrock, ground depth and LAND/RIVER/OCEAN ownership while sharing the complete prewrite recovery snapshot. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare signed chunk seams and final heightmaps, reject climate or saved-height mismatch before mutation and verify water-domain cells remain untouched in real native integration. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- V8 opt-in native ecotone material preflight — 2026-10-28

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# V8 opt-in native ecotone material preflight — 2026-10-28

SOURCE-ONLY EXPERIMENTAL V8 revision-3 new-chunk path. It is NOT registered into production generation. No playable JAR, native Minecraft, GPU, Gradle or Java25 gate is claimed.

## Pinned inputs and audit
- Prior V8GeneratorSnapshot blob fecb8c4850730d0c515169c4a467207f3aaf1230.
- Dev53 full prewrite recovery materializer blob d5c2e88ac81b206c7e7ce421c32a3eb18977bf1e.
- Dev57 V8SurfacePolicy blob 9aceaaa11f486d8861eb8fb20bbc5a944030bf60 and EcotoneSurfaceField blob a171ac13e36db1f076ba4ac820c3e1d2ce11ec0e.
- Latest completed audit docs/engine-hourly-audits/2026-10-28T1530-cec6143/REVIEW.md pinned cec61432aa1cb0d4f1909a33d537bd03ca87e31d: code-level surface policy corroborated but no native landscape, real codecs or fresh complete regrade. 50/100 is carried.

## Implementation
- V8EcotoneColumnPlan: bounded 16x16 immutable column preflight; consumes saved-policy continuous 40-biome weights at absolute world coordinates and an external hydrology-owned LAND/RIVER/OCEAN domain. Its weighted soil depth respects the frozen V8 ground depth; bedrock, air and water-domain cover are excluded.
- DensityChunkMaterializer.fillSavedEcotone is an explicit call only for saved V8 revision 3 NEW chunks. It preflights every climate input and all approved material states before native setters, then paints ground grass/podzol/dirt and vanilla snow blocks at the already-existing surface and soil layer. No density/physical height/finite quantity change. It primes final heightmaps once and uses existing full same-chunk prewrite snapshot/rollback on setter failure. The caller must provide a deterministic absolute-coordinate climate producer bound to this saved world.
- V8EcotoneResult retains continuous vegetation density/patch flags but DOES NOT place vegetation or register a generator. No legacy V1-V7 or V8 rev1/rev2 dispatch, saved profiles, player edits, renderer, or water source files altered.

## Focused source/API-double tests (Java21)
On Linux OpenJDK 21.0.12.1, nine focused source suites passed with 14,150,419 explicit assertions. The new V8EcotoneNativeChecks suite passed 27,748 checks and compared 12,288 voxel states; checks include absolute signed chunk seams, deterministic repeats and parallel/reversed sampling, source/height mismatch rejected before writes, protected hydrology domains, final heightmaps, bounded soil depth, no bedrock rewrite and same-chunk postwrite rollback. These used test-only Mojang/Minecraft APIs and a clearly labeled test-only EcotoneBlend provider; they DO NOT certify real game physics, real JsonOps/NbtOps or native save compatibility. Exact source and test harness accompany the user-facing evidence ZIP. Existing component suites and new harness were actually run; full Python tools/test_ecotone_blend.py was not run in this isolated fixture.

OPEN GATES: Java25, full Gradle/NeoForge, real Mojang codecs, native climate producer, saved-world reopen, independent pre-generation base-column query/native climate feed, legacy/new chunk borders, native geology and relief, visual transects, Windows AMD8060S Vulkan/OpenGL, hardware budgets, multiplayer/long-soak and finite-water constituent conservation. No JAR or release produced; owner handles builds. Water and renderer files deliberately not touched. Last full external grade 5.0/10, independent rubric unchanged. Next priority is base-column parity and native climate provider, followed by versioned relief before hydrology.


<!-- END PRESERVED SOURCE -->
