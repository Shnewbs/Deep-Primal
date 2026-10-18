package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Dependency-free differential checks for hot-path math and bounded memory behavior. */
public final class CoreChecks {
    public static void main(String[] args) {
        Random random = new Random(20261018L);
        int checked = 0;
        for (int lower : new int[]{-384, -128}) {
            for (int i = 0; i < 100_000; i++) {
                // Quarter-block heights exercise negative coordinates and exact cell boundaries.
                double h = (random.nextInt(6000) - 2000) / 4.0;
                int expected = lower;
                for (int y = 632; y >= lower; y -= 8) if (h > y) { expected = y; break; }
                int actual = ColumnHeight.surfaceY(h, lower, 632, 8);
                if (actual != expected) throw new AssertionError(h + ": " + actual + " != " + expected);
                checked++;
            }
        }
        BoundedLru<Integer, Integer> cache = new BoundedLru<>(3);
        cache.put(1, 1); cache.put(2, 2); cache.put(3, 3);
        check(cache.get(1) == 1, "cache hit");
        cache.put(4, 4);
        check(cache.get(2) == null && cache.size() == 3, "LRU eviction");
        for (int i = 0; i < 100_000; i++) cache.put(i, i);
        check(cache.size() == 3, "cache capacity under stress");
        cache.clear(); check(cache.size() == 0 && cache.hits() == 0, "reload reset");
        BoundedLru<Integer, Integer> disabled = new BoundedLru<>(0);
        disabled.put(1, 1); check(disabled.size() == 0, "disabled cache");
        TickWindow ticks = new TickWindow(3);
        ticks.record(1_000_000); ticks.record(2_000_000); ticks.record(3_000_000); ticks.record(4_000_000);
        check(ticks.count() == 3 && ticks.snapshot().medianMs() == 3, "tick ring rollover");
        ticks.clear(); check(ticks.snapshot().count() == 0, "tick reset");
        System.out.println("PASS: " + checked + " differential surface cases, bounded-cache stress, tick-ring rollover.");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
