# Dev217 — Terrain noise lattice saturation repair (2026-11-02)

## Scope and concurrent writer isolation
- Production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainNoise.java`.
- Regression: `src/coreChecks/java/com/shnewbs/deepprimal/core/TerrainNoiseLatticeChecks.java`.
- Registration: `src/coreChecks/java/com/shnewbs/deepprimal/core/TerrainV7Checks.java`, already called by `CoreChecks`.
- Read Dev215 independent audit and the active Dev216 `WaterSimulation`/CoreChecks claim. **No WaterSimulation or CoreChecks edits**; no renderer/Vulkan/LOD files touched.
- Original production Git blob: `cd107471d6700d7491b865a7dbec0e254af46df2`; corrected blob: `113aa9610c85a636b3a537d2f6fd330afee68c41`. Registered regression blob: `4bb86a3f4ae37b02af81dfe26c643613fc3ecfcc`.

## Defect and repair
The original noise code cast `floor(x / scale + phase)` directly to signed `int`, which **saturates** rather than wrapping at the 32-bit hash lattice boundary. The resulting `dx = x - saturatedInt` could exceed 1 by hundreds of millions, generating unbounded noise for fine scales at world-edge coordinates. The new implementation retains the fractional part using the double floor, and casts the integer lattice through `long` before narrowing to `int`, preserving the intended modulo-2^32 hash indexing. Vertical `volume` interpolation similarly uses a `long` lattice coordinate for salt derivation and the original fractional offset.

## Local evidence (OpenJDK 21)
- Strict `javac -Xlint:all -Werror` and `java -Xverify:all` passed for exact baseline and candidate production source plus standalone regression.
- Ordinary-world differential replay: **400,000 seeded cases**, covering `noise`, `field`, `volume` and both legacy/shifted phase modes. Baseline and candidate produced the identical bitwise digest `d55994914622f4d3` (1,200,000 output values).
- Candidate regression: **1,240,000 checks passed**, including signed world-edge coordinates with 0.01 scale, 2^32 horizontal hash periodicity, and vertical long-lattice interpolation beyond signed-int range.
- Original baseline failed the new negative control: `AssertionError: saturated horizontal lattice: 540616.1902527093`.
- Evidence ZIP: `DeepPrimal_dev217_terrain_noise_lattice_evidence.zip`, SHA-256 `ad492cc19438794043cc5e2c4a7c3e560cb26fcb589910781fc0e540dc5a40a0`. It contains the exact source, tests, commands and output logs.

## Limits and release gate
No full Java 25 NeoForge/Gradle compile, playable JAR, client/server startup, save/reopen, native Vulkan/LOD or FPS test was possible: local GitHub DNS failed, and the complete private checkout, Java 25 and resolved Gradle/NeoForge dependencies are unavailable. The `TerrainV7Checks` registration was inspected but not compiled against its full dependency graph. No GitHub Actions were invoked. No qualified JAR was replaced or deleted. This is a **scoped source/test validation**, not a release qualification.

## Follow-up
Re-run `./gradlew clean check jar` under Java 25 with the complete checkout and dependencies; test V1–V7 seed replay, near-border fine-scale terrain, V8 density integration, LOD seam geometry and Vulkan/OpenGL rendering. The historical whole-engine grade is not changed by this isolated correction.
