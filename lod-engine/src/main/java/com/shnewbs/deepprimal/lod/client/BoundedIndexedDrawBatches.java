package com.shnewbs.deepprimal.lod.client;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Render-thread scratch for synchronous indexed-draw API batches. No call may
 * retain more than its declared count of draw references (or the hard cap).
 * The caller submits the list synchronously and closes it in a finally block.
 * These are API batches, not GPU multi-draw indirect.
 */
final class BoundedIndexedDrawBatches<T> {
  private final int maxBatch;
  private LimitedDrawList<T> scratch;
  private boolean inBatch;

  BoundedIndexedDrawBatches(int maxBatch) {
    if (maxBatch < 1) throw new IllegalArgumentException("indexed draw batch limit");
    this.maxBatch = maxBatch;
  }

  /** Start a batch with a hard checked record budget, even if a caller miscounts. */
  ArrayList<T> begin(int expectedCount) {
    if (expectedCount < 1 || expectedCount > maxBatch)
      throw new IllegalArgumentException("indexed draw batch count");
    if (inBatch) throw new IllegalStateException("nested indexed submission");
    if (scratch == null) scratch = new LimitedDrawList<>(maxBatch);
    else scratch.clear();
    scratch.activate(expectedCount);
    inBatch = true;
    return scratch;
  }

  /** Scrub all draw references even when native submission or packing throws. */
  void end() {
    if (!inBatch) throw new IllegalStateException("no indexed submission");
    scratch.clear();
    scratch.deactivate();
    inBatch = false;
  }

  int retainedReferences() { return scratch == null ? 0 : scratch.size(); }

  int reservedSlots() { return scratch == null ? 0 : maxBatch; }

  void reset() {
    if (inBatch) throw new IllegalStateException("reset during indexed submission");
    scratch = null;
  }

  /** ArrayList compatibility is required by the synchronous RenderPass API. */
  private static final class LimitedDrawList<E> extends ArrayList<E> {
    @Serial private static final long serialVersionUID = 1L;
    private final int maximum;
    private int permitted;
    private boolean active;

    LimitedDrawList(int maximum) {
      super(maximum);
      this.maximum = maximum;
    }

    void activate(int count) {
      permitted = count;
      active = true;
    }

    void deactivate() {
      active = false;
      permitted = 0;
    }

    private void checkGrowth(int additional) {
      if (!active) throw new IllegalStateException("indexed batch is closed");
      if (additional > permitted - size() || additional > maximum - size())
        throw new IllegalStateException("indexed draw record budget exceeded");
    }

    @Override public boolean add(E draw) {
      checkGrowth(1);
      return super.add(draw);
    }

    @Override public void add(int index, E draw) {
      checkGrowth(1);
      super.add(index, draw);
    }

    @Override public boolean addAll(Collection<? extends E> draws) {
      checkGrowth(draws.size());
      return super.addAll(draws);
    }

    @Override public boolean addAll(int index, Collection<? extends E> draws) {
      checkGrowth(draws.size());
      return super.addAll(index, draws);
    }

    @Override public void ensureCapacity(int capacity) {
      if (capacity > maximum) throw new IllegalArgumentException("indexed batch capacity");
      super.ensureCapacity(capacity);
    }
  }
}
