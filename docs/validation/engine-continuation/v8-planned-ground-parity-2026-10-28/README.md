# Deep Primal dev48 — planned V8 ground-material parity (source-only)

**Version:** `0.0.1-alpha.29-dev.48-v8materials.1`. **Production adapter:** `src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java`. Parent Git blob `6a6fafe5330be47c992d200b0cb8dd5a09ca0dca`; final source blob `8f5dafb0636c7cd89a07349f1e85e6ea7537001d`, independently matched against the locally Java25-tested source with `git hash-object`.

## Actual implementation

Added `fillPlannedSurface(GroundSurfaceProfile.Compiled, ChunkAccess, int)`. This **explicit-call-only experimental V8 path** consumes already-certified V8 occupancy/16-cubed sections and paints the exact same topsoil and deeper dirt layers as the existing unplanned `fill(profile, chunk)`. The new path retains the two-block bedrock floor, actual upper-world material guard, underground air/cave termination, signed Y, old air clearing and chunk-local heightmap semantics. It uses **one final heightmap priming** after surface overlay rather than priming the intermediate stone layer too. The old public `fillPlanned(DensityGraph,...)` still primes normally and maintains its existing signature; the new method intentionally has a distinct name to avoid ambiguity for old `fillPlanned(null,...)` call sites. No registered V8 generator/codec, saved-world format, default V1–V7 geography, finite water, approved art, Themes, renderer, or GPU source changed.

## Fresh focused Java25 tests (Minecraft API stand-ins only)

With Eclipse Temurin **25.0.5+8**, `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`:

- **DENSITY_PLANNED_GROUND_PASS:** 1,235,414 checks / 290,560 voxel comparisons / 13,056 heightmap columns / 51 cases, including all-solid, all-air, floating roof, layered cave, noise, signed and boundary heights, budget 0/16/32/64/256, stale-filled chunks, bedrock and top-of-world exclusion. Per controlled API double: legacy profile primes heightmaps twice; new planned profile primes once.
- **DENSITY_BASEHEIGHT_PASS:** 874,642 checks / 34,304 columns / 771,584 voxels.
- **DENSITY_NATIVE_BRIDGE_PASS:** 11,047,019 checks / 1,376,512 native positions.
- **Aggregate 13,157,075 focused assertions**, not a native engine grade or speed benchmark.
- Unmodified parent failed the new named-method regression (`NoSuchMethodException`). **Four separately compiled mutation controls failed as designed**: invalid topmost paint, inverted topsoil, overpainting cave-air gaps, and redundant intermediate heightmap priming. All mutation source/fixture alterations are test-only.

Complete test-only Minecraft stubs, real compiled density/adapter source copies, Java25 test code, failed baseline, passing logs, mutation records and a unified source patch are provided in the development evidence ZIP. None of these double classes are part of the production mod payload.

## Independent audit and still-open gates

Consumed `docs/engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md`, pinned `1aa65a8b1068f6261fe0eb99e6a4394132636fdb`. Its **LOD first-scan retry defect belongs to the separate renderer builder**; this pass deliberately does not edit LOD/renderer files. The **last accepted full external grade is 50/100 (5.0/10)**, carried; no new regrade.

The latest October 27 owner override reserves **full Gradle/NeoForge/Minecraft/GPU/JAR building to the owner**. Thus **not run**: whole Java25 Gradle 9.2.1 build, native 26.1.2 material/heightmap/lighting/topology tests, saved V8 codec/profile/reopen, frozen V1–V7 native replay, Vulkan/OpenGL, Windows AMD8060S, named-hardware tick/frame/memory, water long soak, chemistry/volume and loaded/unloaded persistence. No JAR, Release, GitHub Actions, or CurseForge publication claimed. Do not activate V8 until these separate gates pass. Next: owner-run native V8 material/world-creation/save parity; builder then continues V8 saved-profile ownership and finite-water persistence/conservation acceptance.
