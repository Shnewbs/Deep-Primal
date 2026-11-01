package com.shnewbs.deepprimal.core;

/** Fixed-size primitive ring; no allocation on a server tick. */
public final class TickWindow {
    private final long[] nanos;
    private int next;
    private int count;
    public TickWindow(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Empty tick window");
        nanos = new long[capacity];
    }
    public void record(long duration) {
        // A negative elapsed time is invalid telemetry, not a zero-duration tick.
        // Drop it without advancing the ring; never fail the live server tick callback.
        if (duration < 0) return;
        nanos[next] = duration;
        next = (next + 1) % nanos.length;
        count = Math.min(count + 1, nanos.length);
    }
    public int count() { return count; }
    public SampleStats snapshot() {
        if (count == 0) return new SampleStats(0, 0, 0, 0, 0);
        return SampleStats.of(java.util.Arrays.copyOf(nanos, count));
    }
    public void clear() { next = 0; count = 0; }
}
