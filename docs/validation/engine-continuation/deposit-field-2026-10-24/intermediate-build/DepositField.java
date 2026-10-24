package com.shnewbs.deepprimal.core;

/** Bounded neighboring deposit lookup for a future saved terrain revision, not wired into V1-V4. */
public final class DepositField {
  public static final int CANDIDATES = 9;
  private final TerrainNoise noise;
  public DepositField(TerrainNoise noise) { this.noise = java.util.Objects.requireNonNull(noise); }

  public static final class Column {
    private final Geology.Column host;
    private final double[] bodies = new double[CANDIDATES * 4];
    private final int[] types = new int[CANDIDATES];
    private Column(Geology.Column host) { this.host = java.util.Objects.requireNonNull(host); }
    public int candidateCount() { return CANDIDATES; }
    /** No allocation or world/chunk lookup. Overlap priority is absolute grid X then Z. */
    public Geology.Ore ore(int y, int bed) {
      for (int i = 0; i < CANDIDATES; i++) {
        int at = i * 4;
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
    var out = new Column(host);
    int gx = Math.floorDiv(x, 32), gz = Math.floorDiv(z, 32), i = 0;
    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++, i++) {
      int cx = gx + dx, cz = gz + dz, at = i * 4;
      out.bodies[at] = x - ((long) cx * 32 + 8 + noise.hash(cx, cz, 1810) * 16);
      out.bodies[at + 1] = z - ((long) cz * 32 + 8 + noise.hash(cx, cz, 1811) * 16);
      out.bodies[at + 2] = -96 + noise.hash(cx, cz, 1812) * 352;
      out.types[i] = (int) (noise.hash(cx, cz, 1813) * 8);
      out.bodies[at + 3] = 3 + noise.hash(cx, cz, 1814) * 5;
    }
    return out;
  }
}
