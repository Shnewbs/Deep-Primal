# Deep Primal Dev163 — independent hourly audit

**Date:** 2026-11-01 19:30 UTC (11:30 PST). **Prior audit main:** `f253aa5d076be71275fbfa3c6fb1a02efaa091c4`. **Inspected main:** `184f0f3ca03cddf51052d7ff866fe178f956070a`.

## Commit and scope review

GitHub compare reports **6 new commits, 0 behind**. Dev162 modified `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterDynamics.java`, added `WaterDynamicsHydrologyFiniteChecks.java`, registered it in `CoreChecks.java`, and added its validation report and writer lease. The writer lease in `docs/validation/engine-continuation/water-hydrology-finite-2026-11-01/CLAIM.json` is **completed-released**, with no conflict identified in these paths. Organics, native renderer, terrain runtime, and LOD source files were not changed in this commit window.

Production change: reject nonfinite speed and ocean relief inputs, promote signed-int floor arithmetic to double, preserve ordinary finite calculations. Reviewed the production diff and registered test. No further independently confirmed production defect; no production code was changed.

## Independent reproducible verification

- Reconstructed exact production `WaterDynamics.java` from previously archived Dev151 source plus Dev162 GitHub diff; local `git hash-object` **matched GitHub blob `35af84115a215bd2d6993ce1102fc42fbd39ca5a`**. SHA256 `2379407647d8143d425de6db86e9a13429bca96c87ceea6c2538d61220d4a7d9`.
- Strict OpenJDK21 `javac --release 21 -Xlint:all -Werror` compiled exact production class and two independent tests; `java -ea -Xverify:all` passed.
- **1,100,521 independent hydrology assertions passed:** 500,000 ordinary speed parity, 500,000 ordinary ocean parity, 100,000 adversarial double samples, 500 extreme floor/relief combinations, 21 invalid-input checks.
- **1,170,764 independent presentation assertions passed**, re-running archived Dev151 presentation suite against Dev162 production source.
- **2,271,285 total isolated assertions passed.**
- **3/3 defective mutations failed as expected:** removed speed guard (assertion 1), removed ocean guard (assertion 4), restored signed-int floor overflow (assertion 397).
- Local evidence ZIP: `DeepPrimal_Dev163_IndependentAudit_20261101T1930_Evidence.zip`, **21,118 bytes**, SHA256 **`a87eff069e0cf8e2037aa114f1911bce7341ded6db4ce1220e194ad6c13003d4`**; ZIP integrity verified. Contains exact production source, independent tests, passing logs, three mutation failures, source SHA manifest, environment diagnostics.

## Compatibility, renderer, terrain, LOD and build gates

- Root `gradle.properties` still pins **NeoForge 26.1.2.122**; root build and module builds require **Java25**; wrapper uses **Gradle 9.2.1**.
- Vulkan adapter README documents optional platform-native bundling and Vulkan validation; LOD module is present and targets Java25. Neither was changed by Dev162. **No Vulkan/OpenGL readbacks, LOD memory/throughput, FPS or hardware benchmarks were run.**
- No native finite-water conservation, save/reopen, water-flicker, terrain seams, climate integration, worldgen performance or NeoForge 26.3/26.4 compatibility acceptance was run.
- This isolated container has **Java21 only, no complete private repository checkout, no Gradle distribution/cache or NeoForge dependencies**. `git ls-remote` fails because the container cannot resolve `github.com`; the connected GitHub API was used for code inspection and report publication.
- **Full Java25 NeoForge build is blocked. No fresh playable JAR or JAR SHA256 was produced; no existing audit JAR was deleted. No GitHub Actions were invoked.**

## Grade and follow-up

**Scoped hydrology numerical admission: 8.5/10 provisional** (exact-source compilation, deterministic parity, adversarial coverage, negative controls). **Whole-engine: 5.0/10 historical baseline, not freshly regraded.** The scoped grade is not evidence of native runtime acceptance.

Highest priority: restore complete local checkout, JDK25, Gradle/NeoForge dependencies, build and verify a playable JAR and SHA256, then replace the previous audit JAR only after verifying its replacement. Follow with native water mass/source conservation, saved-world recovery, terrain seams, Vulkan/OpenGL rendering and LOD performance tests.
