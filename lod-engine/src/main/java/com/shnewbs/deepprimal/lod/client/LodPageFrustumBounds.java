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
  // A/B diagnostic only. Default remains enabled; change with a JVM startup flag.
  // No persisted world/graphics settings or runtime scene state are modified.
  static final boolean ENABLED = !"false".equalsIgnoreCase(
      System.getProperty("deepprimal.lod.pageFrustumBroadphase", "true"));

  private LodPageFrustumBounds() {}

  /**
   * Freeze each resident part's world-space bounds reference at publication.
   * Minecraft AABBs are immutable; GPU mesh bounds fields are not. Culling
   * must use the captured geometry bounds, not later field reassignment.
   */
  static AABB[][] snapshot(List<List<TerrainGpuMesh>> levels) {
    AABB[][] captured = new AABB[levels.size()][];
    for (int detail = 0; detail < levels.size(); detail++) {
      List<TerrainGpuMesh> parts = levels.get(detail);
      AABB[] bounds = new AABB[parts.size()];
      for (int i = 0; i < bounds.length; i++) bounds[i] = parts.get(i).bounds;
      captured[detail] = bounds;
    }
    return captured;
  }

  static AABB[] build(List<List<TerrainGpuMesh>> levels) {
    // Broadphase-off A/B does not allocate unused union metadata.
    if (!ENABLED) return new AABB[levels.size()];
    return fromSnapshot(snapshot(levels));
  }

  static AABB[] fromSnapshot(AABB[][] levels) {
    AABB[] result = new AABB[levels.length];
    if (!ENABLED) return result;
    for (int detail = 0; detail < levels.length; detail++) {
      AABB[] parts = levels[detail];
      // A singleton cannot save a test; empty levels already have no draw calls.
      if (parts.length <= 1) continue;
      double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY,
          minZ = Double.POSITIVE_INFINITY;
      double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY,
          maxZ = Double.NEGATIVE_INFINITY;
      boolean valid = true;
      // If all part bounds describe exactly the same volume, one positive
      // coarse visibility result also proves every fine-part result. Keep the
      // validated first box by identity as the draw-time fast-path marker.
      AABB representative = parts[0];
      boolean uniform = true;
      for (AABB box : parts) {
        if (!valid(box)) {
          // Never allow a malformed part to turn conservative rejection into a
          // false negative. Leave that level on the original per-part path.
          valid = false;
          break;
        }
        if (uniform && !sameBox(box, representative)) uniform = false;
        minX = Math.min(minX, box.minX);
        minY = Math.min(minY, box.minY);
        minZ = Math.min(minZ, box.minZ);
        maxX = Math.max(maxX, box.maxX);
        maxY = Math.max(maxY, box.maxY);
        maxZ = Math.max(maxZ, box.maxZ);
      }
      if (valid) result[detail] = uniform ? representative
          : new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    return result;
  }

  /**
   * Only a bounds object returned directly from the first part proves all
   * part AABBs were equal at page publication. Mixed pages have a separately
   * allocated conservative union, which is insufficient for positive culling.
   */
  static boolean allPartsShareBounds(AABB pageBounds, List<TerrainGpuMesh> parts) {
    return pageBounds != null && parts.size() > 1
        && pageBounds == parts.get(0).bounds;
  }

  /** Uniform identity marker validated from the same frozen source as the union. */
  static boolean allPartsShareSnapshot(AABB pageBounds, AABB[] parts) {
    return pageBounds != null && parts.length > 1 && pageBounds == parts[0];
  }

  private static boolean sameBox(AABB a, AABB b) {
    return a.minX == b.minX && a.minY == b.minY && a.minZ == b.minZ
        && a.maxX == b.maxX && a.maxY == b.maxY && a.maxZ == b.maxZ;
  }

  private static boolean valid(AABB a) {
    return a != null && Double.isFinite(a.minX) && Double.isFinite(a.minY)
        && Double.isFinite(a.minZ) && Double.isFinite(a.maxX)
        && Double.isFinite(a.maxY) && Double.isFinite(a.maxZ)
        && a.minX <= a.maxX && a.minY <= a.maxY && a.minZ <= a.maxZ;
  }
}
