package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.ChunkMaterializer.ChunkKey;
import com.shnewbs.deepprimal.core.ChunkMaterializer.MaterializedChunk;
import com.shnewbs.deepprimal.core.ChunkMaterializer.WaterData;
import com.shnewbs.deepprimal.core.density.AdvancedDensityField;
import com.shnewbs.deepprimal.core.density.DensityChunk;

/** Small real-shape materialization for core checks; no live terrain or renderer required. */
final class PipelineTestFixtures {
  private static final DensityChunk EMPTY_DENSITY =
      DensityChunk.sample((x, y, z) -> -1.0, 0, 0, 0, 1);
  private static final AdvancedDensityField.MeshData EMPTY_MESH =
      new AdvancedDensityField.MeshData(new float[0], new int[0], new byte[0], new short[0]);
  private static final WaterData EMPTY_WATER =
      new WaterData(new float[256], new float[256], new float[256]);

  static MaterializedChunk chunk(ChunkKey key) {
    return new MaterializedChunk(key, EMPTY_MESH, EMPTY_WATER, EMPTY_DENSITY);
  }

  private PipelineTestFixtures() {}
}
