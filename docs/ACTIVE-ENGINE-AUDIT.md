# Active Deep Primal engine audit

## 2026-10-25 channel revision 3 staged: river-mouth delta fans

No artifact, release or score change. `WatershedRegion` channel revision 3 is
implemented and checked; production access is opt-in through a new creation-time
switch and two new preset packs. Existing `engine_standard` / `engine_deep`
presets still save `channel_revision: 2`, and revision 0/1/2 output is unchanged.

- Scope: a routed node whose parent is ocean, catchment ≥ 1,800,000 and stage
  ≤ SEA+6 becomes a delta mouth (at most six per 8192-block region, largest
  first). The fan extends inland from the mouth: its surface rises from
  SEA+0.35 at the shoreline to mouth stage+0.45 at the apex and only raises
  ground below that surface, never lowering or burying higher terrain. Three
  distributary reaches fork at 45% of the fan and spread to ±0.62 of its
  half-width, with 1.6–4.6 block radii and about 1–3 blocks of water, ending
  just above sea level. Fans and branches join the tile index, so sampling stays
  local and bounded.
- Opt-in: `riverDeltas` (default false) selects `engine_deltas_standard` or
  `engine_deltas_deep`, which save `channel_revision: 3`. The generator codec
  accepts 0..3 and revision 3 still requires V7 geography.
- Core evidence (JDK 25, dependency-free suite): full `CoreChecks` exit 0 in
  285.8 s with `DELTA_REVISION_PASS fans=21 endpoints=252 coreColumns=252
  coreWet=111 depositedColumns=350 widest=220.0 longest=129.2 deepest=7.82
  outsideChanges=0` and `DELTA_CORE_PASS revision2Replay=true
  fansBounded=true distributariesContained=true`. Coverage is three seeds × nine
  regions: all 252 distributary endpoints are wet; every sampled fan column is
  either an open channel or dry above sea level; no sampled channel spills into
  a lower dry neighbour; no water deeper than 3 blocks is filled; no channel
  water is below sea level; nothing changes outside the fan footprint; and
  revision 3 rejects non-V7 geography. Frozen revision-2 digests for seeds
  0/73/20261020 (`e01bcdaeec5dcbb3`, `7d3ee65224b2359a`, `2bafc04ee1ebe957`)
  are asserted in `DeltaChecks` and unchanged.
- Build: `gradlew --offline build` passes on this checkout (35 tasks, 4m 51s:
  core and cache checks, visual-validation compilation, module boundaries,
  jarJar). The engine-core compile and jarJar tasks were up to date because the
  resolved revision-3 source reproduces the already-built artifact byte for byte;
  `CoreChecks` re-ran against the merged delta checks. The local non-Vulkan artifact is
  `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21.jar`, 114,294,083 bytes,
  SHA-256 `ac9bb39e62258f2fcb2c0cda26549c18cae3accb463a9ae039ea2452361d66c4`;
  it contains both new packs and is a local build, not a release. An earlier
  build in this session rewrote the local non-Vulkan `...dev.20.jar` from this
  source; the released `-vulkan-windows` dev.20 artifact and its recorded hashes
  are untouched, so never use the rewritten local dev.20 file as dev.20
  evidence.
- Not claimed: seaward progradation into `mask < 0` ocean (V7 ocean geography
  stays frozen), avulsion, oxbows, anastomosing reaches, separately budgeted
  distributary discharge, delta-channel navigation behaviour, native or visual
  acceptance. Revision 3 is in no delivered artifact; grade remains 50/100.

## 2026-10-26 dev.20 Earth-Scaled Rivers (channel revision 2)

Artifact `DeepPrimal-Windows-Vulkan-alpha.29-dev.20-Earth-Rivers.jar`, 125,215,496
bytes, SHA-256 `d3d861949725c06f0b5ef78549a93a3747da8c26742432be11652a8364282f01`;
nested Core/LOD/Performance/Graphics/Vulkan jars are dev.20.

- Scope: `WatershedRegion` revision 2 (hydraulic-geometry width, great-river and
  estuary flare, graded trunk incision to 22.1/sqrt(area), stepped canyon walls,
  meander warp, braided reaches with dry bars; bars excluded near lakes). Presets
  save `channel_revision: 2`; codec accepts 0..2.
- Survey: revision 1 identical to baseline. Rev 2 width buckets
  [<4,<12,<32,<64,<128,>=128] = [21614, 680074, 68185, 5206, 3147, 318], max 214.6;
  incision buckets [<2,<8,<16,<32,<64,>=64] = [553, 877, 238, 108, 15, 1], max 73.1;
  940 braided nodes. ChannelAudit: 168,309 wet columns, 0 inverted, 0 flood edges,
  137 dry bar columns, 34 canyon transects.
- Core: `EARTH_RIVER_PASS` (327 incised centres, deepest 76.3, 0 tunnelled, 0 flood
  edges) and the full core suite pass.
- Native: full Gradle build; `run_gates.ps1 -Tag dev20` all 13 gates true (fresh
  world records `channel_revision=2`); `TERRAIN_AUDIT_NATIVE_PASS` fresh and reopen
  (blocks=3069816 chunks=44 water=47088 biomes=704). The previously failing audit
  fixture was repaired: `DepositNativeChecks.generator` strips V7-only
  `material_revision`/`channel_revision` for non-V7 generators.
- Not claimed: deltas, anastomosing channels, oxbows, separately budgeted braid
  discharge, glacial outwash placement, visual acceptance. Tributaries above
  incised trunks become hanging cascades by design. Grade remains 50/100.

Current checkpoint (2026-10-25): **dev.19 Continuous Water Edges is in the Library;
development stopped at the owner's request after finalizing this correction.**
Hourly continuation automation is removed. All changes remain uncommitted on main.
Artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.19-Continuous-Water-Edges.jar`,
125,181,395 bytes; SHA-256
`dbbf7fa87c9d4b94d8cc12fe87f08c808c94bbdd4bc2b254aa4cccabd6480574`.

Resident visible water top boundaries now sample shared continuous height
history at the current frame clock. Updates change only boundary position and
height displacement, not opacity/material clocks, topology, terrain or finite
water. This fixes the shared-edge upload-clock defect reproduced below.
Unchanged edges issue no writes. Rejected writes retain accepted CPU bytes,
warn and recompute next frame; the third consecutive page failure is fatal.
Visible neighbors also expire holds when the last-changing page is hidden.

- [x] Initial candidate live creek: OpenGL 0/121,794 and Vulkan 0/110,781
  above-tolerance shared-corner disagreements, down from 25,256/122,425 and
  27,513/124,581 respectively. Maximum residual 0.000009775162 blocks.
- [x] Final full build/core checks, including hidden-source expiry.
- [x] Final packaged OpenGL/Vulkan: eleven fenced GPU readbacks each, zero
  differing bytes, 28 corner witnesses, continuous shared heights, unchanged
  material/light/opacity/clock bytes, no idle writes, rejected-write recovery,
  third-failure escalation and hidden-source expiry.
- [x] Environment/reopen/source/reserve, three 1,200-tick reaches and six soil
  revision/save gates passed before the final presentation-only expiry change.
  All reaches retain 67/67, 115/115 and 655/655 wet, zero dry-column ticks or
  sampled bank overtopping. No physical hydrology code changed in dev.19.
- [x] Final package: seven archives, 545 compiled classes, 333 resources,
  36 soil blockstates and four identical API/reload captures. Both final API
  clients complete reload, waterfall readbacks and clean shutdown.
- [ ] Hard 2 ms runtime budget: initial component p99 is 2.1028 ms OpenGL /
  2.3944 ms Vulkan; lifetime maxima 13.2385 / 59.3759 ms. Functional correction
  is not performance acceptance. Full-residency/driver-tail work remains.
- [ ] Complete owner-scene flicker, sides, opacity/wet-dry transitions, connected
  waterfalls, other seeds and menu-close acceptance remain open.

Final evidence: `build\dev19-build-final.log`,
`build\gates-dev19-client-{OpenGL,Vulkan}-release.log`,
`build\stream-dev19-package.json`; earlier native gates `build\gates-dev19-*`.
Live initial-candidate evidence:
`build\water-live-dev19-{opengl,vulkan}-captures\water-live-continuity.json`.
A final Vulkan live recheck exited cleanly after camera placement but before
measurement; `build\water-live-dev19-final-vulkan.log` is not a continuity pass.
It does not replace the earlier successful live runs or final isolated GPU
checks. No cause is assigned to that early exit.

Validation-only failed attempts were retained/excluded: a boundary witness
initially included side-wall vertices; another fixture passed a heap buffer
to native upload and Vulkan crashed. The fixture now uses native direct memory
and checks packed top vertices; both final complete clients pass. No production
workaround was added for those fixture errors. Last full grade stays **50/100**;
engine completion and all unaccepted roadmap items remain explicitly open.

Current water checkpoint (2026-10-25): **dev.18 Exact Stopped Water is in the Library.**
125,179,617 bytes; SHA-256
`3bf64519fa70de18bcf3cd7344e0a3db36f9b9c8ca2a733d6606a69e47565ddf`.

Reproduced a dev.15-17 presentation defect: once one-unit oscillation stopped,
identical raw samples returned early forever, leaving the displayed surface at
the pair's midpoint. The 400 ms hold now expires after the last real reversal,
then retargets continuously over 100 ms to the authoritative height. Unchanged
publications cannot extend the hold; stale revisions cannot service or restart
it. GPU page cleanup stays scheduled even while the midpoint is visually still.
This changes presentation only, not water quantities, source supply or terrain.

- [x] Pre-fix regression fails in `build\dev17-settle-repro.log`; full dev.18
  build passes, including 150 exact-release cases across levels, directions and
  cadences, shared-corner duplicates, stale samples and unchanged opacity behavior.
- [x] Packaged OpenGL and Vulkan actual-upload checks: midpoint cleanup at 1501,
  continuous release, exact 0.5-block GPU position/zero displacement at 1602,
  final cleanup zero. Existing dry-fade, rewet and newer-edit guards still pass.
- [x] Follow-up GPU readback, not just the retained upload mirror: three fenced
  copies per API at hold/release/idle match all accepted bytes. Twelve interior
  corner witnesses per API verify target/displacement pairs
  `(0.53125,0)`, `(0.5,512/16384)` and `(0.5,0)`.
- [x] Environment/save/reopen, finite source/reserve, three 1,200-tick reaches,
  material revisions 2/1/0 fresh/reopen, dual-API reload and 60 waterfall
  readbacks per API pass. Reaches retain 67/67, 115/115 and 655/655 wet columns
  every tick, with zero dry-column ticks or sampled bank overtopping.
- [x] Seven archives, 545 compiled classes, 333 root resources, 36 soil
  blockstates and four identical API/reload previews.
- [ ] Actual live-stream displayed-height/edge/wet-dry continuity, connected
  waterfalls and the owner's complete flicker/menu report remain unaccepted.
- [ ] Full-tick storm latency, complete world ledger and physical source
  unload/reopen/edit acceptance remain open.

Evidence: `build\dev18-build.log`, `build\gates-dev18-*`,
`build\gates-dev18-client-{OpenGL,Vulkan}-retry.log`,
`build\stream-dev18-packaged-{opengl,vulkan}-captures\water-fade-continuity.txt`,
`build\stream-dev18-package.json`. First client attempts completed the midpoint
assertions but failed writing a nonexistent fixture output directory; the
fixture now creates it, and both complete retries pass with clean shutdown.
Stronger readback evidence: `build\gates-dev18-client-{OpenGL,Vulkan}-readback.log`
and `build\stream-dev18-readback-{opengl,vulkan}-captures\water-fade-continuity.txt`.
Private readback capability is added only to fixture buffers; distributed
production classes/resources and the supplied JAR are unchanged.
Controlled upload/clock checks are not rendered live-scene acceptance or a
performance claim. Last full grade remains **50/100**; engine incomplete.

### Live shared-edge reproduction after dev.18

- [x] Added private read-only `water-live-continuity-check`: 600 client ticks of
  visible resident top-boundary upload mirrors, decoded with the production
  shader's height/time arithmetic. Cloned disposable high-creek world, seed
  20261020; source x638.953/z1447.820, natural stage 288.25; render/simulation
  distances 8/5, 30-second camera warm-up, no paused ticks.
- [x] OpenGL: 37 sampled pages, 122,425 distinct page-corner comparisons across
  ticks, 25,256 disagreements above one displacement quantum plus epsilon;
  maximum 0.203125 blocks. Same-page duplicate-vertex disagreements: zero.
- [x] Vulkan repeat: 38 pages, 124,581 comparisons, 27,513 disagreements;
  maximum 0.33333302 blocks. Same-page duplicate disagreements: zero.
- [x] Both live clients save/disconnect/shut down cleanly. Vulkan releases
  buffers/images to zero. No production change or replacement JAR in this probe.
- [ ] Repair resident shared-height publication/clock consistency. Shared CPU
  `heights` history alone does not update already uploaded neighbor endpoints;
  `synchronizeCornerNeighbors` currently patches material-direction packets,
  not their height/displacement clocks. Require continuous interpolation,
  stale/edit guards, failed-write recovery and measured bounded work.

Evidence: `build\water-live-dev18-opengl-captures\water-live-continuity.json`,
`build\water-live-dev18-vulkan-repeat-captures\water-live-continuity.json`,
`build\water-live-dev18-{opengl,vulkan-repeat}.log`. The first Vulkan diagnostic
(`build\water-live-dev18-captures`) also reproduced disagreement but counted
duplicate vertices; use the later per-page/corner-deduplicated runs above.
These are running-world CPU upload-mirror observations, not GPU pixel gaps or
a measurement of every frame, sides or opacity transitions. Runs are not
tick/frame matched across APIs; counts are not an API performance comparison.
This is a confirmed acceptance failure, not a passing visual test or a complete
explanation of the owner's flicker. No inflated grade follows from reproduction.

Previous water checkpoint (2026-10-25): **dev.17 Leaner Weather Sampling is in the Library.**
125,179,328 bytes; SHA-256
`f78c983ab7fe5b513fb79aa31e383c3417a85d162e6c41b358e7a6330d97b811`.

Removed redundant full terrain/strata/deposit evaluation from representative
precipitation. Soil and sealed surfaces use their existing material rules;
rock/ice query the exact province/fault permeability subset. Ordinary hydrology
visits use the subset too, retaining their required planning/climate sample.
Saved geography, sample weights, chemistry, cadence and source limits are unchanged.

- [x] `GEOLOGY_PERMEABILITY_SUBSET_PASS`: 40,000 bit-exact oracle/full-column
  comparisons across four seeds, negative/distant coordinates and all four
  province/fault permeability values.
- [x] Same-world packaged dev.16/dev.17 four-region component comparison, repeated
  twice: 64 loaded centers, 100 warm-up and 200 measured batches per scenario.
  Exact recharge/constituents retained for all 36 soil forms, vanilla soil
  controls, nine rocks, four ice/snow blocks and glass.
- [x] Repeated batch p50: soil 0.3752 -> 0.1347 ms; rock/ice 0.3655 -> 0.0746 ms;
  glass 0.2850 -> 0.0287 ms. See architecture for p95, method and host identity.
- [x] Full build; packaged environment/save/reopen/source/reserve/three reaches,
  OpenGL/Vulkan and six saved-soil gates. The first six soil attempts failed on
  a test-only reflective signature; the updated null-noise soil fixture passes
  all six on retry (`build\gates-dev17-soil*-retry.log`).
- [x] Package identity: seven archives, 545 compiled classes, 333 root resources,
  36 soil blockstates and four API/reload previews with zero differing pixels.
- [ ] Full-tick many-region storm latency, scheduling/contention and cold regions.
- [ ] Complete world ledger, physical source unload/reopen/edit and live-stream
  temporal mesh acceptance, including the owner's unresolved flicker/menu report.

Evidence: `build\dev17-build.log`, `build\storm-dev16-baseline.json`,
`build\storm-dev17-candidate.json`, `build\storm-dev{16,17}-repeat.json`,
`build\gates-dev17-*`, `build\stream-dev17-package.json`.
The benchmark explicitly enables and completes disposable fixture skylight before
timing, and excludes setup/generation/lighting. It is not full server tick
latency, player-scene performance or a hard 2 ms guarantee. Last full grade
remains 50/100. Older checkpoint statements below are historical.

Current water checkpoint (2026-10-25): **dev.16 Fair Rain/Snow JAR verified and in the Library.**
125,179,290 bytes; SHA-256
`306599c5004cd0790a9ba562c57a5225d48684ec332140872483ac6f1fc4927c`.
Dev.12-15 are earlier delivered checkpoints; the following dev.11 narrative is
historical. Its original artifact was hash-verified and added to the Library too.

The dev.15 packaged runtime reproduced rain-first atmospheric starvation in a
controlled mixed jungle/snowy-plains region: with one quantum available and ten
quanta of groundwater room, rain took one and snow took zero, whereas the
capacity-limited proportional share was rain zero, snow one.
`WaterAccount.distribute` now allocates the finite available supply between
capacity-limited rain/snow demands in one preflighted transfer. Rounding gives
snow less than one extra quantum. Chemistry rejection leaves all accounts intact.
No terrain, saved-state format, quota, recharge cadence, owner PNG or fluid
transport change is included in dev.16.

- [x] Mixed-biome precipitation respects finite supply and destination capacity:
  `ENVIRONMENT_MIXED_PRECIPITATION_PASS cases=24`, swapping warm/cold samples,
  atmosphere 0/1/100/100000 and groundwater room 0/10/full.
- [x] Independent integer-oracle core checks: 10,000 randomized splits including
  overflow-safe chemistry, exact totals and atomic rejection.
- [x] Full build; packaged environment fresh/reopen, source/reserve, three
  1,200-tick reaches, soil revisions 2/1/0 fresh/reopen, OpenGL and Vulkan gates.
- [x] Package checks: seven archives, 545 compiled classes, 333 root resources,
  36 soil blockstates; four API/reload previews with zero differing pixels.
- [ ] Many-region storm tick-latency acceptance and full catchment ledger.
- [ ] Live-stream displayed-height/edge continuity, physical source unload/reopen
  and the owner's intermittent menu-close reproduction.

Evidence: `build\env-dev15-mixed-repro2-fresh.log`, `build\dev16-build.log`,
`build\env-dev16-{fresh,reopen}.txt`, `build\env-dev16-fresh.log`,
`build\gates-dev16-*`, `build\stream-dev16-package.json`.
These are scoped functional checks, not storm performance, complete worldgen,
owner-scene flicker or full visual acceptance. Last full grade remains 50/100.

Current water checkpoint (2026-10-25): **dev.11 Spatial Recharge JAR verified and staged.**
The Library-upload contribution was unavailable in this run; dev.10 remains the
latest Library item. Dev.11 is retained in this session's files folder.

Native pre-fix reproduction: identical grass/sand regional fixtures received
49,152 quanta when grass was visited first versus 88,473 with sand first.
`HydrologyRuntime.precipitation` now sums one fixed chunk-center sample per
currently loaded chunk, at most 16 per 64x64 region. Each represents 256 columns
and uses its own live surface/bed permeability and precipitation biome. Ocean
boundaries and impermeable roofs contribute nothing. Only already-loaded chunks
are examined; rain and snow debit the finite atmosphere into their respective
regional accounts. Existing quotas, STOP protection and saved terrain are unchanged.

Rain is now a regional snapshot once per authorized environmental interval, even
when all samples are sealed. Opening a sampled roof after that snapshot takes
effect next interval; it cannot trigger another scan/payment in the same interval.
This replaces dev.10's later-eligible-visit behavior with spatially ordered sampling.
Runtime-cache reconstruction remains fail-closed.

Native post-fix `ENVIRONMENT_SPATIAL_RAIN_PASS`: both visit orders yield exactly
22,937 quanta (one grass sample plus one sand sample), sealed samples excluded,
exact atmospheric debit, 100-quanta limited-atmosphere case and absent negative
region without forced loading. `ENVIRONMENT_SEALED_REGION_PASS` retains drought
clock/no-seeding/repeat-visit/cache-rebuild and exact constituent checks.

All candidate gates passed:
- Full Windows build and core 180,000 weather intervals.
- Environmental fresh/reopen and native source/drought reserve.
- Three 1,200-tick REQUIRE reaches: 67/67, 115/115, 655/655 wet every tick,
  zero dry-column ticks, no newly wet or sampled overtopped columns. Final falls
  0.1875/0/0.0625; these do not establish a startup-dip improvement.
- Soil material1/legacy0 fresh/reopen; OpenGL/Vulkan resource reload and
  60 waterfall readbacks each; Vulkan buffers=0/images=0.
- Seven archives, 545 compiled classes, 333 resources, 36 soil blockstates and
  four pixel-identical previews.

Evidence: `build/stream-dev11-spatial-before-fresh.log`,
`build/stream-dev11-build-final.log`, `build/stream-dev11-environment-{fresh,reopen}.*`,
`build/stream-dev11-source-native.log`, `build/stream-dev11-{high,low,wide}.log`,
`build/stream-dev11-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev11-final-{opengl,vulkan}.log`, `build/stream-dev11-package.json`.
Artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.11-Spatial-Recharge.jar`,
125,177,114 bytes, SHA-256
`10b1839c697861de3051cb55c953835d60fd1bf6e5799c2a30afb5b618a375e5`.

