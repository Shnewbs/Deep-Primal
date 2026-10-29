# Expanded replica — docs/engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md |
| Preserved source SHA-256 | `59747f45550519c46f3a94e0f6dee309639d57865fdc92752c241466b9688e94` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent Deep Primal code audit — 2026-10-27 22:31 UTC”. Its original opening states: “Pinned engine source: 227f53b7a95e66028831f74102f011f46125643d (dev38; parent code commit 816aab2b2150ff5463253f2d5636d41970914740). New finding, not a complete native regrade. No production code or other role's checkout/schedules changed. The owner retains full Java25/Gradle/Minecraft/GPU build responsibility under the October 27 override.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `HydrologyRuntime.transferred`.

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

- Independent Deep Primal code audit — 2026-10-27 22:31 UTC
- New finding: no post-receiver-write ownership check
- Receipt and limitations

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent Deep Primal code audit — 2026-10-27 22:31 UTC

**Pinned engine source:** `227f53b7a95e66028831f74102f011f46125643d` (dev38; parent code commit `816aab2b2150ff5463253f2d5636d41970914740`). **New finding, not a complete native regrade.** No production code or other role's checkout/schedules changed. The owner retains full Java25/Gradle/Minecraft/GPU build responsibility under the October 27 override.

## New finding: no post-receiver-write ownership check

At [WaterRuntime.java lines 210–253](https://github.com/Shnewbs/Deep-Primal/blob/227f53b7a95e66028831f74102f011f46125643d/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L210-L253), after a source write the new checks correctly verify the source at lines 233–235 and the receiver **before** writing it at lines 236–238. On successful `HydrologyRuntime.write(l,b,...)`, execution proceeds straight through `dropResources` to `HydrologyRuntime.transferred` at line 249 **without rechecking either cell's loaded/authoritative state**. [HydrologyRuntime.java lines 72–90](https://github.com/Shnewbs/Deep-Primal/blob/227f53b7a95e66028831f74102f011f46125643d/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L72-L90) performs native synchronous `setBlock` and suppresses external chemistry invalidation while `internalWrites != 0`. If callback edits/unloads can occur during the receiver write, success/chemistry can be recorded against replaced state. The accepted dev38 [isolated model](https://github.com/Shnewbs/Deep-Primal/blob/227f53b7a95e66028831f74102f011f46125643d/docs/validation/engine-continuation/guard-dominance-water-2026-10-27/WaterReceiverCommitModel.java) tested mutation during the *source* write, not *receiver* write.

**Independent isolated Java21 model:** `javac --release 21 -Xlint:all ReceiverAfterWriteReentrancyProbe.java && java -Xverify:all ReceiverAfterWriteReentrancyProbe` — 5/5 scenarios. Normal transfer and dev38 receiver pre-write protection behave correctly. Three **new conditional** cases (callback changes destination, changes source, or unloads destination after the native receiver write) still report success/chemistry commit. This fixture is a source-order model, **not compiled production Minecraft**. Native `setBlock(flags=2|16)` callback behavior, saved-world corruption, and device effects remain unverified. Do not represent this as proven production data loss.

**Repair/acceptance:** validate both cells after the receiver write, design explicit transaction/compensation semantics for a failed postwrite validation without clobbering newer player edits or leaking finite source quantity, and test callback-induced source/destination replacement/unload in real NeoForge. Verify water/salt/sediment conservation and persisted save/reopen/long soak. The retained high-priority water acceptance gates remain open.

## Full rubric — carried, NOT regraded

| Original category | /10 | Unrun/unfinished gate |
|---|---:|---|
| Terrain realism | 5 | General V8 3D density/visual acceptance |
| Biome blending/coherence | 4 | Weighted ecotones |
| Hydrology and caves | 6 | Native loaded/unloaded, chemistry rollback, temporal waterfall |
| Features/structures | 4 | General spacing/exclusion pipeline |
| Performance/threading | 3 | Historical ~14.45–14.99 ms warm fill vs 5 ms target, current p95/p99 absent |
| Determinism/stability | 7 | Cross-host replay, long-soak/edit matrix |
| Architecture/code quality | 6 | Current full module build unrun |
| Extensibility/data-driven | 4 | Complete stage profile/API |
| Tests/benchmarks/docs | 7 | NeoForge event-injection and owner GPU matrix unrun |
| Polish/config/tooling | 4 | Water/LOD visual acceptance, performance tails |
| **Historical total** | **50/100 = 5.0/10, Tier 2** | **Strictly >8.5 acceptance NOT MET** |

Original [10-category grading](https://github.com/Shnewbs/Deep-Primal/blob/227f53b7a95e66028831f74102f011f46125643d/docs/engine-review-2026-10-24/REVIEW.md) and [last independent review](https://github.com/Shnewbs/Deep-Primal/blob/227f53b7a95e66028831f74102f011f46125643d/docs/engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md) supply historical scores. The dev38 guard-shape fix is present; the builder's 29 Python and 274,989 Java21 modeled assertions were **not** independently rerun.

## Receipt and limitations

Fresh read-only pinned GitHub source inspection. Production blob identities: WaterRuntime `f1a073024f16b3219df0d030d2cc5b235126edfb`, HydrologyRuntime `29072f52cef0ba70a199f7a8d83e3d4d4d61ca7f`, guard-validator `8fb090bd7c8e8b8988ed4783feba13d9914aeb1b`. Independent Java21 model compiled and ran with `-Xverify:all`, 5/5 cases; the original local fixture SHA256 is `74ba389154fdde40f3bb0c7dabebe33bc357c98efdd198f8ff7a665a332c8756`. Local audit/evidence ZIP SHA256 `5fc54a1c7a89796cc7bc2ce40c0e9c7667ff74f5a324bfa410bbb8d8d4c16db2`. Java25 and its user-provided archive were absent in this execution workspace, GitHub DNS failed for shell clone; GitHub connector worked. No full checkout, native/JAR/CRC, save/reopen, GPU pixels, AMD8060S, benchmark, release or regrade claimed. No grade changes, no scheduler changes. Owner handles full builds; independent auditor remains enabled.


<!-- END PRESERVED SOURCE -->
