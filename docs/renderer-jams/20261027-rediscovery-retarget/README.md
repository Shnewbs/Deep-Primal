# Renderer jam: bounded LOD rediscovery retarget (staged)

Source orientation: main `2ae5e5f406e9a9d91aeb0d689772cbd35396524c` at claim; scoped ownership receipt `c0a3a6eeef2dfb12bda2dbb60fa507b8ecff254b`. This work **does not alter production sources** or distribute a runtime JAR.

## Reproduced source gap

In production `VoxelLodRenderer.tick`, `deliveredScan` is constructed only when null (line 259 of the pinned source), then repeatedly drained. Ordinary native render-distance changes, travel, and completion do not recreate it. As a result already-delivered native chunks newly entering the discovery footprint may remain absent from this scan; the independent chunk-load callback may still provide coverage in some cases. This is a code-path defect, not a measured visible-hole reproduction.

## Implemented staged increment

- `candidate/DeliveredChunkScan.java`: adds `shouldRetarget(x,z,effectiveRadius)` without modifying the old resumable visitor/ring algorithm. A changed effective radius always restarts; after completion any center change restarts; while incomplete restart only after a bounded Chebyshev distance threshold `max(1,min(8,radius/8))` in chunks. Signed extremes use long arithmetic, so center movement cannot overflow to a false near distance. No extra background workers or unbounded collections.
- `candidate/VoxelLodRenderer.patch`: applies the decision in the *existing* client tick, preserving the established maximum-native radius, 128-coordinate/tick admission budget, pause/retry and dirty-queue guards. It changes no mesh, materials, water, opacity, lifetime, saved geography or Vulkan/OpenGL GPU code.
- `checks/DeliveredRetargetChecks.java`: validates radius changes, thresholds, completed-scan travel, pressure retry and signed extremes, plus nine complete unaltered ring scans.
- `run_checks.sh`: compiles the **exact staged core helper** and original repository geometry regression with `javac --release 21`, runs `-Xverify:all`, executes a deliberately broken negative control, and checks that the patch applies to the current checkout.

## Fresh local evidence

2026-10-27 UTC, isolated Linux container with OpenJDK21; no runtime Minecraft or GPU available:

```
DELIVERED_SCAN_PASS coordinates=108999 outerBeyond32=92064 workBound=128 nearestFirst=true unique=true pauseRetry=true negativeCoordinates=true noChunkReferences=true
DELIVERED_RETARGET_PASS checks=1056 completeScans=9 geometryPreserved=true pressureSafe=true signedExtreme=true
NEGATIVE_CONTROL_EXPECTED_REJECTION
RENDERER_RETARGET_PATCH_APPLIES
```

The negative control removes the complete-scan move case; it compiled and correctly failed on `completed move +1:16`. The patch-apply check here used a faithful extracted **259-line positioning fixture**, not a complete project checkout; the included script repeats `git apply --check` on the actual repository. Local hash identities match the staged Git blobs. See `test-results.json`.

## Qualification boundary and coordination

This is **candidate source only**. The automation environment has no accessible complete private working tree, Java25 or NeoForge/Gradle toolchain, so a full root/module build, packaging, release and actual GPU/Windows/backend lifecycle/scene admission tests are not run. Do not promote or ship just because component checks pass. The prior unpromoted `20261027-admission-gate` candidate also touches `VoxelLodRenderer.java`; integrate against a fresh main only after resolving that candidate and rerunning full gates. Active `docs/GRAPHICS-UPSCALING-WORK-STATE.json` ownership of FSR/DLSS/graphics settings remains untouched.

Next: full Java25/Gradle/core+LOD admission checks, mixed edit/camera/native unload/travel coverage with signed coordinates and effective setting changes, GPU Vulkan-first/OpenGL fallback visual handoff and water order, production package/identity/native lifecycle, then qualified direct-main promotion and a uniquely versioned development JAR. Retarget thresholds are conservative heuristics, not evidence of increased full-detail render distance or a proven FPS improvement.
