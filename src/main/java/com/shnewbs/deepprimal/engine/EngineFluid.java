package com.shnewbs.deepprimal.engine;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Platform adapter for our fluid. Deliberately derives from Fluid, not WaterFluid/FlowingFluid. */
public final class EngineFluid extends Fluid {
  public static final IntegerProperty UNITS = IntegerProperty.create("units", 1, 16);
  public static final IntegerProperty SPEED = IntegerProperty.create("speed", 0, 7);
  public static final IntegerProperty FLOW = IntegerProperty.create("flow", 0, 16);

  public EngineFluid() {
    registerDefaultState(
        defaultFluidState().setValue(UNITS, 16).setValue(FLOW, 16).setValue(SPEED, 3));
  }

  protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> b) {
    b.add(UNITS, FLOW, SPEED);
  }

  public net.neoforged.neoforge.fluids.FluidType getFluidType() {
    return net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value();
  }

  public Item getBucket() {
    return EngineRegistry.BUCKET.get();
  }

  public int getTickDelay(LevelReader level) {
    return 1;
  }

  protected float getExplosionResistance() {
    return 100;
  }

  protected boolean canBeReplacedWith(
      FluidState s, BlockGetter l, BlockPos p, Fluid f, Direction d) {
    return false;
  }

  public int getAmount(FluidState s) {
    return s.getValue(UNITS);
  }

  public boolean isSource(FluidState s) {
    return s.getValue(UNITS) == 16;
  }

  public float getOwnHeight(FluidState s) {
    return s.getValue(UNITS) / 16f;
  }

  public float getHeight(FluidState s, BlockGetter l, BlockPos p) {
    return l.getFluidState(p.above()).getType() == this ? 1 : getOwnHeight(s);
  }

  public VoxelShape getShape(FluidState s, BlockGetter l, BlockPos p) {
    return Shapes.box(0, 0, 0, 1, getHeight(s, l, p), 1);
  }

  protected BlockState createLegacyBlock(FluidState s) {
    return EngineRegistry.WATER_BLOCK
        .get()
        .defaultBlockState()
        .setValue(UNITS, s.getValue(UNITS))
        .setValue(FLOW, s.getValue(FLOW))
        .setValue(SPEED, s.getValue(SPEED));
  }

  protected Vec3 getFlow(BlockGetter l, BlockPos p, FluidState s) {
    int d = s.getValue(FLOW);
    return d == 16
        ? Vec3.ZERO
        : new Vec3(
            Math.cos(d * Math.PI / 8) * (.1 + s.getValue(SPEED) * .1),
            0,
            Math.sin(d * Math.PI / 8) * (.1 + s.getValue(SPEED) * .1));
  }

  protected void tick(ServerLevel l, BlockPos p, BlockState b, FluidState s) {
    WaterRuntime.activate(l, p);
  }
}
