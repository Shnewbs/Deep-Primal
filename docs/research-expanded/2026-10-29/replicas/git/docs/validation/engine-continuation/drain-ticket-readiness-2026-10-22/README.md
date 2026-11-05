# Expanded replica — docs/validation/engine-continuation/drain-ticket-readiness-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-ticket-readiness-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/drain-ticket-readiness-2026-10-22/README.md |
| Preserved source SHA-256 | `aa6b24f465cb0f4b4fc855c6fe33fe458c66dc876b6e15c3f0cbc389c4f579b6` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The bounded drain-readiness candidate with pinned restoration tooling and source-extracted controls for duplicate events, delayed FULL, withdrawal and probe limits. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the readiness model uses world doubles, and native reload progress does not resolve the separate eight-cell film starvation and basin-settling failures. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add separate lifecycle-readiness, queue-fairness and settling gates so a successful reload repair cannot silently accept the retained-drain experiment. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Retain cached and physical reload controls while competing eight film cells against 1,024 stable cells, asserting bounded probes, quantity conservation and eventual outlet progress. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Bounded drain reload readiness: fresh native evidence, integration blocked

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Bounded drain reload readiness: fresh native evidence, integration blocked

2026-10-22, after the workspace reset. Source ref: `work/engine-after-alpha24`.
Accepted solver remains `2c431bb9a4cb922dae5b441c29676ad9d9a6bc5a`.
This checkpoint preserves an independently authored candidate, fixtures and evidence;
it does **not** install the failing retained-drain experiment in production source.
Main, alpha.24 installers and published V1/V2/frozen V3 geography remain unchanged.

## What changed

The isolated JDK25/JDK21/Gradle/native environment was restored. All 381 recovered
default-build inputs matched their Git blobs before experiment injection. The new
optional `tools/restore_validation_toolchain.py` pins sizes and SHA-256 for five
private development assets, bounds downloads, extracts safely into a fresh directory
and refuses overwrite. Its verify-only check passes all actual archives; refusal
and unchanged-byte checks pass. The tools are not mod payload. The ordinary wrapper
remains appropriate on a normal build host. These old pinned versions were selected
for reproducibility, not as a claim about latest upstream versions.

Fresh tracing reproduces a missing wakeup in the preserved retained-drain
experiment. Chunk 261,0 transitions through ticket levels 34 -> 32 -> 31 while its
LevelChunk remains resident; no second ChunkEvent.Load arrives. Load-only wakeup
therefore leaves the long-route unit at its origin. Earlier no-FULL probes establish
availability changes, **not physical unload**. The separate physical-reload fixture
now requires an observed ChunkEvent.Unload for chunk 290,0 before reloading it.

`ticket-readiness-candidate.patch` adds bounded ticket-event readiness tracking on
top of the prior unaccepted experiment. Load/ticket callbacks record keys only;
level stepping probes at most 16 keys per host tick using FULL,false, retains
not-yet-ready keys in FIFO order, and wakes indexed origins only when available.
Keys are coalesced/capped at 1,024, overflow rebuilds from the bounded live wait
index, and withdrawal/forgetting removes obsolete keys. Existing bounds remain
16 retained jobs, 8,192 nodes/job, 128-node turns, 128 waiting origins,
1,024 watch links and 6,144 drain visits/host tick. No forced generation or
geographic change is added. NeoForge API observation is recorded in
`upstream-api-evidence.json`; no upstream implementation was copied.

Canonical candidate SHA-256:
`d3449da9be7ab9f8992db77127e5b168f04ca350ee0b0d25a7907b8dfac29f59`.
Most safety runs used the separately preserved opt-in trace source
`dc36161850fc0dd5d7571e1732975c8cce578a35dbd648085847798a9eb641fa`;
their individual records identify it. Packaged runs use the canonical untraced
source. Validation fixtures and trace instrumentation are evidence only.

## Fresh results

Every pass requires an explicit fixture record, no assertion/crash, and normal
save. Gradle exit zero alone is insufficient. Failures and inspected crash stacks
are preserved alongside passes in `native-results.json`, `native-excerpts.txt` and
`inspected-crashes.json`. No client/display screenshots were produced by these
headless server checks.

