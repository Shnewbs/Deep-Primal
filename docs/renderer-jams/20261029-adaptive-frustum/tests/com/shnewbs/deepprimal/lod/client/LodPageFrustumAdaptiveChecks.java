package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Source-exact CPU regression: no Minecraft classes or GPU mocks in production. */
public final class LodPageFrustumAdaptiveChecks {
  private static long assertions;
  private static void check(boolean condition, String reason) {
    assertions++;
    if (!condition) throw new AssertionError(reason + " #" + assertions);
  }

  private static boolean visible(AABB box) {
    return box.minX < 32 && box.maxX > -32 && box.minZ < 32 && box.maxZ > -32;
  }

  private static List<Integer> select(List<TerrainGpuMesh> parts, AABB union, boolean useCoarse) {
    var visibleParts = new ArrayList<Integer>();
    if (useCoarse && union != null && !visible(union)) return visibleParts;
    for (int i = 0; i < parts.size(); i++)
      if (visible(parts.get(i).bounds)) visibleParts.add(i);
    return visibleParts;
  }

  public static void main(String[] args) {
    boolean adapt = !"false".equalsIgnoreCase(
        System.getProperty("deepprimal.lod.pageFrustumAutoSkip", "true"));
    check(LodPageFrustumAdaptiveGate.ENABLED == adapt, "startup setting");
    var gate = new LodPageFrustumAdaptiveGate();
    check(gate.shouldTestPages(), "initial sample");
    gate.completed(true, 64, 0);
    for (int i = 0; i < 3; i++)
      check(gate.shouldTestPages() == !adapt, "bounded skip " + i);
    check(gate.shouldTestPages(), "sample again");
    gate.completed(true, 64, 65);
    check(gate.shouldTestPages(), "keep coarse when saves more part tests");
    gate.completed(true, 64, 64);
    check(gate.shouldTestPages() == !adapt, "equal work triggers bypass");
    gate.reset();
    gate.completed(true, 31, 0);
    check(gate.shouldTestPages(), "small sample not suppressed");
    gate.completed(false, 64, 0);
    check(gate.shouldTestPages(), "unsampled frame not accepted as evidence");
    gate.completed(true, 64, 1);
    gate.reset();
    check(gate.shouldTestPages(), "world reset");

    // A modeled extract intentionally feeds the policy only completed frames.
    // It compares the actual production cached AABB union with fine-part order.
    var random = new Random(0x2716A55L);
    gate.reset();
    long sampled = 0, bypassed = 0, coarseRejected = 0;
    for (int frame = 0; frame < 180; frame++) {
      boolean coarse = gate.shouldTestPages();
      int tested = 0, avoided = 0;
      for (int page = 0; page < 128; page++) {
        var parts = new ArrayList<TerrainGpuMesh>();
        int count = 2 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
          double x = random.nextInt(256) - 128;
          double z = random.nextInt(256) - 128;
          parts.add(new TerrainGpuMesh(new AABB(x, -5, z, x + 6, 5, z + 6)));
        }
        AABB box = LodPageFrustumBounds.build(List.of(parts))[0];
        check(box != null, "multi-part bounds");
        List<Integer> original = select(parts, box, false);
        List<Integer> candidate = select(parts, box, coarse);
        check(original.equals(candidate), "order/visibility parity");
        if (coarse) {
          tested++;
          if (!visible(box)) avoided += parts.size();
        } else bypassed++;
      }
      if (coarse) {
        sampled++;
        coarseRejected += avoided;
      }
      gate.completed(coarse, tested, avoided);
    }
    check(sampled > 0 && coarseRejected > 0, "samples and rejection witnesses");
    if (adapt) check(bypassed > 0, "adaptation applied");
    else check(bypassed == 0, "disabled policy remains fixed coarse");

    // No complete frame = no policy update even when a later test throws.
    gate.reset();
    check(gate.shouldTestPages(), "aborted sample first");
    check(gate.shouldTestPages(), "aborted sample must not suppress");
    System.out.println("LOD_PAGE_FRUSTUM_ADAPTIVE_PASS adapt=" + adapt
        + " assertions=" + assertions + " sampledFrames=" + sampled
        + " bypassedPages=" + bypassed + " avoidedPartTests=" + coarseRejected);
  }
}
