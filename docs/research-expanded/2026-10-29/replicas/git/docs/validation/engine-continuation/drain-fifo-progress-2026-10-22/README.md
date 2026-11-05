# Expanded replica — docs/validation/engine-continuation/drain-fifo-progress-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-fifo-progress-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/drain-fifo-progress-2026-10-22/README.md |
| Preserved source SHA-256 | `76c4fb07a88c94e3a1eaa41be3c8fbe5d871f2c8a2954269ce0e04c0ae4d83cf` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The drain FIFO candidate addressing one-unit-film starvation while retaining 16 resident jobs, 8,192 nodes per job and a bounded temporary witness. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the film regression is fixed but larger settling still fails, and docs-only staged runtime evidence is not accepted production source. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a fair retry and temporary-resource contract accounting for the extra witness, chunk-watch release and total per-host-tick work. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Fill all resident slots, enqueue film requests and unavailable boundaries, verify FIFO progress and finally-block cleanup, then require complete settling and zero idle work before integration. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Drain request FIFO: film starvation fixed, larger settling gate still fails

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Drain request FIFO: film starvation fixed, larger settling gate still fails

Fresh 2026-10-22 continuation after the workspace reset. Working ref:
`work/engine-after-alpha24`. Accepted production solver remains
`2c431bb9a4cb922dae5b441c29676ad9d9a6bc5a`. This checkpoint preserves source and
evidence **under docs only**; it does not integrate the unaccepted drain runtime,
modify main, release an installer or change published V1/V2/frozen V3 geography.

## Reproduction and focused change

The inherited [ticket-readiness candidate](../drain-ticket-readiness-2026-10-22/README.md)
fails the existing native filling check: eight one-unit film cells remain at their
origins and the lower outlet stays empty after 120 ticks. Fresh validation-only
reflection tracing confirms the 1,024 competing settled cells repeatedly fill the
16 retained-drain slots; the eight live cells remain active without a resident
search. Their quantity stays exact. See `profile-before` in `native-results.json`
and the actual job origins/frontiers in `native-excerpts.txt`.

The independently authored scheduling patch gives drain requests their own packed,
coalesced FIFO. Unlike cycling and requeueing the entire active list, its head
persists when a visit quota ends. Each ordinary host tick allows at most 3,072
first-turn visits, within the unchanged 6,144 total drain-visit ceiling. Retained
searches receive a separate fair turn allowance. The FIFO caps at 65,536 keys;
duplicates are checked before capacity, and overflow retains an active or native
scheduled wake. No quantity is represented by or discarded with a work entry.

The 16 retained jobs and 8,192 nodes/job stay unchanged. A new request can still
test a nearby outlet when those slots are full: one temporary witness receives at
most 128 visits, then releases all its chunk watches in `finally`. Unfinished work
returns to the drain FIFO. That one temporary witness can admit at most 517 seen
positions (origin + four initial neighbors + at most four discoveries/visit), in
addition to the resident bound. Missing-chunk waits retain their prior capped
index. Per-host assertions check visit, queue, readiness, watch-link and residency
bounds; filling also verifies every remaining chunk watcher belongs to a resident
job, catching temporary-witness leaks.

Transfer, drain-path, pressure, write-invalidation and chunk-event bodies remain
unchanged relative to the ticket candidate; see exact body hashes in
`unchanged-transport-bodies.json`. Scheduling order does change, so this is not a
claim of general output equivalence or performance improvement.

The final candidate is `fifo-WaterRuntime.java`, SHA-256
`8ca0d657c403d4a65118202ad34cd9e6ffa0aee5ff5f125861f98e9a41ae3ee8`.
The initial safety batch used SHA
`26211cdd4156cbd798936ab1cfbeea9e8e500df4fa798bb840b950b95da37411`.
Only one FIFO comment was normalized afterward; all 104 compiled main-class
digests are identical. The canonical source then passed the full build and
packaged/loaded-idle checks. See `canonical-source-identity.json`.

## Actual validation

Passes require an explicit record, no assertion/crash and normal save/shutdown.
Raw records include failures as well as passes. `traced_source_sha256` is the
runner's legacy field name for the input runtime hash, even with tracing disabled.
Headless servers produced no client screenshots. Assertion stacks were inspected
and preserved in `inspected-crashes.json`.

