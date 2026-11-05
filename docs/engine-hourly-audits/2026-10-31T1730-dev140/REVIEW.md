# Dev140 independent audit — horizon LOD geometry (2026-10-31 17:30 PDT)

## Scope and commits
- Previous audit head: `ef0a4a61ffcc05d841fefc99abfd92fe6539c1fb` (Dev139).
- Builder checkpoint: `1bf072cbcb1d9b082e7ab2e033626d2d6ca42278` (three commits: claim, production fix, claim release).
- Reviewed changes in `HorizonView.java`, `HorizonViewSafetyChecks.java`, `CoreChecks.java`, and the scoped builder validation report.
- The builder's main-writer lease was marked `checkpointed` and `lease_released: true` before independent test commits.
- Independent regression addition: `ea0e50715a93861cee2c79e43894d6d8110e3ad1`.
- Test registration: `a07712248d1b070b0c04f713ec8ffe6a7a2656f7`.

## Evidence and reproducibility
- Production `HorizonView.java` blob SHA verified with local `git hash-object`: `26eba18ff8f38b71ad886a2359fc4b41943a106e`.
- Production `FarTerrain.java` blob SHA verified: `dab0e72284152dadf32a7666dd891a3b3fa10030`.
- Independent Java21 strict compile: `javac --release 21 -Xlint:all -Werror` passed.
- Runtime: `java -ea -Xverify:all` passed, **400,191 assertions**, LOD tiers `[28485, 95104, 276411]`.
- Oracle uses tile rectangle edge distances rather than the production tile-center expression. It covers exact 1024/2560 block thresholds, signed tiles, invalid FOV, invalid workload, minimized viewport and 400,000 deterministic randomized geometry cases.
- Three deliberately broken variants were compiled and **all rejected**: inclusive fine threshold (assertion 29), wrong half-tile width (assertion 195), and wrong tile-center coordinate (assertion 5).
- The new `IndependentHorizonGeometryChecks` is registered in `CoreChecks` for future full builds. Remote blob and registration were verified.
- No new production defect was confirmed. Production implementation left unchanged.

## Compatibility and build acceptance
- `gradle.properties` still pins `neo_version=26.1.2.122` and `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`; the source is now Dev140. Version metadata lags.
- The repository's `build.gradle` requests Java 25. The upstream NeoForge 26.3.x branch also declares Java 25 (https://github.com/neoforged/NeoForge/blob/26.3.x/gradle.properties).
- NeoForge 26.3/26.4 binary compatibility is **not verified**. Loader version availability is not proof of port compatibility.
- Local execution environment has OpenJDK 21.0.12.1, no full private checkout, no JDK25 or cached Gradle/NeoForge dependencies; direct GitHub CLI access fails DNS. No full `gradlew build`, Minecraft startup, Vulkan/OpenGL hardware benchmark, native water/terrain/persistence or whole-engine acceptance was possible.
- **No qualified JAR or JAR SHA-256 produced. No older JAR deleted. No GitHub Actions used.**

## Grades
- Scoped LOD admission/geometry: **8.5/10 provisional**, supported by isolated strict compilation, independent geometry oracle, and negative controls. Not a native FPS grade.
- Whole-engine baseline: **5.0/10 historical**, **not freshly regraded**.
- Priorities: restore full Java25 build, native minimized/restore LOD transition, Vulkan/LOD throughput, V8 crash-safe save/reopen, finite-water conservation and NeoForge version matrix.
