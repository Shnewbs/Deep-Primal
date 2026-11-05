# Dev173 — Independent Deep Primal audit (2026-11-02 00:30 UTC)

**Baseline:** `3e9786d2129bd3278bce5a5e6fa6ccaba0e35a4c` (Dev171 audit). **Reviewed head:** `e483ac902aca713a02102367b9a8caba66d80175` (Dev172). **New commits:** 3; five paths changed. The writer lease was `completed-released` before this audit. No GitHub Actions were invoked.

## Changes and ownership

[Compare Dev171..Dev172](https://github.com/Shnewbs/Deep-Primal/compare/3e9786d2129bd3278bce5a5e6fa6ccaba0e35a4c...e483ac902aca713a02102367b9a8caba66d80175) covers finite-coordinate/velocity admission in `WakeField.add`, `WakeFieldFiniteAdmissionChecks` registered in `HydrologyChecks`, validation notes and the released lease. No renderer backend, terrain, LOD, saved-world, or performance implementation changed in this delta. `WakeRenderer.tick()` gates real-world wake admission with `velocity.horizontalDistanceSqr() < .003`; the additional subnormal correction is a numerical core-contract hardening, not a demonstrated in-game artifact.

## Confirmed defect and correction

`WakeField.add` normalized a nonzero finite velocity by `vx / Math.hypot(vx,vz)` and `vz / Math.hypot(vx,vz)` unless `hypot` overflowed. At subnormal magnitudes `hypot` rounds away directional precision. Exact reproduction: `vx = vz = Double.MIN_VALUE` yielded `(dx,dz)=(1,1)` (magnitude `sqrt(2)`) instead of `(sqrt(1/2),sqrt(1/2))` (unit magnitude). This distorts the direction used for wake geometry if such inputs reach the core API.

Correction: reuse the existing max-component scaling path when `length < Double.MIN_NORMAL`, as well as when it is infinite. Preserve the original normal-range arithmetic and zero/nonfinite rejection. No new allocations, changes to ring capacity, or changes to the renderer backend.

## Independent tests and evidence

- Baseline `WakeField.java` exact Git blob `68f2e5bf39176e3e35dab92566ca65360eafe953`; independently reconstructed and verified with `git hash-object`.
- Corrected `WakeField.java` expected Git blob `9b9d2400a57be7e8a0d94babbbb5f978a23726d0`.
- New `WakeFieldSubnormalDirectionChecks.java` expected Git blob `0a980b41ca280e2518bda01ccafbc4ea66eaa7f1`.
- Original exact baseline plus new regression: strict Java21 compilation passed, then the negative control failed with `minimum positive X actual=1.0 expected=0.7071067811865476` (exit 1).
- Corrected complete production class plus prior Dev172 regression: **1,200,269 assertions passed**.
- Corrected complete production class plus new independent regression: **500,262 assertions passed**. Includes 100,000 seeded subnormal direction/unit-vector cases, 50,000 ordinary bitwise-parity cases, signed axes, ring expiration.
- **1,700,531 isolated Java21 assertions passed** under `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`.
- The new regression is registered in `HydrologyChecks.run()`. These tests validate the core Java class, not Minecraft native fluid state or GPU pixels.

## Compatibility, packaging and outstanding acceptance

`gradle.properties` remains pinned to NeoForge `26.1.2.122`; root `build.gradle` targets Java25, ModDevGradle `2.0.148`, Gradle wrapper `9.2.1`, and JarJar module bundling. A 26.3 NeoForge release exists, but no 26.3/26.4 Deep Primal compile or runtime compatibility was demonstrated. The local environment has only Java21, no complete private checkout or resolved Gradle dependencies, and `git ls-remote` cannot resolve github.com. Connected GitHub read/write is available, but does not provide a local clone or build toolchain.

**No full Java25 `check`/`coreChecks`/JarJar build, playable JAR, or JAR SHA256 was produced. No prior JAR was deleted.** Native water conservation and save/reopen, terrain seams, Vulkan/OpenGL pixels, LOD correctness, and FPS/VRAM remain unaccepted. These must not be marked passing from isolated source tests.

## Grades and next gates

- **8.5/10 provisional** for the narrowly tested `WakeField` normalization/admission contract, including a sensitive negative control. Not a GPU acceptance score.
- **5.0/10 historical whole-engine baseline**, not freshly regraded due to missing full runtime and build evidence.
- Priority: obtain full Java25 checkout/toolchain/dependencies, run Gradle `check` and package JarJar, verify JAR SHA256 and test a fresh world, saved-world reopen, waterfall continuity, chunk borders, Vulkan/OpenGL fallback and LOD frame times before deleting any prior audit JAR.
