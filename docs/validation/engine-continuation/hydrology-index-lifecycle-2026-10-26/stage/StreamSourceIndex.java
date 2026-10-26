package com.shnewbs.deepprimal.engine;

import java.util.*;

/** Server-owned headwater order, maintained at source lifecycle changes rather than each tick. */
final class StreamSourceIndex<T> implements Iterable<Map.Entry<Long,T>> {
  private record Node<T>(long key, T value, double height, long sequence) implements Map.Entry<Long,T> {
    public Long getKey() { return key; }
    public T getValue() { return value; }
    public T setValue(T ignored) { throw new UnsupportedOperationException(); }
  }
  private final NavigableMap<Long,Node<T>> byKey = new TreeMap<>();
  private final NavigableSet<Node<T>> ordered = new TreeSet<>(
      Comparator.<Node<T>>comparingDouble(n -> -n.height()).thenComparingLong(Node::sequence));
  private long sequence;
  void put(long key, T value, double height) {
    Objects.requireNonNull(value);
    if (!Double.isFinite(height)) throw new IllegalArgumentException("Non-finite headwater");
    var old = byKey.get(key);
    if (old != null && old.value() == value && Double.compare(old.height(), height) == 0) return;
    long order = old == null ? Math.incrementExact(sequence) : old.sequence();
    var next = new Node<>(key, value, height, order);
    if (old != null) ordered.remove(old); else sequence = order;
    byKey.put(key, next); ordered.add(next);
  }
  void remove(long key) { var old = byKey.remove(key); if (old != null) ordered.remove(old); }
  /** A retired cursor must not invalidate a newer generation at the same chunk key. */
  void remove(long key, T expected) {
    var old = byKey.get(key);
    if (old != null && old.value() == expected) remove(key);
  }
  boolean contains(long key) { return byKey.containsKey(key); }
  int size() { return byKey.size(); }
  public Iterator<Map.Entry<Long,T>> iterator() {
    var source = ordered.iterator();
    return new Iterator<>() {
      public boolean hasNext() { return source.hasNext(); }
      public Map.Entry<Long,T> next() { return source.next(); }
    };
  }
}
