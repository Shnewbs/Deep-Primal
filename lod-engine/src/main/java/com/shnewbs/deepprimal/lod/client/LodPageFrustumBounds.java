package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.List;
import net.minecraft.world.phys.AABB;

/**
 * Conservative, per-resident-page LOD frustum bounds. Built once after all GPU
 * parts have their world-space bounds, not once per camera frame. A null entry
 * means the page must use the unchanged per-part visibility path.
 */
final class LodPageFrustumBounds {
  private LodPageFrustumBounds() {}

  static AABB[] build(List<List<TerrainGpuMesh>> levels) {
    AABB[] result = new AABB[levels.size()];
    for (int detail = 0; detail < levels.size(); detail++) {
      List<TerrainGpuMesh> parts = levels.get(detail);
      // A singleton cannot save a test; empty levels already have no draw calls.
      if (parts.size() <= 1) continue;
      double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY,
          minZ = Double.POSITIVE_INFINITY;
      double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY,
          maxZ = Double.NEGATIVE_INFINITY;
      boolean valid = true;
      for (TerrainGpuMesh part : parts) {
        AABB box = part.bounds;
        if (!valid(box)) {
          // Never allow a malformed part to turn conservative rejection into a
          // false negative. Leave that level on the original per-part path.
          valid = false;
          break;
        }
        minX = Math.min(minX, box.minX);
        minY = Math.min(minY, box.minY);
        minZ = Math.min(minZ, box.minZ);
        maxX = Math.max(maxX, box.maxX);
        maxY = Math.max(maxY, box.maxY);
        maxZ = Math.max(maxZ, box.maxZ);
      }
      if (valid) result[detail] = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    return result;
  }

  private static boolean valid(AABB a) {
    return a != null && Double.isFinite(a.minX) && Double.isFinite(a.minY)
        && Double.isFinite(a.minZ) && Double.isFinite(a.maxX)
        && Double.isFinite(a.maxY) && Double.isFinite(a.maxZ)
        && a.minX <= a.maxX && a.minY <= a.maxY && a.minZ <= a.maxZ;
  }
}
