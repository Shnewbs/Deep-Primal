package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/** Production-excluded pure Java regression; compares the exact old stable sort. */
public final class LodEvictionHeapChecks {
  private static long checks;
  private LodEvictionHeapChecks() {}

  private static void require(boolean yes, String message) {
    checks++;
    if (!yes) throw new AssertionError(message);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static List<Long> oldOrder(LinkedHashMap<Long, Integer> cache,
      Long keep, double cx, double cz) {
    var list = new ArrayList<>(cache.keySet());
    list.sort(Comparator.<Long>comparingInt(k -> keep != null && k.longValue() == keep ? 1 : 0)
        .thenComparing(Comparator.comparingDouble((Long k) -> {
          double dx = (int) (k >> 32) * 16. + 8 - cx;
          double dz = (int) (long) k * 16. + 8 - cz;
          return dx * dx + dz * dz;
        }).reversed()));
    return list;
  }

  private static void verify(LinkedHashMap<Long, Integer> cache, Long keep,
      double cx, double cz) {
    var original = new ArrayList<>(cache.keySet());
    List<Long> reference = oldOrder(cache, keep, cx, cz);
    var heap = new LodEvictionHeap(cache.keySet(), keep, cx, cz);
    for (int i = 0; i < reference.size(); i++) {
      require(heap.hasNext(), "premature exhaustion " + i);
      require(heap.next() == reference.get(i), "victim differs at " + i
          + " expected=" + reference.get(i));
    }
    require(!heap.hasNext(), "not exhausted");
    require(original.equals(new ArrayList<>(cache.keySet())), "input map mutated");
    try {
      heap.next();
      throw new AssertionError("accepted empty heap");
    } catch (NoSuchElementException expected) {
      checks++;
    }
  }

  public static void main(String[] ignored) {
    verify(new LinkedHashMap<>(), null, 0, 0);
    LinkedHashMap<Long, Integer> cache = new LinkedHashMap<>(64, .75f, true);
    cache.put(key(1, 0), 1);
    cache.put(key(0, 1), 2);
    cache.put(key(-1, 0), 3);
    cache.put(key(0, -1), 4);
    cache.put(key(0, 0), 5);
    verify(cache, null, 8, 8);
    verify(cache, key(0, -1), 8, 8);
    cache.get(key(1, 0)); // stable ties follow access-order map iteration
    verify(cache, key(1, 0), 8, 8);
    verify(cache, key(1, 0), Double.MAX_VALUE, -Double.MAX_VALUE);
    Random random = new Random(0x5B34FF28);
    int[] extreme = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1000000, -1,
        0, 1, 1000000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
    for (int run = 0; run < 1200; run++) {
      cache.clear();
      int count = 1 + random.nextInt(run % 3 == 0 ? 81 : 256);
      while (cache.size() < count) {
        int x = run % 3 == 0 ? extreme[random.nextInt(extreme.length)]
            : random.nextInt(1000) - 500;
        int z = run % 3 == 0 ? extreme[random.nextInt(extreme.length)]
            : random.nextInt(1000) - 500;
        cache.put(key(x, z), run);
      }
      int move = random.nextInt(cache.size());
      long chosen = new ArrayList<>(cache.keySet()).get(move);
      cache.get(chosen);
      Long keep = run % 5 == 0 ? Long.valueOf(chosen)
          : run % 5 == 1 ? Long.valueOf(987654321L) : null;
      double cx = run % 7 == 0 ? Double.MAX_VALUE
          : run % 7 == 1 ? -Double.MAX_VALUE : random.nextInt(2000) - 1000;
      double cz = run % 7 == 2 ? Double.MAX_VALUE
          : run % 7 == 3 ? -Double.MAX_VALUE : random.nextInt(2000) - 1000;
      verify(cache, keep, cx, cz);
    }
    cache.clear();
    for (int i = 0; i < 4096; i++) cache.put(key(i - 2048, (i * 131) % 2048), i);
    verify(cache, key(0, 0), 12.5, -17.25);
    System.out.println("LOD_EVICTION_HEAP_PASS assertions=" + checks);
  }
}
