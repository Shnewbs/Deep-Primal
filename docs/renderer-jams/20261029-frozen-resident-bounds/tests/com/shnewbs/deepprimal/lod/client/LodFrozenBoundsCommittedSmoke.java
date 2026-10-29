package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Production-excluded frozen-AABB page-publication and mutation regression. */
public final class LodFrozenBoundsCommittedSmoke {
  private static long checks;
  private LodFrozenBoundsCommittedSmoke() {}
  private static void check(boolean ok) {
    if (!ok) throw new AssertionError("assertion " + (++checks));
    checks++;
  }
  private static AABB box(int x) {
    return new AABB(x, -2, 0, x + 1, 3, 2);
  }
  private static boolean hit(AABB a, int x) {
    return a != null && Double.isFinite(a.minX) && Double.isFinite(a.maxX)
        && a.maxX >= x - 1 && a.minX <= x + 1;
  }
  private static List<Integer> select(AABB[] frozen, AABB coarse, long[] runs, int x) {
    List<Integer> out = new ArrayList<>();
    if (LodPageFrustumBounds.ENABLED && coarse != null) {
      if (!hit(coarse, x)) return out;
      if (LodPageFrustumBounds.allPartsShareSnapshot(coarse, frozen)) {
        for (int i = 0; i < frozen.length; i++) out.add(i);
        return out;
      }
    }
    boolean prior = false;
    for (int i = 0; i < frozen.length; i++) {
      boolean v = LodPageFrustumBounds.ENABLED
          && LodPartFrustumRuns.repeatsPrevious(runs, i) ? prior : hit(frozen[i], x);
      if (v) out.add(i);
      prior = v;
    }
    return out;
  }
  public static void main(String[] args) {
    Random rng = new Random(36020261029L);
    for (int n = 0; n < 4_000; n++) {
      List<TerrainGpuMesh> parts = new ArrayList<>();
      for (int i = rng.nextInt(95); i > 0; i--) {
        AABB original = !parts.isEmpty() && rng.nextBoolean()
            ? parts.get(parts.size() - 1).bounds : box(rng.nextInt(80) - 40);
        parts.add(new TerrainGpuMesh(original));
      }
      AABB[][] frozen = LodPageFrustumBounds.snapshot(List.of(parts));
      AABB coarse = LodPageFrustumBounds.fromSnapshot(frozen)[0];
      long[] mask = LodPartFrustumRuns.fromSnapshot(frozen)[0];
      for (int i = 0; i < parts.size(); i++) {
        check(frozen[0][i] == parts.get(i).bounds);
        check(LodPartFrustumRuns.repeatsPrevious(mask, i) == (i > 0
            && LodPartFrustumRuns.sameFiniteBounds(frozen[0][i - 1], frozen[0][i])));
        parts.get(i).bounds = rng.nextBoolean() ? box(1000 + i) : null;
      }
      for (int x = -45; x <= 45; x += 15) {
        List<Integer> direct = new ArrayList<>();
        for (int i = 0; i < frozen[0].length; i++) if (hit(frozen[0][i], x)) direct.add(i);
        check(direct.equals(select(frozen[0], coarse, mask, x)));
      }
    }
    System.out.println("PASS LodFrozenBoundsCommittedSmoke assertions=" + checks
        + " broadphase=" + LodPageFrustumBounds.ENABLED);
  }
}
