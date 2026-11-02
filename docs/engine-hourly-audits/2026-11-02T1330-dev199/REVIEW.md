# Dev199 — Independent water/engine audit (2026-11-02 13:30 UTC)

**Baseline:** `3d0d0f369f874ddceb29ddc8212ce63d2d1af080`. **Reviewed:** four commits since Dev197, including water mesh shaping, standalone renderer parity, and worldgen reentrant-retry hardening.

## Confirmed defect: wrapped waterfall-side texture UV

In both `SmoothWaterRenderer.vertex` implementations, side faces used `WaterMeshShape.flowingV(x+dx,y+dy,z+dz)` at each vertex. At x=z=0 the historical function produces **V=0.0 at y=0** and **V=0.75 at y=0.25**. Linear interpolation therefore moves the atlas coordinate in the wrong direction by +0.75 across a quarter-block strip rather than the expected -0.25. This is a deterministic UV geometry defect; **no native GPU screenshot was captured**.

Correction: use `WaterMeshShape.sideV(dy)=1-dy` for side faces, preserving per-block atlas coordinates in [0,1] and monotonically descending UVs. Top faces and the original periodic `flowingV` utility remain unchanged. Both mod and standalone renderers were changed identically. The `WaterMeshShapeChecks` regression now covers all 16 water-height values across four side subdivisions.

## Independent local validation

- Exact original `WaterMeshShape.java` blob: `25cd78b8338fa27e97a4bbdca352fc175699d2db`, matched using local `git hash-object`.
- Java 21 `javac --release 21 -Xlint:all -Werror`: passed.
- Independent `WaterSideUvAudit`: **200,064 checks passed**.
- Extended `WaterMeshShapeChecks`: **274,625 existing periodic samples plus 64 new side-UV cases passed**.
- Historical modulo implementation: **negative control failed as expected** (bottom V=0.0, quarter V=0.75).
- Evidence archive: `DeepPrimal_Dev199_IndependentAudit_20261102T1330_Evidence.zip`, SHA256 `58005ac7ac74ce95a9e1063fe0febeee103c02e275747b72a2fac17989537b94`.

## Acceptance boundaries

The worldgen retry changes were source-reviewed but not independently run under the full engine. No full private checkout or Gradle/NeoForge dependency cache is mounted; direct git clone failed DNS. Java 25 is not mounted here. Consequently **no playable JAR, JAR SHA, full NeoForge compatibility result, water persistence test, terrain seam test, Vulkan/OpenGL test, LOD FPS measurement, or native renderer acceptance** can be claimed. No GitHub Actions were invoked. No superseded audit JAR was deleted.

**Provisional scoped waterfall UV grade: 8.0/10** (source-level regression proven, native rendering unverified). **Whole-engine grade: historical 5.0/10, not freshly regraded.** Next: Java 25 full Gradle build, `coreChecks`, client/server startup, visual waterfall acceptance, saved-world replay, terrain and LOD/Vulkan performance benchmarks.
