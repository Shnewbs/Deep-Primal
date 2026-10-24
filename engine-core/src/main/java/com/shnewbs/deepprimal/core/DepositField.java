package com.shnewbs.deepprimal.core;

/** Bounded neighboring deposit lookup for opt-in V5; never used for saved V1-V4. */
public final class DepositField {
  public static final int CANDIDATES = 9;
  private static final double[] NO_BODIES = new double[0];
  private static final int[] NO_TYPES = new int[0];
  private final TerrainNoise noise;
  public DepositField(TerrainNoise noise) { this.noise = java.util.Objects.requireNonNull(noise); }

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
   * Build once per column, then reuse for all heights; arrays contain at most324 raw bytes.
   * Empty columns reuse private zero-length arrays; metadata is allocated only on a hit. */
  public Column column(int x, int z, Geology.Column host) {
    java.util.Objects.requireNonNull(host);
    double[] bodies = NO_BODIES;
    int[] types = NO_TYPES;
    int gx = Math.floorDiv(x, 32), gz = Math.floorDiv(z, 32), i = 0, footprintMask = 0;
    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++, i++) {
      int cx = gx + dx, cz = gz + dz, at = i * 4;
      // Keep the R50 source-hash work/order; this change isolates array allocation.
      double offsetX = x - ((long) cx * 32 + 8 + noise.hash(cx, cz, 1810) * 16);
      double offsetZ = z - ((long) cz * 32 + 8 + noise.hash(cx, cz, 1811) * 16);
      double centerY = -96 + noise.hash(cx, cz, 1812) * 352;
      int type = (int) (noise.hash(cx, cz, 1813) * 8);
      double radius = 3 + noise.hash(cx, cz, 1814) * 5;
      double ax = offsetX / (radius * 1.7), az = offsetZ / radius;
      // Nonnegative vertical distance cannot make a rejected horizontal sum intersect.
      if (ax * ax + az * az > 1) continue;
      if (footprintMask == 0) {
        bodies = new double[CANDIDATES * 4];
        types = new int[CANDIDATES];
      }
      bodies[at] = offsetX;
      bodies[at + 1] = offsetZ;
      bodies[at + 2] = centerY;
      types[i] = type;
      bodies[at + 3] = radius;
      footprintMask |= 1 << i;
    }
    return new Column(host, bodies, types, footprintMask);
  }
}
