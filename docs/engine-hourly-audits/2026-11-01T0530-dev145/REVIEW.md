# Deep Primal Dev145 — independent audit

**Audit time:** 2026-11-01 05:30 UTC (2026-10-31 22:30 PDT).  
**Previous audit:** Dev144 at `2ccca88839f8d94436290349b3f9719f4255e5bb`.  
**Audited source head:** `72ff59c8d1e9908323984bd38dbb43dafd202ae6`.  
**Delta:** three Builder commits (`8c19ce0b`, `fdc4116c`, `72ff59c8`), five affected paths.

## Change review

The Builder corrected `TickWindow.record(long)` to reject negative elapsed tick durations without advancing its bounded ring or creating false zero-duration samples. Its new `TickWindowDurationChecks` is registered in `CoreChecks`. Writer lease is checkpointed and released; no overlapping Builder scope was modified. The server telemetry and worldgen probe both use `TickWindow`; the changed class remains a primitive ring without platform dependencies.

## Independent reproduction and verification

Retrieved production `TickWindow.java` and `SampleStats.java` from the audited GitHub head and confirmed local Git blob identities exactly: `d08f99ca2a115c6e418307fd6bbcbcd26d8f460d` and `6a0d50aede879044f2945a6500964490c6495ea7`.

Strict Java21 compilation with `javac --release 21 -Xlint:all -Werror` passed. Independent `java -ea -Xverify:all` test passed **1,072,697 assertions** across **384,000 operations** (274,128 valid, 109,872 rejected negative), **6,000 clears**, 3,000 seeded trials, and ring capacities 1–65. Oracle independently retains only the most recent valid samples and sorts them for p50/p95/p99/max comparisons.

Three deliberate defective variants were compiled and **all rejected**: negative-to-zero, throw-on-negative, and advance-ring-on-negative. The production fix is therefore corroborated by independent negative controls. No additional production defect was confirmed in the changed scope. The original Builder suite reports 688,297 checks in its committed evidence; this audit does not present that Builder run as an independent execution.

**Evidence ZIP:** `DeepPrimal_Dev145_IndependentAudit_20261101T0530_Evidence.zip`, SHA-256 `f17950ec932c2f1b0863e76bd1b282445ab2a0b109dc9d689244434fa0b5535c`. Contains exact source snapshots, independent harness, pass log, mutant failure logs and manifest.

## Broader acceptance and compatibility

- NeoForge pinned by repository `gradle.properties`: `26.1.2.122`; mod version remains `0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`. Repository `build.gradle` requires Java25 and Gradle wrapper 9.2.1. NeoForge 26.3 upstream uses Java25; its published artifacts do **not** establish Deep Primal compatibility.
- Native Minecraft finite-water conservation, terrain seams, V8 saved-world reopening, Vulkan/OpenGL, LOD, and hardware FPS were **not exercised** in this source-only audit. No compatibility or performance claims are made.
- Local environment: Java21 only, no Gradle binary or cached dependencies, no complete repository checkout; `git ls-remote` cannot resolve github.com. Authenticated GitHub connector is usable for reading and this report.
- **No full Java25/NeoForge build, no fresh playable JAR and no JAR SHA.** Superseded JARs were not deleted. No GitHub Actions used.

## Grades and next gates

**Scoped tick-window admission correctness: 8.5/10 provisional**, supported by exact source identities, strict compilation, independent oracle and 3/3 mutation rejections. **Whole-engine 5.0/10 historical baseline**, not a fresh whole-engine grade; no unsupported pass for untested areas.

Next: restore complete Java25 checkout/Gradle dependencies, build and verify a playable JAR (including SHA-256), retain the previous qualified JAR until the replacement is verified, then execute native water/persistence and Vulkan/LOD acceptance.
