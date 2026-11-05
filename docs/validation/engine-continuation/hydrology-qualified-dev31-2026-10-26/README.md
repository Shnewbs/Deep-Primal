# Hydrology admission and allowance qualification — 2026-10-26

Version `0.0.1-alpha.29-dev.31` promotes the previously staged recharge admission reservation and elapsed-tick allowance overflow repair. The source parent is `164e0aa4748bcbd5433e7c19db1abd4d70e60633`; it includes the renderer's authoritative delivered-chunk revision correction from `02eb0d8f3739cf036717abf82d03dc7e2a325081`. The intervening renderer delivery commit changes documentation only. No art, geography, saved profile, or water quantity formula changes.

## Implemented repairs

- With regular hydrology columns enabled, stop admitting stream-source work 500 microseconds before the existing 2 ms deadline, leaving admission time for recharge/accounting. The staged reproduction previously observed zero regular visits versus 500 candidate visits over 100 simulated ticks. Prior strict Java scheduling evidence covers 3,360 boundary and 1,000 multi-page/headwater/unload cases per variant. This reserves admission time, not a hard latency guarantee: survey, sorting or a single callback can still overrun.
- Saturate ordered elapsed-tick subtraction overflow to a completed interval. Previously quota 1024, start MIN+1, now MAX produced -51; the corrected allowance is 1024. Rewind and sentinel behavior remain unchanged. Four packaged server sessions each passed 1,445 exact BigInteger-oracle cases against the loaded runtime class. Extreme signed-clock coverage is not a claim about ordinary world duration.
- Add a build-input preflight covering documentation consumed by resource validation. Normal, `-O` and `-OO` missing-file controls all reject. The first full forced build failed after 33 executed tasks because three required documentation inputs were omitted from restoration; diagnostics are retained. Restoring their pinned repository bytes fixed the failure, without bypassing checks.

## Fresh validation

- Java 25 full Gradle build and visual-validation compilation passed: final retry 35 actionable tasks, 5 executed and 30 up-to-date; the complete core suite reran. The earlier forced attempt passed core checks before the missing-input failure. Do not describe the successful retry as 35 forced tasks.
- Independently compiled 138 core Java sources. Package comparison matched 146 independent core class files and 592 product class files; 222 private fixture classes were excluded.
- Reconciled the concurrent renderer source, rebuilt LOD/module boundaries/root JAR, then ran every native gate against those final candidate bytes.
- Ten accepted Linux dedicated-server sessions: default fresh/reopen, opt-in fresh/reopen, aquifer fresh/reopen/verify, precipitation fresh/reopen, receiver capacity. Explicit fixture markers, save-all flush acknowledgments, reopen identities and shutdown outcomes were checked. All ten selected the intended root archive and all six nested modules (60 module selections).
- Seven recursive archives passed CRC and duplicate-entry checks. Two Windows native DLLs have AMD64 PE signatures. Themes includes 2,508 matching resources, 43 palettes, 11 approved ground textures and 148 generated soil textures; OpenGL fallback and Vulkan notices remain packaged.
- Candidate artifact: 125,296,337 bytes; SHA256 `d1823835f3c2a8c54217edfcf7349df379375df29f0714707c8d028aaaeac876`. Final exact-source producer and delivery receipts are recorded separately after source promotion.

## Coordination and audit consumption

Consumed `docs/engine-hourly-audits/2026-10-26T1627-b198804/REVIEW.md`, pinned to `b198804f19db960fba6efc52a76b18c603d50601`. Its newer successful committed qualification is distinguished from the automation record marked failed at 16:57. Chosen repair was the combined staged hydrology qualification; this report does not perform a duplicate grade. Last externally verified complete grade remains 50/100 (5.0/10), carried unchanged.

This run used its own project, cache, temporary directory, output and worlds. Root lease owner was `builder-20261026T1700`; water foreground claim was checkpointed. The renderer's scoped claim and commits were read and preserved; no exclusive timed benchmark was reserved. Personal-context and run-record checks did not establish a fresh direct ownership record from the other engine conversation; that evidence remains unavailable. Only this run's idle qualification waiter was interrupted to reconcile the renderer; its build and other workers were not stopped. A full-tree API read failed twice with transport-closed errors; the restored-input identity receipt and inspected commit deltas remain the source reconstruction evidence.

## Unpassed gates and next implementation

No Windows/AMD8060S execution, real OpenGL/Vulkan water-surface rendering, hardware frame/tick latency, multiplayer long-soak, comparative superiority or full external regrade is claimed. Continuous page-boundary/temporal water surfaces, single-owner loaded/unloaded ledger transfers, restart/player edits, bounded catch-up and queue/I/O limits still need their complete acceptance evidence. Next prioritize the world-ledger ownership/catch-up gaps and remaining callback/survey/sort bounds, with current water claims checked before editing. V8 integration and its compatibility gates remain unfinished; frozen V1–V7 and staged R57/R60/R61/R62 remain preserved.

GitHub prerelease creation/upload is unavailable through the exposed authorized capabilities. A saved testing JAR may be delivered after final producer qualification; no GitHub release URL is fabricated. Acceptance is incomplete and the builder remains enabled.
