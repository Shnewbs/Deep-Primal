package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterTransport;
import java.util.WeakHashMap;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Server-thread-only, bounded finite transport. Unloaded borders are sealed, never force-loaded.
 */
public final class WaterRuntime {
  private static final Direction[] SIDES = {
    Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
  };
  private static final WeakHashMap<ServerLevel, Budget> BUDGETS = new WeakHashMap<>();

  private static final class Budget {
    long time = Long.MIN_VALUE;
    int count;
  }

  public static int units(ServerLevel l, BlockPos p) {
    var s = l.getBlockState(p);
    return s.is(EngineRegistry.WATER_BLOCK.get()) ? s.getValue(EngineFluid.UNITS) : 0;
  }

  private static boolean open(ServerLevel l, BlockPos p) {
    if (!l.isInWorldBounds(p) || !l.hasChunkAt(p)) return false;
    var s = l.getBlockState(p);
    return s.isAir() || s.is(EngineRegistry.WATER_BLOCK.get());
  }

  private static void set(ServerLevel l, BlockPos p, int units, int flow) {
    l.setBlock(
        p,
        units == 0
            ? Blocks.AIR.defaultBlockState()
            : EngineRegistry.WATER_BLOCK
                .get()
                .defaultBlockState()
                .setValue(EngineFluid.UNITS, units)
                .setValue(EngineFluid.FLOW, flow),
        3);
  }

  public static void tick(ServerLevel level, BlockPos p) {
    Budget budget = BUDGETS.computeIfAbsent(level, k -> new Budget());
    long t = level.getGameTime();
    if (budget.time != t) {
      budget.time = t;
      budget.count = 0;
    }
    if (budget.count >= 2048) {
      level.scheduleTick(p, EngineRegistry.WATER.get(), 2);
      return;
    }
    budget.count++;
    if (units(level, p) == 0) return;
    boolean moved = move(level, p, p.below(), true);
    int rotate = Math.floorMod(p.hashCode() + (int) t, 4);
    for (int n = 0; n < 4 && units(level, p) > 1; n++)
      moved |= move(level, p, p.relative(SIDES[(rotate + n) & 3]), false);
    if (moved && units(level, p) > 0) level.scheduleTick(p, EngineRegistry.WATER.get(), 2);
  }

  private static boolean move(ServerLevel l, BlockPos a, BlockPos b, boolean down) {
    if (!open(l, b)) return false;
    var e = WaterTransport.exchange(units(l, a), units(l, b), down);
    if (e.moved() == 0) return false;
    int dx = b.getX() - a.getX(), dz = b.getZ() - a.getZ();
    int flow = down ? 16 : dx > 0 ? 0 : dz > 0 ? 4 : dx < 0 ? 8 : 12;
    set(l, a, e.from(), flow);
    set(l, b, e.to(), flow);
    return true;
  }

  private WaterRuntime() {}
}
