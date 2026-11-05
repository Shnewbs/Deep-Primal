# Expanded replica — docs/renderer-jams/20261028-same-ring-rank/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-same-ring-rank/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-same-ring-rank/REVIEW.md |
| Preserved source SHA-256 | `dc41bf8d81ad2dd4996ab9bd034fc244c8c3b9e317d15361bcee9401387cd82a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends full-heap disk-candidate rejection to same-ring exact squared-distance and signed-coordinate ties before allocating ranked keys or boxed set entries. Selection order, 8,192 retention, and 256-entry work turns remain unchanged. The two paired component runs provide constrained CPU evidence, not a universal scanner or renderer speedup.

## Research expansion — gaps and details to deepen

- The benefit is conditional on filled heaps receiving dominated same-ring candidates. Every filename is still enumerated, so fixture allocation and CPU improvements cannot be generalized to all saves, filesystem costs, or native frame latency.

## Research expansion — proposed contracts

- Document the complete rank tuple and predicate equivalence, with candidate-distribution counters and retained-cap saturation. Preserve BigInteger as a test oracle rather than a production dependency, and connect scan outcomes to actual restoration/publication age.

## Research expansion — acceptance checks

- Retain the 64,841-oracle and nine-suite regressions across shuffled ties, duplicates, and signed extrema. Benchmark representative directory distributions and native coverage, reporting the roughly 44.9% fixture allocation reduction separately from GPU/frame measurements.

## Research expansion — source anchors

- Renderer jam 12 — read-only self-audit of exact committed code

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 12 — read-only self-audit of exact committed code

Pinned candidate [`322bd170fc32cc14e1752358be4a05150d79b0f5`](https://github.com/Shnewbs/Deep-Primal/commit/322bd170fc32cc14e1752358be4a05150d79b0f5) with scanner blob `89acf662fd079f70272eab462c36302aa76e8981`, oracle blob `871ede8f87afa77119cc999eaaf366795bb58e31`. Completed implementation was audited **read-only**; no code edits during scoring. Independent [15:30 engine audit](../../engine-hourly-audits/2026-10-28T1530-cec6143/REVIEW.md) remains distinct. Previous renderer [jam 11](../20261028-eviction-reuse/REVIEW.md) score **5.42/10**.

| Equal-weight category | Score /10 | Exact evidence and remaining gate |
|---|---:|---|
| Distance/LOD usable correctness | **6.50 carried** | [Unsigned 128-bit same-ring compare and exact tie](https://github.com/Shnewbs/Deep-Primal/blob/322bd170fc32cc14e1752358be4a05150d79b0f5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L262-L315), 64,841 BigInteger-oracle checks; native far-world/unseen coverage unrun |
| Mesh generation, reuse and scheduling | **6.85 (+0.10)** | [Full rank early rejection](https://github.com/Shnewbs/Deep-Primal/blob/322bd170fc32cc14e1752358be4a05150d79b0f5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L262-L285) before candidate allocation; same-ring CPU p50 -17–23%, Java-thread alloc -44.9% on matched constrained fixture; GPU mesh/build rates unknown |
| GPU culling/submission/memory | **4.00 carried** | [Render indexed submission](https://github.com/Shnewbs/Deep-Primal/blob/322bd170fc32cc14e1752358be4a05150d79b0f5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L748-L812) unchanged, no real GPU MDI/VRAM/fence proof |
| Actual FSR/DLSS | **5.00 carried** | [FSR1 spatial exists; temporal FSR3/4 and DLSS unavailable](../../UPSCALING.md), no new vendor dispatch or hardware evidence |
| Visual/backend/settings | **5.25 carried** | Selection-only change preserves mesh/alpha/order and backend code; actual native Vulkan/GL/water pixels missing |
| Validation and measured performance | **5.10 (+0.10)** | Java21 strict compile and **144,361** assertions across nine suites, expected broken-predicate mutant rejected; 2×220 matched CPU timing/allocation samples; Java25, Gradle and hardware unrun |
| **Overall** | **32.70/60 = 5.45 (+0.03)** | Renderer target >8.5 not met; independent **whole-engine 5.0** only carried |

The allocation improvement applies to filled heaps receiving dominated **same-ring** entries, not every directory. The enumerator still visits all filenames. Nothing here establishes improved native FPS, GPU memory, water visibility or live upscaling.


<!-- END PRESERVED SOURCE -->
