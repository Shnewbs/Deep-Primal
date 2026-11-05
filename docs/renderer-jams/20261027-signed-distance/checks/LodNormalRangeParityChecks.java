package com.shnewbs.deepprimal.lod.client;
import java.util.Random;
/** World-border radius parity with earlier int arithmetic; outside the mod payload. */
public final class LodNormalRangeParityChecks {
  public static void main(String[] args) {
    Random r = new Random(20261027);
    for (int i=0; i<1_000_000; i++) {
      int x=r.nextInt(-1_875_000,1_875_001), z=r.nextInt(-1_875_000,1_875_001);
      int cx=r.nextInt(-1_875_000,1_875_001), cz=r.nextInt(-1_875_000,1_875_001);
      long dx=x-cx, dz=z-cz;
      long oldRing=Math.max(Math.abs(x-cx),Math.abs(z-cz));
      long oldSquared=dx*dx+dz*dz;
      if (oldRing!=LodChunkDistance.chebyshev(x,z,cx,cz)
          || (double)oldSquared!=LodChunkDistance.squared(x,z,cx,cz))
        throw new AssertionError("World-border behavior changed at "+i);
    }
    System.out.println("LOD_NORMAL_RANGE_PARITY_PASS comparisons=1000000 worldBorderChunks=1875000");
  }
}
