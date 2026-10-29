# Expanded replica — docs/engine-hourly-audits/2026-10-26T1135-93ac6cb/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1135-93ac6cb/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T1135-93ac6cb/REVIEW.md |
| Preserved source SHA-256 | `544e76ce265488fafb97ab6283facf12f6257f0230a3c2de536e907f6a59411e` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — dev.29 / 93ac6cb”. Its original opening states: “Download DeepPrimal-26.1.2-0.0.1-alpha.29-dev.29-audit-20261026T1135-93ac6cb-vulkan-windows.jar”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Turn the recorded finite-water scheduling behavior into a progress contract under contention, rather than equating an admission clock with a hard execution deadline.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Specify resumable cursor ownership, fair service among source/recharge/transport queues, and the durable state retained at budget exhaustion.
- Track admitted work, actual elapsed cost, longest indivisible callback, queue age, and deferred debt separately.

## Research expansion — acceptance checks

- Saturate source work while regular recharge is enabled and verify bounded starvation without creating or losing water.
- Interrupt every cursor boundary, unload neighboring chunks, and resume after reopen; compare visited cells and conservation with an uninterrupted oracle.

## Research expansion — source anchors

- Independent engine audit — dev.29 / 93ac6cb
- Isolation, omissions and next priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — dev.29 / 93ac6cb

