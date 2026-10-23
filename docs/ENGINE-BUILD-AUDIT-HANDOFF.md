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
