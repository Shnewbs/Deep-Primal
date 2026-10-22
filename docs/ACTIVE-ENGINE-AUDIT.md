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
