# Expanded replica — docs/engine-review-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-review-2026-10-24/README.md) |
| Source identity | git: docs/engine-review-2026-10-24/README.md |
| Preserved source SHA-256 | `4b1b9419f4aa8f25c58057aecbd6fc3e8af079726f8cbac5475a3ff2e3cced07` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal staged engine work and strict review”. Its original opening states: “Baseline under review: released alpha.28 / V7 at main f141c0b4d058abb311cb088e4f17a575685a2939. The development source is alpha.29-dev. Stage 1 adds a noise library and validation tools; it does not enable V8 terrain or change any saved world. Stages 2–6 and the Minecraft 26.3 adapter remain open. This is not a AAA-grade release.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `NoiseCoreChecks`, `WorldgenBenchmarkChecks`, `EngineChunkGenerator.fillFromNoise`, `DEEPPRIMAL_WORLDGEN_BENCHMARK`.

## Research expansion — gaps and details to deepen

- Deepen exact-versus-approximate density evidence and keep a pure sampler result separate from native terrain materialization.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version arithmetic order, lattice spacing, graph limits, interval certificates, and exact fallback behavior; preserve a scalar reference oracle.
- Document whether a reported speedup excludes materialization, host biome/light/status work, allocation, or cold regional construction.
- Carry the documented approximation disagreements and stage-two integration block as historical evidence. A future optimization must compare sign/occupancy decisions independently and meet native cost and compatibility gates before promotion.

## Research expansion — acceptance checks

- Exercise zero crossings, negative/far coordinates, partial vertical sections, overflow endpoints, uncertain intervals, and all sampling orders.
- Compare actual generated blocks, base-column queries, heightmaps, lighting invalidation, and save/reopen at the real adapter boundary.

## Research expansion — source anchors

- Deep Primal staged engine work and strict review
- Profiling and limitations

