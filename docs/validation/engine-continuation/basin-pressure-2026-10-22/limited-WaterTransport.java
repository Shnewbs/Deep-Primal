package com.shnewbs.deepprimal.core;

/** Integer volume is authoritative; a cell contains sixteen 1/16-block units. */
public final class WaterTransport {
  public static final int CAPACITY = 16;

  public record Exchange(int from, int to, int moved) {}

  public static Exchange exchange(int from, int to, boolean down) {
    if (from < 0 || from > 16 || to < 0 || to > 16)
      throw new IllegalArgumentException("Water capacity");
    int moved = Math.min(from, 16 - to);
    if (!down) moved = Math.min(moved, Math.max(0, (from - to) / 2));
    return new Exchange(from - moved, to + moved, moved);
  }

  /** Communicating surfaces use elevation plus volume as hydraulic head. */
  public static Exchange pressure(int fromY, int from, int toY, int to) {
    return pressure(fromY, from, toY, to, CAPACITY);
  }

  /** Apply a caller's transfer quantum before constructing the conserved result. */
  public static Exchange pressure(int fromY, int from, int toY, int to, int maximum) {
    if (from < 0 || from > CAPACITY || to < 0 || to > CAPACITY)
      throw new IllegalArgumentException("Water capacity");
    if (maximum < 0) throw new IllegalArgumentException("Negative pressure transfer limit");
    long difference = ((long) fromY - toY) * CAPACITY + from - to;
    int moved =
        (int) Math.max(0, Math.min(maximum,
            Math.min(Math.min(from, CAPACITY - to), difference / 2)));
    return new Exchange(from - moved, to + moved, moved);
  }

  /** Shared global corners make independently rebuilt neighboring cells meet exactly. */
  public static double surface(
      double nw, double ne, double sw, double se, double center, double u, double v) {
    u = Math.clamp(u, 0, 1);
    v = Math.clamp(v, 0, 1);
    // Volume belongs to transport; per-cell center corrections create repeated bowls/tents.
    // Corner heights may cross the cell's vertical boundary. Clipping each layer separately
    // gives two different world heights at the very same shared edge.
    return TerrainNoise.lerp(TerrainNoise.lerp(nw, ne, u), TerrainNoise.lerp(sw, se, u), v);
  }

  /** Nearby connected surface height relative to this Y, or NaN for a dry column. */
  public static double surfaceColumn(int below, int here, int above) {
    if (here > 0) return above > 0 ? 1 + above / 16.0 : here / 16.0;
    return below > 0 ? -1 + below / 16.0 : Double.NaN;
  }

  private WaterTransport() {}
}
