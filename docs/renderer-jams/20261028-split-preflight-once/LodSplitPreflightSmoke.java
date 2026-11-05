package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.HorizonMesh;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Small source-only test; comprehensive paired oracle is in the jam evidence archive. */
public final class LodSplitPreflightSmoke {
  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  public static void main(String[] args) {
    Random random = new Random(202610281952L);
    int assertions = 0;
    for (int iteration = 0; iteration < 256; iteration++) {
      int quads = 1 + random.nextInt(1024);
      float[] positions = new float[quads * 12];
      int[] colors = new int[quads * 4];
      int[] normals = new int[quads * 4];
      int[] indices = new int[quads * 6];
      for (int i = 0; i < colors.length; i++) {
        colors[i] = random.nextInt();
        normals[i] = random.nextInt();
        for (int j = 0; j < 3; j++) positions[i * 3 + j] = random.nextFloat();
      }
      for (int q = 0; q < quads; q++) {
        int v = q * 4, i = q * 6;
        indices[i] = v; indices[i + 1] = v + 1; indices[i + 2] = v + 2;
        indices[i + 3] = v; indices[i + 4] = v + 2; indices[i + 5] = v + 3;
      }
      HorizonMesh source = new HorizonMesh(positions, colors, indices, normals, new short[0], -12, 64);
      List<HorizonMesh> split = LodMeshPartSplitter.splitLevel(List.of(source), 32 * 1024);
      int local = 0;
      for (HorizonMesh part : split) {
        require(part.minimum() == -12 && part.maximum() == 64, "bounds");
        for (int v = 0; v < part.vertices(); v++) {
          require(part.colors()[v] == source.colors()[local + v], "colors and opacity");
          require(part.normals()[v] == source.normals()[local + v], "normals");
          for (int axis = 0; axis < 3; axis++) {
            require(Float.floatToRawIntBits(part.positions()[v * 3 + axis]) ==
                Float.floatToRawIntBits(positions[(local + v) * 3 + axis]), "position");
          }
        }
        for (int i = 0; i < part.indices().length; i++)
          require(part.indices()[i] + local == indices[(local / 4) * 6 + i], "index rebasing");
        local += part.vertices();
        assertions++;
      }
      require(local == quads * 4, "all quads emitted");
      if (quads > 356) {
        int[] malformed = indices.clone();
        malformed[0] = -1;
        HorizonMesh unfamiliar =
            new HorizonMesh(positions, colors, malformed, normals, new short[0], -12, 64);
        require(LodMeshPartSplitter.splitLevel(List.of(unfamiliar), 32768).get(0) == unfamiliar,
            "unrecognized topology must pass through");
      }
      require(Arrays.equals(positions, source.positions()), "source array unaffected");
    }
    ArrayList<HorizonMesh> overflowing = new ArrayList<>();
    for (int i = 0; i < 1025; i++)
      overflowing.add(new HorizonMesh(new float[12], new int[4], new int[]{0,1,2,0,2,3},
          new int[4], new short[0], 0, 1));
    require(LodMeshPartSplitter.splitLevel(overflowing, 92) == overflowing, "cap");
    System.out.println("LOD_SPLIT_PREFLIGHT_SMOKE_PASS iterations=256 parts=" + assertions);
  }
}
