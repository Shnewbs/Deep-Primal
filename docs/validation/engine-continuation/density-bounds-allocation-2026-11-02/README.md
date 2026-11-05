# Dev196 — V8 density preflight allocation reduction (2026-11-02)

**Scope:** `DensityVolumeBounds`, one permanent regression, `CoreChecks` registration. The Dev195 independent audit found no defect in the prior water boundary optimization. This work avoids overlapping auditor, organic, native renderer and water-volume files.

## Change
- Remove the six-element temporary coordinate array in `checkBox`, retaining finite/world-limit and inverted-box admission rules.
- Remove the four-element temporary multiplication array in `multiply`. Preserve all four corner products, NaN fail-open to UNKNOWN, signed-zero behavior, outward rounding and SOLID/EMPTY/UNCERTAIN semantics.
- Add a deterministic 100,000-case public occupancy oracle for clamped X*Y and five invalid-coordinate cases, registered in `CoreChecks`.

## Validation and limitations
- An isolated Java 21 strict `javac --release 21 -Xlint:all -Werror` plus `java -ea -Xverify:all` fixture compared the original and optimized multiplication formulas across **1,000,100** deterministic random and boundary interval pairs, including infinities, NaNs and signed zeros: **PASS**. This fixture is not the complete production class.
- The permanent graph-level regression has been committed for the full Java 25 Gradle `CoreChecks` run. **Not claimed executed in this environment.**
- No native allocation/GC benchmark, Vulkan frame timing, complete Gradle build, game startup or playable JAR produced. No performance speedup is claimed without native measurement.
- Private full checkout and Gradle/NeoForge dependency cache remain unavailable through local GitHub DNS. GitHub Actions not used. Preserve previous qualified JAR until replacement is built and SHA256 verified.
