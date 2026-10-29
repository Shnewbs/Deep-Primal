# Expanded replica — docs/renderer-jams/20261027-quad-bounded-upload/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-quad-bounded-upload/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-quad-bounded-upload/BACKLOG.md |
| Preserved source SHA-256 | `7755949af0ecb51d532ba4e4238d67b07d0d83289d6379878a4d10ac23bb63d3` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Defines an off-by-default 32 KiB canonical-quad splitter that preserves vertex/material/water/index order and payload bytes while rebasing indices. Preflight caps emitted parts per detail level; unsupported shapes pass through. Standard 94,208-byte parts already fit the normal upload allowance. Keep the split disabled until an actual target-device benefit outweighs added submission work.

## Research expansion — gaps and details to deepen

- Smaller parts can increase draw and descriptor overhead without reducing total payload. The experiment needs a complete page-publication and GPU lifetime comparison, not only exact CPU geometry or an assumption that the default path needs splitting.

## Research expansion — proposed contracts

- State the supported topology and immutable-input preconditions, failure-atomic output publication, part-count ceiling, and fallback on unsupported or cancelled work. Measure pending replacement overlap and preserve conservative bounds and transparent ordering.

## Research expansion — acceptance checks

- Retain exact canonical-quad and alpha/index parity checks, then compare default and split modes on identical native Vulkan/OpenGL scenes. Record page latency, actual GPU/CPU tails, draw/descriptor counts, memory peak, and water pixels before considering a default change.

## Research expansion — source anchors

- Renderer-only continuation backlog — after optional bounded-quad upload

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer-only continuation backlog — after optional bounded-quad upload

1. **Owner-side hardware A/B gate:** same world/camera, default part layout versus `-Ddeepprimal.lod.experimentalPartUploadKiB=32`. Include CPU/GPU frame p50/p95/p99, part upload sizes/costs, draw/descriptor counts, peak RAM/VRAM, tested/culled/visible parts and startup/travel/edit/resize. Preserve water transparency and native handoff. Turn off experimental split if more draws/tails dominate.
2. **Actual nonblocking upload improvement:** If hardware confirms large-part driver stalls, evaluate fence-safe reusable GPU storage or separately staged vertex/index chunks. Preserve atomic pending-page publication and old visible page until complete.
3. **Visibility/distance:** Fix persistent beyond-index horizon page cutoffs, stable near/native and vertical coverage, independent simulation semantics and authenticated unseen delivery. No distance-slider-only credit.
4. **Upscaling:** Maintain official AMD FSR1 spatial; implement valid temporal motion/depth/jitter handoff before real DLSS provider dispatch. Keep NGX native packaging/license and RTX hardware validation as gates. Frame generation is separate.
5. **Audits:** Source-only 4.67/10 self-audit; independent whole-engine 5.0/10 carried. Do not increase without live/native evidence. Owner handles Java25 full builds/GPU/JAR publication.


<!-- END PRESERVED SOURCE -->
