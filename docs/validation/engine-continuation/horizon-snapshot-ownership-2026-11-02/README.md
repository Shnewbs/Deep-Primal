# Dev176 — Immutable horizon snapshot ownership (2026-11-02 02:06 UTC)

## Scope and motivation

Independent dev175 audit at `870d8f2` concerned drainage float storage; that correction is preserved. This bounded engineering cycle changes only the engine-core `HorizonSamples` snapshot, adds `HorizonSamplesOwnershipChecks`, and registers it from `HorizonChecks.run()`. No native renderer, Vulkan, LOD worker-owned, organics, water solver, saved-world or audit files were modified.

The previous Java record exposed its `short[] heights` and `int[] colors` directly and retained the producer's original buffers. Mutating a producer array after cache publication, or editing an array obtained from a snapshot getter, could change a cached tile after publication. That violates snapshot ownership and could yield nondeterministic far-terrain geometry/color data when reused by the network or mesh worker. This is a source-level vulnerability; no in-game corruption reproduction was performed.

## Correction

- Clone both buffers on construction so the producer cannot mutate a published tile.
- Return defensive copies from public accessors so consumers cannot mutate a cached tile.
- Preserve exact downsample indices, values, detail selection and retained `payloadBytes` accounting.
- Keep `forDetail(sameDetail)` returning the same snapshot without a new object.
- In existing `HorizonChecks`, read the copied buffers once per detail rather than allocating a copy per sample cell.

## Reproducible validation

OpenJDK 21 `javac --release 21 -Xlint:all -Werror` compiled the complete production `HorizonSamples`, `HorizonGeometry` and `FarTerrain` classes plus the new regression. `java -ea -Xverify:all` passed **25,776 assertions** over all three LOD details, producer and consumer mutations, same-detail reuse and downsampling. A strict-compiled old production `HorizonSamples` failed the regression as expected: `AssertionError: Producer changed cached heights` (exit 1). No source excerpt or fake snapshot implementation was used for the positive test.

GitHub identities verified against local `git hash-object`:
- Production `HorizonSamples.java`: `8c0c22d5d67b6aaa3a805dbcebea8bda041c05c3`.
- Regression `HorizonSamplesOwnershipChecks.java`: `f7b1e87f224dd36105effde3716e4cc6f60703b3`.
- Registration `HorizonChecks.java`: `a3989559474292c6a58dd2aeb8c4b3f690551821`.

Reproducible evidence archive: `DeepPrimal_dev176_horizon_snapshot_evidence.zip`, SHA-256 `2f08506908b8b655dd63225daf920170889f60ba41c28a670f72823508c435e4`. Contains full source and test, old negative control, replay script, compile/test logs.

## Build and performance limitations

This environment has Java 21, not Java 25; no complete private checkout, Gradle/NeoForge dependency cache or local GitHub DNS resolution. The full `./gradlew check coreChecks jar`, NeoForge launch, packaged JarJar, Vulkan/OpenGL and native LOD acceptance could not be run. Public getter copies allocate temporary arrays, so the full renderer/network allocation and frame-time impact **must be benchmarked before release**. The isolated test is not a GPU benchmark.

**No playable replacement JAR or JAR SHA-256 exists for this cycle.** Previously successful JARs were not removed. No GitHub Actions used. Historical whole-engine grade remains 5.0/10 pending complete independent revalidation.

## Next acceptance

Restore JDK25 and a complete local repository/dependency checkout; run full `coreChecks` and packaging, then measure tile serialization/mesh-building allocation and latency, real LOD seam continuity and GPU frame pacing. Only replace a retained successful JAR after a verified packaged replacement with recorded SHA-256.
