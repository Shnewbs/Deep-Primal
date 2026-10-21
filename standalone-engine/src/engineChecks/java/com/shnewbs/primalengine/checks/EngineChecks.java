package com.shnewbs.primalengine.checks;

import com.shnewbs.primalengine.core.*;
import com.shnewbs.primalengine.lod.*;
import java.util.Random;

public final class EngineChecks {
  public static void main(String[] args) throws Exception {
    Random r = new Random(20261020);
    long started = System.nanoTime();
    for (int i = 0; i < 500_000; i++) {
      int a = r.nextInt(17), b = r.nextInt(17);
      var e = WaterTransport.exchange(a, b, r.nextBoolean());
      require(e.from() + e.to() == a + b && e.from() >= 0 && e.to() <= 16, "Conservation/capacity");
    }
    var world = new ContinentalWorld(20261020);
    var again = new ContinentalWorld(20261020);
    int land = 0, ocean = 0, rivers = 0;
    for (int z = -8192; z <= 8192; z += 256)
      for (int x = -8192; x <= 8192; x += 256) {
        var s = world.sample(x, z);
        require(s.equals(again.sample(x, z)), "Deterministic rebuild");
        require(s.ground() > -126 && s.ground() < 639, "Height bounds");
        if (s.ocean()) ocean++;
        else land++;
        if (!s.ocean() && !Double.isNaN(s.water())) rivers++;
      }
    // Jittered probes also cover channels between the regular continental survey points.
    for (int n = 0; n < 50_000; n++) {
      var s = world.sample(r.nextDouble() * 6000 - 3000, r.nextDouble() * 6000 - 3000);
      if (!s.ocean() && !Double.isNaN(s.water())) rivers++;
    }
    require(
        land > 500 && ocean > 500 && rivers > 0,
        "Continents/oceans/drainage missing: land="
            + land
            + " ocean="
            + ocean
            + " rivers="
            + rivers);
    for (int z = -1; z <= 1; z++)
      for (int x = -1; x <= 1; x++) world.verifyDrainage(x * 8192, z * 8192);
    for (int i = 0; i < 10000; i++) {
      double a = r.nextDouble(),
          b = r.nextDouble(),
          c = r.nextDouble(),
          d = r.nextDouble(),
          center = r.nextDouble();
      double t = r.nextDouble();
      require(
          Math.abs(
                  WaterTransport.surface(a, b, c, d, center, 1, t)
                      - WaterTransport.surface(b, a, d, c, 1 - center, 0, t))
              < 1e-10,
          "Shared edge split");
    }
    require(world.cachedContinents() <= 12, "Unbounded region cache");
    lodChecks();
    System.out.println(
        "ENGINE_CORE_PASS transfers=500000 deterministicSamples=4225 drainageAcyclic=true"
            + " sharedEdges=10000 land="
            + land
            + " ocean="
            + ocean
            + " riverSamples="
            + rivers
            + " milliseconds="
            + (System.nanoTime() - started) / 1_000_000);
  }

  private static void lodChecks() throws Exception {
    var b = new ColumnarChunk.Builder(-123456, 654321, -384, 1024, 7);
    int[] column = new int[1024];
    for (int c = 0; c < 256; c++) {
      java.util.Arrays.fill(column, 0);
      java.util.Arrays.fill(column, 0, 450, 1);
      if (c == 17) {
        java.util.Arrays.fill(column, 500, 508, 2);
        java.util.Arrays.fill(column, 530, 534, 3);
      }
      b.column(column);
    }
    var snapshot = b.finish(java.util.List.of("air", "stone", "player_build", "tree"));
    var dir = java.nio.file.Files.createTempDirectory("primal-lod-checks");
    try {
      var store = new LodStore(dir);
      require(store.write(snapshot), "LOD first write");
      var decoded = store.read(snapshot.x, snapshot.z);
      require(
          decoded.state(1, 116, 1).equals("player_build")
              && decoded.state(1, 132, 1).equals("air")
              && decoded.state(1, 146, 1).equals("tree"),
          "LOD vertical structures/gaps");
      var older =
          new ColumnarChunk(
              snapshot.x,
              snapshot.z,
              snapshot.minY,
              snapshot.height,
              6,
              snapshot.palette(),
              snapshot.offsets(),
              snapshot.runs());
      require(
          !store.write(older) && store.read(snapshot.x, snapshot.z).revision == 7,
          "Stale LOD overwrite");
      var file = dir.resolve(snapshot.x + "_" + snapshot.z + ".plod");
      byte[] data = java.nio.file.Files.readAllBytes(file);
      java.nio.file.Files.write(file, java.util.Arrays.copyOf(data, data.length / 2));
      boolean rejected = false;
      try {
        store.read(snapshot.x, snapshot.z);
      } catch (java.io.IOException expected) {
        rejected = true;
      }
      require(rejected, "Truncated LOD accepted");
      System.out.println(
          "LOD_STORAGE_PASS signedCoordinates=true height1024=true structures=true trees=true"
              + " airGaps=true staleRevision=true truncation=true bytes="
              + data.length
              + " memoryEstimate="
              + snapshot.estimatedBytes());
    } finally {
      try (var files = java.nio.file.Files.list(dir)) {
        for (var file : files.toList()) java.nio.file.Files.delete(file);
      }
      java.nio.file.Files.delete(dir);
    }
  }

  private static void require(boolean ok, String reason) {
    if (!ok) throw new AssertionError(reason);
  }
}
