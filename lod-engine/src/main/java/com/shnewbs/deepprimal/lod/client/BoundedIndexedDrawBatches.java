package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;

/**
 * Render-thread scratch for synchronous indexed-draw API batches. Each nonempty
 * batch uses at most the reserved number of draw references; the caller submits
 * it before releasing the scratch in a finally block. This bounds retained
 * CPU handles without dropping a visible part or reordering transparency.
 *
 * <p>The returned list must not escape the synchronous RenderPass call. These
 * API batches are not GPU multi-draw indirect or a measured reduction in draws.
 */
final class BoundedIndexedDrawBatches<T> {
  private final int maxBatch;
  private ArrayList<T> scratch;
  private boolean inBatch;

  BoundedIndexedDrawBatches(int maxBatch) {
    if (maxBatch < 1) throw new IllegalArgumentException("indexed draw batch limit");
    this.maxBatch = maxBatch;
  }

  /** Start one bounded batch; the caller adds no more than expectedCount records. */
  ArrayList<T> begin(int expectedCount) {
    if (expectedCount < 1 || expectedCount > maxBatch)
      throw new IllegalArgumentException("indexed draw batch count");
    if (inBatch) throw new IllegalStateException("nested indexed submission");
    if (scratch == null) scratch = new ArrayList<>(maxBatch);
    else scratch.clear();
    inBatch = true;
    return scratch;
  }

  /** Scrub all draw references even when native submission or packing throws. */
  void end() {
    if (!inBatch) throw new IllegalStateException("no indexed submission");
    scratch.clear();
    inBatch = false;
  }

  int retainedReferences() { return scratch == null ? 0 : scratch.size(); }

  int reservedSlots() { return scratch == null ? 0 : maxBatch; }

  void reset() {
    if (inBatch) throw new IllegalStateException("reset during indexed submission");
    scratch = null;
  }
}
