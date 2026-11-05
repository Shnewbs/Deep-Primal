# Legacy fixture schema follow-up — 2026-10-25
Source inspected: `6299c572d320f6a5169c3894e2ff0de6c9b6b97b` (dev.20).
Consumed audit: `docs/engine-hourly-audits/2026-10-25T1749-67bbadc/REVIEW.md`, pinned `67bbadc14d74d515a250f537bd8f3c3cef273bd8`.
External grade remains 50/100, 5.0/10. No new regrade.

## Actual repair
The concurrent dev.20 commit already repaired the reported V6 material/channel contamination. This follow-up preserves that fix. DepositNativeChecks also supports V1-V5, whose EngineChunkGenerator codecs have only biome_source/min_y/height; they cannot encode a terrain profile inherited from a V6/V7 template. The fixture's strict JSON equality would consequently fail.

Remove terrain only for requested revisions below 6, on the existing deep copy. Retain V6/V7 profiles, all biome/height fields and V7 material/channel settings. Production codecs, frozen geography and owner artwork are unchanged. This file is private visualValidation code, excluded from the shipped runtime. No production version bump or new artifact is claimed for this test-only change; current production is dev.20.

## Evidence
- Baseline transformation extracted directly from the committed fixture fails the V1 profile-schema assertion (baseline.log).
- Candidate passes 126 combinations: revisions 1..7, material 0..2, channel 0..2, profile absent/present. Checks caller immutability, supported field retention, legacy field removal and target type (candidate.log).
- Complete modified DepositNativeChecks.java compiles using Java25 against retained dev.19 Minecraft/NeoForge and project class dependencies, in an isolated output directory. The initial direct compile failed because classpath order selected incomplete Gson API classes; putting the actual Gson2.13.2 JAR first passed. Both diagnostics retained. This is a scoped compilation result, not a fresh dev.20 full build.
- run_template_checks.py reproduces the extracted actual transformation checks with REPO JDK25 GSON_JAR OUTPUT_DIR arguments; LegacyTemplateChecks.java is adjacent. It is explicitly NOT Minecraft codec/registry execution. Native V1-V5 codec round-trip and complete fresh/save/reopen gates remain required.
- No full Gradle/module/core run, native execution, client render or package qualification this cycle. No new qualified JAR, Library artifact or GitHub release. No speed/Windows/AMD claims.

## Coordination and blocker
First writer-lease update failed as main advanced from1817bea to dev.20 at6299c57. Reread main/state/code instead of forcing stale work. Acquired a new bounded lease after preserving the dev.20 snapshot. Personal Context searched twice but did not retrieve a reliable live dev.20 ownership record; Git is the evidence for its committed changes. Auditor run record reports completion18:01:52Z. Separate texture owner/art work preserved. No subagents or competing heavy jobs.

Disk remains zero available on the32GB workspace; only815MB /dev/shm was available at start. Small isolated compile/regression work ran in /dev/shm without modifying any auditor checkout/cache/output. Previous staged finite-grid repair remains unpromoted. Full isolated project/build/native/package capacity still needs recovery.

## Next
Rerun the full packaged native compatibility gates, including V1-V5 deposit fixtures and V6/V7 terrain audit fresh/reopen; qualify finite-grid candidate against current dev.20+ source and next free production version. Continue V8 native integration and engine acceptance gates. Do not repeat dev.20 V6 repair or claim this isolated template test proves native acceptance.

