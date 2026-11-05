package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in contract for the real bounded drain-request FIFO and overflow wake path. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class DrainPendingChecks {
  public DrainPendingChecks() { NeoForge.EVENT_BUS.register(this); }

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String record = System.getenv("DEEPPRIMAL_DRAIN_QUEUE_RECORD");
    if (record == null) return;
    var level = event.getServer().getLevel(Level.END);
    for (int c : new int[] {-1, 0, 1}) level.getChunk(c, c);
    var cursor = new BlockPos.MutableBlockPos(-7, 60, -7);
    WaterRuntime.activate(level, cursor);
    var bf = WaterRuntime.class.getDeclaredField("BUDGETS"); bf.setAccessible(true);
    Object budget = ((Map<?, ?>) bf.get(null)).get(level);
    var pf = budget.getClass().getDeclaredField("drainPending"); pf.setAccessible(true);
    var af = budget.getClass().getDeclaredField("active"); af.setAccessible(true);
    var pending = (Collection<?>) pf.get(budget);
    var active = (Collection<?>) af.get(budget);
    active.clear();
    var defer = WaterRuntime.class.getDeclaredMethod("deferDrain", ServerLevel.class,
        BlockPos.class, budget.getClass()); defer.setAccessible(true);
    var expected = new LinkedHashSet<Long>();
    for (int c = -1; c <= 0; c++)
      for (int y = 32; y < 160; y++)
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++) {
            cursor.set(c * 16 + x, y, c * 16 + z);
            expected.add(cursor.asLong()); defer.invoke(null, level, cursor, budget);
          }
    cursor.set(123, 456, 789); // Retained work must be coordinate snapshots.
    check(pending.size() == 65536 && active.isEmpty(), "Exact pending cap/admission");
    var order = expected.iterator();
    for (Object entry : pending)
      check(((Number) entry).longValue() == order.next(), "Pending FIFO/mutable/negative coordinates");
    check(!order.hasNext(), "All pending requests retained");
    defer.invoke(null, level, new BlockPos(-16, 32, -16), budget);
    check(active.isEmpty() && pending.size() == 65536, "Duplicate at cap does not overflow/reorder");
    var first = new BlockPos(16, 64, 16);
    defer.invoke(null, level, first, budget);
    check(active.contains(first.asLong()) && pending.size() == 65536,
        "Pending overflow preserves its active wake");
    for (long key : expected) WaterRuntime.activate(level, BlockPos.of(key));
    var overflow = new BlockPos(17, 64, 16);
    defer.invoke(null, level, overflow, budget);
    check(active.size() == 65536 && pending.size() == 65536
        && level.getFluidTicks().hasScheduledTick(overflow, EngineRegistry.WATER.get()),
        "Two full queues preserve a native scheduled wake");
    pending.clear(); active.clear();
    String result = "DRAIN_PENDING_NATIVE_PASS cells=65536 fifo=true mutableSnapshots=true"
        + " negativeCoordinates=true duplicateAtCapacity=true activeOverflow=true scheduledOverflow=true\n";
    Files.writeString(Path.of(record), result); System.out.print(result);
    event.getServer().halt(false);
  }
}
