# Dev220 — zero-gain 3D density noise octave elision (2026-11-02)

## Scope and proof
- Main baseline at claim: `81679e94c36ca4f76e8534de2437015083508f9b`.
- Production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/density/SeededNoise3D.java`.
- Test: `src/coreChecks/java/com/shnewbs/deepprimal/core/density/SeededNoiseZeroGainChecks.java`, registered from `NoiseCoreChecks`.
- `NoiseSpec.gain() == 0` makes every octave after the first have exactly zero amplitude. Construct only the first octave; retain the original NoiseSpec and algorithm version.
- No legacy V1–V7 terrain noise, saved profiles, water, renderer, LOD or native adapter files changed.

## Local evidence — OpenJDK 21 isolated exact-source compilation
- Baseline production source Git blob: `130e339ae8fd8536fd91118de9e8d3ba848d09af` (verified with `git hash-object` against fetched GitHub blob).
- Strict `javac -Xlint:all -Werror` and `java -Xverify:all` passed for baseline and candidate, using the exact fetched production `SeededNoise3D`, `NoiseSpec`, and `DensityField` sources plus the new test.
- `ZERO_GAIN_PASS checks=108006 digest=3213c1b78f7999f95ae9ac4071090e0a3d29cbc3846b3b41ae0924227258d92d` for both baseline and candidate: 90,000 randomized scalar positions, 6,006 bulk samples, 12,000 nonzero/underflow-gain samples. Bitwise zero-gain parity against one octave and matching baseline/candidate digests.
- The existing default version-one fingerprint remains `6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1` in both variants.
- Isolated warm-JVM median for 200,000 scalar zero-gain samples, five rounds: baseline 56,436,428 ns, candidate 10,467,906 ns (about 5.39x). Not a game benchmark or guaranteed production speedup.

## Boundaries
- The local runtime has Java 21, no full private checkout or resolved NeoForge dependencies; Java 25 Gradle build, packaged JAR, client/server startup, native save/reopen, GPU, water and full engine acceptance were **not** run.
- No GitHub Actions invoked. No qualified playable JAR or JAR SHA-256 produced. Do not delete the previous successful JAR.
- Whole-engine grade remains historical 5.0/10; this change is a narrow optimization, not a release-grade upgrade.
