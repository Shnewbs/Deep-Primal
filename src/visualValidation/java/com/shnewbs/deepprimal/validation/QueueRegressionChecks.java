package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.performance.DistanceRuntime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Exercises transformed native queues against an independent ordered model, excluded from the JAR.
 */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class QueueRegressionChecks {
  private ProbeQueue retained;
  private int retainedCount;

  public QueueRegressionChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) throws Exception {
    String mode = System.getProperty("deepprimal.queueValidation");
    if (mode == null) return;
    ProbeQueue queue = new ProbeQueue();
    if (mode.equals("before")) {
      try {
        queue.add(() -> {}, 1L, 94);
      } catch (IndexOutOfBoundsException expected) {
        System.out.println(
            "QUEUE_BASELINE_REPRODUCED capacity=" + queue.capacity() + " " + expected);
        return;
      }
      throw new AssertionError("Unfixed queue unexpectedly accepted priority 94");
    }
    int expected =
        Math.max(
            ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT, DistanceRuntime.trackingCapacity() + 3);
    check(queue.capacity() == expected, "native bucket allocation matches tracker/sentinel");
    check(!queue.hasWork() && queue.pop() == null, "empty sentinel");
    for (int i = expected - 1; i >= 0; i--) queue.add(() -> {}, i + 100L, i);
    for (int i = 0; i < expected; i++)
      check(queue.pop().chunkPos() == i + 100L, "full-range priority ordering");
    check(!queue.hasWork(), "drains highest bucket");
    queue.add(() -> {}, 7L, expected - 1);
    queue.move(expected - 1, 7L, 0);
    check(queue.pop().chunkPos() == 7L && !queue.hasWork(), "reprioritize highest to lowest");
    queue.add(() -> {}, 8L, 0);
    queue.move(0, 8L, expected - 1);
    queue.clear(8L, true);
    check(!queue.hasWork(), "cancel highest bucket");

    var model = new TreeMap<Integer, LinkedHashMap<Long, List<Integer>>>();
    var observed = new ArrayList<Integer>();
    Random random = new Random(20261018);
    int nextId = 0;
    for (int i = 0; i < 5000; i++) {
      int operation = random.nextInt(4),
          priority = random.nextInt(expected),
          newPriority = random.nextInt(expected);
      long key = random.nextInt(32);
      if (operation == 0) {
        int id = nextId++;
        queue.add(() -> observed.add(id), key, priority);
        model
            .computeIfAbsent(priority, k -> new LinkedHashMap<>())
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(id);
      } else if (operation == 1) {
        queue.move(priority, key, newPriority);
        var bucket = model.get(priority);
        if (bucket != null) {
          List<Integer> tasks = bucket.remove(key);
          if (bucket.isEmpty()) model.remove(priority);
          if (tasks != null)
            model
                .computeIfAbsent(newPriority, k -> new LinkedHashMap<>())
                .computeIfAbsent(key, k -> new ArrayList<>())
                .addAll(tasks);
        }
      } else if (operation == 2) {
        boolean cancel = random.nextBoolean();
        queue.clear(key, cancel);
        if (cancel) {
          for (var bucket : model.values()) bucket.remove(key);
          model.values().removeIf(Map::isEmpty);
        }
      } else pop(queue, model, observed);
      check(queue.hasWork() == !model.isEmpty(), "work state after operation " + i);
    }
    while (!model.isEmpty()) pop(queue, model, observed);
    check(queue.pop() == null && !queue.hasWork(), "final empty sentinel");
    retained = queue;
    retainedCount = expected;
    System.out.println(
        "QUEUE_REGRESSION_PASS tracker="
            + DistanceRuntime.trackingCapacity()
            + " buckets="
            + expected
            + " operations=5000 nativeCount="
            + ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void stopped(ServerStoppedEvent event) {
    if (retained == null) return;
    check(DistanceRuntime.trackingCapacity() == 32, "runtime reset happened first");
    retained.add(() -> {}, 4L, retainedCount - 1);
    check(
        retained.pop().chunkPos() == 4L && !retained.hasWork(),
        "queue retains its capacity after server stop");
    retained = null;
    System.out.println("QUEUE_STOP_RESET_PASS buckets=" + retainedCount);
  }

  private static void pop(
      ProbeQueue queue,
      TreeMap<Integer, LinkedHashMap<Long, List<Integer>>> model,
      List<Integer> observed) {
    var actual = queue.pop();
    if (model.isEmpty()) {
      check(actual == null, "empty pop");
      return;
    }
    var first = model.firstEntry();
    var firstChunk = first.getValue().entrySet().iterator().next();
    check(
        actual != null && actual.chunkPos() == firstChunk.getKey(),
        "nearest bucket and FIFO chunk order");
    observed.clear();
    actual.tasks().forEach(Runnable::run);
    check(observed.equals(firstChunk.getValue()), "task ordering and preservation");
    first.getValue().remove(firstChunk.getKey());
    if (first.getValue().isEmpty()) model.remove(first.getKey());
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static final class ProbeQueue extends ChunkTaskPriorityQueue {
    ProbeQueue() {
      super("deepprimal validation");
    }

    void add(Runnable task, long pos, int priority) {
      submit(task, pos, priority);
    }

    void move(int old, long pos, int priority) {
      resortChunkTasks(old, ChunkPos.unpack(pos), priority);
    }

    void clear(long pos, boolean cancel) {
      release(pos, cancel);
    }

    int capacity() throws Exception {
      var field = ChunkTaskPriorityQueue.class.getDeclaredField("queuesPerPriority");
      field.setAccessible(true);
      return ((List<?>) field.get(this)).size();
    }
  }
}
