# Expanded replica — Renderer Jams/20261028-eviction-heap/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_8a5b9ed861248191a40055060001c1ea&account_id=personal) |
| Source identity | library: Renderer Jams/20261028-eviction-heap/REVIEW.md |
| Preserved source SHA-256 | `75a5fb881af9f750ed09874f2a4fca91063cf878154fa887cd256f0f51072030` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/render-performance.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Replaces full eviction sorting with a primitive max-priority heap, preserving protected-page-last, descending-distance, and stable access-order ties. The caller admits at most 4,096 resident pages; isolated victim-selection medians improve while temporary arrays may allocate more. The review explicitly carries missing native/GPU qualification and possible temporary-allocation growth. This preserved Library copy is byte-identical to its Git jam counterpart.

## Research expansion — gaps and details to deepen

- Comparator compatibility and selection CPU time do not establish better frame latency or memory pressure. The source should distinguish its preserved approximate-distance tie semantics from later exact ranking elsewhere, and link reusable scratch as a separate successor.

## Research expansion — proposed contracts

- Record the eviction ordering tuple, resident-cap dependency, selected-victim count, scratch allocation/retention, and protected-page behavior. Join selection to complete close/retirement and persistence costs before comparing it with the old sort.

## Research expansion — acceptance checks

- Retain 251,940 parity checks, stable ties, and rejected comparator mutation. Measure actual pressure events, heap/GC, full eviction CPU spans, GPU retirement, and visible coverage on the target artifact; do not promote the roughly 83–84% one-victim selector result to FPS.

## Research expansion — source anchors

- Renderer jam 20261028T1301 — read-only self-audit, not independent engine audit

This original shares identical bytes with: [git:docs/renderer-jams/20261028-eviction-heap/REVIEW.md](../../../git/docs/renderer-jams/20261028-eviction-heap/REVIEW.md). Both source identities are retained.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../GRAPHICS-UPSCALING-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/GRAPHICS-UPSCALING-WORK-STATE.json) |
| `LodEvictionHeapBench.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-heap/LodEvictionHeapBench.java) |
| `LodEvictionHeapChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-eviction-heap/LodEvictionHeapChecks.java) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028T1301 — read-only self-audit, not independent engine audit

**Exact source/test snapshot:** [`ca675c2e205a3fd71561e9f18409f4196da3013d`](https://github.com/Shnewbs/Deep-Primal/commit/ca675c2e205a3fd71561e9f18409f4196da3013d). Production change [`fcd437f45dfbac9393b4a06fa7b53363bf9944e7`](https://github.com/Shnewbs/Deep-Primal/commit/fcd437f45dfbac9393b4a06fa7b53363bf9944e7), parent `901953417f7c6899bcf2160275fe9eea3561ea36`. Read-back source blobs: eviction heap `49f8f785a2b271558b72a13c68b858528e9d60c3`, renderer `0665fdbb4f8460100f1cdda6d85eeebff7b3190e`, tests `34ab67fa17fb74e6f0df5f157a422227c1481ad2` and `796b7bc9f71819b9491e42e6f2eba61e0a85867b`. The candidate was not edited during scored review. Review is renderer self-audit only; no whole-engine regrade.

| Six equal-weight categories | Score /10 | Pinned evidence and missing acceptance |
| --- | ---: | --- |
| Distance/LOD correctness and useful coverage | **6.50 carried** | [Original bounded LOD nearest selection](https://github.com/Shnewbs/Deep-Primal/blob/ca675c2e205a3fd71561e9f18409f4196da3013d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L19-L29) remains. No native coverage or unexplored terrain delivery. |
| Mesh generation, reuse and scheduling | **6.50 (+0.25)** | [Primitive heap construction/poll](https://github.com/Shnewbs/Deep-Primal/blob/ca675c2e205a3fd71561e9f18409f4196da3013d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java#L22-L93) replaces full sort in [pressure-only eviction](https://github.com/Shnewbs/Deep-Primal/blob/ca675c2e205a3fd71561e9f18409f4196da3013d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L566-L596). Matched isolated CPU p50 falls ~83–84% for one eviction at 4096 entries; not a measured game-frame improvement. |
| GPU culling, submission and memory | **4.00 carried** | Cache victim **ordering only** changed; actual GPU allocation, close and byte accounting preserved in [same caller](https://github.com/Shnewbs/Deep-Primal/blob/ca675c2e205a3fd71561e9f18409f4196da3013d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L577-L595). No GPU memory or frame measurements. |
| Actual FSR/DLSS integration | **5.00 carried** | [Graphics upscaling state](../../GRAPHICS-UPSCALING-WORK-STATE.json) documents existing FSR 1 spatial; temporal FSR/DLSS runtime dispatch still not validated. No related changes. |
| Visual, backend and settings compatibility | **5.00 carried** | Same byte limits, page revision/old-close, persistence, water and settings code. [Parity oracle](LodEvictionHeapChecks.java) covers ordering; native water pixels, Vulkan/GL resize/reload and world lifecycle unrun. |
| Validation and measured performance | **4.50 carried** | Java21 `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **251,940 passing assertions**; intentionally mutated tie comparator fails. [Paired benchmark](LodEvictionHeapBench.java) at 256 and 4096 entries ran twice. This is CPU microbenchmark, no full game/Java25/native/GPU/VRAM acceptance. |
| **Average** | **31.50 / 60 = 5.25/10** | Previous renderer self-audit **5.21**, delta **+0.04**. Whole-engine independent **5.0** historical and unchanged. Target >8.5 not met. |

**Review caveats:** The new heap has primitive arrays O(n) and may allocate *more temporary bytes* per eviction call than the old sort; CPU timing improvements do not prove improved GC or fps. It computes unchanged double squared distances, so ties follow previous comparator even when double overflows to infinity. No native GPU compilation, water transparency validation, frame-time tails or JAR packaging. JDK25 and archive unavailable in this workspace; Java21 remains installed. New heap is only exercised by real client after owner build.

**Next:** native owner Vulkan/OpenGL camera travel, edit, teleport, saved-world reopen and under-pressure evacuation; paired render-thread p50/p95/p99, actual GPU fences, RAM/VRAM, draw calls, transparency and frame captures. Then optimize native GPU batch/indirect/culling or temporal upscaling from real evidence. Renderer scope/claim only; other schedules and their source unchanged.


<!-- END PRESERVED SOURCE -->
