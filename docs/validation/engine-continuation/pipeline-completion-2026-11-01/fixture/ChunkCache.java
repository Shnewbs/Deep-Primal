package com.shnewbs.deepprimal.core;
import java.util.HashMap;
import java.util.Map;

/** Isolated cache double that can reject insertion after materialization. */
public class ChunkCache {
  private final Map<Object, Object> items = new HashMap<>();
  public boolean reject;
  @SuppressWarnings("unchecked")
  public synchronized <T> T get(Object key) { return (T) items.get(key); }
  public synchronized void put(Object key, Object value, long weight) {
    if (reject) throw new IllegalStateException("cache unavailable");
    items.put(key, value);
  }
  public synchronized void clear() { items.clear(); }
}
