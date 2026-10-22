# Active Deep Primal engine audit

Alpha.24 development installers have been built, checked and supplied. The default is 606,553 bytes; optional Windows Vulkan is 11,537,948 bytes. V3 geography is now frozen. The full engine acceptance remains open.

Read [alpha.24 validation](VALIDATION-0.0.1-alpha.24.md), [staged native evidence](validation/0.0.1-alpha.24/native-evidence.txt) and [installer audit](validation/0.0.1-alpha.24/installer-audit.json) for concrete changes, failures and checks. Fresh evidence includes the nine-seed watershed ensemble, all 33 regional estuaries, 1,000-tick loaded-basin retention, actual world creation/reopen, both graphics APIs, settings restarts, upright inventory captures and a forced Vulkan shutdown-completion failure with zero final native leaks/errors. The final grouping audit removes seven false ocean/lake classifications.

The execution workspace reset around 04:04 UTC. Source and tests were recovered and checkpointed on `audit/alpha24-recovery`; fresh results are distinguished from historical pre-reset measurements. A truncated generated archive and a genuine rejected-callback cleanup failure were both caught before delivery and corrected.

Continue from the source ref in [ENGINE-WORK-STATE.json](ENGINE-WORK-STATE.json), following [the handoff](ENGINE-BUILD-AUDIT-HANDOFF.md). The next priorities are enabled-view distant-water/LOD pass isolation, persistent bounded pressure searches, explicit regional water budgets, terrain/geology variety and LOD/transparency coverage. Catalog/species work remains separately deferred. Current prototypes, three substeps per host tick and small software-driver fixtures do not establish a finished engine, independent 60 Hz simulation, guaranteed 60 FPS or superiority over reference projects.

Hourly work uses an isolated directory after the foreground lease ends, checkpoints reviewable changes and reports actual tests/failures. It is scheduled execution, not a continuous daemon between runs.
