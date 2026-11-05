# Expanded replica — docs/validation/engine-continuation/basin-pressure-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/basin-pressure-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/basin-pressure-2026-10-22/README.md |
| Preserved source SHA-256 | `473605fddc804b2d3ec4c917fe9f6101d97d9bcf1e4a80d3a9eddf5bc6ee2741` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

A staged closed-basin throughput change addressing distant drain witnesses that transferred only one unit despite available head and capacity. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the functional basin gate passed while higher fixture p95 blocked performance promotion; larger residency and original filling waiters remained unresolved. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add separate progress, conservation, settling and latency acceptance for drain throughput, including the exact output digest comparison envelope. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat the 257-visit wet tube, closed-route quantity/FLOW controls and loaded-pool idle/edit scenarios using matched inputs, preserving the blocked promotion until tail cost is explained. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Closed-basin throughput fix and pressure-cap cleanup — 2026-10-22

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Closed-basin throughput fix and pressure-cap cleanup — 2026-10-22

This is a **staged, unreleased experiment**. Accepted production WaterRuntime stays
at `2c431bb9a4cb922dae5b441c29676ad9d9a6bc5a`. Main, supplied alpha.24 installers and
frozen V1/V2/V3 geography are untouched. The functional basin gate now passes;
the higher p95 in these fixture runs blocks performance promotion. Review the
candidate runtime and core together. Neither source snapshot is an installer.

## Reproduction and focused change

The prior staged FIFO source (`8ca0d657…`) commits exactly one unit through every
validated distant drain witness, even with sufficient hydraulic head and capacity.
A new native 257-visit wet-tube probe starts with 15 source units and an empty
lower outlet. Before: one unit reaches the outlet after three bounded turns.
After: four units reach it in the same three turns; every turn conserves all
4,127 units and visits at most 128 cells. There are no intervening level steps.
The old probe deliberately fails native acceptance; its assertion/crash stack
is preserved even though Gradle returns zero.

The independent fix uses the existing pressure quantum of four after all retained
route/edit/availability/head checks. It preserves the one-unit film fallback when
the discrete pressure split rounds to zero. Local and retained pressure use a new
capped core overload, replacing their duplicated cap/reconstructed Exchange code.
The old four-argument core API preserves its behavior. This is source cleanup,
not a claim about measured JIT allocation or whole-engine performance.

`limited-drain.patch` is relative to the prior staged FIFO source. The full patch
against the accepted runtime is also provided; that larger diff includes the
previously staged retained search, readiness and admission work. Exact body audit
finds only localPressure, continueDrain and equalize changed from FIFO; 48 other
parsed methods match. No scheduling, queue limits or lifecycle bodies changed here.
An actual-core differential passes 34,969 legacy cases and 629,442 limited cases,
including extreme heights and negative-limit rejection. IntegratedEngineChecks
adds 29,478 limited cases to its existing 4,913 pressure cases.

## Fresh native results and limits

Twenty-four current-candidate cases pass the strict log/file/shutdown audit:
17 safety gates (one reopen gate needed a separate retry), two basin runs, wet
transfer, loaded-pool idle/edit, and three packaged probes. Long independent
drains, silent edits, unavailable chunks, cached reload, observed physical unload,
save/reopen, pressure/head changes, both 65,536-entry FIFO contracts and exact
quantity checks pass. All seven specified closed-route quantity/FLOW digest pairs
match the previous FIFO candidate. This is limited output coverage, not general
terrain or water equivalence.

The basin geometry, initial 8,192 units and 3,200-tick deadline are unchanged.
Validation now emits final evidence before its acceptance assertion. The final
repeat additionally requires zero processed cells, pressure/drain visits and
transfers on every subsequent host tick after settling, plus no pending work or
chunk watches. It has 832 empty-work-queue samples including the settling tick,
and 831 subsequent zero-transport-work ticks. Both final-source runs produce:

- Quantity SHA256: `957f23583a886c7f3cb4d71565c65c07789f01bf043018552eddca270f07c036`
- Quantity/FLOW SHA256: `e5ca581bb0bdd20fde0ecb10da51cae55369c07b55814bec8e1b90eb22844b8f`

| Fresh basin case | Upper units | Bottom units | Pending | First settled tick | Median ms | p95 ms |
|---|---:|---:|---:|---:|---:|---:|
| basin-before | 368 | 7697 | 522, | 0 | 2.791946 | 4.586631 |
| basin-pressure-a | 0 | 8192 | 0, | 2369 | 2.765976 | 5.941476 |
| basin-limited-a | 0 | 8192 | 0, | 2369 | 2.879217 | 5.649945 |
| basin-limited-b | 0 | 8192 | 0, | 2369 | 2.61675 | 5.261344 |

