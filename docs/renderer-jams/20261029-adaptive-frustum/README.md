# Renderer jam 27 — experimental adaptive page-frustum sampling

Scope: CPU-only LOD visibility selection. No geometry, water, pipeline, Vulkan, OpenGL, resource ownership or FSR/DLSS changes. Default behavior remains the jam-26 fixed coarse broadphase pending native evidence.

## Developer comparisons (restart client between runs)

- Default: page-frustum broadphase enabled; adaptive sampling **disabled**.
- `-Ddeepprimal.lod.pageFrustumAutoSkip=true`: opt-in adaptive mode. After a fully completed extraction samples at least 32 eligible multi-part pages, if page-level tests avoided no more fine AABB tests than the number of coarse AABB tests, it bypasses the next 3 extractions and samples again. It always performs the original ordered per-part culling during bypass; world reset clears history.
- `-Ddeepprimal.lod.pageFrustumAutoSkip=false`: force the unchanged full coarse path.
- `-Ddeepprimal.lod.pageFrustumBroadphase=false`: disable the coarse path entirely, irrespective of adaptive mode.
- `-Ddeepprimal.lod.telemetry=true`: report coarse attempted/visible/rejected, avoided fine tests, sampled-last-extract and adaptive bypassed pages. Counts are from completed extracts; zero GPU occlusion/inference claims.

The heuristic uses counts of AABB tests, **not actual GPU or frame performance**. A camera turn while bypassing remains visually correct because fine tests always run, though the benefit of coarse rejection may be deferred by at most three completed extractions. A failed extract cannot train the heuristic. An opt-in flag is necessary because paired isolated component CPU gains are small and p95/p99 outcomes are mixed.

## Exact source-focused checks

From the repository root, with Java21 or a verified JDK25 and isolated classes:

```sh
ROOT=docs/renderer-jams/20261029-adaptive-frustum
mkdir -p /tmp/deepprimal-renderer-jam27-classes
javac --release 21 -Xlint:all -Werror -d /tmp/deepprimal-renderer-jam27-classes \
  docs/renderer-jams/20261029-page-frustum-broadphase/stubs/net/minecraft/world/phys/AABB.java \
  docs/renderer-jams/20261029-page-frustum-broadphase/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java \
  "$ROOT/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveChecks.java"
for value in false true; do
  java -ea -Xverify:all "-Ddeepprimal.lod.pageFrustumAutoSkip=$value" \
    -cp /tmp/deepprimal-renderer-jam27-classes \
    com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
done
```

Use exactly the same seed, camera, FOV, warm/cold cache, full-detail/vertical/LOD distance and water for native fixed-coarse/adaptive/no-coarse comparisons on both Vulkan and OpenGL. Record CPU/GPU frame p50/p95/p99, draw counts, VRAM, culling ratios and transparent-water pixels. No JAR or qualified GPU acceptance is claimed here.
