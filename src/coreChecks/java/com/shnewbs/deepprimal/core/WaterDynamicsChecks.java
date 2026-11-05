package com.shnewbs.deepprimal.core;

final class WaterDynamicsChecks {
  static void run() {
    if (WaterDynamics.speed(.25, 100, 8) <= WaterDynamics.speed(0, 100, 8))
      throw new AssertionError("Slope does not accelerate flow");
    if (WaterDynamics.mix(0x000000, 0xffffff, .5) != 0x808080)
      throw new AssertionError("Rounded RGB blend");
    if (WaterDynamics.band(1) != 1 || WaterDynamics.band(.35) != 0 || WaterDynamics.band(1.9) != 3)
      throw new AssertionError("Old-save speed default");
    for (int floor : new int[] {-128, -384})
      for (int i = 0; i < 10000; i++) {
        double continent = -1 + i / 5000.0, noise = Math.sin(i * .1), base = 112 + 220 * continent;
        double h = WaterDynamics.oceanHeight(base, continent, noise, floor);
        if (h < floor + 40 || h > Math.max(base, floor + 40))
          throw new AssertionError("Ocean relief bounds");
        if (continent >= -.25 && h != base)
          throw new AssertionError("Land changed by offshore relief");
        double speed = WaterDynamics.speed(i / 1000.0, i, i / 10.0);
        if (!Double.isFinite(speed) || speed < .35 || speed > 1.9)
          throw new AssertionError("Flow bounds");
        if (Math.abs(WaterDynamics.wave(i, -i, i, 1)) > .413)
          throw new AssertionError("Wave amplitude");
      }
    if (HorizonMesh.gpuBytes(0) > 256 * 1024)
      throw new AssertionError("Fine terrain exceeds minimum upload budget");
    System.out.println(
        "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload");
  }

  private WaterDynamicsChecks() {}
}
