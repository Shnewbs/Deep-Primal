package com.shnewbs.deepprimal.core;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/** Bulk query-water restore must retain exact native material/source semantics and bounded tick scratch. */
public final class WaterSimulationBulkRestoreChecks {
  private static long assertions;

  private static void check(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason + " assertion=" + assertions);
  }

  private static byte[] packet(WaterSimulation simulation) {
    ByteBuffer buffer = ByteBuffer.allocate(WaterSimulation.PACKET_BYTES);
    simulation.encodeToPacket(buffer, 0, 42);
    return buffer.array();
  }

  private static void reject(Runnable action, Class<? extends Throwable> expected, String reason) {
    try { action.run(); }
    catch (Throwable failure) {
      check(expected.isInstance(failure), reason + ": wrong exception " + failure);
      return;
    }
    throw new AssertionError(reason + ": accepted invalid import");
  }

  public static void main(String[] args) throws Exception {
    Random random = new Random(0x20261102L);
    for (int trial = 0; trial < 400; trial++) {
      double[] levels = new double[WaterSimulation.CELL_COUNT];
      boolean[] sources = new boolean[levels.length];
      byte[] materials = new byte[levels.length];
      long total = 0, active = 0;
      for (int i = 0; i < levels.length; i++) {
        int units = random.nextInt(17);
        if (random.nextInt(7) == 0) units = 0;
        levels[i] = units / 16.0;
        sources[i] = random.nextBoolean();
        materials[i] = (byte) (units > 0 ? 1 : random.nextInt(2) == 0 ? 0 : 2);
        total += units;
        if (units > 0) active++;
      }
      double[] originalLevels = levels.clone();
      boolean[] originalSources = sources.clone();
      byte[] originalMaterials = materials.clone();
      WaterSimulation simulation = WaterSimulation.fromChunkData(levels, sources, materials);
      check(Arrays.equals(levels, originalLevels) && Arrays.equals(sources, originalSources)
          && Arrays.equals(materials, originalMaterials), "import mutated caller buffers");
      var stats = simulation.getStats();
      check(stats.totalWaterVolumeMl() == total * 62500L && stats.activeCells() == active,
          "bulk totals differ from native quantities");
      byte[] snapshot = packet(simulation);
      for (int i = 0; i < levels.length; i++) {
        int x = i & 15, z = i >>> 4;
        check(simulation.getWaterLevel(x, z) == levels[i], "water quantity changed");
        check(simulation.getMaterial(x, z) == materials[i], "material changed");
        check(simulation.isSource(x, z) == (sources[i] && materials[i] != 2),
            "solid source normalization changed");
        check((snapshot[16 + i * 3] & 255) == (int) (levels[i] * 16), "packet water mismatch");
        check(snapshot[16 + i * 3 + 1] == (byte) (simulation.isSource(x, z) ? 1 : 0),
            "packet source mismatch");
        check(snapshot[16 + i * 3 + 2] == materials[i], "packet material mismatch");
      }
      for (int step = 0; step < 3; step++) {
        simulation.tick(random.nextInt(5), (x, z) -> (x * 11 + z * 7) % 31 - 15);
        check(simulation.getStats().totalWaterVolumeMl() == total * 62500L,
            "closed query tick lost water");
      }
    }

    double[] levels = new double[256];
    boolean[] sources = new boolean[256];
    byte[] materials = new byte[256];
    levels[10] = 1.0 / 16;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials),
        IllegalStateException.class, "wet air");
    materials[10] = 1;
    levels[11] = 0;
    materials[11] = 1;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials),
        IllegalStateException.class, "dry water material");
    materials[11] = 0;
    materials[12] = 3;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials),
        IllegalArgumentException.class, "unsupported material");
    materials[12] = 0;
    levels[12] = Double.NaN;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials),
        IllegalArgumentException.class, "nonfinite volume");
    levels[12] = 0;
    levels[13] = 0.03;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials),
        IllegalArgumentException.class, "nonquantized volume");
    levels[13] = 0;
    reject(() -> WaterSimulation.fromChunkData(new double[255], sources, materials),
        IllegalArgumentException.class, "short import");

    WaterSimulation a = new WaterSimulation(), b = new WaterSimulation();
    a.setWaterLevel(2, 3, 1, true);
    b.setWaterLevel(2, 3, 1, true);
    // A failed callback near the end of the grid must not publish a partial ground update.
    reject(() -> a.tick(1, (x, z) -> x == 15 && z == 15 ? Double.NaN : 100),
        IllegalArgumentException.class, "late invalid height");
    check(a.getPressure(0, 0) == b.getPressure(0, 0)
        && a.getPressure(15, 15) == b.getPressure(15, 15), "failed tick committed ground");
    a.tick(1, (x, z) -> x + z);
    b.tick(1, (x, z) -> x + z);
    check(Arrays.equals(packet(a), packet(b)), "failed tick changed next valid water evolution");

    // Regression against a per-tick new int[256]: the scratch is instance-owned and stable.
    Field scratch = WaterSimulation.class.getDeclaredField("nextGround");
    scratch.setAccessible(true);
    Object retained = scratch.get(a);
    check(retained instanceof int[] && ((int[]) retained).length == WaterSimulation.CELL_COUNT,
        "tick scratch capacity");
    for (int i = 0; i < 100; i++) {
      a.tick(i % 5, (x, z) -> (x + z) % 7);
      check(scratch.get(a) == retained, "tick allocated replacement ground scratch");
    }
    System.out.println("WATER_SIMULATION_BULK_RESTORE_PASS assertions=" + assertions
        + " randomizedImports=400 conservationTicks=1200 scratchReused=100");
  }

  private WaterSimulationBulkRestoreChecks() {}
}
