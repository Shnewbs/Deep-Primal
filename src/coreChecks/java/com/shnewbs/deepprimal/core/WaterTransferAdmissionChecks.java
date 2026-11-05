package com.shnewbs.deepprimal.core;

/** Bounded exhaustive water-accounting admission checks; no Minecraft classes required. */
public final class WaterTransferAdmissionChecks {
  public static void main(String[] args) {
    int checks = 0, accepted = 0;
    for (int from = 0; from <= WaterTransport.CAPACITY; from++)
      for (int to = 0; to <= WaterTransport.CAPACITY; to++)
        for (boolean down : new boolean[]{false, true}) {
          var e = WaterTransport.exchange(from, to, down);
          boolean moves = e.moved() > 0;
          check(WaterTransport.matches(e, from, to) == moves, "valid exchange admission"); checks++;
          if (moves) {
            accepted++;
            check(e.from() + e.to() == from + to, "exact volume conservation"); checks++;
            check(!WaterTransport.matches(e, (from + 1) % 17, to), "changed donor"); checks++;
            check(!WaterTransport.matches(e, from, (to + 1) % 17), "changed receiver"); checks++;
            check(!WaterTransport.matches(new WaterTransport.Exchange(e.from(), e.to(), e.moved()+1), from, to), "changed amount"); checks++;
          }
        }
    for (int from = 0; from <= 16; from++)
      for (int to = 0; to <= 16; to++)
        for (int elevation = -4; elevation <= 4; elevation++) {
          var e = WaterTransport.pressure(64 + elevation, from, 64, to);
          check(WaterTransport.matches(e, from, to) == (e.moved() > 0), "pressure admission"); checks++;
          if (e.moved() > 0) {
            accepted++;
            check(e.from() + e.to() == from + to, "pressure volume conservation"); checks++;
          }
        }
    for (var invalid : new WaterTransport.Exchange[]{
      new WaterTransport.Exchange(-1, 1, 1), new WaterTransport.Exchange(0, 17, 1),
      new WaterTransport.Exchange(0, 0, -1), new WaterTransport.Exchange(0, 0, 17),
      new WaterTransport.Exchange(1, 1, 0),
      new WaterTransport.Exchange(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)}) {
      check(!WaterTransport.matches(invalid, 16, 0), "malformed exchange rejection"); checks++;
    }
    check(!WaterTransport.matches(null, 1, 0), "null exchange rejection"); checks++;
    check(!WaterTransport.matches(new WaterTransport.Exchange(0, 1, 1), 17, 0), "invalid donor amount"); checks++;
    check(!WaterTransport.matches(new WaterTransport.Exchange(0, 1, 1), 1, -1), "invalid receiver amount"); checks++;
    System.out.println("WATER_TRANSFER_ADMISSION_PASS assertions=" + checks
        + " accepted=" + accepted + " zeroUnitsCreated=true");
  }
  private static void check(boolean ok, String what) {
    if (!ok) throw new AssertionError(what);
  }
  private WaterTransferAdmissionChecks() {}
}
