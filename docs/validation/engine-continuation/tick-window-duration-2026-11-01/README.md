# Dev145 — TickWindow negative-duration admission

2026-11-01 UTC. Scoped baseline `2ccca88839f8d94436290349b3f9719f4255e5bb`; current independent audit: `docs/engine-hourly-audits/2026-11-01T0430-dev144/REVIEW.md`.

## Defect and correction

`TickWindow.record(long)` silently converted a negative elapsed tick duration to zero and advanced the bounded ring. This falsely lowered tick percentiles and could evict valid samples. `SampleStats.of` already rejects negative durations, but the ring hid them. The fix discards negative observations without allocating, advancing the ring, or throwing from a live server tick callback. All nonnegative samples and rollover behavior remain unchanged.

Scope: `engine-core/src/main/java/com/shnewbs/deepprimal/core/TickWindow.java`, new `src/coreChecks/java/com/shnewbs/deepprimal/core/TickWindowDurationChecks.java`, registration in `CoreChecks`.

## Local reproducible validation

- Java 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- Original exact production source fails regression: `AssertionError: invalid empty admission` (exit 1).
- Patched production source passes: `TICK_WINDOW_DURATION_PASS checks=688297 operations=256000 valid=192061 rejected=63939 snapshots=86451 clears=2000 capacities=1..32`.
- Independent `ArrayDeque`/sorted percentile oracle across 2,000 seeded trials and 256,000 operations, including negative durations, zero, `Long.MAX_VALUE`, clears and ring rollover.
- Three compiled defective variants rejected: negative-to-zero, throwing on negative, and advancing the ring on negative.
- Local source SHA-256: `51130de9bd65a91c997b8a51f87b03df82ebdc606b21d137b500ad13dbb2e856`.
- Local test SHA-256: `81864aa66d4ca181d4246bc46aef496aa22f43b67e2c9bb425daec16831f9fa7`.
- Local Git blob identities: production `d08f99ca2a115c6e418307fd6bbcbcd26d8f460d`; test `e45393aa9ff45510353d08d5c24477faabe25cdd`.

## Acceptance limits

Source-only Java 21 tests. No complete private checkout, Java 25, cached Gradle/NeoForge dependencies or local GitHub DNS; no full Gradle/Minecraft build, native Vulkan/LOD, finite-water persistence or new playable JAR/SHA. No Actions invoked. Retain any previous verified JAR until a qualified replacement. Whole-engine 5.0/10 remains historical, not a new grade.
