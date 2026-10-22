package com.shnewbs.deepprimal.core;

import java.util.*;

public final class WaterAnimationChecks {
  private static void require(boolean b, String message) {
    if (!b) throw new AssertionError(message);
  }

  private static WaterPatchMesher.Snapshot strip(int origin) {
    byte[] u = new byte[WaterPatchMesher.CELLS], s = new byte[u.length];
    int[] l = new int[u.length], c = new int[256];
    Arrays.fill(c, 0xff3f76e4);
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) {
        u[WaterPatchMesher.index(x, 0, z)] = (byte) (1 + Math.floorMod(origin + x + 2 * z, 16));
        s[WaterPatchMesher.index(x, -1, z)] = 1;
      }
    return new WaterPatchMesher.Snapshot(u, s, l, c);
  }

  public static void run() {
    Random random = new Random(211004);
    var ordered = new WaterSurfaceTransition(0, 0);
    ordered.target(1, 0, 20);
    ordered.target(.25f, 50, 19);
    require(
        ordered.target() == 1 && ordered.at(100) == 1,
        "stale neighboring jobs cannot reverse a shared edge");
    for (int i = 0; i < 10000; i++) {
      float a = random.nextFloat(), b = random.nextFloat(), c = random.nextFloat();
      long start = random.nextInt(1_000_000);
      var t = new WaterSurfaceTransition(a, start);
      t.target(b, start);
      int elapsed = random.nextInt(100);
      float displayed = t.at(start + elapsed);
      require(
          displayed >= Math.min(a, b) - .00001 && displayed <= Math.max(a, b) + .00001,
          "bounded interpolation");
      t.target(c, start + elapsed);
      require(Math.abs(t.at(start + elapsed) - displayed) < .000001, "retarget continuity");
      require(Math.abs(t.at(start + elapsed + 100) - c) < .000001, "settled target");
      t.target(c, start + elapsed + 1000);
      require(t.from(start + elapsed + 1000) == c, "unchanged samples do not replay motion");
    }
    var left = strip(0);
    var right = strip(16);
    var a = WaterPatchMesher.build(left, null, true);
    var b = WaterPatchMesher.build(right, null, true);
    Map<Float, Float> edge = new HashMap<>();
    for (int i = 0; i < a.vertices().length; i += WaterPatchMesher.STRIDE)
      if (a.vertices()[i] == 16 && a.vertices()[i + 10] == 0)
        edge.put(a.vertices()[i + 2], a.vertices()[i + 3]);
    int shared = 0;
    for (int i = 0; i < b.vertices().length; i += WaterPatchMesher.STRIDE)
      if (b.vertices()[i] == 0 && b.vertices()[i + 10] == 0) {
        float z = b.vertices()[i + 2];
        require(Math.abs(edge.get(z) - b.vertices()[i + 3]) < .000001, "shared page edge");
        shared++;
      }
    require(shared > 16, "multiple shared samples");
    byte[] empty = new byte[WaterPatchMesher.CELLS];
    var dry =
        new WaterPatchMesher.Snapshot(
            empty, new byte[empty.length], new int[empty.length], new int[256]);
    var fading = WaterPatchMesher.build(dry, left, true);
    require(fading.count() > 0, "drying geometry retained for fade");
    for (int i = 0; i < fading.vertices().length; i += WaterPatchMesher.STRIDE)
      require(fading.vertices()[i + 13] == 0, "dry geometry fades out");
    require(WaterPatchMesher.build(dry, dry, true).count() == 0, "settled dry geometry released");
    System.out.println(
        "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges="
            + shared
            + " dryFade=true immutableSnapshots=true");
  }
}
