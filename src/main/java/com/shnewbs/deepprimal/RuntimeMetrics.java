package com.shnewbs.deepprimal;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicInteger;

/** Lightweight cumulative work counters; each hook records one aggregate per operation. */
public final class RuntimeMetrics {
    public static final AtomicInteger MAX_SEND_BATCH = new AtomicInteger();
    public static final LongAdder SEND_SELECTIONS = new LongAdder();
    public static final LongAdder SEND_CANDIDATES = new LongAdder();
    public static final LongAdder SEND_NANOS = new LongAdder();
    public static final LongAdder VIEW_COLUMNS = new LongAdder();
    public static final LongAdder VIEW_SECTIONS_MOVED = new LongAdder();
    private RuntimeMetrics() {}
    public static void reset() {
        MAX_SEND_BATCH.set(0); SEND_SELECTIONS.reset(); SEND_CANDIDATES.reset(); SEND_NANOS.reset();
        VIEW_COLUMNS.reset(); VIEW_SECTIONS_MOVED.reset();
    }
}
