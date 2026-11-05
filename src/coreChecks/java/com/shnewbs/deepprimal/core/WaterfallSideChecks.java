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
    ledge();
    cliff();
  }


  /** Supported films and the pool below must meet at one world-space edge. */
  private static void ledge() {
    int checked = 0;
    for (int axis = 0; axis < 2; axis++)
      for (int sign : new int[] {-1, 1})
        for (int upperY : new int[] {-16, 0, 8, 16})
          for (boolean smooth : new boolean[] {false, true})
            for (int amount = 1; amount <= 16; amount++)
              for (int pool = 1; pool <= 16; pool++) {
                int[] witnesses = new int[2];
                int lowerOrigin = Math.floorDiv(upperY - 1, 16) * 16;
                int upperOrigin = Math.floorDiv(upperY, 16) * 16;
                for (int origin : lowerOrigin == upperOrigin ? new int[] {lowerOrigin}
                    : new int[] {lowerOrigin, upperOrigin}) {
                  var current = ledgeSnapshot(axis, sign, origin, upperY, amount, pool);
                  var previous = ledgeSnapshot(axis, sign, origin, upperY, 17 - amount, 17 - pool);
                  var mesh = WaterPatchMesher.build(current, previous, smooth).vertices();
                  for (int i = 0; i < mesh.length; i += WaterPatchMesher.STRIDE) {
                    if (WaterPatchMesher.face(mesh[i + 10]) != 0
                        || Math.abs(mesh[i] - 8) > .1
                        || Math.abs(mesh[i + 2] - 8) > .1)
                      continue;
                    int y = origin + (int) mesh[i + 1];
                    if (y != upperY && y != upperY - 1) continue;
                    for (int t = 0; t <= 4; t++) {
                      float blend = t / 4f;
                      float height = y + mesh[i + 4] + (mesh[i + 3] - mesh[i + 4]) * blend;
                      float to = upperY + Math.max(1 / 256f, -.5f + (amount + pool) / 32f);
                      float from = upperY + Math.max(1 / 256f, -.5f + (34 - amount - pool) / 32f);
                      float expected = from + (to - from) * blend;
                      if (Math.abs(height - expected) > .00001f)
                        throw new AssertionError("Rock ledge disconnect: axis=" + axis + " sign=" + sign
                            + " y=" + upperY + " cellY=" + y + " film=" + amount + " pool=" + pool
                            + " blend=" + blend + " actual=" + height + " expected=" + expected);
                    }
                    witnesses[y == upperY ? 1 : 0]++;
                  }
                }
                if (witnesses[0] == 0 || witnesses[1] == 0)
                  throw new AssertionError("Missing ledge surface witness");
                checked++;
              }
    System.out.println("ROCK_LEDGE_PASS cases=" + checked
        + " axes=2 signs=2 verticalSections=true interpolationSamples=5");
  }

  private static WaterPatchMesher.Snapshot ledgeSnapshot(int axis, int sign, int origin,
      int upperY, int film, int pool) {
    byte[] water = new byte[WaterPatchMesher.CELLS], solid = new byte[water.length];
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++)
        for (int y = -1; y <= 16; y++) {
          int coordinate = axis == 0 ? x : z;
          boolean high = sign > 0 ? coordinate >= 8 : coordinate < 8;
          int worldY = origin + y;
          int bed = high ? upperY - 1 : upperY - 2;
          if (worldY <= bed) solid[WaterPatchMesher.index(x, y, z)] = 1;
          else if (worldY == (high ? upperY : upperY - 1))
            water[WaterPatchMesher.index(x, y, z)] = (byte) (high ? film : pool);
        }
    return new WaterPatchMesher.Snapshot(water, solid, new int[water.length], new int[256]);
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
        if (mesh.count() != 16 * 16 * 4 * (smooth ? 9 : 1))
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


  /** A tributary below a falling column must not pull its connected wall below the next layer. */
  private static void cliff() {
    int vertices = 0;
    for (int lowerY : new int[] {-1, 7, 15})
      for (boolean smooth : new boolean[] {false, true})
        for (int amount = 1; amount <= 16; amount++)
          for (int pool = 1; pool <= 16; pool++) {
            int origin = Math.floorDiv(lowerY, 16) * 16;
            var mesh = WaterPatchMesher.build(cliffSnapshot(origin, lowerY, amount, pool),
                cliffSnapshot(origin, lowerY, 17 - amount, 17 - pool), smooth).vertices();
            int witnesses = 0;
            for (int i = 0; i < mesh.length; i += WaterPatchMesher.STRIDE) {
              int face = WaterPatchMesher.face(mesh[i + 10]);
              if (origin + (int) mesh[i + 1] != lowerY || face < 1 || face > 4
                  || mesh[i + 11] == 0) continue;
              for (int frame = 0; frame <= 12; frame++) {
                float t = frame / 12f;
                float shown = mesh[i + 4] + (mesh[i + 3] - mesh[i + 4]) * t;
                if (Math.abs(shown - 1) > .00001f)
                  throw new AssertionError("Cliff waterfall has open seam: lowerY=" + lowerY
                      + " amount=" + amount + " pool=" + pool + " frame=" + frame
                      + " wallTop=" + shown + " nextWallBottom=1");
              }
              witnesses++; vertices++;
            }
            if (witnesses == 0) throw new AssertionError("No mixed-height cliff wall witnesses");
          }
    System.out.println("CLIFF_WALL_CONTINUITY_PASS vertices=" + vertices
        + " volumePairs=256 smoothModes=2 verticalSections=3 interpolationFrames=13");
  }

  private static WaterPatchMesher.Snapshot cliffSnapshot(int origin, int lowerY, int amount, int pool) {
    byte[] water = new byte[WaterPatchMesher.CELLS];
    for (int y = -1; y <= 16; y++) {
      int worldY = origin + y;
      if (worldY == lowerY || worldY == lowerY + 1)
        water[WaterPatchMesher.index(7, y, 8)] = (byte) amount;
      if (worldY == lowerY - 1) water[WaterPatchMesher.index(8, y, 8)] = (byte) pool;
    }
    return new WaterPatchMesher.Snapshot(water, new byte[water.length], new int[water.length], new int[256]);
  }

  private WaterfallSideChecks() {}
}
