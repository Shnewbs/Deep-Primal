package com.shnewbs.deepprimal.core;

final class StreamSourceCapacityChecks {
  private static void need(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  static void run() {
    int cases = 0;
    for (int y : new int[] {-64, -1, 0, 63, 319}) {
      for (int surfaceUnits = -16; surfaceUnits <= 32; surfaceUnits++) {
        double surface = y + surfaceUnits / 16.0;
        int expected = Math.clamp(surfaceUnits, 0, 16);
        for (int current = 0; current <= 16; current++) {
          need(StreamSourceCapacity.remainingUnits(surface, y, current)
                  == Math.max(0, expected - current),
              "fractional channel capacity mismatch");
          cases++;
        }
      }
    }
    for (double surface : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      need(StreamSourceCapacity.remainingUnits(surface, 0, 0) == 0,
          "non-finite surface must fail closed");
      cases++;
    }
    boolean rejected = false;
    try {
      StreamSourceCapacity.remainingUnits(1, 0, 17);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    need(rejected, "invalid existing quantity accepted");
    System.out.println("STREAM_SOURCE_CAPACITY_PASS cases=" + cases
        + " fractionalLevels=true cappedAtOneBlock=true failsClosed=true noQuantityMutation=true");
  }
}
