# Flow-aware animated water — 2026-10-23

Actual production fix following audit-only2de9543. A real loaded client water cell at99,153,-235 was captured with FLOW0/east,4/south,16/still. The baseline produced identical east/south mesh UVs and explicitly failed. Client state was restored before assertion; quantity was unchanged.515 parent inputs are Git-blob checked,514 unchanged in baseline except the opt-in validation hook. Sealed failure/crash context and original production sources are retained. This reproduces ignored flow direction, not a claim that all visual flicker has been solved.

The independently authored fix captures FLOW with units into the immutable worker snapshot, rotates top UVs along its16 horizontal directions, preserves native flowing-sprite texel density on sides and chooses water_flow for moving cells/water_still for still cells. Legacy four-argument snapshots retain still material. Previous wet material survives dry faces. Existing state-change invalidation already covers direction changes. No live world reads on worker threads, new clocks, solver/water quantity, geography, LOD, Vulkan backend or shader changes.

Self-audit replaced the initial15-float CPU vertex with face/flow metadata in the existing slot10: low3 bits are face, upper code0 is still and1..16 represents directions0..15. CPU14-float/GPU36-byte strides stay unchanged. Face keys/opacity maps mask face so direction changes cannot inflate face histories. Trigonometry is precomputed and direction is selected once per cell. New snapshot flow storage is5832bytes per capture; existing2048-page/4-job bounds remain. Total process memory is not thereby certified. Partial-fade retained CPU geometry remains1120bytes and prior GPU transitions remain intact.

## Fresh final-source checks

Full sealed uncached25-task build passes16 directions/320 vertices, legacy still compatibility, immutable snapshots, unchanged quantity/geometry, atlas bounds and dry material retention, plus10,000 animation retargets/40 shared-edge samples,500,000 conservative transfers and frozen geography checks. Six archive CRCs,408 compiled-class byte identities, exact shader resource and five nested/standalone module identities pass. JAR SHA256 d641c63740a7d8737e77ef39179b4693c50e60a7ed7d0fd793bf34ff98006cb8.515 parent inputs checked/510 unchanged; two production files and three test/hook files changed, two opt-in tests added. Main and installers unchanged.

Two final development/packaged software-Vulkan sessions and scoped software OpenGL each test actual captured source directions0,4,16 before/after resource reload.18 fenced full-page readbacks verify real GPU atlas UVs; selected vertices/expected UVs and exact raw36-byte records are preserved. Final Vulkan resources are0 buffers/0 images, validation errors0; normal save/disconnect, clean crash delta and sealed/live console identity are required. Prior fade/CPU cleanup, actual memory-pressure water reuse, bounded LOD merge/retry/edit/capture, Vulkan frame/device/shutdown controls repeat. Initial functional candidates and short-warmup performance observations are superseded, not substituted for final-source checks.

Three actual wave/LOD profiles/session preserve27 short lake water-stage frames as9 lossless RGB VP9 clips, byte-compared after decode to source PNG pixels.5FPS playback is illustrative; raw frame nanosecond/game-time records remain. Still patterns/bands persist. Ocean-wave resident bytes are0 at this lake even when the option is enabled, so ocean-wave geometry is not exercised. The transient directional-cell fixture proves capture/GPU UV wiring; these ordinary lake sequences do not prove sustained natural river rendering or seamless flow.

## Scoped CPU cost

Matched isolated CPU meshing uses2 process repeats/variant in alternating order,1000 warmups and400 samples per scene/process with256MiB heap. Quantity/geometry digests and vertex float payloads match baseline; baseline lacks the directional feature. Times include allocation/GC and are not full renderer/frame/GPU or owner hardware measures. Ratios are observations, not a speed guarantee or an acceptance threshold. The sloped patch costs about24microseconds more per build (about25% in this run); this cost remains recorded for further profiling. Raw medians/p95 and per-process summaries are retained.

|Scene|Baseline mean median us|Candidate mean median us|Candidate/baseline|
|---|---:|---:|---:|
|flow-flat|56.5|59.2|1.0478|
|flow-porous|1111.1|1064.3|0.9579|
|flow-slopes|95.6|119.6|1.2519|
|still-flat|71.6|73.7|1.0298|

## Remaining work

Flow orientation/material still switches immediately when direction changes; smooth retargeting, side-UV morph continuity, diagonal tile seams and speed-dependent advection remain open. Shared atlas animation introduces no mesh-local phase clock, but that alone does not prove seamless flow. Next capture longer moving streams plus actual ocean-wave/shared horizontal/vertical/native/LOD handoffs across reload/travel/memory pressure. Keep headwater/finite-renewable regional budgets/chemistry, pressure p95/waiters/residency, spatial/coarser LOD, physical input/unload/reopen, geology and prior marker/reload/atlas/direct-stop blockers open. Catalog/species stays deferred. No owner GPU/FPS, independent60Hz, market-superiority or engine completion claim. No installer released.

Reproduce full build/package with included scripts, then run_vulkan.py order-flow-packed, order-flow-packed-packaged and reuse-gl-flow-packed sequentially using private capture and disposable native-world directories. Symlink captures beside scripts; strict audit requires explicit passing records and inspected frames, not only Gradle status. To reproduce baseline, restore both before-source files and run flow-baseline with the opt-in capture fixture; core-check source added with this fix is not a baseline build gate. Respect the work lease.
