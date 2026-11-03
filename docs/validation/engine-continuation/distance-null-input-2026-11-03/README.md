# Deep Primal Dev222 — distance-setting null admission (2026-11-03)

## Scope
- `engine-core/src/main/java/com/shnewbs/deepprimal/core/DistancePolicy.java`: return an empty OptionalInt for null input before accessing String methods. Both public overloads now fail closed for absent persisted/UI input; non-null parsing, limits, and native tracker cap are unchanged.
- `src/coreChecks/java/com/shnewbs/deepprimal/core/DistancePolicyNullInputChecks.java`: new deterministic parser regression, registered from CoreChecks.
- No renderer, water, terrain, saved format, simulation or LOD geometry changes.

## Reproducible local evidence
- Exact baseline GitHub production blob: `33f1077360d0c700d34d1f97bc12ab8f1914832c` (`git hash-object` verified).
- Candidate production blob: `e6c21852d47d2e6b3852e7aeeb94bfd4b0dc54b8`.
- OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed.
- Candidate: `DISTANCE_NULL_INPUT_PASS checks=501268 randomInputs=250000 nullInputs=3`.
- Separate 501,265 non-null assertions passed on both baseline and candidate with identical output. The baseline with the full regression fails with a NullPointerException at the first null input; the candidate passes.
- This is isolated exact-production-class validation, **not** a full root CoreChecks/Gradle/NeoForge execution.
- Reproducible evidence ZIP SHA-256: `24bf6e1ece020031db67501730c97689e18e096dcc8a55790226327c882b79da`.

## Release gate
- Full private checkout, Java 25 and resolved NeoForge/Gradle dependencies unavailable locally. Local GitHub clone fails DNS.
- No playable JAR, JAR SHA-256, native Vulkan/OpenGL acceptance or Minecraft save/reopen produced. Preserve the previous successful JAR. No GitHub Actions used.
- Historical whole-engine grade remains 5.0/10, not freshly regraded.
