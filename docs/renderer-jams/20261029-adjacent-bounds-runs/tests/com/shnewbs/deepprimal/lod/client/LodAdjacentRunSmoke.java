package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Production-excluded smoke with test-only Minecraft mesh/AABB doubles. */
public final class LodAdjacentRunSmoke {
  private static int checks;
  private LodAdjacentRunSmoke() {}
  private static void require(boolean condition) {
    checks++;
    if (!condition) throw new AssertionError("check " + checks);
  }
  private static AABB box(int n) {
    return new AABB(n, 0, -n, n + 1, 5, 1 - n);
  }
  private static List<Integer> visible(List<TerrainGpuMesh> page, boolean runs, int center,
      int[] calls) {
    List<Integer> selected = new ArrayList<>();
    AABB prior = null;
    boolean priorVisible = false;
    for (int i = 0; i < page.size(); i++) {
      AABB current = page.get(i).bounds;
      boolean reused = runs && LodPartFrustumRuns.sameFiniteBounds(prior, current);
      boolean hit = reused ? priorVisible : current != null && current.maxX >= center - 4
          && current.minX <= center + 4;
      if (!reused) calls[0]++;
      if (hit) selected.add(i);
      prior = current;
      priorVisible = hit;
    }
    return selected;
  }
  private static void parity(List<TerrainGpuMesh> page, int center) {
    boolean[] flags = LodPartFrustumRuns.build(List.of(page));
    boolean hasRun = false;
    for (int i = 1; i < page.size(); i++)
      hasRun |= LodPartFrustumRuns.sameFiniteBounds(page.get(i - 1).bounds, page.get(i).bounds);
    require(flags.length == 1 && flags[0] == hasRun);
    int[] oldCalls = {0}, newCalls = {0};
    List<Integer> baseline = visible(page, false, center, oldCalls);
    List<Integer> candidate = visible(page, flags[0], center, newCalls);
    require(candidate.equals(baseline));
    require(oldCalls[0] == page.size());
    require(newCalls[0] <= oldCalls[0]);
  }
  public static void main(String[] args) {
    AABB a = box(0), b = box(10), reversed = new AABB(5, 0, 0, 4, 5, 1);
    require(LodPartFrustumRuns.sameFiniteBounds(a, box(0)));
    require(!LodPartFrustumRuns.sameFiniteBounds(a, b));
    require(!LodPartFrustumRuns.sameFiniteBounds(reversed, reversed));
    require(!LodPartFrustumRuns.sameFiniteBounds(null, null));
    require(!LodPartFrustumRuns.sameFiniteBounds(
        new AABB(Double.NaN, 0, 0, 1, 1, 1), new AABB(Double.NaN, 0, 0, 1, 1, 1)));
    List<TerrainGpuMesh> mixed = List.of(new TerrainGpuMesh(a),
        new TerrainGpuMesh(box(0)), new TerrainGpuMesh(box(0)),
        new TerrainGpuMesh(b), new TerrainGpuMesh(box(10)), new TerrainGpuMesh(a));
    int[] calls = {0};
    require(visible(mixed, true, 0, calls).equals(visible(mixed, false, 0, new int[1])));
    require(calls[0] == 3); // Six mixed-page parts, three distinct adjacent runs.
    parity(mixed, 0);
    parity(List.of(), 0);
    Random rng = new Random(735291);
    for (int n = 0; n < 10_000; n++) {
      ArrayList<TerrainGpuMesh> parts = new ArrayList<>();
      for (int i = rng.nextInt(90); i > 0; i--) {
        int pick = rng.nextInt(8);
        AABB previous = parts.isEmpty() ? null : parts.get(parts.size() - 1).bounds;
        AABB bounds = pick < 4 && previous != null ? box((int) previous.minX)
            : pick == 4 ? reversed : pick == 5 ? null : box(rng.nextInt(40) - 20);
        parts.add(new TerrainGpuMesh(bounds));
      }
      parity(parts, rng.nextInt(50) - 25);
    }
    System.out.println("PASS LodAdjacentRunSmoke checks=" + checks);
  }
}
