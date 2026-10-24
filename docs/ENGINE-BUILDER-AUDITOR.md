# Builder and hourly auditor coordination

Owner directions, 2026-10-24: "you dont stop working, the auditor just comes in every hour to give a graded update so far" and "auditor also builds a current build of the jar during their audit".

## Builder

Task `6ad974bdc204819195e5465aeea9dca2` (Build Deep Primal engine) remains enabled. Continue implementation, focused validation and reviewable source checkpoints toward an evidence-supported average of at least 8/10 across the ten existing categories. An audit/report/checkpoint is not a reason to stop useful builder work or wait for user confirmation. Continue to subsequent authorized gated items during an active run while time/resources permit. Preserve resumable state at actual execution/context/resource boundaries. The scheduling platform executes finite runs; neither an enabled task nor a checkpoint proves an always-running process between runs.

Retain the guarded main writer lease and isolated checkout/output rules. Do not overwrite unrelated work, frozen V1–V7 geography or the staged/unaccepted R57/R60/R61 variants. Record active performance measurement reservations so competing heavy work can defer. Keep implementation/evidence facts available to the auditor without requiring the builder to pause.

## Auditor and current JAR

Task `6adcfe07f3888191aa478160f40027da` (Audit Deep Primal builds) is enabled on an hourly schedule in America/Los_Angeles. This is a separate snapshot/build/report role, not permission to mutate or stop the active builder.

For each audit:

1. Pin the exact current committed main SHA. Inspect source and completed evidence against the original ten-category rubric. Distinguish fresh grading from an unchanged prior score. Last full accepted audit is 50/100, average 5.0, Tier 2/5.
2. Use an independent audit checkout, cache/daemon, outputs and receipt. Build a fresh current development JAR from that pinned snapshot, with a unique commit/run/version identity. Prefer the Windows Vulkan bundle with OpenGL fallback for the owner's Windows/8060S setup. Preserve the complete merged modules/Themes. Staged/unpromoted experiments stay excluded unless separately accepted.
3. Run full available builds/tests and relevant packaged-native checks. Verify outer/nested archive CRCs, platform/module payloads, compiled/packaged/loaded class identity, fixture exclusion, final exported SHA256/size and normal save/reopen/shutdown as applicable. Label missing Windows/client/native checks explicitly. A mathematical check is not game acceptance.
4. Save successful audit JARs using the Library skill and include the real download link with the graded update, pinned SHA, verification scope and remaining defects. These are development snapshots; this instruction does not authorize stable-release labeling or automatic CurseForge/GitHub Release publication.
5. If a build or essential delivery gate fails, retain diagnostics and report the failure. An older verified artifact may be linked only with its actual older identity. Do not claim an unavailable, corrupt or failed current build succeeded.
6. Do not interrupt or take over a builder lease. Source inspection is observational. Use independent resources for compilation/native work, and defer only the auditor's conflicting heavy work during a builder's timed measurement. Never contend over mutable build outputs or caches. A deferred unfinished audit build is reported honestly while the builder continues.
7. Publish per-audit evidence under a unique report path with a guarded fresh-parent/ref update that preserves all builder state and unrelated files. If main advances, re-read and merge only the independent report, or retain it separately; never force a stale ref. Identify newer builder changes that arrived after the pinned snapshot.

Neither role pauses solely because a progress report is finished. Keep both schedules enabled until the owner cancels or a fresh complete audit demonstrates the agreed average of at least 8.0. Never inflate scores for unused libraries, staged code or unrun tests.

## Current recovery checkpoint

Workspace maintenance removed the earlier local full checkout/cache. Current main at `7f314d34caaff2931f6bbb7e5c40671dd299d3ef` and connected Library were checked; available retained Library results include owner installers/evidence, not a full current toolchain/cache. Private CLI checkout has no authentication; current core was restored through the authenticated GitHub tool and all 57 Git blob identities verified.

The pinned restoration helper successfully verifies JDK25, JDK21, Gradle9.2.1, NeoForm and ASM archives. Fresh complete-core javac and R61's existing staged reuse/noise/graph checks pass, including 1,581,340 reuse checks and 102 product core classes. No engine source changes, new performance measurements, full Gradle/native/client build or owner JAR are claimed in this coordination checkpoint. Full source/dependency restoration remains the next audit-build prerequisite. See the accompanying recovery records. Existing staged code and last full grade remain unchanged.



## Owner pause — 2026-10-24

The owner asked to stop development and upload all work, then explicitly clarified to keep the auditor. Builder task `6ad974bdc204819195e5465aeea9dca2` is paused; hourly auditor `6adcfe07f3888191aa478160f40027da` remains enabled. No further implementation or experimental promotion is authorized during this pause. The auditor continues inspecting committed snapshots and producing its requested current development JARs. The owner must request builder resumption. See `docs/validation/engine-continuation/r62-full-validation-pause-2026-10-24/README.md` for the uploaded checkpoint and latest completed isolated candidate gates. Existing V8 and overall grade acceptance remain unfinished.
