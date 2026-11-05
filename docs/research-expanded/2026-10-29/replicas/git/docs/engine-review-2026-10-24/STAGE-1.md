# Expanded replica — docs/engine-review-2026-10-24/STAGE-1.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-review-2026-10-24/STAGE-1.md) |
| Source identity | git: docs/engine-review-2026-10-24/STAGE-1.md |
| Preserved source SHA-256 | `d2ba371e55871512da7011b8b3d57912ec38f50ca36768876b6c85a307816759` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Stage 1 — noise core, completed checkpoint”. Its original opening states: “Code and tests are implemented; game-terrain integration is not. Development version alpha.29-dev adds core/density/{DensityField,NoiseSpec,SeededNoise3D,DensityGrid} and the platform NoiseProfileCodec. Existing V1–V7 production classes remain byte-identical to alpha.28. No V8 generator is registered, and this stage does not improve live chunk performance by itself.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `NoiseProfileCodec`, `NoiseSpec.java`, `SeededNoise3D.java`, `DensityGrid.java`.

## Research expansion — gaps and details to deepen

- Preserve the measurement boundary and avoid promoting component allocation or microbenchmark results into full-engine performance claims.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Record workload, source identities, cold/warm/cached state, process forks, output equality, actual device/backend, and instrumented-versus-release mode.
- Publish raw distributions and separate temporary allocation, resident memory, GPU residency, throughput, and end-to-end latency.

## Research expansion — acceptance checks

- Repeat matched baseline/candidate workloads with alternating order and checksums; investigate differences before interpreting timing.
- Measure the actual native load-to-ready or frame path and report p50/p95/p99 with sample counts and failures.

## Research expansion — source anchors

- Stage 1 — noise core, completed checkpoint
- Code
- Known weaknesses and next gate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Stage 1 — noise core, completed checkpoint

**Code and tests are implemented; game-terrain integration is not.** Development version alpha.29-dev adds `core/density/{DensityField,NoiseSpec,SeededNoise3D,DensityGrid}` and the platform `NoiseProfileCodec`. Existing V1–V7 production classes remain byte-identical to alpha.28. No V8 generator is registered, and this stage does not improve live chunk performance by itself.

## Code

- `engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityField.java`: pure sampling extension interface.
- `NoiseSpec.java`: bounded immutable octave/frequency/gain/salt configuration.
- `SeededNoise3D.java`: genuine seeded 3D gradient field, version-one fingerprint, exact scalar/bulk sampling, fixed arithmetic and request validation.
- `DensityGrid.java`: immutable globally aligned, bounded lattice cache with exact boundary endpoints and explicit interpolation semantics.
- `src/main/java/com/shnewbs/deepprimal/engine/NoiseProfileCodec.java`: strict key/type/range validation and exact signed 64-bit salt strings. Codec only; no live datapack registration yet.
- Tests: `src/coreChecks/java/com/shnewbs/deepprimal/core/density/NoiseCoreChecks.java` and private native `NoiseProfileChecks.java`.
- Benchmarks: `src/noiseBenchmark/java/com/shnewbs/deepprimal/benchmark/NoiseBenchmark.java` and private native `WorldgenBenchmarkChecks.java`.

## Executed tests

Full `:build :visualValidationClasses :noiseBenchmarkClasses` passes: 33 tasks executed, 2m15s. Existing terrain, watershed, cave, hydrology, water animation, cache and module-boundary suites pass alongside:

```text
NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64
fingerprint=6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1
NOISE_PROFILE_CODEC_PASS cases=21 longSaltExact=true invalidRejected=true
```

The independent oracle evaluates all eight weighted gradient corners separately; bulk/scalar comparisons are bit-exact. Shared X/Z/Y faces include negative and near-world-border coordinates. Tests include affine interpolation, immutable parallel readers, malformed profiles, oversized grids, output ranges, NaN/infinities and no partial writes for rejected requests.

The same 10,000-point fingerprint matches default JIT, `-Xint` and `-XX:UseAVX=0`. These are three JVM modes on one Linux x86_64 machine, not Windows/ARM verification.

