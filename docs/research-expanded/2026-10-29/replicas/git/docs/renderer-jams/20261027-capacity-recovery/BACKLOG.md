# Expanded replica — docs/renderer-jams/20261027-capacity-recovery/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-capacity-recovery/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-capacity-recovery/BACKLOG.md |
| Preserved source SHA-256 | `fdfbd49dea5d2ffaedc161ec11f2342796a77e428e37e40d2fc765f3c7fae679` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Promotes bounded steady-frame and transition telemetry after isolated build-capacity recovery. A 512-interval steady window resets on context changes, while crossing intervals are retained separately; complete historical build/headless checks and corrected artifact identity are recorded. Move from isolated capacity repair to named-device complete-frame and GPU correlation.

## Research expansion — gaps and details to deepen

- Transition intervals include waits and cannot isolate world-load or GPU execution. The eight context coordinates need an extensible event fingerprint for backend/provider and resource generation changes, with retained cause information rather than a single mixed transition percentile.

## Research expansion — proposed contracts

- Add context-generation and transition-cause records correlated to artifact, scene, camera, resolution, active provider, and frame identity. Preserve all crossing gaps outside the steady distribution and identify invalid-clock or uninitialized observations explicitly.

## Research expansion — acceptance checks

- Replay each context transition, zero/backward/wrapped intervals, and world-reference release. Measure same-device steady and transition distributions with telemetry enabled and disabled, preserving long gaps rather than discarding them to improve the summary.

## Research expansion — source anchors

- Renderer backlog after renderer.7 delivery

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after renderer.7 delivery
Delivered runtime 2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e; build execution f948194d255cae51684525b49c802afa3c7d7c77; version0.0.1-alpha.29-dev.31-renderer.7. Frame/transition candidates are now production integrated, full35-task build/package and four Linux sessions qualified, new JAR saved as libfile_86fb5c7d020c8191a5dfdb450a6e47e9.
1. Actual GPU discovery/readiness/handoff/cache/edit/travel/transparent-water and client reload/resize/world-switch/save/reopen/shutdown acceptance; complete reproducible scene/seed/camera/resolution/distance/detail/cold-warm CPU/GPU p50/p95/p99, pipeline/draw/descriptor/allocation and RAM/VRAM baselines. CPU intervals include waits; transition gaps are not isolated load times or a complete scene fingerprint.
2. In parallel when named GPU hardware is unavailable, continue concrete bounded mesh/upload reuse and scheduling on real constrained inputs, with protected material/UV/light/water/silhouette edges and honest component measurements; no frame/FPS claims from CPU fixtures.
3. Hierarchy/refinement/hysteresis/coverage/invalidation/cache/streaming; authoritative unexplored-terrain delivery contract and generator/network prerequisites.
4. Independent persisted requested/effective native/vertical/LOD/simulation budgets and graceful admission; no slider-only maxima credit.
5. Real current-official FSR/DLSS SDK providers/required inputs/resource lifetimes/licensing/packaging/hardware. Frame generation separate/later; linear scaling remains distinct.
Scores [4,6,4,0,4,4],mean3.67,delta0,all carried. Last external whole-engine5.0 unchanged. Main source and developer JAR are qualified for available software gates, not GPU/Windows/performance acceptance.
Capacity lesson: inspect process-writable free blocks plus a bounded real write probe, not available_blocks alone. Keep mutable Gradle/project/JNA/temp/native caches directly in private memory under pressure; workspace symlink relocation did not persist. Final native setup uses isolated mutable metadata and verified read-only links to own immutable dependency JARs. No other jobs/cache/output touched.


<!-- END PRESERVED SOURCE -->