Limits: one representative column per chunk is a coarse spatial estimate, not
exact roof/soil area integration. End-to-end storm tick latency across many
regions, mixed rain/snow capacity fairness, additional seeds, complete world
ledger, physical source unload/reopen and temporal flicker remain open.
Last full grade **50/100**; no branch/commit/push or owner-art edits.

Previous water checkpoint (2026-10-25): **dev.10 Regional Weather Clock JAR supplied in the Library.**
Reproduced `Impermeable surface froze the existing regional drought clock` through
the actual dedicated-server `HydrologyRuntime.visit` path before the fix.
An early permeability return skipped `Aquifer.step`, regional discharge allowance
renewal, snowmelt and circulation for an existing aquifer under sealed ground.

Regional updates now advance for an existing aquifer even on an impermeable
column. Sealed ground still cannot initialize a new aquifer, recharge groundwater
or become an outlet. Rain has a separate per-regional-step allowance: a sealed
first visit does not consume a later permeable column's precipitation opportunity.
Repeated visits and runtime-cache reconstruction cannot grant a second rain
allowance in that step. No offline catch-up, reserve or geography change.

Executed evidence:
- Before: `build/stream-dev10-sealed-before-fresh.log` reports the clock assertion.
- After: `ENVIRONMENT_SEALED_REGION_PASS` checks drought advancement, no rain
  through bedrock, no initialization, later rain exactly once, cache rebuild and
  exact water/salt/sediment accounting across 16 repeated visits.
- Environmental fresh/reopen, native source/drought reserve checks, core
  180,000-interval weather tests and full Windows build pass.
- Fresh 1,200-tick REQUIRE reaches retain high 67/67, low 115/115, wide 655/655
  wet every tick, zero dry-column ticks, no newly wet or sampled overtopped columns.
  Final falls: 0.125, 0, 0.0625. High rise 0.25, wide rise 0.3125 remain diagnostics;
  this is a regional-clock fix, not an established startup-dip improvement.
- Soil material1/legacy0 fresh/reopen and OpenGL/Vulkan inventory/reload pass.
  Each API passes 60 waterfall readbacks; Vulkan releases all buffers/images.
  Package: seven archives, 545 classes, 333 resources, 36 soil blockstates,
  four pixel-identical previews.

Logs: `build/stream-dev10-build.log`,
`build/stream-dev10-environment-final-{fresh,reopen}.{log,txt}`,
`build/stream-dev10-source-native.log`, `build/stream-dev10-{high,low,wide}.log`,
`build/stream-dev10-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev10-final-{opengl,vulkan}.log`, `build/stream-dev10-package.json`.
Artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.10-Regional-Weather-Clock.jar`,
125,176,661 bytes, SHA-256
`2a6b0872e19c3d67a070ded6298c205753143b9ad077236a7cb46b89adbff64d`.

Open: precipitation still extrapolates one eligible column's permeability/biome
to the loaded region. Spatially representative native catchment weather,
complete world ledger, physical source unload/reopen and visible flicker are
not accepted by these fixtures. Last full grade stays **50/100**.
Local main retained; no branch, commit, push or owner-art change.

Previous water checkpoint (2026-10-25): **dev.9 Drought Reserve JAR supplied in the Library.**
Reproduced a drought-boundary defect before editing production code:
`stream-dev9-reserve-before.log` fails with `remaining=0 units=5`. A drained outlet
below planned stage still has positive pressure when groundwater reaches STOP,
and the active latch only updates on the next environmental step. Several outlets
could therefore spend the drought reserve within the same interval.

`Aquifer.dischargeUnits` now limits every delivery to whole native units above
STOP, and `canSupply` checks the current reserve immediately. This protects both
natural and dry-land outlets without changing saved state, head law, quotas,
terrain or the 30-step reactivation requirement. Evaporation may still deplete
the inactive reserve; the fix limits outlet discharge, not every environmental loss.

Executed on the dev.9 candidate:
- Before/after reserve regression: six balances from STOP through STOP+768 quanta,
  depressed outlets, repeated delivery without a step and sub-unit remainders.
- `AQUIFER_WEATHER_CYCLE_PASS`: six scenarios, 180,000 simulated 20-tick intervals,
  rain rates 0/64/4096, loaded areas 256/4096 columns, rain/snow/melt, long drought,
  six drought stops, four storm restarts and 186 state reconstructions. Exact
  water/salt/sediment totals checked each interval, with interval quota/debit checks.
- `STREAM_DROUGHT_RESERVE_NATIVE_PASS`: six balances, 16 actual block-write attempts
  per balance, exact water/salt/sediment accounting and no between-step overspend.
  Existing `STREAM_SOURCE_NATIVE_PASS` also passes.
- High creek extended to 5,000 ticks: 67/67 wet each tick, zero dry-column ticks,
  no newly wet or sampled overtopped columns; final fall 0.125, rise 0.25,
  aquifer final fill 0.4062. Sampled startup peak fall 0.6875 is not a demonstrated
  improvement over dev.8's 0.625; this fix addresses drought correctness.
- Low/wide 1,200-tick reaches: 115/115 and 655/655 wet each tick, no sampled
  overtopping, final falls 0 and 0.0625.
- Environmental fresh/reopen, soil material1/legacy0 fresh/reopen, full build,
  OpenGL/Vulkan clients and 60 waterfall readbacks per API pass. Vulkan releases
  all buffers/images. Seven archives, 545 compiled classes, 333 resources,
  36 soil blockstates and four pixel-identical previews verified.

Evidence: `build/stream-dev9-{reserve-before,core,build,source-native,high,low,wide}.log`,
`build/stream-dev9-environment-{fresh,reopen}.*`,
`build/stream-dev9-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev9-final-{opengl,vulkan}.log`, `build/stream-dev9-package.json`.
Artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.9-Drought-Reserve.jar`,
125,176,563 bytes, SHA-256
`281c0122763b54e75f839f2a2f4a2ec78fdeaeab8d503c90e88f0159ff18dcf2`.

Scope limits: weather-cycle checks exercise the real core accounts, head and
drought rules, not native biome/sky/permeability sampling or a complete world
ledger. Full-catchment native storms/droughts, additional seeds, physical
unload/reopen source continuity and temporal flicker remain open. Last full
grade stays **50/100**. No branch, commit, push or owner-art edit.

Previous water checkpoint (2026-10-25): **dev.8 Headwater Baseflow JAR supplied in the Library.**
Dev.7 reach diagnostics showed that the high creek's remaining startup dip came from the
32-unit regional quota. It saturated on 776 of 1,180 ticks, while the source pool's own head
pressure and stage capacity could accept more. Two changes follow:

1. Planned waterways now draw from a separate regional natural quota,
   `Aquifer.baseflowUnits(rainQuantaPerColumnSecond)`. This is the long-run mean rain recharge
   of a 64x64 region: vanilla mean weather duty is 18,000 rain ticks of 114,000, at half
   permeability. That gives 80 units per 20-tick interval at the default 64 quanta, with a floor
   of 32.
   - It is still paid from the same finite aquifer. Head pressure, drought shutdown and the
     fractional stage cap still apply.
   - Dry-land springs keep the separate 32-unit quota and `sourceRate`.
   - The natural quota fails closed until the first aquifer step.
2. Planned columns are kept headwater-first by planned surface, and the maintenance pass offers
   quota to the highest chunks first. This ordering alone did not measurably change the dip
   (peak 0.875 vs 0.94). It is kept as the physically ordered allocation.

The unordered main-scan top-up was removed. Fresh packaged REQUIRE runs over 1,200 ticks:

| Reach | Wet columns kept | Peak fall | Final fall | Above plan | Quota saturation |
|-------|------------------|-----------|------------|------------|------------------|
| High creek | 67/67 | 0.625 (dev.7: 0.94) | 0.1875 (dev.7: 0.3125) | up to 0.3125 | 20 fail-closed ticks only; 1,161 units |
| Low creek | 115/115 | 0.125 | 0 | 0.125 | — |
| Wide river | 655/655 | — | 0.0625 | 0.3125 | — |

- Every reach had zero dry-column ticks and no newly wet or overtopped columns.
- The high creek now sits up to 0.3125 above plan. This is surface levelling between neighbouring
  columns of different planned stage within the same block, with no bank overtopping.
- The low and wide results are unchanged from dev.7.

Other gates on this JAR:
- Core `NATURAL_BASEFLOW_QUOTA_PASS`.
- Native source check, including `headwaterFirst=true` and the exact quota boundary.
- Environmental fresh/reopen.
- Soil material1/legacy0 fresh/reopen.
- OpenGL and Vulkan clients with 60 waterfall readbacks each; Vulkan reported buffers=0 images=0.
- Package checks: 7 archives, 545 classes, 333 resources, 36 soil blockstates and 4
  pixel-identical captures.

Evidence: `build/stream-dev8-{high,high2,low,wide}.log`, `build/stream-dev8-source-native.log`,
`build/stream-dev8-environment-{fresh,reopen}.*`, `build/stream-dev8-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev8-final-{opengl,vulkan}.log`, `build/stream-dev8-build2.log`, `build/stream-dev8-package.json`.

Supplied artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.8-Headwater-Baseflow.jar`, 125,176,558 bytes,
SHA-256 `eaf29368806cf81ee0c888d108741d733213fa1c180df804510365dc86708afb`.

Open:
- Long-run aquifer trajectory under the higher natural quota: sustained drought and storm runs.
- Broader seeds and catchments.
- A complete ledger.
- Unload/reopen and player edits.
- Visible flicker acceptance.

No branch, commit or push was made. Last full grade remains **50/100**.

Previous water checkpoint (2026-10-25): **dev.7 Steady Stream Sources JAR supplied in the Library.**
Dev.6 diagnostics on the high creek near `(639,1448)` showed the 1.0625-block
source-pool loss was not quota or reserve exhaustion. Only 20 of 1,180 ticks were
quota-saturated (the fail-closed start interval); the aquifer stayed about 57% full
and could always supply. The broad scan covered 49 loaded chunks, so each source
column got a 4-unit top-up into its top outlet cell about every 196 ticks.

Planned waterway columns now have a compact per-chunk list. A deadline-bounded
survey of visible engine water after chunk load fills that list, along with ordinary
visits. Each listed column gets at most one top-up per 20-tick interval, with a
`Long.MIN_VALUE` sentinel so game time 0 cannot allow repeats. Delivery targets
the real deficit cell up to two blocks below the outlet. That cell is wholly below
the planned stage. Natural delivery is limited by head pressure, fractional
capacity, finite groundwater and the unchanged 32-unit regional quota.
`sourceRate` still meters dry-land springs. There is no new reservoir or infinite refill.

Fresh packaged REQUIRE runs, 1,200 ticks: high 67/67, low 115/115 and wide 655/655
wet on every tick, with zero dry-column ticks and no newly wet or overtopped
monitored dry columns. Final high-creek fall is 0.3125 blocks (dev.6: 1.0625).
The source region's quota now saturates for 776 ticks, and it delivered 1,691 units
over 58 intervals (previously 1,229 units). Peak fall is still about 0.94 blocks
around ticks 300–500 before recovery, so the startup transient remains open.
Low and wide final falls are 0 and 0.0625.
The wide reach stands up to 0.3125 above per-column plan from tick 100, while its
flow solver is idle. Dev.6 showed 0.25, which indicates level-basin generation
quantization, not the new supply; it remains a diagnostic, not a gate.
Native source check adds exact deficit-cell repayment (8 to 16 units) and rejects
same-interval repeat top-ups. Environmental fresh/reopen, material1/legacy0
fresh/reopen, OpenGL/Vulkan inventory/reload plus 60 waterfall readbacks each,
Vulkan buffers=0 images=0, full 35-task build and package checks all pass.
The checks cover seven archives, 545 classes, 333 resources and four
pixel-identical previews.
Evidence: `build/stream-dev7-{high4,low,wide2}.log`, `build/stream-dev7-source-native3.log`,
`build/stream-dev7-environment-{fresh,reopen}.{log,txt}`, `build/stream-dev7-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev7-final-{opengl,vulkan,build}.log`, `build/stream-dev7-package.json`.

Supplied artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.7-Steady-Stream-Sources.jar`,
125,175,702 bytes, SHA-256
`8a85abdc2c6a374b170f2a2066cdf3dcc7462d6331a3f97f9ab50f6c0f1bfbf2`.
Dev.6/dev.5 remain separately retained. No branch, commit, push or owner PNG edit.
Open: startup transient, quota sizing by planned discharge, broader seeds/catchments,
complete ledger, weather, unload/reopen/edits and visible flicker acceptance.
`sourceRate` config comment wording is deferred to dev.8 to keep source identical to
the tested JAR. Last full grade remains **50/100**.

Previous water checkpoint (2026-10-25): **dev.6 Natural Stream Head JAR supplied in the Library.**
A live nine-chunk V7 creek near `(639,1448)`, stage 288.25, reproduced drainage
with the sealed dev.5 JAR: 67 initially wet monitored columns fell to 40 after
1,200 server ticks. At 64 columns, an active regional aquifer could not reach the
planned outlet because its head used the minimum of sixteen terrain samples
across a 64x64 region. This is evidence for one draining cause, not a diagnosis
of every screenshot or temporal artifact.

The candidate gives planned natural waterways a local perched-head reference:
their original planned surface is the head at the aquifer's drought-stop reserve.
The same reference now governs infiltration and source delivery, avoiding an
artificial low-region drain/replenish cycle. Unplanned dry-land springs keep the
regional datum and sustained-recharge rule. Saved terrain/initial water,
composition, finite groundwater, 32-unit regional discharge quota and fractional
surface cap are unchanged. Supply still falls with depletion and stops; this is
not an infinite source or a promised permanent river.

Full 35-task build passes. The packaged high-altitude source check verifies exact
10-unit debit, no overfill/repeat debit, regional quota, dam/player edits, drought
and sustained-recharge restart at Y288. Environmental rain/infiltration/chemistry/
ocean/no-force-load and separate-process ledger/edit reopen checks pass. Their
old private `visit` reflection signature was stale and is corrected; an initial
stdin `/stop` attempt hit a host command-source error and was stopped/excluded.
The isolated fixture now has an explicit clean-stop flag.

Fresh packaged runs now check every server tick, not just 100-tick snapshots.
Over 1,200 ticks, high/low creeks and a wider river retain 67/67, 115/115 and
655/655 initially wet columns respectively, with zero dry-column ticks.
The wider reach has planned radius 8.168. At every 100-tick bank observation,
all three have zero newly wet monitored dry columns or overtopping. Maximum
final stage loss is still 1.0625 blocks in the high creek, 0.3125 in the fresh
low-creek run and 0.125 in the wider river. An earlier low-creek run lost 0.125;
these bounded runs do not establish identical temporal behavior.
Evidence: `build/stream-dev6-continuity-{highest,first,widest}.log`.

The final full 35-task Windows build passes, followed by selected-JAR native
material1/legacy0 fresh/reopen checks and both OpenGL/Vulkan inventory/reload
checks. Each API also passes 60 production waterfall upload/readback cases,
not an actual connected falling-water scene. Vulkan releases all tracked buffers/
images. Seven recursive archive CRCs, 545 compiled production class identities
and 333 root resources match; four 1200x800 previews are pixel-identical.
Final evidence: `build/stream-dev6-final-build.log`,
`build/stream-dev6-soil-{0,1}-{fresh,reopen}.log`,
`build/stream-dev6-final-{opengl,vulkan}.log` and
`build/stream-dev6-package.json`.

Supplied artifact: `DeepPrimal-Windows-Vulkan-alpha.29-dev.6-Natural-Stream-Head.jar`,
125,173,983 bytes, SHA-256
`443acc2a1c8b43679d9a7c4e39f7c5a6b39413bd5f9c5e68f859c8cb8d61f555`.
Dev.5/dev.4 remain separately retained. No branch, commit, push or owner PNG edit.
Full stage preservation, larger catchments/other seeds, sustained depletion/
weather, complete budget accounting, long-duration transport and visible temporal/
flicker acceptance remain required. A wet-column count is not complete stage or
conservation acceptance. Last full grade remains **50/100**, not a new water grade.

Current soil checkpoint (2026-10-25): **dev.5 Windows Vulkan development JAR supplied.**
All twelve USDA texture families now have bare/grass/podzol forms (36 blocks/items),
generated models/loot/names/tags and family mineral/infiltration values. The owner
PNGs are unchanged. New Standard/Deep worlds save material revision 1; actual old
worlds remain revision 0. The full 35-task Windows build passes, followed by
selected-JAR native material1/legacy fresh/reopen and OpenGL/Vulkan inventory/reload
checks. Each native soil comparison covers 1,572,864 blocks; 3,749 material changes
preserve occupancy/heights, with 539 separately counted unchanged decorations.
All 39 placed identities reopen and all 36 soil permeability routes match their
family. All twelve families occur on selectable dry ground across 12,675 actual
V7 columns/three seeds; the initial narrower field failed this gate and was replaced
before packaging. Seven recursive archive CRCs, 545 compiled production classes and
333 root resources match; four 1200x800 previews are pixel-identical. Artifact:
125,173,842 bytes, SHA-256
`c9675e958e7fa66cd6ca28c2af14d006303f912d9384c491615e073bf45acc55`.
This is not complete soil ecology: palettes/infiltration remain provisional;
orders/horizons/modifiers, wet/organic behavior, farming and granular simulation
are open. Last full engine grade remains **50/100**, not a new partial-feature grade.

Owner screenshot follow-up: pronounced diagonal/stepped water bands are recorded
as an **open visual defect**, not diagnosed as flooding from a still image.
A controlled stepped-bed mesher check produces 6,144 flat free-surface vertices
across signed sections and both smooth modes, with no internal faces; it does not
reproduce the screenshot's cause. Selected-JAR Vulkan captures now cover checker
quantities, a cross-layer slope and a stepped basin in both animated/fallback
modes. The level basin visibly retains bed-step bands beneath a flat water surface;
this establishes a controlled lookalike, not the diagnosis of the owner's world
or a solver/flicker fix. Initial launch attempts were stopped/excluded: one lacked
the parent fixture flag; subsequent diagnostics identified the first-run
AccessibilityOnboardingScreen. The fixture now explicitly accepts its Continue
button. Final run saves/stops and releases tracked GPU buffers/images.
`build/water-bands-vulkan-4.log` and its six captures are the accepted evidence.
Do not alter saved terrain,
transparent-water visibility or finite quantities speculatively.

