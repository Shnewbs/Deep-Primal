# Expanded replica — docs/renderer-jams/20261026-byte-accounting/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-byte-accounting/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-byte-accounting/BACKLOG.md |
| Preserved source SHA-256 | `3f7eb4d772ca974717552e2ad7c56f65d840ba425f65601370e8714a283de83a` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Records a staged LodMesher.Levels.bytes change from streams to three list sums, using indexed access for RandomAccess lists and iterators for sequential lists. Geometry and GPU payload are unchanged; production and artifact qualification were intentionally not advanced at this checkpoint. The continuation should point to the later byte-accounting promotion instead of leaving this candidate looking current.

## Research expansion — gaps and details to deepen

- The query optimization needs an explicit mutability and ownership contract. Recomputing bytes permits mutable lists, while later committed ledgers require immutable accepted payload sizes; those are different semantics and should not be merged retrospectively.

## Research expansion — proposed contracts

- Add the staged-to-promoted lineage and define bytes() as payload accounting rather than allocated heap or resident device memory. Record list ownership, overflow behavior, and the exact scope of any allocation reduction.

## Research expansion — acceptance checks

- Retain mixed-list and mutable-list equivalence checks, then measure actual admission-query frequency and allocation on the target artifact. Require native frame and memory evidence before inferring a renderer benefit.

## Research expansion — source anchors

- Renderer backlog — byte-accounting candidate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog — byte-accounting candidate

Reviewed production parent bf7b36a21b7063c1d273c383008ddfd073d5e5b1; candidate SHA256 4c8d6b60a71c1021d245df622105b1d1a8a4f0bd0dc58fd2a205f83c9be85eec. No production runtime or version changed; no new JAR. Scores [4,6,4,0,4,4], mean3.67 delta0.00, all carried.
1. Reestablish private full-build headroom; apply retained candidate after fresh main/claim check, register its check, run full module/package/native gates before promotion.
2. GPU/client discovery/handoff/cache-in-flight/edit/travel/reload/resize/world-switch and water-ordering regression.
3. Full reproducible camera/travel/edit CPU/GPU frame and pipeline/memory baseline. Byte-query allocation result is scoped candidate evidence only.
4. Bounded mesh/work/upload reuse and hierarchical LOD coverage/invalidation/streaming; preserve materials/light/cutouts/water.
5. Authoritative unexplored-terrain delivery contract and generator/network coordination; independent requested/effective distance budgets, no slider-only credit.
6. Real official FSR/DLSS SDK/provider/input/lifetime/licensing/hardware integration. Frame generation later.
Consume this REVIEW and final-checks before next jam. Prior renderer5 remains delivered source; staged candidate must not be described as shipped.


<!-- END PRESERVED SOURCE -->
