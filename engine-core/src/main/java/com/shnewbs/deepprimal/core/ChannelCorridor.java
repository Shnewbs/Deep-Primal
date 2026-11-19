package com.shnewbs.deepprimal.core;

import java.util.function.DoubleBinaryOperator;

/** Fits the interior of a reach to sub-grid terrain while keeping its graph knots and tangents. */
final class ChannelCorridor {
  private static final double OFFSET_PENALTY = .025;

  record Curve(double ax, double az, double c1x, double c1z,
      double c2x, double c2z, double bx, double bz) {
    OrganicDrainage.Point at(double t) {
      double s = 1 - t;
      return new OrganicDrainage.Point(
          s * s * s * ax + 3 * s * s * t * c1x + 3 * s * t * t * c2x + t * t * t * bx,
          s * s * s * az + 3 * s * s * t * c1z + 3 * s * t * t * c2z + t * t * t * bz);
    }
  }

  record Fit(double nx, double nz, double quarter, double middle, double threeQuarter) {
    double offset(double t) {
      double left, right, leftTangent, rightTangent;
      int interval = Math.min(3, (int) (t * 4));
      switch (interval) {
        case 0 -> {
          left = 0;
          right = quarter;
          leftTangent = 0;
          rightTangent = tangent(quarter, middle - quarter);
        }
        case 1 -> {
          left = quarter;
          right = middle;
          leftTangent = tangent(quarter, middle - quarter);
          rightTangent = tangent(middle - quarter, threeQuarter - middle);
        }
        case 2 -> {
          left = middle;
          right = threeQuarter;
          leftTangent = tangent(middle - quarter, threeQuarter - middle);
          rightTangent = tangent(threeQuarter - middle, -threeQuarter);
        }
        default -> {
          left = threeQuarter;
          right = 0;
          leftTangent = tangent(threeQuarter - middle, -threeQuarter);
          rightTangent = 0;
        }
      }
      double u = t * 4 - interval, v = 1 - u;
      return left + (right - left) * u * u * (3 - 2 * u)
          + u * v * (v * leftTangent - u * rightTangent);
    }
  }

  static Fit fit(DoubleBinaryOperator height, Curve curve, double length) {
    double nx = (curve.az - curve.bz) / length, nz = (curve.bx - curve.ax) / length;
    double reach = Math.min(6, length * .16);
    return new Fit(nx, nz,
        station(height, curve.at(.25), nx, nz, reach),
        station(height, curve.at(.5), nx, nz, reach),
        station(height, curve.at(.75), nx, nz, reach));
  }

  private static double station(DoubleBinaryOperator height, OrganicDrainage.Point p,
      double nx, double nz, double reach) {
    double center = height.applyAsDouble(p.x(), p.z());
    double left = score(height, p, nx, nz, -reach);
    double right = score(height, p, nx, nz, reach);
    double best = center, offset = 0;
    // Equal flanks require a sampled interior minimum to justify picking a side.
    if (Math.abs(left - right) > .01 && Math.min(left, right) < center - .05) {
      best = Math.min(left, right);
      offset = left < right ? -reach : reach;
    }
    double curvature = (left - center) + (right - center);
    if (curvature > .01) {
      double candidate = (left - right) * .5 / curvature;
      if (Math.abs(candidate) < .95 && Math.abs(candidate) > .04) {
        double refined = candidate * reach;
        if (score(height, p, nx, nz, refined) < best - .05) offset = refined;
      }
    }
    return offset;
  }

  private static double score(DoubleBinaryOperator height, OrganicDrainage.Point p,
      double nx, double nz, double offset) {
    return height.applyAsDouble(p.x() + nx * offset, p.z() + nz * offset)
        + OFFSET_PENALTY * offset * offset;
  }

  // The harmonic mean keeps the interpolation within the measured station offsets.
  private static double tangent(double a, double b) {
    return a * b <= 0 ? 0 : 2 * a * b / (a + b);
  }

  private ChannelCorridor() {}
}
