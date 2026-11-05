# Renderer jam 34 — uniform-page positive frustum reuse

This scoped rendering change avoids redundant fine-part frustum tests when all mesh parts in a resident level have **identical finite AABB coordinates**. `LodPageFrustumBounds.build` returns the verified first part's AABB by identity only for uniform pages; mixed pages retain a separate conservative union, invalid pages retain a null box and singleton/empty pages remain fine-only. After a positive coarse visibility check, `VoxelLodRenderer` appends the uniform page's parts in original order without rechecking the same geometry. Rejected pages, nonuniform parts and the broadphase-off mode retain prior semantics. Adaptive skipping remains opt-in and now counts saved fine tests from uniform positive cases as well as rejected pages. The diagnostic counters distinguish these cases.

No GPU submission, terrain/water geometry, transparency, page order, FSR/DLSS, saved profile or content files were modified. Java-only source-exact tests use previously committed minimal AABB/TerrainGpuMesh doubles at `docs/renderer-jams/20261029-frustum-off-build/stubs`. Under October 27 owner's full-build override, no Gradle, Java25 full module, native GPU or JAR test is claimed.

From repository root, in a scratch directory (do not write to another schedule's build outputs):

```sh
mkdir -p /tmp/dp-renderer-uniform-classes
javac --release 21 -Xlint:all -Werror -d /tmp/dp-renderer-uniform-classes \
  docs/renderer-jams/20261029-frustum-off-build/stubs/net/minecraft/world/phys/AABB.java \
  docs/renderer-jams/20261029-frustum-off-build/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java \
  docs/renderer-jams/20261029-uniform-page-visibility/tests/com/shnewbs/deepprimal/lod/client/LodUniformPageVisibilitySmoke.java
java -ea -Xverify:all -cp /tmp/dp-renderer-uniform-classes com.shnewbs.deepprimal.lod.client.LodUniformPageVisibilitySmoke
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp /tmp/dp-renderer-uniform-classes com.shnewbs.deepprimal.lod.client.LodUniformPageVisibilitySmoke
```

Additional extended randomized equality/negative regression and synthetic timing evidence is retained separately in the jam evidence ZIP. Actual Vulkan/OpenGL transparency, hardware p50/p95/p99, upload/VRAM and FSR-DLSS validation are outstanding.
