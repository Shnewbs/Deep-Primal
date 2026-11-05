package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.baseline.*;
import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;

/** Differential saved-terrain checks and geometry coverage checks, excluded from release jars. */
public final class EngineAuditChecks {
  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  public static void run() {
    Random random = new Random(0x18A0D17);
    long samples = 0;
    for (long seed : new long[] {20261020, -914, Long.MAX_VALUE}) {
      var current = new TerrainEngine(seed);
      var old = new Alpha17TerrainEngine(seed);
      for (int i = 0; i < 600; i++) {
        double x = (i < 300 ? 0 : i % 3 - 1) * 8192. + random.nextDouble() * 7000 - 3500;
        double z = random.nextDouble() * 7000 - 3500;
        var a = current.sample(x, z);
        var b = old.sample(x, z);
        require(
            Arrays.equals(
                values(a),
                new double[] {
                  b.ground(),
                  b.water(),
                  b.rain(),
                  b.temperature(),
                  b.slope(),
                  b.sediment(),
                  b.flowX(),
                  b.flowZ(),
                  b.speed(),
                  b.rock()
                }),
            "Published terrain must remain bit exact");
        require(current.raw(x, z) == old.raw(x, z), "Published biome terrain unchanged");
        samples++;
      }
      for (int region = -7; region <= 7; region++) {
        current.verify(region * 8192., 0);
        old.verify(region * 8192., 0);
      }
      require(
          Arrays.equals(values(current.sample(-19, -71)), values(old.sample(-19, -71))),
          "Terrain survives region eviction");
    }
    int h = 16;
    int[] solid = new int[256 * h];
    Arrays.fill(solid, 1);
    var cube = page(solid, h);
    var before = Alpha17LodMesher.build(cube);
    var after = LodMesher.build(cube);
    require(
        after.fine().stream().mapToInt(m -> m.indices().length / 6).sum() == 6,
        "Solid cube merges to six quads");
    require(after.bytes() < before.bytes() / 20, "Flat terrain GPU reduction");
    // Independent unit-face rasterization catches missing faces, filled air gaps and overlaps.
    for (int trial = 0; trial < 12; trial++) {
      int[] cells = new int[256 * 8];
      for (int i = 0; i < cells.length; i++)
        cells[i] = random.nextInt(4) == 0 ? random.nextInt(2) + 1 : 0;
      checkFaces(cells, 8);
    }
    int[] water = new int[256 * 4];
    water[(8 * 16 + 8) * 4 + 1] = 3;
    water[(8 * 16 + 9) * 4 + 1] = 4;
    var fluid = LodMesher.build(page(water, 4));
    boolean step = false;
    for (var m : fluid.fine())
      for (int i = 0; i < m.positions().length; i += 12) {
        float[] p = m.positions();
        if (p[i] == 9 && p[i + 3] == 9 && p[i + 6] == 9 && p[i + 9] == 9) {
          float lo = Math.min(Math.min(p[i + 1], p[i + 4]), Math.min(p[i + 7], p[i + 10]));
          float hi = Math.max(Math.max(p[i + 1], p[i + 4]), Math.max(p[i + 7], p[i + 10]));
          step |= lo == -1.75f && hi == -1.25f;
        }
      }
    require(step, "Unequal water levels expose their fractional step");
    int[] stack = new int[256 * 4];
    stack[(8 * 16 + 8) * 4 + 1] = 3;
    stack[(8 * 16 + 8) * 4 + 2] = 4;
    for (var part : LodMesher.build(page(stack, 4)).fine())
      for (int i = 1; i < part.positions().length; i += 3)
        require(part.positions()[i] != -1.25f, "Stacked fluid has no artificial internal gap");
    var current = new TerrainEngine(20261020);
    var old = new Alpha17TerrainEngine(20261020);
    for (int i = 0; i < 10000; i++) {
      current.raw(i * .7, -i * .3);
      old.raw(i * .7, -i * .3);
    }
    long newNs = Long.MAX_VALUE, oldNs = Long.MAX_VALUE;
    double checksum = 0;
    for (int round = 0; round < 5; round++) {
      long start = System.nanoTime();
      for (int i = 0; i < 20000; i++) checksum += old.raw(i * .7, -i * .3);
      oldNs = Math.min(oldNs, System.nanoTime() - start);
      start = System.nanoTime();
      for (int i = 0; i < 20000; i++) checksum += current.raw(i * .7, -i * .3);
      newNs = Math.min(newNs, System.nanoTime() - start);
    }
    String report =
        "ENGINE_AUDIT_PASS terrainSamples="
            + samples
            + " cubeBytesBefore="
            + before.bytes()
            + " cubeBytesAfter="
            + after.bytes()
            + " rawOldMs="
            + oldNs / 1e6
            + " rawNewMs="
            + newNs / 1e6
            + " checksum="
            + checksum;
    System.out.println(report);
    try {
      var out = Path.of("build/integrated-engine-checks");
      Files.createDirectories(out);
      Files.writeString(out.resolve("audit.txt"), report + "\n");
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static double[] values(TerrainEngine.Sample s) {
    return new double[] {
      s.ground(),
      s.water(),
      s.rain(),
      s.temperature(),
      s.slope(),
      s.sediment(),
      s.flowX(),
      s.flowZ(),
      s.speed(),
      s.rock()
    };
  }

  private static double[] values(Alpha17TerrainEngine.Sample s) {
    return new double[] {
      s.ground(),
      s.water(),
      s.rain(),
      s.temperature(),
      s.slope(),
      s.sediment(),
      s.flowX(),
      s.flowZ(),
      s.speed(),
      s.rock()
    };
  }

  private static ColumnarChunk page(int[] cells, int height) {
    var b = new ColumnarChunk.Builder(0, 0, -3, height, 1);
    for (int c = 0; c < 256; c++) b.column(Arrays.copyOfRange(cells, c * height, (c + 1) * height));
    return b.finish(
        List.of(
            "00000000|air",
            "ff777777|stone",
            "ff884422|roof",
            "ec336699|water12",
            "e4336699|water4"));
  }

  private static void checkFaces(int[] data, int h) {
    var expected = new HashSet<String>();
    int[][] dirs = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++)
        for (int y = 0; y < h; y++) {
          int color = data[(z * 16 + x) * h + y];
          if (color == 0) continue;
          for (int face = 0; face < 6; face++) {
            int[] d = dirs[face];
            int nx = x + d[0], ny = y + d[1], nz = z + d[2];
            if (nx < 0
                || nx >= 16
                || nz < 0
                || nz >= 16
                || ny < 0
                || ny >= h
                || data[(nz * 16 + nx) * h + ny] == 0)
              expected.add(
                  face
                      + ":"
                      + (x + (face == 1 ? 1 : 0))
                      + ":"
                      + (y - 3 + (face == 3 ? 1 : 0))
                      + ":"
                      + (z + (face == 5 ? 1 : 0))
                      + ":"
                      + color);
          }
        }
    var actual = new HashSet<String>();
    for (var m : LodMesher.build(page(data, h)).fine())
      for (int q = 0; q < m.positions().length; q += 12) {
        float[] p = m.positions();
        int normal = m.normals()[q / 3];
        int face =
            (byte) normal < 0
                ? 0
                : (byte) normal > 0
                    ? 1
                    : (byte) (normal >>> 8) < 0
                        ? 2
                        : (byte) (normal >>> 8) > 0 ? 3 : (byte) (normal >>> 16) < 0 ? 4 : 5;
        int x0 = (int) Math.min(p[q], Math.min(p[q + 3], Math.min(p[q + 6], p[q + 9]))),
            x1 = (int) Math.max(p[q], Math.max(p[q + 3], Math.max(p[q + 6], p[q + 9])));
        int y0 = (int) Math.min(p[q + 1], Math.min(p[q + 4], Math.min(p[q + 7], p[q + 10]))),
            y1 = (int) Math.max(p[q + 1], Math.max(p[q + 4], Math.max(p[q + 7], p[q + 10])));
        int z0 = (int) Math.min(p[q + 2], Math.min(p[q + 5], Math.min(p[q + 8], p[q + 11]))),
            z1 = (int) Math.max(p[q + 2], Math.max(p[q + 5], Math.max(p[q + 8], p[q + 11])));
        int id = m.colors()[q / 3] == 0xff777777 ? 1 : 2;
        for (int z = z0; z < Math.max(z0 + 1, z1); z++)
          for (int y = y0; y < Math.max(y0 + 1, y1); y++)
            for (int x = x0; x < Math.max(x0 + 1, x1); x++)
              require(
                  actual.add(face + ":" + x + ":" + y + ":" + z + ":" + id),
                  "No duplicated LOD faces");
        for (int ix : m.indices())
          require(ix >= 0 && ix < m.vertices(), "Indices stay inside GPU part");
      }
    require(expected.equals(actual), "Greedy mesh exactly covers exposed voxel faces");
  }
}
