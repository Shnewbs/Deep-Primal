# Deep Primal dev168 — finite water-surface interpolation (2026-11-01)

Consumed independent audit: `docs/engine-hourly-audits/2026-11-01T2130-dev167/REVIEW.md`. Baseline: `608c6ee68aef65418f1b323dbee94042bb519223`. Bounded writer claim: `50ad95f7f2135072d8b2c95c86f683980e5aa552`.

## Defect

`WaterTransport.surface()` bilinearly interpolated via `TerrainNoise.lerp(a,b,t) = a + (b-a)*t`. Opposite-sign finite doubles overflow the subtraction and create nonfinite mesh heights. Exact edge interpolation can also return a *wrong finite* height through cancellation (e.g. interpolating -Double.MAX_VALUE to -1 at t=1 returned 0).

## Fix

Exact finite endpoints are retained. Normal finite in-hull interpolations retain the old arithmetic and rounding. Only overflow or out-of-hull results use the stable convex combination. The change is presentation-only; no fluid quantity, chemistry, saved data, or worldgen revision changes.

## Reproducible isolated validation

Java 21 `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **1,933,122 assertions passed**. Covers 810,000 extreme four-corner/UV combinations, 200,000 ordinary randomized bit-exact parity samples, 100,000 cross-page shared-edge checks and volume invariants. Baseline production `WaterTransport.java` Git blob `0ede1c7312e40ecf063b6cc86e5a690cce190443` matched the locally reconstructed baseline. Full production class compiled with a test double implementing the exact production `TerrainNoise.lerp` method. An original-behavior negative control failed at an out-of-hull edge result. The production class and regression were committed together with `CoreChecks` registration. GitHub Actions not used.

## Unaccepted gates

Full Java25/Gradle 9.2.1/NeoForge build, complete private checkout, packaged playable JAR and JAR SHA256, native finite-water save/reopen, actual Vulkan/OpenGL cliff pixels and hardware performance remain blocked. No JAR was created or deleted. Historical whole-engine grade remains 5.0/10, not freshly regraded. This fix does not prove all waterfall flicker is resolved.
