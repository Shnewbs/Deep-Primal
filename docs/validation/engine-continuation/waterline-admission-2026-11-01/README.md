# Waterline native placement admission — 2026-11-01

## Scope
Source-only engine-core safety change for `WaterDynamics.surfaceY` and `surfaceLevel`. Malformed waterline samples previously converted `NaN` to Y=0 and a nonzero water level, while infinities/out-of-range finite heights saturated at integer boundaries. Reject nonfinite values and unrepresentable quantized surface Y before an adapter can place a water block. Native valid waterline rounding, including exact integer waterlines, is unchanged. No saved world format, water chemistry, terrain revision, renderer, LOD or Vulkan code changed.

## Reproduction and checks
- Exact original Git blob `645532d2ce5e2f65964979a57701c9dc833426bd` reproduced `AssertionError: invalid surfaceY admitted waterline=NaN` (exit 1).
- Corrected isolated Java21 build: `javac --release 21 -Xlint:all -Werror` followed by `java -ea -Xverify:all`; output `WATERLINE_ADMISSION_PASS checks=826871 invalidWaterlines=7 finiteParity=275619`.
- Seven malformed values tested in both entry points (NaN, positive/negative Infinity, positive/negative Double.MAX_VALUE, above upper int surface, below lower int surface).
- 25,610 regular quantization cases, 9 boundary cases and 250,000 deterministic randomized finite values compared to the original formulas. All 275,619 finite input cases retain both integer results exactly.
- Original code plus three deliberately defective variants (missing finite check, missing lower bound, missing upper bound) fail the permanent regression suite.
- `WaterDynamicsSurfaceAdmissionChecks` is registered in `CoreChecks`. No native Minecraft or GPU tests were run.

## Environment and limitations
OpenJDK 21.0.12.1. The workspace lacks the full private Git checkout, Java 25 and cached Gradle/NeoForge dependencies; direct Git CLI DNS to github.com fails. No Java25 Gradle build, live save/reopen, native finite-water conservation, Vulkan/OpenGL frame readback, playable JAR or JAR SHA-256 is claimed. Previous verified JARs were not deleted. No GitHub Actions invoked. Whole-engine 5.0/10 remains historical, not regraded.

## Next
Restore a complete local Java25/Gradle build; exercise native worldgen with invalid source samples and save/reopen. Prioritize crash-safe V8 chunk persistence, live finite-water conservation and native Vulkan/LOD hardware gates.
