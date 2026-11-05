package com.shnewbs.deepprimal.engine;

import java.util.*;

/** Server-owned headwater order, maintained at source lifecycle changes rather than each tick. */
final class StreamSourceIndex<T> implements Iterable<Map.Entry<Long,T>> {
  private record Node<T>(long key, T value, double height, long sequence, Object retiredInPass) implements Map.Entry<Long,T> {
    public Long getKey() { return key; }
    public T getValue() { return value; }
    public T setValue(T ignored) { throw new UnsupportedOperationException(); }
  }
  private final NavigableMap<Long,Node<T>> byKey = new TreeMap<>();
  private final NavigableSet<Node<T>> ordered = new TreeSet<>(
      Comparator.<Node<T>>comparingDouble(n -> -n.height()).thenComparingLong(Node::sequence));
  private long sequence;
  private Object pass = new Object();
  private Object revision = new Object();
  /** End the survey/regular-visit registration window without scanning dormant entries. */
  void beginPass() { pass = new Object(); }
  /** Keep equal-height registration priority if a source returns before the next source pass. */
  void deactivate(long key) {
    var old = byKey.get(key);
    if (old == null || old.retiredInPass() != null) return;
    revision = new Object();
    ordered.remove(old);
    byKey.put(key, new Node<>(key, old.value(), old.height(), old.sequence(), pass));
  }
  void put(long key, T value, double height) {
    Objects.requireNonNull(value);
    if (!Double.isFinite(height)) throw new IllegalArgumentException("Non-finite headwater");
    var old = byKey.get(key);
    if (old != null && old.retiredInPass() == null && old.value() == value && Double.compare(old.height(), height) == 0) return;
    boolean retainOrder = old != null && (old.retiredInPass() == null || old.retiredInPass() == pass);
    long order = retainOrder ? old.sequence() : Math.incrementExact(sequence);
    var next = new Node<>(key, value, height, order, null);
    revision = new Object();
    if (old != null) ordered.remove(old);
    if (!retainOrder) sequence = order;
    byKey.put(key, next); ordered.add(next);
  }
  void remove(long key) {
    var old = byKey.remove(key);
    if (old != null) { revision = new Object(); ordered.remove(old); }
  }
  /** A retired cursor must not invalidate a newer generation at the same chunk key. */
  void remove(long key, T expected) {
    var old = byKey.get(key);
    if (old != null && old.value() == expected) remove(key);
  }
  boolean contains(long key) {
    var node = byKey.get(key);
    return node != null && (node.retiredInPass() == null || node.retiredInPass() == pass);
  }
  int size() { return ordered.size(); }
  /** A callback changing the index ends this pass; the next tick reopens current priority order.
   * No snapshot copy, queued mutation or retry loop is needed. Server-thread ownership is required.
   */
  final class Pass implements Iterator<Map.Entry<Long,T>> {
    private final Object expectedRevision = revision;
    private final Iterator<Node<T>> source = ordered.iterator();
    boolean valid() { return expectedRevision == revision; }
    public boolean hasNext() { return valid() && source.hasNext(); }
    public Map.Entry<Long,T> next() {
      if (!hasNext()) throw new NoSuchElementException();
      return source.next();
    }
  }
  public Pass iterator() { return new Pass(); }
}
