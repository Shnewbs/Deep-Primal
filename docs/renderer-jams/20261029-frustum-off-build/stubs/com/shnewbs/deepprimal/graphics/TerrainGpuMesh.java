package com.shnewbs.deepprimal.graphics;
import net.minecraft.world.phys.AABB;
/** Test-only minimal shape of GPU part bounds. */
public final class TerrainGpuMesh {
  public final AABB bounds;
  public TerrainGpuMesh(AABB bounds) { this.bounds=bounds; }
}
