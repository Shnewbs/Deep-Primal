package net.minecraft.core;
public class BlockPos {
  protected int x, y, z;
  protected BlockPos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
  public int getX(){return x;} public int getY(){return y;} public int getZ(){return z;}
  public static final class MutableBlockPos extends BlockPos {
    public MutableBlockPos(){super(0,0,0);}
    public MutableBlockPos set(int x,int y,int z){this.x=x;this.y=y;this.z=z;return this;}
  }
}
