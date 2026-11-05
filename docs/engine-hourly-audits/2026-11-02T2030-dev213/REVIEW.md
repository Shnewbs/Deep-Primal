# Dev213 independent audit — 2026-11-02 20:30 UTC

## Commit review
Compared main `18d8388ee24970dd53581295b7656198388f7d66` against Dev211 `b9b7f8a089e54f78bc729d3b16fce83287dd9138`: seven commits ahead, zero behind, five files changed. Reviewed the flow-atlas endpoint correction, registered regression, signed-coordinate extremes, writer lease and validation notes. No engine, terrain, LOD, native renderer or performance source modifications in this delta.

## Reproduction and verification
- Production change: `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java`, commit `69ecfc76c953e79a97b2bdd4e7138e6dbd385d6b`.
- Registered regression: `src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowPhaseEndpointChecks.java`, registered by `7b5c9ceb9e32f2410dbfc232ebf8b0abd35108c8`.
- Java 21 isolated fixture with manually transcribed exact production offset methods; strict `javac -Xlint:all -Werror` and `java -Xverify:all` passed. This is **not** compilation of the entire production class.
- **675,603 assertions passed**, including **9,821 rounded endpoint witnesses** and ordinary phase bitwise parity.
- Negative control removing the endpoint clamp failed at assertion 3 as expected.
- No new confirmed defect; no speculative production edit.

## Build and compatibility limits
- `gradle.properties` pins NeoForge `26.1.2.122`. Build tasks require Java 25.
- Runtime has Java 21 only. Full private checkout, resolved Gradle/NeoForge dependencies and native client/server runtime are unavailable; direct git access fails DNS resolution.
- **No verified playable JAR or JAR SHA256 produced.** Existing JARs preserved. No GitHub Actions.
- Minecraft 26.4 Snapshot 1 (2026-09-22) switched the Default graphics preference to Vulkan; Deep Primal 26.3/26.4 compatibility remains untested.

## Provisional grades
- Scoped flow-atlas offset correctness: **8.5/10** (isolated test and mutation control pass, GPU visual validation outstanding).
- Whole-engine: **not regraded**; historical 5.0/10 is not a current measurement.

## Next gates
Restore full checkout/Java 25/dependencies; run local Gradle `clean check jar`, verify JAR SHA256, then perform client/server startup, water save/reopen and conservation, waterfall seams, terrain continuity, Vulkan/OpenGL/LOD performance acceptance.

Independent replay evidence available as Dev213 local audit ZIP; not a playable JAR.
