# Expanded replica — docs/engine-hourly-audits/2026-10-26T2032-0a936f9/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T2032-0a936f9/REVIEW.md |
| Preserved source SHA-256 | `e6eb714da7225fbfbf4a41f7a6954326af1cea1bad1bd123ad70e64af381db2b` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — pinned 0a936f9”. Its original opening states: “Source: 0a936f9dbacf9bc363ae01c317f64060cc7587b3 · private development testing, not a stable release.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent engine audit — pinned 0a936f9
- Not run / limitations

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `all-gates.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/all-gates.json) |
| `build-command.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/build-command.json) |
| `build-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/build-result.json) |
| `independent-core-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/independent-core-result.json) |
| `package.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/package.json) |
| `post-native-archive.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/post-native-archive.json) |
| `staged-priority-author-evidence.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T2032-0a936f9/staged-priority-author-evidence.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — pinned 0a936f9

Source: [`0a936f9dbacf9bc363ae01c317f64060cc7587b3`](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/) · private development testing, not a stable release.

Production source is unchanged from accepted dev31 (`a1583a5`). The complete Git comparison covers 16 commits / 265 changed files, all under docs; all 862 retained non-documentation build-input hashes were freshly checked. This is a reconstructed committed production snapshot using the verified retained manifest and complete Git delta, not an arbitrary builder checkout. Auditor changed no production code and promoted no staged candidates.

## Grade

**50/100, average 5.0/10, Tier 2/5. All ten scores carried unchanged from the last accepted full review.** This run adds fresh functional build/package/native evidence; it is not a fresh complete gameplay, visual and performance regrade. Staged code, plans and unused libraries receive no gameplay credit. The strictly-greater-than-8.5 target remains unmet.

| Category | /10 | Pinned source and remaining qualification |
| --- | ---: | --- |
| Terrain realism and variety | 5 | [Landforms.java L12](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landforms.java#L12) — Heightfield and special tunnels; general registered V8 density and broad visual acceptance remain open. |
| Biome blending and coherence | 4 | [Landscape.java L76](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76) — Threshold climate categories; weighted surface/vegetation ecotones remain unaccepted. |
| Hydrology and cave quality | 6 | [HydrologyRuntime.java L442](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L442) — Finite sources and accepted dev31 admission/overflow repair; production still scans/copies/sorts its index. Single-owner loaded/unloaded ledger and full cave families remain open. |
| Feature/structure placement | 4 | [EngineChunkGenerator.java L115](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115) — Deterministic vegetation/deposits; no general spacing/exclusion/structure pipeline. |
| Performance and threading | 3 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) — No new latency benchmark; older component improvements do not establish whole load-ready/tick tails or 5ms target. |
| Determinism and stability | 7 | [HydrologyRuntime.java L260](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260) — Versioned profiles/replay protections; no cross-machine bit identity or sustained travel/multiplayer soak. |
| Architecture and code quality | 6 | [WorldGenPipeline.java L22](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22) — Core/module boundaries enforced; adapter still combines revision/material/water/decoration responsibilities. |
| Extensibility and data-driven design | 4 | [TerrainProfile.java L54](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54) — Saved options and deposit definitions; no complete third-party generation-stage contract or integrated graph profiles. |
| Testing, benchmarks, documentation | 7 | [LodScratchChecks.java L12](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12) — Fresh available build/package/native gates recorded individually; missing Windows/AMD, cross-architecture, long-soak and visual/performance matrix. |
| Polish: config, errors, tooling | 4 | [server_smoke.py L17](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/tools/server_smoke.py#L17) — Guarded dev packaging and strict shutdown checks; terrain authoring/preview workflow and owner-hardware presentation acceptance remain open. |

## Current artifact and checks

Full qualification status: **PASS**. [Gate receipt](all-gates.json), [build command](build-command.json), [build result](build-result.json), [independent compilation/core receipt](independent-core-result.json), [package receipt](package.json), [post-native final archive/selection checks](post-native-archive.json).

Java25.0.4.1+1 / Gradle9.2.1, Linux x86_64 functional workload with one Gradle worker, private checkout/output/cache and private daemon. Full build forces task reruns; elapsed 362.38620050399913 seconds. Independent core compilation uses 75 production + 63 fixture sources. This is not a matched latency benchmark; cache setup/restoration and functional build elapsed time do not establish gameplay performance. Pinned restoration helper verified five retained tool archives plus private fallback agent before use; tools remain outside the mod payload.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T2032-0a936f9-vulkan-windows.jar`

- Pinned source SHA: `0a936f9dbacf9bc363ae01c317f64060cc7587b3`.
- Size: **125296749 bytes**. SHA256: `cf51ba52df26eabc4d496a33d9f472a762d0322dc32a212b728c9a2294ffef56`.
- Producer/export identity is rechecked after producer completion, all native consumers and successful save. New export uses a unique completed-producer hardlink, never a prior JAR overwrite.
- 7 outer/nested archives: CRCs and duplicate paths checked; 592 compiled product comparisons, 146 independently compiled core comparisons, 222 private class exclusions.
- Windows Vulkan AMD64 native payloads with OpenGL fallback; no Linux/macOS Vulkan payload claimed in this Windows bundle. Complete merged Themes: 2508 resource hashes, 43 palette files, 11 approved ground textures and 148 generated soil resources, plus module/Vulkan license notices.

