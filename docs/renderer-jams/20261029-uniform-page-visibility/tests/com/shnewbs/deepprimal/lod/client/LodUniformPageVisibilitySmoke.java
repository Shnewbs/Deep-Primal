package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Deterministic CPU-only page culling equivalence, not native GPU pixels. */
public final class LodUniformPageVisibilitySmoke {
  private static int checks;

  private static void check(boolean value) {
    if (!value) throw new AssertionError("uniform-page check " + checks);
    checks++;
  }

  private static AABB box(double x, double z) {
    return new AABB(x, -12, z, x + 16, 64, z + 16);
  }

  private static boolean visible(AABB b, int camera) {
    if (b == null) return true; // malformed conservative bound: fine fallback
    double origin = camera == 0 ? 0 : 100;
    return b.maxX >= origin - 30 && b.minX <= origin + 30
        && b.maxZ >= origin - 30 && b.minZ <= origin + 30;
  }

  private static List<Integer> draw(List<TerrainGpuMesh> parts, AABB coarse,
      boolean reusePositive, int camera) {
    ArrayList<Integer> selected = new ArrayList<>();
    if (coarse != null) {
      if (!visible(coarse, camera)) return selected;
      if (reusePositive && LodPageFrustumBounds.allPartsShareBounds(coarse, parts)) {
        for (int i = 0; i < parts.size(); i++) selected.add(i);
        return selected;
      }
    }
    for (int i = 0; i < parts.size(); i++)
      if (visible(parts.get(i).bounds, camera)) selected.add(i);
    return selected;
  }

  public static void main(String[] args) {
    Random rng = new Random(0x31A4BE);
    for (int trial = 0; trial < 12000; trial++) {
      int count = 2 + rng.nextInt(16);
      int x = rng.nextInt(220) - 110, z = rng.nextInt(220) - 110;
      ArrayList<TerrainGpuMesh> parts = new ArrayList<>();
      for (int p = 0; p < count; p++) parts.add(new TerrainGpuMesh(box(x, z)));
      boolean uniform = trial % 3 != 0;
      if (!uniform) parts.set(1, new TerrainGpuMesh(box(x + 48, z)));
      AABB coarse = LodPageFrustumBounds.build(List.of(parts))[0];
      boolean marked = LodPageFrustumBounds.allPartsShareBounds(coarse, parts);
      check(marked == (LodPageFrustumBounds.ENABLED && uniform));
      for (int view = 0; view < 2; view++)
        check(draw(parts, coarse, false, view).equals(draw(parts, coarse, true, view)));
    }
    AABB invalid = new AABB(Double.NaN, 0, 0, 4, 4, 4);
    List<TerrainGpuMesh> malformed = List.of(new TerrainGpuMesh(box(0, 0)),
        new TerrainGpuMesh(invalid));
    check(LodPageFrustumBounds.build(List.of(malformed))[0] == null);
    check(!LodPageFrustumBounds.allPartsShareBounds(null, malformed));
    check(LodPageFrustumBounds.build(List.of(List.of()))[0] == null);
    System.out.println("UNIFORM_PAGE_SMOKE_PASS checks=" + checks
        + " broadphase=" + LodPageFrustumBounds.ENABLED);
  }
}
