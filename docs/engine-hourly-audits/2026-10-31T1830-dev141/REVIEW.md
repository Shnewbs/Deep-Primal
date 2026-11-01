# Dev141 independent audit — section-slot arithmetic (2026-10-31 18:30 PDT)

## Commit and scope
- Previous independent audit head: `837312fca6fa2e0a31362c12b1888be496c46204` (Dev140).
- Two new commits: `d4c7788ce77d2707cce6e43648038d58f9616781` (scope claim), `b522b775d06e59dd7102dee1a5423068eca8f5b4` (Dev141 production and regression).
- Reviewed `DistancePolicy.sectionSlots`, `DistancePolicySlotChecks`, `CoreChecks` registration, Builder's validation README, Gradle settings, and the released main-writer lease.
- Builder lease status was `checkpointed` and `lease_released: true`. No overlapping production change was made.

## Independent evidence
- Exact production `DistancePolicy.java` SHA-256: `59963b875940bdcf2555dc8d2940e7996c08b92f02b01af07fb37e790a515d3d`; remote Git blob `33f1077360d0c700d34d1f97bc12ab8f1914832c`.
- Isolated strict compilation: `javac --release 21 -Xlint:all -Werror` **passed** on OpenJDK 21.0.12.1.
- Independent `BigInteger` differential oracle: `java -ea -Xverify:all` **passed 1,110,269 assertions**; 350,022 saturated and 200,110 nonsaturated test pairs. Coverage: zero/negative boundaries, int extremes, 350,000 full-range seeded random cases, 200,000 bounded seeded cases, and 10,000 monotonicity checks.
- Four deliberate negative controls were **all rejected**: unchecked signed multiplication, incorrect saturation, missing negative-window rejection, and zero-window incorrectly returning one.
- Independent harness SHA-256: `28a136c9b1c64a36f6e67d0b52d0620618e6ea1ade5e4d1c7847b4e0add20a4b`.
- The Builder's own 400,205 checks were documented in its validation report, but **not independently rerun as a complete repository suite**. The audit's 1,110,269 assertions are separately executed.
- No additional production defect confirmed. Builder fix is correct under tested mathematical contract; no production file edited.

## Build and compatibility
- Current root `build.gradle` requests Java 25; wrapper declares Gradle 9.2.1. `gradle.properties` still pins `neo_version=26.1.2.122` and advertises `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1` despite Dev141 source.
- Official NeoForge 26.1 guidance requires Java 25 and Gradle 9.1+: https://neoforged.net/news/26.1release/ . NeoForge 26.3 source also declares `java_version=25`: https://github.com/neoforged/NeoForge/blob/26.3.x/gradle.properties .
- Local runtime only has Java 21, no Gradle cache, no complete private checkout, and direct `git ls-remote` fails DNS resolution. Full Java25 Gradle/NeoForge build, native water/terrain/V8 persistence, Vulkan/OpenGL/LOD benchmarks and 26.3/26.4 compatibility are **not verified**.
- **No playable JAR or JAR SHA-256 produced. No previous JAR deleted. No GitHub Actions used.**

## Grade and next action
- Scoped section-slot arithmetic: **8.5/10 provisional** (strict compile, independent oracle, four mutation negatives; no native integration).
- Whole-engine grade: **5.0/10 historical**, not freshly regraded.
- Restore full Java25 checkout/build/dependency access; produce and hash a playable JAR, then validate saved-world finite-water conservation, V8 crash recovery, renderer/LOD performance and loader matrix. Only retire a prior audit JAR after replacement is verified.
