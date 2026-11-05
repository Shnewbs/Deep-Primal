# Expanded replica — docs/VALIDATION-0.0.1-alpha.7.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.7.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.7.md |
| Preserved source SHA-256 | `ea1f1c16dad56fe99f7077df5430a41669de44dc947a8f4472d50619f475a466` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Validation — 0.0.1-alpha.7”. Its original opening states: “Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This alpha advances the built-in OpenGL horizon and its resource controls. It is not the completed Vulkan/near-block renderer project and has no hardware-independent FPS guarantee.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Carry the renderer result from bounded CPU/model behavior to actual GPU lifetime, submission, presentation, and target-device evidence.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Use separate ledgers for queued, staged, resident, retired, and reusable bytes, and identify the completion signal permitting reuse.
- Name pre-draw preparation, command encoding, API submission, GPU execution, and presentation intervals separately.

## Research expansion — acceptance checks

- Inject failure before allocation, after reservation, during upload, and before publish; verify accounting and in-flight resource validity.
- Run matched native scenes with travel, stationary recovery, minimize/resize, world switch, and device recreation before claiming frame-time or VRAM gains.

## Research expansion — source anchors

- Validation — 0.0.1-alpha.7
- Implemented scope
- Reproduction

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.7/runtime-summary.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.7/runtime-summary.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Validation — 0.0.1-alpha.7

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This alpha advances the built-in OpenGL horizon and its resource controls. It is **not** the completed Vulkan/near-block renderer project and has no hardware-independent FPS guarantee.

## Implemented scope

- Three server sample resolutions (16/32/64 blocks), projected-size selection and prebuilt coarser draw levels. Pressure can lower draw detail without a server round trip. Shared boundary anchors, walls to the saved world's floor, bottom faces and a snapped inner closure replace exposed one-sided tile edges. Transition sampling matches triangle planes rather than bilinear saddle interpolation.
- Client frame/heap feedback with gradual recovery, target-FPS and memory controls in-game. The target respects a lower native FPS cap. Simulation distance is unchanged. Memory reduction evicts farthest retained tiles and requests stop when additional data cannot fit.
- One low-priority mesh worker operating on primitive snapshots, one queue slot, one ready result, and render-thread uploads. Completion is polled without waiting. World/disconnect/horizon-disable clears cancel pending work and release buffers.
- Shared server cache bounded by payload bytes and entry count, duplicate in-flight request coalescing, sample-level round-robin scheduling, bounded recipients, stale-player pruning, expiry and adaptive tick/heap sampling budgets. Native density state stays on the server thread.
- CPU mesh/packing/upload/submission data, cached vertex bytes, selected detail counts, server cache/network/work data and a sampled asynchronous OpenGL horizon GPU timer. Tick telemetry runs after Deep Primal's sampling handler so that handler is included.
- Existing native distance, vertical/frustum, section relocation, nearest-first chunk sending, XP suppression, default worldgen and renderer handoff remain.

This does not implement a Vulkan device/presentation/shader stack, a near-block renderer replacement, saved edited-world/structure/tree LODs, GPU-driven occlusion, indirect multi-draw batching, total VRAM tracking, a universal controller over vanilla generation/simulation, or broad hardware certification. Generated height-field LODs still approximate the voxel world and can differ at cave, structure, edited terrain and material boundaries.

## Automated and packaged checks

`build` and `compileVisualValidationJava` pass. Checks include the existing 200,000 terrain-height differential cases, 3,000 nearest-selection cases, 34,300 horizontal grid coordinates, vertical/queue limits, renderer ownership policy and bounded caches. Added checks cover 1,540 interpolation/boundary cases, closed mesh vertex counts/bounds for every resolution, a saddle-plane transition regression, byte-weighted eviction/replacement/oversize/clear stress and workload backoff/recovery/heap-pressure behavior.

The distributed JAR excludes development validation classes. All 16 `data/` and `worldgen/` resources are byte-identical to alpha 6; saved voxel terrain and dimension heights do not migrate. The horizon payload is optional protocol version 2, so both peers should update for the new horizon.

## Live correctness runs

Disposable packaged server/client, seed `8000542833694305166`, standard height -128..639, full-detail 6, simulation 2, horizon 256, initially full vertical height. Client heap 3 GiB, server heap 2 GiB. Mesa llvmpipe on a virtual display, client FPS capped at 20. These runs establish rendering and resource-bound behavior, **not real-hardware performance gains**.

The complete footprint and low-view captures are inspected. All three detail classes render, without expanding the native near grid. The closure removes exposed floating edges in those inspected scenes; it is not evidence that every terrain/angle/edited-world seam is solved. Native sections and generated horizon remain visibly distinct representations.

Checks exercise disabling/re-enabling the horizon and shared server-cache reuse, target-FPS/memory input validation and cancel, shrinking cached vertex payload to one MiB, restoring the budget, lower camera views, vertical clipping, large teleports that drop the old footprint, and clean shutdown with optional work in progress. A separate run with adaptive work enabled verifies the horizon repopulates at a distant location after a teleport. One-MiB checks concern cached vertex payload; they do not assert whole-process or total driver memory of one MiB.

A real Sodium 0.9.2 NeoForge installation is used for handoff: native renderer pack and overlapping mixins are inactive, horizon is zero, and diagnostics continue. Other recognized replacements retain policy coverage rather than full live combination coverage. No third-party performance mod is bundled.

Dashboard checks cover desktop/mobile layouts, all six live charts, pause/resume/history, populated dimension rows, JSON export and no browser page errors. HTTP checks retain read-only, loopback Host/Origin/fetch-site rejection and incremental polling behavior. Whole-frame GPU time/VRAM are not measured; GPU timestamps cover only our sampled horizon pass, and driver timing quality varies.

## Reproduction

Use the packaged runs and development controller described in [the alpha 6 validation](VALIDATION-0.0.1-alpha.6.md). Additional development actions are `performance`, `memory=1`, `memory=32`, and `adaptive=true`. Wait for fresh log markers and current telemetry; do not reuse old `.done` files. To finish the full horizon quickly in a correctness run, disable adaptive work while filling, then enable it for feedback/movement tests. Default gameplay keeps adaptive work enabled.

Remaining release gates: representative AMD/Intel/NVIDIA devices, integrated graphics, prolonged travel and resource reloads, genuine multi-client fairness/load, both height profiles under the new renderer, and agreed frame/tick/memory/terrain-arrival budgets. The code provides bounds and diagnostics; this alpha is not certification of those budgets.

## Retained artifact and measurements

See [runtime summary](validation/0.0.1-alpha.7/runtime-summary.json) for the complete footprint, detail counts, memory-limit observations, server totals and teleport recovery.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.7.jar`, 158166 bytes. SHA-256: `7bf919dc244ec746cf50f6f4ea9f202373953257584af4f0d3090300390e384a`. Compiled classes match the packaged artifact; validation classes are excluded.


<!-- END PRESERVED SOURCE -->
