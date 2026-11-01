package com.shnewbs.deepprimal.core;

/** Stateless, continuous 2D land shaping; the platform adapter supplies surface water. */
public final class WaterwayShape {
  private WaterwayShape() {}

  private static double smooth(double value, double low, double high) {
    double t = Math.clamp((value - low) / (high - low), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
  }

  public static double coastWeight(double continents, double erosion) {
    return smooth(continents, -0.26, -0.20)
        * (1.0 - smooth(continents, -0.07, -0.02))
        * smooth(erosion, -0.45, 0.25);
  }

  public static double riverWeight(double continents, double ridges) {
    return lowland(continents) * (1.0 - smooth(Math.abs(ridges), 0.018, 0.22));
  }

  public static double streamWeight(double continents, double ridges, double tributaries) {
    double offset = 0.28 * smooth(tributaries, -0.35, 0.45);
    double distance = Math.abs(Math.abs(ridges) - offset);
    return lowland(continents)
        * (1.0 - smooth(tributaries, 0.15, 0.55))
        * (1.0 - smooth(distance, 0.004, 0.045));
  }

  private static double lowland(double continents) {
    return 1.0 - smooth(continents, 0.18, 0.42);
  }

  public static double coastHeight(
      double original, double continents, double erosion, int seaLevel) {
    double shelf = seaLevel + 3.0 + 150.0 * (continents + 0.15);
    double weight = coastWeight(continents, erosion);
    // Full-strength blends must hit the target even when a finite upstream Y
    // exceeds double's unit precision; cancellation otherwise yields a wrong Y.
    if (weight == 1.0 && Math.abs(original) >= 0x1.0p53) return shelf;
    return original + weight * (shelf - original);
  }

  public static double riverHeight(double coast, double continents, double ridges, double bed) {
    double weight = riverWeight(continents, ridges);
    if (weight == 1.0 && coast > bed && Math.abs(coast) >= 0x1.0p53) return bed;
    return coast - weight * Math.max(0.0, coast - bed);
  }

  public static double height(
      double original,
      double continents,
      double ridges,
      double erosion,
      double detail,
      double tributaries,
      int seaLevel) {
    // Noise and climate adapters must not inject NaN/Infinity into native terrain
    // materialization. Preserve the uncarved height when a shaping input is invalid.
    if (!Double.isFinite(original)) return seaLevel;
    if (!Double.isFinite(continents) || !Double.isFinite(ridges)
        || !Double.isFinite(erosion) || !Double.isFinite(detail)
        || !Double.isFinite(tributaries)) return original;
    double coast = coastHeight(original, continents, erosion, seaLevel);
    double bed = seaLevel - 7.0 + 1.5 * Math.clamp(detail, -1.0, 1.0);
    double river = riverHeight(coast, continents, ridges, bed);
    // Branches share the parent's bed. Neither channel raises the seabed or
    // leaves a lip across its river mouth. Their shoulders fade continuously.
    double stream = streamWeight(continents, ridges, tributaries);
    double shaped = stream == 1.0 && river > bed && Math.abs(river) >= 0x1.0p53
        ? bed : river - stream * Math.max(0.0, river - bed);
    // Even finite but unbounded upstream values can overflow intermediate shelf
    // arithmetic. Fail closed instead of writing a nonfinite terrain height.
    return Double.isFinite(shaped) ? shaped : original;
  }
}
