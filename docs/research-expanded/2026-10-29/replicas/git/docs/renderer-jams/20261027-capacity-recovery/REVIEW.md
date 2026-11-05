# Expanded replica — docs/renderer-jams/20261027-capacity-recovery/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-capacity-recovery/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-capacity-recovery/REVIEW.md |
| Preserved source SHA-256 | `8dbcb114a594022647b3630662f4a7b1b6ce23ec25ecfef3172c67bddc8e1de9` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Promotes bounded steady-frame and transition telemetry after isolated build-capacity recovery. A 512-interval steady window resets on context changes, while crossing intervals are retained separately; complete historical build/headless checks and corrected artifact identity are recorded. The successful 35-task/four-session qualification applies to the frozen promotion, not later runtime changes.

## Research expansion — gaps and details to deepen

- Transition intervals include waits and cannot isolate world-load or GPU execution. The eight context coordinates need an extensible event fingerprint for backend/provider and resource generation changes, with retained cause information rather than a single mixed transition percentile.

## Research expansion — proposed contracts

- Add context-generation and transition-cause records correlated to artifact, scene, camera, resolution, active provider, and frame identity. Preserve all crossing gaps outside the steady distribution and identify invalid-clock or uninitialized observations explicitly.

## Research expansion — acceptance checks

- Replay each context transition, zero/backward/wrapped intervals, and world-reference release. Measure same-device steady and transition distributions with telemetry enabled and disabled, preserving long gaps rather than discarding them to improve the summary.

## Research expansion — source anchors

- Renderer jam 20261027-capacity-recovery — renderer.7 self-audit
- Provenance, ownership and next priority

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-capacity-recovery — renderer.7 self-audit

Pushed runtime **2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e**; tested build-execution source **f948194d255cae51684525b49c802afa3c7d7c77**; initial inspected main **2f9189a22c2db1eb244220ede4e19f3a79312a0e**; final integration parent **87904222016cc483ffee54d0f6399a1bb3949b2c**. Development version **0.0.1-alpha.29-dev.31-renderer.7**, unique recorded build override; Themes stays0.1.0-alpha.8.

This is the renderer jam author's read-only self-audit, not an independent external audit or whole-engine regrade. Implementation was complete before review. All five Java files were read back exactly at the tested candidate and pushed commit; non-documentation root/tree identities prove the reparented runtime inputs equal the tested source. No implementation changed during the scored review. Later receipt/cleanup/cache/upload work changes no product code.

## Delivered renderer increment

Promoted the previously staged frame/transition telemetry into normal production modules and registered ordinary CoreChecks. The client now exports CPU end-frame p50/p95/p99/max, sample count, resolution and per-instance window generation. A bounded512-sample steady window resets on world-object identity, width, height, focus, effective FPS limit, effective native distance, vertical distance and active LOD distance. It drops the interval crossing unlike contexts from the steady window and publishes immediately on reset.

A separate window capped at64 retains valid observed crossing gaps and a since-clear count, with p50/p95/p99/max and explicit scope. Initial observations and reversed clocks invent no crossing sample; zero gaps and signed nanoTime wrap work. clear releases the retained world reference and both histories. These are frame intervals including waits/limiting, **not pure CPU work, GPU execution time or an isolated world-load timer**. Transition aggregates mix kinds of context change; full scene/cause attribution is still absent.

