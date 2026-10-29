# Expanded replica — docs/renderer-jams/20261028-near-ring-fast-reject/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-near-ring-fast-reject/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-near-ring-fast-reject/REVIEW.md |
| Preserved source SHA-256 | `152b008df1f61c4cfa02cc7dc0c81ab6202a595cb202cbb3cbf69bd90bba48d1` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Rejects strictly farther Chebyshev-ring disk candidates before boxing, set lookup, and exact distance work once the nearest heap is full. Equal rings continue through full ordering; 256-entry turns and 8,192-key retention remain unchanged. The self-audit preserves the flaky observation and unavailable permission branch instead of claiming they passed universally.

## Research expansion — gaps and details to deepen

- The recorded allocation benefit depends on dominated far misses and does not apply uniformly to every directory. A flaky asynchronous scanning-state observation is explicitly separate from product correctness and needs a deterministic fixture signal.

## Research expansion — proposed contracts

- Record candidate population by nearer/equal/farther ring, retained-set capacity, and exact tie policy. Replace transient-state polling in tests with observable lifecycle synchronization while preserving source behavior; link the later same-ring full-rank optimization separately.

## Research expansion — acceptance checks

- Keep BigInteger ordering and the rejected greater-or-equal mutant, then replay empty, unsaturated, equal-ring, and far-dominated directories. Compare native visible recovery and frame/I/O tails; mixed CPU results and 6.7–7.7% allocation changes are not FPS evidence.

## Research expansion — source anchors

- Renderer jam 20261028T1102 — read-only self-audit
- Validation and evidence

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../GRAPHICS-UPSCALING-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/GRAPHICS-UPSCALING-WORK-STATE.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028T1102 — read-only self-audit

