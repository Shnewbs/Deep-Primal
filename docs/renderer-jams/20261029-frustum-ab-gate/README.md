# Renderer jam 26 — page-frustum broadphase A/B and accounted checks

**Scoped renderer-only source increment.** The existing conservative multi-part
LOD page-frustum optimization remains enabled by default. For matched native
Vulkan/OpenGL comparisons, launch the Minecraft client with
`-Ddeepprimal.lod.pageFrustumBroadphase=false` to restore the original fine
part-only frustum path, or omit it (default-on). Only the case-insensitive literal
`false` disables it. **Restart the client** between A/B variants; this is a
JVM-startup developer switch, not a saved world/video preference or a GUI toggle.

With `-Ddeepprimal.lod.telemetry=true`, diagnostics expose:

- `lodPageBroadphaseEnabled`: actual startup selector state
- `lodCoarseTestedPages`: page-level frustum tests on the last completed extraction
- `lodCoarseVisiblePages`: page-level tests that required normal fine tests
- `lodCoarseRejectedPages`: page-level tests rejecting the entire page
- `lodCoarseSkippedPartTests`: fine mesh tests skipped by coarse rejects

For completed telemetry-enabled extracts, tested = visible + rejected.
With the A/B flag off, all four coarse counters are zero; the original ordered
fine-part tests and native-chunk handoff remain in place. Coarse bounds can still
be cached but are not tested. No shader, mesh, material ordering, water simulation,
upscaling, render-distance maximum, GPU submission, or driver capability changed.

## Source-focused verification

The committed production helper and option regression build using the
production-excluded Java API doubles from
`../20261029-page-frustum-broadphase/stubs`. Example from the
`docs/renderer-jams/20261029-frustum-ab-gate` directory:

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  ../20261029-page-frustum-broadphase/stubs/net/minecraft/world/phys/AABB.java \
  ../20261029-page-frustum-broadphase/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java \
  ../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java \
  tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumOptionChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
```

These isolated tests prove bounded source-level visibility parity and the
startup switch, not native pixel correctness, actual GPU frame times or FPS.
Owner-run matching needs the same hardware, world seed, camera travel, resolution,
full-detail/vertical/LOD distances, warm/cold cache, and enabled water. Collect
CPU/GPU frame-time p50/p95/p99, coarse/fine culling counts, draw/descriptor counts,
VRAM and transparent-water pixels on both backends. An all-visible camera view
is necessary to expose the extra broadphase-test cost.
