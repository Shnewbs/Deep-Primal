# Active Deep Primal engine audit — alpha.24 candidate

Status: 2026-10-22 05:33 UTC. Source and evidence are checkpointed on [audit/alpha24-recovery](https://github.com/Shnewbs/Deep-Primal/tree/audit/alpha24-recovery). No alpha.24 installer has been released. The engine is not complete.

## Implemented and checked

- Independent V3 regional drainage, connected small tributaries, catchment-based widths/depth/speed, natural lake spill levels, sea connectivity, inlet/estuary beds and dry bank containment. Final shaped slope drives materials; deeper channels get wider valley transitions. V3 coastal detail breaks up the uniform shoreline field. Published V1/V2 geography is retained.
- Bulk column/height-halo reuse, exact per-column cave-noise endpoints, reusable bounded primitive fluid-search storage, shared mesh corner caches and fewer planar water quads.
- Bilinear water surfaces remove per-cell center dimples. A later audit reproduced mismatched corner heights across vertical block layers, including section boundaries. The latest source uses consistent neighboring-column heights, removes layer clipping and an invalid empty-surface shortcut, bounds side UVs to the sprite, and expands animated-page culling bounds. Both presentation paths use the same height rule.

## Fresh evidence after recovery

The execution workspace became unavailable around 04:04 UTC. Source and the native test environment were reconstructed, and the code is now checkpointed frequently. Pre-reset captures/timings are historical; the following checks were repeated.

- Latest full build passes in 57 seconds. Nine-seed watershed suite: 5,846,893 centerline samples, 964 headwaters, 657 lakes, 4,292 connected flowing reaches and 538,908 contained-bank checks. Cache replay and 18,000 frozen V2 sample comparisons pass, alongside the existing V1/V2 suite.
- Cave reuse passes 965,965 frozen Y comparisons in the original check, and 761,165 in an expanded mix of empty/clipped-height columns. Native V2 still matches the published nine-chunk block digest. V3 before/after cave reuse and reopen matched the same digest; subsequent unreleased V3 coast changes intentionally alter geography.
- Native revised geography: five sites, 12,213 water cells, 2,976 dry columns, 80 material columns, 187 sealed banks and 55 connected routes, including all 33 actual land-to-sea reaches in the test region. One old fixed estuary location became dry when the coast changed; auditing every current mouth replaces that obsolete assumption.
- Actual revised V3 Create World/join/save passes on OpenGL; form preparation was 993 ms in this run. Six native terrain views were inspected. Continuous gravel strips are reduced and the inland lake/outlet is visible. Repetitive contours, broader terrain variety and distant water artifacts remain unresolved quality work.
- Latest real-solver basin test passes 1,000 ticks with 49 forced chunks: planned level 153.1875, retained center 153.375–153.4375. Median tick work 16.93 ms, p95 24.74 ms. Pending work decreased from 9,057 to 3,667 and still has transfers; this is not a globally settled hydraulic system.
- Conservative filling and communicating-vessel tests pass: the test basin fills by tick four, all tested volume is conserved, stable test work drains to zero and missing chunks are not force-loaded.
- Latest surface checks cover 441 planar samples, 20,000 bounded interpolation cases, 64 shared vertical-cell vertices and 1,024 shared vertices across vertical section boundaries for all 256 partial-level pairs.
- Earlier recovered OpenGL controlled water captures pass animated/fallback checkerboard and sloped pools. Final GPU checks after the last cross-layer correction are running; Vulkan repetition and final packaging remain pending. The latest lean candidate is 605,223 bytes, before final profile audit.

Compact staged records: [native evidence](validation/0.0.1-alpha.24/native-evidence.txt).

## Continuation and open requirements

See [work state](ENGINE-WORK-STATE.json) and [build/audit handoff](ENGINE-BUILD-AUDIT-HANDOFF.md). The active foreground lease prevents scheduled work from modifying this checkout or racing native tests.

The larger unfinished work includes enabled-view water/LOD coverage and mixed transparency, persistent pressure searches beyond the present 128-cell window, renewable watershed/glacier/aquifer/ocean budgets, coherent geology/ore hosts, intentional underground rivers, erosion/deposition and richer terrain. Do not represent these as delivered by this candidate. Three passes per 20 Hz host tick are nominally 60 substeps/second, not an independent 60 Hz simulator or a guaranteed 60 FPS minimum.

TFC/Terralith/Tectonic, PhysEx, finitewater, BetterWaterMesh, Water Erosion, Distant Horizons and terrain/rendering tools remain architectural references; this candidate does not incorporate their runtime code or demonstrate superiority. Optional Vulkan retains its separately documented existing provenance.
