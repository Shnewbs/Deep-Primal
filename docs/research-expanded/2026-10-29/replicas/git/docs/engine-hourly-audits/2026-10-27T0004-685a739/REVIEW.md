# Expanded replica — docs/engine-hourly-audits/2026-10-27T0004-685a739/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T0004-685a739/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T0004-685a739/REVIEW.md |
| Preserved source SHA-256 | `ea59911e4ba215072cf6aff6d277c7391b5f01953215c10bc00563159e4d8751` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — 685a739”. Its original opening states: “Exact pinned source: 685a7390a8371c9b423594888a5a8e2aad744271. Production code/build inputs are unchanged from the preceding qualified audit at c5abe457c4c7b36a11307128486c240e0b40d508 (previous report). This is a fresh uniquely versioned build, not a re-upload of its binary. Required current owner policies, work state, handoff, active audit and original ten-category rubric were read; historical pause/threshold notices remain superseded.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent engine audit — 685a739
- Isolation, limits and priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — 685a739

Exact pinned source: `685a7390a8371c9b423594888a5a8e2aad744271`. Production code/build inputs are unchanged from the preceding qualified audit at `c5abe457c4c7b36a11307128486c240e0b40d508` ([previous report](https://github.com/Shnewbs/Deep-Primal/blob/e0bcf7c6f85ab84b5d4dec359a5b6d814bc2a88e/docs/engine-hourly-audits/2026-10-26T2333-c5abe45/REVIEW.md)). This is a fresh uniquely versioned build, not a re-upload of its binary. Required current owner policies, work state, handoff, active audit and original ten-category rubric were read; historical pause/threshold notices remain superseded.

**50/100 · average 5.0/10 · Tier 2/5. All ten scores carried unchanged, not a fresh complete regrade.** Current functional checks add scoped evidence; they do not establish broad visual/gameplay/performance acceptance. The strictly-greater-than-8.5 target remains unmet.

| Category | /10 | Pinned evidence and remaining gap |
|---|---:|---|
| Terrain realism and variety | 5 | [Landforms.java L12](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) Heightfield and special tunnels; general registered V8 density and broad visual acceptance remain open. |
| Biome blending and coherence | 4 | [Landscape.java L76](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) Threshold climate categories; weighted surface/vegetation ecotones remain unaccepted. |
| Hydrology and cave quality | 6 | [HydrologyRuntime.java L442](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) Finite sources and accepted dev31 admission/overflow repair; production still scans/copies/sorts its index. Single-owner loaded/unloaded ledger and full cave families remain open. |
| Feature/structure placement | 4 | [EngineChunkGenerator.java L115](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115) Deterministic vegetation/deposits; no general spacing/exclusion/structure pipeline. |
| Performance and threading | 3 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) No new latency benchmark; older component improvements do not establish whole load-ready/tick tails or 5ms target. |
| Determinism and stability | 7 | [HydrologyRuntime.java L260](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260) Versioned profiles/replay protections; no cross-machine bit identity or sustained travel/multiplayer soak. |
| Architecture and code quality | 6 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) Core/module boundaries enforced; adapter still combines revision/material/water/decoration responsibilities. |
| Extensibility and data-driven design | 4 | [TerrainProfile.java L54](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) Saved options and deposit definitions; no complete third-party generation-stage contract or integrated graph profiles. |
| Testing, benchmarks, documentation | 7 | [LodScratchChecks.java L12](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12) Fresh available build/package/native gates recorded individually; missing Windows/AMD, cross-architecture, long-soak and visual/performance matrix. |
| Polish: config, errors, tooling | 4 | [server_smoke.py L17](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/tools/server_smoke.py#L17) Guarded dev packaging and strict shutdown checks; terrain authoring/preview workflow and owner-hardware presentation acceptance remain open. |

## Committed behavior and observed work

No new production changes since the last audit. The already committed LOD byte-query optimization preserves correct totals for all details and mutable/sequential lists; Themes hidden/temporary packaging exclusions preserve normal resources; pinned tool restoration supports bounded verified cache recovery. These were implemented previously, not by this auditor. No new gameplay feature or allocation/frame/FPS improvement is credited in this run.

At the pin the builder holds its own active lease for staged composition-only diffusion, while the renderer claims a private frame-window candidate with no production files and no exclusive benchmark. Released builder measurement and renderer reservations were checked before heavy work and again before native permission. Neither role was interrupted or edited. Staged diffusion, hydrology index/cursor and R57/R60/R61/R62 candidates remain excluded from this JAR.

The production tick still performs removeIf, a full index copy and sort before the first source deadline test: [HydrologyRuntime L447–450](https://github.com/Shnewbs/Deep-Primal/blob/685a7390a8371c9b423594888a5a8e2aad744271/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L447-L450). The per-column guard therefore does not prove a hard 2 ms tick bound. The regular poll/native lookup/reinsert sequence atL461–467 also lacks the staged exact-cursor ownership guards. This is fresh source inspection, not a new live callback/latency experiment; retained source excerpt is hydrology-budget-source.txt.

## Fresh build and qualification

Build receipt: {"pass": true, "exit": 0, "source_sha": "685a7390a8371c9b423594888a5a8e2aad744271", "elapsed_seconds": 318.7850252569997, "fresh_product_compilation": true, "private_agent_verified": true}. Combined gates: {"pass": true, "source_sha": "685a7390a8371c9b423594888a5a8e2aad744271", "full_build": {"pass": true, "exit": 0, "source_sha": "685a7390a8371c9b423594888a5a8e2aad744271", "elapsed_seconds": 318.7850252569997, "fresh_product_compilation": true, "private_agent_verified": true}, "independent_core": {"pass": true, "production_sources": 75, "test_sources": 64, "exit": 0, "log_sha256": "031f697cde831638c8788a97b675edb6cc04876717ab2e14ede423079c32643d"}, "native_sessions": 10, "initial_jna_failure_excluded_and_repaired": true, "archive_package_extra_gates_pass": true}. Java 25.0.4.1+1 / Gradle 9.2.1, Linux x86_64 AMD EPYC 9V74 host, 9 eligible CPUs and 8 GiB cgroup. One-worker forced full build, fresh independent compilation and native matrix; functional duration is not a latency benchmark. All 864 tracked build inputs were freshly rehashed against pinned Git blobs, with exact non-documentation tree/blob identities proving the retained manifest also belongs to this pin.

[Download newly qualified development JAR](https://chatgpt.com/library?file_id=libfile_011430e0d6708191b062465d12cf23eb&account_id=personal). `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261027T0004-685a739-vulkan-windows.jar` · **125296757 bytes** · SHA256 `f9c0b022b984d1caecf40d70ef67cc9e710601e06548396e6bbb0f0c25f898fe`. Windows Vulkan AMD64 bundle with OpenGL fallback. Development testing only; no stable/GitHub Release/CurseForge publication by auditor.

Passed all 35 executed build tasks; fresh independent 75 production + 64 fixture Java sources/full core suite; seven recursive outer/nested CRC/no-duplicate/hidden-entry checks; 592 product/package and 146 independent core class comparisons; 224 private fixture exclusions. Complete 2508 Themes resource hashes, 43 palettes, 11 approved ground images, 148 generated soil resources and module/Vulkan notices. Two AMD64 Windows DLLs and no Linux/macOS Vulkan payload; authoring tools remain excluded.

Ten actual packaged Linux sessions pass: precipitation fresh/reopen; default saved channel2 and opt-in channel5 fresh/reopen with the creation toggle disabled before opt-in reopen; aquifer fresh/reopen/verify across real restarts; receiver constituent capacity. Explicit markers, loaded identities, acknowledged save-all flush, failure precedence, clean shutdown and exit 0 required. Every session binds the root and all six nested modules to the exported bundle. Four profile sessions each pass 1,445 actual-loaded allowance oracle cases. Final producer/export bytes are verified after completion, consumers and saving.

Direct/packaged LOD byte/scratch/reduction/discovery/bounds/timing and package cache/core-close/inline-dispatch/waterfall checks pass. Python shutdown/state/RCON normal/-O/-OO checks and 33 restoration-helper regression executions pass. Fresh packaged independent byte-oracle receipt, if present: {"pass": true, "cases": 7602, "actual_extracted_core_first_on_classpath": true, "core_sha256": "d47d2894caf55206b9ca6f133571033e526dc50a3a5f1edf876d2952d1bc77d3", "no_allocation_or_latency_measurement": true}. Prior allocation measurements and ownership callback probes are historical and were not rerun as those experiments.

Newer committed stage evidence was inspected at b980276d1c4b9b232fd109ea615af387613becc0. The builder reports 101,024 diffusion pair cases, 10,000 equilibrium repeats and 200,000 mixed-network steps with 1,575 snapshot reconstructions. The renderer reports 770 oracle windows, 10,000 intervals and a 141-source staged core suite for context-reset frame telemetry. These are author results, not independently rerun here; neither candidate is production-integrated, bundled or native/GPU qualified. Snapshot reconstruction is not a Minecraft restart. Both remain excluded and earn no gameplay feature credit. See newer-staged-evidence.json.

## Isolation, limits and priorities

Initial native precipitation was rejected despite its explicit water marker and Gradle exit 0 because JNA failed native extraction in the full system temporary location. The failed world/logs remain separate. An actual JNA native-dispatch probe passed after assigning an explicit private jna.tmpdir; server JVM properties were corrected and the complete final native matrix used a fresh replacement precipitation world. Failure precedence was not relaxed.

Completed auditor-only memory-backed checkout/cache/tool resources were reused without concurrent access. This run has its own version, receipt, evidence, temporary output and fresh native worlds. Full build forces recompilation and fresh independent outputs; prior binaries are never relabeled. Native launches exclude the already completed outer producer task. Source remains pinned despite later main activity. Tools/agent identity are verified from retained approved artifacts; no fresh restoration was needed. No builder/renderer checkout, cache, output or lease was touched.

Not run: Windows/AMD8060S execution, live OpenGL/Vulkan scene, fresh controlled allocation/latency benchmarking, cross-architecture identity, sustained multiplayer/travel/world-size soak or complete morphology/content/visual/gameplay regrade. Linux native success is not Windows acceptance. Historical 14.988/14.452ms warm-fill measurements are not new results; 5 ms acceptance remains open. Staged/unused libraries earn no gameplay credit.

Next: fully qualify combined staged hydrology and composition candidates before promotion; establish single-owner loaded/unloaded conservation and bounded catch-up/work; registered V8 saved profiles/topology, weighted ecotones, general feature/structure ownership and actual owner-hardware water/LOD/performance acceptance.

Retention receipt records a successful new save followed by keeping only the three newest user-facing Deep Primal JARs across roles, sorted by actual creation time, and trashing older stable IDs. Only completed auditor-owned old exports are eligible for local cleanup. Source/evidence archives, approved art, tool/dependency caches, unrelated files and all active producers remain protected. Local cleanup is partially unresolved: three old own export/evidence copies were unlinked after hash verification, but one rejected historical evidence JAR reappeared on a later command. Other two export paths remained absent. The saved-JAR limit is enforced; no durable removal or reclaimed disk space is claimed for the reappearing local copy. See local-retention.json for its exact path.

Publication parent/late-source receipt: {"source_sha": "685a7390a8371c9b423594888a5a8e2aad744271", "latest_parent": "b980276d1c4b9b232fd109ea615af387613becc0", "tree_sha": "b980276d1c4b9b232fd109ea615af387613becc0", "newer_non_documentation_changes": [], "newer_staged_evidence": "b980276d1c4b9b232fd109ea615af387613becc0", "allowed_path": "docs/engine-hourly-audits/2026-10-27T0004-685a739/", "guarded_update_required": true, "no_builder_state_overwritten": true, "report_only": true}. The JAR remains pinned to `685a7390a8371c9b423594888a5a8e2aad744271`; newer source changes listed there are not included. Guarded publication adds only this unique audit report directory and preserves unrelated builder work. This is an observational audit, not a builder stop gate.


<!-- END PRESERVED SOURCE -->
