package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in native FIFO contract and warmed admission allocation measurement on either revision. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class ActiveQueueChecks {
  public ActiveQueueChecks() { NeoForge.EVENT_BUS.register(this); }

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String record = System.getenv("DEEPPRIMAL_ACTIVE_QUEUE_RECORD");
    if (record == null) return;
    var level = event.getServer().getLevel(Level.END);
    for (int c : new int[] {-1, 0, 1}) level.getChunk(c, c);
    var cursor = new BlockPos.MutableBlockPos(7, 60, 7);
    WaterRuntime.activate(level, cursor);
    cursor.set(-3, 70, -3);
    WaterRuntime.activate(level, cursor);
    cursor.set(7, 60, 7);
    WaterRuntime.activate(level, cursor);
    var expected = new LinkedHashSet<Long>();
    expected.add(cursor.asLong());
    expected.add(BlockPos.asLong(-3, 70, -3));
    for (int c = -1; c <= 0; c++)
      for (int y = 32; y < 160; y++)
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++) {
            cursor.set(c * 16 + x, y, c * 16 + z);
            WaterRuntime.activate(level, cursor);
            expected.add(cursor.asLong());
          }
    var budgetsField = WaterRuntime.class.getDeclaredField("BUDGETS");
    budgetsField.setAccessible(true);
    Object budget = ((Map<?, ?>) budgetsField.get(null)).get(level);
    var activeField = budget.getClass().getDeclaredField("active");
    activeField.setAccessible(true);
    var queue = (Collection<?>) activeField.get(budget);
    if (queue.size() != 65536) throw new AssertionError("Native queue cap/admission: " + queue.size());
    var order = expected.iterator();
    for (Object entry : queue) {
      long key = entry instanceof BlockPos p ? p.asLong() : ((Number) entry).longValue();
      if (key != order.next()) throw new AssertionError("FIFO order/mutable snapshot changed");
    }
    if (order.hasNext()) throw new AssertionError("Missing queue entries");
    var overflow = new BlockPos(16, 64, 16);
    WaterRuntime.activate(level, overflow);
    if (queue.size() != 65536 || !level.getFluidTicks().hasScheduledTick(overflow, EngineRegistry.WATER.get()))
      throw new AssertionError("Overflow must retain its native scheduled wake");
    WaterRuntime.activate(level, new BlockPos(7, 60, 7));
    if (level.getFluidTicks().hasScheduledTick(new BlockPos(7, 60, 7), EngineRegistry.WATER.get()))
      throw new AssertionError("Duplicate at capacity must not create overflow work");
    // Capacity was warmed above. Attribute allocations only to four identical admission passes,
    // excluding fixture positions, reference ordering, reflection and chunk creation.
    var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    if (!bean.isThreadAllocatedMemorySupported()) throw new AssertionError("Allocation counters unavailable");
    bean.setThreadAllocatedMemoryEnabled(true);
    long thread = Thread.currentThread().threadId();
    long before = bean.getThreadAllocatedBytes(thread);
    for (int pass = 0; pass < 4; pass++) {
      queue.clear();
      for (int c = -1; c <= 0; c++)
        for (int y = 32; y < 160; y++)
          for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++)
              WaterRuntime.activate(level, cursor.set(c * 16 + x, y, c * 16 + z));
      if (queue.size() != 65536) throw new AssertionError("Repeated admission lost cells");
    }
    long allocated = bean.getThreadAllocatedBytes(thread) - before;
    queue.clear();
    String result = "ACTIVE_QUEUE_NATIVE_PASS cells=65536 admissions=262144 fifo=true mutableSnapshots=true"
        + " negativeCoordinates=true duplicateAtCapacity=true scheduledOverflow=true allocatedBytes="
        + allocated + " queue=" + queue.getClass().getName() + "\n";
    Files.writeString(Path.of(record), result);
    System.out.print(result);
    event.getServer().halt(false);
  }
}
