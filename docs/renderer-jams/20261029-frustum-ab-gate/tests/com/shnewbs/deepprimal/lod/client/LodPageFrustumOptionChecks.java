package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Executable against the actual production helper, never packaged into the mod. */
public final class LodPageFrustumOptionChecks {
  private static int assertions;
  private static void check(boolean value, String label) {
    assertions++;
    if (!value) throw new AssertionError(label + " #" + assertions);
  }
  private static boolean visible(AABB box) {
    return box != null && box.minX <= 32 && box.maxX >= -32
        && box.minZ <= 32 && box.maxZ >= -32;
  }
  private static List<Integer> select(List<TerrainGpuMesh> parts, AABB union,
      boolean coarse) {
    List<Integer> result = new ArrayList<>();
    if (coarse && union != null && !visible(union)) return result;
    for (int i = 0; i < parts.size(); i++)
      if (visible(parts.get(i).bounds)) result.add(i);
    return result;
  }
  public static void main(String[] args) {
    boolean expected = !"false".equalsIgnoreCase(
        System.getProperty("deepprimal.lod.pageFrustumBroadphase", "true"));
    check(LodPageFrustumBounds.ENABLED == expected, "startup JVM override");
    Random random = new Random(0x10D6B05L);
    long rejected = 0, visiblePages = 0;
    for (int trial = 0; trial < 20000; trial++) {
      List<TerrainGpuMesh> parts = new ArrayList<>();
      int count = random.nextInt(9);
      for (int j = 0; j < count; j++) {
        double x = random.nextInt(320) - 160;
        double z = random.nextInt(320) - 160;
        parts.add(new TerrainGpuMesh(new AABB(x, -10, z, x + 4, 10, z + 4)));
      }
      List<TerrainGpuMesh> before = List.copyOf(parts);
      AABB union = LodPageFrustumBounds.build(List.of(parts))[0];
      check((union == null) == (count <= 1), "cached union eligibility");
      check(select(parts, union, false).equals(
          select(parts, union, LodPageFrustumBounds.ENABLED)), "ordered fine parity");
      check(before.equals(parts), "input immutability");
      if (union != null && expected) {
        if (visible(union)) visiblePages++;
        else rejected++;
      }
    }
    check((rejected + visiblePages > 0) == expected, "no coarse calls when disabled");
    if (expected) {
      check(rejected > 0, "offscreen rejection witness");
      check(visiblePages > 0, "all-visible overhead witness");
    }
    var malformed = List.of(new TerrainGpuMesh(new AABB(0, 0, 0, 2, 1, 2)),
        new TerrainGpuMesh(new AABB(Double.NaN, 0, 0, 2, 1, 2)));
    check(LodPageFrustumBounds.build(List.of(malformed))[0] == null,
        "invalid bounds keep fine path");
    System.out.println("LOD_PAGE_FRUSTUM_OPTION_PASS enabled=" + expected
        + " assertions=" + assertions + " coarseRejected=" + rejected
        + " coarseVisible=" + visiblePages);
  }
}
