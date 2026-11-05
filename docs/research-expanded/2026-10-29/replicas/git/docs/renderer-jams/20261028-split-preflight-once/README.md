# Expanded replica — docs/renderer-jams/20261028-split-preflight-once/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-split-preflight-once/README.md) |
| Source identity | git: docs/renderer-jams/20261028-split-preflight-once/README.md |
| Preserved source SHA-256 | `c3c7c5e9596f010e6ddf841b6499720a4deead842107e40ae0905a05b1680116` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Avoids verifying canonical quad indices twice in the off-by-default splitter by retaining a bounded primitive preflight plan for emission. Unsupported shapes pass through; position, material/alpha, normal, index, order, cancellation, and part limits are preserved.

## Research expansion — gaps and details to deepen

- The validated plan relies on unchanged source arrays between preflight and emission. That ownership assumption should be explicit, and a lower optional-split CPU median cannot justify enabling splitting when additional native draw/descriptor cost is unknown.

## Research expansion — proposed contracts

- Specify immutable input lifetime, bounded plan size, cancellation cleanup, and failure-atomic emission. Keep unsupported topology behavior and default-off policy, and measure complete page/publication cost rather than only removing one validation pass.

## Research expansion — acceptance checks

- Retain exact parity and mutant controls across the 14,154,029 assertions, then test cancellation and source-ownership misuse. Compare default versus split on actual Vulkan/OpenGL with GPU/CPU tails, memory, descriptor/draw counts, and transparent-water images.

## Research expansion — source anchors

- Renderer jam 16 — single-pass optional quad split preflight

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `CLAIM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-split-preflight-once/CLAIM.json) |
| `LodSplitPreflightSmoke.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-split-preflight-once/LodSplitPreflightSmoke.java) |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-split-preflight-once/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 16 — single-pass optional quad split preflight

Production commit [`0daeacbb7f5c3f26893a0c258341290abb96fa5c`](https://github.com/Shnewbs/Deep-Primal/commit/0daeacbb7f5c3f26893a0c258341290abb96fa5c) avoids verifying canonical quad indices twice for each oversized opt-in 32KiB mesh. This is **off by default**, alters no water/terrain/shader paths and grants no verified Minecraft FPS or GPU-memory gain.

Isolated Java21 tests: source-pinned candidate and baseline each pass 14,154,029 exact/parity assertions (750 randomized scenarios, same digest), committed smoke passes 256 seeded cases/504 pieces, inverted-decision mutant is rejected. Three paired same-process CPU fixtures show 14.0–18.9% lower medians, mixed p99 and only 24 B less per-call allocation. See [self-audit](REVIEW.md), [receipt](RECEIPT.json), [released claim](CLAIM.json), [committed smoke](LodSplitPreflightSmoke.java). Native Java25/Gradle/GPU/JAR gates remain owner-operated and unrun.


<!-- END PRESERVED SOURCE -->
