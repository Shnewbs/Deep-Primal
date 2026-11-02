# Deep Primal Dev189 — independent hourly audit

**UTC window:** 2026-11-02 08:30. **Baseline:** `672ae409788ccce600a1eba9af0709eb984aa6e7` (Dev187). **Reviewed main:** `4e86276366c82c23675d70149106ca7addbac315` (Dev188). **Scope:** independent review of the 12 new commits and eight changed paths; worldgen lifecycle, regression quality, NeoForge build gating, renderer/terrain/water/LOD acceptance status. No other writer-owned production files changed.

## Change review

The 12 commits introduce a shutdown/completion arbitration correction to `engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java`, a registered shutdown regression, deterministic race fixture, negative control, validation script and claim/release documentation. Production `WorldGenPipeline.java` Git blob is **`a2f7080346a1e24d594187f12b0105059b92231b`**. Historical pre-fix Git blob is **`2e1a5cb55007d10745260dd355fbf180d5f2920b`**. The claim was released by the engineering writer before this audit.

The prior implementation removed a pending query before settling its shared future, allowing shutdown to miss that query. The new implementation retains it until settlement and arbitrates with a per-query atomic state (`OPEN` / `SETTLING` / `CANCELLED`). The completion callback remains outside the pipeline monitor, allowing reentrant user continuations.

## Independent evidence (OpenJDK 21, isolated production class)

Compiled the **exact production Git blob** with the repository's minimal `ChunkCache` and `ChunkMaterializer` API fixtures using `javac --release 21 -Xlint:all -Werror`, then ran `java -ea -Xverify:all`. Three independently authored suites passed:

| Test | Result | Scope |
| --- | ---: | --- |
| Seeded lifecycle, cache, key, batching, pending limit | **80,006 assertions PASS** | 20,000 seeded trials; 4,096 batch; 64 pending cap |
| Deterministic shutdown/completion interleaving | **6,000 assertions PASS** | 1,000 pause-gated trials |
| Cache admission failure, dispatch failure, reentrant callbacks | **12,000 assertions PASS** | 6,000 trials |
| **Total** | **98,006 PASS** | Isolated Java21, not a native game build |

A test-only `RaceGate.pause()` was injected into temporary copies after cache admission, before future settlement. The exact historical source, instrumented at the same seam, **failed as expected** at `pending removed prematurely check=2`. Neither instrumentation nor fixture code was added to production. This independently supports the Dev188 race fix. No additional nonconflicting production defect was confirmed.

## Build and compatibility

The repository remains pinned to `neo_version=26.1.2.122` in `gradle.properties`, with a Java25 toolchain in `build.gradle` and ModDevGradle `2.0.148`. The official NeoForged project listing currently shows `26.3.0.101` (https://projects.neoforged.net/). Minecraft's official 26.4 Snapshot 1 notes (published 2026-09-22) make Vulkan the default graphics API: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1 . These are **migration risks**, not evidence of a successful port.

This audit runtime has **Java21 only**, no full private checkout, no local Gradle wrapper/dependency cache, and GitHub DNS does not resolve for local clone. Authenticated GitHub connector access works for source review and committing the audit. No full Java25 Gradle build, NeoForge client/server startup, finite-water conservation, native terrain/world reopen, Vulkan/OpenGL render test, LOD FPS measurement, or memory/throughput benchmark was performed. The new per-query `AtomicInteger` has not been benchmarked under game load.

**No fresh playable JAR or JAR SHA256 was produced. No existing audit JAR was deleted. No GitHub Actions were invoked.**

## Grading and next gate

- **Scoped worldgen shutdown correctness: 8.5/10 provisional**, supported by exact-source, negative-control and 98,006 independent passing assertions.
- **Whole-engine: historical 5.0/10 baseline, not freshly regraded.** Unverified native and platform categories cannot be treated as passes.

Next gate: acquire complete checkout and Java25 dependencies, run full `coreChecks` and Gradle `check`/`jar`, verify client/server boot, shutdown concurrency, world save/reopen, finite-water conservation, terrain seams, Vulkan/OpenGL fallback, LOD memory/FPS, and publish a JAR only after SHA256 and smoke tests. Preserve previous JAR until the verified replacement exists.

**Local evidence archive:** `DeepPrimal_Dev189_IndependentAudit_20261102T0830_Evidence.zip`, SHA256 `cc8fd8a75f12ed12fd5a74e3d27773662036b7890eb63100ed263781d94d6c26` (source snapshots, isolated test fixtures, test scripts, negative-control output, integrity manifest). Archive is a local audit artifact, not a release JAR.
