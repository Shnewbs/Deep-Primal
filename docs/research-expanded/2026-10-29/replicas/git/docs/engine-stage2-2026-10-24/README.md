# Expanded replica — docs/engine-stage2-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-stage2-2026-10-24/README.md) |
| Source identity | git: docs/engine-stage2-2026-10-24/README.md |
| Preserved source SHA-256 | `5ba581886dd743ca02c408f03bbce3ddfc80c34e885f45ab3bfd71deaf6d939e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Stage 2 — bounded density programs and experimental V8 shape”. Its original opening states: “This is a partial stage-two checkpoint, not a playable V8 release. The graph compiler, strict JSON codec and experimental shape recipe are implemented. The isolated native preview writer now passes a bounded dedicated-server parity check, but production generator registration, saved generator snapshots, coupled drainage and visual acceptance remain open. Existing V1–V7 generator registrations and delivered alpha.28 installers remain frozen.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityField`, `DensityChunk`, `ChunkAccess`, `DEEPPRIMAL_DENSITY_CHUNK_NATIVE_CHECK`.

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

- Stage 2 — bounded density programs and experimental V8 shape
- Code and API
- Limitations and next acceptance gate

Related design: [terrain water](../../../../chapters/terrain-water.md) · [architecture authoring](../../../../chapters/architecture-authoring.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `examples/density-profile.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-stage2-2026-10-24/examples/density-profile.json) |
| `examples/ground-surface-profile.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-stage2-2026-10-24/examples/ground-surface-profile.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Stage 2 — bounded density programs and experimental V8 shape

This is a **partial stage-two checkpoint**, not a playable V8 release. The graph compiler, strict JSON codec and experimental shape recipe are implemented. The isolated native preview writer now passes a bounded dedicated-server parity check, but production generator registration, saved generator snapshots, coupled drainage and visual acceptance remain open. Existing V1–V7 generator registrations and delivered alpha.28 installers remain frozen.

The owner's updated acceptance target is an evidence-supported **average of at least 8.5/10 across the ten equally weighted review categories**, with **9.5/10 the ideal stretch target**. A perfect 10/10 is aspirational, not promised. The earlier 8.0/10 target is superseded. Score only implemented and verified behavior; an average does not excuse an unresolved severe weakness in an individual category. The last complete audit remains 50/100, average 5.0. No new full regrade is claimed for unused core geometry. The existing hourly build/audit task is enabled with the updated target; completing this checkpoint does not complete that goal.

## Setup

Java 25, current NeoForge 26.1.2 adapter:

```sh
./gradlew build
./gradlew :densityBenchmark
./gradlew :visualValidationClasses
```

The latest-version port is still open. The current development version is `0.0.1-alpha.29-dev.3`. See the [original architecture](../engine-review-2026-10-24/ARCHITECTURE.md) and [strict baseline review](../engine-review-2026-10-24/REVIEW.md).

## Code and API

- `engine-core/.../density/DensityGraph.java`: versioned definitions, immutable linked programs, cycle/reference/range checks and bounded evaluation. Implements `DensityField`; a positive density denotes solid for later terrain consumers.
- `engine-core/.../density/TerrainShapeV8.java`: continentalness, erosion mask, peaks/valleys, warped continental domain, inland mountain/hill relief, canyon bands and genuine 3D surface modifiers. Floating formations are optional and disabled by default.
- `engine-core/.../density/DensityChunk.java`: exact bounded occupancy for one 16×height×16 chunk footprint plus top-exclusive surface columns. This is a pure-core primitive consumed by the isolated native preview, not by the registered V1–V7 generator.
- `src/main/.../engine/DensityChunkMaterializer.java`: isolated native preview writer and base-column adapter that share the exact `density > 0` occupancy rule, enforce the two-block bedrock floor, and prime both native heightmaps. It is deliberately not registered or wired into the V1–V7 generator.
- `src/main/.../engine/DensityGraphCodec.java`: strict JSON encoding/decoding through Mojang's codec API, with useful JSON paths for structural failures. Definitions can be serialized as complete snapshots; this does **not** yet register a datapack or save a generator profile.
- `src/coreChecks/.../density/DensityGraphChecks.java`: formula oracles, malformed graphs, resource bounds, seed variety, density transitions, lattice boundaries and concurrent replay.
- `src/visualValidation/.../DensityGraphProfileChecks.java`: packaged-runtime codec checks. These exercise the real platform codec, not native V8 block generation.
- `src/noiseBenchmark/.../DensityBenchmark.java`: exact graph versus lattice component benchmark, including approximation and solid/air sign disagreement.

```java
var definition = TerrainShapeV8.definition(false);
var graph = definition.compile(worldSeed);
double density = graph.sample(worldX, worldY, worldZ);
double continentalness = graph.channel("continentalness", worldX, 0, worldZ);
var tile = DensityGrid.sample(graph, chunkX * 16, -128, chunkZ * 16,
                             4, 8, 4, 5, 97, 5);
```

Definitions and nodes copy their lists. Compiled arrays are private and never exposed. Sampling uses no mutable RNG, world access or shared cache; one graph is safe for simultaneous readers. Node order is not semantic: references resolve by ID. All nodes must be reachable from the root so misspelled or abandoned definitions fail validation. `channel` samples any named node already reachable from the root.

### Exact job-owned sampling

`DensityGraph.newSampler()` creates a bounded memoized evaluator for one generation job/thread. It reuses exact node values when their relevant coordinates are unchanged, including horizontal fields across vertical column samples, and recognizes a provably-zero multiplicative factor before evaluating an expensive 3D term. It preserves the scalar evaluator as the reference path; no lattice interpolation, epsilon cutoff or density approximation is involved. Samplers reject cross-thread use, while the immutable compiled graph remains shareable.

The integrated core checks compare 1,572,864 recipe samples bit-for-bit, exercise signed zero and subnormal arithmetic, vary graph node order and traversal direction, and test independent concurrent samplers plus the full 128-node budget. This is a validated core API only: the Minecraft generator does not yet call it, no native V8 geometry is materialized, and V1–V7 output is unchanged. The retained paired measurements show that sampling latency is traversal-order dependent and remains above the 5 ms component target; no whole-chunk or in-game speedup is claimed.

`DensityChunk` applies that exact sampler in column-major order to a 16×height×16 footprint (height capped at 2,048), retaining a bounded solid bitset and 256 top-exclusive surface values. Its deterministic iterator visits only solid voxels in z/x/y order without exposing mutable storage. A deterministic 16×64×16 V8 case is checked voxel-by-voxel against the scalar graph and through the iterator; empty, zero-density, negative-height, world-bound, alignment and maximum-height cases are also covered.

The native preview writer now materializes that occupancy into a supplied `ChunkAccess` using temporary stone/deepslate surface materials and a fixed two-layer bedrock floor. Its paired base-column method applies the same exact sign/material rule. An opt-in dedicated-server check (`DEEPPRIMAL_DENSITY_CHUNK_NATIVE_CHECK`) compares every native block to the base column and occupancy, and checks both heightmaps against the top-exclusive density surfaces. It passed on one negative-coordinate chunk (`-2, 3`), checking 196,352 blocks and 256 columns for seed `20261020`. This proves a bounded adapter path only: the preview is unregistered, does not add V8 biomes, water, geology or saved-profile replay, and must not be mistaken for a playable generator.

An additional component run on the current Windows build host used six warmups and 16 distinct full-height (768-block) footprints. Means were 1,190.50 ms for scalar graph evaluation in y-inner order, 32.03 ms for the exact sampler in the same order, 321.75 ms for the exact sampler with x-inner order, and 34.68 ms for exact chunk occupancy materialization. Mean thread allocations were 10 B, 1,968 B, 1,968 B and 27,656 B respectively. The scalar/sampler density sums match bit-for-bit. This short single-host run is not forked performance acceptance; exact sampling/materialization still exceeds the 5 ms component target, and the order sensitivity reinforces the need to match the native scheduler. The 19.04 ms lattice comparison remains approximate and is not eligible for occupancy.

## JSON reference

[Minimal example](examples/density-profile.json): a deliberately simple affine density field for authoring/tests, not the complete V8 recipe. The full recipe is defined in `TerrainShapeV8` and round-trips through the codec in native validation.

| Field | Meaning / validation |
| --- | --- |
| `version` | Integer `1`; future/unknown versions fail |
| `root` | ID of the output node |
| `nodes` | 1–128 node objects; unique IDs and all reachable |
| node `id` | `[a-z][a-z0-9_]{0,63}` |
| node `op` | Operation below; case-insensitive enum name |
| node `inputs` | Ordered array of referenced IDs, omitted for zero inputs |
| node `parameters` | Ordered finite numeric parameters, omitted when empty |
| node `noise` | Required only on noise nodes; strict [stage-one noise profile](../engine-review-2026-10-24/README.md#configuration-reference) |

Unknown keys, null/wrong types, invalid arity, missing references, cycles, duplicates and unreachable nodes fail. The codec compiles a seed-zero validation program before accepting decoded definitions. Noise salts stay exact signed 64-bit decimal strings.

| Operation | Inputs | Parameters | Semantics |
| --- | ---: | --- | --- |
| `constant` | 0 | value | Constant value |
| `x`, `y`, `z` | 0 | none | World coordinate, including transformed coordinates inside a warp |
| `noise_2d` | 0 | none | Seeded 3D gradient noise sampled at Y=0 |
| `noise_3d` | 0 | none | Seeded 3D field |
| `add`, `multiply`, `min`, `max` | 2 | none | Ordered arithmetic |
| `abs` | 1 | none | Absolute value |
| `clamp` | 1 | min, max | Clamp to strictly ordered bounds |
| `smoothstep` | 1 | min, max | Clamped cubic 0..1 transition |
| `lerp` | 3 | none | A, B, weight; weight clamps to 0..1; exact endpoints |
| `warp` | 4 | amplitude | Source, displacement X/Y/Z; source samples at displaced world coordinates |

Graphs have maximum depth 24 and maximum 4,096 node visits per scalar evaluation. This includes repeated DAG references; noise octaves add their own bounded work inside a noise visit. These are resource bounds, not latency guarantees. Conservative interval analysis rejects output ranges beyond ±1e12 and requires warp displacement fields to remain within [-1,1]. Warp amplitudes are 0..65,536 blocks. Public graph coordinates are finite and within ±30,000,000; the depth/amplitude limits keep nested noise coordinates within the noise core's ±32,000,000 domain.

## Ground surface preview

The explicit `DensityChunkMaterializer.fill(GroundSurfaceProfile.Compiled, chunk)` overload coats the topmost connected density surface with the owner's registered ground material samples. The original graph-only overload remains stone/deepslate. Neither overload is called by a registered terrain generator, and V1-V7 geography remains frozen.

[`GroundSurfaceProfile`](examples/ground-surface-profile.json) is a complete version-1 inline snapshot. It contains 2-64 ordered material IDs from the three supported ground samples, strictly increasing boundaries in (0,1), coherent displacement in 0..0.25, scale in 8..8192 blocks, an exact decimal-string signed-long salt, soil depth in 1..16 blocks, and a named density channel with finite increasing normalization limits within +/-1e12. Every field is required; unknown keys, wrong JSON types, unknown materials, fractional integer fields and oversized lists fail explicitly. Lists are defensively copied. Compiling checks the channel against the supplied graph and binds immutable block states. Supply the same world seed for both graph and surface compilation.

The channel is evaluated at `(worldX, 0, worldZ)`, normalized/clamped to 0..1, then passed to the coherent gradient. The example uses `eroded` solely as a spatial authoring control: it is **not** a physical soil, moisture or ecological model. Real climate/substrate progression and final material families remain open. No world access, mutable random sequence or chunk-local coordinate controls selection.

Only connected solids below the highest exposed density voxel receive soil, stopping at the first air gap and above the two-layer bedrock floor. Lower cave floors and air gaps are untouched. A solid column cut off at the world's inclusive maximum Y is not treated as an exposed soil surface. Base-column queries apply the same policy, and both native heightmaps retain density geometry. This does not yet solve slopes, soil horizons on every overhang, aquifers or hydrology.

`groundMaterialResources` runs headlessly and is included in `check`; it validates the ten owner-image hashes, grayscale/repeat/mask invariants, models/items/tint indices and strict profile codec cases. Dedicated-server validation is opt-in with `DEEPPRIMAL_GROUND_MATERIAL_CHECK=fresh` or `reopen` and `-PvisualValidation`. The fixture checks actual registries, self drops, shovel/substrate tags and saved material identities, then exact native/base-column parity and reversed generation order on adjacent signed-coordinate V8 chunks across two seeds. Thin roofs, cave gaps, empty/full columns, ceiling clipping and bedrock get separate controls.

The fixture writes surface/graph JSON snapshots and stable block-name fingerprints into its isolated test world for a separate-process replay. These are **validation files**, not a gameplay generator save codec, level.dat integration or datapack loader. Selected-JAR fresh/reopen runs now pass 1,572,864 block/reverse-order comparisons each, with identical stored fingerprints for seeds 20261020 and Long.MIN_VALUE. Packaged Windows OpenGL/Vulkan inventory material/reload previews also pass with pixel-identical captures; this is not a V8 terrain-world visual or performance pass. See the [executed material validation and rejected attempts](../art/ground-textures-v2/README.md#integration-validation-alpha29-dev3). No promotion, performance improvement or higher engine grade follows from this surface preview.

## Limitations and next acceptance gate

- The scalar graph oracle is recursive and reevaluates common subexpressions. The exact job sampler avoids some repeated work, but sampling latency remains order-sensitive and above the component target. Neither path is generated machine code or an explicit SIMD kernel.
- The erosion channel is a shape-control field; it does not simulate physical erosion. Canyon bands are not downhill river routing. Hydrology must carve its actual planned reaches into this density stage.
- `height` is a recipe reference surface, not the final top solid block once 3D modifiers act. A native adapter must derive base columns/heightmaps from the same occupancy policy as materialization.
- The native preview proves exact occupancy, base-column parity, heightmaps and bedrock barriers for one bounded region, but does not provide production materials, water barriers, biome selection, saved-profile replay or chunk lifecycle integration.
- Mathematical non-heightfield columns do not prove attractive or supported overhangs. Connectivity, thin roofs, unintended detached fragments, bedrock and aquifer barriers need native tests and visual review.
- Shared lattice faces are continuous, but interior interpolation can change density signs and topology. Do not enable the coarse lattice solely because it is faster.
- Floating features are deliberately opt-in. The default-off recipe can still require support/connectivity cleanup; it has not earned a claim of zero stray blocks.
- JSON structure is bounded after parsing. A host must separately bound raw datapack bytes and enforce registry lifecycle, profile versioning, saved snapshots and mod API compatibility.
- No 26.3 port, owner-hardware performance, cross-machine determinism, native async scheduling, structures, new biome ecotones or complete cave/aquifer stage is delivered here.

Next: exact-versus-lattice roof/topology gates, saved graph/profile replay and coupled river/basin barriers. Extend native parity coverage across chunk boundaries, seeds and reopen before registration. Preserve the current preview until those pass. Then proceed through climate blending, hydrology/caves, features and measured optimization using the original stage order.


<!-- END PRESERVED SOURCE -->
