# Expanded replica — docs/ENGINE-HOURLY-BUILDS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/ENGINE-HOURLY-BUILDS.md) |
| Source identity | git: docs/ENGINE-HOURLY-BUILDS.md |
| Preserved source SHA-256 | `78fe54443ef9d6f469859d9fcb9c474432ad365d476a3579dfde8c704a63165c` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Engine-only build cycles”. Its original opening states: “Owner authorization: October 25, 2026. This current instruction supersedes all older owner pauses, the 8.0/inclusive-8.5 targets and auditor-only build-delivery restrictions. The owner explicitly resumes the existing builder and authorizes direct commits/pushes to main and qualified development testing releases.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Separate historical cycle instructions, active ownership, evidence publication, and runtime acceptance; this replica does not start or reconfigure a task.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Reference an explicit policy revision and owner, then record claim scope, expiry, expected source parent, and completed evidence without inheriting stale status text.
- Keep source-only documentation publication independent from build/release qualification and avoid treating a completed checkpoint as continuous work.
- Replace the need to infer current status from append order with a derived status view keyed by source commit, subsystem, saved revision, evidence scope, and accepted artifact. Keep the long original ledger intact.
- Link each repaired/staged/superseded item to the exact later finding or acceptance record. The final Dev77 source property, earlier Dev69 checkpoint, Dev32 overview, and Dev53 root work-state checkpoint belong to different provenance rows.
- Maintain a cross-system ownership table for finite-water transactions, world-generation revisions, GPU publication, and asynchronous persistence so a successful local fix cannot silently close a wider requirement.

## Research expansion — acceptance checks

- Simulate concurrent commits and verify a guarded publication preserves the newer work instead of overwriting it.
- Reject stale or expired ownership records and ensure a docs-only change cannot alter an engine grade, build status, or scheduler.
- Query one finding across its first failure, staged fix, scoped pass, and later native acceptance; report missing transitions explicitly.
- Run a documentation consistency check that detects mismatched version declarations without modifying historical outcomes or active ownership records.

## Research expansion — source anchors

- Engine-only build cycles
- Cycle contract

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Engine-only build cycles

Owner authorization: October 25, 2026. This current instruction supersedes all older owner pauses, the 8.0/inclusive-8.5 targets and auditor-only build-delivery restrictions. The owner explicitly resumes the existing builder and authorizes direct commits/pushes to main and qualified development testing releases.

- Builder: `6ad974bdc204819195e5465aeea9dca2`; existing conversation `6ad9825d-74c8-83e8-b01c-24bb40d74bd8`.
- Independent graded auditor: `6adcfe07f3888191aa478160f40027da`; existing conversation `6adbf49e-ce88-83e8-aefd-55bac7c067aa`.
- Cadence: hourly, America/Los_Angeles. Reuse these tasks; do not create overlapping builders or duplicate audits.
- Scope: the Deep Primal built-in engine and engine mechanics only.
- Stop condition: a fresh complete independent audit with the equally weighted ten-category overall average **strictly greater than 8.5/10**. Exactly 8.5 does not pass. Last full verified baseline remains 50/100 (5.0/10); this policy does not regrade anything.

## Cycle contract

