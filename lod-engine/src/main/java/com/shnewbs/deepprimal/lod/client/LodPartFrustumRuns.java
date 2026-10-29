package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.List;
import net.minecraft.world.phys.AABB;

/**
 * Compact, page-publication-only adjacency metadata. A positive conservative
 * page union cannot prove visibility of mixed parts, but neighboring parts with
 * equal finite AABBs share the same fine-frustum verdict. Store one bit for each
 * proven repeated neighbor; never allocate or compare six coordinates per frame.
 * Each mesh's bounds must remain unchanged during its resident-page lifetime.
 */
final class LodPartFrustumRuns {
  private static final long[] EMPTY = new long[0];
  private LodPartFrustumRuns() {}

  static long[][] build(List<List<TerrainGpuMesh>> levels) {
    long[][] masks = new long[levels.size()][];
    for (int detail = 0; detail < levels.size(); detail++) {
      List<TerrainGpuMesh> parts = levels.get(detail);
      long[] bits = null;
      AABB previous = null;
      for (int i = 0; i < parts.size(); i++) {
        AABB current = parts.get(i).bounds;
        if (sameFiniteBounds(previous, current)) {
          if (bits == null) bits = new long[((parts.size() - 1) >>> 6) + 1];
          bits[i >>> 6] |= 1L << (i & 63);
        }
        previous = current;
      }
      masks[detail] = bits == null ? EMPTY : bits;
    }
    return masks;
  }

  static boolean repeatsPrevious(long[] mask, int index) {
    return index > 0 && (index >>> 6) < mask.length
        && (mask[index >>> 6] & (1L << (index & 63))) != 0;
  }

  /** Only immutable, finite, correctly ordered bounds can share a predicate. */
  static boolean sameFiniteBounds(AABB a, AABB b) {
    return valid(a) && valid(b)
        && a.minX == b.minX && a.minY == b.minY && a.minZ == b.minZ
        && a.maxX == b.maxX && a.maxY == b.maxY && a.maxZ == b.maxZ;
  }

  private static boolean valid(AABB box) {
    return box != null && Double.isFinite(box.minX) && Double.isFinite(box.minY)
        && Double.isFinite(box.minZ) && Double.isFinite(box.maxX)
        && Double.isFinite(box.maxY) && Double.isFinite(box.maxZ)
        && box.minX <= box.maxX && box.minY <= box.maxY && box.minZ <= box.maxZ;
  }
}
