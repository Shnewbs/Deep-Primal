package com.shnewbs.deepprimal.core.lod;

import com.shnewbs.deepprimal.core.HorizonMesh;
import java.util.*;
import java.security.MessageDigest;
import java.nio.ByteBuffer;

/** Frozen mesh signatures cover solid/water priority, air gaps and all three details. */
public final class LodReductionChecks {
  public static ColumnarChunk scene(int kind, int floor) {
    int height = kind == 4 ? 256 : 64;
    var b = new ColumnarChunk.Builder(-17, -3, floor, height, 11);
    for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
      int[] ids = new int[height];
      for (int y = 0; y < height; y++) {
        ids[y] = switch(kind) {
          case 0 -> y < height / 2 ? 1 : 0;
          case 1 -> y < 4 || y >= 25 && y < 35 ? 1 + (x + z) % 2 : 0;
          case 2 -> y < 8 + x * 3 ? 1 + z % 2 : 0;
          case 3 -> y < 12 ? 1 : y < 17 ? 3 + (x + z) % 2 : y == 17 && x % 4 == 2 ? 2 : 0;
          default -> y < 200 ? 1 : y < 210 ? 3 : 0;
        };
      }
      b.column(ids);
    }
    return b.finish(List.of("00000000|air", "ff809060|stone", "ff604020|soil",
        "e880a0ff|water8", "f080a0ff|water16"));
  }

  public static String signature(List<List<HorizonMesh>> levels) throws Exception {
    var digest = MessageDigest.getInstance("SHA-256");
    var bytes = ByteBuffer.allocate(4);
    for (var meshes : levels) {
      digest.update(bytes.clear().putInt(meshes.size()).array());
      for (var mesh : meshes) {
        for (int[] values : new int[][] {new int[]{mesh.minimum(), mesh.maximum()},
            mesh.colors(), mesh.normals(), mesh.indices()}) {
          digest.update(bytes.clear().putInt(values.length).array());
          for (int v : values) digest.update(bytes.clear().putInt(v).array());
        }
        digest.update(bytes.clear().putInt(mesh.positions().length).array());
        for (float v : mesh.positions()) digest.update(bytes.clear().putInt(Float.floatToIntBits(v)).array());
        for (short v : mesh.heights()) digest.update(bytes.clear().putInt(v).array());
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  public static void main(String[] args) throws Exception {
    String[] expected = {"ee936654b3d310db5e7b5a0136dbcf25b5976c4e4f559c618d8122cb13b95487","e58f6d28148a5b433ac43df1c8be805b018d8d8dc7581b29ccde344661f34856","e66b60151a613744ee331b4604f5c62d1775b363572c8e2954f1fe16f3f991b0","f865bfa109e1563ac20563e604ced141f15a04c3a8dfd1c5a90571236c16c088","7e90c5234bee8e1a7b7891ca94b7effa92483a444cb19dfba68ccdee483db3aa","c42080e50edaabbd185082e3ce7786ac0b8d40280264e78b60756f6be5405ff6","aafa88a8f3253d78a1d7bc42795a8698c2a9b3f647f2df1e5fdc2cac7f2d761a","d7cce30f55f5a9a1201eeda189da431093cc7795e1e85ba66a547a247e5ec1d3","3e9ff3e553604ddd8c752b393eda49ad896156a18662a3bcedcfbc300565df2a","7d425651d8cf6ba6e16621f9d983e3fc72544b1318e9e8a7866d3a3651bef185"};
    int index = 0;
    for (int kind = 0; kind < 5; kind++) for (int floor : new int[]{-64, 320}) {
      var levels = LodMesher.build(scene(kind, floor));
      String actual = signature(List.of(levels.fine(), levels.medium(), levels.coarse()));
      if (!actual.equals(expected[index++])) throw new AssertionError("LOD geometry changed: " + kind + " " + floor);
      System.out.println("LOD_REDUCTION_SIGNATURE " + kind + " " + floor + " " + actual);
    }
  }
}
