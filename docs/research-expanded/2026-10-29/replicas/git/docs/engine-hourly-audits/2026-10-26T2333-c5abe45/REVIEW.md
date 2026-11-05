# Expanded replica — docs/engine-hourly-audits/2026-10-26T2333-c5abe45/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2333-c5abe45/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T2333-c5abe45/REVIEW.md |
| Preserved source SHA-256 | `0ddf8ca5bc5a18f53f77fd7b82941fede417464b326d6fec1568d73998759958` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — c5abe45”. Its original opening states: “Exact pinned source: c5abe457c4c7b36a11307128486c240e0b40d508. Previous audit pin: 4a8b0fb52e7473a816655096c42f089dd340e735. Required current owner policies, work state, handoff, active audit and original rubric were read. No implementation/staged promotion by auditor.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent engine audit — c5abe45
- Actual changes and fresh findings
- Isolation, limits and next priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — c5abe45

Exact pinned source: `c5abe457c4c7b36a11307128486c240e0b40d508`. Previous audit pin: `4a8b0fb52e7473a816655096c42f089dd340e735`. Required current owner policies, work state, handoff, active audit and original rubric were read. No implementation/staged promotion by auditor.

**50/100 · average 5.0/10 · Tier 2/5. All ten scores carried unchanged; not a fresh complete regrade.** Functional checks do not establish broad visual/gameplay/performance acceptance. Strictly greater than 8.5 remains unmet.

