# Expanded replica — docs/validation/engine-continuation/stream-deadline-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/stream-deadline-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/stream-deadline-2026-10-26/README.md |
| Preserved source SHA-256 | `3510fc49ca78923d7f27189c1a1db71c97676b772a7a3cf8b9dd873b32a86c99` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged between-column deadline check after one admitted stream page continued all 256 callbacks when its first callback exhausted the allowance. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the exact-method reproduction uses deterministic boundary doubles, and an initial negative expected-count oracle was corrected rather than counted as a product failure. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add per-item admission rules for multi-item pages, including cursor resume position and zero remaining-admission behavior. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Advance time within the first callback, require later columns to defer without duplication, clamp the independent oracle correctly and preserve complete native qualification as a separate gate. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Staged natural-stream deadline admission

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Staged natural-stream deadline admission

Production remains dev.29 at `b1db1bbbef903d33feecf32af986761abe2de5b7`.
This directory preserves an independently authored candidate, not a promoted
engine change or a newly qualified binary. It follows the completed dev.29
precipitation repair and uses the roadmap's bounded-work gate. The latest
consumed independent audit remains
`docs/engine-hourly-audits/2026-10-26T1033-4a583db/REVIEW.md`, pinned to
`4a583dbcdec4df4be1ac7572c384813986067f2a`.

## Defect and candidate

`HydrologyRuntime.tick` checks its deadline between stream pages but not between
columns within one page. The exact production method, compiled against
deterministic time and Minecraft boundary doubles, executes all 256 callbacks
when its first callback consumes the remaining budget: 255 callbacks begin
after expiry. The staged condition checks the same deadline before every
stream column. It admits one callback in that case and no late callbacks across
the 210 count/cost/survey combinations. Five additional cases cover the concrete
reproduction, page/column ordering, headwater priority, unloaded pages and stale
page entries. Both baseline characterization and candidate assertions pass
(215 cases each). Production file bytes were compared with pinned Git source.

This only bounds further admission. One callback, survey, cleanup or sorting
can still overrun; the test does not establish an actual 2 ms latency bound.
Headwater-first ordering is retained, not replaced by a round-robin cursor.
Fairness under sustained overload and native finite-quota behavior remain gates.
No water quantities, regional quotas, source backlogs, geography or art change.

## Reproduce

Run from a checkout of the pinned production source:

```sh
python3 docs/validation/engine-continuation/stream-deadline-2026-10-26/reproduce.py --source src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java --java-home /path/to/jdk25 --output /tmp/deep-primal-stream-deadline
```

The script extracts the actual method and refuses source drift at the changed
condition. `repair.patch` and the complete staged source are reviewable inputs.
An initial fixture-only assertion incorrectly allowed negative expected counts
when the survey had already exceeded the deadline; that oracle was corrected
to clamp remaining admissions to zero before the successful full rerun. It was
not a production failure and no failed run is counted as passing evidence.

## Remaining qualification and coordination

No Gradle/native build was started for this candidate: the shared filesystem had
717 MB free after removing only our already-saved duplicate exports. The existing
qualified dev.29 artifact remains available. This stage is deliberately outside
production until full Java25 build/module/core and packaged native source,
water-budget, save/reopen and shutdown checks pass. Add an actual loaded-world
stream fixture and examine overload/headwater behavior before promotion.
Windows/AMD8060S, real OpenGL/Vulkan pixels, controlled latency and long-soak
multiplayer remain unverified; no new grade is awarded.

Main and claims were re-read at 11:34–11:41 UTC on 2026-10-26. The bounded writer
claim was acquired at `93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a` and is released
with this checkpoint. Personal-context lookup returned no newer matching chat
record; this is unavailable cross-chat evidence, not confirmation of inactivity.
Recorded auditor state was completed at 10:44 UTC; it is not a live-process
guarantee. All fixture files and outputs are isolated from other checkouts.

The last complete accepted external grade remains 50/100 (5.0/10); the latest
scoped provisional audit is 48/100 (4.8/10). Acceptance remains incomplete.


<!-- END PRESERVED SOURCE -->
