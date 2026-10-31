# Deep Primal independent audit — Dev135 — 2026-10-31 19:28 UTC

## Pin and new changes
- Previous independent audit: `91a8c3605bb144276f93b046c88e6e1f03b64b4a` (Dev134+).
- Pinned reviewed main: `65fb424817bbb8dfd9fa06912cf7428691f00708` (Dev135).
- GitHub compare reports **six new commits**, modifying `ColumnHeight.java` and `CoreChecks.java`, adding `ColumnHeightBoundaryChecks.java` and two validation documents.
- The Builder's completed claim `engine-20261031T1904-column-height-exact` is `completed-released`; no overlapping production edits made.
- Exact fetched Git blobs independently reproduced by `git hash-object`: `ColumnHeight.java` = `852dc341919c9cff8d225d267491dfaac7c44617`, `ColumnHeightBoundaryChecks.java` = `a5084d53b5ca9f90874c267a0ae0f0e52dc5ff7c`, unchanged `WaterAccount.java` = `d8512869d8e06740d57898381145ece344f4661b`.

## Independent verification
- Java `21.0.12.1`: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **PASS**.
- Exact committed Dev135 boundary suite: **504,412** assertions passed.
- Independent exact-double BigDecimal oracle: **280,000** assertions passed (100,000 adversarial signed ranges and 180,000 brute-force realistic-grid cases).
- Unchanged water-account conservation/admission smoke: **600,000** assertions passed across 150,000 randomized transfers and splits; water/salt/sediment totals and capacities retained.
- Total **1,384,412 positive assertions**. Three compiled negative variants rejected: inclusive grid boundary, truncating negative division, and original Dev134 floating division.
- No newly confirmed production defect. No engine source correction made. Evidence ZIP and local validation logs supplied in the audit conversation.

## Related systems and blockers
- `ColumnSurface.compute` delegates to repaired helper; codec restricts `cell_height` to 1..64. Tests also cover direct hostile helper inputs.
- Water-account checks do **not** qualify native finite-water chunk-boundary or save/reopen conservation.
- Six-commit diff contains no renderer, Vulkan, LOD, runtime water, native persistence or other terrain-pipeline modifications; their actual runtime regressions, FPS and throughput are **unverified**.
- `build.gradle` requires JDK25 and NeoForge ModDev `2.0.148`. `gradle.properties` pins NeoForge `26.1.2.122` and still declares Dev127 `mod_version` despite Dev135 source; coordinate artifact versioning with Builder before changing.
- NeoForge 26.3 releases exist upstream, but Deep Primal 26.3/26.4 compatibility is unverified.
- Local Git cannot resolve github.com. Container lacks complete private checkout, JDK25 and cached Gradle/NeoForge dependencies. Full game JAR, native Minecraft, Vulkan/OpenGL and LOD benchmarks **not run**. No JAR or JAR SHA256 produced; no prior audit JAR deleted; no GitHub Actions.

## Grade and next gates
- Provisional scoped column-height numerical correctness/mutation coverage: **8.5/10** (Java21 isolated).
- Whole-engine **5.0/10** carried forward, not freshly regraded. Target strictly >8.5/10 remains unproven.
- Next: pinned JDK25 full local `clean check jar` and SHA256; V8 durable chunk publication/crash recovery; finite-water save/reopen; native Vulkan/OpenGL and LOD performance; coordinate version metadata.
