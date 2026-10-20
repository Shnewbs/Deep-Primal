package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.worldgen.OceanHeight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;

/** Find seeded offshore relief without loading chunks, then check one complete native column. */
final class OceanServerChecks {
  record Point(int x, int z, double expected) {}

  static Point find(ServerLevel level) {
    OceanHeight[] field = {null};
    level
        .getChunkSource()
        .randomState()
        .router()
        .preliminarySurfaceLevel()
        .mapAll(
            f -> {
              if (f instanceof OceanHeight h) field[0] = h;
              return f;
            });
    if (field[0] == null) throw new AssertionError("Ocean density missing from new profile");
    Point best = null;
    var source = level.getChunkSource().getGenerator().getBiomeSource();
    for (int z = -8192; z <= 8192; z += 256)
      for (int x = -8192; x <= 8192; x += 256) {
        var biome =
            source.getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(96),
                QuartPos.fromBlock(z),
                level.getChunkSource().randomState().sampler());
        if (!biome.is(BiomeTags.IS_OCEAN)
            || biome.value().coldEnoughToSnow(new BlockPos(x, 96, z), level.getSeaLevel()))
          continue;
        double h =
            field[0].compute(
                new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, 0, z));
        if (best == null || h < best.expected()) best = new Point(x, z, h);
      }
    if (best == null) throw new AssertionError("No unfrozen ocean sample");
    return best;
  }

  static void run(ServerLevel level) {
    var point = find(level);
    var pos = new BlockPos(point.x(), level.getSeaLevel() - 1, point.z());
    level.getChunkAt(pos);
    if (!level.getFluidState(pos).is(FluidTags.WATER))
      throw new AssertionError("Ocean surface is not water");
    int bed = pos.getY();
    while (bed > level.getMinY() && level.getFluidState(pos.atY(bed)).is(FluidTags.WATER)) bed--;
    int threshold = level.getMinY() < -128 ? -280 : -60;
    if (bed > threshold || !level.getBlockState(pos.atY(bed)).blocksMotion())
      throw new AssertionError("Native trench bed too shallow or unsupported: " + bed);
    DrainageServerChecks.record(
        "OCEAN_TRENCH_PASS x="
            + point.x()
            + " z="
            + point.z()
            + " bed="
            + bed
            + " waterDepth="
            + (level.getSeaLevel() - 1 - bed));
  }

  private OceanServerChecks() {}
}
