package com.shnewbs.deepprimal.lod.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * Standalone, source-pinned regression for the renderer disk-index owner.
 * Compile alongside the two production LodDisk*.java files; no NeoForge or GPU needed.
 * A static PendingApply that accesses owner counters fails compilation before main().
 */
public final class LodDiskCounterCompileChecks {
  private static int assertions;

  private static void check(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason);
  }

  private static Object pending(LodDiskIndexService owner,
      List<Long> discard, List<Long> incoming) throws Exception {
    Class<?> type = Class.forName(
        "com.shnewbs.deepprimal.lod.client.LodDiskIndexService$PendingApply");
    Constructor<?> constructor = type.getDeclaredConstructor(
        LodDiskIndexService.class, List.class, List.class, HashMap.class, long.class);
    constructor.setAccessible(true);
    return constructor.newInstance(owner, discard, incoming, new HashMap<Long, Long>(), 0L);
  }

  private static boolean advance(Object pending, LinkedHashSet<Long> queue,
      LongConsumer forgotten) throws Exception {
    Method method = pending.getClass().getDeclaredMethod(
        "advance", LinkedHashSet.class, LongConsumer.class);
    method.setAccessible(true);
    return (boolean) method.invoke(pending, queue, forgotten);
  }

  public static void main(String[] args) throws Exception {
    LodDiskIndexService owner = new LodDiskIndexService();
    LinkedHashSet<Long> queue = new LinkedHashSet<>(List.of(1L, 2L));
    List<Long> forgotten = new ArrayList<>();
    Object first = pending(owner, List.of(1L), List.of(3L, 4L));
    check(advance(first, queue, forgotten::add), "bounded apply should finish");
    check(queue.equals(new LinkedHashSet<>(List.of(2L, 3L, 4L))), "ordered queue");
    check(forgotten.equals(List.of(1L)), "discard callback");
    var d = owner.diagnostics(true, queue.size());
    check(d.appliedRemovals() == 1L, "removed count");
    check(d.appliedAdds() == 2L, "added count");
    check(d.appliedSheds() == 0L, "no shed");
    owner.reset();
    d = owner.diagnostics(true, queue.size());
    check(d.appliedRemovals() == 0L && d.appliedAdds() == 0L
        && d.appliedSheds() == 0L, "reset counts");
    check(!owner.scanning() && !owner.started(), "restored accessors");

    LodDiskIndexService fullOwner = new LodDiskIndexService();
    LinkedHashSet<Long> full = new LinkedHashSet<>();
    for (long i = 0; i < LodDiskNearestScan.MAX_KEYS; i++) full.add(i);
    List<Long> shed = new ArrayList<>();
    Object second = pending(fullOwner, List.of(), List.of(8192L, 8191L));
    check(advance(second, full, shed::add), "full-queue apply");
    check(full.size() == LodDiskNearestScan.MAX_KEYS, "hard queue cap");
    check(full.contains(8192L) && !full.contains(8191L), "farthest shed");
    check(shed.equals(List.of(8191L)), "shed callback");
    d = fullOwner.diagnostics(true, full.size());
    check(d.appliedAdds() == 1L && d.appliedSheds() == 1L, "shed counters");
    fullOwner.reset();
    d = fullOwner.diagnostics(true, full.size());
    check(d.appliedAdds() == 0L && d.appliedSheds() == 0L, "shed reset");
    System.out.println("LOD_COUNTER_COMPILE_PASS assertions=" + assertions);
  }
}
