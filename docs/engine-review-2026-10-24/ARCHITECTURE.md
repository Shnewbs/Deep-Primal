# Builder architecture and staged acceptance

Status: proposed before new implementation on 2026-10-24. Baseline: alpha.28, V7, main f141c0b4d058abb311cb088e4f17a575685a2939. This is an implementation/acceptance plan, not a claim that its features are delivered.

## Platform choice

Continue Java 25 and NeoForge, retaining the platform-independent `engine-core` module and Deep Primal identity. Minecraft 26.3 is the current released game; the verified Deep Primal adapter is 26.1.2/NeoForge 26.1.2.122. A 26.3 adapter and its renderer/GUI migration must compile and pass native save/client gates before claiming latest-version support. The 26.3 official migration primer describes major shader/rendering and worldgen registry changes; changing the version string alone is insufficient. Existing modules and Themes remain intact. Fabric is not an additional target in this sequence.

Sources checked 2026-10-24:
- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- https://docs.neoforged.net/primer/docs/26.3/

## Pipeline contracts

1. **Noise core:** immutable seeded 3D scalar fields, validated octave parameters, axis-contiguous bulk sampling and bounded immutable global-lattice tiles. Use fixed integer hash/gradient tables and Java strict floating-point arithmetic; avoid trigonometric random gradients, implicit FMA and order-dependent RNG. No mutable global cache. Exact field sampling and interpolated lattice sampling are distinct APIs, with separate accuracy/latency measurements.
2. **Terrain shape:** compile validated JSON density descriptions into an immutable graph of continentalness, erosion, peaks/valleys, warped ridges, canyon masks and genuine 3D density. Coarse lattice evaluation precedes materialization. Optional floating formations are intentional features, bounded and configurable. A maximum graph depth/node budget rejects runaway profiles. This requires V8; V1–V7 remain frozen.
3. **Biomes:** climate vectors include temperature, humidity, altitude and continentalness. Weighted neighboring climate sites drive continuous relief/surface/vegetation weights. Minecraft's final biome ID remains categorical; smooth color/material/ecotone behavior needs its own policy and tests. Do not claim that renaming a discrete biome selector creates seamless blending.
4. **Hydrology/caves:** retain region-level priority-flood/downhill graph planning and immutable basin/reach records. Add explicit ocean outlet/endoreic cases and consistent boundary dependencies. Couple river carve masks to density before final surfaces. Add cheese/spaghetti/noodle fields with protected roofs, fluid barriers and finite aquifer/source accounts. No unlimited source conversion. Live solver authority and worldgen water placement remain separate.
5. **Features:** deterministic spatial candidates, stable feature salts, spacing/exclusion and biome-weight rules. Feature ownership is based on world-space anchors; inspect overlapping neighbor candidates without mutating neighbor chunks. Apply clipped structures/vegetation/ores in a stable order, with bounded footprints. Snapshot registries and profiles into saves so datapack reload cannot silently alter explored-world continuity.
6. **Optimization/adapter:** use the host's documented generation scheduler, immutable job inputs and exclusive chunk ownership. Return a real completion future, with errors/cancellation propagated. Bound expensive regional work and caches; deduplicate pending region work without holding a global monitor through region construction. No blocking server-thread join or unbounded worker queues. Native lighting, biome population, features and serialization remain separately measured costs.

The public extension surface will use small field/stage/provider interfaces, typed validated profiles and stable identifiers. Extensions must declare version, salt, bounds and determinism obligations. The first stage supplies noise/field/grid interfaces only; it does not advertise an unfinished generator extension API as complete.

## Stage gates and outputs

| Stage | Required code | Tests/benchmarks | Promotion gate |
| --- | --- | --- | --- |
| 1 noise | 3D field, validated spec, bulk fill, immutable lattice | Independent scalar oracle, exact endpoints, negative/far coordinates, invalid limits, parallel replay, JIT/interpreter hashes, raw/lattice timings and approximation error | Core/module build; no change to legacy fields |
| 2 terrain | Bounded JSON density graph and V8 profile | Multi-seed native surfaces/overhangs/canyons, chunk seams, replay, altitude distribution | Native block/base-column consistency and visual review |
| 3 biomes | Climate weights and ecotones | Border transects, neighboring chunk/order tests, biome-material support, palette validation | Native transition views and saved profile replay |
| 4 hydrology/caves | Coupled carve/underground density and aquifers | Whole watershed routing, source/mouth/bank tests, roof/barrier tests, finite-budget endurance, chunk order | Native loaded/unloaded/reopen/edit cases |
| 5 features | Placement registry, spacing and clipped structures | Independent spacing oracle, overlap, chunk order/replay, malformed datapacks | Native cross-border structures and supported vegetation |
| 6 optimize/platform | Async adapter, measured caches, 26.3 integration | Fresh JVM warm/cold/native benchmark distributions, allocation, concurrency/lifecycle, full client/server save | Latest stable native build; owner-hardware frame/tick evidence |

Each completed stage must publish code paths, exact test output, benchmark workload/host/mean/p50/p95/max/allocation, and known weaknesses. Microbenchmarks never count as whole-chunk benchmarks. SIMD-friendly primitive arrays are permitted; actual SIMD acceleration is not claimed without generated-code/equivalence measurements. No preview Vector API dependency is required initially.

## Performance contract

The owner's <5 ms average is an acceptance target, not a measured property. Report native 16x16 chunks with explicit Y bounds, seed/site corpus, cold regional construction, warm unique chunks, concurrency, wall latency, CPU/allocation and checksum. Include p95/p99 and worst case; cached repeats must not masquerade as unexplored-chunk generation. Separately measure total load-to-ready latency including host biome/light/status work. A direct private call on the server thread demonstrates synchronous method duration, not proof of the normal host call site's thread. Zero stalls on all hardware is not a defensible absolute guarantee.

## Current scope

This checkpoint begins with a strict baseline review and stage 1 implementation. Remaining stages stay open until their gates pass; no new terrain revision is enabled by the noise library alone. Delivered alpha.28 JARs and saved V7 worlds remain unchanged. The staged library is not credited as game terrain in the review scores.
