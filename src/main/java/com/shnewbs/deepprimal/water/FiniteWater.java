package com.shnewbs.deepprimal.water;

import com.shnewbs.deepprimal.core.FluidVolume;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Optional server-thread solver, driven by Minecraft's persistent scheduled fluid ticks. */
public final class FiniteWater {
  private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
  private static final WeakHashMap<ServerLevel, Budget> BUDGETS = new WeakHashMap<>();

  private static final class Budget {
    long tick = Long.MIN_VALUE;
    int remaining;
  }

  public static boolean enabled() { return RiverConfig.FINITE_VOLUME.get(); }

  private static boolean spend(ServerLevel level, int amount) {
    var budget = BUDGETS.computeIfAbsent(level, unused -> new Budget());
    if (budget.tick != level.getGameTime()) {
      budget.tick = level.getGameTime();
      budget.remaining = RiverConfig.FLUID_WORK.get();
    }
    if (budget.remaining < amount) return false;
    budget.remaining -= amount;
    return true;
  }

  public static int units(BlockState state) {
    return state.getBlock() instanceof LiquidBlock && state.getFluidState().is(FluidTags.WATER)
        ? state.getFluidState().getAmount() : 0;
  }

  private static boolean open(ServerLevel level, BlockPos p) {
    if (p.getY() < level.getMinY() || p.getY() >= level.getMaxY() || !level.hasChunkAt(p)) return false;
    var state = level.getBlockState(p);
    return state.isAir() || units(state) > 0;
  }

  public static void tick(ServerLevel level, BlockPos p) {
    if (!level.hasChunkAt(p)) return;
    if (!spend(level, 8)) { schedule(level, p); return; }
    int source = units(level.getBlockState(p));
    if (source == 0) return;
    var down = p.below();
    if (open(level, down) && transfer(level, p, down, FluidVolume.down(source, units(level.getBlockState(down))))) return;
    int start = (int) Math.floorMod(p.asLong() ^ level.getGameTime() / 5, 4);
    for (int i = 0; i < SIDES.length; i++) {
      var next = p.relative(SIDES[(start + i) & 3]);
      if (open(level, next) && transfer(level, p, next, FluidVolume.sideways(source, units(level.getBlockState(next))))) return;
    }
    if (RiverConfig.FLUID_PRESSURE.get()) equalize(level, p);
  }

  public static boolean transfer(ServerLevel level, BlockPos from, BlockPos to, int amount) {
    if (amount <= 0 || from.equals(to) || !open(level, from) || !open(level, to)) return false;
    var a = level.getBlockState(from); var b = level.getBlockState(to);
    int source = units(a), target = units(b);
    if (source < amount || target + amount > FluidVolume.CAPACITY) return false;
    if (!level.setBlock(from, state(a, source - amount), 2 | 16)) return false;
    if (!level.setBlock(to, state(target == 0 ? a : b, target + amount), 2 | 16)) {
      level.setBlock(from, a, 2 | 16);
      return false;
    }
    wake(level, from); wake(level, to);
    return true;
  }

  static BlockState state(BlockState template, int units) {
    if (units == 0) return Blocks.AIR.defaultBlockState();
    return (template.getBlock() instanceof LiquidBlock ? template : Blocks.WATER.defaultBlockState())
        .setValue(LiquidBlock.LEVEL, FluidVolume.nativeLevel(units));
  }

  /** Explicit input/output for buckets, rain and machine adapters. Returns the signed change. */
  public static int exchange(ServerLevel level, BlockPos p, int requested, boolean simulate) {
    if (!enabled() || requested == 0 || !open(level, p)) return 0;
    var previous = level.getBlockState(p);
    int old = units(previous), next = (int) Math.clamp((long) old + requested, 0, 8), actual = next - old;
    if (actual == 0 || simulate) return actual;
    if (!level.setBlock(p, state(previous, next), 2 | 16)) return 0;
    wake(level, p);
    return actual;
  }

  private static void equalize(ServerLevel level, BlockPos origin) {
    var queue = new ArrayDeque<BlockPos>(); var seen = new HashSet<BlockPos>();
    queue.add(origin); seen.add(origin);
    BlockPos high = null, low = null;
    long highHead = Long.MIN_VALUE, lowHead = Long.MAX_VALUE;
    int limit = RiverConfig.PRESSURE_SEARCH.get();
    while (!queue.isEmpty() && spend(level, 1)) {
      var p = queue.removeFirst(); int amount = units(level.getBlockState(p)); var above = p.above();
      if (open(level, above) && units(level.getBlockState(above)) == 0) {
        long head = (long) p.getY() * 8 + amount;
        if (head > highHead) { high = p; highHead = head; }
        if (head < lowHead) { low = amount == 8 ? above : p; lowHead = head; }
      }
      for (var direction : Direction.values()) {
        if (seen.size() >= limit) break;
        var next = p.relative(direction);
        if (level.hasChunkAt(next) && units(level.getBlockState(next)) > 0 && seen.add(next)) queue.addLast(next);
      }
    }
    if (high != null && low != null && !high.equals(low))
      transfer(level, high, low, FluidVolume.pressure(high.getY(), units(level.getBlockState(high)), low.getY(), units(level.getBlockState(low))));
  }

  public static void wake(ServerLevel level, BlockPos p) {
    var changed = level.getBlockState(p).getBlock();
    for (var direction : Direction.values()) {
      var next = p.relative(direction);
      if (level.hasChunkAt(next)) level.neighborChanged(next, changed, null);
      schedule(level, next);
    }
    schedule(level, p);
  }

  private static void schedule(ServerLevel level, BlockPos p) {
    if (level.hasChunkAt(p) && units(level.getBlockState(p)) > 0)
      level.scheduleTick(p, level.getFluidState(p).getType(), 5);
  }

  private FiniteWater() {}
}