| Gate | Actual result |
| --- | --- |
| Accepted checkpoint full build | PASS: non-Vulkan build/core/module checks; 103 packaged main classes match; outer/four nested CRCs pass |
| Source-extracted readiness model | PASS: exact method bodies, 2,048 duplicate events, 128 origins/1,024 links, delayed FULL, withdrawal, overflow, idle and 16-probe bound; world doubles, not native acceptance |
| Prior experiment cached reload | FAIL: one sleeping waiter/source unit remains; assertion/crash despite Gradle zero |
| Ticket candidate cached reload | PASS: 480 ticks, two units conserved each tick, outlets reached, zero final pending/jobs/watches, 21 idle samples |
| Physical unload/reload | PASS at 3,200 ticks: actual unload observed, no-force gap, 1,025-cell route, two units conserved each tick, final zero work and 21 idle samples |
| Earlier physical attempts | NOT PASS: first watchdog timeout caused by vanilla empty-server pause; corrected pause setting then failed 2,000-tick deadline. Both retained. This is a latency miss, not erased by the later longer pass |
| Closed routes/silent edits/unavailable/reopen | PASS: conservative fixed/edit/reload-edit/unavailable/reopen cases; wall edit while waiting stays intact |
| Long pressure/head/edit/unload/idle/reopen | PASS: preserved 4,190/4,192-unit cases, continuation and invalidation; large-component residency saturation remains open |
| Existing short pressure | PASS: 144 units conserved, raised surface, disconnected/no-force controls |
| Active FIFO | PASS: 65,536 cells, 262,144 admissions, order/mutable/negative/cap/overflow; warmed admission allocation 304 bytes, not total solver memory |
| Existing 120-tick filling | FAIL on both inherited and ticket experiments: eight-unit film does not reach lower outlet. Accepted checkpoint passes with pending=0. Saved candidate has all eight one-unit cells at their original film positions and outlet=0 |
| Closed stepped basin | FAIL after 3,200 ticks: 8,192 units conserved every tick, upper=698, bottom=7,457, 37 intermediate, pending=500. No repeat B was run after failure |
| Canonical candidate full build/package | PASS compile/core/module; all 104 main classes byte-match, outer/four nested CRCs pass; **runtime acceptance remains false** |
| Canonical packaged cached reload | PASS: same quantity/FLOW digests, 480 ticks, zero pending/work/watches and 21 idle samples |
| Canonical packaged filling | FAIL: sink=0, pending=1032, transfers=0; 16 resident jobs, 128 waiting jobs, 624 watch links, 6144 drain visits that host tick; assertion/crash despite Gradle zero |

The film regression is attributable to the inherited retained-drain experiment
relative to the accepted checkpoint. The specific admission/fairness mechanism is
still a hypothesis. The ticket-readiness patch fixes its targeted reload defect;
it does not fix general drain transport or establish acceptance of its parent.
The basin also remains active rather than settled. Do not integrate either runtime
until the filling/basin failures are corrected and safety/package checks repeat.

The final canonical JAR is development evidence, not a supplied installer.
See `candidate-build-audit.json` and `package-post-native.json` for class/hash/CRC
identity and unchanged bytes after packaged tests. Packaged filling is recorded
separately in native results, including its failure diagnostics. No new visual,
Vulkan, enabled-view coverage, broad performance or owner GPU/FPS checks ran.
Three solver substeps in a 20 Hz host are not an independent 60 Hz simulation.

## Reproduce in an isolated directory

Keep the accepted checkout untouched. Copy this report's scripts/files into a
fresh experiment root containing a `project/` source copy of the accepted commit,
and use the sibling prior report
`../drain-reload-audit-2026-10-22` as `--audit`. Review scripts before use; runners
create loopback/offline validation servers, bound each run to 480 wall seconds,
and set validation-only `pause-when-empty-seconds=0` so a long test can finish.

1. Restore tools with `python3 project/tools/restore_validation_toolchain.py --output toolchain`.
   Build the private agent using the existing
   `project/tools/compat/build_process_fallback.py --java-home toolchain/jdk-25.0.4.1+1 --asm-jar toolchain/checked-agent/asm-9.9.1.jar --neoform-jar toolchain/checked-agent/neoform-runtime-2.0.31-all.jar --output toolchain/checked-agent/neoform-process-fallback.jar`.
   The agent's own compatibility checks must pass. Existing reports retain exact
   input hashes; the helper was verified against actual archives rather than
   re-downloading every asset a second time.
2. Run `python3 prepare_ticket_candidate.py --audit ../drain-reload-audit-2026-10-22`.
   The original unaccepted patch/source lives in that prior report. This focused
   patch applies to its staged source and does not stand alone on accepted source.
3. Run `python3 check_ticket_readiness.py ticket-WaterRuntime.java --lifecycle-fixture ../drain-reload-audit-2026-10-22/check_deferred_load.py`.
4. For traced reproduction, `python3 prepare_native_trace.py before --audit ../drain-reload-audit-2026-10-22`,
   then `python3 run_native.py reload-before reload`. Switch to `ticket` and run
   the reload/safety scripts sequentially. The expanded ClosedWaterChecks snapshot
   is included; do not confuse its physical-unload case with cached availability.
5. For canonical/package checks, copy `ticket-WaterRuntime.java` to production
   WaterRuntime **only inside the disposable experiment project**. Copy the
   preserved validation fixtures into its visualValidation source tree. Build
   using the current-proxy helper/arguments in `run_native.py`, replacing the run
   target with `build`; compare current classes/CRC before running
   `python3 run_native.py packaged-reload reload --packaged --no-trace`.
   For diagnostic film reproduction use
   `DEEPPRIMAL_FILL_TRACE=true python3 run_native.py packaged-filling filling --suite filling --packaged --no-trace`.
   Diagnostic output is validation-only and assertions/deadlines are unchanged.

The next implementation step is to reproduce/profile retained-drain admission
and scheduling on the eight-cell film with its 1,024 stable competing cells,
repair progress without unbounded work or quantity changes, and repeat the
closed-basin settling gate. Retain the new cached/physical reload and edit tests.
Pressure/drain continuation beyond 8,192 nodes, regional renewable/finite water
budgets, enabled-view water/LOD/transparency, coherent geology and broader terrain
acceptance remain open. Catalog/species work stays separately deferred.


<!-- END PRESERVED SOURCE -->
