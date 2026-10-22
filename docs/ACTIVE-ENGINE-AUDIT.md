# Active engine audit — alpha.24 candidate

## Recovery checkpoint — 2026-10-22 04:25 UTC

The execution workspace became unavailable during the next performance experiment. GitHub main and the previously delivered alpha.23 build remain intact. The unreleased local installer, native captures and local test environment were not preserved by that workspace reset.

Recovered source is being checkpointed on [audit/alpha24-recovery](https://github.com/Shnewbs/Deep-Primal/tree/audit/alpha24-recovery), starting at commit `cd0fac8c04298f14f11015bd39b150380a216a12`. This is a work-in-progress branch, not a released installer. The Minecraft adapter and native fixtures are still being restored.

A fresh standalone Java 25 rerun of the recovered core passes all nine seeds with the same pre-reset counts: 5,845,944 centerline samples, 951 headwaters, 648 lakes, 18,000 unchanged V2 samples, 4,286 face-connected flowing reaches and 539,438 contained-bank checks. New bulk-column inputs also match full samples after restoring the slope metadata.

The native measurements below are historical evidence from the pre-reset candidate. Fresh full/native validation is required before releasing the recovered candidate.


This is an ongoing implementation and validation record, not a declaration that the engine is complete. Status recorded at 2026-10-22 03:24 UTC. The candidate code is still being validated locally; this status checkpoint does not publish the candidate installer.

## Current scope

- Own continental generator and region drainage planner, with new geography saved as `continental_v3`.
- Connected small tributaries, variable channel width/depth/speed, natural lake spill elevations, and consistent shore/bed placement.
- Remove artificial per-cell water dimples in animated and chunk-buffer rendering.
- Keep published V1 and V2 geography reproducible.
- Audit native creation, saved revision/codec round trips, generated water cells, vegetation support, rendering, and runtime cost.

## Evidence so far

- First core build passes; new bilinear surface checks cover planar surfaces and 20,000 bounded interpolation cases.
- A follow-up audit reproduced invalid lake outlet beds and wet metadata without a native fluid voxel. Both are corrected in the current V3 implementation; native integration remains to be checked.
- Core watershed ensemble passes 5,138,836 centerline samples. Three seeded continents contain 313 headwaters and 209 lakes. An analytical closed basin also produces a lake. Parent routing and water heads are monotonic.
- 6,000 V2 samples match the alpha.23 baseline exactly; existing V1 differential checks pass.
- V3 region cache is capped at six immutable plans, approximately 4.1–4.2 MB each for the sampled continents. Replay after eviction passes.
- Native V3 creation/save and generator codec round trip pass. Nine generated chunks were audited.
- The first native face-connectivity audit failed on a source reach. After correcting shallow stream cores, a new run passes 20 source reaches, 11,092 actual water cells, and 2,976 dry columns across five sites.
- A generated basin stays at exactly 153.25 blocks for 200 ticks with its full 49-chunk footprint loaded. The initial run measured median server tick work 21.56 ms and p95 47.03 ms. A run with reusable search buffers measured 19.65 ms median and 47.17 ms p95; these are two software-test runs, not a hardware FPS guarantee. Pending work is shrinking and still transferring water.
- Native OpenGL controlled checkerboard/sloped water captures pass in animated and fallback rendering. An isolated reproduction using the old surface kernel and mesher shows the repeated peaks/dimples; the corrected surface is continuous with the same measured cell volumes.
- Visual review rejected broad clay banks, sparse tree crowns, and the original headwater scene selection. Candidate material/crown changes and new site selection are awaiting fresh captures.
- Expanding connectivity checks to complete flow reaches exposed an estuary gap and a river-to-lake inlet gap. Candidate fixes now extend receiving beds and distinguish connected sea from landlocked depressions below sea level. The expanded multi-seed suite is running again; full native revalidation follows it.

## Acceptance still open

- Revalidate native channel/lake water placement and outlet retention after the expanded estuary/inlet fixes.
- Fresh creation/join and save/reopen of V3 geography.
- Visual review of coast, narrow headwater, broad river/confluence, lake outlet, and ridge scenes in OpenGL and Vulkan.
- Water surface comparison in animated and fallback paths, including sloped and thin water.
- Native performance measurements and installer audit.

## Broader work that must not be represented as finished

Renewable watershed/ocean budgets, detailed geological strata and ore hosts, intentional underground rivers, live erosion/deposition, full-distance renderer coverage, and transparent-material ordering still require separate implementation and native evidence. Existing modules and prototypes do not establish completion or superiority over reference projects.

The hourly progress automation reports verified changes and failures; it does not itself perform background development.
