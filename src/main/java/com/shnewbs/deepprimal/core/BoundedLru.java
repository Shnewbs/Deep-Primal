package com.shnewbs.deepprimal.core;

import java.util.LinkedHashMap;

/** Single-threaded LRU with an explicit upper bound; no world/chunk objects are stored here. */
public final class BoundedLru<K, V> {
    private final int capacity;
    private final LinkedHashMap<K, V> entries = new LinkedHashMap<>(16, .75f, true);
    private long hits;
    private long misses;
    public BoundedLru(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Negative cache capacity");
        this.capacity = capacity;
    }
    public V get(K key) {
        V value = entries.get(key);
        if (value == null) misses++; else hits++;
        return value;
    }
    public void put(K key, V value) {
        if (capacity == 0) return;
        entries.put(key, value);
        if (entries.size() > capacity) entries.pollFirstEntry();
    }
    public void clear() { entries.clear(); hits = 0; misses = 0; }
    public int size() { return entries.size(); }
    public int capacity() { return capacity; }
    public long hits() { return hits; }
    public long misses() { return misses; }
}
