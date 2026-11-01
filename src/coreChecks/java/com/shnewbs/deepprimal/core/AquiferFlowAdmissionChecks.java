package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Overflow-safe baseflow quotas and fail-closed invalid source-pressure admission. */
public final class AquiferFlowAdmissionChecks {
  private static long checks;
  private static void need(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message + " check=" + checks);
  }

  private static int referenceBaseflow(int rain) {
    long amount = Math.max(0, rain) * 4096L * 18_000 / 114_000 / 2 / WaterAccount.SCALE;
    return (int) Math.min(Integer.MAX_VALUE, Math.max(32L, amount));
  }

  private static int legacyDischarge(Aquifer aquifer, double datum, double surface,
      int receivingCapacity, int rate) {
    if (!aquifer.active() || aquifer.ground.water() <= aquifer.stop()
        || aquifer.head(datum) <= surface || receivingCapacity <= 0) return 0;
    int pressure = (int) Math.ceil((aquifer.head(datum) - surface) * 2);
    return (int) Math.min((aquifer.ground.water() - aquifer.stop()) / WaterAccount.SCALE,
        Math.min(receivingCapacity, Math.min(Math.max(0, rate), pressure)));
  }

  public static void main(String[] args) {
    need(Aquifer.baseflowUnits(64) == 80, "ordinary natural baseflow");
    need(Aquifer.baseflowUnits(0) == 32, "dry floor");
    need(Aquifer.baseflowUnits(-1) == 32, "negative input floor");
    need(Aquifer.baseflowUnits(4096) == 5173, "ordinary regional quota");
    need(Aquifer.baseflowUnits(Integer.MAX_VALUE) == Integer.MAX_VALUE,
        "large positive rain must not wrap into negative baseflow");
    int previous = 0;
    for (long value = 0; value <= Integer.MAX_VALUE; value += 4096L) {
      int actual = Aquifer.baseflowUnits((int) value);
      need(actual == referenceBaseflow((int) value), "saturating quota oracle at " + value);
      need(actual >= previous, "quota must be monotone at " + value);
      previous = actual;
    }
    for (int input : new int[] {Integer.MIN_VALUE, -100, -1, 0, 1, 64, 4096,
        1_000_000, 1_000_000_000, 1_600_000_000, 1_700_000_000,
        1_800_000_000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE}) {
      need(Aquifer.baseflowUnits(input) == referenceBaseflow(input),
          "baseflow extreme oracle at " + input);
    }

    var aquifer = new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY * 3 / 4, 27, 51),
        WaterAccount.Parcel.EMPTY, 30, true, 0, 30);
    var saved = aquifer.ground.snapshot();
    for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      for (double good : new double[] {-1000, 0, 1000}) {
        need(aquifer.dischargeUnits(bad, good, 16, 16) == 0,
            "invalid groundwater datum must not authorize flow");
        need(aquifer.dischargeUnits(good, bad, 16, 16) == 0,
            "invalid receiving waterline must not authorize flow");
      }
    }
    need(aquifer.ground.snapshot().equals(saved), "admission must never debit storage");

    var random = new Random(0x164A51F0L);
    for (int i = 0; i < 200_000; i++) {
      double datum = random.nextInt(10000) - 5000 + random.nextDouble();
      double surface = random.nextInt(10000) - 5000 + random.nextDouble();
      int capacity = random.nextInt(1024) - 12;
      int rate = random.nextInt(1024) - 12;
      need(aquifer.dischargeUnits(datum, surface, capacity, rate)
          == legacyDischarge(aquifer, datum, surface, capacity, rate),
          "finite discharge parity scenario=" + i);
    }
    for (double datum : new double[] {-Double.MAX_VALUE, -100, 0, 100, Double.MAX_VALUE}) {
      for (double surface : new double[] {-Double.MAX_VALUE, -100, 0, 100, Double.MAX_VALUE}) {
        need(aquifer.dischargeUnits(datum, surface, 16, 16)
            == legacyDischarge(aquifer, datum, surface, 16, 16),
            "finite extreme discharge parity");
      }
    }
    need(aquifer.ground.snapshot().equals(saved), "finite pressure queries changed storage");
    System.out.println("AQUIFER_FLOW_ADMISSION_PASS assertions=" + checks
        + " baseflowGrid=524288 finiteDischarges=200025 invalidHeightCases=18");
  }
  private AquiferFlowAdmissionChecks() {}
}
