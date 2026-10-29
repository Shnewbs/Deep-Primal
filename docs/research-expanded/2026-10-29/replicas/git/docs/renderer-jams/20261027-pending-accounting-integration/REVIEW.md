# Expanded replica — docs/renderer-jams/20261027-pending-accounting-integration/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-accounting-integration/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-pending-accounting-integration/REVIEW.md |
| Preserved source SHA-256 | `42a05a37c4db03d82e0fa5507a35de2399725d5a136ddfe99949aa5a6b068ca0` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Integrates indexed PendingGpu.bytes summation into production without replacing newer retarget work. The focused Java25 equivalence fixture mirrors the private shape, while a complete module build and native artifact are explicitly unrun at this checkpoint. The reported source integration is not a packaged-client or GPU result.

## Research expansion — gaps and details to deepen

- A mirror can confirm algorithm equivalence without executing the actual private production class. The later committed byte ledger changes complexity and ownership assumptions, so the expansion should preserve the historical O(parts) semantics and link their successor.

## Research expansion — proposed contracts

- Add source/mirror identity and actual-class gate status to the receipt. Define when byteSize may change, who owns accepted buffers, and which later ledger implementation supersedes recomputation; do not import immutability assumptions backward into this integration.

## Research expansion — acceptance checks

- Exercise empty and multi-part sums, overflow policy, and mutation expectations against the real class when available. Measure the actual admission path and retain full native/module qualification as a separate gate from the 10,063-case mirror.

## Research expansion — source anchors

- Read-only renderer jam self-audit — 20261027-pending-accounting-integration

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Read-only renderer jam self-audit — 20261027-pending-accounting-integration

**Reviewed source:** [b53d66c0c326de8f5259d42a49598cdd79004d92](https://github.com/Shnewbs/Deep-Primal/commit/b53d66c0c326de8f5259d42a49598cdd79004d92), exact final code blob e035637f91b1800d06b0c47c7676f35db8ddc118. Audit after production edit and focused JDK25 tests; no code changes during scoring. This is the author's renderer-only self-audit, **not an external engine regrade**.

| Equal-weight renderer category | Previous | Current | Pinned evidence and exclusions |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | 4 | [DeliveredChunkScan retarget](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L30-L45) unchanged; native camera/travel/unexplored terrain not tested |
| Mesh generation/reuse/scheduling | 6 | 6 | [LodMesher](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L1-L65) unchanged; no new mesh output or build/worker timing |
| GPU culling/submission/memory | 4 | 4 | [PendingGpu.bytes](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L86-L106) replaces allocating stream with equivalent indexing; source prefix/suffix exact, [admission points](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L604-L667) unchanged; no GPU residency, driver readback or p99 proof |
| Actual FSR/DLSS | 5 | 5 | Inherited FSR1 EASU/RCAS prior [dev32 evidence](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/docs/validation/upscaling-2026-10-27/README.md), **not rerun**; optional DLSS NGX cannot run in game until temporal inputs/dispatch/RTX hardware, not credited as completed DLSS |
| Visual/backend/settings compatibility | 5 | 5 | No shader/mesh/transparency/control changes; no fresh native Vulkan/GL resize/reload, AMD/RTX acceptance |
| Validation and measured performance | 4 | 4 | JDK25 standalone 10,063 exact-algorithm equivalence cases, eight 200k-call ABBA-like accounting microprobes, matched checksums/allocated bytes. **Not actual loaded production class**; no full Gradle/root/full module or named-GPU benchmark |
| **Mean** | **4.67/10** | **4.67/10** | **Δ 0.00** (28/60); conservative until qualified package/native measurements |

The narrow CPU microprobe is not an FPS improvement claim. Signed-long summing, null/ordering and element membership are not changed; private three-level collections are only mutated on the owning client event flow. The JDK25 component probe is not the separate existing 10,063 real-class [staged admission-gate work](https://github.com/Shnewbs/Deep-Primal/tree/b53d66c0c326de8f5259d42a49598cdd79004d92/docs/renderer-jams/20261027-admission-gate), which stays staged until final source/native qualification. No one has independently validated Windows execution. No Java25 **full** build, build artifact CRC/identity, packaged lifecycle, OpenGL/Vulkan pixels, graphics driver, AMD8060S, RTX/DLSS or upload-delay tail numbers were run.

**Prior independent whole-engine grade remains 50/100 = 5.0/10, Tier 2/5, carried forward.** The renderer grade 4.67/10 is not that overall grade and is far below the >8.5 targeted renderer acceptance. New JARs/releases: **0**.

Next: full checkout/toolchain-dependent packaging first, then real-client compared latency/memory and staged admission test integration.


<!-- END PRESERVED SOURCE -->
