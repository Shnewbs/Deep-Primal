# Water side UV continuity — 2026-10-23

Parent d2f96e2dfadc06beef0c661bbda74324a5c43423; guarded lease parent 979a5e796d2f9fa861cd9da5d045bf8fdbb6079f. Unreleased continuation on work/engine-after-alpha24. Main and supplied installers remain intact. Engine acceptance is incomplete.

## Reproduction and focused change

The existing height morph moves geometry over 100 ms but immediately switches side texture V to the new target. The production shader, unchanged from the parent, produces 640 differing pixels across six controlled matched-height pairs with identical geometry. The settled pairs match; this is a real texture-displacement defect, not a silhouette difference or screenshot timing claim. Baseline raw images, complete sealed console, explicit failing result, unchanged-production source proof and exact pixel fixture are preserved. The expected baseline failure is not a client crash or acceptance pass.

Two production files change: renderer upload derives side V from the admitted shared height (including rejected stale captures), encodes side/flow mode in the unused high bits of native block-light coordinates, and carries sprite bounds in existing matrix storage. The shader masks those bits before light sampling and moves/clamps side V with the same displacement used by geometry. CPU stride remains 14 floats; GPU stride remains 36 bytes; uniform allocation remains the existing matrix. Top UV selection, light values, height/opacity timing, quantity, solver, worker bounds, LOD and frozen geography are preserved. No installer is released.

## Fresh validation

Full uncached Linux Vulkan build: 25 executed tasks, explicit build/core/terrain/hydrology/water records, sealed raw/gzip identity. All six archive CRCs, 408 compiled classes and five nested/standalone module identities match; validation and private compatibility-agent payloads are absent. Pinned toolchain and Mesa/Xvfb were restored and hash checked after workspace loss; old pre-reset measurements are historical.

Two strict software Vulkan sessions (including packaged class origins) and one scoped OpenGL llvmpipe fallback session pass explicit records, reload, normal save/shutdown, sealed log/crash checks and prior water/LOD/buffer lifetime regressions. Each passes six exact matched-height pixel pairs for still/flowing sides at 0/50/100 ms and twelve actual production uploads, including repeated retargets, stale revisions and native light coordinates. Totals: 18 exact pairs/36 controlled renders, zero differing pixels; 36 fenced upload readbacks/25,920 bytes. Prior real-client FLOW capture and eighteen GPU direction/material UV samples before/after reload also pass. Controlled pixel tests use the real production pipeline and gradient atlas; upload tests independently decode actual production buffers and real nonzero atlas bounds. Neither alone proves full live-stream acceptance.

Twenty-seven actual lake frames in nine wave/LOD profiles were inspected; lossless RGB VP9 videos decode byte-for-byte to their source frame pixels. Playback is illustrative, not measured FPS. Water and terrain remain present. Enabled-wave lake runs have waveBytes=0; actual ocean waves remain untested. Page order and two-layer pixel controls retain their explicit passes, without establishing per-primitive transparency.

The first restored build exited zero but its client console lacked all required completion records. It is explicitly rejected under rejected-capture/, with selected daemon records and full daemon hash. A later complete build passes; that does not explain the missing client tail. No new final crashes, save errors, Vulkan validation errors or tracked buffer/image leaks were observed. The 200,000-case CPU algebra check is double precision, not native shader or performance evidence.

## Remaining work and continuation

Next: smooth direction/material retargeting, then diagonal seam and longer moving-stream coverage, speed-dependent advection, shared/native/LOD transitions and actual ocean-wave coverage. Preserve bounded storage and quantity; do not hide abrupt directional UV changes behind the height fix. Source/headwater reservoirs, compatible-liquid chemistry/mixing and conserved constituent budgets are requirements only. Prior staged retained-drain pressure experiments remain separate and unaccepted: fanout latency, 1024 waiters, larger-region/residency limits and broad output/memory acceptance remain open. Spatial/coarser LOD discovery beyond the 8192-key cap, physical input/server unload/reopen, unexplained reload/atlas/marker/save failures, coherent geology and owner-GPU/frame-time/market comparisons remain unresolved. Catalog/species work stays deferred. Solver substeps are not independent 60 Hz simulation.

The source, reproducible fixtures, strict audit scripts, compact logs, raw controlled pixels/readbacks and exact live-frame videos are preserved here for review. A successful checkpoint is a continuation point, not engine completion.
