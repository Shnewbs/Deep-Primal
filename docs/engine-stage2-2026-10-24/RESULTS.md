# Stage-two partial checkpoint: executed evidence

## Acceptance decision

The graph/profile infrastructure passes its correctness and packaging checks. **V8 is not integrated or enabled. Performance and lattice topology block promotion.** The last complete engine grade stays **50/100 (5.0 average), Tier 2/5**. The owner's continuing acceptance target is at least 80/100 across the same ten categories. This checkpoint neither regrades unused features nor declares stage two complete.

## Tests and package identity

Full uncached `:build :visualValidationClasses :noiseBenchmarkClasses`: **33 tasks executed, BUILD SUCCESSFUL in 2m24s** (145.1s wrapper elapsed). The current-proxy compatibility wrapper, JDK 25/JDK 21 and Gradle 9.2.1 are the same private toolchain as stage one.

```text
NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64
DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784 overhangColumns=5
minHeight=20.93897487422737 maxHeight=325.50950620538816
fingerprint=c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26
NOISE_PROFILE_CODEC_PASS cases=21 longSaltExact=true invalidRejected=true
DENSITY_GRAPH_CODEC_PASS cases=34 roundtripSamples=2048 invalidRejected=23
WORLDGEN_BENCHMARK_PASS rows=132 replayHashes=12
```

The graph checks include independent affine/warp formula expectations, arithmetic and interpolation endpoints, cycles, references, unreachable/duplicate nodes, arity, invalid numeric/domain/range limits, depth/work budgets and immutable concurrent replay. The shape corpus is four seeds × 128 columns. Five columns contain multiple solid/air transitions; this is mathematical evidence of non-heightfield occupancy, not five artistically approved overhangs or proof of no detached blocks. Height refers to the recipe reference surface. Shared X/Y faces include negative and near-world-limit coordinates; the underlying stage-one lattice also retains its original X/Y/Z boundary tests.

Independent javac: **57 core sources / 101 class matches**. The development Linux package passes **518 project class matches, seven outer/nested archive CRCs and exclusion of 166 private test/benchmark class paths**. All **509 prior production class files** match stage one byte-for-byte. Nine new density/profile classes are added. Artifact SHA256: `5f0a969a1f45ad8e42b06462a06175aa3ccf2a484ed5bd0f74986b5c53436a40`; 126,196,181 bytes. This is a private validation package, not an owner release.

The native server verifies selected-JAR hashes for the graph, recipe, codec and existing generator/core. It executes codec tests and the existing V7 chunk benchmark, saves all dimensions and stops normally with no crash reports. All 12 replay hashes match the released alpha.28 fixture. It **does not create V8 blocks**. The offline auth service logs a public-key DNS/network failure; no network-auth success is claimed. Gradle retains its existing deprecation warnings, and the private daemon log reports a shutdown registry-integrity warning; the explicit build/package/class/native records remain the scoped acceptance evidence.

Across default JIT, `-Xint` and `-XX:UseAVX=0`, 2,048 density samples over four seeds and floating off/on configurations produce the same SHA256: `d5b932bd0699645d461e8a939e0760c7759ab0dc9565866623d225e4a283f10f`. This is one Linux x86_64 host, not cross-machine certification.

## Windows isolated native preview

On 2026-10-24, the opt-in dedicated-server check ran against a temporary `ProtoChunk` at chunk `(-2, 3)` with V8 seed `20261020`. It compared every checked block against both the exact occupancy and the independent base-column adapter, then checked the two worldgen heightmaps and the two-layer bedrock floor:

```text
DENSITY_CHUNK_NATIVE_PASS blocks=196352 columns=256 baseColumnParity=true occupancyExact=true bedrockBarrier=true heightmaps=true
```

