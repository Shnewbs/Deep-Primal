# Expanded replica — docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T1339-2067e27/REVIEW.md |
| Preserved source SHA-256 | `0513cc459124db0cd128feedf27f31454485c00aa0f3003f4d316e75c6faf699` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent current development build audit — 2067e27”. Its original opening states: “Download current uniquely named development JAR”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent current development build audit — 2067e27
- Isolation and limits

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent current development build audit — 2067e27

[Download current uniquely named development JAR](https://chatgpt.com/library?file_id=libfile_0b07185d35548191b187382a12ad02da&account_id=personal)

**50/100, average5.0/10, Tier2/5. All ten scores carried.** Fresh scoped verification below is not a complete regrade and does not demonstrate the strictly-greater-than8.5 goal. Stage-only work and unused libraries receive no gameplay credit. The renderer's separate six-category self-review is not this rubric.

Exact source pin from main at start: `2067e273e85fb4f80530c86a7c238106cbbb4cdf`. Version `0.0.1-alpha.29-dev.29-audit-20261026T1339-2067e27`. Final size125294742bytes; SHA256 `ba71f7937145171558326a3ebdca332d85f04b89b33a2ec24a77e4bf8a0b5c56`. Windows Vulkan AMD64 development bundle with OpenGL fallback, complete Themes0.1.0-alpha.8, notices and approved art. Producer/export bytes verified equal after completion, all native sessions and saving. Fresh producer and regular export, no old JAR relabel/overwrite, stable label, CurseForge or GitHub Release publication.

## Ten equal categories

| Category | /10 | Status | Pinned code and evidence |
|---|---:|---|---|
| Terrain | 5 | carried | [L34](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/main/java/com/shnewbs/deepprimal/engine/EngineRegistry.java#L34): V1–V7 registered; fresh V6/V7/density checks pass. Registered native V8 saved topology remains unaccepted. |
| Biomes | 4 | carried | [L76](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Threshold palette selection persists; no weighted surface/vegetation ecotone acceptance. |
| Hydrology/caves | 6 | carried | [L73](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterAccount.java#L73): Fresh split/precipitation/restart tests pass; broad loaded/unloaded account ownership, edits, cave morphology and native scheduling remain open. |
| Features/structures | 4 | carried | [L115](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L115): Deterministic deposits/native replay pass. Generic spacing/exclusion and structure pipeline remain incomplete. |
| Performance/threading | 3 | carried | [L423](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill/shared cache setup persist; telemetry is not a speedup. Historical14.452–14.988ms fill means exceeded5ms; no controlled performance rerun. |
| Determinism/stability | 7 | carried | [L260](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L260): Ten current packaged native sessions pass saved profiles/replay and exact quantities. Cross-machine identity/long multiplayer soak unrun. |
| Architecture | 6 | carried | [L22](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L22): Module boundaries and real core lifecycle controls pass. Generator coupling and incomplete loaded/unloaded ownership remain. |
| Extensibility | 4 | carried | [L54](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved terrain/deposit options work; full V8 graph/profile/feature lifecycle authoring and public extension contract are not accepted. |
| Tests/docs | 7 | carried | [L6](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/src/coreChecks/java/com/shnewbs/deepprimal/core/RollingTimingChecks.java#L6): Fresh full build, independent core, package/native checks and scoped survey reproduction pass. Hardware/client/soak matrix incomplete. |
| Polish/tooling | 4 | carried | [L17](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/tools/server_smoke.py#L17): Strict error/save/shutdown checks, complete Themes and art resources preserved. Client visuals, authoring and platform gates remain open. |

## Source delta and latest completed evidence

**No production/test/build-source changes since prior auditor pin68b2278.** Fresh root Git tree comparisons prove all nine selected build-module trees identical (src,engine-core,tools,gradle,graphics-settings,gui-themes,lod-engine,performance-engine,renderer-vulkan); top-level build inputs also match. Changes are documentation, work state and staged qualification evidence. This audit still produces the requested new JAR. Frozen V1–V7 geography, saved profiles, finite water/constituents, owner edits, identity and separate modules preserved. No R57/R60/R61/R62, stream deadline/wrap candidate or other staged prototype is promoted.

Latest builder [survey qualification](https://github.com/Shnewbs/Deep-Primal/blob/2067e273e85fb4f80530c86a7c238106cbbb4cdf/docs/validation/engine-continuation/survey-qualification-2026-10-26/README.md) addresses the previously missing actual-survey-method extraction. Builder receipt records baseline38cases/5973assertions and candidate158cases/27752assertions, plus full staged HydrologyRuntime targeted API compilation against retained game/product dependencies (five emitted classes, -proc:none). That targeted compile is inspected historical evidence, not this audit's full production build or native acceptance.

Auditor freshly reran its committed check.py against exact pinned production and candidate bytes, reproducing both counts. It extracts each survey method verbatim and controls Minecraft/terrain/time boundaries with doubles. Cases cover five clock origins, six costs, five initial partial cursors, no skipped/duplicate columns across resumption, unloaded defer/no forced load, stale removal, wet/dry/ocean filtering and expired/wrapped deadlines. Production wrapped deadline suppresses work, candidate admits10columns. Source/method hashes and fresh receipt retained. Initial transfer added one trailing newline to candidate source; exact Git-blob bytes were restored and both counts rerun successfully, with the initial receipt retained separately. Candidate SHA256 ec9c07dcc1b3b7621fe6b099e3e51d812bda9984dfee270ffb63fa67a820c311 remains outside production and exported JAR. Native survey/source quotas, conservation, restart, latency and fairness still require qualification before promotion. No >8.5/performance credit from test counts.

Prior exact tick-clock evidence216baseline/1266candidate cases and1200000arithmetic checks was inspected and rerun in the previous audit; not rerun here. Production per-page deadline checks can admit255late callbacks in that synthetic exhaustion control; inner-column/wrap candidate is still excluded. Single admitted calls and cleanup/sorting may exceed the total budget. LOD bounded timing telemetry remains unchanged, defaults OFF and measures scoped CPU work, not GPU/whole-frame times. FSR/DLSS vendor providers remain absent. These defects/gaps are not closed by this fresh build.

## Fresh verification

Java25.0.4.1/Gradle9.2.1 full available build passed in352.4s:35actionable tasks executed with --rerun-tasks, all core/cache/material resources/module boundaries and visualValidation compile. No implementation assertions or product test source sets removed. Independent javac -g compiles74production+60private fixtures and complete -Xverify:all CoreChecks passes. Python state12+shutdown16+RCON12 tests pass normal/-O/-OO. Direct and package-extracted RollingTiming checks pass1580oracle windows/30000samples/100000concurrent writes; incidental suite elapsed output is not a controlled benchmark.

Seven outer/nested archives pass CRC/no duplicates;590production compiler/package class identities and144independent core hashes match;218private classes excluded. Two Windows AMD64PE DLLs and no Linux/macOS Vulkan payload, OpenGL fallback,2508Themes resources,43palette resources,11approved ground images,148generated soils and exact Vulkan LICENSE/COPYING/UPSTREAM notices verified. Tools, authoring generators, compatibility agent and private fixtures excluded. Actual package-core code-source/lifecycle/water/cliff/split/LOD bounds checks pass. Final ten native loader selections and post-native archives/PE/producer-export hashes pass. All870restored source/resource/doc identities reverified after native sessions. Current pinned restoration tool archives/private agent hashes verified; immutable tools reused read-only, not redownloaded.

Ten freshly executed sequential packaged Linux native sessions: precipitation fresh/reopen with nonzero actual demand control and exact rejected/saved parcels; default channel2 fresh/reopen; opt-in channel5 fresh/reopen with creation toggle OFF before reopen, saved5 retained; aquifer fresh/reopen/verify with two real restarts, depths1/64, Long.MIN_VALUE/bounded step and exact quantities; receiver sevenatomic rejections/fourexact-capacity transfers. Every case requires explicit records, exported outer/nested product identity, RCON save-flush acknowledgment, error precedence, ordered shutdown and exit0. No current build/package/smoke failure. Native metadata/cache initialization occurs before launch; prior audit setup failures are historical, not repeated or counted as current passes.

## Isolation and limits

Independent audit checkout/output/cache/daemon/receipt; previous completed auditor cache moved into this unique private path with no concurrent users. Reclaimed only completed auditor-owned reproducible module outputs and extracted native caches. All prior JARs, source and evidence retained. Builder files/checkout/lease/cache/process untouched; no competing shared mutable output. Initial and pre-native timing observations are completed, explicitly nonexclusive functional checks. Other chat evidence is repository/run receipts, not a claim of live cross-chat synchronization. Audit progress never stops the builder.

Linux AMD EPYC9V74 virtual host,8GiB process budget; game heap1024MiB,Gradle768MiB,one worker,seed20261020,view/simulation2. No /dev/dri or Windows executor. **Not run:** Windows/AMD8060S execution; actual OpenGL/Vulkan client/GPU/pixels/telemetry under camera/reload/reset; controlled chunk/tick/frame/IO/VRAM p50/p95/p99 benchmarks; long travel/edit/unload/multiplayer soak; cross-machine replay; complete world-water ledger and registered saved V8 topology/materialization. Linux server smoke is not Windows/client acceptance. Successful math/functional checks do not prove natural cliff visuals or market-leading performance.

Next: full/native source-quota-budget-save/restart qualification of staged scheduler before promotion; loaded/unloaded single-owner accounting/edits/catch-up; registered V8 saved integration; ecotones/features; natural cliff/LOD temporal scenes and named hardware controlled performance/soak. Auditor remains enabled because fresh complete average>8.5 is not demonstrated. No continuous execution outside actual runs implied.

Main re-read before publication: `2067e273e85fb4f80530c86a7c238106cbbb4cdf`. JAR pin remains `2067e273e85fb4f80530c86a7c238106cbbb4cdf`. Main remained at the pinned SHA in this final read; the audit publication adds only its own report/evidence. Later builder commits are not represented by the JAR.


<!-- END PRESERVED SOURCE -->
