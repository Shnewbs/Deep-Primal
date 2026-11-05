# Expanded replica — docs/validation/engine-continuation/v8-materialize-preflight-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-materialize-preflight-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-materialize-preflight-2026-10-28/README.md |
| Preserved source SHA-256 | `f54f67105d90ab54ebc0e9358ba386498c0ab9f6c4e236b8bc358272062e320a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev51 precomputing V8 density and resolving every surface and subsurface material before block or heightmap mutation, then priming final heightmaps once. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that API-double zero-write failure checks do not establish native callback, lighting, codec or save/reopen behavior. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a complete material-resolution preflight with immutable planned occupancy and a defined boundary between validation failure and commit failure. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject late-column material lookup failures and assert zero writes, compare final material and heightmap parity, then repeat actual native callbacks before exposing the opt-in path. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev51 — V8 materialization preflight and heightmap safety

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev51 — V8 materialization preflight and heightmap safety

Independent audit consumed: docs/engine-hourly-audits/2026-10-28T0933-4b61329/REVIEW.md, pinned 4b6132969ab517bd581b044308a30a62e95fb411. **Historical ten-category grade carried 50/100 (5.0/10), not regraded.** Main code version 0.0.1-alpha.29-dev.51-v8preflight.1.

## Actual engine change

In src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java, the explicitly invoked V8 planned-ground path now completes exact density sampling and resolves every required ground surface channel/block state and subsurface dirt state **before the first block/heightmap write**. Invalid surface material lookup can fail without half-painting a chunk. It then writes precomputed occupancy without resampling and skips redundant interim unprimed heightmap updates, priming both final WORLD_SURFACE_WG and OCEAN_FLOOR_WG maps after painting. The raw planned path keeps its existing heightmap behavior. This is **preflight safety**, not transactional rollback for errors after native writes begin. No V8 generator registration, existing V1-V7 geography, owner/player edits, finite water accounting, approved art, graphics or LOD changes.

Production tested blob: 58c4b7d0f04435620858c1eef919d50ec171f15f; previous blob 561676a414261f67cc39dc8f22e254a5181ab18e.

## Validation

**Java 21.0.12.1** with javac --release 21 -Xlint:all -Werror and java -ea -Xverify:all. Actual changed production source compiled against isolated API-compatible Minecraft/Mojang stand-ins. New V8MaterialPreflightChecks: **209,429 assertions**, **49,152 exact voxel material parity comparisons** across three scenarios, three injected late-column failed ground resolves with **zero block/heightmap mutations**, and three final-map parity / no interim heightmap update cases. Five existing V8 regression suites: 14,392 + 594,901 + 874,642 + 1,235,414 + 11,047,019 = **13,766,368** assertions. Total **13,975,797 passed**. Original dev50 adapter fails new negative on partially written chunks; corrected blob passes.

Conversation evidence ZIP: DeepPrimal_Dev51_V8_MaterialPreflight_SourceEvidence.zip, **59,039 bytes**, SHA256 89af56c15c9a5b4344b8f0768b74496712ea2b116c394f0d5f512e4ed22d98d3; archive CRC checked. Contains actual production files, stand-ins, runnable focused test and failure log. The JDK25 owner archive was not mounted locally; Java25 not run.

No full Gradle/Java25/NeoForge build, native world save/reopen, actual Mojang JsonOps/NbtOps or callbacks, Windows AMD8060S Vulkan/OpenGL frame tests, real waterfall continuity, water loaded/unloaded conservation, qualified JAR, GitHub release or fresh complete regrade. The October 27 owner override assigns full builds to the owner. Next: owner-native V8 codec/chunk/heightmap/lighting test and V1-V7 replay; water durable transfer/persistence and GPU continuous-water gates remain open.

<!-- END PRESERVED SOURCE -->
