# Expanded replica — DeepPrimal_Audit_20261027T1734_83c099f_REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_779676fa0d78819193c7f51336e3937a&account_id=personal) |
| Source identity | library: DeepPrimal_Audit_20261027T1734_83c099f_REVIEW.md |
| Preserved source SHA-256 | `ffcf12f8b05c8e37f7c7cd266b125f87061dc10a14c7d426026515b23de607ec` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../README.md) · [Integrated design](../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../chapters/validation-evidence.md) · [Discrepancy register](../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent audit — 2026-10-27 17:34Z”. Its original opening states: “Pinned main: 83c099fd455c0154f511e2d2f67f8582822a83fa (2026-10-27 17:32:06Z). Source version: 0.0.1-alpha.29-dev.33-loadedreads.1. This is an observational audit; no Builder checkout/lease/process/output was touched, no production source was changed, no staged R57/R60/R61/R62 overlay was promoted. Local independent audit receipt only. Live cross-chat process state not independently checked; the pinned recorded builder lease is released.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `WaterRuntime.units`.

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

- Deep Primal independent audit — 2026-10-27 17:34Z
- Fresh evidence (independent, limited)
- Build and delivery result

Related design: [terrain water](../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent audit — 2026-10-27 17:34Z

**Pinned `main`:** [`83c099fd455c0154f511e2d2f67f8582822a83fa`](https://github.com/Shnewbs/Deep-Primal/commit/83c099fd455c0154f511e2d2f67f8582822a83fa) (2026-10-27 17:32:06Z). **Source version:** `0.0.1-alpha.29-dev.33-loadedreads.1`. This is an observational audit; no Builder checkout/lease/process/output was touched, no production source was changed, no staged R57/R60/R61/R62 overlay was promoted. Local independent audit receipt only. Live cross-chat process state not independently checked; the pinned recorded builder lease is released.

**50/100, average 5.0/10, Tier 2/5 — scores CARRIED from the original full rubric and most recent completed audit, NOT a fresh full regrade.** The target strictly above 8.5/10 is not attained or evidenced; water acceptance is incomplete.

| Equally weighted category | /10 | Evidence and remaining gap |
|---|---:|---|
| Terrain realism and variety | 5 | [Landforms L12–28](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12-L28); no accepted general registered V8 3D density/geography. |
| Biome blending and coherence | 4 | [Landscape L76–97](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76-L97); weighted vegetation/material ecotones unaccepted. |
| Hydrology and caves | 6 | [WaterRuntime L137–171](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L137-L171) now guards unloaded native reads and uses one neighbor sample. No qualified native unload/reopen/ledger, sustained constituent conservation, continuous GPU cliffs or broad cave families. |
| Features and structures | 4 | [EngineChunkGenerator L96–129](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96-L129); general spacing/exclusion/structure ownership absent. |
| Performance and threading | 3 | Original [strict review](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/engine-review-2026-10-24/REVIEW.md) warm fill ~14–15 ms vs requested 5 ms; no current matched native p50/p95/p99/tail or owner GPU measurements. |
| Determinism and stability | 7 | Prior review legacy native V1–V7 profiles/chunk replay; new dev33 native save/reopen is unrun. |
| Architecture and code quality | 6 | Separate `engine-core`, Graphics, Performance, LOD, Vulkan; current Java25 Gradle core/module boundaries unrun. |
| Extensibility/data-driven | 4 | [TerrainProfile L54–73](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54-L73) supports saved palette/deposits, not complete stage/graph API. |
| Testing/benchmarks/docs | 7 | Existing extensive tests and [dev33 source checks](https://github.com/Shnewbs/Deep-Primal/tree/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/validation/engine-continuation/unloaded-water-reads-2026-10-27); no current full native, Windows/AMD or sustained multiplayer evidence. |
| Polish/config/tooling | 4 | Guarded configuration and package notices; authoring/tooling and owner hardware acceptance incomplete. |

## Fresh evidence (independent, limited)

- Re-read pinned `main`, work state, hourly builds, Builder/Auditor policy, handoff, active audit, original rubric, and [latest completed report](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md). The most recent report **also carried** 50/100. Confirmed actual dev33 production diff in commit [`ad3df4f`](https://github.com/Shnewbs/Deep-Primal/commit/ad3df4f6ec7ad54aff342a76bb3b632c61a80fa6), not a complete Java25/native release.
- **PASS:** independently compiled manually transcribed [production five-line `WaterRuntime.units` method L138–142](https://github.com/Shnewbs/Deep-Primal/blob/83c099fd455c0154f511e2d2f67f8582822a83fa/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L138-L142) against instrumented Java21 API fakes, `javac --release 21`, `java -Xverify:all`. The **136-case** matrix covers world bounds, chunk loaded/unloaded, water/dry state and quantities 0..16. No out-of-world/unloaded block reads; gate evaluation order matches source. **Not** an automatic source-extraction test or real Minecraft/NeoForge proof.
- **PASS, but HISTORICAL binary only:** fresh outer and all six nested archive CRC checks against *preexisting* `alpha.29-dev.32-upscaling.1` Windows Vulkan JAR, 125,383,020 bytes, SHA256 `04a7a8bf6f88c9b69e3b6cba2c24e6030e1d5e8c38192ee14b8c0e8e16a6d083`. This is NOT a current dev33 package, and its packaged class identity cannot support dev33 implementation claims.

## Build and delivery result

**No new qualified JAR.** Available host only has OpenJDK 21.0.12.1, no Java25, complete pinned private repository checkout, Gradle wrapper/dependencies. `git ls-remote https://github.com/Shnewbs/Deep-Primal.git` failed DNS (`Could not resolve host: github.com`); from isolated output, `./gradlew --no-daemon --no-build-cache --rerun-tasks :build :visualValidationClasses -PvulkanPlatform=windows` failed with exit 127, missing file. The GitHub connector provided authoritative pinned document/code reads but did not restore a full executable checkout. Full Java25, source/compiled/package class identity, native save/reopen/shutdown, Windows/AMD8060S, actual Vulkan/OpenGL pixels and long-duration frame/tick tail gates **NOT RUN**. No old JAR relabeled or uploaded, no GitHub Release/CurseForge. Without a newly saved qualified JAR, **no three-newest-JAR retention deletion** occurred.

**Next:** restore complete current source with pinned tooling/Java25, run full build/boundaries and native water unloaded-border and constituent/save/reopen behavior. Then actual graphical cliff/waterfall and measured p50/p95/p99, plus longer multiplayer/soak. This is not a Builder pause gate.


<!-- END PRESERVED SOURCE -->