Current continuation (2026-10-25): **dev.4 development JAR supplied; full engine acceptance remains open.** Owner v3 side art and final sand are preserved. Dual-tint materials now render opaque sides through the actual inventory shader pair as well as terrain/block/entity paths. OpenGL and Vulkan inventory/reload previews pass opaque-face pixel assertions. The previously observed Vulkan GUI failure was secondary to converter rejection of bitwise-complement syntax and an unmatched NeoForge unlit vertex shader interface; both source contracts are corrected. Stacked side-wall clipping preserves uncapped shared free surfaces. Each API passes 60 production GPU upload/readbacks covering repeated quantities, adjacent free surfaces, smooth modes and signed vertical pages, not a full waterfall scene or drainage/flooding fix. Full Windows build/current-art hashes and selected-JAR native fresh/reopen/client gates pass. The 7 archive CRCs, 141 root/399 nested classes and 186 resources match compiled outputs. All four inventory/reload captures match exactly across OpenGL/Vulkan. Vulkan releases tracked buffers/images; operation counters are not full validation-layer coverage. World lighting/AO/mips, soil catalog placement, native V8/save/hydrology and target performance remain open; **last full grade remains 50/100**. See [current material contract](art/ground-textures-v2/README.md) and [the owner's new renderer reference](LOD-AND-DRAINAGE-ARCHITECTURE.md#planet-smith-rendering-reference).

Previous local continuation (2026-10-24): **alpha.29-dev.3; development resumed by owner on main, not paused.** Owner texture update `5c54904` is fast-forwarded without overwriting existing gradient work. Three opt-in ground material samples and a strict versioned surface profile now connect the ordered gradient to the isolated V8 native preview. The full 33-task Windows Vulkan build passes. Selected-JAR native fresh/reopen runs verify material drops/tags/identities and 1,572,864 block/reverse-order comparisons per run; stored graph/surface snapshots replay identical block-name fingerprints across two seeds. Packaged OpenGL and Vulkan model/inventory/reload previews pass, with pixel-identical 1200x800 captures before/after reload and between APIs; Vulkan reports zero validation errors and tracked resources at shutdown. This is bounded material-preview acceptance, not V8 gameplay, terrain-world visual/performance acceptance or complete worldgen. Side-mask dual tint, actual soil/ecological profiles, saved generator integration and coupled hydrology remain open. See [material limits and evidence](art/ground-textures-v2/README.md) and [surface preview](engine-stage2-2026-10-24/README.md#ground-surface-preview). The development JAR is supplied; **the last full engine grade remains 50/100, not regraded**.

Earlier continuation (owner clarification, 2026-10-24): **minimum acceptance target average ≥8.5/10 across all ten equally weighted categories; ideal stretch target ≥9.5/10; alpha.29-dev.2 partial stage 2**. A 10/10 is an aspirational perfect score, not a promise. The previous 8.0 target is superseded. Score only implemented and verified behavior; do not average away a weak category or credit unintegrated previews. The existing hourly build/audit task remains enabled but does not itself complete this goal. Last verified full grade remains50/100. Read [stage-two scope](engine-stage2-2026-10-24/README.md), [executed results](engine-stage2-2026-10-24/RESULTS.md) and ENGINE-WORK-STATE.json. Bounded density programs, strict JSON profiles and an experimental V8 shape recipe pass full33-task build,40987 graph checks,34 codec cases, independent/package identities and native V7 replay/save/stop. All509 prior production class files are unchanged. No V8 registration or owner installer. Exact component evaluation1.06–1.14s/full volume, lattice17.67–18.88ms and906 occupancy disagreements block promotion. Next optimize repeated expressions and prove conservative topology refinement, then native V8 block/base-column/heightmap/saved-profile/hydrology/visual gates. Stages3–6 and latest-version support stay open. Freeze V1–V7, preserve unpromoted R57 and all historical requirements. A checkpoint does not finish the goal; report only actual progress and do not imply continuous activity between finite scheduled runs.


Current Builder/Grader checkpoint (2026-10-24): **alpha.29-dev stage 1; alpha.28 remains the delivered preview.** Read [the strict review](engine-review-2026-10-24/REVIEW.md), [architecture](engine-review-2026-10-24/ARCHITECTURE.md), [stage results](engine-review-2026-10-24/STAGE-1.md) and ENGINE-WORK-STATE.json. Current engine grade is **50/100, Tier 2/5** under the owner's explicit rubric. The new immutable 3D noise/grid library and strict codec pass the full 33-task build, independent scalar/seam/parallel tests, exact salt round-trips, same-host JVM fingerprints and packaged-native save/stop checks. All 504 existing production classes remain byte-identical to alpha.28; no V8 registration or new owner installer. Released warm native fill means are 14.45–14.99 ms excluding lighting/IO/biome/status costs, so the <5 ms goal is unmet. JFR points to block writes, heightmaps and ore evaluation as well as field work. Stages 2–6, current-version migration, cross-machine/long-duration/performance and visual acceptance remain open. Next implement the bounded V8 density stage under the recorded gates. Preserve all frozen geography and unpromoted R57 LOD work. Current pressure code retains 128-node turns up to 8,192 nodes/job and 16 jobs; older descriptions of a permanently restarted 128-cell pressure search below are historical.


Current foreground release (2026-10-24): **alpha.28 / V7 terrain**. Read [the V7 scope](TERRAIN-V7.md), [release validation](VALIDATION-0.0.1-alpha.28.md) and ENGINE-WORK-STATE.json. Reproduced thin-water bed clipping and seed-origin aliasing are fixed; new V7 worlds add broader channels, regional landforms and biome-based surfaces/canopy. Freeze delivered V1–V7 geography. Standard/Deep/native legacy save-reload, actual UI seed binding, matched Vulkan beds and OpenGL smooth fallback pass. Full engine, broad live water/LOD/transparency, recovery-marker root cause and Windows/owner-GPU acceptance remain open. R57 LOD staging is preserved and unpromoted. The packaged-validation classpath filter and loaded-class identity guard are mandatory for future selected-JAR comparisons.

Current continuation (2026-10-24): use **main** after branch consolidation. R44 retirement source is integrated experimentally; its589-input snapshot is historical engine-only evidence and must not overwrite merged source. Read the R44/reconciliation entry below and ENGINE-WORK-STATE.json. Full engine, hard2ms and Windows/ownerGPU acceptance remain open.

Alpha.24 development installers have been built, checked and supplied. The default is 606,553 bytes; optional Windows Vulkan is 11,537,948 bytes. V3 geography is now frozen. The full engine acceptance remains open.

Read [alpha.24 validation](VALIDATION-0.0.1-alpha.24.md), [staged native evidence](validation/0.0.1-alpha.24/native-evidence.txt) and [installer audit](validation/0.0.1-alpha.24/installer-audit.json) for concrete changes, failures and checks. Fresh evidence includes the nine-seed watershed ensemble, all 33 regional estuaries, 1,000-tick loaded-basin retention, actual world creation/reopen, both graphics APIs, settings restarts, upright inventory captures and a forced Vulkan shutdown-completion failure with zero final native leaks/errors. The final grouping audit removes seven false ocean/lake classifications.

The execution workspace reset around 04:04 UTC. Source and tests were recovered and checkpointed on `audit/alpha24-recovery`; fresh results are distinguished from historical pre-reset measurements. A truncated generated archive and a genuine rejected-callback cleanup failure were both caught before delivery and corrected.

Continue from the source ref in [ENGINE-WORK-STATE.json](ENGINE-WORK-STATE.json), following [the handoff](ENGINE-BUILD-AUDIT-HANDOFF.md). The next priorities are enabled-view distant-water/LOD pass isolation, persistent bounded pressure searches, explicit regional water budgets, terrain/geology variety and LOD/transparency coverage. Catalog/species work remains separately deferred. Current prototypes, three substeps per host tick and small software-driver fixtures do not establish a finished engine, independent 60 Hz simulation, guaranteed 60 FPS or superiority over reference projects.

Hourly work uses an isolated directory after the foreground lease ends, checkpoints reviewable changes and reports actual tests/failures. It is scheduled execution, not a continuous daemon between runs.

## 2026-10-22 isolated continuation: page admission

A source-extracted Java regression reproduces farther discoveries evicting closer resident animated-water pages at the cache limit. The unreleased working-branch fix supplies incoming-page distance and rejects lower/equal-priority replacement, including uploads whose farther alternatives are busy. The scheduler fixture now passes 513 overflow discoveries, byte-budget rejection/recovery and 2,000 randomized victim selections. See [reproduction, evidence and limits](validation/engine-continuation/2026-10-22-page-admission.md).

Only extracted production scheduler logic was compiled/executed in this continuation. Workspace maintenance removed the JDK 25/Gradle/native environment; full build/core/native/visual/performance checks remain pending. This is not acceptance of enabled-view coverage or mixed transparency, and no new installer was built. Main and delivered alpha.24 bytes/geography remain unchanged.

## 2026-10-22 continuation: restored JDK25 core and pressure reproduction

Restored and Git-blob-verified 370 default-build inputs plus JDK 25/Gradle 9.2.1 in an isolated directory. All 54 dependency-free core/check classes compile and CoreChecks passes, including the nine-seed/frozen-geography suite. The page-admission scheduler fixture also passes again under JDK 25. Full default Gradle build attempts fail before compilation with Java network-unreachable errors resolving plugins; native/visual/performance tests remain unrun.

A new fixture executes the actual pressure-search body against a sealed in-memory 262-cell U-bend. It visits 128 cells, transfers nothing and queues no continuation; 100 external restarts repeat the prefix. A short control moves four units conservatively. The `--expect-fixed` acceptance mode deliberately fails on the current source. This is an algorithm reproduction using world doubles, not native Minecraft acceptance, and no production water/runtime code was changed. See [fresh evidence and next gates](validation/engine-continuation/2026-10-22-restoration-and-pressure.md). No installer was produced.

## 2026-10-22 continuation: full build restored and native cutoff confirmed

The fresh default non-Vulkan Gradle build now passes, including all CoreChecks and module-boundary checks. Optional current-proxy development tooling and a narrowly pinned private NeoForm process-command fallback resolve the previous Java-network/build-host blockers. Neither is included in the mod. A private Java 21 installation also restores the asset task. All 370 existing recovered source inputs still match their recorded Git blobs.

A new validation-only native fixture confirms the actual long-pressure defect: the short U-bend control rises and conserves 144 units; the 262-cell bend visits 128 cells on its first search and remains unequal after 120 server ticks, conserving all 4,192 units. Explicit records and normal save/shutdown were inspected. Reproduction succeeds; solver acceptance remains false. No production water/runtime code was changed in this continuation. See [fresh build, failures, native evidence and limits](validation/engine-continuation/2026-10-22-build-access-and-native-pressure.md).

An alpha.25-dev default archive passes outer/four nested CRC checks but has not passed release acceptance and was not supplied as an installer. Main and delivered alpha.24 bytes remain unchanged. No fresh visual/Vulkan/renderer-performance checks ran; historical graphics evidence stays separate. Next implementation is fair bounded persistent pressure/drain continuation with edit/unload invalidation and conservation checks. Enabled-view coverage, water budgets, geology and LOD acceptance remain open.

## 2026-10-22 continuation: retained pressure candidate; performance promotion blocked

The unreleased working branch now retains pressure frontiers across bounded 128-node turns, with FIFO resident-job scheduling, explicit memory limits, actual chunk-write invalidation and chunk-unload cancellation. Two independent native 262-cell U-bends equalize while conserving 4,192 units each. Silent wall edits, actual unload/no-force-load, isolated idle, separate-process reopen, existing pressure/filling and the final non-Vulkan build/core/module checks pass. No geography or renderer source changed. See [source, native evidence and limits](validation/engine-continuation/2026-10-22-pressure-continuation.md).

**Do not promote this candidate to main or release an installer.** A fresh same-fixture 1,000-tick/49-chunk basin pair passes retention on both revisions, but the candidate costs 17.092/27.239 ms median/p95 versus 15.595/24.754 ms baseline, with 7,093 pending jobs versus 3,676. Complete settling and performance acceptance remain unproven. These are fresh container measurements, not owner GPU/FPS guarantees. The first unload fixture also generated a real assertion/crash despite Gradle exit zero; its corrected gap probe then passed and both outcomes are recorded.

Next work is profiling/coalescing overlapping searches and restart/admission churn, retaining the new safety/progress regressions, before promotion. The 16-job/8,192-node residency boundary and unchanged short drain search remain incomplete. Enabled-view water/LOD coverage, transparency, regional water budgets, geology and broad terrain quality stay open; no new visual/Vulkan/GPU checks ran. Main, delivered alpha.24 bytes and frozen V3 geography remain unchanged. Catalog/species work remains deferred.

## 2026-10-22 continuation: volume changes retain pressure progress

Fresh native before/after reproduction proves ordinary positive water-volume changes restarted retained searches: old 129 → 129 nodes/restart 1; revised 129 → 257/restart 0, conserving 4,190 units. The unreleased candidate retains wet frontiers, refreshes current heads within bounds, defers churn fairly, coalesces only fully explored stable components, and reduces duplicate reads/pending allocations. Long bends, silent edits, actual unload/no-force-load, isolated idle/reopen, existing filling/pressure, repeated basin retention and the final core/module build pass. A separate packaged-JAR native probe also passes. See [changes, evidence and limits](validation/engine-continuation/2026-10-22-pressure-head-retention.md).

ABBA 1,000-tick/49-chunk tests improve over the preceding continuation candidate: revised median 17.608–17.725 ms/p95 27.456–31.423 versus old 19.093–19.402/33.123–33.130; pending 6,099–6,198 versus 6,881–7,221. This does not close performance acceptance: thousands of entries remain, matched current pre-continuation comparisons/complete settling/output/memory are still absent, and residency saturation/general drain continuation remain open. No visual/Vulkan/GPU checks ran. Budgets, geology, enabled-view LOD/transparency and broader terrain acceptance remain unfinished.

A stale packaged runtime was caught despite valid ZIP CRCs in copied incremental output. The rejected package was rebuilt uncached; the final archive passes outer/four nested CRCs and all 103 main-class byte comparisons, then the packaged native regression. Future package audits must check source/class identity too. Main, delivered alpha.24 installers and frozen V1/V2/V3 geography remain unchanged; no installer was released.

## 2026-10-22 continuation: compact active FIFO, broader acceptance still open

The unreleased working branch stores the active-water FIFO as packed coordinates and reuses a local stepping cursor. A new native 65,536-cell contract checks FIFO order, mutable input snapshots, negative coordinates, duplicates at capacity and scheduled overflow. Four warmed admission passes (262,144 activations) allocate 17,197,016 bytes before versus 304 bytes after; this measures the admission path, not total solver/live memory or FPS. No transport rule, budget, terrain or graphics source changed. See [candidate source, reproduction and fresh gates](validation/engine-continuation/2026-10-22-active-queue-allocation.md).

All 21 native cases (initial matrix, interleaved basin repeats and packaged filling), the full non-Vulkan core/module build, 103 packaged class comparisons and outer/four nested CRC checks pass. Long/head/edit/unload/idle/reopen/filling/pressure controls conserve the expected quantities and inspect explicit records plus normal saves. Initial staging caught an incompatible newer validation fixture on the pre-continuation baseline; it was restored and rebuilt before accepted native runs.

Timing is not certified: initial candidate medians are 17.913–20.419 ms versus current 16.819–17.615; the later interleaved pair is 16.728–18.864 versus 17.638–20.857. All retention gates pass, but final quantities/FLOW digests vary even between unchanged-revision repeats, and roughly 6,100 entries remain. The loaded snapshot rectangle has open boundaries and is not a regional mass ledger. Allocation improvement does not close settling/output/latency/resident-memory acceptance. No new visual/Vulkan or GPU checks ran. Main, supplied alpha.24 installers and frozen V1/V2/V3 geography remain unchanged.

Next is a deterministic closed native basin replay or explicit boundary-flux ledger before broader performance promotion, followed by pressure/drain residency/continuation. Water budgets, enabled-view coverage/transparency, LOD and geology remain unfinished; catalog/species work stays deferred.

## 2026-10-22 continuation: failed drain work preserved; reload lifecycle audit

The prior foreground lease expired. An isolated audit preserved the unaccepted retained-drain source, native fixture, original failed matrix and a separate lifecycle patch without integrating solver code. Fresh read-only region inspection confirms the failed reload's long-route unit remains at its source while the short control drains; this is not merely a leftover counter. A Java17 source-extracted fixture reproduces direct level access inside the pre-FULL chunk-load callback. The candidate defers wakeup and passes modeled duplicate, capacity/overflow and cleanup checks. This identifies a lifecycle violation; the actual native stall's full cause and correction are still unproven. See [source, fresh evidence, prior failures and reproduction](validation/engine-continuation/drain-reload-audit-2026-10-22/README.md).

Workspace maintenance removed JDK25/Gradle/native dependencies. No new full build, core/module, native, visual, Vulkan, package or performance checks ran. The earlier native reload and 800-tick basin failures remain failures; later cases in that matrix never ran. Production WaterRuntime, main, supplied installers and frozen V1/V2/V3 geography are unchanged by this checkpoint. Restore the environment, trace actual event delivery/readiness, then repeat reload/conservation/edit/unload/reopen, closed-basin and build/package gates before integration. Budgets, larger pressure/drain residency, geology and enabled-view LOD/transparency remain open. Work lease released.

## 2026-10-22 continuation: bounded ticket readiness; native failures block integration

The isolated JDK25/JDK21/Gradle/native environment is restored and 381 default-build inputs were Git-blob verified. A new optional checksum-pinned tool-restoration helper verifies all five archives and refuses overwrite. Both accepted-source and candidate full non-Vulkan core/module builds pass. Production solver source remains at the accepted checkpoint; the independently authored candidate is preserved as evidence only. See [fresh source, tests and failures](validation/engine-continuation/drain-ticket-readiness-2026-10-22/README.md).

Native tracing identifies a reload defect in the inherited retained-drain experiment: a resident LevelChunk regains FULL availability through ticket changes without another ChunkEvent.Load. The candidate queues bounded ticket/load readiness checks (16 no-force probes/host tick, 1024 keys) and wakes origin waiters on the level step. Cached reload, a separate fixture requiring actual physical ChunkEvent.Unload, silent-edit/unavailable/reopen, long-pressure safety and active FIFO checks pass. The physical route misses its earlier 2000-tick deadline and passes at 3200; the first attempt timed out because vanilla empty-server pause was enabled. All outcomes are recorded.

**Do not integrate the retained-drain runtime or release an installer.** Existing eight-unit film filling fails on both inherited and ticket experiments; the accepted checkpoint passes. Canonical packaged failure shows zero transfers, all 16 resident drain slots, 128 waiting jobs and 1032 pending cells; the admission/fairness cause is not proven yet. A sealed 8192-unit basin remains conserved but fails full settling at 3200 ticks: upper698, bottom7457, intermediate37, pending500. Gradle returns zero despite the assertions/crashes. Explicit records/crash stacks and saved film positions were inspected.

The final canonical candidate archive passes outer/four nested CRCs and 104 main-class byte comparisons; its packaged reload passes and packaged filling fails, with identical archive hash before/after. No new client visual/Vulkan/owner GPU checks ran. Solver substeps are not independent 60 Hz. The next implementation is a bounded drain progress/fairness repair, followed by repeat filling/closed-basin and safety/package gates. Pressure residency, regional water budgets, enabled-view LOD/transparency and geology remain open; catalog/species is deferred. Work lease released; main, supplied alpha.24 installers and frozen geography remain unchanged.


## 2026-10-22 continuation: drain FIFO repairs starvation; basin still blocks integration

An independent bounded drain-request FIFO fixes the inherited retained-drain candidate's eight-unit film starvation. Fresh before tracing shows 1024 settled competitors repeatedly filling the resident slots while the live film stays queued; the final candidate delivers seven units at tick 43 and eight at 44, passing the unchanged 120-tick filling assertions. Requests retain their FIFO head across quota boundaries, with a 65,536-key cap/overflow wake, at most 3,072 first-turn visits within 6,144 total drain visits/host, 16 retained jobs and 128-node turns. One bounded temporary witness can probe a short outlet at residency saturation; per-host assertions verify its watches are released. Transport and topology-event bodies are unchanged. See [source, tests and limits](validation/engine-continuation/drain-fifo-progress-2026-10-22/README.md).

All 17 sequential safety cases pass, including long drains, silent wall edits, cached and observed physical unload/reload, no-force gaps, pressure/head/edit/unload/idle/reopen and both FIFO capacity/overflow contracts. A new fully loaded 1,024-unit pool plus late film clears all work, stays idle for 20 ticks, then wakes after an ordinary floor edit, fills the new sink and clears pending work again while conserving every tick. Seven specified closed-route quantity/FLOW digests match prior ticket source. Full non-Vulkan core/module/geography build, five archive CRCs,226 packaged-class comparisons and canonical packaged filling/reload pass. An allocation repeat measured 304 bytes after an initial 80,560-byte counter; both records remain and no general allocation/memory/performance claim is made.

**Do not integrate the unaccepted runtime or release an installer.** The larger sealed 8,192-unit basin still fails complete settling at 3,200 ticks: upper 368, bottom 7,697, intermediate 127, pending 681, with quantity exact. Minecraft asserts/crashes while Gradle returns zero. Original unpinned filling still has 1,024 pending/waits at 120 despite its repaired live route. Failed coalescing/probe attempts and two corrected validation-fixture compile errors are preserved alongside final passes. The accepted production solver remains `2c431bb`; this checkpoint changes docs/evidence only. Main, supplied installers and frozen geography remain unchanged.

Next repair repeated basin search/admission churn, then repeat full settling/safety/package checks. Larger residency continuation, renewable/finite regional water budgets, enabled-view water/LOD/transparency and geology remain open; catalog/species stays deferred. No fresh visual/Vulkan/owner GPU acceptance ran, and substeps remain distinct from independent 60 Hz. Work lease released.

## 2026-10-22 continuation: closed basin settles; pressure-cap cleanup staged

An independent distant-drain throughput fix replaces the inherited one-unit-only
commit with the existing four-unit pressure quantum after witness/head/capacity
validation, preserving one-unit film progress. The engine core now owns the capped
pressure overload and its legacy API remains exact across 34,969 differential
cases; 629,442 capped cases and 29,478 new integrated cases pass. Local and retained
pressure lose duplicated cap/reconstructed Exchange code. Scheduling, bounds,
topology and lifecycle bodies match the prior staged FIFO. See [source, failure
evidence and acceptance limits](validation/engine-continuation/basin-pressure-2026-10-22/README.md).

The old wet-witness probe transfers one instead of four units and asserts/crashes
despite Gradle zero. The old sealed basin again conserves 8,192 units but fails full
settling at 3,200 (upper 368, bottom 7,697, pending 681). Both current-source repeats now
settle at tick 2,369 with identical quantity/FLOW hashes and final pending/watch counts
zero. The stricter repeat checks zero actual solver work on all 831 subsequent
ticks. The fully loaded pool passes 20 zero-work idle ticks, ordinary floor-edit
wake, exact quantity and final cleanup. Seven specified route digest pairs match
the prior FIFO. All 24 final native safety/idle/basin/packaged cases pass the strict
record-file/stdout/final-stop/save/build audit, along with uncached non-Vulkan
core/geography/module build, five CRCs and 226 packaged-class comparisons.

Self-audit caught a truncated reopen log capture (record file exists, but no stdout
pass/final shutdown/build completion), stopped the matrix and required an explicit
separate-process retry. It passes; the rejected attempt remains excluded. The
runner no longer treats a startup autosave as final acceptance. The first full
build also caught a new validation-local name collision; the corrected core test
and generator then pass the full build. Neither event is relabeled as a native
solver pass. The matching archive hash remains unchanged after packaged probes.

**Production source remains at 2c431bb; runtime/core changes are staged evidence
only.** Candidate basin p95 remains higher: 5.26–5.65 ms versus 4.59 ms baseline,
despite eventual settling and fewer combined visits. Broad latency, output and
resident-memory acceptance remains open. Next profile increased pressure work and
tail latency while preserving the new functional gates. Original unpinned filling
still has 1,024 waiters at tick 120; larger residency, regional budgets, enabled-view LOD/
water/transparency and geology remain unfinished. No fresh visual/Vulkan/owner GPU
check ran; the display/Vulkan test environment is absent. Main, supplied installers
and frozen V1/V2/V3 geography are unchanged. Work lease released.

## 2026-10-22: accepted pressure admission cleanup; Vulkan next

Two production methods now centralize admission checks and retain resident job
ownership defensively. Exact-source normal amount reads fall from three to one;
no measured gameplay latency/FPS improvement is asserted. The overlap reproduction
is deliberately injected and normal-call reachability is unproven. All 13 final
native gates, uncached non-Vulkan build/core/module checks, five CRCs and 225 class
comparisons pass. Baseline assertion crashes and rejected intermediate capture are
preserved as failures. See [source, evidence and scope](validation/engine-continuation/pressure-admission-2026-10-22/README.md).

The retained-drain experiment stays separate and unaccepted; its newly combined
cleanup needs fresh full validation. Main/installers/geography remain unchanged.
Vulkan is now the primary graphics reference/test API at the owner's direction;
OpenGL fallback remains modular. No fresh graphics acceptance yet. Next restore
the Vulkan environment and exercise enabled-view water/LOD and cleanup gates.

## 2026-10-22: Vulkan primary sample and fresh software-driver validation

Added validation-only water/LOD scene isolation/observation commands and corrected
stale Vulkan build/sample instructions. Production renderer and frozen geography
are unchanged. The restored Vulkan Linux build/core/module checks pass, as do six
archive CRCs and 406 class-byte comparisons. Four strict Vulkan sessions pass
creation/reopen, device transfers/shader/depth/stencil, active LOD with animated
and fallback water, resource reload, memory guard and forced shutdown-completion
rejection. All finish with zero VUIDs, validation errors and tracked resources.

The initial OpenGL recovery attempt and incomplete scene capture are excluded.
An early reopen screenshot shows transient partial water/exposed terrain; warmed
images recover. Cause and complete coverage remain unaccepted. These are software
Vulkan correctness checks, not owner GPU/FPS guarantees. See [screenshots, exact
records, failures and limits](validation/engine-continuation/vulkan-primary-2026-10-22/README.md).
Next implement bounded Vulkan coverage-recovery stress across joins, teleports,
low budgets and player edits. Water p95/residency, regional budgets, geology and
LOD acceptance remain open; no installer released or main merge.


## 2026-10-22: native Vulkan completion crash and LOD disk retention fixed

[Fresh evidence and limitations](validation/engine-continuation/vulkan-lod-recovery-2026-10-22/README.md) record two focused production fixes. Actual V3 creation/disconnect and a native injected completion reproduced premature public render-pass ownership; ownership now begins after lazy frame-start callbacks. The old native LOD disk queue lost an out-of-range persisted key; rotation now preserves it within 64 inspections per call. Exact-source bounded/fairness controls and real store/worker/GPU checks pass.

Uncached full Linux Vulkan build/core/geography/module checks, six CRCs and 406 class matches pass. Two strict combined native sessions (development and packaged JAR origins) pass regression/device/reload/normal-disconnect/shutdown gates with zero VUIDs, validation errors and final native resources. Source audit: 502/505 original inputs unchanged, two production changes plus validation dispatch/cleanup, two new fixtures. Water/core/geography are unchanged.

Two direct-stop sessions are rejected despite Gradle zero because saves logged rejected-executor failures. Validation now disconnects/saves before stop; direct quit safety is not established. Malformed generated archives were rejected. An unforced packaged launch selected OpenGL recovery and failed the Vulkan assertion; final sessions explicitly retry Vulkan. Marker persistence after FIRST_FRAME/clean shutdown remains unexplained, so clean unforced reopening is still a blocker. Next instrument it in-process, then extend actual physical teleport/edit/budget coverage. The isolated disk fixture does not validate physical teleport or all eviction recovery. Scene LOD is active but this camera has zero visible LOD triangles; exposed lake regions/transparency remain open. Software-driver passes are not owner GPU/FPS guarantees. No installer released, main untouched; retained-drain experiment, regional budgets, pressure p95/residency, geology and LOD hierarchy remain open. Catalog/species remains deferred.


## 2026-10-22: focused startup re-arm reset; intermittent marker remains

[Fresh source, native scopes and rejected attempts](validation/engine-continuation/vulkan-startup-lifecycle-2026-10-22/README.md) add one production reset: a new successful arm clears prior completion. The native baseline asserts; 32-cycle development and packaged owner-method replays now pass. They explicitly replay the lifecycle and do not recreate the GPU device or establish a natural second-initialization trigger. Full uncached Linux Vulkan/core/geography build, six CRCs/406 class matches, package origins, device checks/reload/saved disconnect/shutdown pass with zero Vulkan errors/resources.

One unforced development reopen is clean, but the following unforced packaged launch leaves the marker despite selecting Vulkan and clean resource shutdown. Do not claim clean packaged-reopen acceptance or attribute the original persistence to the re-arm latch. Temporary tracing observes successful deletion then later presence without another traced owner arm; paths agree. Ptrace is denied. Next capture bounded in-process filesystem events and owner calls, then repeat unforced packaged reopening; also isolate the early atlas timeout under matched warm state. Direct-stop safety, physical teleport/edit/budget coverage and all existing water/geology/LOD requirements remain open.

Self-audit excludes an atlas timeout, a packaged console whose later bytes no longer match its reported hash, and setup/model rerun errors. New native output is sealed after bounded stability sampling and verified by raw/compressed hashes; no old report is rehashed into a pass. Two upright terrain captures are scoped visual evidence only. One production file changes; water/core/prior frame+LOD fixes/frozen geography remain unchanged. No installer or main merge; software checks are not owner GPU/FPS guarantees.


## Persisted LOD eviction recovery checkpoint — 2026-10-22

[Source, regressions and scoped evidence](validation/engine-continuation/lod-eviction-recovery-2026-10-22/README.md) reproduce a real persisted page losing its key after GPU eviction. One production LOD file now tracks persisted provenance and requeues eligible evictions in an ordered unique 8,192-key index. It retains queued edit guards, rejects stale/unwritten/disabled restores, bounds cleanup/selection to 64 keys/call, and defers fixed-view victims until the player chunk or memory setting changes. An intermediate missing-edit-guard failure remains excluded.

Full uncached Linux Vulkan/core/geography/module build, six CRCs, 407 class comparisons and all five nested/standalone identities pass. Two strict development/packaged software-Vulkan sessions pass four eviction recovery cycles each, fixed-view idle/movement recovery, four admission controls, actual native chunk capture/store provenance, queued stale-edit rejection, frame/device/reload/save/shutdown gates and zero final resources/errors. These isolated GPU/disk and payload-reservation tests do not establish physical teleport, actual sustained memory-pressure coverage, complete water/transparency or owner GPU/FPS acceptance. Water/core/Vulkan code and frozen geography are unchanged; one production plus three validation files differ among 508 checked inputs.

Temporary marker tracing confirms deletion followed by workspace recreation with a different inode/original mtime and only one recorded Java write; a matched /tmp diagnostic and final /tmp sessions are clean. Writer/root cause is unproven; ptrace is denied and no speculative recovery workaround is shipped. A reload timeout is excluded despite prior focused pass records. Two later final sessions pass but do not explain that failure or the earlier atlas stall; a bounded phase-aware reload watchdog is preserved.

Next physical teleport/edit/reload and actual low-memory coverage; address 8,192-key saturation, same-camera freed-capacity recovery and coarser hierarchy. Preserve marker/reload/atlas and direct-stop blockers, staged water p95/1,024 waiters/larger residency, regional budgets and geology; catalog/species stays deferred. Main/installers remain unchanged and the lease is released. No continuous execution or independent 60 Hz/owner FPS claim.


## LOD capacity wakeup checkpoint — 2026-10-22

[Fresh source, real ballast reproduction and limits](validation/engine-continuation/lod-capacity-wakeup-2026-10-22/README.md) fix one missed recovery: an evicted persisted page can now resume at the same player chunk/budget when its recorded bytes and one cache entry fit without another eviction. The native baseline still defers after closing 4,193,476 bytes of real GPU ballast; the candidate restores its 1,656-byte page. It stays idle under pressure and retains bounds/edit guards.

Full sealed uncached Linux Vulkan/core/geography/module build, six CRCs, 407 class comparisons, five module identities and two strict development/packaged software-Vulkan sessions pass. The real ballast/free-capacity route, prior eviction controls, native snapshot/store provenance, injected edit guard, device/frame/reload/save/shutdown checks finish with zero GPU errors/resources. One production plus one validation file differ among 508 inputs. Water/core/Vulkan/frozen geography are unchanged; no installer or main merge.

Maintenance required verified source/toolchain restoration. An initial Gradle Binary store setup failure is excluded; the unchanged baseline retry reproduces the expected assertion. An incomplete cold-build client console is not used as complete build acceptance; the final candidate has sealed explicit passing records. These do not fix or explain prior marker/reload/atlas issues. The native ballast is synthetic in an isolated renderer; physical teleport, real player edits, complete mixed transparency and sustained world-view memory coverage remain open, as do 8,192-key saturation/coarser hierarchy and owner GPU/FPS acceptance. Preserve water p95/1,024 waiter/residency, regional budget and geology requirements; catalog/species remains deferred. Work lease released.


## Persisted LOD upload retry and authoritative travel — 2026-10-23

[Source, failures and scoped evidence](validation/engine-continuation/lod-upload-retry-2026-10-23/README.md) fix another real lost-key path: a live budget reduction cancels and releases a partial persisted GPU upload but previously forgot its recovery key. The native baseline asserts after an 8-to-4 MiB reduction beside actual 4 MiB ballast and a 552-byte partial upload. Eligible completed/pending pages now retain bounded deferrals; newer persisted data may replace older resident revisions. Edit/session/persistence guards and no-read pressure behavior pass.

Full sealed uncached Linux Vulkan/core/module/geography build, six CRCs/407 class matches/five module identities and two final strict development/packaged cancellation sessions pass, including previous eviction/edit/capture, GPU/frame/reload/save/shutdown controls and zero final errors/resources. A parent-source and final packaged candidate travel pass observe three server teleports, client unload, authoritative glass edit/removal, matching real persisted snapshots and resource reload while enforcing a 4 MiB global LOD payload budget each client tick. These are ordinary server block updates, not simulated player-input placement, server physical unload, separate-process reopen or complete pixel coverage acceptance.

Self-audit rejects an invalid ClientLevel.hasChunk unload oracle and a later-mutated workspace console. Final strict audits require live/sealed byte identity and use fresh private /tmp captures; the writer/cause is unproven. Prior marker/reload/atlas and direct-stop failures remain open. Inspected images retain coverage/terrain/water concerns; low-memory data recovery is narrower than full enabled-view rendering. One production file changes; water/core/Vulkan/frozen geography match the parent. No main merge/installer or owner GPU/FPS/independent 60 Hz claim.

Next reproduce actual scene coverage gaps with assertions/watchdogs, then repair pages/coarser fallback/index saturation. Preserve staged water p95/1,024 waiter/residency, regional budget and geology requirements; catalog/species remains deferred. Work lease released.


## Bounded LOD startup-index merge — 2026-10-23

[Source, baseline failure and scoped evidence](validation/engine-continuation/lod-index-merge-2026-10-23/README.md) repair a real cap violation: one queued recovery key plus a full completed scan previously became 8,193 keys. Production now retains existing recovery order/edit guards and admits nearest-first scan entries only into remaining slots. Four exact-source/native merge cases stay at 8,192; this does not solve spatial/paged discovery or coverage beyond that cap.

Full sealed uncached Linux Vulkan/core/module/geography build, six CRCs/407 class matches/five identities and two strict development/packaged native sessions pass. They repeat prior actual GPU cancellation/eviction/edit/capture, frame/device/reload/save/shutdown controls with zero final GPU resources/errors and matching sealed/live logs. Scan completion is controlled; no actual 8,192-file scan, new authoritative travel, player-input/server physical unload or complete visual coverage acceptance is claimed. One production file changes; water/core/Vulkan/frozen geography and main/installers remain unchanged.

The owner's latest delivered Windows Vulkan alpha.24 JAR was provided again; it excludes later alpha.25 development changes. Continue toward market-leading quality with comparative evidence before superiority claims. Next bounded spatial/paged LOD discovery/coarser coverage under actual scene pressure; keep marker/reload/atlas/direct-stop and water p95/waiter/residency/budget/geology blockers open. Catalog/species is deferred. Work lease released.


## Water drying-fade continuity — 2026-10-23

[Source, native before failure and scoped checks](validation/engine-continuation/water-dry-fade-2026-10-23/README.md) address the owner's seamless-flow request. A zero-vertex dry/dry recapture previously deleted a real 46,080-byte GPU water page only 25 ms into its 100 ms drying fade. The native baseline asserts; production now keeps that existing GPU page until its original cleanup deadline. Repeated empty updates do not extend the deadline, rewetting retargets from half opacity, and newer-edit protection/cleanup remain intact.

Full sealed uncached Linux Vulkan/core/module/geography build, six CRCs/407 class matches/five identities and two strict development/packaged native sessions pass. They repeat LOD, actual GPU retry/eviction/edit/capture, frame/device/reload/save/shutdown controls with zero final resources/errors and matching sealed/live logs. The new test uploads actual water GPU meshes with controlled snapshots/time; it does not draw that isolated page or prove pixel-complete live-river flicker acceptance. Inspected lake stills are limited evidence. One production renderer changes; authoritative water/core/LOD/Vulkan/geography and main/installers remain unchanged.

Next prioritize matched live-flow frame/coverage sequences and shared adjacent-page/native/LOD handoffs, including dry/rewet, reload/travel/low memory. Keep regional budgets, staged water p95/waiter/residency, spatial/coarser LOD, geology and existing marker/reload/atlas/direct-stop blockers open. Catalog/species stays deferred; no owner GPU/FPS/independent 60 Hz claim. Work lease released.


## Water GPU update continuity under memory pressure — 2026-10-23

[Source and explicit before/after evidence](validation/engine-continuation/water-gpu-reuse-2026-10-23/README.md) fix a reproduced same-size water upload stall. A real 46,080-byte page beside 33,508,224 GPU ballast bytes cannot replace itself under the baseline's 32 MiB admission check, despite requiring no larger final allocation. The renderer now updates the existing GPU allocation when data fits. The shared graphics mesh retains full capacity accounting and updates the draw count; larger updates keep the bounded admission/retry and closer-page coverage protections.

Full sealed uncached build/core/geography, six CRCs/407 class matches/five module identities, two strict development/packaged Vulkan sessions and one scoped OpenGL fallback session pass. Native checks cover 16 near-capacity updates, shrink/regrowth, bounded oversized retry, 288 GPU bytes read back, and prior fade/LOD/GPU/frame/reload/save/shutdown protections. The fixture uses actual GPU allocations but controlled isolated pages; images do not prove live-flow temporal continuity or full coverage. Two production files change; water solver/core/LOD/Vulkan backend/frozen geography remain identical. Main/installers are unchanged; no owner GPU/FPS/independent 60 Hz or market-superiority claim.

Next establish actual live-flow frames/coverage and adjacent-page/native/LOD continuity with dry/rewet/reload/travel/low memory; flow UV/timebase remains open. Keep spatial/coarser LOD, pressure performance/waiter/residency, regional water budgets/geology and existing native failures open. Catalog/species remains deferred. Lease released, acceptance incomplete.


## Mixed wet/dry page water continuity and headwater/composition direction — 2026-10-23

[Source, explicit baseline and scoped GPU checks](validation/engine-continuation/water-partial-fade-2026-10-23/README.md) fix a mixed-page fade cut: a second capture removed 20 of 40 water vertices only 25 ms into a 100 ms fade while another patch stayed wet. Retain absent still-dying face groups with their original capture revision, reuse immutable arrays, and encode original opacity timing in the existing vertex byte. Rewetting replaces old faces; finished fades release history; cleanup deadlines no longer restart on each rebuild.

Full sealed uncached build/core/geography, six CRCs/408 class matches/exact shader resource/five module identities, two final strict development/packaged Vulkan sessions and scoped OpenGL fallback pass. Six GPU readbacks cover 8,640 bytes and expected fade/rewet progression; CPU history is 1,120 bytes in the fixture, arrays are reused, and completed partial cleanup retains steady water. Prior water/LOD/GPU/frame/reload/save/shutdown protections remain passing. A first baseline startup/action timeout is excluded; its cause is unresolved. Controlled GPU geometry/shader-input checks are not live-scene pixel continuity acceptance. Main/installers, water solver/core/LOD/Vulkan backend and frozen geography remain unchanged.

The owner clarified headwater source outlets and composition mixing. docs/WATER-BUDGET-REQUIREMENTS.md now requires metered headwater supply, shared compatible river/ocean physical volume, conservative constituent mixing/advection, no level rise from chemistry alone, accounted ocean discharge and per-family mixing/layering/reaction rules. Sparse persisted chemistry/reservoirs remain unimplemented. No hidden drain for real inflow or source multiplication.

Next actual live-flow temporal/coverage checks and adjacent-page/native/LOD/UV work; retain water budget/chemistry, pressure continuation/performance, spatial/coarser LOD, geology and previous native blockers. Catalog/species stays deferred. No owner GPU/FPS/independent 60 Hz claim. Acceptance remains incomplete; lease released.


## Live water page-order/pixel audit — 2026-10-23

[Validation-only checkpoint](validation/engine-continuation/water-live-order-2026-10-23/README.md): no production code change. Existing bounds-distance page sorting survives actual map insertion-order permutations. Controlled production-pipeline two-layer pixel A/B proves order affects color, not a new engine defect. Two final strict software Vulkan sessions and scoped OpenGL fallback capture27 real water-stage frames across9 wave/LOD views, losslessly preserved with decoded pixel identity. The lake has zero ocean-wave geometry even with the setting enabled; ocean wave coverage stays open. Fine repeated patterns/bands remain; no complete live-flow/flicker acceptance.

Full sealed25-task build,408 class identities,6 archive CRCs,5 module identities, prior fade/GPU/LOD/frame/reload/save/shutdown controls pass. Initial new harness compile and OpenGL texture-usage failures are preserved/excluded; both APIs rerun with legal named flags. All production, main/installers, water quantity and frozen geography unchanged. Audit confirms animated capture drops FLOW/SPEED and always selects water_still. Next implement focused flow-aware texture/UV behavior, then actual ocean-wave/shared-edge/longer coverage. Keep other registered water/pressure/LOD/geology blockers open. No owner GPU/FPS or independent60Hz claim; engine unfinished.


## Flow-aware animated water capture and atlas UVs — 2026-10-23

[Reproduced defect, source and final checks](validation/engine-continuation/water-flow-texture-2026-10-23/README.md): actual client cell FLOW0/east versus4/south previously produced identical animated UVs. Source restored before explicit baseline assertion. Capture now carries direction, mesher rotates flow UVs and selects flowing/still atlas material. Shared atlas timing stays in place; physical solver/quantity and frozen geography unchanged. Packed face/flow metadata retains14-float CPU/36-byte GPU format and1120-byte partial-fade retained payload; no expanded face histories.5832 extra flow bytes per snapshot under existing page/job caps.

Final sealed25-task build,16-direction/320-vertex core test,408 class identities/6 CRCs/5 module identities pass. Two strict software Vulkan development/packaged sessions and scoped OpenGL pass18 GPU direction samples before/after reload, selected raw UV records,27 short live lake frames and prior water/LOD/GPU/frame/save/shutdown controls. CPU observations retain matched geometry; no renderer/FPS performance claim. Superseded15-float candidate and noisy short-warmup measurements are preserved/excluded from final-source acceptance.

This fixes ignored direction, not complete seamless flow: UV orientation retargeting/side morph/diagonal seams and speed advection still need work. Lake wave geometry is0bytes even with the option enabled; actual ocean/stream/longer coverage stays open. Retain other water budgets/chemistry, pressure, LOD, geology and native blockers. Main/installers unchanged; engine acceptance remains incomplete.


## 2026-10-23 — side UV height continuity checkpoint

Reproduced 640 differing production-shader pixels at matched displayed heights; fixed side UV motion and stale-admitted-height coherence in two production files, preserving 14-float CPU/36-byte GPU payloads, existing matrix allocation, light, clocks, quantity and frozen geography. Full uncached 25-task build, six CRCs/408 class identities/five module identities, two strict software Vulkan sessions and one scoped OpenGL fallback pass. Eighteen exact pixel pairs now have zero differences; 36 real upload readbacks/25,920 bytes cover repeated retargets, stale revisions and light. Twenty-seven short lake frames inspected; actual ocean geometry remains untested. First incomplete build console is explicitly rejected despite exit zero. No installer; no owner GPU/FPS or whole-stream acceptance.

Evidence: [docs/validation/engine-continuation/water-side-uv-2026-10-23/README.md](validation/engine-continuation/water-side-uv-2026-10-23/README.md). Next implement smooth direction/material retargeting and validate real streams/diagonal seams; retain all existing solver/budget/LOD/geology and marker/reload blockers. Lease released, acceptance incomplete.


## 2026-10-23 — flow-top world phase and mip-wrap audit

Fixed same-direction per-block UV resets (6,144 differing baseline pixels) and a superseded implicit-wrap mip defect (132 pixels beyond1/255 tolerance; max255). Four production files preserve14-float CPU/36-byte GPU payload and existing matrix allocation, quantity and frozen geography. Final clean serial25-task build,400,000 shared-edge component checks,6 CRCs/408 class identities/5 module identities, two strict software Vulkan sessions and scoped OpenGL pass.18 base pairs exact;18 diagnostic mip pairs within explicit1/255 rounding tolerance. Retained side/fade/actual GPU upload/flow/LOD/frame/reload/save gates pass.27 lake frames inspected; distant loading changes and waveBytes0 prevent complete scene/ocean acceptance. Failed missing-world/compile/truncated-archive attempts are preserved and excluded; causes not claimed resolved.

Evidence: [docs/validation/engine-continuation/water-flow-phase-2026-10-23/README.md](validation/engine-continuation/water-flow-phase-2026-10-23/README.md). Next smooth temporal/different-direction retargets and validate sustained streams; profile added varying/arithmetic. Keep all registered pressure/budget/chemistry/LOD/geology/native blockers open. No installer, owner GPU/FPS, superiority or independent60Hz claim. Lease released; acceptance incomplete.


## 2026-10-23 — direction-only water crossfade

Reproduced abrupt direction sampling (baseline7680 channels beyond1/255,max120), then added100ms fixed-endpoint single-draw RGBA blend with latest pending target, preserved age/stale guards, bounded32768 history, exact distant cell phase and preserved light/opacity/payload sizes. Self-audit caught initial-idle retarget delay, fixture compile error and GLSL330 reserved-name crash; corrected, preserved and excluded. A truncated Gradle-successful outer archive was also rejected; its cause remains unresolved. Clean serial25-task build,240 ordered direction pairs,6 CRCs/409 class identities/5 modules pass. Two strict software Vulkan and scoped OpenGL pass1008 controlled renders/720 blend comparisons within1/255,21 actual upload readbacks/15120 bytes,plus prior water/LOD/frame/reload/save/shutdown gates.27 lake frames inspected;waves0 bytes.

Evidence: [docs/validation/engine-continuation/water-direction-blend-2026-10-23/README.md](validation/engine-continuation/water-direction-blend-2026-10-23/README.md). Still/flow/material and spatial different-direction continuity,sustained streams/ocean,history cap churn and matched profiling remain open. Preserve registered pressure/water budget/chemistry/geology/LOD/native blockers. No owner GPU/FPS,superiority,independent60Hz or installer claim. Lease released;acceptance incomplete.


## 2026-10-23 — still/flow top-surface continuity

Four production files extend the 100 ms fixed-endpoint texture blend through still code 16, with sparse previous-snapshot admission, independent sprite bounds, queued/stale guards and unchanged 14-float/36-byte vertices. The unchanged-production baseline explicitly fails despite normal exit/save: 1055 bad vertices, 774022 channels beyond 1/255, maximum 219. Full serial 25-task build, 272 core pairs, six CRCs, 409 class identities and five module identities pass. Two strict software Vulkan sessions and scoped GL pass 2304 actual-upload draws, 1920 weighted RGBA checks and 1179 GPU readbacks (848880 bytes), maximum 1/255 and zero violations. Retained water/LOD/reload/save gates pass. The failed fixture-import and production-accessor builds plus an opacity-auditor correction are preserved and excluded.

Evidence: [still/flow checkpoint](validation/engine-continuation/water-material-blend-2026-10-23/README.md). All 27 lake frames inspected; distant LOD cutoffs remain and wave bytes are zero. Next: side-face material/tint transitions, spatial edges, long streams/ocean, cap churn and matched profiling. Preserve registered water budgets/chemistry, pressure, geology and LOD/native blockers. Added flat vec4 cost is unprofiled. No owner GPU/FPS, independent 60 Hz, superiority or installer claim. Lease released; engine acceptance incomplete.


## 2026-10-23 — shared side/underside water material clock

Three production files extend the admitted 100 ms still/flow presentation to all faces, recover queued/stale side U, and clamp atlas V at fragment sampling. The baseline explicitly fails despite normal exit/save: 25740 bad vertices, 3850240 channels beyond 1/255, maximum 192; the underside reads a neighboring tile. Self-audit rejects a fixed-height-passing vertex clamp: 16 simultaneous 4-to-16 height/material routes fail 1536 channels, maximum 9. Unwrapped interpolation plus a fragment guard fixes it. No new varying, vertex or uniform allocation; CPU 14 floats/GPU 36 bytes and the 32768-entry history remain.

The full serial 25-task build, retained 272 core pairs, six CRCs, 409 class identities and five modules pass. Two strict software Vulkan sessions and scoped GL pass 12000 actual-upload draws, 10080 weighted RGBA comparisons and 6027 GPU readbacks (5207328 bytes), maximum 1/255 and zero violations. Retained water/LOD/reload/save/shutdown gates pass. An incorrect extra-route index, exact raster-edge oracle ties and GL readback row ordering were caught, documented and corrected; both Vulkan and GL were repeated on the final fixture. All failed and superseded attempts are preserved and excluded from final counts.

Evidence: [side/underside material checkpoint](validation/engine-continuation/water-side-material-2026-10-23/README.md). All 27 lake frames inspected; distant LOD cutoffs remain and wave bytes are zero. Next: spatial different-direction shared edges, sustained streams/ocean, cap churn and matched profiling. Shader/history cost and broader tint/height/opacity/resource-pack acceptance remain open. Preserve registered water budgets/chemistry, pressure, geology and LOD/native blockers. No owner GPU/FPS, independent 60 Hz, superiority or installer claim. Lease released; engine acceptance incomplete.

## 2026-10-23 continuation: spatial seams reproduced; sampled-color reference staged

Added a validation-only exact shared-edge GPU regression and independently authored staged corner-distribution reference. Production renderer/shader/solver/geography code is unchanged. In each of two final software Vulkan sessions (development + packaged), all 1,920 different-direction edges reproduce a seam while 128 same-direction controls are byte-exact. Averaging wrapped UVs hides edges but fails 60/64 uniform-flow fidelity cases; that candidate is rejected and never promoted. Fixed equal-weight sampled-color blends pass 2,048 controlled edge pairs plus 256 uniform endpoint controls byte-exact. The staged reference passes 100,000 edges/17,000 uniform cases, maximum nine distinct directions. See [source, raw evidence, rejected candidate and limits](validation/engine-continuation/water-spatial-seam-2026-10-23/README.md).

The full serial uncached 25-task build/core, six archive CRCs/409 class identities, retained actual-upload/history/phase/mip gates, normal save/shutdown, zero fresh crashes and zero Vulkan validation errors/tracked GPU resources pass. Final totals are 24,832 diagnostic draws/12,416 comparisons. These are diagnostic phase-helper/shader checks, not actual spatial uploads or full stream/ocean/FPS acceptance. Initial overlapped build/native output is excluded; compact raw evidence independently replays. No installer released and no production fix promoted.

Next: implement bounded shared corner weights/phase/temporal history in the real upload path, retain uniform flow and existing material transitions, validate actual page halos/streams/ocean/filtering/edit/fallback behavior, then profile. A naive distribution can need nine texture evaluations per fragment and is not performance-accepted. Keep the owner task enabled; water budgets/pressure/geology and rectangular LOD/horizon cutoffs remain open.

## Shared-corner water presentation — 2026-10-23

Implemented the previously reproduced different-direction top-surface seam in actual production uploads. Five production files add a canonical global-corner direction from wet-neighbor FLOW vectors, bounded shared-corner transition histories, and bilinear blending of sampled colors. Uniform source/target UVs retain the old interpolated endpoint coordinates; a shared Q25 coefficient include prevents shader drift. The 36-byte GPU / 14-float CPU vertex strides, low light bytes, opacity clocks, geometry and water quantities remain unchanged. A separate 32,768-entry corner history and 24 additional varying bytes are added; mixed transitions can evaluate eight endpoints, static uniform water one and uniform transitions two. This cost requires broader profiling.

The exact unchanged parent baseline verifies 411 production blobs and loaded class/shader identities: 2,204/2,332 shared-edge routes fail, while all 136 uniform controls are byte-exact. A mixed-source baseline attempt was excluded. Self-audit rejected 24 nearest-texel fidelity differences (maximum 5/255), then 25 source-endpoint fade differences; both endpoints now interpolate consistently without changing tolerance. An independent raw-byte audit also found an expired height-clock signed-byte wrap; clamping completed offsets to the 100 ms window fixes it, covered by 98,307 long-idle cases. Earlier passing variants are excluded from final totals and preserved with failures.

Fresh serial full build passes all 25 tasks; core checks include 8,435,621 codec cases, 98,307 height-clock cases and 34,000 shared-halo comparisons. All six archive CRCs, 410 compiled-class identities, shader source identities and five nested module identities pass. Two final software Vulkan sessions (development and packaged class origins), followed by OpenGL fallback, pass 29,616 actual-upload diagnostic draws / 14,808 pixel comparisons. All final top-field comparisons have zero channels beyond 1/255 and zero uncovered pixels; retained direction/material/side/height/fade/reuse checks pass. Explicit records, sealed console hashes, source/runtime identities, saves, crash deltas, Vulkan error/resource cleanup and screenshots are audited independently. These are controlled edge reprojections of real uploaded payloads, not full live-stream acceptance or owner GPU/FPS evidence.

Next: reproduce a new corner request arriving on only one page while its neighbor remains resident, then implement bounded neighbor-consistent adoption without losing accepted endpoints. Matched delayed repacks here are checked only after both pages have adopted the request. Active cap churn, real long streams/ocean, spatial side seams, tint/chemistry/other liquids, arbitrary alpha/linear packs, broader height/opacity/edit combinations and matched frame-time/resident-memory acceptance remain open. Lake screenshots still show distant rectangular LOD/horizon boundaries. Water budgets, pressure continuation, geology, enabled-view/LOD acceptance and other requirement-register work remain unfinished. Solver/runtime, player edits, backend/LOD source and frozen V1/V2/V3 geography are unchanged. No installer release or main merge; this is an unreleased continuation checkpoint.

Evidence: `docs/validation/engine-continuation/water-shared-corners-2026-10-23/README.md`.

## Staggered resident-neighbor water publication — 2026-10-23

No production or public validation-source change promoted. The expanded actual-upload Vulkan baseline reproduces 32 staggered new-request failures (15,280 channels beyond1/255, maximum120), while all prior paired-repack/settled/uniform/mip controls pass. A shared CPU corner history cannot update already-resident GPU bytes. The new80route matrix covers X/Z, near/far, both first-page orders, same-page controls and0/25/50/75/100ms; far cases include20second-old resident clocks.

An independently authored staged renderer/core fix retains accepted vertex bytes, reads canonical endpoint histories and patches up to8same-layer resident neighbors before the next draw without captures/remeshing or endpoint-clock restarts. It rebases top/side/height/opacity clocks and preserves geometry/light/quantity,36-byte vertex stride, existing GPU allocations and bounded history. Equivalent representations skip rewrites. CPU mirrors can retain up to32MiB, with additional transient copy/direct-buffer memory; whole-page scan/rebase/copy/write cost remains a blocker.

Fresh restored baseline and candidate full builds pass25tasks each. Candidate passes180,000new clock/codec cases plus existing core/geography/hydrology checks,6archive CRCs,411class identities and5nested module identities. Development and packaged software Vulkan, followed by OpenGL fallback, pass30,576actual-upload diagnostic draws/15,288pixel comparisons with no channels beyond1/255 and no uncovered pixels. Independent raw-byte decoding verifies resident neighbor source/target/clock updates without a new request. All retained material/direction/fade/reuse/save/shutdown gates pass, with no fresh crash or Vulkan error/tracked-resource leak. Fresh world creation/join/save/shutdown passes. The diagnostic reprojects actual GPU payloads at identical shared-edge positions; full streams/ocean/FPS are not accepted.

Self-audit rejects promotion: a synthetic33,554,304byte full-residency rebase alone takes7.80msmedian/8.54msp95 against the current2msextraction target, excluding neighbor scans, copies and GPU writes. This is a CPU upper-bound workload, not a measured full-scene frame or ownerGPU result. Functional software-driver passes do not override that performance gate. The candidate and expanded fixture remain under staged evidence; working-branch production/public-test sources, main and supplied installers are unchanged.

Next implementation: reduce/bound boundary preparation and publication cost while preserving coherent old coverage and accepted endpoints; rerun the new32failure reproduction, clock regressions, actual uploads and matched CPU/GPU/resident-memory checks before promotion. Also retain active cap churn, broader height/opacity/edit/queued/stale and failure-recovery coverage, actual long streams/ocean, spatial side/tint/chemistry/resource-pack work, rectangular LOD/horizon cutoffs, pressure/budgets/geology and other requirement-register blockers. Engine acceptance remains incomplete. Environment recovery is complete; pre-maintenance native measurements are historical. Offline evidence and575-input staged restoration replay independently.

Evidence: `docs/validation/engine-continuation/water-neighbor-publication-2026-10-23/README.md`.

## Bounded neighbor corner-packet prototype — 2026-10-23

No production or public validation source promoted. Replaced the previous staged whole-page rebase/copy design with independent corner clocks and indexed144-byte boundary patches. Height, opacity and neighboring page upload origins are preserved; expired corner pairs retire in bounded262080-byte/32-header turns before other extraction work. Existing36-byte vertices, GPU allocation, uniforms, varyings and samplers remain unchanged. The97-bit payload uses normalized finite UV bit carriers and a negative signed-normal marker, with a UV-sign escape for the -128/-127 normalization ambiguity. CPU accepted-byte mirrors still cost up to32MiB, plus <=932064bytes of boundary indices and temporary transfers; memory-pressure acceptance is open.

Final full25-task Vulkan/core/geography/hydrology build passes210000new packet cases, including endpoint decoding, clocks near wrap, native light/height/opacity preservation and retirement. Development and packaged software Vulkan followed by OpenGL pass30672actual-upload diagnostic draws/15336pixel comparisons over2420routes/session. These include the previous80staggered-neighbor routes, far starts at65520ms across wrap and8actual GPU retirement/wrap routes/session. Independent byte/pixel audits find zero channels beyond1/255, max1, no uncovered pixels and58080decoded top vertices. Retained material/direction/phase/side UV/fade/reuse checks and normal save/shutdown pass; final sessions have no new crash or Vulkan validation error/tracked resource leak. Six archive CRCs,411compiled classes and5nested module identities match. No new world-creation validation this checkpoint.

Self-audit found real new regression: the first UV0.x>=2 packet marker collided with the unchanged repeating-UV phase fixture. OpenGL far-origin direction6 mip pair9 failed1024pixels,max255. It is fixed with the explicit signed-normal marker; original phase fixture and tolerances are unchanged and all final APIs pass. An earlier Vulkan run crashed because the partial-fade validator still read raw target-normal opacity; the independent decoder was corrected. One later core-test variable shadowing compile failure is also preserved. Superseded passing Vulkan runs, failed OpenGL run, first crash and compile failure remain sealed and excluded from final totals.

Synthetic32MiB retirement is now divided into129CPU slices:262080bytes/turn,p95=891644ns,max1374075ns in the recorded probe. This is CPU retirement only, excluding GPU copies, boundary publication and all other extraction work. It is not a matched speedup against the earlier8.54ms whole-residency rebase, nor full-frame/ownerGPU evidence. A conservative source service bound is193frames at<=250ms clock advance/frame before65536ms wrap; saturated churn and native maximum-residency service still need validation. Do not promote on these narrow performance passes.

Next implementation: measure worst-case indexed boundary publication and retirement with real GPU staging/copy counts under full residency, cap churn, combined height/opacity/edit updates and injected publication failures. Coalesce/bound GPU range submissions while preserving both-neighbor coherence, then establish matched frame-time/resident-memory and long stream/ocean visual acceptance. Production staggered-neighbor seams remain until promotion. Keep LOD/horizon cutoffs, pressure/search continuation, explicit renewable/finite water/chemical budgets, geology and all other requirement-register blockers open. V1/V2/V3 geography, quantity, player edits, main and supplied installers remain unchanged. Engine acceptance remains incomplete.

Evidence: `docs/validation/engine-continuation/water-boundary-packet-2026-10-23/README.md`.

### Validation environment interruption — 2026-10-23 14:26:14 UTC

The environment disconnected after the final build/native runs, offline replay and isolated restoration check. No later build/native execution is claimed. Through the Git connector, all14staged source files were reconstructed from the uploaded patch and verified against their pre-interruption Git hashes. The original restore manifest, test summary, restoration/replay records, performance record and benchmark source were also recovered byte-for-byte. 305of408original evidence artifacts are preserved; all final raw compressed GPU vertices/pixels, upload-detail matrices, console logs and PNG archives are included. The failed marker and compile-test sources are also exact recoveries. 103remaining originals include some launch/runtime-identity/build/package wrappers, the full audit/launcher scripts and standalone crash text; the missing list records their hashes. The first failure console and phase images remain available.

Build/native continuation currently needs environment recovery. Recover missing metadata or regenerate its audit from preserved raw inputs before treating the evidence bundle as fully replayable again. The preserved offline pass records describe checks completed before disconnection. Keep the candidate staged, the lease released and the task enabled while this interruption is resolved. No promotion, installer release or engine-completion claim follows.


## 2026-10-23 — synchronous water publication repair and fresh validation

Continued from `91c8b9488fd617c6cc9705cce490b503fc3c6058` in an isolated source directory. Restored pinned build/native tools and reproduced accepted CPU-mirror poisoning after a rejected GPU range write: 16 actual Vulkan routes retained832different bytes even after explicit retry. Staged separate transfer buffers and post-success mirror/deadline/cursor commits; added a read-only retirement predicate and native failure/budget fixtures. Six source files changed since R35 (two production, four validation); complete17-file staged restoration remains separate from the575public source inputs.

Fresh full25-task build and210000packet/core cases pass. Development/packaged software Vulkan and OpenGL fallback pass48rejection/manual-retry cases,192CPU/GPU reads,15336pixel comparisons/30672draws, zero bad channels beyond tolerance1 and zero uncovered pixels. Explicit result/save/shutdown/action/console/source-class-shader records pass; no new final crashes. Six archive CRCs,411class identities and five nested-module identities pass after catching and excluding a truncated outer JAR before launch and rebuilding serially. Its root cause is still unproven.

Each final session verifies33,554,304bytes across eight actualGPU pages,129explicit diagnostic retirement turns and136writes, each<=262080bytes. GPU/CPUreadback hashes match. CPU-enqueue p95 is0.750/1.182/1.368ms but maxima46.477/35.978/3.778ms (development Vulkan/packaged Vulkan/GL). Scanning/copy/submission exclude GPUcompletion and normal-frame work. Performance remains unaccepted; no ownerGPU/FPS,2048pagechurn or full-stream claim. Whole-cohort atomicity, automatic caller retry and asyncdevice-loss remain open.

Independent archived replay verifies15739files and full raw pixel/readback oracles; guarded restoration checks575base inputs before17writes. Thirty previously missing wrappers recovered with exact originalGitSHA;73original artifacts remain unavailable. R35 replay remains historical and lacks three action histories. New capture archives use<=512KiBparts for API recovery. Main, public source and released installers remain unchanged; V1/V2/V3 geography stays frozen. The continuation lease is released at this checkpoint; engine acceptance is unfinished.

Review [source, raw captures, rejected attempts and next step](validation/engine-continuation/water-publication-commit-2026-10-23/README.md). Next: profile/reduce measured submission outliers and boundary scatter, then normal-frame cap churn/memory, edits/height/opacity, retry policy and long streams/ocean. Existing LOD horizon/coverage, solver continuation, explicit budgets/chemistry and geology priorities remain open.


## 2026-10-23 — retirement cap profiling after the publication checkpoint

Continued implementation/audit after `afa0c20803c72d288434ebc91bec7746398e62e7`. Two validation source files changed; no additional production source changed. Added per-turn backend/encoder/CPU/GC observations, actual2048page GPU residency, staging occupancy and runtime fixture identity. Removed a redundant full-size fixture constructor upload using a private seed-quad/readable-buffer setup; allocation metadata and actual GPU contents are checked. Vulkan probes are skipped on OpenGL.

Eight additional native sessions pass explicit result, sealed-console, action, save/shutdown and no-new-crash checks. Five exercise2048privateGPU pages/33554304bytes/129diagnosticturns/2175writes. Max perturn17writes/262080bytes, max perrange16416bytes. All2048GPU/CPU hashes/session match an independent Python steady-packet encoding. These reflected pages bypass production admission/eviction and are not drawn; this does not establish churn, ordinary-frame, memory or full-scene acceptance.

The duplicate setup fills67108608of67108864stagingbytes, leaving256bytes before the first16416byte write. Its observed offset reset and56.727ms backend write follow unchanged StagingBuffer overflow code, which submits/flushes and waits for fences. Exclusive fence time is not measured; concurrent GC/other backend work and earlier outlier causes remain distinct. The cleaned2048page setup uploads33849216bytes. Repeating129turns inside one action still overflows atturn126; three single-setup maxima57.156/41.561/41.398ms do not establish a performance improvement. Final Vulkan p951.843ms/max41.398ms; final8pageGLp952.849ms/max16.534ms is not a matched APIbenchmark. Performance gates remain open.

Final Vulkan/GL runtime fixture resource hashes match the retained compiled class. Earlier diagnostic variants and their limits are retained. Production archive/class identities remain unchanged and pass. Independent archived replay verifies156capture members; guarded restoration verifies all17prior staged sources before applying the2validation overlays. Main/public sources/installers/geography/quantity are unchanged. Release the lease at this checkpoint; acceptance remains incomplete.

Review [profiles, cleanup source, exact captures and staging-path audit](validation/engine-continuation/water-retirement-profile-2026-10-23/README.md). Next: retire one slice per distinct rendered frame after controlled setup, capture staging occupancy and matched total-frame/memory, then admission/eviction churn, boundary scatter and streams/ocean. Restore the prior17-file publication candidate first, then the2-file validation overlay; the work state records both manifests. Other LOD, geology, pressure and water-budget requirements remain open.

## R38 — shared extraction budget corrected; rendered-frame retirement audited (2026-10-23)

Continued from R37 `f5e840c3f4475751003ae41b490ee0a3be0d0e4e` in an isolated directory after taking the released lease. Actual source change: staged `AnimatedWaterRenderer.extract` now derives its optional-work deadline from the existing entry timestamp instead of restarting a fresh2ms after mandatory retirement. Native reproduction with controlled8/12/20ms retirement stalls polled all4pending futures after each over-budget retirement; the identical compiled fixture now observes0optional probes in all9final development Vulkan/packaged Vulkan/GL cases, while preserving actual retirement and GPU bytes. This is1production file plus3validation files changed sinceR37; complete candidate19staged files(5production/14validation). No public source/main/installer promotion, solver change or V1/V2/V3 geography change.

Fresh full builds pass25executed tasks twice, including210000retained packet/core cases; final build includes the correction. An initial NeoForm-generated decompile archive failed before mod compilation (3407868bytes, SHA256 e98889de25ecfc02336eaf36147ac7380121752fef8d9b3c71036c677d094e6b); quarantine/retry passes, exact invalid bytes and logs retained, cause remains unproven. Final package checks pass6CRCs/411class identities/5nested-module identities with private agents and validation fixtures excluded.

Eight fresh native sessions pass their explicit world-save/shutdown/crash gates (creation uses WORLD_CREATION_PASS; the other7use CLIENT_SAVED_DISCONNECT_PASS), zero new crashes and Vulkan validationErrors=0/resources0. Three final regression sessions pass15336pixel comparisons/30672draws, maxchannel1/no uncovered pixels, plus48rejection/manual-retry cases/192paired reads/0differing bytes. The originalR36retrytarget baseline is explicitly historical; this checkpoint's deadline reproduction is fresh.

A new real-frame fixture retires one slice per distinct rendered frame after setup fencing/settling. Three final active sessions each verify2048actualGPU pages/33554304bytes/129frames/2175writes, max18writes and262080bytes perframe, no predicted staging overflow and all2048readback hashes matching an independent packet codec. Final development Vulkan retirementp95/max1.221446/11.126275ms; packaged1.200735/3.763378ms; GL1.086613/2.267559ms. Final steady Vulkan has0writes, retirementp95/max0.374763/0.894044ms. These private pages are not drawn and bypass admission/eviction; scene-water keysets differ and cached/visibleLOD tiles are0. Frame intervals include waits/limiting, heap/direct-pool records omit other native memory, and250ms controlled clock steps are not independent60Hz. No matched API speedup/full-scene/FPS/ownerGPU/performance acceptance follows. The optional admission fix is not a hard total2ms bound. Screenshot inspection leaves the distant cutoff unresolved.

[Source, exact evidence, failure reproduction and restore/replay instructions](validation/engine-continuation/water-shared-budget-2026-10-23/README.md). Independent replay verifies15789archived members and restores all581candidate build inputs after complete hash validation before writing. Prefer the new complete source snapshot into a nonexistent isolated directory over rebuilding the old17file+2overlay chain. Original575public inputs remain provenance-checked, new stagedmanifest lists19files. Restore JDK/Gradle and graphics with retained scripts/pins. Mesa remains25.2.8; obsoleteXvfb1.6URLs failed403/404, so the1.9Xvfb/common pair is explicitly repinned/checksummed from official Ubuntu metadata. No cross-environment performance claim. The73missing older original evidence files remain unresolved.

Next implementation: workload-matched real scene/admission/eviction cap churn and boundary scatter, then reproduce and correct a remaining retirement tail or optional-work starvation defect. Automatic retry/cohort atomicity, async device loss, streams/ocean continuity, complete enabled-view/LOD hierarchy and cutoff, pressure continuation/fanout/unpinned filling, renewable/finite water budgets/compatible-liquid chemistry and coherent geology remain open. Catalog/species remain separately deferred. Release this checkpoint's lease; engine acceptance is incomplete and implementation continues from this source snapshot.

## R39 — contiguous boundary submissions batched; expanded rejection/range audit (2026-10-23)

Continued from R38 `159f64617a8e9710f83ccb1182a040199a6b1b95` after taking the released lease and restoring581hash-verified inputs into a new isolated directory. Reproduced one GPU submission per changed boundary quad in the staged neighbor synchronizer. Focused correction combines only physically contiguous changed quads, flushes at unchanged gaps/page boundaries and splits at262080bytes. CPU mirror and queued cleanup deadline commit only after successful range submission. One production and2validation files changed; complete candidate20staged files(5production/15validation),582source inputs,579other prior inputs unchanged. No shader/vertex/uniform/topology/solver/quantity/chemistry/geography change, and no public-source/main/installer promotion.

Expanded before/after fixture is compiled-identical and covers256/2048changed quads, contiguous/eight-quad groups, both axes, origin/nearworldlimit, queued deadlines, first/second-range rejection, manualretry and idempotence. Final development Vulkan/packaged Vulkan/GL pass132cases/264paired CPU-GPU readbacks with an independent packet/range/deadline oracle. Contiguous256/2048quads reduce256/2048writes to1/2; eight-quad groups use32/256writes. Payload stays36864/294912bytes, maximum range262080bytes, no gap uploads and zero idempotent writes. Initial20case diagnostics and their original source/class/oracle are retained separately from expanded44case fixture.

Four fresh full builds pass25executed tasks and210000retained core cases each; no new corecase-count claim. Seven native sessions pass explicit save/shutdown/sealed-console/crash gates with0newcrashes; Vulkan validationErrors=0/finalbuffers0/images0 and deliberate-shutdown-rejection continuation pass. Final15336pixel comparisons/30672draws have maxchannel1/0uncovered;48prior-publication rejection cases/192pairedreads and9shared-deadline cases pass. Final package6CRCs/411class/fivenestedmodule identities pass; private validation/agent excluded. Eleven recorded build/native job intervals do not overlap.

The three final real-frame privateGPU controls each pass2048pages/33554304bytes/129distinctframes/2175writes, max18writes/262080bytes perframe with0predicted stagingoverflows and independentGPUhashes. Development Vulkan retirementp95/max1.608468/5.797546ms; packaged1.370791/15.202327ms; GL0.946683/1.356970ms. These are software-driver observations, not comparative API/FPS acceptance. New transient scratch is bounded262080native+144heapbytes versus prior144native, without retained-page/mirror growth; broad resident-memory acceptance is open. Expanded development scattered2048quad calls still6.04–10.43ms, contiguous2.01–4.66ms. Whole-cohort scan/latency therefore remains over2ms in larger controls. No hard total extraction bound is claimed.

All private pages bypass scene discovery/admission/eviction and are not drawn; admitted scene-water keysets differ and cached/visibleLOD tiles are0. Screenshots still show the distant cutoff. Controlled250ms/frame clock steps are not independent60Hz; frame/memory measurement scopes remain limited. No full-scene/capchurn/streams/ocean/ownerGPU/performance/market-superiority acceptance.

[Source, losslessly compacted exact evidence and restore/replay instructions](validation/engine-continuation/water-boundary-batches-2026-10-23/README.md). Fresh archived replay verifies980physical capture/build members, reconstructs all26155capture paths from968distinct objects, reruns both fixture variants and all native-data oracles, and restores582source inputs after complete hashes-before-write checks. Capture archives shrink39867410→29944089bytes without omitting paths; all parts<=512KiB. Prefer the complete source snapshot into a nonexistent isolated directory. R38same toolchain/Mesa25.2.8/Xvfb1.9 assets were reused; older missing73original evidence files and corrupt-generated-JAR cause remain unresolved, not freshly recreated. OriginalR36 retry baseline is explicitly historical.

Next: real workload-matched admission/eviction cap churn, then reduce or incrementally bound remaining scattered boundary scan/publication costs while preserving local corner continuity/retry safety. Retirement tails, automatic retry/cohort atomicity, async device loss, streams/ocean, enabled-view LOD/cutoff, pressure continuation/fanout/unpinned filling, explicit renewable/finite water/compatible-liquid chemistry budgets and coherent geology remain open. Preserve V1/V2/V3, player edits and the requirement register; catalog/species remain separately deferred. Release this checkpoint lease; acceptance is incomplete and continuation remains enabled.


## R40 — water admission reservation checkpoint (2026-10-23)

Evidence: [docs/validation/engine-continuation/water-admission-plan-2026-10-23/README.md](validation/engine-continuation/water-admission-plan-2026-10-23/README.md). Source and compact raw evidence are staged only; main/public production sources and installers remain unchanged. The R40 lease is released after this checkpoint; engine acceptance is incomplete and continuation remains enabled.

A guarded production-upload baseline with actual33,554,304resident bytes under the32MiB cap and real mesher-generated far/busy neighbors reproduces eight nominal growth denials destroying sixteen valid neighboring meshes. The focused renderer fix preflights sufficient strictly-farther nonbusy GPU bytes, then releases only the stable farthest-first sufficient prefix. Insufficient admission mutates no page; zero-GPU placeholders are excluded from byte reclamation and the separate discovery/page-count policy is unchanged. One production and two private validation files change; complete583-input snapshot/21staged files(5production/16validation), other580prior inputs unchanged. Quantity, geometry/graphics packets, edits and frozenV1/V2/V3 geography are preserved.

Three full25task builds and a scoped private compile pass; each full build retains210000core packet cases. Final developmentVulkan, immutable packagedVulkan andGLfallback pass48admission cases/24denials/432pairedCPU-GPU reads with zero denied resident losses and unchanged shared baseline packet bytes;15336seam pixel comparisons/30672draws,132boundary cases/264reads,48rejection cases/192reads,nine deadline cases and129distinct-frame retirement turns per session also pass explicit save/crash/resource/shutdown gates. Package audit verifies6archive CRCs,411class identities and5nested/standalone modules; two full candidate builds reproduce identical packageSHA256. Source/archive replay verifies583inputs before writes,15828logical capture paths/967content objects and983physical capture/build/rejected-package members. Every prior evidence blob remains intact.

Self-audit excludes four stale stronger-fixture attempts and one packaged startup failure: a global task rerun produced an empty built graphics descriptor despite nonempty source. Original class/compile metadata, exact failed archive and raw consoles are preserved. Private validation compilation now disables its cache/incremental outputs and checks immediate post-compiler, pre-native and loaded class identities. A fresh complete build restores the byte-identical previously audited package; the successful packaged launch excludes jar/jarJar rebuilding and verifies the outer package unchanged. Underlying writer/cache/artifact causes remain unproven; no shipping workaround is asserted. Ten native attempts include five accepted fresh results, four excluded stale attempts and one failed startup, with no new crash reports. All recorded launcher intervals are sequential; compact captures preserve every logical path/hash (35,084,078→24,835,584compressed bytes). Seventy-three older missing originals remain missing.

This is controlled nominal admission proof using private nondrawn pages, with a patterned near ballast allocation, not actual visible discovery/travel/capchurn or total GPU/process peak memory acceptance. Allocation/device failure after a sufficient plan can still lose victims. Synchronous planning/upload/scattered scans and retirement remain unaccepted against2ms: current private Vulkan retirement maxima10.981ms development/5.071ms packaged. Next implement matched visible-scene travel/edit/cap churn and bounded scattered publication, then continue long-stream/ocean and LOD/coarser coverage. Full enabled-view, ownerGPU/FPS, market comparison, reservoirs/chemistry/compatibility, pressure continuation and geology remain open as registered; catalog/species remains deferred. Substeps are not independent60Hz simulation. A checkpoint does not complete the engine.


## R41 — coalesced water boundary packet updates (2026-10-23)

[Source, raw controls and replay](validation/engine-continuation/water-quad-packet-2026-10-23/README.md) stage a focused CPU optimization. The fresh scalar native boundary control passes data gates but reproduces the admission overrun: large 2,048-quad routes reach 8.677 ms, and the complete control reaches 14.925 ms. The renderer previously decoded and encoded the shared packet separately per changed corner. It now gathers a mask, checks accepted bytes read-only and encodes a private quad's four vertices once for its changed subset. GPU counts/payloads/range grouping, 262,080-byte limit, 36-byte layout, shader fields, cleanup clocks and failure/retry ordering are unchanged. There is no new helper allocation or persistent history. Two production and two private validation files change; the complete 584-input snapshot has 22 staged files (5 production/17 validation), with the other 580 prior inputs unchanged. Quantity, physical solver, edits and frozen V1/V2/V3 geography are preserved.

Two full 25-task builds pass; the candidate retains 210,000 packet cases and adds 210,000 known-input/exact-scalar cases covering all 16 masks, wrap/long clocks, nonzero offsets and per-vertex opacity/light/nonmaterial bytes. Four software-native sessions pass: fresh scalar boundary control, development Vulkan, immutable packaged Vulkan and OpenGL fallback. Candidate controls pass 132 boundary cases/264 paired CPU-GPU reads with exact after-call/retry packets matching the fresh control; 48 admission cases/432 paired reads with no denied resident losses; 15,336 seam pixel comparisons/30,672 draws; 48 synchronous rejection cases/192 reads; nine deadline cases; and 129 distinct-frame retirement turns per session. The R40 admission and older publication baselines remain historical, not fresh validation. Save/disconnect, raw/sealed console identity, crash delta and Vulkan validation/resource/shutdown gates pass explicitly. Six package CRCs, 411 compiled class identities and five nested/standalone identities pass, with private checks absent and the packaged artifact unchanged across launch.

Pinned same-compiler scalar/bulk/bulk/scalar packet benchmarks use 16 warmups and 31 samples per mask, each sample processing 16 passes of 2,048 quads. Self-audit caught asymmetric per-update counting in preliminary controls; both timed counters were removed and primary controls rerun. Earlier classpath/instrumented diagnostics remain separate. Counter-free two-corner median samples are 3.669–3.683 ms scalar versus 1.864–1.880 ms bulk, with independently reconstructed identical final packets and sealed class bytes. The four-corner API-generality control is also retained. These are packet arithmetic observations, not GPU publication, full scans, frame/FPS or owner-GPU acceptance. Cold native routes vary and some regress.

The 2 ms gate remains open: candidate boundary maxima are 5.591 ms development and 5.722 ms packaged Vulkan; private retirement maxima are 6.711/10.144 ms. Whole-neighbor processing remains synchronous. Next implement bounded/resumable scattered publication with stable local corner clocks and retry commits, and match actual visible-scene discovery/admission/eviction under travel, edits and cap churn. Private pages and static lake captures do not establish full enabled-view, peak memory, sustained streams/oceans, LOD/coarser coverage, owner GPU or market comparison. Sufficient-plan allocation/device-failure atomicity and all prior artifact/native blockers remain open. Follow regional budgets/chemistry/pressure/geology requirements; catalog/species stays deferred. Solver substeps are not independent 60 Hz simulation.

Archive replay verifies all 584 source inputs before writing, 15,707 current capture paths and 1,027 physical capture/build/benchmark/historical-admission members, including pinned counter-free benchmark class/data oracles. Lossless capture compaction reduces 36,458,204 to 24,216,463 compressed bytes. Benchmark compaction retains 316 paths/14,893,985 logical bytes as 85 content objects/1,159,166 unique bytes. All recorded Gradle/native/timed benchmark processes are sequential. Earlier repository evidence is preserved; 73 older missing originals remain missing. No new build/native failures or crash reports occurred; benchmark instrumentation correction is recorded. Main/public production sources and installers are unchanged. The lease is released; continuation remains enabled and engine acceptance is incomplete.

## 2026-10-23 R42: index neighbor-facing boundary work; performance remains unaccepted

Read [the R42 source, controls and evidence](validation/engine-continuation/water-boundary-index-2026-10-23/README.md). The independently authored immutable direction index skips boundary quads that cannot share a corner with an incoming page. A known-coordinate960-quad full-boundary model visits7680entries before versus1088after over eight directions, with identical relevant masks. This is a work-count reduction, not real-scene latency acceptance. The586-input snapshot contains24staged files (6production/18validation); two production and three private validation files change sinceR41, while581prior inputs are identical.

Two full25-task builds pass. Five software-native sessions pass: cold and full-suite R41 controls, development Vulkan, immutable packaged Vulkan and OpenGL fallback. The three candidates pass768expanded boundary cases/1536paired CPU-GPU reads, all matching fresh controls and independent packet/rejection oracles. They also pass48admission cases/432paired reads,15336seam pixel comparisons,48publication cases,nine deadline cases and129distinct-frame retirement turns per session. Candidate core checks add688129coordinate/order/immutability/storage cases and retain both210000packet suites. Explicit saved disconnect, crash delta0, Vulkan validationErrors0/resources0/shutdown continuation, seven raw/sealed console identities, six archive CRCs,412compiled-class and five nested-module identities pass. Runtime hashes/disassembly confirm current indexed code; three lake screenshots were inspected. Offline replay checks971physical archive members,20926logical capture paths and586candidate/584control inputs without new native execution.

**Do not promote for performance or release an installer.** Full-suite Vulkan boundary maxima are2.538ms control,3.108ms candidate development and2.439ms packaged; observations are mixed. Cold batch-only112.745ms is diagnostic, not the primary comparison. Retirement maxima remain5.896/17.417ms. Construction adds CPU and bounded index storage: raw payload bound2,878,112bytes at nominal caps, excluding object/transient/process/GPU peak memory. Whole-neighbor publication remains synchronous; GPU range/payload/cleanup/retry semantics are unchanged. No full-scene cap-churn, ownerGPU/FPS or market acceptance follows.

Next implement bounded/resumable scattered/cohort publication with explicit cross-frame visibility, target supersession, edit/unload/reset and retry rules, then validate actual visible-scene travel/edit/cap churn and sustained streams/oceans. Keep distant LOD/coarser coverage, broader mixed transparency, allocation/device-loss/automatic-retry behavior, water budgets/compatible constituents, pressure continuation and geology open; catalog/species stays deferred. Prior artifact/marker/reload/atlas/save causes remain unresolved;73older missing originals remain missing. Quantity, player edits, modular Vulkan/GL, frozen geography, main and supplied installers are preserved. Engine acceptance is incomplete.

## 2026-10-23 R43: bounded recovery after rejected water-boundary writes

Read [the R43 source, controls and replayable evidence](validation/engine-continuation/water-boundary-recovery-2026-10-23/README.md). A fresh R42 product control propagates 40 controlled pre-backend boundary-write rejections and has no automatic continuation; its private fixture catches these so the client can save. The focused renderer/core fix defers rejected ranges, retains at most 16 ranges/1 MiB of vertex bytes, and attempts one write of at most 65,520 bytes per distinct rendered frame. Retries rebase only requested material corners over current accepted data, preserve newer nonmaterial fields and unselected corners, expire wrapped transitions, cancel superseded/stale pages and clear retained work on release/reset. Queue overflow requests a bounded fresh mesh. Three consecutive recovery-write failures remain fatal. Normal successful publication stays synchronous.

Two full 25-task builds and four software-native sessions pass: fresh product control, development Vulkan, immutable packaged Vulkan and modular OpenGL fallback. Candidate core checks add 470,000 recovery cases and retain the index/packet/terrain/watershed/module suites. Candidate native checks pass 120 retry/safety cases and 360 paired reads over 240 distinct recovery frames: 48 transient recoveries, 48 supersession/stale cancellations and 24 expected bounded terminal controls. Regression checks pass 768 normal boundary cases/1,536 paired reads, 48 admission cases/432 reads without denied resident loss, 15,336 seam pixel comparisons, 48 publication-consistency cases, nine deadline cases and 129 distinct-frame retirement turns per candidate. The new recovery baseline is fresh; normal-boundary/admission/publication regression baselines are historical. Explicit save/disconnect, crash delta zero, Vulkan validationErrors/resources zero and shutdown continuation pass. Six closed raw/sealed build/native console identities, six package CRCs, 413 compiled class identities and five nested-module identities pass. Three static lake screenshots were inspected, without treating them as temporal/full-coverage acceptance.

Self-audit retains one failed private compilation before Minecraft started (missing checked exception declaration), an audit-script syntax correction and two corrected oracle assumptions. No product/native code was changed to satisfy those oracle corrections: newly constructed steady transitions start at age zero, and only failed ranges are rebased. All final independent captured-byte checks pass. A transient execution-transport disconnect returned the same isolated workspace; source/sealed evidence identities and original/compacted replays pass after reconnect.

The 588-input snapshot contains 26 staged files (six production/20 validation); two production and five private validation files change since R42, with 581 prior inputs unchanged. Offline replay verifies 1,092 physical archive members, 15,740 logical capture paths, all 588 candidate inputs and 587 reconstructed control inputs without new native execution. Lossless snapshot deduplication reduces archives from 82,111,663 to 29,827,493 bytes while reconstructing every original whole-file hash. Work-state graphics/restoration aliases now point to this checkpoint instead of stale R41 values. Earlier evidence is preserved; 73 older originals remain missing.

Do not promote or release an installer. Private recovery maxima are 0.875/1.636 ms in development/packaged Vulkan, but private retirement maxima remain 10.225/9.923 ms; normal publication remains synchronous and the hard 2 ms gate is unmet. Raw queue bounds exclude total object/process/GPU peak memory. Pre-backend fault injection does not establish partial-write/async-loss atomicity or whole-cohort visibility. Next implement bounded normal cross-frame cohort publication with a visibility policy, then real visible-scene travel/edit/cap churn, overflow-to-remesh completion and pending-recovery/retirement interleavings. Streams/oceans, enabled-view/LOD/coarser coverage, regional budgets/compatible constituent chemistry, pressure continuation and geology remain open; catalog/species is deferred. Preserve water quantity, edits, frozen V1/V2/V3 geography and modular Vulkan-first graphics. Solver substeps are not independent 60 Hz; software checks are not owner-GPU/FPS or market evidence. Main, public production source and supplied installers are unchanged; all native/build jobs are closed and the lease is released. Engine acceptance remains incomplete.

## 2026-10-24 R44: demand-driven retirement evidence and consolidated-main reconciliation

See [R44 reproduction, source and replay](validation/engine-continuation/water-retirement-idle-2026-10-23/README.md) and [merged-source reconciliation](validation/engine-continuation/r44-main-reconciliation-2026-10-24/integration-audit.json). Two production and three private-validation changes since R43 remove fully steady pages from mandatory retirement polling. Successful normal/retry writes rearm a page without resetting an in-progress sweep; live pairs remain scheduled; rejected retirement writes preserve accepted CPU bytes/cursor/pending state. The 32-header/262080-byte per-turn limits remain. Quantity, solver, edits and geography are unchanged. Normal successful neighbor publication remains synchronous.

Two complete 25-task builds, 60000 new core cases and five independently sealed software-native sessions pass. One interrupted build has unknown exit and is retained/excluded. Fresh R43 control keeps 2048 private pages queued across16 idle frames; all four candidates reach zero. The modeled4193280 baseline scanned bytes across those idle turns versus zero is a source/queue work model, not an instrumented CPU-byte counter. Each session has145 distinct rendered frames,2048 independent packet/GPU comparisons and a separate raw paired lifecycle fixture. Three full candidates retain120 retry cases/360 paired reads over240 recovery frames,768 normal boundary cases/1536 reads,48 admission cases/432 reads with no denied resident loss,15336 corner pixel comparisons,48 publication cases and9 deadline cases. Normal-boundary/admission/publication controls remain historical. Explicit save/disconnect, crash delta zero, Vulkan error/resource zero and shutdown continuation pass. Seven sealed build/native consoles, six archive CRCs,413 compiled classes and five nested identities pass. Offline replay verifies1093 physical members,15757 logical paths and589 candidate/588 control inputs; raw readbacks are reconstructed losslessly.

Hard2ms remains unmet: private steady candidate retirement max16.692ms and full development/packaged Vulkan4.315/4.811ms. Context changed between control/candidate; equivalent host timing conditions are unestablished, so no controlled latency, API or FPS comparison follows. Static lake views still show page-like water bands; their cause needs matched view/load-state analysis. These tests are not seamless visible-stream, full-scene/cap-churn, ownerGPU/FPS, independent60Hz or market acceptance.

The owner's consolidation commit28b2625 already integrates the exact27 R44 staged files, Themes alpha.8 and an independent Maritime1.21.1 project. This reconciliation changes no production code. It verifies669 engine/Themes files against Git,459 packaged compiled classes, seven root/nested CRCs, the existing31-task build, the merged GUI success marker and40 capture hashes. Eight Journal-tab images were inspected. Raw build/client logs are now sealed with SHA256. OpenAL/cursor/shader-output/deprecation warnings remain. The GUI task dispatched root and standalone clients; their records are not independently attributed, and its separate shutdown-completion record is absent. Do not treat that GUI session as a rerun of R44's independently sealed water suite or Windows GPU validation. The delivered merged Windows preview remains experimental. Maritime is a separate project and is not inside this26.1.2 JAR.

Continue from **main**, preserving Themes and all consolidation files. The589-input snapshot is an archival engine-only reproduction, not the current merged source: never restore it over main. State/audit pointers now reflect this, while older records remain historical. Next implement bounded cross-frame normal publication with explicit visibility/edit/unload/supersession rules, then run independently attributed merged packaged water gates and actual visible travel/edit/cap churn, recovery/retirement overlap and overflow-to-remesh completion. Streams/oceans, water budgets/compatible constituent mixing, pressure continuation, geology and enabled-view/LOD remain open; catalog/species remains deferred. No new installer acceptance or engine-completion claim. All jobs are closed and the lease is released.

## 2026-10-24 R45: isolate root-client validation dispatch

[Reproduction, code change and raw evidence](validation/engine-continuation/root-client-isolation-2026-10-24/README.md). Fresh consolidated Gradle dry-run reproduces unqualified `runClient` selecting both the root engine and standalone Themes clients. The proxy helper now accepts opt-in `--root-game-task runClient` or `runServer` before `--`, appends the absolute root task and rejects conflicting game selectors before Gradle starts. README documents it. Legacy arguments and explicit standalone task paths are preserved; use the new option or `:runClient` for engine-only validation.

Three real offline Gradle graphs pass: control selects two clients, opt-in selects only root, explicit Themes selects only Themes. Eight isolated argument controls pass (client/server, legacy/standalone preservation and four conflicts). All three compressed graph logs match recorded original SHA256. This changes one development-tool file and README; no engine runtime, solver, shader, geography, water quantity or installer changes. No full build, Minecraft session, screenshot, timing or ownerGPU/FPS acceptance is claimed.

Another foreground native job was observed while the work-state lease still named this worker. Owned-process cleanup was requested and no further Gradle/native jobs started; other checkout/jobs were untouched. Final sealed results corrected an initial assumption that the third dry-run was interrupted: it had completed successfully, as had its four parser-conflict checks. Optional psutil was unavailable; cleanup used standard-library process ownership. The additional eight argument controls started no Gradle jobs. The shared-cache/task graphs are not performance evidence.

Next use scoped root dispatch for the merged packaged Vulkan water suite with independent save/crash/resource/shutdown completion records once foreground work permits. Bounded normal cross-frame publication, actual stream/ocean temporal continuity and scene churn, hard2ms, LOD, regional water budgets/constituents, pressure and geology remain open; catalog/species stays deferred. R44 graphics evidence remains the latest engine-native checkpoint. Own lease released; engine acceptance incomplete.

## 2026-10-24 R46: staged fix for a regional spring-rate cache bypass

[Source reproduction, candidate and evidence](validation/engine-continuation/regional-budget-cache-2026-10-24/README.md). Alpha26 main c7a0373 contains finite accounts and saved V4 terrain; its full/native evidence is retained in VALIDATION-0.0.1-alpha.26.md. This continuation found a separate hydrology rate-cap defect. Chunk-unload cache cleanup can clear an unrelated RegionWork inside its persistent aquifer's current20tick interval. Reconstruction initializes discharged=0, permitting another shared32unit allowance without a new aquifer step. It does not duplicate water: each emission still debits finite groundwater.

One focused staged runtime file names the existing limit and initializes reconstructed regional budgets exhausted. Only the existing successful aquifer.step reset authorizes another allowance. Repeated cache loss can conservatively suppress delivery; it cannot grant extra output. No saved account format, chemistry, generated geography, engine rendering or installer changes.

An independent harness extracts the actual RegionWork/reset/guard/remaining-limit expressions and compiles them with unchanged actual Aquifer/WaterAccount using retained JDK25. Across861 cache schedules/172200 modeled visits, baseline fails with54972 over-budget observations and maximum80units per interval. Candidate passes with zero and maximum32. Exact groundwater+delivered quantity is checked after every visit; ordinary interval delivery and one allowance after an offline gap remain32. The first private generated fixture failed compilation due to an overbroad placeholder replacement; correcting only the extractor resolves it. No product workaround was made for that fixture error.

These are source-extracted modeled visits, not Minecraft ticks, native events, independent60Hz or performance acceptance. The fix is preserved under candidate/, not copied into public production source. No full/native/visual build or new installer was made. Next exercise actual unrelated-region unload cache clearing, reload inside the same interval, multiple outlets, ordinary throughput, chemistry and save/reopen before promotion; then resume the owner-requested terrain/cave/material expansion and bounded Vulkan publication work. R44 latency/full-coverage/ownerGPU and remaining environmental/geology/LOD gates stay open. Own lease released; acceptance incomplete.

## 2026-10-24 R47: full build and packaged hydrology validation of R46 candidate

[Source identities, raw logs, fixture and package audit](validation/engine-continuation/regional-budget-native-2026-10-24/README.md). Restored current-main engine/Themes inputs against Git into a new isolated build; only the unchanged R46 staged HydrologyRuntime was applied. One new private RegionalCacheChecks fixture and local runners/audits were authored. No additional runtime fix or generated-geography changes since R46. Independent Maritime and standalone Themes wrappers are outside this engine-only build snapshot.

The complete forced31-task Linux-Vulkan bundle build passes core accounting, R44 retirement, V4 terrain, full watershed, cache and module gates. Seven root/nested CRCs and475compiled product classes match; private validation is absent. Native class origins confirm the packaged root and standalone core; that core equals the embedded module. The archive remains unchanged across both accepted server runs.

Two packaged dedicated-server processes pass explicit success records, all-dimensions saves, requested stop and zero crash reports. Existing hydrology checks cover shared outlets, blocked/head-limited delivery, infiltration, measured bucket chemistry/advection, fixed-stage ocean accounting, finite inland water, actual weather debit, codec/once-only initialization and no forced unloaded outlet. The new fixture uses the production loaded-column visit path: eight accepted outlets deliver32units, then actual NeoForge unload-event dispatch clears a controlled two-region/two-cursor cache. Eight further visits retain total32units with exact groundwater/ocean/atmosphere/fixture-water conservation and unchanged aquifer interval. A separate process reopens saved V4 and verifies groundwater, dynamic wet age, composition and player edit retention.

The first private fixture compilation failed before Minecraft started: ServerLevel.setGameTime is unavailable. Retained failed source/logs; corrected the fixture to use the actual game clock and an earlier aquifer step. Runtime candidate unchanged. Both subsequent server sessions pass. No performance or graphics-client test was run; a Linux Vulkan bundle on a dedicated server is not Vulkan-driver or ownerGPU/FPS validation.

This is native event fault injection, not physical chunk eviction: cache population is fixture-controlled and the unrelated chunk remains resident. Consequently the candidate remains staged under the evidence path; main production source and owner installer are unchanged. Next exercise server-driven physical eviction/reload, repeated unrelated-region churn and multi-region throughput/chemistry before promotion, then resume terrain/cave/material authoring and bounded Vulkan water publication. Full engine, hard2ms, enabled-view/LOD, Windows and ownerGPU/FPS gates remain open. Own jobs closed and lease released.

## 2026-10-24 R48: physical eviction/reload passes; regional rate fix promoted

[Physical test, promoted source, package identities and all attempts](validation/engine-continuation/regional-budget-physical-2026-10-24/README.md). One production HydrologyRuntime file (unchanged R46 candidate) now fails closed after RegionWork reconstruction until the next aquifer step; one new private PhysicalRegionalChecks fixture is retained under src/visualValidation. No saved-state, geography, quantity, composition or graphics change. R46/R47 staging-only status is historical; continue from current main, not the archived partial snapshots.

Two regions each deliver32units through actual loaded-column visits. The real server chunk manager services released auxiliary and outlet tickets through two physical churn cycles. Checks observe64 auxiliary FULL evictions,32 different auxiliary LevelChunk instances after reload, four different outlet instances, and64 real auxiliary unload events. Cache clearing and absent FULL lookups are observed, not injected; no cache map mutation or synthetic event dispatch occurs. Outlet water survives disk reload. Both regions remain at32units in the same aquifer interval, then resume another32each after an explicit20tick saved-clock advance. Exact water/salt/sediment totals pass after each cycle and interval. Chunk-manager service loops keep the level clock unchanged during eviction: these are controlled native operations, not independent60Hz or natural-world throughput evidence.

Two accepted packaged server processes pass the physical fixture, existing hydrology suite, and separate saved V4/groundwater/dynamic-age/composition/player-edit reopen. Explicit success files, requested stop, all-dimensions saves and zero accepted-run crash reports pass. A fresh forced31task full build passes accounting, terrain/watershed, cache and module gates. Seven archive CRCs and475compiled identities pass; all recursively extracted native/fresh-build member bytes match. Relocation changed standalone core ZIP metadata, so raw core-container equality is false; every core member still equals the embedded core.

Self-audit retains and excludes three actual private-fixture game crashes despite Gradle exit0: ocean coordinates produced no spring output, guessed pruning counts ignored forced-ticket neighbor loading, and an attempt retaining outlet tickets failed the combined physical/cache-clear gate within its bound. The accepted fixture selects non-ocean sites, waits for observed state, releases outlet tickets too, and proves different outlet instances. Runtime source never changed during these fixture corrections. Do not reinterpret rejected Gradle0 attempts as passes.

The scoped functional hydrology gate is closed and the one-file fix is promoted; broader natural-region/season/storm throughput, memory/latency and editable-world coverage remain open. No owner installer was released; the supplied Alpha26 JAR predates this promotion. Dedicated-server execution is not Vulkan graphics, Windows, ownerGPU/FPS, hard2ms or complete engine acceptance. Resume current terrain/cave/material and bounded Vulkan publication priorities, preserving frozen V1-V4 geography and edits. Own jobs closed; lease released.

## 2026-10-24 R49: staged bounded neighboring deposit field

[Core reproduction, four candidate files and build/legacy evidence](validation/engine-continuation/deposit-field-2026-10-24/README.md). V4 core ore lookup considers only its current32block source cell, although an ellipsoid can extend into an adjacent cell. The test finds1072 eligible missing-neighbor core cases; first seed0/x=-4064/z=-143/y17/COPPER uses the declared deep-bed input, not a native cave/soil/actual-block assertion.

Two staged core files add DepositField and share Geology's exact host/ellipsoid predicate; two private validation files add its oracle and invocation. Each immutable per-column field builds9 neighboring sources, queries at most9 with no query allocation/world lookup, and uses absolute-grid X/Z overlap priority. Raw arrays total324bytes, excluding host/object/allocator/process/GPU overhead. Construction adds45 source hashes per column; profiling remains open. Code review moved metadata fill before immutable-object construction, then repeated the full build.

The wider49source oracle passes90960 cases including25000 signed grid seams, deterministic replay and near-surface exclusion. A separately compiled legacy harness produces identical SHA256 across4,280,000 geology/rock/ore results per version against baseline and final candidate core. Final forced31task build and an intermediate candidate31task build pass existing water/R48 runtime compilation, V4/legacy terrain, full watershed, cache and module gates plus the explicit new deposit pass record. Seven archive CRCs/477compiled classes/private-test exclusion pass. No failed checks or native game sessions.

Candidate source remains under staged/, with no production generator/default-pack/codec/saved-revision or public source changes. No installer was released. The field is not called by active worldgen; the predicate refactor is staged too. Its new distribution/overlap priority requires an explicit new saved terrain revision before activation, preserving existing V1–V4 output. Next wire opt-in versioning and native base-column/chunk equality, chunk/order seams, codec/save, host/cave/soil and density/overlap checks, then constructor/query/allocation profiles. These core/build checks do not establish native visual, frame/FPS or ownerGPU acceptance. Broader authoring/cave, water, Vulkan publication and LOD work remain open; catalog/species deferred. Own jobs closed and lease released.


## 2026-10-24 R50: staged V5 native deposit integration and footprint pruning

[Source, native records, cost probe and rejected attempts](validation/engine-continuation/versioned-deposits-2026-10-24/README.md). Since R49, the candidate adds an explicit continental_v5 codec/registration and threads its field through both native generation paths. V5 retains V4 shape/water/caves/surfaces and changes only deposit lookup; existing V1–V4 paths/default presets remain unchanged. A nine-bit immutable horizontal footprint mask skips unreachable sources while retaining predicate arithmetic and absolute overlap order. A chunk shares its field wrapper. Five production candidate files and three private checks are staged; no public production path or installer is promoted.

Final nonincremental forced31task build, two intermediate full builds,90960 oracle cases, seven archive CRCs and477compiled product identities pass. Fresh public-baseline native control has96complete chunk digests across two seeds/revisions1–4/twelve sites; every digest matches the candidate. Candidate checks8393529base-column blocks and1305522native underground blocks against an independent49source oracle. Fifty-eight actual blocks change (35emerald/23copper in this selected sample). Protected states match, including80204cave-air,259565water,21128soil-band and12288bedrock-band blocks. Twenty-four reversed-order chunks match. An actual private-preset V5 world saves/reopens with revision5, a gold-block edit and240260underground FULL-saved-chunk comparisons. Final baseline/reopen records verify runtime fixture hashes, package/core identities, requested stop, all-dimensions saves and zero crashes. Dedicated-server execution is not Vulkan graphics, Windows, independent60Hz or ownerGPU/FPS evidence.

Six interleaved CPU microprobe processes retain identical query checksum5064. Sparse-field median query time falls35.28→2.09ns (94.07%);6843of8192columns have empty footprints. Construction increases409.65→436.49ns and escaping host+field allocation472→480bytes/column, eight extra bytes for the mask/object layout. Final field increment over the88byte legacy host is392bytes; arrays contain324rawbytes. Queries allocate zero measured thread bytes. These are scoped CPU/allocation observations, not full-generation/frame/peak-memory or market acceptance.

Four attempts are excluded: private syntax and vanished generated Themes resource before Minecraft; empty changed-ore coverage causing a real game crash despite Gradle0; and expanded baseline source retaining an eight-site compiled fixture. The latter cannot establish twelve-site coverage. Resource/stale-compile causes remain unproven. Final isolated validation outputs were rebuilt with incremental compilation disabled and executing fixture hashes checked; fresh baseline has96digests and candidate120. Sources, bytecode, failed console hashes and crash report are retained; no speculative product workaround.

Functional/version/persistence gates pass but promotion remains blocked on broader all-ore/host/density/overlap balance, native client visual transects and matched whole-chunk timing/allocation. Apply only the eight staged files to an isolated current-main checkout; never activate the change inside V4 saves. Continue terrain/deposit/cave authoring and bounded Vulkan water publication after these gates. Existing full water/pressure/LOD/ownerGPU acceptance remains open; catalog/species deferred. Supplied installers are unchanged; own jobs closed and lease released.


## 2026-10-24 R51: staged lazy deposit arrays and independently verified native coverage

[Source, all native attempts, cost samples and compiler guards](validation/engine-continuation/deposit-allocation-2026-10-24/README.md). Since R50, one production candidate file changes: DepositField reuses private empty arrays and allocates fixed metadata only for columns with a reachable footprint. Nonempty query arithmetic/order and all geography remain identical. Final source retains R50 source-level hash expressions/order; an earlier extra hash/axis-pruning variant is archived after worse pooled p95 in all four native cases. Eleven files are staged (five production, six private); no public production path, default/saved V1–V4 or installer is promoted.

Final forced31task build passes4194304R50-reference comparisons (1542ore hits),90960independent oracle cases and existing water/terrain/watershed/cache/module gates. Independent direct JDK25 compilation of44core source files matches all71classes per variant byte-for-byte. Seven archive CRCs,477compiled product identities and private-class exclusion pass. Four final matched packaged server processes verify independently derived loaded production-class hashes, identical fixture hashes, explicit stop/all-dimensions saves and zero crashes. Each reopens a copy of the same saved V5 input and retains revision5, player gold edit and240260FULL-saved-chunk comparisons. The synthetic world input is archived with file hashes for reproducibility.

All120R50 native chunk digests remain unchanged, including96V1–V4. Expanded native generation preserves128complete V5 digests across16seeds and verifies sixteen terrain-valid witnesses for each of the eight currently implemented ore kinds against the wider oracle/native blocks/base columns. Fourteen of21compatible ore/host pairs are observed; missing COAL:SANDSTONE, IRON:BASALT/CALCITE/DIORITE/GRANITE/SANDSTONE and REDSTONE:GRANITE remain explicit. Witness selection is targeted, not unbiased ore-density/gameplay balance. Larger catalogs/datapack authoring and catalog/species work remain separately unfinished/deferred.

Matched warm server-thread ProtoChunk+full fill/decorate allocation decreases in all four cases by pooled medians13320–80280bytes/chunk, with33/81/125/219active footprint columns. Lighting/biome/network/render pipeline and process/GPU peak memory are excluded. Final p95 is better in three sampled cases but7.23%worse in one; medians are mixed. No general latency acceptance follows. Sparse escaping-column allocation480→179.2822B/column and dense480→427.96875B; query allocation0 and query timings vary less than1% in opposite directions. Construction controls vary. Raw data and unexplained per-process allocation variation are retained, without attributing them to the change.

Two builds are rejected: unreadable copied .rsync-tmp output before Minecraft, and a31task Gradle0 nonincremental build retaining the earlier outer class despite revised source. Valid CRC/compiled-output equality was insufficient. Revised-source acceptance requires independent source-to-bytecode comparison; clearing only own candidate outputs plus the final rebuild produces correct bytes, then native guards confirm them. Causes remain unproven; no speculative product workaround. Earlier source/native timing records are preserved as earlier-variant evidence, not final-source passes.

Keep V5 staged. Next cover the seven missing host pairs and Vulkan client material/ore/cave transects, broader density/overlap balance, and investigate matched tail-latency context with compilation/GC/host telemetry before promotion. Preserve source/compiled/runtime identity guards even with incremental compilation disabled. Then continue terrain authoring and bounded Vulkan water publication; full stream/ocean, pressure/water budget, enabled-view LOD and ownerGPU/FPS gates remain open. Dedicated-server runs are not graphics, Windows or independent60Hz evidence. Supplied installers are unchanged. Own jobs closed and lease released; engine acceptance incomplete.


## 2026-10-24 R52: frozen V6 fixed-ensemble verification

Alpha.27 at `65705ec9f6eed1c9ecc26e6e8d54cd908707babc` supersedes historical R49–R51 staging-only status: V5 support and V6 authoring are now production, and delivered V1–V6 geography is frozen. Continue current main; do not restore old staged snapshots. See [alpha.27 acceptance and failures](VALIDATION-0.0.1-alpha.27.md).

[Fresh fixture, raw results, source identities and limits](validation/engine-continuation/v6-ensemble-2026-10-24/README.md). This continuation changes validation/evidence only. No new runtime defect was reproduced and no production fix or installer is proposed. A new independent wider trigonometric oracle agrees with116279272 voxel queries across262144columns/1024chunks/four seeds/eight latitude strata. Reversed definition order agrees everywhere;91eligible overlaps retain priority. The final fixed-seed chunk selection corrects a regularly aligned initial grid, and full integer-Y coverage corrects a coarse stride that missed a one-voxel emerald witness. All diagnostic samples remain separate; no failed product check or timing selection is hidden.

49core sources plus frozen profile/check inputs match immutable Git blobs before/after fresh direct JDK25 compilation/execution. Existing V6 checks freshly pass8712columns/110398cave cells, and existing authored-deposit oracle passes1559040comparisons/13631hits. Final ensemble has51370pre-carving eligible ore voxels, five ore kinds and14host pairs. Gold/redstone/lapis are absent from this bounded sample, not proven absent in native generation; alpha.27's selected native witnesses cover all8. Deep-like bounds, core-only field counts, fixed strata and omitted cave/soil/native filtering prevent balance acceptance.

Read-only review confirms alpha.27's four committed Standard/Deep save/reopen records and two software-Vulkan client records, with no recorded crashes; this continuation did not rerun Minecraft or inspect those PNGs. No fresh full Gradle/native/graphics/performance or Windows/GPU check ran. Broaden native rare-host/density/cave evidence before any V7 authoring changes, then continue bounded Vulkan water publication, enabled-view LOD, pressure and water-budget work. Engine acceptance incomplete; owned jobs finished and lease released.


## 2026-10-24 R53: prevent unreadable LOD cache replacement

[Production fix, real-file reproduction, source/package identities and limits](validation/engine-continuation/lod-store-budget-2026-10-24/README.md). One promoted LodStore.write preflight now enforces the existing reader's per-name/aggregate palette character budgets before any path mutation. Baseline accepts a257×4096character palette, replaces the old revision and then cannot read its own result. Candidate rejects that request and preserves the prior compressed bytes/revision; no cache format, terrain, quantity, player-edit, graphics or preset change. One private fixture and its CoreChecks invocation are added.

Nineteen real-filesystem checks pass directly and again against the actual embedded product core: invalid first/replacement writes, exact-limit ASCII/three-byte UTF roundtrip, stale valid revisions, temporary cleanup and separate-store reopen. Fresh forced31task full build passes existing V6/legacy deposits/terrain/water/watershed/cache/module gates. Independent direct compilation matches49core sources/87classes; seven archive CRCs,502compiled product identities and45private-class exclusions pass. The packaged IO process verifies the loaded LodStore hash against independent source before the checks. 660parent engine/Themes inputs are Git verified; only the one runtime file and private checks change.

The initial reporter assumed the earlier alpha27 record's32task count. Actual build exits0 with31executed tasks and every semantic PASS record; that assertion alone is corrected without altering/repeating the build. Original rejected reporter output and task list remain. No behavioral/native-game failure occurred. No new Minecraft, visual, GPU, Windows or performance session ran; packaged JVM IO is not graphics/60Hz/FPS evidence. No owner installer released; delivered alpha27 Windows JAR predates this fix.

This closes the scoped cache replacement defect, not general rectangular horizon cutoffs, beyond8192key discovery/coarser fallback or full enabled-view LOD. Continue native rare-host/density/cave work and bounded Vulkan water publication, streams/oceans, pressure/budgets and ownerGPU/performance gates on current main. Delivered V1–V6 geography remains frozen; new geography needsV7. Existing corrupt files are not repaired by preflight. Own jobs finished and lease released; full engine acceptance remains incomplete.


## 2026-10-24 R54: staged entry-bounded nearest-key LOD cursor

[Source, real-file prefix reproduction, fresh tests and integration blockers](validation/engine-continuation/lod-paged-index-2026-10-24/README.md). Current production still scans only the first8192directory matches before nearest sorting. The source-extracted actual scan omits a readable saved page at a chosen camera among8205valid current-format snapshots. One independent LodDiskIndex component is preserved under staged/, not public production. It enumerates all entries over1–256entry calls, including invalid names in the quota, and retains at most8192nearest canonical keys for a fixed camera. Exact full-int squared distances use an unsigned sum/carry with stable ties. No world/chunk loads, writes or geometry reads occur inside this cursor.

The final real-filesystem fixture passes14303checks. Its8205page case finishes in33calls at quota256and matches an independent BigInteger full-sort oracle, including the omitted camera page. Four fresh camera rescans, malformed names, three signed-extreme ordering cases, per-turn/heap/result limits, completion close, normal cancellation and missing-root behavior pass. Immutable Git blob/source/extraction identities and compiled hashes are retained. A first compile attempt referenced pruned inputs and was rejected before fixture execution; restored current-parent LodStore/ColumnarChunk then compile/pass using the retained system Java17jdk.compiler module. No product/native failure is concealed.

Pinned JDK25/JDK21/Gradle/native/dependency caches were removed by workspace maintenance. No new full build, current JDK25/core-module/package/native Minecraft, visual, Vulkan/OpenGL, performance or installer validation ran. Prior R53/alpha27 passes are historical. The cursor is not connected to the renderer; production prefix cutoff remains. Next restore the environment and implement a fair one-turn worker adapter with world/session/camera cancellation, shared8192key recovery/edit guards, actual cap-full behavior and native Vulkan travel/save/shutdown/coverage tests before promotion. Directory latency and bounded final8192key sorting/copying are not a2msdeadline or rendered-frame guarantee; spatial hierarchy/coarser fallback/rediscovery remain incomplete. Frozen V1–V6, water quantities and edits are unchanged. Own jobs closed and lease released; continue engine work.


## 2026-10-24 R55: restored environment and staged LOD worker lifecycle

[Staged source, restored inputs, fresh build/executor/package evidence and limits](validation/engine-continuation/lod-worker-request-2026-10-24/README.md). One new staged LodIndexRequest wraps unchanged R54cursor. Render-side cancellation only signals; open/advance/close stay on the original worker. Completed results carry owner/epoch/camera and explicit canPublish rejects stale/late-cancelled or same-view replacement owners. One private real-executor fixture/invocation is added. Four files are staged, with no public production, preset, geography, water, edit or installer change.

Five pinned tool archives and private compatibility checks pass.661immutable-parent engine/Themes inputs are restored/Git-verified;158resource files come from the checksum-verified delivered alpha27 artifact. Final direct, full-build and embedded-core JVM runs pass347checks on an actual one-thread/two-slot executor over8205directory entries, including33turns/32interleaved geometry jobs, bounded work, queued/mid cancellation, wrong-thread close rejection, submission rejection/retry, publication guards and close before graceful shutdown. R54's actual-readable-snapshot regression freshly passes14303checks under JDK25. These are JVM metadata/executor/IO checks, not Minecraft/rendered frames/independent60Hz.

A fresh PTY-backed forced31task build passes all core/water/V6/legacy/cache/module gates. Independent direct compilation matches51sources/92core classes; seven CRCs,507product-class identities and46private-class exclusions pass. Loaded LodStore/cursor/request hashes match independently derived source in final packaged347check execution. Both audited candidate package hashes are identical. No new Minecraft, graphics/driver, Windows, visual/FPS or performance run occurred; no installer supplied.

The first cold build returns0 and its daemon reports all31tasks/pass markers, but ordinary stdout is truncated at startup, so that capture is rejected. Raw stdout/daemon/report retained, including registry teardown integrity warning; cause unproven. Separate fresh terminal capture provides complete accepted records, followed by source/package verification. Earlier340check request source/results are intermediate before the final canPublish API and seven added checks; final347records are authoritative. No failed behavioral/game check is hidden.

R54's toolchain/source restoration blocker is closed. Use isolated lod-lifecycle-1232/project with sibling tools/cache, but reconstruct current main and apply only four staged files if source changes. Actual VoxelLodRenderer adapter is still missing; production prefix cutoff remains. Next integrate fair one-turn scheduling, existing shared-key/edit guards and current owner/epoch/camera publication checks, and replace/disarm shutdownNow only after proving cleanup drains on the same worker. Saturated cleanup queue, worker loss/IO faults, live camera/world/reset and native Vulkan travel/edit/save/shutdown/visibility require validation before promotion. Full spatial LOD/coarser fallback, water publication/pressure/budgets, native rare-host/cave and ownerGPU/performance gates remain open. Frozen V1–V6 retained. Own jobs closed and lease released; engine acceptance incomplete.


## 2026-10-24 R56: staged renderer adapter and saturation-safe worker cleanup

[Source, real-executor reproduction, exact method extraction and rejected attempts](validation/engine-continuation/lod-renderer-adapter-2026-10-24/README.md). One new LodIndexWorker and modified actual VoxelLodRenderer are staged beside unchanged R54/R55 cursor/request. A full two-slot queue rejects naive same-worker cleanup and leaves its cursor open after work drains; the fixture subsequently closes it on its owner. New afterExecute cancellation cleanup and pre-shutdown wake avoid discarding close work. Renderer service submits one bounded metadata turn per tick, guards current epoch/camera/request on publication, retains recovery/shared-cap/edit methods and uses graceful reset. This is staging only: public prefix cutoff/source/installers unchanged.

Fresh Java17 compiler-module checks pass898real-worker cases, including100saturated shutdown runs,33turns/32interleaved geometry jobs, rejection/retry and abrupt execute failure cleanup before thread replacement. Exact serviceIndex/reset extraction passes20checks and12JVM repeats with non-game state doubles. Unchanged R55 request fixture freshly passes347checks. Git blob input and source/compiled identities retained. These are not full renderer/game/native/graphics/frame checks. An initial out-and-back camera adapter bug is corrected; earlier source and corrected failing oracle retained. A premature harness completion predicate caused rejected assertions and is corrected to require uncancelled matching-view completion; earlier partial passes superseded.

Prior R55 full checkout/tools/cache was pruned. Java17 remains; no fresh JDK25/Gradle/full renderer/module/package/Minecraft/Vulkan/GL/visual/performance or installer validation was attempted. Restore pinned tools/source/dependencies then compile and run independent source/package guards, followed by Vulkan-first actual travel/backtrack/edit/toggle/world reset/save/reopen/shutdown and visible coverage before promotion. Retiring-worker accumulation/old geometry persistence, IO fault matrix, full-backlog nearest-key admission, hierarchy/rediscovery/total frame/memory remain open. Water/pressure/budgets, native rare-host/cave and ownerGPU/FPS acceptance unfinished. Frozen V1–V6/edits/quantity retained; no new JAR. Own jobs complete, lease released; continue engine work.


## 2026-10-24 R57: staged Future cancellation/admission fix and pinned tool recovery

[Source, baseline failures, corrected candidate and JDK25 records](validation/engine-continuation/lod-future-cancellation-2026-10-24/README.md). One staged LodIndexWorker changes; renderer/cursor/request reused unchanged. Actual queued Future.cancel skips R56 Callable cleanup, neither signals request nor releases admission. Two baseline processes reproduce signal and permanent-slot failures. Candidate FutureTask cancellation signals/wakes same-worker cleanup without interrupting filesystem work; failed cancellation preserves completed results. Per-turn atomic tokens release normal/cancelled work without an old task clearing newer admission. Occupied admission uses the renderer's existing bounded rejection/retry path.

Five pinned tools restore successfully; JDK21/Gradle9.2.1 version commands pass.49immutable public core source blobs verified and complete52source candidate core compiles onJDK25.0.4.1+1, with94product core class hashes. Fresh -Xverify:all gates pass803new Future checks/100queued cancellation cases,898worker,20exact extracted renderer methods with doubles and347R55request checks. Twelve additional Future and12adapter JVM repeats pass. This is direct core compilation/JVM testing, not full Gradle/renderer/module/package/native or graphics acceptance. First candidate had a real completion/admission timing race; its full-core failure/source retained, token correction passes. Initial private barrier into a full queue also failed; corrected bounded-retry fixture and failed capture retained.

Remaining project/Themes/resource/dependency checkout, compatibility agent and native display/driver still need restoration. Next full Gradle/complete renderer/source-bytecode/package gates, then Vulkan-first actual camera/backtrack/edit/toggle/worldreset/save/reopen/shutdown/visible coverage before promotion. Retiring-worker accumulation/old geometry writes, IO failure matrix, full backlog/coarser hierarchy/rediscovery, water/pressure/budgets, terrain and ownerGPU/FPS acceptance remain open. No public source or installer change; frozen V1–V6/quantity/edits retained. Public prefix cutoff persists. Own jobs complete and lease released; engine incomplete.


## 2026-10-24 R60: staged exact density common-expression reuse

[Source, full core checks, raw matched benchmark forks and limits](validation/engine-continuation/exact-density-reuse-2026-10-24/README.md). One independently authored staged DensityGraph sampler adds bounded job-owned coordinate-aware memo slots and conservative per-node dependencies. Exact scalar method text and TerrainShapeV8 recipe remain unchanged. Keys retain relevant coordinate bits including signedzero and distinct warped positions. Graph readers stay immutable; each sampler belongs to its creation thread. Public production/worldgen and supplied installers unchanged; frozenV1–V7 retained.

57immutable core source blobs verified, complete candidate compiles withJDK25/102product core classes, and final clean own-output class hashes match timed density classes.1580860newchecks pass, including1572864exact recipe sample comparisons/four seeds/floatingoff-on, all channels, warp reuse, signedzero, invalidinput/ownership and16independent jobs. Existing183806noise/40987graph suites retain fingerprints. No failed behavioral check. Class report initially counted the old benchmark main; corrected product path count and clean compilation retained in scope audit.

Three fresh alternating-order component JVMs/fourwarmups/eightmeasured volumes per mode reproduce scalar means1016–1039ms and exact memo means53.96–55.70ms for196608samples, with1968measured allocated bytes/job. All paired checksums match. Per-fork p95 memo56.28–59.85ms; eight samples provide limited tails. Column-major Y-inner order/one recipe-seed and omitted native costs limit conclusions. This remains above5ms and is not native chunk/FPS/owner hardware or market acceptance. Raw memory bound4224sampler bytes plus object overhead at128nodes; graph dependency array<=512rawbytes. No global residency guarantee.

No fullGradle/module/codec/package/native Minecraft/Vulkan/GL/visual/installer check this run. Keep the optional sampler staged and unenabled pending those gates and broader graphs/orders/cold/tails/global job bounds. Next profile remaining exact3D/evaluator work, conservative topology/refinement, nativeV8 materialization/base-column/heightmap/save snapshots/planned hydrology and Vulkan scene acceptance. Coarse lattice906sign disagreements persist. Last complete grade unchanged50/100(5.0),Tier2/5; no new full regrade.80/100target and broader engine acceptance incomplete. Own jobs finished and lease released.


## 2026-10-24 R61: staged exact masked density evaluation

[Source, failure correction, exact checks, matched forks and diagnostic costs](validation/engine-continuation/masked-density-2026-10-24/README.md). One staged DensityGraph adds compile-time3D-expression markers and <=4non3Dzero-product factors perADD to R60's job-owned sampler. A nonzero left addend can skip an exactly-zero right product; zero addends retain original arithmetic/signedzeros. Finite validated graph nodes make this shortcut exact. Scalar evaluator text/recipe unchanged; public source/worldgen/installers and frozenV1–V7 unchanged.

57current-parent core blobs and R60staged baseline verified. Complete candidate compiles/JDK25/102product core classes;1581340exact/ownership checks,183806noise and40987graph checks pass. Added480signedzero/subnormal/mask cases. Diagnostic-onlyinstrumented copy counts196608->450563Dnoise calls with equalchecksum; counters excluded from timing/runtime. Three paired alternating-launch component JVMs/4warm8measured volumes each record R60means52.86–58.34ms/p9553.03–68.71ms versus candidate31.38–32.72ms/p9533.01–39.44ms, identical outputs and1968B/job. Small one-recipe/seed column-major component corpus, not native/frame/ownerhardware/market acceptance; still exceeds5ms.

Separate initialization probe exposes additional cost: single-JVM-per-variant50warm/200measured graph compiles record80.5->127.7microseconds and21141->41337B. No robust constructor/whole-pipeline performance acceptance. First candidate failed unreachable-node validation withNPE before proper rejection; source/results and its earlier timing forks retained/excluded. Validation now runs before optimizer metadata; final complete core gates and new timing forks pass with matching finalclass hashes.

No fullGradle/module/codec/package/nativeMinecraft/Vulkan/GL/visual/installer check. Remaining full game source/dependency/nativecheckout absent locally; directcore evidence is not shipped-runtime acceptance. Keep unenabled/staged. Next broader graph/order/cold/tail/initialization evidence and metadata overhead, fullbuild/package guards, nativeV8 materialization/base-column/heightmap/savegraph/topology/routedhydrology andVulkan coverage. Historical906lattice occupancy mismatches persist; water/LOD/otherstages/latestversion open. Last full grade unchanged50/100(5.0),Tier2/5; no regrade or80point acceptance. Own jobs finished, lease released; engine incomplete.
