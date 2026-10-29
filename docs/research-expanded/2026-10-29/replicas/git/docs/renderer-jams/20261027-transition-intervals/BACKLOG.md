# Expanded replica — docs/renderer-jams/20261027-transition-intervals/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-transition-intervals/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-transition-intervals/BACKLOG.md |
| Preserved source SHA-256 | `5d8460a349d0353687f96d33b5cb627a6642be75efedd3ca8cc74a24fe0d97af` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends the staged frame-window candidate with a separate bounded transition history, retaining initialized nonnegative crossing gaps while isolating steady-context percentiles. It preserves zero gaps, clock wrap, reset, and explicit invalid-clock behavior. Link capacity-recovery promotion, then collect named-hardware transition traces instead of repeating staged qualification.

## Research expansion — gaps and details to deepen

- The aggregate transition window does not attribute causes or isolate load/GPU time. This staged source also needs a clear link to later promotion, preventing its successful focused adapter/core checks from being mistaken for a new native artifact.

## Research expansion — proposed contracts

- Add transition cause and before/after context identities, scene/artifact fingerprint, frame sequence, and sample availability. Keep all observed gaps reviewable separately from steady statistics and label included limiter/wait time.

## Research expansion — acceptance checks

- Reproduce all eight context crossings and bounded-window edge cases, including first observation and backwards clocks. On the promoted artifact, correlate transition events with actual presentation and GPU timing; preserve the failed capacity attempt separately from the final successful focused rerun.

## Research expansion — source anchors

- Renderer continuation after transition diagnostics

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer continuation after transition diagnostics
Production remains c5abe457c4c7b36a11307128486c240e0b40d508/dev.31. Combined staged frame/transition candidate 302b93f12976c9a37cb680a677218daa9e0ed4e4; no new JAR.
1. Recover isolated full-build capacity; rebase combined staged files onto fresh main under a production claim, merge only the CoreChecks registration insertion, force Java25/Gradle/module/resources and complete product/platform/archives/identities/native/client qualification, then promote and save a fresh unique testing JAR.
2. Live Vulkan-first/OpenGL fallback discovery/readiness/handoff/cache/edit/travel/water-ordering and lifecycle gates; complete named-hardware camera/scene/seed/resolution/detail/cold-warm CPU/GPU frame/pipeline/draw/descriptor/RAM/VRAM baselines. Crossing frame gaps include waits and are not isolated world-load times or GPU execution. Attribution/full scene fingerprint still missing.
3. Bounded mesh/upload reuse and worker work; hierarchy/refinement/hysteresis/coverage/invalidation; preserve signed seams/material/light/UV/water.
4. Authoritative unexplored delivery contract with generator/network coordination; persisted independent requested/effective native/vertical/LOD/simulation budgets and graceful admission. No slider-only expansion.
5. Real official FSR/DLSS SDK provider/input/lifetime/licensing/packaging/hardware integration, truthful fallback. Frame generation separate/later.
Scores [4,6,4,0,4,4],mean3.67,delta0,all carried. Last whole-engine external5.0 unchanged. Continue dedicated renderer scope; no other schedules changed.


<!-- END PRESERVED SOURCE -->
