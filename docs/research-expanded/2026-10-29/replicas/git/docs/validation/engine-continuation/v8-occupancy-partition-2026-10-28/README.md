# Expanded replica — docs/validation/engine-continuation/v8-occupancy-partition-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-occupancy-partition-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-occupancy-partition-2026-10-28/README.md |
| Preserved source SHA-256 | `a51369cdc83a0696c58161425ab67157b8e8a690a2cab9415ae8c922cf92069d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev43 deterministic disjoint integer-box partitioning with 0–256 classifications and dimensions up to 64 per axis, emitting UNCERTAIN tiles when budget expires. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that classification admission bounds do not prove native speed or allow uncertain voxels to disappear from occupancy. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an exact-cover planner invariant with disjoint output, complete coverage, conservative classifications and mandatory downstream exact sampling. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject mutants that drop exhausted tiles, overlap halves or classify unknown space empty, then compare complete graph and native materialized occupancy before measuring any optimization benefit. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev43 V8 bounded occupancy partition — source-only checkpoint

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev43 V8 bounded occupancy partition — source-only checkpoint

**Version:** `0.0.1-alpha.29-dev.43-v8partition.1`. Engine work on `main`, no native activation, no JAR or release. Consumed independent audit [2026-10-28T0134-4fc83e6](../../../engine-hourly-audits/2026-10-28T0134-4fc83e6/REVIEW.md) pinned to `4fc83e6021df26d2f3df26ef8afa563db6650fe9` (subsequently committed by read-only auditor at `c0e7e2d`). Historical fully reviewed grade remains **50/100 (5.0/10)**, not regraded.

## Concrete implementation

- Added `DensityVolumePlan`: deterministic, disjoint, exact-cover integer-box partitioner for the existing experimental V8 `DensityVolumeBounds`. Hard admission limit **0..256 classifications**, dimensions at most 64 per axis; exhaustion emits UNCERTAIN tiles for exact downstream sampling rather than skipping any voxel. Result reports certified solid/empty and fallback counts.
- Added conservative root-coordinate dependency mask (X=1, Y=2, Z=4) in `DensityVolumeBounds`. Traversal includes all warp displacement inputs. The planner avoids splitting axes that provably cannot affect output, and prioritizes vertical subdivisions. This fixes a locally reproduced budget-exhaustion pathology: a Y-only flat plane previously subdivided its numerically uncertain zero layer horizontally until all 256 queries were consumed.
- Registered `DensityVolumePlanChecks` in `CoreChecks`; tests individual/cross-boundary, budget exhaustion, forced UNKNOWN, deterministic replay, full tile coverage/no-overlap, double-output conservation, integer world limits, X/Y/Z and horizontal/3D noise masks, bounded valid WARP, and 28 randomized modeled blocks. No V1–V7 production generator, saved profile, owner art, water ledger or renderer module changed.

## Executed and deliberately unclaimed

- On this isolated host, OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror` and `java -Xverify:all` against Git-identical source blobs plus **API-compatible DensityGraph/NoiseSpec doubles** passed `DENSITY_VOLUME_PLAN_PASS assertions=158901 certified=7936 maxProbes=256`.
- Four deliberately broken variants were compiled and rejected: dropping budget-exhausted tiles, overlapping split halves, false EMPTY on budget exhaustion, and treating Y-only graphs as X/Y/Z dependent. The initial naive three-axis splitter failed its own plane-certification test and was corrected before commit.
- New source/test blob identities: `DensityVolumeBounds.java` `a8f811a2676ae74d93b52506ed747e88f0d354a4`; `DensityVolumePlan.java` `8b7f764f52735ea9c7aa9496037b7a2e61de741b`; `DensityVolumePlanChecks.java` `ec3cc5b2a042bbcbfa614cf9ca03b68e0c8ee4f5`.
- Not executed: owner JDK25 (archive absent in this run), real compiled `DensityGraph` / `SeededNoise3D`, full Gradle/module checks, native V8 block/heightmap/topology/save/reopen, Vulkan/OpenGL, Windows AMD8060S, or frame/tick/memory and water acceptance. **No claim of faster native generation**, engine completion, or qualifying artifact.

## Coordination and next gates

Independent renderer's separate LOD scope was active and unmodified; previous foreground water claim had expired and all water implementation files were left alone. The builder used a bounded main lease and guarded source references. Owner's October 27 override assigns full builds to the owner; no Actions/build/release was requested.

**Next:** Run the registered tests against actual Java25 product graph and native V8 chunk materializer; test exact per-block outputs and saved-profile replay, then measure whether this optional planner saves time under realistic Minecraft tick/cache constraints before ever activating it. Preserve all connected-waterfall, one-owner unloaded transfer, chemistry ledger, save/reopen and named-GPU acceptance gates. Stop only on a fresh independent ten-category average strictly greater than 8.5/10 **and** completed water gates.


<!-- END PRESERVED SOURCE -->
