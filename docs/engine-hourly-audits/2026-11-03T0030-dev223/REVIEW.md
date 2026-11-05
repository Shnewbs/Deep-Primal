# Dev223 — Independent Deep Primal audit (2026-11-03 00:30 UTC)

## Baseline and scope
- Baseline main: `633061a5bbfc98a84cfd2dcd0e8cf58435373291`.
- Compared against previous independent audit `1fa387b96461d7a873ce0a480c9046f41e449210`: **5 commits**, 7 changed files.
- Two commits introduce and index an organic hunting specification; three are Dev222 distance-parser claim, implementation, test and release bookkeeping. No native water/terrain/LOD/rendering production source changed in this range.
- The writer lease in `docs/ENGINE-MAIN-WRITER-LEASE.json` is `completed-released`; no active writer overlap found in the reviewed lease.
- Root `gradle.properties`: NeoForge `26.1.2.122`. Root `build.gradle`: Java 25 toolchain, ModDevGradle `2.0.148`. Official NeoForged project index listed `26.3.0.101` on 2026-11-03 (https://projects.neoforged.net/); **no cross-version compatibility demonstrated**.

## Reproduction and independent validation
- Exact production `engine-core/src/main/java/com/shnewbs/deepprimal/core/DistancePolicy.java` Git blob `e6c21852d47d2e6b3852e7aeeb94bfd4b0dc54b8`, independently verified using `git hash-object`.
- Exact registered `src/coreChecks/java/com/shnewbs/deepprimal/core/DistancePolicyNullInputChecks.java` blob `ca06406d9616b12bd110078e1577b55bbfb107b2`, likewise verified.
- Strict OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed.
- Registered Dev222 regression: **501,268 assertions passed**.
- Additional independent oracle uses a regex and `BigInteger`, deliberately different from the production digit-accumulation algorithm: **752,428 assertions passed**, including 350,000 seeded randomized parser cases, 50,000 seeded section-slot cases, extreme integers, Unicode digits, null input, caps, clamps, and queue priorities.
- **Total: 1,253,696 assertions passed** in isolated tests.
- Pre-fix production Git blob `33f1077360d0c700d34d1f97bc12ab8f1914832c` independently reproduced the original null-input `NullPointerException`. Candidate passed. The negative control therefore rejected the historical defect.
- No additional production defect was confirmed. No speculative source correction was made.

## Release and engine acceptance
| Area | Evidence | Status |
| --- | --- | --- |
| New commits | GitHub compare 5 commits, 7 files | Reviewed |
| Distance input admission | Exact blobs, 1,253,696 checks, negative control | **PASS isolated** |
| CoreChecks registration | Direct source inspection | Registered; full suite **not run** |
| Engine and water simulation | No native runtime | Unverified |
| Terrain and persistence | No world creation/save-reopen | Unverified |
| Vulkan/OpenGL and LOD | No native client, frame captures or benchmarks | Unverified |
| NeoForge 26.3/26.4 | No compile or launch on either | Unverified |
| Fresh playable JAR | No complete local checkout, Java 25 or resolved Gradle/NeoForge dependencies; GitHub clone DNS failure | **BLOCKED** |

## Grades (evidence limited)
- Scoped distance-parser correctness: **8.5/10 provisional**, based on isolated exact-source tests; not a game-level acceptance score.
- Local release readiness: **2.0/10**; no fresh validated JAR, native startup or saved-world tests.
- Historical whole-engine baseline: **5.0/10**, not freshly regraded; no defensible new whole-engine score.

## Evidence and retention
- Local evidence archive: `DeepPrimal_Dev223_IndependentAudit_20261103T0030_Evidence.zip`
- Archive SHA-256: `a421c4d378e7a0cf21f7e6ebd63ea48ae031b70d9e08809e423238920f194eab`
- Includes exact GitHub production/test sources, independent oracle, historical negative-control source, replay script, logs and SHA256 manifest. ZIP integrity verified.
- No playable JAR or JAR SHA was produced. **Do not delete any previous verified audit JAR** until a replacement has been built and verified.
- No GitHub Actions invoked.

## Next gates
Restore complete private checkout and Java 25 with resolved NeoForge/Gradle dependencies; run root `coreChecks` and `check`, package a fresh JAR, verify manifest/JarJar entries and SHA-256, then run client/server startup, water conservation and save/reopen, terrain continuity, Vulkan/OpenGL fallback, LOD and FPS benchmarks. Only then replace the previous verified audit JAR.
