# Expanded replica — docs/validation/engine-continuation/hydrology-incremental-index-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/hydrology-incremental-index-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/hydrology-incremental-index-2026-10-26/README.md |
| Preserved source SHA-256 | `b786da354b6972def7f0669a1ece406863ff0cadc9ee03b3dcb8a1859c087db0` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged incremental hydrology source index replacing repeated whole-index cleanup, copying and sorting after time admission. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that earlier admission guards did not bound the admitted prelude itself; a failed disk compilation is retained separately from the later unique-output API build. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an incremental ordering contract covering stable ties, partial survey registration, bounded maintenance and explicit invalidation events. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare ordering with the original comparator over many mutations, preserve partial-survey timing and separately measure maintenance work under load/unload rather than inferring bounded cost from one deadline check. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Incremental source ordering — staged, 2026-10-26

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Incremental source ordering — staged, 2026-10-26
Base main e056cf942927d355be8fd12e20ba13e423ccffb6; production source remains dev31 a1583a508b0d67130d387bc018defd5758acd496. No new JAR or grade.
Consumed latest completed audit docs/engine-hourly-audits/2026-10-26T1730-a1583a5/REVIEW.md, pin a1583a508b0d67130d387bc018defd5758acd496. The audit independently verified dev31 repairs but left callback/survey/sort bounds, world-ledger ownership, V8, GPU and soak gates open. All ten scores remain carried50/100.

## Implementation
The previous two admission guards prevent starting work after expiry but still scan/copy/sort the complete source index whenever admitted. This candidate replaces per-tick LinkedHashMap cleanup, ArrayList copying and sorting with a server-owned incremental source index: two ordered trees, keyed lookup and descending headwater/stable insertion order. Lifecycle mutations perform logarithmic index maintenance; unchanged ticks allocate an iterator and visit only the admitted prefix. Equal-height replacements retain their sequence, removals remove both index records, invalid heights are rejected before mutation, and returned entries cannot be modified by callers.
HydrologyRuntime integration refreshes index membership/headwater after regular visits and survey progress, removes unloaded chunks, rejects stale cursor identity, and preserves delayed first registration until a survey finishes. An already registered partially surveyed cursor refreshes its priority. The source loop retains per-column deadline checks, headwater ordering and finite quota/accounting paths; regular recharge remains a separate later pass. All new sources remain under stage/, not production.

## Actual validation
Initial complete candidate API compilation passed; first index regression passed20,000 mutations. A later refinement preserves partial-survey registration timing and adds actual-runtime lifecycle tests. Its disk compile failed explicitly with No space left on device; that failure is retained and supersedes any earlier disk compile receipt.
A clean unique /dev/shm output then compiled all four final source files against retained Minecraft/NeoForge APIs with Java25, binary-only source lookup, no implicit compilation. Exit0. Tests run with -Xverify:all:
-20,000 randomized add/update/remove mutations compared after every operation to a LinkedHashMap plus stable descending reference sort; ties, generation replacements, full unload, invalid-height rejection and0/1/17/128/8192-item consumption prefixes pass (20,008 checks).
-Reflection against the actual newly compiled HydrologyRuntime.refreshSource/Work/Cursor passes registration, dry-source removal and stale-generation removal.
See memory-retry.txt for explicit results. JVM perfdata warnings reflect the full disk; no benchmark uses perfdata here. No new full core compilation/suite, Gradle/module build, packaged native session or GPU gate was run this cycle.

## Boundaries and follow-up
This is integrated staged source, not accepted gameplay or an unused-library score increase. The new index must still pass exact source delivery/quota/weather/save/reopen/load-unload integration, stable headwater/tie behavior across actual lifecycle event sequences, complete resource/module/fixture-exclusion/package gates and long travel/edits. Any admitted single callback or survey may overrun; logarithmic tree operations do not prove a hard2ms guarantee or named-hardware latency. No source fairness/world-ledger/catch-up completion claim.
Frozen V1–V7, profiles, owner art, quantities/constituents and renderer production files are untouched. No staged R57/R60/R61/R62 promotion.

## Coordination and storage
Root lease builder-20261026T1758; water foreground checkpointed. Renderer byte-accounting claim scoped to its own docs, functional-only; its concurrent bbf70616cb85cf11d1186afd88106ea8cbfde5b3 checkpoint is preserved. Auditor recorded17:30 completion and its new report was consumed; personal-context search did not establish direct fresh ownership from the originally named other-engine conversation. No other checkout/cache/output was modified. Retained dependencies were read-only; fresh builder1758 and unique memory output were used.
Storage started26MB and reached0. Full isolated build/native/JAR qualification remains unavailable; no prior binary relabeled. Latest qualified builder artifact remains dev31; latest auditor build is separately pinned to the same production source. Keep task enabled and restore isolated build headroom for qualification, then continue world-ledger/V8/client acceptance. No continuous execution claim.


<!-- END PRESERVED SOURCE -->
