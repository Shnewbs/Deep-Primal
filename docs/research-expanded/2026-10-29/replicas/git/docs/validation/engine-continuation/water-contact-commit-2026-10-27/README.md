# Expanded replica — docs/validation/engine-continuation/water-contact-commit-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-contact-commit-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-contact-commit-2026-10-27/README.md |
| Preserved source SHA-256 | `36292d020cb9e47195fff22e73a063eb1e36d1fd725be0d793bd4df66a31507b` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Moving prepared contact parcels into HydrologyRuntime.commitContact so the composition owner validates quantities, constituents, state, adjacency, loaded status and thread. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that a local unload callback reproduction and scoped native diagnostics do not establish all transaction failures or a complete final producer. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a single-owner contact commit contract with revalidation immediately before native reads and after all admission callbacks. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject changed water amounts, stale parcels, vertical gaps and wrong-thread calls, unload during validation and require no unavailable read or partial constituent mutation. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Conservative contact commit and source-index integration

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Conservative contact commit and source-index integration

Source-only `dev.32-contact-candidate.3`; not promoted into the public dev.31
distribution. Base main is `265a0da7467ce516645dba66036bfa6123465127`.

## Implemented behavior

The contact adapter previously wrote both sparse composition entries itself.
It now submits prepared parcels to `HydrologyRuntime.commitContact`, the same
runtime that owns bulk water composition and persistence. The new commit rejects
changes to either cell's individual water quantity, salt/sediment creation or
destruction, stale state/composition, self/non-neighbor pairs, unloaded cells,
vertical air gaps and wrong-thread calls. It also rechecks each chunk immediately
before its native read and rechecks both after validation. An injected unload
callback reproduced an unloaded-neighbor read before this repair; four targeted
callback cases now preserve metadata and perform no unloaded read. The adapter
has the corresponding guard between its two initial block reads. A no-op does not dirty saved data.
Legal commits update both composition entries without a world callback between
writes and dirty the shared saved data once. Explicit zero-salt ocean overrides
remain present, preventing implicit ocean salinity from reappearing.

This combines the staged cursor-ownership/source-index runtime with the exact
diffusion solver, fixed-residency contact budget and existing finite-solver hooks.
Thirteen physical/accounting methods match current production byte-for-byte,
including emit, infiltration, source replenishment/allowance, precipitation,
bulk transfer, ocean exchange, placement and pickup. The new contact method is
the only physical/accounting addition to that staged runtime; index/cursor changes
retain their earlier lifecycle behavior. No geography, saved format, approved
art, renderer/module policy or R57/R60/R61/R62 promotion is included.

## Validation

`checks.json` and raw receipts report final results. The exact new commit/storage
methods are extracted into the narrow native-double fixture; 50,021 cases cover
legal conservative pairs and rejected operations. The negative control removes
the per-cell volume guard and is rejected even when aggregate water is unchanged.
Existing 12,302 contact cases and 9,481,867 budget assertions pass. Helper-only
allocation evidence excludes the adapter, constructor and engine latency.

All six candidate sources compile against Minecraft26.1.2/NeoForge26.1.2.122.
The private Linux fixture uses a real engine-generated world and loads all six
top-level candidate class identities. In one fresh/reopen pair it executes the
source index's six actual-tick finite quota/order cases, five partial-survey cases,
and exact persistent aquifer/outlet quantities, alongside contact mixing across
a chunk boundary, 1,000 repeated admissions capped at 2,048 visits, real solver
ticks, codec round-trip and rejected invalid contact commits. Both fixture markers,
save-all flush acknowledgment, clean shutdown and exit0 are required. Narrowly
listed offline public-key DNS failures do not establish multiplayer acceptance.

Seven private split archives pass CRC/duplicate/private-fixture exclusion and
20 fresh compiled class comparisons. They overlay candidates into the qualified
`2e15d24` baseline and load modules separately. They are **not** a full Gradle
producer, current-main distribution or releasable JAR. They do not include later
renderer telemetry. The independent combined core snapshot includes that renderer
commit (`2e1aa76`) and is validated separately.

An old audit checkout's CoreChecks source had changed. Its old manifest hash was
rejected; the exact current Git blob was used instead. The other138 baseline
sources were individually rehashed before private copying, and the four current
renderer/core test blobs were verified separately. No mutable cache or output
was shared or modified. See `core-source-snapshot.json`.

## Remaining gates and coordination

The latest completed audit at cycle entry was
`docs/engine-hourly-audits/2026-10-27T0230-2e15d24/REVIEW.md`, pinned to
`2e15d246c76b047c57cd8f5ce0b70d9219d3b894`. It carries50/100 (5.0/10), not a new
complete grade. Combined integration and bounded work were chosen from its open
priorities. The next auditor was recorded in progress; the renderer held a
separate nonexclusive telemetry claim. Direct fresh ownership evidence from the
original engine chat was unavailable in Personal Context. Root ownership was
checkpointed before this bounded builder claim.

Both disk and memory-backed storage initially had zero available capacity; a
later temporary opening allowed scoped private checks. The retained isolated
full producer's cache/root/Themes footprint alone previously measured1.3GB.
Full Gradle/build/module/bundled identity and weather/travel/lifecycle qualification
remain open, as do real OpenGL/Vulkan temporal scenes, Windows/AMD8060S,
loaded/unloaded transactional ownership/catch-up, multiplayer/soak and controlled
named-hardware frame/tick latency. No fresh release or completion is claimed.

Next: qualify this combined candidate with a full isolated producer when capacity
permits; extend weather/travel/player-edit and single-owner loaded/unloaded
coverage before broader visual/platform acceptance. The builder remains enabled.

## Reproduction

Restore `stage/` into a new private directory. `make_fixture.py` extracts the
actual commit method into doubles; compile its outputs with WaterAccount,
WaterContactBudget, WaterContactRuntime and the four controlled checks under `checks/`.
Run all check mains with Java25, private temporary/working directories and
`-Xverify:all`. The unchanged full core inputs and exact renderer overrides are
listed in `core-source-snapshot.json`.

`api_compile.py` hashes then copies immutable completed inputs into the owned run
root. `prepare_native.py` and `split_native.py` produce only the private diagnostic;
`run_native.py fresh` followed by `reopen` verifies both fixtures and identities.
Adapt recorded absolute paths only to a new owned root with the same pinned
input hashes. Never use the diagnostic as a user-facing distribution.

## Boundary audit reconciliation

The newly completed `docs/engine-hourly-audits/2026-10-27T0332-2e1aa76/REVIEW.md`,
pinned to `2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e`, was read before publication.
It carries the same50/100, explicitly not a fresh complete regrade. It confirms
production still copies/sorts its source index and lacks the staged cursor guards.
This combined candidate addresses the integration/ownership boundary without
claiming production promotion, loaded/unloaded ledger completion or latency gains.
Its full-core receipt is complete (143 sources, exit0), not the earlier running
receipt mentioned by the auditor. Source and native receipts here are newer scoped
author evidence and do not change that audit's independent score.

A bounded actual1MiB disk write failed ENOSPC; the temporary-memory probe passed.
`capacity-probe.json` records both. The full producer remains unavailable in this
owned workspace; another role's successful producer is not a shared mutable cache.


<!-- END PRESERVED SOURCE -->
