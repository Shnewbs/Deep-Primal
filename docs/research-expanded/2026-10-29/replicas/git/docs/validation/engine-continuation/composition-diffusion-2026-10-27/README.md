# Expanded replica — docs/validation/engine-continuation/composition-diffusion-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/composition-diffusion-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/composition-diffusion-2026-10-27/README.md |
| Preserved source SHA-256 | `951c98044d965df2b4337968b2ce7c44f8aa009d1239354b23017cf8b871fab7` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged WaterAccount.diffuseWith primitive exchanging bounded salt and sediment between connected wet accounts without changing water quantity. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that callers must own both accounts on the server thread and prove connectivity; the primitive alone does not integrate native contacts, refill or advection. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a composition-only exchange contract separating constituent transfer, physical volume, dry deposits, connectivity and authoritative thread ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Assert exact salt and sediment conservation, unchanged volumes and dry deposits, then integrate only bounded adjacent pairs with edit/unload revision checks before packaged persistence qualification. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Bounded composition-only water exchange (staged)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Bounded composition-only water exchange (staged)

Base production snapshot: d83f3943479ab75ac73741d564496e8c4bd92610. Consumed newest completed external audit: docs/engine-hourly-audits/2026-10-26T2333-c5abe45/REVIEW.md, source c5abe457c4c7b36a11307128486c240e0b40d508. All ten scores remain carried at 5.0/10; no fresh complete regrade.

## Implementation

Source candidate dev.32-composition-candidate.1 adds WaterAccount.diffuseWith. It exchanges independently limited salt and sediment between two physically connected wet accounts without changing either water quantity. No advection, refill, source creation or surface-height change occurs. Callers must own both accounts on the server thread and validate connectivity. Dry deposits remain untouched, aliases are no-ops and negative limits reject before mutation.

Signed concentration excess is calculated exactly with bounded-size BigInteger products and divided by combined volume toward zero. This avoids overshoot and order-dependent rounding shuttles. Transfers are capped by each constituent's independent per-call limit, donor mass and receiver mass capacity. No additional saved state is introduced. This is a deterministic primitive, not a physical diffusion coefficient, sediment settling model or native scheduling policy.

## Evidence

JDK25.0.4.1 compiled exact candidate and fixtures. checks.json records all outputs/source SHA256 values.
- 101024 pairwise cases, including maximum valid masses/volumes, empty/dry accounts, saturation, tiny amounts, reverse ordering and independent limits.
- 10000 equilibrium repeats without rounding drift.
- 200000 mixed-network steps across24 accounts, combining diffusion, bulk transfer, evaporation and1575 snapshot reconstructions; exact water/salt/sediment totals checked every step.
- Unmodified current WaterBudgetChecks/Aquifer regressions pass:200000 transfers,10000 precipitation splits,180000 weather intervals,186 aquifer reconstructions, drought/head/baseflow checks.

These are direct Java checks, not full-core/module/native game/save-file/network/long-soak or GPU acceptance. Snapshot reconstruction is not a Minecraft restart. No measured allocation/latency claim; BigInteger work requires profiling before per-cell activation.

To reproduce: compile stage/WaterAccount.java and evidence/DiffusionChecks.java plus DiffusionNetworkChecks.java with JDK25; run both main classes. For the unchanged budget suite, add Aquifer.java and src/coreChecks/.../WaterBudgetChecks.java from the pinned base, plus evidence/BudgetMain.java. Compare source hashes in checks.json.

## Delivery gates and coordination

Production runtime and version remain unchanged; no new builder JAR or GitHub release was made. This source-only candidate is intentionally excluded until bounded native contact integration and full Gradle/module/package/native qualification. Existing staged hydrology index is also still unpromoted. Required water visuals on actual OpenGL/Vulkan, Windows/AMD8060S, loaded/unloaded single-owner persistence, multiplayer/soak and named-hardware latency remain unpassed.

Overlay reported zero available space; shared memory had867MiB free. Used only /dev/shm/dp-builder2400 and read own retained JDK. Insufficient space for isolated full project/dependency/output qualification. No other writer cache/output was modified or deleted; no durable cleanup claim. Latest renderer frame-window claim685a739 is documentation-only and nonexclusive; preserved in publication. Water claim checkpointed. Personal context and automation inventory checked; original other-chat fresh ownership and detailed current run state unavailable. New audit publication was preserved by rebasing the failed initial claim onto e0bcf7c.

Next: integrate only validated adjacent contact pairs with bounded per-tick work and edit/unload revision ownership; run packaged conservation/save/reopen checks alongside staged source index. Then full distributable gates before promotion/delivery. Writer lease released; acceptance remains incomplete.


<!-- END PRESERVED SOURCE -->
