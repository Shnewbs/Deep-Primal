package com.shnewbs.deepprimal.lod.client;

import java.util.List;
import java.util.Objects;

/**
 * Incremental committed GPU payload accounting for a partially uploaded LOD page.
 * Only a successful append to the owning pending-level list updates the total.
 * All calls occur on the render thread; this is not a concurrent counter.
 *
 * <p>The caller provides the actual allocated GPU buffer size, not the estimated
 * source-mesh size. Accepted GPU buffers have fixed allocation sizes for their
 * lifetime; a rejected/failed append never changes the accounting value.
 */
final class PendingGpuBytes {
  private long committed;

  long bytes() {
    return committed;
  }

  <T> void append(List<T> target, T gpuResource, long allocatedBytes) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(gpuResource, "gpuResource");
    if (allocatedBytes < 0 || allocatedBytes > Long.MAX_VALUE - committed)
      throw new IllegalArgumentException("invalid pending GPU allocation size");
    // ArrayList.add can fail (e.g., OOME). Commit only after it succeeds.
    target.add(gpuResource);
    committed += allocatedBytes;
  }
}
