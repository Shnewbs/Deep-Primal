# Expanded replica — docs/renderer-jams/20261027-transition-intervals/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-transition-intervals/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-transition-intervals/REVIEW.md |
| Preserved source SHA-256 | `3a5522cfa400df4fa3fe617f2c5b447196e67fda98c9a1d0f27fd887d3b093b2` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends the staged frame-window candidate with a separate bounded transition history, retaining initialized nonnegative crossing gaps while isolating steady-context percentiles. It preserves zero gaps, clock wrap, reset, and explicit invalid-clock behavior. The 1,024 oracle-window checks and complete CoreChecks are scoped evidence, not a packaged GPU result.

## Research expansion — gaps and details to deepen

- The aggregate transition window does not attribute causes or isolate load/GPU time. This staged source also needs a clear link to later promotion, preventing its successful focused adapter/core checks from being mistaken for a new native artifact.

## Research expansion — proposed contracts

- Add transition cause and before/after context identities, scene/artifact fingerprint, frame sequence, and sample availability. Keep all observed gaps reviewable separately from steady statistics and label included limiter/wait time.

## Research expansion — acceptance checks

- Reproduce all eight context crossings and bounded-window edge cases, including first observation and backwards clocks. On the promoted artifact, correlate transition events with actual presentation and GPU timing; preserve the failed capacity attempt separately from the final successful focused rerun.

## Research expansion — source anchors

- Renderer jam 20261027-transition-intervals — self-audit
- Implementation and consumed finding

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-transition-intervals — self-audit

Exact reviewed source **302b93f12976c9a37cb680a677218daa9e0ed4e4**; implementation checkpoint **2ce27032e3af79f6b6b4ca8ee78208c71bcf1e7a**; initial main **878cfdb126d30c76a6b9844d0848d2c7101e6be8**. Production remains **c5abe457c4c7b36a11307128486c240e0b40d508**, project version0.0.1-alpha.29-dev.31. No new enabled runtime/version/JAR.

This is the renderer author's read-only self-audit, not an independent external audit. Five staged Java files were read back exactly at the reviewed pin; no implementation changed during scored review. The later evidence/claim receipt changes only dedicated documentation. All six scores are explicitly carried unchanged from the previous renderer jam.

## Implementation and consumed finding

Previous frame-window audit b980276 identifies that context invalidation discards crossing intervals. The new helper retains those valid observed gaps in a separate RollingTiming window capped at min(capacity,64) and keeps a count since clear. Steady-context percentiles remain isolated; only initialized, nonnegative crossing gaps enter the transition window. Initial observations and backwards clocks invent no transition. Clock wrap and zero gaps work. clear releases both histories/count and the retained world reference. Generation remains the original per-instance identifier.

ClientTelemetry adds transition sample/count and p50/p95/p99/max milliseconds, with an explicit scope: observed crossing frame gaps including waits, not isolated world-load or GPU time. These aggregate multiple kinds of context changes; they do not identify a particular cause or measure the exact load operation. This repairs the previous diagnostic blind spot without mixing steady and crossing samples.

Actual staged files:
- candidate/FrameIntervalWindow.java -> new core FrameIntervalWindow; transition recording at L32.
- candidate/ClientTelemetry.java -> client adapter; transition export at L42.
- checks/TransitionIntervalChecks.java -> new normal-suite fixture, independent deque/sorted oracle.
- checks/FrameIntervalChecks.java -> previous reset fixture plus normal-suite transition-test invocation.
- candidate/CoreChecks.java -> prior staged one-line registration snapshot. Reapply the insertion to fresh main, never overwrite a newer CoreChecks wholesale.
- run_checks.py -> isolated reproducible Java checks, binary-only actual game/module adapter classpath and private JVM temporary files.

All are under this docs/renderer-jams directory only. No normal production source/build/resource is changed; not bundled or enabled. Terrain geography, water quantities/simulation/surfaces/order, shaders, mesh buffers, renderer modules, art/notices, distance caps/tracking and vendor SDKs are unchanged. No claim of new FPS, reduced allocations, GPU bytes or usable distance.

## Fresh validation