The intermediate pressure-wrapper run and final capped-core runs have identical
transport work and final digests: 12,812,347 drain visits, 6,371,970 pressure visits
and 80,277 transfers. The failing FIFO baseline has 18,987,408 drain visits,
5,291,264 pressure visits and 84,999 transfers. Fewer combined visits and eventual
settling do **not** establish lower tick latency: candidate p95 is worse in these
runs. Final states differ from the failing baseline, and the full workload still
needs broader matched output, latency and resident-memory acceptance. No general
speedup, 60 FPS, independent 60 Hz or owner-GPU guarantee is claimed.

The strengthened loaded 1,024-unit pool/8-unit competing film case passes 20
consecutive zero-work idle ticks, then an ordinary floor edit wakes it, fills the
new 16-unit sink, and releases all pending work/watches again. Quantity is checked
every tick. Drain turns remain at most 128 visits; 16 retained jobs, 8,192 nodes/job,
65,536 pending keys, 3,072 admission visits and 6,144 total drain visits/host stay
bounded. Readiness stays at 16 probes/host and 1,024 keys. These are local residency
limits, not general continuation beyond them.

## Audit catches and build evidence

The first pressure-reopen attempt has a passing record file but a truncated log
without stdout acceptance, final shutdown or Gradle completion. Its cause is
unproven and it is excluded from the pass count. The batch stopped. The subsequent
separate-process retry preserves both saved 4,192-unit routes and passes all gates.
The runner was tightened: startup autosave cannot count as final shutdown; it now
checks matching file/stdout acceptance and build completion, followed by an ordered
stop/save. `strict-native-audit.json` applies those conditions to all 24 accepted
cases, including the earlier logs. Baseline failures remain failures. 
The first full build failed because the new negative-limit test local collided
with an existing corrupt-cache test local. It was renamed to pressureLimitRejected
in the fixture and generator; runtime/core source did not change. The failure is
preserved in initial-build-failure.json. The final default **non-Vulkan** build
runs uncached tasks. CoreChecks, frozen
geography/nine-seed checks and module boundaries pass. The final development JAR
passes outer/four nested archive CRCs and all 226 compiled-class byte comparisons
(104 main, 58 core, 37 graphics settings, 13 LOD, 14 performance). Validation and
private-agent classes are absent. Packaged filling, wet-transfer and reload pass;
the audited archive hash is unchanged afterward. No installer is supplied.

The isolated project Git-blob comparison matches 378 tracked inputs. Four existing
inputs differ intentionally: candidate runtime, core pressure API, core checks and
the previously staged filling fixture. Four native fixture files are additions.
Generator, renderer, settings and module-boundary inputs remain unchanged. Optional
8-unit vanilla compatibility and the 16-unit engine are separately wired; the
quantity-authority audit explains why they were not blindly combined as duplicate
code. No regional reservoirs or unlimited refill were added.

## Reproduce and continue

Use a disposable `project/` directory with the current branch inputs and a private
JDK25/JDK21/Gradle toolchain as described in the handoff. Tool restoration is the
existing optional checksum-pinned helper; no build-host shim is mod payload. Get
the prior report's canonical `fifo-WaterRuntime.java` if regenerating the patches.
The saved limited runtime/core snapshots can instead be copied directly into their
normal project source paths. Copy IntegratedEngineChecks to coreChecks and the five
native fixture snapshots to visualValidation. Use a fresh directory for every run.

Run `check_limited_pressure.py`, then `run_fifo_safety.py` sequentially. Also run
`python3 run_native.py basin-limited-a basin-fixed --no-trace` before the finishing
helper so the strict audit has two current-source basin cases. The finishing
helper runs wet transfer, loaded idle/edit, a fresh basin, an uncached full build,
archive/class audits, and the three packaged probes. Native runners have a
480-second wall watchdog and use offline loopback servers with empty-server pause
disabled only in disposable fixtures. Inspect actual records and failures; never
treat Gradle zero or a startup save as native acceptance. The exact run sequence
also included the preserved incomplete reopen and its explicit retry.

Next profile the increased pressure work/tail latency while retaining the new
settling, one-unit film and zero-idle-work gates. Keep this experiment separate
until performance promotion is justified. Original unpinned filling still retains
1,024 waiters at tick 120; larger residency continuation remains incomplete.
Renewable/finite regional reservoirs, enabled-view water/LOD coverage, mixed
transparency and coherent geology remain unfinished; catalog/species is deferred.
No fresh visual/Vulkan run occurred: this environment has no DISPLAY, Xvfb or
vulkaninfo. Historical/software-driver checks do not validate this source or owner
GPU/FPS. Three substeps in a 20 Hz host remain distinct from independent 60 Hz.


<!-- END PRESERVED SOURCE -->