1. Run `python3 tools/validate_engine_work_state.py` on the pinned work-state before interpreting or claiming its lease; run it again before publishing state edits. It rejects duplicate keys and misplaced ownership fields but does not replace time/ownership/ref checks. Read current main, ENGINE-WORK-STATE.json, the latest completed external audit and its source identity, ACTIVE-ENGINE-AUDIT, ENGINE-BUILD-AUDIT-HANDOFF and engine roadmaps. Check the other engine chat/run records, Git ownership/leases, commits and active timing reservations. Record unavailable or stale evidence honestly. Avoid any active writer's files/resources; do not overwrite concurrent changes.
2. Claim and refresh a bounded writer lease on main. Work on the latest audit's highest-impact unfinished engine findings and consume each new completed audit in the next cycle. Fall back to roadmap gates where the audit gives no new work. Record the consumed audit identity, changes, evidence and next action.
3. Implement and validate actual engine work. Terrain/density, climate/ecotones, geology/material mechanics, drainage/caves/aquifers, finite liquids/chemistry, graphics/culling/LOD/Vulkan, scheduling/caches, performance and saved/profile lifecycle are in scope. Generation feature/structure mechanisms and engine config/tooling are in scope when required by the engine audit/roadmap. Unrelated gameplay, GUI redesign, catalogs, other mods/ports and standalone-game work are excluded.
4. Preserve owner-approved art, player edits, saved profiles, frozen V1-V7 geography, finite water/constituents and separate engine/graphics/performance/LOD/Vulkan modules. Geography changes require V8 and its gates. Current main includes finalized alpha.29-dev.19; older audit dev.3 snapshots must not overwrite it. Do not automatically promote staged R57/R60/R61/R62. Use complete-family texture previews when requesting approval for engine-required new art, and continue independent engine work.
5. Run meaningful affected checks plus the full available build/core/module gates. Validate final JAR/nested archives, platform payloads, fixture exclusion, compiled/package/source identity, producer completion, size and SHA256. Run relevant packaged native/save/reopen/shutdown/graphics gates and explicitly identify unavailable Windows/AMD acceptance. Failed/corrupt builds do not ship.
6. Commit all intended validated engine code and compact evidence directly to main using a fresh expected-parent/ref guard. Re-read/reconcile main before retrying a changed parent. Do not force-push or discard other work. Build a uniquely versioned testing JAR from the exact pushed source.
7. Publish qualified cycle builds as GitHub development/prerelease releases and send the verified release/JAR download link. Save the user-facing testing JAR through the Library skill as well. This authorization does not label builds stable or authorize CurseForge promotion. Verify publishing and bytes before claiming success. If a gate or publisher blocks delivery, retain resumable source/evidence, state the actual blocker and provide only genuinely qualified artifacts.
8. At the run boundary record audit/source/build identity, actual fixes, pass/fail and omitted checks, last external grade, next engine priority and release/expire the lease. Continue useful implementation during active runtime; a report/audit is not a stop signal. Scheduled execution is finite and is not an always-running process between cycles.

## Independent auditor

Keep the existing independent grader and its rubric. It may read/build pinned committed snapshots using separate checkout/output/cache and its independent receipt, without taking over or interrupting the builder. Avoid shared timed measurement contention. Its existing audit JAR policy remains; the builder additionally owns the newly authorized end-of-cycle release/delivery. Both targets now use strictly greater than 8.5/10. Grade evidence, not plans or unused/staged libraries.

## Resumption checks

At 2026-10-25T17:29:07.574Z, main was `d247f1a6a46bdca3045091c3b415f3de9b510115`. The other engine conversation reported paused builder/active auditor; the automation inventory independently confirmed those states. The main writer lease had expired, its timed measurement was completed, and no Deep Primal Java/Gradle process was observed. These are point-in-time checks and must be repeated by each cycle. Historical dev.19 limitations and the 5.0 baseline remain open; no engine code/build/release is claimed by this coordination change.

## Water-engine continuation

Latest owner direction, October 25, 2026: continue the water engine alongside the
broader engine until it is state of the art and done. Reuse the existing builder
and independent auditor. Keep the builder enabled while either the original
strictly-greater-than-8.5 engine threshold or the recorded water acceptance gates
remains incomplete. A successful bug fix, JAR or partial test does not close the
water engine. Read WATER-ENGINE-WORK-STATE.json for scoped ownership and evidence;
continue actual implementation of conservation, connected flows/waterfalls,
seasonal/coarse-to-fine accounts, save/restart/edits/unloading, multiplayer/soak
and measured GPU/frame/tick/memory gates. Preserve the original rubric and honest
finite-run reporting; no claim of uninterrupted execution between scheduled runs.


<!-- END PRESERVED SOURCE -->
