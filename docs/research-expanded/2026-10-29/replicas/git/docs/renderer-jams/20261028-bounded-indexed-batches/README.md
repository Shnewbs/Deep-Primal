# Expanded replica — docs/renderer-jams/20261028-bounded-indexed-batches/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-bounded-indexed-batches/README.md) |
| Source identity | git: docs/renderer-jams/20261028-bounded-indexed-batches/README.md |
| Preserved source SHA-256 | `0098e0903f402be8dc80639e73381a124f0f75c80ab8bcb6b10935a756470a31` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Bounds each synchronous drawMultipleIndexed call to 4,096 ordered Java draw records and clears references after return or failure. The direct path is preserved, and diagnostics count returned API batches rather than hardware multi-draw-indirect commands.

## Research expansion — gaps and details to deepen

- Bounded reference retention is valuable but does not prove cheaper submission. The recorded CPU preparation comparison was slower, and native transparency, driver calls, and GPU performance remain unmeasured; later hard-limit work also strengthens misuse protection.

## Research expansion — proposed contracts

- Add a borrowed-list lifetime contract, stable pass/material order, completion semantics, and a link to the hard-limit successor. Any future indirect path must have its own command capacity, overflow, culling, and fallback contract rather than reuse this API-batch label.

## Research expansion — acceptance checks

- Retain ordered delivery, callback failure, nested-call, and scrub checks for 131,072 parts. Run exact-artifact Vulkan/OpenGL batched/direct comparisons at ordinary and extreme visible counts with CPU/GPU timings, memory, and transparent-water images.

## Research expansion — source anchors

- Renderer jam 18 — bounded indexed-draw API submission
- Isolated source test

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 18 — bounded indexed-draw API submission

Pinned implementation uses at most 4,096 Java draw-record references per synchronous \`drawMultipleIndexed\` call. It preserves the visible-part order and every draw, including extremely large visible sets, without holding on to the previous frame's indexed records after the API returns or raises a failure. Ordinary API batches are **not GPU multi-draw indirect**.

Existing nonbatched submission stays unchanged. Synchronous batching keeps the same pipeline, transforms, depth, lighting, materials, alpha, water handling, and vertex/index format; the unchanged native GPU and transparency behavior still require owner validation. A new diagnostic reports completed \`drawMultipleIndexed\` API calls, not hardware draw counts.

## Isolated source test

\`\`\`sh
find source tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.BoundedIndexedDrawBatchesChecks
\`\`\`

Production-excluded checks exercise ordered complete delivery, 4,096-entry budget, 131,072 input parts, callback failure, nested submission, reset, and retention scrubbing. See \`BoundedIndexedDrawBatchesChecks.java\` and the separately retained test evidence. Hardware frame-time/FPS, Native Vulkan/OpenGL, Java25/full Gradle/NeoForge and JAR qualification remain owner-run gates.


<!-- END PRESERVED SOURCE -->
