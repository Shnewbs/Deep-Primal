package net.minecraft.world.phys;
/** Minimal test-only class, not an in-game AABB. */
public final class AABB {
  public static long created;
  public final double minX,minY,minZ,maxX,maxY,maxZ;
  public AABB(double a,double b,double c,double d,double e,double f) {
    minX=a;minY=b;minZ=c;maxX=d;maxY=e;maxZ=f;
    created++;
  }
}
