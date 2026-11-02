# Dev195 independent audit — 2026-11-02T1130Z

## Baseline and scope
Previous audit: `344eb06bb91f272e99357e348b4fd28aa32ee6ea` (Dev193). Latest engineering head before this report: `a053c86269988550b730c6f466b848178efee955`. GitHub compare returned **3 commits ahead, 0 behind**: Dev194 writer claim `8511714`, water boundary-index optimization and test `9449820`, and claim release `a053c86`. Five paths changed (lease, WaterBoundaryIndex, regression, CoreChecks registration, validation notes). The engineering lease is marked completed/released. No other engine, Vulkan, LOD, terrain, save, or water-quantity source changed in this delta.

## Independent source and Java25 verification
Recovered the user's uploaded Temurin JDK25 archive from Library; SHA256 `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`; extracted and ran **Temurin 25.0.5+8**. Independently fetched and reconstructed byte-exact GitHub production and regression sources:
- `WaterBoundaryIndex.java`: Git blob `85c74867e1b25ea6acb148c3ef0dfe43633a38c4`, SHA256 `45f7e34711041c04ed5167117079f15daebc4e6b30f78db3e9fe111c58f34858`.
- `WaterBoundaryIndexFastPathChecks.java`: Git blob `083f94a27d366f3ffd661fd88a2369a34ee100ea`, SHA256 `5ed9864ee236df7211a8fe08153ffafef5b7db12c6d6e08bd3f3a0f6f424ecb8`.

Both `javac --release 21 -Xlint:all -Werror` and `javac --release 25 -Xlint:all -Werror` compiled production, engineering and auditor test sources. Both `java -ea -Xverify:all` executions passed **1,530,073 assertions per runtime**:
- Existing engineering exact nine-neighbor differential: **1,098,120**.
- New auditor-owned geometric oracle: **431,953**. It covered 2,500 seeded randomized input batches, all 256 cell positions, duplicates/high bits, retained storage bounds, stable neighbor order, immutability after caller-array mutation, invalid-neighbor rejection, and four concurrent readers across 400 rounds.
- Sensitivity check: a deliberately corrupted E-edge predicate (`x == 15` -> `x == 14`) was rejected at assertion 487 under both JDKs. This is an injected negative control, **not** a production bug.

No additional defect was confirmed in the optimized boundary index; therefore no speculative production source edit. The independent test fixture and logs are in the local evidence ZIP, not registered in production CoreChecks because existing differential/oracle suites already cover the same behavior.

## Build and compatibility gates
Repository configuration inspected: `neo_version=26.1.2.122`, Java toolchain **25**, ModDevGradle **2.0.148**, Gradle wrapper target **9.2.1**. Official 26.4 Snapshot 1 (2026-09-22) makes Vulkan the default graphics preference; 26.3/26.4 compatibility has not been built or runtime tested. No new Vulkan, LOD, terrain, or save-format code in this three-commit delta. No native FPS/VRAM benchmark, fluid rendering in Minecraft, water conservation in a running world, terrain seams, or saved-world recovery test was possible.

Full private Git checkout and resolved Gradle/NeoForge dependencies remain absent in the local runtime, and `git ls-remote` fails DNS resolution for github.com. Restoring Java25 **does not** make a full playable JAR possible. No full Gradle `check`, client/server startup, replacement JAR or JAR SHA256. No superseded JAR deleted; no GitHub Actions invoked.

## Grades and follow-up
**Scoped water boundary-index correctness: 8.5/10 provisional** (exact source, strict dual-JDK tests, independent oracle, negative control; native integration missing). **Historical whole-engine baseline: 5.0/10, not freshly regraded**. Next acceptance gate: obtain complete repository and dependencies, run Java25 Gradle `check` and playable JAR packaging, verify SHA256, then client/server startup, native water rendering and conservation, saved-world replay, terrain seam tests and Vulkan/LOD frame-time comparisons. Only delete a superseded audit JAR after verifying its replacement.

Local evidence: `DeepPrimal_Dev195_IndependentAudit_20261102T1130_Evidence.zip`, ZIP SHA256 `de7a40c28c432d688889123bda373cc1c9d1bdb35c9805c1409a841408afeb8d`, integrity checked. Evidence is not committed as a binary.

[Dev193..Dev194 compare](https://github.com/Shnewbs/Deep-Primal/compare/344eb06bb91f272e99357e348b4fd28aa32ee6ea...a053c86269988550b730c6f466b848178efee955) · [Mojang 26.4 Snapshot 1](https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1) · [NeoForged](https://neoforged.net/)
