package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.util.Random;

/** Query-water material is derived from quantity; reject phantom-water imports. */
public final class WaterSimulationMaterialAdmissionChecks {
  private static long assertions;
  private static void check(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message);
  }
  private static void reject(Runnable action, String message) {
    try { action.run(); }
    catch (IllegalStateException expected) { assertions++; return; }
    throw new AssertionError(message + ": accepted invalid dry material");
  }
  private static void validCells(WaterSimulation sim) {
    ByteBuffer packet = ByteBuffer.allocate(WaterSimulation.PACKET_BYTES + 11);
    sim.encodeToPacket(packet, 7, 20261101L);
    check(packet.position() == 0, "packet caller position changed");
    for (int i = 0; i < WaterSimulation.CELL_COUNT; i++) {
      int x = i & 15, z = i >>> 4;
      int units = (int) Math.round(sim.getWaterLevel(x, z) * 16);
      int material = sim.getMaterial(x, z);
      check(material == 2 || material == (units > 0 ? 1 : 0), "material/quantity mismatch at " + i);
      check(packet.get(7 + 16 + i * 3) == units, "packet quantity mismatch at " + i);
      check(packet.get(7 + 16 + i * 3 + 2) == material, "packet material mismatch at " + i);
      if (material == 2) check(!sim.isSource(x, z), "solid source at " + i);
    }
  }
  public static void main(String[] args) {
    WaterSimulation sim = new WaterSimulation();
    sim.setWaterLevel(0, 0, 0, true);
    reject(() -> sim.setMaterial(0, 0, (byte) 1), "dry source");
    check(sim.isSource(0, 0), "failed write changed source");
    check(sim.getMaterial(0, 0) == 0, "failed write changed material");
    sim.setWaterLevel(1, 0, 1.0 / 16, true);
    reject(() -> sim.setMaterial(1, 0, (byte) 0), "wet air");
    reject(() -> sim.setMaterial(1, 0, (byte) 2), "wet solid");
    sim.setMaterial(1, 0, (byte) 1);
    check(sim.removeWater(1, 0, 1.0 / 16) == 1.0 / 16, "drain quantity");
    reject(() -> sim.setMaterial(1, 0, (byte) 1), "drained cell");
    check(sim.getMaterial(1, 0) == 0 && sim.isSource(1, 0), "drain keeps source metadata but clears water material");
    sim.setMaterial(0, 0, (byte) 2);
    check(!sim.isSource(0, 0), "solid conversion clears source");
    validCells(sim);

    double[] levels = new double[256];
    boolean[] sources = new boolean[256];
    byte[] materials = new byte[256];
    materials[10] = 1;
    reject(() -> WaterSimulation.fromChunkData(levels, sources, materials), "imported dry water");
    materials[10] = 0;
    materials[11] = 2;
    sources[11] = true;
    levels[12] = 1.0;
    materials[12] = 1;
    WaterSimulation loaded = WaterSimulation.fromChunkData(levels, sources, materials);
    check(loaded.getMaterial(11, 0) == 2 && !loaded.isSource(11, 0), "solid import source normalization");
    check(loaded.getWaterLevel(12, 0) == 1.0, "wet import volume");
    validCells(loaded);

    Random rng = new Random(20261101L);
    for (int trial = 0; trial < 400; trial++) {
      double[] volumes = new double[256];
      boolean[] sourceFlags = new boolean[256];
      byte[] materialFlags = new byte[256];
      long total = 0;
      for (int i = 0; i < 256; i++) {
        int choice = rng.nextInt(6);
        if (choice == 0) { materialFlags[i] = 2; sourceFlags[i] = rng.nextBoolean(); }
        else {
          int u = rng.nextInt(17);
          volumes[i] = u / 16.0;
          total += u;
          sourceFlags[i] = rng.nextBoolean();
          materialFlags[i] = (byte) (u > 0 ? 1 : 0);
        }
      }
      WaterSimulation sample = WaterSimulation.fromChunkData(volumes, sourceFlags, materialFlags);
      check(sample.getStats().totalWaterVolumeMl() == total * 62500L, "import volume changed");
      validCells(sample);
      for (int step = 0; step < 3; step++) {
        sample.tick(1, (x, z) -> (x * 13 + z * 7) % 19 - 9);
        check(sample.getStats().totalWaterVolumeMl() == total * 62500L, "closed query water created/destroyed");
        validCells(sample);
      }
    }
    System.out.println("WATER_SIMULATION_MATERIAL_ADMISSION_PASS assertions=" + assertions + " randomizedImports=400 conservationSteps=1200");
  }
}
