# Expanded replica — docs/ENGINE-BUILD-AUDIT-HANDOFF.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/ENGINE-BUILD-AUDIT-HANDOFF.md) |
| Source identity | git: docs/ENGINE-BUILD-AUDIT-HANDOFF.md |
| Preserved source SHA-256 | `19b85e2229249f26bc02818378d14a3849a31703aaefb6b4b5d754eff331c9c7` |
| Review method | structured-evidence |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal build/audit continuation”. Its original opening states: “Latest, 2026-10-26: dev.20 Earth-Scaled Rivers is released to main and the Library. Owner request: "perfect water engine part, including all water types and river sizes, including super wide rivers, and canyon rivers." New worlds save channel_revision: 2; revisions 0/1 replay unchanged (survey identical). Artifact DeepPrimal-Windows-Vulkan-alpha.29-dev.20-Earth-Rivers.jar, 125,215,496 bytes, SHA-256 d3d861949725c06f0b5ef78549a93a3747da8c26742432be11652a8364282f01. Adds hydraulic-geometry widths (great rivers to 214 blocks, estuary …”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `EARTH_RIVER_PASS`, `TERRAIN_AUDIT_NATIVE_PASS`, `DepositNativeChecks`, `DELTA_REVISION_PASS`.

## Research expansion — gaps and details to deepen

- Separate historical cycle instructions, active ownership, evidence publication, and runtime acceptance; this replica does not start or reconfigure a task.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Reference an explicit policy revision and owner, then record claim scope, expiry, expected source parent, and completed evidence without inheriting stale status text.
- Keep source-only documentation publication independent from build/release qualification and avoid treating a completed checkpoint as continuous work.
- Replace the need to infer current status from append order with a derived status view keyed by source commit, subsystem, saved revision, evidence scope, and accepted artifact. Keep the long original ledger intact.
- Link each repaired/staged/superseded item to the exact later finding or acceptance record. The final Dev77 source property, earlier Dev69 checkpoint, Dev32 overview, and Dev53 root work-state checkpoint belong to different provenance rows.
- Maintain a cross-system ownership table for finite-water transactions, world-generation revisions, GPU publication, and asynchronous persistence so a successful local fix cannot silently close a wider requirement.

## Research expansion — acceptance checks

- Simulate concurrent commits and verify a guarded publication preserves the newer work instead of overwriting it.
- Reject stale or expired ownership records and ensure a docs-only change cannot alter an engine grade, build status, or scheduler.
- Query one finding across its first failure, staged fix, scoped pass, and later native acceptance; report missing transitions explicitly.
- Run a documentation consistency check that detects mismatched version declarations without modifying historical outcomes or active ownership records.

## Research expansion — source anchors

- Deep Primal build/audit continuation
- Current acceptance and evidence
- Next substantive work

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/engine-continuation/r44-main-reconciliation-2026-10-24/integration-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/r44-main-reconciliation-2026-10-24/integration-audit.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal build/audit continuation

**Latest, 2026-10-26: dev.20 Earth-Scaled Rivers is released to main and the Library.**
Owner request: "perfect water engine part, including all water types and river
sizes, including super wide rivers, and canyon rivers." New worlds save
`channel_revision: 2`; revisions 0/1 replay unchanged (survey identical).
Artifact `DeepPrimal-Windows-Vulkan-alpha.29-dev.20-Earth-Rivers.jar`,
125,215,496 bytes, SHA-256
`d3d861949725c06f0b5ef78549a93a3747da8c26742432be11652a8364282f01`.
Adds hydraulic-geometry widths (great rivers to 214 blocks, estuary flare),
graded trunk incision with stepped canyon walls (deepest 76 blocks), meanders
and braided reaches with dry bars. Evidence: `EARTH_RIVER_PASS`, full core
checks, 13 native gates, `TERRAIN_AUDIT_NATIVE_PASS` fresh+reopen (the
`DepositNativeChecks` V6 fixture now strips V7-only revisions). Not claimed:
deltas, anastomosing rivers, oxbows, separately budgeted braid discharge,
visual acceptance. Grade unchanged at 50/100 pending a fresh full regrade.

Staged 2026-10-25: channel revision 3 (river-mouth delta fans), revision 4
(oxbow lakes on top of deltas) and revision 5 (conserved distributary discharge
budgets with sqrt(share) branch widths) are implemented behind the `riverDeltas`
creation switch and the `engine_deltas_standard` / `engine_deltas_deep` packs,
which now save `channel_revision: 5`; default engine presets and revisions 0-4
are untouched, and revisions 3-4 match frozen digests. Evidence is core-only
(`EARTH_RIVER_PASS`, `DELTA_REVISION_PASS`, `DELTA_CORE_PASS`, `OXBOW_PASS`,
`DISTRIBUTARY_PASS`, `DISTRIBUTARY_CORE_PASS`, full core suite, offline Gradle
build); no artifact, native gate, save/reopen, visual acceptance or regrade. See
`ACTIVE-ENGINE-AUDIT.md` and the revision-3/5 section of `TERRAIN-V7.md`.

Historical dev.19 note: owner stop instruction, 2026-10-25: finalize the current dev.19 task, then stop.
Dev.19 Continuous Water Edges is finalized and in the Library; hourly automation
has been removed. Do not restart development without a new owner instruction.
All user/uncommitted work remains on existing main, with no commit or push.

Artifact: 125,181,395 bytes, SHA-256
`dbbf7fa87c9d4b94d8cc12fe87f08c808c94bbdd4bc2b254aa4cccabd6480574`.
Visible resident top boundaries now use current-frame shared continuous height
samples, preserving nonheight bytes and finite physics. Hidden last-changing
pages no longer prevent visible-neighbor hold expiry. Initial live runs have
zero above-tolerance disagreements on both APIs; final GPU/recovery/expiry,
full build and package checks pass. See ACTIVE-ENGINE-AUDIT for scope/evidence,
including the incomplete final live recheck and excluded fixture failures.

Performance is not accepted: p99 component samples exceed 2 ms, and maximum
residency/driver tails remain open. Broader flicker/menu/worldgen acceptance is
incomplete; last full grade 50/100. Historical next-item instructions below
do not override the owner's stop.

Latest local checkpoint (2026-10-25): **dev.18 Exact Stopped Water is in the Library**.
125,179,617 bytes; SHA-256
`3bf64519fa70de18bcf3cd7344e0a3db36f9b9c8ca2a733d6606a69e47565ddf`.

The dev.15 midpoint filter could remain stuck after oscillation stopped:
duplicate raw heights returned before any exact-height recovery. Reproduced
before the fix in `build\dev17-settle-repro.log`. Holds now expire 400 ms after
the last real reversal, followed by a continuous 100 ms return to the actual
height. `remaining` keeps renderer cleanup alive while a midpoint is still;
duplicate/stale publications do not extend it. Opacity remains unfiltered.
No physical water, saved geography or owner-art change.

Full build and 150 release cases pass. Both packaged APIs pass actual GPU
upload lifetime/exact-height checks, resource reload and waterfall readbacks;
environment/reopen, finite source/reserve, three reaches and six soil gates
pass. Package checks retain seven archives/545 classes/333 resources and four
identical captures. Initial client runs failed only on the new fixture's absent
output directory after the midpoint assertions; fixed directory creation and
both complete retries pass. See ACTIVE-ENGINE-AUDIT for evidence paths.
The follow-up `*-readback.log` clients additionally fence/copy/map three actual
GPU buffers per API at hold/release/idle: all bytes match, twelve interior corner
witnesses per API have exact target/displacement, and both complete clients
shut down cleanly. The preview waits on both water readback futures before
reload/exit. This is private validation only; the Library JAR is unchanged.

The next gate now has a running-world reproduction: private
`water-live-continuity-check` samples 600 client ticks of visible resident
top-boundary mirrors. OpenGL 25,256/122,425 unique page-corner comparisons
disagree, maximum 0.203125 blocks; Vulkan repeat 27,513/124,581 disagree,
maximum 0.33333302. Same-page duplicates agree exactly in both; neither run
paused. Both save/shutdown cleanly. See `build\water-live-dev18-{opengl,vulkan-repeat}.log`
and their `*-captures\water-live-continuity.json`.

Prioritize resident shared-height endpoint/clock publication: the current
neighbor synchronization only updates direction packets. Shared history is
not sufficient to keep already uploaded geometry coherent. Do not "fix" this
by disabling interpolation, freezing the solver or snapping boundaries.
Preserve stale/edit guards and failure recovery, and measure any whole-page
rebase/cohort approach against runtime budgets before promotion. This remains
unfixed; probe observations are CPU upload mirrors using shader arithmetic,
not GPU pixel acceptance. The first Vulkan probe counted duplicate vertices;
use the later deduplicated results above for comparison counts.

Next: live-stream displayed-height/shared-edge and wet/dry face continuity,
full-tick many-region storm latency, world ledger, physical source unload/reopen/
edit and menu-close reproduction. The isolated GPU fixture is not proof that
the owner's whole flicker report is solved. Preserve dev.12-17 semantics,
uncommitted main and owner edits; bump beyond dev.18 before production edits.
Last full grade remains 50/100; engine acceptance incomplete.

Previous local checkpoint (2026-10-25): **dev.17 Leaner Weather Sampling is in the Library**.
125,179,328 bytes; SHA-256
`f78c983ab7fe5b513fb79aa31e383c3417a85d162e6c41b358e7a6330d97b811`.

Precipitation previously built terrain/full geology for soil, glass and ice;
it now reads fixed material permeability or the exact two-field geological
subset. Ordinary hydrology visits reuse that subset. No saved terrain or water
accounting policy change. 40,000 core oracle cases are bit exact. Four-region,
64-center native component benchmarks retain exact recharge/chemistry; repeated
p50 soil/rock-ice/glass costs are 0.1347/0.0746/0.0287 ms versus dev.16
0.3752/0.3655/0.2850 ms. These exclude fixture setup and do not certify real tick
latency. See LOD-AND-DRAINAGE-ARCHITECTURE for method, hardware and p95.

Full build/environment/reopen/source/reserve/reaches/dual APIs/package identities
pass. Update `GroundMaterialChecks`' reflection signature with the helper:
its first six soil runs failed before testing output, then all six retry gates
passed with a null-noise provider proving soil permeability is independent.
The new `StormSamplingChecks` is private validation code, excluded from the JAR;
enable with DEEPPRIMAL_STORM_PROFILE plus DEEPPRIMAL_ENVIRONMENT_RECORD in a
disposable server fixture. Its controlled chunk skylight must be enabled and
completed before measuring, without blocking the server task executor.

Next: full-tick multi-region storm latency or live-stream displayed-height/edge
continuity; retain complete ledger, physical unload/reopen/edit and menu-close
reproduction gaps. Preserve dev.12-16 behavior and all owner edits. Bump beyond
dev.17 before production edits. Grade remains 50/100; engine acceptance incomplete.
Earlier version/candidate and upload-availability statements below are historical.

Latest local checkpoint (2026-10-25): **dev.16 Fair Rain/Snow is in the Library**.
125,179,290 bytes, SHA-256
`306599c5004cd0790a9ba562c57a5225d48684ec332140872483ac6f1fc4927c`.
The original dev.11 artifact was verified against its recorded hash and added
without rebuilding. Dev.12-15 remain delivered historical artifacts.

Reproduced mixed-biome rain-first starvation against the packaged dev.15 runtime.
Dev.16 proportionally splits finite atmosphere by capacity-limited rain/snow
demand, conserving exact water/salt/sediment with atomic constituent-capacity
preflight. Native 24-case warm/cold/capacity matrix, 10,000 core oracle cases,
full build, environment save/reopen, source/reserve, three reaches, six soil
revision/save checks, both rendering APIs and package identities all pass.
See ACTIVE-ENGINE-AUDIT for scope and raw evidence paths.

Preserve dev.12 deep/legacy aquifer depths and channel revisions, dev.13 material
revision 2 with 0/1 replay, dev.14 per-tick cumulative source allowance and
dev.15 presentation-only surface settling. No saved format/geography change.
Next finite run: many-region storm latency or live-stream displayed-height/edge
continuity; also retain world-ledger, physical unload/reopen/edit and menu-close
reproduction gaps. Do not treat the earlier inferred parcel oscillation as
proof that the owner's complete flicker report is fixed. Grade remains 50/100;
no engine-complete claim. Bump the production version before further code edits.

Latest local checkpoint (2026-10-25): **dev.11 Spatial Recharge JAR verified and staged**.
125,177,114 bytes, SHA-256
`10b1839c697861de3051cb55c953835d60fd1bf6e5799c2a30afb5b618a375e5`.
Artifact is in this session's files directory. Library-upload tool unavailable;
dev.10 remains the latest Library item. Add the existing verified dev.11 artifact
when that contribution is available; do not rebuild or overwrite it.

Reproduced first-visited soil bias: grass-first49,152 versus sand-first88,473
quanta for the same region. Replaced that extrapolation with at most16 fixed
loaded-chunk center samples, each weighted256 and using local bed/biome. Native
post-fix orders both yield22,937, with finite atmosphere and no forced chunks.
All server/soil/dual-API/package gates pass. See ACTIVE-ENGINE-AUDIT.

Regional rain is a once-per-interval spatial snapshot: edits after sampling apply
next step, not a second scan during the same step. Preserve clock/reserve/quota/
cadence guards. Next: mixed-biome/capacity weather and storm performance evidence,
physical source unload/reopen, complete world ledger and temporal flicker.
Dev.12 is next production version. Grade remains50/100.

Previous local checkpoint (2026-10-25): **dev.10 Regional Weather Clock JAR is in the Library**.
125,176,661 bytes, SHA-256
`2a6b0872e19c3d67a070ded6298c205753143b9ad077236a7cb46b89adbff64d`.
Native fixture reproduced a frozen drought clock beneath bedrock. Existing
regional aquifers now advance even on impermeable visits, without seeding new
groundwater there. Per-step rain accounting allows a later permeable visit to
recharge exactly once; repeated visits/cache reconstruction cannot duplicate it.
Dev.7 scheduling, dev.8 allowances and dev.9 drought reserve protection retained.

Environmental fresh/reopen, native clock/source/reserve, full build/core,
soil 1/0 fresh/reopen, both APIs and package gates pass. Three 1,200-tick reaches
keep 67/67, 115/115, 655/655 wet; final falls 0.125/0/0.0625; no sampled overtopping.
See ACTIVE-ENGINE-AUDIT for before/after evidence and scope.

Next: precipitation still extrapolates one eligible column's permeability/biome
over the loaded region; reproduce spatial sampling bias before replacing it
with bounded representative accounting. Native catchment weather/full ledger,
source unload/reopen and temporal flicker remain open. Dev.11 is the next
production version. Last full grade **50/100**.

Previous local checkpoint (2026-10-25): **dev.9 Drought Reserve JAR is in the Library**.
125,176,563 bytes, SHA-256
`281c0122763b54e75f839f2a2f4a2ec78fdeaeab8d503c90e88f0159ff18dcf2`.
Reproduced reserve overspend (`remaining=0 units=5`) at a depressed outlet before
the next aquifer step. `Aquifer` now checks current STOP on supply and caps each
delivery to whole units above STOP. Evaporation can still lower inactive reserves.
Preserve dev.7 scheduling and dev.8 headwater allocation/quotas.

Core weather-cycle checks run 180,000 intervals across six scenarios with exact
water/salt/sediment totals, six drought stops, four storm restarts and 186 state
reconstructions. Native reserve and source checks, environmental fresh/reopen,
soil 1/0 fresh/reopen, dual-API and package checks all pass.
High creek extended to 5,000 ticks keeps 67/67 wet with no sampled overtopping;
final fall is 0.125. This does not resolve the startup dip or visible flicker.
See ACTIVE-ENGINE-AUDIT for evidence and scope.

Next: native full-catchment weather and ledger checks, additional seeds, physical
source unload/reopen and connected waterfall/flicker scenes. Core weather-cycle
coverage is not a native storm acceptance claim. Dev.10 is the next production
version. Last full grade remains **50/100**.

Previous local checkpoint (2026-10-25): **dev.8 Headwater Baseflow JAR is in the Library**.
It is 125,176,558 bytes, SHA-256
`eaf29368806cf81ee0c888d108741d733213fa1c180df804510365dc86708afb`.

What changed:
- Planned waterways use a separate finite regional natural quota,
  `Aquifer.baseflowUnits`: mean regional rain recharge, 80 units/interval by default, floor 32.
- Dry springs keep the 32-unit quota and `sourceRate`.
- Planned columns are served headwater-first.

Results:
- High creek peak dip is 0.625 (was 0.94); final fall is 0.1875; quota is no longer saturated.
- High, low and wide reaches keep all columns wet with no flooding.
- Native, environmental, soil, dual-API and package gates all pass.

Next: long drought/storm aquifer trajectory, seeds/catchments, ledger, flicker.
Bump to dev.9 before production edits. Last full grade remains **50/100**.

Previous local checkpoint (2026-10-25): **dev.7 Steady Stream Sources JAR is in the Library**,
125,175,702 bytes, SHA-256
`8a85abdc2c6a374b170f2a2066cdf3dcc7462d6331a3f97f9ab50f6c0f1bfbf2`.
The remaining high-creek loss came from scan cadence, not the budget. A source
column was refilled about every 196 ticks because the scan covers 49 chunks.
Planned waterway columns are now surveyed after load and listed per chunk.
They receive one finite top-up per 20-tick interval into the actual deficit cell.
Delivery is head-, stage- and 32-unit-quota-limited.
High/low/wide 1,200-tick REQUIRE runs keep 67/67, 115/115 and 655/655 wet columns with zero
dry ticks and no flooding. Final high fall is 0.3125 (was 1.0625), but a
transient peak of about 0.94 blocks remains around ticks 300–500. All native,
environmental, soil, dual-API and package gates pass; see ACTIVE-ENGINE-AUDIT. Next: startup
transient, discharge-scaled quota, seeds/catchments, ledger, weather, flicker.
Bump to dev.8 before production edits. Last full grade remains **50/100**.

Previous local checkpoint (2026-10-25): **dev.6 Natural Stream Head JAR is in the Library**,
125,173,983 bytes, SHA-256
`443acc2a1c8b43679d9a7c4e39f7c5a6b39413bd5f9c5e68f859c8cb8d61f555`.
The sealed dev.5 high creek reproduced 67 to 40 wet columns over 1,200 ticks:
an active aquifer referenced a regional terrain minimum below its elevated
outlets. Natural infiltration and replenishment now share a local planned head
at the finite drought-stop reserve. Unplanned springs, finite chemistry/accounts,
32-unit regional quota, fractional cap, saved terrain and player edits are preserved.
This simplified perched-head correction is not infinite refill or a completed
regional water cycle.

Fresh selected-JAR high/low/wide reaches retain 67/67, 115/115 and 655/655 wet
columns on every one of 1,200 ticks, with zero dry-column ticks. All 100-tick
bank samples show no newly wet monitored dry columns/overtopping. High-creek
stage still falls up to 1.0625 blocks; the fresh low creek falls 0.3125.
Full 35-task Windows build, native source debit/quota/dam/drought/recharge,
environmental fresh/reopen, material1/legacy0 fresh/reopen, both API inventory/
reload and controlled waterfall upload/readback gates pass. Seven recursive
archives, 545 production classes, 333 resources and four pixel-identical
1200x800 previews pass final packaging checks. Dev.5/dev.4 remain separate.
See the newest ACTIVE-ENGINE-AUDIT entry for exact logs.

Continue real stage/flow continuity, complete water/salt/sediment accounting,
other seeds/larger catchments, sustained drought/storm/recharge, unload/reopen/
player edits and actual connected waterfall/temporal scenes. The owner screenshot's
exact cause and visible flicker are still open. Continue soil profile mechanics
and saved V8/topology/performance gates without claiming complete worldgen.
Last full grade remains **50/100**. Work remains on local main; no branch,
commit, push or owner PNG edit was made.

Previous local checkpoint (2026-10-25): **dev.5 Soil Families JAR is in the Library**,
125,173,842 bytes, SHA-256
`c9675e958e7fa66cd6ca28c2af14d006303f912d9384c491615e073bf45acc55`.
Twelve documented texture classes now have 36 registered bare/grass/podzol forms,
generated content, provisional mineral tints and infiltration values. New
Standard/Deep worlds save material revision 1; actual legacy worlds remain revision
0. Full 35-task build, selected-JAR native new/legacy fresh/reopen, all family
permeabilities, both API inventory/reload, archive/class/resource and exact-pixel
gates pass. A three-seed, 12,675-column terrain distribution reaches all twelve;
the initial additive selector failed and was replaced before delivery. Details
and unfinished profile/gameplay mechanics are in ACTIVE-ENGINE-AUDIT.md and the
soil catalog implementation checklist. No branch, commit, push or owner PNG edit.

The owner's latest water screenshot is an open bug report: repeated diagonal/
stepped bands. Controlled native Vulkan basin captures show the bed steps through
a mathematically flat surface in both animated/fallback paths; no owner-world
root cause, stream conservation/flooding or flicker fix is established.
The new stepped-bed core check excludes internal faces and preserves the flat
surface across signed sections. The in-game fixture was initially blocked by
first-run accessibility onboarding, now explicitly accepted. Six selected-JAR
water-shape captures and normal save/stop/GPU release pass. Preserve that evidence;
next isolate the actual owner-world geometry/transport/appearance rather than
masking bands with opaque water or changing frozen terrain. Last full grade is
still **50/100**, not a claim that the 8.5 target is reached.

Current local continuation (2026-10-25): **dev.4 development JAR delivered from main.** The owner's v3 side artwork and final sand from `670ddd9` are preserved. Single-pass dual-tint side materials now cover terrain, moving blocks, entities, inventory and NeoForge's unlit inventory shader pair. Actual OpenGL/Vulkan inventory/reload captures include opaque-face pixel assertions; earlier model-only checks missed absent sides. The Vulkan startup failure was traced to unsupported `~` syntax in the converter, then a missing paired unlit vertex interface; the apparent missing GUI shader was a secondary reload failure. Stacked waterfall side walls are clipped independently of shared free surfaces, with separate animation histories. The full 33-task Windows build and selected-JAR native fresh/reopen/client checks pass. Seven archive CRCs, 141 root classes, 399 nested classes and 186 resources match; OpenGL/Vulkan captures match exactly before/after reload. Each API passes 60 controlled production waterfall upload/readback cases, not complete stream simulation or scene acceptance. The unique dev.4 JAR is in the Library (125,120,400 bytes; SHA-256 `60eb6dae7dbaa2f19e98befc4cb256cec58045f26417cd573b023a95f4f98691`). This is a development checkpoint, not complete worldgen.

The owner authorizes the soil catalog work and requests worldgen/terrain priority, continued checklist implementation, no new branches and working JARs approximately hourly when practical. Historical "catalog deferred" and separate-branch instructions below do not override that direction. Implement the twelve texture classes with cover/tint/placement and a saved material revision; preserve existing worlds and vanilla registry identities. Native world lighting/mips, waterfall scenes, drainage/flooding and V8 gameplay remain open. Last full engine grade stays **50/100, Tier 2**, with target average **at least 8.5**, ideally 9.5. The supplied [Planet Smith rendering reference](LOD-AND-DRAINAGE-ARCHITECTURE.md#planet-smith-rendering-reference) adds measured renderer work, not a reason to defer hydrology or silently rewrite the host renderer.

Current continuation (2026-10-24): **owner target average ≥8/10; alpha.29-dev.2 partial stage 2**. The existing hourly build/audit task is enabled with this target and the same strict ten-category rubric. Last verified full grade remains50/100. Read [stage-two scope](engine-stage2-2026-10-24/README.md), [executed results](engine-stage2-2026-10-24/RESULTS.md) and ENGINE-WORK-STATE.json. Bounded density programs, strict JSON profiles and an experimental V8 shape recipe pass full33-task build,40987 graph checks,34 codec cases, independent/package identities and native V7 replay/save/stop. All509 prior production class files are unchanged. No V8 registration or owner installer. Exact component evaluation1.06–1.14s/full volume, lattice17.67–18.88ms and906 occupancy disagreements block promotion. Next optimize repeated expressions and prove conservative topology refinement, then native V8 block/base-column/heightmap/saved-profile/hydrology/visual gates. Stages3–6 and latest-version support stay open. Freeze V1–V7, preserve unpromoted R57 and all historical requirements. A checkpoint does not finish the goal; report only actual progress and do not imply continuous activity between finite scheduled runs.


Current Builder/Grader checkpoint (2026-10-24): **alpha.29-dev stage 1; alpha.28 remains the delivered preview.** Read [the strict review](engine-review-2026-10-24/REVIEW.md), [architecture](engine-review-2026-10-24/ARCHITECTURE.md), [stage results](engine-review-2026-10-24/STAGE-1.md) and ENGINE-WORK-STATE.json. Current engine grade is **50/100, Tier 2/5** under the owner's explicit rubric. The new immutable 3D noise/grid library and strict codec pass the full 33-task build, independent scalar/seam/parallel tests, exact salt round-trips, same-host JVM fingerprints and packaged-native save/stop checks. All 504 existing production classes remain byte-identical to alpha.28; no V8 registration or new owner installer. Released warm native fill means are 14.45–14.99 ms excluding lighting/IO/biome/status costs, so the <5 ms goal is unmet. JFR points to block writes, heightmaps and ore evaluation as well as field work. Stages 2–6, current-version migration, cross-machine/long-duration/performance and visual acceptance remain open. Next implement the bounded V8 density stage under the recorded gates. Preserve all frozen geography and unpromoted R57 LOD work. Current pressure code retains 128-node turns up to 8,192 nodes/job and 16 jobs; older descriptions of a permanently restarted 128-cell pressure search below are historical.


Current foreground release (2026-10-24): **alpha.28 / V7 terrain**. Read [the V7 scope](TERRAIN-V7.md), [release validation](VALIDATION-0.0.1-alpha.28.md) and ENGINE-WORK-STATE.json. Reproduced thin-water bed clipping and seed-origin aliasing are fixed; new V7 worlds add broader channels, regional landforms and biome-based surfaces/canopy. Freeze delivered V1–V7 geography. Standard/Deep/native legacy save-reload, actual UI seed binding, matched Vulkan beds and OpenGL smooth fallback pass. Full engine, broad live water/LOD/transparency, recovery-marker root cause and Windows/owner-GPU acceptance remain open. R57 LOD staging is preserved and unpromoted. The packaged-validation classpath filter and loaded-class identity guard are mandatory for future selected-JAR comparisons.

Previous foreground release (2026-10-24): **alpha.27 / V6 terrain is delivered on main**. Read [the V6 scope](TERRAIN-V6.md), [accepted evidence](VALIDATION-0.0.1-alpha.27.md) and ENGINE-WORK-STATE.json before continuing. Freeze delivered V1–V6 geography. The older R51 staged snapshot below is historical and must not overwrite V6 production. Standard/Deep creation and reopen, environmental accounting, authored ores, supported river tunnels, finite melt, local cave-ins and software Vulkan captures pass; broader engine/owner-GPU and deferred mechanics remain open.


Current continuation (2026-10-24): use **main**. R51's lazy V5 deposit metadata remains staged. Final build, independently compiled core bytes and guarded all-ore/native/save checks pass; allocation is lower but latency is mixed. Read ENGINE-WORK-STATE.json and the R51 entry below. Missing host pairs, Vulkan client visuals and broader balance/performance gate promotion. Default/saved V1–V4, public production and installers remain unchanged; full engine acceptance is incomplete.

The owner requested repeated implementation, audit and rebuild work on the independent terrain, water, graphics and LOD engine, with hourly evidence-based updates. This is a long-running engineering task; an arbitrary claim of perfection is not its completion criterion.

## Work coordination

Read `ENGINE-WORK-STATE.json`, this record, `ACTIVE-ENGINE-AUDIT.md` and the current source branch before continuing. If a foreground lease is current, report progress only. Do not edit the foreground checkout, start competing Gradle/native processes, or claim an idle foreground worker is doing background development. If the source advances during a run, preserve work on a separate branch and report the conflict; never overwrite another worker's changes.

When the foreground lease has ended or the work state explicitly permits continuation, take one concrete highest-priority unfinished item through reproduction, implementation, focused checks and a reviewable checkpoint. The owner's current direction is to use this existing local main checkout, preserving all uncommitted/user changes. Do not create branches, commit, push, stash, reset, rebase or pull without explicit owner authorization. Older isolated-branch instructions below are historical. Do not reconstruct or expose credentials. Do not spawn sub-agents.

Record meaningful source, regression fixtures and compact evidence in the current checkout and existing audit/checklist files; this does not authorize a commit. Preserve unrelated files and the owner's documentation/catalog work. Do not publish an unvalidated installer or overwrite a previously supplied artifact. Report exactly what ran, what failed, what changed and what remains.

## Current acceptance and evidence

Alpha.24 development installers have now been supplied. V3 geography is frozen; subsequent geographic changes need a new saved revision. Continue from the working ref in ENGINE-WORK-STATE.json. See `validation/0.0.1-alpha.24/native-evidence.txt` for the staged evidence; later stages supersede earlier binaries. The workspace reset around 04:04 UTC destroyed local pre-reset captures and installers. Recovered code is checkpointed on `audit/alpha24-recovery`; fresh core/native evidence is explicitly distinguished from historical measurements.

The latest source corrects river/lake/sea continuity, banks, final-slope materials, cave-noise reuse and water corner geometry across vertical cell/section boundaries. Final native GPU/lake checks of that corner fix now pass, including Vulkan destruction-time validation and a deliberately rejected shutdown completion. The default, no-LOD and Windows delivery profiles now pass nested archive, class/module, native-platform and common-payload audits. Repetitive terrain contours, distant water artifacts and broader worldgen quality are not accepted as complete.

Required checks include the full core/module build; preserved V1/V2 geography and native V2 block digest; actual Create World/join/save/reopen; native channel/inlet/estuary and bank checks; a fully loaded generated basin running the real solver; conservative filling/pressure; matched OpenGL/Vulkan animated/fallback captures; visual inspection and resource lifetime; and profile/installer auditing. Minecraft can crash after an assertion while Gradle exits zero: require explicit acceptance records and inspect failures, not just the process exit code.

The standard build uses JDK 25, Gradle 9.2.1, Minecraft 26.1.2 and NeoForge 26.1.2.122. The private current test toolchain is beside the checkout in `../toolchain`; it is not part of the mod. Its launcher uses an isolated-process compatibility shim only for NeoForm build tools, private software GPU/display dependencies and captured logs. Use the normal wrapper in an ordinary environment. If native execution is unavailable, report that limit and preserve reproducible work; never relabel core math checks as native or visual acceptance.

## Next substantive work

1. Finish the candidate's current native/visual gates. Keep the default installer lean and graphics/LOD/Vulkan independently modular. Native tests on Mesa software drivers do not certify Windows/owner GPU performance.
2. Reproduce enabled-view distant water/page coverage and mixed transparency. Capture separate near terrain, animated water, fallback water and LOD A/B cases, including teleport, edits, unload/reload and resource reload. Disabling LOD in a diagnostic screenshot is not acceptance of an enabled-view defect. Preserve coverage under memory pressure.
3. Reproduce a sealed communicating U-bend with a water path longer than 128 nodes. Current pressure/drain searches have a bounded window without general persistent continuation. Implement fair bounded continuation and safe edit/unload handling; raising a constant is not a complete global hydraulic engine. Preserve volume and never force-load missing chunks. Keep settled worlds idle.
4. Implement native finite/renewable watershed, glacier/snowmelt, aquifer and ocean/atmosphere budgets. Inputs and removals need explicit accounting and world persistence. Do not freeze natural water as an apparent fix or add unlimited source refill. Respect the owner's balanced/default and optional strict/source-safety directions in the requirement records.
5. Continue coherent terrain/geology/material and LOD architecture against `TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md`, `LOD-AND-DRAINAGE-ARCHITECTURE.md` and the configuration/deposit contract. Catalog/species naming and environmental-content work remains separately deferred; do not invent a finished roster. Prioritize connected terrain and real editable-world behavior over superficial noise or asset scatter.

## Constraints that survive updates

- Keep the Deep Primal product identity. It owns its Overworld generator and native liquid; inspiration from TFC/Terralith/Tectonic, PhysEx, finitewater, BetterWaterMesh, Water Erosion, Distant Horizons and terrain/rendering tools does not mean bundling or wrapping their engines.
- Retain graphics settings ownership and complete native Video Settings takeover, with optional LOD and Vulkan implementations behind module boundaries. Preserve settings persistence and saved-world compatibility.
- Published V1/V2 generation stays reproducible. Once a V3 installer is delivered, changing its geography requires a new saved generation revision.
- Do not equate three substeps in a 20 Hz host tick with an independent 60 Hz simulator or a guaranteed 60 FPS minimum. No unbounded flood-to-completion loop or whole-world ocean scan.
- Use primary technical sources and verify licenses/provenance before incorporating code. Proprietary terrain/renderer code is not available to copy merely because it is a reference.
- Measure matched workloads, output equivalence, memory and latency. Keep individual software-test timings distinct from broad performance or reference-mod superiority claims.
- Preserve user edits and water quantity across chunk boundaries, save/reload, multiplayer, teleports and resource changes. Mark absent evidence as open.
- Do not pause the hourly task until the owner asks or the agreed acceptance work is demonstrably complete. Each update must report actual changes and tests; say plainly when there is no new verified progress.

## Archive and shutdown gates

A truncated large workspace JAR was caught during this audit. Build and stage final archives after producer completion, validate every outer/nested ZIP CRC, then check staged bytes/hashes again before delivery. A compiler success or native development launch does not validate the exported installer. Avoid concurrent reads/edits of live build outputs.

A rejected screenshot callback also exposed a real shutdown cleanup bug. The native VulkanShutdownChecks fixture injects a failed completion and requires the following completion to run. Require zero destruction-time Vulkan VUID messages, zero tracked resources and final counters after cleanup. UI capture fixtures must await the image write. Preserve this regression when changing the graphics backend.

## Historical alpha.24 scene context

The historical alpha.24 native coast used about 1.76 MB of animated water residency at this camera. That pre-reset observation is not fresh candidate validation. Repetitive contours and suspicious distant-water patterns remain visible quality concerns; their cause is unproven. Isolate the waves overlay as well as animated/fallback water, near terrain and LOD before diagnosing missing pages. A useful lake-outlet camera for seed 20261020 is x=107.68827612381756, y=176.5875, z=-226.797007486035, yaw=180, pitch=33.02386755579665. Keep geometry/volumes and the camera fixed across comparisons.

The final ocean-grouping fix removes seven false lake classifications from the nine-seed ensemble: 650 lakes, 964 headwaters, 4,292 connected reaches. The latest 1,000-tick basin is retained; pending work remains nonzero. Do not turn these into claims that regional hydrology or the full terrain design is finished. New installer releases need unique versioned names; do not replace the supplied alpha.24 bytes.

## Packaged class freshness

The pressure-head continuation caught a valid-CRC JAR containing an older WaterRuntime after copying incremental build output. Compare packaged main-class bytes with current compiled outputs, not just CRCs, and run a focused packaged-native regression. If stale, reject that archive, rebuild with `--no-build-cache --rerun-tasks jar`, and repeat identity/CRC/native checks after producer completion. See `validation/engine-continuation/2026-10-22-pressure-head-retention-build.json`. The exact incremental cause remains unproven; do not reuse or deliver the rejected archive.

## Current retained-drain candidate and acceptance gates

Read [the basin/pressure audit](validation/engine-continuation/basin-pressure-2026-10-22/README.md) and work-state staged source paths.
The optional checksum-pinned tool restoration helper can recreate private tools
after a reset; it is never mod payload. The accepted production solver was 2c431bb at that checkpoint; the later
pressure-admission cleanup below supersedes it on the working branch. The disposable project and staged runtime/core are an **unaccepted
experiment** and must be reviewed/copied together. Main and supplied installers
are unchanged.

The candidate now uses the existing four-unit pressure quantum through validated
drain witnesses, retaining one-unit film progress. It shares a capped core pressure
overload rather than duplicating caps. Two current-source closed 8,192-unit basin
runs settle at tick 2,369 with identical quantity/FLOW hashes; the stricter repeat
then checks zero solver work on every following host tick. All 24 strict native
safety/idle/basin/package cases, uncached default non-Vulkan core/module build,
five archive CRCs and 226 class comparisons pass. The initial validation compile
failure and incomplete reopen log capture are preserved, followed by corrected
passing gates. A startup autosave or record file alone is not complete acceptance.

**Keep runtime/core out of production until performance promotion is justified.**
Current-source basin p95 is 5.26–5.65 ms versus 4.59 ms on the failing FIFO baseline.
The candidate settles and reduces total search visits, but that does not establish
lower tick latency or general output/memory acceptance. Original unpinned filling
still retains 1,024 waiters at 120 ticks. Next profile the increased pressure work
and tail latency without weakening settling, film, FIFO bounds, physical unload,
player edits or zero-idle-work checks. Preserve the 16-job/8,192-node residency
limits and current per-host bounds; larger-region continuation remains open.

Historical failures stay in ACTIVE-ENGINE-AUDIT and the linked reports. Regional
renewable/finite reservoirs, enabled-view water/LOD coverage, transparency and
geology remain unfinished; catalog/species stays deferred. No fresh visual/Vulkan
or owner GPU/FPS acceptance is established here. Substeps are not independent 60 Hz.

## Current accepted cleanup and Vulkan priority

Read [pressure admission evidence](validation/engine-continuation/pressure-admission-2026-10-22/README.md).
Production now contains the independently authored, validated two-method cleanup
on the accepted solver: centralize new-admission validation (amount reads 3 to 1)
and preserve resident job ownership defensively. Injected overlap reachability
in ordinary gameplay and a latency causal link are unproven. All 13 final native
cases, uncached non-Vulkan core/module build, five CRCs and 225 class matches pass.
Main/installers are unchanged. The newly staged retained-drain cleanup has only
source-extracted controls; it must repeat full native/basin gates before promotion.

Owner direction: prioritize Vulkan as the primary sample/reference graphics API
and test target, preserving OpenGL fallback and independent module boundaries.
Continue actual implementation after checkpoints; keep owner updates to concise
verified milestones or blockers. Restore Vulkan/display/driver before enabled-view
coverage/transparency and shutdown injection checks; do not claim GPU/FPS guarantees
from software-driver runs. Regional water budgets, pressure residency/performance,
geology and LOD acceptance remain open; catalog/species remains deferred.

## Current Vulkan-first native checkpoint

The [Vulkan primary report](validation/engine-continuation/vulkan-primary-2026-10-22/README.md)
now supersedes the missing-environment blocker. All 124 optional module inputs
were restored exactly. The uncached Vulkan Linux build/core/module checks, six
CRCs and 406 compiled class comparisons pass. Four strict software-Vulkan sessions
cover fresh V3 creation, saved-world reopen, GPU transfer/shader/depth/stencil,
enabled animated/fallback water and actual active LOD, resource reload, memory
guard and rejected-completion shutdown with zero final buffers/images and VUIDs.
The final capture starts/ends without a recovery marker and uses no forced retry.

Preserve the failed OpenGL fallback assertion and incomplete scene capture as
failures. The early reopened screenshot shows partial water/exposed terrain while
delivery warms; the later scene recovers. This is not full coverage acceptance.
The final camera-matched images continued simulation/LOD warming, so they do not
prove equal-state output or performance. Next establish a bounded Vulkan coverage
recovery regression under joins, teleports, constrained budgets and edits, then
address demonstrated missing coverage/transparency without changing geography.
No production renderer fix or owner GPU/FPS acceptance is claimed. OpenGL remains
a modular fallback; Vulkan is the primary sample/reference target. Keep updates
brief and keep working through the open acceptance items.


## Current Vulkan frame/LOD recovery checkpoint

Read [fresh source, before failures and scoped acceptance](validation/engine-continuation/vulkan-lod-recovery-2026-10-22/README.md).
Two production fixes now delay public Vulkan pass ownership until fenced frame-start callbacks finish and retain out-of-range indexed LOD disk keys within the existing 64-key/call bound (up to two calls per scheduled tick). Exact-source controls, uncached Linux Vulkan build/core/geography checks, six CRCs/406 class comparisons and two strict combined native sessions pass, including packaged module origins, real store/worker/GPU upload, eight frame completions/eight in-pass rejections per session, reload, normal save/disconnect and zero-resource/zero-error Vulkan shutdown.

Self-audit rejected direct-stop sessions that logged chunk-save errors despite Gradle zero. Validation now uses normal save/disconnect and rejects save/server-stop/scheduler failures. Direct-stop save safety remains open. Malformed standalone module bytes and an actual OpenGL recovery fallback are excluded. Both final combined sessions use explicit Vulkan retry: the recovery marker remains after FIRST_FRAME/clean shutdown, with cause unproven. Do not claim clean unforced reopen. Next instrument that lifecycle in-process, then test real physical teleport/memory-pressure/edit coverage. Isolated client-position selection is not physical teleport acceptance. The viewed scene has LOD active but zero visible LOD triangles; the isolated synthetic page proves real GPU upload only. Exposed right-hand lake regions and mixed transparency remain unaccepted.

Water runtime/core/frozen geography are unchanged; retained-drain work stays separate/unaccepted. No installer or main merge. Prior native counts/measurements above are historical to this checkpoint. Software Vulkan is not owner GPU/FPS acceptance. Preserve all recorded hydrology, pressure residency/performance, geology and LOD requirements; catalog/species remains deferred.


## Vulkan startup re-arm lifecycle checkpoint

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


<!-- END PRESERVED SOURCE -->
