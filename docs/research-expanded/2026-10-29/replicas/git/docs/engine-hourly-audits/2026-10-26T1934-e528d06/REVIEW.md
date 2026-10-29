# Expanded replica — docs/engine-hourly-audits/2026-10-26T1934-e528d06/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T1934-e528d06/REVIEW.md |
| Preserved source SHA-256 | `4fc8c8e6247a24361c6fbbd287f60897c77d75f4c0b3d0fb6e6d214717f74d60` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent audit checkpoint — e528d06”. Its original opening states: “No new JAR. Zero overlay headroom and insufficient independent memory-backed build capacity prevent full Gradle/module/package/native qualification. This is not a successful current build.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent audit checkpoint — e528d06
- Remaining defects and next priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `loop-harness.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/loop-harness.txt) |
| `physical-methods.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/physical-methods.json) |
| `probe-source-identity.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/probe-source-identity.json) |
| `production-inputs.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/production-inputs.json) |
| `targeted-result.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1934-e528d06/targeted-result.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent audit checkpoint — e528d06

**No new JAR.** Zero overlay headroom and insufficient independent memory-backed build capacity prevent full Gradle/module/package/native qualification. This is not a successful current build.

Pinned main at start: `e528d066d8202ac1a2822de700099a34ce67d470`. Complete three-commit comparison from prior auditor pin8665e07 contains **no non-documentation changes**. Production remains dev31; latest staged lifecycle work is outside production. A fresh build is still required despite unchanged production, but could not be completed here. No intended version is represented as a produced artifact, and no fresh size/SHA is fabricated.

**50/100, average 5.0/10, Tier 2/5. All ten scores carried, not a fresh complete regrade.** No staged/unused library earns shipped-feature credit; mean strictly above 8.5 remains unproven.

## Ten equally weighted categories

| Category | /10 | Status | Pinned implementation and evidence/limits |
|---|---:|---|---|
| Terrain | 5 | Carried | [L34](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java#L34). Production unchanged; prior V1–V7/density/core compatibility evidence carried. Registered saved V8 and broad visual morphology remain unaccepted. |
| Biomes | 4 | Carried | [L76](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76). Threshold palette unchanged; weighted ecotone/material/vegetation pipeline still missing. |
| Hydrology/caves | 6 | Carried | [L451](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L451). Fresh staged lifecycle probes pass, but candidate is not production. Full water ledger, loaded/unloaded ownership and cave/temporal acceptance remain open. |
| Features/structures | 4 | Carried | [L115](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115). Current deposit/tree mechanisms unchanged. No general spacing/exclusion/structure pipeline acceptance. |
| Performance/threading | 3 | Carried | [L45](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45). Production cleanup/copy/sort and individual callbacks remain unbounded. No fresh latency, allocation, FPS or hardware benchmark. |
| Determinism/stability | 7 | Carried | [L260](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260). Prior complete core and native replay evidence carried. No fresh whole-core/native/cross-machine/soak pass here. |
| Architecture | 6 | Carried | [L22](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22). Module boundaries unchanged. New extracted lifecycle/model checks do not prove complete lifecycle integration or global water ownership. |
| Extensibility | 4 | Carried | [L54](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54). Saved options/deposit API unchanged. Registered V8 graph/profile/authoring API remains incomplete. |
| Tests/docs | 7 | Carried | [L12](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/LodScratchChecks.java#L12). Fresh baseline-failure controls 144 loop cases, 5,512 extracted lifecycle checks and 20,008 index checks pass. Full build/package/native gates remain unfinished. |
| Polish/tooling | 4 | Carried | [L17](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/tools/server_smoke.py#L17). Current work-state validates; existing qualified JAR retained. Client/platform/authoring acceptance and full build capacity remain missing. |

## New completed work inspected

Read pinned main, current work-state, handoff, ACTIVE-ENGINE-AUDIT, Builder/auditor policy, hourly policy and original rubric. Current Builder is checkpointed, reservation released and exclusive_benchmark=false; no Builder stop/pause/reset/kill/lease takeover or workspace edit. No fresh direct other-chat ownership proof is claimed.

[Builder staged lifecycle report](https://github.com/Shnewbs/Deep-Primal/blob/e528d066d8202ac1a2822de700099a34ce67d470/docs/validation/engine-continuation/hydrology-index-lifecycle-2026-10-26/README.md) repairs two defects in the **previous staged incremental index**, not two reproduced production dev31 defects:

1. The regular visit path could discard a missing-FULL chunk cursor while leaving its source/survey entries. Revised staged code shares forgetChunk with unload, removing all memberships and applying the existing regional cache-retention policy without forcing a chunk load.
2. A retired/dry cursor refresh could remove the newer generation's source. Revised remove(key,expectedCursor) preserves replacement identity.

Builder reports complete candidate API compilation, 144 extracted-loop cases, 5,512 actual staged-runtime lifecycle checks, 20,008 index checks and 1,445 allowance cases and a fresh complete 138-source core pass. Those complete-API/allowance/core claims are **author evidence inspected, not fresh auditor executions**. Full mod/Gradle/package/native capacity gate remains open.

## Fresh independent evidence

Pinned stage/baseline source blobs and author harness were fetched exactly, Git-blob/SHA256 verified, and executed solely in this auditor's unique output. No candidate was promoted.

- Previous staged loop intentionally fails on stale index/survey after a missing-FULL chunk. Revised candidate passes **144 cases**, covering counts1/2/17/64/128/8192, quotas0/1/2/16/64/128, mixed availability and expired/open admission; explicit doubles reject forced loads.
- Previous staged refresh intentionally fails because a retired cursor removes a newer source generation. Revised extracted methods pass **5,512 lifecycle checks**: 1,000 replacement sequences at signed-extreme keys, active/dry stale refresh, 512 removals, idempotence and empty final indexes.
- Actual candidate StreamSourceIndex class freshly compiles and passes **20,000 randomized mutations / 20,008 checks** against a stable LinkedHashMap+descending-sort oracle, invalid-height rejection, immutable entries, replacement/ties/unload and bounded caller prefixes.
- All **five complete physical method bodies** (replenishStream, streamAllowance, visit, precipitation, write) match production exactly by fresh full-body comparison and SHA256. This is not a proof of scheduling equivalence or whole-world conservation.
- All **862** retained non-documentation input hashes freshly match; complete Git comparison confirms unchanged production. Current work-state structural validation passes.

**Scope:** the fresh lifecycle check targets actual extracted Work/Cursor/refreshSource/forgetChunk bodies inside a self-contained fixture with explicit native/clock doubles. It adapts only fixture class/target names and changes its marker to extractedMethods=true. All assertions remain. It is **not** a complete HydrologyRuntime API compile, a Minecraft world, a packaged native session, or gameplay acceptance. Index tests execute the exact staged standalone class. Baseline failures are required negative controls, not product passes. See [loop-harness.txt](loop-harness.txt), [targeted-result.json](targeted-result.json), [physical-methods.json](physical-methods.json), [probe-source-identity.json](probe-source-identity.json) and [production-inputs.json](production-inputs.json).

The previous auditor's successful 138-source complete core/LOD pass and prior dev31 full 35-task build, 592 product-class comparisons, seven archive checks and ten native sessions matrix remain **historical carried evidence**. They were not rerun or relabeled here. No fresh performance/latency/allocation/GPU claim.

## Build and resource gate

Overlay free space remained0. At preflight shared memory had about13MiB free, later fluctuating above200MiB; this is below the complete private project/cache/Windows bundle/Themes/native workload. The small probes fit with 128 MiB compiler/fixture heaps and 96 MiB index execution. Full build/cache/daemon/export resources were not started; no competing shared mutable tool/output/cache directory was used. Read-only retained JDK and production input files were used; fresh source/probe outputs are isolated in `/dev/shm/audit-e528d06-20261026T1934`.

**Unfinished:** current full Java 25/Gradle/core/module build, uniquely named Windows Vulkan/OpenGL-fallback JAR, source/compiled/package class identity, outer/nested CRCs, platform payloads/notices/complete Themes/private-fixture exclusion, packaged source-quota/partial-survey/load-unload/reentrant callback/weather/save/reopen/shutdown, final producer/export SHA/size and successful new save. No corrupt/failing/old installer is shipped as new.

No production changes were made, and no R57/R60/R61/R62 or staged prelude/index/renderer byte-accounting files were included in a mod payload. Frozen V1–V7 geography, profiles, quantities/constituents, player edits, approved art and modular Deep Primal identity remain untouched. The earlier renderer byte-accounting candidate remains staged and receives no gameplay/FPS/VRAM credit.

## Last verified artifact and retention

[Previous verified dev31 audit JAR—not a fresh build](https://chatgpt.com/library?file_id=libfile_4c1faa66c5d88191a5fd74d6019a690d&account_id=personal). Source `a1583a508b0d67130d387bc018defd5758acd496`; version `0.0.1-alpha.29-dev.31-audit-20261026T1730-a1583a5`; 125,296,766 bytes; SHA256 `4661080982896a19bca54751c33b78fbbe07a4b59b7bbafa8d9b64e039630229`. This older byte stream was neither uploaded again nor relabeled.

All Library pages were checked without a MIME restriction; exactly the newest three user-facing Deep Primal JARs remain. No new save/deletion was needed. Source/evidence archives, art, tools/dependency caches, unrelated mods and active other-role outputs were untouched. The prior completed auditor export duplicates had already been cleaned; no additional local export was created.

## Remaining defects and next priorities

Current production still performs unbounded source-index cleanup/copy/sort and can overrun on one callback/survey. The staged lifecycle repair does not qualify the combined index. Restore independent build capacity first, then require full Gradle/module/package/native quota/partial-survey/load/unload/reentrancy/tie-order/weather/restart gates before promotion. Follow with persistent single-owner water transfers, bounded catch-up, world edits, registered saved V8, ecotones/features/cave families and live water/LOD acceptance.

**Not run:** fresh whole-core/full mod build and current archives/native matrix; Windows/AMD8060S; actual OpenGL/Vulkan pixels/reload/resize/reset/culling, cliff-water temporal continuity and live LOD handoff/cache races; named-hardware frame/chunk/tick/IO/VRAM measurements; long multiplayer/travel/edit/unload soak; cross-machine replay and full world-water ledger. Standalone Linux probes are not Windows acceptance. Actual FSR/DLSS providers/frame generation remain absent.

Main re-read before publication: `e528d066d8202ac1a2822de700099a34ce67d470`. Pin stays `e528d066d8202ac1a2822de700099a34ce67d470`; at publication main still equals the pin, with no newer production changes observed. Only this unique audit evidence/report path is published with a latest-parent/ref guard. No GitHub Release, CurseForge publication or stable label. Partial audit work remained possible despite the temporary build-capacity gate; retain the task for continuation until the owner cancels or a fresh complete average strictly exceeds 8.5.


<!-- END PRESERVED SOURCE -->
