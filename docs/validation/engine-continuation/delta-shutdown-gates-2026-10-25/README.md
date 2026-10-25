# Delta fixture coverage and native shutdown gate — 2026-10-25

Base main: `b4359f4ace9cad1b482b03192c87d017eccb291c` (dev.20 plus the owner's new delta core work). Writer lease: `3a51ad0fc6d7a8be92da65175b5e3223188c5619`.
Consumed independent audit: [2026-10-25T1849-31dd0b2](../../../engine-hourly-audits/2026-10-25T1849-31dd0b2/REVIEW.md), pinned `31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545`.
Last external grade **50/100 (5.0/10), carried forward**. No regrade or >8.5 acceptance.

## Repairs

1. **DeltaChecks dry-bank coverage.** Fresh unchanged main compiled but CoreChecks failed with `No depositional delta plain: 0`. The five near-trunk points per fan were all wet: 105/105 across 21 fans. An independent wider 756-point survey found 50 raised dry-bank points. The fixture now adds a bounded 9-by-21 survey across each fan's length and width. All original water, ground, revision-2 fingerprint, endpoint-depth, containment and outside-footprint assertions remain, including the original minimum deposition count. This corrects the sampling defect without changing delta geometry or lowering a threshold. Final checks find 233 deposited dry-bank points, 252 wet distributary endpoints, maximum sampled depth 7.82 and zero sampled outside changes. This is numerical fixture evidence, not visual or integrated channel-revision-3 acceptance.

2. **server_smoke.py native completion gate.** Its old final check accepted only the launcher exit code, and wrote partial success receipts before the complete fresh/reopen sequence. Minecraft crash cleanup can save all dimensions and still leave Gradle successful. The new explicit verifier rejects native failure markers even if PASS/cleanup lines exist, requires ordered startup/stop/final-save/loader-close records, and optionally requires exact fixture PASS tokens with `--require-marker`. Save flush must acknowledge success. A new run removes the old receipt, and success is written only after both completed sessions; at least two sessions are required. The controlled run-loop reproduction injects the retained real dev.19 crash log with launcher exit zero after successful mock commands: baseline writes success, candidate rejects it and leaves no success receipt. This is a harness regression with controlled processes, not a newly executed Minecraft crash.

## Executed evidence

- 110 current core/check Java sources compiled on Java 25.0.4.1+1. Unchanged main fails at the delta fixture (baseline-core.log).
- Complete dependency-free CoreChecks passes with only DeltaChecks changed (delta-candidate-core.log). A subsequent clean full compilation produces 172 class files byte-identical to the executed baseline-plus-fixture classpath. Noise/graph/reuse/adversarial/density, terrain, geology/soil, drainage, finite-water and other core gates pass. Incidental timing lines are not controlled benchmarks.
- Ten Python regression methods, including failure precedence, missing/out-of-order shutdown, exact required-token matching, failed save acknowledgement and stale/partial receipts, pass normally and with `python -O`. The explicit new checks do not depend on Python assertions.
- Historical full-log replay correctly rejects dev.19's real native crash and accepts the auditor's dev.20 fresh and reopen logs with terrain/deposit/soil/source PASS markers required. SHA256 identities are retained. These are historical logs, not fresh native runs; optional external key-fetch errors remain distinct from main/server-thread failures.
- The existing staged finite-grid candidate still passes 11,728 checks against current core inputs; the unchanged production grid still fails its extreme finite-endpoint reproduction. It remains unpromoted. The local dev.21 runtime/version overlay prepared for qualification was excluded from the final source scope.
- Final source check verifies 831 present tracked inputs: only DeltaChecks and server_smoke differ; test_server_smoke.py is new. Production core, main adapter, resources, art, profiles, version and all other existing files remain unchanged.

## Build blocker and environment

Recovered storage by removing only this builder's duplicate extracted tools and incomplete tool-download copies after verifying retained authoritative tool files/archive SHA256 values. No other checkout/cache/output was modified. The first duplicate check refused deletion because partial tool copies differed; those were classified explicitly before cleanup.

Restored independent current source and copied a private Gradle cache excluding assets. Capacity then fell to 123,654,144 disk bytes and zero memory-backed bytes; the previous audit's root/Themes build outputs alone occupy approximately 704,643,072 bytes, before a further 126,877,696-byte native extraction cache. Reflink cloning is unsupported. Full Gradle was **not started** after this failed preflight. Removed only the just-copied private cache and moved this builder's source resources out of scarce memory storage. Full source remains resumable under this builder's engine-builder-1927 directory.

No fresh full Gradle/module-boundary/package/native/server/client/visual/performance or Windows/AMD8060S gate ran. No new JAR, release, version bump or Library upload is claimed. The auditor's qualified dev.20 binary is pinned to 31dd0b2 and predates the new delta core source and these validation changes; it is not a current-main build. Current production version remains dev.20 because only validation/tooling is promoted here.

## Coordination and continuation

Git state was re-read; the owner's b4359f4 delta changes were preserved. Personal Context was searched twice, but did not supply a reliable current delta-writer claim. The auditor's recorded latest run completed at 18:59:32Z; its successful legacy/native fixture qualification closes the prior dev.19 fixture blocker. The main lease was expired and the recorded measurement reservation completed before this builder claimed a bounded lease. No Java job was observed at start. No subagents or other-checkout jobs were used. Final update uses the expected parent; the writer lease is released at this checkpoint.

Next: restore sufficient isolated full-build/native/output capacity; qualify the existing finite-grid fix and permanent regression wiring with a version beyond current production. Then continue native V8 saved-profile/materialization/topology/routed-water gates and bounded performance/water/LOD acceptance. Preserve unaccepted overlays, the owner's delta implementation, frozen saved geography, exact water accounting and approved art. No continuous execution between finite runs is claimed.

## Reproduction

On this checkpoint, `python3 -m unittest discover -s tools -p test_server_smoke.py -v` exercises the harness without a game or network. Repeat with `python3 -O` to check explicit rejection behavior. Compile all engine-core/src/main/java and src/coreChecks/java sources using Java25, then run `com.shnewbs.deepprimal.core.CoreChecks` with `-Xverify:all -Xmx1G`. For the baseline failure use b4359f4; for the independent survey compile DeltaDiagnostic.java against those classes. Full native qualification remains a separate required gate.
