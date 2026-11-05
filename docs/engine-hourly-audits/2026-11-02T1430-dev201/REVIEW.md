# Dev201 — Independent renderer and frame-budget audit (2026-11-02 14:30 UTC)

**Baseline:** `fa3f55b531f5cd79e4c3082c3779eade2dce1398` (Dev199). **Audited head:** `9625955c76bd53e8ffdb14b94b64ff44af5b836b`. GitHub compare: 2 commits ahead, 0 behind, 4 changed paths. Commits: `fce97d893832d99bc9e7f2ea8a0d97bfee0a7271` (scoped claim) and `9625955c76bd53e8ffdb14b94b64ff44af5b836b` (negative native FPS limit normalization and registered checks).

## Independent findings

The Dev200 correction in `engine-core/src/main/java/com/shnewbs/deepprimal/core/VisualBudget.java` normalizes negative native FPS limits to the zero/unlimited sentinel before combining them with focus/minimized limits. This fixes the original negative propagation while preserving the previous behavior for all nonnegative native limits. The claim is marked `completed-released`, so no competing source edits were made.

**No additional confirmed production defect** was found in the audited change. No speculative fix was committed.

## Independent local reproduction and validation

- Fetched the exact production source from GitHub and independently matched its Git blob `8c1db5ed00a80bc9bf6d089197d23fccbe42be8a` and SHA256 `a2c96eda236a0fd78005602762780d40bfda6b381f3ba0c60812185e09ce5df1`.
- Strict OpenJDK 21 compilation: `javac --release 21 -Xlint:all -Werror` **passed**. `java -Xverify:all` **passed**.
- Independent oracle: **2,113,500 checks passed**, including 13,500 exhaustive edge-value/focus/minimize combinations, 2,000,000 randomized full-int-domain comparisons and 100,000 nonnegative historical-parity checks. The expected-value oracle uses independent `long` arithmetic.
- Historical negative control compiled but **failed as expected**, at native `Integer.MIN_VALUE`: expected 0, actual `Integer.MIN_VALUE`.
- Evidence ZIP: `DeepPrimal_Dev201_IndependentAudit_20261102T1430_Evidence.zip`, SHA256 `d835ebb6775594fceb7d0e5d8ab834ec133cc494ee7977f7b1614bd129ccd89d`. Includes exact source, independent test, historical negative control, compile/replay script, logs and scope report.

## Compatibility and acceptance boundaries

Current `gradle.properties` pins `neo_version=26.1.2.122`; `build.gradle` requests Java 25. Minecraft's [September 22, 2026 Snapshot 1](https://www.minecraft.net/fi-fi/article/minecraft-26-4-snapshot-1) makes Vulkan the default graphics API in 26.4 snapshots. No 26.3 or 26.4 compilation or compatibility acceptance was performed.

No complete private repository checkout or Gradle/NeoForge dependency cache is available in this runtime. `git ls-remote` fails because `github.com` DNS does not resolve. Only Java 21 is locally mounted; the previously uploaded Java 25 distribution is not mounted for this run. **No full Gradle build, playable JAR, JAR SHA256, client/server startup, water persistence, terrain seam, native Vulkan/OpenGL/LOD benchmark, or saved-world replay can be claimed.** Existing audit JARs were not deleted; no GitHub Actions were invoked.

**Provisional scoped frame-limit correctness grade: 8.5/10** (2.1 million independent checks; native focus/minimize transitions untested). **Whole-engine grade: historical 5.0/10, not freshly regraded.**

Next: restore full checkout and Java 25 dependency-resolved Gradle build, verify packaged JAR and SHA256, test actual focus/minimize behavior, then run water conservation/save-reopen, terrain-border, Vulkan/LOD and client/server acceptance gates.
