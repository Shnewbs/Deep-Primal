package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterTransport;
import java.util.*;
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
  private static final int PRESSURE_VISITS_PER_TICK = 4096, PRESSURE_SEARCH = 128;

  private static final class Budget {
    long time = Long.MIN_VALUE;
    int count, pressureVisits;
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

  private static net.minecraft.world.level.block.state.BlockState state(
      net.minecraft.world.level.block.state.BlockState old, int units, int flow) {
    if (units == 0) return Blocks.AIR.defaultBlockState();
    return (old.is(EngineRegistry.WATER_BLOCK.get())
            ? old
            : EngineRegistry.WATER_BLOCK.get().defaultBlockState())
        .setValue(EngineFluid.UNITS, units)
        .setValue(EngineFluid.FLOW, flow);
  }

  private static void wake(ServerLevel l, BlockPos p) {
    if (units(l, p) > 0) l.scheduleTick(p, EngineRegistry.WATER.get(), 2);
    for (Direction d : Direction.values()) {
      BlockPos q = p.relative(d);
      if (l.isInWorldBounds(q) && l.hasChunkAt(q)) {
        l.neighborChanged(q, EngineRegistry.WATER_BLOCK.get(), null);
        if (units(l, q) > 0) l.scheduleTick(q, EngineRegistry.WATER.get(), 2);
      }
    }
  }

  private static boolean transfer(
      ServerLevel l, BlockPos a, BlockPos b, WaterTransport.Exchange exchange, int flow) {
    if (exchange.moved() == 0) return false;
    var beforeA = l.getBlockState(a);
    var beforeB = l.getBlockState(b);
    // Commit both volumes before notifying neighbors; failed writes retain the original volume.
    if (!l.setBlock(a, state(beforeA, exchange.from(), flow), 2 | 16)) return false;
    if (!l.setBlock(b, state(beforeB, exchange.to(), flow), 2 | 16)) {
      l.setBlock(a, beforeA, 2 | 16);
      return false;
    }
    wake(l, a);
    wake(l, b);
    return true;
  }

  public static void tick(ServerLevel level, BlockPos p) {
    Budget budget = BUDGETS.computeIfAbsent(level, k -> new Budget());
    long t = level.getGameTime();
    if (budget.time != t) {
      budget.time = t;
      budget.count = 0;
      budget.pressureVisits = 0;
    }
    if (budget.count >= 2048) {
      level.scheduleTick(p, EngineRegistry.WATER.get(), 2);
      return;
    }
    budget.count++;
    if (!level.isInWorldBounds(p) || !level.hasChunkAt(p)) return;
    if (units(level, p) == 0) return;
    boolean moved = move(level, p, p.below(), true);
    int rotate = Math.floorMod(p.hashCode() + (int) t, 4);
    for (int n = 0; n < 4 && units(level, p) > 1; n++)
      moved |= move(level, p, p.relative(SIDES[(rotate + n) & 3]), false);
    if (!moved && units(level, p) > 0) moved = equalize(level, p, budget);
    if (moved && units(level, p) > 0) level.scheduleTick(p, EngineRegistry.WATER.get(), 2);
  }

  private static boolean move(ServerLevel l, BlockPos a, BlockPos b, boolean down) {
    if (!open(l, b)) return false;
    var e = WaterTransport.exchange(units(l, a), units(l, b), down);
    if (e.moved() == 0) return false;
    int dx = b.getX() - a.getX(), dz = b.getZ() - a.getZ();
    int flow = down ? 16 : dx > 0 ? 0 : dz > 0 ? 4 : dx < 0 ? 8 : 12;
    return transfer(l, a, b, e, flow);
  }

  /** Bounded connected-water search; sealed and unloaded cells cannot become outlets. */
  private static boolean equalize(ServerLevel l, BlockPos origin, Budget budget) {
    if (budget.pressureVisits >= PRESSURE_VISITS_PER_TICK) {
      l.scheduleTick(origin, EngineRegistry.WATER.get(), 2);
      return false;
    }
    var queue = new ArrayDeque<BlockPos>();
    var seen = new HashSet<BlockPos>();
    queue.add(origin);
    seen.add(origin);
    BlockPos high = null, low = null;
    long highHead = Long.MIN_VALUE, lowHead = Long.MAX_VALUE;
    while (!queue.isEmpty() && budget.pressureVisits < PRESSURE_VISITS_PER_TICK) {
      BlockPos p = queue.removeFirst();
      budget.pressureVisits++;
      int amount = units(l, p);
      BlockPos above = p.above();
      if (open(l, above) && units(l, above) == 0) {
        long head = (long) p.getY() * WaterTransport.CAPACITY + amount;
        if (head > highHead) {
          highHead = head;
          high = p;
        }
        if (head < lowHead) {
          lowHead = head;
          low = amount == WaterTransport.CAPACITY ? above : p;
        }
      }
      for (Direction d : Direction.values()) {
        BlockPos q = p.relative(d);
        if (seen.size() < PRESSURE_SEARCH
            && l.isInWorldBounds(q)
            && l.hasChunkAt(q)
            && !seen.contains(q)
            && units(l, q) > 0) {
          seen.add(q);
          queue.addLast(q);
        }
      }
    }
    if (!queue.isEmpty()) l.scheduleTick(origin, EngineRegistry.WATER.get(), 2);
    if (high == null || low == null || high.equals(low)) return false;
    return transfer(
        l,
        high,
        low,
        WaterTransport.pressure(high.getY(), units(l, high), low.getY(), units(l, low)),
        16);
  }

  private WaterRuntime() {}
}
