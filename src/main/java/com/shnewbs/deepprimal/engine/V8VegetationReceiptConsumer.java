package com.shnewbs.deepprimal.engine;

import java.util.Objects;

/**
 * Explicit downstream consumer for postwrite V8-r6 vegetation observations.
 *
 * <p>This helper never mutates a chunk or invents placement state. It admits
 * the immutable receipt against the exact prepared profile and signed chunk,
 * then exposes only the planned sites that were actually observed after the
 * native setters completed. Callers must still own any later persistence or
 * gameplay authority.
 */
public final class V8VegetationReceiptConsumer {
  private V8VegetationReceiptConsumer() {}

  public record RealizedPlacement(int localX, int localZ,
                                  V8VegetationColumnPlan.Placement placement) {
    public RealizedPlacement {
      if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16)
        throw new IndexOutOfBoundsException("V8 realized local column");
      Objects.requireNonNull(placement, "V8 realized placement");
    }
  }

  public record RealizedVegetation(RealizedPlacement[] placements, int plannedCount,
                                  int realizedCount) {
    public RealizedVegetation {
      Objects.requireNonNull(placements, "V8 realized placements");
      if (plannedCount < 0 || realizedCount < 0 || realizedCount > plannedCount
          || realizedCount != placements.length)
        throw new IllegalArgumentException("V8 realized vegetation counts");
      placements = placements.clone();
    }

    @Override
    public RealizedPlacement[] placements() {
      return placements.clone();
    }
  }

  /**
   * Admit and project a native receipt for downstream consumers.
   *
   * <p>The returned projection is deterministic and bounded to the 16x16
   * destination chunk. Unobserved candidates are omitted; no alternate plant,
   * height or neighboring-chunk state is synthesized.
   */
  public static RealizedVegetation consume(
      V8GeneratorSnapshot.Prepared prepared,
      net.minecraft.world.level.chunk.ChunkAccess chunk,
      V8VegetationColumnPlan planned,
      V8VegetationNativeReceipt receipt) {
    Objects.requireNonNull(planned, "V8 planned vegetation");
    V8VegetationNativeReceipt admitted =
        DensityChunkMaterializer.requireCompatibleVegetationReceipt(prepared, chunk, receipt);
    var realized = new RealizedPlacement[admitted.realizedCount()];
    int index = 0;
    for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
      if (!admitted.realized(x, z)) continue;
      var placement = planned.placement(x, z);
      if (placement == null)
        throw new IllegalStateException("V8 receipt realized an unplanned site");
      realized[index++] = new RealizedPlacement(x, z, placement);
    }
    if (index != realized.length)
      throw new IllegalStateException("V8 receipt realized count changed during projection");
    return new RealizedVegetation(realized, admitted.plannedCount(), admitted.realizedCount());
  }
}
