package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.HorizonMesh;
import com.shnewbs.deepprimal.core.lod.LodMesher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Optional, worker-thread LOD upload partitioner. The current LodMesher emits independent
 * four-vertex/six-index quads. Splitting only that layout preserves the exact triangle,
 * material, normal, light-alpha and draw order of every source part. All pieces retain the
 * original conservative bounds so culling cannot inadvertently reveal a hole.
 *
 * <p>Shared-vertex/legacy meshes are returned unchanged. This is a diagnostic experiment,
 * not a claim of reduced whole-frame time; small parts require more GPU allocations/draws.
 */
final class LodMeshPartSplitter {
  static final long QUAD_GPU_BYTES = 4L * 20 + 6L * 2;
  static final int MAX_PARTS_PER_LEVEL = 1024;

  private LodMeshPartSplitter() {}

  static LodMesher.Levels split(LodMesher.Levels levels, int targetKiB) {
    Objects.requireNonNull(levels, "levels");
    if (targetKiB < 0) throw new IllegalArgumentException("negative part budget");
    if (targetKiB == 0) return levels;
    long targetBytes = targetKiB * 1024L;
    List<HorizonMesh> fine = splitLevel(levels.fine(), targetBytes);
    List<HorizonMesh> medium = splitLevel(levels.medium(), targetBytes);
    List<HorizonMesh> coarse = splitLevel(levels.coarse(), targetBytes);
    return fine == levels.fine() && medium == levels.medium() && coarse == levels.coarse()
        ? levels : new LodMesher.Levels(fine, medium, coarse);
  }

  static List<HorizonMesh> splitLevel(List<HorizonMesh> meshes, long targetBytes) {
    Objects.requireNonNull(meshes, "meshes");
    if (targetBytes < 0) throw new IllegalArgumentException("negative part budget");
    if (targetBytes < QUAD_GPU_BYTES) return meshes;
    long maxQuads = Math.max(1L, targetBytes / QUAD_GPU_BYTES);
    int quadsPerPart = (int) Math.min(1024L, maxQuads);
    int planned = 0;
    boolean changed = false;
    // Record the validated split plan once. The old material-preserving topology
    // scan was repeated during emission for every oversized canonical mesh.
    int[] plannedParts = new int[Math.min(meshes.size(), MAX_PARTS_PER_LEVEL)];
    // Preflight before allocating any part arrays. A cap failure keeps the original page.
    for (int meshIndex = 0; meshIndex < meshes.size(); meshIndex++) {
      HorizonMesh mesh = meshes.get(meshIndex);
      if (Thread.currentThread().isInterrupted())
        throw new java.util.concurrent.CancellationException();
      Objects.requireNonNull(mesh, "mesh");
      int count = mesh.gpuBytes() > targetBytes && canonicalQuads(mesh)
          ? (mesh.vertices() / 4 + quadsPerPart - 1) / quadsPerPart : 1;
      if (count > 1) changed = true;
      if (count > MAX_PARTS_PER_LEVEL - planned) return meshes;
      plannedParts[meshIndex] = count;
      planned += count;
    }
    if (!changed) return meshes;
    ArrayList<HorizonMesh> parts = new ArrayList<>(planned);
    for (int meshIndex = 0; meshIndex < meshes.size(); meshIndex++) {
      HorizonMesh mesh = meshes.get(meshIndex);
      if (Thread.currentThread().isInterrupted())
        throw new java.util.concurrent.CancellationException();
      // Preflight already checked the exact topology (or chose pass-through).
      // Never silently transform an unrecognized/legacy water or roof mesh.
      if (plannedParts[meshIndex] == 1) {
        parts.add(mesh);
        continue;
      }
      int totalQuads = mesh.vertices() / 4;
      for (int q = 0; q < totalQuads; q += quadsPerPart) {
        if (Thread.currentThread().isInterrupted())
          throw new java.util.concurrent.CancellationException();
        int take = Math.min(quadsPerPart, totalQuads - q);
        int firstVertex = q * 4, firstIndex = q * 6;
        int[] indices = Arrays.copyOfRange(mesh.indices(), firstIndex, firstIndex + take * 6);
        for (int i = 0; i < indices.length; i++) indices[i] -= firstVertex;
        parts.add(new HorizonMesh(
            Arrays.copyOfRange(mesh.positions(), firstVertex * 3, (firstVertex + take * 4) * 3),
            Arrays.copyOfRange(mesh.colors(), firstVertex, firstVertex + take * 4),
            indices,
            Arrays.copyOfRange(mesh.normals(), firstVertex, firstVertex + take * 4),
            new short[0], mesh.minimum(), mesh.maximum()));
      }
    }
    return List.copyOf(parts);
  }

  /** Conservative: never rewrite an unfamiliar index topology or shared roof vertices. */
  private static boolean canonicalQuads(HorizonMesh mesh) {
    int vertices = mesh.vertices();
    if (vertices == 0 || vertices % 4 != 0 || mesh.heights().length != 0
        || mesh.positions().length != 3L * vertices || mesh.normals().length != vertices
        || mesh.indices().length != 6L * (vertices / 4)) return false;
    int[] indices = mesh.indices();
    for (int quad = 0; quad < vertices / 4; quad++) {
      int v = quad * 4, i = quad * 6;
      if (indices[i] != v || indices[i + 1] != v + 1 || indices[i + 2] != v + 2
          || indices[i + 3] != v || indices[i + 4] != v + 2 || indices[i + 5] != v + 3)
        return false;
    }
    return true;
  }
}
