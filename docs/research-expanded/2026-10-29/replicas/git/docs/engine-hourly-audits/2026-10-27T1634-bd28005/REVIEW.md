# Expanded replica — docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md |
| Preserved source SHA-256 | `5a3f3504884c3574b25b69dffa269165df2ef2c1450d40f9f2e46299553ab832` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent Deep Primal audit — 2026-10-27 16:34Z”. Its original opening states: “Pinned engine source: bd28005d7c8703c7abdd822634cb4a07875d2038. Audit role was read-only against source and did not touch the Builder or its lease. Latest independent full grade remains 50/100 (5.0/10, Tier 2/5); scores are CARRIED, not a fresh complete regrade. Strictly greater than 8.5 not met. No change to the engine rubric.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent Deep Primal audit — 2026-10-27 16:34Z

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) |
| `../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) |
| `../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) |
| `../../../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96) |
| `../../../src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L438` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L438) |
| `../../../src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) |
| `../../validation/upscaling-2026-10-27/package.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/upscaling-2026-10-27/package.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent Deep Primal audit — 2026-10-27 16:34Z

Pinned engine source: `bd28005d7c8703c7abdd822634cb4a07875d2038`. Audit role was read-only against source and did not touch the Builder or its lease. Latest independent full grade remains **50/100 (5.0/10, Tier 2/5)**; scores are **CARRIED**, not a fresh complete regrade. Strictly greater than 8.5 not met. No change to the engine rubric.

| Category | Score |
|---|---:|
| Terrain realism and variety | 5 |
| Biome blending and coherence | 4 |
| Hydrology and cave quality | 6 |
| Feature/structure placement | 4 |
| Performance and threading | 3 |
| Determinism and stability | 7 |
| Architecture and code quality | 6 |
| Extensibility and data-driven design | 4 |
| Testing, benchmarks and documentation | 7 |
| Polish: config, errors and tooling | 4 |
| **Total** | **50/100** |

Source references: [original rubric](../../engine-review-2026-10-24/REVIEW.md), [previous complete audit](../2026-10-27T0430-bbf07e1/REVIEW.md), [current source package](../../validation/upscaling-2026-10-27/package.json); [Landforms](../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12), [Landscape](../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76), [HydrologyRuntime](../../../src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L438), [EngineChunkGenerator](../../../src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96), [WorldGenPipeline](../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22), [TerrainProfile](../../../src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54). No registered fully accepted V8/weighted ecotones/general structure pipeline; no whole-game 5ms perf, cross-architecture/multiplayer/owner GPU acceptance. Finite loaded/unloaded ledgers and connected cliff GPU visuals remain open.

**Fresh independent evidence:** Materialized *existing* development JAR `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.32-upscaling.1-vulkan-windows.jar` from Library, size 125383020, SHA256 `04a7a8bf6f88c9b69e3b6cba2c24e6030e1d5e8c38192ee14b8c0e8e16a6d083`; matches [author package report](../../validation/upscaling-2026-10-27/package.json). Independent recursive CRCs for root+six nested JARs all pass; counts root676 / nested157,77,31,24,2873,4310; no duplicate entries/zip-slip/root fixture paths; selected433 project class headers all Java25/69; real FSR 1 EASU/RCAS shader payload and vendor license present; Windows shaderc/VMA DLLs present; NVIDIA NGX runtime absent. Existing author software OpenGL/Vulkan results were **not rerun**. Source/compiled/package identity, Windows, owner AMD8060S, independent GPU frames and native saves are **unrun**, not waived.

**Requested NEW audit JAR blocked:** isolated host has Java21 only, no complete private Git checkout/Gradle wrapper, and `git ls-remote` fails DNS on github.com. `./gradlew :build :visualValidationClasses -PvulkanPlatform=windows` could not start. **No new JAR produced or relabeled.** Builder's existing dev32 JAR remains a pre-existing production artifact. No JAR deletion (retention newest three already observed). No public upload/release. Full evidence+repro script uploaded to Library: `/Deep Primal/DeepPrimal_Independent_Audit_20261027T1634_bd28005_Evidence.zip`, library ID `libfile_3922ba178c14819192b0dae664ff9585`. Detailed report Library `libfile_c7adb99f314c819192d2559d922aa913`.

**Next:** restore pinned Java25/full private source via approved tooling, produce a uniquely named actual audit build, run full Gradle/native/save/reopen/visual checks, then audit production water contact/index bounds and owner GPU/performance. No builder interruption or state edits.


<!-- END PRESERVED SOURCE -->
