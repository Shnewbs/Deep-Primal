package com.shnewbs.deepprimal.core.lod;
import com.shnewbs.deepprimal.core.HorizonMesh;
import java.util.*;
/** Byte admission must include every detail, recompute mutable inputs, and avoid indexed linked-list traversal. */
public final class LodByteAccountingChecks {
  private static long oracle(List<HorizonMesh> meshes) {
    long total = 0;
    for (var mesh : meshes) total += mesh.colors().length * 20L + mesh.indices().length * 2L;
    return total;
  }
  private static final class Sequential extends LinkedList<HorizonMesh> {
    Sequential(List<HorizonMesh> source) { super(source); }
    public HorizonMesh get(int index) { throw new AssertionError("Indexed sequential traversal"); }
  }
  private static void check(LodMesher.Levels levels) {
    long expected = oracle(levels.fine()) + oracle(levels.medium()) + oracle(levels.coarse());
    if (levels.bytes() != expected) throw new AssertionError("Incorrect mesh byte admission");
  }
  public static void main(String[] args) throws Exception {
    for (int kind = 0; kind < 5; kind++) {
      var built = LodMesher.build(LodReductionChecks.scene(kind, -64));
      var changed = new ArrayList<>(built.fine());
      check(built);
      check(new LodMesher.Levels(new Sequential(built.fine()),
          new Sequential(built.medium()), new Sequential(built.coarse())));
      var mutable = new LodMesher.Levels(changed, built.medium(), built.coarse());
      check(mutable);
      changed.clear(); check(mutable);
      changed.addAll(built.medium()); changed.addAll(built.coarse()); check(mutable);
      check(new LodMesher.Levels(List.of(), built.medium(), List.of()));
      check(new LodMesher.Levels(built.fine(), List.of(), built.coarse()));
    }
    check(new LodMesher.Levels(List.of(), List.of(), List.of()));
    var random = new Random(71423);
    for (int sample = 0; sample < 500; sample++) {
      List<HorizonMesh> parts = new ArrayList<>();
      for (int i = 0, n = random.nextInt(20); i < n; i++)
        parts.add(new HorizonMesh(new float[0], new int[random.nextInt(512)],
            new int[random.nextInt(1024)], new int[0], new short[0], -64, 320));
      check(new LodMesher.Levels(parts, new Sequential(parts), List.copyOf(parts)));
    }
    LodScratchChecks.main(args);
    System.out.println("LOD_BYTE_ACCOUNTING_PASS scenes=5 randomizedLists=500 sequentialNoIndexedGet=true mutableRecomputed=true allDetails=true");
  }
}
