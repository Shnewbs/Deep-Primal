# Deep Primal Dev146 — independent audit and repair

**Audit window:** 2026-11-01 06:30 UTC (2026-10-31 23:30 PDT).  
**Previous audited head:** `39abfe101e02e901525ad53060b3864389745875`.  
**Builder head reviewed:** `0175958bb0c5b1c517ec8032bbb7e10da47b6f37`.  
**Delta:** eight commits, seven changed paths; primary production change `WorldGenPipeline.java`.

## Review and confirmed defect

The Builder repaired two cases where a null materialization or a cache `RuntimeException` left a query future unresolved. Its 250-trial isolated suite and documented negative controls were reviewed.

A remaining related defect was independently reproduced: a synchronous `Error` from `materializer.materializeAsync` escaped `generateChunk` while leaving an entry in `pending`. Separately, an `Error` thrown by cache insertion during `whenComplete` was captured only in the ignored dependent stage, so the caller's future timed out even though the pending entry had been removed. The tests deliberately inject `AssertionError` and `LinkageError` to represent invariant/linkage failures; they do not simulate a real fatal VM crash.

The correction catches `Throwable` in the synchronous dispatch and cache-admission callback, completing the original query future exceptionally while retaining pending cleanup. It does not alter terrain density, water volumes, rendering, LOD, or persisted world data. The writer lease for the Builder's Dev146 scope was checkpointed and released before the auditor wrote these changes.

## Evidence and tests

- Exact audited production Git blob: `c705484b042d91b53fc00b8ea2173e4c9538f798`.
- Exact corrected production Git blob: `18a3524cd2c2b08eb5e63f18223b2711a366a0c2`.
- Independent regression Git blob: `5620a9a38d0f7f0e1f7c3d2b9412c5621ae44cd4`; registered in the isolated pipeline completion `run.sh`.
- Baseline synchronous `AssertionError`: failed with `Synchronous Error escaped API; pending=1`.
- Baseline cache `AssertionError`: failed with `CACHE_ERROR_STUCK pending=0` after a 250 ms timeout.
- Corrected production source: strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed **3,750 existing checks in 250 trials plus 600 independent Error-path checks in 100 trials**.
- The local fixture uses isolated test doubles for `ChunkMaterializer` and `ChunkCache`. This is not a Java25 NeoForge build or native Minecraft execution.
- Local evidence archive: `DeepPrimal_Dev146_IndependentAudit_20261101T0630_Evidence.zip`; SHA-256 `7f2581961c17a306d1a503d2e4c707dfa41ccb1ad8efe5d437716a107669c24c`.

## Build and compatibility acceptance

- `gradle.properties` pins NeoForge `26.1.2.122` and mod version `0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`; root and engine-core build scripts require **Java25**, Gradle wrapper is **9.2.1**.
- Current runner provides Java21 only, no complete checkout, no Gradle binary/cached dependencies, and no GitHub DNS through Git CLI. GitHub connector was used for source read and writes.
- No Java25 Gradle/NeoForge compile, playable JAR, JAR SHA-256, or native client test was possible. No previous JAR was removed and no GitHub Actions were used.
- Water conservation, terrain seams, V8 save/reopen, Vulkan/OpenGL, LOD, frame pacing and hardware performance were **not independently executed**. NeoForge 26.3/26.4 compatibility is unverified.

## Grades

**Scoped async worldgen future-completion integrity: provisional 8.5/10**, supported by exact production Git blob identities, baseline negative controls, strict compilation and 4,350 passing isolated checks. This is not a whole-engine acceptance grade. **Historical whole-engine baseline: 5.0/10** (carried forward, not freshly reassessed). Unexecuted native categories receive no invented scores.

## Next gates

Restore a complete local checkout with JDK25 and Gradle 9.2.1, perform a full `build` and runtime smoke test, produce and hash a fresh playable JAR, and only then replace a superseded qualified audit JAR. Continue independent native water/persistence and Vulkan/LOD acceptance when the environment supports them.
