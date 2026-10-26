# Aquifer elapsed-tick overflow candidate

Base: 044aa85fd1ecdc9f62faa33ee6a3ca38aaa7221b; production remains dev.26-water.1. The staged tree contains the proposed dev.27 version, Aquifer repair, regression and CoreChecks wiring. Do not promote or deliver a dev.27 JAR until packaged native/save/reopen gates finish.

Reproduction: Aquifer constructed with lastTick=Long.MIN_VALUE and step(0) returns false. The codec currently permits every signed long tick. The mathematical elapsed duration exceeds20, but signed subtraction overflows negative. This is an extreme persisted-counter/API boundary, not an observed ordinary-world stall, and does not explain the auditor's unresolved opt-in reopen timeout.

Repair: first reject non-forward timestamps; for ordered timestamps treat negative subtraction as an elapsed gap larger than Long.MAX_VALUE. Preserve the20-tick minimum, one fixed step after any gap, no accumulated backlog, drought/activation thresholds and unchanged water/salt/sediment accounts. Frozen geography, art, water renderer, R57/R60/R61/R62 and foreground ledge changes are untouched.

The standalone Java25 regression checks 144 boundary combinations and50000 deterministic random pairs against BigInteger elapsed arithmetic. It verifies one-step wet ages, saved tick, duplicate replay rejection and exact ground/snow/constituent conservation. Baseline fails as expected; repaired candidate passes50144 cases.

Restoration: copied the completed auditor's source and dependency cache into builder-owned directories; no auditor directory changed.840 present tracked files match current-main Git blobs, with only the intended Aquifer modification differing at preflight. Main after the source pin contains audit/coordination changes only. Private toolchain/helper remains outside product; all outputs/caches are separate. Initial build setup used the wrong local working directory and failed before compilation; corrected invocation retained separately.

Consumed audit: docs/engine-hourly-audits/2026-10-26T0653-0818658/REVIEW.md, pinned081865869425a1ce7d7148d6782ee485218549d1. Latest scoped and last full accepted grade5.0/10; no new full regrade. Existing native/visual records are historical, not fresh candidate acceptance. Foreground water claim expired03:19Z and root writer was released. Personal-context lookup did not establish live other-chat activity. Auditor recorded completed07:20Z; this does not imply ongoing liveness.

Next: complete packaged native fresh/reopen/shutdown and source/package identity gates for the candidate, then guarded promotion/versioned delivery. Continue the broader loaded/unloaded/edit water-ledger and registered V8 saved-profile/topology work. Neither this boundary fix nor successful build closes water-engine acceptance.

Independent Java25 compilation of130 production/test inputs and the complete CoreChecks suite pass (core-result.json exit0), including500000 randomized hydrology transfers. Incidental printed timings are not controlled benchmarks.

Full Java25/Gradle9.2.1 build passes35 executed tasks in5m20s, including core/resource/module/cache boundaries and visual fixture compilation. Package validator passes7 archive CRCs,587 product and142 independent core class matches,208 private exclusions,2508 Themes resources,43 palettes,11 approved ground images and148 generated soil resources. Packaged-core regression passes50144 cases. Native save/reopen/shutdown on the candidate and real GPU/Windows gates have NOT run; no new user-facing qualified JAR or release. Local build artifacts remain candidates, not current production.
