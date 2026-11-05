# Expanded replica — docs/renderer-jams/20261028-same-ring-rank/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-same-ring-rank/README.md) |
| Source identity | git: docs/renderer-jams/20261028-same-ring-rank/README.md |
| Preserved source SHA-256 | `b89824d00b6a5183c2be1d10a6b1d2a895151c71e9cd208be765fe71ba6b2820` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends full-heap disk-candidate rejection to same-ring exact squared-distance and signed-coordinate ties before allocating ranked keys or boxed set entries. Selection order, 8,192 retention, and 256-entry work turns remain unchanged.

## Research expansion — gaps and details to deepen

- The benefit is conditional on filled heaps receiving dominated same-ring candidates. Every filename is still enumerated, so fixture allocation and CPU improvements cannot be generalized to all saves, filesystem costs, or native frame latency.

## Research expansion — proposed contracts

- Document the complete rank tuple and predicate equivalence, with candidate-distribution counters and retained-cap saturation. Preserve BigInteger as a test oracle rather than a production dependency, and connect scan outcomes to actual restoration/publication age.

## Research expansion — acceptance checks

- Retain the 64,841-oracle and nine-suite regressions across shuffled ties, duplicates, and signed extrema. Benchmark representative directory distributions and native coverage, reporting the roughly 44.9% fixture allocation reduction separately from GPU/frame measurements.

## Research expansion — source anchors

- Renderer jam 12 — same-ring exact-rank early rejection

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `LodSameRingRankChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-same-ring-rank/LodSameRingRankChecks.java) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 12 — same-ring exact-rank early rejection

Jam ID: `renderer-20261028T1555-same-ring-ranked-precheck`. Production parent claim `ef23d6481a8be638ab7fd30742b6645187727da1`; committed code [`322bd170`](https://github.com/Shnewbs/Deep-Primal/commit/322bd170fc32cc14e1752358be4a05150d79b0f5), shared version `0.0.1-alpha.29-dev.57-surface-policy.1` unchanged. Production scanner blob `89acf662fd079f70272eab462c36302aa76e8981` and exact committed [BigInteger oracle](LodSameRingRankChecks.java) blob `871ede8f87afa77119cc999eaaf366795bb58e31`.

**Implemented:** The background LOD disk candidate selector now rejects same-Chebyshev-ring candidates whose exact unsigned 128-bit squared distance or signed coordinate tie cannot displace the current worst page, **before** allocating `RankedKey`, boxing a `Long` or probing the retained set. The previous ring-only rejection remains. No mesh, color, water, shader, save, host chunk tracker, distance maximum, upscaler or backend files changed. Existing selection ordering, 8,192-key and 256-entry-per-turn bounds remain.

**Consumed:** root `AGENTS.md` absent, current main, engine/water work-state, LOD architecture, renderer ledger and independent [2026-10-28 15:30 UTC audit](../../engine-hourly-audits/2026-10-28T1530-cec6143/REVIEW.md). That audit verified prior eviction order but did not test GPU frame times. No other writer claimed the affected renderer file; isolated source, cache and benchmark outputs used. The renderer-only claim was released after checkpoint.

**Reproduce Java21 source checks:** in an isolated checkout, copy scanner and coordinator into `src/com/shnewbs/deepprimal/lod/client`, copy the committed oracle and prior renderer scanner smoke sources into `tests/com/shnewbs/deepprimal/lod/client`; run `javac --release 21 -Xlint:all -Werror -d classes @sources.txt` and `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSameRingRankChecks`. Final suite results: **nine tests, 144,361 passing assertions**; oracle alone 64,841 assertions on shuffled equal rings, signed extrema, duplicates, capacities 1/2/7/31/256/512/8192, comparing against BigInteger. A deliberately inverted new predicate compiled but failed at `same-ring-0-1 rank 0`; mutant excluded. All tested Java21 source bytes and individual test logs retained in the verified source/evidence ZIP.

**Matched CPU-only comparison** (Linux Java 21.0.12.1; 3,740 same-ring keys, cap 256, two runs of 220 paired samples, baseline/candidate): median Java-thread allocations **558,728/307,880 bytes** in both runs (~44.9% less). CPU p50 first **241,954/187,471 ns**, repeat **245,499/203,256 ns** (~17–23% less). CPU p95 first 665,159/421,944 ns, repeat 640,412/427,141 ns. CPU p99 noisy: 1,686,493/1,664,089 ns and 3,507,364/1,536,959 ns. These metrics include test reconciliation and **are not game-frame, GPU upload, VRAM, universal filesystem overhead, or FPS**.

**Unrun:** JDK25 unavailable (archive and directory absent), root Gradle/NeoForge build and packaged-JAR qualification owner-operated, native Minecraft, Vulkan/OpenGL water pixels, resize/reload/switch/reopen/edit, GPU/VRAM/frame-time acceptance, NVIDIA RTX and real temporal FSR/DLSS. No JAR, release, hosted Actions or CurseForge publication attempted.

**Next:** owner-run native same-seed Vulkan/OpenGL camera/travel/teleport/edit/reopen with RAM/VRAM, p50/p95/p99, draw/upload descriptors and water transparency; compare default vs 32KiB optional split. Continue valid temporal upscaling prerequisites only in renderer scope.


<!-- END PRESERVED SOURCE -->
