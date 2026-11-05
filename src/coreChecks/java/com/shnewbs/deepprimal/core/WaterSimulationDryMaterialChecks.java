package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.util.Random;

/** Dry cells must not retain the query water material after transport or removal. */
public final class WaterSimulationDryMaterialChecks {
  private static long assertions;
  private static void check(boolean condition, String reason) {
    assertions++;
    if (!condition) throw new AssertionError(reason);
  }
  private static void verifyMaterial(WaterSimulation simulation, String reason) {
    for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
      byte material = simulation.getMaterial(x, z);
      double volume = simulation.getWaterLevel(x, z);
      check(material == 2 || material == (volume > 0 ? 1 : 0),
          reason + " cell=" + x + "," + z + " material=" + material + " volume=" + volume);
      if (material == 2) {
        check(volume == 0, reason + " solid gained water");
        check(!simulation.isSource(x, z), reason + " solid became source");
      }
    }
  }
  public static void main(String[] args) {
    WaterSimulation simulation = new WaterSimulation();
    simulation.setWaterLevel(0, 0, 1.0 / 16, true);
    check(simulation.removeWater(0, 0, 1.0 / 16) == 1.0 / 16, "remove return");
    check(simulation.isSource(0, 0), "source metadata must survive draining");
    verifyMaterial(simulation, "explicit removal");
    simulation.setWaterLevel(0, 0, 1.0 / 16, false);
    simulation.tick(1, (x, z) -> x == 0 && z == 0 ? 10 : 0);
    check(simulation.getWaterLevel(0, 0) == 0, "pressure emptied high cell");
    verifyMaterial(simulation, "pressure transfer");
    ByteBuffer packet = ByteBuffer.allocate(WaterSimulation.PACKET_BYTES + 3);
    simulation.encodeToPacket(packet, 3, 1);
    check(packet.get(3 + 16 + 2) == 0, "packet marks drained cell as air");
    Random random = new Random(0xD1395EEDL);
    for (int trial = 0; trial < 120; trial++) {
      WaterSimulation sample = new WaterSimulation();
      for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
        if (random.nextInt(7) == 0) sample.setMaterial(x, z, (byte) 2);
        else if (random.nextBoolean()) sample.setWaterLevel(x, z, random.nextInt(17) / 16.0, random.nextBoolean());
      }
      long volume = sample.getStats().totalWaterVolumeMl();
      verifyMaterial(sample, "initial " + trial);
      for (int step = 0; step < 12; step++) {
        sample.tick(random.nextInt(5), (x, z) -> ((x * 11 + z * 7) % 23) - 11);
        check(sample.getStats().totalWaterVolumeMl() == volume, "closed-grid volume conserved");
        verifyMaterial(sample, "trial " + trial + " step " + step);
      }
    }
    System.out.println("WATER_SIMULATION_DRY_MATERIAL_PASS assertions=" + assertions + " trials=120");
  }
}
