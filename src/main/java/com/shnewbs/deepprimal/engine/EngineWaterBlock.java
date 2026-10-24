package com.shnewbs.deepprimal.engine;

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
  public static final net.minecraft.world.level.block.state.properties.BooleanProperty OCEAN =
      net.minecraft.world.level.block.state.properties.BooleanProperty.create("ocean_origin");
  public static final MapCodec<EngineWaterBlock> CODEC = simpleCodec(EngineWaterBlock::new);

  public EngineWaterBlock(BlockBehaviour.Properties p) {
    super(p);
    registerDefaultState(
        defaultBlockState()
            .setValue(OCEAN, false)
            .setValue(EngineFluid.UNITS, 16)
            .setValue(EngineFluid.FLOW, 16)
            .setValue(EngineFluid.SPEED, 3));
  }

  public MapCodec<EngineWaterBlock> codec() {
    return CODEC;
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
    b.add(EngineFluid.UNITS, EngineFluid.FLOW, EngineFluid.SPEED, OCEAN);
  }

  protected RenderShape getRenderShape(BlockState s) {
    return RenderShape.INVISIBLE;
  }

  protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
    return Shapes.empty();
  }

  protected FluidState getFluidState(BlockState s) {
    return EngineRegistry.WATER
        .get()
        .defaultFluidState()
        .setValue(EngineFluid.UNITS, s.getValue(EngineFluid.UNITS))
        .setValue(EngineFluid.FLOW, s.getValue(EngineFluid.FLOW))
        .setValue(EngineFluid.SPEED, s.getValue(EngineFluid.SPEED));
  }

  protected void onPlace(BlockState s, Level l, BlockPos p, BlockState old, boolean moved) {
    if (l instanceof net.minecraft.server.level.ServerLevel server)
      WaterRuntime.activate(server, p);
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
    if (l instanceof net.minecraft.server.level.ServerLevel server)
      WaterRuntime.activate(server, p);
    else ticks.scheduleTick(p, EngineRegistry.WATER.get(), 1);
    return s;
  }

  public ItemStack pickupBlock(
      LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
    if (state.getValue(EngineFluid.UNITS) != 16) return ItemStack.EMPTY;
    if (level instanceof net.minecraft.server.level.ServerLevel server) {
      var parcel = HydrologyRuntime.pickup(server, pos);
      return parcel == null ? ItemStack.EMPTY : EngineBucket.filled(parcel);
    }
    return EngineRegistry.BUCKET.get().getDefaultInstance();
  }

  public java.util.Optional<net.minecraft.sounds.SoundEvent> getPickupSound() {
    return java.util.Optional.of(net.minecraft.sounds.SoundEvents.BUCKET_FILL);
  }
}
