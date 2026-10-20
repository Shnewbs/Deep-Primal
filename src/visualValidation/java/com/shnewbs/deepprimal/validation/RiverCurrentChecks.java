package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.water.RiverConfig;
import com.shnewbs.deepprimal.water.RiverWater;
import com.shnewbs.deepprimal.water.RiverWaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Native movement, bucket and state checks; never packaged. */
final class RiverCurrentChecks {
  static void reopen(ServerLevel level) {
    var point = new BlockPos(8, 501, 8);
    var state = level.getBlockState(point);
    require(
        state.getBlock() instanceof RiverWaterBlock
            && state.getValue(RiverWaterBlock.CURRENT) == 11,
        "Saved river direction changed on reopen");
    require(
        state.getFluidState().getFlow(level, point).lengthSqr() > .99, "Reopened current missing");
    DrainageServerChecks.record("RIVER_REOPEN_PASS direction=11 flow=true");
  }

  static void run(ServerLevel level) {
    var block = RiverWater.BLOCK.get();
    var point = new BlockPos(8, 501, 8);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++)
        for (int y = 500; y <= 503; y++) {
          boolean boundary = x == 0 || x == 15 || z == 0 || z == 15 || y == 500;
          level.setBlock(
              new BlockPos(x, y, z),
              boundary
                  ? Blocks.STONE.defaultBlockState()
                  : y == 503 ? Blocks.AIR.defaultBlockState() : block.flowing(1, 0),
              2 | 16);
        }
    for (int i = 0; i < 16; i++) {
      var state = block.defaultBlockState().setValue(RiverWaterBlock.CURRENT, i);
      level.setBlock(point, state, 2 | 16);
      Vec3 flow = state.getFluidState().getFlow(level, point);
      Vec3 expected = new Vec3(Math.cos(i * Math.PI / 8), 0, Math.sin(i * Math.PI / 8));
      require(flow.distanceTo(expected) < 1e-8, "Stored direction missing from native fluid flow");
      var encoded =
          net.minecraft.world.level.block.state.BlockState.CODEC
              .encodeStart(com.mojang.serialization.JsonOps.INSTANCE, state)
              .getOrThrow();
      require(
          net.minecraft.world.level.block.state.BlockState.CODEC
                  .parse(com.mojang.serialization.JsonOps.INSTANCE, encoded)
                  .getOrThrow()
              == state,
          "Current direction did not serialize");
      require(
          state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.WATER,
          "River is not native water");
    }
    double[] speeds = new double[2], boats = new double[2];
    for (int band : new int[] {0, 3}) {
      for (int z = 1; z < 15; z++)
        for (int x = 1; x < 15; x++)
          for (int y = 501; y <= 502; y++)
            level.setBlock(
                new BlockPos(x, y, z), block.flowing(1, 0, band == 0 ? .35 : 1.9), 2 | 16);
      speeds[band == 0 ? 0 : 1] = item(level, 5);
      boats[band == 0 ? 0 : 1] = boat(level, 10);
    }
    require(
        speeds[1] > speeds[0] * 1.4,
        "Native non-player speed bands lost: " + java.util.Arrays.toString(speeds));
    require(boats[1] > boats[0] * 1.4, "Boat speed bands lost");
    DrainageServerChecks.record(
        "RIVER_SPEED_PASS slow="
            + speeds[0]
            + " fast="
            + speeds[1]
            + " boatSlow="
            + boats[0]
            + " boatFast="
            + boats[1]);
    for (int z = 1; z < 15; z++)
      for (int x = 1; x < 15; x++)
        for (int y = 501; y <= 502; y++)
          level.setBlock(new BlockPos(x, y, z), block.flowing(1, 0), 2 | 16);
    level.setBlock(point, block.flowing(1, 0), 2 | 16);
    double itemWith = item(level, 5);
    double boatWith = boat(level, 10);
    boolean enabled = RiverConfig.CURRENTS.get();
    double itemWithout, boatWithout;
    try {
      RiverConfig.CURRENTS.set(false);
      itemWithout = item(level, 5);
      boatWithout = boat(level, 10);
      require(
          block.defaultBlockState().getFluidState().getFlow(level, point).lengthSqr() == 0,
          "Disabled current still active");
    } finally {
      RiverConfig.CURRENTS.set(enabled);
    }
    require(itemWith > .2 && Math.abs(itemWithout) < .01, "Item did not drift downstream");
    require(boatWith > .2 && Math.abs(boatWithout) < .01, "Boat did not drift downstream");
    var bucket = block.pickupBlock(null, level, point, level.getBlockState(point));
    require(
        bucket.is(Items.WATER_BUCKET) && level.getBlockState(point).isAir(),
        "Native bucket failed");
    level.setBlock(point, block.defaultBlockState().setValue(RiverWaterBlock.CURRENT, 11), 2 | 16);
    System.out.println(
        "RIVER_CURRENT_PASS directions=16 codec=true nativeWater=true bucket=true itemDrift="
            + itemWith
            + " itemOff="
            + itemWithout
            + " boatDrift="
            + boatWith
            + " boatOff="
            + boatWithout);
    DrainageServerChecks.record(
        "RIVER_CURRENT_PASS directions=16 codec=true nativeWater=true bucket=true itemDrift="
            + itemWith
            + " itemOff="
            + itemWithout
            + " boatDrift="
            + boatWith
            + " boatOff="
            + boatWithout);
  }

  private static double item(ServerLevel level, double z) {
    var item = new ItemEntity(level, 3.5, 502.3, z, new ItemStack(Items.STICK));
    item.setDeltaMovement(Vec3.ZERO);
    level.addFreshEntity(item);
    for (int i = 0; i < 40; i++) item.tick();
    double distance = item.getX() - 3.5;
    item.discard();
    return distance;
  }

  private static double boat(ServerLevel level, double z) {
    var boat = EntityType.OAK_BOAT.create(level, EntitySpawnReason.COMMAND);
    require(boat != null, "Boat creation failed");
    boat.setPos(3.5, 502.5, z);
    boat.setDeltaMovement(Vec3.ZERO);
    level.addFreshEntity(boat);
    for (int i = 0; i < 40; i++) boat.tick();
    double distance = boat.getX() - 3.5;
    boat.discard();
    return distance;
  }

  private static void require(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
