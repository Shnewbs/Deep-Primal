# Deep Primal Dev197 — independent audit (2026-11-02 12:30 UTC)

## GitHub scope

- Compared Dev195 head `7fd00584b74c28f19d2f73852077d203ae678bac` against current engineering head `639eabed541a3c9f06cf37102cf1730c136a58d8`: **2 commits** (`3deb886c` engineering claim; `639eabed` density-bounds optimization and permanent regression).
- Inspected commit diffs and the current `DensityVolumeBounds.java`, `DensityGraph.java`, `DensityVolumeBoundsAllocationChecks.java`, `gradle.properties` and `build.gradle` through the authenticated GitHub connector.
- `DensityVolumeBounds.java` Git blob: `2a121a804d699cfe7abc19b19ef67573bc2165c8`.
- Build still pins `neo_version=26.1.2.122`, Java 25, ModDevGradle 2.0.148.

## Independently executed isolated Java 21 tests

An independent test transcribed only the two changed methods from the current and prior production source and compared **raw IEEE-754 endpoint bits**, not merely occupancy classifications. It also compared the prior and current coordinate admission logic.

- 28,561 exhaustive ordered four-input combinations from 13 special values (signed zero, infinities, NaN, subnormals, finite extremes).
- 2,000,000 seeded random interval-pair comparisons, including exceptional values.
- 200,000 seeded coordinate-box admission comparisons.
- **2,228,561 comparisons passed** with `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`.
- Deliberately corrupting the low-bound calculation produced a negative-control failure at comparison 342.

**Finding:** No regression confirmed in the two changed methods. The optimization removes two temporary arrays, but other allocations (`Box`, `Range`, `new int[1]`) remain. No native GC/FPS gain claimed. The newly registered graph-level `CoreChecks` regression was **not executed** here.

## Acceptance matrix

| Area | Evidence | Status |
| --- | --- | --- |
| V8 density bounds | Exact-bit isolated method parity and box admission | Scoped PASS |
| Water conservation and persistence | No native run this cycle | Not revalidated |
| Terrain borders and saved-world replay | No native run | Not revalidated |
| LOD and culling | No native run | Not revalidated |
| Vulkan/OpenGL, FSR/DLSS, FPS | No native run | Not revalidated |
| NeoForge 26.1.2.122 | Version pin inspected | Runtime unverified |
| NeoForge 26.3/26.4 | No port compiled | Unverified |
| Playable JAR + SHA256 | No build | Blocked |

**Scoped correctness grade: 8.5/10.** Whole-engine score not freshly regraded (historical baseline 5.0/10).

## Packaging blocker and retention

Local environment has Java 21, no mounted Java 25, no full private checkout, Gradle wrapper or resolved NeoForge dependencies. Direct GitHub access failed DNS resolution (`Could not resolve host: github.com`). GitHub connector supports individual source inspection and this report commit but not a full local Gradle checkout. **No playable JAR or JAR SHA256 was produced. No previous audit JAR was deleted. No GitHub Actions were used.**

Next: restore authenticated checkout and Java 25/Gradle dependencies, run `coreChecks`, `check`, `jar` locally, then native water conservation, world replay, terrain seams, LOD and Vulkan/OpenGL acceptance.

Local evidence ZIP: `DeepPrimal_Dev197_IndependentAudit_20261102T1230_Evidence.zip`, SHA256 `9d6112da3bd51ccc430c24a9911570d24bf95b808ad9771a5253d87adcfd231d`; includes test source, positive/negative logs, diagnostics, and integrity manifest.