Commands with exact arguments/environment are retained in run_checks.py and evidence/*-compile.json, frame-checks.json, full-core.json and negative-control.json. Java25.0.4.1+1, private /dev/shm/dp-renderer-transition-c748d588. All75 base core Java files individually match the current core Git subtree cdb78e5e084a98f0eb1d92899ddb0c871876d3c1. Fresh compile adds one staged production helper and66 fixtures: **142 sources**.

- Transition fixture: **1,024 oracle windows**, capacities1/17/64/512; 256 transitions each, bounded last64 where applicable, lifetime count, p50/p95/p99/max, no steady-window pollution, clear, invalid negative gaps, long clock wrap and zero gaps.
- An additional read-only CrossingReasonProbe confirms exact crossing-gap retention and zero steady samples for each of the8 context coordinates; evidence/crossing-reasons.json and reason-probe source retained.
- Prior frame fixture freshly repeats770 oracle windows/10,000 intervals and8 independent context resets.
- Negative control removes crossing recording from a separate private helper copy. It compiles then fails with AssertionError: Transition oracle. Deliberate expected rejection; no candidate failure.
- Clean actual client-adapter javac compilation succeeds against real patched Minecraft/NeoForge binary dependencies and prior qualified project/module classes, with fresh staged core first. This is not full module/package linkage or client execution.
- Complete -Xverify:all CoreChecks: **passed, exit0, 269.808 seconds**. Fresh LOD byte/scratch/frozen geometry/bounds/max-height/cancellation/parallel checks and existing core/cache/lifecycle/water/conservation/version-replay checks pass. Ordinary diagnostic timings in that suite are not controlled renderer measurements.

Initial full-core attempt failed at LodStoreBudgetChecks because /tmp had no space (FileSystemException). Its failed log/result remain under evidence/initial-temp-failure. Runner now explicitly sets java.io.tmpdir and TMPDIR to private memory and disables JVM perf-data scratch; clean compilation and complete checks were repeated without any implementation change. Only final completed checks count.

## Build, hardware and artifact blocker

Preflight at2026-10-27T01:02:53Z: workspace0available bytes, private memory469,549,056bytes, below the conservative2GiB full-isolated-qualification start gate. No repeated broad cleanup or edits to other jobs. Full Java25/Gradle9.2.1 :build :visualValidationClasses --rerun-tasks was **not started**, not called passed. No new JAR, download, release/upload or Library save exists for this candidate. No previous binary relabeled.

Unrun: complete Gradle/module/resource build; final producer and outer/nested archive CRC/class/source/package/native payload/fixture/art/notices identities; packaged native save/reopen/shutdown; Vulkan/OpenGL client pixels, resize/reload/world switch/client save/reopen/shutdown; Windows, AMD8060S/NVIDIA hardware; actual camera/travel/edit CPU/GPU frame/pipeline/draw/descriptor/RAM/VRAM baselines. /dev/dri absent. Linux direct checks prove no GPU acceptance. Simulated timestamp oracle inputs are not measured real frames.

## Six equally weighted scores — all carried unchanged

| Renderer category | /10 | Pinned evidence and limits |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Production discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259), fresh LOD checks in evidence/full-core.txt. Live readiness/handoff/seams/coverage/unseen delivery remain open. |
| Mesh generation/reuse/scheduling | 6 | [Production byte traversal L16](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16), fresh LOD_BYTE_ACCOUNTING_PASS/LOD_SCRATCH_PASS. Prior measured query allocation gain carried, not rerun. No new scheduling/mesh implementation. |
| GPU culling/submission/memory | 4 | [Production culling L477](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477). Ordinary draw submission remains; no new GPU/arena/MDI/VRAM evidence. |
| Actual FSR/DLSS | 0 | [Fallback policy L5](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5). Real providers/inputs/dispatch absent; linear scaling/frame generation earn no upscaling implementation credit. |
| Visual/backend/settings compatibility | 4 | [Staged adapter L42](https://github.com/Shnewbs/Deep-Primal/blob/302b93f12976c9a37cb680a677218daa9e0ed4e4/docs/renderer-jams/20261027-transition-intervals/candidate/ClientTelemetry.java#L42), evidence/adapter-compile.json and reset fixture. No actual backend/pixels/settings lifecycle acceptance. |
| Validation/measured performance | 4 | [New fixture L8](https://github.com/Shnewbs/Deep-Primal/blob/302b93f12976c9a37cb680a677218daa9e0ed4e4/docs/renderer-jams/20261027-transition-intervals/checks/TransitionIntervalChecks.java#L8), fresh oracle/negative-control/full-core and capacity preflight. Full product/GPU/hardware/frame-performance gates absent. |

**22/6=3.67/10; previous3.67; delta0.00.** No external whole-engine grade change; carried5.0 is not a new complete audit. Staged telemetry earns no shipping feature credit.

## Coordination and next repair

Read actual main, engine/water states, missing root AGENTS.md, architecture/graphics/LOD roadmap, renderer frame-window review/backlog and latest independent0004-685a739 audit and released lease. Roadmap/architecture and root production/module blobs remain unchanged. Independent35-task/10session qualification is carried context, not new checks of either renderer telemetry candidate. Its staged-exclusion finding remains respected.

General builder owns its active exact diffusion continuation through01:18:57Z; renderer claims only this unique docs subtree, no production files or exclusive timing. No overlap with engine/water files/state, no agents/messages to others/process kills/pauses/shared mutable build outputs/automation changes. Fresh expected-parent nonforce updates preserve concurrent main. Own claim released in receipt; temporary capacity block does not complete the renderer workstream.

Next: restore enough private qualification capacity; apply/rebase the combined frame-window candidate on fresh current main under a new production claim, run full build/package/native/client gates and then deliver a uniquely versioned testing JAR. No wholesale copying stale CoreChecks. Capture live GPU discovery/readiness/native handoff/cache/edit/travel/water coverage and matched camera/scene/seed/resolution/detail/cold-warm baselines. Transition aggregate is not a complete scene fingerprint or cause trace. Then bounded mesh/upload reuse/hierarchy/refinement/coverage, authoritative unexplored delivery contract coordinated with generator/network owners, independent persisted requested/effective full/vertical/LOD/simulation budgets and real official FSR/DLSS providers/licensing/inputs/lifetimes/hardware. Frame generation later/separate.


<!-- END PRESERVED SOURCE -->
