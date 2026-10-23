# Active Deep Primal engine audit

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