| Gate | Result and limit |
| --- | --- |
| Original film reproduction | FAIL on inherited ticket source: outlet zero at 120 ticks, exact eight units retained |
| FIFO film repair | PASS: outlet seven at tick 43, eight at tick 44; original 120-tick assertions, three-substep falling, grass, consecutive basin fill, interior wake and conservation all pass |
| Temporary-watch and budget assertions | PASS each host tick in the filling replay: resident-only watches after service, at most 128 visits/turn, 3,072 admission and 6,144 total drain visits/host, 65,536 pending keys, existing readiness/wait/residency caps |
| Closed routes, edits, cached reload, missing chunks, reopen | PASS: exact quantities each tick, no-force gaps, player wall edits preserved, settled loaded routes release all work/watches and pass 21 idle samples |
| Actual physical unload/reload | PASS: observed ChunkEvent.Unload before reload, 1,025-cell route, exact two units, zero final pending/watches, 21 idle samples at the unchanged 3,200-tick deadline |
| Long pressure/head/edit/unload/idle/reopen and short pressure | PASS: expected 4,190/4,192 and 144-unit controls; residency saturation still occurs in larger searches |
| Active FIFO contract | PASS: 65,536 cells/262,144 admissions, order, mutable coordinates, negative coordinates, duplicate-at-capacity and scheduled overflow |
| New drain FIFO contract | PASS: 65,536 keys, packed mutable/negative snapshots, order, duplicates, active overflow and scheduled wake when both queues are full |
| Fully loaded pool idle/edit | PASS: 1,024 pool units and eight film units conserved every tick; late film completes by 120, all work clears, 20 idle samples precede ordinary floor edit; edited sink fills to 16 and final pending returns to zero at 480 |
| Larger closed stepped basin | **FAIL** at 3,200 ticks: all 8,192 units conserved, upper=368, bottom=7,697, intermediate=127, pending=681. No repeat B after failure |
| Full default non-Vulkan build | PASS: CoreChecks, nine-seed/frozen-geography checks and core/module boundaries |
| Final development archive | PASS: outer/four nested ZIP CRCs; 104 main + 58 core + 37 graphics settings + 13 LOD + 14 performance classes match current compiled bytes (226 total); no validation/private-agent payload |
| Canonical packaged filling/reload | PASS: same film completion at 44 and correct reload quantity/FLOW digests; archive bytes unchanged after native checks |

The sequential safety batch has 17 passing cases, followed by the loaded-pool and
two packaged passes. Seven closed-route quantity/FLOW digest pairs match the prior
ticket candidate exactly; see `matched-output-controls.json`. That comparison does
not cover general basin output, latency, resident memory or owner hardware.

**Integration remains blocked.** The original unpinned filling world still has
1,024 pending requests/waits at tick 120, despite the repaired live film. The
larger basin remains active and fails complete settling. Minecraft again crashes
on that assertion while Gradle returns zero. Do not turn a targeted film pass,
correct ZIPs or an exit code into acceptance of the full retained-drain solver.

Earlier failed attempts are also retained: quiet-component coalescing did not
resolve the unpinned fixture, and a bounded probe lane without a separate FIFO
still let the active-list prefix consume each phase's quota. Neither attempt is
in the final runtime. Two validation-only compile errors (a shadowed local and an
incorrect shutdown accessor on LevelTickEvent.Post) were corrected before their
later native runs. Those initial build failures are recorded as failures, not
native executions or solver defects.

No new active-queue allocation improvement is claimed: the first warmed admission
counter was 80,560 bytes, versus 304 in the earlier checkpoint. A separate fresh-process repeat measured 304 bytes; both raw records
are retained; total solver allocation/memory and broad performance remain
unaccepted. Three solver substeps in a 20 Hz host are not independent 60 Hz
simulation. No fresh visual, Vulkan, enabled-view coverage or owner GPU/FPS
validation ran. The known environment blocker for those checks remains separate
from the native server environment, which worked here.

## Review and reproduce

Use a disposable experiment root with a `project/` source copy. The private tools
are the previously checksum-verified JDK25/JDK21/Gradle toolchain; ordinary hosts
can use their installed tools/wrapper. Never apply these experiments to an active
checkout or supplied installer. `isolation-source-audit.json` records the copy and
changed inputs; default generator/renderer/module inputs remain unchanged.

1. Copy the prior report's `ticket-WaterRuntime.java` into the experiment root.
   Copy this report's scripts there. The final focused
   `drain-fifo-admission.patch` applies to that ticket source, which itself depends
   on the prior unaccepted retained-drain experiment. It is not a stand-alone
   accepted-source patch. Alternatively run `prepare_admission_candidate.py` then
   `prepare_fifo_candidate.py` to regenerate the final candidate/patch.
2. For before tracing, copy `ProfileWaterFillingChecks.java` to
   `project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFillingChecks.java`
   and the inherited ticket source to the project's WaterRuntime path. Run
   `python3 run_native.py profile-before filling --suite filling --no-trace`.
3. For the candidate, copy `fifo-WaterRuntime.java` to WaterRuntime **inside the
   disposable project only**. Install this report's canonical WaterFillingChecks,
   ClosedWaterChecks, DrainPendingChecks and DrainIdleChecks in visualValidation.
   Run `python3 run_fifo_safety.py` sequentially; run the loaded case with
   `python3 run_native.py fifo-loaded-idle-corrected idle --suite drain-idle --no-trace`.
   Every runner has a 480-second wall watchdog, fresh loopback/offline run directory
   and validation-only empty-server pause disabled. Existing directories are
   refused unless an explicit reopen case selects one.
4. Reproduce the remaining gate with
   `python3 run_native.py basin-fifo-corrected basin-fixed --no-trace`.
   Inspect explicit assertion/crash records even when Gradle exits zero.
5. Use the current-proxy build arguments from `run_native.py`, replacing the run
   target with `build`. After the producer exits, run `python3 audit_package.py`,
   then packaged filling/reload via `--packaged --no-trace`. Check the staged JAR
   hash again after both tests. `finish_validation.py` records the exact sequential
   finishing commands used here; it expects the completed 17-case safety report.

Next implement a bounded reduction of repeated basin searches/admission churn,
then repeat complete settling, fully loaded idle/edit, conservation, queue,
pressure, physical reload and final build/package gates before integration.
Larger-than-8,192-node continuation, renewable/finite regional water budgets,
enabled-view water/LOD/transparency, coherent geology and broader terrain
acceptance remain open. Catalog/species work stays separately deferred.


<!-- END PRESERVED SOURCE -->
