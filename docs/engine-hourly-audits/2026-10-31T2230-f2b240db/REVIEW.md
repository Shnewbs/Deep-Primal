# Dev138 independent audit — 2026-10-31 22:30 UTC

## Source and scope
- Baseline: `34fdcdacab2834e8d4297b799106e54faa09a7ec` (Dev137 independent audit).
- Audited Builder head: `f2b240db67d02b075fe2f8dd9768e4370649dbea`.
- Three commits reviewed: `6289ad3262f0f7aeffc14e2d2d0d7fd7733fab92` (claim), `c905bace2dd541ee6d110baf57d19fbcc31ada2c` (production fix/tests), `f2b240db67d02b075fe2f8dd9768e4370649dbea` (claim release).
- Changed files: `EngineHealthCheck.java`, `EngineHealthCheckIsolationChecks.java`, `CoreChecks.java`, and two claim documents.
- Exact Git blob identities verified locally for the tested production class `82aa8fe195cab852b051b2809768dce94129e0e0` and repository regression `f97e7e81563f6833b6dfdea3d9f03c62c8c30637`.

## Findings
1. **Builder's confirmed defect fixed, independently verified.** Prior `EngineHealthCheck.runAll()` allowed a supplier's `AssertionError` to abort the remaining registered checks. The new `catch(AssertionError)` records a failed `CheckResult` with its original cause and continues. `LinkageError` and `InternalError` still propagate.
2. **No additional production defect confirmed in the changed code.** Independent tests also checked snapshot semantics when a supplier registers another check during iteration, concurrent registration, disabled checks, immutable failure lists, `runOrThrow`, null returns, explicit failures, and exception diagnostic preservation.
3. **NaN water sentinel is intentional.** `TerrainEngine.Sample.wet()` is `Double.isFinite(water)`; dry columns use `Double.NaN`. Consequently, `CoreValidation.validateSeed()` correctly permits NaN water while rejecting infinite water. Do **not** mechanically replace `Double.isInfinite(s.water())` with `!Double.isFinite(s.water())`.
4. **Version metadata lag remains.** `gradle.properties` declares `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1` and `neo_version=26.1.2.122`. Do not infer 26.3/26.4 compatibility from upstream artifact availability.

## Independent validation (local Java 21, isolated)
- Exact committed production and regression source identities matched Git blob hashes.
- `javac --release 21 -Xlint:all -Werror` passed with minimal `WaterTransport` and `ChunkCache` API stubs.
- `java -ea -Xverify:all` repository regression: **179,820 assertions passed** across **50,000** randomized trials.
- Separate auditor-owned harness: **432,840 assertions passed** across **120,000** seeded randomized trials and **250** concurrent registrations.
- Mutation control 1 (remove the new `AssertionError` handler): rejected with uncaught `AssertionError: assertion-test`.
- Mutation control 2 (broaden handler to `Error`): rejected because `LinkageError` was swallowed.
- Scope limitation: these are **isolated Java 21 checks**, not Java 25, NeoForge integration, a native water simulation, or in-game FPS/LOD benchmarks.

## Grade and gates
| Area | Evidence-backed status |
|---|---|
| Invariant-check isolation | **8.5/10 provisional scoped correctness**; independent and negative-control tests pass |
| Terrain / water | Source-level NaN sentinel inspected; no new native terrain, water, conservation or save/reopen tests |
| Renderer / Vulkan / OpenGL / LOD | Not exercised in this audit |
| Performance | No native FPS, chunk throughput, GPU or allocation measurements |
| NeoForge 26.1.2 | Repository pinned to `26.1.2.122`; full build unverified |
| NeoForge 26.3/26.4 | Compatibility unverified |
| Whole engine | Historical **5.0/10** baseline carried forward, **not freshly regraded** |
| Playable mod JAR | **Not produced**; no JAR SHA-256 to report |

## Packaging and preservation
- Local runtime: OpenJDK `21.0.12.1`; no JDK 25, no complete private repository checkout, no Gradle wrapper or cached NeoForge dependencies. `git ls-remote` failed DNS resolution for `github.com`.
- A complete Java 25 Gradle/NeoForge build cannot be honestly certified here. No mod JAR was created, no older audit JAR was deleted, and no GitHub Actions were invoked.
- Local evidence bundle contains the exact source snapshots, auditor-owned test, mutation sources, logs, and checksums.
- NeoForged's [26.1 porting notes](https://neoforged.net/news/26.1release/) require Java 25 and Gradle 9.1+ for Minecraft 26.1.

## Next gates
1. Restore a full authenticated local checkout plus JDK 25 and Gradle/NeoForge cache; compile, launch, verify, SHA-256 the resulting JAR, then replace older verified audit JARs.
2. Run V8 saved-world crash/reopen persistence and finite-water conservation tests.
3. Capture reproducible Vulkan/OpenGL/LOD throughput, frame-time, mesh rebuild, and memory benchmarks.
