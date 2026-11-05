# Expanded replica — docs/validation/engine-continuation/v8-ocean-outlet-seeds-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-ocean-outlet-seeds-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-ocean-outlet-seeds-2026-10-29/README.md |
| Preserved source SHA-256 | `ac39e9749428b872e1075fbf7ff5c4a19092fb5b56660d8f32d0d84f670a06dd` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev71 checkpoint introduces opt-in routing policy v4 and an exact-lattice marine-seed interface feeding a new optional production WatershedRegion boundary predicate. It separates per-vertex seed admission from four-corner surface classification and removes forced sea elevation only for the explicit new caller. Null preserves legacy behavior. The receipt reports 19 isolated Java21/API-double suites, bounded witness-cache tests, and mutation rejection, but states that the changed production WatershedRegion was not independently compiled. Modeled lower stage therefore does not establish native basin, spillway, or cross-region hydraulic behavior.

## Research expansion — gaps and details to deepen

- Actual evidence gap: the production constructor and flood-stage changes were inspected and edited, while executed fixtures used a WatershedRegion double. Wiring a predicate in the model cannot qualify the real priority-flood algorithm.
- Actual algorithm limit: per-vertex marine admission still inherits the bounded v3 heuristic and its large-inland-basin risk. Exact lattice alignment does not itself prove the source is globally marine.
- Elaboration: the new authority callback needs explicit failure, purity, profile identity, and stage-unit contracts; null legacy behavior and unresolved new-policy evidence must remain distinguishable.

## Research expansion — proposed contracts

- Specify the boundary authority as a deterministic read-only function of saved world/profile, exact 32-block lattice position, and source revision. Reject nonfinite or misaligned inputs and report unresolved provenance without silently supplying a sea seed.
- Treat initial flood height and downstream boundary-root stage as separate algorithm outputs with explicit conditions for sea-stage substitution. Preserve the old constructor and all frozen-policy results exactly.
- Key the 256-entry component witness cache by source policy and world identity, define eviction/reconstruction behavior, and retain the evidence scope of each cached witness.
- Add a production-dependency gate compiling and exercising the actual WatershedRegion implementation before interpreting adapter/model stage results as terrain correctness.

## Research expansion — acceptance checks

- Test null legacy and opt-in v4 constructors on the same region corpus; require exact old behavior for the legacy path and documented seed/stage differences only for v4.
- Compare exact per-vertex seeds with four-corner classification near mixed shores and both signed 8192 boundaries.
- Exercise dry seeds, unresolved large components, misaligned coordinates, invalid saved fingerprints, and cache eviction without silently granting marine authority.
- Run actual regional flood/lake-stage and spillway fixtures with genuine saved-profile reopen, then separately audit native water/constituent quantities.

## Research expansion — source anchors

- Source changes
- Executed verification
- Next gates

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev71 — V8 outlet-seeded priority flood (source-only)

This cycle consumes the latest completed independent source audit at `docs/engine-hourly-audits/2026-10-28T2331-d99837b/REVIEW.md` (pinned `d99837bb76ddfa6fd56cb5f133eb5021dde8c131`); carried historical full grade **50/100, 5.0/10**, unchanged. Current source version `0.0.1-alpha.29-dev.71-v8-outlet-seeds.1`.

## Source changes

* Actual production `WatershedRegion` now accepts an optional deterministic ocean-boundary authority at exact 32-block sea-edge lattice vertices. Under this new explicit caller only, it omits the legacy forced sea elevation for initial flooded heights and downstream boundary-root stages. **Null preserves all legacy V1–V7 and saved V8 routing behavior**. The new caller must be tied to saved world source identity.
* `V8OceanConnectivity.isMarineSeedAt` distinguishes exact source-bound per-vertex marine admission from legacy four-corner surface material classification. Adds bounded 256-entry component witness cache and retains v3 bounded search for fully enclosed deep basins; the v2/v3 `isMarineAt` path remains unchanged. There are still false-positive risks for inland basins larger than its search budget.
* `V8RoutedWaterSource` adds *opt-in*, separately domain-fingerprinted route policy **v4** and `authorOutletSeeded` / `outletSeedFingerprint` / saved r5 reopen handling; the same admitted marine-edge seeds now feed its regional sea mask AND the actual production `WatershedRegion` priority-flood constructor. Older routing v1-v3, saved profiles/graphs, chemistry and finite water amounts remain untouched.

## Executed verification

Linux OpenJDK/javac **21.0.12.1**. Portable `run_focused.py` compiled 45 selected source and API-double files, 19 Java21 suites with `javac --release 21 -Xlint:all -Werror`, and executed `java -ea -Xverify:all`: **19/19 suites PASS, 21,012,727 aggregate fixture assertions/checks**. Included new closed deep-basin/nonmarine seed, open corridor seed, signed seam, bounded cache and saved r5 v4 fingerprint/reopen checks. A first failed testing attempt used an unreachable continentalness node in a *test-only* graph; fixed by connecting it through a zero-weight density node, without changing product output. Three independently compiled broken-control variants were correctly rejected under `python3 -OO`: skipping boundary-marine gate, skipping modeled priority-flood seed predicate, trusting a closed lake as a sea seed. Separate logs/receipts and exact source Git blob IDs in `results.json` and portable source-only ZIP.

**Critical exclusions:** Java21 selected production V8 classes compiled with isolated Mojang and `WatershedRegion` API doubles. The actual modified `WatershedRegion.java` was inspected and changed from current Git content, **but not independently compiled**; the modeled lower basin stage is not an actual native stage, lake spill or geography proof. No Java25/full Gradle9.2.1, real Mojang codec, native chunk or saved-world reopen, whole-engine legacy replay, real cross-region discharge, finite-water chemistry conservation, gameplay world seam, OpenGL/Vulkan or CPU/memory benchmark. `tools/test_ecotone_blend.py` was not rerun (component unchanged). No JAR produced or published per October 27 owner instruction.

## Next gates

Implement authoritative saved global ocean-outlet topology, cross-region priority-flood receiver/flow area and lake spill/discharge conservation rather than bounded wet-area heuristics; verify actual WatershedRegion Java25 full/native parity across signed watershed and chunk borders and real saved profile reopen. Continue water quantity/constituent loaded/unloaded transaction and owner-side Vulkan/OpenGL visual latency checks. No legacy save conversion or native V8 registration is authorized by this source-only checkpoint.


<!-- END PRESERVED SOURCE -->
