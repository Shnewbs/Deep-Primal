# Expanded replica — docs/renderer-jams/20261026-delivered-discovery/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-delivered-discovery/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-delivered-discovery/BACKLOG.md |
| Preserved source SHA-256 | `86236b1af432375a886ca1c48ff1629c2c0cacc2117fceaf0fdc62e4891e422d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Expands delivered-chunk rediscovery beyond the former 32-chunk clipping using a Chebyshev-ring cursor over the effective native radius, bounded at 128 coordinate visits per tick and a 512-entry admission guard. Nonloading checks and native revision precedence are preserved. Prioritize useful coverage and long-travel fairness after linking the later retarget repair.

## Research expansion — gaps and details to deepen

- Cursor enumeration is not visible readiness. The fixed-center limitation at this historical pin was later addressed by retarget work, but fairness during continuous movement and end-to-end coverage age still need integrated evidence.

## Research expansion — proposed contracts

- Add separate discovered, captured, meshed, GPU-published, and visible counters keyed by world generation and source revision. Link the retarget successor and measure oldest eligible work without treating unavailable authoritative chunks as scheduling failures.

## Research expansion — acceptance checks

- At a 125-chunk radius, verify all 63,001 coordinates are enumerated under bounded turns, then verify actual visible coverage on the native client. Exercise movement, detach, and stale disk results without loading extra chunks.

## Research expansion — source anchors

- Renderer backlog after jam 20261026-delivered-discovery

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after jam 20261026-delivered-discovery

Completed: bounded attach discovery over the initial effective native square, max125; 128 coordinate callbacks/tick; admission512; unfocused pause; authoritative dirty revision over disk work; enumeration diagnostics. Source 02eb0d8f3739cf036717abf82d03dc7e2a325081; version0.0.1-alpha.29-dev.30-renderer.5. Self-grade [4,6,4,0,4,4], average3.67, delta0.00.

1. GPU client readiness/coverage regression: radius33/96/125, signed-coordinate travel, attach/reload/world switch, native/disk/edit races, capture/upload completion and water ordering. Enumeration-complete must not be presented as ready coverage.
2. Reproducible cold/warm camera/travel/edit baseline with full context and CPU/GPU frame p50/p95/p99, rebuild/cull/allocation/upload, draw/descriptor counts and RAM/VRAM. Current run has functional counts only.
3. Measure remaining mesh allocation and worker/capture/upload pressure; preserve immutable result arrays, UV/material/light/fluid/protected edges. Evaluate meshoptimizer on real constrained inputs before shipping native code. No false indirect-batching claims.
4. Hierarchical visibility/refinement/hysteresis/cache invalidation and authoritative unexplored-terrain delivery contract. Coordinate generator/network-owned prerequisites; no terrain redesign.
5. Independent persisted requested/effective full-detail/vertical/LOD/simulation budgets and graceful admission. No full-detail increase beyond125 without host tracking/network/allocation proof.
6. Actual FSR spatial/temporal and DLSS super-resolution providers using current official SDK/native licensing, detection, required render inputs and lifetimes. Present unavailable reasons honestly; preserve native fallback. Frame generation later.

Carried limits: jam4 component allocation30–49% lower is historical (not rerun); no fresh native GPU/frame measurement. Linear Vulkan presentation is not FSR/DLSS. Keep this backlog separate from engine/water scope and external grading. Next jam must consume REVIEW.md and final corrected source, not the superseded d675 code/package.


<!-- END PRESERVED SOURCE -->
