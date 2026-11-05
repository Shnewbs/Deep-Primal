# Dev172 — finite water-wake admission (2026-11-02)

Reviewed main `3e9786d2129bd3278bce5a5e6fa6ccaba0e35a4c`, Dev171 independent audit, the completed prior writer lease and the hydrology/wake call sites. This narrow follow-up fixes a previously untested core water-wake arithmetic defect without modifying the renderer backend, native water ledger, terrain, saved formats or organics.

## Reproduction and correction

The prior `WakeField.add` checked `Double.isFinite(x + y + z + hypot(vx, vz) + strength)`. The addition can overflow for individually finite coordinates. `hypot` itself can overflow for two finite, large velocity components. Both situations discarded valid wakes. Validate each input independently, reject nonfinite/zero vectors, and normalize in a scaled branch only when `hypot` overflows. Ordinary finite vectors retain the original `vx / length` and `vz / length` operations exactly.

## Evidence

- Exact baseline `WakeField.java` blob: `571dac721de68ae2021b82f547822f974124eaac` (locally verified with `git hash-object`).
- Java 21 `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **1,200,269 assertions passed** in the new complete-class `WakeFieldFiniteAdmissionChecks`.
- Original class compiled with the same test and failed on `finite position must be admitted despite sum overflow` (exit 1), proving a sensitive regression.
- Tests cover large finite coordinates and velocities, signed directions, nonfinite rejection, zero vectors, unchanged ring-slot admission, 200,000 seeded ordinary samples with bitwise direction parity, and 40-tick expiration. Registered via `HydrologyChecks.run()`.
- This is isolated Java21 validation. It is **not** a full NeoForge/Java25 build, Vulkan/OpenGL GPU run, playable JAR, native save/reopen or broad performance acceptance.

## Packaging blockers and follow-up

No complete private checkout, Java25 toolchain, Gradle distribution or resolved NeoForge dependency graph in this execution container; local Git cannot resolve github.com. Connected GitHub source publication works. No GitHub Actions used. No replacement playable JAR or JAR SHA256; preserve prior verified JARs. Next: full Java25 `check`/`coreChecks`/JarJar, native water save/reopen, actual waterfall pixels and Vulkan/LOD frame-time acceptance. Whole-engine grade remains historical **5.0/10** pending a new independent audit.
