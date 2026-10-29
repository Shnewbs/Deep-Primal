package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import java.util.Objects;

/**
 * Bounded, coordinate-stable surface preflight for explicit V8 revision-3 new-chunk
 * generation. Climate input must be sampled at absolute world coordinates from
 * the same saved climate producer. It cannot be a chunk-local random stream.
 * It never modifies density, physical water, a chunk or a saved profile.
 */
public final class V8EcotoneColumnPlan {
  @FunctionalInterface
  public interface ClimateSource {
    ClimateDatum sample(int worldX, int topY, int worldZ);
  }

  /** The hydrology engine, not the ecotone sampler, owns water-domain identity. */
  public record ClimateDatum(EcotoneBlend.Climate climate, EcotoneBlend.Domain domain) {
    public ClimateDatum {
      Objects.requireNonNull(climate, "climate");
      Objects.requireNonNull(domain, "domain");
    }
  }

  /** Absent outside eligible dry-land surfaces. Density/bedrock is authoritative. */
  public record Column(EcotoneSurfaceField.Sample cover, int soilDepth, int topY) {
    public Column {
      Objects.requireNonNull(cover, "cover");
      if (soilDepth < 1 || soilDepth > 16)
        throw new IllegalArgumentException("V8 soil depth outside 1..16");
    }
  }

  private final Column[] columns;
  private V8EcotoneColumnPlan(Column[] columns) {
    this.columns = columns.clone();
  }

  public Column column(int localX, int localZ) {
    if (localX < 0 || localX >= DensityChunk.WIDTH
        || localZ < 0 || localZ >= DensityChunk.WIDTH)
      throw new IndexOutOfBoundsException("V8 local column");
    return columns[localZ * DensityChunk.WIDTH + localX];
  }

  public int eligibleColumns() {
    int n = 0;
    for (Column column : columns) if (column != null) n++;
    return n;
  }

  /**
   * Resolve every climate callback, blended cover and soil depth before the
   * native setter is invoked. Height, domain, profile revision and seed are
   * bound to the already prepared immutable snapshot.
   */
  public static V8EcotoneColumnPlan build(V8GeneratorSnapshot.Prepared prepared,
      DensityChunk density, int originX, int originZ, ClimateSource source) {
    Objects.requireNonNull(prepared, "prepared");
    Objects.requireNonNull(density, "density");
    Objects.requireNonNull(source, "source");
    if (prepared.snapshot().version() < V8GeneratorSnapshot.SURFACE_VERSION
        || prepared.snapshot().version() > V8GeneratorSnapshot.VEGETATION_VERSION)
      throw new IllegalArgumentException("explicit V8 revision 3 required for native ecotone material plan");
    int minY = density.minY(), maxY = density.maxYExclusive();
    prepared.requireHeight(minY, maxY - minY);
    Column[] resolved = new Column[DensityChunk.WIDTH * DensityChunk.WIDTH];
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        int top = density.surfaceExclusive(x, z) - 1;
        if (top < minY + 2 || top >= maxY) continue;
        int worldX = Math.addExact(originX, x);
        int worldZ = Math.addExact(originZ, z);
        var input = Objects.requireNonNull(source.sample(worldX, top, worldZ), "V8 climate datum");
        if (input.domain() != EcotoneBlend.Domain.LAND) continue;
        var cover = prepared.previewEcotoneSurface(worldX, worldZ,
            input.climate(), input.domain());
        // Soil factor is the continuous all-biome weighted value; the integer
        // layer count is capped to the existing saved ground-profile depth.
        int maximum = prepared.surface().soilDepth();
        int depth = Math.max(1, Math.min(maximum,
            (int) Math.round(maximum * cover.soilFactor())));
        resolved[z * DensityChunk.WIDTH + x] = new Column(cover, depth, top);
      }
    return new V8EcotoneColumnPlan(resolved);
  }
}
