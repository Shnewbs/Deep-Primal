# Deep Primal Dev144 — independent audit and repair

**Review window:** 2026-11-01 04:30 UTC (2026-10-31 21:30 PDT)  
**Baseline:** `4854c5872913b701d9fbfbcc5a918f195c526cdf` (Dev144 Builder).  
**Delta since Dev143:** two commits: `51a2349e` (claim) and `4854c587` (waterline admission).

## Confirmed production defect

The Builder's new `WaterDynamics.surfaceY` correctly rejects nonfinite or out-of-range *base* Y values, but `SurfaceWaterAquifer` and `DrainageServerChecks` still calculated the exclusive water top using `surfaceY(waterline) + 1`. A finite waterline of `2147483648.0` passes the base-Y guard, returning `2147483647`, then the native int increment wraps to `-2147483648`. This is a genuine boundary admission regression, not a claim that normal Minecraft heights reach that value.

**Reproduction:** isolated exact Builder core source blob `043192412a3225499d321a271022b8cb603c264a`, Java21 strict compilation; output `LEGACY_NATIVE_TOP waterline=2.147483648E9 surfaceY=2147483647 top=-2147483648`.

## Corrections committed to main

1. `WaterDynamics.surfaceTopY(double)` validates the exclusive native water-top Y before incrementing, while retaining all existing base-Y and water-level semantics.
2. `SurfaceWaterAquifer` and the drainage visual validator use the checked API rather than raw `+ 1`.
3. `WaterDynamicsNativeTopChecks` added and registered in `CoreChecks`.

Commits: `ade255be` (new regression), `332873bf` (core fix), `39e74ae1` (native aquifer), `379e953c` (registration), `bf678ac7` (visual validation). All use `[skip ci]`; no GitHub Actions invoked.

## Reproducible isolated verification

- Java: OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- New checked-top suite: **900,037 assertions passed**, including 300,000 seeded randomized inputs, 10 invalid and nine valid explicit boundary inputs.
- Existing Builder waterline suite: **826,871 assertions passed**.
- **3/3 negative controls rejected**: unchecked integer increment, off-by-one exclusive top, and excessive max-Y rejection.
- Corrected production source Git blob `0c4af5e0da8955f98a73dd6ee3a40af90af17cc8` and new regression Git blob `d85cb7a8abb4766b230a798ebc9d4892d97ed1ea` match local tested source.
- Evidence archive SHA256: `7c12d6d8b5fac95d7f330f613ebbc6d635a559d4563e270f49361e8fe2934575`.

## Acceptance gates and grades

| Gate | Result |
| --- | --- |
| New commits and overlapping claims reviewed | PASS (Builder claim released) |
| Overflow reproduced and corrected | PASS, isolated |
| Regression/mutation checks | PASS |
| GitHub main source/commit verification | PASS |
| Native Minecraft worldgen and V8 save/reopen | NOT TESTED |
| Vulkan/OpenGL, terrain and LOD hardware performance | NOT TESTED |
| NeoForge 26.3/26.4 compatibility | UNVERIFIED |
| Java25 full Gradle/NeoForge package | BLOCKED |
| Fresh playable JAR and SHA256 | NOT PRODUCED |

Scoped **native water-top admission safety: 8.5/10 (provisional)**, based only on strict isolated Java tests and negative controls. Whole-engine **5.0/10 is a historical baseline**, not a new score. Unverified systems are not graded as passed.

## Packaging blocker and retention

The execution container has Java21, no JDK25, no complete private repository checkout, no Gradle dependency cache and Git CLI cannot resolve `github.com`. Repository `build.gradle` requires Java25; `gradle.properties` still pins `neo_version=26.1.2.122` and `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`. Newer 26.3 artifacts exist upstream but are not proof of Deep Primal compatibility. No verified JAR or JAR SHA was produced, and no previous JAR was deleted. No GitHub Actions used.

**Next:** restore local Java25/Gradle packaging, exercise native world-height admission and save/reopen, then finite-water conservation, terrain seams, and Vulkan/LOD frame benchmarks.
