# Exact density reuse — continuation R60

Parent `44b89f541ae098f0ad6c16f622c7bffb058ffcc7` (partial stage two).
One candidate runtime file changes: DensityGraph.java. TerrainShapeV8 is copied
unchanged for review. Both remain staged under this evidence directory; public
production sources, generator registrations, installers and delivered V1–V7 are
unchanged. The last complete ten-category grade remains **50/100 (5.0/10), Tier2/5**;
this is not a new full regrade or an achieved80/100 target.

## Reproduced problem and implementation

The recorded recursive graph repeats horizontal/common expressions for each voxel.
Fresh matched scalar controls reproduce **1,016–1,039ms mean** for a196,608-sample
volume. It is a real component-budget failure; no incorrect occupancy is attributed
to the exact scalar evaluator. Its method text and shape-recipe bytes are unchanged.

An optional newSampler() creates job-owned memo storage. Compilation conservatively
marks each node's coordinate dependencies. Each slot keys all relevant coordinate
bits, including signed zero; different warped positions cannot alias. Constant and
horizontal values can survive consecutive vertical samples, and same-point DAG
references reuse exact results. Ordered arithmetic/eager branches match the scalar
switch. There is no interpolation, reordering, approximate noise or recipe change.
The graph remains immutable/shareable; each mutable sampler belongs to its creation
thread, and cross-thread use throws. Independent jobs create independent samplers.
Sampling order affects reuse efficiency, never the selected value.

Storage is one value/three coordinate words/valid bit per node:33rawbytes/node,
maximum4,224rawbytes for128nodes, plus five arrays/object/thread reference overhead.
An additional immutable dependency mask costs4rawbytes/node per compiled graph,
maximum512bytes. No global cache, unbounded node admission or retained tile cache.
Maximum recursive graph budgets remain unchanged. A caller must still bound concurrent
jobs/sampler lifetimes; these per-instance limits are not a global memory guarantee.

## Fresh checks

57 immutable-parent public core sources are Git-blob verified before compilation.
The complete candidate core compiles with pinned JDK25.0.4.1+1, with102product core
classes. Output is cleaned only inside this isolated job before final compilation.
Final timed density class hashes match the clean final compile. Class/source records
and exact scalar/recipe scope checks are retained in evidence/.

- **1,580,860 new checks**, including1,572,864 bit-exact full-volume comparisons across
  four seeds/floating off/on, all named channels, shared warped/source coordinates,
  signed-zero outputs, rejected NaN/missing channels, ownership and16independent jobs.
- **183,806 original noise checks** retain their fingerprint.
- **40,987 original graph checks** retain their fingerprint, including29,784shared-face
  samples and the existing mathematical shape observations.

All final checks pass. No failing product/core check is hidden. During reporting,
class counting initially included the previous benchmark executable; it is corrected
to product-core paths and the final compile clears own output. No runtime fix is
attributed to that reporting/output hygiene correction. No full Gradle/module/codec,
package identity/native Minecraft/Vulkan/OpenGL/visual/Windows/ownerGPU/FPS check ran.
Direct javac/core equality does not validate a distributed installer.

## Matched component benchmark

Three fresh JDK25 JVMs; one default-off V8 recipe/seed; alternating mode order;
four warmups and eight measured unique-coordinate volumes per mode/fork. Each volume
samples16×768×16=196,608 points, with Y innermost within each X/Z column. Scalar and
memo use the same order/coordinates and checksum consumer; all paired checksums match,
including warmups. Memo construction/allocation is inside measurement. Graph compilation
and output arrays are excluded. No concurrent benchmark jobs were started here.

| Fork | Scalar mean / p95 ms | Exact memo mean / p95 ms | Memo allocation bytes/volume |
| --- | --- | --- | ---: |
| 0 | 1038.542 /1078.050 | 54.851 /56.603 |1968 |
| 1 | 1016.465 /1096.267 | 53.960 /56.283 |1968 |
| 2 | 1017.858 /1050.304 | 55.698 /59.854 |1968 |

P95 uses nearest-rank and equals the maximum with only8measurements: limited tail
coverage. Scalar allocated zero measured thread bytes in these intervals. The memo
path is about94.5–94.7% lower mean here, **still above5ms before native costs**. This
is a component-only, column-major result on this container; different orders/graphs,
random access, cold compilation and whole-world costs may behave differently. It
is not native chunk performance, a comparison with another engine or owner hardware.
Raw CSVs, every fork, summary and timed class identities are retained.

## Reproduction and next gates

Restore the57 immutable files listed in evidence/parent-inputs.json below parent/,
then apply the single staged candidate to a copy. With the pinned JDK25 toolchain
at the path used by the retained scripts, run evidence/run.py. The scripts verify
Git inputs, compile all core sources and execute the new and existing checks.
Compile ReuseBenchmark.java against those classes, then run evidence/benchmark.py
for the matched3-JVM probe. implement.py regenerates the candidate from parent source.
Toolchains, original Git-backed source copies and class binaries are not committed.

Keep the sampler staged/unenabled pending full module/codec/build/package checks,
broader graph/order/allocation/cold/tail measurements and native geometry integration.
The historical906coarse-lattice sign disagreements remain unresolved; exact memo
preserves exact occupancy but does not make interpolation safe. Next profile remaining
exact3D/common-expression costs, define conservative topology/refinement, then implement
native V8 materialization/base-column/heightmap parity, saved graph snapshots and routed
hydrology with chunk-order/save/reopen/Vulkan visual checks. No native V8 path was
introduced here. Water/LOD defects, broader biome/cave/structure stages, bounded async
scheduling, latest-version migration and the80/100acceptance target remain open.
