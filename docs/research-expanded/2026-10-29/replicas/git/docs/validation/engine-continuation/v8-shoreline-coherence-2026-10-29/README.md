# Expanded replica — docs/validation/engine-continuation/v8-shoreline-coherence-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-shoreline-coherence-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-shoreline-coherence-2026-10-29/README.md |
| Preserved source SHA-256 | `79932b190d1ff758ba8d1d6673694bbe4419176aa11245d748d12502180856b4` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev74 introduces opt-in saved routing policy v7 to align water-kind metadata with the same four-corner OR ocean predicate used for routed sea-stage geometry. The historical v6 mismatch could label a mixed coastal cell LAKE while assigning sea stage; v7 changes classification without changing ground, stage, or physical quantity. Earlier fingerprints remain frozen. The receipt reports exact tested/published source identities, 19 Java21/API-double suites, mixed-corner fixtures, and rejected mutations. It retains the authored-domain limitation and explicitly excludes real watershed dependency compilation, native reopen, global discharge, and GPU acceptance.

## Research expansion — gaps and details to deepen

- Elaboration: coherent OCEAN labeling and sea-stage geometry still do not certify global marine connection or authorize replenishment. Classification, initial composition, and physical ocean-boundary ownership need separate consumer rules.
- Actual scope limitation: v7 repairs a four-corner OR/AND inconsistency while retaining an authored ocean domain and unchanged hydraulic stage. It is not a new global routing or conservation result.
- Actual evidence gap: the relevant production WatershedRegion sampling semantics were represented by API doubles rather than compiled with real dependencies; native mixed-shore behavior remains unqualified.

## Research expansion — proposed contracts

- Define one saved-policy classification contract relating sea-stage status, connected corner evidence, graph-domain admission, and water-kind metadata. State which rule each frozen policy uses instead of silently normalizing old profiles.
- Require explicit marine-boundary certification for account exchange and source identity. A v7 OCEAN label alone must not create a renewable emitter, initialize duplicate salinity mass, or convert an inland pool.
- Stamp terrain stage and water-kind metadata with the same accepted plan revision; consumers reject mismatched snapshots rather than combining a current label with old geometry.
- Extend the mixed-corner fixture from metadata to actual production sampling and native shoreline columns, preserving stage/ground equality where the receipt promises classification-only change.

## Research expansion — acceptance checks

- Enumerate all sixteen four-corner marine masks and verify each policy's documented label/stage relationship, including signed seam and corner positions.
- Compare v6 and v7 on mixed shores: require identical stage and ground with only the intended metadata difference; retain exact old-profile reopen.
- Place a falsely authored inland ocean domain and verify classification evidence is not promoted automatically to accounted boundary replenishment.
- Run genuine codecs and native mixed-coast captures, then separately test composition initialization and finite-water restart conservation.

## Research expansion — source anchors

- Actual change
- Focused verification
- Version seven still depends upon a correctly authored `ocean_domain` graph.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev74 — V8 mixed-corner shoreline routing coherence (opt-in)

**Source-only checkpoint.** Reviewed the independent `2026-10-29T0735-529bbe9` audit at pinned source `529bbe95f850ef561f195f99b7827ee53e94f50c`. Its new finding: production `WatershedRegion.sample` uses OR over four connected ocean nodes to assign sea-stage water, whereas V8 v6 routing metadata uses an all-four graph-mask marine check. A 2×2 partial coastal mask therefore produces stage 96 with water kind LAKE, not OCEAN.

## Actual change

`V8RoutedWaterSource` now offers **explicit, opt-in saved routing policy v7**, `authorShorelineAligned` and a domain-separated SHA-256 fingerprint. Its `plan` water-kind classification for a sea-stage cell uses `Region.oceanAt`, the same four-corner **OR** of connected, ocean-domain-admitted nodes that provides the routed sea-stage result. The existing v6 behavior and saved fingerprints are unchanged; saved V8 r5 graph, climate binding, prior policies v1-v6 and legacy V1-V7 geography do not silently migrate.

Version seven still depends upon a correctly authored `ocean_domain` graph. This is no globally connected ocean algorithm. It neither changes routed stage, ground geometry nor physical volume/constituent accounting. It does not activate V8 native gameplay.

## Focused verification

- Source ZIP from committed dev73 identity, Git objects verified before editing: previous route blob `2a19635c511403baf6eea40e60f084aa7abc146c` and previous test blob `dff4ecbe847124bb57cf81516b9c3fde75e5fe5c`.
- Modified route source blob `a4128096a81449b6455f397c0d2fc25b228dbf25`; modified test blob `42760f4d1d5545f109a1a54171a5e2f296fd1ec4`. Both GitHub blob identities matched byte-for-byte locally tested sources.
- OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`; **19/19 isolated source/API-double suites and 21,013,527 assertions/checks**. `V8RoutedWaterSourceChecks`: **5,100** checks.
- Four positive/negative signed-coast seam masks exercise mixed-corner v6 LAKE versus opt-in v7 OCEAN with equal stage/ground, saved-profile modeled reopen, mask-edited source identity, inland exclusion, reverse/parallel query order and bounded caches.
- Two separately compiled broken source variants were rejected by the new coastal assertion: retaining old all-four label, and replacing `Region.oceanAt` OR with AND.
- The positive suite includes API doubles for Mojang/Minecraft and **production WatershedRegion is not compiled with its real dependencies**. The original `tools/test_ecotone_blend.py` runner was absent from this partial portable workspace and not rerun; the existing isolated `EcotoneSurfaceFieldChecks` passed.

See `results.json` and the portable `DeepPrimal_Dev74_ShorelineCoherence_SourceEvidence.zip` from this cycle for complete 19-suite output and two mutation traces. No Gradle 9.2.1/Java25/full NeoForge, native saved-world reopening, Minecraft gameplay, GPU, finite-water lifecycle, cross-region discharge or performance acceptance has been claimed. No JAR was created; owner retains build responsibility.

Independent baseline carried unchanged at **50/100 (5.0/10)**. Next: true global marine/outlet topology, cross-region discharge and lake spill, native mixed-coast/reopen and conservation testing, continuous terrain blending/material transitions.


<!-- END PRESERVED SOURCE -->
