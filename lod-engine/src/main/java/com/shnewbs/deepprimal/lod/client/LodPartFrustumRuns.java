package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.List;
import net.minecraft.world.phys.AABB;

/**
 * Render-thread metadata for multi-part LOD pages. The coarse union can prove
 * rejection, but a positive union does not prove that every mixed part is visible.
 * Within that fine path, adjacent identical finite AABBs have the same frustum
 * answer. Detect only whether each level has such runs at page publication;
 * never retain per-part references or allocate arrays during camera extraction.
 */
final class LodPartFrustumRuns {
  private LodPartFrustumRuns() {}

  static boolean[] build(List<List<TerrainGpuMesh>> levels) {
    boolean[] repeated = new boolean[levels.size()];
    for (int detail = 0; detail < levels.size(); detail++) {
      AABB previous = null;
      for (TerrainGpuMesh part : levels.get(detail)) {
        AABB current = part.bounds;
        if (sameFiniteBounds(previous, current)) {
          repeated[detail] = true;
          break;
        }
        previous = current;
      }
    }
    return repeated;
  }

  /** Reuse only a pure, valid AABB predicate. Malformed bounds use original fine culling. */
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
