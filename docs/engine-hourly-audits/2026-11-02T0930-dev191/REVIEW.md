# Dev191 — independent audit of Dev190 water query scratch

**UTC cycle:** 2026-11-02 09:30–09:38. **Reviewed baseline:** `e6074729346d9fcb42543478b5fb3f3979043de7` (8 commits since Dev189). **Scope:** Dev190 query-water bulk restore, reentrant callback safety, ordinary transport parity, Java21 isolated verification, build/NeoForge acceptance. This audit is independent of the Dev190 engineering claim, which was marked completed/released.

## Confirmed regression and correction

The Dev190 optimization moved the 256-int height scratch from a local tick variable to the instance field `nextGround`. A `DoubleBinaryOperator` height callback may call `tick` again on the same object: synchronized Java monitors are reentrant. The nested call overwrote the outer call's partially filled scratch. On returning, the outer tick published mixed terrain heights, changing pressure and potentially water movement. This did not happen before Dev190, when each invocation owned its own array.

**Negative control:** the exact original production Git blob `cb0de12bdef9b536a63e512ef6452b06d2218aff` fails on trial 0, assertion 2 (reentrant callback at cell 68; cell 0 pressure uses nested rather than outer height).

**Fix:** track sampling depth under the existing synchronized tick monitor. The common non-reentrant tick continues using the retained instance scratch with no per-tick scratch allocation. Reentrant calls allocate a separate 256-int scratch, and `finally` restores depth even when a callback throws. Preserve the original two-phase sample/validate-then-commit behavior.

## Verification

- `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` with exact `WaterSimulation` and `WaterTransport` production sources, and isolated test-only `TerrainNoise` / `EngineMetrics` stubs.
- **78,228 independent assertions passed:** 300 seeded reentrant cases, every one of 256 cells, nested failure/recovery, three-deep callbacks, and ordinary scratch identity retention.
- **616,510 existing registered Dev190 bulk-restore assertions passed** on the corrected source. Existing test Git blob `050e0342dc12f8b329a91adf82a8bbbbc6f0f806`.
- **Ordinary original-vs-corrected parity:** 250 randomized imports, 1,500 ticks, exact packet/pressure/flow digest `cd5909e4d0e2816e` on both implementations; water conservation checked every tick.
- Proposed corrected production source Git blob `57e72048fabc544e709f360d598db3d18fb6e204`; new registered regression Git blob `468fb3bd0a43a8802c3e3151c04becd60d45fa82`.
- Local evidence ZIP `DeepPrimal_Dev191_IndependentAudit_20261102T0930_Evidence.zip` SHA256 `dd74aebe6bd474db8c0f1d7e5ecdad667475e9ae4952a5f706330700958f2f63`.

## Scope, compatibility and grades

Repo `gradle.properties` still pins NeoForge `26.1.2.122`; root `build.gradle` requires Java25. Minecraft 26.4 snapshots change the default graphics API to Vulkan (Minecraft 26.4 Snapshot 1, September 22, 2026: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1). This is a compatibility watch item, not proof of native 26.4 compatibility.

**Provisional scoped query-water safety: 8.5/10** (isolated tests only). **Whole-engine historical baseline: 5.0/10**, not freshly regraded. Renderer, Vulkan, LOD FPS, terrain seams, native finite-water persistence, client/server startup and Java25 NeoForge compatibility remain unverified in this run.

**Packaging blocker:** local runtime has Java21, no complete private checkout, no Java25 toolchain, no cached Gradle/NeoForge dependencies, and local GitHub DNS is unresolved. No full Gradle build or playable JAR was produced; therefore no replacement JAR SHA256 exists. Existing JARs were preserved. No GitHub Actions invoked.

**Next acceptance:** restore Java25 checkout/dependencies; run `./gradlew --offline check jar` only after dependencies are resolved (otherwise online dependency fetch), test packaged client/server and finite-water save/reopen, then native Vulkan/LOD/terrain benchmarks. Do not delete an older audit JAR until a replacement is independently verified.
