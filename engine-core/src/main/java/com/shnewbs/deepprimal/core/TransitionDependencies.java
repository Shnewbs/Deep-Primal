package com.shnewbs.deepprimal.core;

/** Exact, bounded identity tracking for geometry inputs; no hashes or per-frame collections. */
public final class TransitionDependencies {
    private final Object[] sources;
    private long x = Long.MIN_VALUE, z;
    private int radius, count, next;
    // A coordinate sentinel is not a substitute for an initialized capture: every long is valid.
    private boolean initialized, changed;
    public TransitionDependencies(int capacity) { sources = new Object[capacity]; }
    public void begin(long x, long z, int radius) {
        changed = !initialized || this.x != x || this.z != z || this.radius != radius;
        initialized = true;
        this.x = x; this.z = z; this.radius = radius; next = 0;
    }
    public void include(Object source) {
        if (next == sources.length) {
            // An incomplete capture cannot be used as a baseline on retry.
            initialized = false;
            throw new IllegalStateException("Transition dependency limit");
        }
        if (sources[next] != source) { changed = true; sources[next] = source; }
        next++;
    }
    public boolean end() {
        if (next != count) changed = true;
        java.util.Arrays.fill(sources, next, Math.max(next, count), null);
        count = next;
        return changed;
    }
    public void clear() { java.util.Arrays.fill(sources, null); count = next = 0; x = Long.MIN_VALUE; initialized = false; }
}
