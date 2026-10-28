package com.shnewbs.deepprimal.lod.client;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Chooses the next live chunk snapshot from the renderer's bounded insertion-ordered
 * dirty queue. When the camera travels, oldest-first processing could spend an
 * entire queue's worth of captures on distant chunks before refreshing a nearby
 * page. Prefer the closest dirty chunk (Chebyshev ring, then squared distance),
 * except for a periodic oldest-first admission that guarantees queued work can
 * eventually advance even during continuous nearby changes.
 *
 * <p>Does not load chunks, allocate a sorted list, mutate the queue, inspect
 * GPU state or override revisions. VoxelLodRenderer bounds the dirty queue at
 * 4096 entries and performs a single capture per selected work interval.
 */
final class LodCapturePriority {
  private LodCapturePriority() {}

  static long choose(LinkedHashMap<Long, Long> dirty, int cameraChunkX,
      int cameraChunkZ, boolean oldestFirst) {
    if (dirty.isEmpty()) throw new IllegalArgumentException("empty dirty queue");
    Iterator<Long> keys = dirty.keySet().iterator();
    long chosen = keys.next();
    if (oldestFirst) return chosen;

    long dx = (int) (chosen >> 32) - (long) cameraChunkX;
    long dz = (int) chosen - (long) cameraChunkZ;
    long bestRing = Math.max(Math.abs(dx), Math.abs(dz));
    double bestSquared = (double) dx * dx + (double) dz * dz;
    while (keys.hasNext()) {
      long key = keys.next();
      dx = (int) (key >> 32) - (long) cameraChunkX;
      dz = (int) key - (long) cameraChunkZ;
      long ring = Math.max(Math.abs(dx), Math.abs(dz));
      if (ring > bestRing) continue;
      double squared = (double) dx * dx + (double) dz * dz;
      if (ring < bestRing || squared < bestSquared) {
        chosen = key;
        bestRing = ring;
        bestSquared = squared;
      }
    }
    // Strict comparison keeps insertion order as the deterministic tie breaker.
    return chosen;
  }
}
