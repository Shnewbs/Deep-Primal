# Expanded replica — docs/validation/engine-continuation/drain-reload-audit-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/drain-reload-audit-2026-10-22/README.md |
| Preserved source SHA-256 | `fb80cec4bfaca50e4ed009bdd1420607e84c167f66fe2274a8d380250fbf9d56` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The failed drain reload audit using saved End region files to show the long-tube unit remained at its origin while the short control drained. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that prior no-unload passes remain valid only for their cases; a pending reload waiter caused rejection despite Gradle returning zero. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a chunk-readiness event ledger separating ticket registration, native FULL availability, withdrawal and resumed drain ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reproduce cached and physically unloaded reloads, record actual readiness delivery, inspect conserved saved quantities and reject any pending waiter or native failure before promotion. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Drain reload audit and unaccepted lifecycle candidate — 2026-10-22

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `ClosedWaterChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/ClosedWaterChecks.java) |
| `deferred-drain-load-candidate.patch` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/deferred-drain-load-candidate.patch) |
| `environment.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/environment.json) |
| `final-drain-matrix.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/final-drain-matrix.json) |
| `inspect_closed_save.py` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/inspect_closed_save.py) |
| `lifecycle-results.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/lifecycle-results.json) |
| `prior-native-reload-excerpt.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/prior-native-reload-excerpt.txt) |
| `saved-state-evidence.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/saved-state-evidence.json) |
| `staged-WaterRuntime.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/staged-WaterRuntime.java) |
| `unaccepted-drain-experiment.patch` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/drain-reload-audit-2026-10-22/unaccepted-drain-experiment.patch) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Drain reload audit and unaccepted lifecycle candidate — 2026-10-22

Production base: `2c431bb9a4cb922dae5b441c29676ad9d9a6bc5a` (coordination-only successor `fb35bd2f83d2a58289405b402a2abe429281195d`). **No production solver change or installer is accepted by this checkpoint.** Main and supplied alpha.24 bytes, renderer modules, and frozen V1/V2/V3 geography are unchanged. The prior foreground lease expired before this isolated audit began.

## New verified findings

The failed reload world was inspected directly from saved End region files, without running Minecraft, loading chunks, or changing the saves. In the failed candidate, the long tube's only unit remains at `(4096,200,8)`; its short control's unit is at the bottom `(8208,198,8)`. In the prior passing no-unload control, the long unit instead reaches `(4353,198,8)`. Both inspected tubes contain exactly one saved unit in each of the three inspected worlds. Thus the reload failure is not merely a stale diagnostic after successful movement: the long route did not resume. This is **new forensic inspection of prior saves**, not a new native run or continuous per-tick conservation test. See [saved positions and region hashes](saved-state-evidence.json) and [the read-only inspector](inspect_closed_save.py).

The staged listener calls `resumeDrainWaiters` directly from `ChunkEvent.Load`; that calls `activate`, which queries the level. The [NeoForge 26.1.x primary source](https://github.com/neoforged/NeoForge/blob/02fa4bbcb70cf41520eaf0ea3979d2f4b2b3d5b9/src/main/java/net/neoforged/neoforge/event/level/ChunkEvent.java) documents that this callback precedes FULL promotion and requires delaying level interactions. Its contract was read directly during this audit; no upstream implementation was copied. The retained `WaterRuntime$Events.class` was also disassembled and contains the immediate wake call, so this finding is not based solely on a newer source edit.

A fresh Java17 fixture compiles the **exact extracted** callback, waiter cleanup/resume and activation bodies with world doubles. The old listener attempts a prohibited pre-FULL world query. The separately authored candidate records bounded chunk coordinates during the callback and defers activation to the level stepping hook. Five modeled cases pass: delayed wake and index cleanup; 2,048 duplicate callbacks coalescing; unrelated-load rejection; 128 origins/1,024 links; and 1,025 turnover events exercising the bounded overflow fallback. A repeated empty flush performs no world queries. See [fresh results](lifecycle-results.json).

This proves a lifecycle-contract violation and exercises a candidate correction. It **does not establish that this is the sole cause of the native reload stall**, validate real event ordering/thread delivery, prove full solver correctness, or certify save/reopen. The fixture deliberately models callback timing; its world API and indices are doubles. The existing same-thread delivery guard and unloaded-origin behavior still need native tracing. No native pass is claimed for the correction.

## Preserved work and reproduction

- [Unaccepted original experiment](unaccepted-drain-experiment.patch) applies to the base production WaterRuntime; [its complete source snapshot](staged-WaterRuntime.java) has SHA-256 `7fef33cd462b0d67dde73c12aa167e905705f9b0cb585a73cf764093bb19aae6`.
- [Focused lifecycle patch](deferred-drain-load-candidate.patch) applies **after** that experiment. It is documentation/evidence only, not integrated into `src/main`. The new deferred set is capped at 1,024 coordinates; overflow wakes the bounded current origin set at the stepping hook instead of silently dropping it. Existing transport budgets and quantities are untouched by this focused patch.
- [ClosedWaterChecks.java](ClosedWaterChecks.java) preserves the prior opt-in native fixture, including unload/reload, silent edits, multiple long routes, reopen and the closed stepped basin. It lives outside compiled source sets here.
- Recreate the separate lifecycle candidate with `python3 prepare_deferred_patch.py`, then run `python3 check_deferred_load.py` from this directory. It uses `java com.sun.tools.javac.Main` and Java17 without external dependencies. It compiles selected bodies, **not the complete Minecraft mod**.
- To inspect a retained native world: `python3 inspect_closed_save.py /absolute/path/to/world`. The inspector handles the two known tube geometries only, not arbitrary world conservation. Region hashes in the recorded evidence identify the exact prior saves.

The accepted base source's Git blob `581134f7e05b72190deabf86aa8ebba39739e2d6` matches the repository. The original foreground checkout was read only and its staged source hash was rechecked unchanged.

## Prior failures remain failures

[Prior native matrix](final-drain-matrix.json) records explicit clean passes for no-unload long routes, silent wall edits and actual unload/no-force-load. Reload then failed with one pending entry; the wrapper correctly rejected it even though Gradle returned zero. The preceding baseline also failed to resume after reload. [Failure excerpt](prior-native-reload-excerpt.txt) retains the assertion, crash and shutdown signals. These are the same native outcomes reported previously, not fresh passes.

The earlier 800-tick basin candidate also failed full settling; the wet-prefix probe still had 3,183 upper-shelf units and 685 pending entries. Both retained all 8,192 units at sampled ticks. Their compact failed records are preserved alongside the baseline record. The final matrix stopped at reload, so its later reopen/3,200-tick basin/pressure/filling cases never ran.

## Blocker and next implementation gate

Workspace maintenance removed the private JDK25/Gradle/native dependency environment. Only the system Java17 runtime/compiler remains usable; [environment evidence](environment.json) distinguishes the extracted test from the required MC26.1.2/NeoForge26.1.2.122 build. No new full build, core/module, native, visual, Vulkan, package or performance checks ran in this audit.

Restore that toolchain; instrument actual chunk-load thread, FULL readiness, waiter-key registration/removal and origin availability; then test this candidate against the identical reload scene without origin injection. Require explicit native pass records, per-tick exact quantities, no force-loading, preserved edits, empty final work/watch indices, separate-process reopen, both closed-basin repeats, existing pressure/queue/filling regressions, full build and fresh packaged-class/CRC/native audits. If it still fails, retain the instrumentation and fix the proven event-delivery/registration cause before promotion.

Pressure/drain residency beyond 8,192 nodes, basin settling/output/timing/memory, regional finite/renewable water budgets, enabled-view water/LOD/transparency and coherent geology remain open. No claim of independent 60 Hz execution, owner GPU/FPS, broader performance superiority, or terrain completion follows from these tests. Catalog/species work stays deferred.


<!-- END PRESERVED SOURCE -->
