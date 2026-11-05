# Expanded replica — docs/engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md |
| Preserved source SHA-256 | `9da8d0aba905afa57b7b346ea75fd565293649899d5bdc47041366271890e526` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent engine audit — 2026-10-28 11:28 UTC”. Its original opening states: “Pinned main at start: 581f343e460ea21ef645ece1a7f6205a4ed2fb31; mod_version=0.0.1-alpha.29-dev.52-v8recover.1. Builder's completed engine source checkpoint: 96aad1eb. Latest committed renderer implementation: eeeb3d71. Pinned main also includes later documentation-only renderer review. This audit never modified runtime/Builder/renderer code or any active checkout.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `ChunkAccess`, `DeepPrimal_Audit_20261028T1128_581f343_SourceEvidence.zip`, `JsonOps`, `NbtOps`.

## Research expansion — gaps and details to deepen

- Carry the water finding through the real native callback boundary while preserving quantity, constituent identity, and exactly-once ownership.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Bind write scopes to world, level, immutable cell coordinates, expected state, and revision; unrelated callbacks must retain their own invalidation and accounting.
- Recheck final state after synchronous callbacks before committing water/salt/sediment ledgers; define bounded compensation and explicit failure when restoration is impossible.

## Research expansion — acceptance checks

- Exercise refused-but-applied and accepted-but-replaced writes, same-volume chemistry changes, unrelated-cell mutation, cross-level callbacks, and unload during commit.
- Compare joint loaded/unloaded/reservoir quantity and composition before and after edit, save, restart, and reopen using the actual host runtime.

## Research expansion — source anchors

- Deep Primal independent engine audit — 2026-10-28 11:28 UTC
- Role and scope
- Newly demonstrated conditional atomicity limitation

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent engine audit — 2026-10-28 11:28 UTC

