# Deep Primal independent Dev133 audit — 2026-10-31 17:30 UTC

## Source identity and change review
- Audit baseline `9ea2591a0f23b328ff129d6ff941b2d7714f6135`; pinned tested source `23d79395e6b06e6e8ae36c51c01f0ada5e28294c`.
- One new commit, four changed files: `ByteLru.java`, `ByteLruOverflowChecks.java`, and two Builder validation/claim documents.
- Production `ByteLru.java` Git blob `b9d1abe36a571cd9e682b0aec78f9bd4950b07d1`; SHA256 `ccfba8e3bed25501b0d8306a89e75b3ea95f9ff3da3779f38ee0e9571d896400`.
- Unchanged ancillary production `WorkBudget.java` blob `3e2f4e8a48f79f5edc33f19f4b379b7b1720cac2`; `FarTerrain.java` blob `dab0e72284152dadf32a7666dd891a3b3fa10030`.
- The Builder repaired a real cache-accounting defect: replacement and eviction now subtract the originally admitted payload weight, not a possibly mutated current weight. The API still returns payloads by reference and preserves access-order LRU behavior.

## Independent local validation
- OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- Independent `BigInteger` / access-order / snapshot-weight model, two reproducible runs: **1,365,198 assertions each**, 142,289 insertion operations each; **2,730,396 passing assertions total**. Covers 7 byte budgets including `Long.MAX_VALUE`, 5 entry ceilings, mutable weights, exact identity, LRU hits, zero-size entries, rejected negative sizes, weigher-once behavior, throwing-weigher atomicity, and clear.
- Separate unchanged WorkBudget/FarTerrain regression: **400,006 assertions passed**, covering 200,000 signed-coordinate key and tile-distance trials plus healthy/neutral/invalid recovery gates.
- **Combined positive assertions: 3,130,402.**
- Two compiled deliberately defective `ByteLru` variants (eviction reweigh and replacement reweigh) both failed. Independent accounting-only controls reproduced incorrect byte accounting, beyond merely detecting extra weigher calls.
- No additional confirmed production defect in this scoped review; no overlapping Builder source changes were made.

## Whole-engine acceptance boundaries
- Only Java21 isolated code compiled. Full Java25 Gradle 9.2.1 / NeoForge, native save/reopen/crash recovery, finite-water conservation, terrain/worldgen, Vulkan/OpenGL, LOD and frame-time benchmarks were **not executed**.
- The repository still pins `neo_version=26.1.2.122` and `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`, despite Dev133 source. Newer NeoForge 26.1.2/26.3 builds exist, but **compatibility is not demonstrated**; do not silently upgrade across Minecraft releases.
- The local environment has no complete repository checkout, Java25 toolchain or cached Gradle/NeoForge dependencies; `git ls-remote` failed with `Could not resolve host: github.com`. Consequently no complete mod JAR was built or SHA256-qualified.
- Existing audit JARs were not removed; no GitHub Actions were invoked.

## Grade and follow-up
- **Scoped cache-accounting correctness: provisional 8.5/10** on isolated Java tests; not an in-game or throughput grade.
- **Whole-engine grade: carried-forward 5.0/10**, not freshly regraded. Target strictly above 8.5/10 remains unproven.
- Next: establish full pinned Java25/Gradle checkout and build, qualify JAR SHA256, validate V8 save/reopen/crash paths, finite-water conservation and Windows Vulkan/LOD performance; repair metadata version drift in a coordinated release step.
- The independent audit did not modify Builder checkout, lease, caches, release artifacts, or engine source.
