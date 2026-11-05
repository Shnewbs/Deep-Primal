# Expanded replica — docs/renderer-jams/20261029-normal-angle-fastpath/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-normal-angle-fastpath/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-normal-angle-fastpath/REVIEW.md |
| Preserved source SHA-256 | `6e86c085bcbca675c3d11c08b114673bad669b270fff4aaf672026a21c5d4e90` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Audits ab15e44’s exact helper/test and unchanged renderer call site. It records 1,400,403 normal-angle checks per mode, inherited regression coverage, mutation rejection, and two warmed arithmetic comparisons, while explicitly excluding native production call-stack timing and GPU acceptance.

## Research expansion — gaps and details to deepen

- The source proof is strong for the scoped numeric transformation but leaves deployment and workload frequency unverified. The large fixture percentage can obscure the small number of angle comparisons per optional extraction and the fact that default gameplay does not enable this gate.

## Research expansion — proposed contracts

- Add a cost attribution record containing active policy, extraction frequency, gate invocations, actual CPU span, total cull/frame span, and uncertainty. Preserve jam29’s adverse result as history and identify the optimized source successor without combining percentages from different fixtures.

## Research expansion — acceptance checks

- Verify exact packaged source identity, actual camera getters, and ordinary/extreme/nonfinite parity under the supported runtime. Capture fixed/adaptive/off camera paths and lifecycle failures with CPU/GPU tails and water pixels; retain default-off unless native quality and performance evidence supports a change.

## Research expansion — source anchors

- Jam 30 renderer self-audit — code read-only, independent engine grade unchanged
- Pinned evidence and tests

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Jam 30 renderer self-audit — code read-only, independent engine grade unchanged

**ID** `renderer-20261029T0957-normal-angle-fastpath`. **Previous main** `954e2323727170ea9ef56b2ac4db59fa5f497c32`; claim commit `2b447a3fab9aeaa81a96ad45743c82278e00ad99`; **pinned production/test source** [`ab15e44cdad0a40966b6682f06b486031bb3aa5e`](https://github.com/Shnewbs/Deep-Primal/commit/ab15e44cdad0a40966b6682f06b486031bb3aa5e). This review followed source completion and used GitHub-pinned blobs; no code changes during scored review. **Not** an independent external engine audit. Only the helper and a new renderer jam test changed; no other schedules or modules were changed.

## Pinned evidence and tests

- [Helper lines 58–76](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java#L58-L76), Git blob `af8f3870bdec7013e3e26becff72e7771ca8d51a`: ordinary finite headings use bounded conditional subtract instead of float `% 360`; extreme finite angle double remainder, nonfinite conservative fallback, four-block/eight-degree sample thresholds unchanged. Previous exact helper diff verified.
- [Exact-parity test lines 25–56](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/docs/renderer-jams/20261029-normal-angle-fastpath/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumNormalAngleChecks.java#L25-L56), blob `cb18b130b2410447c85692370321ae9412645605`: 1,400,403 comparisons/checks per mode, 350k random normal pairs, sign mirrors, edge values, 8° decision and extreme overflow witness.
- [Unchanged LOD native handoff, coarse/fine fallback and ordering](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L527-L611), blob `2bb1c95d9b800da7b036d0b1a89f4249afca3c87`. Water alpha and shader not touched.
- `javac --release 21 -Xlint:all -Werror -d classes @sources.list`, then 17 executions of `java -ea -Xverify:all` (seven regression classes × two adaptive flags, plus three broadphase overrides): **4,218,937 assertions passed**, 10/10 source/diff checks passed. Negative missing-wrap mutant failed at check 50 (expected). Exact logs, source, test doubles and SHA256 manifest are in the evidence ZIP.
- Paired synthetic arithmetic fixture: 41 samples × 150k operations per Java process, 20 warmups, two processes. Old p50/p95/p99 **534024/657571/714818 ns**, candidate **167413/188464/365621 ns**; repeated old **537039/629359/682218 ns**, candidate **167582/186290/329807 ns**. Median synthetic reduction **68.6–68.8%**. This duplicates arithmetic in a fixture and is **not actual production call-stack frame time**, GPU pixel output, measured FPS, or distance-range benefit.
- Host: Linux, Java21. Owner JDK25 path/archive absent and not located in Library, full build remains owner-operated. No full Java25 Gradle 9.2.1/NeoForge, Windows AMD/RTX, Vulkan/OpenGL native, water pixels, GPU p50/p95/p99/VRAM, reload/resize/world switch/reopen, temporal FSR/DLSS dispatch, or qualified JAR.

## Six equal-weight renderer grades, /10

| Category | Score | Pinned grounds and limitations |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.55 carried** | [Native handoff and fine culling](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L535-L611) unchanged; distant unexplored coverage unproven |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Earlier mesh/split checks](../20261028-split-preflight-once/REVIEW.md); no mesh work this jam |
| GPU culling/submission/memory | **4.55 carried** | [CPU heuristic helper](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java#L58-L76); no GPU indirect/VRAM data |
| Actual AMD FSR / NVIDIA DLSS | **5.00 carried** | [FSR 1 spatial implemented](../../UPSCALING.md); no temporal FSR or in-game RTX DLSS evaluate |
| Visual/backend/settings | **5.30 carried** | [Unchanged fine-part fallback](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L527-L611); pixels/native unrun |
| Validation/measured performance | **6.00 (+0.05)** | [Parity regression](https://github.com/Shnewbs/Deep-Primal/blob/ab15e44cdad0a40966b6682f06b486031bb3aa5e/docs/renderer-jams/20261029-normal-angle-fastpath/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumNormalAngleChecks.java#L25-L56), 17 Java runs, 4,218,937 assertions, mutant, 10 shape checks, narrow synthetic timings, no full-frame measurements |
| **Mean** | **5.73 (+0.01)** | (6.55+6.95+4.55+5.00+5.30+6.00)/6 = **5.725**, half-up to 5.73 vs jam29 5.72 |

External whole-engine baseline **5.0/10 carried**, not changed. Scores are not inflated by missing GPU or unavailable temporal provider functionality. Full source review and exact bench logs retained in the Library evidence archive (`31071` bytes, 25 entries, CRC and member SHA256 PASS). No JAR/release. **Next priority:** on-device Vulkan/OpenGL fixed/adaptive/off A/B under camera travel/all-visible/offscreen views and transparent-water pixels; then profile actual GPU submission/mesh upload and newer FSR/DLSS prerequisites before raising native claims.


<!-- END PRESERVED SOURCE -->
