import static org.lwjgl.util.meshoptimizer.MeshOptimizer.*;

import com.shnewbs.deepprimal.core.HorizonMesh;
import java.nio.*;
import java.util.Arrays;
import org.lwjgl.system.MemoryUtil;

/** Standalone, non-shipping microprobe. No Minecraft thread or GPU access. */
public final class MeshOptimizerProbe {
  public static void main(String[] args) {
    System.out.println(
        "{\"lwjgl\":\"3.4.1\",\"meshoptimizerVersion\":" + MESHOPTIMIZER_VERSION + ",\"cases\":[");
    for (int kind = 0; kind < 4; kind++) {
      int side = 65;
      short[] h = new short[side * side];
      int[] c = new int[h.length];
      for (int z = 0; z < side; z++)
        for (int x = 0; x < side; x++) {
          int i = z * side + x;
          h[i] =
              (short)
                  switch (kind) {
                    case 0 -> 96;
                    case 1 -> Math.round(160 + 32 * Math.sin(x * .1) * Math.cos(z * .12));
                    case 2 -> x < 32 ? 96 : 192;
                    default -> x < 32 ? 96 : Math.round(96 + (x - 32) * 1.3);
                  };
          c[i] = kind == 3 && x < 32 ? 0xf9007088 : 0xff52763c;
        }
      var mesh = HorizonMesh.build(h, c, 0, -128);
      FloatBuffer positions = MemoryUtil.memAllocFloat(mesh.positions().length);
      IntBuffer indices = MemoryUtil.memAllocInt(mesh.indices().length),
          out = MemoryUtil.memAllocInt(mesh.indices().length);
      FloatBuffer attributes = MemoryUtil.memAllocFloat(mesh.vertices() * 4),
          weights = MemoryUtil.memAllocFloat(4),
          error = MemoryUtil.memAllocFloat(1);
      ByteBuffer locks = MemoryUtil.memCalloc(mesh.vertices());
      try {
        positions.put(mesh.positions()).flip();
        indices.put(mesh.indices()).flip();
        weights.put(new float[] {.1f, .1f, .1f, 10f}).flip();
        int locked = 0;
        for (int v = 0; v < mesh.vertices(); v++) {
          int color = mesh.colors()[v];
          attributes
              .put(((color >> 16) & 255) / 255f)
              .put(((color >> 8) & 255) / 255f)
              .put((color & 255) / 255f)
              .put((color >>> 24) == 249 ? 1 : 0);
          float x = mesh.positions()[v * 3], z = mesh.positions()[v * 3 + 2];
          if (x == 0
              || x == 512
              || z == 0
              || z == 512
              || ((kind == 2 || kind == 3) && (x == 248 || x == 256))) {
            locks.put(v, (byte) meshopt_SimplifyVertex_Lock);
            locked++;
          }
        }
        attributes.flip();
        long[] timings = new long[20];
        long count = 0;
        float reported = 0;
        for (int iteration = -10; iteration < 20; iteration++) {
          long start = System.nanoTime();
          count =
              meshopt_simplifyWithAttributes(
                  out,
                  indices,
                  positions,
                  mesh.vertices(),
                  12,
                  attributes,
                  16,
                  weights,
                  locks,
                  mesh.indices().length / 2 / 3 * 3,
                  .25f,
                  meshopt_SimplifyErrorAbsolute,
                  error);
          long elapsed = System.nanoTime() - start;
          if (iteration >= 0) timings[iteration] = elapsed;
          reported = error.get(0);
        }
        if (count % 3 != 0
            || count > mesh.indices().length
            || !Float.isFinite(reported)
            || reported > .25001f) throw new AssertionError("Invalid simplification result");
        boolean[] retained = new boolean[mesh.vertices()];
        for (int i = 0; i < count; i++) {
          int v = out.get(i);
          if (v < 0 || v >= retained.length) throw new AssertionError("Invalid vertex");
          retained[v] = true;
        }
        // Check roof edge anchors; duplicated skirt vertices may legitimately be welded.
        for (int v = 0; v < side * side; v++)
          if (locks.get(v) != 0 && !retained[v])
            throw new AssertionError("Locked roof anchor removed: " + v);
        Arrays.sort(timings);
        System.out.printf(
            java.util.Locale.ROOT,
            "%s{\"name\":\"%s\",\"inputTriangles\":%d,\"outputTriangles\":%d,\"reportedAbsoluteError\":%.6f,\"lockedVertices\":%d,\"medianMicros\":%.3f,\"roofAnchorsPreserved\":true}%n",
            kind == 0 ? "" : ",",
            new String[] {"flat", "hills", "cliff", "water-boundary"}[kind],
            mesh.triangles(),
            count / 3,
            reported,
            locked,
            timings[10] / 1000.0);
      } finally {
        MemoryUtil.memFree(positions);
        MemoryUtil.memFree(indices);
        MemoryUtil.memFree(out);
        MemoryUtil.memFree(attributes);
        MemoryUtil.memFree(weights);
        MemoryUtil.memFree(error);
        MemoryUtil.memFree(locks);
      }
    }
    System.out.println("]}");
  }
}
