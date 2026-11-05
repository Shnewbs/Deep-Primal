# Expanded replica — docs/renderer-jams/20261026-lod-reduction/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-lod-reduction/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-lod-reduction/BACKLOG.md |
| Preserved source SHA-256 | `85857c7f419abfabecae219b5ba244b31c4146dcfe93a7f27604e6eb1166c406` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Optimizes LOD reduction by avoiding redundant zero fills and selecting the first opaque sample, or the first water sample when no opaque sample exists, for 2x2 and 4x4 groups. Frozen geometry signatures preserve signed and partial-water cases. Select the next optimization from full meshing profiles instead of repeating this loop benchmark.

## Research expansion — gaps and details to deepen

- The component benchmark showed mixed tails despite several improved medians. The expansion should define the workload distribution and semantic selection oracle before treating reduced reduction-loop work as an overall meshing or frame benefit.

## Research expansion — proposed contracts

- Document first-opaque/first-water priority as part of the reduction contract and record transparent-material exceptions explicitly. Tie any default acceptance to representative caves, sparse columns, solid terrain, and water fixtures with complete build/publication cost.

## Research expansion — acceptance checks

- Compare all frozen geometry/material signatures, test cancellation and mixed sample order, and measure repeated target-native build latency plus frame tails. Preserve the observed tail regressions and unchanged allocation/payload facts.

## Research expansion — source anchors

- Renderer backlog after jam3

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after jam3

Done: exact part bounds; bounded opt-in CPU LOD timing and counters; byte-equivalent zero-fill/opaque-sample reduction with frozen signatures and matched exploratory CPU probe.

Next: controlled named-device camera/travel/edit baselines and GPU/client culling/telemetry lifecycle. Current component medians improve4/5, tails are mixed and allocations unchanged; no FPS/whole-frame acceptance. Complete worker rebuild/allocation, descriptors/draw, full-frame/GPU timing and actual RAM/VRAM visibility. Then mesh reuse/scheduling, conservative hierarchy/refinement/coverage and fence-safe storage.

Retain explored-only contract; implement renderer-owned authoritative unexplored delivery prerequisites with generator/network owners before claiming unseen coverage. Full-detail/vertical/LOD budgets stay separate from simulation. Actual FSR/DLSS SDK/provider/input/dispatch/lifetime/device validation remains future work; frame generation is distinct. No configured-max-only performance credit.

Self-audit3.50/10, delta0.00; external engine rubric untouched. Preserve active hydrology scheduler qualification and other feature scopes. Dedicated renderer workstream remains enabled.


<!-- END PRESERVED SOURCE -->
