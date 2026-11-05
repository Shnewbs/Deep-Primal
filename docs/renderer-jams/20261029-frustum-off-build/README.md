# Renderer jam 33 — fair coarse-frustum-off A/B admission

When `-Ddeepprimal.lod.pageFrustumBroadphase=false`, live LOD fine-part frustum tests are already authoritative. The page-upload path previously still walked and allocated conservative coarse AABBs that were never consumed. This jam skips that build work only for the explicit off mode, retaining the correctly sized null-entry bounds array required by existing `Entry` consumers. Default-on bounding/unions, mesh geometry, draw order, water opacity, Vulkan/OpenGL renderer, LOD selection and world settings are unchanged. A/B measurements no longer include unused coarse-union upload overhead.

Reproduce the **isolated API-double** source test (not full NeoForge validation) with a Java 21+ JDK from repo root:

```sh
WORK="$(mktemp -d)"
cp lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java "$WORK/LodPageFrustumBounds.java"
cp docs/renderer-jams/20261029-frustum-off-build/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumDisabledBuildChecks.java "$WORK/LodPageFrustumDisabledBuildChecks.java"
# Compile these two and both test-only stubs from this jam preserving package paths.
javac --release 21 -Xlint:all -Werror -d "$WORK/classes" "$WORK/LodPageFrustumBounds.java" "$WORK/LodPageFrustumDisabledBuildChecks.java" docs/renderer-jams/20261029-frustum-off-build/stubs/net/minecraft/world/phys/AABB.java docs/renderer-jams/20261029-frustum-off-build/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java
java -ea -Xverify:all -cp "$WORK/classes" com.shnewbs.deepprimal.lod.client.LodPageFrustumDisabledBuildChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp "$WORK/classes" com.shnewbs.deepprimal.lod.client.LodPageFrustumDisabledBuildChecks
```

Note: this checks source-level behavior under API doubles only. Owner-run full Java25 Gradle, native client GPU water-depth, resource lifecycle and p50/p95/p99 remain outstanding.
