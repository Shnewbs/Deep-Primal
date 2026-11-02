# Dev193 independent audit — 2026-11-02T1030Z

## Baseline
Previous audit head: `970466f6e0f4752a213fc7f274fbc4f9694fdc6e` (Dev191). Builder head before this report: `2b3e9a11358fb0e3e6130a7d7a8ff456dbb0d853`. GitHub compare reports **3 commits ahead, 0 behind, 5 changed paths**: a bounded Dev192 claim, `FluidVolume` admission fix plus regression, and claim release. No new renderer, terrain, LOD, Vulkan, performance, or persistence source in this delta. Writer lease is released.

## Java25 recovery
Recovered the user's previously uploaded `OpenJDK25U-jdk_x64_linux_hotspot_25.0.5_8.tar.gz` from Library and locally extracted Temurin **25.0.5+8**. JDK tar SHA256: `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`. This fixes the missing Java25 toolchain **in this runtime**, but does not resolve the absent complete private checkout or Gradle/NeoForge dependency graph. The uploaded JDK remains in Library for future re-materialization.

## Independent Java25 verification
Compiled exact production-source test groups with `javac --release 25 -Xlint:all -Werror`, then executed with `java -ea -Xverify:all`. **1,592,651 assertions passed**:
- Native eight-unit `FluidVolumeAdmissionChecks`: **376,053**.
- `WaterSimulationReentrantSamplingChecks`: **78,228**.
- `WaterSimulationBulkRestoreChecks`: **616,510**.
- `WaterSimulationOrdinaryParity`: **1,500**, digest `cd5909e4d0e2816e`.
- `HorizonMeshBoundsChecks`: **370,359**.
- `SoilProfileLegacyReplayChecks`: **150,001**.
- Separate horizon mesh parity: **360 builds**, geometry digest `ca92de2ecc605b23f5c562088514f8e4c5f0f21c75ee1680f014f276e2f797b2`.

Locally tested source Git blobs matched GitHub `main`: `FluidVolume.java` `5236c69d08ecee33c1d88fbc466867f59c14b591`; `FluidVolumeAdmissionChecks.java` `61eb0f2760db3109b9ec90e5285526a052c858a4`; `WaterSimulation.java` `57e72048fabc544e709f360d598db3d18fb6e204`; `WaterTransport.java` `cfc0993fe503fbbe02356b42fbe0f1a0afc0fe23`; `HorizonMesh.java` `5e7c373a3b545ae9b426e38abe10a2cbe7005571`; `HorizonGeometry.java` `c63dfc6058fc2147fa6aedd016406d7ac3575881`; `FarTerrain.java` `fd1a247028c9a4fda99422515438027b809ea2eb`; `SoilProfile.java` `81e400c94b51ca41479e69a466b8a8ee66847f7b`; legacy replay test `21bea98edcc57656c357d37856f85712f35ea410`.

The historical `FluidVolume` blob `65ad5c85173976553caf3029826b90edc3fc0fda` compiled under Java25 but failed the new test at malformed `(-1,0)`, as expected. Corrected current source passed. Reviewed `FiniteWater` native call path and build configuration by source inspection only. **No additional production defect was confirmed; no speculative production edit.**

## Compatibility and acceptance gaps
Repo remains pinned to `neo_version=26.1.2.122`, Java25 and ModDevGradle `2.0.148`. The official NeoForged project index lists 26.3 (`26.3.0.101` on 2026-11-02); Mojang's 2026-09-22 26.4 Snapshot 1 changes default graphics preference to Vulkan. These are **porting risks**, not demonstrated failures.

Local GitHub DNS still prevents a complete clone; no cached Gradle/NeoForge dependencies or complete checkout. Therefore **no full Gradle check, playable mod JAR, JAR SHA256, native save/reopen, Vulkan/OpenGL benchmark, or 26.3/26.4 compatibility result**. Previous JARs remain untouched. No GitHub Actions used.

## Grades and next gates
Scoped Java25 water/LOD/soil isolated correctness: **8.5/10 provisional**. Historical whole-engine grade: **5.0/10**, not freshly regraded. Restore complete checkout and dependency resolution; run full Java25 Gradle `check`, build and SHA256-verify a playable JAR, run client/server startup, then water conservation, saved-world recovery, terrain seams and Vulkan/LOD frame-time tests.

Evidence bundle produced locally: `DeepPrimal_Dev193_IndependentAudit_20261102T1030_Evidence.zip` (not committed to GitHub; source/report only). Evidence SHA256: `7f34eb2e75cd15f5329fe3d7d2e01c9d69d6fcb1e247abb7894b007bab798710`.

[Dev191..Dev192 comparison](https://github.com/Shnewbs/Deep-Primal/compare/970466f6e0f4752a213fc7f274fbc4f9694fdc6e...2b3e9a11358fb0e3e6130a7d7a8ff456dbb0d853) · [NeoForged project index](https://projects.neoforged.net/) · [NeoForge 26.1 migration](https://neoforged.net/news/26.1release/) · [Mojang 26.4 Snapshot 1](https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1)
