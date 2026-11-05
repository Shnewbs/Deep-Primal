# Expanded replica — docs/renderer-jams/20261029-finite-yaw-overflow/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-finite-yaw-overflow/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261029-finite-yaw-overflow/BACKLOG.md |
| Preserved source SHA-256 | `730fa7dc193d075b6b4048a232f162c0abde79bcfa658b05bb03b7f2663844de` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Prioritizes native builds, three-way frustum comparisons, lifecycle and water-image acceptance after the finite-yaw correction. It explicitly records a 29–34% slower angle-arithmetic fixture and keeps the experimental adaptive path disabled by default.

## Research expansion — gaps and details to deepen

- The subsequent normal-angle fast path supersedes the local arithmetic concern at source level, but does not complete this backlog’s native gate. The continuation needs a dependency link that distinguishes repaired arithmetic cost from unresolved production call-stack and whole-frame measurement.

## Research expansion — proposed contracts

- Add a supersession entry pointing to jam30, retaining jam29’s adverse result and exact fixture scope. Order follow-up work as actual caller compilation, fixed/adaptive/off capture, correctness images, then measured bottleneck selection; preserve separate temporal-provider and unexplored-delivery dependencies.

## Research expansion — acceptance checks

- Verify the accepted artifact contains both extreme-finite correctness and ordinary-angle optimization. Run ordinary and abrupt/extreme-turn scenarios with native CPU/GPU frame distributions, culling-state diagnostics, and water parity; do not combine unrelated synthetic percentages into a claimed renderer speedup.

## Research expansion — source anchors

- Renderer jam29 follow-up backlog (renderer-only)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam29 follow-up backlog (renderer-only)

1. **Owner native build gate:** Java25 Gradle/NeoForge compile and packaged identity verification; source-only Jam29 artifact is not a JAR.
2. **Actual GPU baselines:** matched Vulkan/OpenGL fixed/opt-in adaptive/disabled page-frustum trials, ordinary and extreme turns, camera travel/edit, water transparency and world native handoff. Capture CPU/GPU p50/p95/p99, draw/descriptor counts, upload/rebuild, RAM/VRAM, cache/resident/pending.
3. **Performance concern to evaluate:** this jam's CPU *angle arithmetic* fixture ran 29–34% slower despite retaining normal-angle fast path. Sample realistic game extraction frequencies before additional CPU culling changes; experiment remains default-off. Do not call isolated component timings FPS.
4. **Functional acceptance:** resource reload, resize, world switch, save/reopen, shutdown, edit invalidation, signed seams, transparent-water ordering and LOD transitions.
5. **Next feature after measurements:** bounded mesh/submission and streaming optimizations, requested vs effective full-detail/vertical/LOD controls with host safety; temporal FSR and DLSS only once actual vendor provider dispatch and temporal inputs are validated on supported hardware. Frame generation is later.
6. Keep this scope distinct from terrain/water builder, independent auditor and unrelated GUI/ports/standalone work. Preserve current `main` history and released claims.


<!-- END PRESERVED SOURCE -->
