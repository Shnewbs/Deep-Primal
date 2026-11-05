# Dev205 — Independent Deep Primal audit (2026-11-02 16:30 UTC)

## Scope and coordination

- Prior independent audit: `15c3bd7573ca4c54fdd29296ab00dd63b584ba75` (Dev203).
- Dev204 engineering baseline: `459c564ae00ca328060c59d1685ca345520a57b0`, **8 commits ahead**, with six changed paths in the terminal chunk-cache close scope.
- Reviewed the production change in `ChunkCache.java`, the new `CacheCloseRaceChecks.java`, `CoreChecks` registration, the runner, and the released engine writer lease. The engineering claim was marked `completed-released`. No concurrent production scope was taken.
- This audit modifies **one existing test file** and adds this report. No production, water, terrain, Vulkan, LOD, organics, or CI files changed.

## Confirmed regression and correction

Dev204 correctly added `ensureOpen()` after an in-flight chunk computation returns, rejecting stale cache values following terminal `close()`. However, `CoreCloseLifecycleChecks.java` still expected the blocked loader to return `"computed"` **after** `EngineCore.close()`. Because `EngineCore.stop()` closes the density cache, the old registered test now failed with `ExecutionException(IllegalStateException("cache closed"))`. This is a **registered-test expectation regression**, not a defect in the corrected cache implementation.

Repaired `src/coreChecks/java/com/shnewbs/deepprimal/core/CoreCloseLifecycleChecks.java` to expect `ExecutionException` with `IllegalStateException` cause while preserving checks that shutdown does not block, the caller-owned executor stays alive, closed caches remain empty, and stopped engines reject new queries.

- Correction commit: `fc5030b43159687dd992f5a897837beee233ac3d`.
- Corrected test Git blob: `e0e44e5ffacb257bc9d27fc51296c651485d2d1a`.
- Original test Git blob: `eced6a462b300da225d1ee3f61f0d509bffb9358`.
- Exact production `ChunkCache.java` blob: `b99355c7b618fdb4840b838d40e05431f101a99f`.
- Exact production `EngineCore.java` blob: `8da45c77d39b61435dd2f9ce5fbb368745dc3a89`.

## Independent reproduction

Local Java21 `javac --release 21 -Xlint:all,-cast -Werror` compiled the exact GitHub `ChunkCache.java`, `EngineCore.java`, lifecycle tests and minimal dependency stubs; the real production close/cache paths and test sources were used, but **this is not a complete core or NeoForge build**.

- Original test + current production source: **failed as expected** at line 23 with `ExecutionException: IllegalStateException: cache closed`.
- Corrected test + current production source: **passed 250 independent JVM replays**, plus **50 repeatable replays** in the evidence runner.
- Existing `CacheLifecycleChecks`: **14 checks passed**.
- Dev204 `CacheCloseRaceChecks`: **5,001 assertions passed**, 1,000 deterministic blocked-loader close trials.
- Negative control: corrected test + exact historical cache blob `0ed8a911b0baabc6c35f204761e2e6b4c9e2a826` **failed as expected** with `closed world returned obsolete cache result`. Thus the corrected test detects the old unsafe behavior.
- All test variants passed strict Java21 compilation and bytecode verification where applicable.

The accompanying independently verified local evidence ZIP contains exact source, baseline and mutant, stubs clearly marked as such, reproduction script, logs and SHA256 manifest. ZIP SHA256: `4a178f2815ce1fe330c66481b385338dfe34fb2e7185b0e5646bc21811a12491`.

## Compatibility and acceptance boundaries

- `gradle.properties` still pins `neo_version=26.1.2.122`; root `build.gradle` requires Java25 and ModDevGradle `2.0.148`. The official NeoForged project index lists `26.3.0.101` as of this audit. No 26.3 or 26.4 port compile was performed.
- Minecraft 26.4 Snapshot 1, published **2026-09-22**, changed the default graphics API preference to Vulkan and removed automatic graphics API fallback after a startup crash. Native renderer fallback testing is still required.
- Local runtime has OpenJDK21, but no complete private checkout, mounted Java25 toolchain, Gradle executable or resolved NeoForge dependencies. Direct `git ls-remote` failed DNS resolution. The GitHub connector provided scoped source access and the validated test-file commit, not a full local checkout.
- **No playable replacement JAR or JAR SHA256** was produced; no older JAR was deleted. **No GitHub Actions** were invoked.
- Native water conservation, persistence/reopen, terrain seams, Vulkan/OpenGL and LOD FPS remain **not runtime-tested** in this audit.

## Grades and next priorities

- Scoped terminal-close lifecycle/test correctness: **8.5/10 provisional**, grounded in exact-source baseline failure, corrected repeated pass, historical negative control and cache test coverage. Not a native runtime grade.
- Whole-engine: **5.0/10 historical baseline**, not freshly regraded.
- Next: restore full Java25 Gradle checkout and dependencies, run the complete registered `coreChecks` and `jar`, verify playable JAR SHA256, launch client and server, test water/save/terrain and Vulkan/LOD performance. Retain previous JAR until a fresh replacement is verified.

## References

- https://projects.neoforged.net/
- https://www.minecraft.net/fi-fi/article/minecraft-26-4-snapshot-1
