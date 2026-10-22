# Active engine audit — alpha.24 candidate

Status: 2026-10-22 05:06 UTC. Candidate source is checkpointed on [audit/alpha24-recovery](https://github.com/Shnewbs/Deep-Primal/tree/audit/alpha24-recovery), currently `0bcb01bbd002c2474a6dff91891aa7e1951d3f2f`. No alpha.24 installer has been released.

## Implemented in the candidate

- Independent V3 continental drainage: connected small tributaries, accumulated catchment, varying widths, natural lake spill levels, connected sea classification, continuous inlet/estuary beds and dry bank floors applied after the union of wet channels. Published V1/V2 geography remains versioned.
- Water surfaces interpolate shared corners without artificial center dimples. Planar/sloping patches use one quad; genuinely curved saddles retain subdivision. Adjacent mesh jobs reuse corner heights.
- Materials use the final shaped terrain slope. Riparian banks remain sealed against cave cuts. Trees have revised crowns and deterministic footprint checks.
- Bulk chunk samples reuse an 18×18 height halo. Cave columns cache the exact vertical noise endpoints instead of recomputing them for every Y. Pressure/drain searches reuse bounded primitive scratch storage.

## Fresh verification after workspace recovery

The execution workspace became unavailable around 04:04 UTC. The local candidate and test environment were reconstructed from the preserved GitHub source and session work. Source changes are now checkpointed frequently. Pre-reset screenshots and timings are historical evidence, not substitutes for these reruns.

- Full recovered build passed. A second full build with cave reuse passed in 43 seconds, including module/platform boundaries and the complete core suite.
- Nine-seed watershed suite: 5,845,944 centerline samples, 951 headwaters, 648 lakes, 4,286 connected flowing reaches and 539,438 contained-bank checks. Cache eviction/replay and 18,000 frozen V2 sample comparisons pass, in addition to existing V1/V2 regressions.
- New cave comparison: 965,965 Y positions against the published alpha.23 formula, including negative heights, world-edge coordinates and the bounded fallback; 21,771 cavity positions exercised.
- Fresh native watershed run before cave reuse: five sites, 11,260 water cells, 2,976 dry columns, 80 material-column comparisons, 187 sealed banks and 23 connected source/inlet/estuary routes. Generation plus audit took 15,933.60 ms in this test environment.
- Fresh V3 codec round trip and nine-chunk digest match the pre-reset candidate: `5c58aa00a2718e7177221d9caea486cbc66da57884f94ce3db2b14f1ba8095f2`. The measured request/audit time was 532.00 ms before cave reuse; this single run is not an FPS claim.
- Lean candidate with cave reuse is 604,606 bytes and contains four intended modules, with no validation classes in the root archive. Final installer audit is pending.

## Active acceptance work

- Fresh 1,000-tick real-solver lake run passed with 49 forced chunks: initial surface 153.1875, retained center 153.375–153.4375. Median tick work 15.22 ms, p95 22.71 ms; pending work decreased from a peak of 9,057 to 3,693 and still has active transfers. This is a finite, loaded-basin test, not global hydrology or a settled-state claim.
- Repeat native geometry/digests after cave reuse and verify saved V2 native blocks against the published digest.
- Repeat actual Create World/join/save and reopen. The first recovered client attempt was blocked by the test display's missing GLX module; that dependency has been extracted into the private test environment for a retry.
- Capture and inspect coast, real narrow headwater, river, lake, outlet and ridge scenes on OpenGL/Vulkan, plus controlled animated/fallback water surfaces.
- Recheck bounded filling/pressure behavior, profile packaging, resource release and native costs before releasing an installer.

## Broader requirements still unfinished

Renewable watershed/ocean budgets, persistent pressure searches beyond the current 128-cell search window, geological strata/ore hosts, intentional underground rivers, live erosion/deposition, full-distance LOD coverage and transparent-material ordering remain separate implementation work. Three fluid passes per 20 Hz server tick are nominally 60 substeps/second; they do not establish an independent 60 Hz simulator or a guaranteed 60 FPS minimum.

TFC/Terralith/Tectonic, PhysEx, finitewater, BetterWaterMesh, Water Erosion, Distant Horizons, Gaea/World Machine, Nanite, Cesium and meshoptimizer remain references. This candidate does not incorporate their runtime code or establish performance superiority.

Hourly progress reporting remains enabled. It reports evidence; it does not itself perform background development.
