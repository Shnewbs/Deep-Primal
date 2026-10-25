package com.shnewbs.deepprimal.core;

/** Falling columns must not draw the same vertical wall twice in successive wet cells. */
public final class WaterfallSideChecks {
  public static void main(String[] args) {
    run();
  }

  static void run() {
    int checked = 0;
    for (boolean smooth : new boolean[] {false, true})
      for (int amount = 1; amount <= 16; amount++)
        for (int previousAmount = 1; previousAmount <= 16; previousAmount++) {
          var mesh = WaterPatchMesher.build(column(amount, 0), column(previousAmount, 0), smooth);
          for (int i = 0; i < mesh.vertices().length; i += WaterPatchMesher.STRIDE) {
            var v = mesh.vertices();
            int face = WaterPatchMesher.face(v[i + 10]);
            int y = (int) v[i + 1];
            if (face < 1 || face > 4 || y == 15) continue;
            if (v[i + 3] < 0 || v[i + 3] > 1 || v[i + 4] < 0 || v[i + 4] > 1)
              throw new AssertionError("Overlapping waterfall side: cellY=" + y
                  + " currentUnits=" + amount + " previousUnits=" + previousAmount
                  + " target=" + v[i + 3] + " previous=" + v[i + 4]);
            if (v[i + 11] != 0 && (v[i + 3] != 1 || v[i + 4] != 1))
              throw new AssertionError("Disconnected waterfall layer wall");
            checked++;
          }
        }
    // Identical world geometry on both sides of a vertical page boundary.
    for (int amount = 1; amount <= 16; amount++) {
      var below = WaterPatchMesher.build(column(amount, 0), null, true);
      var above = WaterPatchMesher.build(column(amount, 16), null, true);
      for (var mesh : new WaterPatchMesher.Mesh[] {below, above})
        for (int i = 0; i < mesh.vertices().length; i += WaterPatchMesher.STRIDE) {
          var v = mesh.vertices();
          int face = WaterPatchMesher.face(v[i + 10]);
          if (face < 1 || face > 4) continue;
          if (v[i + 11] != 0 && (v[i + 3] > 1 || v[i + 3] < 0))
            throw new AssertionError("Waterfall page boundary exposes overlapping wall");
          checked++;
        }
    }
    System.out.println("WATERFALL_SIDE_PASS vertices=" + checked
        + " amounts=16 previousAmounts=16 smoothModes=2 verticalPages=true");
    basin();
  }

  private static void basin() {
    int checked = 0;
    for (int origin : new int[] {-16, 0, 16})
      for (boolean smooth : new boolean[] {false, true}) {
        byte[] units = new byte[WaterPatchMesher.CELLS], solid = new byte[units.length];
        for (int z = -1; z <= 16; z++)
          for (int x = -1; x <= 16; x++) {
            int floor = 7 - Math.floorMod(origin + x + z, 12);
            for (int y = -1; y <= 16; y++) {
              int index = WaterPatchMesher.index(x, y, z);
              if (y <= floor) solid[index] = 1;
              else if (y <= 8) units[index] = (byte) (y == 8 ? 8 : 16);
            }
          }
        var snapshot = new WaterPatchMesher.Snapshot(units, solid, new int[units.length], new int[256]);
        var mesh = WaterPatchMesher.build(snapshot, snapshot, smooth);
        if (mesh.count() != 16 * 16 * 4)
          throw new AssertionError("Stepped lake bed generated internal water faces: " + mesh.count());
        for (int i = 0; i < mesh.vertices().length; i += WaterPatchMesher.STRIDE) {
          var v = mesh.vertices();
          if (WaterPatchMesher.face(v[i + 10]) != 0 || v[i + 1] + v[i + 3] != 8.5F
              || v[i + 1] + v[i + 4] != 8.5F)
            throw new AssertionError("Stepped lake bed changed a level free surface");
          checked++;
        }
      }
    System.out.println("STEPPED_BASIN_MESH_PASS vertices=" + checked
        + " signedSections=3 smoothModes=2 internalFaces=0 levelSurface=true");
  }

  private static WaterPatchMesher.Snapshot column(int amount, int originY) {
    byte[] units = new byte[WaterPatchMesher.CELLS];
    for (int y = -1; y <= 16; y++)
      if (originY + y < 32) units[WaterPatchMesher.index(8, y, 8)] = (byte) amount;
    return new WaterPatchMesher.Snapshot(
        units, new byte[units.length], new int[units.length], new int[256]);
  }

  private WaterfallSideChecks() {}
}
