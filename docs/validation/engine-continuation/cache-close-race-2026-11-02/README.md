# Dev204 — Terminal chunk-cache close race (2026-11-02 UTC)

## Scope and coordination

- Baseline GitHub `main`: `15c3bd7573ca4c54fdd29296ab00dd63b584ba75` (Dev203 independent audit).
- Consumed `docs/engine-hourly-audits/2026-11-02T1530-dev203/REVIEW.md` and checked the existing writer lease (released).
- Only `ChunkCache.java`, `CacheCloseRaceChecks.java`, `CoreChecks.java`, the scoped validation documents, and the writer lease are changed. No organics, renderer, water physics, terrain, save-format, or GitHub Actions changes.

## Reproduced defect

`ChunkCache.getOrCompute` releases its lock while a caller computes a chunk. If another thread calls terminal `close()` during the computation, `close()` clears entries and increments `generation`. The original completion path checked `generation != observed` first and returned the computed value even though the world had been retired. This bypassed the cache's `ensureOpen()` terminal gate and could deliver stale chunk results to a caller after shutdown.

The exact baseline `ChunkCache.java` blob is `0ed8a911b0baabc6c35f204761e2e6b4c9e2a826`. A deterministic latch-controlled test reproduces the stale return on the baseline; its negative control fails with `closed world returned obsolete value`.

## Correction

The completion path now calls `ensureOpen()` under the cache monitor **before** comparing generations. A terminal close rejects late completion with `IllegalStateException`; a reusable `clear()` still allows the original caller to receive its computed value without repopulating the cache. No new allocations or locks are introduced on the hot path.

## Reproducible validation

Run `bash docs/validation/engine-continuation/cache-close-race-2026-11-02/run.sh` from a complete checkout. It compiles the exact production cache and both registered lifecycle fixtures with an isolated `EngineMetrics` counter stub, executes positive checks, and reconstructs the old completion path as a negative control. No GitHub Actions or NeoForge runtime are used.

- OpenJDK 21 `javac --release 21 -Xlint:all,-cast -Werror`: PASS.
- Java `-ea -Xverify:all` existing `CacheLifecycleChecks`: **14 checks passed** (including clear-during-load behavior).
- New `CacheCloseRaceChecks`: **5,001 assertions passed**, 1,000 deterministic close-during-load interleavings.
- Compiled negative control: **failed as expected** with the reproduced stale-return assertion.
- Production `ChunkCache.java` Git blob: `b99355c7b618fdb4840b838d40e05431f101a99f`; SHA-256 `d18ff908dac05b27e065bd8a7ebd037d0421d1dbf18dda2ed8700c9e84ef3177`.
- New test Git blob: `9335197562b0b2e983c19ab10413f484cf4d0bf8`.

## Boundaries and next work

This is an **isolated** Java21 production-source regression, not a full Java25 Gradle/NeoForge compile or a native client/server integration test. No playable JAR or JAR SHA-256 was produced. The local environment cannot resolve github.com for a full checkout, has Java21 but not Java25, and lacks the resolved Gradle/NeoForge dependency tree. Previously qualified JARs must be retained until a verified replacement exists. The independent historical whole-engine grade remains **5.0/10**, not freshly regraded.

Next: restore the Java25 checkout/build, run `coreChecks` and `jar`, validate close/clear lifecycle under actual chunk-generation cancellation, and continue finite-water save/reopen and Vulkan/LOD native acceptance.
