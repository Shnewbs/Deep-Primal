# Dev167 — Independent Deep Primal audit (2026-11-01 21:30 UTC)

**Baseline:** `97ac42a77a7984978f4ee64987fa386f7209a020` (Dev165 audit). **Builder head:** `2c704c3999af1bb390afa356c67fabb201d00500` (Dev166). **Scope:** six new commits, deposit worldgen correctness, toolchain/NeoForge version watch, independent regression reproduction. No GitHub Actions.

## Commit review

[Dev165 to Dev166 comparison](https://github.com/Shnewbs/Deep-Primal/compare/97ac42a77a7984978f4ee64987fa386f7209a020...2c704c3999af1bb390afa356c67fabb201d00500) contains six commits and five changed paths: a released bounded writer claim, finite deposit-elevation admission in `DepositPlan.java`, a registered `DepositPlanFiniteAdmissionChecks` regression, and Builder validation documentation. The new `if (!Double.isFinite(y)) continue;` guard is appropriately after final elevation computation, preserving ABSOLUTE/GEOLOGICAL nonfinite *unused value* acceptance. Builder reports 751,471 isolated Java21 assertions and 3/3 negative controls, but the auditor did **not** independently replay that entire suite; the Builder's pre-existing broad oracle timed out. No Vulkan, LOD, native water, or renderer production changes occurred in this delta.

## Independently reproduced defect and correction

Full original production `DepositPlan.java` fetched from GitHub; local `git hash-object` matched blob `5c62e5e16cd8805bdcf2fa98ddae28a661f77f3d`. `chunk()` invoked `surface.applyAsDouble(x,z)` for **every** deposit reference, even though ABSOLUTE and GEOLOGICAL never read the result. A throwing callback caused avoidable worldgen failure in those two modes, and an expensive callback was unnecessarily evaluated.

Corrected `DepositPlan.chunk()` to call the surface callback **only for DEPTH**. New `DepositPlanSurfaceCallbackChecks` covers all four shapes and three references, requires nonzero candidates, verifies that DEPTH still invokes the callback and that ABSOLUTE/GEOLOGICAL never invoke it. Registered in `CoreChecks.java`.

**Local validation:** Exact production `DepositPlan` with minimal deterministic `TerrainNoise` and `Geology` dependency doubles compiled with `javac --release 21 -Xlint:all -Werror`. `java -ea -Xverify:all` passed **1,103,247 independent assertions** and **24 new regression assertions**. Original production failed with `IllegalStateException: unused surface callback executed` in both independent and new regression negative controls. Original and corrected finite-material digest matched **`2c0f4ce0ae9d4ccd`** over **1,103,232 material samples**. The dependency doubles mean this is an exact-class **isolated** validation, not the full production TerrainNoise/Geology integration or native Minecraft acceptance.

## Compatibility and build gates

Repository remains on `neo_version=26.1.2.122`, Gradle wrapper 9.2.1, Java toolchain 25 and ModDev 2.0.148. As of 2026-11-01, Java Edition 26.3 is released, NeoForge 26.3 builds are available, and 26.4 remains in snapshots (see [version tracker](https://gamehosting.pl/najnowsza-wersja-minecraft/), updated 2026-11-01; [NeoForge](https://neoforged.net/)). **No Deep Primal 26.3 or 26.4 compatibility build was performed.**

The isolated environment has Java21, no full checkout, no Gradle executable/distribution or resolved NeoForge dependencies, and no local GitHub DNS resolution. No Java25 build, playable JAR, JAR SHA256, native water conservation, V8 save/reopen, terrain seam test, Vulkan/OpenGL or LOD benchmark. No superseded JAR deleted. No GitHub Actions used.

## Grading and next gates

- **8.5/10 provisional scoped deposit callback admission correctness**, based on original-failure reproduction, full production class under dependency doubles, passing 1.1M+ independent assertions, and finite digest parity.
- **5.0/10 historical whole-engine baseline**, retained rather than invented as a fresh comprehensive grade.
- Renderer, native water, terrain, persistence, LOD and NeoForge 26.3/26.4 remain **not runtime-tested**.

Restore Java25, complete checkout and Gradle dependency resolution; run full `check`, `coreChecks`, packaged JarJar validation and native runtime. Only then publish a verified JAR/SHA256 and retire the preceding JAR.

## Evidence

`DeepPrimal_Dev167_IndependentAudit_20261101T2130_Evidence.zip` SHA256 `72045fa8745e70bf629b4dc5c9628c29ee03bcc8c31a3bf0af6829c6a50dc226`, ZIP CRC verified. Contains original/fixed production class, deterministic dependency doubles, independent harness, new regression, mutation negative control, compile/run logs, manifest and replay script. Kept out of GitHub binaries.
