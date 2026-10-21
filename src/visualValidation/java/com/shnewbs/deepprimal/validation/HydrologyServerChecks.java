package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.water.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Native fluid ticks, transactional ports, rain and snow in a disposable world. */
final class HydrologyServerChecks {
  static void run(ServerLevel level) {
    boolean original = RiverConfig.FINITE_VOLUME.get();
    int work = RiverConfig.FLUID_WORK.get();
    RiverConfig.FLUID_WORK.set(16384);
    try {
      var stable = new BlockPos(60, 505, 1);
      level.getChunkAt(stable);
      level.setBlock(
          stable,
          RiverWater.CHANNEL.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 5),
          2 | 16);
      for (int i = 0; i < 50; i++) nativeTick(level, stable);
      require(FiniteWater.units(level.getBlockState(stable)) == 3, "Planned level changed");
      require(level.getBlockState(stable.below()).isAir(), "Stable river spilled vertically");
      level.setBlock(stable, Blocks.AIR.defaultBlockState(), 2 | 16);
      RiverConfig.FINITE_VOLUME.set(true);
      var base = new BlockPos(64, 500, 0);
      level.getChunkAt(base);
      level.getChunkAt(base.offset(7, 0, 3));
      for (int y = 0; y <= 7; y++)
        for (int z = 0; z <= 2; z++)
          for (int x = 0; x <= 6; x++)
            level.setBlock(
                base.offset(x, y, z),
                y == 0 || x == 0 || x == 6 || z == 0 || z == 2
                    ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(),
                2 | 16);
      var source = base.offset(2, 5, 1);
      level.setBlock(source, Blocks.WATER.defaultBlockState(), 2 | 16);
      for (int repeat = 0; repeat < 12; repeat++)
        for (int y = 1; y <= 6; y++)
          for (int x = 1; x <= 5; x++) nativeTick(level, base.offset(x, y, 1));
      require(total(level, base) == 8, "Basin lost/created volume");
      require(FiniteWater.units(level.getBlockState(source)) == 0, "Water did not fall");
      var portPos = base.offset(3, 2, 1);
      level.setBlock(
          portPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 4), 2 | 16);
      var port = WaterPort.at(level, portPos);
      require(port == WaterPort.at(level, portPos), "Cell journal identity");
      var water = FluidResource.of(net.minecraft.world.level.material.Fluids.WATER);
      try (var tx = Transaction.openRoot()) {
        require(port.extract(0, water, 375, tx) == 375, "Partial extraction");
      }
      require(port.getAmountAsLong(0) == 500, "Aborted port changed volume");
      try (var tx = Transaction.openRoot()) {
        require(port.extract(0, water, 375, tx) == 375, "Committed extraction");
        tx.commit();
      }
      require(port.getAmountAsLong(0) == 125, "Committed volume");
      try (var tx = Transaction.openRoot()) {
        require(port.insert(0, water, 999, tx) == 875, "Port quantization/capacity");
        tx.commit();
      }
      require(port.getAmountAsLong(0) == 1000, "Port fill");
      // A sealed U-bend: the exposed high column must raise the lower connected outlet.
      for (int y = 1; y <= 6; y++)
        for (int x = 1; x <= 5; x++)
          level.setBlock(base.offset(x, y, 1), Blocks.STONE.defaultBlockState(), 2 | 16);
      for (int x = 1; x <= 5; x++)
        level.setBlock(base.offset(x, 1, 1), Blocks.WATER.defaultBlockState(), 2 | 16);
      for (int y = 2; y <= 5; y++)
        level.setBlock(base.offset(1, y, 1), Blocks.WATER.defaultBlockState(), 2 | 16);
      for (int y = 2; y <= 3; y++)
        level.setBlock(base.offset(5, y, 1), Blocks.WATER.defaultBlockState(), 2 | 16);
      level.setBlock(base.offset(1, 6, 1), Blocks.AIR.defaultBlockState(), 2 | 16);
      level.setBlock(base.offset(5, 4, 1), Blocks.AIR.defaultBlockState(), 2 | 16);
      level.setBlock(base.offset(5, 5, 1), Blocks.AIR.defaultBlockState(), 2 | 16);
      int before = total(level, base);
      nativeTick(level, base.offset(1, 5, 1));
      require(
          FiniteWater.units(level.getBlockState(base.offset(5, 4, 1))) > 0,
          "U-bend did not equalize");
      require(total(level, base) == before, "U-bend changed total volume");
      var soil = new BlockPos(76, 500, 8);
      level.getChunkAt(soil);
      level.setBlock(soil, Blocks.DIRT.defaultBlockState(), 2 | 16);
      for (int y = 501; y < level.getMaxY(); y++)
        level.setBlock(soil.atY(y), Blocks.AIR.defaultBlockState(), 2 | 16);
      level.getServer().setWeatherParameters(0, 6000, true, false);
      level.setRainLevel(1);
      for (int i = 0; i < 100; i++)
        WeatherHydrology.tick(
            level, soil.above(), net.minecraft.world.level.biome.Biome.Precipitation.RAIN);
      int column = (soil.getZ() & 15) * 16 + (soil.getX() & 15);
      require(
          level.getChunkAt(soil).getData(WeatherHydrology.SOIL).get(column) == 8,
          "Soil did not saturate");
      require(
          FiniteWater.units(level.getBlockState(soil.above())) > 0, "No runoff after saturation");
      RiverConfig.UNLIMITED_SNOW.set(true);
      var snow = new BlockPos(78, 500, 8);
      for (int y = 501; y < level.getMaxY(); y++)
        level.setBlock(snow.atY(y), Blocks.AIR.defaultBlockState(), 2 | 16);
      level.setBlock(snow, Blocks.STONE.defaultBlockState(), 2 | 16);
      for (int i = 0; i < 90; i++) {
        var top =
            level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, snow);
        Precipitation.snow(level, top);
      }
      require(
          level.getBlockState(snow.above(9)).is(Blocks.SNOW_BLOCK)
              || level.getBlockState(snow.above(9)).is(Blocks.SNOW),
          "Snow capped at 64 layers");
      DrainageServerChecks.record(
          "HYDROLOGY_NATIVE_PASS stableNativeTicks=50 basinConservation=8 U-bend=true rollback=true"
              + " committed=true quantization125=true soil=8 runoff=true unlimitedSnow=true");
    } finally {
      RiverConfig.FINITE_VOLUME.set(original);
      RiverConfig.UNLIMITED_SNOW.set(false);
      RiverConfig.FLUID_WORK.set(work);
    }
  }

  private static int total(ServerLevel level, BlockPos base) {
    int total = 0;
    for (int y = 1; y <= 6; y++)
      for (int x = 1; x <= 5; x++)
        total += FiniteWater.units(level.getBlockState(base.offset(x, y, 1)));
    return total;
  }

  private static void nativeTick(ServerLevel level, BlockPos p) {
    var state = level.getBlockState(p);
    var fluid = state.getFluidState();
    if (fluid.getType() instanceof FlowingFluid flowing) flowing.tick(level, p, state, fluid);
  }

  private static void require(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  private HydrologyServerChecks() {}
}
