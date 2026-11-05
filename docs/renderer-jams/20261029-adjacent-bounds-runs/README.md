# Renderer jam 35: adjacent valid fine-part frustum verdict reuse

**Scope:** `lod-engine` only. `LodPartFrustumRuns` discovers whether a committed resident-page detail has any adjacent valid identical AABB pair, once per page upload. On a mixed-bound page, the render thread reuses a prior fine-part frustum verdict only for adjacent bounds with all finite, ordered and equal coordinates. All parts retain their original order; opaque/water blend and GPU draw commands remain untouched. Distinct AABBs and malformed bounds still invoke the prior fine predicate.

This complements, rather than relaxes, the existing full-page conservative coarse rejection and proven-uniform positive path. `-Ddeepprimal.lod.pageFrustumBroadphase=false` still executes the original fine test for every part for comparison. The optional adaptive skip policy trains only from coarse savings, not fine adjacent reuse, and is now trained **after** successful render-state publication. Adjacent reuse is not a GPU-occlusion/indirect-draw feature.

**New opt-in diagnostics** (`-Ddeepprimal.lod.telemetry=true`): `lodAdjacentBoundsReusedVisiblePartTests` and `lodAdjacentBoundsReusedRejectedPartTests`, both per completed extraction. Prior `lodTestedParts` and `lodRejectedParts` count only actual fine-predicate invocations. Counters reset on world teardown.

**Source regression command (isolated Java21, test-only AABB/GPU-mesh doubles):**

```sh
find src stubs tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d out @sources.txt
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodAdjacentRunSmoke
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodPartFrustumRunsChecks
```

The committed `tests/com/shnewbs/deepprimal/lod/client/LodAdjacentRunSmoke.java` is production-excluded; test doubles and extended checks are supplied in local evidence, not the game JAR.

**Open owner GPU gates:** same-seed Vulkan/OpenGL native with mixed/uniform/unique bounds, 3-way fixed/adaptive/off, FSR1 Quality/native resolution, transparent water, travel/edits/reload/resize/world-switch, p50/p95/p99 CPU and GPU frame times, GPU memory and returned/hardware draw counts. Full build and JAR are owner-operated under the October 27 override. No new temporal FSR/DLSS/FSR4 or GPU performance claim.
