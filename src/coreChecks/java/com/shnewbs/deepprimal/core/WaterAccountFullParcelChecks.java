package com.shnewbs.deepprimal.core;

import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.util.Random;

/** Full-parcel exactness and randomized water/chemistry conservation oracle. */
public final class WaterAccountFullParcelChecks {
  private static long assertions;
  private static long digest = 0x6a09e667f3bcc909L;
  private static void check(boolean ok, String why) {
    assertions++;
    if (!ok) throw new AssertionError(why + " #" + assertions);
  }
  private static long portion(long mass, long amount, long volume) {
    return amount == 0 ? 0 : BigInteger.valueOf(mass)
        .multiply(BigInteger.valueOf(amount)).divide(BigInteger.valueOf(volume)).longValueExact();
  }
  private static void record(long n) { digest = Long.rotateLeft(digest ^ n, 13) * 0x9e3779b97f4a7c15L; }
  public static void main(String[] args) {
    Random r = new Random(21420261102L);
    long maxMass = WaterAccount.MAX * 1024;
    for (int i = 0; i < 100_000; i++) {
      long water = r.nextLong(WaterAccount.MAX + 1);
      long salt = r.nextLong(maxMass + 1);
      long sediment = r.nextLong(maxMass + 1);
      long request = switch (i % 5) {
        case 0 -> water;
        case 1 -> WaterAccount.MAX;
        case 2 -> 0;
        case 3 -> r.nextLong(WaterAccount.MAX + 1);
        default -> -1;
      };
      WaterAccount account = new WaterAccount(WaterAccount.MAX,
          new WaterAccount.Parcel(water, salt, sediment));
      long expectedWater = Math.clamp(request, 0, water);
      var expected = new WaterAccount.Parcel(expectedWater,
          portion(salt, expectedWater, water), portion(sediment, expectedWater, water));
      var preview = account.preview(request);
      check(preview.equals(expected), "exact BigInteger fraction");
      check(account.snapshot().equals(new WaterAccount.Parcel(water, salt, sediment)),
          "preview cannot mutate");
      var taken = account.take(request);
      check(taken.equals(expected), "take uses preview");
      var remainder = account.snapshot();
      check(remainder.water() + taken.water() == water, "water conserved");
      check(remainder.salt() + taken.salt() == salt, "salt conserved");
      check(remainder.sediment() + taken.sediment() == sediment, "sediment conserved");
      record(taken.water()); record(taken.salt()); record(taken.sediment());
      record(remainder.water()); record(remainder.salt()); record(remainder.sediment());
    }
    // The largest legal full parcel must transfer without rounding or loss.
    var full = new WaterAccount.Parcel(WaterAccount.MAX, maxMass, maxMass);
    for (int i = 0; i < 25_000; i++) {
      WaterAccount from = new WaterAccount(WaterAccount.MAX, full);
      WaterAccount to = new WaterAccount(WaterAccount.MAX, WaterAccount.Parcel.EMPTY);
      check(from.preview(Long.MAX_VALUE).equals(full), "near-limit full preview");
      check(WaterAccount.move(from, to, Long.MAX_VALUE) == WaterAccount.MAX,
          "near-limit full transfer");
      check(from.snapshot().equals(WaterAccount.Parcel.EMPTY), "source fully debited");
      check(to.snapshot().equals(full), "destination fully credited");
    }
    System.out.println("WATER_FULL_PARCEL_PASS assertions=" + assertions
        + " digest=" + Long.toUnsignedString(digest, 16));
  }
  private WaterAccountFullParcelChecks() {}
}
