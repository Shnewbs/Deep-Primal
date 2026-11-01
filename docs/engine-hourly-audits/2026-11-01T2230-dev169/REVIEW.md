# Dev169 — Independent Deep Primal audit (2026-11-01 22:30 UTC)

**Baseline:** `608c6ee68aef65418f1b323dbee94042bb519223` (Dev167 audit). **Reviewed Builder head:** `540d201714541cf9b0874b0e5ef309bff2ecffce`. **Commits:** 2, five changed paths. No GitHub Actions.

## New-commit and ownership review

[Compare Dev167..Dev168](https://github.com/Shnewbs/Deep-Primal/compare/608c6ee68aef65418f1b323dbee94042bb519223...540d201714541cf9b0874b0e5ef309bff2ecffce) includes a bounded writer claim and a water-surface convex interpolation fix in `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java`, a new `WaterSurfaceConvexAdmissionChecks` regression, `CoreChecks` registration, and validation documentation. The claim file reports `status: completed` and releases the scope. No new renderer, terrain, LOD, performance, persistence or native-water code was changed in this delta.

## Independent reproduction and validation

Fetched exact production `WaterTransport.java` (Git blob `49d5709cd209fd901501fb0f0afd5c098b62d829`), locally reconstructed and verified with `git hash-object`. Used a minimal `TerrainNoise` test double containing the exact production `lerp(a,b,t) = a + (b-a)*t`. Locally reconstructed the committed convex regression and wrote a separate independent seeded oracle.

`javac --release 21 -Xlint:all -Werror` passed; `java -ea -Xverify:all` passed:
- **1,933,122** committed-regression-equivalent assertions (finite convex extreme corners, ordinary bitwise parity, shared-edge continuity, integer-volume conservation).
- **3,000,000** additional independent assertions (500,000 randomized finite-bit corner cases with four invariants each, 200,000 ordinary bit-exact cases, 200,000 pressure-conservation/bounds/admission cases with four invariants each).
- **4,933,122 total** passing Java21 isolated assertions.
- Original `surfaceLerp = TerrainNoise.lerp` mutation **failed** the regression at an extreme finite corner case, returning `0.0` outside the valid hull `[-Double.MAX_VALUE,-1.0]`. This negative control verifies the regression detects the original defect.

**No new nonconflicting production defect was confirmed.** Therefore no production code change was warranted. The test double and excerpt-based harness do not prove full engine/native Minecraft correctness or performance.

## NeoForge and packaging

Repository `gradle.properties`: `neo_version=26.1.2.122`; `build.gradle`: Java25 toolchain, Gradle wrapper 9.2.1, ModDevGradle 2.0.148, JarJar module packaging. Official [NeoForged project listing](https://projects.neoforged.net/neoforged/neoforge/) displayed 26.3.0.99 on 2026-11-01. **No Deep Primal 26.3 or 26.4 compatibility build was run**; a version listing is not proof of compatibility.

Local environment: Java21 only; no Java25, no Gradle executable/resolved dependencies, no complete checkout, and `git ls-remote` fails because `github.com` cannot be resolved. **No playable JAR or JAR SHA256 produced.** Existing audit JARs were not deleted; no GitHub Actions used. Vulkan/OpenGL visuals, LOD, FPS, native water conservation, V8 save/reopen and terrain seam tests remain unaccepted.

## Grades and evidence

- **8.5/10 provisional scoped water-surface convex correctness**: exact production class, independent negative control, strict compilation and 4.93M passing isolated assertions.
- **5.0/10 historical whole-engine baseline**, retained; **not** a new comprehensive grade. Renderer/LOD/terrain/native water/NeoForge ports: ungraded this cycle due to lack of runtime tests.

Local audit evidence: `DeepPrimal_Dev169_IndependentAudit_20261101T2230_Evidence.zip`; SHA256 `f54a7f9574e88412a316dc757e58a7a3cc804df675b3b4076e2334692de7e249`, ZIP CRC verified. Includes exact WaterTransport source, test double, regression-equivalent test, independent oracle, original-defect negative-control failure, strict compilation logs and environment diagnostics. ZIP is kept out of GitHub as requested for binary artifacts.

**Next gates:** obtain Java25 and complete source/dependency checkout; run full `check`, `coreChecks`, JarJar verification, native water persistence, terrain seams and Vulkan/LOD hardware acceptance. Publish/retire a JAR only after verified replacement and SHA256.
