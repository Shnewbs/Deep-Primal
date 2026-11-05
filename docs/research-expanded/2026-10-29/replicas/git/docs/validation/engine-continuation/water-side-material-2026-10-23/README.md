# Expanded replica — docs/validation/engine-continuation/water-side-material-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-side-material-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-side-material-2026-10-23/README.md |
| Preserved source SHA-256 | `dd90857d1c8a0707b7b1805ba663eb0d3d38212a5bfd935a4b0a23243695e893` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extending admitted still/flow transitions to sides and undersides while preventing exact-V1 sampling of a neighboring atlas tile. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that backend readback row conventions required fixture correction, and arbitrary alpha or linear-filter resource packs remain outside the demonstrated cases. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a face-material sampling contract specifying atlas edge handling, transition age and a normalized image-coordinate convention for comparisons. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat all face routes and moving-height crops on each backend, record any readback flip explicitly and verify raw production vertex bytes remain unchanged by fixture corrections. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Side/underside water material continuity — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Side/underside water material continuity — 2026-10-23

Source parent d8aa5a47892ece5272549058dc61b3b6e9b814f7; guarded parent 7516906be08ac793f1ca090731bdd442b7ec607a. Unreleased continuation on work/engine-after-alpha24. Main and supplied installers remain intact. Frozen geography, player edits and water quantity are unchanged; engine acceptance remains incomplete.

## Reproduction and implementation

Unchanged production switches still/flowing side sprites immediately. The actual production-upload/shader baseline explicitly fails despite exit zero and normal save: 25,740 bad vertices, 3,850,240 channels beyond 1/255, maximum 192. It also samples a neighboring magenta diagnostic atlas tile on the still underside at exact V=1. The exact 320-route fixture, source identities, raw GPU bytes, lossless PNGs and sealed console are retained.

Three production files make every face follow the same admitted 100 ms material transition. Two fixed endpoints, one latest pending request, stale guards and sparse stable-still history remain. Side U is recovered from raw full/half-tile coordinates before applying the admitted target, preventing queued/stale requests from changing the target UV. Direction-only changes between flowing states do not add a side crossfade because their side UVs are identical.

Both sampled materials track the same displayed height. Existing current/previous bounds varyings use negative width to mark nonperiodic side tiles. UVs and gradients stay unwrapped through interpolation, then each endpoint V clamps half a level-zero texel inside its tile at fragment sampling. This fixes the demonstrated underside boundary and avoids a second translucent surface. No new varying, vertex or uniform allocation is added; CPU 14-float/GPU 36-byte strides and the 32,768-entry history bound remain. Core, solver, mesher geometry, LOD/backend and geography source are unchanged. Additional bounded history probes and shader work are unprofiled.

Self-audit rejected an initially passing fixed-height candidate: a vertex-space atlas clamp distorted interpolation during simultaneous height/material changes. Sixteen added rising 4/16-to-16/16 routes found 1,536 channels beyond tolerance, maximum 9. Moving only sampling clamps into the fragment shader corrects this; final native gates were repeated. This is a focused rendering fix, not full live-stream acceptance.

## Fresh validation

The serial uncached full build passes 25 tasks, explicit core/geography/hydrology records and retained 272 ordered direction pairs. All six outer/nested archive CRCs, 409 compiled-class identities and five nested/standalone module identities pass. Shader bytes match source. Private build agents, validation fixtures and the unaccepted drain experiment remain excluded from the mod payload.

Two strict software Vulkan sessions (development and packaged origins), then scoped OpenGL software fallback pass on the final production and diagnostic source. Each tests 320 fixed-height routes (16 directions, both material orders, near cell 3/far cell 15, all four sides and underside) plus 16 simultaneous rising-height/material routes at near/far origins. Across three sessions: 12,000 actual production-upload shader draws, 10,080 weighted RGBA comparisons and 6,027 actual GPU upload readbacks (5,207,328 bytes), maximum error 1/255, zero violations. Independent Python recalculates pixels, expected atlas UVs, light/source/age/cell packing, original opacity/height clocks, cleanup deadlines, full geometry masks away from exact raster ties and sparse-history behavior. The diagnostic draw selects an actual uploaded face through its real index buffer; it is not a complete stream scene.

