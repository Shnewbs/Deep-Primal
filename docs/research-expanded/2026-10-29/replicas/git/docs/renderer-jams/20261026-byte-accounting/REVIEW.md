# Expanded replica — docs/renderer-jams/20261026-byte-accounting/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-byte-accounting/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261026-byte-accounting/REVIEW.md |
| Preserved source SHA-256 | `65ed70c9be0994013ed2a67b359c5d470c39e4170477562421db03edb28c1f87` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Records a staged LodMesher.Levels.bytes change from streams to three list sums, using indexed access for RandomAccess lists and iterators for sequential lists. Geometry and GPU payload are unchanged; production and artifact qualification were intentionally not advanced at this checkpoint. The review keeps the capacity-blocked build and unchanged production explicit.

## Research expansion — gaps and details to deepen

- The query optimization needs an explicit mutability and ownership contract. Recomputing bytes permits mutable lists, while later committed ledgers require immutable accepted payload sizes; those are different semantics and should not be merged retrospectively.

## Research expansion — proposed contracts

- Add the staged-to-promoted lineage and define bytes() as payload accounting rather than allocated heap or resident device memory. Record list ownership, overflow behavior, and the exact scope of any allocation reduction.

## Research expansion — acceptance checks

- Retain mixed-list and mutable-list equivalence checks, then measure actual admission-query frequency and allocation on the target artifact. Require native frame and memory evidence before inferring a renderer benefit.

## Research expansion — source anchors

- Renderer jam 20261026-byte-accounting — staged candidate self-audit
- Implemented candidate

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `candidate/LodMesher.java#L16` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-byte-accounting/candidate/LodMesher.java#L16) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261026-byte-accounting — staged candidate self-audit

