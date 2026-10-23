package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Flow changes the material orientation, never fluid geometry or quantity. */
final class WaterFlowTextureChecks {
  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static WaterPatchMesher.Snapshot cell(int direction) {
    byte[] units = new byte[WaterPatchMesher.CELLS];
    byte[] solid = new byte[units.length], flow = new byte[units.length];
    int[] light = new int[units.length], color = new int[256];
    Arrays.fill(color, 0xff3f76e4);
    Arrays.fill(flow, (byte) 16);
    units[WaterPatchMesher.index(3, 0, 3)] = 12;
    solid[WaterPatchMesher.index(3, -1, 3)] = 1;
    flow[WaterPatchMesher.index(3, 0, 3)] = (byte) direction;
    return new WaterPatchMesher.Snapshot(units, solid, light, color, flow);
  }

  static void run() {
    var still = cell(16);
    var reference = WaterPatchMesher.build(still, still, true);
    var legacy = new WaterPatchMesher.Snapshot(still.units(), still.solid(), still.lights(), still.colors());
    require(Arrays.equals(reference.vertices(), WaterPatchMesher.build(legacy, legacy, true).vertices()), "legacy material is still water");
    int checked = 0;
    for (int direction = 0; direction < 16; direction++) {
      var snapshot = cell(direction);
      var units = snapshot.units().clone();
      var flow = snapshot.flow().clone();
      var mesh = WaterPatchMesher.build(snapshot, snapshot, true);
      require(mesh.count() == reference.count(), "direction preserves coverage");
      var a = reference.vertices();
      var b = mesh.vertices();
      int step = WaterPatchMesher.STRIDE;
      for (int i = 0; i < a.length; i += step) {
        for (int j = 0; j < 14; j++)
          if (j != 5 && j != 6 && j != 10)
            require(Float.floatToRawIntBits(a[i+j]) == Float.floatToRawIntBits(b[i+j]), "direction preserves geometry, lighting and transitions");
        require(WaterPatchMesher.face(b[i+10]) == (int)a[i+10] && WaterPatchMesher.direction(b[i+10]) == direction, "flow material retained through worker output");
        require(b[i+5] >= 0 && b[i+5] <= 1 && b[i+6] >= 0 && b[i+6] <= 1, "UV stays within its atlas sprite");
        if (WaterPatchMesher.face(b[i+10]) != 0) require(b[i+5] <= .5f && b[i+6] <= .5f, "flowing side uses native texel density");
      }
      // Top order is (0,0),(0,1),(1,1),(1,0). V must increase along actual flow.
      double vx = b[3*step+6] - b[6], vz = b[step+6] - b[6];
      double angle = direction * Math.PI / 8;
      require(Math.abs(vx*Math.cos(angle) + vz*Math.sin(angle) - .5) < .000001, "texture V follows flow rather than crossing it");
      require(Math.abs(vx*Math.sin(angle) - vz*Math.cos(angle)) < .000001, "flow rotation remains orthogonal");
      require(Arrays.equals(units, snapshot.units()) && Arrays.equals(flow, snapshot.flow()), "worker snapshot remains immutable");
      var dry = new WaterPatchMesher.Snapshot(new byte[WaterPatchMesher.CELLS], still.solid(), still.lights(), still.colors());
      var fading = WaterPatchMesher.build(dry, snapshot, true).vertices();
      for (int i = 0; i < fading.length; i += step) require(WaterPatchMesher.direction(fading[i+10]) == direction && fading[i+13] == 0, "drying faces retain original flow material");
      checked += mesh.count();
    }
    System.out.println("WATER_FLOW_TEXTURE_CORE_PASS directions=16 vertices="+checked+" legacyStill=true quantityAndGeometryUnchanged=true snapshotImmutable=true dryMaterialRetained=true atlasBounds=true");
  }

  private WaterFlowTextureChecks() {}
}
