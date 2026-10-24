package com.shnewbs.deepprimal.core;

/** Bounded neighboring deposit lookup for opt-in V5; never used for saved V1-V4. */
public final class R50DepositField {
  public static final int CANDIDATES = 9;
  private final TerrainNoise noise;
  public R50DepositField(TerrainNoise noise) { this.noise = java.util.Objects.requireNonNull(noise); }

  public static final class Column {
    private final Geology.Column host;
    private final double[] bodies;
    private final int[] types;
    private final int footprintMask;
    private Column(Geology.Column host, double[] bodies, int[] types, int footprintMask) {
      this.host = java.util.Objects.requireNonNull(host);
      this.bodies = bodies;
      this.types = types;
      this.footprintMask = footprintMask;
    }
    public int candidateCount() { return CANDIDATES; }
    public int activeCandidateCount() { return Integer.bitCount(footprintMask); }
    /** No allocation or world/chunk lookup. Overlap priority is absolute grid X then Z. */
    public Geology.Ore ore(int y, int bed) {
      for (int mask = footprintMask; mask != 0; mask &= mask - 1) {
        int i = Integer.numberOfTrailingZeros(mask), at = i * 4;
        var ore = host.oreFrom(bodies[at], bodies[at + 1], bodies[at + 2], types[i], bodies[at + 3], y, bed);
        if (ore != Geology.Ore.NONE) return ore;
      }
      return Geology.Ore.NONE;
    }
  }

  /** Nine regional candidates are sufficient: max horizontal semiaxes13.6/8 < cell width32.
   * Centers lie8..24 inside each cell. No body can reach a cell two steps away.
   * Build once per column, then reuse for all heights; raw arrays contain324 bytes. */
  public Column column(int x, int z, Geology.Column host) {
    var bodies = new double[CANDIDATES * 4];
    var types = new int[CANDIDATES];
    int gx = Math.floorDiv(x, 32), gz = Math.floorDiv(z, 32), i = 0, footprintMask = 0;
    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++, i++) {
      int cx = gx + dx, cz = gz + dz, at = i * 4;
      bodies[at] = x - ((long) cx * 32 + 8 + noise.hash(cx, cz, 1810) * 16);
      bodies[at + 1] = z - ((long) cz * 32 + 8 + noise.hash(cx, cz, 1811) * 16);
      bodies[at + 2] = -96 + noise.hash(cx, cz, 1812) * 352;
      types[i] = (int) (noise.hash(cx, cz, 1813) * 8);
      bodies[at + 3] = 3 + noise.hash(cx, cz, 1814) * 5;
      double ax = bodies[at] / (bodies[at + 3] * 1.7), az = bodies[at + 1] / bodies[at + 3];
      // A nonnegative vertical term cannot reduce this horizontal sum.
      // Retain the original oreFrom arithmetic/order for every surviving source.
      if (ax * ax + az * az <= 1) footprintMask |= 1 << i;
    }
    return new Column(host, bodies, types, footprintMask);
  }
}
