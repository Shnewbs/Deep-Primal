# Deep Primal Dev149 — independent audit and repair (2026-11-01T10:30Z)

## Scope and source evidence

Prior independent auditor head: `b05984693a47422348665a2c4756a8da41df6efd`.
Reviewed seven Builder commits through `bc5aa432b3298f8efab529af4be6b57e64ed3a7d` (GitHub compare: ahead 7, behind 0). Builder changed `VisualBudget.outside`, registered `VisualBudgetOutsideChecks`, and recorded its validation. The Builder's scoped writer lease is checkpointed and released. No Builder files were modified in this audit.

Independent defect reproduced in `src/main/java/com/shnewbs/deepprimal/BenchmarkReport.java`: `summary()` divided sample count by unvalidated `elapsedSeconds`. Zero produced Infinity throughput; negative elapsed produced negative throughput; NaN and infinity were accepted; a subnormal positive value could overflow throughput to infinity. Because `write()` calls `summary()` before filesystem creation, rejecting these cases there also prevents invalid report artifacts.

## Repair and regression

- Production fix commit: `c7629af210b7d27cd7eadb2b6d37f1693f43f96a`. Reject nonfinite/nonpositive elapsed and nonfinite calculated throughput before report publication.
- Regression test commit: `20e37968b3a871c4905ae9e7b3bd66013b097e82` (`BenchmarkReportElapsedChecks.java`).
- CoreChecks registration commit: `ee93df65063719ec361fe9953c137f08429df6cd`.
- Corrected production blob: `3c0cb5f4b52e1aa2a0a09b94694da025ed942070`. Regression blob: `94ea5a2503b9e056669b35c49b2bd9125fc67888`.
- Test cases: seven invalid elapsed values (0, negative zero, negative, NaN, positive/negative infinity, and `Double.MIN_VALUE`); four valid values including `Double.MAX_VALUE`; no-orphan checks; valid CSV/TXT output. Four deliberately defective variants all failed the test.

## Executed local validation

OpenJDK 21.0.12.1; exact committed production, regression, `SampleStats`, `VisualBudget`, and `VisualBudgetOutsideChecks` blobs compiled with `javac --release 21 -Xlint:all -Werror`, executed with `java -ea -Xverify:all`.

- `VISUAL_OUTSIDE_PASS checks=350057` (Builder's 250k finite parity cases, ~100k independent geometric oracle cases, malformed inputs).
- `BENCHMARK_ELAPSED_PASS checks=26` (7 invalid reports rejected, no invalid report directory, valid report files created).
- Negative controls: original + three partial regressions rejected, 4/4.
- Evidence ZIP SHA256: `63769778e5cfec0ddfbc065e8431ad7fcf47da816ff6415fdefe9857d508fda4`.

## Build and compatibility gates

`gradle.properties` still declares `neo_version=26.1.2.122`, `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`. `build.gradle` requires Java25 and Gradle 9.2.1. This execution environment has Java21, no complete private checkout, no local Gradle/dependency cache, and `git ls-remote` fails DNS resolution for github.com. GitHub connector source reads/writes work, but do not provide a complete local Gradle checkout. **No Java25/NeoForge compile, client/server startup, native water/terrain/V8 persistence, Vulkan/OpenGL/LOD benchmark, or 26.3/26.4 compatibility test was executed.**

No playable JAR or JAR SHA256 was produced; superseded JARs were not deleted. No GitHub Actions were used.

## Grades and next gates

- Scoped Builder visual culling admission: **8.5/10 provisional** (isolated geometric tests, no GPU/native validation).
- Scoped benchmark report integrity: **8.5/10 provisional** (reproduced defect repaired, strict isolated tests, negative controls).
- Whole-engine: **5.0/10 historical baseline only**, not a fresh full-system grade.

Priority: restore complete checkout and Java25/Gradle/NeoForge dependencies, compile and smoke-test a playable JAR and record SHA256, then validate native finite-water conservation, saved-world V8 recovery, terrain-border seams, and Vulkan/LOD performance. Keep existing last verified JAR until a replacement passes.
