# Dev187 independent Deep Primal audit — 2026-11-02 07:30 UTC

## Source window and scope
- Prior independent audit baseline: `493a970bb7ce7ba7ee6cbfc9542e2b4d98c86710` (Dev185).
- Verified GitHub `main` at `d6ac4b275b9170d42650199ba24e2133964f103c` before publishing this report.
- `compare_commits` reported **9 commits ahead, 0 behind**, with one production file modified (`engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java`), one CoreChecks registration, one new regression class, and isolated fixture/documentation files.
- Engineering Dev186 change: reject a materializer result whose chunk key (X, Z, dimension) does not equal the requested key before caching. Its writer claim is `completed-released`, so the auditor did not overlap active production changes.

## Reproduction and independent verification
- Retrieved the current production `WorldGenPipeline.java` and committed `WorldGenPipelineKeyChecks.java` via authenticated GitHub. Reconstructed local files have **exact Git blob IDs** `2e1a5cb55007d10745260dd355fbf180d5f2920b` and `6df4e82840e17463c4c2918214baf4ba4bbe4d84`. Fixture Git blob IDs also match.
- Compiled exact production and committed regression source with Java21: `javac --release 21 -Xlint:all -Werror`, executed with `java -ea -Xverify:all`.
- Committed Dev186 regression: **110,000 assertions passed** across 10,000 seeded trials.
- Auditor-authored independent regression: **80,019 assertions passed** across 10,000 additional seeded trials, covering client-copy cancellation isolation, coalescing, cache-hit parity, 64-query admission cap, 4096-key batch behavior, shutdown/late-result handling, and synchronous/async/materialization/cache error paths.
- Negative control: removing only the two-line wrong-key guard from the exact production class compiled but failed at the first mismatch with `AssertionError: Wrong-key materialization succeeded` (expected).
- **190,019 total positive isolated assertions passed.** No additional production defect was confirmed; therefore no speculative production correction was made.

## Native acceptance and compatibility
- Repo `gradle.properties` pins `neo_version=26.1.2.122`; root `build.gradle` requests Java25 and ModDevGradle 2.0.148.
- Minecraft 26.3 is released and NeoForge 26.3 builds are available; 26.4 is still in snapshots as of this audit. Neither 26.3 nor 26.4 compatibility is established by these Java21 core tests.
- Native water conservation, river/terrain borders, persistence recovery, Vulkan/OpenGL, GPU LOD and FPS remain untested. No game launch or complete Java25 Gradle build occurred.
- Local runtime has Java21 only, no full private checkout, no resolved Gradle/NeoForge dependencies and no working GitHub DNS for `git clone`. **No playable JAR was produced, so there is no new playable-JAR SHA256.** No old JAR was deleted. No GitHub Actions were used.

## Grading and gates
- Scoped worldgen key-admission correctness: **8.5/10 provisional** (exact production/regression blobs, 190,019 positive assertions, negative control); native integration pending.
- Whole engine: **5.0/10 historical baseline, not regraded**. There is no new evidence to justify a fresh overall score.
- Priority: restore Java25 and a complete checkout; build and SHA256-verify a packaged playable JAR locally, validate client/server startup and save/reopen, then test water conservation, terrain seams and Vulkan/LOD frame performance. Consider a targeted deterministic shutdown/completion race test; no race was confirmed this round.
