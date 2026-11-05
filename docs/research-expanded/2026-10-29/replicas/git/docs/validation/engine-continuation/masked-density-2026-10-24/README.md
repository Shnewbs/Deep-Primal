# Expanded replica — docs/validation/engine-continuation/masked-density-2026-10-24/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/masked-density-2026-10-24/README.md) |
| Source identity | git: docs/validation/engine-continuation/masked-density-2026-10-24/README.md |
| Preserved source SHA-256 | `d2a278ae986f64839a6e82b82d2c90ad9c6faafa657ba5f17f75ef9af81e5e9d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R61 exact masked-density evaluation skipping expensive 3D noise where the eventual product mask is exactly zero while retaining job-owned evaluation. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that a rejected optimizer variant inspected unreachable nodes too early, and exact skipping does not resolve 906 coarse-lattice sign disagreements. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an optimizer validity contract that validates the graph before metadata construction and permits only provably exact evaluation shortcuts. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject unreachable programs before optimization, compare signed-zero and full-volume outputs, rerun final-source timings and keep coarse topology refinement as a separate native acceptance gate. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R61 — staged exact masked-density evaluation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R61 — staged exact masked-density evaluation

Parent `81a113275ac8e2849d75e47c7f055e8c151c9c4f` (R60).
One staged runtime candidate changes: DensityGraph.java, continuing R60's optional
job-owned sampler. TerrainShapeV8 is reused unchanged. Public production source,
registrations, water quantity, player edits, delivered V1–V7 and installers remain
unchanged. **Last full grade remains50/100, average5.0/10, Tier2/5; no new full regrade.**

## Concrete problem and focused implementation

A fresh instrumented copy of R60 samples196,6083Dnoise nodes for one196,608-point
volume even where the eventual product mask is exactly zero. Current recipe ordering
computes expensive3Dnoise before multiplying by near-surface/inland masks. That is
avoidable component work, not evidence that native V8 exists or was accelerated.

Compilation now records whether each expression contains3Dnoise and selects at most
four non3D leaf factors from a product on the right of an ADD. The optional sampler
first evaluates the left addend. If it is nonzero and a recorded factor is exactly
zero, it returns the left addend. Otherwise it performs the original ordered sum.
No factor reassociation, interpolation, epsilon or density cutoff is introduced.

Why this shortcut preserves output: graph validation permits finite bounded numeric
nodes only; evaluated factors/products in this language stay finite at valid graph
coordinates. A zero leaf makes the enclosing multiplication-only tree either+0or-0.
Adding either signed zero to a finite nonzero addend preserves its exact bits. When
the addend is itself zero, the original arithmetic is retained to preserve signed
zero. The scalar evaluator method text and shape recipe bytes are unchanged. Pure
noise/program nodes have no external side effects. This rule is not applicable to
arbitrary user functions or languages admitting NaN/infinite operands.

Guard factor storage is bounded by4factors/ADD and128nodes, at most2,048rawinteger
bytes plus128rawhas3Dbytes per graph, excluding reference arrays/objects/collections.
Sampler storage remains R60's33rawbytes/node and creation-thread ownership. Graph
compilation does additional bounded traversal; guard evaluation can add work for
other recipes. No global cache/job residency or arbitrary-graph speed guarantee.

## Checks, failures and identities

57immutable original core inputs remain Git-blob verified; their source tree is
unchanged by R60's docs-only commits. Exact R60 sampler source identity is retained as
before-DensityGraph.java. Complete candidate core compiles onJDK25 with102product
core classes. Final measured graph class hashes match the clean check compilation.

- **1,581,340 exact/ownership checks pass**, including1,572,864full-volume recipe
  comparisons across four seeds/floatingoff-on; named channels, distinct warped
  coordinates, signed zeros, invalid inputs and independent jobs. Added480cases
  cover bothzero signs, nonzero positive/negative and subnormal addends.
- Original **183,806noise** and **40,987graph** checks retain fingerprints.
- Diagnostic instrumented variants count196,608→45,0563Dcalls for one column-major
  volume with identical checksum. Instrumentation is excluded from timings/runtime
  candidate. Its source-generation script is retained.

The first candidate failed the existing unreachable-node rejection check with a real
NullPointerException: optimizer metadata inspected unreachable program slots before
validation. Validation now rejects unreachable nodes first. Failed results/source
are under intermediate/. Earlier valid-recipe timing records belong to that rejected
variant and are not final-source acceptance; final timings are rerun. No failed
variant is promoted. No new full Gradle/module/codec/package/native/Vulkan/GL/visual,
Windows or installer check ran. The remaining full project/dependency/native checkout
is unavailable locally; direct core checks do not validate the full game adapter.

## Matched component results

Three paired fresh JVMs, alternating control/candidate launch order;4warmups and
8measured unique-coordinate volumes per variant/fork. One default-off recipe/seed,
196,608samples/volume, column-major Y-inner order, same checksum consumer. All paired
checksums match. Job-sampler allocation is included; graph compilation excluded.

| Fork | R60 mean / p95 ms | Candidate mean / p95 ms | Bytes/job, both |
| --- | --- | --- | ---: |
| 0 |58.342 /68.712 |31.380 /33.007 |1968 |
| 1 |55.637 /62.906 |32.722 /33.721 |1968 |
| 2 |52.859 /53.031 |32.314 /39.440 |1968 |

P95 is the maximum for eight measured values, not strong tail evidence. Different
seeds/graphs/orders/hardware may differ. These are component samples, not native
chunk/load-ready/frame/FPS, owner-hardware or market-comparison results. **Still
above5ms before native materialization.** Raw timings/CSV/source/class identities
are retained; no faster-only fork selection.

A separate50warm/200measured compilation probe records control80.5µs/21,140.9B versus
candidate127.7µs/41,337.2B per compiled graph. It excludes recipe-definition creation
and is only one process per variant, not robust constructor latency acceptance.
It exposes additional metadata/traversal/temporary-allocation cost; no whole-pipeline
memory improvement follows. Reuse compiled immutable graphs rather than infer that
per-voxel gains eliminate initialization costs.

## Reproduction and next implementation

Restore the57Git inputs listed by evidence/parent-inputs.json into parent/. With
retained pinned JDK25, evidence/run.py verifies inputs, overlays the staged candidate,
cleans only this job's output and runs the core/oracle gates. The scalar reference
and prior sampler source are retained; implement.py generates the candidate from it.
benchmark.py expects compiled ReuseBenchmark in clean control/candidate classpaths;
count_probe.py generates diagnostic-only copies and compile-probe.json records the
separate initialization observation. Toolchain, input copies and binaries are not
committed; source identities and reproducible scripts are.

Keep this sampler staged/unenabled. Next broaden adversarial graph/order/cold/tail
and metadata-cost checks, run full module/codec/build/package guards, then nativeV8
materialization/base-column/heightmap parity, saved snapshots and routed hydraulic
barriers. Historical906coarse-lattice sign disagreements remain unresolved: this
exact shortcut does not justify approximate occupancy. Conservative topology/refinement,
Vulkan travel/edit/save/reopen/shutdown/visible coverage, water/LOD issues, biome/cave/
structure stages, bounded native scheduling, latest-version port and the80/100goal
remain open. No new JAR or engine completion claim.


<!-- END PRESERVED SOURCE -->