**Pinned `main` at start:** [`581f343e460ea21ef645ece1a7f6205a4ed2fb31`](https://github.com/Shnewbs/Deep-Primal/commit/581f343e460ea21ef645ece1a7f6205a4ed2fb31); `mod_version=0.0.1-alpha.29-dev.52-v8recover.1`. Builder's completed engine source checkpoint: [`96aad1eb`](https://github.com/Shnewbs/Deep-Primal/commit/96aad1eb44740b53e2ecac1841b30f00448d833b). Latest committed renderer implementation: [`eeeb3d71`](https://github.com/Shnewbs/Deep-Primal/commit/eeeb3d711949f0a867b89ac4474cbad7815be532). Pinned `main` also includes later documentation-only renderer review. This audit never modified runtime/Builder/renderer code or any active checkout.

## Role and scope

Read `docs/ENGINE-WORK-STATE.json`, `docs/ENGINE-BUILD-AUDIT-HANDOFF.md`, `docs/ACTIVE-ENGINE-AUDIT.md`, `docs/ENGINE-BUILDER-AUDITOR.md`, `docs/ENGINE-HOURLY-BUILDS.md`, the accepted [ten-category rubric](../../engine-review-2026-10-24/REVIEW.md), the [10:30 UTC independent audit](../2026-10-28T1030-10cb0c6/REVIEW.md), Dev52's [work evidence](../../validation/engine-continuation/v8-postwrite-recovery-2026-10-28/README.md) and the [renderer near-ring review](../../renderer-jams/20261028-near-ring-fast-reject/REVIEW.md). Pinned work state reports Builder cycle completed/source-only, `lease_released=true` at 11:08:54 UTC; renderer near-ring claim completed/released. No active timed measurement was used. All auditor compilation, test classes and intermediate output stayed under an independent `/mnt/data/deepprimal-audit-20261028T1128-581f343/` directory. This run used exact committed production source reconstructed from retained previous audit evidence, matched via Git blob SHA to the pinned GitHub files; it was **not** a complete Gradle checkout.

## Actual committed changes observed since last audit

- [Dev52 V8 surface materialization and undo](https://github.com/Shnewbs/Deep-Primal/blob/581f343e460ea21ef645ece1a7f6205a4ed2fb31/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L94-L156) journals original block states per cell and restores in reverse order on synchronous postwrite setter, overlay or heightmap errors; [bounded undo implementation](https://github.com/Shnewbs/Deep-Primal/blob/581f343e460ea21ef645ece1a7f6205a4ed2fb31/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L219-L289) retains at most 256 × 2048 entries. Git blob **`1399606ac7067a19dbf7e4529821135c161de7fa`** independently matched. V8 remains opt-in/unregistered.
- [LOD nearest-ring fast reject](https://github.com/Shnewbs/Deep-Primal/blob/581f343e460ea21ef645ece1a7f6205a4ed2fb31/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L262-L284), Git blob **`3c2530804b28c74b08d492f7baf343d6517a717b`** independently matched: excludes strictly farther ring candidates before ranking; equal-ring tie candidates still compare. Renderer owner reported 6.73%–7.69% lower scanner allocation in its isolated benchmark, but mixed CPU median and no FPS/GPU acceptance. Not independently benchmarked in this audit.
- The auditor changed **zero** production source, feature switches, branches, cache, Builder state or scheduled tasks.

## Fresh source-level execution (not native Minecraft)

Isolated Linux OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. Exact committed production Git blobs were compiled against clearly identified isolated Minecraft/Mojang stand-ins, not real NeoForge/Mojang classes. All compiled tests below passed:

| Focus | Actual passing assertions |
| --- | ---: |
| Existing V8 saved profile and strict root envelope | 609,293 |
| Existing V8 base height and planned ground | 2,110,056 |
| Existing V8 native section bridge API-double test | 11,047,019 |
| **New independent Dev52 recovery/side-effect test** | **16,405** |
| Seven LOD scanner/index/retry/filename tests | 38,740 |
| **Total fresh passing assertions** | **13,821,513** |

Independent Dev52 recovery test confirms direct postwrite exceptions at setter calls **1** and **1,000** restore each of 8,192 old cells and derived maps in the test double. A late snapshot-read failure also backtracks earlier direct writes. An injected fault *during rollback* preserves the original exception, attaches one suppressed recovery exception and leaves one cell modified. The previous Dev51 production source **fails** the new positive rollback regression as an expected negative control.

### Newly demonstrated conditional atomicity limitation

A setter callback can mutate a **future** cell before V8 snapshots it. In the new API-double test, callback mutation of cell `(1,-4,0)` during the first raw write, followed by a direct failure at write 1,000, leaves **one callback-modified cell** after recovery. The original state was overwritten before the first visit/snapshot of that cell; the journal therefore records the already-mutated value. This is an actual reproducible *Java-double* failure of whole-chunk atomicity, not proof that the same callback occurs in native Minecraft. Dev52's own documentation explicitly excludes arbitrary native callback/lighting/I/O effects. **Native gate:** verify whether target `ChunkAccess` setters have such side effects and require caller discard/regenerate on failed write or rollback; never treat this as a complete transaction. The bounded undo arrays also introduce per-chunk memory/read/write overhead that is **not benchmark accepted**.

Seven exact-version LOD suites passed, including signed extreme coordinates and 1,856 real filesystem filename cases against an independent ranking oracle. Some asynchronous suite assertion counts varied with the scan timing; no failing final result or measured renderer frame improvement is claimed.

## Ten equally weighted category scores — historical baseline **carried**, NOT freshly regraded

| Category | /10 | Pinned rationale and open acceptance |
| --- | ---: | --- |
| Terrain realism/variety | **5** | [Experimental V8 is explicit-call-only](https://github.com/Shnewbs/Deep-Primal/blob/581f343e460ea21ef645ece1a7f6205a4ed2fb31/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L16-L18); no native registered-worldgen evidence; frozen V1–V7 retained |
| Biome coherence/blending | **4** | [Accepted review](../../engine-review-2026-10-24/REVIEW.md): thresholded palettes; no weighted ecotone native evidence |
| Hydrology/caves | **6** | Historical basin/finite-water features; no new native volume/salt/sediment unload/reopen or waterfall acceptance |
| Structures/features | **4** | Historical tree/deposit placement; native spacing/exclusion/structure stage remains unproven |
| Performance/threading | **3** | Accepted native chunk p95 exceeds target; undo-journal costs and current CPU/GPU frame tails not measured |
| Determinism/stability | **7** | Prior frozen saves/digests; new V8 code remains API-double and native callback/lighting/reopen open |
| Architecture/code quality | **6** | Module separation and explicit experimental adapter, but generation orchestration remains coupled |
| Extensibility | **4** | Snapshot/profile work not yet registered as a full feature/config/worldgen lifecycle |
| Tests/benchmarks/docs | **7** | 13,821,513 actual focused assertions here; Java25, full native/owner hardware, long soak still open |
| Polish/config/tooling | **4** | No new native UI, real GPU pixels, Windows/Vulkan/GL or end-user preview delivery |

**Overall = 50/100, 5.0/10, Tier 2 (carried from October 24 accepted review).** Not a fresh complete engine regrade; staged or unused prototypes earn no gameplay credit. The owner threshold is a **fresh complete score strictly greater than 8.5/10**; still unmet.

## Build, artifact and next gates

The **October 27 owner override** assigns full Java25/Gradle/Minecraft/GPU builds and JAR publication to the owner; the earlier mandatory auditor JAR wording is superseded. JDK25 and its owner-provided archive are absent here. Therefore **no Java25 execution, Gradle/NeoForge full build, native save/reopen/shutdown, source/compiled/JAR-class or nested archive CRC/payload verification, Windows/AMD8060S Vulkan/OpenGL, live waterfall pixels/frame-time, benchmark under builder reservation, fresh qualified development JAR, GitHub Release, CurseForge upload, or JAR retention deletion occurred**. No old JAR was relabeled. A Linux Java21 source-test pass is **not** Windows-native acceptance.

Independent evidence ZIP: `DeepPrimal_Audit_20261028T1128_581f343_SourceEvidence.zip`, **92,341 bytes**, SHA256 **`7b5f4a420f2c6cb31d095d91e00bd837904c105873730825d333a8d03588af2f`**, **70 entries**, all ZIP CRC and included manifest SHA256 checks pass. Library path: `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1128_581f343_SourceEvidence.zip`. These are evidence-ZIP checks, not JAR package validation.

**Next priorities:** native V8 caller-abandonment/rollback with real callbacks/lighting, `JsonOps`/`NbtOps` and saved-profile/world reopen with frozen V1–V7 replay; water mass and constituent conservation across unload/edit/restart; and owner GPU Vulkan/GL water/LOD frame p50/p95/p99 with memory/VRAM measurements. Independent audit is observational and not a Builder stop gate.


<!-- END PRESERVED SOURCE -->
