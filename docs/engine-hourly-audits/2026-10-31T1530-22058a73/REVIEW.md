# Deep Primal Dev131+ independent audit — 2026-10-31 15:30 UTC

## Source and nonoverlap
- Audit baseline: `22058a735873addcaed27df235f2889ab029431b` (Dev131 malformed-telemetry correction).
- Baseline Git blob identities verified: `WorkBudget.java` `272d67d8fd99cb1a5c624d6449f9d15df70630a4`; `WorkBudgetTelemetryChecks.java` `313710930a4922c236673e4aa65d394d6a71a507`.
- Builder's Dev131 scope claim is `completed-released`; auditor changed only the two released WorkBudget/test files and this audit document. No water, terrain, Vulkan, LOD, native chunk persistence or Builder lease/workspace touched.

## Confirmed defect
Dev131 correctly reset recovery after invalid telemetry, but neutral **valid** telemetry (frame time between 1.05x and 1.15x target, or heap fraction between 0.75 and 0.85) did not reset the 59-frame recovery streak. A single good frame after such a neutral frame could increase optional background work without 60 consecutive healthy frames, contradicting the Dev131 acceptance description.

Reproduction against exact baseline production blob: 100 overloaded samples reach fraction 0.125; 59 healthy frames; one neutral 18ms frame at target 16.67ms; one healthy frame. Baseline unexpectedly recovers to 0.15. The same flaw occurs at heap fraction 0.80.

## Corrections committed directly to main
- Production commit `8237eed7887f9375a70acd9200861110ad759fa3`: explicitly preserve the good-frame counter until 60 healthy samples, reset it for neutral samples, preserve fast backoff and malformed telemetry behavior.
- Regression commit `237a211746c7ebfd3e4c4a0959eb5a0b63ca9633`: test neutral frame-time and heap interruption and update the independent reference model.
- Remote verified production blob `3e2f4e8a48f79f5edc33f19f4b379b7b1720cac2`; test blob `b91a57d39f7f5860f8a0ba8c9c1ed996091e7968`.

## Reproducible local Java 21 evidence
- OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`.
- Baseline repository checks: **300,031 passed**.
- Baseline + independent neutral-frame audit: **failed as expected** (0.15 actual vs 0.125 expected).
- Baseline + updated repository tests: **failed as expected** at neutral-streak assertion 33.
- Corrected repository checks: **300,037 passed**.
- Corrected independent randomized oracle: **300,005 passed** (300,000 deterministic trials plus boundary/lifecycle assertions).
- Corrected positive checks total **600,042**; two defective controls rejected.
- This is isolated Java core validation, not native Minecraft, NeoForge integration or a full build.

## Compatibility and release gates
- `gradle.properties` still declares `neo_version=26.1.2.122` and stale `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`.
- Java25 full Gradle, native save/reopen, finite-water conservation, renderer/Vulkan/OpenGL, terrain/LOD benchmarks and NeoForge 26.3/26.4 were **not** executed or certified.
- No complete repository checkout, Gradle dependency cache or Git CLI DNS was available in the isolated audit runtime. No verified replacement mod JAR or JAR SHA was produced.
- **No previous JAR deleted**. No GitHub Actions used.

## Grading
- Scoped WorkBudget hysteresis logic: **8.0/10 provisional** for tested pure-Java behavior; not an engine-wide score.
- Whole-engine baseline: **5.0/10 carried forward**, not a fresh comprehensive grade. Target strictly above 8.5 remains unverified.
- Next: obtain complete checkout and Java25/Gradle dependencies; build and launch actual mod, test save/reopen, water conservation, 125+ chunk flight throughput and Vulkan/OpenGL performance before superseding any audit JAR.
