package net.minecraft.world.level.block.state;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
public final class BlockState {
  public record Fluid(boolean empty) { public boolean isEmpty(){return empty;} }
  private final String id;
  private final boolean air, solidTop, dry;
  public BlockState(String id,boolean air,boolean solidTop,boolean dry){this.id=id;this.air=air;this.solidTop=solidTop;this.dry=dry;}
  public boolean isAir(){return air;}
  public Fluid getFluidState(){return new Fluid(dry);}
  public boolean isFaceSturdy(BlockGetter chunk,BlockPos pos,Direction face){
    return solidTop && face==Direction.UP;
  }
  @Override public boolean equals(Object obj){return obj instanceof BlockState s && Objects.equals(id,s.id) && air==s.air && solidTop==s.solidTop && dry==s.dry;}
  @Override public int hashCode(){return Objects.hash(id,air,solidTop,dry);}
}
