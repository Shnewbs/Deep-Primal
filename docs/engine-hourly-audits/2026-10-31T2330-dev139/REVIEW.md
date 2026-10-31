# Dev139 independent audit — 2026-10-31 23:30 UTC

## New commits reviewed
Baseline: `b2e2ab64670af69b37c50a8389b775a9c497b30e`.
Three Builder commits: `e153f0f3` (claim), `3a1e7049` (solid-cell query-water guard), `26fd69f9` (claim release). Native water, renderer and LOD were not changed by these commits.

## Confirmed and corrected defect
Exact production `WaterSimulation.java` Git blob `f0f4d1dfbd14947bc36f366793d3202989c33f1e` reproduced a dry-water material mismatch:
- After explicit removal: `material=1 level=0.0`.
- After pressure transfer: `material=1 level=0.0`.
- Query packet serialized the drained cell with material `1`.

The setter uses material `0` for dry, nonsolid cells, but `removeWater` and `exchange` did not normalize material when the integer units reached zero. Fixed both paths, preserving source metadata and the solid guard.

Commits:
- Production correction: `e56ea40a28db2be76840e01e3e71193d9dbac1aa`
- New regression: `668e2ecfa7ee8d99ad430639cce29ba4f4a43165`
- CoreChecks registration: `a79e8318a2d1bb42723019e17a3577edb7000b30`

## Independent validation
Exact production `WaterSimulation.java` and exact repository test sources were compiled using Java21 with minimal dependency API stubs, `javac --release 21 -Xlint:all -Werror`; run with `java -ea -Xverify:all`.
- Existing `WaterSimulationMaterialChecks`: **693,494** assertions, **300** seeded trials passed.
- New `WaterSimulationDryMaterialChecks`: **516,444** assertions, **120** seeded trials passed.
- Three negative controls failed as expected: original source, missing removal fix, missing exchange fix.
- Fixed production Git blob `6882c5d52d68b787f1dad954e4f1fdededb3b305`, SHA256 `9371e14e24d60c7d705e8cd05fead4395b2eb16b2613ebe21656bbe3fff62636`.
- New test Git blob `add66e4b4d0f4bb3d849bb9da70bf3bf95af65ef`.

## Acceptance and grade
Scoped query-water consistency: **provisional 8.5/10** (isolated correctness only). Whole-engine historical baseline: **5.0/10**, not freshly regraded.
Full Java25 Gradle/NeoForge packaging, native water persistence and chemistry, terrain seams, Vulkan/OpenGL, LOD and FPS benchmarks were **not run**. Environment has Java21, no Gradle command, no complete private checkout, and Git CLI cannot resolve github.com. GitHub connector writes succeeded. Repository remains pinned to NeoForge `26.1.2.122`; 26.3/26.4 compatibility unverified. Mod version metadata still says Dev127.
**No new playable JAR or JAR SHA256 was produced. No previous JAR deleted. No GitHub Actions used.**

Next: restore full JDK25 private build environment, validate actual Minecraft runtime and issue verified JAR before replacing any older audit artifact.
