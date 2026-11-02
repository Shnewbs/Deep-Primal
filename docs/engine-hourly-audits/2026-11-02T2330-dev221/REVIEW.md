# Dev221 independent audit — 2026-11-02 23:30 UTC

## Reviewed commits
- Baseline: Dev219 `81679e94c36ca4f76e8534de2437015083508f9b`.
- Two subsequent commits: `51f71a8a396cafec313d1cff1060816ad21f8aaa` (claim) and `4480622b3dbd22a0a880d41f7921f7199c21c519` (zero-gain 3D noise octave elision).
- The change preserves the stored NoiseSpec, and elides unnecessary octave arrays and evaluations only for zero gain. Water, terrain legacy V1–V7, renderer and LOD files were not changed by Dev220.

## Confirmed test-coverage defect repaired
The existing `SeededNoiseZeroGainChecks` passed when the production optimization was reverted to `int count = spec.octaves();`. Output parity alone therefore did not protect the performance improvement. Added a narrow reflection-based allocation assertion: zero gain with eight requested octaves allocates one, nonzero gain allocates eight, and the original saved NoiseSpec still reports eight. No production API was changed.

Correction committed to main: `e3e5c0a22d448dca6be5c01a3473a99afe98127b`.

## Independent local Java 21 validation
Exact production sources and the corrected test matched the fetched GitHub blob SHAs:
- SeededNoise3D `5b732010cb95a1587c48583ec3d401a1a4008181`
- NoiseSpec `67af7fba4b55aad858034fcc9cf2655271c95dc4`
- DensityField `ae21097a27fe21b1992b2c2790245436de97649b`
- corrected SeededNoiseZeroGainChecks `dd39bf78647325d095552caa425787e2fad0f227`

Strict `javac -Xlint:all -Werror` and `java -Xverify:all` passed. Candidate produced `ZERO_GAIN_PASS checks=108006 digest=3213c1b78f7999f95ae9ac4071090e0a3d29cbc3846b3b41ae0924227258d92d`. Historical baseline with historical test also passed, confirming the coverage gap. Historical baseline with corrected test failed at the intended allocation assertion. Reproducible replay script and negative-control logs are preserved in the downloadable audit evidence.

## Release gates and grade
- Root Gradle requires Java 25; `gradle.properties` still pins NeoForge `26.1.2.122`. NeoForge 26.3/26.4 compatibility remains unverified.
- This environment has Java 21 only, no complete private checkout, no resolved Gradle/NeoForge dependencies, and direct GitHub clone fails DNS resolution. Full Gradle, client/server, GPU, water persistence, terrain continuity, LOD/FPS and save/reopen acceptance were not run.
- No playable JAR or JAR SHA256 was produced. Previous JARs were not deleted. No GitHub Actions were used.
- Provisional scoped noise optimization regression coverage: **8.5/10**. Local release readiness: **2.0/10**. Historical whole-engine baseline: **5.0/10**, not freshly regraded.

Next: restore Java 25 and full checkout, run `check`, build a packaged JAR and verify SHA256 plus native startup, water, terrain, renderer and LOD before replacing any prior audit build.
