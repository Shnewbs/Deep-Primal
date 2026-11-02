package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent surface invariants and actual generated patch topology. */
final class WaterSurfaceShapeChecks {
  static void run() {
    int checked = 0;
    for (int v = 0; v <= 20; v++)
      for (int u = 0; u <= 20; u++) {
        double x = u / 20., z = v / 20.;
        if (WaterTransport.surface(1, 1, 1, 1, .0625, x, z) != 1)
          throw new AssertionError("Cell-center dimple");
        if (Math.abs(WaterTransport.surface(.2, .4, .3, .5, .95, x, z) - (.2 + .2 * x + .1 * z))
            > 1e-12) throw new AssertionError("Planar tent");
        checked++;
      }
    var random = new Random(20261022);
    for (int n = 0; n < 20000; n++) {
      double a = .0625 + random.nextDouble() * .9375,
          b = .0625 + random.nextDouble() * .9375,
          c = .0625 + random.nextDouble() * .9375,
          d = .0625 + random.nextDouble() * .9375;
      double h =
          WaterTransport.surface(
              a, b, c, d, random.nextDouble(), random.nextDouble(), random.nextDouble());
      if (h < Math.min(Math.min(a, b), Math.min(c, d)) - 1e-12
          || h > Math.max(Math.max(a, b), Math.max(c, d)) + 1e-12)
        throw new AssertionError("New surface extremum");
    }
    byte[] units = new byte[WaterPatchMesher.CELLS], solid = new byte[units.length];
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) {
        units[WaterPatchMesher.index(x, 0, z)] = (byte) (((x + z) & 1) == 0 ? 16 : 1);
        solid[WaterPatchMesher.index(x, -1, z)] = 1;
      }
    var mesh =
        WaterPatchMesher.build(
            new WaterPatchMesher.Snapshot(units, solid, new int[units.length], new int[256]),
            null,
            true);
    int top = 0;
    for (int i = 0; i < mesh.vertices().length; i += WaterPatchMesher.STRIDE) {
      if (mesh.vertices()[i + 9] != (8 | (8 << 4))
          || mesh.vertices()[i + 10] != 0) continue;
      if (mesh.vertices()[i + 3] != 17 / 32f)
        throw new AssertionError("Patch checkerboard dimples");
      top++;
    }
    if (top != 16) throw new AssertionError("Unexpected smooth top tessellation: " + top);
    int verticalSeams = 0;
    for (int amount = 1; amount <= 16; amount++) {
      byte[] stepped = new byte[WaterPatchMesher.CELLS];
      for (int z = -1; z <= 16; z++)
        for (int x = -1; x <= 16; x++) {
          for (int y = -1; y <= 8; y++) stepped[WaterPatchMesher.index(x, y, z)] = 16;
          if (x >= 8) stepped[WaterPatchMesher.index(x, 9, z)] = (byte) amount;
        }
      var step =
          WaterPatchMesher.build(
              new WaterPatchMesher.Snapshot(
                  stepped, new byte[stepped.length], new int[stepped.length], new int[256]),
              null,
              true);
      int shared = 0;
      for (int i = 0; i < step.vertices().length; i += WaterPatchMesher.STRIDE) {
        float[] a = step.vertices();
        if (a[i + 10] != 0 || Math.abs(a[i] - 8) > .1 || Math.abs(a[i + 2] - 8) > .1) continue;
        if (a[i + 1] + a[i + 3] != 9 + amount / 32f)
          throw new AssertionError(
              "Water surface tears across a vertical voxel boundary: units="
                  + amount
                  + " height="
                  + (a[i + 1] + a[i + 3]));
        shared++;
      }
      if (shared < 4) throw new AssertionError("Missing both sides of stepped surface");
      verticalSeams += shared;
    }
    int verticalPageSeams = 0;
    for (int lower = 1; lower <= 16; lower++)
      for (int upper = 1; upper <= 16; upper++) {
        int shared = 0;
        for (int origin : new int[] {0, 16}) {
          byte[] stepped = new byte[WaterPatchMesher.CELLS];
          for (int z = -1; z <= 16; z++)
            for (int x = -1; x <= 16; x++)
              for (int y = -1; y <= 16; y++) {
                int worldY = origin + y;
                int amount =
                    worldY < 15
                        ? 16
                        : worldY == 15 ? (x < 8 ? lower : 16) : worldY == 16 && x >= 8 ? upper : 0;
                stepped[WaterPatchMesher.index(x, y, z)] = (byte) amount;
              }
          var step =
              WaterPatchMesher.build(
                  new WaterPatchMesher.Snapshot(
                      stepped, new byte[stepped.length], new int[stepped.length], new int[256]),
                  null,
                  true);
          for (int i = 0; i < step.vertices().length; i += WaterPatchMesher.STRIDE) {
            float[] a = step.vertices();
            if (a[i + 10] != 0
                || Math.abs(a[i] - 8) > .1
                || Math.abs(a[i + 2] - 8) > .1) continue;
            if (origin + a[i + 1] + a[i + 3] != 15.5f + (lower + upper) / 32f)
              throw new AssertionError(
                  "Vertical section seam changed world surface height: lower="
                      + lower
                      + " upper="
                      + upper
                      + " origin="
                      + origin
                      + " localY="
                      + a[i + 1]
                      + " height="
                      + a[i + 3]
                      + " expected="
                      + (15.5f + (lower + upper) / 32f));
            shared++;
          }
        }
        if (shared < 4) throw new AssertionError("Missing vertical section seam coverage");
        verticalPageSeams += shared;
      }
    System.out.println(
        "WATER_SURFACE_SHAPE_PASS planarSamples="
            + checked
            + " extremaCases=20000 planarPatchVertices="
            + top
            + " verticalSeamVertices="
            + verticalSeams
            + " verticalPageSeamVertices="
            + verticalPageSeams);
  }
}
