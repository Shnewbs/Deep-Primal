# Expanded replica — docs/renderer-jams/20261027-frame-window/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-frame-window/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-frame-window/REVIEW.md |
| Preserved source SHA-256 | `7640608e83db897a22f6fc70158ac8ef0f2ae7391d4d20f05e79916b4dda5811` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Stages a bounded 512-interval frame window keyed by eight context coordinates so mixed worlds, dimensions, focus, and settings do not pollute steady percentiles. Crossing intervals are initially discarded; a later transition-interval candidate restores separate visibility into those gaps. This staged review does not establish the later capacity-recovery promotion.

## Research expansion — gaps and details to deepen

- The source’s scope is steady-context statistics, not complete latency accounting. The expansion should link the transition successor and include backend/provider, source artifact, scene fingerprint, and first-sample semantics without retroactively claiming this candidate captured all transitions.

## Research expansion — proposed contracts

- Document the sampling population, reset coordinates, invalid-clock handling, and count/extent metadata. Add a separate transition event record and preserve raw context changes so benchmark selection cannot hide a hitch by resetting the steady window.

## Research expansion — acceptance checks

- Replay every reset coordinate and wrapped/negative/zero intervals, verify no cross-context steady sample, and compare the exact integrated artifact’s full frame stream with dashboard windows. Native GPU and presentation remain distinct measurements.

## Research expansion — source anchors

- Renderer jam 20261027-frame-window — staged implementation self-audit
- Concrete implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-frame-window — staged implementation self-audit

Exact candidate source: **b69d022383571977b90926770893baf271c91fd1**. Initial inspected main: **e0bcf7c6f85ab84b5d4dec359a5b6d814bc2a88e**; claim parent **61799e93f6ffd130f508ddef7f8edf8d8b3a591e**; candidate integration parent **f365b3a6deaa74437fb2b89bd545d61cc7078ba3**. Production runtime remains **c5abe457c4c7b36a11307128486c240e0b40d508**, normal project version **0.0.1-alpha.29-dev.31**. No new runtime/version/JAR or release.

This is the renderer jam author's read-only self-audit, not an independent audit or whole-engine regrade. Four candidate Java files were frozen at the exact commit above, read back byte-for-byte and reviewed after implementation. No candidate code changed during this review. The later evidence/claim receipt does not modify those files.

## Concrete implementation

Existing ClientTelemetry accumulates frame intervals across a world-object switch, framebuffer resize, focus/FPS-limit change and distance changes. It exports p50/p95/max but omits its available p99 and sample count. A diagnostic sample can therefore mix unlike contexts. No live GPU reproduction is claimed.

The staged FrameIntervalWindow bounds samples to 512 in the adapter, reuses existing RollingTiming nearest-rank nanosecond statistics, and resets on eight independent context coordinates: world object identity, framebuffer width/height, focus, effective FPS limit, effective native distance, vertical distance and active LOD distance. It discards the interval crossing a reset. The next stable frame starts the new sample window. Timestamp zero, nanoTime wrap, backwards clock rejection and observation-gap clearing are checked. There is no per-frame context record/list/string construction in the helper; no allocation/performance improvement was measured.

The staged ClientTelemetry adapter preserves existing percentile field names and milliseconds, adds frameP99Ms, frameSampleCount, frameWindowGeneration, frameWidth/frameHeight and an explicit CPU interval scope. A context reset publishes immediately, even inside the ordinary one-second diagnostic cadence. An empty window reports zero values with count0. The generation is local to this telemetry instance. An unavailable dashboard clears the previous observation/world reference.

Intended production files, currently **only in this dedicated candidate directory**:
- candidate/FrameIntervalWindow.java -> engine-core/src/main/java/com/shnewbs/deepprimal/core/FrameIntervalWindow.java.
- candidate/ClientTelemetry.java -> src/main/java/com/shnewbs/deepprimal/client/ClientTelemetry.java.
- checks/FrameIntervalChecks.java -> src/coreChecks/java/com/shnewbs/deepprimal/core/FrameIntervalChecks.java.
- candidate/CoreChecks.java demonstrates the one-line normal-suite registration. Reapply that insertion to fresh main; do not overwrite newer CoreChecks wholesale.

