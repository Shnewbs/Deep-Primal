package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Render-thread scratch for synchronous indexed-draw API submissions. A large visible
 * LOD set is split into ordered, bounded batches without retaining references to
 * GPU-owned draw records after a submission returns or fails. Each batch is issued
 * before the same list is reused; the consumer must not retain the passed list.
 *
 * <p>Multiple drawMultipleIndexed calls are still ordinary API batches: they are
 * not GPU multi-draw indirect and imply no hardware draw-call reduction.
 */
final class BoundedIndexedDrawBatches<T> {
  private final int maxBatch;
  private ArrayList<T> scratch;
  private boolean submitting;

  BoundedIndexedDrawBatches(int maxBatch) {
    if (maxBatch < 1) throw new IllegalArgumentException("indexed draw batch limit");
    this.maxBatch = maxBatch;
  }

  /** Prepares each draw once, in source order, and submits nonempty contiguous batches. */
  void submit(int count, IntFunction<? extends T> prepare, Consumer<? super List<T>> issue) {
    if (count < 0) throw new IllegalArgumentException("negative indexed draw count");
    Objects.requireNonNull(prepare, "prepare");
    Objects.requireNonNull(issue, "issue");
    if (submitting) throw new IllegalStateException("nested indexed submission");
    if (count == 0) return;
    if (scratch == null) scratch = new ArrayList<>(maxBatch);
    submitting = true;
    try {
      int index = 0;
      while (index < count) {
        scratch.clear();
        int end = index + Math.min(maxBatch, count - index);
        while (index < end) scratch.add(Objects.requireNonNull(prepare.apply(index++), "draw"));
        // RenderPass consumes this collection synchronously while the pass is open.
        issue.accept(scratch);
      }
    } finally {
      // Prevent stale draw records from pinning GPU buffers through edits,
      // batch-mode changes, failed submissions or idle frames.
      scratch.clear();
      submitting = false;
    }
  }

  int retainedReferences() { return scratch == null ? 0 : scratch.size(); }

  int reservedSlots() { return scratch == null ? 0 : maxBatch; }

  void reset() {
    if (submitting) throw new IllegalStateException("reset during indexed submission");
    scratch = null;
  }
}
