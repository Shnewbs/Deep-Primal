package com.shnewbs.deepprimal.core;

/** Saved channel revision 11. Terrain/catchment geometry, not an authoritative runtime ledger. */
public final class HydraulicChannel {
  public record Section(double radius, double depth, double speed, double bend) {}

  /** Rain-weighted drainage area is a discharge proxy; steep reaches are more confined. */
  public static Section section(double area, double grade, double bend) {
    if (!Double.isFinite(area) || area < 0 || !Double.isFinite(grade) || grade < 0
        || !Double.isFinite(bend)) throw new IllegalArgumentException("Channel geometry inputs");
    double q = Math.max(1, area / 18_000.0), slope = Math.sqrt(Math.min(grade, 4));
    double radius = Math.clamp(.8 + 1.1 * Math.sqrt(q) / (1 + 1.5 * slope), 1.1, 96);
    double depth = Math.clamp(.65 + Math.pow(q, .32) / (1 + 2 * slope), .8, 24);
    double turn = Math.clamp(bend, -1, 1);
    depth *= 1 + .25 * Math.abs(turn);
    // Manning-style velocity proxy. Saved presentation speed remains in the existing 0..1 range.
    double hydraulicRadius = 2 * radius * depth / (2 * radius + 2 * depth);
    double velocity = Math.pow(hydraulicRadius, 2.0 / 3) * Math.sqrt(Math.clamp(grade, .00015, .25)) / .045;
    return new Section(radius, depth, Math.clamp(velocity / 6, .08, .85), turn);
  }

  /** Positive turn is leftward; the deeper thalweg shifts to the right (outer) bank. */
  public static double bedDepth(double depth, double lateral, double bend) {
    if (!Double.isFinite(depth) || depth < 0 || !Double.isFinite(lateral)
        || !Double.isFinite(bend)) throw new IllegalArgumentException("Channel section inputs");
    double center = -.35 * Math.clamp(bend, -1, 1), d = Math.clamp(lateral, -1, 1) - center;
    double edge = Math.abs(d / (d < 0 ? 1 + center : 1 - center));
    return .12 + Math.max(0, depth - .12) * (1 - Math.pow(Math.clamp(edge, 0, 1), 2.5));
  }

  public static double bend(double inX, double inZ, double outX, double outZ) {
    if (!Double.isFinite(inX) || !Double.isFinite(inZ) || !Double.isFinite(outX) || !Double.isFinite(outZ))
      throw new IllegalArgumentException("Channel tangent inputs");
    double length = Math.hypot(inX, inZ) * Math.hypot(outX, outZ);
    return length < 1e-9 ? 0 : Math.clamp((inX * outZ - inZ * outX) / length, -1, 1);
  }

  /** One normalized bisector per graph knot, reused by every incoming/outgoing cubic. */
  public static OrganicDrainage.Point tangent(double inX, double inZ, double outX, double outZ) {
    if (!Double.isFinite(inX) || !Double.isFinite(inZ) || !Double.isFinite(outX) || !Double.isFinite(outZ))
      throw new IllegalArgumentException("Channel tangent inputs");
    double a=Math.hypot(inX,inZ), b=Math.hypot(outX,outZ);
    double x=(a<1e-9?0:inX/a)+(b<1e-9?0:outX/b);
    double z=(a<1e-9?0:inZ/a)+(b<1e-9?0:outZ/b);
    double length=Math.hypot(x,z);
    if(length<1e-9)return b<1e-9?new OrganicDrainage.Point(0,0):new OrganicDrainage.Point(outX/b,outZ/b);
    return new OrganicDrainage.Point(x/length,z/length);
  }
  private HydraulicChannel() {}
}
