# R62 — staged density metadata allocation reduction

Source parent: `fa59cc793bf20a18ebbc058c645761975b0fd62e`. Continues the unpromoted R61 sampler. One staged runtime file changes: `DensityGraph.java`. Production source, recipe, registrations, finite water, player edits, frozen V1–V7 and owner installers remain unchanged. **Last full engine grade remains 50/100, average 5.0, Tier 2/5.** This is a continuation toward the owner's verified 8.0 average target, not a full regrade or completed V8 stage.

## Problem and implementation

R61 creates a LinkedHashSet and stream pipeline for every program node, including nodes with no useful zero guard. It also recursively rediscovers 3D-noise presence across shared expressions. Its separate constructor probe showed higher initialization allocation despite faster sampling.

The final candidate changes metadata construction only:

- Memoize 3D-noise presence with a bounded temporary byte array (unknown/no/yes).
- Visit only eligible ADD right-hand expressions when collecting guards.
- Reuse one four-int scratch array while collecting right-to-left factors, retaining duplicate elimination and the four-factor limit.
- Copy only populated factor lists; share a private immutable empty array for the rest.
- Remove the retained per-graph 3D-presence array; it is not needed during sampling.

The ordered scalar evaluator and complete job-owned Sampler source are **text-identical to R61**. This changes neither arithmetic, signed-zero handling, coordinate keys, ownership nor sampling policy. The recipe is unchanged. There is no SIMD, approximate occupancy, new cache residency or native integration claim.

Maximum scratch payload is 128 byte flags plus four ints during construction. Retained factor storage remains at most four ints/node plus its bounded reference/array overhead, with shared empty lists. Individual immutable graphs may be reused between jobs; samplers remain thread-owned.

## Checks and failures

Both R61 control and final candidate compile all 57 core sources on the restored pinned JDK 25. Each passes:

```text
DENSITY_REUSE_PASS checks=1581340 recipeVolumes=8 exactSamples=1572864
NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64
DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784
DENSITY_ADVERSARIAL_PASS checks=105472 acceptedGraphs=180 rejectedByBudget=0
```

The new corpus covers randomized bounded operations/noise/warps, reversed and permuted traversal, reordered node definitions, named channels, negative/far coordinates, signed zeros/subnormals, the complete 128-node budget and a fourth guard at program index127. The harness retains its earlier `highPackedByte` output label; the final implementation uses arrays, and that case verifies the high-index/fourth-factor boundary.

The original fixture generator created an unused WARP leaf, so the first run accepted only 100 generated graphs and failed its coverage threshold. The generator was corrected before acceptance. Its failed source is retained under `evidence/intermediate/`; this was a private fixture failure, not a passing product run.

A first candidate packed four guard IDs into one integer. It passed correctness and cut allocation, but all three measurements of one sampling case were slower. It was replaced with the constructor-only implementation, retaining the original sampling loop. That source, checks and timings remain under `evidence/packed-intermediate/`; they are not final-source acceptance. Mixed timings do not establish a universal causal regression.

An independent clean javac compile produces **102 core classes matching the checked/timed candidate byte-for-byte**. All 56 other original core sources, including the recipe, remain unchanged. Source hashes, scalar/Sampler text identity, class hashes and raw results are retained. No full Gradle/module/codec/package/native/Vulkan/GL/Windows or current-JAR check ran for this staged change; full project/dependency restoration remains open. Core checks are not game acceptance.

## Matched constructor measurements

Linux x86_64 validation container, JDK 25.0.4.1, not the owner's AMD 8060S. Three paired fresh JVMs, alternating control/candidate order. Four definitions: both terrain recipes, a shared 2D DAG and a full 128-node constant graph. Definitions are created outside timing. There are 128 warmups and 512 measured compilations per definition/fork, with alternating case order. A volatile retained graph makes allocation observable. The complete graph construction is measured; sampling is measured separately. All paired checksums match and class bytes remain unchanged during timing.

| Definition | R61 mean allocation bytes | Candidate mean allocation bytes | R61 mean latency µs | Candidate mean latency µs |
| --- | --- | --- | --- | --- |
| Shape, floating off | 37,188–37,416 | 17,602–18,128 | 19.945–26.251 | 17.343–22.642 |
| Shape, floating on | 43,729–43,982 | 20,298–20,875 | 28.056–29.758 | 19.767–22.167 |
| Shared 2D DAG | 7,231–7,242 | 3,044–3,059 | 22.235–41.822 | 3.720–5.767 |
| Constant 128 | 69,735–69,746 | 24,716–24,731 | 35.854–49.567 | 26.369–30.363 |

Ranges retain all three fork means. Allocation is consistently lower for this corpus; constructor latency is mixed for the default-off shape, so no universal speed claim follows. Cold first-compilation observations, distributions, maxima and allocation are in the raw CSV/summary; cold samples are not pooled with warm measurements.

## Broader component sampling

Two seeds, floating off/on and two traversal orders: eight workloads, three paired fresh JVMs. Each measured volume consumes 196,608 values (16×768×16), includes sampler creation, excludes graph construction/native block/water/feature/light/IO work. Each case has one separately recorded cold first volume, three warmups and four measured unique nearby volumes. Sites are the retained seed 73 modifier area and seed 20261020 high-terrain area. P95/p99 are maxima for four samples and are weak tail evidence.

| Workload | R61 fork means ms | Candidate fork means ms |
| --- | --- | --- |
| Seed 73 off, Y inner | 30.489 /33.462 /34.180 | 36.591 /34.857 /32.765 |
| Seed 73 on, Y inner | 44.854 /46.640 /49.855 | 46.941 /41.419 /45.543 |
| Seed 20261020 off, Y inner | 32.997 /35.677 /34.330 | 32.061 /31.917 /31.835 |
| Seed 20261020 on, Y inner | 42.981 /48.009 /48.156 | 42.258 /41.657 /43.236 |

The X-inner workloads are much slower: candidate fork means span 254.814–307.122ms. Their raw distributions and all cold values are retained. Job allocation is unchanged: 1,968 B for floating-off and 2,336 B for floating-on. All control/candidate checksums match. This demonstrates order sensitivity; do not assume an x-fast consumer gains the column-reuse benefits.

**Sampling latency remains mixed and the 5 ms target is unmet before native work.** Identical sampler source does not substitute for stronger timing evidence. This checkpoint accepts the scoped allocation reduction as staged progress only; it does not claim improved whole-chunk latency, game FPS or owner-hardware performance. Historical 906 coarse-lattice occupancy disagreements remain unresolved.

## Reproduction and continuation

Restore the 57 original core inputs, overlay the retained R61 control graph in `control/engine-core`, and overlay this candidate graph in `candidate/engine-core`. Put the pinned JDK under the adjacent `resume-tools/toolchain` directory or adjust the runner's tool path. `evidence/run_checks.py` compiles explicit source lists and runs both control/candidate suites. `evidence/benchmark.py` then runs paired benchmarks sequentially, verifies matching checksums and unchanged class hashes, and retains every run. Do not include intermediate fixtures in the compilation source list.

Keep the candidate staged/unenabled. Next restore the full project/dependencies and run full module/codec/package guards; then implement native V8 materialization/base-column/heightmap/saved-profile parity and coupled routed water/topology gates. Further latency work must respect column ownership/order, exact output and bounded job storage; no coarse interpolation shortcut is accepted. Continue the climate, cave/aquifer, feature/structure, native scheduler and latest-version stages afterward. The hourly auditor builds its own pinned current JAR without pausing the builder; staging here does not silently enter that JAR.
