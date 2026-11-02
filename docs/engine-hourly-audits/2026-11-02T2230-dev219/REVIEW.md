# Dev219 — Independent Deep Primal audit (2026-11-02 22:30 UTC)

## Scope, baseline and commit review
- Baseline: Dev215 audit commit `83f301324c64d2a285236e78bd410cf693024031`.
- Reviewed through: `d8db315565c5656499dfe9f23aa51c83b0962dae` (7 commits ahead, 0 behind according to GitHub compare).
- Production change: `engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainNoise.java` (blob `113aa9610c85a636b3a537d2f6fd330afee68c41`); registered test: `src/coreChecks/java/com/shnewbs/deepprimal/core/TerrainNoiseLatticeChecks.java` (blob `4bb86a3f4ae37b02af81dfe26c643613fc3ecfcc`); registration in `TerrainV7Checks.java`.
- Dev217 fixes signed-int saturation in horizontal noise lattice and preserves long vertical interpolation; concurrent Dev216 water telemetry claim was inspected but no WaterSimulation or CoreChecks files were edited. Commit messages and file stats checked via GitHub compare.

## Independently executed verification (OpenJDK 21)
- Strict `javac -Xlint:all -Werror` compilation and `java -Xverify:all` passed for independently transcribed algorithm-equivalent fixtures.
- Registered `TerrainNoiseLatticeChecks` regression mirrored from GitHub: **1,240,000 checks passed**.
- Additional independent randomized world-edge, determinism, finite field/volume, 2^32 lattice periodicity and long-vertical-lattice checks: **1,000,000 assertions passed**.
- Historical versus candidate ordinary-world differential: **900,000 double outputs compared bit-for-bit**, zero mismatches across shifted/unshifted and extreme seeds.
- Deliberately reverted saturated-int-lattice behavior was rejected with `AssertionError: saturated int lattice: -1.364577226467213E9` (expected negative control).
- No further production defect confirmed; no speculative production code changes made.

## Compatibility, architecture and release gates
- `gradle.properties` at reviewed head still pins `neo_version=26.1.2.122`. Root `build.gradle` requires Java toolchain 25 and NeoForge ModDev `2.0.148`.
- NeoForge 26.3 and Minecraft 26.4 are **not** validated by this test. Minecraft 26.4 Snapshot 1 (2026-09-22) made Vulkan the default graphics API and stopped automatic API switching after startup crashes: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1
- Water conservation, terrain save/reopen, Vulkan/OpenGL renderer, LOD seam/FPS, full client/server startup and native benchmarks were not executed. No whole-engine performance or release claims.
- No full checkout, Java 25 toolchain, Gradle wrapper or resolved NeoForge dependencies were available in the local runtime. GitHub clone failed DNS resolution; isolated Java 21 verification is not a full build.
- **No playable JAR or JAR SHA256 produced. No superseded audit JAR deleted. No GitHub Actions invoked.**

## Grades (evidence scoped)
- Terrain-noise lattice correctness: **8.5/10 provisional** (property and regression tests; missing full engine acceptance).
- Full engine: **not freshly graded**; retain historical 5.0/10 baseline, not a new measurement.
- Local release readiness: **2.0/10** (no Java 25 Gradle/NeoForge build or native runtime).
- Overall release decision: **BLOCKED**, not a verified playable build.

## Evidence and next steps
- Local evidence archive: `DeepPrimal_Dev219_IndependentAudit_20261102T2230_Evidence.zip`
- SHA256: `6a5f43c291c80d2ec5782226de4d0fd7179e0d4cb454bc54b2f475217d7926f3`.
- Archive contains replay script, tests, algorithm-equivalent fixtures, positive and negative logs and SHA256 manifest; ZIP integrity checked. Fixtures are **not** claimed to be byte-identical GitHub source.
- Next: restore Java 25 + complete checkout + dependency cache, run `./gradlew clean check jar` without GitHub Actions, verify packaged JarJar metadata, then run client/server startup, water save/reopen, terrain seam and Vulkan/OpenGL + LOD performance tests. Only retire old JAR after new JAR passes tests and SHA256 is recorded.
