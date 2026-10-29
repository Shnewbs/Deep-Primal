# Expanded replica — docs/engine-hourly-audits/2026-10-28T0432-09a57c1/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0432-09a57c1/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T0432-09a57c1/REVIEW.md |
| Preserved source SHA-256 | `bdb670c8d5759c47a60886ee51996a9e6f6d7ae119ddebde8d5700a043e16718` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal — independent audit, 2026-10-28 04:32 UTC”. Its original opening states: “Pinned committed main: 09a57c1896eb1429b2af83223d4a3d6bd5ad8724, version 0.0.1-alpha.29-dev.45-v8sections.1. This was an independent, read-only production-source audit using isolated local source/test/output paths. No Builder, renderer, work-state, lease, shared cache or user JAR was modified.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityChunk`, `DensitySectionLayoutChecks`, `DeepPrimal_Audit_20261028T0432_09a57c1_SourceEvidence.zip`.

## Research expansion — gaps and details to deepen

- Deepen exact-versus-approximate density evidence and keep a pure sampler result separate from native terrain materialization.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version arithmetic order, lattice spacing, graph limits, interval certificates, and exact fallback behavior; preserve a scalar reference oracle.
- Document whether a reported speedup excludes materialization, host biome/light/status work, allocation, or cold regional construction.
- Carry the documented approximation disagreements and stage-two integration block as historical evidence. A future optimization must compare sign/occupancy decisions independently and meet native cost and compatibility gates before promotion.

## Research expansion — acceptance checks

- Exercise zero crossings, negative/far coordinates, partial vertical sections, overflow endpoints, uncertain intervals, and all sampling orders.
- Compare actual generated blocks, base-column queries, heightmaps, lighting invalidation, and save/reopen at the real adapter boundary.

## Research expansion — source anchors

- Deep Primal — independent audit, 2026-10-28 04:32 UTC
- New source inspected and executed
- Strict ten-category rubric (carried, not newly regraded)

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [architecture authoring](../../../../../chapters/architecture-authoring.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — independent audit, 2026-10-28 04:32 UTC

**Pinned committed main:** [`09a57c1896eb1429b2af83223d4a3d6bd5ad8724`](https://github.com/Shnewbs/Deep-Primal/commit/09a57c1896eb1429b2af83223d4a3d6bd5ad8724), version `0.0.1-alpha.29-dev.45-v8sections.1`. This was an independent, read-only production-source audit using isolated local source/test/output paths. No Builder, renderer, work-state, lease, shared cache or user JAR was modified.

## New source inspected and executed

- [DensitySectionLayout.java L45–109](https://github.com/Shnewbs/Deep-Primal/blob/09a57c1896eb1429b2af83223d4a3d6bd5ad8724/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensitySectionLayout.java#L45-L109) projects source V8 `DensityChunk` occupancy into Y/Z/X 16³ palette bits with signed-Y floor sections, partial-section coverage, immutable export and deterministic traversal. Actual Git blob `669a9781fb6c7cb78d7de7744b85d26d9b0bf712` was verified before compilation alongside the exact nine-source product graph/test snapshot. The registered Java21 `DensitySectionLayoutChecks` **passed 3,792,966 assertions, 2,043,904 native index positions and 499 sections**. New independently authored dense-value oracle **passed 4,929,571 assertions, 2,756,608 native index positions, 673 sections from 124 chunk samples**, including 2,048-block full/empty columns, extreme signed coordinates, eight heterogeneous volume shapes and top-exclusive heightmap parity. These tests validate math/bit order, **not** actual NeoForge section writes, lighting or saved V8 worlds. GitHub source search found the `project()` call only in tests/docs, not in native generation.
- [LodPageUploadProbe.java L13–59](https://github.com/Shnewbs/Deep-Primal/blob/09a57c1896eb1429b2af83223d4a3d6bd5ad8724/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageUploadProbe.java#L13-L59) and [VoxelLodRenderer.java L630–750](https://github.com/Shnewbs/Deep-Primal/blob/09a57c1896eb1429b2af83223d4a3d6bd5ad8724/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L630-L750) add opt-in page-level diagnostics after successful GPU cache insertion. Exactly pinned helper `ca5535ad8d6ce443455c266b821ccd51e1dc8842` and RollingTiming `0be7512957537596295ba204f8f1dfc176aa686f` compiled with Java21. Registered **36,117 assertions** and smoke pass; independent **six-thread, 36,000-operation/366-assertion** counter, percentile bound and world-reset test passes. Post-admission memory drops are measured; prior pre-admission rejections are not represented by that drop counter. This diagnostic has no real Vulkan/OpenGL frame, VRAM or GPU-fence acceptance and proves no speedup.

**Test conditions:** Linux x86_64 AMD EPYC 9V74; `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` with separate compiler/classes directory. A first independent test-only compile failed on a double-to-int oracle mismatch; correcting the integer-coordinate cast produced the final full passing run, with no product code change. The requested Temurin Java25.0.5+8 directory/archive was not mounted.

## Strict ten-category rubric (carried, not newly regraded)

| Category | /10 | Primary blocker |
|---|---:|---|
| Terrain realism and variety | 5 | Native V8 generator and 3D/visual acceptance absent |
| Biome coherence | 4 | Weighted ecotone/placement system not integrated |
| Hydrology and caves | 6 | Water callback/quantity ledger, physical unload/reopen and long-term native acceptance incomplete |
| Features/structures | 4 | Deterministic general placement/exclusion system missing |
| Performance/threading | 3 | Historical native warm fill 14.45–14.99ms exceeds target of 5ms; no fresh native measure |
| Determinism/stability | 7 | Frozen V1–V7 verified historically; V8 native profile/legacy/cross-host acceptance missing |
| Architecture | 6 | Modular core/renderer/LOD; V8 native bridge incomplete |
| Extensibility | 4 | Saved graph/biome/feature authoring APIs not active |
| Testing/benchmarks/docs | 7 | Core checks strong; no current Java25/full/native/GPU/soak |
| Polish/tooling | 4 | Waterfalls, flicker, distant LOD, GPU diagnostics/GUI/terrain tools unfinished |
| **Overall — Tier 2** | **50/100 (5.0/10)** | **Strict >8.5 goal not attained** |

The category numbers are the **historical 2026-10-24 complete review**, carried without a full regrade. Newly tested unintegrated infrastructure receives no gameplay performance credit.

**Not run:** Java25 full Gradle 9.2.1/modules, native Minecraft section/base-column/lighting/save-reopen/shutdown, selected JAR, nested ZIP CRC/compiled package identity, Windows AMD 8060S, Vulkan/OpenGL framebuffer/VRAM/FSR/DLSS and matched host p95/p99. Per the **October 27 owner override**, the user handles complete builds and publication; this audit made **no JAR, GitHub Action, GitHub release or CurseForge release**. Existing JAR retention was not changed.

**Next priorities:** (1) owner's Java25 native V8 section/block/heightmap/profile/legacy/lighting checks and matched chunk performance; (2) Builder's finite-water callback/conservation, unloaded ownership and cliff continuity; (3) renderer's real GPU LOD upload/draw/fence/memory acceptance, same-scene A/B of optional splitting. Keep the independent auditor active and nonblocking until a fresh complete independent grade **strictly above 8.5/10** or owner cancellation.

**Auditor-only local evidence:** `DeepPrimal_Audit_20261028T0432_09a57c1_SourceEvidence.zip`; 36,720 bytes, SHA-256 `ef18121dc73b8de9a32ee2648abd2cf513a55754dd8c6778259a6d2565babecb`, 29 valid ZIP entries. This ZIP is source/test evidence, not an installable JAR.


<!-- END PRESERVED SOURCE -->
