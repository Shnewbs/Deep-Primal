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
    long bestDx = dx, bestDz = dz;
    while (keys.hasNext()) {
      long key = keys.next();
      dx = (int) (key >> 32) - (long) cameraChunkX;
      dz = (int) key - (long) cameraChunkZ;
      long ring = Math.max(Math.abs(dx), Math.abs(dz));
      if (ring > bestRing) continue;
      double squared = (double) dx * dx + (double) dz * dz;
      if (ring < bestRing || squared < bestSquared
          || (squared == bestSquared && exactSquaredLess(dx, dz, bestDx, bestDz))) {
        chosen = key;
        bestRing = ring;
        bestSquared = squared;
        bestDx = dx;
        bestDz = dz;
      }
    }
    // A double rounds distinct very large squared distances to the same value.
    // The exact 128-bit tie comparison preserves true nearest-first priority
    // across the full signed-int coordinate domain without allocating BigInteger.
    // Equal exact distances retain insertion order for fair deterministic ties.
    return chosen;
  }

  private static boolean exactSquaredLess(long dx, long dz, long otherDx, long otherDz) {
    long x = dx * dx, z = dz * dz;
    long low = x + z;
    long high = Math.multiplyHigh(dx, dx) + Math.multiplyHigh(dz, dz)
        + (Long.compareUnsigned(low, x) < 0 ? 1 : 0);
    long ox = otherDx * otherDx, oz = otherDz * otherDz;
    long otherLow = ox + oz;
    long otherHigh = Math.multiplyHigh(otherDx, otherDx) + Math.multiplyHigh(otherDz, otherDz)
        + (Long.compareUnsigned(otherLow, ox) < 0 ? 1 : 0);
    int compareHigh = Long.compareUnsigned(high, otherHigh);
    return compareHigh < 0 || (compareHigh == 0
        && Long.compareUnsigned(low, otherLow) < 0);
  }
}