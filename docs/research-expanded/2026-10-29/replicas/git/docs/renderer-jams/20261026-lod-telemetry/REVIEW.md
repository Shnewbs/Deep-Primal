# Expanded replica — docs/renderer-jams/20261026-lod-telemetry/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-lod-telemetry/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261026-lod-telemetry/REVIEW.md |
| Preserved source SHA-256 | `5ad101fb7598b8325e7807f0a0bc324648654bbf8fa949e77ae98f1d71930f8c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds opt-in 240-observation CPU cull, upload, and submit timing windows, lazy diagnostic snapshots, and world reset. The record distinguishes submitted parts from GPU draw counts and prospective GPU payload bytes from actual CPU or device memory. These probes establish observability, not a measured frame-time improvement.

## Research expansion — gaps and details to deepen

- Independent last-operation gauges and percentile windows need correlated identities and population definitions. Without those, a dashboard may combine unrelated frames or interpret absent work and unavailable timings as zero-cost rendering.

## Research expansion — proposed contracts

- Add frame/world/device generation, observation type, sample count, units, skipped/failed/deferred outcomes, and snapshot timestamp. Preserve bounded storage and lazy sorting; label CPU API time, GPU completion, payload estimates, and measured memory separately.

## Research expansion — acceptance checks

- Verify empty windows, world resets, partial failures, and mixed cull/upload/submit populations. Measure telemetry-on/off overhead in the target client and correlate asynchronous GPU timestamps without blocking the render thread.

## Research expansion — source anchors

- Renderer jam20261026-lod-telemetry — self-audit
- Implemented increment

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam20261026-lod-telemetry — self-audit

Base2617d261284e0ae4f3a07f627e301ec92122f560; ownership claim961787258382f82455cb4ce562a9d79e9d0138fa; exact frozen implementation9c27df7af31c57c7c59aef8254be29590001d315. Version0.0.1-alpha.29-dev.29-renderer.2 is supplied by the committed jam build init script. The final receipt commit is docs-only relative to this runtime snapshot. No product edits during scored review. This is the renderer author's self-audit, not an external independent regrade.

## Implemented increment

Three bounded240-sample windows record CPU elapsed nanoseconds for LOD visibility selection, pending upload attempts and successful nonempty draw submission. Record operations reuse primitive arrays; sort/copy occurs only on explicit diagnostic reads. Synchronized snapshots expose nearest-rank p50/p95/p99/max and sample count to the existing LodAccess→ClientTelemetry diagnostic feed. An empty window is samples0, not a measured zero-cost stage. World/session reset clears all windows and gauges.

New gauges report tested/rejected/submitted parts, last uploaded GPU bytes, and pending mesh's prospective GPU budget. The latter is NOT actual Java-array RAM, VRAM residency, driver staging memory or total pending-worker memory. Counts are separate latest-operation gauges, not an atomic whole-frame trace. Submitted parts are not GPU draw calls/indirect commands. Upload samples include deferred/paused/failed attempts; submit samples cover successful nonempty LOD pass CPU submission, not GPU execution. Culling excludes result extraction, eviction and uploads. None is a whole-frame timing.

Telemetry defaults OFF. Add JVM argument `-Ddeepprimal.lod.telemetry=true` and restart for collection; inspect `lodTelemetryEnabled`, `lodCullTiming`, `lodUploadTiming`, `lodSubmitTiming` and workload keys in the existing dashboard client diagnostics. No config GUI redesign or public network endpoint was added. No faces, transparent-water order/vertices, resource IDs, saved profiles, geography, simulation, distance caps or module boundaries changed.

## Scores (six equally weighted categories)

| Category | /10 | Review scope | Pinned evidence and limitations |
|---|---:|---|---|
| Distance/LOD correctness and usable coverage | 4 | carried | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L466): Same explored-only native handoff and projected-error selection; bounds regression retained. No increased usable-distance or unseen delivery acceptance. |
| Mesh generation/reuse/scheduling | 5 | carried | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L221): Jam1 exact bounds and bounded greedy material/fluid meshes retained. No new reuse/scheduling speed claim; meshoptimizer development-only. |
| GPU culling/submission/memory | 4 | scoped inspection | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L457): CPU cull/upload/submit telemetry and counts now implemented, gated by restart property. Not GPU timestamps, hardware acceptance, hierarchy or real multi-draw indirect. |
| Actual FSR/DLSS implementation | 0 | fresh absence verification | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L5): No vendor provider/dispatch; requests still native fallback. Spatial presentation and frame generation receive no FSR/DLSS credit. |
| Visual/backend/settings compatibility | 4 | carried, source-reviewed | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L738): No topology/shader/water/settings change. Reset clears timing windows and gauges; common GPU interface retained. Current client/backend/reload/resize/world-switch acceptance unrun. |
| Validation and measured performance | 4 | scoped fresh tests | [Source](https://github.com/Shnewbs/Deep-Primal/blob/9c27df7af31c57c7c59aef8254be29590001d315/src/coreChecks/java/com/shnewbs/deepprimal/core/RollingTimingChecks.java#L6): 1580 independent nearest-rank oracle windows,30000 samples,100000 concurrent writes; capacity/reset/negative/overflow checks and packaged actual-class test pass. No real camera/travel/edit/frame/memory performance baseline. |

**Average21/6 =3.50/10; prior renderer jam3.50, delta0.00.** Instrumentation makes measurements possible but earns no fabricated FPS or GPU performance credit. The whole-engine external grade is unchanged.

## Consumed evidence and coordination

Read jam1 REVIEW/BACKLOG, current engine/water state, LOD/drainage architecture, latest audit and all intervening main commits. No new AGENTS.md appeared in the compared paths; prior inspected root/module trees had none. Latest independent audit docs/engine-hourly-audits/2026-10-26T1135-93ac6cb/REVIEW.md qualifies the water owner's precipitation repair, including ten native sessions. The inherited jam1 delivery blocker is closed; this is carried evidence, not a fresh ten-session rerun in this jam. Preserve staged stream-deadline work without promoting it.

Initial engine/water claims were checkpointed and the latest functional reservation completed. Claim is renderer-only, separate source/cache/output, immutable retained toolchain reused read-only. No subagents, other schedules, shared mutable outputs, force pushes or unrelated state edits. Last jam's completed own dependency cache/module outputs were removed to recover space; its source, receipts and retained JAR were preserved. Other writers' files/processes/caches were untouched.

Intervening six production/test inputs were retrieved from pinned2617d26; all other retained inputs were compared against prior Git blob identities with jam1's two committed bounds changes applied. New renderer files read back byte-identically at9c27df7. Exact commands, logs, source/package identities, omitted gates and delivery result are retained alongside this report.

## Baselines, omissions and next repair

Current host is Linux AMD EPYC9V74 virtual CPU; no /dev/dri device. Tests use synthetic timing samples and actual compiled/package classes. No actual scene/seed/camera/resolution/API/warm/cold run, before/after frame p50/p95/p99, GPU query, driver allocation/upload tails, descriptors/draw count baseline, VRAM/RAM or improvement ratio was measured. Synthetic timing inputs are not game timings. Vendor runtime/SDK integration was not attempted; FSR/DLSS/frame generation remain unimplemented.

Next capture named-hardware fixed-camera/travel/edit datasets with this collector, then fill worker rebuild/allocation/full-frame/GPU/descriptor and memory gaps. Actual OpenGL/Vulkan client pixels, optional telemetry runtime hooks, culling equivalence, resource reload, resize, world switch, client save/reopen/shutdown and longer-travel soak remain explicit unrun gates. Dedicated-server save/reopen validates server lifecycle only. Compilation/CPU metrics do not certify them. Development testing delivery, if successful below, is not stable or hardware performance acceptance.

## Final qualification and delivery

Full Java25.0.4.1/Gradle9.2.1 build exits0 in5m20s:36/36 tasks executed, including complete core/cache/resource/module gates, visualValidationClasses and the new rendererTelemetryChecks task. Seven archives pass recursive CRC/duplicate checks;590 product class identities,2 directly compiled timing classes and218 fixture exclusions pass. Windows-only Vulkan DLL payload/OpenGL fallback, Themes2508 resources,43 palettes,11 approved ground images,148 generated soil resources and notices pass. Source identity/readback and package-extracted RollingTiming tests pass. No source edit after the scored snapshot.

Two fresh packaged Linux native default-profile sessions pass fresh/reopen, actual-class identity, terrain/deposit/soil/source checks, baseline replay hashes, explicit save flush and ordered clean shutdown. This is not client/GPU acceptance, not a fresh ten-session precipitation requalification. Producer/export hashes remain equal after native completion and after Library saving. Optional telemetry hooks on GPU hardware remain unrun.

[Download development testing JAR](https://chatgpt.com/library?file_id=libfile_cf30e66e397c819182dd72f68bc7e854&account_id=personal). Version0.0.1-alpha.29-dev.29-renderer.2; runtime source9c27df7af31c57c7c59aef8254be29590001d315; size125294484bytes; SHA256`f59be2b93f2fa5933eaa5a6c3e0e6e87fad608242199f23f86fe9308b00fd637`. Library save succeeded with local metadata applied. No GitHub Release publisher is exposed; no release/CurseForge/stable promotion attempted. This is a new producer, not a relabeled older binary.

At final disk pressure, only completed own prior renderer cache/build outputs and this jam's reproducible theme compile output were removed after their package checks; source, all receipts, approved art and both current JAR copies were retained. No other job was interrupted or cleaned. No uninterrupted execution between runs is implied. Ownership released in this receipt commit; feature schedule remains enabled.


<!-- END PRESERVED SOURCE -->