No mesh, upload, shader, tracking, distance maximum, terrain, finite-water simulation, transparency, native dependency, art or resource IDs change. Production source/build/module trees are unchanged. This is implemented and tested staged source, not an enabled/shipping integration.

## Fresh checks and exact limits

Java25.0.4.1+1; Linux x86_64, no /dev/dri. Private run root /dev/shm/dp-renderer-frame-window-c748d588; no other job's mutable checkout/cache/output used. The complete 75 production core source blobs were individually verified against main's core Git subtree cdb78e5e084a98f0eb1d92899ddb0c871876d3c1. Fresh javac -g compiles those plus the new helper (76 production sources) and65 fixtures including the staged registration: **141 source files**.

Commands/results are retained exactly in evidence/core-compile.json, frame-checks.json, adapter-compile.json, full-core.json and negative-control.json. The executed runner is run_checks.py, with the explicit private path used for this run.

Passed:
- 10,000 generated intervals against a bounded deque/sorted nearest-rank oracle; **770 oracle windows**, all8 independent context resets, no crossing interval admitted, empty/next interval, zero time origin, signed long clock wrap, reversal and clear/gap cases.
- Negative control: delete only world-identity invalidation from a separate private helper copy. It compiles, then the exact fixture rejects it with AssertionError: Crossing interval0. This is a deliberate expected rejection, not a candidate test failure.
- Actual ClientTelemetry source compiles with javac -g -proc:none -sourcepath '' against real patched Minecraft/NeoForge binaries and prior qualified project/module classes, with the fresh staged core first. Final output was cleaned before repeat and contains exactly one adapter class. This is direct compilation, not full module linkage, game launch or packaged execution.
- **Complete -Xverify:all CoreChecks passed, exit0,271.363s**. Includes fresh registered FrameIntervalChecks, LOD byte oracle/ten frozen geometry signatures/18part bounds/25retained serial builds/64parallel jobs/height4096/cancellation, and existing core/cache/lifecycle/saved-version/finite-water regressions. Raw diagnostic timing inside broad core tests is not a controlled renderer benchmark.

An initial adapter harness accidentally included dependency sources jars on its classpath; javac attempted unrelated NeoForge sources against an older fastutil binary and failed with four errors. Those failed logs are retained under evidence/failed-initial-adapter. Excluding source/javadoc jars and disabling implicit source lookup fixes the harness without changing candidate code; the final clean adapter compilation passes. No failing output is delivered.

Not executed: full Java25/Gradle9.2.1/module/resource build, candidate outer/nested archive CRCs, compiled/packaged identities, Windows payload/notice/art/fixture-exclusion checks, native packaged save/reopen/shutdown, client reload/resize/world-switch/save/reopen/shutdown, actual OpenGL/Vulkan pixels or named AMD/NVIDIA performance. The prior renderer.6 and independent2333 qualification receipts are carried history, not new tests of this candidate.

## Capacity gate and delivery

Workspace reported **0 available bytes**. Removed only completed own reproducible Themes build/native-cache/generated outputs after confirming prior own consumers/producers had exited; directories initially disappeared but later reappeared and availability remained0. **No durable reclaimed bytes claimed.** No other writer's files/processes were removed or interrupted.

Fresh preflight at2026-10-27T00:08:11Z found0disk and629,309,440private-memory bytes available, below the conservative2GiB start gate for a complete isolated producer/module/archive/native qualification. The full target is recorded in evidence/full-build-preflight.json; it **was not started**, not reported failed or passed. Direct checks used bounded private memory and completed. No new JAR was produced, relabeled, uploaded or released. No new download link exists for this change.

## Six equally weighted renderer scores

All category values below are explicitly **carried unchanged** from the previous renderer.6 self-audit. This staged telemetry increment receives no shipping implementation, FPS, usable-distance or vendor integration credit.

