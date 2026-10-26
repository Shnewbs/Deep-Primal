package com.shnewbs.deepprimal.core.lod;

import java.util.*;
import java.util.concurrent.*;

/** Validate immutable output ownership when later levels/builds reuse private scratch. */
public final class LodScratchChecks {
  private static String signature(LodMesher.Levels l) throws Exception {
    return LodReductionChecks.signature(List.of(l.fine(), l.medium(), l.coarse()));
  }
  public static void main(String[] args) throws Exception {
    LodReductionChecks.main(args);
    LodPartBoundsChecks.main(args);
    var saved = LodMesher.build(LodReductionChecks.scene(3, -64));
    String frozen = signature(saved);
    var identities = new IdentityHashMap<Object, Boolean>();
    int arrays = 0;
    for (int detail = 0; detail < 3; detail++) for (var mesh : saved.level(detail)) {
      for (Object array : new Object[]{mesh.positions(), mesh.colors(), mesh.normals(),
          mesh.indices(), mesh.heights()}) {
        if (identities.put(array, true) != null) throw new AssertionError("Output array alias");
        arrays++;
      }
    }
    for (int i = 0; i < 25; i++) LodMesher.build(LodReductionChecks.scene(i % 5, 320));
    if (!signature(saved).equals(frozen)) throw new AssertionError("Later build mutated output");
    var executor = Executors.newFixedThreadPool(4);
    try {
      List<Future<String>> futures = new ArrayList<>();
      for (int i = 0; i < 64; i++) {
        final int kind = i % 5;
        futures.add(executor.submit(() -> signature(LodMesher.build(LodReductionChecks.scene(kind, -64)))));
      }
      for (int i = 0; i < futures.size(); i++) {
        String serial = signature(LodMesher.build(LodReductionChecks.scene(i % 5, -64)));
        if (!futures.get(i).get().equals(serial)) throw new AssertionError("Parallel build differs");
      }
    } finally { executor.shutdownNow(); }
    if (!signature(saved).equals(frozen)) throw new AssertionError("Parallel build mutated output");
    var tallest = new ColumnarChunk.Builder(-17, -3, -2048, 4096, 99);
    int[] solid = new int[4096];
    Arrays.fill(solid, 1);
    for (int column = 0; column < 256; column++) tallest.column(solid);
    var tall = LodMesher.build(tallest.finish(List.of("00000000|air", "ff809060|stone")));
    for (int detail = 0; detail < 3; detail++) {
      var meshes = tall.level(detail);
      if (meshes.size() != 1 || meshes.get(0).vertices() != 24
          || meshes.get(0).minimum() != -2048 || meshes.get(0).maximum() != 2048)
        throw new AssertionError("Maximum-height box differs");
    }
    Thread.currentThread().interrupt();
    try {
      LodMesher.build(LodReductionChecks.scene(0, -64));
      throw new AssertionError("Cancellation ignored");
    } catch (CancellationException expected) {
      // No shared scratch may mutate a previously returned mesh on cancellation either.
    } finally { Thread.interrupted(); }
    if (!signature(saved).equals(frozen)) throw new AssertionError("Cancelled build mutated output");
    System.out.println("LOD_MAX_HEIGHT_AND_CANCEL_PASS height=4096 details=3 outputStable=true");
    System.out.println("LOD_SCRATCH_PASS noOutputAliasing=true arrays=" + arrays
        + " retainedSnapshotStable=true laterSerialBuilds=25 parallelJobs=64");
  }
}
