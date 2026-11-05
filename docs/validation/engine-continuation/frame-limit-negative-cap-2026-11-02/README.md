# Dev200 — invalid native FPS cap admission (2026-11-02)

## Scope
`VisualBudget.frameLimit` previously propagated negative native FPS caps (including `Integer.MIN_VALUE`) into the render limiter. When a positive unfocused/minimized cap was configured, `Math.min(negative, cap)` returned the negative value instead of the configured limit. This is a defensive invalid-input correction; a negative native cap has not been observed in an actual Minecraft client.

Normalize a negative native cap to the existing zero/unlimited sentinel, then apply the configured cap. All nonnegative native limits and all nonpositive override (disabled) semantics remain unchanged. No renderer mixins, Vulkan, LOD, water physics, terrain, worldgen, saved data or organics changed.

## Validation
- OpenJDK 21 `javac --release 21 -Xlint:all -Werror`: passed for exact source and registered regression.
- Java 21 `java -Xverify:all`: `VISUAL_FRAME_LIMIT_PASS checks=100808` (800 enumerated combinations, eight direct invariants and 100000 deterministic seeded scenarios).
- Baseline negative control compiled and **failed as expected** at native=`Integer.MIN_VALUE`, unfocused, no override: expected 0, actual `Integer.MIN_VALUE`.
- Source SHA-256: `a2c96eda236a0fd78005602762780d40bfda6b381f3ba0c60812185e09ce5df1`.
- Regression SHA-256: `a720d23a3d77ff50c0e7a8a9382da975b4ce4e56fe6287c67a0d16686c6d9fe7`.
- Source Git blob: `8c1db5ed00a80bc9bf6d089197d23fccbe42be8a`; regression Git blob: `c0c9003ffd989fdc84c524afee26141e0912da50`.
- Existing `CoreChecks` registration preserved; **full** `CoreChecks` not run.
- Consumed latest independent audit: `docs/engine-hourly-audits/2026-11-02T1330-dev199/REVIEW.md` (waterfall UV fix already landed; no overlap).

## Acceptance limits
The local environment has Java 21 but no complete private repository checkout, Java 25 toolchain, resolved Gradle/NeoForge dependencies or native client/GPU. The local GitHub DNS lookup fails. Therefore no playable replacement JAR, JAR SHA-256, native FPS validation, or new whole-engine grade is claimed. Prior qualified JARs remain untouched. GitHub Actions were not used. Next owner build should validate actual focus/minimize transitions and Vulkan/OpenGL frame pacing; separately restore Java25 and water save/reopen gates.
