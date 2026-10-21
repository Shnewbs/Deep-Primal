package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;

public final class IntegratedEngineChecks {
  private static void require(boolean v, String message) {
    if (!v) throw new AssertionError(message);
  }

  public static void run() {
    try {
      for (int from = 0; from <= 16; from++)
        for (int to = 0; to <= 16; to++)
          for (boolean down : new boolean[] {false, true}) {
            var exchange = WaterTransport.exchange(from, to, down);
            require(
                exchange.from() + exchange.to() == from + to
                    && exchange.from() >= 0
                    && exchange.to() <= 16,
                "Engine transport conserves all legal states");
          }
      long begin = System.nanoTime();
      int pressureCases = 0;
      for (int dy = -8; dy <= 8; dy++)
        for (int from = 0; from <= 16; from++)
          for (int to = 0; to <= 16; to++) {
            var e = WaterTransport.pressure(dy, from, 0, to);
            require(
                e.from() + e.to() == from + to && e.from() >= 0 && e.to() <= 16,
                "Connected pressure conserves capacity and volume");
            long before = (long) dy * 16 + from - to;
            long after = (long) dy * 16 + e.from() - e.to();
            require(
                e.moved() == 0 || (before > 1 && after >= 0 && after < before),
                "Pressure decreases head without overshoot");
            pressureCases++;
          }
      require(
          WaterTransport.pressure(503, 16, 502, 0).moved() == 16,
          "Elevated source can raise a connected lower surface");
      require(
          WaterTransport.pressure(Integer.MIN_VALUE, 16, Integer.MAX_VALUE, 0).moved() == 0,
          "Head arithmetic does not wrap");
      System.out.println(
          "ENGINE_PRESSURE_CORE_PASS cases="
              + pressureCases
              + " conserved=true monotonicHead=true");
      TerrainEngine a = new TerrainEngine(20261020), b = new TerrainEngine(20261020);
      int wet = 0, land = 0, ocean = 0;
      double min = 1e6, max = -1e6;
      StringBuilder csv = new StringBuilder("x,z,height,water,forest\n");
      for (int z = -3600; z <= 3600; z += 96)
        for (int x = -3600; x <= 3600; x += 96) {
          var s = a.sample(x, z);
          require(Double.isFinite(s.ground()), "finite terrain");
          require(s.ground() > -128 && s.ground() < 630, "world bounds");
          if (s.wet()) {
            if (s.water() > TerrainEngine.SEA + 1) wet++;
            else ocean++;
          } else land++;
          min = Math.min(min, s.ground());
          max = Math.max(max, s.ground());
          csv.append(x)
              .append(',')
              .append(z)
              .append(',')
              .append(s.ground())
              .append(',')
              .append(s.water())
              .append(',')
              .append(a.forest(x, z))
              .append('\n');
        }
      System.out.println("INTEGRATED_COVERAGE river=" + wet + " land=" + land + " ocean=" + ocean);
      Path map = Path.of("build/integrated-engine-checks");
      Files.createDirectories(map);
      Files.writeString(map.resolve("terrain.csv"), csv);
      require(wet > 5 && land > 100 && ocean > 100, "continent river ocean coverage");
      Random random = new Random(2517);
      for (int n = 0; n < 1000; n++) {
        int x = random.nextInt(7000) - 3500, z = random.nextInt(7000) - 3500;
        require(a.sample(x, z).equals(b.sample(x, z)), "seed and cache independent");
      }
      a.verify(0, 0);
      a.verify(-8192, -8192);
      var saved = a.sample(19, -71);
      for (int n = 0; n < 14; n++) a.verify(n * 8192., 0);
      require(a.cachedRegions() <= 12 && a.sample(19, -71).equals(saved), "eviction deterministic");
      var builder = new ColumnarChunk.Builder(-3, 8, -32, 128, 5);
      List<String> palette =
          List.of("00000000|air", "ff777777|stone", "ff996633|roof", "f9336699|water");
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          int[] ids = new int[128];
          Arrays.fill(ids, 0, 33, 1);
          if (x >= 4 && x <= 11 && z >= 4 && z <= 11) ids[80] = 2;
          if (z == 0) ids[33] = 3;
          builder.column(ids);
        }
      ColumnarChunk page = builder.finish(palette);
      var levels = LodMesher.build(page);
      for (int d = 0; d < 3; d++) {
        var meshes = levels.level(d);
        require(!meshes.isEmpty(), "LOD coverage");
        boolean roof = false, floor = false;
        for (var mesh : meshes) {
          require(
              mesh.vertices() < 65536 && mesh.gpuBytes() <= 256 * 1024, "bounded native upload");
          for (int index : mesh.indices())
            require(index >= 0 && index < mesh.vertices(), "LOD valid indices");
          for (int p = 1; p < mesh.positions().length; p += 3) {
            float y = mesh.positions()[p];
            roof |= y == 49;
            floor |= y == 1;
          }
        }
        require(roof && floor, "roof and lower terrain retained at all levels");
      }
      require(
          page.state(8, 30, 8).equals("00000000|air")
              && page.state(8, 48, 8).equals("ff996633|roof"),
          "building air gap");
      Path folder = Files.createTempDirectory("deepprimal-lod-check");
      var store = new LodStore(folder);
      require(store.write(page), "cache write");
      require(store.read(-3, 8).state(8, 48, 8).contains("roof"), "cache roundtrip");
      var older =
          new ColumnarChunk(
              page.x,
              page.z,
              page.minY,
              page.height,
              4,
              page.palette(),
              page.offsets(),
              page.runs());
      require(!store.write(older), "stale cache rejected");
      Files.write(folder.resolve("-3_8.plod"), new byte[] {1, 2, 3});
      boolean rejected = false;
      try {
        store.read(-3, 8);
      } catch (java.io.IOException expected) {
        rejected = true;
      }
      require(rejected, "corrupt cache rejected");
      Files.delete(folder.resolve("-3_8.plod"));
      Files.delete(folder);
      Path out = Path.of("build/integrated-engine-checks");
      Files.createDirectories(out);
      Files.writeString(out.resolve("terrain.csv"), csv);
      String report =
          "INTEGRATED_CORE_PASS samples=5776 river="
              + wet
              + " land="
              + land
              + " ocean="
              + ocean
              + " height="
              + min
              + ".."
              + max
              + " lodBytes="
              + levels.bytes()
              + " ms="
              + (System.nanoTime() - begin) / 1e6;
      Files.writeString(out.resolve("result.txt"), report + "\n");
      System.out.println(report);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
}
