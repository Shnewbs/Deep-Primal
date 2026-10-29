# Expanded replica — docs/validation/engine-continuation/2026-10-22-active-queue-allocation.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-active-queue-allocation.md) |
| Source identity | git: docs/validation/engine-continuation/2026-10-22-active-queue-allocation.md |
| Preserved source SHA-256 | `4cb23b1664b0dd6b00f9ac3a5f80d1f21da7a8c61e7b4f5ffe9ad5c9836b6ac5` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The bounded active-water FIFO allocation experiment comparing repeated 65,536-position admissions and validating mutable positions, capacity duplicates and native overflow. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that admission allocation improvement does not certify whole-solver latency, settling, resident memory or FPS; the active-basin backlog and pressure residency limits remain. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a queue contract and matched-workload ledger separating admission allocation, scheduled work, conservation, backlog and end-state comparability. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat FIFO order, negative-coordinate and duplicate-at-capacity controls, then compare closed-basin or explicit-boundary-flux replays against the pinned production revision before promoting performance claims. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Active-water queue allocation experiment — 2026-10-22

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `2026-10-22-active-queue-build.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-active-queue-build.json) |
| `2026-10-22-active-queue-experiment.patch` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-active-queue-experiment.patch) |
| `2026-10-22-active-queue-interleaved.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-active-queue-interleaved.json) |
| `2026-10-22-active-queue-matrix.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-active-queue-matrix.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Active-water queue allocation experiment — 2026-10-22

Base: `97da4d775ad355d07ef9273e572bef3aabda9220`; fresh isolated post-reset validation. Main and delivered alpha.24 installers are untouched. V1/V2/V3 geography is frozen and unchanged. This report distinguishes an allocation improvement from whole-solver latency/settling acceptance.

## Reproduction and independently authored candidate

The native active queue retains BlockPos objects and linked-hash entries for its bounded FIFO. The prior JFR recorded substantial BlockPos/LinkedHashMap allocation. The same new `ActiveQueueChecks` runs against the unmodified checkpoint and the isolated candidate, in an actual server dimension. It checks all 65,536 FIFO entries, mutable input snapshots, negative coordinates, duplicate admission at capacity, and native scheduled overflow.

After warming the queue capacity, four identical 65,536-position admission passes allocate **17,197,016 bytes** on the previous implementation and **304 bytes** on the candidate, measured with the owning thread's ThreadMXBean allocation counter. Fixture setup, expected-order objects, reflection and chunk creation are outside the measurement. This is an admission allocation measurement, not total solver allocation, live heap, resident memory or FPS.

The candidate stores the active FIFO as a primitive linked long set, snapshots input coordinates immediately, and reuses one local mutable cursor while stepping. Retained pressure positions are still copied. Caps, order, overflow scheduling, transport budgets and physical transfer rules are unchanged. Fastutil is already a host dependency. Only WaterRuntime changes among 379 verified restored existing non-document inputs; optional omitted Vulkan files remain preserved in the Git base tree. The exact candidate is retained in [the reviewable patch](2026-10-22-active-queue-experiment.patch).

## Fresh native safety evidence

All 16 initial matrix cases have explicit passing records, normal saves and no crash reports/assertion signals. The [matrix](2026-10-22-active-queue-matrix.json) includes native queue before/after, six generated-basin runs, head changes, two independent long bends, silent wall edits, actual missing-chunk unload/no-force-load, isolated idle and separate-process reopen, filling and short pressure controls.

Long bends conserve 4,192 units each; the head-change case conserves 4,190; the explicit wall edit leaves 4,176. The unloaded gap stays absent until the harness deliberately reloads it for volume auditing. Idle/reopen each observe 21 quiet samples with no remaining searches or chunk watches. Some edit/head/unload cases still hit pressure residency limits. Those limits and the unchanged short drain search remain unresolved. Three solver substeps per 20 Hz host tick are not an independent 60 Hz simulation.

## Generated-basin observations and limits

Every 1,000-tick test uses seed 20261020, 49 forced chunks, view/simulation distance 2, fresh worlds and the unchanged LakeRetentionChecks SHA-256 `792eacf1b3a402f558513c78f79af60e93dfb19f29b1a7c84d78fbbe937ed138`. The pre-continuation source is verified against `da03d9c`; checkpoint and candidate use the same restored toolchain. No profiler is enabled in these timings.

The six initial runs all pass the existing spill-level tolerance. Fresh pre-continuation medians are 17.430–18.480 ms/p95 31.525–32.487 with 3,601–3,623 pending entries. Current checkpoint medians are 16.819–17.615/p95 27.438–32.683 with 6,020–6,050 pending. Candidate medians are 17.913–20.419/p95 27.778–34.134 with 6,067–6,136 pending. This does **not** show faster whole-solver execution from the queue change; possible regression needs the interleaved follow-up, not selective comparison to older timings.

The optional new LakeWaterSnapshot records water quantities and FLOW state across the exact loaded rectangle, without force-loading. All six initial snapshots match: 49 chunks, 15,266 water cells, 184,538 units, digest `b0c7c4f0dce1f464edc67f54cc8a8cb63e266b590decffa34106fbab22a302b5`. Final snapshots differ even between unchanged-revision repeats. Exact output equivalence and full settling are unproven. This rectangle has open boundaries: changes in its unit total are not a closed-box conservation result or an independently diagnosed loss/gain. A closed replay or explicit boundary-flux ledger is still needed. These are container CPU timings, not owner-device/GPU guarantees.

## Caught staging failure

The first pre-continuation compile retained a newer validation fixture which called pressure diagnostics absent from the old runtime. That failed before native execution; it is not an acceptance pass. The baseline was restored against all 376 existing non-document inputs from da03d9c, the extra mutation mixin removed, and a clean rebuild then passed. The dedicated failed log is retained locally as `active-queue-pre-a-staging-failure.log`; final native records are separate.

## Remaining gates

The interleaved, build and package results are recorded below. The allocation fix is checkpointed only on the unreleased working branch; main promotion and installer release remain blocked. No new display, OpenGL, Vulkan, enabled-view LOD or visual tests ran. Regional reservoirs/budgets, pressure/drain residency/continuation, larger-region settling, geology and broad terrain/LOD/transparency acceptance remain open; catalog/species work remains deferred. Do not publish an installer or merge an experiment with an unresolved latency regression into stable source.

## Interleaved follow-up and checkpoint decision

The additional current/candidate/candidate/current runs all pass retention, explicit records, normal saves and crash inspection. [Interleaved records](2026-10-22-active-queue-interleaved.json):

| Run | Median tick ms | p95 tick ms | Final pending |
| --- | ---: | ---: | ---: |
| Current A | 17.637536 | 33.110540 | 6,150 |
| Candidate A | 18.864003 | 30.841375 | 6,121 |
| Candidate B | 16.728404 | 27.899567 | 6,111 |
| Current B | 20.857089 | 38.623535 | 6,062 |

These observations do not reproduce a consistently slower median, and candidate p95 is lower in this interleaved batch. The initial batch and this one vary materially, however; two repeats and non-identical final physics are not a latency certification. No claim of faster whole-solver execution, complete settling, broad memory acceptance or owner FPS is made. The independently measured admission allocation reduction and all functional checks support checkpointing the focused change on the working branch, with stable promotion still blocked. The candidate does not reduce the roughly 6,100-entry active-basin backlog.

## Fresh full build and packaged native gate

The full non-Vulkan build passes in 62 seconds, including CoreChecks, the frozen-geography ensemble and core/module boundaries. The watershed record retains 5,846,893 centerline samples, 4,292 connected reaches, 650 lakes, 964 headwaters and 18,000 saved-V2 comparisons. Only the active queue/cursor production code changed; terrain and renderer sources did not.

The 614,359-byte development JAR passes all 103 main-class byte comparisons plus the outer and four nested ZIP CRC audits. Validation/build compatibility classes are absent. A separate native launch using `-PpackagedValidation` passes the existing filling regression: consecutive progress, three-block first-tick fall, four-tick basin fill, thin films/plants/dormant activation, 1,024 competing stable cells, conserved water, no force-load and zero final pending work in that controlled fixture. The post-run JAR hash still matches the audited bytes. See [build and package scope](2026-10-22-active-queue-build.json). This is not a released installer; main and delivered alpha.24 bytes are unchanged. No stale package was accepted.

## Reproduction and next step

Use the existing restored JDK25/Gradle9.2.1/current-proxy launcher described in the prior build handoff, with `-PwithoutVulkan -Pmod_version=0.0.1-alpha.25-dev -PvisualValidation -PrunDir=<fresh-isolated-world> runServer`. The asset task also needs the recorded JDK21 installation. Servers use offline loopback/ephemeral port, seed 20261020 and view/simulation distance 2. Set `DEEPPRIMAL_ACTIVE_QUEUE_RECORD=<record>` for the FIFO/allocation fixture. For basin runs use `DEEPPRIMAL_LAKE_TICKS=1000`, `DEEPPRIMAL_LAKE_RECORD=<record>` and `DEEPPRIMAL_LAKE_SNAPSHOT=<json>` for the optional loaded-rectangle digest. The snapshot helper does not change the unchanged LakeRetentionChecks fixture. Use `-PpackagedValidation` to exercise the audited development JAR after building. The previous long/head/edit/unload/idle/reopen/filling/pressure fixture invocations remain valid.

Next: establish a deterministic fully loaded closed-basin replay or explicit boundary-flux ledger, then compare conserved output and tick cost against pre-continuation/current source. Profile remaining active-basin pressure/drain churn and resident/allocation memory with that matched workload before promotion. Preserve these queue-cap/mutable-position/overflow controls and the existing pressure edit/unload/idle regressions while extending drain continuation and pressure residency coverage.


<!-- END PRESERVED SOURCE -->
