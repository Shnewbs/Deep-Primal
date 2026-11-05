# Expanded replica — docs/validation/engine-continuation/water-cell-write-scope-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-cell-write-scope-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-cell-write-scope-2026-10-27/README.md |
| Preserved source SHA-256 | `ed6e031013eb99696a2c0f948718ed26a25ca2fd4465526ee6793af69e8fff33` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev40 replacing global internal-write suppression with one matching event token keyed by level identity, cell and exact old/requested states on the calling thread. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that indistinguishable same-parameter foreign transitions and off-thread native mutation remain unverified, so scoped suppression is not a durable transaction proof. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a native-event ownership contract for nested tokens, duplicate callbacks, same-cell ABA and unrelated-cell chemistry invalidation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject earlier and later callbacks across cells and levels, require innermost-token matching and finally cleanup, then verify compensation failures and restart accounting independently. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- dev40 — cell- and level-owned finite-water native write exemptions

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# dev40 — cell- and level-owned finite-water native write exemptions

**Consumed independent audit:** [2026-10-27 23:32 UTC](../../../engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md), pinned production source `a14721c72625ea15ad9cb9168a6140c3f6882005`. The external auditor modeled unrelated-cell/same-volume chemistry being retained while a global process-wide `internalWrites` flag hid callback edits; no native NeoForge data-loss case was claimed. Historical external full-engine grade is **50/100 = 5.0/10**, not freshly regraded.

**Builder scope/version:** `0.0.1-alpha.29-dev.40-waterwritescope.1`. Current production `HydrologyRuntime.write` uses a single-event native write token keyed by **reference-identical ServerLevel, packed BlockPos, exact old BlockState and exact requested BlockState**. `HydrologyRuntime.externalChange` suppresses only the first matching event on the active calling thread. It no longer drops **all** foreign edits while another cell is being written. Same-cell foreign transitions, same-world other cells, other ServerLevels and duplicate events are allowed through to existing chemistry invalidation. Cross-thread events are not exempted by the helper if forwarded, but the current LevelChunk mixin forwards only server-thread events; off-thread mutation behavior remains unverified. Nested same-cell scopes select the innermost token. Try/finally unwinds tokens on both success and exception; a token exists only while the native write is active, and thread-local storage is removed after the last exit.

The existing quantity/ocean-marker invalidation contract, `contents` parcel initialization, `WaterTransferCommit` post-receiver source+receiver barriers, and full V1-V7 frozen geography/saved profiles/player edits are unchanged. This does **not** make transient source/receiver exceptions or failed compensation automatically conservative. A same-parameter foreign callback transition can be indistinguishable from the native author's own transition until the event source is instrumented. Exact once and persisted ownership remain native acceptance gates.

## Executed, isolated Java 21 source checks

- Exact `WaterWriteScope.java` bytes: Git blob `79c537fbb6fdd5ae169448c324b09b7137ee6bca`, SHA-256 `7e210a9d8dc72d7c60aa5a48162685293d56706f267c65062de06daa1c168525`. Test source Git blob `9d7ac6a66f4af745caabab6d6f39c4351c33be96`. Both were compiled from the exact bytes now committed.
- `javac --release 21 -Xlint:all -d classes WaterWriteScope.java WaterWriteScopeChecks.java` and `java -Xverify:all -cp classes com.shnewbs.deepprimal.engine.WaterWriteScopeChecks`: **PASS, 480,028 checks**, no compiler warnings. Covers nested same-cell/other-cell, world identity, thread isolation, exception and bad-close recovery, one-event consumption, same-volume chemistry reset model, signed positions, and 120,000 randomized ownership and cleanup trials. The test is also invoked by `CoreChecks.main` during a full owner build.
- **Four deliberately corrupted implementations rejected** (nonzero test exit): global write suppression, cross-world exemption, repeat-event exemption and same-cell outer-token fallthrough.
- Connector-backed production integration: **nine source structural assertions** passed, and **three corrupted integration-source variants** were rejected. They check scoped begin/consume/unwind, loaded admission/no-op handling, retained composition invalidation, `CoreChecks` registration and version increment. These are static guards, **not** JVM-compiled `HydrologyRuntime` or NeoForge acceptance.
- The previous renderer job was observed separately claiming `lod-engine/` and `docs/renderer-jams/` only; its subsequent commit did not overlap these builder files. The foreground water claim is checkpointed with no active expiry. The writer used guarded main ref updates and did not stop other roles.

## Unrun essential gates and follow-up

The owner (October 27 directive) performs complete Java25/Gradle/Minecraft/GPU builds. JDK25/archive were absent from this execution-local workspace; shell GitHub clone failed DNS; authenticated GitHub connector provided pinned committed source. **Not run:** full Java25/Gradle/module/core suite, native LevelChunk notification order and reentrant callback injections, volume/salt/sediment save/reopen/rollback/level-unload, multiplayer long soak, GPU Vulkan/OpenGL connected waterfalls, AMD8060S frame/tick p50/p95/p99, JAR packaging/CRC and Windows execution. No JAR, GitHub Release, CurseForge, stable label or new external grade is claimed.

**Next builder priority:** Native event-injection on the exact write source (including same-cell ABA, earlier/later callbacks, unload, failed native write and compensation), then persistent single-owner constituent transaction/recovery records; only then can conservative volume/salt/sediment under restart and multiplayer be claimed. Continue live cliff temporal pixels and bounded performance acceptance in the engine/water schedule, respecting the dedicated renderer's ownership.


<!-- END PRESERVED SOURCE -->
