# Expanded replica — docs/renderer-jams/20261026-lod-telemetry/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-lod-telemetry/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-lod-telemetry/BACKLOG.md |
| Preserved source SHA-256 | `1d1c08d6a09b731f4df21bdb361b04ca10ccf44942abc8e2b5b32de2341f650e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds opt-in 240-observation CPU cull, upload, and submit timing windows, lazy diagnostic snapshots, and world reset. The record distinguishes submitted parts from GPU draw counts and prospective GPU payload bytes from actual CPU or device memory. Connect later page, frame, transition, and submit-stage probes through one schema rather than adding incomparable dashboard numbers.

## Research expansion — gaps and details to deepen

- Independent last-operation gauges and percentile windows need correlated identities and population definitions. Without those, a dashboard may combine unrelated frames or interpret absent work and unavailable timings as zero-cost rendering.

## Research expansion — proposed contracts

- Add frame/world/device generation, observation type, sample count, units, skipped/failed/deferred outcomes, and snapshot timestamp. Preserve bounded storage and lazy sorting; label CPU API time, GPU completion, payload estimates, and measured memory separately.

## Research expansion — acceptance checks

- Verify empty windows, world resets, partial failures, and mixed cull/upload/submit populations. Measure telemetry-on/off overhead in the target client and correlate asynchronous GPU timestamps without blocking the render thread.

## Research expansion — source anchors

- Renderer backlog — jam2

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog — jam2

Completed: per-part exact bounds (jam1); opt-in bounded CPU cull/upload/submit percentile windows and workload counters through existing LOD diagnostic feed (jam2).

Next: capture actual fixed-camera/travel/edit baselines with telemetry enabled on named OpenGL/Vulkan hardware; add worker rebuild/allocation, descriptor/draw and actual CPU/GPU memory visibility. Frame/GPU timing, cold/warm scene identity and matched performance remain mandatory open gates. Prove live culling/LOD handoff/edit/reload/resize/world-switch/shutdown equivalence before performance promotion.

Then mesh reuse/scheduling, conservative hierarchy/refinement/coverage and fence-safe storage. Define an authoritative unexplored-terrain contract before unseen delivery. Expand independently persisted full-detail/vertical/LOD controls only with tracker/network/allocation admission; simulation stays separate. Actual FSR/DLSS providers and official SDK/licensing/resource-lifetime/hardware validation remain unimplemented. Frame generation is later and distinct.

Consumed independent dev29 audit93ac6cb: precipitation rejection repaired and ten packaged native sessions pass; jam1's inherited crash blocker is closed by the water owner. No renderer visual/FPS acceptance follows from that fix. No changes to water simulation or the staged stream-deadline repair.

Renderer self-audit remains3.50/10, delta0.00 (4,5,4,0,4,4); no external engine regrade. Keep this dedicated feature workstream enabled and other schedules unchanged.


<!-- END PRESERVED SOURCE -->
