# Dev143 isolated engine performance-statistics duration guard

2026-11-01 UTC. Baseline `b693b3e5824974e811b7c269ea2755f3a299bb8c`; scoped writer lease `0fe18010580eb06fb7bcca974d44e3cfff316833`. Last independently reviewed audit: `docs/engine-hourly-audits/2026-10-31T2330-dev139/REVIEW.md` (whole-engine 5.0/10 historical).

## Defect and scope

`SampleStats.of(long[])` previously accepted negative elapsed nanoseconds and could publish negative median/max benchmark latency. Negative elapsed time is invalid telemetry, not an unusually fast completed request. Reject all arrays containing a negative sample with `IllegalArgumentException`, without mutating the caller's array. All nonnegative arrays, zero durations and `Long.MAX_VALUE` retain the existing nearest-rank percentile calculation. Register `SampleStatsDurationChecks` in `CoreChecks`.

Production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/SampleStats.java`.
Test: `src/coreChecks/java/com/shnewbs/deepprimal/core/SampleStatsDurationChecks.java`.
Integration: `src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java`.

## Executed validation

Local Linux OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: `SAMPLE_STATS_DURATION_PASS checks=120009 randomizedArrays=20000 negativeCases=6 inputImmutable=true`.

Independent sorted-array nearest-rank oracle for 20,000 seeded nonnegative arrays, six equality/parity assertions per trial. Six negative input cases, empty input rejection, zero durations, extreme `Long.MAX_VALUE`, input immutability. Original production behavior was recreated as a negative control; the regression failed with `AssertionError: Negative duration accepted: [-1]` (exit 1). Patched code passed (exit 0).

Local candidate production SHA-256: `c2b1eebd888b3b98d661e2d841354fdb5ff2363ac72126ebd8b07ccafc5b880c`.
Local candidate test SHA-256: `249d577cc93dc9513a98e6642169af26984d77a3ea2d11d0bfcc1165f8f109b4`.

## Unmet gates

No complete local private checkout, Java25, Gradle/NeoForge dependency cache, or native Minecraft runtime. Git CLI DNS: `Could not resolve host: github.com`. No complete NeoForge JAR, game JAR SHA-256, native Vulkan/LOD benchmark, water persistence or whole-engine regrade. No GitHub Actions. Retain previous successful JAR; do not delete it. No release artifact is claimed.
