package com.shnewbs.deepprimal.core;

import java.util.LinkedHashMap;
import java.util.function.ToLongFunction;

/** Payload-byte budget plus an entry ceiling. Callers retain ownership of returned values. */
public final class ByteLru<K,V> {
    /** Snapshot the admission weight: cached values may be mutable after put(). */
    private record Entry<V>(V value, long weight) {}
    private final LinkedHashMap<K,Entry<V>> entries = new LinkedHashMap<>(16, .75f, true);
    private final long limit;
    private final int maximum;
    private final ToLongFunction<V> weigh;
    private long bytes, hits;
    public ByteLru(long limit, int maximum, ToLongFunction<V> weigh) {
        if (limit < 0 || maximum < 0) throw new IllegalArgumentException();
        this.limit = limit; this.maximum = maximum; this.weigh = weigh;
    }
    public V get(K key) {
        Entry<V> entry = entries.get(key);
        V value = entry == null ? null : entry.value();
        if (value != null) hits++;
        return value;
    }
    public void put(K key, V value) {
        long size = weigh.applyAsLong(value);
        if (size < 0) throw new IllegalArgumentException("Negative payload size");
        Entry<V> old = entries.remove(key);
        if (old != null) bytes -= old.weight();
        if (size > limit || maximum == 0) return;
        // Reserve room before adding: bytes + size can overflow even when each
        // payload is individually within the configured nonnegative long budget.
        while (bytes > limit - size || entries.size() >= maximum) {
            var it = entries.entrySet().iterator();
            bytes -= it.next().getValue().weight();
            it.remove();
        }
        entries.put(key, new Entry<>(value, size));
        bytes += size;
    }
    public long bytes() { return bytes; }
    public long hits() { return hits; }
    public int size() { return entries.size(); }
    public void clear() { entries.clear(); bytes = hits = 0; }
}
