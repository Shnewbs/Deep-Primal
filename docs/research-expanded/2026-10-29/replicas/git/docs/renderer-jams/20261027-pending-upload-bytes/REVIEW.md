# Expanded replica — docs/renderer-jams/20261027-pending-upload-bytes/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-pending-upload-bytes/REVIEW.md |
| Preserved source SHA-256 | `c9416321c40b941702b0ca3138018b1650ce0b2d1e6cf081d27465a0c5ca8c74` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Stages private PendingGpu stream removal using indexed iteration over three ArrayLists. The method intentionally recomputes sizes and therefore tolerates mutable queried byte sizes; the actual-private-method fixture passes while full artifact qualification is blocked at this historical checkpoint. The 10,063-case actual-class fixture is stronger than a shape-only test, but still not full GPU qualification.

## Research expansion — gaps and details to deepen

- This candidate is superseded by later integration and O(1) ledger work. The expansion should state that lineage and retain the original mutable-size semantics instead of making the old candidate appear to prove immutable ownership accounting.

## Research expansion — proposed contracts

- Add links to pending-accounting integration and the committed ledger, plus a status distinction between staged, compiled actual class, promoted source, and native exercise. Label reduced query allocation separately from resident or retained memory.

## Research expansion — acceptance checks

- Reproduce the private-method equivalence cases and verify later integration preserves its intended contract or explicitly migrates it. Require exact-artifact native admission measurements before associating the staged allocation result with frame-time improvement.

## Research expansion — source anchors

