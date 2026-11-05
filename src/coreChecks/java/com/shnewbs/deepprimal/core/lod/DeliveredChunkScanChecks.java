package com.shnewbs.deepprimal.core.lod;

import java.util.*;

public final class DeliveredChunkScanChecks {
  private static long key(int x, int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }
  public static void main(String[] args) {
    int checks = 0, outer = 0;
    for (int radius : new int[]{0, 1, 2, 32, 33, 96, 125}) {
      int cx = -177, cz = -31;
      var scan = new DeliveredChunkScan(cx, cz, radius);
      Set<Long> found = new HashSet<>();
      int[] priorRing = {0};
      while (!scan.done()) {
        int before = scan.visited();
        int n = scan.drain(128, (x,z) -> {
          int ring = Math.max(Math.abs(x-cx),Math.abs(z-cz));
          if (ring < priorRing[0] || ring > radius || !found.add(key(x,z)))
            throw new AssertionError("Order, range or duplicate");
          priorRing[0] = ring;
          return true;
        });
        if (n < 1 || n > 128 || scan.visited() - before != n) throw new AssertionError("Work bound");
      }
      for (int z = cz-radius; z <= cz+radius; z++) for (int x = cx-radius; x <= cx+radius; x++) {
        if (!found.contains(key(x,z))) throw new AssertionError("Missed delivered coordinate");
        if (Math.max(Math.abs(x-cx),Math.abs(z-cz)) > 32) outer++;
        checks++;
      }
      if (found.size() != scan.total() || scan.drain(128,(x,z)->{throw new AssertionError("Restart");}) != 0)
        throw new AssertionError("Completion");
    }
    var paused = new DeliveredChunkScan(-9, 7, 2);
    long[] retry = {0};
    if (paused.drain(128,(x,z)->{retry[0]=key(x,z);return false;}) != 0 || paused.visited()!=0)
      throw new AssertionError("Pressure skipped coordinate");
    paused.drain(1,(x,z)->{if(key(x,z)!=retry[0])throw new AssertionError("Wrong retry");return true;});
    int before = paused.visited();
    if (paused.drain(0,(x,z)->{throw new AssertionError("Zero budget visited");})!=0 || paused.visited()!=before)
      throw new AssertionError("Zero budget");
    try { paused.drain(1,(x,z)->{throw new IllegalStateException("Failure control");}); }
    catch (IllegalStateException expected) {}
    if (paused.visited()!=before)throw new AssertionError("Failed callback advanced scan");
    for (int radius : new int[]{-1,126}) {
      try { new DeliveredChunkScan(0,0,radius);throw new AssertionError("Invalid radius accepted"); }
      catch (IllegalArgumentException expected) {}
    }
    try { new DeliveredChunkScan(Integer.MAX_VALUE,0,1);throw new AssertionError("Overflow accepted"); }
    catch (IllegalArgumentException expected) {}
    System.out.println("DELIVERED_SCAN_PASS coordinates="+checks+" outerBeyond32="+outer
        +" workBound=128 nearestFirst=true unique=true pauseRetry=true negativeCoordinates=true noChunkReferences=true");
  }
}