This is a passing adapter parity check for one chunk, not a registered generator, multi-seed/chunk-boundary acceptance, visual review or whole-engine performance result. `.\gradlew.bat coreChecks build -PvulkanPlatform=windows --no-daemon` also passed; the produced Windows Vulkan development JAR is 124,999,338 bytes with SHA256 `f43f6ad9db0e2d3ee87ee1aacaf4326bbd9d58c1b3b802f52f20959305841cfa`. Its outer and nested archives pass CRC checks, and the packaged materializer, hydrology refill, boat fix and animated-water shader were verified. The artifact is staged in the session Library; no live client/GPU acceptance is claimed.

## Benchmark results

Same Linux AMD EPYC 9V74 container / Java 25.0.4.1 as stage one; not the owner's 8060S system. No competing timed workload. Component workload is a full 16×768×16 volume, 196,608 consumed scalar results per iteration, six warmups/mode and 16 measured unique positions/mode with alternating order, three fresh JVMs. The recipe has 56 nodes, depth 20 and a maximum 164 node visits/sample. The lattice includes construction and interpolation of all requested samples. There is no native block, surface, feature, water, lighting or IO work in these numbers.

| Component | Fork 1 mean / p95 ms | Fork 2 mean / p95 ms | Fork 3 mean / p95 ms | Mean allocation bytes |
| --- | --- | --- | --- | ---: |
| Exact recursive graph | 1137.871 / 1379.704 | 1057.993 / 1258.540 | 1069.952 / 1185.428 | 10 |
| Lattice construction + sampling | 18.699 / 23.941 | 17.667 / 21.221 | 18.876 / 24.477 | 19,472 |

**Both paths fail the performance goal before native materialization is included.** Shared horizontal expressions are repeatedly evaluated; the scalar recursive program is a correctness baseline, not a production-efficient evaluator. Future work needs bounded reuse/compiled evaluation, per-field sampling policies and native measurements. Merely moving these costs to a worker does not make them acceptable.

The unchanged V7 native workload averages 14.529 ms over 96 warm unique chunks, p95 17.795 ms/max 33.955 ms, about 2.314 MB thread allocation/chunk. It includes fill/authored decorations and excludes ProtoChunk allocation, host biome population, lighting, IO and status scheduling. This single run is consistent with prior costs and earns **no performance-improvement claim**.

## Approximation self-audit

The first four nearby tiles produced RMS 0.000003412/max 0.000006891 and zero occupancy disagreements over 393,216 samples. That limited corpus under-exercises the 3D terrain. It is retained in the original timing outputs and **rejected as broad topology acceptance**.

The expanded deterministic corpus selects low, high, strongest inland canyon and strongest surface modifier sites from 128 candidate columns for each of four seeds: **16 tiles, 1,572,864 exact/lattice comparisons**, including 193,265 samples with absolute exact density below 1.

```text
DENSITY_STRATIFIED_ERROR_COMPLETE samples=1572864 nearSurfaceSamples=193265
rms=0.007190563 max=0.106991034 signMismatches=906
```

**906 solid/air decisions differ.** This can change thin roofs, entrances or small formations. The coarse lattice is unaccepted for final occupancy. These measured maxima are corpus observations, not analytic error bounds. The expanded diagnostic was added after timing collection, compiled separately with javac and run against the unchanged verified production core. The exact timing-source revision is retained under `runners/timed-source/`; the timing loop and production fields did not change.

## Next implementation priorities

1. Continue optimizing exact job-owned graph sampling; current full-volume sampling remains above the component target. Measure allocation and full-volume distributions against exact scalar output oracles; do not change the recipe to hide the cost.
2. Define a conservative refinement/topology policy; compare exact final occupancy around roofs/surfaces and planned hydraulic barriers. An observed error threshold alone is not a safety proof.
3. Extend native V8 parity coverage across seeds, chunk boundaries, order and reopen, then snapshot the complete graph into a distinct saved revision before registration/default changes.
4. Couple planned river/basin carving and aquifer barriers, then continue climate/ecotones, cave families, structures and the current-version/optimization stage. Preserve all V1–V7 geography and the unpromoted R57 LOD experiment.

No new client/GPU, playable V8, latest-version, cross-machine, full-engine or owner-hardware acceptance is claimed.
