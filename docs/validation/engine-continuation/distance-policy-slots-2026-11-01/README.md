# Dev141 — bounded section-slot accounting (2026-11-01)

## Problem and scoped change

The old `DistancePolicy.sectionSlots(distance, verticalSections)` multiplied three positive factors in signed `long` arithmetic without checking overflow. For example, `sectionSlots(100000, 2147483647)` returned `-6333515492941474433`, although the actual number of section slots exceeds `Long.MAX_VALUE`. A negative input was also silently treated as a valid window. This helper is currently directly exercised by `RuntimeChecks`; no measured production FPS or memory improvement is claimed.

The method now rejects negative distances/section counts, returns zero for zero vertical sections, and saturates to `Long.MAX_VALUE` if the nonnegative exact product cannot fit in a signed `long`. Existing ordinary inputs, including `sectionSlots(32,48)=202800`, are unchanged. No native water, renderer, V8 or saved-world changes.

## Validation

Exact prior source blob: `ec952430f75c15812ed64d82f9bf15faac6ac681`. Production source SHA-256: `59963b875940bdcf2555dc8d2940e7996c08b92f02b01af07fb37e790a515d3d`. Regression test SHA-256: `3713bef2113ccb0796b5670b4c0d410caddce06d3d11f983dd41314414317e0c`.

Isolated local Java21.0.12.1 compilation of production `DistancePolicy.java` and new `DistancePolicySlotChecks.java`: `javac --release 21 -Xlint:all -Werror`; execution `java -ea -Xverify:all`: **400205 checks passed**. The oracle computes `(2r+1)^2 * sections` using independent `BigInteger` arithmetic, clamps to `Long.MAX_VALUE`, covers signed-invalid and zero boundaries, 100 Cartesian boundary combinations, and 200000 seeded random cases. The original source failed at `(100000,2147483647)`. Three negative controls were rejected: no overflow guard, incorrect saturation value, and disabled negative-input rejection. New suite is registered in `CoreChecks` for a future complete build.

## Build and acceptance limits

Only the two isolated Java files were compiled; **the full `CoreChecks` suite and Gradle/NeoForge build were not run**. Local environment has Java21 but not the required Java25 toolchain, complete private checkout, or cached Gradle/NeoForge dependencies; direct GitHub DNS fails. No new playable JAR or JAR SHA-256. Existing successful JARs are retained, GitHub Actions were not used. Current `gradle.properties` still advertises Dev127 and NeoForge 26.1.2.122; 26.3/26.4 and GPU acceptance remain unverified.

Consumed independent audit `docs/engine-hourly-audits/2026-10-31T1730-dev140/REVIEW.md` (head `837312fca6fa2e0a31362c12b1888be496c46204`). Historical whole-engine grade remains **5.0/10**, not freshly regraded. Next: restore full local Java25 build and qualified JAR, then native V8 persistence, finite-water save/reopen and Vulkan/LOD acceptance.