Actual product/source changes:
- [FrameIntervalWindow.java L15](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/FrameIntervalWindow.java#L15): bounded steady/transition recording; valid-gap branch atL31.
- [ClientTelemetry.java L27](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/main/java/com/shnewbs/deepprimal/client/ClientTelemetry.java#L27): context observation, existing millisecond keys preserved, p99/count/resolution/scope and transition exports atL38.
- [CoreChecks.java L8](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L8): normal-suite registration.
- [FrameIntervalChecks.java L8](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/coreChecks/java/com/shnewbs/deepprimal/core/FrameIntervalChecks.java#L8): prior oracle/reset checks and transition fixture invocation.
- [TransitionIntervalChecks.java L8](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/coreChecks/java/com/shnewbs/deepprimal/core/TransitionIntervalChecks.java#L8): independent bounded deque/sorted oracle and edge checks.

No terrain-generation, water simulation/chemistry/quantities/surface/shader, art/resource-ID/notice, distance maximum/tracking/network, mesh layout, SDK or frame-generation change. Native125 and explored LOD192 default/512 maximum remain. No staged source-index/contact/water-budget implementation is included; their concurrent documentation is preserved.

## Fresh qualification and artifact identity

Final Java25.0.4.1+1 / Gradle9.2.1 **35 executed tasks**, forced --rerun-tasks, :build and :visualValidationClasses, producer exit0 in303.439s. Full ordinary core/cache/resource/module checks pass, including registered new frame/transition cases and existing LOD byte/scratch/geometry/bounds/parallel/max-height/cancellation and water/version regressions. Functional build duration is not a renderer benchmark.

Fresh independent javac -g compiles **142 sources** (76 production core,66 fixtures); affected frame/transition fixtures pass with -Xverify:all. Full independent CoreChecks from the prior staged jam is carried, not claimed fresh here; this run's full suite executes in the forced Gradle build.

Fresh affected evidence:
-770 steady oracle windows/10,000 supplied intervals,8 independent context-reset cases;1,024 transition oracle windows at capacities1/17/64/512, bounds64, clear, zero/negative/wrap/steady separation checks.
-Actual extracted product core precedes all fixture directories. PackagedFrameChecks asserts FrameIntervalWindow code source equals that nested JAR, then the same fresh fixtures pass.
-The previous negative-control experiments are carried context, not rerun this jam. No measured real game-frame, allocation, FPS, VRAM or usable-distance gain is claimed.

Seven recursive archive CRC/duplicate/hidden-entry gates pass. **593 compiled/product/package classes**, **147 independent core matches**, **226 private fixture exclusions**. Both actual DLLs are checked as **PE AMD64**, not merely filename suffixes; no Linux/macOS Vulkan payload. OpenGL fallback startup/classes remain. Exact Vulkan/module notices, **2,508** Themes resource hashes,43 palettes,11 approved ground images and148 generated soils pass. Asset generators and private checks are excluded.

Four final actual packaged Linux server sessions pass: default fresh/reopen with saved channel2, and opt-in fresh/reopen with saved channel5 retained after disabling its creation toggle. Seed20261020, view/simulation2, fresh unique worlds, private ports26431/26432. Require terrain/deposit replay, source/soil/codec checks, live loaded-class identities, acknowledged save-all flush, failure precedence, ordered shutdown and exit0. All six nested mods are selected from this exported outer JAR in every session. These are server regressions, not client/GPU/Windows acceptance.

Final producer and all native consumers completed before delivery. Producer/export bytes and all7 CRCs were checked after native runs; SHA256/size checked again after saving. No earlier or failing binary relabeled. Complete commands/results and exact private harnesses are retained in evidence and tools; compact class identity records retain each equal compiled/package hash plus the raw receipt hash.

[Download renderer.7 development testing JAR](https://chatgpt.com/library?file_id=libfile_86fb5c7d020c8191a5dfdb450a6e47e9&account_id=personal).

**125,298,439 bytes**, SHA256 **cea144872616bd6d02bd7d3af3f9b275c06a5442c36c77f515deb48d0f737068**. Actual returned Library identity libfile_86fb5c7d020c8191a5dfdb450a6e47e9; save succeeded and local metadata applied. This is a new renderer.7 producer tied to pushed runtime 2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e and proven-identical tested inputs f948194d255cae51684525b49c802afa3c7d7c77. No release-write/upload tool or gh executable was available; no GitHub Release, stable or CurseForge publication is claimed.

## Capacity recovery, rejected attempts and isolation

The initial disk report showed0available bytes. Verified extracted JDK25/JDK21/Gradle commands and pinned hashes, then removed only completed own redundant download archives and expired renderer.6 export copies. File lengths are not claimed as physical reclaimed bytes. A bounded1MiB real write probe succeeded despite available_blocks=0: this process could use approximately499MB of filesystem free blocks. The earlier available-byte-only gate was insufficient for this recovered-root situation. A fresh full build placed the large Themes output in private memory and completed normally.

Native retries were **not** accepted:
1. Initial nested-jar extraction failed with No space left on device in the old disk cache.
2. Moving completed own cache metadata through workspace symlinks did not persist as intended between calls; a retry failed writing Gradle temporary scripts.
3. A subsequent immutable Gradle script workspace lacked metadata from the interrupted setup; rejected.
4. An attempted full private dependency-cache copy filled shared memory before launch. A following setup attempt also failed before a server launched. Preserved the failure history and removed only this own failed partial copy and already verified reproducible Themes outputs.

Final native launches use a **direct private memory Gradle home/project cache and mutable metadata**, plus read-only links to136 immutable dependency JARs from the completed own cache. All136 were rehashed unchanged after consumers. No other role's mutable cache/output is borrowed. Completed outer producer/compilation tasks and Themes tasks are disabled only for immutable packaged native launches; classes/resources/producer had already passed the full forced build. Class-source, six-nested-selection and final byte guards are mandatory and pass. Primary dependencies, source, art and user saves are preserved.

Two upload setup attempts failed creating the service temporary directory before any successful transfer. After hashing/removing three obsolete own incomplete download chunks, the actual /tmp directory probe passed; the same current prepared-upload helper/request succeeded. No unknown upload outcome or duplicate successful save is claimed. Failed setup logs are separated from final pass receipts.

The original producer JAR, hash and source stayed frozen throughout native/cache/upload repairs. Temporary outputs/cache relocation are not claimed as rendering performance gains. Resource/deprecation/compiler warnings remain; no code was changed to bypass checks.

## Six equally weighted renderer scores

All six category numbers are explicitly **carried unchanged** from the preceding renderer self-audit, with fresh scoped evidence. Shipping this instrumentation does not establish broad hardware, coverage or upscaler acceptance.

| Category | /10 | Pinned implementation/check evidence and remaining gate |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259), fresh LOD/core regressions in build.txt. No actual GPU readiness/handoff/cache/edit/travel/silhouette/signed-seam/coverage or unseen-delivery acceptance. |
| Mesh generation/reuse/scheduling | 6 | [Byte traversal L16](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16), fresh byte/scratch/geometry/64parallel/max-height/cancel cases in build.txt. Prior query-allocation gain carried, not remeasured. No new mesh/scheduling/frame gain or shipping meshoptimizer. |
| GPU culling/submission/memory | 4 | [Culling L477](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477), [ordinary indexed draw L121](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L121). No new GPU culling/arena/MDI/descriptor/VRAM measurement; CPU interval telemetry is not GPU time. |
| Actual FSR/DLSS implementation | 0 | [Fallback policy L5](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5). Providers/required inputs/dispatch absent. Basic linear scaling and frame generation earn no FSR/DLSS credit. |
| Visual/backend/settings compatibility | 4 | [Telemetry L27](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/main/java/com/shnewbs/deepprimal/client/ClientTelemetry.java#L27), fresh full-module build, PE/native payload/resource checks and four packaged sessions. Actual OpenGL/Vulkan client pixels, resize/reload/world switch, water ordering and Windows/AMD lifecycle unrun. |
| Validation and measured performance | 4 | [Helper L15](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/engine-core/src/main/java/com/shnewbs/deepprimal/core/FrameIntervalWindow.java#L15), [transition fixture L8](https://github.com/Shnewbs/Deep-Primal/blob/2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e/src/coreChecks/java/com/shnewbs/deepprimal/core/TransitionIntervalChecks.java#L8), fresh35-task/593/147/226/7archive/4session receipts and true product-source fixture. Named-hardware camera/travel/edit CPU/GPU/pipeline/memory baselines and instrumentation overhead remain unmeasured. |

**22/6=3.67/10; previous3.67; delta0.00.** External whole-engine5.0 is carried and unchanged; no independent complete regrade or >8.5 target acceptance.

## Provenance, ownership and next priority

Consumed prior frame-window/transition reviews: preserve context separation and crossing hitches, distinguish intervals from GPU/load timing, register normal checks, and close full build/package/native qualification before promotion. Consumed latest independent0230-2e15d24 audit: its35-task/10native checks are historical context, staged candidates stay excluded until their own qualification, and GPU/performance acceptance remains open. Read current main, root AGENTS absence, engine/water states, architecture/graphics/LOD roadmap, recent runs/claims/measurement reservations. Source/roadmap blobs remained current.

862 inherited tracked input hashes match the expected source (including the two intended existing-file substitutions); three newly added Java files match the pinned candidate. Non-doc root/tree equality proves final reparenting preserves all tested runtime/build inputs. One stale-parent check stopped before any update; fresh integration preserved concurrent water-contact-budget documentation/state under expected-parent nonforce guards. The water builder's active scope was its own budget docs/state; renderer scope was these five Java paths plus this receipt. No other schedules, jobs, work states, source/art or player worlds were edited, killed or paused. No sub-agents/messages to others/exclusive timing reservation.

Own ownership is released in the final receipt at the actual boundary. Dedicated renderer work remains enabled; finite execution is not uninterrupted background work.

Next: actual GPU discovery/readiness/native handoff/cache/edit/travel/water/lifecycle coverage and complete reproducible scene/seed/camera/resolution/distance/detail/cold-warm CPU/GPU frame/pipeline/allocation/draw/descriptor/resident/pending/cache RAM/VRAM baselines. Eight reset coordinates and aggregate crossing gaps are not a complete scene fingerprint/cause trace. If hardware is unavailable, continue dependency-ready bounded mesh/upload reuse and scheduling with constrained-input checks and honest component measurements. Then hierarchy/refinement/hysteresis/coverage/invalidation, authoritative unexplored delivery contract coordinated with generator/network prerequisites, independent persisted requested/effective full/vertical/LOD/simulation budgets and real current-official FSR/DLSS providers/inputs/lifetimes/licensing/hardware; frame generation later/separate.

Unrun gates remain explicit: /dev/dri GPU, Windows/AMD8060S/NVIDIA execution, real backend pixels, client resource reload/resize/world switch/save/reopen/shutdown, lighting/cutouts/transparent-water temporal order, live handoff/cache races, long multiplayer/travel/edit/unload soak and full game-frame measurements. Compilation, synthetic timestamps, PE headers and headless sessions cannot prove them.


<!-- END PRESERVED SOURCE -->
