# Dev93: V8 relief V5 real-VM creation gate (source-only)
Base main (before implementation): `61282dc3949ba5f526b29cae3a25440f138ab3fb`. Source version: `0.0.1-alpha.29-dev.93-relief-core-gate.1`.

## Independent audit consumed
[2026-10-30T0125-32efd75d](../../../engine-hourly-audits/2026-10-30T0125-32efd75d/REVIEW.md) pins `32efd75ddb17817757dc13c33a2da1cc721fcf20`. Auditor independently passed 9,679,628 scalar-oracle assertions on Java25/21, but **did not execute the real production DensityGraph VM**. It carried the 50/100 baseline, not a new grade.

## Production change
New authoring only: `V8GeneratorSnapshot.newReliefV5` now compiles the selected graph with the actual seed BEFORE returning a saveable new profile, failing closed on invalid/unreachable graph, cycles, nonfinite bounds or depth/work budgets. It does not automatically recompile or replace any saved V8 graph. The V5 node recipe, V1-V7 geography, saved V8-r1..r6 snapshots and player edits are unchanged.

## Required full-core test integration
`CoreChecks` invokes new `V8ReliefV5GraphChecks`, adapted from the existing opt-in `tools/tests/V8ReliefV5JoinChecks.java` to use the **production** DensityGraph VM, not the synthetic API double. It checks graph budget and recipe marker; V1/V4 coastal and canyon behavior; V5 bounded relief; signed-world deterministic samples; reversed/parallel exact DensityChunk occupancy and heightmap parity. The full-core runner now has a mandatory test hook for this V5 authoring path, so owner full builds should fail on an invalid real graph.

## Executed checks and exclusions
Linux OpenJDK 21.0.12.1: preexisting exact V5 component compiled and passed four positive processes at 212,735 assertions per process; four compiled mutants were rejected. This tests the **unchanged** production `V8ReliefJoinV5.java` Git blob `b193fa30f25b6ace3dfa8be077f3137f17aa3d2e`, with a synthetic API double, in normal and optimized Python runners. It is regression evidence only, not execution of the new factory or mandatory real-VM test. No real DensityGraph/JDK25/Minecraft/native/GPU test was executed in this builder runtime. Owner controls full Gradle/JAR builds. New compiled-source test remains an **open owner acceptance gate**, and can expose a failing V5 graph; do not enable it as default generation until resolved.

## Next
Have owner run `./gradlew --no-daemon coreChecks` or the project's exact full-core task under Java25/Gradle9.2.1, then resolve any real V5 VM failures before opt-in native saved-policy reopening and signed chunk/old-new border and water-conservation acceptance. Resume ledger-to-native finite-water witness coupling without overlapping foreground water/renderer ownership.
