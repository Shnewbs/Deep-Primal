# Deep Primal dev188 — worldgen shutdown/completion arbitration

Date: 2026-11-02 UTC. Starting main: `672ae409788ccce600a1eba9af0709eb984aa6e7` (Dev187 independent audit). Writer claim: [CLAIM.json](CLAIM.json). This change is limited to engine-core worldgen lifecycle; no native terrain, finite-water chemistry, renderer, LOD or saved-world format changes.

## Confirmed race

The previous `WorldGenPipeline` removed a materialization query from `pending` while holding the pipeline monitor, then settled its shared `CompletableFuture` **outside** that monitor. If `shutdown()` acquired the monitor after removal but before settlement, it could miss the query and a materializer result could succeed after shutdown. This is a real interleaving reproduced by the deterministic negative control, not a claimed observed gameplay crash.

A first narrow correction retained the pending query until future settlement, but left a smaller completion-vs-shutdown race because `shutdown()` cancels futures outside the monitor. The final correction uses a per-query atomic decision (`OPEN` → `SETTLING` or `CANCELLED`). Shutdown chooses cancellation under the pipeline monitor and clears cache; completion competes through compare-and-set after cache admission. If shutdown wins, later materializer completion cannot publish success. If completion wins first, it is allowed to finish outside the monitor, preserving callback reentrancy and the pre-shutdown result. Terminal shutdown does not re-admit queries.

## Validation (OpenJDK 21)

Run `bash docs/validation/engine-continuation/pipeline-shutdown-race-2026-11-02/run.sh` in a checkout containing the prior Git blob. It compiles the **exact** production class with the already committed isolated `ChunkCache`/`ChunkMaterializer` API fixtures and uses `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`.

- Existing worldgen key-admission regression: **110,000 assertions PASS**, 10,000 trials.
- Registered new shutdown/coalescing/late-result regression: **60,000 assertions PASS**, 10,000 trials.
- Test-only post-admission/pre-settlement barrier: **6,000 assertions PASS**, 1,000 deterministic trials. The runner injects `RaceGate.pause()` only into temporary compilation copies; production has no test hook.
- Negative control: prior production Git blob `2e1a5cb55007d10745260dd355fbf180d5f2920b`, instrumented at the same seam, **fails as expected** at assertion 2 (`pending removed before future settled`).
- **176,000 passing isolated assertions total**. The new shutdown regression is registered in `CoreChecks`.

Exact source Git blob: `a2f7080346a1e24d594187f12b0105059b92231b`. New registered test Git blob: `8e7339b072b7f147a5d601bb5faeba7d4f733751`.

## Build, performance and release gate

No full Java25 Gradle/NeoForge build, packaged client/server launch, save/reopen, native Vulkan or GPU test was run. This runtime has Java21 only, no full private checkout or cached Gradle/NeoForge dependencies, and local GitHub DNS cannot resolve for Git clone. The authenticated GitHub connector is used for source commits; **GitHub Actions are not used**.

The atomic decision adds a `Query` and `AtomicInteger` per outstanding request. Its allocation and throughput overhead, especially under 64 concurrent admissions and slow user continuations, still needs production benchmarking. There is **no verified playable replacement JAR or JAR SHA256**. Preserve the last successful JAR until a fully built and verified replacement exists. The historical whole-engine score remains **5.0/10**; this scoped regression is not a full-engine regrade.

Next: obtain the complete Java25 checkout and dependency cache; run full CoreChecks, Gradle build, native server/client startup, shutdown races and world reopen; then benchmark worldgen throughput and continue finite-water conservation and Vulkan/LOD acceptance.
