# Dev143 independent audit — 2026-11-01 03:30 UTC

## Scope and new commits
Baseline `b693b3e5824974e811b7c269ea2755f3a299bb8c`. Reviewed three new commits through `7c0b4beadc13a7d912637cb99677876d17e19944`: `0fe18010` (lease), `d3ac646d` (negative duration guard and tests), `7c0b4bea` (lease release). Builder lease status was `checkpointed`, `lease_released=true`. No renderer, LOD, terrain, hydrology, or native runtime code changed in those three commits.

## Confirmed defect and repair
The new `SampleStats.of` validation rejects negative benchmark duration samples. However, `BenchmarkReport.write` previously wrote the CSV **before** calling `summary()`, which invokes `SampleStats.of`. A negative or empty sample therefore threw `IllegalArgumentException` after creating a report directory and an orphan CSV. Independent Java21 reproduction: `AssertionError: Invalid report created output directory` (exit 1).

Fix: precompute `reportSummary = summary()` before creating any directory or writing any CSV. Valid reports still produce the same CSV and text content. Add `BenchmarkReportWriteChecks` to permanent `CoreChecks`.

## Executed verification
OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` on isolated copies of `SampleStats`, `BenchmarkReport`, and the regression test.
- Original implementation: fails negative control (exit 1, orphan output).
- Corrected implementation: `BENCHMARK_REPORT_WRITE_PASS checks=12 invalidReports=4 validReports=1 noOrphanFiles=true`.
- Invalid cases include negative-only, negative-first, negative-last, and empty; valid case includes zero and `Long.MAX_VALUE`.
- No production water, terrain, LOD, renderer or GPU behavior was inferred from these isolated tests.

## Build, compatibility and grades
`gradle.properties` still declares `neo_version=26.1.2.122` and Dev127 version metadata; `build.gradle` requires Java25 and Gradle 9.2.1 wrapper. Local environment has Java21, no complete private checkout, no Gradle dependency cache, and Git CLI DNS fails. Thus no full NeoForge build, native water/terrain/V8 save-reopen test, Vulkan/OpenGL/LOD performance benchmark, or 26.3/26.4 compatibility acceptance was performed.

Provisional scoped benchmark reporting integrity: **8.5/10**. Whole-engine **5.0/10 historical baseline**, not freshly regraded. A complete native acceptance grade strictly greater than 8.5/10 remains unverified.

**No qualified new JAR, JAR SHA256, or deletion of an older JAR. No GitHub Actions.** Next: restore full Java25 packaging and verify finite-water persistence, V8 saved worlds, and native Vulkan/LOD performance.
