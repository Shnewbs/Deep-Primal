# Expanded replica — docs/renderer-jams/20261027-pending-accounting-integration/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-accounting-integration/README.md) |
| Source identity | git: docs/renderer-jams/20261027-pending-accounting-integration/README.md |
| Preserved source SHA-256 | `41a82196d9db4689f0e0ef4bd12859601c103979458c0db3071e9ac5f3183df0` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Integrates indexed PendingGpu.bytes summation into production without replacing newer retarget work. The focused Java25 equivalence fixture mirrors the private shape, while a complete module build and native artifact are explicitly unrun at this checkpoint.

## Research expansion — gaps and details to deepen

- A mirror can confirm algorithm equivalence without executing the actual private production class. The later committed byte ledger changes complexity and ownership assumptions, so the expansion should preserve the historical O(parts) semantics and link their successor.

## Research expansion — proposed contracts

- Add source/mirror identity and actual-class gate status to the receipt. Define when byteSize may change, who owns accepted buffers, and which later ledger implementation supersedes recomputation; do not import immutability assumptions backward into this integration.

## Research expansion — acceptance checks

- Exercise empty and multi-part sums, overflow policy, and mutation expectations against the real class when available. Measure the actual admission path and retain full native/module qualification as a separate gate from the 10,063-case mirror.

## Research expansion — source anchors

- Renderer jam 20261027-pending-accounting-integration
- Reproducible scoped execution

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `build-attempt.log` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-accounting-integration/build-attempt.log) |
| `probe.log` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-accounting-integration/probe.log) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-pending-accounting-integration

The renderer schedule promoted the already-staged, exact private-array accounting loop into **production** [VoxelLodRenderer lines 96–104](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96-L104), without pulling an older full VoxelLodRenderer candidate over current retargeting, changing water/transparency, or editing engine/water/graphics-upscaling ownership.

**Parent main:** a5b5f0c7bf3a6b2774d47721c9a8b6ccb71253f0. **Scoped lease commit:** bab2ec0ce5ef38f40281076765c31a7d37a918ca. **Product code:** [b53d66c0c326de8f5259d42a49598cdd79004d92](https://github.com/Shnewbs/Deep-Primal/commit/b53d66c0c326de8f5259d42a49598cdd79004d92). **Source blob:** e035637f91b1800d06b0c47c7676f35db8ddc118. Exactly one method is changed: the nested PendingGpu.bytes() once used a newly built three-stage Stream pipeline on repeated upload/memory budget checks. It now performs simple indexed accumulation over the same three ArrayLists and returns the same long total. No GPU draw method, shader, mesh geometry, resource, resident-accounting mutation, water surface, profiling switch or version label was changed. Source before/after prefix and suffix were verified byte-identical. The result matches the previously staged candidate algorithm, excluding its comment.

## Reproducible scoped execution

Temurin **Java 25.0.5+8** was located in /mnt/data/deepprimal-toolchains/jdk-25.0.5+8 and actually invoked. A deterministic standalone Java25 structure-equivalence control compared the original stream expression and exact integrated indexed-loop algorithm on 10,063 three-level cases: **10,063/10,063 pass**. Under a separate warmed, interleaved four-old/four-new 200,000-call JVM microprobe, every pair had identical checksum 3026541304310; median wall times were **27.0195 ms old** and **10.164 ms indexed** and allocated byte counts **108,800,040 vs 40 per 200,000 calls**. This is a *synthetic accounting microprobe*, not real client FPS, GPU memory, load time, p99 frame latency or a full source build. See [probe summary](probe.log), [isolated build attempt](build-attempt.log) and [self-audit](REVIEW.md). Portable test source, full logs and structured evidence were saved in evidence ZIP SHA256 e119228921390b1020f59bcd49fac74f396c98787f2947ef1ec650909b02ea66.

The official module/private admission fixture and exact-root Java25/Gradle build could **not** execute: an isolated main clone failed `Could not resolve host: github.com` (exit 128). Full checked-out source/Gradle wrapper absent, despite JDK25 being available. There is **no fresh qualified JAR** and no GitHub or CurseForge release, and no old dev32 binary has been relabeled. The checked source's existing `mod_version` is 0.0.1-alpha.29-dev.33-loadedreads.1; it is not a new distributable identity.

## Renderer backlog handoff

1. Restore exact Git checkout+Gradle dependencies and run full Java25 root, module, native, package and source identity gates, including the already-staged admissionChecks for actual private PendingGpu.
2. Capture qualified OpenGL/Vulkan and AMD8060S camera/travel/edit matched p50/p95/p99 frame/upload/heap/VRAM, culling, section rebuild, draw submissions; test pending upload cancellation, resize/reload/world switch and transparency.
3. Integrate staged capacity/admission checks only after current-main API/test reconciliation; preserve the released source retarget fix, frozen V1–V7 geography and independent water+FSR1/DLSS ownership.
4. Investigate real native horizon delivery/LOD fairness and explicit unexplored-terrain authority; do not equate a larger slider or old software FSR1 results with new FPS/RTX/DLSS acceptance.

The scoped lease is released at this checkpoint. Previous independent full-engine grade 50/100 is unchanged.


<!-- END PRESERVED SOURCE -->