| Category | /10 | Pinned evidence and missing acceptance |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 4 | [Production discovery L259](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L259); current full-core.txt preserves sampled mesh/geometry checks. Live readiness/handoff/coverage and authoritative unseen delivery remain unrun. |
| Mesh generation/reuse/scheduling | 6 | [Production byte traversal L16](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L16); fresh LOD_BYTE_ACCOUNTING_PASS/LOD_SCRATCH_PASS in full-core.txt. Prior allocation gain is carried, not remeasured. No new mesh/scheduling implementation or frame gain. |
| GPU culling/submission/memory | 4 | [Production culling L477](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L477), [ordinary indexed draws L121](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/graphics-settings/src/main/java/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java#L121). No GPU changes, MDI, fence-safe arena or GPU/VRAM measurement. |
| Actual FSR/DLSS implementation | 0 | [Production fallback policy L5](https://github.com/Shnewbs/Deep-Primal/blob/c5abe457c4c7b36a11307128486c240e0b40d508/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5). Real providers/render inputs/dispatch remain absent. Basic linear scaling and frame generation earn no upscaling credit. |
| Visual/backend/settings compatibility | 4 | [Staged adapter L27](https://github.com/Shnewbs/Deep-Primal/blob/b69d022383571977b90926770893baf271c91fd1/docs/renderer-jams/20261027-frame-window/candidate/ClientTelemetry.java#L27), fresh adapter-compile.json and context cases. No visual pass changed. Backend/client lifecycle, water ordering, real resize/world switch and settings compatibility are not accepted by headless tests. |
| Validation and measured performance | 4 | [Staged window L19](https://github.com/Shnewbs/Deep-Primal/blob/b69d022383571977b90926770893baf271c91fd1/docs/renderer-jams/20261027-frame-window/candidate/FrameIntervalWindow.java#L19), [fixture L17](https://github.com/Shnewbs/Deep-Primal/blob/b69d022383571977b90926770893baf271c91fd1/docs/renderer-jams/20261027-frame-window/checks/FrameIntervalChecks.java#L17), fresh frame-checks/full-core/negative-control and full-build-preflight. No actual camera/travel/edit CPU/GPU frame p50/p95/p99, upload/cull/allocation/draw/descriptor/RAM/VRAM baseline or new packaged product. |

**22/6=3.67/10; previous3.67; delta0.00.** External ten-category whole-engine5.0 unchanged; no independent regrade or >8.5 acceptance.

## Consumed evidence, ownership and next repair

Read current main, absent root AGENTS.md, engine/water state and scoped ownership, architecture/graphics/LOD roadmap and recent run records/commits. Consumed renderer.6 final self-audit/backlog and independent engine2333-c5abe45 audit published at e0bcf7c. Independent auditor's35task/10native-session and byte-oracle checks are historical context, not rerun by this jam. Preserve the distinction between query allocation optimization and correct totals/geometry. General builder composition-diffusion/tool-recovery and water scopes remain separate and unmodified.

A bounded docs-only claim was checked before candidate integration; no production files were claimed or overwritten. Fresh expected-parent nonforce updates preserve concurrent engine documentation. Claim's initial manually entered started_at was inaccurate; final receipt uses the actual Git claim commit timestamp **2026-10-27T00:03:12Z**. No other automation changed, no agents spawned, no jobs killed/paused and no exclusive performance reservation asserted. Ownership is released in the final receipt.

First repair: recover sufficient isolated full-build capacity, rebase these four staged files onto actual current main under a fresh bounded production claim, then force the full build and all candidate package/native/client gates before promotion and a uniquely versioned testing JAR. Do not replace fresh CoreChecks with this old snapshot wholesale.

Remaining telemetry limitations: these are CPU end-frame intervals including limiter/waits, not pure CPU work or GPU time. Context resets intentionally omit the crossing interval; world-switch latency/hitches require a separate transition measurement. Eight coordinates are not a complete scene fingerprint: seed, camera/travel/edit trace, cold/warm status, renderer/API/upscaler/material/water/LOD details, reload events and exact scene identity remain required. No observed hardware baseline is fabricated.

Then live GPU discovery/readiness/cache/edit/travel/native handoff/transparent-water coverage and complete matched camera/travel/edit baselines; bounded mesh/upload reuse and hierarchy/refinement/hysteresis; authoritative unexplored-terrain delivery contract with generator/network prerequisites; independent persisted requested/effective full/vertical/LOD/simulation budgets; actual current-official FSR/DLSS SDK/providers/inputs/lifetimes/licensing/hardware. Frame generation remains separate/later. Dedicated renderer continuation remains enabled.


<!-- END PRESERVED SOURCE -->
