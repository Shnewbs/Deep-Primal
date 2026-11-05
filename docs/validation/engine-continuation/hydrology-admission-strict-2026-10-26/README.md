# Hydrology admission validation continuation

Source pin: `08b9ae940f3897e98efa7b1bbd32a6f0a2e17271`. Production remains dev.30; the reservation candidate is still staged and no new JAR is qualified here.

## Implemented

The staged exact-tick harness now checks 1,000 deterministic multi-page cases per variant, including stable headwater ordering with equal heights, unloaded pages, removal of detached cursors, source deadlines and disabled regular scanning. These supplement its 3,360 boundary cases and 100-tick starvation reproduction. Baseline regular visits remain zero; the candidate admits 500 in that reproduction. This is scheduling with deterministic boundary doubles, not Minecraft water transport or a latency benchmark.

A real validation defect was reproduced: the harness used a Python assertion to reject a failed Java process. A controlled JVM exit of 23 caused the old harness to exit successfully and write a passing receipt under both `-O` and `-OO`. Explicit source-shape and JVM-exit exceptions now replace those assertions. The corrected harness rejects the injected failure in normal, `-O` and `-OO` modes, without writing a passing receipt. Genuine Java tests pass in all three modes. Failure controls are intentionally negative tests, not engine acceptance records.

The corrected `probe.py` replaces the staged harness at `../hydrology-recharge-reservation-2026-10-26/probe.py`; the runtime candidate itself is unchanged. Use its required source, source-pin, java-home, fixture and output arguments as documented there. `failure-controls.json` records the six before/after executions.

## Current-source checks and limitations

All 135 isolated core/check Java sources match current Git blob identities, including the latest LodMesher and LodReductionChecks. Java25 compilation passes and all ten LOD geometry signatures match. The complete current-source core suite also passes, exit zero; see core result/log. No renderer code is edited and no renderer benchmark is claimed.

The filesystem had 104 MB free at preflight. Removing an exact duplicate old builder derivative preserved its matching export but did not recover meaningful physical space. New release/build/native qualification remains unfinished; there is no new installer or version bump. Core-only checks must not be labeled a full Gradle build. Restore sufficient private build/output headroom, then run the staged reservation's full Gradle/module/package/source-quota/recharge/save/reopen/shutdown gates before promotion. Real OpenGL/Vulkan visuals, Windows/AMD8060S, multiplayer/soak and hardware latency remain open.

Consumed independent audit: `docs/engine-hourly-audits/2026-10-26T1433-9b26b86/REVIEW.md`, pin `9b26b8619b3a44c1542cc10f40a11658c4cd3cf0`. Carried and last full accepted score remain 5.0/10; no regrade. Current root and water claims were checkpointed at preflight; renderer claim was completed. Personal context supplied no newer direct foreground ownership proof. Auditor run records show completion at 14:41 UTC, which is historical state rather than current worker liveness.