Retained top-material, direction, phase/mip, side-height, real capture/FLOW, GPU reuse, partial fade, LOD index/retry/edit, frame/device/reload/save/shutdown gates pass. No new final crash/save failures; Vulkan validation errors and tracked GPU resources are zero. Environmental sound-device errors remain recorded. These are software drivers, not owner AMD GPU/FPS or performance guarantees.

27 fresh lake frames across nine wave/LOD profiles were visually inspected and stored as lossless RGB videos with exact decoded-byte checks. Distant rectangular LOD/horizon cutoffs remain unresolved. Wave geometry is zero bytes even when enabled; ocean waves and sustained actual streams remain untested. Playback at five frames per second is illustrative, not measured FPS.

## Rejected attempts and diagnostic corrections

The first expanded height fixture selected nonexistent face 6 for extra route IDs, producing transparent images and maximum error 255. Mapping to physical faces 1–4 exposes the real vertex-clamp defect above. A generated Python indentation error was caught before audit. The independent geometry mask initially excluded pixel centers exactly on the analytic top edge; all 256 differences were exact 8×8 raster ties. The final audit requires exact interior/exterior masks and binary alpha, permitting either tie inclusion only at the exact boundary. The lower-quarter RGBA comparison and 1/255 tolerance are unchanged.

The first OpenGL run passes all 320 fixed-height routes but fails the new height crop because raw OpenGL row zero is lower physical Y while Vulkan row zero is upper Y. The final fixture records the backend/convention and explicitly flips OpenGL diagnostic readback rows before both PNG output and weighted comparisons. Production, transforms and raw vertex bytes are unchanged for this correction. Both Vulkan sessions and OpenGL were repeated on that exact final fixture. Earlier passing Vulkan runs are superseded and excluded from final counts. No failed report was rewritten into a pass; raw logs and failed sources remain sealed and reviewable.

## Replay and continuing work

Run `python verify_archived_evidence.py` for CPU-only replay in a temporary copy. It reconstructs deduplicated PNGs from per-archive manifests with SHA-256 checks, restores sealed logs and 27 lake frames from lossless videos with exact RGB hashes, and reruns both strict auditors. PNG compression may differ for restored lake frames; RGB pixels must match. `python rehydrate_evidence.py --all` also restores rejected/superseded controls for inspection. The compact archive deduplicates identical PNG bytes without discarding named test outputs. Large native JSON records and raw GPU readbacks are gzip-compressed with exact original/compressed hashes in compressed-data-manifest.json; the same helper restores their original bytes before auditing.

Native execution requires the recorded pinned toolchain and disposable Vulkan-primary-20261022 world through DEEPPRIMAL_NATIVE_RUN_DIR. Use the repository's final fixture and the recorded action/command inputs; do not count earlier fixture variants as final acceptance. Current measurements are fresh; pre-reset evidence stays historical. Only source and compact evidence are checkpointed, with no installer release.

Next address spatial shared edges between different flow directions, then sustained actual streams/ocean, cap churn and matched CPU/GPU profiling. Tint/composition, other liquids, arbitrary alpha/linear-filter resource packs, broader simultaneous height/opacity/edit combinations and complete enabled-view coverage remain open. Preserve the registered regional water budgets/headwater outlets/chemistry, pressure fanout/p95/1024 waiters/beyond8192 continuation, coarser LOD, physical unload/input, marker/reload/atlas/save investigations and coherent geology. Catalog/species stays deferred and the drain experiment stays separate. Solver substeps are not independent 60 Hz simulation. This is a continuation checkpoint, not engine completion or a market-superiority claim.


<!-- END PRESERVED SOURCE -->
