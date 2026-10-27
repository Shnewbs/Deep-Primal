package com.shnewbs.deepprimal.core.lod;

import java.util.HashSet;
import java.util.Set;

/** Self-contained LOD rediscovery contract checks; no game/GL/GPU claims. */
public final class DeliveredRetargetChecks {
  private static long key(int x, int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }
  private static void ok(boolean valid, String label) {
    if (!valid) throw new AssertionError(label);
  }
  public static void main(String[] args) {
    int checks = 0;
    for (int radius : new int[]{0,1,2,8,16,32,64,96,125}) {
      int cx = -177, cz = -31;
      DeliveredChunkScan scan = new DeliveredChunkScan(cx, cz, radius);
      ok(!scan.shouldRetarget(cx, cz, radius), "stationary:" + radius); checks++;
      ok(scan.shouldRetarget(cx, cz, radius == 125 ? 124 : radius + 1), "radius:" + radius); checks++;
      int threshold = Math.max(1, Math.min(8, radius / 8));
      if (threshold > 1) {
        ok(!scan.shouldRetarget(cx + threshold - 1, cz, radius), "below threshold:" + radius); checks++;
      }
      ok(scan.shouldRetarget(cx + threshold, cz, radius), "x threshold:" + radius); checks++;
      ok(scan.shouldRetarget(cx, cz - threshold, radius), "z threshold:" + radius); checks++;
      ok(scan.shouldRetarget(cx, cz, radius == 125 ? 124 : radius + 1), "radius independent of travel"); checks++;
      Set<Long> unique = new HashSet<>();
      while (!scan.done()) {
        int before = scan.visited();
        int admitted = scan.drain(128, (x,z) -> {
          ok(Math.max(Math.abs(x-cx),Math.abs(z-cz)) <= radius, "out of radius");
          ok(unique.add(key(x,z)), "duplicate");
          return true;
        });
        ok(admitted > 0 && admitted <= 128 && scan.visited()-before == admitted, "bounded scan"); checks++;
      }
      ok(unique.size() == scan.total(), "coverage:" + radius); checks++;
      ok(!scan.shouldRetarget(cx,cz,radius), "completed stationary"); checks++;
      ok(scan.shouldRetarget(cx+1,cz,radius), "completed move +1:" + radius); checks++;
      ok(scan.shouldRetarget(cx,cz-1,radius), "completed move -1:" + radius); checks++;
    }
    DeliveredChunkScan pressure = new DeliveredChunkScan(-2, 4, 32);
    ok(pressure.drain(128,(x,z)->false) == 0 && pressure.visited() == 0, "pressure retains position"); checks++;
    ok(!pressure.shouldRetarget(-1,4,32), "pressure small move bounded"); checks++;
    ok(pressure.shouldRetarget(2,4,32), "pressure larger move retarget"); checks++;
    DeliveredChunkScan edge = new DeliveredChunkScan(Integer.MAX_VALUE-125, Integer.MIN_VALUE+125, 125);
    ok(edge.shouldRetarget(Integer.MIN_VALUE+125,Integer.MAX_VALUE-125,125), "signed extreme movement"); checks++;
    ok(!edge.shouldRetarget(Integer.MAX_VALUE-125,Integer.MIN_VALUE+125,125), "signed stationary"); checks++;
    System.out.println("DELIVERED_RETARGET_PASS checks="+checks+" completeScans=9 geometryPreserved=true pressureSafe=true signedExtreme=true");
  }
}
