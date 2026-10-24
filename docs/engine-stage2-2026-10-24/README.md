# Stage 2 — bounded density programs and experimental V8 shape

This is a **partial stage-two checkpoint**, not a playable V8 release. The graph compiler, strict JSON codec and experimental shape recipe are implemented. Registration, native block/base-column materialization, saved generator snapshots, coupled drainage and visual acceptance remain open. All 509 production class files from stage one remain unchanged; nine new classes are added. Delivered alpha.28 installers and V1–V7 geography remain frozen.

The owner's acceptance target is an evidence-supported **average of at least 8.0/10 across the ten equally weighted review categories**. The last complete audit remains 50/100, average 5.0. No new full regrade is claimed for unused core geometry. The existing hourly build/audit task is enabled with the new target; completing this checkpoint does not complete that goal.

## Setup

Java 25, current NeoForge 26.1.2 adapter:

```sh
./gradlew build
./gradlew :densityBenchmark
./gradlew :visualValidationClasses
```

The latest-version port is still open. The current development version is `0.0.1-alpha.29-dev.2`. No new owner installer is supplied. See the [original architecture](../engine-review-2026-10-24/ARCHITECTURE.md) and [strict baseline review](../engine-review-2026-10-24/REVIEW.md).

## Code and API

- `engine-core/.../density/DensityGraph.java`: versioned definitions, immutable linked programs, cycle/reference/range checks and bounded evaluation. Implements `DensityField`; a positive density denotes solid for later terrain consumers.
- `engine-core/.../density/TerrainShapeV8.java`: continentalness, erosion mask, peaks/valleys, warped continental domain, inland mountain/hill relief, canyon bands and genuine 3D surface modifiers. Floating formations are optional and disabled by default.
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

## Limitations and next acceptance gate

- Graph evaluation is a recursive immutable program, not generated machine code or an explicit SIMD kernel. Repeated common subexpressions are reevaluated. Later optimization must preserve the exact oracle and versioned output.
- The erosion channel is a shape-control field; it does not simulate physical erosion. Canyon bands are not downhill river routing. Hydrology must carve its actual planned reaches into this density stage.
- `height` is a recipe reference surface, not the final top solid block once 3D modifiers act. A native adapter must derive base columns/heightmaps from the same occupancy policy as materialization.
- Mathematical non-heightfield columns do not prove attractive or supported overhangs. Connectivity, thin roofs, unintended detached fragments, bedrock and aquifer barriers need native tests and visual review.
- Shared lattice faces are continuous, but interior interpolation can change density signs and topology. Do not enable the coarse lattice solely because it is faster.
- Floating features are deliberately opt-in. The default-off recipe can still require support/connectivity cleanup; it has not earned a claim of zero stray blocks.
- JSON structure is bounded after parsing. A host must separately bound raw datapack bytes and enforce registry lifecycle, profile versioning, saved snapshots and mod API compatibility.
- No 26.3 port, owner-hardware performance, cross-machine determinism, native async scheduling, structures, new biome ecotones or complete cave/aquifer stage is delivered here.

Next: native V8 materialization with identical block/base-column/heightmap semantics, exact-versus-lattice roof/topology gates, saved graph/profile replay and coupled river/basin barriers. Preserve the current preview until those pass. Then proceed through climate blending, hydrology/caves, features and measured optimization using the original stage order.
