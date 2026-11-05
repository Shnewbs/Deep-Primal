# Deep Primal renderer jam 30 — bounded normal-angle fast path

**Source/tests:** [ab15e44cdad0a40966b6682f06b486031bb3aa5e](https://github.com/Shnewbs/Deep-Primal/commit/ab15e44cdad0a40966b6682f06b486031bb3aa5e). **Renderer-only ownership:** `renderer-20261029T0957-normal-angle-fastpath`.

This jam consumes the [jam29 self-audit](../20261029-finite-yaw-overflow/REVIEW.md), where extra camera-angle arithmetic was measurably adverse. In the optional adaptive page-frustum pose gate, ordinary finite yaw/pitch inputs bounded to ±360° now compute the shortest turn by subtracting 360 only when needed, avoiding float remainder. The extreme finite-angle double-modulo and nonfinite conservative fallback remain intact. Fine per-part frustum culling, visibility/draw order, meshes, water alpha, GPU paths and default-off policy are unchanged.

**Reproduce source-only suite (Java 21 or newer):**

```sh
find src tests stubs -name '*.java' | sort > sources.list
javac --release 21 -Xlint:all -Werror -d classes @sources.list
for auto in false true; do
  for test in LodPageFrustumNormalAngleChecks LodPageFrustumFiniteYawChecks LodPageFrustumPoseChecks LodPageFrustumAdaptiveChecks LodPageFrustumOptionChecks LodPageFrustumToggleChecks LodPageFrustumBoundsChecks; do
    java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=$auto -cp classes com.shnewbs.deepprimal.lod.client.$test
  done
done
for mode in false FALSE true; do
  java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumAutoSkip=true -Ddeepprimal.lod.pageFrustumBroadphase=$mode -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
done
```

**Results:** 17 runs, 4,218,937 assertions, 10/10 source checks; the deliberately missing-wrap mutation compiled and failed at assertion 50. Matched CPU micro-fixture p50 improved by 68.6–68.8%, but this is **synthetic arithmetic, not Minecraft FPS or native GPU acceptance**. Owner keeps full Java25/Gradle/GPU packaging. No new JAR was created or published.

[Full read-only renderer self-audit](REVIEW.md); [source-only 25-entry ZIP evidence](https://github.com/Shnewbs/Deep-Primal/commit/ab15e44cdad0a40966b6682f06b486031bb3aa5e) also saved to Library `/Deep Primal/Renderer Jams/`, SHA256 `6e6dff84c6dc3c970948f19c4a1b5508b9b221dee694901663b8a68a8dd5f6d1`. Next actions in [NEXT.md](NEXT.md).
