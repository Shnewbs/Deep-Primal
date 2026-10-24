package com.shnewbs.deepprimal.core;

final class BoatBuoyancyChecks {
  static void run() {
    int cases = 0;
    for (boolean engineWater : new boolean[] {false, true})
      for (boolean underwater : new boolean[] {false, true})
        for (boolean underFlowingWater : new boolean[] {false, true}) {
          boolean expected =
              engineWater && (underwater || underFlowingWater);
          need(
              EngineBoatBuoyancy.shouldUseSurfaceBuoyancy(
                  engineWater, underwater, underFlowingWater)
                  == expected,
              "engine-water buoyancy policy mismatch");
          cases++;
        }
    System.out.println(
        "BOAT_BUOYANCY_POLICY_PASS cases="
            + cases
            + " vanillaUnchanged=true engineWaterSubmersionRecovers=true");
  }

  private static void need(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