Independent javac compilation of 55 core sources yields 93 matching class files. The development JAR passes seven archive CRC checks, 509 packaged project class matches and exclusion of 162 private check/benchmark classes. All 504 pre-existing production project classes match the released alpha.28 package byte-for-byte. Native benchmark replay hashes match across two released runs, the development run and the separate profile run. The development run also executes the codec checks and saves/stops normally with no crash report. No new client/GPU test is claimed for this unused core library.

## Benchmarks

Host: Linux container on AMD EPYC 9V74, Java 25.0.4.1, Java sees 8 processors. This is not the owner's Ryzen/8060S machine. Measurements below are retained without selecting only the fastest fork.

Component workload: one four-octave field, 16×768×16 = 196,608 samples per iteration; 12 warmups per mode, 24 measured unique-coordinate iterations, three fresh JVMs, alternating mode order. Timing includes consuming the resulting array. Output array allocation is outside the measured interval. The lattice path includes tile allocation/construction and interpolation of all samples.

| Mode | Fork 1 mean / p95 ms | Fork 2 mean / p95 ms | Fork 3 mean / p95 ms | Mean allocated bytes/iteration |
| --- | --- | --- | --- | ---: |
| Exact scalar | 33.472 / 44.603 | 32.084 / 36.776 | 31.765 / 33.990 | 6.7 |
| Exact bulk | 34.099 / 44.568 | 31.456 / 32.581 | 31.665 / 33.854 | 0 |
| Lattice build + interpolation | 2.664 / 3.388 | 5.158 / 5.736 | 5.128 / 5.407 | 19,472 |

The batch API has no consistent latency advantage over exact scalar sampling in this run. Lattice sampling reduces the sampled workload but changes interior values. For the retained 65,536-point error corpus, RMS error is 0.0036002048 and maximum error 0.0134137754. This is one default profile, not a safe bound for all profiles or threshold topology. Tile payload is 19,400 bytes; allocated memory includes object/array overhead. First-fork lattice timing differs substantially from later forks; no stable <5 ms component guarantee is claimed.

These are **not full-chunk timings**. In two fresh packaged alpha.28 servers, actual native fill/decorate averages 14.988 and 14.452 ms across 96 warm unique chunks per run. Cold first-chunk averages 180.753 and 165.892 ms across four seeds per run. Full host biome/lighting/IO/status work is excluded. The unused-library development package averages 15.807 ms warm in its single run; no generation improvement or reliable regression is inferred from that variance because all existing generation class bytes match exactly. See `evidence/native-summary.json` for distributions and allocation.

A separate JFR run yields 76 sampled stacks containing both the benchmark caller and `fillFromNoise`. Leaf samples most often hit `ProtoChunk.setBlockState` (23), `Heightmap.update` (18) and `DepositPlan.Column.at` (15). Counts are samples, not exact timing percentages; untimed replay fills also occur within that caller. No raw profile timings are pooled with normal benchmark runs. Optimize measured materialization/heightmap/deposit work as well as field evaluation.

## Known weaknesses and next gate

- No density graph, domain-warp composition, V8 terrain, biome weights, new caves, structures or async native adapter is implemented by this stage.
- Codec is not a datapack registration. No live configuration setting changes a saved generator.
- Coarse interpolation can erase thin features or change density signs. Stage 2 needs exact/local refinement and topology/error acceptance, with C0 seam tests and native roof/water-barrier tests.
- No explicit SIMD/vector kernel; no claim that the JIT vectorizes gradient hashes.
- No cross-machine bit replay, native scheduler parallel scaling, owner-GPU performance or latest 26.3 adapter acceptance.
- Payload bounds cover individual grids. A future multi-tile cache still needs an explicit global residency/eviction budget and cancellation lifecycle.
- Benchmark corpus is four native seeds in nearby central regions and three component JVM forks; it is not continent-wide travel, long-running memory or total load-to-ready acceptance.

Next stage: bounded versioned JSON density graph and V8 shape integration, preserving every V1–V7 path. The review's performance/threading findings must constrain that design from the start. Keep the new core unenabled until native geometry, saved profile and hydrology gates pass.


<!-- END PRESERVED SOURCE -->
