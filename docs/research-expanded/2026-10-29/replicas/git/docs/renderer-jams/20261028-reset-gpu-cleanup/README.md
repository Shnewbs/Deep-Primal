# Expanded replica — docs/renderer-jams/20261028-reset-gpu-cleanup/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-gpu-cleanup/README.md) |
| Source identity | git: docs/renderer-jams/20261028-reset-gpu-cleanup/README.md |
| Preserved source SHA-256 | `ecb1bdedd9209546510c21690762953548ad2e55428f38cd5a7958716147cf03` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Makes LOD reset relinquish pending ownership, attempt every cached GPU close, clear world/cache state, and then throw the first failure with suppressed cleanup errors. Geometry, water, and backend rendering paths are unchanged.

## Research expansion — gaps and details to deepen

- The model covers close faults but the later reset-control repair addresses earlier cancellation/shutdown exceptions. Neither local helper proves safe native retirement or device-loss recovery, so the expansion should connect both repairs and the backend lifetime contract.

## Research expansion — proposed contracts

- Add a reset ownership ledger covering pending, resident, recorded, submitted, and retired resources. Link control-teardown supersession and distinguish attempted release, completed release, and quarantined uncertainty; retain the first failure without abandoning later cleanup.

## Research expansion — acceptance checks

- Retain 44,444 focused checks and the no-close mutation, then inject actual backend faults during resize, reload, world switch, and shutdown. Verify all eligible resources are attempted once, stale state is cleared, and completion-aware memory accounting converges.

## Research expansion — source anchors

- Renderer jam 14 — resource-safe LOD world reset

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `CLAIM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-gpu-cleanup/CLAIM.json) |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-reset-gpu-cleanup/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 14 — resource-safe LOD world reset

Jam `renderer-20261028T1800-gpu-reset-failure-cleanup`, source [`96f368e8e212db211376c111a9bef148f9d1f67f`](https://github.com/Shnewbs/Deep-Primal/commit/96f368e8e212db211376c111a9bef148f9d1f67f), version `0.0.1-alpha.29-dev.59-ecotone-query.1`.

The existing LOD `reset()` could abandon pending and cached GPU buffers when a close operation threw. It now relinquishes pending ownership before closing, tries every cache entry, clears state, then propagates the first close failure (with later failures suppressed). No geometry, water, shader, native backend, settings, worldgen or unrelated file changed.

**Isolated Java21 strict build + 44,444 assertions passed**, mutation test failed as expected. No Java25/full Gradle/native GPU/client/JAR under October 27 owner-build override. See [read-only self-audit](REVIEW.md), [receipt](RECEIPT.json), and [claim](CLAIM.json). Renderer score **5.51/10 (+0.03)**; separate independent engine score remains historical 5.0/10. No frame-time/FPS/VRAM credit.


<!-- END PRESERVED SOURCE -->
