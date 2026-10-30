package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import java.util.Objects;

/**
 * Absolute-coordinate, bounded vegetation placements for the explicit V8-r6
 * saved-policy path. Reuses the exact preflight cover samples, not a new biome
 * winner or a second mutable climate callback. Neither this planner nor its
 * placements modify terrain, liquid amounts, hydrology or saved profiles.
 *
 * <p>Each signed-world lattice cell has at most one stable plant anchor. The
 * continuous ecotone vegetationPatch field governs patch occupancy; sparse
 * anchor and species hashes are world-seed bound, not per-call random state.
 */
public final class V8VegetationColumnPlan {
  public enum Plant { SHORT_GRASS, FERN }
  public record Placement(Plant plant, int y) {
    public Placement { Objects.requireNonNull(plant, "plant"); }
  }

  private static final int SIDE = 16;
  private static final long X_SALT = 0x39e021b708ec9621L;
  private static final long Z_SALT = 0x7273e65cf7b1320dL;
  private static final long THIN_SALT = 0x49fb386a73cd8205L;
  private static final long SPECIES_SALT = 0x89de54e4a1c7902bL;
  private final Placement[] sites;
  private final int count;

  private V8VegetationColumnPlan(Placement[] sites, int count) {
    this.sites = sites;
    this.count = count;
  }

  public Placement placement(int localX, int localZ) {
    if (localX < 0 || localX >= SIDE || localZ < 0 || localZ >= SIDE)
      throw new IndexOutOfBoundsException("V8 vegetation local column");
    return sites[localZ * SIDE + localX];
  }

  public int count() { return count; }

  /**
   * Planned WORLD_SURFACE_WG height for an explicit r6 column. A plant adds
   * one block only when its saved position is exactly the top air cell in the
   * independently computed raw/soil column. This is a PREWRITE prediction:
   * native setter callbacks may veto or overwrite the plant, in which case
   * the actual chunk heightmap and realized receipt take precedence.
   * OCEAN_FLOOR_WG ignores these non-motion-blocking plant species.
   * No neighboring chunk, climate callback or per-call randomness is used.
   */
  public int plannedWorldSurfaceExclusive(int localX, int localZ,
      int rawHeightExclusive, int minY, int maxYExclusive) {
    if (minY >= maxYExclusive || rawHeightExclusive < minY
        || rawHeightExclusive > maxYExclusive)
      throw new IllegalArgumentException("V8 raw height outside world column");
    Placement site = placement(localX, localZ);
    if (site == null || site.y() != rawHeightExclusive
        || site.y() >= maxYExclusive) return rawHeightExclusive;
    return Math.addExact(site.y(), 1);
  }

  /** Build after climate/material preflight, before the first native write. */
  public static V8VegetationColumnPlan build(V8EcotoneColumnPlan columns,
      int originX, int originZ, int minY, int height, long savedSeed,
      V8VegetationPolicy policy) {
    Objects.requireNonNull(columns, "V8 dry-land columns");
    Objects.requireNonNull(policy, "saved vegetation policy");
    if (Math.floorMod(originX, SIDE) != 0 || Math.floorMod(originZ, SIDE) != 0)
      throw new IllegalArgumentException("V8 vegetation origin must be chunk-aligned");
    if (height <= 0) throw new IllegalArgumentException("V8 vegetation height");
    int maxY = Math.addExact(minY, height);
    Math.addExact(originX, SIDE - 1);
    Math.addExact(originZ, SIDE - 1);
    int spacing = policy.anchorSpacing();
    Placement[] result = new Placement[SIDE * SIDE];
    int placed = 0;
    if (policy.densityScale() == 0) return new V8VegetationColumnPlan(result, 0);
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        var column = columns.column(x, z);
        if (column == null) continue; // authoritative wet, air and bedrock exclusions
        EcotoneSurfaceField.Sample cover = column.cover();
        if (!cover.vegetationPatch() || cover.snowPatch()
            || cover.cover() == EcotoneSurfaceField.Cover.BARE) continue;
        int y = Math.addExact(column.topY(), 1);
        if (column.topY() < minY + 2 || y >= maxY) continue;
        int wx = originX + x, wz = originZ + z;
        long gx = Math.floorDiv(wx, spacing), gz = Math.floorDiv(wz, spacing);
        int ax = (int) (unit(savedSeed ^ X_SALT, gx, gz) * spacing);
        int az = (int) (unit(savedSeed ^ Z_SALT, gx, gz) * spacing);
        if (Math.floorMod(wx, spacing) != ax || Math.floorMod(wz, spacing) != az)
          continue;
        // Consume the continuous ecotone vegetation weight; policy scale remains
        // the saved global cap while the blended field supplies local density.
        double localDensity = policy.densityScale() * cover.vegetation();
        if (!(localDensity > 0) || unit(savedSeed ^ THIN_SALT, gx, gz) >= localDensity) continue;
        double speciesSum = cover.grass() + cover.podzol();
        if (!(speciesSum > 0)) continue;
        double fernWeight = cover.podzol() / speciesSum;
        Plant species = unit(savedSeed ^ SPECIES_SALT, gx, gz) < fernWeight
            ? Plant.FERN : Plant.SHORT_GRASS;
        result[z * SIDE + x] = new Placement(species, y);
        placed++;
      }
    return new V8VegetationColumnPlan(result, placed);
  }

  private static double unit(long seed, long x, long z) {
    long h = seed ^ x * 0x9e3779b97f4a7c15L ^ z * 0xd1b54a32d192ed03L;
    h = (h ^ h >>> 30) * 0xbf58476d1ce4e5b9L;
    h = (h ^ h >>> 27) * 0x94d049bb133111ebL;
    return ((h ^ h >>> 31) >>> 11) * 0x1.0p-53;
  }
}
