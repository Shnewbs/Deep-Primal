# Expanded replica — docs/renderer-jams/20261028-page-upload-latency/TESTS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-page-upload-latency/TESTS.md) |
| Source identity | git: docs/renderer-jams/20261028-page-upload-latency/TESTS.md |
| Preserved source SHA-256 | `131968adc4438b34609a16a78bd6bf5f85220735aea2d4f79ac8166289be32ad` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds 240-observation page admission-to-cache-commit elapsed-time, extraction-pass count, and accepted-payload windows, with successful completions separated from stale and memory drops. The final source records after successful cache insertion and old-resource cleanup.

## Research expansion — gaps and details to deepen

- Wall elapsed page latency includes queueing and multiple frames; it is not GPU transfer time. Completion may need a separate visible/publication stage, and failed cleanup must not silently create a successful observation or lose allocation ownership.

## Research expansion — proposed contracts

- Add page identity, source generation, queue wait, accepted upload, cache publication, first visible frame, and GPU completion events. Preserve distinct drop reasons, saturation, and zero-sample semantics, then correlate the probe with part and frame histories.

## Research expansion — acceptance checks

- Retain the 36,117-assertion randomized and drop-classification tests plus source ordering checks. In the exact artifact, compare default and optional split uploads with page-to-visible latency, GPU timestamps, memory peak, complete-frame tails, and water images.

## Research expansion — source anchors

- Focused test receipt (renderer-only)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Focused test receipt (renderer-only)

Pinned product code `0f1ef9cd0f0e739a6bd757421105df42b3f85d44`. This jam does **not** contain a qualified JAR.

- `javac --release 21 -Xlint:all -Werror -d classes @sources.txt`: **PASS** (three production Java source shapes plus the standalone helper and smoke).
- `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadProbeChecks`: **PASS** 36,117 assertions / 60,000 randomized observations across 12 capacity/seed combinations.
- `java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadSmoke`: **PASS**; its committed blob `ba5a8d5686e6da74b83af3ad23c10df10e58f6b9` matches local executed source.
- Private mutant: change only `case MEMORY -> memoryDrops` to `case MEMORY -> staleDrops`; compilation passes and the extended fixture exits **1** at `successful-only counts` (expected negative control).
- Final Git read-back of all four source/test paths: **PASS** at the pinned commit. Six integration anchors verify two discard paths, saturating frame count, reset, labeled diagnostics and cache-commit-before-record ordering.
- Java25 owner archive and directory absent in this isolated Linux environment. `java -version`, `javac -version` = 21.0.12.1. **No** full Gradle/module/product build, native Minecraft, GPU, Windows DLL, delivered JAR or actual FPS acceptance.

Evidence ZIP: `DeepPrimal_Renderer_PageUploadLatency_20261028_0f1ef9c_SourceEvidence.zip`, **6,765 bytes**, SHA-256 `fb7e2192189eb6a54df397f1caec58da5e0c5131eb7a502424bd4a4a408c90ec`, 9 CRC-valid entries. Source-only; no release.


<!-- END PRESERVED SOURCE -->
