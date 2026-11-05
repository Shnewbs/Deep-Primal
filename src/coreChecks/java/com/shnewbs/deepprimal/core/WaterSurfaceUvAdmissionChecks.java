package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Malformed UV samples must not poison otherwise finite shared water vertices. */
public final class WaterSurfaceUvAdmissionChecks {
  private static long assertions;
  private static void check(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message);
  }

  public static void main(String[] args) {
    Random random = new Random(202611011700L);
    double[] edge = {-Double.MAX_VALUE, -1e308, -1024, -0.0, 0.0, 1024, 1e308, Double.MAX_VALUE};
    for (double a : edge) for (double b : edge) {
      double expected = WaterTransport.surface(a, b, a, b, 0, 0.5, 0.5);
      double nanU = WaterTransport.surface(a, b, a, b, 0, Double.NaN, 0.5);
      double nanV = WaterTransport.surface(a, b, a, b, 0, 0.5, Double.NaN);
      check(Double.isFinite(nanU) && Double.isFinite(nanV), "NaN UV poisoned finite corners");
      check(Double.doubleToRawLongBits(expected) == Double.doubleToRawLongBits(nanU), "NaN U midpoint mismatch");
      check(Double.doubleToRawLongBits(expected) == Double.doubleToRawLongBits(nanV), "NaN V midpoint mismatch");
    }
    for (int i = 0; i < 200_000; i++) {
      double nw = random.nextDouble(-2000, 2000);
      double ne = random.nextDouble(-2000, 2000);
      double sw = random.nextDouble(-2000, 2000);
      double se = random.nextDouble(-2000, 2000);
      double u = random.nextDouble();
      double v = random.nextDouble();
      double original = TerrainNoise.lerp(TerrainNoise.lerp(nw, ne, u), TerrainNoise.lerp(sw, se, u), v);
      double got = WaterTransport.surface(nw, ne, sw, se, 0, u, v);
      check(Double.doubleToRawLongBits(original) == Double.doubleToRawLongBits(got), "Finite UV changed");
      double midpointU = WaterTransport.surface(nw, ne, sw, se, 0, 0.5, v);
      double midpointV = WaterTransport.surface(nw, ne, sw, se, 0, u, 0.5);
      double midpointBoth = WaterTransport.surface(nw, ne, sw, se, 0, 0.5, 0.5);
      double badU = WaterTransport.surface(nw, ne, sw, se, 0, Double.NaN, v);
      double badV = WaterTransport.surface(nw, ne, sw, se, 0, u, Double.NaN);
      double badBoth = WaterTransport.surface(nw, ne, sw, se, 0, Double.NaN, Double.NaN);
      check(Double.doubleToRawLongBits(badU) == Double.doubleToRawLongBits(midpointU), "NaN U not normalized");
      check(Double.doubleToRawLongBits(badV) == Double.doubleToRawLongBits(midpointV), "NaN V not normalized");
      check(Double.doubleToRawLongBits(badBoth) == Double.doubleToRawLongBits(midpointBoth), "Both NaN UV not normalized");
      check(Double.isFinite(badU) && Double.isFinite(badV) && Double.isFinite(badBoth), "Nonfinite UV output");
    }
    // A malformed UV cannot change authoritative integer-volume exchange.
    for (int from = 0; from <= WaterTransport.CAPACITY; from++)
      for (int to = 0; to <= WaterTransport.CAPACITY; to++) {
        var exchange = WaterTransport.exchange(from, to, true);
        check(exchange.from() + exchange.to() == from + to, "Water quantity changed");
      }
    System.out.println("WATER_SURFACE_UV_PASS assertions=" + assertions);
  }
  private WaterSurfaceUvAdmissionChecks() {}
}
