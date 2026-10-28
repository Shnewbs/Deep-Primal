package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.LongConsumer;

/**
 * Client-thread coordinator for the independent, entry-bounded disk-index worker.
 * One cursor is active at a time. No filesystem enumeration or result sorting
 * takes place on the client thread; active mesh captures stay on their worker.
 *
 * The caller owns the queue, edit guards, and deferred records. A completed
 * scan may replace far recovery keys, but any recovery added after the scan
 * began wins admission. Dropped keys remain on disk for future retargets.
 */
final class LodDiskIndexService {
  private LodDiskNearestScan.Cursor cursor;
  private boolean started;
  private int centerX, centerZ;

  /** Returns true only when a completed scan failed (never on transient latency). */
  boolean tick(Path folder, int cameraX, int cameraZ, LinkedHashSet<Long> queue,
      boolean enabled, LongConsumer forgotten) {
    if (!enabled || folder == null) {
      reset();
      return false;
    }
    boolean retarget = !started
        || Math.max(Math.abs((long) cameraX - centerX),
                    Math.abs((long) cameraZ - centerZ)) >= LodDiskNearestScan.RETARGET_CHUNKS;
    if (retarget && cursor != null) cursor.cancel();
    if (cursor != null && cursor.cancelled()) {
      // Do not accumulate retired directory threads during repeated teleports.
      if (!cursor.stopped()) return false;
      cursor = null;
    }
    if (retarget && cursor == null) {
      cursor = LodDiskNearestScan.start(folder, cameraX, cameraZ,
          LodDiskNearestScan.MAX_KEYS, new ArrayList<>(queue));
      centerX = cameraX;
      centerZ = cameraZ;
      started = true;
    }
    if (cursor == null) return false;
    cursor.advance();
    LodDiskNearestScan.Completion completed = cursor.completed();
    if (completed == null) return false;
    if (completed.failure() != null) {
      cursor = null;
      return true;
    }
    // The scan already ranks initial recovery + all on-disk keys on its own
    // thread. Never sort its immutable List.copyOf result in place.
    LinkedHashSet<Long> selected = new LinkedHashSet<>();
    for (long key : queue) {
      if (!cursor.wasQueuedInitially(key)) selected.add(key);
    }
    for (long key : completed.keys()) {
      if (selected.size() >= LodDiskNearestScan.MAX_KEYS) break;
      selected.add(key);
    }
    ArrayList<Long> previous = new ArrayList<>(queue);
    queue.clear();
    queue.addAll(selected);
    cursor = null;
    for (long key : previous)
      if (!queue.contains(key)) forgotten.accept(key);
    return false;
  }

  void reset() {
    if (cursor != null) cursor.cancel();
    cursor = null;
    started = false;
  }

  // Focused check accessors; never poll them from a different client thread.
  boolean scanning() { return cursor != null; }
  boolean started() { return started; }
}
