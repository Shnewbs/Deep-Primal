package com.shnewbs.deepprimal.core;

/** Budget between indivisible native section builds. Player edits always bypass it. */
public final class SyncRebuildBudget {
    private int count, maximum;
    private long elapsed, limit;
    public void reset(int maximum, long nanoseconds) {
        this.maximum = maximum; limit = nanoseconds; count = 0; elapsed = 0;
    }
    public boolean allows(boolean playerEdit) { return playerEdit || count < maximum && elapsed < limit; }
    public void completed(long nanoseconds) {
        // A malformed/very large native duration must not wrap the accounting
        // negative and re-enable ordinary synchronous builds in the same frame.
        if (count < Integer.MAX_VALUE) count++;
        long duration = Math.max(0L, nanoseconds);
        elapsed = duration > Long.MAX_VALUE - elapsed ? Long.MAX_VALUE : elapsed + duration;
    }
}
