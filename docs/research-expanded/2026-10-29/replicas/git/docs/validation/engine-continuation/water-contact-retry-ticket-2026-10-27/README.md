# Expanded replica — docs/validation/engine-continuation/water-contact-retry-ticket-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-contact-retry-ticket-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-contact-retry-ticket-2026-10-27/README.md |
| Preserved source SHA-256 | `751ddc5d23d1238793e26c782d4ff2172e7c9cb7b34b0b97ee5fdf188c173063` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Retaining a next-tick native fluid ticket after failed contact commit or exhausted budgets because same-tick FIFO reactivation could be consumed as DUPLICATE. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the fifteen API-stub assertions do not prove actual scheduler behavior, and removed or unloaded origins must not be force-loaded or resurrected. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a retry-epoch contract separating deduplication success from admission attempts and requiring a surviving next-tick wake for still-loaded water. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject a commit then drain the current FIFO in the same tick, require one later retry after epoch rotation and verify unloaded origins defer to normal load/wake ownership. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deferred water-contact retry survives the same-tick FIFO (2026-10-27)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deferred water-contact retry survives the same-tick FIFO (2026-10-27)

**Staged source only, not in the shipped Minecraft runtime.** Derived from `water-contact-probe-budget-2026-10-27/stage/WaterContactRuntime.java`; only the contact adapter is modified. Earlier staged `WaterContactBudget`, `WaterAccount`, `WaterRuntime`, `HydrologyRuntime` and `StreamSourceIndex` remain byte-identical and are not silently promoted. The exact proposed adapter is `stage/WaterContactRuntime.java` here.

## Correctness defect repaired

The earlier adapter marked a contact pair FIRST in the per-tick dedup table **before** the actual `HydrologyRuntime.commitContact`. When that commit rejected an unloaded or stale pair, it only reactivated the origin in the current finite solver FIFO. That reactivation can be processed **in the same game tick** while the edge is still DUPLICATE, losing a future retry. Global visit or collision-probe exhaustion also merely reactivated the current FIFO. The new adapter retains bounded `WaterRuntime.activate` but also requests a **one-tick native fluid scheduler ticket** for a still-loaded water origin on any of those three deferrals. Next-tick rotation forgets the failed edge and tries again; unloaded/removed origins are left to chunk-load/wake handling instead of force-loading or resurrecting water. No new water, salt or sediment; no simulation catch-up, geography changes, art or renderer changes.

## Checks performed

- Reproduced the core adapter with JDK 21 **API stubs** and compiled all required types with `javac`; `java -Xverify:all` passed **15/15 named assertions**, including rejected metadata commit, same-tick dedup, next-tick retry, exact before/after water/salt/sediment amounts, admission cap, forced-probe deferral, removed/unloaded origin and server-thread guard. See `evidence.json` for local evidence artifact identity.
- Previous stage's `CONTACT_PROBE_BUDGET_PASS cases=1773608` is **inherited**, not freshly rerun here.
- This is an **API-stubbed regression**, not full Minecraft/NeoForge validation. Java25/Gradle full producer and native packaged cold/reopen/solver/scheduler/long-soak checks are unrun. Linux Vulkan/OpenGL and named Windows AMD8060S GPU and tick/frame benchmarks are unrun. Do not release a new JAR based on this evidence alone.

## Handoff

1. Restore Java 25, native dependencies and full pinned source. Integrate all six staged classes plus this newer adapter in a private copy, then full Gradle/core/module/package checks with exact class and archive identities.
2. Confirm with actual Minecraft scheduled fluid tick that denied/rejected contacts remain live into the next host tick and that no uncontrolled ticket storm appears under dense lake contacts; run chunk unload/reload, player edit and save/reopen tests.
3. Then test multi-chunk connected surface/GPU acceptance and measured tail budgets. External audit `docs/engine-hourly-audits/2026-10-27T1634-bd28005/REVIEW.md` pins `bd28005d7c8703c7abdd822634cb4a07875d2038` and carries **50/100, 5.0/10**, not a fresh regrade. Acceptance requires external grade >8.5 plus all water gates.

Saved portable Java21 stub harness/evidence: Library `libfile_8602f2a5eea8819185425ac7f4eb2297`, 8,506 bytes SHA256 `bcce704295f8de1a7cf539821b596c1c1f343613f2165aff057cc4e7de5549b9`. The staged adapter Git blob is `75d1a25521daa93274ebc5747c419190338e659c`, identical to the locally compiled source blob. Neither is a native game build.


<!-- END PRESERVED SOURCE -->
