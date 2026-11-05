# Water contact collision-probe budget — staged engine increment (2026-10-27)

**Status: source-only, not promoted into the shipping game, and no new JAR.** Base integration is `docs/validation/engine-continuation/water-contact-commit-2026-10-27/stage/`, whose six-file current-main production qualification is still outstanding. Override **only** its `WaterContactBudget.java` and `WaterContactRuntime.java` with the two `stage/` files in this directory. The remaining `WaterAccount.java`, `WaterRuntime.java`, `HydrologyRuntime.java` and `StreamSourceIndex.java` remain as in that earlier stage; do not replace unrelated newer files or frozen geography.

## Engineering change

The existing staged exact unordered contact table bounded each individual lookup by its array size, but an adversarial collision chain could still consume repeated large scans over a single tick. The candidate now charges **every examined slot**, including duplicates and misses, against a **cumulative per-level, per-tick probe ceiling** of `4 * visitLimit`. With the planned runtime `visitLimit=2048`, this is at most **8192 bucket probes per server tick**, independent of the number of caller retries or collisions. The table still holds at most 2048 exact pairs in a fixed 4096-slot array. The special signed clock and stamp wrap reset preserve epoch isolation.

`contactStatus(a,b)` returns `FIRST`, `DUPLICATE`, or `DEFER`. The adapter expressly requeues the origin through the existing finite solver FIFO on `DEFER`, rather than treating probe exhaustion as a duplicate and permanently dropping chemistry work. A rejected `HydrologyRuntime.commitContact` similarly requeues the origin to retry after a transient stale-native-state/unload rejection. Both paths exit the adapter invocation to avoid draining the same exhausted budget again. The compatibility boolean `firstContact` remains for old probes/tests but **cannot distinguish DEFER** and is not used by this adapter.

This stage does not change block water quantities, constituent transfer arithmetic, save format, V1–V7 geography, renderer, graphics settings or approved art. It does not establish a whole-tick 2ms bound: parcel reads, native lookups, solver, chunk callbacks and writes still require native/timed admission evidence.

## Reproducible checks executed in isolated local JDK21

The two standalone Java sources below compile and execute without Minecraft dependencies:

```sh
javac -d out stage/WaterContactBudget.java checks/WaterContactProbeBudgetChecks.java
java -Xverify:all -cp out com.shnewbs.deepprimal.core.WaterContactProbeBudgetChecks
```

**PASS:** `CONTACT_PROBE_BUDGET_PASS cases=1773608`. Includes randomized exact-pair/dedup oracle across six caps and 200 ticks each; adversarial colliding hashes and explicit probe exhaustion; retry after epoch rotation; unordered identity; backward signed time and generation-stamp wrap. Actual local helper source Git blob is `0e04859206fb323ea3ccde80015732ddc3000e37`; check source blob is `2e8e3ffd5a8bc5b975c8d6fe6b0ec87e258f0c1e`. These identities were independently compared with the compiled local files after the Git blobs were created.

A second **Java21 API-stubbed adapter** compile and `-Xverify:all` run passed `CONTACT_ADAPTER_RETRY_PASS cases=6 native_api_stubbed=true`. It covers commit-rejection retry, successful activation, synthetic exhausted-probe retry, next-tick continuation and per-world cleanup. Stubbed Minecraft classes are not real native game/NeoForge execution; this auxiliary fixture is not packaged. Adapter Git blob `4669702f49374f7a926968740ec7e0cf0e83c6bf` matched the locally compiled staged adapter exactly.

## Qualification still required

- Restore full pinned toolchain/source/dependencies and run the **complete Java25/Gradle build**, core/module boundaries, independent compiled/package identities, all seven recursive archive guards and private-fixture exclusion with **all six combined staged sources**.
- Exercise packaged Minecraft26.1.2/NeoForge water and lifecycle tests: actual clock epochs, full and partial cells, chemistry/constituent conservation, failed commit/unloaded neighbors, FIFO deferred work, player edits, save/reopen/shutdown, storms and travel; extend loaded/unloaded single-owner and long-soak coverage.
- Test real OpenGL and Vulkan visible cliff/stream continuity and named Windows AMD8060S frame/tick latency, including a collision-heavy scene. No FPS, 2ms, GPU, platform or integrated native result is claimed here.

The latest completed independent review consumed is `docs/engine-hourly-audits/2026-10-27T0430-bbf07e1/REVIEW.md`, pinned to `bbf07e1e40d2c367f9a9a7aed4a691cf2a436297`; it **carries** the last complete baseline **50/100 (5.0/10)** and is not a fresh regrade. Latest qualified development JAR remains dev.31. Engine and water acceptance remain open. No competing renderer checkout/claim was modified.