[Download the newly qualified current development JAR](https://chatgpt.com/library?file_id=libfile_9e3e7084b1a88191ad3da512621df519&account_id=personal).

Python shutdown/work-state/RCON gates run in normal, -O and -OO modes. Packaged cache-loader/CoreClose/inline-dispatch/waterfall regressions and direct+packaged LOD scratch/reduction/discovery/timing gates are recorded individually. Strict package identity gates bind all ten native logs to the exported root JAR and all six nested module selections. Native sessions are precipitation fresh/reopen; default saved channel2 fresh/reopen; opt-in channel5 fresh/reopen with creation toggle disabled before reopening; aquifer fresh/reopen/verification across real restarts; receiver constituent-capacity checks. Each requires explicit pass records, save-all flush acknowledgement and clean shutdown with failure precedence. Four profile sessions additionally run the actual loaded allowance method against 1,445 BigInteger-oracle cases each. Frozen geography/saved-profile replay is checked for the covered fixtures; this is not universal world acceptance.

## New evidence inspected and unchanged defects

The new **staged** source-priority candidate preserves equal-height registration ordering when a dry source returns before the next cleanup boundary. It retains a dormant registration token, deactivates only its ordered-set entry, changes the window token at beginPass and releases active/dormant records on explicit unload/missing FULL. Previous staged index lifecycle identity protections remain. Production HydrologyRuntime still uses whole-index cleanup/copy/sort; no candidate bytes entered this JAR.

[Author checkpoint](https://github.com/Shnewbs/Deep-Primal/blob/0a936f9dbacf9bc363ae01c317f64060cc7587b3/docs/validation/engine-continuation/hydrology-source-priority-2026-10-26/README.md) reports a failing previous-stage control, 100,003 index comparisons across100,000 randomized transitions, 1,000 actual-refresh scenarios, complete API compilation, 5,512 runtime lifecycle checks, 20,008 index checks, 144 extracted-loop cases, 1,445 allowance cases and fresh138-source core checks. Those whole-runtime and other suite counts are **inspected author results**, not newly repeated independent tests in this audit. A fresh independent execution of the exact pinned index/model fixture passes100,003 comparisons/100,000 transitions; the author compatibility baseline fails the expected dry/re-add priority witness. This is a standalone JVM model, not a whole-runtime/native proof; see priority-independent-result.json. Retained [author evidence](staged-priority-author-evidence.json) preserves exact scope. The prior independent extracted-method probes remain historical and do not certify a whole Minecraft runtime.

Next priorities: qualify the combined staged index with actual packaged native finite-quota/order/partial-survey/reentrant-callback/load/unload/weather/save/reopen cases before promotion; finish persistent single-owner loaded/unloaded water transfers and bounded catch-up; then registered V8/profile/topology, ecotones, deterministic structure mechanisms and real water/LOD scenes. Current accepted dev31 allowance/admission and bounded LOD discovery are functional repairs, not a complete hard2ms or visible-ready proof.

## Not run / limitations

No Windows execution, AMD8060S acceptance, live OpenGL/Vulkan GPU scene, cross-architecture determinism matrix, new controlled performance benchmark, sustained multiplayer/travel/large-world soak or complete visual/gameplay regrade. Linux dedicated-server native passes cannot establish Windows driver/client acceptance. No stable release, CurseForge or GitHub Release was published.

## Isolation, retention and publication

Root builder and renderer timing claims were released/functional-only at pin. Private audit lease and receipts do not pause, take over or edit another role. Only auditor-owned completed resources were relocated into this unique audit directory. No exclusive performance measurement was run.

Retention: {"inventory_exhausted": true, "kept": [{"created_at": "2026-10-26T20:50:43.533185Z", "library_file_id": "libfile_9e3e7084b1a88191ad3da512621df519", "name": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T2032-0a936f9-vulkan-windows.jar"}, {"created_at": "2026-10-26T17:46:07.728458Z", "library_file_id": "libfile_4c1faa66c5d88191a5fd74d6019a690d", "name": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261026T1730-a1583a5-vulkan-windows.jar"}, {"created_at": "2026-10-26T17:26:23.512753Z", "library_file_id": "libfile_04a8b79f273c8191907d6c704c833a13", "name": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-vulkan-windows.jar"}], "new_qualified_save_confirmed_before_trash": true, "older_auditor_local_exports_removed": ["audit-2067e27-20261026T1339/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.29-audit-20261026T1339-2067e27-vulkan-windows.jar", "audit-4a583db-20261026T1033/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.28-water.2-audit-20261026T1033-4a583db-vulkan-windows.jar", "audit-b198804-20261026T1627-iut76dcp/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.30-audit-20261026T1627-b198804-vulkan-windows.jar"], "order": "Actual Library file creation timestamps, descending; no MIME filter", "trashed": {"external_connectors_accessed": false, "results": [{"destination_path": null, "directory_id": null, "error_code": null, "file_id": "file_00000000dd9881f580fa48683efb69ab", "library_artifact_type": "other", "library_file_id": "libfile_da696bd6b3908191aa5c2eeb5f81ae2d", "message": "File moved to trash.", "mime_type": "application/java-archive", "operation": "delete", "path": "/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.30-renderer.5-vulkan-windows.jar", "status": "succeeded"}], "warnings": []}}. Source/evidence archives, approved art, unrelated mods and tooling/dependency caches are preserved. Older binary bytes were never re-uploaded to appear current.

Before publication main was re-read: {"builder_state_not_modified": true, "guarded_expected_parent": "0a936f9dbacf9bc363ae01c317f64060cc7587b3", "latest_main": "0a936f9dbacf9bc363ae01c317f64060cc7587b3", "newer_commits": 0, "pinned_jar_source": "0a936f9dbacf9bc363ae01c317f64060cc7587b3", "source_changes_since_pin": []}. Any later builder commit is distinct from this JAR source pin. Publication adds only this unique per-audit report/evidence directory using a guarded latest-parent update, preserving builder state and unrelated files. The audit is observational and creates no builder stop gate.


<!-- END PRESERVED SOURCE -->
