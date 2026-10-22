# Pressure continuation candidate: native progress, performance gate blocked

Date: 2026-10-22. Base: `da03d9cecc54d3302281890cfc1310ed6b143b68`; source branch: `work/engine-after-alpha24`. This is an **unreleased working checkpoint**, not a stable merge or installer release. Main and supplied alpha.24 bytes remain unchanged. V1/V2/V3 geography is unchanged.

## Actual source change

`WaterRuntime` now retains a connected-water search's frontier, visited set and candidate surfaces across turns. The old 128-node array lost continuation once full. The new search still visits at most 128 nodes per turn, schedules resident jobs in FIFO rotation, and retains unfinished jobs across host ticks. It commits through the existing conservative paired transfer. Pending diagnostics include resident jobs.

The first implementation has explicit limits: 16 resident jobs per level, 8,192 retained nodes per job (131,072 total node slots), the existing host-tick pressure budget, and at most 16 admissions/turns per substep. Normal stepping therefore uses at most 6,144 pressure visits per host tick; direct adapters remain under the existing 12,288 absolute host-tick allowance. It does not flood to completion in one call. At the residency boundary, the search records saturation and finishes its bounded explored portion; arbitrary larger connected regions are **not** accepted. Drain searches still have the old 128-cell window and no general continuation.

Jobs watch their read chunks. The small server-only `PressureChunkMutationMixin` observes actual `LevelChunk.setBlockState` returns, including edits with neighbor notification suppressed. A changed visited cell or adjacent boundary/outlet invalidates the witness graph; unrelated writes elsewhere in the chunk and unchanged-unit flow-direction updates do not. Chunk unload invalidates all jobs watching that chunk. Invalid jobs discard the old frontier before restarting; world/level unload and server stop clear state. No production chunk loading is introduced. This is independently authored code using host APIs, with no new dependency or borrowed solver.

`LongPressureChecks` now supports explicit `fixed`, `edit`, `unload`, `idle` and `reopen` modes. Its default `reproduce` mode deliberately retains the historical failure expectation. The old source-extracted script now identifies itself as historical and requires a pre-continuation `--root`; it directs current acceptance to the native fixture rather than silently compiling an incompatible extraction.

## Fresh verification

All results below are fresh post-reset checks. Earlier alpha.24 graphics evidence is separate.

- Final default **non-Vulkan** `build`: exit 0, `BUILD SUCCESSFUL in 55s`; all CoreChecks and core/module boundaries pass. The nine-seed watershed suite still reports 5,846,893 centerline samples, 4,292 connected reaches, 650 lakes, 964 headwaters and 18,000 saved-V2 comparisons. [Compact build evidence](2026-10-22-pressure-continuation-build.txt).
- Native Overworld `fixed`: two independent 262-cell bends both reach equal eight-unit upper surfaces after 120 host ticks. Each retains all 4,192 units. The first search retains its frontier after exactly 128 visits. Observed turn maximum 128, resident-job maximum 16; all configured visit/node bounds hold.
- Native `edit`: a stone wall inserted into the already visited prefix with flags `2 | 16` is preserved. The retained search restarts, the disconnected remote outlet stays empty, and all remaining 4,176 units are conserved (the explicit wall edit removed one 16-unit cell).
- Native `unload`: a previously visited middle chunk at 262,0 actually unloads before continuation resumes. The remote outlet stays empty, the production solver leaves that chunk missing for 120 ticks, and the test harness subsequently reloads it to verify all 4,192 units remain. The harness alone forces/releases test chunks; this does not permit production force-loading.
- Native isolated-End `idle`: both communicating bends equalize and conserve volume. The final 21 sampled ticks have zero queued work, visits, retained nodes or watched chunks in that fixture dimension. This proves fixture idleness, not that the generated Overworld settles.
- Native separate-process `reopen`: reads the idle test's saved blocks without reconstructing or reinjecting water. Both 4,192-unit totals and equal surfaces survive; another 120 ticks pass, with zero search work and watches in the isolated fixture.
- Existing native pressure regression passes communicating vessels, a disconnected wall and no-force-load. Existing filling regression passes three-block first-tick fall, basin filling at tick four, films, plants, dormant interior activation and 1,024 competing stable cells; final pending work is zero in that controlled fixture. Three transport substeps per 20 Hz host tick are not an independent 60 Hz simulator or FPS guarantee.

Every final native case has an explicit passing record and normal save/shutdown. Logs/crash directories were inspected; Gradle exit status alone was not accepted. See [native records and caught failures](2026-10-22-pressure-continuation-native.txt).

## Fresh matched generated-basin cost

Both revisions ran the unchanged `LakeRetentionChecks` fixture in separate fresh worlds: seed 20261020, 1,000 ticks, 49 forced chunks, view/simulation distance 2, same restored host/toolchain. The fixture SHA is recorded in [paired results](2026-10-22-pressure-basin-comparison.json). It checks 25 central surface columns against spill-level tolerance, not a complete regional mass ledger. These are single runs, not repeated statistical certification.

| Measure | Base `da03d9c` | Continuation candidate |
| --- | ---: | ---: |
| Retention gate | Pass | Pass |
| Median server tick work | 15.595 ms | 17.092 ms |
| p95 server tick work | 24.754 ms | 27.239 ms |
| Final pending jobs | 3,676 | 7,093 |
| Final sampled surface | 153.375–153.4375 | 153.1875 |
| Final pressure visits/tick | 12,288 | 6,144 |

**Performance promotion remains blocked.** This pair costs about 9.6% more at median and 10.0% more at p95, with a larger unresolved queue. The new pending count includes at most 16 resident jobs, which cannot explain the backlog difference. Neither run proves complete settling, output equivalence or owner-device performance. The revised solver fixes the demonstrated small long-route cutoff within its working set but has not met the broader performance/settling requirement. Do not merge it to main or publish an installer while this gate is unresolved.

## Caught failures and packaging limits

The initial compile caught renamed ChunkPos packing APIs; these were corrected before the passing build. The first unload fixture chose a chunk still held by a neighboring forced ticket. It raised an assertion and generated a Minecraft crash report **despite Gradle exit 0**. The fixture probe was moved farther inside the already visited gap, and actual unload then passed. The failed probe is recorded as a failure, never as native acceptance. Offline public-key DNS and LAN-pinger errors were nonfatal.

The final development JAR is 612,612 bytes and passes outer/four nested archive CRC checks. Validation and private build-compatibility classes are absent; the mutation observer is present. [Hash and package scope](2026-10-22-pressure-development-archive.json). It is not a release-audited installer and was not supplied.

## Next implementation and remaining acceptance

Profile duplicate/overlapping pressure searches, invalidation/restart churn and admissions. Coalesce or reuse work without losing a disconnected component, player edits or fair progress; repeat the matched basin and long-bend/edit/unload/idle regressions. Keep the performance regression visible until corrected or the candidate is revised. Then extend bounded drain continuation and behavior at the pressure residency limit; preserve safe activation across unload/reload and persistent saved volume.

The requirement register remains open: enabled-view near/far water coverage, memory pressure, mixed transparency, renderer/LOD lifetime and performance; renewable regional reservoirs with finite local water, perennial/seasonal supply, depletable wells and accounted ocean recycling; geology/material fields and terrain quality. No new OpenGL/Vulkan/visual or GPU/FPS checks ran; a display and the optional Vulkan environment remain unavailable. V3 geography is frozen, and catalog/species work remains separately deferred. This checkpoint is partial engineering progress, not full engine acceptance.
