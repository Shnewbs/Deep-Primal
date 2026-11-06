package com.shnewbs.deepprimal.core;

import java.util.function.DoubleBinaryOperator;

/** Terrain-derived runoff and low-corridor fitting; no random displacement or shape templates. */
public final class OrganicDrainage {
  public record Point(double x, double z) {}

  /** Keep the flood resolver's escape tree on flats; improve receivers only on real downslope. */
  public static int receiver(int a, int side, double spacing, double[] filled,
      int[] order, int original) {
    int x = a % side, z = a / side;
    if (original < 0 || x == 0 || z == 0 || x == side-1 || z == side-1) return original;
    int best = original;
    double drop = (filled[a]-filled[original])
        / (spacing*Math.hypot(x-original%side,z-original/side));
    for (int dz=-1;dz<=1;dz++) for (int dx=-1;dx<=1;dx++) {
      if (dx==0 && dz==0) continue;
      int b=(z+dz)*side+x+dx;
      if (order[b] >= order[a]) continue;
      double slope=(filled[a]-filled[b])/(spacing*Math.hypot(dx,dz));
      if (slope > drop+1e-12) { best=b; drop=slope; }
    }
    return best;
  }

  /** Positive curvature across the local gradient indicates a terrain-convergent hollow. */
  public static boolean convergent(int a, int side, double spacing, double[] height) {
    int x=a%side,z=a/side;
    if (x==0 || z==0 || x==side-1 || z==side-1) return false;
    double gx=(height[a+1]-height[a-1])/(2*spacing);
    double gz=(height[a+side]-height[a-side])/(2*spacing);
    double xx=(height[a+1]-2*height[a]+height[a-1])/(spacing*spacing);
    double zz=(height[a+side]-2*height[a]+height[a-side])/(spacing*spacing);
    double xz=(height[a+side+1]-height[a+side-1]-height[a-side+1]+height[a-side-1])/(4*spacing*spacing);
    double g2=gx*gx+gz*gz;
    double curvature=g2<1e-12?(xx+zz)*.5:(gz*gz*xx-2*gx*gz*xz+gx*gx*zz)/g2;
    return curvature>1e-6;
  }

  /** Fit a shared knot/control to a nearby lower terrain corridor, retaining the center on ties. */
  public static Point lowerCorridor(DoubleBinaryOperator terrain, double x, double z,
      double dx, double dz, double reach) {
    double length=Math.hypot(dx,dz);
    if (length<1e-9) return new Point(x,z);
    double nx=-dz/length,nz=dx/length;
    double best=terrain.applyAsDouble(x,z),offset=0;
    for (int n=1;n<=3;n++) for (int sign=-1;sign<=1;sign+=2) {
      double d=sign*reach*n/3;
      double score=terrain.applyAsDouble(x+nx*d,z+nz*d)+.005*d*d;
      if (score<best-1e-9) { best=score;offset=d; }
    }
    return new Point(x+nx*offset,z+nz*offset);
  }

  private OrganicDrainage() {}
}
