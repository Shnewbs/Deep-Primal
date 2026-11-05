# Expanded replica — docs/engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md |
| Preserved source SHA-256 | `d6b8b6a10972f4e859373f36911e98bcb4253881616bb0dafa9711b25ff9e3a1` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent Deep Primal audit — 2026-10-27 17:34Z”. Its original opening states: “Pinned committed main: 83c099fd455c0154f511e2d2f67f8582822a83fa, source version 0.0.1-alpha.29-dev.33-loadedreads.1. Read-only engine/source review; no Builder checkout or lease touched. No staged overlays promoted.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent Deep Primal audit — 2026-10-27 17:34Z

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent Deep Primal audit — 2026-10-27 17:34Z

Pinned committed main: [83c099fd455c0154f511e2d2f67f8582822a83fa](https://github.com/Shnewbs/Deep-Primal/commit/83c099fd455c0154f511e2d2f67f8582822a83fa), source version `0.0.1-alpha.29-dev.33-loadedreads.1`. Read-only engine/source review; no Builder checkout or lease touched. No staged overlays promoted.

**Outcome: 50/100, average 5.0/10, Tier 2/5, CARRIED from the original full audit — NOT a fresh complete regrade.** The target strictly greater than 8.5 remains unmet. Category scores in unchanged original rubric order: terrain 5, biome 4, hydrology/caves 6, features 4, performance 3, determinism 7, architecture 6, extensibility 4, QA/docs 7, polish 4.

Fresh limited evidence: independently compiled and ran a 136-case Java21 API-fake test of the transcribed [production water-unit method](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L137-L142), checking bounds/chunk admission/read order for 0..16 units. All pass. This is not native NeoForge/gameplay acceptance. Rechecked CRC/SHA of the EXISTING older dev32 JAR, 125383020 bytes and SHA256 `04a7a8bf6f88c9b69e3b6cba2c24e6030e1d5e8c38192ee14b8c0e8e16a6d083`: root+six nested archives pass. It is **not** the requested new dev33 build.

**New JAR blocked:** GitHub clone in isolated container fails DNS; Java21 only, no pinned full checkout, Java25, Gradle wrapper/cache. Attempt to run `./gradlew :build :visualValidationClasses -PvulkanPlatform=windows` failed (127; no wrapper). Current native, save/reopen/shutdown, Java25/module, compiled/source identities, real Vulkan/OpenGL pixels, Windows AMD8060S and performance NOT RUN. No new qualified JAR or release, no relabeling, and no retention deletions.

Frozen source baseline and original rubric: [REVIEW](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/engine-review-2026-10-24/REVIEW.md). Last completed external [audit](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md) also carried 50/100. Current [state](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/ENGINE-WORK-STATE.json) records native border-unload, owner GPU and long-soak as open. No independent whole-engine grade increase.

Complete report: [Library report](https://chatgpt.com/library?file_id=libfile_779676fa0d78819193c7f51336e3937a&account_id=personal) · [compact evidence](https://chatgpt.com/library?file_id=libfile_dcca2fc2814c8191a06a0b8a5d376f8c&account_id=personal) SHA256 `eb5e9fcf3564b8fb1049dd5c2f938ca3405348ab75357ab673f6b70269e27ee3`.

Next: restore pinned source+Java25, then full build/package/live unload/reopen conservation, water cliff GPU temporal continuity, multiplayer/soak and p50/p95/p99. Auditor does not pause the Builder.

<!-- END PRESERVED SOURCE -->
