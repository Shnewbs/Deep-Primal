# Expanded replica — docs/engine-hourly-audits/2026-10-26T2232-4a8b0fb/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2232-4a8b0fb/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T2232-4a8b0fb/REVIEW.md |
| Preserved source SHA-256 | `79a497cfccff26ec5802d6397185c4bdae1dd33838f8f4b80103ace34338464c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent pinned engine audit — 4a8b0fb”. Its original opening states: “Exact source SHA: 4a8b0fb52e7473a816655096c42f089dd340e735. Production remains accepted dev31 at a1583a5; complete comparison from preceding audit pina849187 contains 3 commits and no non-documentation source changes. All 862 retained non-documentation inputs were freshly rehashed against the verified Git manifest and complete delta. Required owner policy/work state/handoff/active audit/original rubric were read. Historical pauses do not override current owner authorization. No product implementation or staged promotion by auditor.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent pinned engine audit — 4a8b0fb
- Fresh ownership findings and inspected diagnostics
- Restoration, isolation and limits

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent pinned engine audit — 4a8b0fb

Exact source SHA: `4a8b0fb52e7473a816655096c42f089dd340e735`. Production remains accepted dev31 at a1583a5; complete comparison from preceding audit pina849187 contains 3 commits and no non-documentation source changes. All 862 retained non-documentation inputs were freshly rehashed against the verified Git manifest and complete delta. Required owner policy/work state/handoff/active audit/original rubric were read. Historical pauses do not override current owner authorization. No product implementation or staged promotion by auditor.

**50/100, average5.0/10, Tier2/5 — all ten scores carried unchanged. Not a fresh complete regrade.** Functional qualification does not establish broad morphology, gameplay, visuals, performance or cross-platform acceptance. Strictly greater than8.5 remains unmet.

| Category | /10 | Pinned evidence / remaining gap |
| --- | ---: | --- |
| Terrain realism and variety | 5 | [Landforms.java L12](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) Heightfield and special tunnels; general registered V8 density and broad visual acceptance remain open. |
| Biome blending and coherence | 4 | [Landscape.java L76](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) Threshold climate categories; weighted surface/vegetation ecotones remain unaccepted. |
| Hydrology and cave quality | 6 | [HydrologyRuntime.java L442](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) Finite sources and accepted dev31 admission/overflow repair; production still scans/copies/sorts its index. Single-owner loaded/unloaded ledger and full cave families remain open. |
| Feature/structure placement | 4 | [EngineChunkGenerator.java L115](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115) Deterministic vegetation/deposits; no general spacing/exclusion/structure pipeline. |
| Performance and threading | 3 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) No new latency benchmark; older component improvements do not establish whole load-ready/tick tails or 5ms target. |
| Determinism and stability | 7 | [HydrologyRuntime.java L260](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260) Versioned profiles/replay protections; no cross-machine bit identity or sustained travel/multiplayer soak. |
| Architecture and code quality | 6 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) Core/module boundaries enforced; adapter still combines revision/material/water/decoration responsibilities. |
| Extensibility and data-driven design | 4 | [TerrainProfile.java L54](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) Saved options and deposit definitions; no complete third-party generation-stage contract or integrated graph profiles. |
| Testing, benchmarks, documentation | 7 | [LodScratchChecks.java L12](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12) Fresh available build/package/native gates recorded individually; missing Windows/AMD, cross-architecture, long-soak and visual/performance matrix. |
| Polish: config, errors, tooling | 4 | [server_smoke.py L17](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/tools/server_smoke.py#L17) Guarded dev packaging and strict shutdown checks; terrain authoring/preview workflow and owner-hardware presentation acceptance remain open. |

## Current build and package

Full build receipt: {"pass": true, "exit": 0, "source_sha": "4a8b0fb52e7473a816655096c42f089dd340e735", "elapsed_seconds": 307.0807109369998, "fresh_product_compilation": true, "private_agent_verified": true}. Combined qualification: {"pass": true, "source_sha": "4a8b0fb52e7473a816655096c42f089dd340e735"}. Java25.0.4.1+1 / Gradle9.2.1, Linux x86_64, private one-worker functional build/cache/daemon/output. This build forces reruns even though production is unchanged. Functional elapsed time is not a latency benchmark. Pinned restoration helper freshly verified five retained tool archives; private agent identity is retained. Tools/private fixtures are excluded from payload.

[Download newly qualified development JAR](https://chatgpt.com/library?file_id=libfile_c71a1a08131881919896663fcfb17865&account_id=personal). Version/run/source identity: `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T2232-4a8b0fb-vulkan-windows.jar`, **125296756 bytes**, SHA256 `c54cd9ea03fd855f99ce4493388ad6bbc9e803bb6e055164b7894cae0331dc74`. Not stable; no CurseForge/GitHub Release publication.

Passed 35 full build tasks, independent 75 production + 63 fixture source compilation/core suite, seven outer/nested CRC/no-duplicate archive checks, 592 product and 146 independent core class identities, 222 fixture exclusions. Windows Vulkan AMD64 payload with OpenGL fallback; complete 2,508 merged Themes resource hashes,43 palette files,11 approved ground textures,148 generated soil resources and module/Vulkan notices. Final producer/export bytes rehashed after all producers, native consumers and saving.

Ten packaged Linux sessions passed: precipitation fresh/reopen; default channel2 and opt-in channel5 fresh/reopen with creation toggle off before opt-in reopen; aquifer fresh/reopen/verify across actual restarts; receiver constituent capacity. Each requires explicit records, save-all flush acknowledgement and clean shutdown/failure precedence. Strict selections bind root and all six nested modules to the exported bundle. Four profile sessions each pass 1,445 actual-loaded allowance oracle cases. Additional direct/packaged LOD scratch/reduction/discovery/timing, package-cache/core-close/inline-dispatch/waterfall, module/resource and Python normal/-O/-OO shutdown/work-state/RCON gates passed.

## Fresh ownership findings and inspected diagnostics

Fresh independent probes compile and run exact committed regular/survey loops:1,000 regular cases plus 480 survey cases pass. The preceding staged runtime fails obsolete-cursor/retired-survey witnesses. These use deterministic native/clock doubles, not Minecraft callback mutation injection. See ownership-independent-result.json and the two raw logs. No staged runtime was promoted or included in this production JAR.

The new staged regular loop polls without removing, verifies exact cursor ownership after native lookup, and rotates/removes only the current registration. Survey rechecks ownership after lookup, between columns, and before publication/requeue. Replacements retain their queue/index priority; an admitted read-only column may complete without publishing retired state. Cross-thread safety and fairness under perpetual mutation are not established.

[Builder checkpoint](https://github.com/Shnewbs/Deep-Primal/blob/4a8b0fb52e7473a816655096c42f089dd340e735/docs/validation/engine-continuation/hydrology-cursor-ownership-2026-10-26/README.md) reports full candidate API/core and prior lifecycle/index/priority/allowance regressions; six actual-tick quota cases plus five native partial-survey cases and an exact clean-world save/reopen pair. It uses a substituted-class diagnostic root with six separately loaded immutable modules, not a full Gradle distributable bundle. An initial JNA ENOSPC reopen was strictly rejected; the final new-world pair supersedes it. These author native results were inspected, not independently rerun. Complete weather/travel/native callback stress and loaded/unloaded ownership remain open.

## Restoration, isolation and limits

The previous scratch checkout, tools and caches were no longer present. Restored the approved complete-work checkpoint, verified all current 862 tracked build inputs against individual pinned Git blobs using complete non-documentation subtree inventories, and fetched 235 changed/missing files through the connector. Validated approved alpha8 Themes source archive. The tracked generator was already available; an extra private source-directory entry caused the initial duplicate-class build failure. Removing that audit-only entry corrected setup; original failure is retained. Canonical generator remains in its private themeAssets source set, excluded from payload. Pinned restoration helper freshly downloaded/verified JDK25/JDK21/Gradle and two agent dependencies. The compatibility agent was rebuilt from pinned source and its constructor/unknown-command guards passed; its new bytes/hash are retained. Direct unauthenticated Git clone failed; exact source restoration succeeded through the connector. No builder checkout or cache was accessed.

Earlier failed attempts are retained separately: duplicate audit-only generator source, a transient Gradle dependency binary-store error, and missing documentation-backed ground-resource fixtures. The full core suite passed in the third attempt, but that build still failed and its artifact was never saved. All required fixtures were restored from the pin before the final retry. Full functional workload uses fresh private Gradle cache/daemon/checkout/output; no exclusive timing benchmark. Owner policies/work-state/water ownership are rechecked before native work. Source/evidence/art/tool archives are preserved. The audit does not create a builder stop gate.

No Windows/AMD8060S execution, live OpenGL/Vulkan scene, new controlled performance benchmark, cross-architecture determinism matrix, sustained multiplayer/travel/world-size soak, or complete visual/gameplay regrade. Linux native success is not Windows acceptance. Staged candidates and unused libraries receive no gameplay feature credit.

Next priorities: builder qualifies combined staged index through full Gradle/module/bundle plus packaged weather/travel/native callback/lifecycle gates before promotion; finish conservative loaded/unloaded water ownership and bounded catch-up, then registered V8/profile/topology, ecotones, structures and real water/LOD acceptance.

Retention is recorded in retention.json: after confirming qualified new save, preserve newest three user-facing JARs by creation time, trash older stable IDs; no older binary relabeled/uploaded. Persistent evidence/art/tool caches/unrelated files are preserved.

Publication receipt: 
{"jar_pinned_source": "4a8b0fb52e7473a816655096c42f089dd340e735", "latest_main": "25cb2065b11be716ea3d2f8f3a5477a813e9bbf1", "newer_commits": 1, "newer_nonDocs": [], "builder_state_overwritten": false, "guarded_expected_parent": "25cb2065b11be716ea3d2f8f3a5477a813e9bbf1"}
Pinned JAR source remains 4a8b0fb52e7473a816655096c42f089dd340e735 regardless of newer builder commits. Guarded publication adds only this unique audit report directory and does not overwrite builder state.


<!-- END PRESERVED SOURCE -->
