# Deep Primal build/audit continuation

The owner requested repeated implementation, audit and rebuild work on the independent terrain, water, graphics and LOD engine, with hourly evidence-based updates. This is a long-running engineering task; an arbitrary claim of perfection is not its completion criterion.

## Work coordination

Read `ENGINE-WORK-STATE.json`, this record, `ACTIVE-ENGINE-AUDIT.md` and the current source branch before continuing. If a foreground lease is current, report progress only. Do not edit the foreground checkout, start competing Gradle/native processes, or claim an idle foreground worker is doing background development. If the source advances during a run, preserve work on a separate branch and report the conflict; never overwrite another worker's changes.

When the foreground lease has ended or the work state explicitly permits continuation, take one concrete highest-priority unfinished item through reproduction, implementation, focused checks and a reviewable checkpoint. Work in an isolated checkout/directory. Use a normal authenticated Git checkout when available; the connected GitHub API supports private source reads and guarded tree/commit/ref writes when CLI authentication is unavailable. Do not reconstruct or expose credentials. Do not spawn sub-agents.

Checkpoint meaningful source, regression fixtures and compact evidence on the working branch. Use the latest tree and expected parent/ref, preserving unrelated files and the owner's documentation/catalog work. Do not publish an unvalidated installer or merge a failing experiment into the stable branch. Report exactly what ran, what failed, what changed and what remains.

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
