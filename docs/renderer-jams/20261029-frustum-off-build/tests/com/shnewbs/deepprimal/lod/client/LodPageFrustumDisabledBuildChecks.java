package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;

/** Source-matched regression with API doubles, not actual GPU frustum pixels. */
public final class LodPageFrustumDisabledBuildChecks {
  private static long assertions;
  private LodPageFrustumDisabledBuildChecks() {}
  private static void check(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError("assertion " + assertions + ": " + message);
  }

  private static List<List<TerrainGpuMesh>> fixture(int levels, int parts) {
    ArrayList<List<TerrainGpuMesh>> all = new ArrayList<>();
    for (int l=0;l<levels;l++) {
      ArrayList<TerrainGpuMesh> many = new ArrayList<>();
      for (int i=0;i<parts;i++)
        many.add(new TerrainGpuMesh(new AABB(i, l-1, -i, i+1,l+1, i+1)));
      all.add(List.copyOf(many));
    }
    return List.copyOf(all);
  }

  public static void main(String[] args) {
    boolean enabled = !"false".equalsIgnoreCase(
        System.getProperty("deepprimal.lod.pageFrustumBroadphase", "true"));
    check(LodPageFrustumBounds.ENABLED == enabled, "startup flag parity");
    List<List<TerrainGpuMesh>> parts = fixture(3, 48);
    long constructionsBefore = AABB.created;
    AABB[] unions = LodPageFrustumBounds.build(parts);
    check(unions.length == 3, "level count");
    long coarseAllocations = AABB.created - constructionsBefore;
    if (enabled) {
      check(AABB.created == constructionsBefore + 3, "one conservative union per multi-part level");
      for (int l=0;l<3;l++) {
        AABB u = unions[l];
        check(u != null, "enabled union exists");
        check(u.minX==0 && u.maxX==48 && u.minY==l-1 && u.maxY==l+1,
            "conservative horizontal and vertical union");
        check(u.minZ==-47 && u.maxZ==48,"conservative depth union");
      }
    } else {
      check(AABB.created == constructionsBefore,
          "disabled A/B must not construct unused coarse bounds");
      for (AABB u: unions) check(u == null,"disabled A/B must keep fine-only path");
    }
    if (!enabled) {
      List<List<TerrainGpuMesh>> guarded = new java.util.AbstractList<>() {
        @Override public int size() { return 3; }
        @Override public List<TerrainGpuMesh> get(int index) {
          throw new AssertionError("disabled mode must never inspect mesh contents");
        }
      };
      AABB[] none = LodPageFrustumBounds.build(guarded);
      check(none.length == 3 && none[0] == null && none[1] == null && none[2] == null,
          "disabled mode only reads level count");
    }
    for (int l=0;l<3;l++) {
      check(parts.get(l).size()==48,"original mesh list preserved");
      for (int i=0;i<48;i++)
        check(parts.get(l).get(i).bounds.minX == i,"part and ordering preserved");
    }
    List<List<TerrainGpuMesh>> invalid=List.of(
        List.of(new TerrainGpuMesh(new AABB(0,0,0,1,1,1)),
            new TerrainGpuMesh(new AABB(Double.NaN,0,0,1,1,1))));
    check(LodPageFrustumBounds.build(invalid)[0] == null,
        "invalid page never produces false-negative coarse bounds");
    System.out.println("LOD_FRUSTUM_DISABLED_BUILD_PASS enabled="+enabled+
        " assertions="+assertions+" allocatedCoarseAabbs="+coarseAllocations);
  }
}
