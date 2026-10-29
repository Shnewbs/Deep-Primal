# Expanded replica — docs/renderer-jams/20261028-filename-parser/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-filename-parser/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-filename-parser/REVIEW.md |
| Preserved source SHA-256 | `1f2f7e487657b4b19a5f36dfe380628c789f78f55521fbcd2812e270b2d5b672` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Parses canonical signed x_z.plod names directly to avoid intermediate substrings, arrays, and reconstructed strings. It rejects overflow and noncanonical signs/zeros while preserving exact nearest ordering, scan-turn limits, and queue capacity. The 22–24% allocation reduction is a scanner-thread result, not a renderer or GPU speedup.

## Research expansion — gaps and details to deepen

- The parser lowers measured scanner allocations but both reported CPU medians are slower. Full rescans still visit all names, so the meaningful next contract is discovery workload and producer notification rather than assuming faster string parsing solves I/O tails.

## Research expansion — proposed contracts

- Document filename grammar, signed-domain boundaries, invalid-name policy, and allocation scope. Add examined/retained/rejected name counts, scan age, and eventual write-publication notification as a separately qualified proposal; keep periodic reconciliation for recovery.

## Research expansion — acceptance checks

- Retain 3,122 filename cases and the seven-suite regression, then use huge real saves with cold/warm filesystem states and stationary file arrival. Measure I/O latency, Java allocation/retention, and native visible age; preserve the observed slower CPU results.

## Research expansion — source anchors

- Renderer jam 20261028-filename-parser — read-only six-category self-audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-filename-parser — read-only six-category self-audit

**Exact scored code snapshot:** [`4d19fa909bb39fe21a5db95f87fe9eeb5092c108`](https://github.com/Shnewbs/Deep-Primal/commit/4d19fa909bb39fe21a5db95f87fe9eeb5092c108), version `0.0.1-alpha.29-dev.50-v8strict.1`. Source and test blobs were read back from GitHub after the guarded main push and matched the executed local bytes. This audit occurred **after implementation was frozen** and involved no source edits. It is the **renderer jam self-audit**, not the independent whole-engine audit. Historical prior renderer grade: [5.17/10](../20261028-nonempty-refresh/REVIEW.md). No fresh GPU acceptance or packaged binary.

| Equally weighted category | Score /10 | Pinned evidence and missing gates |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.5 (carried)** | Existing [8,192-key/256-turn contract](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L19-L26) and [exact signed-distance ranking](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L312-L344) retained. Seven focused suites passed, including [3,122 filename parity cases](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/docs/renderer-jams/20261028-filename-parser/LodFilenameParserChecks.java#L59-L100). No native pixels, unseen-world delivery, save/reopen or effective full-detail reach shown. |
| Mesh generation/reuse/scheduling | **6.25** | [Allocation-free numeric parsing in worker metadata selection](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L240-L257) and [bounds/overflow code](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L283-L311) reduce a repeat-scan's measured allocated Java bytes by 22–24% in the isolated scanner. Not mesh generation itself, not proof of fewer GPU draws or faster game frame. Modest +0.25 scoped scheduling credit. |
| GPU culling/submission/memory | **4.0 (carried)** | Commit changes only metadata indexing + source-excluded test; no culling buffers, indirect draws, VRAM pooling, GPU fence or GPU measurements. |
| Actual FSR/DLSS implementation | **5.0 (carried)** | [FSR1 spatial documentation](../../UPSCALING.md) and inactive NGX temporal path unchanged. No temporal FSR, active DLSS-SR evaluate, licensed packaged runtime or NVIDIA RTX test. Frame generation remains a separate unavailable feature. |
| Visual/backend/settings compatibility | **5.0 (carried)** | Existing renderer backend, water transparency, shader, resource identifiers, saved config/profiles untouched (see [exact commit](https://github.com/Shnewbs/Deep-Primal/commit/4d19fa909bb39fe21a5db95f87fe9eeb5092c108)). No Vulkan/OpenGL visual or reload test. |
| Validation and measured performance | **4.5 (carried)** | Java21 `javac --release 21 -Xlint:all -Werror` and seven `java -ea -Xverify:all` suites passed **43,764** final assertions; [test fixture](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/docs/renderer-jams/20261028-filename-parser/LodFilenameParserChecks.java#L1-L104). Two measured before/after scanner-allocation runs: p50 reductions 24.3%/22.3%; measured CPU p50 was **worse** both times, and shared-host path I/O is noisy. Java25, full Gradle, GPU frame timings and native acceptance absent. |
| **Overall** | **31.25/60 = 5.2083 ≈ 5.21/10** | Previous 31.0/60 = 5.1667 ≈ 5.17; exact delta **+0.0417/10**. Far below target **>8.5/10**. |

**Read-only conclusion:** the parser is materially lighter in temporary Java allocations while preserving previous nearest-index ordering and canonical 32-bit names on tested filesystems; it has **not** shown better CPU scan latency or Minecraft frame time. Java25 and native hardware remain unrun, not failed. Renderer source remains frozen at `4d19fa9`. The independent ten-category whole-engine result remains historically **5.0/10**, not regraded. The renderer-only ownership claim is released in the documentation checkpoint.


<!-- END PRESERVED SOURCE -->
