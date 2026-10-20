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
  public static final IntegerProperty SPEED = IntegerProperty.create("speed", 0, 3);
  private static final Vec3[][] VECTORS = new Vec3[4][16];

  static {
    for (int i = 0; i < 16; i++) {
      double angle = i * Math.PI / 8;
      for (int band = 0; band < 4; band++)
        VECTORS[band][i] =
            new Vec3(Math.cos(angle), 0, Math.sin(angle))
                .scale(com.shnewbs.deepprimal.core.WaterDynamics.speedForBand(band));
    }
  }

  public RiverWaterBlock(Properties properties) {
    super(Fluids.WATER, properties);
    registerDefaultState(defaultBlockState().setValue(CURRENT, 0).setValue(SPEED, 1));
  }

  @Override
  public MapCodec<LiquidBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(CURRENT, SPEED);
  }

  public BlockState flowing(double x, double z) {
    return flowing(x, z, 1);
  }

  public BlockState flowing(double x, double z, double speed) {
    int direction = Math.floorMod((int) Math.round(Math.atan2(z, x) * 8 / Math.PI), 16);
    return defaultBlockState()
        .setValue(CURRENT, direction)
        .setValue(SPEED, com.shnewbs.deepprimal.core.WaterDynamics.band(speed));
  }

  public static Vec3 current(BlockState state) {
    if (!(state.getBlock() instanceof RiverWaterBlock) || !RiverConfig.CURRENTS.get())
      return Vec3.ZERO;
    Vec3 vector =
        VECTORS[RiverConfig.VARIABLE_SPEED.get() ? state.getValue(SPEED) : 1][
            state.getValue(CURRENT)];
    double strength = RiverConfig.STRENGTH.get();
    return strength == 1 ? vector : vector.scale(strength);
  }
}
