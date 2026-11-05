package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent rectangle-distance oracle for horizon LOD and invalid view telemetry. */
public final class IndependentHorizonGeometryChecks {
  private static long assertions;
  private static void require(boolean b, String why) {
    assertions++;
    if (!b) throw new AssertionError(why + " at check " + assertions);
  }
  private static int oracle(HorizonView v, int tx, int tz) {
    // Geometric rectangle-distance oracle: compute tile edges, not tile center.
    double left = (double)tx * 512, right = ((long)tx + 1L) * 512.0;
    double near = (double)tz * 512, far = ((long)tz + 1L) * 512.0;
    double dx = Math.max(Math.max(left - v.x(), v.x() - right), 0);
    double dz = Math.max(Math.max(near - v.z(), v.z() - far), 0);
    double sq = dx * dx + dz * dz;
    return sq < v.fineSquared() ? 0 : sq < v.mediumSquared() ? 1 : 2;
  }
  public static void main(String[] args) {
    HorizonView v = HorizonView.create(0, 0, 720, 70, 1);
    require(v.fineSquared() == 1048576.0, "fine threshold");
    require(v.mediumSquared() == 6553600.0, "medium threshold");
    for (int tx : new int[] {-6,-5,-3,-2,-1,0,1,2,3,5,6}) {
      for (int tz : new int[] {-6,-5,-3,-2,-1,0,1,2,3,5,6}) {
        require(v.detail(tx,tz)==oracle(v,tx,tz), "boundary geometry");
      }
    }
    require(v.detail(2,0)==1 && v.detail(-3,0)==1, "exact fine threshold exclusive");
    require(v.detail(5,0)==2 && v.detail(-6,0)==2, "exact medium threshold exclusive");
    for (double fov : new double[] {0, -1, 180, 180.00001, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
      for (int tx : new int[] {-100,0,100,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
        require(HorizonView.create(0,0,1080,fov,1).detail(tx,0)==2,"invalid fov coarse");
      }
    }
    for (double workload : new double[] {Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
      for (int tx : new int[] {-100,0,100,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
        require(HorizonView.create(0,0,1080,70,workload).detail(tx,0)==2,"invalid workload coarse");
      }
    }
    for (int height : new int[] {0,-1,Integer.MIN_VALUE}) {
      for (int tx : new int[] {-100,0,100,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
        require(HorizonView.create(0,0,height,70,1).detail(tx,0)==2,"minimized coarse");
      }
    }
    Random r = new Random(0x140A11D1L);
    int[] tiers = new int[3];
    for (int i=0;i<400_000;i++) {
      double x = r.nextInt(10_000_000) - 5_000_000 + r.nextDouble();
      double z = r.nextInt(10_000_000) - 5_000_000 + r.nextDouble();
      int height = r.nextInt(3500)+1;
      double fov = r.nextDouble()*179.99+.005;
      double workload = r.nextDouble()*3-1;
      int tx = (int)Math.floor(x/512) + r.nextInt(23)-11;
      int tz = (int)Math.floor(z/512) + r.nextInt(23)-11;
      HorizonView view = HorizonView.create(x,z,height,fov,workload);
      int got = view.detail(tx,tz), expected=oracle(view,tx,tz);
      require(got==expected,"random geometry at "+i);
      tiers[got]++;
    }
    require(tiers[0]>1000 && tiers[1]>1000 && tiers[2]>1000, "all tiers exercised");
    System.out.println("INDEPENDENT_HORIZON_AUDIT_PASS assertions="+assertions+" tiers="+java.util.Arrays.toString(tiers));
  }
  private IndependentHorizonGeometryChecks() {}
}
