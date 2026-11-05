package com.shnewbs.deepprimal.core;

/**
 * One view snapshot replaces repeated configuration reads, square roots and trigonometry per tile.
 */
public record HorizonView(double x, double z, double fineSquared, double mediumSquared) {
  public static HorizonView create(
      double x, double z, int pixelsHigh, double fov, double workload) {
    // A minimized/zero-sized viewport or invalid telemetry must not request
    // fine LOD tiles. Zero thresholds choose the cheapest detail for every tile.
    if (!Double.isFinite(x) || !Double.isFinite(z) || pixelsHigh <= 0
        || !Double.isFinite(fov) || fov <= 0 || fov >= 180
        || !Double.isFinite(workload) || workload <= 0) {
      // Never retain a NaN camera origin in the published snapshot.
      return new HorizonView(Double.isFinite(x) ? x : 0, Double.isFinite(z) ? z : 0, 0, 0);
    }
    double projection =
        Math.clamp(
            pixelsHigh / 720.0 * Math.tan(Math.toRadians(35)) / Math.tan(Math.toRadians(fov / 2)),
            .5,
            3);
    double scaleSquared = Math.max(.25, projection * projection * Math.clamp(workload, .125, 1));
    return new HorizonView(x, z, 1024.0 * 1024 * scaleSquared, 2560.0 * 2560 * scaleSquared);
  }

  public int detail(int tileX, int tileZ) {
    double dx =
        Math.max(
            0, Math.abs((tileX + .5) * FarTerrain.TILE_BLOCKS - x) - FarTerrain.TILE_BLOCKS / 2.0);
    double dz =
        Math.max(
            0, Math.abs((tileZ + .5) * FarTerrain.TILE_BLOCKS - z) - FarTerrain.TILE_BLOCKS / 2.0);
    double distanceSquared = dx * dx + dz * dz;
    return distanceSquared < fineSquared ? 0 : distanceSquared < mediumSquared ? 1 : 2;
  }
}
