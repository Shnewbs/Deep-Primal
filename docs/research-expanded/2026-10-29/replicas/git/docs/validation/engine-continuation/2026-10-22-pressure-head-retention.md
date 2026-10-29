# Expanded replica — docs/validation/engine-continuation/2026-10-22-pressure-head-retention.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-pressure-head-retention.md) |
| Source identity | git: docs/validation/engine-continuation/2026-10-22-pressure-head-retention.md |
| Preserved source SHA-256 | `bb947908c60ea9ebc0af3f2eb12947ad500f71b6feef01f9d28ccf3c2ceab052` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The correction of pressure-head retention after ordinary transfers invalidated retained pressure graphs, with repeated basin and explicit retention checks. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that tolerance-based surface retention is not identical regional physics or a whole-region mass ledger, and all 6,144 pressure visits remain occupied at the final tick. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a distinction between invalidating topology and updating quantities, plus an explicit settling definition for retained search state. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat head changes, edits and unloads while tracking conservation, retained graph validity, resident nodes and backlog against matched pre-continuation and candidate workloads. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Pressure head retention and bounded search cost — 2026-10-22

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `2026-10-22-pressure-head-change.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-pressure-head-change.txt) |
| `2026-10-22-pressure-head-retention-build.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-pressure-head-retention-build.json) |
| `2026-10-22-pressure-head-retention-matrix.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-pressure-head-retention-matrix.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Pressure head retention and bounded search cost — 2026-10-22

Unreleased continuation of `0432800ec599f50d4df1f8482072a85f3a0ef4e4`, in an isolated directory after acquiring the work-state lease. Main remains `d021557dd9fc6a7e3d29d81a80b55a608fdbb3d6`. No installer was delivered, no geography changed, and promotion remains blocked. All measurements below are fresh post-reset checks of restored source; earlier native/graphics evidence is historical and is not a substitute for these checks.

## Reproduced defect and focused correction

The previous candidate invalidated an entire retained pressure graph when an ordinary positive-to-positive water-volume change touched its witness. The same opt-in `HeadFrontierProbe` was installed in both isolated revisions. In the real server, after one 128-visit slice and a conservative one-unit head change, the old code repeats its prefix: 129 retained nodes before and after, restart delta 1. The revised code advances 129 → 257 nodes, restart delta 0. Both preserve 4,190 units. These are explicit native before/after records, not a world-double simulation.

`WaterRuntime` now preserves wet connectivity across positive volume changes, validates current endpoint volumes/surfaces before a conservative transfer, and derives the outlet from the currently wet lower surface. Dirty heads receive bounded rescans; after two refreshes, unresolved work returns to the pending FIFO rather than holding a resident slot indefinitely. Topology writes, unloads and newly loaded boundaries invalidate witnesses. A useful fresh pair can transfer at the end of a slice. Only a fully explored, unsaturated, unchanged component with no useful pressure move coalesces duplicate pressure requests. Active gravity/drain requests remain independent.

Additional cost reductions avoid duplicate state reads/zero-move exchanges, skip already-seen pressure neighbors before world queries, and store pending positions as a primitive FIFO set. The limits remain 128 visits/turn, 16 resident jobs, 8,192 retained nodes/job and existing per-host-tick budgets. This is still three solver substeps in a 20 Hz host tick, not an independent 60 Hz simulator.

The source audit checks 377 restored existing non-document inputs against the base Git tree: only `WaterRuntime.java` and validation-only `LongPressureChecks.java` changed. `HeadFrontierProbe.java` and the optional streaming `WaterJfrSummary.java` are added development tools. Omitted optional renderer files remain preserved by the repository base tree; they were not deleted.

## Fresh native checks

The compact [matrix](2026-10-22-pressure-head-retention-matrix.json) records individual explicit markers, exits and fixture identity. The extra [head-change record](2026-10-22-pressure-head-change.txt) runs 120 real ticks after the partial-volume change: both upper surfaces reach 7 units and all 4,190 units remain.

The final-source matrix passes two independent long bends (4,192 units each), silent wall edits (4,176 units), actual chunk-gap unload/no force-load (4,192 units), isolated settled idle, separate-process reopen of that same world, existing filling and short pressure/disconnection controls. Idle/reopen each require 21 quiet samples. Coalescing and bounded head deferral are exercised. Some fixtures still report residency saturation; this remains an unresolved acceptance limit.

Every case requires its explicit pass record, normal process completion, and no assertion/unexpected Minecraft exception. A Gradle exit of zero alone is insufficient. These server fixtures have no screenshots and do not validate rendered water/LOD coverage.

## Repeated basin comparison

Unprofiled fresh worlds, ABBA order, same unchanged `LakeRetentionChecks` SHA-256 `792eacf1b3a402f558513c78f79af60e93dfb19f29b1a7c84d78fbbe937ed138`, seed 20261020, view/simulation distance 2, 49 forced chunks, 1,000 ticks:

| Revision/run | Median tick ms | p95 tick ms | Final pending |
| --- | ---: | ---: | ---: |
| Previous candidate A | 19.402420 | 33.130358 | 7,221 |
| Revised A | 17.608034 | 31.423414 | 6,099 |
| Revised B | 17.725262 | 27.455720 | 6,198 |
| Previous candidate B | 19.092585 | 33.123441 | 6,881 |

All four explicit retention checks pass. This repeat comparison improves cost/backlog over the previous continuation candidate, but output is tolerance-based surface retention, not bit-identical regional physics or a whole-region mass ledger. Thousands of entries remain and both revised runs still use all 6,144 scheduled pressure visits at the final tick. They report no stable-component coalescing in this active basin. Earlier pre-continuation timings came from a separate run and cannot establish current superiority or resolve the previous release blocker. Broad performance, complete settling and memory acceptance remain open; these are container CPU timings, not owner GPU/FPS guarantees.

## Build and package gate

The final full non-Vulkan build, CoreChecks (including the nine-seed/frozen-geography suite), core boundary and module boundaries pass. The first archive CRC check passed but a class-byte comparison caught a stale packaged WaterRuntime in copied incremental output. That archive was rejected; the exact incremental cause is not established. A forced uncached jar rebuild then passes outer/four nested CRCs and all 103 main-class byte comparisons. Validation/profiler/build-compatibility classes are absent. A separate native launch using the rebuilt JAR (`-PpackagedValidation`) also explicitly passes the 129 → 257 frontier/no-restart/4,190-unit check. See the [build/archive record](2026-10-22-pressure-head-retention-build.json). No installer was released. Future isolated/copied builds must verify packaged class identity as well as CRCs; CRC validity alone does not establish source freshness.

## Profiling, reproduction and limits

JFR uses `settings=profile` on `runServer` only, in separate runs excluded from the timing table. The streaming helper reads sampled server-thread water stacks without exporting huge JFR JSON. Allocation weights are estimates over the sampled run, not live/resident heap measurements. Before/after compact summaries are staged beside this report. Sampled water-related allocation weights fall from 3,408,929,376 to 2,811,091,216 bytes; water execution samples are 1,537/1,703 server samples before and 1,522/1,696 after. BlockPos allocations remain the largest sampled class (about 1.77 GB after). This does not certify resident memory or normalize for every difference in evolving physical output.

Use the restored JDK25/Gradle9.2.1/current-proxy launcher described in the prior [build evidence](2026-10-22-build-access-and-native-pressure.md), with `-PwithoutVulkan -Pmod_version=0.0.1-alpha.25-dev -PvisualValidation -PrunDir=<isolated-world> runServer`. The asset task also needs the recorded private JDK21 installation. Servers use loopback/ephemeral port/offline mode and the fixed properties above. Set `DEEPPRIMAL_HEAD_FRONTIER_CHECK=before` on the base or `after` on the revised source and `DEEPPRIMAL_HEAD_PROBE_RECORD=<record>` for the two-slice regression. Set `DEEPPRIMAL_LONG_PRESSURE_CHECKS=true` and `DEEPPRIMAL_LONG_PRESSURE_MODE=heads|fixed|edit|unload|idle|reopen` for the longer scenarios; reopen reuses the idle world in a separate server process. The existing lake fixture uses `DEEPPRIMAL_LAKE_TICKS=1000` and `DEEPPRIMAL_LAKE_RECORD=<record>`; the other fixtures retain their prior invocation. Build compatibility agents are private development inputs, never shipped mod dependencies.

Exploratory cleanup probes and an intentionally interrupted early matrix are excluded from final-source acceptance. The old-code frontier failure is an expected reproduction, not a revised-code pass. No new visual/display/Vulkan or owner-GPU checks ran; that environment remains unavailable. The 8,192-node boundary, general drain continuation (still a 128-cell search), pending admission/settling cost, enabled-view LOD/water/transparency coverage, regional finite/renewable budgets and geology/terrain variety remain open. Catalog/species work remains separately deferred.

Next: compare repeated matched workloads against the pre-continuation production revision, profile remaining active-basin queue/churn and allocation cost, and establish settling/output/memory evidence before any promotion. Continue bounded drain/large-component coverage after those pressure gates. Preserve the delivered V1/V2/V3 geography and alpha.24 installer bytes.


<!-- END PRESERVED SOURCE -->
