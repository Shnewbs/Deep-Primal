# Alpha 12 — Mountain streams and gorges

Validated on 2026-10-19 against Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25, from alpha 11 commit `f1a2203484bf4fb3681935b32e364771e83a8e56`.

## Delivered behavior

New mountain tributaries descend at local elevations, merge along a drainage graph, and cut through obstructing saddles instead of putting every stream bed near sea level. Their widths increase with contributing catchment area. Lowland river trunks and coastal beach shaping remain available. Submerged inland beds now follow the actual local water height: arid biomes use sand, ordinary inland beds use gravel, and steep/rocky surfaces retain rock precedence.

| New-world startup settings | Selected terrain |
| --- | --- |
| `riversAndCoasts = false` | Original `prototype` / `prototype_deep` |
| `riversAndCoasts = true`, `mountainStreams = false` | Frozen alpha 11 `waterways_v1` / `waterways_v1_deep` |
| Both true, the default | Alpha 12 `waterways_v2` / `waterways_v2_deep` |

`extraUndergroundDepth` still selects Y=-384..639 instead of -128..639. These are creation settings; restart after changing them. All six pack IDs remain registered, with only the selected creation pack required. Saved dimensions keep their serialized noise settings and height. There is no regeneration or height migration of existing terrain.

## Implementation and cost controls

`MountainDrainage` lives in the platform-neutral core. It samples a jittered 64-block routing grid inside canonical 4,096-block regions, seeds existing wet destinations, uses a minimum-spill priority flood to build acyclic routes, accumulates contributing area, and lowers downstream channel profiles where necessary. Routing inputs are capped at the shared physical world ceiling Y=640 so headwater profiles cannot be derived from inaccessible noise heights above the buildable world. A completely dry region with no wet destination creates no invented outlet. New channels remain inside their region and join the existing lowland/coastal water field.

A fixed four-way cache retains at most 32 immutable region records per seeded sampler, approximately 4.2 MiB of retained region arrays at capacity. Neighboring-catchment tests check that ordinary four-region access does not resample terrain repeatedly. Cache misses use the calling native generation worker; there is no additional terrain executor, permanent watershed map, or per-tick river simulation.

Native density visitors intern structurally equal `WaterwayHeight` nodes. Runtime cache identity is excluded from equality so terrain, biome depth, surface water and horizon sampling share the seeded planner. A regression assertion caught and corrected duplicate planner instances during development. Chunk wrapping retains that planner. Surface water is precomputed for the chunk's 256 columns; the per-voxel aquifer path only reads that array and native cached surface height. Fluid handling outside the local surface band continues through Minecraft's aquifer.

Vulkan, graphics settings and common performance retain their separate modules. Their classes and bundled dependency classes match alpha 11 byte-for-byte. This update adds no river-mod dependency. See [the cross-reference audit](RIVER-MOD-COMPARISON-2026-10-19.md) for the distinction between source inspection, author-documented behavior, and features not implemented here.

## Native generation checks

The final packaged installer was loaded by a dedicated server with development validation classes supplied separately. Tests survey nine routing regions for each seed, then generate actual blocks along elevated channels, intervening gorges and descending courses. Quarter-segment samples check that the selected water surface does not climb. Nodes and midpoints require water in the center column with a supported bed; the surrounding 7×7 regions also count actual water and bed materials. Mountain and gorge samples are promoted to FULL chunks to verify that water remains after carvers, features and lighting. Layered dry coastal beaches are checked separately in 25×25 regions.

| Profile / seed | Selected channel segments | Segments above Y=160 | Gorge segments, terrain minus water >16 | Mountain water level | FULL mountain / gorge water blocks |
| --- | ---: | ---: | ---: | ---: | ---: |
| Standard / 452343 | 1,151 | 488 | 184 | 308 | 142 / 122 |
| Extra depth / 947291 | 1,190 | 448 | 294 | 311 | 141 / 147 |

These are sample-segment counts, not counts of distinct rivers or gorges. Six nodes were followed in the standard course and eight in the deep course, with additional checks between nodes. The selected gorge water levels were 126 and 113. Coastal beach checks passed at (1920,-3968) and (1376,-3936). Map checks took approximately 190 ms after earlier sampler use; this is not an isolated cold-generation benchmark, a whole-world performance bound, or an FPS measurement.

Both frozen original and alpha 11 profiles were created, saved, reopened after enabling the new creation settings, and checked for exact noise-setting IDs, world heights and hexadecimal surface-height snapshots. These are reproducible legacy-profile saves created by the fixture, not a claim that a user-uploaded alpha 11 save was tested.

An apparent startup stall during the console-driven test was investigated with a thread dump. The server was already running normally in `waitUntilNextTick`; console-command delivery was not a reliable test-control path in this environment. Validation now runs automatically from the test server's first post-start tick and writes unique per-run records. It then stops the test server normally. The final checks require successful validation, save and process exit; console readiness text alone is not treated as proof. No production startup workaround was added.

## Build and package checks

`build compileVisualValidationJava --offline` passed, including module boundaries, 100,000 existing waterway cases, 20,000 drainage sampling cases, downhill/acyclic/wet-outlet checks, ridge incision, no-outlet preservation, negative coordinates, cache eviction, neighboring-cache reuse and the existing 200,000 surface, queue/grid, horizon and cache checks. Generated resources are idempotent. All 26 pre-alpha-12 terrain/preset/pack resources match alpha 11 exactly in both source and installer.

Installer: `DeepPrimal-26.1.2-0.0.1-alpha.12.jar`, **22,540,815 bytes**, 19,930 bytes larger than alpha 11.

SHA-256: `2d5c3f60321d403517f9b5ba48f2cd88cab023d864b055aac461de8114f77ccf`.

Own class counts: root 42, core 26, performance 14, graphics settings 16, Vulkan 178. All compiled project classes and all four nested module JARs match their shipped bytes. There are no duplicate project classes, development validation classes, or repackaged Minecraft/Mojang classes.

`build -PwithoutVulkan --offline` also passed, producing a 236,634-byte package with exactly three current nested modules and no Vulkan binary. Restoring the standard profile produced the exact original installer bytes. This was a build/package check, not a separate client boot.

The environment required the same local-only NeoFormRuntime process-command workaround described in alpha 11. That patched build tool is not shipped. Headless server tests used the official asset index without downloading client-only asset objects. The final matrix used a Java launch description exported from NeoForge's prepared development run, the packaged root/module JARs, and separate validation classes.

## Reproduce and remaining limits

With Java 25 and prepared dependencies, after accepting Minecraft's EULA, run `python3 tools/validate_mountain_rivers.py ./gradlew --accept-eula`. Tests run automatically inside the server; no console commands need to be forwarded. Existing `run-drainage-*` test worlds are preserved. `--offline` uses prepared dependencies; optional `--java-launch` accepts a prepared launch description. Each run uses a unique validation record to prevent stale evidence from satisfying a later check. Validation code is excluded from the installer.

The new solver creates regional tributary catchments, not one globally routed watershed. Sea-level trunks remain; upland routes do not stitch across region borders. Elevated lake generation, arbitrary ocean-drainage guarantees, continuous erosion, rainfall/discharge budgets and custom sustained currents on flat water are not implemented. Native water is used. Finite routing samples can produce angular bends; appearance, river density and banks need interactive playtesting across more seeds. The distant horizon shares the carved height field but retains its existing sea-level water approximation and does not render fine elevated stream water. GPU/hardware FPS validation was not repeated for this generation-only update.

Evidence: [runtime summary](validation/0.0.1-alpha.12/runtime-summary.json) and [check markers](validation/0.0.1-alpha.12/checks.txt).
