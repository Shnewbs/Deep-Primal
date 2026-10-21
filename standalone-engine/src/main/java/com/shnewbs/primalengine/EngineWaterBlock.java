package com.shnewbs.primalengine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.*;

public final class EngineWaterBlock extends Block implements BucketPickup {
  public static final MapCodec<EngineWaterBlock> CODEC = simpleCodec(EngineWaterBlock::new);

  public EngineWaterBlock(BlockBehaviour.Properties p) {
    super(p);
    registerDefaultState(
        defaultBlockState().setValue(EngineFluid.UNITS, 16).setValue(EngineFluid.FLOW, 16));
  }

  public MapCodec<EngineWaterBlock> codec() {
    return CODEC;
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
    b.add(EngineFluid.UNITS, EngineFluid.FLOW);
  }

  protected RenderShape getRenderShape(BlockState s) {
    return RenderShape.INVISIBLE;
  }

  protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
    return Shapes.empty();
  }

  protected FluidState getFluidState(BlockState s) {
    return PrimalEngine.WATER
        .get()
        .defaultFluidState()
        .setValue(EngineFluid.UNITS, s.getValue(EngineFluid.UNITS))
        .setValue(EngineFluid.FLOW, s.getValue(EngineFluid.FLOW));
  }

  protected void onPlace(BlockState s, Level l, BlockPos p, BlockState old, boolean moved) {
    l.scheduleTick(p, PrimalEngine.WATER.get(), 2);
  }

  protected BlockState updateShape(
      BlockState s,
      LevelReader l,
      ScheduledTickAccess ticks,
      BlockPos p,
      Direction d,
      BlockPos q,
      BlockState n,
      RandomSource r) {
    ticks.scheduleTick(p, PrimalEngine.WATER.get(), 2);
    return s;
  }

  public ItemStack pickupBlock(
      LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
    if (state.getValue(EngineFluid.UNITS) != 16) return ItemStack.EMPTY;
    if (!level.isClientSide()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    return PrimalEngine.BUCKET.get().getDefaultInstance();
  }

  public java.util.Optional<net.minecraft.sounds.SoundEvent> getPickupSound() {
    return java.util.Optional.of(net.minecraft.sounds.SoundEvents.BUCKET_FILL);
  }
}
