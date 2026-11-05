# Expanded replica — docs/renderer-jams/20261028-reset-control-cleanup/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-control-cleanup/README.md) |
| Source identity | git: docs/renderer-jams/20261028-reset-control-cleanup/README.md |
| Preserved source SHA-256 | `7c5e169f0a1cdc0ded900861a3fcc3e898d09737a390e9fdb445545ab00a09b1` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Contains reset failures from future cancellation, disk-service reset, and executor shutdown so later GPU release and stale-world cleanup still run. Future/worker ownership is revoked before fallible calls; the first error is rethrown with later errors suppressed.

## Research expansion — gaps and details to deepen

- Revoking publication authority and requesting cancellation do not prove worker termination. The renderer needs an observable retirement state so old work can release its own resources without mutating a new attachment or leaking untracked threads.

## Research expansion — proposed contracts

- Define reset phases: revoke generation, detach handles, request cancellation, retire workers, release owned resources, clear published state, then propagate aggregated failure. Keep blocked-worker and GPU completion accounting bounded without waiting on the render thread.

## Research expansion — acceptance checks

- Retain the 128 fault-mask model and missing-reset mutation, then inject real disk/worker cancellation failures during world switch. Verify later releases, exact exception identity, no old-world publication, bounded retired workers, and eventual native memory settlement.

## Research expansion — source anchors

- Renderer jam 15 — reset-control teardown isolation

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `CLAIM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-control-cleanup/CLAIM.json) |
| `LodResetControlChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-control-cleanup/LodResetControlChecks.java) |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-control-cleanup/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 15 — reset-control teardown isolation

Implementation [`969abe129b94df7734afe61bc3b5d76f5155bf13`](https://github.com/Shnewbs/Deep-Primal/commit/969abe129b94df7734afe61bc3b5d76f5155bf13). Prevent exceptions from asynchronous capture cancellation, disk scan reset or executor shutdown from abandoning later cache GPU releases and stale-world cleanup. First failure remains visible; subsequent failures are suppressed. This is renderer-only.

**Java 21 isolated fault-injection checks:** 1,664 assertions passed for 128 masks × two throwable configurations; deleted-disk-reset negative check failed as intended. Source anchors verified on pinned implementation. The shared version is `0.0.1-alpha.29-dev.60-relief-v2.1` (not changed). **No Java25, full build, native GPU, FPS, VRAM or JAR acceptance.**

[Read-only six-category self-audit](REVIEW.md) **5.53/10 (+0.02)**. Independent engine review remains 5.0/10. [Test source](LodResetControlChecks.java); [receipt](RECEIPT.json); [released claim](CLAIM.json).


<!-- END PRESERVED SOURCE -->
