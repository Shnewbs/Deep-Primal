# Still/flow surface crossfade — 2026-10-23

Source parent 80978fac66a0893bcf34b566b619fc01fb9e5f8b; guarded parent cc28af8b3ac109979f929357644dabae6f8da4c1. Unreleased continuation on work/engine-after-alpha24. Main, installers and frozen geography remain intact. Engine acceptance remains incomplete.

## Reproduction and implementation

Unchanged production switches abruptly between still and flowing top sprites. A standalone core reproduction fails all 32 switches. The actual production upload/shader baseline exits zero and saves normally, but explicitly fails: 1,055 bad vertices, 774,022 channels beyond 1/255 tolerance, maximum error 219. The exact fixture, source identities, raw vertex bytes, lossless PNG controls and sealed console are retained. Gradle success alone is insufficient.

Four production files extend the existing 100 ms blend through direction 16 (still). Two fixed endpoints and one latest pending request avoid chains of blended history. The previous accepted nonempty snapshot supplies the first endpoint; unchanged still cells create no history. Queued and stale requests preserve admitted target UVs, including reconstructed cell-local still coordinates. Each shader endpoint carries independent sprite bounds. Flow keeps the existing Q25 world phase; still keeps cell-local phase. Unwrapped gradients remain outside conditionals, and sampled RGBA blends in one fragment.

The 32,768-entry history cap, CPU 14-float/GPU 36-byte strides and original opacity/height timing remain unchanged. Two spare texture-matrix values supply still bounds. A new flat vec4 adds 16 bytes of interstage values; its cost is unprofiled. Solver, water quantity, mesher geometry, player edits, geography and LOD/backend source are unchanged. No performance improvement is claimed.

## Fresh validation

The full serial uncached build passes all 25 tasks and explicit core, geography and hydrology records, including 272 ordered direction pairs and repeated, queued, stale and early-idle cases. All six archive CRCs, 409 compiled-class identities and five nested/standalone module identities match. Validation fixtures, the private build agent and the unaccepted drain experiment remain excluded from the payload.

Two strict software Vulkan sessions (development and packaged class origins), followed by scoped OpenGL software fallback, pass. Each checks 64 still-to-flow/flow-to-still routes at near cell 3 and far cell 15, origins ±29,999,984, with 768 actual production-upload shader draws, 640 weighted RGBA comparisons and 393 actual GPU upload readbacks. Across three sessions: 2,304 draws, 1,920 comparisons and 1,179 readbacks (848,880 bytes), maximum error 1/255, zero channels beyond tolerance, bad vertices or sparse-still-history violations.

The independent Python audit rereads raw bytes, derives light/source/age/UV/opacity expectations from positions and actual sprite bounds, and recalculates weighted PNG pixels. Queued reversals, stale rejection and cleanup deadlines are checked. Retained direction, phase, mip, side, real capture/FLOW, GPU reuse, partial fade, LOD index/retry/edit, frame, reload, save and shutdown gates pass. Final Vulkan validation errors and tracked resources are zero; there are no new final crash or save failures. Environmental sound-device errors are not suppressed.

All 27 lake frames across nine wave/LOD profiles were inspected and stored in lossless RGB VP9 with exact decoded-byte checks. Rectangular distant horizon/LOD cutoffs remain visible; their cause is unresolved. Wave geometry remains zero bytes even when enabled, so actual ocean waves remain untested. Short lake sequences and the opaque diagnostic atlas do not establish complete stream, flicker, transparent resource-pack or enabled-view acceptance. Video playback at five frames per second is illustrative, not measured FPS.

The first visual fixture failed compilation because wildcard JOML imports made Math ambiguous; explicit imports fixed it before the native baseline. The first candidate build called nonexistent snapshot.flow; this was corrected to the existing direction accessor before the full build and all final native runs. Failed sources and logs are preserved and excluded. The initial independent opacity auditor expected a zero timing byte, while the existing original-start clock reaches -100 even with constant opacity. The corrected audit demands exact -min(time,100), 127/127 endpoints, zero padding and opaque rendered pixels. Raw evidence and production were unchanged for that correction. The failing baseline confirms the gate detects the defect.

## Replay and remaining work

Run `python verify_archived_evidence.py` for CPU-only replay in a temporary copy. It extracts controlled PNGs, verifies sealed gzip logs, restores 27 frames from lossless videos with RGB hash checks, and reruns both strict auditors. PNG compression may differ; decoded pixels must match exactly. For extraction in place, run `python rehydrate_evidence.py`, then `python audit_water_side.py vulkan-order-material-final vulkan-order-material-final-packaged` and `python audit_opengl_side.py`.

Native runs require the recorded pinned toolchain and disposable Vulkan-primary-20261022 saved world through DEEPPRIMAL_NATIVE_RUN_DIR. Current measurements are fresh; pre-reset observations remain historical. Only source and compact evidence are preserved; no installer is published.

Next implement and validate side-face still/flow/material continuity, then different-direction spatial edges, sustained actual streams/ocean, history-cap churn and matched CPU/GPU profiling. Speed-driven advection, other liquid families/tints, owner GPU/FPS and full enabled-view acceptance remain open. Solver substeps are not independent 60 Hz simulation.

Retain registered headwater outlets, regional reservoir budgets and compatible chemistry/volume separation; pressure fanout/p95, 1,024 waiters and beyond-8,192 continuation; coarser LOD, physical unload/input, marker/reload/atlas/save investigations and coherent geology. Catalog/species remains deferred, and the unaccepted drain experiment stays separate. This checkpoint is a continuation point, not engine completion.
