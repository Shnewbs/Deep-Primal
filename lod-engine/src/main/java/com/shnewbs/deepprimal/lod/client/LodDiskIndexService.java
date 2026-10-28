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
  private PendingApply apply;
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
    if (retarget) {
      if (cursor != null) cursor.cancel();
      // A new camera invalidates an incomplete previous priority reordering.
      apply = null;
    }
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
    if (apply != null) {
      if (apply.advance(queue, forgotten)) apply = null;
      return false;
    }
    if (cursor == null) return false;
    cursor.advance();
    LodDiskNearestScan.Completion completed = cursor.completed();
    if (completed == null) return false;
    if (completed.failure() != null) {
      cursor = null;
      return true;
    }
    // The scanner returns immutable, ranked candidates and a discard list.
    // Apply both in small turns; neither List.sort nor an 8192-entry hash rebuild
    // is allowed on the client/render thread.
    apply = new PendingApply(completed.discardKeys(), completed.keys());
    cursor = null;
    if (apply.advance(queue, forgotten)) apply = null;
    return false;
  }

  /** Owner-thread mutations are individually capped to avoid a frame-time spike. */
  private static final class PendingApply {
    private final java.util.List<Long> discard, incoming;
    private int removed, added;

    PendingApply(java.util.List<Long> discard, java.util.List<Long> incoming) {
      this.discard = discard;
      this.incoming = incoming;
    }

    boolean advance(LinkedHashSet<Long> queue, LongConsumer forgotten) {
      int remaining = LodDiskNearestScan.ENTRIES_PER_TURN;
      while (remaining > 0 && removed < discard.size()) {
        long key = discard.get(removed++);
        if (queue.remove(key)) forgotten.accept(key);
        remaining--;
      }
      while (remaining > 0 && added < incoming.size()) {
        long key = incoming.get(added++);
        if (queue.size() < LodDiskNearestScan.MAX_KEYS) queue.add(key);
        remaining--;
      }
      return removed == discard.size() && added == incoming.size();
    }
  }

  void reset() {
    if (cursor != null) cursor.cancel();
    cursor = null;
    apply = null;
    started = false;
  }

  // Focused check accessors; never poll them from a different client thread.
  boolean scanning() { return cursor != null || apply != null; }
  boolean started() { return started; }
}
