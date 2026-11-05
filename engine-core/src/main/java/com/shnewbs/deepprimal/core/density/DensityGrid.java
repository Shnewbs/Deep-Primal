package com.shnewbs.deepprimal.core.density;

import java.util.Objects;

/**
 * Immutable bounded global-lattice cache. Nodes are exact; between-node interpolation is an
 * approximation, explicitly separate from sampling the source field. No hidden global cache.
 */
public final class DensityGrid implements DensityField {
  private final int originX, originY, originZ, stepX, stepY, stepZ, sizeX, sizeY, sizeZ;
  private final double[] values;

  private DensityGrid(int x, int y, int z, int sx, int sy, int sz, int nx, int ny, int nz, double[] values) {
    originX=x; originY=y; originZ=z; stepX=sx; stepY=sy; stepZ=sz; sizeX=nx; sizeY=ny; sizeZ=nz;
    this.values=values;
  }

  /** origins must align to the world lattice, including negative origins; dimensions count nodes. */
  public static DensityGrid sample(DensityField field, int x, int y, int z,
                                   int sx, int sy, int sz, int nx, int ny, int nz) {
    Objects.requireNonNull(field, "field");
    int count = (int)SeededNoise3D.checkedCount(nx,ny,nz);
    axis(x,sx,nx); axis(y,sy,ny); axis(z,sz,nz);
    double[] data = new double[count];
    if (field instanceof SeededNoise3D noise) noise.fill(data,0,nx,ny,nz,x,y,z,sx,sy,sz);
    else {
      int at=0;
      for (int iz=0;iz<nz;iz++) for (int iy=0;iy<ny;iy++) for (int ix=0;ix<nx;ix++) {
        double value=field.sample(x+(long)ix*sx,y+(long)iy*sy,z+(long)iz*sz);
        if (!Double.isFinite(value)) throw new IllegalArgumentException("nonfinite field sample");
        data[at++]=value;
      }
    }
    return new DensityGrid(x,y,z,sx,sy,sz,nx,ny,nz,data);
  }

  private static void axis(int origin, int step, int nodes) {
    if (step<1 || step>64 || nodes<2 || Math.floorMod(origin,step)!=0)
      throw new IllegalArgumentException("aligned origin, spacing 1..64 and >=2 nodes required");
    SeededNoise3D.coordinate(origin);
    SeededNoise3D.coordinate(origin+(long)(nodes-1)*step);
  }

  public int nodeCount() { return values.length; }
  public long payloadBytes() { return (long)values.length*Double.BYTES; }

  @Override
  public double sample(double x,double y,double z) {
    double qx=local(x,originX,stepX,sizeX), qy=local(y,originY,stepY,sizeY), qz=local(z,originZ,stepZ,sizeZ);
    int ix=Math.min((int)qx,sizeX-2), iy=Math.min((int)qy,sizeY-2), iz=Math.min((int)qz,sizeZ-2);
    double tx=qx-ix,ty=qy-iy,tz=qz-iz;
    double lo=lerp(lerp(at(ix,iy,iz),at(ix+1,iy,iz),tx),lerp(at(ix,iy+1,iz),at(ix+1,iy+1,iz),tx),ty);
    double hi=lerp(lerp(at(ix,iy,iz+1),at(ix+1,iy,iz+1),tx),lerp(at(ix,iy+1,iz+1),at(ix+1,iy+1,iz+1),tx),ty);
    return lerp(lo,hi,tz);
  }

  private static double local(double world,int origin,int step,int nodes) {
    if (!Double.isFinite(world) || world<origin || world>origin+(long)(nodes-1)*step)
      throw new IllegalArgumentException("sample outside grid");
    return (world-origin)/step;
  }

  private double at(int x,int y,int z) { return values[(z*sizeY+y)*sizeX+x]; }
  // Exact endpoints are necessary for bit-identical neighboring grid faces.
  private static double lerp(double a,double b,double t) {
    if (t==0) return a;
    if (t==1) return b;
    double delta=b-a;
    // Finite opposite-sign endpoints can overflow their difference. Weighted terms
    // remain finite and have opposite signs. Preserve ordinary interpolation bits.
    return Double.isFinite(delta) ? a+delta*t : a*(1-t)+b*t;
  }
}
