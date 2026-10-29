# Expanded replica — docs/renderer-jams/20261027-admission-gate/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-admission-gate/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-admission-gate/REVIEW.md |
| Preserved source SHA-256 | `309f9d3015f6a8b709e29c7c3586176c96987e1d70d1442c1ba44f89e5f151ff` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Stages a real-class admission test gate for PendingGpu byte accounting, with explicit module registration and source/classpath identity checks. It distinguishes the actual private production method from a duplicated shape and notes that root checks can skip the LOD module. The 10,063-oracle checks qualify a focused staged gate, not a full installer.

## Research expansion — gaps and details to deepen

- The important expansion is gate routing and evidence identity. A passing root task with LOD excluded must not satisfy the renderer gate, and duplicated frozen oracles need provenance so later fixtures cannot silently drift from the actual admission semantics.

## Research expansion — proposed contracts

- Add a machine-readable required-task manifest showing module inclusion, actual class origin, fixture hash, oracle version, and skipped reason. Keep staged source separate from production promotion and require a fresh merged candidate rather than overwriting newer renderer work.

## Research expansion — acceptance checks

- Run the actual private admission path with overflow, mutation, empty, and multi-part cases; deliberately exclude the module and confirm qualification remains incomplete. Promote only after source, module, package, and exact-artifact native gates are separately recorded.

## Research expansion — source anchors

- Renderer jam 20261027-admission-gate — scoped self-audit
- Concrete change and review

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `run_gate.py` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-admission-gate/run_gate.py) |
| `verify_gate.py` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-admission-gate/verify_gate.py) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-admission-gate — scoped self-audit

Initial main 155218b2035d528d9526bb39d1e0db4b15ae40d2; claim afa9694bc4cefe7f057da664998912499fcd4dbf; final candidate **4794d5decef56361000f9782e00df5bb46c2d274**. This is an **unpromoted candidate** containing the previous pending-byte optimization plus three new validation/build files. Main receives only this dedicated docs directory. Delivered runtime remains2e1aa76 / renderer.7; no new distribution version/JAR.

This is the renderer author's read-only scoped self-audit, not an independent external audit or a fresh complete renderer regrade. Review started 2026-10-27 05:03:57 UTC, after final code and affected checks completed. All four source files were read back exactly at the final candidate. Sixteen compiled class snapshots are frozen; implementation is unchanged during review.

## Concrete change and review

