package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
  private static final int FIRST_RETRY_TICKS = 20;
  private static final int MAX_RETRY_TICKS = 320;
  private static final int EMPTY_RESCAN_TICKS = 200;

  private LodDiskNearestScan.Cursor cursor;
  // Keep the old worker owned through its directory-stream close. A reset may
  // occur while an interrupted filesystem call is still unwinding.
  private LodDiskNearestScan.Cursor retiring;
  private PendingApply apply;
  private boolean retryScheduled;
  private int retryCountdown, retryDelayTicks;
  private boolean started;
  private int centerX, centerZ;
  // An accepted recovery can be removed and requeued while a worker is scanning.
  // Queue membership alone cannot identify a renewed owner. These tokens are
  // scoped to a scan and never require 8192-key comparisons on the render thread.
  private final HashMap<Long, Long> renewed = new HashMap<>();
  private long scanGeneration;
  private boolean invalidated;

  /** Caller invokes this on every requeue/renewal after changing its queue. */
  void recoveryQueued(long key) {
    if (cursor == null && apply == null) return;
    if (!renewed.containsKey(key) && renewed.size() == LodDiskNearestScan.MAX_KEYS) {
      // Never silently discard an untracked owner. Rebuild an exact snapshot.
      invalidated = true;
      return;
    }
    renewed.put(key, scanGeneration);
  }

  /** Returns true only when a completed scan failed (never on transient latency). */
  boolean tick(Path folder, int cameraX, int cameraZ, LinkedHashSet<Long> queue,
      boolean enabled, LongConsumer forgotten) {
    if (!enabled || folder == null) {
      reset();
      return false;
    }
    if (invalidated) {
      // Abort old work rather than evict a newer recovery after token overflow.
      if (cursor != null) cursor.cancel();
      apply = null;
      started = false;
      renewed.clear();
      invalidated = false;
    }
    // Do not spawn a replacement after a world reset while the previous
    // directory worker is still closing; none of this work blocks the client.
    if (retiring != null) {
      if (!retiring.stopped()) return false;
      retiring = null;
    }
    boolean moved = started
        && Math.max(Math.abs((long) cameraX - centerX),
                    Math.abs((long) cameraZ - centerZ)) >= LodDiskNearestScan.RETARGET_CHUNKS;
    if (moved) {
      // An actual camera retarget can bypass a stale folder's retry delay.
      retryScheduled = false;
      retryCountdown = retryDelayTicks = 0;
    } else if (retryScheduled && retryCountdown > 0) {
      retryCountdown--;
    }
    // Allow an in-progress bounded queue application to finish before retry.
    boolean retryDue = retryScheduled && retryCountdown == 0 && apply == null;
    boolean retarget = !started || moved || retryDue;
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
      scanGeneration++;
      renewed.clear();
      cursor = LodDiskNearestScan.start(folder, cameraX, cameraZ,
          LodDiskNearestScan.MAX_KEYS, new ArrayList<>(queue));
      retryScheduled = false;
      centerX = cameraX;
      centerZ = cameraZ;
      started = true;
    }
    if (apply != null) {
      if (apply.advance(queue, forgotten)) {
        apply = null;
        renewed.clear();
      }
      return false;
    }
    if (cursor == null) return false;
    cursor.advance();
    LodDiskNearestScan.Completion completed = cursor.completed();
    if (completed == null) return false;
    if (completed.failure() != null) {
      retireCursor();
      scheduleRetry();
      return true;
    }
    boolean missingDirectory = cursor.missingDirectory();
    // The scanner returns immutable, ranked candidates and a discard list.
    // Apply both in small turns; neither List.sort nor an 8192-entry hash rebuild
    // is allowed on the client/render thread.
    apply = new PendingApply(completed.discardKeys(), completed.keys(), renewed, scanGeneration);
    retireCursor();
    if (missingDirectory) scheduleRetry();
    else if (completed.keys().isEmpty()) {
      // A stable but empty save directory may receive its first persisted page
      // without camera movement. Poll at a fixed low cadence, off the client thread.
      clearRetry();
      retryScheduled = true;
      retryCountdown = EMPTY_RESCAN_TICKS;
    } else clearRetry();
    if (apply.advance(queue, forgotten)) {
      apply = null;
      renewed.clear();
    }
    return false;
  }

  /** Owner-thread mutations are individually capped to avoid a frame-time spike. */
  private static final class PendingApply {
    private final java.util.List<Long> discard, incoming;
    private final HashMap<Long, Long> renewed;
    private final long generation;
    private final HashSet<Long> shed = new HashSet<>();
    private int removed, added, farthest;

    PendingApply(java.util.List<Long> discard, java.util.List<Long> incoming,
        HashMap<Long, Long> renewed, long generation) {
      this.discard = discard;
      this.incoming = incoming;
      this.renewed = renewed;
      this.generation = generation;
      this.farthest = incoming.size() - 1;
    }

    private boolean isRenewed(long key) {
      return renewed.getOrDefault(key, -1L) == generation;
    }

    boolean advance(LinkedHashSet<Long> queue, LongConsumer forgotten) {
      int remaining = LodDiskNearestScan.ENTRIES_PER_TURN;
      while (remaining > 0 && removed < discard.size()) {
        long key = discard.get(removed++);
        // A post-snapshot renewal owns the key, even if its identity and
        // membership look identical to the original scan's candidate.
        if (!isRenewed(key) && queue.remove(key)) forgotten.accept(key);
        remaining--;
      }
      while (remaining > 0 && added < incoming.size()) {
        long key = incoming.get(added);
        if (shed.contains(key) || queue.contains(key)) {
          added++;
          remaining--;
          continue;
        }
        if (queue.size() >= LodDiskNearestScan.MAX_KEYS) {
          // The protected owner may consume a slot originally designated for
          // a discovered page. Shed the farthest *unrenewed* ranked recovery,
          // never a newly requeued owner. Inspections count against the same
          // bounded owner-thread work budget as inserts/removals.
          while (remaining > 0 && farthest > added
              && queue.size() >= LodDiskNearestScan.MAX_KEYS) {
            long victim = incoming.get(farthest--);
            remaining--;
            if (!isRenewed(victim) && queue.remove(victim)) {
              shed.add(victim);
              forgotten.accept(victim);
            }
          }
          if (remaining == 0) break;
          if (queue.size() >= LodDiskNearestScan.MAX_KEYS) {
            // All lower-priority slots are protected. Fail closed at the cap.
            added++;
            remaining--;
            continue;
          }
        }
        queue.add(key);
        added++;
        remaining--;
      }
      return removed == discard.size() && added == incoming.size();
    }
  }

  private void retireCursor() {
    if (cursor != null && !cursor.stopped()) retiring = cursor;
    cursor = null;
  }

  private void scheduleRetry() {
    // Tick-count backoff avoids directory churn and warning spam on read-only
    // or not-yet-created save locations. A successful scan clears the delay.
    retryDelayTicks = retryDelayTicks == 0 ? FIRST_RETRY_TICKS
        : Math.min(MAX_RETRY_TICKS, retryDelayTicks * 2);
    retryCountdown = retryDelayTicks;
    retryScheduled = true;
  }

  private void clearRetry() {
    retryScheduled = false;
    retryCountdown = retryDelayTicks = 0;
  }

  void reset() {
    if (cursor != null) {
      cursor.cancel();
      retireCursor();
    }
    apply = null;
    started = false;
    renewed.clear();
    invalidated = false;
    clearRetry();
  }

  // Focused check accessors; never poll them from a different client thread.
  boolean scanning() { return cursor != null || apply != null; }
  boolean started() { return started; }
}
