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
    if (from < 0 || from > CAPACITY || to < 0 || to > CAPACITY)
      throw new IllegalArgumentException("Water capacity");
    long difference = ((long) fromY - toY) * CAPACITY + from - to;
    int moved = (int) Math.max(0, Math.min(Math.min(from, CAPACITY - to), difference / 2));
    return new Exchange(from - moved, to + moved, moved);
  }

  /**
   * Admit a proposed integer-volume exchange only if its snapshots match the two
   * loaded native cells immediately before writing. This rejects stale work,
   * malformed adapters and transfers that would implicitly create/destroy water.
   * Chemistry is conserved separately by WaterAccount and never changes this volume.
   */
  public static boolean matches(Exchange proposal, int beforeFrom, int beforeTo) {
    if (proposal == null || beforeFrom < 0 || beforeFrom > CAPACITY
        || beforeTo < 0 || beforeTo > CAPACITY) return false;
    int moved = proposal.moved();
    if (moved <= 0 || moved > CAPACITY || proposal.from() < 0
        || proposal.to() > CAPACITY) return false;
    return proposal.from() + moved == beforeFrom
        && proposal.to() - moved == beforeTo;
  }

  /** Preserve legacy rounding for ordinary corners; finite extremes can overflow b-a. */
  private static double surfaceLerp(double a, double b, double t) {
    double value = TerrainNoise.lerp(a, b, t);
    if (!Double.isFinite(a) || !Double.isFinite(b)) return value;
    // At exact tile edges, cancellation of huge finite heights can produce a
    // wrong *finite* result. Endpoints must be exact for shared-page continuity.
    if (t == 0.0) return a;
    if (t == 1.0) return b;
    // Ordinary interpolation retains the original arithmetic and rounding.
    // Only overflow or an out-of-hull result uses the stable convex form.
    if (Double.isFinite(value) && value >= Math.min(a, b) && value <= Math.max(a, b))
      return value;
    return a * (1.0 - t) + b * t;
  }

  /** Shared global corners make independently rebuilt neighboring cells meet exactly. */
  public static double surface(
      double nw, double ne, double sw, double se, double center, double u, double v) {
    // A NaN UV can arise from malformed mesh coordinates. Math.clamp preserves
    // NaN, which would otherwise poison every interpolated vertex height.
    // Use the cell midpoint for undefined UVs; retain normal edge clamping.
    u = Double.isNaN(u) ? 0.5 : Math.clamp(u, 0, 1);
    v = Double.isNaN(v) ? 0.5 : Math.clamp(v, 0, 1);
    // Volume belongs to transport; per-cell center corrections create repeated bowls/tents.
    // Corner heights may cross the cell's vertical boundary. Clipping each layer separately
    // gives two different world heights at the very same shared edge.
    return surfaceLerp(surfaceLerp(nw, ne, u), surfaceLerp(sw, se, u), v);
  }

  /** Nearby connected surface height relative to this Y, or NaN for a dry column. */
  public static double surfaceColumn(int below, int here, int above) {
    if (here > 0) return above > 0 ? 1 + above / 16.0 : here / 16.0;
    return below > 0 ? -1 + below / 16.0 : Double.NaN;
  }

  /** A ledge's solid cell still contributes the water film resting immediately above it. */
  public static double surfaceColumn(int below, int here, int above, boolean solidHere) {
    return here == 0 && solidHere && above > 0 ? 1 + above / 16.0
        : surfaceColumn(below, here, above);
  }

  /** The lower surface shares the upper film's floor constraint in world coordinates. */
  public static double supportedSurface(double average, boolean supportedFilm, boolean supportedAbove) {
    return supportedAbove ? Math.max(1 + 1.0 / 256, average)
        : supportedSurface(average, supportedFilm);
  }

  /** Rendering only: a shared thin-water corner must remain above its solid floor. */
  public static double supportedSurface(double average, boolean supportedFilm) {
    return supportedFilm ? Math.max(1.0/256,average) : average;
  }

  private WaterTransport() {}
}
