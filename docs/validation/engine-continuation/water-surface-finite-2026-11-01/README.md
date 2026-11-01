# Dev142 — finite-water surface presentation input guard (2026-11-01)

## Reproduction and correction

The prior `WaterSurfaceTransition` accepted NaN and +/-Infinity as a raw height. An invalid versioned update could poison the interpolation target and the last raw value, and a packet with `version=Long.MAX_VALUE` could block subsequent legitimate revisions. The unversioned interpolation path had the same nonfinite poisoning risk. The constructor could seed a permanently invalid initial height.

The patch replaces an invalid initial height with a finite zero fallback, ignores nonfinite unversioned and versioned updates, and checks validity **before** advancing the revision or expiry state. No native fluid amounts, solver, saved-world schema, mesh format, Vulkan driver or LOD logic changes. The fallback is a presentation safety measure, not a proof that upstream height generation is always finite.

## Local isolated evidence

OpenJDK 21.0.12.1. Compiled the production `WaterSurfaceTransition.java` and the new `WaterSurfaceFiniteChecks.java` with `javac --release 21 -Xlint:all,-cast -Werror`, then executed `java -ea -Xverify:all`. Existing redundant casts in the unmodified methods require disabling the cast-only lint warning; no other warning was suppressed.

`WATER_SURFACE_FINITE_PASS assertions=1000027 finiteParityScenarios=2000 finiteParitySteps=200000 invalidConstructorSanitized=3 invalidVersionedIgnored=3 invalidUnversionedIgnored=3 revisionNotPoisoned=true`

The independent-in-test `BaselineWaterSurfaceTransition` is an unchanged behavioral copy of the pre-fix production class. Finite-input parity compares exact float bits, interpolation, cleanup and offset over 200,000 deterministic operations. Original production failed the new regression immediately on invalid initial height. Three mutants (missing initial fallback, missing unversioned guard, missing versioned guard) were separately compiled and rejected.

## Build and acceptance

Consumed `docs/engine-hourly-audits/2026-10-31T1830-dev141/REVIEW.md` (independent section-slot audit). This is an isolated Java21 correctness check, **not** the full Gradle `CoreChecks` suite, native GPU test, live water acceptance, or playable NeoForge build. The local runner lacks Java25, full repository checkout, cached Gradle/NeoForge dependencies and GitHub DNS; the GitHub connector is used only for source commits. No Actions, no JAR or JAR SHA-256. Preserve previous successful builds. Historical whole-engine grade 5.0/10 remains unchanged.

Next: obtain complete Java25 offline build environment and test actual saved-world water/LOD/render acceptance, then publish a verified JAR.
