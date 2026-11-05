# Expanded replica — Renderer Jams/20261028-capture-distance-exact/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_5a3a60a730988191ba558c07b4c962fc&account_id=personal) |
| Source identity | library: Renderer Jams/20261028-capture-distance-exact/REVIEW.md |
| Preserved source SHA-256 | `fee2e45fc2b1ac910d9e77bdd550f8d387a06f2faaf92fab0c255cf8c8bc5ef2` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/render-performance.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs capture-priority double-distance ties by comparing exact unsigned 128-bit sums of widened delta squares within the same Chebyshev ring. Exact distance ties remain insertion ordered, and the existing oldest-first fairness escape is preserved. The 10,174 focused assertions and paired chooser timings remain source/JVM evidence only. This preserved Library copy is byte-identical to its Git jam counterpart.

## Research expansion — gaps and details to deepen

- Exact ordering is a correctness property, while the recorded chooser medians became slightly slower. The extension should connect this helper to actual dirty-queue age and visible publication without making extreme-coordinate correctness a performance claim.

## Research expansion — proposed contracts

- Document the full ordering tuple, signed coordinate domain, insertion-tie policy, and fairness quota. Preserve exact tested-source identity, including the corrected trailing newline, and link independent GPU-relative coordinate and projection contracts.

## Research expansion — acceptance checks

- Retain the BigInteger oracle and former-baseline failure at signed extrema, plus saturation/fairness checks. On the native client, measure queue selection cost and capture-to-visible age under realistic and adversarial dirty sets; verify no geometry or water behavior changes.

## Research expansion — source anchors

- Renderer jam 20261028T1154 — read-only self-audit of exact committed candidate

This original shares identical bytes with: [git:docs/renderer-jams/20261028-capture-distance-exact/REVIEW.md](../../../git/docs/renderer-jams/20261028-capture-distance-exact/REVIEW.md). Both source identities are retained.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../GRAPHICS-UPSCALING-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/GRAPHICS-UPSCALING-WORK-STATE.json) |
| `../20261028-near-ring-fast-reject/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-near-ring-fast-reject/REVIEW.md) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028T1154 — read-only self-audit of exact committed candidate

**Pinned source commit:** [`6ec69da13429709f53b6a664af4ed14e120a35d8`](https://github.com/Shnewbs/Deep-Primal/commit/6ec69da13429709f53b6a664af4ed14e120a35d8), immediate parent `826aee58e11024fe1d0ed2480eba5c9ca8443396` (the earlier Git contents transfer lost a final newline). Claimed parent `aa91e9c3cd7464cf3133e6b79c1d4f159c1e1c64`; scoped claim `1f681c22ae9e533d62d83f46090cad624e36c148`. Git blob for production file `c0f94f73ffa989738a71b0ce019a0160a137c817` matches exact tested local file bytes. New test blob `0d07d4500e7a50d8e0bdd97d9d8199f73fac620a`; CPU diagnostic blob `e47724ab589e46d9cc2b53d710c92bde43f14fc5`. **No production changes during this final read-only self-review.** This is not the independent whole-engine auditor.

| Equally weighted renderer category | /10 | Pinned evidence and unrun gates |
| --- | ---: | --- |
| Distance/LOD correctness and useful coverage | **6.50 (carried)** | [Capture priority long delta and exact comparison](https://github.com/Shnewbs/Deep-Primal/blob/6ec69da13429709f53b6a664af4ed14e120a35d8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L26-L68) and [BigInteger oracle / extreme case / saturation test](https://github.com/Shnewbs/Deep-Primal/blob/6ec69da13429709f53b6a664af4ed14e120a35d8/docs/renderer-jams/20261028-capture-distance-exact/LodCaptureExactDistanceChecks.java#L20-L121). Exact same-ring distance correctness improved, but no game-visible LOD or unseen terrain evidence. |
| Mesh generation/reuse/scheduling | **6.25 (carried)** | [Chooser](https://github.com/Shnewbs/Deep-Primal/blob/6ec69da13429709f53b6a664af4ed14e120a35d8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java#L19-L68) retains FIFO starvation escape and no queue copy. Old source fails new regression. CPU median 256/4096 key samples mildly slower, so no performance promotion. |
| GPU culling/submission/memory | **4.00 (carried)** | No draw, barrier, buffer, descriptor, fence, VRAM or Vulkan implementation changed. Native GPU not run. |
| Actual FSR/DLSS integration | **5.00 (carried)** | [Graphics upscaling state](../../GRAPHICS-UPSCALING-WORK-STATE.json) reports FSR1 spatial present, temporal FSR/DLSS SR active production dispatch and RTX acceptance absent. No upscaling work this jam. |
| Visual/backend/settings compatibility | **5.00 (carried)** | No mesh positions/material order, water depth/transparency, backend, settings, shader, resource IDs, profiles or textures changed. Native GL/Vulkan water pixels and reload unrun. |
| Validation and measured performance | **4.50 (carried)** | Java21 strict compile and `-ea -Xverify:all`: **10,174 assertions** in two focused suites. Baseline negative control exits 1 at an expected demonstrated ordering bug. [CPU microbenchmark](https://github.com/Shnewbs/Deep-Primal/blob/6ec69da13429709f53b6a664af4ed14e120a35d8/docs/renderer-jams/20261028-capture-distance-exact/LodCapturePriorityBenchmark.java#L12-L31) two paired runs; 256-key baseline/candidate medians **521/641**, **561/631** ns; 4096-key **6920/7150**, **6940/7070** ns. No full native or GPU benchmark. |
| **Average** | **31.25/60 = 5.2083 ≈ 5.21/10** | Previous [5.21/10](../20261028-near-ring-fast-reject/REVIEW.md); **delta 0.00**. Whole-engine 5.0/10 historical grade unchanged, not regraded. Owner >8.5 target not met. |

**Test environment:** Debian Linux OpenJDK 21.0.12.1. Full JDK25 absent here; Linux owner-provided archive not mounted. No full Gradle, Windows Minecraft native run, GPU pixels, AMD/NVIDIA upscaling or RTX DLSS testing, shader resize/reload, page-upload/fence/frame-time/VRAM, world save/reopen, packaged artifact CRC/class/native validation or release. Only test-evidence ZIP integrity can be validated; never label that as a game JAR. Reviewer found no blocker in the scoped source/test snapshot. Next meaningful acceptance remains the owner-native Vulkan/OpenGL camera/travel/edit/reopen scene matrix and fixed hardware measurements. Keep external engine reviewer separate.


<!-- END PRESERVED SOURCE -->
