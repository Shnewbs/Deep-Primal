package com.shnewbs.deepprimal.water;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/** Vanilla water with a persistent current direction; no block entity or ticking solver. */
public final class RiverWaterBlock extends LiquidBlock {
  public static final MapCodec<LiquidBlock> CODEC = simpleCodec(RiverWaterBlock::new);
  public static final IntegerProperty CURRENT = IntegerProperty.create("current", 0, 15);
  private static final Vec3[] VECTORS = new Vec3[16];

  static {
    for (int i = 0; i < VECTORS.length; i++) {
      double angle = i * Math.PI / 8;
      VECTORS[i] = new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }
  }

  public RiverWaterBlock(Properties properties) {
    super(Fluids.WATER, properties);
    registerDefaultState(defaultBlockState().setValue(CURRENT, 0));
  }

  @Override
  public MapCodec<LiquidBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(CURRENT);
  }

  public BlockState flowing(double x, double z) {
    int direction = Math.floorMod((int) Math.round(Math.atan2(z, x) * 8 / Math.PI), 16);
    return defaultBlockState().setValue(CURRENT, direction);
  }

  public static Vec3 current(BlockState state) {
    if (!(state.getBlock() instanceof RiverWaterBlock) || !RiverConfig.CURRENTS.get())
      return Vec3.ZERO;
    Vec3 vector = VECTORS[state.getValue(CURRENT)];
    double strength = RiverConfig.STRENGTH.get();
    return strength == 1 ? vector : vector.scale(strength);
  }
}
