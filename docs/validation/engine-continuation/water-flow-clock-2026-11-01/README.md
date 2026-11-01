# Dev158 — signed-long water flow animation clock admission

**Date:** 2026-11-01 UTC. **Scope:** WaterFlowTransition only, permanent coreChecks registration and this report. Independent organics and auditor paths were excluded.

## Defect
`now - started` can overflow a signed long when the start and observation lie near opposite signed limits. The old `age()` then clamped a negative wrapped difference to zero, incorrectly leaving a completed water-flow blend active. `target()` used the same unsafe subtraction, preventing a queued direction from advancing. This is an adversarial clock-boundary defect; real gameplay occurrence has not been established.

## Fix
Use a saturating elapsed helper: earlier/equal timestamps return 0; positive elapsed differences at or above 100ms, including signed subtraction overflow, return 100. Ordinary timestamps retain exact integer millisecond values. The same helper controls retarget admission and age/active/queued queries. No water mass or GPU shader changes.

## Evidence
- Java 21 `javac --release 21 -Xlint:all -Werror`: PASS compiling the complete, dependency-free production WaterFlowTransition class with the permanent regression class.
- `java -ea -Xverify:all`: PASS **500,011** assertions (250,000 deterministic ordinary-domain age parity cases, 250,000 active parity cases, 11 boundary/version/queue checks).
- Negative control with original subtraction: FAILED as expected at "overflow elapsed saturates".
- Source SHA-256: `82c49831cd13be5a4feadf920a5b970fa808ca43f1eb0750bd98dc5590892738`; regression SHA-256: `dd3ead10595a5ddd9ae245c88627af0552facddca455e655fd9a46fafeb171f2` (local tested files).
- `CoreChecks` now calls `WaterFlowClockAdmissionChecks.main` so the regression is part of the project's core check entrypoint.

## Acceptance gates not met
No complete Java25/Gradle 9.2.1/NeoForge compilation, playable JAR, Vulkan GPU test, or native world save/reopen validation was possible in this environment. Local Git CLI cannot resolve GitHub; GitHub connector was used for source changes. No Actions run, and no previous successful JAR deleted.

## Follow-up
Restore Java25 and a full dependency-resolved checkout, execute full `coreChecks`, run native water simulation/save-reopen conservation and Vulkan/LOD benchmarks. Historical whole-engine baseline remains 5.0/10, not freshly regraded.
