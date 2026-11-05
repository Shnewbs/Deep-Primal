# Expanded replica — docs/renderer-jams/20261027-frame-window/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-frame-window/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-frame-window/BACKLOG.md |
| Preserved source SHA-256 | `44249c53e4a8e2678d9e61be7319534ef57a121604aa7f1fc8aad60972bff7da` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Stages a bounded 512-interval frame window keyed by eight context coordinates so mixed worlds, dimensions, focus, and settings do not pollute steady percentiles. Crossing intervals are initially discarded; a later transition-interval candidate restores separate visibility into those gaps. Follow the transition-interval and promotion lineage before treating this as the deployed frame diagnostic.

## Research expansion — gaps and details to deepen

- The source’s scope is steady-context statistics, not complete latency accounting. The expansion should link the transition successor and include backend/provider, source artifact, scene fingerprint, and first-sample semantics without retroactively claiming this candidate captured all transitions.

## Research expansion — proposed contracts

- Document the sampling population, reset coordinates, invalid-clock handling, and count/extent metadata. Add a separate transition event record and preserve raw context changes so benchmark selection cannot hide a hitch by resetting the steady window.

## Research expansion — acceptance checks

- Replay every reset coordinate and wrapped/negative/zero intervals, verify no cross-context steady sample, and compare the exact integrated artifact’s full frame stream with dashboard windows. Native GPU and presentation remain distinct measurements.

## Research expansion — source anchors

- Renderer backlog after frame-window staging

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after frame-window staging
Candidate b69d022383571977b90926770893baf271c91fd1; production remains c5abe457c4c7b36a11307128486c240e0b40d508/dev.31. No new JAR.
1. Recover private build headroom; apply/rebase the four staged Java changes from actual current main under a fresh scoped production claim. Add only the demonstrated registration line to fresh CoreChecks. Force full Java25/Gradle/module/resource build, recursive archive/compiled/package/platform/fixture/art/notices identities and packaged backend/native/client gates before promotion and a unique qualified JAR.
2. Capture actual GPU-first camera/travel/edit cold/warm baselines and discovery/readiness/cache/edit/native-handoff/water ordering coverage. FrameIntervalWindow is CPU interval instrumentation only; do not call it GPU time, complete context capture or measured game performance. Measure world-switch transition latency separately from reset windows.
3. Bounded mesh/upload reuse, scheduling and hierarchical selection/refinement/hysteresis/coverage/invalidation; preserve material/UV/light/fluid/silhouette/signed seams.
4. Authoritative unexplored delivery contract with generator/network-owned prerequisites. Independent requested/effective persisted native/vertical/LOD/simulation budgets; never slider-only expansion.
5. Real official FSR/DLSS provider/input/resource lifetime/licensing/packaging/hardware integration; truthful native fallback. Frame generation separate/later.
Six renderer grades [4,6,4,0,4,4], mean3.67, all carried unchanged. Whole-engine external5.0 unchanged. Capacity is temporary; renderer continuation remains enabled.


<!-- END PRESERVED SOURCE -->
