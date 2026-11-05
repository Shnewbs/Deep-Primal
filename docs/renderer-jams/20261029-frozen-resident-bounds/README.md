# Renderer jam 36: frozen resident-page frustum bounds

Fix: previous page coarse unions and adjacent-part reuse were cached at GPU page publication while the fine frustum path later re-read the publicly writable `TerrainGpuMesh.bounds` reference. Reassigning that reference could hide actual resident geometry. The renderer now snapshots immutable AABB references *once* per fully uploaded page and uses the same snapshot for page union, adjacent mask, uniform identity and fine selection. The frozen bounds live only as long as their resident page; geometry/draw ordering/transparent water/shaders are unchanged.

Only three `lod-engine` Java sources and this production-excluded test directory changed. The previous explicit broadphase-off A/B setting bypasses coarse/adjacent checks while still evaluating frozen uploaded geometry. No other engine, water, graphics-settings, Vulkan, upscaler, settings, jobs or builds touched.

## Isolated Java 21 regression (not a game or GPU test)

Assemble the two committed helper classes, test doubles and committed smoke file in their original package directories, then:

```sh
find src stubs tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d out @sources.txt
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsCommittedSmoke
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsCommittedSmoke
```

The separate verified evidence ZIP includes additional randomized mutation, null/malformed, negative-coordinate/multilevel cases and previous jam 34/35 regressions. The intentionally wrong live-field variant must fail. No native Vulkan/OpenGL water pixels, GPU frame-time p50/p95/p99, VRAM, Java25 Gradle build, game JAR, temporal FSR or NVIDIA DLSS acceptance is claimed. Those remain under the owner's October 27 full-build arrangement.
