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
  private static final int NONEMPTY_RESCAN_TICKS = 400;

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
  // Pre-size the bounded renewal map so a full scan handoff does not trigger
  // client-thread HashMap growth/rehash at the 8,192-key admission limit.
  private final HashMap<Long, Long> renewed = new HashMap<>(
      LodDiskNearestScan.MAX_KEYS, 1.0f);
  private long scanGeneration;
  private boolean invalidated;
  // Lifetime counters for owner-thread bounded streaming work. These are
  // diagnostics-only and let baseline capture report actual apply pressure
  // without instrumenting filesystem or GPU paths.
  private long appliedRemovals;
  private long appliedAdds;
  private long appliedSheds;

  /** Caller invokes this on every requeue/renewal after changing its queue. */
  void recoveryQueued(long key) {
    // A cancelled cursor may still be retiring while its directory stream closes.
    // Keep recording renewals during that handoff so a requeued page cannot be
    // mistaken for stale scan output when the bounded apply resumes.
    if (cursor == null && apply == null && retiring == null) return;
    // Keep the common path to one probe. Once the bounded map is full, reject
    // a new key before insertion so the overflow path cannot transiently grow
    // the backing table or allocate a remove tombstone.
    if (renewed.size() >= LodDiskNearestScan.MAX_KEYS
        && !renewed.containsKey(key)) {
      invalidated = true;
      return;
    }
    Long previous = renewed.putIfAbsent(key, scanGeneration);
    if (previous == null && renewed.size() > LodDiskNearestScan.MAX_KEYS) {
      // Defensive fail-closed guard if the admission bound changes concurrently.
      renewed.remove(key);
      invalidated = true;
    }
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
    else {
      // A successfully scanned directory can gain new persisted pages while the
      // camera stays still, whether it was empty or already contained pages.
      // Reopen a bounded background cursor at a low cadence; no filesystem
      // access, filename ranking, or blocking waits happen on the client thread.
      // Backoff for failures remains separate from successful rediscovery.
      clearRetry();
      retryScheduled = true;
      retryCountdown = completed.keys().isEmpty()
          ? EMPTY_RESCAN_TICKS : NONEMPTY_RESCAN_TICKS;
    }
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
    // Most scan applies never need to evict a discovered page. Keep this
    // allocation lazy so ordinary bounded refreshes do not allocate an empty
    // HashSet on the client/render thread.
    private HashSet<Long> shed;
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
        if (!isRenewed(key) && queue.remove(key)) {
          appliedRemovals++;
          forgotten.accept(key);
        }
        remaining--;
      }
      while (remaining > 0 && added < incoming.size()) {
        long key = incoming.get(added);
        if ((shed != null && shed.contains(key)) || queue.contains(key)) {
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
              if (shed == null) {
                // A single bounded allocation sized to this scan avoids
                // repeated HashSet growth when a full queue sheds many pages.
                shed = new HashSet<>(Math.max(16, Math.min(
                    LodDiskNearestScan.MAX_KEYS, incoming.size())), 1.0f);
              }
              shed.add(victim);
              appliedSheds++;
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
        appliedAdds++;
        added++;
        remaining--;
      }
      return removed == discard.size() && added == incoming.size();
    }

    int remaining() {
      return (discard.size() - removed) + (incoming.size() - added);
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

  /**
   * Read-only owner-thread snapshot for renderer diagnostics and bounded
   * baseline capture. It reports requested work state without touching the
   * cursor thread or forcing filesystem activity.
   */
  record Diagnostics(
      boolean enabled,
      boolean started,
      boolean retryScheduled,
      int retryCountdown,
      int retryDelayTicks,
      int queueSize,
      int renewalSize,
      long scanGeneration,
      long examined,
      long turns,
      boolean cursorActive,
      boolean cursorCompleted,
      boolean missingDirectory,
      boolean retiring,
      int pendingApply,
      long appliedRemovals,
      long appliedAdds,
      long appliedSheds,
      int pendingApplyRemaining) {}

  Diagnostics diagnostics(boolean enabled, int queueSize) {
    LodDiskNearestScan.Cursor active = cursor;
    LodDiskNearestScan.Completion completed = active == null ? null : active.completed();
    return new Diagnostics(
        enabled,
        started,
        retryScheduled,
        retryCountdown,
        retryDelayTicks,
        queueSize,
        renewed.size(),
        scanGeneration,
        active == null ? 0L : active.examined(),
        active == null ? 0L : active.turns(),
        active != null,
        completed != null,
        active != null && active.missingDirectory(),
        retiring != null,
        apply == null ? 0 : 1,
        appliedRemovals,
        appliedAdds,
        appliedSheds,
        apply == null ? 0 : apply.remaining());
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