**Pinned implementation:** [`eeeb3d711949f0a867b89ac4474cbad7815be532`](https://github.com/Shnewbs/Deep-Primal/commit/eeeb3d711949f0a867b89ac4474cbad7815be532); parent `a6ee38079422d1e317928ffb50ae57bf2cfd59b1`, inherited version `0.0.1-alpha.29-dev.51-v8preflight.1`. Final source blob `3c2530804b28c74b08d492f7baf343d6517a717b`; source-excluded regression blob `e0a0b229cd2e1365223b99771505f32f15a815b4`. Re-read GitHub blobs after successful guarded push: identical to locally tested files. No production edits during this scored review. **This is renderer self-audit, not an independent whole-engine audit.**

| Equally weighted rubric | /10 | Pinned evidence and explicit missing gates |
| --- | ---: | --- |
| Distance/LOD correctness and coverage | **6.50 (carried)** | [8192-key/256-entry limit](https://github.com/Shnewbs/Deep-Primal/blob/eeeb3d711949f0a867b89ac4474cbad7815be532/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L19-L26), [strict ring check with full same-ring tie](https://github.com/Shnewbs/Deep-Primal/blob/eeeb3d711949f0a867b89ac4474cbad7815be532/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L262-L284), [BigInteger independent oracle across signed extremes](https://github.com/Shnewbs/Deep-Primal/blob/eeeb3d711949f0a867b89ac4474cbad7815be532/docs/renderer-jams/20261028-near-ring-fast-reject/LodNearRingFastRejectChecks.java#L25-L65). No native LOD pixels, save/reopen, edit, or unseen-world coverage. |
| Mesh generation/reuse/scheduling | **6.25 (carried)** | [Early far-ring rejection before Long boxing, hash lookup and RankedKey allocation](https://github.com/Shnewbs/Deep-Primal/blob/eeeb3d711949f0a867b89ac4474cbad7815be532/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L262-L275). Paired 6000-file/512-retained scanner Java-thread allocation p50 improves **7.69%** and **6.73%**. CPU p50 mixed between runs; no demonstrated GPU or game-frame benefit and no separate grade promotion. |
| GPU culling/submission/memory | **4.00 (carried)** | [Actual changed files](https://github.com/Shnewbs/Deep-Primal/commit/eeeb3d711949f0a867b89ac4474cbad7815be532) are saved LOD metadata + standalone test + jam note, not GPU submission/indirect batches/buffer fencing. GPU unrun. |
| Actual FSR/DLSS implementation | **5.00 (carried)** | [Existing upscaler state](../../GRAPHICS-UPSCALING-WORK-STATE.json): FSR1 spatial present; temporal FSR and active DLSS-SR Vulkan NGX dispatch/RTX validation absent. Nothing changed here. No frame generation credit. |
| Visual/backend/settings compatibility | **5.00 (carried)** | No materials, water transparency, shader/API, graphics options, world profiles, IDs, resource reload changes. No actual Vulkan/GL screenshot, resize, save/reopen or shutdown test. |
| Validation and measured performance | **4.50 (carried)** | Java21 `javac --release 21 -Xlint:all -Werror`; eight `java -ea -Xverify:all` suites **79,526 final assertions** including [35,893 independent exact-ranking oracle assertions](https://github.com/Shnewbs/Deep-Primal/blob/eeeb3d711949f0a867b89ac4474cbad7815be532/docs/renderer-jams/20261028-near-ring-fast-reject/LodNearRingFastRejectChecks.java#L42-L72). A deliberately corrupted `>=` boundary failed. CPU p50 mixed; no FPS or native GPU measurements. One earlier asynchronous RetryChecks run intermittently missed a transient scanning state; final full suite passed. |
| **Overall** | **31.25/60 = 5.2083 ≈ 5.21/10** | Prior [5.21](../20261028-filename-parser/REVIEW.md), **delta 0.00**, no inflated graphics score. Whole-engine 5.0/10 **historically carried**, NOT regraded. Target above 8.5 not met. |

## Validation and evidence

Final test totals: NearestScan 9,006; Incremental 8,295; IndexService 529; Retry 4,399; RecoveryEpoch 783; NonemptyRefresh 3,296; FilenameParser 17,325; NearRingFastReject 35,893 = **79,526**. Strict compilation and `-ea -Xverify:all` passed in the final Java21 replay. The earlier one-off flaky RetryChecks failure remains explicit, not represented as a fixed implementation bug. Permission-denied branch was not applicable on this privileged Linux host.

Paired CPU+allocation diagnostic: 6,000 real filenames, cap 512, 40 alternating baseline/candidate samples, 6 warmups, two runs. Run 1 old/new p50: **3,937,464 / 3,634,632 B** allocated, **5,771,734 / 4,971,702 ns** CPU. Run 2 old/new: **3,954,032 / 3,687,808 B** allocated, **4,968,718 / 5,064,202 ns** CPU. Frame p95/p99, VRAM, draw counts and native pixels **not measured**. A `>=` mutation was rejected by the oracle test.

**Owner-build override:** JDK25/archive absent locally and Library filename searches yielded no match. Java25 full Gradle/NeoForge, native Minecraft, Windows Vulkan/OpenGL, AMD/NVIDIA GPU, temporal upscaling, package CRC/native payload, new JAR and release/publisher all **not run**. No old binary relabeled.

**Evidence archive:** `DeepPrimal_Renderer_NearRingFastReject_20261028_eeeb3d7_SourceEvidence.zip`; 40,847 bytes, SHA256 `c1e89e0a22f899200b87ffcb73b6354eb9a9c862ac2ff4e82c78b0341761565b`; all ZIP CRCs and manifest hashes passed. Successful Library upload: `/Deep Primal/Renderer Jams/`.

**Next:** owner-native camera travel/edit/reopen with matching Vulkan/OpenGL GPU/CPU p50/p95/p99, pending and resident RAM/VRAM and water transparency; stabilize the existing asynchronous scanning-state test separately. The scoped renderer claim is released; other writer leases/schedules are unchanged.


<!-- END PRESERVED SOURCE -->