Initial and reviewed production source: `bf7b36a21b7063c1d273c383008ddfd073d5e5b1` (dev.31). Candidate source SHA256: `4c8d6b60a71c1021d245df622105b1d1a8a4f0bd0dc58fd2a205f83c9be85eec`, retained verbatim in [candidate/LodMesher.java](candidate/LodMesher.java#L16). This is the renderer author's read-only self-audit after candidate code completion, not an independent external audit. **Production runtime is unchanged; this candidate is staged only. No new renderer JAR exists.** Final receipt commit identifies the exact retained snapshot; no code edits during scored review.

## Implemented candidate

Levels.bytes previously allocated a Stream.of/flatMap/mapToLong pipeline every time capture/upload/admission queried mesh size. The candidate replaces that pipeline with three list sums. RandomAccess lists use indexed traversal; sequential lists use an iterator so LinkedList does not become quadratic. It recomputes every time, retaining the public record's behavior when callers mutate a supplied list. No cached stale byte count, shared scratch, geometry/UV/material/light/fluid/shader change or native dependency. Per-mesh gpuBytes arithmetic is unchanged.

Baseline is exact current-main LodMesher with only class/constructor name replacement to BaselineLodMesher. The candidate changes only this byte-accounting method/helper. Target production path is engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java; this run publishes its candidate and private checks only under this dedicated docs path, so no unqualified production edit is integrated.

## Fresh scoped checks and measurement

Java25.0.4.1, -Xmx512m -Xverify:all, Linux EPYC9V74 host. All **75 current engine-core Java source blobs** were verified against Git tree 6b217a520d2707f6bda78d66440e38d8c0018454 before compilation; the current LodMesher is replaced with the candidate, and baseline plus three retained private LOD fixtures/new private check are compiled alongside it. Exact javac argv is compile-command.json. No packaged or cached classes are on the final compile/runtime classpath. An earlier packaged-classpath exploratory pass is retained in checks.txt but is not substituted for final-checks.txt.

For each of five real immutable mesher inputs, create baseline/candidate meshes outside measurement, compare full geometry signatures, warm each bytes method 10,000 calls, then run five alternating baseline/candidate batches of 20,000 calls each. ThreadMXBean calling-thread allocated-byte deltas are recorded; results are consumed through a volatile sink. No elapsed-time benchmark or exclusive host reservation. These are repeated budget-query allocations, not complete mesh-generation allocation, peak RAM, GPU bytes, VRAM, frame time or FPS.

| Fixture | Baseline total B /100,000 queries | Candidate B /100,000 queries | Accounted mesh bytes (unchanged) |
|---|---:|---:|---:|
| 0 | 58,379,408 | 0 | 1,656 |
| 1 | 56,800,000 | 0 | 108,192 |
| 2 | 56,000,000 | 0 | 57,776 |
| 3 | 56,000,000 | 0 | 81,880 |
| 4 | 56,000,000 | 0 | 4,140 |

About 560–584 allocated bytes/query before, zero measured on these warmed RandomAccess output fixtures after. This is a development candidate component result, not shipped performance credit or a universal allocation guarantee for sequential/custom lists. Sequential fallback intentionally may allocate its iterator.

Fresh checks pass for four list cases: immutable output, mutable ArrayList, sequential LinkedList whose indexed get throws, and empty fine list. Clearing a mutable list must update subsequent counts. Empty all-level total is zero. Independent arithmetic oracle sums actual color/index lengths. Baseline/candidate five-scene geometry signatures match. Ten frozen signed-coordinate/floor signatures, 18-part exact bounds, retained output through25 later serial builds,64 parallel jobs, maximum4096height and cancellation regressions pass. See final-checks.txt and tools/ByteAccountingChecks.java.

Commands:
- Exact Java25 javac argument array: compile-command.json (fresh all-core compilation, exit0).
- Java25 `java -Xmx512m -Xverify:all -cp jam6/classes com.shnewbs.deepprimal.core.lod.ByteAccountingChecks`: exit0, BYTE_ACCOUNTING_PASS.
- Full Java25/Gradle/module/package/native build: **not run**, insufficient isolated filesystem space.
- No regression failures in final scoped checks. Passing standalone core checks does not qualify a mod JAR.

## Required preflight and consumed audits

Read current main, full engine/water state, LOD/drainage architecture, renderer diagnostics roadmap, previous delivered-discovery REVIEW/BACKLOG, recent commit/root inventories and completed renderer ownership. No AGENTS.md exists in inspected root/core trees or retained module source inventory. Engine/water owners are checkpointed; no exclusive measurement reservation is active. Existing engine builder/auditor scopes and automations are unchanged; no agents or external messages used.

Consumed previous renderer finding: real client discovery/cache races and camera/travel/edit baselines remain missing, and neither helper tests nor server lifecycle proves GPU acceptance. This host still exposes no /dev/dri. Selected a bounded CPU allocation repair while preserving those gates; did not invent fresh client evidence.

Consumed latest independent audit docs/engine-hourly-audits/2026-10-26T1730-a1583a5/REVIEW.md at pinned current main. It freshly qualified dev31/ten packaged Linux sessions and the earlier delivered discovery, while carrying its whole-engine5.0 grade. Recharge/allowance fixes are now production and must not be called still staged. Later prelude guard remains engine-owned staged work. Its prior build/native/package results are historical, not this jam's passes.

## Six equally weighted renderer scores

All six production scores are explicitly **carried from jam5**, unchanged. This staged implementation earns no production implementation increase. No fresh whole-renderer or engine regrade is claimed.

| Category | /10 | Pinned production/test evidence and missing gates |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Current discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/bf7b36a21b7063c1d273c383008ddfd073d5e5b1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259); previous jam enumeration tests carried. Actual client visible coverage/handoff/travel/cache race remains unrun. |
| Mesh generation/reuse/scheduling | 6 | [Current scratch L45](https://github.com/Shnewbs/Deep-Primal/blob/bf7b36a21b7063c1d273c383008ddfd073d5e5b1/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L45); prior scratch allocation result carried, not rerun. Fresh candidate byte-query tests and final-checks.txt are development-only; no shipped credit. |
| GPU culling/submission/memory | 4 | [Current culling L477](https://github.com/Shnewbs/Deep-Primal/blob/bf7b36a21b7063c1d273c383008ddfd073d5e5b1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477); no GPU execution/submission/arena/descriptor/VRAM measurements. A CPU sum optimization is not a GPU memory reduction. |
| Actual FSR/DLSS implementation | 0 | [UpscalingPolicy L5](https://github.com/Shnewbs/Deep-Primal/blob/bf7b36a21b7063c1d273c383008ddfd073d5e5b1/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5); no providers or vendor dispatch introduced. Linear scaling/frame generation earn no integration credit. |
| Visual/backend/settings compatibility | 4 | [Current gpuBytes L20](https://github.com/Shnewbs/Deep-Primal/blob/bf7b36a21b7063c1d273c383008ddfd073d5e5b1/engine-core/src/main/java/com/shnewbs/deepprimal/core/HorizonMesh.java#L20); fresh exact geometry/byte checks in final-checks.txt, historical module/package audit only. Client OpenGL/Vulkan/settings/reload/resize/world-switch/water pixels unrun. |
| Validation and measured performance | 4 | [Candidate L16](candidate/LodMesher.java#L16), source-identity.json, compile-command.json, final-checks.txt. Fresh all-core compile/correctness and narrow allocations only; full Gradle/JAR/native/client/named-device frame gates unrun. |

Average **22/6 =3.67/10**, prior **3.67**, delta **0.00**. Target above8.5 remains unachieved. Independent engine grade is unchanged.

## Storage and delivery blocker

Filesystem headroom began292MiB, fell to26MiB during scoped checks and then reached0. Only an already completed renderer5 local export duplicate was removed after verifying its successful saved-file receipt and equality with the retained producer; cleanup.json records exact identity. Other jobs' output/source/caches/processes were untouched. This did not restore stable build headroom. A new isolated full project/cache/output/native qualification requires substantially more space than available. No partial JAR, failing build or older delivered binary is relabeled as new. No Library upload or release publication attempted because no qualified new artifact exists.

This is a temporary capacity gate, not permission to alter other feature schedules or take over storage cleanup. Candidate/source/test/evidence is checkpointed in Git; own scoped claim is released at receipt publication. Renderer schedule remains enabled as explicitly requested.

## Next repair and backlog

First restore adequate private build headroom, re-read main/claims, apply the exact candidate to a fresh current-source checkout, add the private check to the normal qualification task and run full Java25/Gradle/module build. Then verify producer completion, source/compiler/package identities, seven CRCs/native/platform payload/notices/art/fixture exclusion, uniquely versioned development JAR, packaged native save/reopen and actual applicable client/backend lifecycle gates before runtime promotion/delivery. Re-run any affected review/check after corrections.

Still open: GPU camera/travel/edit baseline with seed/camera/resolution/API/distance/detail/warmness/water/LOD context; p50/p95/p99 CPU/GPU frames, rebuild/cull/upload/allocation, draw/descriptor counts and resident/pending/cache RAM/VRAM; live native/LOD handoff/cache/edit invalidation and water ordering. Then hierarchical streaming/coverage and authoritative unexplored-terrain contract. Distance maxima remain125 native, explored LOD default192/max512; simulation stays separate. Real FSR/DLSS providers/licensing/render inputs/hardware are later dependent work, frame generation distinct.


<!-- END PRESERVED SOURCE -->
