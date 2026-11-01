# Deep Primal dev152 — waterway terrain finite admission

**Date:** 2026-11-01. **Branch:** `main`. **Scope:** `WaterwayShape.height()` and core regression.

## Correction
The waterway terrain height computation previously propagated NaN or Infinity from nonfinite density/climate samples and could overflow when otherwise finite but extreme continental noise made the coastal shelf nonfinite. `WaterwayHeight` uses this height in the world-generation density function.

The correction returns `seaLevel` for a nonfinite original terrain sample, and the original uncarved height for nonfinite shaping inputs or overflowed shaping results. Finite ordinary geography, coast/river/tributary math, and V1–V7 constants remain unchanged.

## Reproduction and verification
- Java 21 strict isolated compile: `javac --release 21 -Xlint:all -Werror`; run: `java -ea -Xverify:all`.
- **600,020** checks: 300,000 seeded finite-domain bit-exact comparisons against an independent baseline, 300,000 finite-output checks, 18 nonfinite input cases, 2 finite-overflow cases.
- Existing `WaterwayChecks` logic separately passed in an isolated reproduction with 100,000 randomized geography cases plus fixed continuity/river-mouth/width checks.
- Original guard-removed implementation fails the new regression as expected at `bad original fallback` (exit 1).
- Production source blob: `5572aabb7a83779acb40eb121afe34039f144942`; SHA-256: `95a1bc664dbef837aed2c1959f5ec94ae40bb58c16578c8c29fd0f08efd2e201`.
- Test source blob: `b3044f3533f680d21294ed0852e7940188589b17`; SHA-256: `5d92f9bbf45fe80dcd6ddda3f2cb486f7819e7a681270e16a95e5bd0cd091483`.
- The tested files were confirmed byte-identical to GitHub via `git hash-object`; test is registered in `CoreChecks`.

### Reproduce
```sh
mkdir -p /tmp/deepprimal-waterway-checks
javac --release 21 -Xlint:all -Werror -d /tmp/deepprimal-waterway-checks \
  engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterwayShape.java \
  src/coreChecks/java/com/shnewbs/deepprimal/core/WaterwayFiniteAdmissionChecks.java
java -ea -Xverify:all -cp /tmp/deepprimal-waterway-checks \
  com.shnewbs.deepprimal.core.WaterwayFiniteAdmissionChecks
```

## Qualification limits
No complete local private checkout, Java 25, Gradle/NeoForge dependency cache, Minecraft native save/reopen, Vulkan/LOD hardware run or playable JAR was available. This is **source-only**; no JAR or JAR SHA-256 was generated. No GitHub Actions were used and no previous successful JAR was deleted. The historical whole-engine grade **5.0/10** is unchanged pending comprehensive independent revalidation.
