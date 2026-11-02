# Deep Primal — Dev183 independent audit (2026-11-02 05:30 UTC)

## Commit range and coordination
- Previous independent audit: `be25f82584b8b04ce1c93eb7331fbdfab747a57d` (Dev181).
- Reviewed six new engineering commits through `63a011db247155ce76a1118955ab466be2c0794a` (Dev182).
- GitHub compare reports six commits and five changed files: `EngineMetrics.java`, `EngineMetricsTimingChecks.java`, `CoreChecks.java`, and two validation/claim documents.
- No new production renderer, water, terrain or LOD files changed in this interval. The Dev182 engineering claim is released; no overlapping source edit was made.

## Independent correctness verification
- Retrieved production `engine-core/src/main/java/com/shnewbs/deepprimal/core/EngineMetrics.java` at Git blob `bcb2066df3056cf8d5b9f1632a473fd91c08f7b2`. Independently reconstructed local file and verified its exact Git blob hash using `git hash-object`.
- Inspected registered engineering regression `EngineMetricsTimingChecks.java` (blob `5445d13d814a11bfcb8baf1ecba835a5387fa53c`) and `CoreChecks.java` registration (blob `77f48ea56c613a9ae79a740d01d3d7ed83c46b95`).
- Compiled the exact production class and independent audit with OpenJDK 21: `javac --release 21 -Xlint:all -Werror`.
- Ran with `java -ea -Xverify:all`: **1,300,026 assertions passed**.
- Independent oracle: 600,000 seeded randomized duration samples across three channels, including negative, zero and saturating extremes, checked counts and totals after every call.
- Concurrency: 8 threads × 80,000 = **640,000** successful samples, with exact post-join count and nanosecond total parity.
- Cache ratio: **100,000** randomized hit/miss pairs, including near-`Long.MAX_VALUE` values, with independent double-precision reference.
- Snapshot formatting, reset and empty baselines verified.
- Negative-control mutant that admitted a `-1` duration failed at assertion 1 as expected. This mutant is not production code.
- **No additional confirmed nonconflicting production defect** in the changed scope; no corrective production commit was necessary.

## Compatibility and acceptance
- Repository `gradle.properties`: NeoForge `26.1.2.122`; `build.gradle`: Java toolchain **25** and ModDev plugin `2.0.148`.
- Official NeoForge Maven has 26.3 releases; Minecraft 26.4 remains in snapshots. **26.3/26.4 compatibility unverified**, not inferred from isolated Java21 tests.
- Native water conservation, terrain seams, saved-world replay, Vulkan/OpenGL renderer, LOD culling and FPS **not runtime-tested**.
- Scoped engine-metrics correctness: **8.5/10 provisional**. Whole-engine baseline: **5.0/10 historical**, not freshly regraded.
- Local runtime has only Java21, lacks complete private repository checkout and resolved Gradle/NeoForge dependencies. **No fresh playable JAR or JAR SHA256 produced**; no superseded JAR deleted. No GitHub Actions used.

## Reproducible evidence
- Local evidence: `DeepPrimal_Dev183_IndependentAudit_20261102T0530_Evidence.zip` (9,551 bytes, 11 entries, ZIP integrity verified).
- SHA256: `9e56d94cb2ca00b3f225d62f4936e90d8cff87d5776266c7f53cad5f7bb58f99`.
- Includes exact production class, independent test source, negative-control mutant, strict compilation log, passing test output, expected negative failure and environment diagnostics.
- Next gate: complete Java25 checkout and local NeoForge build, verify packaged mod JAR checksum, then native client/server validation before replacing any older audit JAR.