Related design: [terrain water](../../../../chapters/terrain-water.md) · [architecture authoring](../../../../chapters/architecture-authoring.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `examples/noise-profile.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-review-2026-10-24/examples/noise-profile.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal staged engine work and strict review

Baseline under review: released alpha.28 / V7 at main `f141c0b4d058abb311cb088e4f17a575685a2939`. The development source is alpha.29-dev. Stage 1 adds a noise library and validation tools; **it does not enable V8 terrain or change any saved world**. Stages 2–6 and the Minecraft 26.3 adapter remain open. This is not a AAA-grade release.

Read [the architecture proposed before coding](ARCHITECTURE.md), [the evidence-based review](REVIEW.md), and [stage 1 results](STAGE-1.md). The delivered alpha.28 installer stays the owner preview while these stages are gated. No new owner installer is supplied by this checkpoint.

## Setup and checks

Use Java 25 and the repository Gradle wrapper with the current 26.1.2 adapter:

```sh
./gradlew build
./gradlew :noiseBenchmark
./gradlew :visualValidationClasses
```

`build` runs the dependency-free core tests and module-boundary checks. `NoiseCoreChecks` is included in the main CoreChecks runner. `noiseBenchmark` is intentionally separate from correctness checks: latency is environment-dependent and benchmark noise must not fail normal builds. It measures one field over 16x768x16 samples, not a complete Minecraft chunk. Run multiple fresh JVMs on an otherwise idle host and retain every result, including slow ones.

The `WorldgenBenchmarkChecks` private server fixture measures actual `EngineChunkGenerator.fillFromNoise`, including existing decorations and excluding chunk construction, native biome population, lighting, IO and the host status graph. Set `DEEPPRIMAL_WORLDGEN_BENCHMARK` to a result path and use a disposable packaged-validation server. It must reach `WORLDGEN_BENCHMARK_PASS`, then save/stop normally. Only selected-JAR runs with matching loaded class hashes count. `DEEPPRIMAL_NOISE_CODEC_CHECK=true` enables strict JSON codec tests on the development package. Production world generation is unchanged by these fixtures.

## Stage-one API

`DensityField` is a pure `double sample(x,y,z)` interface. `SeededNoise3D` implements immutable genuine 3D gradient noise with fixed arithmetic order, fixed gradients, a 64-bit seed and independently salted octaves. The new algorithm has version 1 and a regression fingerprint. It never changes the old `TerrainNoise` implementation. Inputs must be finite and within ±32,000,000 on every axis. Output is bounded to [-1,1]; normalized octaves retain that range. No trig gradients, mutable RNG or shared cache is used.

```java
var field = new SeededNoise3D(worldSeed, NoiseSpec.DEFAULT);
double value = field.sample(worldX, worldY, worldZ);
var tile = DensityGrid.sample(field, chunkX * 16, -128, chunkZ * 16,
                             4, 8, 4, 5, 97, 5);
double approximate = tile.sample(worldX, worldY, worldZ);
```

The tile includes both boundary faces; all origins must align to their world-lattice spacing, including at negative coordinates. A standard-height tile retains 2,425 doubles, 19,400 raw payload bytes, plus its small object/array overhead. Adjacent tiles evaluate the same world coordinates, and endpoint interpolation preserves bit identity. No cross-tile/global cache or mutable array is exposed. Each tile is immutable and safe to share between jobs.

**Interpolation is an approximation.** Exact tile nodes match the scalar field; interior values need not. A 4x8x4 lattice must not be used blindly for thin high-frequency features or cavern/roof topology. Stage 2 must choose per-field sampling density and measure native threshold errors. Trilinear interpolation is C0 continuous, not a guarantee of continuous derivatives. The tile has explicit bounds; samples outside fail rather than wrapping or silently clamping.

`SeededNoise3D.fill` writes an x-fast primitive double array (`(z * sizeY + y) * sizeX + x`) with the same arithmetic and bit results as repeated scalar calls. It validates the entire request before touching output. Positive finite step sizes, valid output ranges and at most 1,048,576 samples are required. The simple loops are friendly to optimization, but no explicit SIMD acceleration or whole-chunk speedup is claimed.

## Configuration reference

`NoiseProfileCodec.CODEC` serializes the stage-one `NoiseSpec`. [Example JSON](examples/noise-profile.json) is a codec example, **not an active datapack hook**. It is not registered into V7 world settings. Stage 2 must add a versioned saved graph/profile before a datapack can enable it.

| Key | Default | Validation |
| --- | --- | --- |
| frequency_x | 1/192 | Finite 1/1048576..16 cycles/block |
| frequency_y | 1/96 | Same bounds |
| frequency_z | 1/192 | Same bounds |
| octaves | 4 | Integer 1..8 |
| lacunarity | 2 | Finite 1..4; every highest-axis octave frequency <=16 |
| gain | 0.5 | Finite 0..1 |
| salt | "0" | Decimal **string**, exact signed 64-bit integer |

Unknown keys, invalid numeric types, nonfinite/range errors, fractional octaves and malformed/overflowing salts fail decoding with errors. The salt string prevents JSON floating-point rounding from corrupting values above 2^53. Encoded profiles include all keys; omitted keys use the documented defaults.

## Extension guide

A mod may implement `DensityField` and sample it into a bounded `DensityGrid`. Providers must be pure functions of immutable seed/profile/world coordinates. They must not read a mutable world, use shared RNG state, mutate neighboring chunks, block on IO, return nonfinite values or hide unbounded caches. Tests should include negative coordinates, shared faces, out-of-order calls and concurrent replay. Grid construction accepts arbitrary providers and checks each returned value for finiteness.

This first-stage interface is an experimental extension surface, not a stable complete worldgen plugin API. It does not supply registry lifecycle, density-graph compilation, structure hooks, feature ordering or a native scheduler. Those belong to the later stage contracts in ARCHITECTURE.md. Published geography stays versioned; changing a field in an existing saved revision is prohibited.

## Profiling and limitations

Benchmark outputs distinguish exact scalar/bulk evaluation from lattice construction plus sampling. Keep approximation error alongside timing. Native profiling uses a separately attributed JFR run so instrumentation timings are not pooled with ordinary benchmark samples. Review source/runtime evidence together; CPU sampling cannot establish allocation totals or absolute frame budgets by itself.

Cross-JIT determinism on one x86_64 host is not cross-machine verification. Linux/Windows and x86_64/ARM replay fixtures, the owner's AMD 8060S gameplay, sustained memory/latency, 26.3 integration, full density/cave/hydrology and structure work remain open. Do not award those features because this library can eventually support them.

## Retained raw evidence

[Download the audit evidence ZIP](https://chatgpt.com/library?file_id=libfile_e2cd7d865cb481918de7534dc3bafe47&account_id=personal). It includes sealed native console logs, benchmark measurements, the JFR recording and a SHA256 manifest. Source and compact results are versioned here; the archive contains no owner installer.


<!-- END PRESERVED SOURCE -->
