package com.shnewbs.deepprimal.lod.client;

import net.minecraft.world.phys.AABB;

/**
 * One-pass resident-page culling metadata from an already frozen AABB snapshot.
 * The conservative union and adjacent-equality mask must describe the same
 * uploaded geometry, even if a mesh later reassigns its public bounds field.
 * No visibility work runs here; this is invoked only when a page is published.
 */
final class LodPublishedBoundsPreIdentity {
  private static final long[] NO_REPEATS = new long[0];
  // Startup-only A/B switch. Default preserves the committed two-back fast path.
  // Disabling avoids its i-2 publication comparisons and metadata for pages
  // with unique bounds, without changing coarse/adjacent or fine culling.
  static final boolean TWO_BACK_ENABLED = !"false".equalsIgnoreCase(
      System.getProperty("deepprimal.lod.twoBackFrustumReuse", "true"));

  private LodPublishedBoundsPreIdentity() {}

  /** The caller allocates one page array and (when enabled) one mask array. */
  static void populate(AABB[][] levels, AABB[] pages, long[][] runs) {
    populate(levels, pages, runs, null);
  }

  /**
   * Optionally mark nonadjacent i-2 duplicates in the same publication scan.
   * Draw-time consumers use the two-back result only after its first occurrence
   * has already been tested; no per-frame AABB comparisons are necessary.
   * The original overload keeps older tests and callers byte-for-byte in behavior.
   */
  static void populate(AABB[][] levels, AABB[] pages, long[][] runs,
      long[][] twoBackRuns) {
    if (pages.length != levels.length
        || (LodPageFrustumBounds.ENABLED && runs.length != levels.length)
        || (LodPageFrustumBounds.ENABLED && twoBackRuns != null
            && twoBackRuns.length != levels.length))
      throw new IllegalArgumentException("LOD publication metadata array length mismatch");
    // Keep the original broadphase-off behavior: no union or repeated-part
    // metadata is computed or retained in the A/B comparison mode.
    if (!LodPageFrustumBounds.ENABLED) return;
    for (int detail = 0; detail < levels.length; detail++) {
      AABB[] parts = levels[detail];
      AABB representative = parts.length == 0 ? null : parts[0];
      boolean wholePageValid = true;
      boolean uniform = true;
      boolean previousValid = false;
      AABB previous = null, beforePrevious = null;
      boolean beforePreviousValid = false;
      long[] repeated = null, twoBackRepeated = null;
      double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY,
          minZ = Double.POSITIVE_INFINITY;
      double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY,
          maxZ = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < parts.length; i++) {
        AABB box = parts[i];
        boolean valid = finiteOrdered(box);
        // Invalid parts disable coarse rejection, but later valid adjacent
        // parts still share their own fine-frustum result. Do not stop early.
        // The adjacent verdict is also the two-back exclusion. Compute it
        // once and short-circuit a costly i-2 comparison for adjacent runs.
        boolean adjacent = i > 0 && previousValid && valid && sameBox(previous, box);
        if (adjacent) {
          if (repeated == null) repeated = new long[((parts.length - 1) >>> 6) + 1];
          repeated[i >>> 6] |= 1L << (i & 63);
        }
        // A-B-A and longer alternations can share an earlier decision even
        // when the immediate neighbor has different bounds. Never assign two
        // aliases to one part; adjacent reuse takes precedence.
        if (TWO_BACK_ENABLED && twoBackRuns != null && !adjacent && i > 1
            && beforePreviousValid && valid
            && sameBox(beforePrevious, box)) {
          if (twoBackRepeated == null)
            twoBackRepeated = new long[((parts.length - 1) >>> 6) + 1];
          twoBackRepeated[i >>> 6] |= 1L << (i & 63);
        }
        if (!valid) {
          wholePageValid = false;
        } else if (wholePageValid) {
          if (uniform) {
            if (!sameBox(representative, box)) {
              // The prefix had identical numerical bounds. Defer its union
              // reductions until the first different part; an all-uniform
              // page needs only the validated representative identity.
              // Replaying the prefix in original order retains Math.min/max
              // signed-zero behavior on mixed pages with value-equal AABBs.
              uniform = false;
              for (int j = 0; j <= i; j++) {
                AABB member = parts[j];
                minX = Math.min(minX, member.minX);
                minY = Math.min(minY, member.minY);
                minZ = Math.min(minZ, member.minZ);
                maxX = Math.max(maxX, member.maxX);
                maxY = Math.max(maxY, member.maxY);
                maxZ = Math.max(maxZ, member.maxZ);
              }
            }
          } else {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
          }
        }
        beforePrevious = previous;
        beforePreviousValid = previousValid;
        previous = box;
        previousValid = valid;
      }
      runs[detail] = repeated == null ? NO_REPEATS : repeated;
      if (twoBackRuns != null)
        twoBackRuns[detail] = twoBackRepeated == null ? NO_REPEATS : twoBackRepeated;
      // A single part cannot save any culling checks, matching the existing
      // union contract and its reference-identity uniform marker.
      if (parts.length > 1 && wholePageValid)
        pages[detail] = uniform ? representative
            : new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
  }

  private static boolean sameBox(AABB a, AABB b) {
    // Some upload parts legitimately share an immutable AABB instance. Keep
    // exact coordinate equality for independently allocated equal bounds.
    return a == b || a.minX == b.minX && a.minY == b.minY && a.minZ == b.minZ
        && a.maxX == b.maxX && a.maxY == b.maxY && a.maxZ == b.maxZ;
  }

  private static boolean finiteOrdered(AABB b) {
    return b != null && Double.isFinite(b.minX) && Double.isFinite(b.minY)
        && Double.isFinite(b.minZ) && Double.isFinite(b.maxX)
        && Double.isFinite(b.maxY) && Double.isFinite(b.maxZ)
        && b.minX <= b.maxX && b.minY <= b.maxY && b.minZ <= b.maxZ;
  }
}
