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
