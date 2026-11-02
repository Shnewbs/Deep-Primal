package com.shnewbs.deepprimal.core.density;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Random;

/** Bitwise output replay for zero-gain octave elision; never changes a saved profile. */
public final class SeededNoiseZeroGainChecks {
  private static long checks;
  private static void equal(double expected, double actual, String context) {
    checks++;
    if (Double.doubleToRawLongBits(expected) != Double.doubleToRawLongBits(actual))
      throw new AssertionError(context + " expected=" + expected + " actual=" + actual);
  }

  public static void main(String[] args) throws Exception {
    var digest = MessageDigest.getInstance("SHA-256");
    var bytes = ByteBuffer.allocate(Long.BYTES);
    Random random = new Random(0x2e021220L);
    long[] seeds = {0, 1, -1, 20261102L, Long.MIN_VALUE, Long.MAX_VALUE};
    for (long seed : seeds) {
      var single = new SeededNoise3D(seed,
          new NoiseSpec(1.0 / 192, 1.0 / 96, 1.0 / 192, 1, 2, 0, seed));
      var eight = new SeededNoise3D(seed,
          new NoiseSpec(1.0 / 192, 1.0 / 96, 1.0 / 192, 8, 2, 0, seed));
      for (int i = 0; i < 15_000; i++) {
        double x = (random.nextDouble() * 2 - 1) * 32_000_000;
        double y = (random.nextDouble() * 2 - 1) * 32_000_000;
        double z = (random.nextDouble() * 2 - 1) * 32_000_000;
        double actual = eight.sample(x, y, z);
        equal(single.sample(x, y, z), actual, "zero gain scalar");
        if (!Double.isFinite(actual) || Math.abs(actual) > 1) throw new AssertionError("noise bounds");
        bytes.clear(); bytes.putLong(Double.doubleToRawLongBits(actual)); digest.update(bytes.array());
      }
      double[] bulk = new double[11 * 7 * 13 + 2];
      eight.fill(bulk, 1, 11, 7, 13, -32_000_000, -128, 31_999_900, 3, 8, 2);
      int index = 1;
      for (int z = 0; z < 13; z++) for (int y = 0; y < 7; y++) for (int x = 0; x < 11; x++) {
        double actual = bulk[index++];
        equal(single.sample(-32_000_000 + x * 3, -128 + y * 8, 31_999_900 + z * 2),
            actual, "zero gain bulk");
        bytes.clear(); bytes.putLong(Double.doubleToRawLongBits(actual)); digest.update(bytes.array());
      }
      if (bulk[0] != 0 || bulk[bulk.length - 1] != 0) throw new AssertionError("output guard");
    }
    // Nonzero gain is covered by the existing NoiseCoreChecks fingerprint; also
    // fingerprint the ordinary multioctave and underflow-weight edge profiles.
    for (double gain : new double[] {Double.MIN_VALUE, 1e-300, .5, 1}) {
      var noise = new SeededNoise3D(345, new NoiseSpec(1.0 / 512, 1.0 / 256, 1.0 / 512,
          8, 2, gain, 987654321));
      for (int i = 0; i < 3_000; i++) {
        double actual = noise.sample((i - 1500) * 201.125, (i % 47) - 23, (1500 - i) * 173.5);
        if (!Double.isFinite(actual) || Math.abs(actual) > 1) throw new AssertionError("ordinary noise bounds");
        bytes.clear(); bytes.putLong(Double.doubleToRawLongBits(actual)); digest.update(bytes.array());
        checks++;
      }
    }
    System.out.println("ZERO_GAIN_PASS checks=" + checks + " digest="
        + HexFormat.of().formatHex(digest.digest()));
  }
  private SeededNoiseZeroGainChecks() {}
}
