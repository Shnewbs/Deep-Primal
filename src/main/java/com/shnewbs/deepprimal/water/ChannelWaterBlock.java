package com.shnewbs.deepprimal.water;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Versioned generated water whose planned partial surfaces do not spill across dry banks. */
public final class ChannelWaterBlock extends RiverWaterBlock {
  public static final MapCodec<net.minecraft.world.level.block.LiquidBlock> CODEC = simpleCodec(ChannelWaterBlock::new);
  public ChannelWaterBlock(BlockBehaviour.Properties properties) { super(properties); }
  @Override public MapCodec<net.minecraft.world.level.block.LiquidBlock> codec() { return CODEC; }
  @Override public boolean stableChannel() { return true; }
}
