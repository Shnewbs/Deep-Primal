# Expanded replica — docs/validation/engine-continuation/water-receiver-postcommit-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-receiver-postcommit-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-receiver-postcommit-2026-10-27/README.md |
| Preserved source SHA-256 | `952e6e4bba2f91b4700c195f265cbbf4485eec25d438ec84bb7befd82f742497` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev39 post-receiver-write barriers rechecking both loaded source and receiver state before finite-water transfer can report chemistry success. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that modeled callback rejection does not establish end-to-end conservation when foreign edits or failed rollback leave unresolved native state. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a persistent single-owner transfer and compensation journal proposal that records unresolved ownership instead of equating thrown exceptions with conservation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Mutate or unload either cell during every write and rollback callback, compare native water and constituent totals and verify recovery behavior after actual save/reopen. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- dev39 — post-receiver-write conservative finite-water commit barrier

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# dev39 — post-receiver-write conservative finite-water commit barrier

**Production commit:** [`c969ace0dc1c236fcb371e86d121b478fbf7688c`](https://github.com/Shnewbs/Deep-Primal/commit/c969ace0dc1c236fcb371e86d121b478fbf7688c)  
**Development version:** `0.0.1-alpha.29-dev.39-waterpostwrite.1`  
**Consumed independent audit:** [2026-10-27 22:31 UTC](../../../engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md), audited source `227f53b7a95e66028831f74102f011f46125643d`. That review identified absent post-receiver-write authority checks and three modeled reentrant callback cases. It was **not** a full independent regrade. Carried grade: **50/100 = 5.0/10**, target strictly greater than 8.5 and complete water gates still open.

## Production changes

- `WaterRuntime.transfer` now checks **both** loaded source/receiver states after the receiver native write, and after resource-drop callbacks, before recording water/salt/sediment chemistry or incrementing transfer diagnostics. A native `setBlock` return alone is no longer accepted as proof of ownership.
- `WaterTransferCommit.afterReceiverWrite` is a dependency-free four-outcome decision policy: `COMMIT`, `ROLLBACK_SOURCE`, `ROLLBACK_BOTH`, `LOST_OWNERSHIP`. Only an observed, still-owned receiver can commit; an unloaded/replaced source or receiver throws rather than falsely reporting success.
- Compensating a receiver that was actually written despite a false return restores **receiver before source**, avoiding a refund while credited destination water remains. Each rollback uses exact current native-state equality and verifies the resulting block. Never overwrite an intervening player/callback edit or force-load an unloaded chunk. If no safe compensation exists, throw an explicit ownership-loss exception instead of claiming conserved success.
- If the source native write returned false after changing state, the transfer now detects and safely restores only its authored state, or throws on foreign replacement. Native chemistry is not committed on any rejected transfer.
- Old V1–V7 geography, saved profiles, art, hydrology/chemistry limits, unrelated modules, renderer, water surface presentation and staged R57/R60/R61/R62 remain untouched.

## Focused checks performed on October 27, 2026

- Actual committed **production policy helper** blob `42a5223bd475bbeb73bf3936abe3164e788c1f18` matches `git hash-object` of the tested local `WaterTransferCommit.java` byte for byte.
- `javac --release 21 -Xlint:all -d classes WaterTransferCommit.java WaterTransferCommitChecks.java` and `java -Xverify:all -cp classes com.shnewbs.deepprimal.engine.WaterTransferCommitChecks`: **PASS, 200,065 focused checks** including truth table, 16 source/receiver callback scenarios, exact modeled water/salt/sediment balance and 200,000 deterministic random policy cases.
- Three corrupted policy mutations (`allow_lost_source`, `ignore_receiver_owner`, `ignore_rollback_both`) all **rejected** by the same suite with nonzero exit. These variants were never promoted.
- Connector-backed review of the committed `WaterRuntime.java` integration: **20 source-order/static assertions**, plus **five negative source mutations rejected**. This is a structural safety probe, not production JVM verification or control-flow proof.
- Evidence ZIP: `DeepPrimal_Dev39_20261027_Water_PostWrite_SourceEvidence.zip`, **4,954 bytes**, SHA-256 `41fe2bcaab4545be0bbe8cca58f43ad1832cf8c18fab83aa426405b196c6b4d4`; seven entries, archive CRC verified. The ZIP is a test/evidence archive, **not a Minecraft JAR**.

## Open acceptance and risks

- The native `WaterRuntime` class, `HydrologyRuntime` integrations, NeoForge callback/reentrancy, actual reload/unload rollback, source/receiver ABA ownership, constituent journal persistence, save/reopen and multiplayer/long-soak are **not yet qualified**. A synchronous callback that replaces cells can still create an exceptional/unresolved external edit; throwing and refusing chemistry success is **not** proof of end-to-end quantity/constituent preservation.
- Java/Javac here were **21.0.12.1**. The owner-supplied Java25 archive/restored directory were not present in this workspace; shell GitHub access failed DNS. Full Java25 Gradle, NeoForge, module/core compilation, JAR/nested CRC validation, native package, Windows/AMD8060S, Vulkan/OpenGL waterfall pixels and p50/p95/p99 frame/tick measurements were **not executed**, in accordance with the October 27 owner-build handoff.
- **No qualified JAR, GitHub development release, CurseForge upload, independent regrade or completed water-engine claim.** The separate auditor remains responsible for the grade; this is builder validation only.

**Next:** Owner native event-injection tests for callback mutation/unload during **each** write and during rollback, with exact native water/salt/sediment before/after checks and save/reopen; design a persistent single-owner transaction/compensation journal for unresolved foreign-ownership cases. Then test natural cliff/waterfall temporal rock-contact seams on real Vulkan/OpenGL and named-hardware tick/frame tails.


<!-- END PRESERVED SOURCE -->
