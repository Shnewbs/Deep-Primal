package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.Random;

/** Exact-source transfer regression: previewed parcels must be debited once, without rounding drift. */
public final class WaterAccountDebitChecks {
  private static long assertions;
  private static void check(boolean ok, String label) {
    assertions++;
    if (!ok) throw new AssertionError(label + " at " + assertions);
  }
  private static long portion(long mass, long amount, long volume) {
    return amount == 0 ? 0 : BigInteger.valueOf(mass).multiply(BigInteger.valueOf(amount))
        .divide(BigInteger.valueOf(volume)).longValueExact();
  }
  public static void main(String[] args) {
    Random random = new Random(20820261102L);
    for (int i = 0; i < 25_000; i++) {
      long water = random.nextLong(WaterAccount.MAX + 1);
      long salt = random.nextLong(WaterAccount.MAX * 512 + 1);
      long sediment = random.nextLong(WaterAccount.MAX * 512 + 1);
      WaterAccount source = new WaterAccount(WaterAccount.MAX,
          new WaterAccount.Parcel(water, salt, sediment));
      long capA = random.nextLong(WaterAccount.MAX + 1);
      long capB = random.nextLong(WaterAccount.MAX + 1);
      long initialSaltA = i % 7 == 0 ? WaterAccount.MAX * 1024 : 0;
      long initialSedimentB = i % 11 == 0 ? WaterAccount.MAX * 1024 : 0;
      WaterAccount a = new WaterAccount(capA, new WaterAccount.Parcel(0, initialSaltA, 0));
      WaterAccount b = new WaterAccount(capB, new WaterAccount.Parcel(0, 0, initialSedimentB));
      long demandA = random.nextLong(WaterAccount.MAX + 1);
      long demandB = random.nextLong(WaterAccount.MAX + 1);
      long amount = Math.min(water, Math.min(demandA, capA));
      long total, first;
      if ((i & 1) == 0) {
        total = amount;
        first = amount;
      } else {
        long acceptedA = Math.min(demandA, capA), acceptedB = Math.min(demandB, capB);
        total = Math.min(water, acceptedA + acceptedB);
        first = total == 0 ? 0 : portion(total, acceptedA, acceptedA + acceptedB);
      }
      long massSalt = portion(salt, total, water);
      long massSediment = portion(sediment, total, water);
      long firstSalt = total == 0 ? 0 : portion(massSalt, first, total);
      long secondSediment = total == 0 ? 0 : massSediment - portion(massSediment, first, total);
      boolean admitted = initialSaltA + ((i & 1) == 0 ? massSalt : firstSalt)
          <= WaterAccount.MAX * 1024
          && ((i & 1) == 0 || initialSedimentB + secondSediment <= WaterAccount.MAX * 1024);
      long moved = (i & 1) == 0 ? WaterAccount.move(source, a, demandA)
          : WaterAccount.distribute(source, a, b, demandA, demandB);
      check(moved == (admitted ? total : 0), "Transfer admission");
      long paid = admitted ? total : 0;
      long paidSalt = admitted ? massSalt : 0;
      long paidSediment = admitted ? massSediment : 0;
      check(source.snapshot().equals(new WaterAccount.Parcel(
          water - paid, salt - paidSalt, sediment - paidSediment)), "Exact source debit");
      check(source.water() + a.water() + b.water() == water, "Water conservation");
      check(source.snapshot().salt() + a.snapshot().salt() + b.snapshot().salt()
          == salt + initialSaltA, "Salt conservation");
      check(source.snapshot().sediment() + a.snapshot().sediment() + b.snapshot().sediment()
          == sediment + initialSedimentB, "Sediment conservation");
      check(a.water() <= a.capacity() && b.water() <= b.capacity(), "Receiver capacity");
    }
    System.out.println("WATER_ACCOUNT_DEBIT_PASS assertions=" + assertions);
  }
  private WaterAccountDebitChecks() {}
}
