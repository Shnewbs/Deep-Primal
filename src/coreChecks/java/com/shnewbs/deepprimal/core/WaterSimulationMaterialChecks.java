package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.util.Random;

/** Isolated deterministic query-water solid-cell and conservation regression. */
public final class WaterSimulationMaterialChecks {
  private static long assertions;
  private static void check(boolean condition, String reason) {
    assertions++;
    if (!condition) throw new AssertionError(reason);
  }
  private static void reject(Runnable action, Class<? extends Throwable> expected, String reason) {
    try { action.run(); } catch (Throwable failure) {
      check(expected.isInstance(failure), reason + ": wrong exception " + failure);
      return;
    }
    throw new AssertionError(reason + ": accepted invalid mutation");
  }
  private static void verifySolid(WaterSimulation water, int x, int z, String reason) {
    check(water.getMaterial(x, z) == 2, reason + ": solid overwritten");
    check(water.getWaterLevel(x, z) == 0, reason + ": water entered solid");
    check(!water.isSource(x, z), reason + ": solid labelled as source");
  }
  public static void main(String[] args) {
    WaterSimulation water = new WaterSimulation();
    water.setMaterial(0, 0, (byte) 2);
    verifySolid(water, 0, 0, "initial solid");
    reject(() -> water.setWaterLevel(0, 0, 1, true), IllegalStateException.class, "positive setter on solid");
    verifySolid(water, 0, 0, "failed positive setter");
    reject(() -> water.setWaterLevel(0, 0, 0, true), IllegalStateException.class, "source setter on solid");
    verifySolid(water, 0, 0, "failed source setter");
    water.setWaterLevel(0, 0, 0, false);
    verifySolid(water, 0, 0, "zero setter on solid");
    reject(() -> water.addWater(0, 0, 1.0 / 16), IllegalStateException.class, "add to solid");
    verifySolid(water, 0, 0, "failed addition");
    water.setWaterLevel(1, 0, 0, true);
    water.setMaterial(1, 0, (byte) 2);
    verifySolid(water, 1, 0, "solid conversion clears dry source");
    double[] levels = new double[256];
    boolean[] sourceFlags = new boolean[256];
    byte[] materialFlags = new byte[256];
    sourceFlags[7] = true;
    materialFlags[7] = 2;
    levels[8] = 1.0 / 16;
    sourceFlags[8] = true;
    materialFlags[8] = 1;
    WaterSimulation loaded = WaterSimulation.fromChunkData(levels, sourceFlags, materialFlags);
    verifySolid(loaded, 7, 0, "imported dry solid");
    check(loaded.getWaterLevel(8, 0) == 1.0 / 16, "imported wet level preserved");
    check(loaded.isSource(8, 0), "imported wet source preserved");
    check(loaded.getMaterial(8, 0) == 1, "imported wet material preserved");
    water.setWaterLevel(2, 0, 1, true);
    long before = water.getStats().totalWaterVolumeMl();
    for (int step = 0; step < 20; step++) {
      water.tick(1, (x, z) -> x == 0 ? -1 : 0);
      verifySolid(water, 0, 0, "tick solid A " + step);
      verifySolid(water, 1, 0, "tick solid B " + step);
      check(water.getStats().totalWaterVolumeMl() == before, "closed query volume conserved");
    }
    ByteBuffer packet = ByteBuffer.allocate(WaterSimulation.PACKET_BYTES + 7);
    water.encodeToPacket(packet, 7, 123);
    check(packet.get(7 + 16 + 2) == 2, "packet retains solid A");
    check(packet.get(7 + 16 + 3 * 1 + 2) == 2, "packet retains solid B");
    check(packet.get(7 + 16 + 3 * 1 + 1) == 0, "packet clears invalid solid source");
    Random random = new Random(0x1395A7E1L);
    for (int trial = 0; trial < 300; trial++) {
      WaterSimulation sample = new WaterSimulation();
      boolean[] solid = new boolean[256];
      for (int i = 0; i < 256; i++) {
        if (random.nextInt(4) == 0) {
          sample.setMaterial(i & 15, i >>> 4, (byte) 2);
          solid[i] = true;
        } else if (random.nextBoolean()) {
          sample.setWaterLevel(i & 15, i >>> 4, random.nextInt(17) / 16.0, random.nextBoolean());
        }
      }
      long conserved = sample.getStats().totalWaterVolumeMl();
      for (int step = 0; step < 12; step++) {
        sample.tick(random.nextInt(5), (x, z) -> (x + z) % 7 - 3);
        check(sample.getStats().totalWaterVolumeMl() == conserved, "random closed-grid conservation");
        for (int i = 0; i < 256; i++) {
          if (solid[i]) verifySolid(sample, i & 15, i >>> 4, "random solid " + trial + ":" + step + ":" + i);
        }
      }
    }
    System.out.println("WATER_SIMULATION_MATERIAL_PASS assertions=" + assertions + " trials=300");
  }
}
