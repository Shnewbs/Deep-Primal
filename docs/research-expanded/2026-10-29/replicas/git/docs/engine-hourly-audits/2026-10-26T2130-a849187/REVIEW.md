# Expanded replica — docs/engine-hourly-audits/2026-10-26T2130-a849187/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2130-a849187/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T2130-a849187/REVIEW.md |
| Preserved source SHA-256 | `24021b252ea72305f61f98d936309c422cedf297143a00e007413f39c8bd961f` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent pinned engine audit — a849187”. Its original opening states: “Exact source SHA: a8491871875bf214cf4ad532c3ccc19e00f23804. Production remains accepted dev31 at a1583a5; complete comparison from preceding audit pin0a936f9 contains3commits and no non-documentation source changes. All862 retained non-documentation inputs were freshly rehashed against the verified Git manifest and complete delta. Required owner policy/work state/handoff/active audit/original rubric were read. Historical pauses do not override current owner authorization. No product implementation or staged promotion by auditor.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent pinned engine audit — a849187
- Fresh scoped finding and inspected builder evidence
- Limitations and isolation

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `retention.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2130-a849187/retention.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent pinned engine audit — a849187

Exact source SHA: `a8491871875bf214cf4ad532c3ccc19e00f23804`. Production remains accepted dev31 at a1583a5; complete comparison from preceding audit pin0a936f9 contains3commits and no non-documentation source changes. All862 retained non-documentation inputs were freshly rehashed against the verified Git manifest and complete delta. Required owner policy/work state/handoff/active audit/original rubric were read. Historical pauses do not override current owner authorization. No product implementation or staged promotion by auditor.

**50/100, average5.0/10, Tier2/5 — all ten scores carried unchanged. Not a fresh complete regrade.** Functional qualification does not establish broad morphology, gameplay, visuals, performance or cross-platform acceptance. Strictly greater than8.5 remains unmet.

| Category | /10 | Pinned evidence / remaining gap |
| --- | ---: | --- |
| Terrain realism and variety | 5 | [Landforms.java L12](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) Heightfield and special tunnels; general registered V8 density and broad visual acceptance remain open. |
| Biome blending and coherence | 4 | [Landscape.java L76](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) Threshold climate categories; weighted surface/vegetation ecotones remain unaccepted. |
| Hydrology and cave quality | 6 | [HydrologyRuntime.java L442](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) Finite sources and accepted dev31 admission/overflow repair; production still scans/copies/sorts its index. Single-owner loaded/unloaded ledger and full cave families remain open. |
| Feature/structure placement | 4 | [EngineChunkGenerator.java L115](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115) Deterministic vegetation/deposits; no general spacing/exclusion/structure pipeline. |
| Performance and threading | 3 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) No new latency benchmark; older component improvements do not establish whole load-ready/tick tails or 5ms target. |
| Determinism and stability | 7 | [HydrologyRuntime.java L260](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260) Versioned profiles/replay protections; no cross-machine bit identity or sustained travel/multiplayer soak. |
| Architecture and code quality | 6 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) Core/module boundaries enforced; adapter still combines revision/material/water/decoration responsibilities. |
| Extensibility and data-driven design | 4 | [TerrainProfile.java L54](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) Saved options and deposit definitions; no complete third-party generation-stage contract or integrated graph profiles. |
| Testing, benchmarks, documentation | 7 | [LodScratchChecks.java L12](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12) Fresh available build/package/native gates recorded individually; missing Windows/AMD, cross-architecture, long-soak and visual/performance matrix. |
| Polish: config, errors, tooling | 4 | [server_smoke.py L17](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/tools/server_smoke.py#L17) Guarded dev packaging and strict shutdown checks; terrain authoring/preview workflow and owner-hardware presentation acceptance remain open. |

## Current build and package

Full build receipt: {"pass": true, "exit": 0, "source_sha": "a8491871875bf214cf4ad532c3ccc19e00f23804", "elapsed_seconds": 319.7506310190074, "fresh_product_compilation": true, "private_agent_verified": true}. Combined qualification: {"pass": true, "source_sha": "a8491871875bf214cf4ad532c3ccc19e00f23804"}. Java25.0.4.1+1 / Gradle9.2.1, Linux x86_64, private one-worker functional build/cache/daemon/output. This build forces reruns even though production is unchanged. Functional elapsed time is not a latency benchmark. Pinned restoration helper freshly verified five retained tool archives; private agent identity is retained. Tools/private fixtures are excluded from payload.

[Download newly qualified development JAR](https://chatgpt.com/library?file_id=libfile_78a0e39016b88191952d3f29b434b346&account_id=personal). Version/run/source identity: `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T2130-a849187-vulkan-windows.jar`, **125296780 bytes**, SHA256 `c7308e7f9a294eb464d542fbd768cf09c7d67bc967bfb305f46b31423770c5bd`. Not stable; no CurseForge/GitHub Release publication.

Passed35full build tasks, independent75production+63fixture source compilation/core suite, seven outer/nested CRC/no-duplicate archive checks, 592 product and 146 independent core class identities, 222 fixture exclusions. Windows Vulkan AMD64 payload with OpenGL fallback; complete2508 merged Themes resource hashes,43palette files,11approved ground textures,148generated soil resources and module/Vulkan notices. Final producer/export bytes rehashed after all producers, native consumers and saving.

Ten packaged Linux sessions passed: precipitation fresh/reopen; default channel2 and opt-in channel5 fresh/reopen with creation toggle off before opt-in reopen; aquifer fresh/reopen/verify across actual restarts; receiver constituent capacity. Each requires explicit records, save-all flush acknowledgement and clean shutdown/failure precedence. Strict selections bind root and all six nested modules to the exported bundle. Four profile sessions each pass1445 actual-loaded allowance oracle cases. Additional direct/packaged LOD scratch/reduction/discovery/timing, package-cache/core-close/inline-dispatch/waterfall, module/resource and Python normal/-O/-OO shutdown/work-state/RCON gates passed.

## Fresh scoped finding and inspected builder evidence

Fresh independent execution of the exact committed callback-loop extraction: **2004 candidate cases pass**; the prior staged baseline fails the obsolete-cursor witness (2deliveries where0required). Runtime and extracted fixture hashes match the author records. Deterministic chunk/clock doubles exercise callback removal, replacement, reordering, drying, insertion, no-ops, missing FULL, next-pass recovery and no forced loading. This is not a whole-runtime compile or real Minecraft callback-injection test. See callback-independent-result.json and callback-independent.txt.

The new staged index Pass captures an identity revision and terminates invalid traversal after mutations; loop guards recheck after chunk lookup and between source columns. In-flight transaction completes; current sources are reconsidered next tick. Idempotent/no-op changes do not invalidate. No concurrency or perpetual-mutation fairness guarantee. Production still uses whole-index cleanup/copy/sort: candidate classes are not in the delivered production JAR.

[Builder checkpoint](https://github.com/Shnewbs/Deep-Primal/blob/a8491871875bf214cf4ad532c3ccc19e00f23804/docs/validation/engine-continuation/hydrology-callback-pass-2026-10-26/README.md) and callback-author-records.json were inspected. Author reports whole candidate API compile, lifecycle/priority/allowance suites, fresh138sourcecore, six actual-tick controlled native quota scenarios with exact water/salt/sediment and one saved-parcel reopen check. The diagnostic root substitutes candidate classes and loads six immutable modules separately; it is not a full bundled release. Producer identity guards have12 positive/negative normal/-O/-OO controls. Initial ENOSPC, compile/dependency/RCON setup failures and a rejected stale-fixture launch are explicitly excluded by author. These native results were not independently rerun here and do not promote the candidate.

Next: builder qualifies combined staged index in full build/module/bundle gates, native partial-survey/callback/weather/travel cases, then persistent single-owner loaded/unloaded transfers, bounded catch-up, V8 profiles/topology, ecotones/structure mechanisms and real water/LOD scenes.

## Limitations and isolation

No Windows/AMD8060S execution, live OpenGL/Vulkan scenes, cross-architecture matrix, new controlled latency benchmark, sustained multiplayer/travel/large-world soak or complete gameplay/visual regrade. Linux native success is not Windows acceptance. No score credit for staged prototypes or unused libraries.

Timing reservations checked before build and again before native work; only auditor-owned completed resources were moved/cleaned. Builder checkout/cache/output/lease untouched. Disk availability reached zero during producer execution; diagnostics and completion status are authoritative, not an assumed success. See own-completed-cleanup.json.

Retention receipt: [retention.json](retention.json). Only after a qualified new save, older JARs beyond newest3 are trashed by stable ID; source/evidence archives, art, tools, caches and unrelated mods remain. No older binary re-uploaded.

Publication latest-parent receipt: {"jar_pinned_source": "a8491871875bf214cf4ad532c3ccc19e00f23804", "latest_main": "a8491871875bf214cf4ad532c3ccc19e00f23804", "builder_state_overwritten": false, "guarded_expected_parent": "a8491871875bf214cf4ad532c3ccc19e00f23804"}. JAR pin remains a8491871875bf214cf4ad532c3ccc19e00f23804 regardless of newer builder changes. Evidence publication adds only this unique audit directory through a guarded expected-parent update; no builder state or unrelated files overwritten. This report is observational, not a builder stop gate.


<!-- END PRESERVED SOURCE -->
