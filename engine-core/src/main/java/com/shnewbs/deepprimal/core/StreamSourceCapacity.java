package com.shnewbs.deepprimal.core;

/** Limits metered stream input to the generated fractional water surface. */
public final class StreamSourceCapacity {
  public static int remainingUnits(double surface, int blockY, int currentUnits) {
    if (currentUnits < 0 || currentUnits > 16) throw new IllegalArgumentException("Current units");
    if (!Double.isFinite(surface)) return 0;
    double planned = (surface - blockY) * 16.0;
    int capacity = planned <= 0 ? 0 : planned >= 16 ? 16 : (int) Math.round(planned);
    return Math.max(0, capacity - currentUnits);
  }

  private StreamSourceCapacity() {}
}
