# Dev132 — fail-closed synchronous native rebuild accounting

Date: 2026-10-31 UTC. Source parent: `39fec2915f59e4aaf1760749ff0e823e32560f31`. Latest consumed independent audit: `docs/engine-hourly-audits/2026-10-31T1530-22058a73/REVIEW.md`. Narrow scope: `SyncRebuildBudget`, one dependency-free test, `CoreChecks` registration and this evidence. Claim completed/released in this commit; no overlapping active claim for this class found. No renderer-Vulkan, LOD worker, water, terrain, profile or resource files modified.

## Defect and change

The existing `completed()` accumulated native rebuild durations with signed `long` addition and incremented an `int` counter. A sufficiently large or malformed accumulated duration could wrap negative, so `allows(false)` re-enabled synchronous native section builds in the same frame despite an exhausted CPU budget. Count rollover could likewise re-enable ordinary builds. Saturate elapsed at `Long.MAX_VALUE` and count at `Integer.MAX_VALUE`; preserve negative-duration clamping, reset semantics, strict time/count thresholds and the unconditional player-edit bypass. No changes to normal sub-overflow measurements.

## Executed validation

Local OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`, exact staged production and test source:
- **300,010 checks passed**: elapsed overflow, count overflow via reflection at the boundary, exact cap, reset, player edits and 300 seeded sequences of 500 operations each, compared after every operation with a `BigInteger` oracle.
- Compiled original production source with the new test: **failed as expected at check 1** (elapsed rollover).
- Compiled mutant retaining `count++` with elapsed saturation: **failed as expected at check 9** (count rollover).
- Production source SHA-256: `650fad58e37526bdeba162f78f6673902c31280c509b85bd272d8b8a6dbe4253`.
- Test source SHA-256: `9d6fe9feec993a9a22f3135bfdf530041c69a68f12a6b820663ded221f70a6bd`.

## Qualification boundary

The complete repository checkout, Java 25 and Gradle dependency cache were unavailable; Git CLI DNS to GitHub failed. No full NeoForge build, real Minecraft native rebuild stress, Vulkan/OpenGL profiling, or verified replacement JAR was produced. No old JAR deleted. GitHub Actions not used. This isolated pure-Java test does not establish full-engine performance or readiness. Independent whole-engine grade remains 50/100 (5.0/10), pending a new comprehensive audit.

Next: full pinned Java25 build and packaged native save/reopen, water conservation, terrain-border and GPU/LOD acceptance; profile actual synchronous section-build tails.
