package net.minecraft.world.level.chunk;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
public final class ChunkAccess implements BlockGetter {
  private final Map<String,BlockState> cells = new HashMap<>();
  private final BlockState air;
  private long reads;
  public ChunkAccess(BlockState air){this.air=air;}
  private static String key(int x,int y,int z){return x+","+y+","+z;}
  public void set(int x,int y,int z,BlockState state){cells.put(key(x,y,z),state);}
  public BlockState getBlockState(BlockPos pos){reads++;return cells.getOrDefault(key(pos.getX(),pos.getY(),pos.getZ()),air);}
  public long reads(){return reads;}
}