- Renderer jam 20261027-pending-upload-bytes — staged candidate self-audit
- Concrete increment and code review

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `checks/PendingUploadBytesChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/checks/PendingUploadBytesChecks.java) |
| `evidence/candidate-compile.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/candidate-compile.json) |
| `evidence/candidate-pending-bytecode.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/candidate-pending-bytecode.txt) |
| `evidence/checks-complete.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/checks-complete.json) |
| `evidence/component-measurements.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/component-measurements.json) |
| `evidence/full-core.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/full-core.txt) |
| `evidence/negative-controls.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/negative-controls.json) |
| `evidence/probe-1-candidate.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/evidence/probe-1-candidate.txt) |
| `integration-proof.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/integration-proof.json) |
| `run_checks.py` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-upload-bytes/run_checks.py) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-pending-upload-bytes — staged candidate self-audit

Inspected main **3ced10a9e1491bf3b57e45bfa64489166fba2a9b**; ownership receipt **5c625a29fd3ebce3dc988a1a9b115407ca95019c**; exact runtime candidate **b0cd440bb11818a35f3042b8c5bd17e9deed24f0**, parent **5c625a29fd3ebce3dc988a1a9b115407ca95019c**. Candidate is **not promoted** to main. Existing delivered runtime remains **2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e**, development renderer.7; no new version or JAR is claimed.

This is the renderer author's read-only, scoped self-audit, not an independent external audit or a fresh complete renderer/engine regrade. Implementation and checks completed before scored review at 2026-10-27 04:03:12 UTC. Candidate source was fetched back exactly from its commit. All238 recorded compiled class snapshots were rehashed unchanged after checks. No implementation was edited during the review.

## Concrete increment and code review

Only [VoxelLodRenderer.PendingGpu.bytes, L96](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96) changes: replace stream/flatMap/mapToLong/sum with indexed traversal of the existing private ArrayLists and a long sum. This avoids transient streams and iterators during repeated upload-admission, stale-result subtraction and completed-entry byte queries. Traversal still recomputes current values and remains O(parts); it is not cached accounting, buffer reuse, a GPU arena, culling, batching or indirect submission.

Reviewed complete exact candidate and single-method diff. Constructor creates the outer and all three inner ArrayLists; the private upload path appends actual successful meshes. The fixture asserts those list invariants. All details, empty/partial progress, removed parts, current mutable byteSize values and values above signed32-bit range remain included. No persistent counter can become stale. Current renderer-thread ownership is unchanged; this change adds no concurrency contract.

[Budget rejection L606](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L606), [obsolete subtraction L615](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L615), [actual upload L639](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L639), [append L648](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L648) and [entry accounting L662](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L662) are unchanged except line displacement. Buffer creation, closure, upload failure handling, revisions, priority, budgets and residency are not redesigned. Bytecode inspection confirms long arithmetic and indexed queries in the actual compiled nested class. Constructor/close behavior is unchanged.

No terrain generation, finite-water simulation/chemistry/quantities, topology/material/UV/lighting/normals/silhouette/edge/water mesh data, shaders, art, resources, saved profiles/edits, options/maxima, networking, SDK/native dependency or frame generation changes. Frozen V1–V7 and module separation remain protected.

The one production-path edit is stored as a complete source file under candidate/ and an exact patch under evidence/. Main receives only this dedicated docs directory, checks/tools/evidence, backlog, ledger and released claim. The new fixture is private staging tooling, not yet registered in a normal client-validation task; registration and packaged fixture exclusion must be verified during qualification.

## Fresh executable evidence

Run: `python3 /dev/shm/dp-renderer-pending-bytes-c748d588/run_checks.py`. [Runner](run_checks.py), every full executable command/classpath, exit and elapsed time are in evidence/*-compile.json, probe-*.json and full-core.json.

- **385 current-main Java blobs verified** across core/root/graphics/LOD; fresh Java25.0.4.1+1 javac -g compiles **142 sources (76 core +66 fixtures)**, exit0. Full fresh independent **CoreChecks exit0,258.263s**, including frame/transition, LOD reduction/byte/scratch/bounds/max-height/cancellation/parallel checks and existing frozen terrain/water/cache regressions. This is the ordinary core suite; it does not execute client GPU uploads.
- Baseline and candidate full VoxelLodRenderer source each compile cleanly with `javac -g -proc:none -sourcepath ''` against actual Minecraft26.1.2/NeoForge26.1.2.122 APIs, fresh core and own prior qualified module classes. **400 prior module dependency classes** match renderer.7's recorded compiled hashes. This is a narrow actual-API compile, not a fresh full Gradle/module build.
- Four fresh JVM processes in baseline/candidate/candidate/baseline order invoke the actual private compiled PendingGpu.bytes via a bound MethodHandle. Every process asserts the loaded candidate/baseline class location, avoiding an accidentally loaded production copy.
- **10,063 oracle assertions per fork**, five real LodMesher scenes (solid, separated roof/air, stepped material, fractional water, tall solid/water), partial uploads, clear/removal/current-byte mutation, long totals,10,000 deterministic random mutations. Independent Math.addExact traversal is the oracle.
- Real mesh geometry signatures and admission byte totals agree before/after. Fixtures use constructor-free TerrainGpuMesh holders containing actual mesher-derived byte sizes; **no buffers, device, native upload, close or GPU pixels execute**. Test-only Unsafe warnings are retained; it is not a runtime dependency or payload.
- Two compiled negative controls are correctly rejected: omitting the coarse detail and narrowing the sum to int. Their nonzero exits with the expected Wrong resident bytes assertion are passing rejection tests, not hidden product failures.
- Candidate/source hashes and all238 fresh compiled classes are frozen in review-frozen-snapshot.json/class-sha256.json. Actual API compiled dependencies and source hashes are separately retained. javap returned0; its default perf-data shared-memory warning is preserved, not a behavioral failure.

## Matched component observations

See [all20 measurement rows](evidence/component-measurements.json) and four raw probe logs. Host Linux x86_64 AMD EPYC9V74,9 eligible CPUs; no exclusive timing reservation, other jobs continue. Each scene:100,000 warm queries,128 measured batches of1024 queries. Allocation uses the calling thread's ThreadMXBean counter; query execution uses the actual compiled private class. Scene coordinates x=-17,z=-3,minY=-64, five real material/fluid fixtures. Camera/resolution/game API/distance/detail/cold-world state are not applicable and are null; this is not a running client baseline.

Observed allocation per query: baseline **544.000000–550.882568 bytes**; candidate **0–0.002258 bytes** across both forks/five scenes. These are measured process/component results, not a guarantee of exactly zero allocation in all builds/hardware. Geometry signatures and byte totals match in every scene.

Raw timing p50/p95/p99 values are **percentiles of batch-average query times**, not individual-call or frame percentiles. Candidate batch-median query costs range16.440–47.582ns; baseline59.602–194.991ns in these processes. Short microbenchmarks, JIT phases, MethodHandle overhead and nonexclusive host contention limit inference; candidate scene3's second-fork p99 is240.959ns versus the first baseline194.610ns, so no universal tail improvement is claimed. No CPU/GPU game-frame, FPS, upload latency, driver-allocation, VRAM, draw/descriptor or usable-distance gain follows.

## Delivery blocker and unrun gates

Fresh real1MiB writes fail with **ENOSPC** in both workspace and /tmp, even though filesystem_free_blocks shows16MiB; available blocks are zero. Private memory fluctuated roughly236–401MB during checks and was261,468,160bytes at the recorded later preflight. A real memory-backed probe succeeds. This is insufficient for the isolated full Themes/distribution producer plus mutable build/native/package outputs. No other writer's cache, files, outputs or jobs were reclaimed or paused.

Full Java25/Gradle/module build is **not run for this candidate**, not a passed carried result. Thus final outer/nested CRCs, native/platform payload, normal fixture exclusion, compiled/packaged identities, artifact size/SHA256, producer completion and packaged client/backend reload/resize/world-switch/save/reopen/shutdown qualification are unrun. No candidate binary was produced, relabeled, saved or uploaded. Prior renderer.7's full35-task/package/fourLinux results and the newly consumed independent audit's results belong to their pinned source only.

Hardware gates remain unrun: this host has no /dev/dri; no OpenGL/Vulkan client, Windows, AMD8060S or NVIDIA execution; no live discovery/readiness/native handoff/cache/edit/travel/negative-seam/silhouette/lighting/cutout/transparent-water ordering or sustained client/multiplayer lifecycle acceptance. No SDK provider/dispatch/resource-lifetime integration is added. Linear scaling remains distinct from FSR/DLSS, and frame generation is a later separate feature.

## Six equally weighted renderer grades

All numbers below are explicitly **carried unchanged** from the renderer.7 self-audit. This scoped candidate review supplies narrow fresh evidence, not a fresh complete six-category acceptance audit. Unpromoted code earns no shipping feature credit.

| Renderer category | /10 | Pinned source/check/measurement evidence and missing acceptance |
|---|---:|---|
| Distance/LOD correctness and usable coverage |4| [Delivered discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259); fresh [full core log](evidence/full-core.txt) discovery/geometry/bounds checks. No increased maximum, unexplored-delivery contract or actual client/hardware coverage. |
| Mesh generation/reuse/scheduling |6| [Delivered mesher byte traversal L16](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16); fresh core signatures,64 parallel/max-height/cancel checks. New [private query L96](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96) and [component rows](evidence/component-measurements.json) are staged; no new shipping scheduling/meshoptimizer credit. |
| GPU culling/submission/memory |4| [Candidate byte query L96](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96), [actual compiled bytecode](evidence/candidate-pending-bytecode.txt), [four probes](evidence/checks-complete.json); this is CPU admission-query allocation. [Delivered ordinary indexed draw L121](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L121) is not real GPU MDI. No new GPU-memory/reuse/culling/draw/descriptor measurement. |
| Actual FSR/DLSS implementation |0| [Delivered fallback policy L5](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5); providers/inputs/dispatch absent. Candidate changes no SDK path. No FSR/DLSS or frame-generation credit. |
| Visual/backend/settings compatibility |4| [Upload unchanged L639](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L639), [candidate actual-API compile](evidence/candidate-compile.json), [oracle/signature probes](evidence/probe-1-candidate.txt). No new actual backend pixels, packaged client/Windows lifecycle or water-ordering evidence. |
| Validation and measured performance |4| [Exact query L96](https://github.com/Shnewbs/Deep-Primal/blob/b0cd440bb11818a35f3042b8c5bd17e9deed24f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96), [fixture](checks/PendingUploadBytesChecks.java), [negative controls](evidence/negative-controls.json), fresh142-source/full-core/actual-API/fourfork evidence and [component measurements](evidence/component-measurements.json). Full Gradle/package/native and complete named-hardware camera/travel/edit frame/pipeline/memory baselines absent. |

**22/6=3.67/10; previous3.67; delta0.00.** Target >8.5 remains unmet. Whole-engine independent5.0 remains unchanged.

## Coordination, consumed audits and next repair

Read current main, root/module AGENTS absence, engine/water states, architecture and graphics/LOD roadmaps, scheduler/build policy, recent commits, all renderer claims, active audit/run records and measurement ownership. General builder active scope is its combined water-contact-commit docs/state; water scope is checkpointed/released. No candidate file overlaps an active writer; only this dedicated docs path is integrated. All old renderer claims are released. Performance reservation was released/nonexclusive; these component observations do not claim exclusive hardware.

Consumed preceding renderer.7 receipt: full qualification is mandatory before promotion, private writable-block probes must supplement reported availability, GPU remains absent, and further work should include concrete bounded upload/mesh changes rather than only diagnostics. Consumed initial0230 independent audit and freshly published [0332 audit of2e1aa76](https://github.com/Shnewbs/Deep-Primal/blob/0dd3d8bec006de27d742e96e7758b00359e8e7dd/docs/engine-hourly-audits/2026-10-27T0332-2e1aa76/REVIEW.md): telemetry is CPU observability, its35-task/10Linux/product-source results do not prove client/GPU acceptance; staged candidates remain excluded pending their own full producer gates. The latest audit reports renderer.7 packaged frame/transition checks passing; those are independently authored historical results, not rerun here or validation of this candidate.

Main advanced to0dd3d8b during checks with audit documentation only; the compare contains no runtime changes and our claim is preserved. Fresh parent/claim/state checks precede guarded nonforce publication. No source/work-state/other schedule, source-art/player-save, other writer cache/output or job was edited/overwritten/killed/paused; no agents or outbound messages. Scoped ownership is released in the final receipt. Dedicated renderer continuation remains enabled.

Next repair: restore enough isolated writable build/package headroom, register the actual-API private admission fixture in focused validation, and qualify this one-file candidate via the full Java25/Gradle/module producer, archive/payload/fixture/source-compiled-package identity and packaged lifecycle checks before promotion and a uniquely versioned new JAR. In parallel, obtain actual GPU scenes for camera/travel/edit/coverage/water/client gates and complete CPU/GPU/draw/descriptor/allocation/RAM/VRAM baselines. Then bounded mesh/upload reuse, hierarchy/refinement/hysteresis/coverage/invalidation/streaming, authoritative unexplored-terrain delivery coordination, independent persisted requested/effective distance/simulation budgets and real vendor upscaling. No task acceptance or uninterrupted background execution is claimed.


Final integration parent 3d24e86f9dc0473d1ffa07dda7617e3c1dcf1c91 preserves subsequent builder checkpoint documents/state. Non-documentation root identities remain exactly equal to initial main; own active claim and builder scope were reread immediately before integration. See [integration proof](integration-proof.json). Released at 2026-10-27 04:08:06 UTC.


<!-- END PRESERVED SOURCE -->