[LOD build script L15](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/build.gradle#L15) adds a private admissionChecks source set with actual module and game API compile/runtime classpaths. [Java25 pendingGpuBytesChecks L21](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/build.gradle#L21) invokes the real private PendingGpu accounting fixture. The task is connected to module check atL30 and root check atL32; root withoutLod skips the gate. Module-only checks still run it when explicitly requested. Java library jar assembly does not package this separate source set.

[PendingUploadBytesChecks L38](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/admissionChecks/java/com/shnewbs/deepprimal/core/lod/PendingUploadBytesChecks.java#L38) asserts actual product-class location, actual private ArrayList layout and10,063 independent oracle checks. The existing fixture is moved into candidate validation with a new invocation of [frozen geometry signatures L49](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/admissionChecks/java/com/shnewbs/deepprimal/core/lod/LodReductionChecks.java#L49): ten real material/water/air-gap/tall geometry cases at negative and elevated minima. The helper is copied from the current registered core fixture without changing frozen expected signatures. This small duplication is a maintenance cost; future fixture changes must keep the expected core and admission signatures aligned.

Pending byte arithmetic [L96](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96) is unchanged from b0cd440. No new runtime optimization, GPU arena/MDI/culling, shader, simulation, water surface/transparency/order, geography, profiles/edits, art/resources/notices, distance maximum, tracking/networking, SDK provider or frame generation change.

Fixture uses constructor-free TerrainGpuMesh holders with actual mesher-derived byte sizes and a bound MethodHandle into the real private product class. No GPU buffers/device/uploads/close or client pixels execute. Test-only Unsafe warnings are retained; Unsafe is not added to production source or bundled fixtures.

## Fresh checks and their exact limits

[run_gate.py](run_gate.py) and [verify_gate.py](verify_gate.py) retain actual commands. Evidence records complete command arrays, stdout, exits and durations.

A **targeted isolated Gradle9.2.1/Java25 harness**, not the complete NeoForge root build, executes the exact candidate lod-engine/build.gradle and all six actual LOD module sources. Its small root provides the Java plugin, actual already-restored Minecraft/NeoForge classpath and a no-op artifact prerequisite; its core/graphics projects expose own previous qualified compiled dependencies. It does not compile those modules afresh, run NeoForge artifact generation or prove full-root plugin/classpath compatibility. Harness scripts and inputs are preserved. This distinction is material.

Fresh pass matrix:
- Root check target executes new module compilation, private fixture compilation and actual pendingGpuBytesChecks. Initial root-only registration passes; subsequent refinement also registers module check, then repeats affected matrix on the final snapshot.
- Root check --dry-run includes the task; -PwithoutLod excludes it. Explicit module check --dry-run includes it.
- Deliberately omit coarse-detail bytes in the harness product source, then :check --rerun-tasks: exit1 with the expected Wrong resident bytes assertion. This is a successful negative-control rejection.
- Restore exact candidate, then :check :lod-engine:jar --rerun-tasks: exit0, **four executed tasks**, final ten frozen signatures and10,063 oracle assertions pass. Final matrix duration9.212s is a functional gate duration, not renderer latency.
- Diagnostic LOD module JAR only: **42,493 bytes**, SHA256 **a64e9c322f8efc1ea6e37b0845afc3299fac41fbdbc72fea72ed080e179a5d44**; CRC/no duplicate entries pass, **14 compiled class identities** match, both private fixture classes excluded. This tiny component is not a testing installer or user-facing release and is not delivered as one.
- Sixteen total main/fixture class snapshots retained. No code changed during review. Prior142-source/full-core258.263s/ABBA/allocation and two arithmetic controls are carried from the preceding jam, **not freshly rerun as a full core suite here**.

Incidental fixture MEASURE lines are retained as diagnostic output. There is **no fresh matched before/after performance study** in this jam. Previous544–551B/query to<=0.002258B/query ABBA result belongs to the preceding candidate run. Batch query percentiles are not frame percentiles or GPU timing. No FPS, native upload, memory residency or usable-distance gain is claimed.

## Blocker, delivery and missing gates

Actual1MiB workspace and /tmp writes again fail ENOSPC, despite16MiB reported filesystem-free blocks; reported available blocks are zero. Private memory was316,583,936bytes initially and380,387,328bytes at later preflight, insufficient for isolated full Themes/distribution plus mutable build/native outputs. No /dev/dri. No other writer's resources were reclaimed, paused or killed.

**Full Java25/Gradle/NeoForge/module distribution build is unrun for this candidate.** Targeted four-task success does not replace it. Final outer/nested archive CRC/native-platform payload/notice/resource/fixture exclusion/source-compiled-packaged identity/producer completion/size/SHA checks for a complete JAR are unrun. Packaged client/backend reload/resize/world-switch/save/reopen/shutdown, Linux server distribution sessions, Windows/AMD8060S/NVIDIA, actual OpenGL/Vulkan pixels, transparent-water ordering, native handoff/edit/cache/travel/seams, full frame/pipeline/draw/descriptor/RAM/VRAM and long-soak baselines remain unrun.

No new qualified complete JAR, Library save, release upload, stable or CurseForge publication. Existing renderer.7 and independent audit distributions exclude this candidate and are not relabeled.

## Six equally weighted renderer scores

All scores are **carried unchanged** from the previous renderer self-audit; fresh evidence is limited to the gate/module diagnostic. No shipping credit for unpromoted validation or optimization. Source/test evidence is pinned here or at the delivered runtime.

| Category | /10 | Evidence and missing acceptance |
|---|---:|---|
| Distance/LOD correctness and usable coverage |4| [Delivered discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259); fresh frozen signatures in evidence/final-check-and-module-jar.txt. No real client handoff/seams/coverage/unseen-delivery acceptance. |
| Mesh generation/reuse/scheduling |6| [Frozen geometry L49](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/admissionChecks/java/com/shnewbs/deepprimal/core/lod/LodReductionChecks.java#L49), fresh ten signatures and10,063 accounting cases. No fresh scheduling/meshoptimizer/real-frame gain; preceding allocation result carried only. |
| GPU culling/submission/memory |4| [Private query L96](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L96), fixture and module identities in evidence/gate-matrix.json. CPU accounting only; no actual GPU-memory/MDI/culling/draw evidence. |
| Actual FSR/DLSS implementation |0| [Delivered policy L5](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5); no provider/inputs/dispatch integration. Linear scaling/frame generation earn no credit. |
| Visual/backend/settings compatibility |4| [Actual-API gate L21](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/build.gradle#L21), fresh root/module/withoutLod graphs and diagnostic fixture exclusion in evidence/gate-matrix.json. Full real root/withoutLod distributions and GPU/backend/client pixels/lifecycle remain unrun. |
| Validation and measured performance |4| [Fixture L38](https://github.com/Shnewbs/Deep-Primal/blob/4794d5decef56361000f9782e00df5bb46c2d274/lod-engine/src/admissionChecks/java/com/shnewbs/deepprimal/core/lod/PendingUploadBytesChecks.java#L38), expected negative rejection, final four-task targeted gate,14 class identities and component CRC/exclusion. No fresh complete core/distribution or matched hardware performance acceptance. |

**22/6=3.67/10; previous3.67; delta0.00.** External whole-engine5.0 unchanged; >8.5 target unmet.

## Provenance, ownership and next repair

Read current main/states/claims, prior renderer audit/backlog, architecture and roadmaps; root/module AGENTS remain absent. Non-documentation root identities equal preceding delivered source; applicable roadmap/architecture blobs unchanged. Latest independent0430-bbf07e1 audit and its completed released lease consumed: it supplies full baseline build/native evidence while explicitly excluding staged code and denying feature credit to diagnostics. Those results belong to its source, not this candidate. Engine/water claims are checkpointed; measurement reservation released/nonexclusive. Only this dedicated docs path is integrated under an expected-parent nonforce guard. No other schedules/work-states, mutable outputs/caches, user saves or source/art were edited; no agents/outbound messages.

Consumed previous jam's explicit next repair by registering the affected fixture and verifying task routing/failure/exclusion. Remaining repair: recover private writable headroom, run the complete actual root Java25/Gradle/NeoForge/module build with the staged four-file candidate, repeat archive/platform/resource/identity/fixture and packaged lifecycle gates, then guarded promotion and uniquely versioned complete testing JAR. Actual GPU coverage and camera/travel/edit baselines stay open. Continue bounded reuse/scheduling, hierarchy/streaming/unexplored delivery contract, independent requested/effective distance/simulation budgets and actual FSR/DLSS providers/inputs/lifetimes/licensing after dependencies permit. Frame generation remains later/separate. Claim released at final integration; dedicated renderer continuation remains enabled.


Final reread at publication: parent 2bc1f69c6d80d7b184e3d370b1c62335e3c7db30 preserves newer builder water-contact-probes documents/state. All non-doc root identities remain identical; own active claim and current builder scope were checked. See integration-proof.json. Scoped renderer claim released at 2026-10-27 05:06:46 UTC.


<!-- END PRESERVED SOURCE -->
