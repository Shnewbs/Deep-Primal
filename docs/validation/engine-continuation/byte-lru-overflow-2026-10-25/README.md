# ByteLru admission overflow repair — staged

Source: `65dbbb81a2dadf4fdf594f19535ae83806c9da2a` (current dev.21 delta/oxbow source).
Consumed audit: `docs/engine-hourly-audits/2026-10-25T1953-2854805/REVIEW.md`, pinned to `28548052886603214914c5d6fd49ffbfe85d2a02`.
Last external grade: 50/100 (5.0/10), carried forward; no new full regrade.

## Defect and repair

With budget `Long.MAX_VALUE`, entry cap 4 and a stable weight function, put weights `Long.MAX_VALUE` and `1` under distinct keys. The baseline adds before checking its limit: bytes wraps to `Long.MIN_VALUE`, two entries survive and the budget check is bypassed. This is a supported extreme-value API case; the current horizon caller's small practical budget does not establish an observed in-game overflow.

The independently authored candidate reserves room before adding. Since `0 <= size <= limit`, `limit - size` is safe; evict until `bytes <= limit - size` and there is an entry slot, then add. The previous replacement/oversized invalidation policy, zero-capacity behavior, recency and negative-weight rejection are retained. The existing requirement for stable weights is unchanged. No terrain, hydrology, art, configuration, saved-profile or production-version changes are made.

The runtime candidate is deliberately stored here, not promoted into production. This checkpoint does not include the separately staged finite DensityGrid fix or R57/R60/R61/R62 overlays. They retain their gates.

## Evidence

`baseline.log` records the expected failing assertion with bytes -9223372036854775808 and size 2. `candidate-regression.log` records 3,379,936 checks against an independent BigInteger accounting oracle: 80,000 seeded operations across five byte limits and four entry ceilings, lookups/retention, replacements, oversized values, zero weights, negative rejection and reset. These are assertion counts, not distinct world scenarios.

All 111 current core/check input files were checked against their Git blob identities in `inputs.json`; only ByteLru was replaced in the isolated candidate compilation, alongside the private regression. See `summary.json` and the complete `candidate-core.log` for final completion status. Java 25 uses `-Xverify:all`; this is direct core compilation/execution, not a Gradle or packaged Minecraft build. Timing lines emitted by the core suite are diagnostic only, not controlled benchmark evidence.

## Reproduction

With an unmodified checkout of the pinned source, JDK25 root, and a nonexistent output directory:

```sh
python run_checks.py /path/to/pinned-checkout /path/to/jdk25 /path/to/new-output
```

The runner verifies every pinned input, requires the specific baseline overflow assertion, compiles the candidate independently, requires its pass marker and runs CoreChecks. It never edits that checkout. The runner itself was syntax-checked; the equivalent recorded commands were executed manually this run (the entire runner was not rerun just to duplicate the core suite).

## Blockers and continuation

The overlay filesystem reports zero available bytes; shared memory has approximately 0.5GB free. Other writers' caches/checkouts/output are untouched. An isolated full Gradle dependency/build workspace and packaged native execution were not attempted under that capacity constraint. This is not a failed Gradle run. No fresh testing JAR, archive qualification, release upload, Windows/AMD8060S execution, graphics/performance acceptance or Library artifact is claimed.

The audit's native V8 materialization/topology, full water timing/continuity and broader enabled-view/LOD findings remain open. This bounded-cache repair advances an independently reproducible defect while those larger gates lack build capacity; it does not close those findings or improve the score. Restore isolated build capacity, qualify this and the finite-grid candidate with full build/module/package checks and relevant packaged gates, bump beyond the then-current production version on promotion, then resume native V8 and current opt-in revision-4 save/reopen/visual acceptance. Preserve the current foreground delta/oxbow work and approved resources.

Coordination: Personal Context and auditor run records were checked. Personal Context did not establish a reliable current live writer claim. The latest observed auditor run completed at 20:01:38Z; Git lease was expired and no Java process was observed at acquisition. A bounded main writer lease was acquired with expected-parent protection. These are point-in-time observations, not continuous cross-chat monitoring.
