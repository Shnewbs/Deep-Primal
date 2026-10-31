package net.minecraft.world.level.chunk;
import net.minecraft.world.level.ChunkPos;
public final class ChunkAccess {
  private final ChunkPos pos;
  private final int minY,height;
  public ChunkAccess(int x,int z,int minY,int height){this.pos=new ChunkPos(x,z);this.minY=minY;this.height=height;}
  public ChunkPos getPos(){return pos;}
  public int getMinY(){return minY;}
  public int getHeight(){return height;}
}
