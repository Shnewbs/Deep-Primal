# Expanded replica — docs/renderer-jams/20261026-part-bounds/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-part-bounds/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-part-bounds/BACKLOG.md |
| Preserved source SHA-256 | `652a020fcfb98a9937887b9949c2c5692d0fa971d418466c28d43262bf6dcce1` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs per-part bounds by resetting empty extrema on every builder flush and deriving bounds from actual vertices. Geometry remains unchanged; synthetic vertical culling rejects previously loose parts more accurately. The historical packaging hold concerns a separately carried water failure. Link later water/publication status, then prioritize native culling and boundary-image acceptance.

## Research expansion — gaps and details to deepen

- Tight static bounds should become an explicit contract across deforming water, animated geometry, material cutouts, and source generations. The historical water blocker also needs a superseding-status link so it is not mistaken for a still-open current defect.

## Research expansion — proposed contracts

- Define conservative bound expansion for any visual displacement and require bounds to share geometry revision and origin convention. Record where bounds are exact, conservatively expanded, or unavailable, and keep culling behavior separate from draw-count or FPS claims.

## Research expansion — acceptance checks

- Exercise every frustum plane, signed/large coordinates, near/LOD transitions, and water displacement. Verify native pixels remain present when bounds are tight and measure actual cull/submit cost; the synthetic rejection ratio alone is insufficient.

## Research expansion — source anchors

- Renderer backlog after 20261026-part-bounds

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after 20261026-part-bounds

- Done: exact per-part bounds with baseline failure, geometry-equivalent result, complete build and packaged regression.
- Next: reproducible fixed-camera/travel/edit counters and telemetry (CPU/GPU frame p50/p95/p99, cull/rebuild/upload/allocation, draws/descriptors, resident/pending/cache RAM/VRAM), with named device/API and warm/cold scene identity. Current host has no GPU device; CPU-only counts are not acceptance.
- Verify live full-frustum bounds coverage, unload/edit/native handoff, reload/resize/world-switch/save/reopen/shutdown on both backends.
- Improve mesh reuse, worker/upload bounds and fence-safe storage; only then evaluate hierarchy and compatible real indirect batches.
- Define renderer-owned authoritative unseen-terrain delivery contract and coordinate generator/network prerequisites; currently explored-only.
- Expand independently persisted full-detail/vertical/LOD budgets with tracker/network admission; do not just increase sliders or alter simulation.
- Integrate actual officially documented/licensed FSR/DLSS providers, inputs, dispatch, lifetimes and device detection; linear spatial scaling is not FSR, frame generation is separate.
- Delivery gate owned by water builder: qualify precipitation admission fix and packaged native lifecycle; current combined renderer JAR is withheld.

First renderer self-audit3.50/10 (4,5,4,0,4,4); previous renderer delta unavailable. Whole-engine external rubric unchanged. Dedicated renderer schedule remains enabled; other schedules untouched.


<!-- END PRESERVED SOURCE -->
