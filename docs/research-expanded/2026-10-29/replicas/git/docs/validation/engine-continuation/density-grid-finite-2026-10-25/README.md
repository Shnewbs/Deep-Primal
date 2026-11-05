# Expanded replica — docs/validation/engine-continuation/density-grid-finite-2026-10-25/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/density-grid-finite-2026-10-25/README.md) |
| Source identity | git: docs/validation/engine-continuation/density-grid-finite-2026-10-25/README.md |
| Preserved source SHA-256 | `e018c64bbaae4d5aa5eb64669f50aa55561374ef7ef9629ca9eeb7c4aeba9b7e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged DensityGrid finite-interpolation repair after subtraction of opposite-sign finite endpoints produced infinity or later NaN. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that finite source samples alone did not guarantee finite interpolation, and the memory-backed core experiment did not complete full-project or native qualification. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a numerical contract for density interpolation covering extreme finite values, endpoint identity and invalid-sample handling without assuming metric signed-distance semantics. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare extreme signed endpoints and tiny interpolation factors against the stated finite-result oracle, preserve normal-range output controls, and rerun the final package before promotion. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Staged density-grid finite interpolation repair — 2026-10-25

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Staged density-grid finite interpolation repair — 2026-10-25

Source: `48063e8020fa94b38e841ca3b2add9b966e6655a` (finalized dev.19).
Consumed independent audit: `docs/engine-hourly-audits/2026-10-25T1646-ec687c2/REVIEW.md`, pinned `ec687c2158563f03c8f6737cade635f7e2de66b9`.
Last external grade: **50/100 (5.0/10), unchanged**. No fresh regrade.

## Implemented candidate
DensityGrid accepts arbitrary finite DensityField samples, but its interpolation subtracts endpoints. Opposite-sign finite endpoints can overflow that subtraction, yielding infinity (and potentially NaN in later interpolation), violating the grid's finite-sample assumption. Baseline reproduction fails with a=-Double.MAX_VALUE, b=Double.MAX_VALUE/2 and t=Double.MIN_VALUE.

Candidate uses weighted endpoint terms only when the endpoint difference overflows; ordinary interpolation and exact endpoint branches remain unchanged. This is an authoring/density-stage correctness repair, not native V8 integration or a performance improvement. Existing bounded terrain graph outputs do not approach these extreme values. The grid is not promoted into world generation, and its approximate topology remains unaccepted.

## Verification
- 108 original input Git blob identities verified against pinned current main: 62 production core, 46 core check sources. Older audit inputs were used only when their blob SHA matched; 15 newer files fetched at the exact current source.
- Baseline new regression FAILS with explicit nonfinite assertion.
- Candidate standalone regression PASSES: 11,728 checks, all three interpolation axes, extreme/opposite/same-sign finite endpoints, signed-zero endpoint identity, high-precision decimal convex-combination oracle, and 10,000 bit-identical ordinary-range cases.
- Full dependency-free Java25 CoreChecks PASSES; retained candidate.log includes explicit pass records for noise, graph, 1,581,340 reuse checks, adversarial graphs, density chunks, terrain, soils, water, cave, watershed and lifecycle core behavior. Original noise/graph fingerprints retained.
- Separate noise/graph invocations also pass. These repeat checks are not independent benchmark evidence.
- No full Gradle/module/native/graphics/package execution this cycle. Timing lines printed by core checks are incidental and are NOT controlled performance claims.

## Promotion and delivery gate
**Staged only.** Production DensityGrid, gradle.properties and all existing engine/art resources are unchanged. Candidate patch and complete replacement source are retained here; no feature branch or PR. Reserve next free development version beyond dev.19 only when integrating after full gates; no production version bumped for an unpromoted candidate.

Filesystem initially reported 100% full / zero available bytes. Verified and removed three duplicate tool archives from this builder's older workspace (484,338,664 bytes), retaining SHA-identical copies and extracted tools. It still reported zero available space afterwards; see space-recovery.json. The isolated 1.9 MB core workspace used /dev/shm. Other writers/auditor checkouts, caches and outputs were not modified. Insufficient storage prevents a fresh independent full project/cache/native/output/package environment; no full build was attempted and no new JAR or release is claimed. This is a recoverable capacity blocker, not engine completion.

## Coordination
Personal Context search found relevant historical pause/resumption and audit records, but no reliable live claim for the other engine chat. An unrelated project result was excluded. Auditor task peek showed its latest run completed at 2026-10-25T16:51:33Z. Main writer lease was expired, performance reservation completed, and no Java/Gradle process was observed before this run. Bounded main writer lease claimed with expected-parent update; no timing benchmark reservation necessary. No continuous background execution claim.

## Reproduce
Use a checkout of the pinned source, JDK25, and a writable output/temp directory:
```sh
python3 run_checks.py /path/to/pinned/repo /path/to/jdk25 /path/to/new/evidence
```
The runner validates all input blob SHAs, reproduces the failing baseline, and compiles/tests the candidate separately without editing the supplied checkout. Set TMPDIR to a directory with sufficient capacity if needed.

## Next
Recover isolated full-build capacity, apply candidate.patch only against compatible current source, wire regression into core checks, bump beyond current production version, then full Gradle/module/package gates before promotion. Continue higher-impact native V8 saved-profile/materialization/topology/routed-water integration and dev.19 live-water/performance acceptance; do not repeat the already-finalized shared-edge repair or promote older R57/R60/R61/R62 overlays automatically. Windows/AMD8060S, full visual and native acceptance remain open. Builder remains enabled toward the independent >8.5 target.



<!-- END PRESERVED SOURCE -->
