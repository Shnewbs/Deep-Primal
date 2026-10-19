package com.shnewbs.deepprimal.core;

import java.util.LinkedHashMap;
import java.util.function.ToLongFunction;

/** Payload-byte budget plus an entry ceiling. Callers retain ownership of returned values. */
public final class ByteLru<K,V> {
    private final LinkedHashMap<K,V> entries = new LinkedHashMap<>(16, .75f, true);
    private final long limit;
    private final int maximum;
    private final ToLongFunction<V> weigh;
    private long bytes, hits;
    public ByteLru(long limit, int maximum, ToLongFunction<V> weigh) {
        if (limit < 0 || maximum < 0) throw new IllegalArgumentException();
        this.limit = limit; this.maximum = maximum; this.weigh = weigh;
    }
    public V get(K key) { V value = entries.get(key); if (value != null) hits++; return value; }
    public void put(K key, V value) {
        long size = weigh.applyAsLong(value);
        if (size < 0) throw new IllegalArgumentException("Negative payload size");
        V old = entries.remove(key); if (old != null) bytes -= weigh.applyAsLong(old);
        if (size > limit || maximum == 0) return;
        entries.put(key, value); bytes += size;
        while (bytes > limit || entries.size() > maximum) {
            var it = entries.entrySet().iterator(); bytes -= weigh.applyAsLong(it.next().getValue()); it.remove();
        }
    }
    public long bytes() { return bytes; }
    public long hits() { return hits; }
    public int size() { return entries.size(); }
    public void clear() { entries.clear(); bytes = hits = 0; }
}
