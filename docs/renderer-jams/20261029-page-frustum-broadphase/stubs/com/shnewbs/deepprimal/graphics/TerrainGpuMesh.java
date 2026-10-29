package com.shnewbs.deepprimal.graphics;

import net.minecraft.world.phys.AABB;

/** Test-only GPU-mesh stand-in exposing the production world-space bound. */
public final class TerrainGpuMesh {
  public final AABB bounds;
  public TerrainGpuMesh(AABB bounds) { this.bounds = bounds; }
}
