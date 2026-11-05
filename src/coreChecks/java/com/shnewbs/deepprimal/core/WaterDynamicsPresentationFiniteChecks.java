package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Protect shader-facing water colors and vertex displacements from invalid inputs. */
public final class WaterDynamicsPresentationFiniteChecks {
  private static long checks;

  private static void check(boolean valid, String message) {
    checks++;
    if (!valid) throw new AssertionError(message);
  }

  private static int originalMix(int a, int b, double amount) {
    double t = Math.clamp(amount, 0, 1);
    int red = (int) Math.round((a >> 16 & 255) * (1 - t) + (b >> 16 & 255) * t);
    int green = (int) Math.round((a >> 8 & 255) * (1 - t) + (b >> 8 & 255) * t);
    int blue = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
    return red << 16 | green << 8 | blue;
  }

  private static int originalPalette(int color, double temperature, double rainfall) {
    int cold = originalMix(color, 0x63a9b6, Math.clamp((.5 - temperature) * 1.5, 0, .8));
    return originalMix(cold, 0x77724a, Math.clamp((temperature - .8) * rainfall * .65, 0, .7));
  }

  private static double originalWave(double x, double z, double ticks, double storm) {
    double p = ticks * .045;
    return (Math.sin(x * .095 + z * .031 - p)
            + .45 * Math.sin(z * .17 - x * .04 - p * 1.41)
            + .2 * Math.sin(x * .29 + z * .12 - p * 1.9)
            + .12 * Math.sin(x * .37 - z * .23 - p * 2.3)
            + .08 * Math.sin(x * .51 + z * .41 - p * 2.7)
            + .05 * Math.sin(z * .67 - x * .32 - p * 3.1)
            + .03 * Math.sin(x * .83 + z * .53 - p * 3.6)
            + .02 * Math.sin(z * 1.07 - x * .73 - p * 4.1))
        * (1.65 / 1.95)
        * (.10 + Math.clamp(storm, 0, 1) * .15);
  }

  public static void main(String[] args) {
    int color = 0x2a789e;
    for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      check(WaterDynamics.mix(color, 0xffffff, invalid) == color, "invalid blend changed source color: " + invalid);
      check(WaterDynamics.palette(color, invalid, 0.7) == color, "invalid temperature changed source color");
      check(WaterDynamics.palette(color, 0.5, invalid) == color, "invalid rainfall changed neutral source color");
      check(WaterDynamics.palette(color, 0.1, invalid) == color, "invalid rainfall changed cold source color");
      check(WaterDynamics.wave(invalid, 3, 8, .5) == 0, "invalid X displaced water");
      check(WaterDynamics.wave(3, invalid, 8, .5) == 0, "invalid Z displaced water");
      check(WaterDynamics.wave(3, 8, invalid, .5) == 0, "invalid time displaced water");
      check(WaterDynamics.wave(3, 8, 10, invalid) == 0, "invalid storm displaced water");
    }
    check(WaterDynamics.mix(0x123456, 0xffffff, 0) == 0x123456, "zero blend");
    check(WaterDynamics.mix(0x123456, 0xffffff, 1) == 0xffffff, "full blend");
    check(WaterDynamics.mix(0, 0xffffff, .5) == 0x808080, "rounded half blend");
    check(WaterDynamics.wave(Double.MAX_VALUE, Double.MAX_VALUE, 1, .5) == 0,
        "overflowing finite phase sums must not enter the renderer");

    Random random = new Random(0x151A20261101L);
    for (int i = 0; i < 300_000; i++) {
      int a = random.nextInt(), b = random.nextInt();
      double amount = random.nextDouble() * 4 - 1.5;
      check(WaterDynamics.mix(a, b, amount) == originalMix(a, b, amount), "finite blend parity " + i);
    }
    for (int i = 0; i < 200_000; i++) {
      int nativeColor = random.nextInt();
      double temperature = random.nextDouble() * 4 - 1.5;
      double rainfall = random.nextDouble() * 4 - 1.5;
      check(WaterDynamics.palette(nativeColor, temperature, rainfall)
          == originalPalette(nativeColor, temperature, rainfall), "finite palette parity " + i);
    }
    for (int i = 0; i < 100_000; i++) {
      double x = random.nextDouble() * 1_000_000 - 500_000;
      double z = random.nextDouble() * 1_000_000 - 500_000;
      double ticks = random.nextDouble() * 100_000;
      double storm = random.nextDouble() * 4 - 1.5;
      double expected = originalWave(x, z, ticks, storm);
      double actual = WaterDynamics.wave(x, z, ticks, storm);
      check(Double.doubleToRawLongBits(actual) == Double.doubleToRawLongBits(expected),
          "finite wave parity " + i);
      check(Double.isFinite(actual), "finite wave result " + i);
    }
    System.out.println("WATER_PRESENTATION_FINITE_PASS checks=" + checks
        + " blendParity=300000 paletteParity=200000 waveParity=100000 invalidCases=24");
  }

  private WaterDynamicsPresentationFiniteChecks() {}
}
