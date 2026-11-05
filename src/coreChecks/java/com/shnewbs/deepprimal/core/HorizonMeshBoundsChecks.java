package com.shnewbs.deepprimal.core;
import java.util.Arrays;
import java.util.Random;
public final class HorizonMeshBoundsChecks {
  static long assertions;
  static void check(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason + " assertion=" + assertions);
  }
  public static void run() {
    Random random = new Random(2026110203L);
    for (int detail = 0; detail < 3; detail++) {
      int n = HorizonGeometry.cells(detail), points = (n+1)*(n+1);
      short[] heights = new short[points];
      int[] colors = new int[points];
      for (int i=0;i<points;i++) colors[i]=0xff123456;
      // Ordinary geometry must retain existing bounds.
      Arrays.fill(heights, (short) 120);
      HorizonMesh ordinary = HorizonMesh.build(heights, colors, detail, -64);
      check(ordinary.minimum() == -64 && ordinary.maximum() == 120, "ordinary bounds");
      // Water's 1/8-block surface offset must be included in the advertised AABB.
      Arrays.fill(heights, (short) -64);
      Arrays.fill(colors, 0xf9123456);
      HorizonMesh water = HorizonMesh.build(heights, colors, detail, -64);
      check(water.minimum() <= -65, "water roof below declared minimum");
      // A below-floor captured tile must not be culled by incorrect minimum.
      Arrays.fill(heights, (short) -120);
      Arrays.fill(colors, 0xff123456);
      HorizonMesh below = HorizonMesh.build(heights, colors, detail, -64);
      check(below.minimum() <= -120, "below-floor roof bounds");
      for (int trial=0;trial<50;trial++) {
        for(int i=0;i<points;i++) {
          heights[i]=(short)(random.nextInt(800)-500);
          colors[i]=random.nextBoolean()?0xf9123456:0xff123456;
        }
        HorizonMesh mesh=HorizonMesh.build(heights, colors, detail, -64);
        for(int i=0;i<mesh.vertices();i++) {
          float y=mesh.positions()[i*3+1];
          check(y >= mesh.minimum() && y <= mesh.maximum(), "vertex outside declared vertical bounds");
        }
      }
    }
    System.out.println("HORIZON_MESH_BOUNDS_PASS assertions="+assertions);
  }
  public static void main(String[] args) { run(); }
  private HorizonMeshBoundsChecks() {}
}