| Category | /10 | Current pinned evidence and remaining gap |
|---|---:|---|
| Terrain realism and variety | 5 | [Landforms.java L12](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) Heightfield and special tunnels; general registered V8 density and broad visual acceptance remain open. |
| Biome blending and coherence | 4 | [Landscape.java L76](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) Threshold climate categories; weighted surface/vegetation ecotones remain unaccepted. |
| Hydrology and cave quality | 6 | [HydrologyRuntime.java L442](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) Finite sources and accepted dev31 admission/overflow repair; production still scans/copies/sorts its index. Single-owner loaded/unloaded ledger and full cave families remain open. |
| Feature/structure placement | 4 | [EngineChunkGenerator.java L115](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115) Deterministic vegetation/deposits; no general spacing/exclusion/structure pipeline. |
| Performance and threading | 3 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) No new latency benchmark; older component improvements do not establish whole load-ready/tick tails or 5ms target. |
| Determinism and stability | 7 | [HydrologyRuntime.java L260](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260) Versioned profiles/replay protections; no cross-machine bit identity or sustained travel/multiplayer soak. |
| Architecture and code quality | 6 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) Core/module boundaries enforced; adapter still combines revision/material/water/decoration responsibilities. |
| Extensibility and data-driven design | 4 | [TerrainProfile.java L54](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) Saved options and deposit definitions; no complete third-party generation-stage contract or integrated graph profiles. |
| Testing, benchmarks, documentation | 7 | [LodScratchChecks.java L12](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12) Fresh available build/package/native gates recorded individually; missing Windows/AMD, cross-architecture, long-soak and visual/performance matrix. |
| Polish: config, errors, tooling | 4 | [server_smoke.py L17](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/tools/server_smoke.py#L17) Guarded dev packaging and strict shutdown checks; terrain authoring/preview workflow and owner-hardware presentation acceptance remain open. |

## Actual changes and fresh findings

[LodMesher Levels.bytes](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16) replaces its stream pipeline with indexed RandomAccess traversal and linear iterator traversal for sequential lists. All three details are still summed and mutable totals recomputed. The previous implementation already produced correct totals: this is a query-allocation optimization, not a newly demonstrated geometry/accounting repair. Mesh/GPU-byte formula, water and saved geography are unchanged. New LodByteAccountingChecks is registered in ordinary CoreChecks.

[Themes build filters](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/gui-themes/build.gradle#L36) exclude hidden/rsync-temporary inputs while preserving source/approved art. [Tool restoration](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/tools/restore_validation_toolchain.py#L63) adds verified read-only archive-cache recovery, bounded transfer and removal only after successful extraction. No hydrology staged source is in this JAR.

Fresh independent byte oracle: **7,602 cases each on current and exact prior-production implementations**, both pass. BigInteger oracle, all 8 detail masks, mutable/sequential lists, sequential indexed-get rejection and 100,000 repeated references. No negative-control failure is claimed. See lod-independent.json; actual packaged-class repeat is separately recorded. Restoration regressions passed 11 cases in each normal/-O/-OO mode (33 executions); five pinned real retained tool archives freshly size/SHA verified.

Newer renderer report at d83f3943479ab75ac73741d564496e8c4bd92610 changes documentation only. Inspected author evidence reports warmed RandomAccess allocation 560–597B/query versus 0–0.0012, full 35-task build, four packaged Linux sessions and unchanged normal Themes bytes. Those allocation results were not independently rerun here and do not prove FPS/frame/VRAM gains. The renderer six-category self-grade is separate.

## Fresh qualification

Build receipt: {"pass": true, "exit": 0, "source_sha": "c5abe457c4c7b36a11307128486c240e0b40d508", "elapsed_seconds": 334.1357464980001, "fresh_product_compilation": true, "private_agent_verified": true}. All gates: {"pass": true, "source_sha": "c5abe457c4c7b36a11307128486c240e0b40d508", "build": {"pass": true, "exit": 0, "source_sha": "c5abe457c4c7b36a11307128486c240e0b40d508", "elapsed_seconds": 334.1357464980001, "fresh_product_compilation": true, "private_agent_verified": true}, "independent_core": {"pass": true, "production_sources": 75, "test_sources": 64, "exit": 0, "log_sha256": "d1fa5fbc554497253c687b579750a1cbcad1323f6bb6aba5e339509153d474c1"}, "native_sessions": 10, "package_archive_class_checks": true, "source_and_output_storage_recovery_completed": true}. Java25.0.4.1+1 / Gradle9.2.1, Linux x86_64, one-worker private checkout/output/cache/single-use daemon; forced task reruns. Functional elapsed time is not a latency benchmark. All 864 tracked build inputs verified against complete pinned Git subtree manifests and rehashed after build/native.

[Download newly qualified development JAR](https://chatgpt.com/library?file_id=libfile_3a97efaede848191b2583b901ee7197e&account_id=personal). `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T2333-c5abe45-vulkan-windows.jar` · **125296758 bytes** · SHA256 `5b85503cff8f637696d92b152470230742960e13f611297425df42c95e54fc28`. Windows Vulkan AMD64 with OpenGL fallback; development testing only. No stable/GitHub Release/CurseForge publication.

Passed 35 executed build tasks; fresh independent 75 production + 64 fixture Java sources/full core; seven outer/nested CRC/no-duplicate/hidden-entry gates; 592 product/package and 146 independent core class matches; 224 private class exclusions. Complete 2508 Themes resource hashes, 43 palettes, 11 approved ground textures, 148 generated soils and module/Vulkan notices. Two Windows AMD64 DLLs, no Linux/macOS Vulkan payload.

Ten actual packaged Linux sessions: precipitation fresh/reopen; default channel2 and opt-in channel5 fresh/reopen with creation toggle disabled before opt-in reopen; aquifer fresh/reopen/verify across restarts; receiver capacity. Explicit markers, loaded-class identity, save-all flush acknowledgement, failure precedence, clean shutdown and exit0 required. All root+six nested modules selected from exported bundle. Four profile sessions each pass 1,445 actual-loaded allowance oracle cases. Producer/export bytes verified after producer/consumers and saving. Additional direct/packaged LOD byte/scratch/reduction/discovery/bounds/timing, cache/core-close/inline-dispatch/waterfall, resource/module and normal/-O/-OO Python smoke/state/RCON gates pass.

## Isolation, limits and next priorities

Only completed auditor-owned resources relocated to this unique private run. No builder/renderer checkout/cache/output/lease edited. Released builder measurement and nonexclusive renderer claim checked before compile and native. No exclusive timing benchmark. Disk preflight reached zero headroom; private archives were hash-preserved in independent memory resources, but later originals reappeared, so no durable reclamation is claimed. After the independent compile/suite storage failures, a direct memory-backed test working directory, caches, temporary files, evidence and native worlds were used. Workspace symlink redirection proved insufficient. The final independent suite passed. The first memory native setup failed before server launch because its assetProperties placeholder was missing; it was restored, and the immutable outer producer task was excluded during native runs. Failed receipts/logs remain separate; no failing installer was saved. All 2,555 normal Themes file entries also match the prior qualified module exactly.

Hydrology cursor/index remains staged/unpromoted. Prior 1,480 callback probes and substituted-class native diagnostics are historical, not rerun here. Full combined Gradle/module/bundle and packaged weather/travel/callback qualification plus conservative loaded/unloaded ownership remain open. Staged/unused libraries earn no gameplay credit.

Not run: Windows/AMD8060S execution, live OpenGL/Vulkan scene, independent allocation/controlled latency benchmarks, cross-architecture identity, sustained multiplayer/travel/world-size soak, complete morphology/content/visual/gameplay regrade. Linux is not Windows acceptance. Prior 14.988/14.452ms warm-fill results are historical; 5 ms target remains unaccepted.

Next: qualify combined staged hydrology; establish single-owner loaded/unloaded conservation and bounded catch-up/work; registered V8 profiles/topology, weighted ecotones, general feature/structure ownership and measured whole-workload/owner-hardware water/LOD acceptance.

After qualified new save, keep newest 3 user-facing Deep Primal JARs across roles by actual creation time; trash older stable IDs. Preserve source/evidence archives, approved art/tools/dependency caches and unrelated files. See retention and local-cleanup receipts.

Publication parent: {"source_sha": "c5abe457c4c7b36a11307128486c240e0b40d508", "latest_parent": "d83f3943479ab75ac73741d564496e8c4bd92610", "tree_sha": "d83f3943479ab75ac73741d564496e8c4bd92610", "newer_non_documentation_changes": [], "source_conflict": false, "allowed_path": "docs/engine-hourly-audits/2026-10-26T2333-c5abe45/", "guarded_update_required": true, "report_only": true, "no_builder_state_overwritten": true}. Later changes separately recorded. JAR source remains `c5abe457c4c7b36a11307128486c240e0b40d508`. Guarded latest-parent publication adds only this per-audit directory and preserves builder state; this report is observational, not a builder stop gate.


<!-- END PRESERVED SOURCE -->