[Download DeepPrimal-26.1.2-0.0.1-alpha.29-dev.29-audit-20261026T1135-93ac6cb-vulkan-windows.jar](https://chatgpt.com/library?file_id=libfile_cdfec1113b388191ae82220c2191d16f&account_id=personal)

**Qualified private development testing JAR. Scoped score50/100, average5.0/10,Tier2/5.** This restores the prior hydrology/stability penalties after freshly rerunning the actual failure case; eight other scores are carried. It is not a fresh complete whole-engine regrade or >8.5 acceptance.

Exact source pin `93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a` read from main before restoration. Unique version `0.0.1-alpha.29-dev.29-audit-20261026T1135-93ac6cb`. Final size125291270bytes; SHA256 `6e2c7d5295e85e93f350369c7a6e50e03934ecffeb7e637e3db38a51cf8dbc2a`. Windows Vulkan bundle with OpenGL fallback and complete Themes0.1.0-alpha.8. Producer/export bytes equal after producer completion, all native sessions and Library save. A regular export copy is used; no earlier binary is relabeled. No GitHub Release/CurseForge or stable release promotion.

## Ten equal categories

| Category | /10 | Status | Pinned implementation and fresh scoped evidence |
|---|---:|---|---|
| Terrain | 5 | carried | [L34](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java#L34): Registered V1–V7; V6/V7/density checks pass; no native registered V8 saved-profile/topology acceptance. |
| Biomes | 4 | carried | [L76](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Threshold palette selection persists; core gradients/biomes pass, no weighted ecotone surface/vegetation acceptance. |
| Hydrology/caves | 6 | 5 → 6, restore prior penalty | [L73](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterAccount.java#L73): Capacity rejection now returns0 before mutation. 80000 split cases and actual precipitation positive/rejection/persistence fresh+reopen pass. Broad world ledger/cave/water acceptance remains open. |
| Features/structures | 4 | carried | [L115](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115): Authored deposit/native replay checks pass; empty structure set and incomplete generic spacing/exclusion pipeline remain. |
| Performance/threading | 3 | carried | [L423](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and shared region synchronization persist. New LOD bounds are a correctness fix, no measured speedup. Historical14.452–14.988ms warm fills exceeded5ms; not rerun. |
| Determinism/stability | 7 | 6 → 7, restore prior penalty | [L260](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260): Former native precipitation crash no longer occurs in its actual-call fixture. Ten packaged sessions, saved/reopened account parity and default/channel5 replay pass. No cross-machine or long-soak acceptance. |
| Architecture | 6 | carried | [L22](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22): Both module boundaries, independent core and package-extracted lifecycle controls pass. Shared generator coupling and incomplete loaded/unloaded ownership remain. |
| Extensibility | 4 | carried | [L54](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved terrain/deposit options work; full registered V8, ecotone/feature lifecycle and authoring API unaccepted. Staged/unused prototypes earn no gameplay credit. |
| Tests/docs | 7 | carried | [L1](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/src/coreChecks/java/com/shnewbs/deepprimal/core/SplitAdmissionChecks.java#L1): Complete build/core/resource checks, new regression in normal entrypoint, packaged tests and ten native sessions. Windows/GPU/soak/controlled benchmark matrix incomplete. |
| Polish/tooling | 4 | carried | [L17](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/tools/server_smoke.py#L17): Strict native/RCON error precedence, complete Themes/art/notices preserved. LOD exact bounds pass synthetic controls; client/authoring/deprecation and platform limits remain. |

The mean is5.0, not a proportion of tests passing. Last complete accepted baseline remains50/100. Current scoped reassessment closes only the reproduced precipitation regression penalty; it does not certify complete water, V8, renderer or world-generation quality.

## Actual implementation changes and evidence inspected

Since previous audit pin4a583db, production WaterAccount.distribute changes supported constituent-capacity rejection from an exception to ordinary zero delivery before any account mutation. Donor/ground/snow water,salt,sediment remain unchanged on rejection; allocation/rounding and invalid-alias errors remain. SplitAdmissionChecks is now in normal CoreChecks,80000cases. The private native PrecipitationPersistenceChecks is committed in visualValidation and excluded from the shipped archive. It proves nonzero real precipitation demand before checking repeated saturated rejection, codec roundtrip and actual-process save/reopen without force-loading the remote aquifer.

LOD Builder now initializes/resets each emitted part's extrema independently instead of retaining empty chunk-floor space/prior parts. LodPartBoundsChecks verifies18parts,0loose bounds across signed coordinates/floors, all resolutions, solid materials and partial water, while retaining the geometry/material/normal/index digest. Synthetic slab rejections162versus18 are correctness/visibility counts, not GPU/frame performance. Actual FSR/DLSS providers are not implemented or credited; the separate six-category renderer self-audit is not substituted for this engine rubric.

Inspected immutable reports: [dev.29 qualification](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/docs/validation/engine-continuation/precipitation-release-2026-10-26/README.md) and [LOD bounds self-audit](https://github.com/Shnewbs/Deep-Primal/blob/93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a/docs/renderer-jams/20261026-part-bounds/REVIEW.md). Their failed intermediate hard-link export/flag comparisons and limited hardware scope are retained as historical evidence, not fresh audit failures or passes. The older pinned auditor's actual precipitation crash is retained at d6e3250 and is not erased by this repair.

The committed cliff-wall/shared-history repairs are included unchanged. Earlier foreground Windows/AMD GPU/readback/live-mirror receipts were inspected previously; no current audit Windows/GPU/pixel/FPS acceptance is inferred from those receipts. No R57/R60/R61/R62 or new scheduler prototype was promoted. The builder's active functional natural-stream scheduler work is outside this pin.

## Fresh build and package checks

Full Java25.0.4.1/Gradle9.2.1 build:35tasks executed, no product tests/assertions/source sets excluded. Full core,cache,ground resources and both module boundaries pass. Independent javac -g compiles73production+59fixture sources; complete -Xverify:all CoreChecks passes. Core diagnostic elapsed times are not a controlled benchmark. Package-extracted actual-core code-source guards, lifecycle/water/cliff regressions, SplitAdmissionChecks and LodPartBoundsChecks pass.

Seven outer/nested archive CRCs and duplicate-member checks pass. 588product compiler/package classes and142independent core hashes match; 217private classes excluded. Windows-only Vulkan native payload, OpenGL fallback,2508Themes resource hashes,43palette resources,11approved ground images,148generated soil resources and exact Vulkan LICENSE/COPYING/UPSTREAM notices pass. Private build agent, tools, authoring generators and validation fixtures stay outside mod payload. Final post-native archives/platform signatures and source/byte identities retained separately.

Python12state+16shutdown+12RCON tests pass normal/-O/-OO,40permode. All860restored build/resource/doc inputs match pinned Git identities; five retained pinned restoration assets/private agent reverified. Per-module trees were fetched independently to avoid large recursive-tree transport errors. Inputs include current source only; retained older source resources are reused only after current Git blob equality checks.

## Ten packaged Linux native sessions

All require explicit fixture pass records, actual production code from the exported outer/nested JAR, RCON save-flush acknowledgment, failure precedence, ordered shutdown and launcher exit0. Native errors override Gradle0. Archive SHA is checked after producers finish and throughout qualification.

1. Precipitation fresh/reopen: nonzero admitted positive control, repeated saturated constituent rejection, exact saved/decoded water/salt/sediment, no remote forced aquifer load. Former actual native crash no longer occurs.
2. Default channel2 fresh/reopen: actual terrain/deposit/soil/source native checks, loaded-class hashes, matching replay/default baseline hashes.
3. Opt-in channel5 fresh/reopen: same native gates; creation toggle disabled before reopen and actual saved channel5 retained.
4. Aquifer fresh/reopen/verify: two process restarts, depths1/64, extreme Long.MIN_VALUE timestamp, bounded steps, exact quantities/constituents and no force load.
5. Receiver capacity: seven atomic rejections/four exact-capacity transfers, exact water/salt/sediment retention without block mutation on rejected preparations.

These total10actual sessions, not10benchmark repetitions or Windows tests. Private local disposable worlds, seed20261020,view/simulation2,1024MiB game heap,768MiB Gradle ceiling. Product heap defaults are unchanged. Source files unchanged by audit; exported/producer SHA remains identical after all sessions.

## Isolation, omissions and next priorities

Linux x86_64 AMD EPYC9V74 virtual host,9exposed CPUs,8GiB cgroup limit. New isolated source/output/temp and private Gradle cache/daemon. Only the prior completed auditor's cache was moved into this unique directory and completed auditor build outputs reclaimed; source/evidence and saved artifacts preserved. Retained immutable JDK/Gradle tools are read-only and outside payload. No builder checkout,state,lease,cache,process or active measurement was edited/paused/killed. At pin the builder reservation is active functional_scheduler_reproduction with exclusive_benchmark=false and own stream1134 resources; it does not reserve a shared timed performance run. No timings are claimed here. Audit progress is observational and not a builder stop gate.

Not run: Windows/AMD8060S execution, actual OpenGL/Vulkan client/GPU/pixels, natural moving cliff/wet-dry scene acceptance, FSR/DLSS, current frame/tick/chunk/I/O/VRAM performance comparisons, multiplayer/long-soak, cross-machine replay, full loaded/unloaded journal/edit/catch-up ledger, registered V8 topology/saved-graph/routed-water acceptance. Linux smoke is not Windows acceptance. Frozen V1–V7, profiles, edits, finite quantities, approved art, Deep Primal identity and separate graphics/LOD/Vulkan modules preserved within the checked scope.

Next: bounded natural-stream scheduling and single-owner loaded/unloaded water persistence/edit/restart/catch-up; independent V8 saved-profile/topology parity; natural cliff temporal/pixel and LOD travel/edit/reset/error coverage; named-hardware latency/tails; ecotone and feature-stage completeness. A split-admission repair and10native sessions do not establish state-of-the-art water or a >8.5 engine.

During this audit, newer main added staged stream-deadline evidence at d1b0ea44000e854714f0f6b83780d60ab0b54c3f. The production tick method admits up to255 later callbacks after a simulated first callback exhausts the budget; the staged per-column condition has only deterministic boundary-double qualification. This is not a measured native2ms limit and is not promoted or included in this JAR. See later-stream-evidence.json; performance remains3/10 pending real workload acceptance.

## Publication identity

JAR source stays `93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a`. Latest main reread before guarded publication: `d1b0ea44000e854714f0f6b83780d60ab0b54c3f`; newer implementation, if present, is excluded from this immutable JAR. Only this per-audit report/receipt path is added on the latest parent with a guarded ref update; builder state/unrelated files untouched. Audit remains enabled because fresh complete >8.5 acceptance has not been demonstrated. No claim of continuous execution between finite runs.


<!-- END PRESERVED SOURCE -->
