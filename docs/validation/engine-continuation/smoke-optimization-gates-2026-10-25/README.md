# Native smoke acceptance survives Python optimization

Base main: c5794ea222902761ea897e8767617c434efb0e81. Consumed independent audit: docs/engine-hourly-audits/2026-10-25T2055-cf46cf7/REVIEW.md (pin cf46cf7e58760e0af1aa5f68edc021c91ab9b150). External grade remains 50/100, carried forward.

The auditor reports full build/package and default-channel2/opt-in4 native save/reopen acceptance on its pinned dev.21 snapshot. Neither staged overflow repair is promoted. Those qualification priorities remain open; this repair addresses the reliability of their future native acceptance harness.

## Reproduction and implementation

The production tools/server_smoke.py used ten assert statements for seed identity, sample presence/replay, bedrock and runtime cache/benchmark/cancellation checks. Python -O/-OO removes them. Controlled invalid server responses therefore reached a successful fresh/reopen receipt even though the game evidence was invalid. Lifecycle shutdown checks alone did not prevent this.

All ten assertions are now explicit conditional RuntimeError failures. The existing finally cleanup and end-only receipt publication remain intact. No acceptance conditions are weakened. No engine runtime, resource, native fixture, saved geography, water accounting, art or product version is changed. This is committed validation tooling, not a new dev.22 runtime or a promotion of the staged density/cache fixes.

The actual run() path is exercised with controlled process/RCON doubles. New tests reject wrong seed, missing sample, changed reopened samples, missing bedrock, initially busy benchmark, absent cache hit, failed radius rejection, failed active-request rejection, failed cancellation and leaked forced chunks. Both ordinary and runtime-enabled successful fresh/reopen controls remain accepted. Failed sessions leave no stale or partial success receipt.

## Executed checks

With the final 16-test fixture, the baseline passes normal Python but fails ten cases under -O: each malformed scenario wrongly succeeds. Candidate passes all 16 tests under normal Python, -O and -OO. Existing crash precedence, ordered shutdown, required-marker, flush and receipt tests also pass. Logs and hashes are retained here. These are controlled harness regressions, not new native Minecraft runs or a gameplay regrade.

Reproduce from the committed repository:

```sh
python -m unittest discover -s tools -p test_server_smoke.py -v
python -O -m unittest discover -s tools -p test_server_smoke.py -v
python -OO -m unittest discover -s tools -p test_server_smoke.py -v
```

To reproduce the baseline failure, run the current fixture against server_smoke.py from the base SHA under -O in an isolated directory.

## Limits and next steps

The overlay filesystem again reports zero available storage. No separate full Gradle/build/module/package/native environment was started, no new JAR was produced or published, and no Windows/AMD8060S, graphics or performance claim is made. The prior auditor's dev.21 artifact remains its original immutable snapshot and does not contain this tools-only checkpoint. Restore isolated build capacity to qualify ByteLru and finite DensityGrid repairs with a version bump and full package/native gates, then continue V8 integration and current opt-in footprint/flow/visual acceptance.

Personal Context search found no reliable live writer claim; auditor run records showed its latest run completed at 21:06:39Z. Main's writer lease had expired, the timing reservation was completed, and no Java/Gradle process was observed at acquisition. These checks are point-in-time evidence, not continuous coordination. Work used a separate memory-backed directory; no other checkout/cache/output was edited.
