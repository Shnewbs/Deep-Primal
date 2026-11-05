package com.shnewbs.deepprimal.core.lod;

import java.util.*;
import java.security.MessageDigest;
import java.nio.ByteBuffer;

/** Real mesher input: mixed materials, fractional water, gaps and split output parts. */
public final class LodPartBoundsChecks {
  public static void main(String[] args) throws Exception {
    boolean strict = args.length == 0;
    var digest = MessageDigest.getInstance("SHA-256");
    int parts = 0, loose = 0, rejectedBefore = 0, rejectedAfter = 0;
    for (int floor : new int[] {-64, 0, 320}) {
      var builder = new ColumnarChunk.Builder(-17, -1, floor, 48, 7);
      for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
        int[] ids = new int[48];
        for (int y = 0; y < ids.length; y++)
          ids[y] = y < 8 ? 0 : 1 + Math.floorMod(x / 4 + z / 4 + y, 4);
        builder.column(ids);
      }
      var levels = LodMesher.build(builder.finish(List.of(
          "00000000|air", "ff809060|stone", "ff604020|soil",
          "e880a0ff|water8", "f080a0ff|water16")));
      for (int detail = 0; detail < 3; detail++) {
        var meshes = levels.level(detail);
        if (meshes.size() < 2) throw new AssertionError("Fixture must split at every detail");
        for (var mesh : meshes) {
          int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
          var bytes = ByteBuffer.allocate(4);
          for (int i = 0; i < mesh.positions().length; i++) {
            float v = mesh.positions()[i];
            digest.update(bytes.clear().putInt(Float.floatToIntBits(v)).array());
            if (i % 3 == 1) { low = Math.min(low, (int)Math.floor(v)); high = Math.max(high, (int)Math.ceil(v)); }
          }
          for (int[] values : new int[][] {mesh.colors(), mesh.normals(), mesh.indices()})
            for (int value : values) digest.update(bytes.clear().putInt(value).array());
          if (mesh.minimum() > low || mesh.maximum() < high) throw new AssertionError("Nonconservative bounds");
          if (mesh.minimum() != low || mesh.maximum() != high) loose++;
          // Exact vertical slab rejection oracle, including the renderer's existing +1 top pad.
          for (int slab = floor - 1; slab <= floor + 50; slab++) {
            boolean exact = high + 1 < slab || low > slab + 1;
            boolean actual = mesh.maximum() + 1 < slab || mesh.minimum() > slab + 1;
            if (actual && !exact) throw new AssertionError("Visible geometry rejected");
            if (actual) rejectedBefore++;
            if (exact) rejectedAfter++;
          }
          parts++;
        }
      }
    }
    System.out.println("LOD_PART_BOUNDS parts=" + parts + " loose=" + loose
        + " slabRejects=" + rejectedBefore + " exactRejects=" + rejectedAfter
        + " geometrySha256=" + HexFormat.of().formatHex(digest.digest()));
    if (strict && loose != 0) throw new AssertionError("Bounds retain unrelated earlier vertices: " + loose);
  }
}
