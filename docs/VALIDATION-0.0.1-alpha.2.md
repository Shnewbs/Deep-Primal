# Validation — 0.0.1-alpha.2

Date: 2026-10-18. Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25 / Gradle 9.2.1.

## Changes and scope

This alpha expands the terrain prototype and its performance controls. It does not implement the survival conversion or unlimited height.

- Replaces the tall-column preliminary-surface scan with the direct `deepprimal:column_surface` density function. The authored height field is independent of Y; no search is needed to locate its highest positive grid cell.
- Uses the engine's bounded 5×5 X/Z height grid per chunk. Generation stays on native generation workers; no second terrain worker pool or unbounded global terrain cache is added.
- Adds explicit sky, fog, ambient-light and cloud colors; high clouds; small-scale terrain detail; slope-based exposed rock and high-mountain rocky subsoil. This addresses the dark atmosphere and thick dirt bands observed in the supplied images. Flora and snow placement still use vanilla biome features.
- Adds a bounded diagnostic sampler LRU with reload/shutdown invalidation, fixed primitive timing rings, `/deepprimal perf`, observed heap peaks, tick timings, and GC deltas.
- Adds configurable benchmark limits, slow-tick pacing, request timeouts, bounded executor queues, ticket cleanup and shutdown handling. Filesystem report work runs off the server thread, and snapshots own their data.
- Suppresses block/mob XP at the drop source, preserving unconditional vanilla XP removal and reducing avoidable orb creation.
- Separates core checks and opt-in client capture from the distributed mod, consolidates version metadata, and adds reproducible smoke/comparison tooling.

Diagnostic pacing and caching affect operator probes. They are not general exploration throttles, rendering optimizations, or an FPS guarantee.

## Functional checks

- Clean build, standalone core checks, platform-boundary verification, JSON regeneration, and Python syntax checks.
- 200,000 differential surface-height cases across both lower bounds, including negative and exact grid boundaries. Bounded-LRU stress/eviction/disabled behavior and timing-ring rollover/reset also pass.
- Fresh dedicated worlds for both 768- and 1,024-block presets; FULL chunks; correct bedrock floors; save/flush; clean shutdown; restart with identical four-point sampler responses.
- A 289-point sampler survey on each preset. Switching from the last-column cache to the bounded native grid preserves all 289 diagnostic results. Sampling does not generate target chunks.
- Live diagnostic checks cover duplicate cache hits, cache clearing on `/reload`, configured radius rejection, duplicate-run rejection, cancellation and an empty persistent force-load list after cleanup. The force-load check verifies persistent force-load state; temporary probe-ticket release is also enforced in the code paths.
- A development graphical client joins the local dedicated server, renders the tall mountain scene, captures it and exits cleanly. Explicit colors produce a blue sky and light atmospheric fog. The final capture shows rocky cliff faces replacing exposed dirt bands.
- With a connected player, `/experience set` is cleared back to zero on the next tick. An explicitly summoned XP orb is rejected and no XP orb entity remains. This does not claim a complete fishing, combat, or profession XP implementation.

The visual test uses Mesa llvmpipe software rendering under a virtual display at render distance 6, with server view distance 6 and simulation distance 3. It verifies client loading/rendering, not gaming-GPU FPS. Audio is unavailable in the headless test environment. The optional `visualValidation` capture code is excluded from the release JAR.

## Controlled comparison

Baseline source: commit `e6cec3dd4de634a62103140efc5b5aa65ef22ba5` (alpha 1). Candidate: alpha 2 source in this change. Each version starts a fresh world, seed **20261018**, view/simulation distances **3**, no connected players, **2 GiB** maximum server heap. Three regions centered on chunks `(200,200)`, `(-240,160)`, `(320,-280)` each receive 25 sequential FULL requests, followed immediately by the same 25 requests again. That is 75 cold and 75 repeat requests per version. Alpha 2's optional slow-tick pause is disabled for this comparison.

Hardware: Linux 6.18.55 container, 8 available processors on an AMD EPYC 9V74 host, approximately 9.7 GiB host-visible RAM. Shared virtualized hardware; no client test runs during the measured batches. Startup/JIT, scheduling, native neighbor generation and I/O remain included. Alpha 2 adds surface detail and changes surfaces, so output blocks differ from alpha 1.

| Pass | Metric | Alpha 1 | Alpha 2 |
| --- | --- | --- | --- |
| Cold | Total elapsed | 16.83s | 17.49s |
| Cold | Requests/sec | 4.456 | 4.288 |
| Cold | Median ready | 199.777ms | 199.729ms |
| Cold | p95 ready | 400.715ms | 350.410ms |
| Cold | p99 / max ready | 1604.199ms | 1652.120ms |
| Repeat | Total elapsed | 11.11s | 10.96s |
| Repeat | Requests/sec | 6.751 | 6.843 |
| Repeat | Median ready | 149.948ms | 149.944ms |
| Repeat | p95 ready | 150.483ms | 150.714ms |
| Repeat | p99 / max ready | 251.121ms | 200.660ms |

The results are mixed. Cold p95 is lower, while aggregate cold throughput is about 3.8% lower; repeat throughput is about 1.4% higher. These small samples on shared hardware do not establish a broad speedup. Request polling imposes visible timing steps, and repeat requests may use resident or saved chunks. No pure-generator CPU improvement, allocation reduction, or FPS increase is inferred from this table. Reported elapsed totals sum rounded batch durations.

The direct solver removes work by construction; native-grid caching bounds and reuses height samples. Their isolated contribution has not been established by a stage-level CPU profile. Added detail has a cost. The full performance gate remains open: stage CPU/allocation profiles, multiple seeds and runs, representative multiplayer exploration, frame spikes on real GPUs, storage/network cost, generation-order seams, and height scaling still need measurement and agreed budgets.

## Reproduction

After accepting the Minecraft EULA, run from the repository root with Java 25:

```sh
./gradlew clean build
python3 tools/generate_worldgen.py
python3 tools/server_smoke.py ./gradlew --preset prototype --suffix comparison --radius 2 --comparison --restarts 2 --accept-eula
python3 tools/server_smoke.py ./gradlew --preset prototype_deep --suffix deep --port 25579 --runtime-checks --accept-eula
python3 tools/compare_benchmarks.py BASELINE_REPORT_DIRECTORY CANDIDATE_REPORT_DIRECTORY
```

Use fresh run directories for cold tests. Run the same harness against the alpha 1 checkout for the baseline, omitting `--runtime-checks` there. The harness uses loopback-only, disposable offline-mode servers and disables diagnostic slow-tick pacing. Those server properties are test fixtures, not deployment defaults.

Raw comparison CSV, summaries and sampler records: [validation/0.0.1-alpha.2](validation/0.0.1-alpha.2/). Deep-profile and runtime-check records accompany them. Build environment workarounds from the original validation still apply; they are not shipped in mod code. No Minecraft game distribution or world save is committed.

## Playtest limitations

Use fresh disposable worlds. Terrain detail and surfaces changed, so existing and newly generated chunks may meet at visible seams. Height profiles remain bounded at Y=-128..639 or -384..639. Renewable groundwater, native materials, Earth data, LOD, custom vegetation and the broader survival systems remain future work. The original screenshots used render and simulation distance 32; lower initial distances are documented as playtesting starting points and are not forced by the mod.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.2.jar`. SHA-256: `e42e31137711b8216e975c12cdd11d3ae1508a5a66188ae6714ef39d67612337`.
