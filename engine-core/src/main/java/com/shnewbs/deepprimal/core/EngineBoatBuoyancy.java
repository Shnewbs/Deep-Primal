package com.shnewbs.deepprimal.core;

public final class EngineBoatBuoyancy {
  public static boolean shouldUseSurfaceBuoyancy(
      boolean engineWater, boolean underwater, boolean underFlowingWater) {
    return engineWater && (underwater || underFlowingWater);
  }

  private EngineBoatBuoyancy() {}
}
